

/*
 * Class:        PlotFormat
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

package umontreal.iro.lecuyer.charts;

import   java.util.Formatter;
import   java.util.Locale;
import   java.util.StringTokenizer;
import   java.util.regex.Pattern;

import   java.io.Reader;
import   java.io.StringReader;
import   java.io.IOException;

import   org.jfree.data.xy.XYSeriesCollection;
import   org.jfree.data.category.CategoryDataset;
import   org.jfree.data.io.CSV;

/**
 * Provide tools to import and export data set tables
 * to and from Gnuplot, MATLAB and Mathematica compatible
 * formats, or customized format.
 * 
 */
public class PlotFormat {
   private PlotFormat() {}

   /**
    * Parses <TT>data</TT> according to the standard GNUPlot format and stores
    *   the extracted values in the returned table. All unrecognized characters
    *    and empty lines will be cut. Deleting comments inside the input
    *    <TT>String</TT> is recommended.
    * 
    * @param data String to parse.
    * 
    *    @return Table that represent data contained in <TT>data</TT>.
    *    @exception IllegalArgumentException if each line of the input <TT>String</TT> doesn't share the same number of values
    * 
    * 
    */
   public static double[][] fromGNUPlot (String data)  {
      int maxLineNumber = 16;
      int columnNumber = 0;
      int lineNumber = 0;
      double[][] retour = new double[1][1]; //initialisation temporaire

      Pattern p = Pattern.compile("\\n\\n*");
      String[] string = p.split(data); // A chaque String correspond une ligne

      for (int j = 0; j < string.length; j++) {
         if (string[j].charAt(0) != '#' && string[j].length() != 0) {
            //On ne traite pas les lignes de commentaires ni les lignes

            p = Pattern.compile("\\s\\s*");
            String[] result = p.split(string[j]);
            double[] line = new double[result.length];

            int k = 0; // Compte le nombre de valeurs sur la ligne courante
            for (int i = 0; i < result.length; i++) {
               try {
                  line[k] = Double.valueOf(result[i]);   // les chaines de caracteres qui ne sont pas des nombres sont ignorees
                  k++;
               } catch (NumberFormatException nfe) {}
            }

            if (k != 0) { // il y a des valeurs sur la ligne
               //La premiere ligne determine combien de colonnes aura notre tableau de retour
               if (lineNumber == 0) {
                  columnNumber = k;
                  retour = new double[columnNumber][maxLineNumber]; // Allocation du tableau de retour
               } else if ( columnNumber != k ) // Toutes les lignes de la String n'ont pas le meme nombre de valeurs
                  throw new IllegalArgumentException("Each line must have the same number of values");

               if (lineNumber >= maxLineNumber) { // Cas ou notre tableau initial n'est plus assez grand
                  maxLineNumber = maxLineNumber * 2;
                  double[][] temp = new double[columnNumber][maxLineNumber];
                  for (int i = 0; i < columnNumber; i++)
                     for (int l = 0; l < retour[0].length; l++)
                        temp[i][l] = retour[i][l];
                  retour = temp;
               }

               for (int i = 0; i < columnNumber; i++)
                  retour[i][lineNumber] = line[i];
               lineNumber++;
            }
         }
      }
      // Cree un tableau aux dimensions exactes
      double[][] temp;
      if (columnNumber == 0)
         temp = null;
      else
         temp = new double[columnNumber][lineNumber];
      for (int i = 0; i < columnNumber; i++)
         for (int l = 0; l < lineNumber; l++)
            temp[i][l] = retour[i][l];
      retour = null;
      return temp;
   }


   /**
    * Parses <TT>data</TT> according to the standard CSV format
    *    and stores the extracted values in the returned table.
    * 
    * @param data String to parse.
    * 
    *    @return Table that represent data contained in <TT>data</TT>.
    *    @exception IllegalArgumentException if each line of the input <TT>String</TT> doesn't share the same number of values
    * 
    * 
    */
   public static double[][] fromCSV (String data)  {
      int maxLineNumber = 16;
      int columnNumber = 0;
      int lineNumber = 0;
      double[][] retour = new double[1][1]; //initialisation temporaire

      Pattern p = Pattern.compile("\\n\\n*");
      String[] string = p.split(data); // A chaque String correspond une ligne

      for (int j = 0; j < string.length; j++) {
         if (string[j].length() != 0) {
            //On ne traite pas les lignes vides

            p = Pattern.compile(",,*");
            String[] result = p.split(string[j]);
            double[] line = new double[result.length];

            int k = 0; // Compte le nombre de valeurs sur la ligne courante
            for (int i = 0; i < result.length; i++) {
               try {
                  line[k] = Double.valueOf(result[i]);   // les chaines de caracteres qui ne sont pas des nombres sont ignorees
                  k++;
               } catch (NumberFormatException nfe) {}
            }

            if (k != 0) { // il y a des valeurs sur la ligne
               //La premiere ligne determine combien de colonnes aura notre tableau de retour
               if (lineNumber == 0) {
                  columnNumber = k;
                  retour = new double[columnNumber][maxLineNumber]; // Allocation du tableau de retour
               } else if ( columnNumber != k ) // Toutes les lignes de la String n'ont pas le meme nombre de valeurs
                  throw new IllegalArgumentException("Each line must have the same number of values");

               if (lineNumber >= maxLineNumber) { // Cas ou notre tableau initial n'est plus assez grand
                  maxLineNumber = maxLineNumber * 2;
                  double[][] temp = new double[columnNumber][maxLineNumber];
                  for (int i = 0; i < columnNumber; i++)
                     for (int l = 0; l < retour[0].length; l++)
                        temp[i][l] = retour[i][l];
                  retour = temp;
               }

               for (int i = 0; i < columnNumber; i++)
                  retour[i][lineNumber] = line[i];
               lineNumber++;
            }
         }
      }
      // Cree un tableau aux dimensions exactes
      double[][] temp;
      if (columnNumber == 0)
         temp = null;
      else
         temp = new double[columnNumber][lineNumber];
      for (int i = 0; i < columnNumber; i++)
         for (int l = 0; l < lineNumber; l++)
            temp[i][l] = retour[i][l];
      retour = null;
      return temp;
   }


   /**
    * Parses <TT>data</TT> according to a user defined format and stores the
    *  extracted values in the returned table. <TT>betweenValues</TT> sets
    *   characters between values on the same line and <TT>endLine</TT> sets
    *   characters which separates each line of the input data set. Usually, this
    *   parameter contains the classic end of line character <TT>%n</TT>.
    *    This method uses regular expressions, so it is possible to use regular
    *   characters as defined in the standard java API, specially in the class
    *   {@link Pattern} (package java.util.regex).
    * 
    * @param betweenValues <TT>String</TT> which separates values on the same line.
    * 
    *    @param endLine <TT>String</TT> which separates lines.
    * 
    *    @param data String to parse.
    * 
    *    @return Table that represent data contained in <TT>data</TT>.
    *    @exception IllegalArgumentException if each line of the input <TT>String</TT> doesn't share the same number of values
    * 
    */
   public static double[][] fromCustomizedFormat (String betweenValues,
                                           String endLine, String data)  {
      int maxLineNumber = 16;
      int columnNumber = 0;
      int lineNumber = 0;
      double[][] retour = new double[1][1]; //initialisation temporaire

      Pattern p = Pattern.compile("(" + endLine + ")(" + endLine + ")*");
      String[] string = p.split(data); // A chaque String correspond une ligne

      for (int j = 0; j < string.length; j++) {
         if (string[j].length() != 0) {
            //On ne traite pas les lignes vides

            p = Pattern.compile("(" + betweenValues + ")(" + betweenValues + ")*");
            String[] result = p.split(string[j]);
            double[] line = new double[result.length];

            int k = 0; // Compte le nombre de valeurs sur la ligne courante
            for (int i = 0; i < result.length; i++) {
               try {
                  line[k] = Double.valueOf(result[i]);   // les chaines de caracteres qui ne sont pas des nombres sont ignorees
                  k++;
               } catch (NumberFormatException nfe) {}
            }

            if (k != 0) { // il y a des valeurs sur la ligne
               //La premiere ligne determine combien de colonnes aura notre tableau de retour
               if (lineNumber == 0) {
                  columnNumber = k;
                  retour = new double[columnNumber][maxLineNumber]; // Allocation du tableau de retour
               } else if ( columnNumber != k ) // Toutes les lignes de la String n'ont pas le meme nombre de valeurs
                  throw new IllegalArgumentException("Each line must have the same number of values");

               if (lineNumber >= maxLineNumber) { // Cas ou notre tableau initial n'est plus assez grand
                  maxLineNumber = maxLineNumber * 2;
                  double[][] temp = new double[columnNumber][maxLineNumber];
                  for (int i = 0; i < columnNumber; i++)
                     for (int l = 0; l < retour[0].length; l++)
                        temp[i][l] = retour[i][l];
                  retour = temp;
               }

               for (int i = 0; i < columnNumber; i++)
                  retour[i][lineNumber] = line[i];
               lineNumber++;
            }
         }
      }
      // Cree un tableau aux dimensions exactes
      double[][] temp;
      if (columnNumber == 0)
         temp = null;
      else
         temp = new double[columnNumber][lineNumber];
      for (int i = 0; i < columnNumber; i++)
         for (int l = 0; l < lineNumber; l++)
            temp[i][l] = retour[i][l];
      retour = null;
      return temp;
   }


   /**
    * Stores data tables <TT>data</TT> into a <TT>String</TT>,
    *    with format understandable by GNUPlot.
    * 
    * @param data data tables.
    * 
    *    @return String that represent data tables in GNUPlot format.
    * 
    */
   public static String toGNUPlot (double[]... data)  {
      checkData(data);
      Formatter formatter = new Formatter(Locale.US);

      for (int i = 0; i < data[0].length; i++) {
         for (int j = 0; j < data.length; j++) {
            formatter.format("%20f", data[j][i]);
         }
         formatter.format("%n");
      }
      formatter.format("%n%n");
      return formatter.toString();
   }


   /**
    * Stores series collection <TT>data</TT> into a <TT>String</TT>,
    *    with format understandable by GNUPlot.
    * 
    * @param data data tables.
    * 
    *    @return String that represent data tables in GNUPlot format.
    * 
    */
   public static String toGNUPlot (XYSeriesCollection data)  {
      return toGNUPlot(toTable(data));
   }


   /**
    * Stores data tables <TT>data</TT> into a <TT>String</TT>
    *    with format CSV (comma-separated value tabular data).
    *    The output string could be imported from a file to a matrix
    *    into Mathematica with Mathematica's function <TT>Import["fileName", "CSV"]</TT>,
    *    or into MATLAB with MATLAB's function <TT>csvread('fileName')</TT>.
    * 
    * @param data data tables.
    * 
    *    @return String that represent data tables in CSV format.
    * 
    */
   public static String toCSV (double[]...data)  {
      checkData(data);
      Formatter formatter = new Formatter(Locale.US);

      for (int i = 0; i < data[0].length; i++) {
         for (int j = 0; j < data.length - 1; j++) {
            formatter.format("%-20f, ", data[j][i]);
         }
         formatter.format("%-20f%n", data[data.length-1][i]);      //le dernier
      }
      formatter.format("%n");

      return formatter.toString();
   }


   /**
    * Stores series collection <TT>data</TT> into a <TT>String</TT>
    *    with format CSV (comma-separated value tabular data).
    * 
    * @param data data tables.
    * 
    *    @return String that represent data tables in CSV format.
    * 
    */
   public static String toCSV (XYSeriesCollection data)  {
      return toCSV(toTable(data));
   }


   /**
    * Stores data tables <TT>data</TT> into a <TT>String</TT> with customized format.
    * <TT>heading</TT> sets the head of the returned <TT>String</TT>, <TT>footer</TT>
    * sets its end, <TT>betweenValues</TT> sets characters between values on the same
    *  line and finally <TT>endLine</TT> sets characters which separates each line of
    *  the input data set. Normally, this parameter contains the classic end of line
    *   character `%n'.
    * 
    * @param heading head of the returned <TT>String</TT>.
    * 
    *    @param footer end of the returned <TT>String</TT>.
    * 
    *    @param betweenValues <TT>String</TT> which separates values on the same line.
    * 
    *    @param endLine <TT>String</TT> which separates lines.
    * 
    *    @param data data tables.
    * 
    *    @return String that represent data tables in customized format.
    * 
    */
   public static String toCustomizedFormat (String heading, String footer,
                                   String betweenValues, String endLine,
                                   int precision, double[]...data)  {
      checkData(data);
      Formatter formatter = new Formatter(Locale.US);
      String myString = "%20."+ precision +"f";

      formatter.format("%s", heading);
      for (int i = 0; i < data[0].length; i++) {
         for (int j = 0; j < data.length - 1; j++) {
            formatter.format(myString+"%s", data[j][i], betweenValues);
         }
         formatter.format(myString+"%s", data[data.length-1][i], endLine);      //le dernier de la ligne
      }
      formatter.format("%s", footer);
      return formatter.toString();
   }


   /**
    * Stores data tables <TT>data</TT> into a <TT>String</TT>
    *    with customized format from an <TT>XYSeriesCollection</TT> variable.
    * 
    * @param heading head of the returned <TT>String</TT>.
    * 
    *    @param footer end of the returned <TT>String</TT>.
    * 
    *    @param betweenValues <TT>String</TT> which separates values on the same line.
    * 
    *    @param endLine <TT>String</TT> which separates lines.
    * 
    *    @param data data tables.
    * 
    *    @return String that represent data tables in customized format.
    * 
    */
   public static String toCustomizedFormat (String heading, String footer,
                                   String betweenValues, String endLine,
                                   int precision, XYSeriesCollection data)  {
      return toCustomizedFormat (heading, footer, betweenValues, endLine,
                       precision, toTable(data));
   }


   /* *
    * Converts a <TT>XYSeriesCollection</TT> object into a <TT>double[][]</TT>.
    */
   private static double[][] toTable (XYSeriesCollection data) {
      double[][] transform = new double[data.getSeriesCount()*2][];

      for(int i = 0; i<data.getSeriesCount(); i++) {
         double[][] temp = data.getSeries(i).toArray();
         transform[2*i] = temp[0];
         transform[2*i+1] = temp[1];
      }
      return transform;
   }


   /* *
    * Check the data table <TT>data</TT>.
    *
    * @exception  IllegalArgumentException   If the input tables doesn't have the same length.
    */
   private static void checkData(double[]...data) {
      for(int i = 0; i < data.length-1; i++) {
         if(data[i].length != data[i+1].length)
            throw new IllegalArgumentException("Data tables " + i + " and " + (i+1) + " must share the same length");
      }
   }

/* //Inutile si on exporte en CSV
   private static String mathematicaFormatPoint (double x, double y) {
      // Writes the pair (x, y) in returned string, in a format understood
      // by Mathematica
      StringBuffer sb = new StringBuffer();
      String S;

      sb.append ("   { ");
      if ((x != 0.0) && (x < 0.1 || x > 1.0)) {
         S = PrintfFormat.E (16, 7, x);
         int exppos = S.indexOf ('E');
         if (exppos != -1)
            S = S.substring (0, exppos) + "*10^(" +
                             S.substring (exppos+1) + ")";
      }
      else
         S = PrintfFormat.g (16, 8, x);

      sb.append (S + ",     ");

      if (y != 0.0 && (y < 0.1 || y > 1.0)) {
         S = PrintfFormat.E (16, 7, y);
         int exppos = S.indexOf ('E');
         if (exppos != -1)
            S = S.substring (0, exppos) + "*10^(" +
                             S.substring (exppos+1) + ")";
      }
      else
        S = PrintfFormat.g (16, 8, y);

      sb.append (S + " }");
      return sb.toString();
   }*/


}
