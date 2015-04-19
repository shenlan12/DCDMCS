package adapters;

import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import umontreal.iro.lecuyer.charts.CategoryChart;
import umontreal.iro.lecuyer.charts.SSJCategorySeriesCollection;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * Project: DCDMC
 * Package: adapters
 * Date: 19/Apr/2015
 * Time: 14:13
 * System Time: 2:13 PM
 */

/*
 * Class:        BoxSeriesCollection
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

/**
 * This class stores data used in a
 * {@link CategoryChart CategoryChart}.
 * It also provides complementary tools to draw
 *  box-and-whisker plots; for example, one may
 * add or remove plots series and modify plot style. This class is linked with
 * the JFreeChart <TT>DefaultBoxAndWhiskerCategoryDataset</TT> class to store
 * data plots, and linked with the JFreeChart
 * <TT>BoxAndWhiskerRenderer</TT> to render the plots.
 *
 */
public class BoxSeriesCollectionAdapter extends SSJCategorySeriesCollection {
    final double BARWIDTH = 0.1;

    /**
     * Modified Method: change label from serie to Cluster plus the number of instances in the cluster
     *
     * Creates a new <TT>BoxSeriesCollection</TT> instance with default
     *    parameters and given data series. The input parameter represents series
     *    of point sets.
     *
     * @param data series of point sets.
     *
     *
     */
    public BoxSeriesCollectionAdapter (double[]... data)  {
        renderer = new BoxAndWhiskerRenderer();
        seriesCollection = new DefaultBoxAndWhiskerCategoryDataset ();

        DefaultBoxAndWhiskerCategoryDataset tempSeriesCollection =
                (DefaultBoxAndWhiskerCategoryDataset)seriesCollection;

        for (int i = 0; i < data.length; i ++) {
            if (data[i].length == 0)
                throw new IllegalArgumentException("Unable to render the plot. data["
                        + i +"] contains no row");
            final List<Double> list = new ArrayList<Double>();
            for (int j = 0; j < data[i].length-1; j ++)
                list.add(data[i][j]);

            // "i + 1" since the cluster label should start with 1 instead of 0
            tempSeriesCollection.add(list, 0, "[ " + data[i].length + " ]");
            list.clear();
        }
        ((BoxAndWhiskerRenderer)renderer).setMaximumBarWidth(BARWIDTH);
    }


    /**
     * Modified Method: change label from serie to Cluster plus the number of instances in the cluster
     *
     * Adds a data series into the series collection. Vector <TT>data</TT> represents
     *    a point set. Only <SPAN  CLASS="textit">the first</SPAN> <TT>numPoints</TT> of
     *    <TT>data</TT> will be added to the new series.
     *
     * @param data Point set
     *
     *    @param numPoints Number of points to add
     *
     *    @return Integer that represent the new point set's position in the JFreeChart <TT>DefaultBoxAndWhiskerXYDataset</TT> object.
     *
     */
    public int add (double[] data, int numPoints)  {
        DefaultBoxAndWhiskerCategoryDataset tempSeriesCollection =
                (DefaultBoxAndWhiskerCategoryDataset)seriesCollection;

        final List<Double> list = new ArrayList<Double>();
        for (int i = 0; i < numPoints; i ++)
            list.add(data[i]);

        int count = tempSeriesCollection.getColumnCount();

        // "count + 1" since the cluster label should start with 1 instead of 0
        tempSeriesCollection.add(list, 0, "[ " + numPoints + " ]");
        return count;
    }

    /**
     * Creates a new <TT>BoxSeriesCollection</TT> instance with an empty dataset.
     *
     */
    public BoxSeriesCollectionAdapter ()  {
        renderer = new BoxAndWhiskerRenderer();
        seriesCollection = new DefaultBoxAndWhiskerCategoryDataset();
        ((BoxAndWhiskerRenderer)renderer).setMaximumBarWidth(BARWIDTH);
    }


    /**
     * Creates a new <TT>BoxSeriesCollection</TT> instance with default parameters
     *  and input series <TT>data</TT>. Only <SPAN  CLASS="textit">the first</SPAN> <TT>numPoints</TT>
     *  of <TT>data</TT> will taken into account.
     *
     * @param data point sets.
     *
     *    @param numPoints Number of points
     *
     *
     */
    public BoxSeriesCollectionAdapter (double[] data, int numPoints)  {
        renderer = new BoxAndWhiskerRenderer();

        ((BoxAndWhiskerRenderer)renderer).setMaximumBarWidth(BARWIDTH);
        seriesCollection = new DefaultBoxAndWhiskerCategoryDataset ();

        DefaultBoxAndWhiskerCategoryDataset tempSeriesCollection =
                (DefaultBoxAndWhiskerCategoryDataset)seriesCollection;

        final List<Double> list = new ArrayList<Double>();
        for (int i = 0; i < numPoints; i ++)
            list.add(data[i]);

        tempSeriesCollection.add(list, 0, 0);
    }

    /**
     * Creates a new <TT>BoxSeriesCollection</TT> instance with default parameters and given data series.
     *    The input parameter represents a <TT>DefaultBoxAndWhiskerCategoryDataset</TT>.
     *
     * @param data series of point sets.
     *
     */
    public BoxSeriesCollectionAdapter (DefaultBoxAndWhiskerCategoryDataset data)  {
        renderer = new BoxAndWhiskerRenderer();
        ((BoxAndWhiskerRenderer)renderer).setFillBox(false);
        seriesCollection = data;
        ((BoxAndWhiskerRenderer)renderer).setMaximumBarWidth(BARWIDTH);
    }


    /**
     * Adds a data series into the series collection. Vector <TT>data</TT> represents
     *    a point set.
     *
     * @param data point sets.
     *
     *    @return Integer that represent the new point set's position in the JFreeChart <TT>DefaultBoxAndWhiskerXYDataset</TT> object.
     *
     */
    public int add (double[] data)  {
        return add(data, data.length);
    }

    /**
     * Gets the current name of the selected series.
     *
     * @param series series index.
     *
     *    @return current name of the series.
     *
     */
    public String getName (int series)  {
        return (String)((DefaultBoxAndWhiskerCategoryDataset)seriesCollection).getColumnKey(series);
    }


    /**
     * Returns the range (<SPAN CLASS="MATH"><I>y</I></SPAN>-coordinates) min and max values.
     *
     * @return range min and max values.
     *
     */
    public double[] getRangeBounds()  {
        double max=0, min=0;
        DefaultBoxAndWhiskerCategoryDataset tempSeriesCollection =
                (DefaultBoxAndWhiskerCategoryDataset)seriesCollection;

        if(tempSeriesCollection.getColumnCount() != 0 && tempSeriesCollection.getRowCount() != 0) {
            max = tempSeriesCollection.getItem(0, 0).getMaxOutlier().doubleValue() ;
            min = tempSeriesCollection.getItem(0, 0).getMinOutlier().doubleValue() ;
        }

        for(int i = 0; i < tempSeriesCollection.getRowCount(); i++) {
            for( int j = 0; j < tempSeriesCollection.getColumnCount(); j++) {
                max = Math.max(max, tempSeriesCollection.getItem(i, j).getMaxOutlier().doubleValue() );
                min = Math.min(min, tempSeriesCollection.getItem(i, j).getMinOutlier().doubleValue() );
            }
        }

        double[] retour = {min, max};
        return retour;
    }


    /**
     * Returns in a <TT>String</TT> all data contained in the current object.
     *
     * @return All data contained in the current object as a {@link String}.
     *
     */
    public String toString()  {
        Formatter formatter = new Formatter(Locale.US);
        for(int i = 0; i < seriesCollection.getRowCount(); i++) {
            formatter.format(" Series " + i + " : %n");
            for(int j = 0; j < seriesCollection.getColumnCount(); j++)
                formatter.format(",%15e%n", seriesCollection.getValue(i, j));
        }
        return formatter.toString();
    }


    /**
     * NOT IMPLEMENTED: To do.
     *
     * @param ymin
     *
     *    @param ymax
     *
     *    @return LaTeX source code
     *
     */
    public String toLatex (double YScale, double YShift,
                           double ymin, double ymax)  {
        throw new UnsupportedOperationException(" NOT implemented yet");
/*
      // Calcule les bornes reelles du graphique, en prenant en compte la position des axes

      ymin = Math.min(YShift, ymin);
      ymax = Math.max(YShift, ymax);

      DefaultBoxAndWhiskerCategoryDataset tempSeriesCollection = (DefaultBoxAndWhiskerCategoryDataset)seriesCollection;
      Formatter formatter = new Formatter(Locale.US);
//       double var;
//       double margin = ((BoxAndWhiskerRenderer)renderer).getMargin();

//       for (int i = tempSeriesCollection.getColumnCount() - 1; i >= 0; i--) {
//          List temp = tempSeriesCollection.getBins(i);
//          ListIterator iter = temp.listIterator();
//
//          Color color = (Color)renderer.getSeriesPaint(i);
//          String colorString = detectXColorClassic(color);
//          if (colorString == null) {
//             colorString = "color"+i;
//             formatter.format( "\\definecolor{%s}{rgb}{%.2f, %.2f, %.2f}%n",
//                               colorString, color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0);
//          }
//
//          HistogramBin currentBin=null;
//          while(iter.hasNext()) {
//             double currentMargin;
//             currentBin = (HistogramBin)iter.next();
//             currentMargin = ((margin*(currentBin.getEndBoundary()-currentBin.getStartBoundary()))-XShift)*XScale;
//             if ((currentBin.getStartBoundary() >= xmin && currentBin.getStartBoundary() <= xmax)
//                && (currentBin.getCount() >= ymin && currentBin.getCount() <= ymax) )
//             {
//                var = Math.min( currentBin.getEndBoundary(), xmax);
//                if (filled[i]) {
//                   formatter.format("\\filldraw [line width=%.2fpt, opacity=%.2f, color=%s] ([xshift=%.4f] %.4f, %.4f) rectangle ([xshift=-%.4f] %.4f, %.4f); %%%n",
//                         lineWidth[i], (color.getAlpha()/255.0), colorString,
//                         currentMargin, (currentBin.getStartBoundary()-XShift)*XScale, 0.0,
//                         currentMargin, (var-XShift)*XScale, (currentBin.getCount()-YShift)*YScale);
//               }
//               else {
//                   formatter.format("\\draw [line width=%.2fpt, color=%s] ([xshift=%.4f] %.4f, %.4f) rectangle ([xshift=-%.4f] %.4f, %.4f); %%%n",
//                         lineWidth[i], colorString,
//                         currentMargin, (currentBin.getStartBoundary()-XShift)*XScale, 0.0,
//                         currentMargin, (var-XShift)*XScale, (currentBin.getCount()-YShift)*YScale);
//               }
//             }
//             else if (   (currentBin.getStartBoundary() >= xmin && currentBin.getStartBoundary() <= xmax)
//                         && (currentBin.getCount() >= ymin && currentBin.getCount() > ymax) )
//             { // Cas ou notre rectangle ne peut pas etre affiche en entier (trop haut)
//                var = Math.min( currentBin.getEndBoundary(), xmax);
//                if (filled[i]) {
//                   formatter.format("\\filldraw [line width=%.2fpt,  opacity=%.2f, color=%s] ([xshift=%.4f] %.4f, %.4f) rectangle ([xshift=-%.4f] %.4f, %.4f); %%%n",
//                         lineWidth[i], (color.getAlpha()/255.0), colorString,
//                         currentMargin, (currentBin.getStartBoundary()-XShift)*XScale, 0.0,
//                         currentMargin, (var-XShift)*XScale, (ymax-YShift)*YScale);
//               formatter.format("\\draw [line width=%.2fpt, color=%s, style=dotted] ([xshift=%.4f] %.4f, %.4f) rectangle ([yshift=3mm, xshift=-%.4f] %.4f, %.4f); %%%n",
//                         lineWidth[i], colorString,
//                         currentMargin, (currentBin.getStartBoundary()-XShift)*XScale, (ymax-YShift)*YScale,
//                         currentMargin, (var-XShift)*XScale, (ymax-YShift)*YScale);
//                }
//                else {
//                   formatter.format("\\draw [line width=%.2fpt, color=%s] ([xshift=%.4f] %.4f, %.4f) rectangle ([xshift=-%.4f] %.4f, %.4f); %%%n",
//                         lineWidth[i], colorString,
//                         currentMargin, (currentBin.getStartBoundary()-XShift)*XScale, 0.0,
//                         currentMargin, (var-XShift)*XScale, (ymax-YShift)*YScale);
//
//               formatter.format("\\draw [line width=%.2fpt, color=%s, style=dotted] ([xshift=%.4f] %.4f, %.4f) rectangle ([yshift=3mm, xshift=-%.4f] %.4f, %.4f); %%%n",
//                         lineWidth[i], colorString,
//                         currentMargin, (currentBin.getStartBoundary()-XShift)*XScale, (ymax-YShift)*YScale,
//                         currentMargin, (var-XShift)*XScale, (ymax-YShift)*YScale);
//                }
//             }
//          }
//       }
      return formatter.toString();
*/
    }


}
