

/*
 * Class:        FrechetDist
 * Description:  Fréchet distribution
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

package umontreal.iro.lecuyer.probdist;

import umontreal.iro.lecuyer.util.*;
import optimization.*;
import umontreal.iro.lecuyer.functions.MathFunction;

/**
 * Extends the class {@link ContinuousDistribution} for the  <SPAN  CLASS="textit">Fr&#233;chet</SPAN>
 * distribution, with location parameter <SPAN CLASS="MATH"><I>&#948;</I></SPAN>, scale
 *  parameter <SPAN CLASS="MATH"><I>&#946;</I> &gt; 0</SPAN>, and shape parameter 
 * <SPAN CLASS="MATH"><I>&#945;</I> &gt; 0</SPAN>, where we use
 *  the notation 
 * <SPAN CLASS="MATH"><I>z</I> = (<I>x</I> - <I>&#948;</I>)/<I>&#946;</I></SPAN>. It has density
 * 
 * <P></P>
 * <DIV ALIGN="CENTER" CLASS="mathdisplay">
 * <I>f</I> (<I>x</I>) = <I>&#945;e</I><SUP>-z<SUP>-<I>&#945;</I></SUP></SUP>/(<I>&#946;z</I><SUP><I>&#945;</I>+1</SUP>),&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;for <I>x</I> &gt; <I>&#948;</I>
 * </DIV><P></P>
 * and distribution function
 * 
 * <P></P>
 * <DIV ALIGN="CENTER" CLASS="mathdisplay">
 * <I>F</I>(<I>x</I>) = <I>e</I><SUP>-z<SUP>-<I>&#945;</I></SUP></SUP>,&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;for <I>x</I> &gt; <I>&#948;</I>.
 * </DIV><P></P>
 * Both the density and the distribution are  0 for 
 * <SPAN CLASS="MATH"><I>x</I>&nbsp;&lt;=&nbsp;<I>&#948;</I></SPAN>.
 * 
 * <P>
 * The mean is given by
 * 
 * <P></P>
 * <DIV ALIGN="CENTER" CLASS="mathdisplay">
 * <I>E</I>[<I>X</I>] = <I>&#948;</I> + <I>&#946;&#915;</I>(1 - 1/<I>&#945;</I>),
 * </DIV><P></P>
 * where <SPAN CLASS="MATH"><I>&#915;</I>(<I>x</I>)</SPAN> is the gamma function. The variance is
 * 
 * <P></P>
 * <DIV ALIGN="CENTER" CLASS="mathdisplay">
 * Var[<I>X</I>] = <I>&#946;</I><SUP>2</SUP>[<I>&#915;</I>(1 - 2/<I>&#945;</I>) - (<I>&#915;</I>(1 - 1/<I>&#945;</I>))<SUP>2</SUP>].
 * </DIV><P></P>
 * 
 */
public class FrechetDist extends ContinuousDistribution {
   private double delta;
   private double beta;
   private double alpha;


   private static class Optim implements Lmder_fcn {
      protected double[] x;
      protected int n;

      public Optim (double[] x, int n) {
         this.n = n;
         this.x = x;
      }

      public void fcn (int m, int n, double[] par, double[] fvec, double[][] fjac, int iflag[])
      {
         if (par[1] <= 0.0 || par[2] <= 0.0) {
            final double BIG = 1.0e100;
            fvec[1] = BIG;
            fvec[2] = BIG;
            fvec[3] = BIG;
            return;
         }

         double sum1, sum2, sumb, sum4, sum5;
         double z, w, v;
         double alpha = par[1];
         double beta = par[2];
         double mu = par[3];

         if (iflag[1] == 1) {
            sum1 = sum2 = sumb = sum4 = sum5 = 0;
            for (int i = 0; i < n; i++) {
               z = (x[i] - mu) / beta;
               sum1 += 1.0 / z;
               v = Math.pow(z, -alpha);
               sum2 += v / z;
               sumb += v;
               w = Math.log(z);
               sum4 += w;
               sum5 += v * w;
            }

            fvec[2] = sumb - n;   // eq. for beta
            fvec[3] = (alpha + 1) * sum1 - alpha * sum2;   // eq. for mu
            fvec[1] = n / alpha + sum5 - sum4;   // eq. for alpha

         } else if (iflag[1] == 2) {
            throw new IllegalArgumentException ("iflag = 2");
            // The 3 X 3 Jacobian must be calculated and put in fjac
         }
      }
   }


   private static class Function implements MathFunction {
      private int n;
      private double[] x;
      private double delta;
      public double sumxi;
      public double dif;

      public Function (double[] y, int n, double delta) {
         this.n = n;
         this.x = y;
         this.delta = delta;
         double xmin = Double.MAX_VALUE;
         for (int i = 0; i < n; i++) {
            if ((y[i] < xmin) && (y[i] > delta))
               xmin = y[i];
         }
         dif = xmin - delta;
      }

      public double evaluate (double alpha) {
         if (alpha <= 0.0) return 1.0e100;
         double v, w;
         double sum1 = 0, sum2 = 0, sum3 = 0;
         for (int i = 0; i < n; i++) {
            if (x[i] <= delta)
               continue;
            v = Math.log(x[i] - delta);
            w = Math.pow(dif / (x[i] - delta), alpha);
            sum1 += v;
            sum2 += w;
            sum3 += v * w;
         }

         sum1 /= n;
         sumxi = sum2 / n;
         return 1 / alpha + sum3 / sum2 - sum1;
      }
   }


   /**
    * Constructor for the standard <SPAN  CLASS="textit">Fr&#233;chet</SPAN>
    *    distribution with parameters  <SPAN CLASS="MATH"><I>&#946;</I></SPAN> = 1 and <SPAN CLASS="MATH"><I>&#948;</I></SPAN> = 0.
    * 
    */
   public FrechetDist (double alpha) {
      setParams (alpha, 1.0, 0.0);
   }


   /**
    * Constructs a <TT>FrechetDist</TT> object with parameters
    *   <SPAN CLASS="MATH"><I>&#945;</I></SPAN> = <TT>alpha</TT>,  <SPAN CLASS="MATH"><I>&#946;</I></SPAN> = <TT>beta</TT> and  <SPAN CLASS="MATH"><I>&#948;</I></SPAN> = <TT>delta</TT>.
    * 
    */
   public FrechetDist (double alpha, double beta, double delta) {
      setParams (alpha, beta, delta);
   }


   public double density (double x) {
      return density (alpha, beta, delta, x);
   }

   public double cdf (double x) {
      return cdf (alpha, beta, delta, x);
   }

   public double barF (double x) {
      return barF (alpha, beta, delta, x);
   }

   public double inverseF (double u) {
      return inverseF (alpha, beta, delta, u);
   }

   public double getMean() {
      return getMean (alpha, beta, delta);
   }

   public double getVariance() {
      return getVariance (alpha, beta, delta);
   }

   public double getStandardDeviation() {
      return getStandardDeviation (alpha, beta, delta);
   }

   /**
    * Computes and returns the density function.
    * 
    */
   public static double density (double alpha, double beta, double delta,
                                 double x) {
      if (beta <= 0)
         throw new IllegalArgumentException ("beta <= 0");
      if (alpha <= 0)
         throw new IllegalArgumentException ("alpha <= 0");
      final double z = (x - delta)/beta;
      if (z <= 0.0)
         return 0.0;
      double t = Math.pow (z, -alpha);
      return  alpha * t * Math.exp (-t) / (z * beta);
   }


   /**
    * Computes and returns  the distribution function.
    * 
    */
   public static double cdf (double alpha, double beta, double delta,
                             double x) {
      if (beta <= 0)
         throw new IllegalArgumentException ("beta <= 0");
      if (alpha <= 0)
         throw new IllegalArgumentException ("alpha <= 0");
      final double z = (x - delta)/beta;
      if (z <= 0.0)
         return 0.0;
      double t = Math.pow (z, -alpha);
      return  Math.exp (-t);
   }


   /**
    * Computes and returns  the complementary distribution function <SPAN CLASS="MATH">1 - <I>F</I>(<I>x</I>)</SPAN>.
    * 
    */
   public static double barF (double alpha, double beta, double delta,
                              double x) {
      if (beta <= 0)
         throw new IllegalArgumentException ("beta <= 0");
      if (alpha <= 0)
         throw new IllegalArgumentException ("alpha <= 0");
      final double z = (x - delta)/beta;
      if (z <= 0.0)
         return 1.0;
      double t = Math.pow (z, -alpha);
      return  -Math.expm1 (-t);
   }


   /**
    * Computes and returns the inverse distribution function.
    * 
    */
   public static double inverseF (double alpha, double beta, double delta,
                                  double u) {
      if (u < 0.0 || u > 1.0)
         throw new IllegalArgumentException ("u not in [0, 1]");
      if (beta <= 0)
         throw new IllegalArgumentException ("beta <= 0");
      if (alpha <= 0)
         throw new IllegalArgumentException ("alpha <= 0");
      if (u >= 1.0)
         return Double.POSITIVE_INFINITY;
      if (u <= 0.0)
         return delta;
      double t = Math.pow (-Math.log (u), 1.0/alpha);
      if (t <= Double.MIN_NORMAL)
         return Double.MAX_VALUE;
      return delta + beta / t;
   }


   /**
    * Given <SPAN CLASS="MATH"><I>&#948;</I> =</SPAN> <TT>delta</TT>, estimates the parameters 
    * <SPAN CLASS="MATH">(<I>&#945;</I>, <I>&#946;</I>)</SPAN>
    *   of the <SPAN  CLASS="textit">Fr&#233;chet</SPAN> distribution
    *    using the maximum likelihood method with the <SPAN CLASS="MATH"><I>n</I></SPAN> observations
    *    <SPAN CLASS="MATH"><I>x</I>[<I>i</I>]</SPAN>, 
    * <SPAN CLASS="MATH"><I>i</I> = 0, 1,&#8230;, <I>n</I> - 1</SPAN>. The estimates are returned in a two-element
    *     array, in regular order: [<SPAN CLASS="MATH"><I>&#945;</I></SPAN>, <SPAN CLASS="MATH"><I>&#946;</I></SPAN>].
    * 
    * @param x the list of observations used to evaluate parameters
    * 
    *    @param n the number of observations used to evaluate parameters
    * 
    *    @param delta location parameter
    * 
    *    @return returns the parameters [
    * <SPAN CLASS="MATH">hat(&alpha;)</SPAN>, 
    * <SPAN CLASS="MATH">hat(&beta;)</SPAN>]
    * 
    */
   public static double[] getMLE (double[] x, int n, double delta) {
      if (n <= 1)
         throw new IllegalArgumentException ("n <= 1");

      Function func = new Function (x, n, delta);
      double a = 1e-4;
      double b = 1.0e12;
      double alpha = RootFinder.brentDekker (a, b, func, 1e-12);
      double par[] = new double[2];
      par[0] = alpha;
      par[1] = func.dif * Math.pow (func.sumxi, -1.0/alpha);
      return par;
   }


   /**
    * Given <SPAN CLASS="MATH"><I>&#948;</I> =</SPAN> <TT>delta</TT>, creates a new instance of a <SPAN  CLASS="textit">Fr&#233;chet</SPAN>
    * distribution with parameters <SPAN CLASS="MATH"><I>&#945;</I></SPAN> and <SPAN CLASS="MATH"><I>&#946;</I></SPAN> estimated using the
    * maximum likelihood method based on the <SPAN CLASS="MATH"><I>n</I></SPAN> observations <SPAN CLASS="MATH"><I>x</I>[<I>i</I>]</SPAN>,
    * 
    * <SPAN CLASS="MATH"><I>i</I> = 0, 1,&#8230;, <I>n</I> - 1</SPAN>.
    * 
    * @param x the list of observations to use to evaluate parameters
    * 
    *    @param n the number of observations to use to evaluate parameters
    * 
    *    @param delta location parameter
    * 
    */
   public static FrechetDist getInstanceFromMLE (double[] x, int n,
                                                 double delta) {
      double par[] = getMLE (x, n, delta);
      return new FrechetDist (par[0], par[1], delta);
   }


   /**
    * Returns the mean of the <SPAN  CLASS="textit">Fr&#233;chet</SPAN> distribution with
    *  parameters <SPAN CLASS="MATH"><I>&#945;</I></SPAN>, <SPAN CLASS="MATH"><I>&#946;</I></SPAN> and <SPAN CLASS="MATH"><I>&#948;</I></SPAN>.
    * 
    * @return the mean
    * 
    */
   public static double getMean (double alpha, double beta, double delta) {
      if (beta <= 0)
         throw new IllegalArgumentException ("beta <= 0");
      if (alpha <= 1)
         throw new IllegalArgumentException ("alpha <= 1");
      double t = Num.lnGamma(1.0 - 1.0/alpha);
      return delta + beta * Math.exp(t);
   }


   /**
    * Returns the variance of the <SPAN  CLASS="textit">Fr&#233;chet</SPAN> distribution with parameters
    *    <SPAN CLASS="MATH"><I>&#945;</I></SPAN>, <SPAN CLASS="MATH"><I>&#946;</I></SPAN> and <SPAN CLASS="MATH"><I>&#948;</I></SPAN>.
    * 
    * @return the variance
    * 
    */
   public static double getVariance (double alpha, double beta,
                                     double delta) {
      if (beta <= 0)
         throw new IllegalArgumentException ("beta <= 0");
      if (alpha <= 2)
         throw new IllegalArgumentException ("alpha <= 2");
      double t = Num.lnGamma(1.0 - 1.0/alpha);
      double mu = Math.exp(t);
      double v = Math.exp(Num.lnGamma(1.0 - 2.0/alpha));
      return beta * beta * (v - mu * mu);
   }


   /**
    * Returns the standard deviation of the <SPAN  CLASS="textit">Fr&#233;chet</SPAN> distribution
    * with parameters <SPAN CLASS="MATH"><I>&#945;</I></SPAN>, <SPAN CLASS="MATH"><I>&#946;</I></SPAN> and <SPAN CLASS="MATH"><I>&#948;</I></SPAN>.
    * 
    * @return the standard deviation
    * 
    */
   public static double getStandardDeviation (double alpha, double beta,
                                              double delta) {
      return  Math.sqrt(getVariance (alpha, beta, delta));
   }


   /**
    * Returns the parameter <SPAN CLASS="MATH"><I>&#945;</I></SPAN> of this object.
    * 
    */
   public double getAlpha() {
      return alpha;
   }


   /**
    * Returns the parameter <SPAN CLASS="MATH"><I>&#946;</I></SPAN> of this object.
    * 
    */
   public double getBeta() {
      return beta;
   }


   /**
    * Returns the parameter <SPAN CLASS="MATH"><I>&#948;</I></SPAN> of this object.
    * 
    */
   public double getDelta() {
      return delta;
   }



   /**
    * Sets the parameters  <SPAN CLASS="MATH"><I>&#945;</I></SPAN>, <SPAN CLASS="MATH"><I>&#946;</I></SPAN> and <SPAN CLASS="MATH"><I>&#948;</I></SPAN> of this object.
    * 
    */
   public void setParams (double alpha, double beta, double delta) {
      if (beta <= 0)
         throw new IllegalArgumentException ("beta <= 0");
      if (alpha <= 0)
         throw new IllegalArgumentException ("alpha <= 0");
      this.delta  = delta;
      this.beta = beta;
      this.alpha = alpha;
   }


   /**
    * Return an array containing the parameters of the current object
    *     in regular order: [<SPAN CLASS="MATH"><I>&#945;</I></SPAN>, <SPAN CLASS="MATH"><I>&#946;</I></SPAN>, <SPAN CLASS="MATH"><I>&#948;</I></SPAN>].
    * 
    * 
    */
   public double[] getParams() {
      double[] retour = {alpha, beta, delta};
      return retour;
   }


   public String toString () {
      return getClass().getSimpleName() + " : alpha = " + alpha + ", beta = " + beta + ", delta = " + delta;
   }

}
