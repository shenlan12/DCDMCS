

/*
 * Class:        NiedSequenceBase2
 * Description:  digital Niederreiter sequences in base 2.
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

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import umontreal.iro.lecuyer.util.PrintfFormat;


/**
 * This class implements digital sequences constructed from the
 * Niederreiter sequence in base 2.
 * 
 */
public class NiedSequenceBase2 extends DigitalSequenceBase2 { 

   private static final int MAXDIM = 318;  // Maximum dimension.
   private static final int NUMCOLS = 30;  // Maximum number of columns.
 
 

   /**
    * Constructs a new digital sequence in base 2 from the first <SPAN CLASS="MATH"><I>n</I> = 2<SUP>k</SUP></SPAN> points 
    *     of the Niederreiter sequence,
    *     with <SPAN CLASS="MATH"><I>w</I></SPAN> output digits, in <TT>dim</TT> dimensions.
    *     The generator matrices 
    * <SPAN CLASS="MATH"><B>C</B><SUB>j</SUB></SPAN> are <SPAN CLASS="MATH"><I>w</I>&#215;<I>k</I></SPAN>.
    * Restrictions: 
    * <SPAN CLASS="MATH">0&nbsp;&lt;=&nbsp;<I>k</I>&nbsp;&lt;=&nbsp;30</SPAN>, <SPAN CLASS="MATH"><I>k</I>&nbsp;&lt;=&nbsp;<I>w</I></SPAN>, and <TT>dim</TT> <SPAN CLASS="MATH">&nbsp;&lt;=&nbsp;318</SPAN>.
    * 
    * @param k there will be 2^k points
    * 
    *    @param w number of output digits
    * 
    *    @param dim dimension of the point set
    * 
    * 
    */
   public NiedSequenceBase2 (int k, int w, int dim)  {
      init (k, w, w, dim);
   } 



   public String toString() {
      StringBuffer sb = new StringBuffer ("Niederreiter sequence:" +
                                           PrintfFormat.NEWLINE);
      sb.append (super.toString());
      return sb.toString();
   }

   private void init (int k, int r, int w, int dim) {
      if ((dim < 1) || (dim > MAXDIM))
         throw new IllegalArgumentException 
            ("Dimension for NiedSequenceBase2 must be > 1 and <= " + MAXDIM);
      if (r < k || w < r || w > MAXBITS || k >= MAXBITS) 
         throw new IllegalArgumentException
            ("One must have k < 31 and k <= r <= w <= 31 for NiedSequenceBase2");
      numCols   = k;
      numRows   = r;   // Unused!
      outDigits = w;
      numPoints = (1 << k);
      this.dim  = dim;
      normFactor = 1.0 / ((double) (1L << (outDigits)));
      genMat = new int[dim * numCols];
      initGenMat();
   }


   public void extendSequence (int k) {
      init (k, numRows, outDigits, dim);
   }


   // Initializes the generator matrices for a sequence. 
   /* I multiply by 2 because the relevant columns are in the 30 least 
      significant bits of NiedMat, but if I understand correctly,
      SSJ assumes that they are in bits [31, ..., 1]. 
      Then I shift right if w < 31. */
   private void initGenMat ()  {
      for (int j = 0; j < dim; j++)
         for (int c = 0; c < numCols; c++) {
            genMat[j*numCols + c] = NiedMat[j*NUMCOLS + c] << 1;
            genMat[j*numCols + c] >>= MAXBITS - outDigits;
         }
   }

/*
   // Initializes the generator matrices for a net. 
   protected void initGenMatNet()  {
      int j, c;

      // the first dimension, j = 0.
      for (c = 0; c < numCols; c++)
         genMat[c] = (1 << (outDigits-numCols+c));

      for (j = 1; j < dim; j++)
         for (c = 0; c < numCols; c++)
            genMat[j*numCols + c] = 2 * NiedMat[(j - 1)*NUMCOLS + c];
   }
*/

   // ****************************************************************** 
   // Generator matrices of Niederreiter sequence. 
   // This array stores explicitly NUMCOLS columns in 318 dimensions.

   private static int[] NiedMat;
   private static final int MAXN = 9540;

   static {
      NiedMat = new int[MAXN];

      try {
         InputStream is = 
            NiedSequenceBase2.class.getClassLoader().getResourceAsStream (
            "umontreal/iro/lecuyer/hups/dataSer/Nieder/NiedSequenceBase2.ser");
         if (is == null)
            throw new FileNotFoundException (
               "Cannot find NiedSequenceBase2.ser");
         ObjectInputStream ois = new ObjectInputStream(is);
         NiedMat = (int[]) ois.readObject();
         ois.close();

      } catch(FileNotFoundException e) {
         e.printStackTrace();
         System.exit(1);

      } catch(IOException e) {
         e.printStackTrace();
         System.exit(1);

      } catch(ClassNotFoundException e) {
         e.printStackTrace();
         System.exit(1);
      }
   }

}

