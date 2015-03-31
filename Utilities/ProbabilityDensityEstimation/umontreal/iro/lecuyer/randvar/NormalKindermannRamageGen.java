

/*
 * Class:        NormalKindermannRamageGen
 * Description:  normal random variate generators using the Kindermann-Ramage method
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
 * This class implements <EM>normal</EM> random variate generators using
 *  the <EM>Kindermann-Ramage</EM> method.
 * The code was taken from UNURAN. It includes the correction
 * of the error in the original <EM>Kindermann-Ramage</EM> method found by the
 * authors in.
 * 
 */
public class NormalKindermannRamageGen extends NormalGen  {



   /**
    * Creates a normal random variate generator with mean <TT>mu</TT>
    *   and standard deviation <TT>sigma</TT>, using stream <TT>s</TT>.
    * 
    */
   public NormalKindermannRamageGen (RandomStream s,
                                     double mu, double sigma)  {
      super (s, null);
      setParams (mu, sigma);
   }


   /**
    * Creates a standard normal random variate generator with mean
    *   <TT>0</TT> and standard deviation <TT>1</TT>, using stream <TT>s</TT>.
    * 
    */
   public NormalKindermannRamageGen (RandomStream s)  {
      this (s, 0.0, 1.0);
   }


   /**
    * Creates a random variate generator for the normal distribution
    *   <TT>dist</TT> and stream <TT>s</TT>.
    * 
    */
   public NormalKindermannRamageGen (RandomStream s, NormalDist dist)  {
      super (s, dist);
      if (dist != null)
         setParams (dist.getMu(), dist.getSigma());
   }


   public double nextDouble() {
      return kindermanRamage (stream, mu, sigma);
   }

   public static double nextDouble (RandomStream s, double mu, double sigma) {
      return kindermanRamage (s, mu, sigma);
   }
   /**
    * Generates a variate from the normal distribution with
    *    parameters <SPAN CLASS="MATH"><I>&#956;</I> =</SPAN>&nbsp;<TT>mu</TT> and <SPAN CLASS="MATH"><I>&#963;</I> =</SPAN>&nbsp;<TT>sigma</TT>, using
    *    stream <TT>s</TT>.
    * 
    */


//>>>>>>>>>>>>>>>>>>>>  P R I V A T E S    M E T H O D S   <<<<<<<<<<<<<<<<<<<<

  private static double kindermanRamage (RandomStream stream, double mu, double sigma) {
    final double XI = 2.216035867166471;
    final double PIhochK = 0.3989422804;
    double U, V, W, X;
    double t, z;

    U = stream.nextDouble();
    if (U < 0.884070402298758) {
      V = stream.nextDouble();
      X = XI*(1.131131635444180*U + V - 1.);
    }

    else if (U >= 0.973310954173898) {
      do {
        V = stream.nextDouble();
        W = stream.nextDouble();
        if (W == 0.) { t=0.; continue; }
        t = XI*XI/2. - Math.log (W);
      } while ( (V*V*t) > (XI*XI/2.) );
      X = (U < 0.986655477086949) ? Math.pow (2*t,0.5) : -Math.pow (2*t,0.5);
    }

    else if (U >= 0.958720824790463) {
      do {
        V = stream.nextDouble();
        W = stream.nextDouble();
        z = V - W;
        t = XI - 0.630834801921960*Math.min (V, W);
      } while (Math.max (V, W) > 0.755591531667601 && 0.034240503750111*
               Math.abs (z) > (PIhochK*Math.exp (t*t/(-2.)) -
                              0.180025191068563*(XI - Math.abs (t))) );
      X = (z < 0) ? t : -t;
    }

    else if (U >= 0.911312780288703) {
      do {
        V = stream.nextDouble();
        W = stream.nextDouble();
        z = V - W;
        t = 0.479727404222441 + 1.105473661022070*Math.min (V, W);
      } while (Math.max (V, W) > 0.872834976671790 && 0.049264496373128*
               Math.abs (z) > (PIhochK*Math.exp (t*t/(-2))
                              - 0.180025191068563*(XI - Math.abs (t))) );
      X = (z < 0) ? t : -t;
    }

    else {
      do {
        V = stream.nextDouble();
        W = stream.nextDouble();
        z = V - W;
        t = 0.479727404222441 - 0.595507138015940*Math.min (V, W);
        if (t < 0.0)
           continue;
      } while (Math.max (V, W) > 0.805777924423817 && 0.053377549506886*
               Math.abs (z) > (PIhochK*Math.exp (t*t/(-2)) -
                              0.180025191068563*(XI - Math.abs (t))) );
      X = (z < 0) ? t : -t;
    }

    return mu + sigma*X;
   }
} 
