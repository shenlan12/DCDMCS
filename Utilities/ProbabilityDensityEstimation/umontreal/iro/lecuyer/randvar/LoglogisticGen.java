

/*
 * Class:        LoglogisticGen
 * Description:  random variate generators for the log-logistic distribution
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
 * This class implements random variate generators for the 
 * <EM>log-logistic</EM> distribution with shape parameter 
 * <SPAN CLASS="MATH"><I>&#945;</I> &gt; 0</SPAN>
 * and scale parameter <SPAN CLASS="MATH"><I>&#946;</I> &gt; 0</SPAN>.
 * The density function of this distribution is
 * 
 * <P></P>
 * <DIV ALIGN="CENTER" CLASS="mathdisplay">
 * <I>f</I> (<I>x</I>) = (<I>&#945;</I>(<I>x</I>/<I>&#946;</I>)<SUP><I>&#945;</I>-1</SUP>)/(<I>&#946;</I>[1 + (<I>x</I>/<I>&#946;</I>)<SUP><I>&#945;</I></SUP>]<SUP>2</SUP>)&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;for <I>x</I> &gt; 0.
 * </DIV><P></P>
 * 
 */
public class LoglogisticGen extends RandomVariateGen  {
   protected double alpha;
   protected double beta;




   /**
    * Creates a log-logistic random variate generator with parameters
    *   <SPAN CLASS="MATH"><I>&#945;</I> =</SPAN> <TT>alpha</TT> and <SPAN CLASS="MATH"><I>&#946;</I> =</SPAN> <TT>beta</TT>, using stream <TT>s</TT>.
    * 
    */
   public LoglogisticGen (RandomStream s, double alpha, double beta)  {
      super (s, new LoglogisticDist(alpha, beta));
      setParams (alpha, beta);
   }


   /**
    * Creates a new generator for the distribution <TT>dist</TT>,
    *      using stream <TT>s</TT>.
    * 
    */
   public LoglogisticGen (RandomStream s, LoglogisticDist dist)  {
      super (s, dist);
      if (dist != null)
         setParams (dist.getAlpha(), dist.getBeta());
   }
    

   /**
    * Generates a variate from the <EM>log-logistic</EM> distribution
    *    with shape parameter 
    * <SPAN CLASS="MATH"><I>&#945;</I> &gt; 0</SPAN> and scale parameter <SPAN CLASS="MATH"><I>&#946;</I> &gt; 0</SPAN>.
    * 
    */
   public static double nextDouble (RandomStream s,
                                    double alpha, double beta) {
      return LoglogisticDist.inverseF (alpha, beta, s.nextDouble());
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


   protected void setParams (double alpha, double beta) {
      if (alpha <= 0.0)
         throw new IllegalArgumentException ("alpha <= 0");
      if (beta <= 0.0)
         throw new IllegalArgumentException ("beta <= 0");
      this.alpha = alpha;
      this.beta = beta;
   }
}
