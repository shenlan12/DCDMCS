

/*
 * Class:        CycleBasedPointSetBase2
 * Description:  
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

package umontreal.iro.lecuyer.hups;

import umontreal.iro.lecuyer.util.PrintfFormat;
import umontreal.iro.lecuyer.rng.RandomStream;
import cern.colt.list.*;


/**
 * Similar to {@link CycleBasedPointSet}, except that the successive
 * values in the cycles are stored as integers in the range
 * 
 * <SPAN CLASS="MATH">{0,..., 2<SUP>k</SUP> -1}</SPAN>, where 
 * <SPAN CLASS="MATH">1&nbsp;&lt;=&nbsp;<I>k</I>&nbsp;&lt;=&nbsp;31</SPAN>.
 * The output values <SPAN CLASS="MATH"><I>u</I><SUB>i, j</SUB></SPAN> are obtained by dividing these integer
 * values by <SPAN CLASS="MATH">2<SUP>k</SUP></SPAN>.  Point sets where the successive coordinates of each
 * point are obtained via linear recurrences modulo 2 (e.g., linear feedback
 * shift registers or Korobov-type polynomial lattice rules)
 * are naturally expressed in this form.
 * Storing the integers 
 * <SPAN CLASS="MATH">2<SUP>k</SUP><I>u</I><SUB>i, j</SUB></SPAN> instead of the <SPAN CLASS="MATH"><I>u</I><SUB>i, j</SUB></SPAN> themselves
 * makes it easier to apply randomizations such as digital random shifts
 * in base 2, which are applied to the bits <SPAN  CLASS="textit">before</SPAN> transforming
 * the value to a real number <SPAN CLASS="MATH"><I>u</I><SUB>i, j</SUB></SPAN>. When a random digital shift is
 * performed, it applies a bitwise exclusive-or of all the points with a single
 *   random point.
 * 
 */
public abstract class CycleBasedPointSetBase2 extends CycleBasedPointSet {

// dim = Integer.MAX_VALUE;     // Dimension is infinite.
   private int[] digitalShift;  // Digital shift, initially zero (null).
                                // Entry j is for dimension j.
   protected int numBits;       // Number of bits in stored values.
   protected double normFactor; // To convert output to (0,1); 1/2^numBits.





   public double getCoordinate (int i, int j) {
      // Find cycle that contains point i, then index in cycle.
      int l = 0;         // Length of next cycle.
      int n = 0;         // Total length of cycles added so far.
      int k;
      for (k = 0;  n <= i;  k++)
         n += l = ((AbstractList) cycles.get (k)).size();
      AbstractList curCycle = (AbstractList) cycles.get (k-1);
      int[] curCycleI = ((IntArrayList) curCycle).elements();
      int coordinate = (i - n + l + j) % curCycle.size();
      int shift = 0;
      if (digitalShift != null) {
         shift = digitalShift[j];
         return (shift ^ curCycleI[coordinate]) * normFactor + EpsilonHalf;
      } else
         return (shift ^ curCycleI[coordinate]) * normFactor;
   }

   public PointSetIterator iterator() {
      return new CycleBasedPointSetBase2Iterator ();
   }


   /**
    * Adds a random digital shift in base 2 to all the points
    *   of the point set, using stream <TT>stream</TT> to generate the random numbers,
    *   for coordinates <TT>d1</TT> to <TT>d2 - 1</TT>.
    *   This applies a bitwise exclusive-or of all the points with a single
    *   random point.
    * 
    * 
    */
   public void addRandomShift (int d1, int d2, RandomStream stream)  {
      if (null == stream)
         throw new IllegalArgumentException (
              PrintfFormat.NEWLINE +
              "   Calling addRandomShift with null stream");
      if (0 == d2)
         d2 = Math.max (1, dim);
      if (digitalShift == null) {
         digitalShift = new int[d2];
         capacityShift = d2;
      } else if (d2 > capacityShift) {
         int d3 = Math.max (4, capacityShift);
         while (d2 > d3)
            d3 *= 2;
         int[] temp = new int[d3];
         capacityShift = d3;
         for (int i = 0; i < dimShift; i++)
            temp[i] = digitalShift[i];
         digitalShift = temp;
      }
      dimShift = d2;
      int maxj;
      if (numBits < 31) {
         maxj = (1 << numBits) - 1;
      } else {
         maxj = 2147483647;
      }
      for (int i = d1; i < d2; i++)
         digitalShift[i] = stream.nextInt (0, maxj);
      shiftStream = stream;

   }


   public void clearRandomShift() {
      super.clearRandomShift();
      digitalShift = null;
   }



   public String formatPoints() {
      StringBuffer sb = new StringBuffer (toString());
      for (int c = 0; c < numCycles; c++) {
         AbstractList curCycle = (AbstractList)cycles.get (c);
         int[] cycle = ((IntArrayList)curCycle).elements();
         sb.append (PrintfFormat.NEWLINE + "Cycle " + c + ": (");
         boolean first = true;
         for (int e = 0; e < curCycle.size(); e++) {
            if (first)
               first = false;
            else
               sb.append (", ");
            sb.append (cycle[e]);
         }
         sb.append (")");
      }
      return sb.toString();
   }

   // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

   public class CycleBasedPointSetBase2Iterator
                   extends CycleBasedPointSetIterator {

      protected int[] curCycleI;          // The array for current cycle

      public CycleBasedPointSetBase2Iterator () {
         super ();
         resetCurCycle (0);
      }

      protected void init() { }

      public void resetCurCycle (int index) {
         curCycleIndex = index;
         curCycle = (AbstractList) cycles.get (index);
         curCycleI = ((IntArrayList) curCycle).elements();
      }

      public double nextCoordinate() {
          // First, verify if there are still points....
          if (curPointIndex >= numPoints)
             outOfBounds();
          int x = curCycleI [curCoordInCycle];
          if (digitalShift != null) {
             if (curCoordIndex >= dimShift)   // Extend the shift.
                addRandomShift (dimShift, curCoordIndex + 1, shiftStream);
             x ^= digitalShift[curCoordIndex];
          }
          curCoordIndex++;
          curCoordInCycle++;
          if (curCoordInCycle >= curCycle.size())
             curCoordInCycle = 0;
          if (digitalShift == null)
             return x * normFactor;
          else
             return x * normFactor + EpsilonHalf;
     }

      public void nextCoordinates (double p[], int dim) {
         // First, verify if there are still points....
         if (curPointIndex >= numPoints)
            outOfBounds();
         if (curCoordIndex + dim >= dimShift)
            addRandomShift (dimShift, curCoordIndex + dim + 1, shiftStream);
         int j = curCoordInCycle;
         int maxj = curCycle.size();
         int x;
         for (int i = 0; i < dim; i++) {
            x = curCycleI [curCoordInCycle++];
            if (curCoordInCycle >= maxj) curCoordInCycle = 0;
            if (digitalShift == null)
               p[i] = x * normFactor;
            else
               p[i] = (digitalShift[curCoordIndex + i] ^ x) * normFactor + EpsilonHalf;
         }
         curCoordIndex += dim;
      }

      public int nextPoint (double p[], int dim) {
         if (getCurPointIndex() >= getNumPoints ())
            outOfBounds();
         curCoordIndex = 0;
         curCoordInCycle = startPointInCycle;
         nextCoordinates (p, dim);
         resetToNextPoint();
         return curPointIndex;
      }
   }
}

