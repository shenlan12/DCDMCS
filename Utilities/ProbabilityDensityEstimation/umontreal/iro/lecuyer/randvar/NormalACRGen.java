

/*
 * Class:        NormalACRGen
 * Description:  normal random variate generators using the
                 acceptance-complement ratio method
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
 * the <EM>acceptance-complement ratio</EM> method. 
 * For all the methods, the code was taken from UNURAN.
 * 
 */
public class NormalACRGen extends NormalGen  {



   /**
    * Creates a normal random variate generator with mean <TT>mu</TT>
    *   and standard deviation <TT>sigma</TT>, using stream <TT>s</TT>.
    * 
    */
   public NormalACRGen (RandomStream s, double mu, double sigma)  {
      super (s, null);
      setParams (mu, sigma);
   }


   /**
    * Creates a standard normal random variate generator with mean
    *   <TT>0</TT> and standard deviation <TT>1</TT>, using stream <TT>s</TT>.
    * 
    */
   public NormalACRGen (RandomStream s)  {
      this (s, 0.0, 1.0);
   }


   /**
    * Creates a random variate generator for the normal distribution 
    *   <TT>dist</TT> and stream <TT>s</TT>.
    * 
    */
   public NormalACRGen (RandomStream s, NormalDist dist)  {
      super (s, dist);
      if (dist != null)
         setParams (dist.getMu(), dist.getSigma());
   }
 
  
   public double nextDouble() {
      return nextDouble (stream, mu, sigma);
   }

   public static double nextDouble (RandomStream s, double mu, double sigma) {
/* **************************************************************************** 
*       SAMPLING A RANDOM NUMBER FROM THE                            
*       STANDARD NORMAL DISTRIBUTION N(0,1).
*---------------------------------------------------------------------------
* GENERATION METHOD :  Acceptance-Complement Ratio 
*--------------------------------------------------------------------------- 
* REFERENCE : 
* - UNURAN (c) 2000  W. Hoermann & J. Leydold, Institut f. Statistik, WU Wien
*         
* -  W. Hoermann and G. Derflinger (1990):                       
*     The ACR Method for generating normal random variables,       
*     OR Spektrum 12 (1990), 181-185.                             
*****************************************************************************/ 
      final double  c1   = 1.448242853;
      final double  c2   = 3.307147487;
      final double  c3   = 1.46754004;
      final double  d1   = 1.036467755;
      final double  d2   = 5.295844968;
      final double  d3   = 3.631288474;
      final double  hm   = 0.483941449;
      final double  zm   = 0.107981933;
      final double  hp   = 4.132731354;
      final double  zp   = 18.52161694;
      final double  phln = 0.4515827053;
      final double  hm1  = 0.516058551;
      final double  hp1  = 3.132731354;
      final double  hzm  = 0.375959516;
      final double  hzmp = 0.591923442;
      final double  as   = 0.8853395638;
      final double  bs   = 0.2452635696;
      final double  cs   = 0.2770276848;
      final double  b    = 0.5029324303;
      final double  x0   = 0.4571828819;
      final double  ym   = 0.187308492;
      final double  ss   = 0.7270572718; 
      final double  t    = 0.03895759111;
    
      double X;
      double rn,x,y,z;
    
      do {
         y = s.nextDouble();
         if (y > hm1) { X = hp*y - hp1; break; }
         else if (y < zm) {  
           rn = zp*y - 1;
           X = (rn > 0) ? (1 + rn) : (-1 + rn);
           break;
         } 
         else if (y < hm) {  
           rn = s.nextDouble();
           rn = rn - 1 + rn;
           z = (rn > 0) ? 2 - rn : -2 - rn;
           if ((c1 - y)*(c3 + Math.abs (z)) < c2) { X = z; break; }
           else {  
             x = rn*rn;
             if ((y + d1)*(d3 + x) < d2) { X = rn; break; }
             else if (hzmp - y < Math.exp (-(z*z + phln)/2)) { X = z; break; }
             else if (y + hzm < Math.exp (-(x + phln)/2)) { X = rn; break; }
           }
        }
      
        while (true) {
           x = s.nextDouble();
           y = ym*s.nextDouble();
           z = x0 - ss*x - y;
           if (z > 0)  rn = 2 + y/x;
           else {
             x = 1 - x;
             y = ym - y;
             rn = -(2 + y/x);
           }
           if ((y - as + x)*(cs + x) + bs < 0) { X = rn; break; }
           else if (y < x + t)
              if (rn*rn < 4*(b - Math.log (x))) { X = rn; break; }
        }
      
      } while (false);
        
      return mu + sigma*X;

   }
   /**
    * Generates a variate from the normal distribution with
    *    parameters <SPAN CLASS="MATH"><I>&#956;</I> =</SPAN>&nbsp;<TT>mu</TT> and <SPAN CLASS="MATH"><I>&#963;</I> =</SPAN>&nbsp;<TT>sigma</TT>, using
    *    stream <TT>s</TT>.
    * 
    */

 } 
