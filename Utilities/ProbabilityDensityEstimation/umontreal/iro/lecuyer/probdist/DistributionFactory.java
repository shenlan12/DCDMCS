

/*
 * Class:        DistributionFactory
 * Description:  allows the creation of distribution objects from a string
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

import java.lang.reflect.*;
import java.util.StringTokenizer;

/**
 * This class implements a string API for the package <TT>probdist</TT>.
 * It uses Java Reflection to allow the creation of probability distribution
 * objects from a string.  This permits one to obtain distribution specifications 
 * from a file or dynamically from user input during program execution.
 * This string API is similar to that of 
 * <A NAME="tex2html1"
 *   HREF="http://statistik.wu-wien.ac.at/unuran/">UNURAN</A>.
 * 
 * <P>
 * The (static) methods of this class invoke the constructor specified 
 * in the string.  For example,
 * 
 * <DIV CLASS="vcode" ALIGN="LEFT">
 * <TT>
 * d = DistributionFactory.getContinuousDistribution (&#34;NormalDist (0.0, 2.5)&#34;);
 * <BR></TT>
 * </DIV>
 * is equivalent to
 * 
 * <DIV CLASS="vcode" ALIGN="LEFT">
 * <TT>
 * d = NormalDist (0.0, 2.5);
 * <BR></TT>
 * </DIV>
 * 
 * <P>
 * The string that specifies the distribution (i.e., the formal parameter
 * <TT>str</TT> of the methods) must be a valid call of the constructor
 * of a class that extends {@link ContinuousDistribution} or
 * {@link DiscreteDistribution}, and all parameter values must be numerical
 * values (variable names are not allowed).
 * 
 *   
 * 
 * <P>
 * The distribution parameters can also be estimated from a set of observations
 * instead of being passed to the constructor.  In that case, one passes the
 * vector of observations, and the constructor estimates the parameters by
 * the maximum likelihood method.
 * 
 */
public class DistributionFactory {
   private DistributionFactory() {}   //  ????   Utile?

   public static Distribution getDistribution (String str) {
      // Extracts the name of the distribution.
      // If there is an open parenthesis, the name contains all the 
      // non-space characters preceeding it.If not,the name is the full string.

      int i = 0;
      str = str.trim();

      int idx = str.indexOf ('(', i);
      String distName;
      if (idx == -1)
         distName = str.substring (i).trim();
      else
         distName = str.substring (i, idx).trim();
 
      // Try to find the class in probdist package.
      Class<?> distClass;
      if (distName.equals ("String"))
         throw new IllegalArgumentException ("Invalid distribution name: " 
                                             + distName);
      try {
         distClass = Class.forName ("umontreal.iro.lecuyer.probdist." 
                                    + distName);
      }
      catch (ClassNotFoundException e) {
         // Look for a fully qualified classname whose constructor
         //  matches this string.
         try {
            distClass = Class.forName (distName);
            // We must check if the class implements Distribution 
            if (Distribution.class.isAssignableFrom(distClass) == false)
               throw new IllegalArgumentException 
                  ("The given class is not a Probdist distribution class.");
         }
         catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException ("Invalid distribution name: " 
                                                + distName);
         }
      }

      String paramStr = "";
      if (idx != -1) {
         // Get the parameters from the string.
         int parFrom = idx;
         int parTo = str.lastIndexOf (')');
         // paramStr will contain the parameters without parentheses.
         paramStr = str.substring (parFrom + 1, parTo).trim();
         if (paramStr.indexOf ('(') != -1 || paramStr.indexOf (')') != -1)
            //All params are numerical,so parenthesis nesting is forbidden here
            throw new IllegalArgumentException ("Invalid parameter string: " 
                                                + paramStr);
      }

      if (paramStr.equals ("")) {
         // No parameter is given to the constructor.
         try {
            return (Distribution) distClass.newInstance();
         }
         catch (IllegalAccessException e) {
            throw new IllegalArgumentException 
                                         ("Default parameters not available");
         }
         catch (InstantiationException e) {
            throw new IllegalArgumentException 
                                         ("Default parameters not available");
         }
      }

      // Find the number of parameters and try to find a matching constructor.
      // Within probdist, there are no constructors with the same
      // number of arguments but with different types.
      // This simplifies the constructor selection scheme.
      StringTokenizer paramTok = new StringTokenizer (paramStr, ",");
      int nparams = paramTok.countTokens();
      Constructor[] cons = distClass.getConstructors();
      Constructor distCons = null;
      Class[] paramTypes = null;
      // Find a public constructor with the correct number of parameters.
      for (i = 0; i < cons.length; i++) {
         if (Modifier.isPublic (cons[i].getModifiers()) &&
             ((paramTypes = cons[i].getParameterTypes()).length == nparams)) {
            distCons = cons[i];
            break;
         }
      }
      if (distCons == null)
         throw new IllegalArgumentException ("Invalid parameter number");

      // Create the parameters for the selected constructor.
      Object[] instParams = new Object[nparams];
      for (i = 0; i < nparams; i++) {
         String par = paramTok.nextToken().trim();
         try {
            // We only need a limited set of parameter types here.
            if (paramTypes[i] == int.class)
               instParams[i] = new Integer (par);
            else if (paramTypes[i] == long.class)
               instParams[i] = new Long (par);
            else if (paramTypes[i] == float.class) {
               if (par.equalsIgnoreCase ("infinity") || par.equalsIgnoreCase 
                                                                 ("+infinity"))
                  instParams[i] = new Float (Float.POSITIVE_INFINITY);
               else if (par.equalsIgnoreCase ("-infinity"))
                  instParams[i] = new Float (Float.NEGATIVE_INFINITY);
               else
                  instParams[i] = new Float (par);
            }
            else if (paramTypes[i] == double.class) {
               if (par.equalsIgnoreCase ("infinity") || par.equalsIgnoreCase
                                                                 ("+infinity"))
                  instParams[i] = new Double (Double.POSITIVE_INFINITY);
               else if (par.equalsIgnoreCase ("-infinity"))
                  instParams[i] = new Double (Double.NEGATIVE_INFINITY);
               else
                  instParams[i] = new Double (par);
            }
            else
               throw new IllegalArgumentException
                  ("Parameter " + (i+1) + " type " + paramTypes[i].getName() +
                   "not supported");
         }
         catch (NumberFormatException e) {
            throw new IllegalArgumentException
               ("Parameter " + (i+1) + " of type " +
                paramTypes[i].getName()+" could not be converted from String");
         }
      }

      // Try to instantiate the distribution class.
      try {
         return (Distribution) distCons.newInstance (instParams);
      }
      catch (IllegalAccessException e) {
         return null;
      }
      catch (InstantiationException e) {
         return null;
      }
      catch (InvocationTargetException e) {
         return null;
      }
   }


 @SuppressWarnings("unchecked")
   /**
    * Uses the Java Reflection API to construct a {@link ContinuousDistribution}
    *    object by estimating parameters of the distribution using the maximum likelihood
    *    method based on the <SPAN CLASS="MATH"><I>n</I></SPAN> observations in table <SPAN CLASS="MATH"><I>x</I>[<I>i</I>]</SPAN>, 
    * <SPAN CLASS="MATH"><I>i</I> = 0, 1,&#8230;, <I>n</I> - 1</SPAN>.
    * 
    * @param distName the name of the distribution to instanciate
    * 
    *    @param x the list of observations to use to evaluate parameters
    * 
    *    @param n the number of observations to use to evaluate parameters
    * 
    * 
    */
   public static ContinuousDistribution getDistributionMLE
                    (String distName, double[] x, int n) {

      Class<?> distClass;
      try
      {
         distClass = Class.forName ("umontreal.iro.lecuyer.probdist." + distName);
      }
      catch (ClassNotFoundException e)
      {
         try
         {
            distClass = Class.forName (distName);
         }
         catch (ClassNotFoundException ex)
         {
            throw new IllegalArgumentException ("Invalid distribution name: " 
                                                + distName);
         }
      }

      return getDistributionMLE ((Class<? extends ContinuousDistribution>)distClass, x, n);
   }


 @SuppressWarnings("unchecked")
   /**
    * Uses the Java Reflection API to construct a {@link DiscreteDistributionInt}
    *    object by estimating parameters of the distribution using the maximum likelihood
    *    method based on the <SPAN CLASS="MATH"><I>n</I></SPAN> observations in table <SPAN CLASS="MATH"><I>x</I>[<I>i</I>]</SPAN>, 
    * <SPAN CLASS="MATH"><I>i</I> = 0, 1,&#8230;, <I>n</I> - 1</SPAN>.
    * 
    * @param distName the name of the distribution to instanciate
    * 
    *    @param x the list of observations to use to evaluate parameters
    * 
    *    @param n the number of observations to use to evaluate parameters
    * 
    * 
    */
   public static DiscreteDistributionInt getDistributionMLE
                    (String distName, int[] x, int n) {

      Class<?> distClass;
      try
      {
         distClass = Class.forName ("umontreal.iro.lecuyer.probdist." + distName);
      }
      catch (ClassNotFoundException e)
      {
         try
         {
            distClass = Class.forName (distName);
         }
         catch (ClassNotFoundException ex)
         {
            throw new IllegalArgumentException ("Invalid distribution name: " 
                                                + distName);
         }
      }

      return getDistributionMLE ((Class<? extends DiscreteDistributionInt>)distClass, x, n);
   }


 @SuppressWarnings("unchecked")
   /**
    * Uses the Java Reflection API to construct a {@link ContinuousDistribution}
    *    object by estimating parameters of the distribution using the maximum likelihood
    *    method based on the <SPAN CLASS="MATH"><I>n</I></SPAN> observations in table <SPAN CLASS="MATH"><I>x</I>[<I>i</I>]</SPAN>, 
    * <SPAN CLASS="MATH"><I>i</I> = 0, 1,&#8230;, <I>n</I> - 1</SPAN>.
    * 
    * @param distClass the class of the distribution to instanciate
    * 
    *    @param x the list of observations to use to evaluate parameters
    * 
    *    @param n the number of observations to use to evaluate parameters
    * 
    * 
    */
   public static <T extends ContinuousDistribution> T getDistributionMLE
                    (Class<T> distClass, double[] x, int n) {
      if (ContinuousDistribution.class.isAssignableFrom(distClass) == false)
               throw new IllegalArgumentException 
                  ("The given class is not a Probdist distribution class.");

      Method m;
      try
      {
         m = distClass.getMethod ("getInstanceFromMLE", double[].class, int.class);
      }
      catch (NoSuchMethodException e) {
         throw new IllegalArgumentException
         ("The given class does not provide the static method getInstanceFromMLE (double[],int)");
      }
      if (!Modifier.isStatic (m.getModifiers()) ||
          !distClass.isAssignableFrom (m.getReturnType()))
         throw new IllegalArgumentException
         ("The given class does not provide the static method getInstanceFromMLE (double[],int)");
      
      try
      {
         return (T)m.invoke (null, x, n);
      }
      catch (IllegalAccessException e) {
         return null;
      }
      catch (IllegalArgumentException e) {
         return null;
      }
      catch (InvocationTargetException e) {
         return null;
      }      
   }


 @SuppressWarnings("unchecked")
   /**
    * Uses the Java Reflection API to construct a {@link DiscreteDistributionInt}
    *    object by estimating parameters of the distribution using the maximum likelihood
    *    method based on the <SPAN CLASS="MATH"><I>n</I></SPAN> observations in table <SPAN CLASS="MATH"><I>x</I>[<I>i</I>]</SPAN>, 
    * <SPAN CLASS="MATH"><I>i</I> = 0, 1,&#8230;, <I>n</I> - 1</SPAN>.
    * 
    * @param distClass the class of the distribution to instanciate
    * 
    *    @param x the list of observations to use to evaluate parameters
    * 
    *    @param n the number of observations to use to evaluate parameters
    * 
    * 
    */
   public static <T extends DiscreteDistributionInt> T getDistributionMLE
                    (Class<T> distClass, int[] x, int n) {
      if (DiscreteDistributionInt.class.isAssignableFrom(distClass) == false)
               throw new IllegalArgumentException 
                  ("The given class is not a discrete distribution class over integers.");
      
      Method m;
      try
      {
         m = distClass.getMethod ("getInstanceFromMLE", int[].class, int.class);
      }
      catch (NoSuchMethodException e) {
         throw new IllegalArgumentException
         ("The given class does not provide the static method getInstanceFromMLE (int[],int)");
      }
      if (!Modifier.isStatic (m.getModifiers()) ||
          !distClass.isAssignableFrom (m.getReturnType()))
         throw new IllegalArgumentException
         ("The given class does not provide the static method getInstanceFromMLE (int[],int)");
      
      try
      {
         return (T)m.invoke (null, x, n);
      }
      catch (IllegalAccessException e) {
         return null;
      }
      catch (IllegalArgumentException e) {
         return null;
      }
      catch (InvocationTargetException e) {
         return null;
      }      
   }


   /**
    * Uses the Java Reflection API to construct a {@link ContinuousDistribution}
    *   object by executing the code contained in the string <TT>str</TT>.
    *   This code should be a valid invocation of the constructor of a 
    *   {@link ContinuousDistribution} object.
    *   This method throws exceptions if it cannot parse the given string and 
    *   returns <TT>null</TT> if the distribution object could not be created due to 
    *   a Java-specific instantiation problem.
    * 
    * @param str string that contains a call to the constructor of a continuous 
    *      distribution
    * 
    *    @return a continuous distribution object or <TT>null</TT> if it could not 
    *      be instantiated
    *    @exception IllegalArgumentException if parsing problems occured 
    *      when reading <TT>str</TT>
    * 
    *    @exception ClassCastException if the distribution string does not represent
    *      a continuous distribution
    * 
    * 
    */
   public static ContinuousDistribution getContinuousDistribution (String str) {
      return (ContinuousDistribution)getDistribution (str);
   }


   /**
    * Same as {@link #getContinuousDistribution getContinuousDistribution}, but for discrete distributions
    *   over the real numbers.
    * 
    * @param str string that contains a call to the constructor of a discrete
    *      distribution
    * 
    *    @return a discrete distribution object, or <TT>null</TT> if it could not 
    *      be instantiated
    *    @exception IllegalArgumentException if parsing problems occured when 
    *      reading <TT>str</TT>
    * 
    *    @exception ClassCastException if the distribution string does not represent
    *      a discrete distribution
    * 
    * 
    */
   public static DiscreteDistribution getDiscreteDistribution (String str) {
      return (DiscreteDistribution)getDistribution (str);
   }



   /**
    * Same as {@link #getContinuousDistribution getContinuousDistribution}, but for discrete distributions
    *   over the integers.
    * 
    * @param str string that contains a call to the constructor of a discrete
    *      distribution
    * 
    *    @return a discrete distribution object, or <TT>null</TT> if it could not 
    *      be instantiated
    *    @exception IllegalArgumentException if parsing problems occured when 
    *      reading <TT>str</TT>
    * 
    *    @exception ClassCastException if the distribution string does not represent
    *      a discrete distribution
    * 
    */
   public static DiscreteDistributionInt getDiscreteDistributionInt (String str) {
      return (DiscreteDistributionInt)getDistribution (str);
   }
}
