

/*
 * Class:        PointSet
 * Description:  Base class of all point sets
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

import java.util.NoSuchElementException;
import java.util.List;
import java.util.ArrayList;
import umontreal.iro.lecuyer.rng.RandomStream;
import umontreal.iro.lecuyer.util.Num;
import umontreal.iro.lecuyer.util.PrintfFormat;


/**
 * This abstract class defines the basic methods
 * for accessing and manipulating point sets.
 * A point set can be represented as a two-dimensional array, whose element
 * <SPAN CLASS="MATH">(<I>i</I>, <I>j</I>)</SPAN> contains <SPAN CLASS="MATH"><I>u</I><SUB>i, j</SUB></SPAN>, the <SPAN  CLASS="textit">coordinate</SPAN> <SPAN CLASS="MATH"><I>j</I></SPAN> of point <SPAN CLASS="MATH"><I>i</I></SPAN>.
 * Each coordinate <SPAN CLASS="MATH"><I>u</I><SUB>i, j</SUB></SPAN> is assumed to be in the unit interval <SPAN CLASS="MATH">[0, 1]</SPAN>.
 * Whether the values 0 and 1 can occur may depend on the
 * actual implementation of the point set.
 * 
 * <P>
 * All points have the same number of coordinates (their <SPAN  CLASS="textit">dimension</SPAN>)
 * and this number can be queried by {@link #getDimension getDimension}.
 * The number of points is queried by {@link #getNumPoints getNumPoints}.
 * The points and coordinates are both numbered starting from 0
 * and their number can actually be infinite.
 * 
 * <P>
 * The {@link #iterator iterator} method provides a <SPAN  CLASS="textit">point set iterator</SPAN>
 * which permits one to enumerate the points and their coordinates.
 * Several iterators over the same point set can coexist at any given time.
 * These iterators are instances of a hidden inner-class that implements
 * the {@link PointSetIterator} interface.
 * The default implementation of iterator provided here relies on
 * the method {@link #getCoordinate getCoordinate} to access the coordinates directly.
 * However, this approach is rarely efficient.
 * Specialized implementations that dramatically improve the performance
 * are provided in subclasses of {@link PointSet}.
 * The {@link PointSetIterator} interface actually extends the
 * {@link RandomStream} interface, so that the
 * iterator can also be seen as a {@link RandomStream} and used wherever
 * such a stream is required for generating uniform random numbers.
 * This permits one to easily replace pseudorandom numbers by the
 * coordinates of a selected set of highly-uniform points, i.e.,
 * to replace Monte Carlo by quasi-Monte Carlo in a simulation program.
 * 
 * <P>
 * This abstract class has only one abstract method:
 * {@link #getCoordinate getCoordinate}.
 * Providing an implementation for this method is already sufficient
 * for the subclass to work.
 * However, in practically all cases, efficiency can be dramatically improved by
 * overwriting {@link #iterator iterator} to provide a custom iterator that does not
 * necessarily rely on {@link #getCoordinate getCoordinate}.
 * In fact, direct use of {@link #getCoordinate getCoordinate} to access the coordinates
 * is discouraged.
 * One should access the coordinates only via the iterators.
 * 
 */
public abstract class PointSet  {

   // The maximum number of usable bits (binary digits).
   // Since Java has no unsigned type, the
   // 32nd bit cannot be used efficiently. This mainly affects digit
   // scrambling and bit vectors. This also limits the maximum number
   // of columns for the generating matrices of digital nets in base 2.
   protected static final int MAXBITS = 31;
   // To avoid 0 for nextCoordinate when random shifting 
   protected double EpsilonHalf = 1.0 / Num.TWOEXP[55];  // 1/2^55

   protected int dim = 0;
   protected int numPoints = 0;
   protected int dimShift = 0;            // Current dimension of the shift.
   protected int capacityShift = 0;       // Number of array elements of shift;
                                          // it is always >= dimShift
   protected RandomStream shiftStream;    // Used to generate random shifts.



   /**
    * Returns the dimension (number of available coordinates) of the point set.
    *    If the dimension is actually infinite, <TT>Integer.MAX_VALUE</TT> is returned.
    *  
    * @return the dimension of the point set or <TT>Integer.MAX_VALUE</TT>
    *    if it is infinite
    * 
    */
   public int getDimension() {
      return dim;
   }


   /**
    * Returns the number of points.
    *    If this number is actually infinite, <TT>Integer.MAX_VALUE</TT> is returned.
    *  
    * @return the number of points in the point set or <TT>Integer.MAX_VALUE</TT>
    *    if the point set has an infinity of points.
    * 
    */
   public int getNumPoints() {
      return numPoints;
   }


   /**
    * Returns <SPAN CLASS="MATH"><I>u</I><SUB>i, j</SUB></SPAN>, the coordinate <SPAN CLASS="MATH"><I>j</I></SPAN> of the point <SPAN CLASS="MATH"><I>i</I></SPAN>.
    * 
    * @param i index of the point to look for
    * 
    *    @param j index of the coordinate to look for
    * 
    *    @return the value of <SPAN CLASS="MATH"><I>u</I><SUB>i, j</SUB></SPAN>
    * 
    */
   public abstract double getCoordinate (int i, int j);


   /**
    * Constructs and returns a point set iterator.
    *  The default implementation returns an iterator that uses the method
    *  {@link #getCoordinate getCoordinate}&nbsp;<TT>(i,j)</TT> to iterate over the
    *  points and coordinates, but subclasses can reimplement it
    *  for better efficiency.
    * 
    * @return point set iterator for the point set
    * 
    */
   public PointSetIterator iterator() {
      return new DefaultPointSetIterator();
   }


   /**
    * Sets the random stream used to generate random shifts to <TT>stream</TT>.
    *  
    * @param stream the new random stream
    * 
    * 
    */
   public void setStream (RandomStream stream) {
      shiftStream = stream;
  }


   /**
    * Returns the random stream used to generate random shifts.
    *  
    * @return the random stream used
    * 
    */
   public RandomStream getStream() {
      return shiftStream;
  }


   /**
    * Randomizes the point set using the given <TT>rand</TT>.
    * 
    * @param rand {@link PointSetRandomization} to use
    * 
    * 
    */
   public void randomize (PointSetRandomization rand)  {
       rand.randomize(this);
   }


   /**
    * This method does nothing for this generic class.
    *   In some subclasses, it adds a random shift to all the points
    *   of the point set, using stream <TT>stream</TT> to generate the random numbers,
    *   for coordinates <TT>d1</TT> to <TT>d2-1</TT>.
    * 
    */
   public void addRandomShift (int d1, int d2, RandomStream stream) {
//   throw new UnsupportedOperationException
//         ("addRandomShift in PointSet called");
     System.out.println (
        "******* WARNING:  addRandomShift in PointSet does nothing");
   }


   /**
    * This method does nothing for this generic class.
    *  Similar to <TT>addRandomShift (0, d2, stream)</TT>,
    *   with <TT>d2</TT> the dimension of the current random shift.
    * 
    * 
    */
   public void addRandomShift (RandomStream stream) {
      addRandomShift (0, dimShift, stream);
  }


   /**
    * Similar to <TT>addRandomShift(d1, d2, stream)</TT>, with
    *   the current random stream.
    */
   @Deprecated
   public void addRandomShift (int d1, int d2) {
      addRandomShift (d1, d2, shiftStream);
  }


   @Deprecated
   public void addRandomShift () {
      addRandomShift (0, dimShift, shiftStream);
   }


   /**
    * Erases the current random shift, if any.
    * 
    */
   public void clearRandomShift() {
      capacityShift = 0;
      dimShift = 0;
//      shiftStream = null;
  }


   /**
    * By default, this method simply calls
    *   <TT>addRandomShift(d1, d2, stream)</TT>.
    * 
    */
   public void randomize (int d1, int d2, RandomStream stream) {
      addRandomShift (d1, d2, stream);
   }


   /**
    * By default, this method simply calls
    *    <TT>addRandomShift(stream)</TT>.
    * 
    * 
    */
   public void randomize (RandomStream stream) {
      addRandomShift (stream);
  }


   /**
    * By default, this method simply calls <TT>addRandomShift(d1, d2)</TT>.
    * 
    */
   @Deprecated
   public void randomize (int d1, int d2) {
      addRandomShift (d1, d2);
  }


   @Deprecated
   public void randomize () {
      addRandomShift();
   }


   /**
    * By default, this method simply calls
    *    <TT>clearRandomShift()</TT>.
    * 
    */
   public void unrandomize() {
      clearRandomShift();
  }


   /**
    * Formats a string that contains information about the point set.
    * 
    * @return string representation of the point set information
    * 
    */
   public String toString()  {
       StringBuffer sb = new StringBuffer ("Number of points: ");
       int x = getNumPoints();
       if (x == Integer.MAX_VALUE)
          sb.append ("infinite");
       else
          sb.append (x);
       sb.append (PrintfFormat.NEWLINE + "Point set dimension: ");
       x = getDimension();
       if (x == Integer.MAX_VALUE)
          sb.append ("infinite");
       else
          sb.append (x);
       return sb.toString();
   }


   /**
    * Same as invoking {@link #formatPoints(int,int) formatPoints}<TT>(n, d)</TT> with <SPAN CLASS="MATH"><I>n</I></SPAN> and <SPAN CLASS="MATH"><I>d</I></SPAN> equal to the
    *    number of points and the dimension of this object, respectively.
    * 
    * @return string representation of all the points in the point set
    *    @exception UnsupportedOperationException if the number of points
    *       or dimension of the point set is infinite
    * 
    * 
    */
   public String formatPoints()  {
      PointSetIterator iter = iterator();
      return formatPoints (iter);
   }


   /**
    * Formats a string that displays the same information as returned by
    *   {@link #toString toString}, together with the first <SPAN CLASS="MATH"><I>d</I></SPAN> coordinates of the
    *    first <SPAN CLASS="MATH"><I>n</I></SPAN> points. If <SPAN CLASS="MATH"><I>n</I></SPAN> is larger than the number of points in the point
    *   set, it is reset to that number. If <SPAN CLASS="MATH"><I>d</I></SPAN> is larger than the dimension of the
    *  points, it is reset to that dimension. The points are printed in the
    *   simplest format, separated by spaces,  by calling the default iterator
    *    repeatedly.
    *  
    * @param n number of points
    * 
    *    @param d dimension
    * 
    *    @return string representation of first d coordinates of first n points
    *       in the point set
    * 
    */
   public String formatPoints (int n, int d)  {
      PointSetIterator iter = iterator();
      return formatPoints (iter, n, d);
   }


   /**
    * Same as invoking {@link #formatPoints(PointSetIterator,int,int) formatPoints}<TT>(iter, n, d)</TT>
    *     with <SPAN CLASS="MATH"><I>n</I></SPAN> and <SPAN CLASS="MATH"><I>d</I></SPAN> equal to the number of points and the dimension, respectively.
    * 
    * @param iter iterator associated to the point set
    * 
    *    @return string representation of all the points in the point set
    *    @exception UnsupportedOperationException if the number of points
    *       or dimension of the point set is infinite
    * 
    * 
    */
   public String formatPoints (PointSetIterator iter)  {
      int n = getNumPoints();
      if (n == Integer.MAX_VALUE)
         throw new UnsupportedOperationException (
            "Number of points is infinite");
      int d = getDimension();
      if (d == Integer.MAX_VALUE)
         throw new UnsupportedOperationException ("Dimension is infinite");
      return formatPoints (iter, n, d);
   }


   /**
    * Same as invoking {@link #formatPoints(int,int) formatPoints}<TT>(n, d)</TT>, but
    *    prints the points  by calling <TT>iter</TT> repeatedly. The order of
    *    the printed points may be different than the one resulting from the
    *   default iterator.
    * 
    * @param iter iterator associated to the point set
    * 
    *   @param n number of points
    * 
    *   @param d dimension
    * 
    *   @return string representation of first d coordinates of first n points
    *       in the point set
    * 
    */
   public String formatPoints (PointSetIterator iter, int n, int d)  {
      if (getNumPoints() < n)
         n = getNumPoints();
      if (getDimension() < d)
         d = getDimension();
      StringBuffer sb = new StringBuffer (toString());
      sb.append (PrintfFormat.NEWLINE + PrintfFormat.NEWLINE
                 + "Points of the point set:" + PrintfFormat.NEWLINE);
      for (int i=0; i<n; i++) {
        for (int j=0; j<d; j++) {
            sb.append ("  ");
            sb.append (iter.nextCoordinate());
         }
         sb.append (PrintfFormat.NEWLINE);
         iter.resetToNextPoint();
      }
      return sb.toString();
   }


   /**
    * Similar to {@link #formatPoints formatPoints}<TT>()</TT>, but the
    * points coordinates are printed in base <SPAN CLASS="MATH"><I>b</I></SPAN>.
    * 
    * @param b base
    * 
    *    @return string representation of all the points in the point set
    *    @exception UnsupportedOperationException if the number of points
    *       or dimension of the point set is infinite
    * 
    * 
    */
   public String formatPointsBase (int b)  {
      PointSetIterator iter = iterator();
      return formatPointsBase (iter, b);
   }


   /**
    * Similar to {@link #formatPoints(int,int) formatPoints}<TT>(n, d)</TT>, but the
    *  points coordinates are printed in base <SPAN CLASS="MATH"><I>b</I></SPAN>.
    * 
    * @param n number of points
    * 
    *    @param d dimension
    * 
    *    @param b base
    * 
    *    @return string representation of first d coordinates of first n points
    *       in the point set
    * 
    */
   public String formatPointsBase (int n, int d, int b)  {
      PointSetIterator iter = iterator();
      return formatPointsBase(iter, n, d, b);
   }


   /**
    * Similar to
    * {@link #formatPoints(PointSetIterator) formatPoints}<TT>(iter)</TT>,
    * but the points coordinates are printed in base <SPAN CLASS="MATH"><I>b</I></SPAN>.
    * 
    * @param iter iterator associated to the point set
    * 
    *    @param b base
    * 
    *    @return string representation of all the points in the point set
    *    @exception UnsupportedOperationException if the number of points
    *       or dimension of the point set is infinite
    * 
    * 
    */
   public String formatPointsBase (PointSetIterator iter, int b)  {
      int n = getNumPoints();
      if (n == Integer.MAX_VALUE)
         throw new UnsupportedOperationException (
            "Number of points is infinite");
      int d = getDimension();
      if (d == Integer.MAX_VALUE)
         throw new UnsupportedOperationException ("Dimension is infinite");
      return formatPointsBase (iter, n, d, b);
   }


   /**
    * Similar to
    * {@link #formatPoints(PointSetIterator,int,int) formatPoints}<TT>(iter, n, d)</TT>,
    * but the points coordinates are printed in base <SPAN CLASS="MATH"><I>b</I></SPAN>.
    * 
    * @param iter iterator associated to the point set
    * 
    *   @param n number of points
    * 
    *   @param d dimension
    * 
    *   @param b base
    * 
    *   @return string representation of first d coordinates of first n points
    *       in the point set
    * 
    */
   public String formatPointsBase (PointSetIterator iter, int n, int d, int b)  {
      if (getNumPoints() < n)
         n = getNumPoints();
      if (getDimension() < d)
         d = getDimension();
      StringBuffer sb = new StringBuffer (toString());
      sb.append (PrintfFormat.NEWLINE + PrintfFormat.NEWLINE
                 + "Points of the point set:" + PrintfFormat.NEWLINE);
      double x;
      int acc = 10;
      if (b == 2)
         acc = 20;
      else if (b == 3)
         acc = 13;
      else
         acc = 10;
      if (null != shiftStream)
         acc += 6;
      int width = acc + 3;
      String chaine;
      for (int i=0; i<n; i++) {
        for (int j=0; j<d; j++) {
            sb.append ("  ");
            x = iter.nextCoordinate();
            chaine = PrintfFormat.formatBase (-width, acc, b, x);
            sb.append (chaine);
         }
         sb.append (PrintfFormat.NEWLINE);
         iter.resetToNextPoint();
      }
      return sb.toString();
   }


   /**
    * Same as invoking {@link #formatPointsNumbered(int,int) formatPointsNumbered}<TT>(n, d)</TT>
    *   with <SPAN CLASS="MATH"><I>n</I></SPAN> and <SPAN CLASS="MATH"><I>d</I></SPAN> equal to the number of points and the dimension,
    *    respectively.
    * 
    * @return string representation of all the points in the point set
    *    @exception UnsupportedOperationException if the number of points
    *       or dimension of the point set is infinite
    * 
    * 
    */
   public String formatPointsNumbered()  {
      int n = getNumPoints();
      if (n == Integer.MAX_VALUE)
         throw new UnsupportedOperationException (
            "Number of points is infinite");
      int d = getDimension();
      if (d == Integer.MAX_VALUE)
         throw new UnsupportedOperationException ("Dimension is infinite");
      return formatPointsNumbered (n, d);
   }


   /**
    * Same as invoking {@link #formatPoints(int,int) formatPoints}<TT>(n,d)</TT>, except that the points are numbered.
    * 
    * @param n number of points
    * 
    *   @param d dimension
    * 
    *   @return string representation of first d coordinates of first n points
    *       in the point set
    * 
    */
   public String formatPointsNumbered (int n, int d)  {
      if (getNumPoints() < n)
         n = getNumPoints();
      if (getDimension() < d)
         d = getDimension();
      StringBuffer sb = new StringBuffer (toString());
      PointSetIterator itr = iterator();
      sb.append (PrintfFormat.NEWLINE + PrintfFormat.NEWLINE
                 + "Points of the point set:");
      for (int i=0; i<n; i++) {
         sb.append (PrintfFormat.NEWLINE + "Point " +
    //                itr.getCurPointIndex() + " = (");
                                           i + "  =  (");
         boolean first = true;
         for (int j=0; j<d; j++) {
            if (first)
               first = false;
            else
               sb.append (", ");
            sb.append (itr.nextCoordinate());
         }
         sb.append (")");
         itr.resetToNextPoint();
      }
      return sb.toString();
   }



// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// This class implements a default point set iterator.
// Since it is inherited by subclasses, it can be used as a base class
// for iterators.
// It is implemented as an inner class because it can then use directly
// the variables of the PointSet class.  It would be more difficult and
// cumbersome to access those variables if it was implemented as a
// separate class.

   protected class DefaultPointSetIterator implements PointSetIterator {

      protected int curPointIndex = 0;      // Index of the current point.
      protected int curCoordIndex = 0;      // Index of the current coordinate.
      protected double EpsilonHalf = 1.0 / Num.TWOEXP[55];
   // protected double EpsilonHalf = PointSet.this.EpsilonHalf;

      protected void outOfBounds () {
         if (getCurPointIndex() >= numPoints)
            throw new NoSuchElementException ("Not enough points available");
         else
            throw new NoSuchElementException ("Not enough coordinates available");
      }

      public void setCurCoordIndex (int j) {
         curCoordIndex = j;
      }

      public void resetCurCoordIndex() {
         setCurCoordIndex (0);
      }

      public int getCurCoordIndex() {
        return curCoordIndex;
      }

      public boolean hasNextCoordinate() {
        return getCurCoordIndex() < getDimension();
      }

      public double nextCoordinate() {
         if (getCurPointIndex() >= numPoints || getCurCoordIndex() >= dim)
            outOfBounds();
         return getCoordinate (curPointIndex, curCoordIndex++);
      }

      public void nextCoordinates (double p[], int d)  {
         if (getCurCoordIndex() + d > getDimension()) outOfBounds();
         for (int j = 0; j < d; j++)
            p[j] = nextCoordinate();
      }

      // This is called with i = numPoints when nextPoint generates the
      // last point, so i = numPoints must be allowed.
      // The "no more point" error will be raised if we ask for
      // a new coordinate or point.
      public void setCurPointIndex (int i) {
         curPointIndex = i;
         resetCurCoordIndex();
      }

      public void resetCurPointIndex() {
         setCurPointIndex (0);
      }

      public int resetToNextPoint() {
         setCurPointIndex (curPointIndex + 1);
         return curPointIndex;
      }

      public int getCurPointIndex() {
        return curPointIndex;
      }

      public boolean hasNextPoint() {
        return getCurPointIndex() < getNumPoints();
      }

      public int nextPoint (double p[], int d) {
         resetCurCoordIndex();
         nextCoordinates (p, d);
         return resetToNextPoint();
      }


      public void resetStartStream() {     // Same as resetCurPointIndex();
         resetCurPointIndex();
      }

      public void resetStartSubstream() {  // Same as resetCurCoordIndex();
         resetCurCoordIndex();
      }

      public void resetNextSubstream() {   // Same as resetToNextPoint();
         resetToNextPoint();
      }

      public void setAntithetic (boolean b) {
         throw new UnsupportedOperationException();
      }

      public double nextDouble() {          // Same as nextCoordinate();
         return nextCoordinate();
      }

      public void nextArrayOfDouble (double[] u, int start, int n) {
         if (n < 0)
            throw new IllegalArgumentException ("n must be positive.");
         for (int i = start; i < start+n; i++)
            u[i] = nextDouble();
      }

      public int nextInt (int i, int j) {
         return (i + (int)(nextDouble() * (j - i + 1.0)));
      }

      public void nextArrayOfInt (int i, int j, int[] u, int start, int n) {
         if (n < 0)
            throw new IllegalArgumentException ("n must be positive.");
         for (int k = start; k < start+n; k++)
            u[k] = nextInt (i, j);
      }

      public String formatState() {
         return "Current point index: " + getCurPointIndex() +
              PrintfFormat.NEWLINE + "Current coordinate index: " +
                  getCurCoordIndex();
      }
   }
}
