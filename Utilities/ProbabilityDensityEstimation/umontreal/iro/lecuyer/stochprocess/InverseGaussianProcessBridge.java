
/* 
 * The conditional bridge distribution is known \cite{fWEB03a},
 * but is not integrable, therefore there can be no bridge method
 * using simply inversion, with a single 
 * \externalclass{umontreal.iro.lecuyer.rng}{RandomStream}.
 */

/*
 * Class:        InverseGaussianProcessBridge
 * Description:  
 * Environment:  Java
 * Software:     SSJ 
 * Copyright (C) 2001  Pierre L'Ecuyer and Universite de Montreal
 * Organization: DIRO, Universite de Montreal
 * @author       
 * @since

 * SSJ is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or
 * any later version.

 * SSJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * A copy of the GNU General Public License is available at
   <a href="http://www.gnu.org/licenses">GPL licence site</a>.
 */

package umontreal.iro.lecuyer.stochprocess;
import umontreal.iro.lecuyer.rng.*;
import umontreal.iro.lecuyer.probdist.*;
import umontreal.iro.lecuyer.randvar.*;



/**
 * Samples the path by bridge sampling: 
 * first finding the process value at
 * the final time and then the middle time, etc.
 * The method <TT>nextObservation()</TT> returns the path value
 * in that non-sequential order. 
 * This class uses two 
 * {@link RandomStream RandomStream}'s to generate
 * a path.
 * 
 */
public class InverseGaussianProcessBridge extends InverseGaussianProcessMSH  {

    // Careful: mu and lambda are completely different from parent class.
    protected double[] imu2;
    protected double[] imuLambdaZ;
    protected double[] imuOver2LambdaZ;

    protected int[] wIndexList;
    protected int   bridgeCounter = -1; // Before 1st observ




   /**
    * Constructs a new <TT>InverseGaussianProcessBridge</TT>.
    * The initial value <TT>s0</TT> will be overridden by <SPAN CLASS="MATH"><I>t</I>[0]</SPAN> when
    * the observation times are set.
    * 
    */
   public InverseGaussianProcessBridge (double s0, double delta, 
                                        double gamma, RandomStream stream,
                                        RandomStream otherStream)  {
        super(s0, delta, gamma, stream, otherStream);
        numberOfRandomStreams = 2;
    }


   /**
    * Generates the path.  The two inner 
    * {@link RandomStream RandomStream}'s
    * are sampled alternatively.
    * 
    */
   public double[] generatePath()  {
        bridgeCounter    = -1;
        observationIndex =  0;
        path[0]          = x0;
        for (int j = 0; j < d; j++)   nextObservation();
        return path;
    }


   /**
    * Instead of using the internal streams to generate the path,
    * it uses two arrays of uniforms <SPAN CLASS="MATH"><I>U</I>[0, 1)</SPAN>.  The length of the arrays <TT>unifNorm</TT> 
    *  and <TT>unifOther</TT> should be equal to the number of time steps, excluding <SPAN CLASS="MATH"><I>t</I><SUB>0</SUB></SPAN>.
    * 
    */
   public double[] generatePath (double[] unifNorm, double[] unifOther)  {
        double s = x0;
        RandomStream cacheStreamNormal = stream;
        RandomStream cacheStreamOther  = otherStream;
        stream = new NonRandomStream(unifNorm);
        normalGen.setStream(stream);
        otherStream = new NonRandomStream(unifOther);

        bridgeCounter    = -1;
        observationIndex =  0;
        path[0]          = x0;
        for (int j = 0; j < d; j++)   nextObservation();

        stream = cacheStreamNormal;
        normalGen.setStream(stream);
        otherStream = cacheStreamOther;
        return path;
    }


   /**
    * Returns the next observation in the bridge order,
    * not the sequential order.
    * 
    */
   public double nextObservation()  {
        double s;
        if (bridgeCounter == -1) 
        {
            double temp = delta * (t[d] - t[0]);
            s = x0 + InverseGaussianMSHGen.nextDouble(otherStream,
                                                normalGen, temp/gamma, temp*temp);
            bridgeCounter    = 0;
            observationIndex = d;
        }
        else
        {
            int j = bridgeCounter * 3;
            int oldIndexL = wIndexList[j];
            int newIndex  = wIndexList[j + 1];
            int oldIndexR = wIndexList[j + 2];

            // Use the fact that \chi^2_1 is equivalent to a normal squared.
            double q = normalGen.nextDouble();
            q *= q;

            double z        = path[oldIndexR];
            double mu       = imu[newIndex];
            double muLambda = imuLambdaZ[newIndex]/z;
            double mu2      = imu2[newIndex];
            double muOver2Lambda = imuOver2LambdaZ[newIndex]*z;

            double root = mu + muOver2Lambda*mu*q -
            muOver2Lambda*Math.sqrt(4.*muLambda*q + mu2*q*q);
            double probabilityRoot1 = mu*(1.+root)/(1.+mu)/(root+mu);
            // Check if reject first root for 2nd one: root2=mu^2/root1.
            if (otherStream.nextDouble() > probabilityRoot1)
                root = mu2/root;
            s = path[oldIndexL] + (path[oldIndexR] - path[oldIndexL]) /(1.0 + root);
            bridgeCounter++;
            observationIndex = newIndex;
        }
        observationCounter = bridgeCounter + 1;
        path[observationIndex] = s;
        return s;
    }


   public void resetStartProcess () {
        observationIndex   = 0;
        observationCounter = 0;
        bridgeCounter = -1;
    }


    protected void init () {
        // imu[] etc. in super.init() will be overriden here.
        // Necessary nonetheless.
        super.init();

        double tauX;
        double tauY;
        double mu;
        double lambdaZ;
        if (observationTimesSet) {
            wIndexList  = new int[3*d];

            int[] ptIndex = new int[d+1];
            int   indexCounter = 0;
            int   newIndex, oldIndexL, oldIndexR;

            ptIndex[0] = 0;
            ptIndex[1] = d;

            // Careful: mu and lambda are completely different from parent class.
            imu            = new double[d+1];
            ilam           = new double[d+1];
            imu2           = new double[d+1];
            imuLambdaZ     = new double[d+1];
            imuOver2LambdaZ= new double[d+1];

            for (int powOfTwo = 1; powOfTwo <= d/2; powOfTwo *= 2) {
                /* Make room in the indexing array "ptIndex" */
                for (int j = powOfTwo; j >= 1; j--) ptIndex[2*j] = ptIndex[j];

                /* Insert new indices and Calculate constants */
                for (int j = 1; j <= powOfTwo; j++) {
                    oldIndexL = 2*j - 2;
                    oldIndexR = 2*j;
                    newIndex  = (int) (0.5*(ptIndex[oldIndexL] + ptIndex[oldIndexR]));

                    tauX      = t[newIndex] - t[ptIndex[oldIndexL]];
                    tauY      = t[ptIndex[oldIndexR]] - t[newIndex];
                    mu        = tauY/tauX;
                    lambdaZ   = delta*delta*tauY*tauY;
                    imu[newIndex]    = mu;
                    ilam[newIndex]   = lambdaZ;
                    imu2[newIndex]   = mu*mu;
                    imuLambdaZ[newIndex]      = mu*lambdaZ;
                    imuOver2LambdaZ[newIndex] = mu/2./lambdaZ;

                    ptIndex[oldIndexL + 1]       = newIndex;
                    wIndexList[indexCounter]   = ptIndex[oldIndexL];
                    wIndexList[indexCounter+1] = newIndex;
                    wIndexList[indexCounter+2] = ptIndex[oldIndexR];

                    indexCounter += 3;
                }
            }

            /* Check if there are holes remaining and fill them */
            for (int k = 1; k < d; k++) {
                if (ptIndex[k-1] + 1 < ptIndex[k]) {
                // there is a hole between (k-1) and k.

                    tauX      = t[ptIndex[k-1]+1] - t[ptIndex[k-1]];
                    tauY      = t[ptIndex[k]]     - t[ptIndex[k-1]+1];
                    mu        = tauY/tauX;
                    lambdaZ   = delta*delta*tauY*tauY;
                    imu[ptIndex[k-1]+1]    = mu;
                    ilam[ptIndex[k-1]+1]   = lambdaZ;
                    imu2[ptIndex[k-1]+1]   = mu*mu;
                    imuLambdaZ[ptIndex[k-1]+1]      = mu*lambdaZ;
                    imuOver2LambdaZ[ptIndex[k-1]+1] = mu/2./lambdaZ;

                    wIndexList[indexCounter]   = ptIndex[k]-2;
                    wIndexList[indexCounter+1] = ptIndex[k]-1;
                    wIndexList[indexCounter+2] = ptIndex[k];
                    indexCounter += 3;
                }
            }
        }
    }

   /**
    * Only returns a stream if both inner streams are the same.
    * 
    */
   public RandomStream getStream()  {
        if( stream != otherStream)
            throw new IllegalStateException("Two different streams or more are present");
        return stream;
    }


   /**
    * Sets the streams.
    * 
    */
   public void setStream (RandomStream stream, RandomStream otherStream)  {
        this.stream = stream;
        this.otherStream = otherStream;
        normalGen.setStream(stream);
    }


   /**
    * Sets both inner streams to the same <TT>stream</TT>.
    * 
    */
   public void setStream (RandomStream stream)  {
        setStream(stream, stream);
    }


}

