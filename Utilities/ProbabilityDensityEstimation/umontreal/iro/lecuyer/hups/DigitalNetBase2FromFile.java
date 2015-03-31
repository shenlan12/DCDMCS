

/*
 * Class:        DigitalNetBase2FromFile
 * Description:  read the parameters defining a digital net in base 2
                 from a file or from a URL address
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

import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import umontreal.iro.lecuyer.util.PrintfFormat;


/**
 * This class allows us to read the parameters defining a digital net
 *  <EM>in base 2</EM> either from a file, or from a URL address on the
 *  World Wide Web. See the documentation in
 * {@link DigitalNetFromFile DigitalNetFromFile}.
 * The parameters used in building the net are those defined in class
 * {@link DigitalNetBase2 DigitalNetBase2}.
 *  The format of the data files must be the following (where <SPAN CLASS="MATH"><I>B</I></SPAN> is any <SPAN CLASS="MATH"><I>C</I><SUB>j</SUB></SPAN>):
 * (see the format in <TT>guidehups.pdf</TT>)
 * <DIV ALIGN="CENTER">
 * 1#1
 * </DIV>
 * 
 * <P>
 * For each dimension <SPAN CLASS="MATH"><I>j</I></SPAN>, there must be a <SPAN CLASS="MATH"><I>k</I></SPAN>-vector of 32-bit integers
 *  (the <SPAN CLASS="MATH"><I>a</I><SUB>i</SUB></SPAN>) corresponding to the columns of 
 * <SPAN CLASS="MATH"><B>C</B><SUB>j</SUB></SPAN>. The
 *   correspondance is such that integer
 *   
 * <SPAN CLASS="MATH"><I>a</I><SUB>i</SUB> = 2<SUP>30</SUP>(<B>C</B><SUB>j</SUB>)<SUB>1i</SUB> +2<SUP>29</SUP>(<B>C</B><SUB>j</SUB>)<SUB>2i</SUB> + <SUP> ... </SUP> +2<SUP>31-r</SUP>(<B>C</B><SUB>j</SUB>)<SUB>ri</SUB></SPAN>.
 * 
 */
public class DigitalNetBase2FromFile extends DigitalNetBase2  {
   private String filename;

   // s is the effective dimension if > 0, otherwise it is dim
   private void readData (Reader re, int r1, int s1)
       throws IOException, NumberFormatException
   {
      try {
         StreamTokenizer st = new StreamTokenizer (re);
         if (st == null) return;
         st.eolIsSignificant (false);
         st.slashSlashComments (true);

         int i = st.nextToken ();
         if (i != StreamTokenizer.TT_NUMBER)
            throw new NumberFormatException();
         b = (int) st.nval;
         st.nextToken ();   numCols = (int) st.nval;
         st.nextToken ();   numRows = (int) st.nval;
         st.nextToken ();   numPoints = (int) st.nval;
         st.nextToken ();   dim = (int) st.nval;
         if (dim < 1) {
            System.err.println (PrintfFormat.NEWLINE +
                "DigitalNetBase2FromFile:   dimension dim <= 0");
            throw new IllegalArgumentException ("dimension dim <= 0");
         }
         if (r1 > numRows)
            throw new IllegalArgumentException (
            "DigitalNetBase2FromFile:   One must have   r1 <= Max num rows");
         if (s1 > dim) {
            throw new IllegalArgumentException ("s1 is too large");
         }
         if (s1 > 0)
            dim = s1;
         if (r1 > 0)
            numRows = r1;

         if (b != 2) {
            System.err.println (
              "***** DigitalNetBase2FromFile:    only base 2 allowed");
            throw new IllegalArgumentException ("only base 2 allowed");
         }
         genMat = new int[dim * numCols];
         for (i = 0; i < dim; i++)
            for (int c = 0; c < numCols; c++) {
                st.nextToken ();
                genMat[i*numCols + c] = (int) st.nval;
            }

      } catch (NumberFormatException e) {
         System.err.println (
            "   DigitalNetBase2FromFile:   not a number  " + e);
         throw e;
      }
   }


    private void maskRows (int r, int w) {
       // Keep only the r most significant bits and set the others to 0.
       int mask = (int) ((1L << r) - 1);
       mask <<= MAXBITS - r;
       for (int i = 0; i < dim; i++)
          for (int c = 0; c < numCols; c++) {
              genMat[i*numCols + c] &= mask;
              genMat[i*numCols + c] >>= MAXBITS - w;
          }
    }



   /**
    * Constructs a digital net in base 2 after reading its parameters from file
    *     <TT>filename</TT>. See the documentation in
    *   {@link DigitalNetFromFile DigitalNetFromFile}.
    *    Parameter <TT>w</TT> gives the number of bits of resolution, <TT>r1</TT> is
    *    the number of rows, and <TT>s1</TT> is the dimension.
    *    Restrictions: <TT>s1</TT> must be less than the maximal dimension, and
    *    <TT>r1</TT> less than the maximal number of rows in the data file.
    *    Also <TT>w</TT> <SPAN CLASS="MATH">&nbsp;&gt;=&nbsp;</SPAN> <TT>r1</TT>.
    * 
    * @param filename Name of the file to be read
    * 
    *    @param r1 Number of rows for the generating matrices
    * 
    *    @param w Number of bits of resolution
    * 
    *    @param s1 Number of dimensions
    * 
    * 
    */
   public DigitalNetBase2FromFile (String filename, int r1, int w, int s1)
         throws IOException, MalformedURLException
   {
      super ();
      if (w < r1 || w > MAXBITS)
         throw new IllegalArgumentException (" Must have numRows <= w <= 31");

      BufferedReader input;
      if (filename.startsWith("http:") || filename.startsWith("ftp:"))
         input = DigitalNetFromFile.openURL(filename);
      else
         input = DigitalNetFromFile.openFile(filename);

      try {
         readData (input, r1, s1);
      } catch (NumberFormatException e) {
         System.err.println (
            "   DigitalNetBase2FromFile:   cannot read from   " + filename);
         throw e;

      }  catch (IOException e) {
         System.err.println (
            "   DigitalNetBase2FromFile:  cannot read from  " + filename);
         throw e;
      }
      input.close();
      maskRows (numRows, w);
      outDigits = w;
      if (numCols >= MAXBITS)
         throw new IllegalArgumentException (" Must have numCols < 31");

      this.filename = filename;
      int x = (1 << numCols);
      if (x != numPoints) {
         System.out.println ("numPoints != 2^k");
         throw new IllegalArgumentException ("numPoints != 2^k");
      }
      // Compute the normalization factors.
      normFactor = 1.0 / ((double) (1L << (outDigits)));

  }


   /**
    * Same as {@link #DigitalNetBase2FromFile DigitalNetBase2FromFile}<TT>(filename, r, 31, s1)</TT> where
    *    <TT>s1</TT> is the dimension and <TT>r</TT> is given in data file <TT>filename</TT>.
    * 
    * @param filename Name of the file to be read
    * 
    *    @param s1 Number of dimensions
    * 
    */
   public DigitalNetBase2FromFile (String filename, int s1)
        throws IOException, MalformedURLException
   {
       this (filename, -1, 31, s1);
   }


   public String toString() {
      StringBuffer sb = new StringBuffer ("File:  " + filename  +
         PrintfFormat.NEWLINE);
      sb.append (super.toString());
      return sb.toString();
   }

   /**
    * Writes the parameters and the generating matrices of this digital net
    *     to a string.
    *     This is useful to check that the file parameters have been read correctly.
    * 
    */
   public String toStringDetailed()  {
      StringBuffer sb = new StringBuffer (toString() + PrintfFormat.NEWLINE);
      sb.append ("dim = " + dim + PrintfFormat.NEWLINE);
      for (int i = 0; i < dim; i++) {
         sb.append (PrintfFormat.NEWLINE + "// dim = " + (1 + i) +
              PrintfFormat.NEWLINE);
         for (int c = 0; c < numCols; c++)
            sb.append  (genMat[i*numCols + c]  + PrintfFormat.NEWLINE);
      }
      sb.append ("--------------------------------" + PrintfFormat.NEWLINE);
      return sb.toString ();
   }


   /**
    * Lists all files (or directories) in directory <TT>dirname</TT>. Only relative
    *   pathnames should be used. The files are  parameter files used in defining
    *   digital nets.  For example, calling <TT>listDir("")</TT> will give the list
    *   of the main data directory in SSJ, while calling <TT>listDir("Edel/OOA2")</TT>
    *   will give the list of all files in directory <TT>Edel/OOA2</TT>.
    * 
    */
   public static String listDir (String dirname) throws IOException  {
      return DigitalNetFromFile.listDir(dirname);
   }


}

