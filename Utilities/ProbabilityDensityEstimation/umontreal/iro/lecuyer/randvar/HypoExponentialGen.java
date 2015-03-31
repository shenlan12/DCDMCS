

/*
 * Class:        HypoExponentialGen
 * Description:  random variate generators for the hypoexponential distribution 
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

package umontreal.iro.lecuyer.randvar;
import umontreal.iro.lecuyer.rng.*;
import umontreal.iro.lecuyer.probdist.*;


/**
 * This class implements random variate generators for the 
 * <SPAN  CLASS="textit">hypoexponential</SPAN> distribution (see classes 
 * {@link umontreal.iro.lecuyer.probdist.HypoExponentialDist HypoExponentialDist} and
 * {@link umontreal.iro.lecuyer.probdist.HypoExponentialDistQuick HypoExponentialDistQuick}
 * in package <TT>probdist</TT> for the definition).
 * 
 */
public class HypoExponentialGen extends RandomVariateGen  {


   /**
    * Creates a hypoexponential random variate generator with
    *  rates 
    * <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB> =</SPAN> <TT>lambda[<SPAN CLASS="MATH"><I>i</I> - 1</SPAN>]</TT>, 
    * <SPAN CLASS="MATH"><I>i</I> = 1,&#8230;, <I>k</I></SPAN>,
    *  using stream <TT>stream</TT>.
    * 
    */
   public HypoExponentialGen (RandomStream stream, double[] lambda)  {
      super (stream, new HypoExponentialDist(lambda));
   }


   /**
    * Creates a new generator for the hypoexponential 
    *    distribution <TT>dist</TT> with stream <TT>stream</TT>.
    * 
    */
   public HypoExponentialGen (RandomStream stream, HypoExponentialDist dist)  {
      super (stream, dist);
//      if (dist != null)
//         setParams (dist.getLambda());
   } 

   
   /**
    * Uses inversion to generate a new hypoexponential variate
    *    with rates 
    * <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB> =</SPAN> <TT>lambda[<SPAN CLASS="MATH"><I>i</I> - 1</SPAN>]</TT>, 
    * <SPAN CLASS="MATH"><I>i</I> = 1,&#8230;, <I>k</I></SPAN>, 
    *    using stream <TT>stream</TT>. The inversion uses a root-finding method
    *   and is very slow.
    * 
    */
   public static double nextDouble (RandomStream stream, double[] lambda)  {
      return HypoExponentialDist.inverseF (lambda, stream.nextDouble());
   }


   /**
    * Returns the <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB></SPAN> associated with this object.
    * 
    */
   public double[] getLambda() {
      return ((HypoExponentialDist)dist).getLambda();
   }



   /**
    * Sets the rates 
    * <SPAN CLASS="MATH"><I>&#955;</I><SUB>i</SUB> =</SPAN> <TT>lam[<SPAN CLASS="MATH"><I>i</I> - 1</SPAN>]</TT>, 
    * 
    * <SPAN CLASS="MATH"><I>i</I> = 1,&#8230;, <I>k</I></SPAN> of this object.
    * 
    */
   public void setLambda (double[] lambda) {
      ((HypoExponentialDist)dist).setLambda(lambda);
   }

}
