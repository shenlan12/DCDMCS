

/*
 * Class:        DiscreteDistIntChart
 * Description:  
 * Environment:  Java
 * Software:     SSJ 
 * Copyright (C) 2001  Pierre L'Ecuyer and Universite de Montreal
 * Organization: DIRO, Universite de Montreal
 * @author       Richard Simard
 * @since        May 2008

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
   import umontreal.iro.lecuyer.probdist.DiscreteDistributionInt;

import org.jfree.chart.JFreeChart;
import javax.swing.JFrame;
import java.awt.*;



/**
 * This class provides tools to plot the mass function and the cumulative
 * probability of a discrete probability distribution over the integers.
 * 
 */
public class DiscreteDistIntChart  {
   protected DiscreteDistributionInt dist;
   protected int a,b;
   protected XYLineChart cdfChart;
   protected XYLineChart probChart;


   /**
    * Constructor for a new <TT>DiscreteDistIntChart</TT> instance used to plot the
    * probabilities of the
    * discrete distribution <TT>dist</TT> over the integers.
    * 
    * @param dist discrete distribution to plot
    * 
    * 
    */
   public DiscreteDistIntChart (DiscreteDistributionInt dist)  {
      this.dist = dist;
      this.a = 0;
      this.b = 0;
   }


   /**
    * Constructor for a new <TT>DiscreteDistIntChart</TT> instance used to plot the
    * probabilities of the
    * discrete distribution <TT>dist</TT> over the interval <SPAN CLASS="MATH">[<I>a</I>, <I>b</I>]</SPAN>.
    * 
    * @param dist continuous distribution to plot
    * 
    *    @param a lower bound of interval
    * 
    *    @param b upper bound of interval
    * 
    */
   public DiscreteDistIntChart (DiscreteDistributionInt dist, int a, int b)  {
      this.dist = dist;
      if (a>=b) throw new IllegalArgumentException ("a is bigger than b");
      this.a = a;
      this.b = b;
      init();
   }


   private void init() {
      int m = b-a+1;
      double[][] cdf = new double[2][m];
      double[][] probability = new double[2][m];

      for (int i = 0; i < m; i++) {
         cdf[0][i] = i+a;
         cdf[1][i] = dist.cdf (i+a);
         probability[0][i] = i+a;
         probability[1][i] = dist.prob (i+a);
      }

      double[][] cdfFinal = new double[2][2*(m-1)];
      for (int i = 0; i < m-1; i++) {
         cdfFinal[0][2*i] = cdf[0][i];
         cdfFinal[0][2*i+1] = cdf[0][i+1];
         cdfFinal[1][2*i] = cdf[1][i];
         cdfFinal[1][2*i+1] = cdf[1][i];
      }

      cdfChart = new XYLineChart("cdf: " + dist.toString(), "", "", cdfFinal);
      probChart = new XYLineChart("probability: " + dist.toString(), "",
                  "", probability);
      cdfChart.setprobFlag (true);
      probChart.setprobFlag (true);

      // Only for tikZ
      XYListSeriesCollection collec = cdfChart.getSeriesCollection();
      collec.setColor(0, Color.BLUE);
      collec.setPlotStyle(0, "thick");
      collec.setMarksType(0, "only marks");

      collec = probChart.getSeriesCollection();
      collec.setColor(0, Color.ORANGE);
      collec.setPlotStyle(0, "ycomb");
      collec.setMarksType(0, "*");
      collec.setDashPattern(0, "solid");
   }

   private void testParam() {
      if (a==0 && b==0) {
         double mean = dist.getMean();
         double sd = dist.getStandardDeviation();
         int xa = (int)Math.round(mean - 3.0*sd);
         if (xa < dist.getXinf())
            xa = dist.getXinf();
         int xb = (int)Math.round(mean + 3.0*sd);
         if (xb > dist.getXsup())
            xb = dist.getXsup();
         setParam(xa, xb);
      }
   }


   /**
    * Displays a chart of the cumulative distribution function (cdf) over the
    *    interval <SPAN CLASS="MATH">[<I>a</I>, <I>b</I>]</SPAN> on the screen using Swing. This method creates an
    *    application containing a chart panel displaying the chart. The created
    *    frame is positioned on-screen, and displayed before it is returned. The
    *    <TT>width</TT> and the <TT>height</TT> of the chart are measured in pixels.
    * 
    * @param width frame width in pixels.
    * 
    *    @param height frame height in pixels.
    * 
    *    @param a lower bound of interval
    * 
    *    @param b upper bound of interval
    * 
    *    @return frame containing the chart
    * 
    */
   public JFrame viewCdf (int width, int height, int a, int b)  {
      setParam(a,b);
      return cdfChart.view(width, height);
   }


   /**
    * Similar to method {@link #viewCdf viewCdf} above.
    *  If the interval <SPAN CLASS="MATH">[<I>a</I>, <I>b</I>]</SPAN> for the graph is
    *  not defined, it will be  set automatically to 
    * <SPAN CLASS="MATH">[<I>&#956;</I> -3<I>&#963;</I>, <I>&#956;</I> +3<I>&#963;</I>]</SPAN>,
    *  where <SPAN CLASS="MATH"><I>&#956;</I></SPAN> and <SPAN CLASS="MATH"><I>&#963;</I></SPAN> are the mean and the variance of the distribution.
    * 
    * @param width frame width in pixels
    * 
    *    @param height frame height in pixels
    * 
    *    @return frame containing the chart
    * 
    */
   public JFrame viewCdf (int width, int height)  {
      testParam();
      return cdfChart.view(width, height);
   }


   /**
    * Displays a chart of the probability mass function over the interval <SPAN CLASS="MATH">[<I>a</I>, <I>b</I>]</SPAN>
    *    on the screen using Swing. This method creates an application containing
    *    a chart panel displaying the chart. The created frame is positioned
    *    on-screen, and displayed before it is returned. The <TT>width</TT> and
    *    the <TT>height</TT> of the chart are measured in pixels.
    * 
    * @param width frame width in pixels.
    * 
    *    @param height frame height in pixels.
    * 
    *    @param a lower bound of interval
    * 
    *    @param b upper bound of interval
    * 
    *    @return frame containing the chart
    * 
    */
   public JFrame viewProb (int width, int height, int a, int b)  {
      setParam(a,b);
      return probChart.viewBar(width, height);
   }


   /**
    * Similar to method {@link #viewProb viewProb} above. 
    *  If the interval <SPAN CLASS="MATH">[<I>a</I>, <I>b</I>]</SPAN> for the graph is
    *  not defined, it will be  set automatically to 
    * <SPAN CLASS="MATH">[<I>&#956;</I> -3<I>&#963;</I>, <I>&#956;</I> +3<I>&#963;</I>]</SPAN>,
    *  where <SPAN CLASS="MATH"><I>&#956;</I></SPAN> and <SPAN CLASS="MATH"><I>&#963;</I></SPAN> are the mean and the variance of the distribution.
    * 
    * @param width frame width in pixels.
    * 
    *    @param height frame height in pixels.
    * 
    *    @return frame containing the chart
    * 
    */
   public JFrame viewProb (int width, int height)  {
      testParam();
      return probChart.viewBar(width, height);
   }


   /**
    * Sets the parameters <SPAN CLASS="MATH"><I>a</I></SPAN> and <SPAN CLASS="MATH"><I>b</I></SPAN> for this object.
    * 
    * @param a lower bound of interval
    * 
    *    @param b upper bound of interval
    * 
    * 
    */
   public void setParam (int a, int b)  {
      if (a >= b) throw new IllegalArgumentException
            ("a is bigger than b" + a + "  " + b);
      this.a = a;
      this.b = b;
      init();
   }


   /**
    * Exports a chart of the cumulative probability to a <SPAN CLASS="logo,LaTeX">L<SUP><SMALL>A</SMALL></SUP>T<SMALL>E</SMALL>X</SPAN> source code using
    *   PGF/TikZ.
    *    This method constructs and returns a string that can be written to
    *    a <SPAN CLASS="logo,LaTeX">L<SUP><SMALL>A</SMALL></SUP>T<SMALL>E</SMALL>X</SPAN> document to render the plot. <TT>width</TT> and <TT>height</TT>
    *    represents the width and the height of the produced chart. These dimensions
    *    do not take into account the axes and labels extra space. The <TT>width</TT>
    *    and the <TT>height</TT> of the chart are measured in centimeters.
    * 
    * @param width Chart's width in centimeters
    * 
    *    @param height Chart's height in centimeters
    * 
    *    @return LaTeX source code
    * 
    */
   public String toLatexCdf (int width, int height)  {
      testParam();
      return cdfChart.toLatex(width, height);
   }


   /**
    * Similar to {@link #toLatexCdf toLatexCdf}, but for the probability instead
    *    of the cdf.
    * 
    * @param width Chart's width in centimeters
    * 
    *    @param height Chart's height in centimeters
    * 
    *    @return LaTeX source code
    * 
    */
   public String toLatexProb (int width, int height)  {
      testParam();
      return probChart.toLatex(width, height);
   }


   /**
    * Returns the chart of the cdf.
    * 
    * @return the chart of the cdf.
    * 
    */
   public XYLineChart getCdf ()  {
      return cdfChart;
   }


   /**
    * Returns the chart of the probability.
    * 
    * @return the chart of the probability.
    * 
    */
   public XYLineChart getProb ()  {
      return probChart;
   }


}
