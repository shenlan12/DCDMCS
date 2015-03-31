

/*
 * Class:        ConstantDist
 * Description:  constant distribution
 * Environment:  Java
 * Software:     SSJ
 * Copyright (C) 2001  Pierre L'Ecuyer and Universite de Montreal
 * Organization: DIRO, Universite de Montreal
 * @author       Éric Buist
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


/**
 * Represents a <SPAN  CLASS="textit">constant</SPAN> discrete distribution taking a single real
 * value with probability 1.
 * Its mass function is
 * 
 * <P></P>
 * <DIV ALIGN="CENTER" CLASS="mathdisplay">
 * <I>p</I>(<I>x</I>) = 1,&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;for <I>x</I> = <I>c</I>,
 * </DIV><P></P>
 * and  <SPAN CLASS="MATH"><I>p</I>(<I>x</I>) = 0</SPAN> elsewhere.
 * 
 */
public class ConstantDist extends DiscreteDistribution  {
   private double c;


   /**
    * Constructs a new constant distribution with probability 1 at <TT>c</TT>.
    * 
    */
   public ConstantDist (double c) {
      super (new double[] { c }, new double[] { 1.0 }, 1);
      this.c = c;
   }


   /**
    * Returns the mean <SPAN CLASS="MATH"><I>E</I>[<I>X</I>] = <I>c</I></SPAN>.
    * 
    * @return <SPAN CLASS="MATH"><I>c</I></SPAN>
    * 
    */
   @Override
   public double getMean() {
      return c;
   }


   /**
    * Returns the variance 
    * <SPAN CLASS="MATH">Var[<I>X</I>] = 0</SPAN>.
    * 
    * @return 0
    * 
    */
   @Override
   public double getVariance()  {
      return 0;
   }


   /**
    * Returns the standard deviation = 0.
    * 
    * @return 0
    * 
    */
   @Override
   public double getStandardDeviation() {
      return 0;
   }


   /**
    * Returns the inverse distribution function 
    * <SPAN CLASS="MATH"><I>c</I> = <I>F</I><SUP>-1</SUP>(<I>u</I>)</SPAN>.
    * 
    * @return <SPAN CLASS="MATH"><I>c</I></SPAN>
    * 
    */
   @Override
   public double inverseF (double u) {
      return c;
   }

}
