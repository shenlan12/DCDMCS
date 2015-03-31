
/*
 * Class:        HypoExponentialDistEqual
 * Description:  Hypo-exponential distribution
 * Environment:  Java
 * Software:     SSJ
 * Copyright (C) 2001  Pierre L'Ecuyer and Universite de Montreal
 * Organization: DIRO, Universite de Montreal
 * @author       Richard Simard
 * @since        February 2014

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
import umontreal.iro.lecuyer.util.*;
import umontreal.iro.lecuyer.functions.MathFunction;

/**
 * This class implements  the <SPAN  CLASS="textit">hypoexponential</SPAN> distribution for the case of equidistant 
 * <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB> = (<I>n</I> + 1 - <I>i</I>)<I>h</I></SPAN>. We have 
 * <SPAN CLASS="MATH"><I>&#955;</I><SUB>i+1</SUB> - <I>&#955;</I><SUB>i</SUB> = <I>h</I></SPAN>, with <SPAN CLASS="MATH"><I>h</I></SPAN> a constant, and <SPAN CLASS="MATH"><I>n</I>&nbsp;&gt;=&nbsp;<I>k</I></SPAN> are integers.
 * 
 * <P>
 * The formula becomes
 * 
 * <P></P>
 * <DIV ALIGN="CENTER" CLASS="mathdisplay">
 * bar(F)(<I>x</I>) = <I>P</I>[<I>X</I><SUB>1</SUB> + <SUP> ... </SUP> + <I>X</I><SUB>k</SUB> &gt; <I>x</I>] = &sum;<SUB>i=1</SUB><SUP>k</SUP><I>e</I><SUP>-(n+1-i)hx</SUP>&prod;<SUB>1#1j=1<tex2html_row_mark>j 2#2i</SUB><SUP>k</SUP><I>n</I>+1-<I>j</I>/<I>i</I>-<I>j</I>.
 * </DIV><P></P>
 * The formula for the density becomes
 * 
 * <P></P>
 * <DIV ALIGN="CENTER" CLASS="mathdisplay">
 * <I>f</I> (<I>x</I>) = &sum;<SUB>i=1</SUB><SUP>k</SUP>(<I>n</I> + 1 - <I>i</I>)<I>he</I><SUP>-(n+1-i)hx</SUP>&prod;<SUB>[tex2html_wrap_indisplay193]j=1<tex2html_row_mark>j [tex2html_wrap_indisplay194]i</SUB><SUP>k</SUP><I>n</I>+1-<I>j</I>/<I>i</I>-<I>j</I>.
 * </DIV><P></P>
 * 
 */
public class HypoExponentialDistEqual extends HypoExponentialDist {
   private double m_h;
   private int m_n;
   private int m_k;



   /**
    * Constructor for equidistant rates.
    *   The rates are 
    * <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB> = (<I>n</I> + 1 - <I>i</I>)<I>h</I></SPAN>, for 
    * <SPAN CLASS="MATH"><I>i</I> = 1,&#8230;, <I>k</I></SPAN>.
    *   
    * @param n largest rate is <SPAN CLASS="MATH"><I>nh</I></SPAN>
    * 
    *    @param k number of rates
    * 
    *    @param h difference between adjacent rates
    * 
    */
   public HypoExponentialDistEqual (int n, int k, double h) {
      super (null);
      setParams (n, k, h);
   }

   public double density (double x) {
      return density (m_n, m_k, m_h, x);
   }

   public double cdf (double x) {
      return cdf (m_n, m_k, m_h, x);
   }

   public double barF (double x) {
      return barF (m_n, m_k, m_h, x);
   }

   public double inverseF (double u) {
      return inverseF (m_n, m_k, m_h, u);
   }

   /**
    * Computes the density function <SPAN CLASS="MATH"><I>f</I> (<I>x</I>)</SPAN>, with the same arguments
    *   as in the constructor.
    * 
    * @param n max possible number of <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB></SPAN>
    * 
    *    @param k effective number of <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB></SPAN>
    * 
    *    @param h step between two successive <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB></SPAN>
    * 
    *    @param x value at which the distribution is evaluated
    * 
    *    @return density at <SPAN CLASS="MATH"><I>x</I></SPAN>
    * 
    */
   public static double density (int n, int k, double h, double x) {
      if (x < 0)
         return 0;
      double r = -Math.expm1(-h*x);
      double v = BetaDist.density(k, n - k + 1, r);
      return h*v*Math.exp(-h*x);
   }


   /**
    * Computes the distribution function <SPAN CLASS="MATH"><I>F</I>(<I>x</I>)</SPAN>, with arguments
    *   as in the constructor.
    *  
    * @param n max possible number of <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB></SPAN>
    * 
    *    @param k effective number of <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB></SPAN>
    * 
    *    @param h step between two successive <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB></SPAN>
    * 
    *    @param x value at which the distribution is evaluated
    * 
    *    @return value of distribution at <SPAN CLASS="MATH"><I>x</I></SPAN>
    * 
    */
   public static double cdf (int n, int k, double h, double x) {
      if (x <= 0)
         return 0;
      double r = -Math.expm1(-h*x);
      double u = BetaDist.cdf(k, n - k + 1, r);
      return u;
   }


   /**
    * Computes the complementary distribution <SPAN CLASS="MATH">bar(F)(<I>x</I>)</SPAN>,
    *   as in formula.
    *  
    * @param n max possible number of <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB></SPAN>
    * 
    *    @param k effective number of <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB></SPAN>
    * 
    *    @param h step between two successive <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB></SPAN>
    * 
    *    @param x value at which the complementary distribution is evaluated
    * 
    *    @return value of complementary distribution at <SPAN CLASS="MATH"><I>x</I></SPAN>
    * 
    */
   public static double barF (int n, int k, double h, double x) {
      if (x <= 0)
         return 1.0;
      double r = Math.exp(-h*x);
      double v = BetaDist.cdf(n - k + 1, k, r);
      return v;
   }


   /**
    * Computes the inverse distribution 
    * <SPAN CLASS="MATH"><I>x</I> = <I>F</I><SUP>-1</SUP>(<I>u</I>)</SPAN>,
    *  with arguments as in the constructor.
    * 
    * @param n max possible number of <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB></SPAN>
    * 
    *    @param k effective number of <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB></SPAN>
    * 
    *    @param h step between two successive <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB></SPAN>
    * 
    *    @param u value at which the inverse distribution is evaluated
    * 
    *    @return inverse distribution at <SPAN CLASS="MATH"><I>u</I></SPAN>
    * 
    */
   public static double inverseF (int n, int k, double h, double u) {
      if (u < 0.0 || u > 1.0)
          throw new IllegalArgumentException ("u not in [0,1]");
      if (u >= 1.0)
          return Double.POSITIVE_INFINITY;
      if (u <= 0.0)
          return 0.0;

      double z = BetaDist.inverseF(k, n - k + 1, u);
      return -Math.log1p(-z) / h;
   }


   /**
    * Returns the three parameters
    * of this hypoexponential distribution as array <SPAN CLASS="MATH">(<I>n</I>, <I>k</I>, <I>h</I>)</SPAN>.
    * 
    * @return parameters of the hypoexponential distribution
    * 
    */
   public double[] getParams() {
      double[] par = new double[]{m_n, m_k, m_h};
      return par;
   }


   public void setParams (int n, int k, double h) {
      m_n = n;
      m_k = k;
      m_h = h;
      m_lambda = new double[k];
      for(int i = 0; i < k; i++) {
         m_lambda[i] = (n - i)*h;
      }
   }


   public String toString() {
      StringBuilder sb = new StringBuilder();
      Formatter formatter = new Formatter(sb, Locale.US);
      formatter.format(getClass().getSimpleName() + " : params = {" +
           PrintfFormat.NEWLINE);
      formatter.format("   %d, %d, %f", m_n, m_k, m_h);
      formatter.format("}%n");
      return sb.toString();
   }
}
