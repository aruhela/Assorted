/*******************
Author  : Amit Ruhela
Purpose : This class plots the events temporaly. 
Example is given as Medium_P_Medium_G_bewakoofiyaan_1.png
Plots three phases of Event : Growth, Peak and Decay: All are color coded
Plot Actual and Smoothed curves.

*******************/
package Elsevier;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.jfree.ui.VerticalAlignment;

public class PlotEventsChart extends ApplicationFrame
{
	private static final long serialVersionUID = 1L;
	static int eventid = 0;

	static double HighestPeakAmplitue = 0;
	static long HighestPeakTime = 0;
	static Font font10 = new Font("NimbusSanL-Regu", Font.BOLD, 10);
	static Font font15 = new Font("NimbusSanL-Regu", Font.BOLD, 15);
	static Font font20 = new Font("NimbusSanL-Regu", Font.BOLD, 20);
	static Font font25 = new Font("NimbusSanL-Regu", Font.BOLD, 25);
	static Font font30 = new Font("NimbusSanL-Regu", Font.BOLD, 30);
	static Font font35 = new Font("NimbusSanL-Regu", Font.PLAIN, 35);
	static NumberFormat formatter = new DecimalFormat("#0.00");
	//static DescriptiveStatistics ds = null;

	//static long preEventTime = 2 * 3600 * 1000;//2Hr

	long d1;
	long d2;
	long d3;
	long d4;

	static String eString = null;
	static String TopicName = "";
	static int eFREQ_IntervalTime = 0;
	//static double gThreshold = 0;
	static int maxValue_celebrities = 0;

	static double gThreshold = 0;

	/*************************************************************************
	 * Constructor
	 * @param mMaxTime 
	 *************************************************************************/
	public PlotEventsChart(String st)
	{
		super("");
		//CommFunctForED.logAndPrint("    PlotEventsChart.PlotEventsChart() " + st);
		eString = st;
		String[] arr = eString.split("\\t");

		TopicName = arr[0];
		eventid = Integer.parseInt(arr[1]);
		eFREQ_IntervalTime = Integer.parseInt(arr[11]);
		gThreshold = Double.parseDouble(arr[12]);

		maxValue_celebrities = 0;
		HighestPeakAmplitue = 0;
		HighestPeakTime = 0;
		//ds = new DescriptiveStatistics();

		String[] PhaseTimeEvent = arr[10].split("_");
		d1 = new Date(Long.parseLong(PhaseTimeEvent[0])).getTime();
		d2 = new Date(Long.parseLong(PhaseTimeEvent[1])).getTime();
		d3 = new Date(Long.parseLong(PhaseTimeEvent[2])).getTime();
		d4 = new Date(Long.parseLong(PhaseTimeEvent[3])).getTime();

	}

	/*************************************************************************
	 * DrawYourChart
	 *************************************************************************/
	public JFreeChart DrawYourChart()
	{
		JFreeChart jfreechart = createTimeSeriesChart();

		ChartPanel chartpanel = new ChartPanel(jfreechart, true, true, true, true, true);
		chartpanel.setPreferredSize(new Dimension(5 * HashDefinesForED.Chart_Width, 2 * HashDefinesForED.Chart_Height));
		chartpanel.setBackground(Color.white);
		setContentPane(chartpanel);
		return jfreechart;
	}

	/*************************************************************************
	 * CreateChart
	 * **********************************************************************/
	@SuppressWarnings("unused")
	public JFreeChart createTimeSeriesChart()
	{
		JFreeChart jfreechart = null;
		XYDataset xydataset1;
		XYDataset xydataset2;
		XYDataset xydataset3;//eRatio
		try
		{
			//Dataset Original
			//xydataset1 = createXYTimeSeriesDataset();

			//Dataset STA
			xydataset2 = createSTADataset();

			xydataset3 = createeRatioDataset();

			jfreechart = ChartFactory.createTimeSeriesChart(GetTitle(), "Time", "Tweets", xydataset2, false, false, false);
			//jfreechart.setTitle(GetTitle());
			jfreechart.getTitle().setFont(font35);

			//			TextTitle legendText = new TextTitle("This is LEGEND: ");
			//			legendText.setPosition(RectangleEdge.BOTTOM);
			//			chart.addSubtitle(legendText);

			Shape shape = new Ellipse2D.Double(-1, -1, 2, 2);

			XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, false); // Lines only
			renderer1.setSeriesShape(0, shape);
			renderer1.setSeriesPaint(0, Color.lightGray);
			renderer1.setSeriesStroke(0, new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 1.0f));

			XYItemRenderer renderer2 = new XYLineAndShapeRenderer(true, true); // Lines only
			renderer2.setSeriesShape(0, shape);
			renderer2.setSeriesPaint(0, Color.black);
			renderer2.setSeriesStroke(0, new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 1.0f));

			XYItemRenderer renderer3 = new XYLineAndShapeRenderer(true, false); // Lines only			
			renderer3.setSeriesPaint(0, Color.blue);
			//renderer3.setSeriesStroke(0, new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 1.0f));
			renderer3.setSeriesStroke(0, new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] { 3.0f }, 0.0f));

			XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
			xyPlot.setBackgroundPaint(Color.white);
			xyPlot.setRangeGridlinePaint(Color.gray);
			xyPlot.setRangeGridlinesVisible(false);
			xyPlot.setDomainCrosshairVisible(true);
			xyPlot.setRangeCrosshairVisible(true);

			ValueAxis yAxis = xyPlot.getRangeAxis();
			//yAxis.setRange(0, GetAlpha.GlobalHighestPeak);

			//xyPlot.setDataset(1, xydataset1);
			//xyPlot.setRenderer(1, renderer1);

			xyPlot.setDataset(0, xydataset2);
			xyPlot.setRenderer(0, renderer2);

			xyPlot.setDataset(2, xydataset3);
			xyPlot.setRenderer(2, renderer3);
			final NumberAxis YAxis2 = new NumberAxis("eRatio");
			YAxis2.setTickLabelFont(font30);
			xyPlot.setRangeAxis(1, YAxis2);
			xyPlot.mapDatasetToRangeAxis(2, 1);

			xyPlot.getDomainAxis().setLabelFont(font35);
			xyPlot.getRangeAxis().setLabelFont(font35);
			
			xyPlot.getDomainAxis().setTickLabelFont(font30);
			xyPlot.getRangeAxis().setTickLabelFont(font30);
			xyPlot.getRangeAxis(1).setLabelFont(font30);

			//((DateAxis)xyPlot.getDomainAxis()).setDateFormatOverride(new SimpleDateFormat("H:mm"));

			xyPlot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
			AddAnotherMarkers(xyPlot);
			AddPhaseMarkedEvents(xyPlot);

			LegendTitle legend = new LegendTitle(xyPlot);
			//LegendTitle legend = jfreechart.getLegend();
			//legend.setPosition(RectangleEdge.RIGHT);

			legend.setBackgroundPaint(new Color(200, 200, 255, 100));
			//legend.setFrame(new BlockBorder(Color.white));
			//legend.setPosition(RectangleEdge.BOTTOM);
			legend.setItemFont(font20);
			legend.setVerticalAlignment(VerticalAlignment.TOP);
			legend.setHorizontalAlignment(HorizontalAlignment.CENTER);
			XYTitleAnnotation ta = new XYTitleAnnotation(0.90, 0.80, legend, RectangleAnchor.BOTTOM_RIGHT); //x,y
			ta.setMaxWidth(0.19);
			xyPlot.addAnnotation(ta);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return jfreechart;
	}

	/*************************************************************************
	 *GetTitle
	 *************************************************************************/
	@SuppressWarnings("boxing")
	private String GetTitle()
	{
		/*String TimeUnit = "";
		if (eFREQ_IntervalTime < 3600)
			TimeUnit = formatter.format(1.0 * eFREQ_IntervalTime / 60) + " Min";
		else if (eFREQ_IntervalTime < (24 * 3600))
			TimeUnit = formatter.format(1.0 * eFREQ_IntervalTime / 3600) + " Hrs";
		else
			TimeUnit = formatter.format(1.0 * eFREQ_IntervalTime / (24 * 3600)) + " Days";

		long lifetime = (d4 - d1) / 1000;
		String eventlife = "";
		if (lifetime < 3600)
			eventlife = formatter.format(1.0 * lifetime / 60) + " Min";
		else if (lifetime < (24 * 3600))
			eventlife = formatter.format(1.0 * lifetime / 3600) + " Hrs";
		else
			eventlife = formatter.format(1.0 * lifetime / (24 * 3600)) + " Days";
		*/
		DateFormat df = new SimpleDateFormat("MMM dd, yyyy");
		return /*TopicName + "\n" +*/df.format(d1) + "  to  " + df.format(d4);

	}

	/*************************************************************************
	 * createXYDataset
	 *************************************************************************/
	public XYDataset createXYTimeSeriesDataset()
	{
		TimeSeries ts = new TimeSeries("Actual Frequency");
		try
		{
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date firstDay = df.parse(HashDefinesForED.gStrFirstDay);

			String st;
			BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(HashDefinesForED.FreqDataFolder + TopicName + ".txt")));
			while((st = br_i.readLine()) != null)
			{
				int index = Integer.parseInt(st.split("\t")[0]);
				int mFreq_act = Integer.parseInt(st.split("\t")[1]);
				int mFreq_smoothed = (int) Double.parseDouble(st.split("\t")[2]);
				long d = firstDay.getTime() + index * eFREQ_IntervalTime * 1000l;
				if ((d >= d1) && (d <= d4))
				{
					ts.add(new Second(new Date(d)), mFreq_act);

					/*if ((d.getHours() > GetAlpha.NightTime_Start) && (d.getHours() < GetAlpha.NightTime_End))
					{
						continue;
					}
					else
					{
						ds.addValue(mFreq_smoothed);
					}*/

					if (mFreq_smoothed > HighestPeakAmplitue)
					{
						HighestPeakAmplitue = mFreq_smoothed;
						HighestPeakTime = d;
					}

				}
			}

			br_i.close();
		}
		catch (Exception e)
		{
			CommFunctForED.logAndPrint("Exception in createXYTimeSeriesDataset");
			e.printStackTrace();
		}

		return new TimeSeriesCollection(ts);

	}

	/*************************************************************************
	 * createXYDataset
	 *************************************************************************/
	public XYDataset createSTADataset()
	{
		TimeSeries ts = new TimeSeries("STA Data");
		try
		{
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			//DateFormat df = new SimpleDateFormat("h:mm");
			df.setTimeZone(TimeZone.getTimeZone("GMT"));

			Date firstDay = df.parse(HashDefinesForED.gStrFirstDay);

			String st;
			BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(HashDefinesForED.FreqDataFolder + TopicName + ".txt")));
			while((st = br_i.readLine()) != null)
			{
				int index = Integer.parseInt(st.split("\t")[0]);
				int mFreq = (int) Double.parseDouble(st.split("\t")[2]);
				long d = firstDay.getTime() + index * eFREQ_IntervalTime * 1000l;
				if ((d >= d1) && (d <= d4))
				{
					ts.add(new Second(new Date(d)), mFreq);
				}
			}
			br_i.close();
		}
		catch (Exception e)
		{
			CommFunctForED.logAndPrint("Exception in createSTADataset");
			e.printStackTrace();
		}
		return new TimeSeriesCollection(ts);
	}

	/*************************************************************************
	 * createXYDataset
	 *************************************************************************/
	public XYDataset createeRatioDataset()
	{
		TimeSeries ts = new TimeSeries("eRatio Data");
		try
		{
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			//DateFormat df = new SimpleDateFormat("h:mm");
			df.setTimeZone(TimeZone.getTimeZone("GMT"));

			Date firstDay = df.parse(HashDefinesForED.gStrFirstDay);

			String st;
			BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(HashDefinesForED.FreqDataFolder + TopicName + ".txt")));
			 br_i.readLine();
			while((st = br_i.readLine()) != null)
			{
				int index = Integer.parseInt(st.split("\t")[0]);
				double eratio = Double.parseDouble(st.split("\t")[4]);
				long d = firstDay.getTime() + index * eFREQ_IntervalTime * 1000l;
				if ((d >= d1) && (d <= d4))
				{
					ts.add(new Second(new Date(d)), eratio);
				}
			}
			br_i.close();
		}
		catch (Exception e)
		{
			CommFunctForED.logAndPrint("Exception in createSTADataset");
			e.printStackTrace();
		}
		return new TimeSeriesCollection(ts);
	}

	/*************************************************************************
	 * AddPeaks
	 *************************************************************************/
	public static void AddAnotherMarkers(XYPlot mPlot)
	{
		//double avgValue = ds.getMean();
		//double stdValue = ds.getStandardDeviation();
		double peakTemp_up = HighestPeakAmplitue;
		//double peakTemp_down = HighestPeakAmplitue;

		//CommFunctForED.logAndPrint("ds.mean=" + ds.getMean() + " stdValue=" + stdValue);

		//		final Marker avgVM = new ValueMarker(avgValue);
		//		avgVM.setPaint(Color.black);
		//		avgVM.setLabel(" avgValue");
		//		avgVM.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
		//		avgVM.setLabelTextAnchor(TextAnchor.TOP_LEFT);
		//		avgVM.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 10.0f }, 0.0f));

		final Marker avgVM_2 = new ValueMarker(gThreshold);
		avgVM_2.setPaint(Color.black);
		//avgVM_2.setLabel(" avgValue + " + GetAlpha.PeakThrSTDCount + "*stdValue");
		//avgVM_2.setLabel(" Threshold = " + formatter.format(gThreshold));
		avgVM_2.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
		avgVM_2.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
		avgVM_2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 10.0f }, 0.0f));
		avgVM_2.setLabelFont(font10);

		//double ratio = GetAlpha.PeakEventThreshold;
		final Marker start = new ValueMarker(1.5);
		//final Marker start = new ValueMarker(ratio * peakTemp_up);
		start.setPaint(Color.green);
		start.setLabel("ETT");
		start.setLabelFont(font25);
		start.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
		start.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
		start.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 10.0f }, 0.0f));

		double ratio = 0.20;
		final Marker end = new ValueMarker(0.5);
		//final Marker end = new ValueMarker(ratio * peakTemp_down);
		end.setPaint(Color.red);
		end.setLabel("EDT");
		end.setLabelFont(font25);
		end.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
		end.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
		end.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 10.0f }, 0.0f));

		//CommFunctForED.logAndPrint("eventid=" + eventid + " avgValue=" + avgValue + " HighestPeakAmplitue=" + HighestPeakAmplitue);

		//mPlot.addRangeMarker(avgVM);
		//mPlot.addRangeMarker(avgVM_2);
		mPlot.addRangeMarker(2, start, Layer.FOREGROUND);
		mPlot.addRangeMarker(2, end, Layer.FOREGROUND);

		/*double ratio = 2.0;
		final Marker avgVM = new ValueMarker(gThreshold);
		avgVM.setPaint(Color.blue);
		avgVM.setLabel("Threshold of Steady Phase");
		avgVM.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
		avgVM.setLabelTextAnchor(TextAnchor.TOP_LEFT);
		avgVM.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 10.0f }, 0.0f));
		mPlot.addRangeMarker(avgVM);*/

	}

	/*************************************************************************
	 * AddPeaks
	 * @param mPlot 
	 *************************************************************************/
	public void AddPhaseMarkedEvents(XYPlot mPlot)
	{
		IntervalMarker mrkr1 = new IntervalMarker(d1, d2);
		IntervalMarker mrkr2 = new IntervalMarker(d2, d3);
		IntervalMarker mrkr3 = new IntervalMarker(d3, d4);
		mrkr1.setPaint(Color.green);
		mrkr2.setPaint(Color.blue);
		mrkr3.setPaint(Color.red);

		mrkr1.setAlpha(0.25f);
		mrkr2.setAlpha(0.25f);
		mrkr3.setAlpha(0.25f);

		mPlot.addDomainMarker(mrkr1);
		mPlot.addDomainMarker(mrkr2);
		mPlot.addDomainMarker(mrkr3);
	}

	/*************************************************************************
	 * writeGraph
	*************************************************************************/
	public static void writeEventGraph(JFreeChart mChart)
	{
		String arr[] = eString.split("\t");
		try
		{
			String chartFileName = HashDefinesForED.EventsFolder_Events;
			if (!arr[4].equals("."))
			{
				chartFileName += arr[6] + "_" + arr[7] + "_" + TopicName + "_" + eventid + ".png";
			}
			else
			{
				chartFileName += TopicName + "_" + eventid + ".png";
			}
			File out = new File(chartFileName);
			ChartUtilities.saveChartAsPNG(out, mChart, (int) (2.2 * HashDefinesForED.Chart_Width), (int) (2.2 * HashDefinesForED.Chart_Height));
		}
		catch (Exception e)
		{
			CommFunctForED.logAndPrint("Exception in writeEventGraph");
			e.printStackTrace();
		}
	}

	/*************************************************************************
	 * Sort the Array
	 *************************************************************************/
	static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map)
	{
		SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<>(new Comparator<Map.Entry<K, V>>()
		{
			@Override
			public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2)
			{
				int res = -(e1.getValue().compareTo(e2.getValue()));
				return res != 0 ? res : 1; // Special fix to preserve items with equal values
			}
		});
		sortedEntries.addAll(map.entrySet());
		return sortedEntries;
	}
}
