

/*
 * Class:        InverseGaussianProcessMSH
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
 * Uses a faster generating method (MSH) 
 * than the simple inversion of the distribution function
 * used by  {@link InverseGaussianProcess}.  
 * It is about 60 times faster.
 * However it requires two 
 * {@link RandomStream RandomStream}'s instead
 * of only one for {@link InverseGaussianProcess}.
 * The second stream is called <TT>otherStream</TT> below and
 * it is used to randomly choose between two roots at each time step.
 * 
 */
public class InverseGaussianProcessMSH extends InverseGaussianProcess  {

    // otherStream is used to decide between the two roots in method MSH.
    protected RandomStream otherStream;
    // Needed for the MSH method of generating inverse gaussian.
    protected NormalGen normalGen;



   /**
    * Constructs a new <TT>InverseGaussianProcessMSH</TT>.
    * The initial value <TT>s0</TT> will be overridden by <SPAN CLASS="MATH"><I>t</I>[0]</SPAN> when
    * the observation times are set.
    * 
    */
   public InverseGaussianProcessMSH (double s0, double delta, double gamma,
                                     RandomStream stream, 
                                     RandomStream otherStream)  {
        super(); // dummy
        this.x0 = s0;
        setParams(delta, gamma);
        this.stream = stream;
        this.otherStream = otherStream;
        normalGen = new NormalGen(stream); 
        numberOfRandomStreams = 2;
    }


   /**
    * Generates the path.  It is done by successively calling
    * <TT>nextObservation()</TT>, therefore the two
    * {@link RandomStream RandomStream}s are
    * sampled alternatively.
    * 
    */
   public double[] generatePath()  {
        double s = x0;
        for (int i = 0; i < d; i++) 
        {
            s += InverseGaussianMSHGen.nextDouble(otherStream,
                                                normalGen, imu[i], ilam[i]);
            path[i+1] = s;
        }
        observationIndex   = d;
        observationCounter = d;
        return path;
    }


   /**
    * Instead of using the internal streams to generate the path,
    * uses two arrays of uniforms <SPAN CLASS="MATH"><I>U</I>[0, 1)</SPAN>.  The length of the arrays should be
    * equal to the number of periods in the observation
    * times. This method is useful for {@link NormalInverseGaussianProcess}.
    * 
    */
   public double[] generatePath (double[] unifNorm, double[] unifOther)  {
        double s = x0;
        // The class NonRandomStream is defined below.
        RandomStream nonRandOther = new NonRandomStream(unifOther);
        // this.stream should keep in memory the original stream of the Normal.
        normalGen.setStream(new NonRandomStream(unifNorm));
        for (int i = 0; i < d; i++) {
            s += InverseGaussianMSHGen.nextDouble(nonRandOther,
                                                normalGen, imu[i], ilam[i]);
            path[i+1] = s;
        }
        observationIndex   = d;
        observationCounter = d;
        normalGen.setStream(stream);  // reset to original stream
        return path;
    }


   /**
    * Not implemented, requires two 
    * {@link RandomStream RandomStream}'s.
    * 
    */
   public double[] generatePath (double[] uniforms01)  {
       throw new UnsupportedOperationException("Use generatePath with 2 streams");
    }


   public double nextObservation() {
        double s = path[observationIndex];
        s += InverseGaussianMSHGen.nextDouble(otherStream, normalGen,
                  imu[observationIndex], ilam[observationIndex]);
        observationIndex++;
        observationCounter = observationIndex;
        path[observationIndex] = s;
        return s;
    }

   /**
    * Only returns a stream if both inner 
    * {@link RandomStream RandomStream}'s are the same.
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
        super.setStream(stream);
        normalGen.setStream(stream);
        setOtherStream(otherStream);
    }


   /**
    * Sets both inner streams to <TT>stream</TT>.
    * 
    */
   public void setStream (RandomStream stream)  {
        super.setStream(stream);
        normalGen.setStream(stream);
        setOtherStream(stream);
    }


   /**
    * Sets the <TT>otherStream</TT>, which is the stream used
    * to choose between the two roots in the MSH method.
    * 
    */
   public void setOtherStream (RandomStream otherStream)  {
        this.otherStream = otherStream;
    }


   /**
    * Returns the <TT>otherStream</TT>, which is the stream used
    * to choose between the two quadratic roots from the MSH method.
    * 
    */
   public RandomStream getOtherStream()  {
        return otherStream;
    }


   /**
    * Sets the normal generator.  It also sets one of the
    * two inner streams to the stream of the normal generator.
    * 
    */
   public void setNormalGen (NormalGen normalGen)  {
        this.normalGen = normalGen;
        stream = normalGen.getStream();
    }


   /**
    * Returns the normal generator.
    * 
    */
   public NormalGen getNormalGen()  {
        return normalGen;
    }



/* *
 *   NonRandomStream:     
 * Given a double array, this class will return those values
 * as if it where a random stream.
 * Careful: Will not hard copy the array given as input.
 * And not checking for end of array for the time being.
 * And not checking j>i.
 */
    protected class NonRandomStream implements RandomStream
    {
       double[] array;
       int position;

       public NonRandomStream(double[] array)
       {
	  this.array = array;
	  position = 0;
       }

       public NonRandomStream(double value)
       {
	  this.array = new double[]{value};
	  position = 0;
       }

       public double nextDouble()
       {
          return array[position++];
       }
    
       public void nextArrayOfDouble(double[] u, int start, int n)
       {
	  for(int i = 0; i < n; i++)
	     u[start+i] = array[position++];
       }

       public void nextArrayOfInt(int i, int j, int[] u, 
			         int start, int n)
       {
	  double diff = (double)(j - i);
	  for(int ii = 0; ii < n; ii++)
	      u[start+ii] = i + 
		(int)Math.round(diff * array[position++]);
       }
    
       public int nextInt(int i, int j)
       {
	  return (int)Math.round( (double)(j-i) * array[position]);
       }

    
       public void resetNextSubstream()
       {
       }

       public void resetStartStream()
       {
	  position = 0;
       }

       public void resetStartSubstream()
       {
       }

       public String toString()
       {
	 return new String("NonRandomStream of length " +
		      array.length);
       }
    }





}

