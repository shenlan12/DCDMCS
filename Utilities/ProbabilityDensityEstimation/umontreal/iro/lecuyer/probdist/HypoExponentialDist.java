
/*
 * Class:        HypoExponentialDist
 * Description:  Hypo-exponential distribution
 * Environment:  Java
 * Software:     SSJ
 * Copyright (C) 2001  Pierre L'Ecuyer and Universite de Montreal
 * Organization: DIRO, Universite de Montreal
 * @author       Richard Simard
 * @since        January 2011

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

import java.util.Formatter;
import java.util.Locale;
import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;
import umontreal.iro.lecuyer.util.*;
import umontreal.iro.lecuyer.functions.MathFunction;


/**
 * This class implements the <SPAN  CLASS="textit">hypoexponential</SPAN> distribution,
 * also called the generalized Erlang  distribution. Let the <SPAN CLASS="MATH"><I>X</I><SUB>j</SUB></SPAN>,
 * 
 * <SPAN CLASS="MATH"><I>j</I> = 1,&#8230;, <I>k</I></SPAN>, be <SPAN CLASS="MATH"><I>k</I></SPAN> independent exponential random variables with different
 * rates <SPAN CLASS="MATH"><I>&#955;</I><SUB>j</SUB></SPAN>, i.e. assume that 
 * <SPAN CLASS="MATH"><I>&#955;</I><SUB>j</SUB>&#8800;<I>&#955;</I><SUB>i</SUB></SPAN> for
 * <SPAN CLASS="MATH"><I>i</I>&#8800;<I>j</I></SPAN>. Then the sum 
 * <SPAN CLASS="MATH">&sum;<SUB>j=1</SUB><SUP>k</SUP><I>X</I><SUB>j</SUB></SPAN> is called a <SPAN  CLASS="textit">hypoexponential</SPAN>
 * random variable.
 * 
 * <P>
 * Let the  <SPAN CLASS="MATH"><I>k</I>&#215;<I>k</I></SPAN> upper triangular bidiagonal matrix
 * 
 * <P></P>
 * <DIV ALIGN="CENTER" CLASS="mathdisplay"><A NAME="eq:tail-hypomatrix"></A>
 * <B>A</B> = 1#1
 * </DIV><P></P>
 * with  <SPAN CLASS="MATH"><I>&#955;</I><SUB>j</SUB></SPAN> the rates of the <SPAN CLASS="MATH"><I>k</I></SPAN> exponential random variables;
 * then the cumulative complementary probability of the hypoexponential
 * distribution is given by
 * 
 * <P></P>
 * <DIV ALIGN="CENTER" CLASS="mathdisplay"><A NAME="eq:tail-hypoexp"></A>
 * bar(F)(<I>x</I>) = <I>P</I>[<I>X</I><SUB>1</SUB> + <SUP> ... </SUP> + <I>X</I><SUB>k</SUB> &gt; <I>x</I>] = &sum;<SUB>j=1</SUB><SUP>k</SUP>(<I>e</I><SUP><B>A</B>x</SUP>)<SUB>1j</SUB>,
 * </DIV><P></P>
 * i.e., it is the sum of the elements of the first row of matrix <SPAN CLASS="MATH"><I>e</I><SUP><B>A</B>x</SUP></SPAN>.
 * The density of the hypoexponential distribution is
 * 
 * <P></P>
 * <DIV ALIGN="CENTER" CLASS="mathdisplay">
 * <I>f</I> (<I>x</I>) = (- <I>e</I><SUP><B>A</B>x</SUP><B>A</B>)<SUB>1k</SUB> = <I>&#955;</I><SUB>k</SUB>(<I>e</I><SUP><B>A</B>x</SUP>)<SUB>1k</SUB>,
 * </DIV><P></P>
 * i.e., it is element <SPAN CLASS="MATH">(1, <I>k</I>)</SPAN> of matrix 
 * <SPAN CLASS="MATH">- <I>e</I><SUP><B>A</B>x</SUP><B>A</B></SPAN>.
 * The distribution function is as usual 
 * <SPAN CLASS="MATH"><I>F</I>(<I>x</I>) = 1 - bar(F)(<I>x</I>)</SPAN>.
 * 
 * <P>
 * See the class {@link HypoExponentialDistQuick} for alternative formulae
 * for the probabilities.
 * 
 */
public class HypoExponentialDist extends ContinuousDistribution {
   protected double[] m_lambda;

   protected static void testLambda (double[] lambda) {
      int m = lambda.length;
      for (int j = 0; j < m; ++j) {
         if (lambda[j] <= 0)
            throw new IllegalArgumentException ("lambda_j <= 0");
      }
   }


   // Builds the bidiagonal matrix A out of the lambda
   private static DoubleMatrix2D buildMatrix (double[] lambda, double x) {
      int m = lambda.length;
      testLambda (lambda);
      DoubleFactory2D F2 = DoubleFactory2D.dense;
      DoubleMatrix2D A = F2.make(m, m);
      for (int j = 0; j < m-1; j++) {
         A.setQuick(j, j, -lambda[j]*x);
         A.setQuick(j, j + 1, lambda[j]*x);
      }
      A.setQuick(m-1, m-1, -lambda[m-1]*x);
      return A;
   }


   private static class myFunc implements MathFunction {
      // For inverseF
      private double[] m_lam;
      private double m_u;

      public myFunc (double[] lam, double u) {
         m_lam = lam;
         m_u = u;
      }

      public double evaluate (double x) {
         return m_u - HypoExponentialDist.cdf(m_lam, x);
      }
   }



   /**
    * Constructs a <TT>HypoExponentialDist</TT> object,
    * with rates 
    * <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB> =</SPAN> <TT>lambda[<SPAN CLASS="MATH"><I>i</I> - 1</SPAN>]</TT>, 
    * <SPAN CLASS="MATH"><I>i</I> = 1,&#8230;, <I>k</I></SPAN>.
    * 
    * @param lambda rates of the hypoexponential distribution
    * 
    */
   public HypoExponentialDist (double[] lambda) {
      supportA = 0.0;
      setLambda (lambda);
  }


   public double density (double x) {
      return density (m_lambda, x);
   }

   public double cdf (double x) {
      return cdf (m_lambda, x);
   }

   public double barF (double x) {
      return barF (m_lambda, x);
   }

   public double inverseF (double u) {
      return inverseF (m_lambda, u);
   }

   public double getMean() {
      return getMean (m_lambda);
   }

   public double getVariance() {
      return getVariance (m_lambda);
   }

   public double getStandardDeviation() {
      return getStandardDeviation (m_lambda);
   }

   /**
    * Computes the density function <SPAN CLASS="MATH"><I>f</I> (<I>x</I>)</SPAN>, with 
    * <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB> =</SPAN>
    * <TT>lambda[<SPAN CLASS="MATH"><I>i</I> - 1</SPAN>]</TT>, 
    * <SPAN CLASS="MATH"><I>i</I> = 1,&#8230;, <I>k</I></SPAN>.
    * 
    * @param lambda rates of the hypoexponential distribution
    * 
    *    @param x value at which the density is evaluated
    * 
    *    @return density at <SPAN CLASS="MATH"><I>x</I></SPAN>
    * 
    */
   public static double density (double[] lambda, double x) {
      if (x < 0)
         return 0;
      DoubleMatrix2D Ax = buildMatrix (lambda, x);
      DoubleMatrix2D T = DMatrix.expBidiagonal(Ax);
      int m = lambda.length;
      return lambda[m-1]*T.getQuick(0, m-1);
   }


   /**
    * Computes the  distribution function <SPAN CLASS="MATH"><I>F</I>(<I>x</I>)</SPAN>, with 
    * <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB> =</SPAN>
    * <TT>lambda[<SPAN CLASS="MATH"><I>i</I> - 1</SPAN>]</TT>, 
    * <SPAN CLASS="MATH"><I>i</I> = 1,&#8230;, <I>k</I></SPAN>.
    *  
    * @param lambda rates of the hypoexponential distribution
    * 
    *    @param x value at which the distribution is evaluated
    * 
    *    @return distribution at <SPAN CLASS="MATH"><I>x</I></SPAN>
    * 
    */
   public static double cdf (double[] lambda, double x) {
      if (x <= 0.0)
         return 0.0;
      if (x >= Double.MAX_VALUE)
         return 1.0;
      double mean = HypoExponentialDist.getMean (lambda);
      double std = HypoExponentialDist.getStandardDeviation(lambda);
      double LOW = mean - 1.5*std;
      if (x > LOW) {
         double p = 1.0 - HypoExponentialDist.barF(lambda, x);
         double LIMIT = 1.0e-3;
         if (p > LIMIT)
            return p;
      }

      DoubleMatrix2D T = buildMatrix (lambda, x);
      DoubleFactory1D fac1 = DoubleFactory1D.dense;
      int m = lambda.length;
      DoubleMatrix1D C = fac1.make(m, 1.0);
      DoubleMatrix1D B = DMatrix.expmiBidiagonal(T, C);
      return Math.abs(B.getQuick(0));
   }


   /**
    * Computes the  distribution function <SPAN CLASS="MATH"><I>F</I>(<I>x</I>)</SPAN>, with 
    * <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB> =</SPAN>
    * <TT>lambda[<SPAN CLASS="MATH"><I>i</I> - 1</SPAN>]</TT>, 
    * <SPAN CLASS="MATH"><I>i</I> = 1,&#8230;, <I>k</I></SPAN>. Returns <SPAN CLASS="MATH">1 -</SPAN><TT>barF(lambda, x)</TT>,
    *    which is much faster than <TT>cdf</TT> but loses precision
    *    in the lower tail.
    *  
    * @param lambda rates of the hypoexponential distribution
    * 
    *    @param x value at which the distribution is evaluated
    * 
    *    @return distribution at <SPAN CLASS="MATH"><I>x</I></SPAN>
    * 
    */
   public static double cdf2 (double[] lambda, double x) {
      if (x <= 0.0)
         return 0.0;
      if (x >= Double.MAX_VALUE)
         return 1.0;
      return (1.0 - HypoExponentialDist.barF(lambda, x));
   }


   /**
    * Computes the complementary distribution <SPAN CLASS="MATH">bar(F)(<I>x</I>)</SPAN>,
    *    with 
    * <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB> =</SPAN> <TT>lambda[<SPAN CLASS="MATH"><I>i</I> - 1</SPAN>]</TT>, 
    * <SPAN CLASS="MATH"><I>i</I> = 1,&#8230;, <I>k</I></SPAN>.
    *  
    * @param lambda rates of the hypoexponential distribution
    * 
    *    @param x value at which the complementary distribution is evaluated
    * 
    *    @return complementary distribution at <SPAN CLASS="MATH"><I>x</I></SPAN>
    * 
    */
   public static double barF (double[] lambda, double x) {
      if (x <= 0.0)
         return 1.0;
      if (x >= Double.MAX_VALUE)
         return 0.0;
      DoubleMatrix2D T = buildMatrix (lambda, x);
      DoubleFactory1D fac1 = DoubleFactory1D.dense;
      int m = lambda.length;
      DoubleMatrix1D C = fac1.make(m, 1.0);
      DoubleMatrix1D B = DMatrix.expBidiagonal(T, C);
      return B.getQuick(0);
   }


   /**
    * Computes the inverse distribution function <SPAN CLASS="MATH"><I>F</I><SUP>-1</SUP>(<I>u</I>)</SPAN>,
    * with 
    * <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB> =</SPAN> <TT>lambda[<SPAN CLASS="MATH"><I>i</I> - 1</SPAN>]</TT>, 
    * <SPAN CLASS="MATH"><I>i</I> = 1,&#8230;, <I>k</I></SPAN>.
    * 
    * @param lambda rates of the hypoexponential distribution
    * 
    *    @param u value at which the inverse distribution is evaluated
    * 
    *    @return inverse distribution at <SPAN CLASS="MATH"><I>u</I></SPAN>
    * 
    */
   public static double inverseF (double[] lambda, double u) {
      if (u < 0.0 || u > 1.0)
          throw new IllegalArgumentException ("u not in [0,1]");
      if (u >= 1.0)
          return Double.POSITIVE_INFINITY;
      if (u <= 0.0)
          return 0.0;

      final double EPS = 1.0e-12;
      myFunc fonc = new myFunc (lambda, u);
      double x1 = getMean (lambda);
      double v = cdf (lambda, x1);
      if (u <= v)
         return RootFinder.brentDekker (0, x1, fonc, EPS);

      // u > v
      double x2 = 4.0*x1 + 1.0;
      v = cdf (lambda, x2);
      while (v < u) {
         x1 = x2;
         x2 = 4.0*x2;
         v = cdf (lambda, x2);
      }
      return RootFinder.brentDekker (x1, x2, fonc, EPS);
   }


   /**
    * Returns the mean, 
    * <SPAN CLASS="MATH"><I>E</I>[<I>X</I>] = &sum;<SUB>i=1</SUB><SUP>k</SUP>1/<I>&#955;</I><SUB>i</SUB></SPAN>,
    *    of the hypoexponential distribution with rates 
    * <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB> =</SPAN>
    * <TT>lambda[<SPAN CLASS="MATH"><I>i</I> - 1</SPAN>]</TT>, 
    * <SPAN CLASS="MATH"><I>i</I> = 1,&#8230;, <I>k</I></SPAN>.
    * 
    * @param lambda rates of the hypoexponential distribution
    * 
    *    @return mean of the hypoexponential distribution
    * 
    */
   public static double getMean (double[] lambda) {
      testLambda (lambda);
      int k = lambda.length;
      double sum = 0;
      for (int j = 0; j < k; j++)
         sum += 1.0 / lambda[j];
      return sum;
   }


   /**
    * Returns the variance,
    * 
    * <SPAN CLASS="MATH">Var[<I>X</I>] = &sum;<SUB>i=1</SUB><SUP>k</SUP>1/<I>&#955;</I><SUB>i</SUB><SUP>2</SUP></SPAN>,
    * of the hypoexponential distribution with rates 
    * <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB> =</SPAN>
    * <TT>lambda[<SPAN CLASS="MATH"><I>i</I> - 1</SPAN>]</TT>, 
    * <SPAN CLASS="MATH"><I>i</I> = 1,&#8230;, <I>k</I></SPAN>.
    * 
    * @param lambda rates of the hypoexponential distribution
    * 
    *    @return variance of the hypoexponential distribution
    * 
    */
   public static double getVariance (double[] lambda) {
      testLambda (lambda);
      int k = lambda.length;
      double sum = 0;
      for (int j = 0; j < k; j++)
         sum += 1.0 / (lambda[j]*lambda[j]);
      return sum;
   }


   /**
    * Returns the standard deviation
    * of the hypoexponential distribution  with rates 
    * <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB> =</SPAN>
    * <TT>lambda[<SPAN CLASS="MATH"><I>i</I> - 1</SPAN>]</TT>, 
    * <SPAN CLASS="MATH"><I>i</I> = 1,&#8230;, <I>k</I></SPAN>.
    * 
    * @param lambda rates of the hypoexponential distribution
    * 
    *    @return standard deviation of the hypoexponential distribution
    * 
    */
   public static double getStandardDeviation (double[] lambda) {
      double s = getVariance (lambda);
      return Math.sqrt(s);
   }


   /**
    * Returns the values <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB></SPAN> for this object.
    * 
    */
   public double[] getLambda() {
      return m_lambda;
   }



   /**
    * Sets the values 
    * <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB> =</SPAN><TT>lambda[<SPAN CLASS="MATH"><I>i</I> - 1</SPAN>]</TT>,
    *  
    * <SPAN CLASS="MATH"><I>i</I> = 1,&#8230;, <I>k</I></SPAN> for this object.
    * 
    */
   public void setLambda (double[] lambda) {
      if (lambda == null)
         return;
      int k = lambda.length;
      m_lambda = new double[k];
      testLambda (lambda);
      System.arraycopy (lambda, 0, m_lambda, 0, k);
   }


   /**
    * Same as {@link #getLambda getLambda}.
    * 
    * 
    */
   public double[] getParams() {
      return m_lambda;
   }


   public String toString () {
      StringBuilder sb = new StringBuilder();
      Formatter formatter = new Formatter(sb, Locale.US);
      formatter.format(getClass().getSimpleName() + " : lambda = {" +
           PrintfFormat.NEWLINE);
      int k = m_lambda.length;
      for(int i = 0; i < k; i++) {
         formatter.format("   %g%n", m_lambda[i]);
      }
      formatter.format("}%n");
      return sb.toString();
   }

}
