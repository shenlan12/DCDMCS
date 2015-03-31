

/*
 * Class:        SSJCategorySeriesCollection
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

import   org.jfree.data.category.CategoryDataset;
import   org.jfree.chart.renderer.category.CategoryItemRenderer;

import   java.util.Locale;
import   java.util.Formatter;
import   java.awt.Color;

/**
 * Stores data used in a <TT>CategoryChart</TT>.
 * This class provides tools to manage data sets and rendering options, and modify
 * plot color, plot style, and marks on points for each series.
 * 
 */
public abstract class SSJCategorySeriesCollection  {
   protected CategoryItemRenderer renderer;
   protected CategoryDataset seriesCollection;



   /**
    * Returns the category-value in the specified series.
    * 
    * @param series required series value.
    * 
    *    @return <SPAN CLASS="MATH"><I>x</I></SPAN>-value at the specified index in the specified series.
    * 
    */
   public String getCategory (int series)  {
      return seriesCollection.getColumnKey(series).toString();
   }


   /**
    * Returns the <SPAN CLASS="MATH"><I>y</I></SPAN>-value at the specified index in the specified series.
    * 
    * @param series required series value.
    * 
    *    @param index value's index.
    * 
    *    @return <SPAN CLASS="MATH"><I>y</I></SPAN>-value at the specified index in the specified series.
    * 
    */
   public double getValue (int series, int index)  {
      return (Double)seriesCollection.getValue(series, index);
   }


   /**
    * Returns the <TT>CategoryDataset</TT> object associated with the current object.
    * 
    * @return <TT>CategoryDataset</TT> object associated with the current variable.
    * 
    */
   public CategoryDataset getSeriesCollection()  {
      return seriesCollection;
   }


   /**
    * Returns range (<SPAN CLASS="MATH"><I>y</I></SPAN>-coordinates) min and max values.
    * 
    * @return range min and max values.
    * 
    */
   public abstract double[] getRangeBounds();


   /**
    * Returns in a <TT>String</TT> all data contained in the current object.
    * 
    * @return All data contained in the current object as a {@link String}.
    * 
    */
   public abstract String toString();


   /**
    * Returns the <TT>CategoryItemRenderer</TT> object associated with the current object.
    * 
    * @return <TT>CategoryItemRenderer</TT> object associated with the current variable.
    * 
    */
   public CategoryItemRenderer getRenderer()  {
      return renderer;
   }


   /**
    * Sets the <TT>CategoryItemRenderer</TT> object associated with the current variable.
    *    This object determines the chart JFreeChart look, produced by method
    *   <TT>view</TT> in class
    *    {@link XYChart XYChart}.
    * 
    * @param renderer  new <TT>CategoryItemRenderer</TT> object.
    * 
    * 
    */
   public void setRenderer (CategoryItemRenderer renderer)  {
      this.renderer = renderer;
   }


   /**
    * Gets the current plotting color of the selected series.
    * 
    * @return current plotting color.
    * 
    */
   public Color getColor (int series)  {
      return (Color)renderer.getSeriesPaint(series);
   }


   /**
    * Sets a new plotting color to the series <SPAN CLASS="MATH"><I>series</I></SPAN>.
    * 
    * @param series series index.
    * 
    *    @param color plotting color.
    * 
    * 
    */
   public void setColor (int series, Color color)  {
      renderer.setSeriesPaint(series, color);
   }


   /**
    * Formats and returns a string containing a <SPAN CLASS="logo,LaTeX">L<SUP><SMALL>A</SMALL></SUP>T<SMALL>E</SMALL>X</SPAN>-compatible source
    *    code which represents this data series collection. The original datasets are shifted and scaled with the
    *    <TT>YShift</TT> and <TT>YScale</TT> parameters.
    *    <TT>ymin</TT> and <TT>ymax</TT> represent the chart bounds.
    * 
    * @param YScale Range original data scale.
    * 
    *    @param YShift Range original data shift value.
    * 
    *    @param ymin Range min bound.
    * 
    *    @param ymax Range max bound.
    * 
    *    @return Latex code.
    * 
    */
   public abstract String toLatex (double YScale, double YShift,
                                   double ymin, double ymax);



   /* *
    * Converts a java Color object into a friendly and readable LaTeX/xcolor string.
    *
    * @param   color    in color.
    * @return           friendly color with string format as possible, null otherwise.
    */
   protected static String detectXColorClassic(Color color) {
      String retour = null;

      int red = color.getRed();
      int green = color.getGreen();
      int blue = color.getBlue();

      // On utilise pas la method Color.equals(Color ) car on ne veut pas tester le parametre de transparence : Alpha
      if (   red == Color.GREEN.getRed()
          && blue == Color.GREEN.getBlue()
          && green == Color.GREEN.getGreen())
         return "green";
      else if (   red == Color.RED.getRed()
               && blue == Color.RED.getBlue()
               && green == Color.RED.getGreen())
         return "red";
      else if (   red == Color.WHITE.getRed()
               && blue == Color.WHITE.getBlue()
               && green == Color.WHITE.getGreen())
         return "white";
      else if (   red == Color.GRAY.getRed()
               && blue == Color.GRAY.getBlue()
               && green == Color.GRAY.getGreen())
          return "gray";
      else if (   red == Color.BLACK.getRed()
               && blue == Color.BLACK.getBlue()
               && green == Color.BLACK.getGreen())
          return "black";
      else if (   red == Color.YELLOW.getRed()
               && blue == Color.YELLOW.getBlue()
               && green == Color.YELLOW.getGreen())
          return "yellow";
      else if (   red == Color.MAGENTA.getRed()
               && blue == Color.MAGENTA.getBlue()
               && green == Color.MAGENTA.getGreen())
          return "magenta";
      else if (   red == Color.CYAN.getRed()
               && blue == Color.CYAN.getBlue()
               && green == Color.CYAN.getGreen())
          return "cyan";
      else if (   red == Color.BLUE.getRed()
               && blue == Color.BLUE.getBlue()
               && green == Color.BLUE.getGreen())
          return "blue";
      else if (   red == Color.DARK_GRAY.getRed()
               && blue == Color.DARK_GRAY.getBlue()
               && green == Color.DARK_GRAY.getGreen())
          return "darkgray";
      else if (   red == Color.LIGHT_GRAY.getRed()
               && blue == Color.LIGHT_GRAY.getBlue()
               && green == Color.LIGHT_GRAY.getGreen())
          return "lightgray";
      else if (   red == Color.ORANGE.getRed()
               && blue == Color.ORANGE.getBlue()
               && green == Color.ORANGE.getGreen())
          return "orange";
      else if (   red == Color.PINK.getRed()
               && blue == Color.PINK.getBlue()
               && green == Color.PINK.getGreen())
          return "pink";


      if (red == 192 && blue == 128 && green == 64)
         return "brown";
     else if (red == 128 && blue == 128 && green == 0)
         return "olive";
      else if (red == 128 && blue == 0 && green == 128)
         return "violet";
      else if (red == 192 && blue == 0 && green ==64)
         return "purple";
      else return null;
   }

   /* *
    * Gives the default color associated with a series
    *
    * @param   index Index of the series in the CategoryDataset object.
    * @return        default color object.
    */
   protected static Color getDefaultColor(int index) {
      if(index%6 == 0)
         return Color.RED;
      else if(index%6 == 1)
         return Color.BLUE;
      else if(index%6 == 2)
         return Color.GREEN;
      else if(index%6 == 3)
         return Color.YELLOW;
      else if(index%6 == 4)
         return Color.MAGENTA;
      else
         return Color.CYAN;
   }

   // Returns maximum value in table t
   protected static double max (double[] t) {
      double aux = t[0];
      for (int i=1 ; i < t.length ; i++)
         if (t[i] > aux)
            aux = t[i];
      return aux ;
   }
   // Returns minimum value in table t
   protected static double min (double[] t) {
      double aux = t[0];
      for (int i=1 ; i < t.length ; i++)
         if (t[i] < aux)
            aux = t[i];
      return aux ;
   }
}
