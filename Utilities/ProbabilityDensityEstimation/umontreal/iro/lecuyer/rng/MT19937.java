

/*
 * Class:        MT19937
 * Description:  Mersenne Twister proposed by Matsumoto and Nishimura
                 with a state size of 19937 bits
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

package umontreal.iro.lecuyer.rng;

import umontreal.iro.lecuyer.util.PrintfFormat;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;

/**
 * Implements the {@link RandomStream} interface via inheritance from
 * {@link RandomStreamBase}. The backbone generator is the MT19937
 * Mersenne Twister, proposed by Matsumoto and Nishimura, which has a state size of 19937 bits and a period length of
 * approximatively  <SPAN CLASS="MATH">2<SUP>19937</SUP></SPAN>.
 * Each instance uses another {@link CloneableRandomStream} to fill its initial state.
 * With this design, the initial states of successive streams are not
 * spaced by an equal number of steps, and there is no guarantee that
 * different streams do not overlap, but damaging overlap is unlikely
 * because of the huge size of the state space.
 * The seed of the RNG, and the state of a stream at any given
 * step, is a 624-dimensional vector of 32-bit integers.
 * The output of <TT>nextValue</TT> has 32 bits of precision.
 * 
 */
public class MT19937 extends RandomStreamBase  {

   private static final long serialVersionUID = 70510L;
   //La date de modification a l'envers, lire 10/05/2007

   private static final double NORM = 1.0 / 0x100000001L;   // 1/(2^32 + 1)

   private static final int N = 624;
   private static final int M = 397;
   private static final int[] MULT_MATRIX_A = {0x0, 0x9908B0DF};
   private static final int UPPER_MASK = 0x80000000;
   private static final int LOWER_MASK = 0x7FFFFFFF;

   private int[] state;
   private int state_i;

   private CloneableRandomStream seedRng;

   private void fillSeed() {
      state_i = N;

      for(int i = 0; i < N; i++)
         state[i] = (int)((long)(seedRng.nextDouble() * 0x100000000L));
   }
 

   /**
    * Constructs a new stream, using <TT>rng</TT> to fill its initial state.
    * 
    * @param rng used to build the seed
    * 
    * 
    */
   public MT19937 (CloneableRandomStream rng)  {
      seedRng = rng;
      name = null;

      state = new int[N];

      //note : on pourrait directement appeler fillSeed, sauf qu'il y aurait
      //       des incoherences si le rng a deja ete appele
      resetStartStream();
   } 


   /**
    * Constructs a new stream with the identifier <TT>name</TT>
    *   (used in the <TT>toString</TT> method).
    * 
    * @param rng used to build the seed
    * 
    *   @param name name of the stream
    * 
    */
   public MT19937 (CloneableRandomStream rng, String name)  {
      this(rng);
      this.name = name;
   } 

   /**
    * Clones the current generator and return its copy.
    *  
    *  @return A deep copy of the current generator
    * 
    */
   public MT19937 clone()  {
      MT19937 retour = null;
      retour = (MT19937)super.clone();
      retour.state = new int[N];
      for (int i = 0; i<N; i++) {
         retour.state[i] = state[i];
      }
      retour.seedRng = seedRng.clone();
      return retour;
   }

   public void resetStartStream() {
      seedRng.resetStartStream();
      fillSeed();
   }

   public void resetStartSubstream() {
      seedRng.resetStartSubstream();
      fillSeed();
   }

   public void resetNextSubstream() {
      seedRng.resetNextSubstream();
      fillSeed();
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      if(name == null)
         sb.append("This MT19937 ");
      else
         sb.append(name + " ");
      sb.append("has a " + seedRng.getClass() + " for its seed." +
         PrintfFormat.NEWLINE);

      sb.append("State = [");
      for(int i = 0; i < N-1; i++)
         sb.append(state[i] + ",");
      sb.append(state[N-1] + "]. ");
      sb.append("State index = " + state_i);

      return sb.toString();
   }

   protected double nextValue() {
      int y;

      if(state_i >= N) {
         int kk;

         for(kk=0; kk < N - M; kk++) {
            y = (state[kk] & UPPER_MASK) | (state[kk+1] & LOWER_MASK);
            state[kk] = state[kk + M] ^ (y >>> 1) ^
                        MULT_MATRIX_A[y & 0x1];
         }
         for(; kk < N - 1; kk++) {
            y = (state[kk] & UPPER_MASK) | (state[kk+1] & LOWER_MASK);
            state[kk] = state[kk + (M - N)] ^ (y >>> 1) ^
                        MULT_MATRIX_A[y & 0x1];
         }
         y = (state[N-1] & UPPER_MASK) | (state[0] & LOWER_MASK);
         state[N-1] = state[M-1] ^ (y >>> 1) ^
                      MULT_MATRIX_A[y & 0x1];

         state_i = 0;
      }

      y = state[state_i++];

      // Tempering */
      y ^= (y >>> 11);
      y ^= (y << 7) & 0x9d2c5680;
      y ^= (y << 15) & 0xefc60000;
      y ^= (y >>> 18);

      long r = (y <= 0) ? y + 0x100000000L : y;

      return r * NORM;
   }


}
