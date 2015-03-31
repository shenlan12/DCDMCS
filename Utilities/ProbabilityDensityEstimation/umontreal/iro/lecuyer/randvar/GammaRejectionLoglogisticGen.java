

/*
 * Class:        GammaRejectionLoglogisticGen
 * Description:  gamma random variate generators using a rejection method
                 with log-logistic envelopes
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

package umontreal.iro.lecuyer.randvar;
import umontreal.iro.lecuyer.rng.*;
import umontreal.iro.lecuyer.probdist.*;


/**
 * This class implements <EM>gamma</EM> random variate generators using
 *  a rejection method with loglogistic envelopes,.
 * For each gamma variate, the first two uniforms are taken from the 
 * main stream and all additional uniforms (after the first rejection)
 * are obtained from the auxiliary stream.
 * 
 */
public class GammaRejectionLoglogisticGen extends GammaGen  {
    
   private RandomStream auxStream;

   // UNURAN parameters for the distribution
   private double beta;
   private double gamma;
   // Generator parameters
   // Rejection with log-logistic envelopes
   private double aa;
   private double bb;
   private double cc;



   /**
    * Creates a gamma random variate generator with parameters <SPAN CLASS="MATH"><I>&#945;</I> =</SPAN> 
    *  <TT>alpha</TT> and <SPAN CLASS="MATH"><I>&#955;</I> =</SPAN> <TT>lambda</TT>, using main stream <TT>s</TT> and 
    *   auxiliary stream <TT>aux</TT>.
    *  The auxiliary stream is used when a random number of uniforms
    *  is required for a rejection-type generation method.
    * 
    */
   public GammaRejectionLoglogisticGen (RandomStream s, RandomStream aux,
                                        double alpha, double lambda)  {
      super (s, null);
      auxStream = aux;
      setParams (alpha, lambda);
      beta  = 1.0/lambda;
      gamma = 0.0;
      init ();
   }


   /**
    * Creates a gamma random variate generator with parameters <SPAN CLASS="MATH"><I>&#945;</I> =</SPAN> 
    *  <TT>alpha</TT> and <SPAN CLASS="MATH"><I>&#955;</I> =</SPAN> <TT>lambda</TT>, using stream <TT>s</TT>.
    * 
    */
   public GammaRejectionLoglogisticGen (RandomStream s,
                                        double alpha, double lambda)  {
      this (s, s, alpha, lambda);
   }


   /**
    * Creates a new generator object for the gamma 
    *     distribution <TT>dist</TT>, using main stream <TT>s</TT> and 
    *     auxiliary stream <TT>aux</TT>. 
    *     The auxiliary stream is used when a random number of uniforms
    *     is required for a rejection-type generation method.
    * 
    */
   public GammaRejectionLoglogisticGen (RandomStream s, RandomStream aux, 
                                        GammaDist dist)  {
      super (s, dist);
      auxStream = aux;
      if (dist != null)
         setParams (dist.getAlpha(), dist.getLambda());
      beta  = 1.0/dist.getLambda();
      gamma = 0.0;
      init ();
   }


   /**
    * Creates a new generator object for the gamma
    *     distribution <TT>dist</TT> and  stream <TT>s</TT> for both the main and
    *     auxiliary stream.
    * 
    */
   public GammaRejectionLoglogisticGen (RandomStream s, GammaDist dist)  {
      this (s, s, dist);
   }


   /**
    * Returns the auxiliary stream associated with this object.
    * 
    */
   public RandomStream getAuxStream() {
      return auxStream;
   }


   public double nextDouble() {
      return rejectionLogLogistic 
                      (stream, auxStream, alpha, beta, gamma, aa, bb, cc);
   }

   /**
    * Generates a new gamma variate with parameters
    *   <SPAN CLASS="MATH"><I>&#945;</I> =</SPAN>&nbsp;<TT>alpha</TT> and <SPAN CLASS="MATH"><I>&#955;</I> =</SPAN>&nbsp;<TT>lambda</TT>, using
    *   main stream <TT>s</TT> and auxiliary stream <TT>aux</TT>.
    * 
    */
   public static double nextDouble (RandomStream s, RandomStream aux, 
                                    double alpha, double lambda) {
      double aa, bb, cc;

      // Code taken from UNURAN
      aa = (alpha > 1.0) ? Math.sqrt (alpha + alpha - 1.0) : alpha;
      bb = alpha - 1.386294361;
      cc = alpha + aa;
    
      return rejectionLogLogistic (s, aux, alpha, 1.0/lambda, 0.0, aa, bb, cc);
   }


   /**
    * Same as {@link #nextDouble nextDouble}&nbsp;<TT>(s, s, alpha, lambda)</TT>.
    * 
    */
   public static double nextDouble (RandomStream s, double alpha, 
                                    double lambda) {
      return nextDouble (s, s, alpha, lambda);
   }


   private static double rejectionLogLogistic
      (RandomStream stream, RandomStream auxStream,
       double alpha, double beta, double gamma,
        double aa, double bb, double cc) {
      // Code taken from UNURAN
      double X;
      double u1,u2,v,r,z;

      while (true) {
         u1 = stream.nextDouble();
         u2 = stream.nextDouble();
         stream = auxStream;
         v = Math.log (u1/(1.0 - u1))/aa;
         X = alpha*Math.exp (v);
         r = bb + cc*v - X;
         z = u1*u1*u2;
         if (r + 2.504077397 >= 4.5*z) break;
         if (r >= Math.log (z)) break;
      }

      return gamma + beta*X;
   }

   private void init() {
      // Code taken from UNURAN
      aa = (alpha > 1.0) ? Math.sqrt (alpha + alpha - 1.0) : alpha;
      bb = alpha - 1.386294361;
      cc = alpha + aa;
   }

}
