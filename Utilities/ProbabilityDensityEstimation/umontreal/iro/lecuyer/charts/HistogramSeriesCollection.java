

/*
 * Class:        HistogramSeriesCollection
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

import   umontreal.iro.lecuyer.stat.*;
import   umontreal.iro.lecuyer.util.Num;

import   org.jfree.chart.renderer.xy.XYBarRenderer;
import   org.jfree.data.statistics.HistogramBin;

import   cern.colt.list.DoubleArrayList;

import   java.util.Locale;
import   java.util.Formatter;
import   java.util.List;
import   java.util.ListIterator;
import   java.awt.Color;

/**
 * Stores data used in a <TT>HistogramChart</TT>. <TT>HistogramSeriesCollection</TT> provides complementary tools to draw histograms.
 * One may add observation sets, define histogram bins, set plot color and plot style,
 * enable/disable filled shapes, and set margin between shapes for each series.
 * This class is linked with class <TT>CustomHistogramDataset</TT> to store data plots,
 * and linked with JFreeChart <TT>XYBarRenderer</TT> to render the plot.
 * <TT>CustomHistogramDataset</TT> has been developed at the Universit&#233; de Montr&#233;al to extend the JFreeChart API,
 * and is used to manage histogram datasets in a JFreeChart chart.
 * 
 */
public class HistogramSeriesCollection extends SSJXYSeriesCollection  {
   protected boolean[] filled;   // fill flag for each series
   protected double[] lineWidth; // sets line width
   protected int numBin = 20;    // default number of bins

   private int calcNumBins (int n) {
      // set the number of bins
      int numbins = (int) Math.ceil (1.0 + Num.log2(n));
      if (n > 5000)
         numbins *= 2;
      return numbins;
   }

   private double[] countersToArray (TallyHistogram hist) {
      final int nb = hist.getNumBins();
      final double binwidth = (hist.getB() - hist.getA()) / nb;
      int[] count = hist.getCounters();
      int sum = 0;
      for (int i = 1 ; i <= nb; i++)
         sum += count[i];
      double[] data = new double [sum];

      double h, base;
      int k = 0;
      for (int i = 1 ; i <= nb; i++) {
         h = binwidth;
         base = hist.getA() + (i-1)*binwidth;
         if (count[i] > 0)
            h /= count[i];
         if (i == nb) {
            for (int j = 0 ; j < count[i] ; j++)
               data[k++] = hist.getB() - j*h;
         } else {
            for (int j = 0 ; j < count[i] ; j++)
               data[k++] = base + j*h;
         }
      }
      return data;
   }

   /**
    * Creates a new <TT>HistogramSeriesCollection</TT> instance with empty dataset.
    * 
    */
   public HistogramSeriesCollection()  {
      renderer = new XYBarRenderer();
      seriesCollection = new CustomHistogramDataset();
   }


   /**
    * Creates a new <TT>HistogramSeriesCollection</TT> instance with given data series.
    *    Bins the elements of data in equally spaced containers (the number of bins
    *    can be changed using the method {@link #setBins setBins}).
    *    Each input parameter represents a data series.
    * 
    * @param data series of point sets.
    * 
    * 
    */
   public HistogramSeriesCollection (double[]... data)  {
      seriesCollection = new CustomHistogramDataset();
      renderer = new XYBarRenderer();
      CustomHistogramDataset tempSeriesCollection =
             (CustomHistogramDataset)seriesCollection;
      for (int i = 0; i < data.length; i ++) {
         numBin = calcNumBins(data[i].length);
         tempSeriesCollection.addSeries(i, data[i], numBin);
      }

      // set default colors
      for (int i = 0; i < tempSeriesCollection.getSeriesCount(); i++) {
         renderer.setSeriesPaint(i, getDefaultColor(i));
      }

      // set default plot style
      filled = new boolean[seriesCollection.getSeriesCount()];
      lineWidth = new double[seriesCollection.getSeriesCount()];
      for (int i = 0; i < tempSeriesCollection.getSeriesCount(); i++) {
         filled[i] = false;
         lineWidth[i] = 0.5;
         setFilled(i, false);
      }
   }


   /**
    * Creates a new <TT>HistogramSeriesCollection</TT> instance with the given data
    *    series <TT>data</TT>. Bins the elements of data in equally spaced containers
    *    (the number of bins can be changed using the method {@link #setBins setBins}).
    *    Only the first <TT>numPoints</TT> of <TT>data</TT> will be taken into account.
    * 
    * @param data Point set
    * 
    *    @param numPoints Number of points to plot
    * 
    * 
    */
   public HistogramSeriesCollection (double[] data, int numPoints)  {
      seriesCollection = new CustomHistogramDataset();
      renderer = new XYBarRenderer();
      CustomHistogramDataset tempSeriesCollection =
             (CustomHistogramDataset)seriesCollection;
      numBin = calcNumBins(numPoints);
      tempSeriesCollection.addSeries(0, data, numPoints, numBin);

      // set default colors
      for (int i = 0; i < tempSeriesCollection.getSeriesCount(); i++) {
         renderer.setSeriesPaint(i, getDefaultColor(i));
      }

      // set default plot style
      filled = new boolean[seriesCollection.getSeriesCount()];
      lineWidth = new double[seriesCollection.getSeriesCount()];
      for (int i = 0; i < tempSeriesCollection.getSeriesCount(); i++) {
         filled[i] = false;
         lineWidth[i] = 0.5;
         setFilled(i, false);
      }
   }


   /**
    * Creates a new <TT>HistogramSeriesCollection</TT>.
    *    Bins the elements of data in equally spaced containers (the number of bins
    *    can be changed using the method {@link #setBins setBins}).
    *    The input parameter represents a set of data plots. Each
    *   {@link cern.colt.list.DoubleArrayList DoubleArrayList} variable corresponds
    *    to a histogram on the chart.
    * 
    * @param data series of observation sets.
    * 
    * 
    */
   public HistogramSeriesCollection (DoubleArrayList... data)  {
      seriesCollection = new CustomHistogramDataset();
      renderer = new XYBarRenderer();
      CustomHistogramDataset tempSeriesCollection =
          (CustomHistogramDataset)seriesCollection;

      for (int i = 0; i < data.length; i++) {
         numBin = calcNumBins(data[i].size());
         tempSeriesCollection.addSeries(i, data[i].elements(), data[i].size(), numBin);
      }

      // set default colors
      for (int i = 0; i < tempSeriesCollection.getSeriesCount(); i++) {
         renderer.setSeriesPaint(i, getDefaultColor(i));
      }

      // set default plot style
      filled = new boolean[seriesCollection.getSeriesCount()];
      lineWidth = new double[seriesCollection.getSeriesCount()];
      for (int i = 0; i < tempSeriesCollection.getSeriesCount(); i++) {
         filled[i] = false;
         lineWidth[i] = 0.5;
         setFilled(i, false);
      }
   }


   /**
    * Creates a new <TT>HistogramSeriesCollection</TT> instance with default
    *  parameters and given data. The input parameter represents a collection of
    *  data observation sets. Each <TT>TallyStore</TT> input parameter represents
    *  an observation set.
    * 
    * @param tallies series of point sets.
    * 
    * 
    */
   public HistogramSeriesCollection (TallyStore... tallies)  {
      seriesCollection = new CustomHistogramDataset();
      renderer = new XYBarRenderer();
      CustomHistogramDataset tempSeriesCollection = (CustomHistogramDataset)seriesCollection;

      double h;
      for (int i = 0; i < tallies.length; i++) {
         // Scott's formula
         h = 3.5*tallies[i].standardDeviation() /
                           Math.pow(tallies[i].numberObs(), 1.0/3.0);
         numBin = (int) ((tallies[i].max() - tallies[i].min()) / (1.5*h));
         tempSeriesCollection.addSeries (i, tallies[i].getArray(),
              tallies[i].numberObs(), numBin, tallies[i].min(), tallies[i].max());
      }

      // set default colors
      for (int i = 0; i < tempSeriesCollection.getSeriesCount(); i++) {
         renderer.setSeriesPaint(i, getDefaultColor(i));
      }

      // set default plot style
      filled = new boolean[seriesCollection.getSeriesCount()];
      lineWidth = new double[seriesCollection.getSeriesCount()];
      for (int i = 0; i < tempSeriesCollection.getSeriesCount(); i++) {
         filled[i] = false;
         lineWidth[i] = 0.5;
         setFilled(i, false);
      }
   }


   /**
    * Creates a new <TT>HistogramSeriesCollection</TT> instance with default
    *  parameters and given data. The input parameter represents a collection of
    *  data observation sets. Each <TT>TallyHistogram</TT> input parameter represents
    *  an observation set. The 2 extra bins at the beginning and at the end of the
    *  tallies are not counted nor represented in the chart.
    * 
    * @param tallies series of point sets.
    * 
    * 
    */
   public HistogramSeriesCollection (TallyHistogram... tallies)  {
      seriesCollection = new CustomHistogramDataset();
      renderer = new XYBarRenderer();
      CustomHistogramDataset tempSeriesCollection = (CustomHistogramDataset)seriesCollection;

      double[] data;
      for (int i = 0; i < tallies.length; i++) {
         data = countersToArray(tallies[i]);
         tempSeriesCollection.addSeries (i, data, data.length,
            tallies[i].getNumBins(), tallies[i].getA(), tallies[i].getB());
      }

      // set default colors
      for (int i = 0; i < tempSeriesCollection.getSeriesCount(); i++) {
         renderer.setSeriesPaint(i, getDefaultColor(i));
      }

      // set default plot style
      filled = new boolean[seriesCollection.getSeriesCount()];
      lineWidth = new double[seriesCollection.getSeriesCount()];
      for (int i = 0; i < tempSeriesCollection.getSeriesCount(); i++) {
         filled[i] = false;
         lineWidth[i] = 0.5;
         setFilled(i, false);
      }
   }


   /**
    * Creates a new <TT>HistogramSeriesCollection</TT> instance.
    *    The input parameter represents a set of plotting data.
    *    Each series of the given collection corresponds to a histogram.
    * 
    * @param data series of point sets.
    * 
    */
   public HistogramSeriesCollection (CustomHistogramDataset data)  {
      renderer = new XYBarRenderer();
      seriesCollection = data;

      // set default colors
      for (int i = 0; i < data.getSeriesCount(); i++) {
         renderer.setSeriesPaint(i, getDefaultColor(i));
      }

      // set default plot style
      filled = new boolean[seriesCollection.getSeriesCount()];
      lineWidth = new double[seriesCollection.getSeriesCount()];
      for (int i = 0; i < data.getSeriesCount(); i++) {
         filled[i] = false;
         lineWidth[i] = 0.5;
         setFilled(i, false);
      }
   }


   /**
    * Adds a data series into the series collection.
    * 
    * @param data new series.
    * 
    *    @return Integer that represent the new point set.
    * 
    */
   public int add (double[] data)  {
      return add (data, data.length);
   }


   /**
    * Adds a data series into the series collection. Only <SPAN  CLASS="textit">the first</SPAN>
    *   <TT>numPoints</TT> of <TT>data</TT> will be added to the new series.
    * 
    * @param data new series.
    * 
    *    @param numPoints Number of points to add
    * 
    *    @return Integer that represent the new point set.
    * 
    */
   public int add (double[] data, int numPoints)  {
      CustomHistogramDataset tempSeriesCollection = (CustomHistogramDataset)seriesCollection;
      tempSeriesCollection.addSeries(tempSeriesCollection.getSeriesCount(),
            data, numPoints, numBin);

      boolean[] newFilled = new boolean[seriesCollection.getSeriesCount()];
      double[] newLineWidth = new double[seriesCollection.getSeriesCount()];

      // color
      int j = seriesCollection.getSeriesCount() - 1;
      renderer.setSeriesPaint(j, getDefaultColor(j));
      newFilled[j] = false;
      newLineWidth[j] = 0.5;

      for (j = 0; j < seriesCollection.getSeriesCount() - 1; j++) {
         newFilled[j] = filled[j];
         newLineWidth[j] = lineWidth[j];
      }

      filled = newFilled;
      lineWidth = newLineWidth;

      return seriesCollection.getSeriesCount() - 1;
   }


   /**
    * Adds a data series into the series collection.
    * 
    * @param observationSet new series values.
    * 
    *    @return Integer that represent the new point set's position in the <TT>CustomHistogramDataset</TT> object.
    * 
    */
   public int add (DoubleArrayList observationSet)  {
      return add(observationSet.elements(), observationSet.size());
   }


   /**
    * Adds a data series into the series collection.
    * 
    * @param tally {@link TallyStore TallyStore} to add values.
    * 
    *    @return Integer that represent the new point set's position in the <TT>CustomHistogramDataset</TT> object.
    * 
    */
   public int add (TallyStore tally)  {
      return add(tally.getArray(), tally.numberObs());
   }


   /**
    * Returns the <TT>CustomHistogramDataset</TT> object associated with the current variable.
    * 
    * @return <TT>CustomHistogramDataset</TT> object associated with the current variable.
    * 
    */
   public CustomHistogramDataset getSeriesCollection()  {
      return (CustomHistogramDataset)seriesCollection;
   }


   /**
    * Returns the bins for a series.
    * 
    * @param series the series index (in the range <TT>0</TT> to <TT>getSeriesCount() - 1</TT>).
    * 
    *    @return A list of bins.
    *    IndexOutOfBoundsExceptionif <TT>series</TT> is outside the specified range.
    * 
    */
   public List getBins (int series)  {
      return ((CustomHistogramDataset)seriesCollection).getBins(series);
   }


   /**
    * Sets <TT>bins</TT> periodic bins from the observation minimum values to the observation maximum value for a series.
    * 
    * @param series the series index (in the range <TT>0</TT> to <TT>getSeriesCount() - 1</TT>).
    * 
    *    @param bins the number of periodic bins.
    * 
    *    IndexOutOfBoundsExceptionif <TT>series</TT> is outside the specified range.
    * 
    */
   public void setBins (int series, int bins)  {
      ((CustomHistogramDataset)seriesCollection).setBins(series, bins);
   }


   /**
    * Sets <TT>bins</TT> periodic bins from <TT>minimum</TT> to <TT>maximum</TT> for a series.
    *    Values falling on the boundary of adjacent bins will be assigned to the higher indexed bin.
    * 
    * @param series the series index (in the range <TT>0</TT> to <TT>getSeriesCount() - 1</TT>).
    * 
    *    @param bins the number of periodic bins.
    * 
    *    @param minimum minimum value.
    * 
    *    @param maximum maximum value.
    * 
    *    IndexOutOfBoundsExceptionif <TT>series</TT> is outside the specified range.
    * 
    */
   public void setBins (int series, int bins, double minimum, double maximum)  {
      ((CustomHistogramDataset)seriesCollection).setBins(series, bins, minimum, maximum);
   }


   /**
    * Links bins given by table <TT>binsTable</TT> to a series.
    *    Values falling on the boundary of adjacent bins will be assigned to the higher indexed bin.
    * 
    * @param series the series index (in the range <TT>0</TT> to <TT>getSeriesCount() - 1</TT>).
    * 
    *    @param binsTable new bins table.
    * 
    *    IndexOutOfBoundsExceptionif <TT>series</TT> is outside the specified range.
    * 
    */
   public void setBins (int series, HistogramBin[] binsTable)  {
      ((CustomHistogramDataset)seriesCollection).setBins(series, binsTable);
   }


   /**
    * Returns the values for a series.
    * 
    * @param series the series index (in the range <TT>0</TT> to <TT>getSeriesCount() - 1</TT>).
    * 
    *    @return A list of values.
    *    (IndexOutOfBoundsExceptionif &lt;code&gt;series&lt;/code&gt; is outside the specified range.
    * 
    */
   public List getValuesList (int series)  {
      return ((CustomHistogramDataset)seriesCollection).getValuesList(series);
   }


   /**
    * Returns the values for a series.
    * 
    * @param series the series index (in the range <TT>0</TT> to <TT>getSeriesCount() - 1</TT>).
    * 
    *    @return A list of values.
    *    (IndexOutOfBoundsExceptionif &lt;code&gt;series&lt;/code&gt; is outside the specified range.
    * 
    */
   public double[] getValues (int series)  {
      return ((CustomHistogramDataset)seriesCollection).getValues(series);
   }


   /**
    * Sets a new values set to a series from a <TT>List</TT> variable.
    * 
    * @param series the series index (in the range <TT>0</TT> to <TT>getSeriesCount() - 1</TT>).
    * 
    *    @param valuesList new values list.
    * 
    * 
    */
   public void setValues (int series, List valuesList)  {
      ((CustomHistogramDataset)seriesCollection).setValues(series, valuesList);
   }


   /**
    * Sets a new values set to a series from a table.
    * 
    * @param series the series index (in the range <TT>0</TT> to <TT>getSeriesCount() - 1</TT>).
    * 
    *    @param values new values table.
    * 
    */
   public void setValues (int series, double[] values)  {
      ((CustomHistogramDataset)seriesCollection).setValues(series, values);
   }


   /**
    * Returns the <TT>filled</TT> flag associated with the <TT>series</TT>-th
    *   data series.
    * 
    * @param series series index.
    * 
    *    @return fill flag.
    * 
    */
   public boolean getFilled (int series)  {
      return filled[series];
   }


   /**
    * Sets the <TT>filled</TT> flag. This option fills histogram rectangular.
    *    The fill color is the current series color, alpha's color parameter will
    *    be used to draw transparency.
    * 
    * @param series series index.
    * 
    *    @param filled fill flag.
    * 
    * 
    */
   public void setFilled (int series, boolean filled)  {
      this.filled[series] = filled;
   }


   /**
    * Returns the margin which is a percentage amount by which the bars are trimmed.
    * 
    */
   public double getMargin()  {
      return ((XYBarRenderer)renderer).getMargin();
   }


   /**
    * Sets the margin which is a percentage amount by which the bars are trimmed for all series.
    * 
    * @param margin margin percentage amount.
    * 
    * 
    */
   public void setMargin (double margin)  {
      ((XYBarRenderer)renderer).setMargin(margin);
   }


   /**
    * Returns the outline width in pt.
    * 
    * @param series series index.
    * 
    * 
    */
   public double getOutlineWidth (int series)  {
      return lineWidth[series];
   }


   /**
    * Sets the outline width in pt.
    * 
    * @param series series index.
    * 
    *    @param outline outline width.
    * 
    * 
    */
   public void setOutlineWidth (int series, double outline)  {
      this.lineWidth[series] = outline;
   }


   public String toLatex (double XScale, double YScale, double XShift,
                          double YShift, double xmin, double xmax,
                          double ymin, double ymax)  {

      // Calcule les bornes reelles du graphique, en prenant en compte la position des axes
      xmin = Math.min(XShift, xmin);
      xmax = Math.max(XShift, xmax);
      ymin = Math.min(YShift, ymin);
      ymax = Math.max(YShift, ymax);

      CustomHistogramDataset tempSeriesCollection = (CustomHistogramDataset)seriesCollection;
      Formatter formatter = new Formatter(Locale.US);
      double var;
      double margin = ((XYBarRenderer)renderer).getMargin();

      for (int i = tempSeriesCollection.getSeriesCount() - 1; i >= 0; i--) {
         List temp = tempSeriesCollection.getBins(i);
         ListIterator iter = temp.listIterator();

         Color color = (Color)renderer.getSeriesPaint(i);
         String colorString = detectXColorClassic(color);
         if (colorString == null) {
            colorString = "color"+i;
            formatter.format( "\\definecolor{%s}{rgb}{%.2f, %.2f, %.2f}%n",
                              colorString, color.getRed()/255.0, color.getGreen()/255.0,
                              color.getBlue()/255.0);
         }

         HistogramBin currentBin=null;
         while(iter.hasNext()) {
            double currentMargin;
            currentBin = (HistogramBin)iter.next();
            currentMargin = ((margin*(currentBin.getEndBoundary()-currentBin.getStartBoundary())))*XScale;
            if ((currentBin.getStartBoundary() >= xmin && currentBin.getStartBoundary() <= xmax)
               && (currentBin.getCount() >= ymin && currentBin.getCount() <= ymax) )
            {
               var = Math.min( currentBin.getEndBoundary(), xmax);
               if (filled[i]) {
                  formatter.format("\\filldraw [line width=%.2fpt, opacity=%.2f, color=%s] ([xshift=%.4f] %.4f, %.4f) rectangle ([xshift=-%.4f] %.4f, %.4f); %%%n",
                        lineWidth[i], (color.getAlpha()/255.0), colorString,
                        currentMargin, (currentBin.getStartBoundary()-XShift)*XScale, 0.0,
                        currentMargin, (var-XShift)*XScale, (currentBin.getCount()-YShift)*YScale);
              }
              else {
                  formatter.format("\\draw [line width=%.2fpt, color=%s] ([xshift=%.4f] %.4f, %.4f) rectangle ([xshift=-%.4f] %.4f, %.4f); %%%n",
                        lineWidth[i], colorString,
                        currentMargin, (currentBin.getStartBoundary()-XShift)*XScale, 0.0,
                        currentMargin, (var-XShift)*XScale, (currentBin.getCount()-YShift)*YScale);
              }
            }
            else if (   (currentBin.getStartBoundary() >= xmin && currentBin.getStartBoundary() <= xmax)
                        && (currentBin.getCount() >= ymin && currentBin.getCount() > ymax) )
            { // Cas ou notre rectangle ne peut pas etre affiche en entier (trop haut)
               var = Math.min( currentBin.getEndBoundary(), xmax);
               if (filled[i]) {
                  formatter.format("\\filldraw [line width=%.2fpt,  opacity=%.2f, color=%s] ([xshift=%.4f] %.4f, %.4f) rectangle ([xshift=-%.4f] %.4f, %.4f); %%%n",
                        lineWidth[i], (color.getAlpha()/255.0), colorString,
                        currentMargin, (currentBin.getStartBoundary()-XShift)*XScale, 0.0,
                        currentMargin, (var-XShift)*XScale, (ymax-YShift)*YScale);
                  formatter.format("\\draw [line width=%.2fpt, color=%s, style=dotted] ([xshift=%.4f] %.4f, %.4f) rectangle ([yshift=3mm, xshift=-%.4f] %.4f, %.4f); %%%n",
                        lineWidth[i], colorString,
                        currentMargin, (currentBin.getStartBoundary()-XShift)*XScale, (ymax-YShift)*YScale,
                        currentMargin, (var-XShift)*XScale, (ymax-YShift)*YScale);
               }
               else {
                  formatter.format("\\draw [line width=%.2fpt, color=%s] ([xshift=%.4f] %.4f, %.4f) rectangle ([xshift=-%.4f] %.4f, %.4f); %%%n",
                        lineWidth[i], colorString,
                        currentMargin, (currentBin.getStartBoundary()-XShift)*XScale, 0.0,
                        currentMargin, (var-XShift)*XScale, (ymax-YShift)*YScale);

                  formatter.format("\\draw [line width=%.2fpt, color=%s, style=dotted] ([xshift=%.4f] %.4f, %.4f) rectangle ([yshift=3mm, xshift=-%.4f] %.4f, %.4f); %%%n",
                        lineWidth[i], colorString,
                        currentMargin, (currentBin.getStartBoundary()-XShift)*XScale, (ymax-YShift)*YScale,
                        currentMargin, (var-XShift)*XScale, (ymax-YShift)*YScale);
               }
            }
         }
      }
      return formatter.toString();
   }
}
