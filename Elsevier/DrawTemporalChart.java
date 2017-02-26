/*******************
Author  : Amit Ruhela
Purpose : This class plots the Topics temporaly. 
Example is given as bewakoofiyaan_LTAW0_STAWw9.png
Plots three phases of Event : Growth, Peak and Decay: All are color coded
Plot all events of a topic

*******************/
package Elsevier;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
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
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;

public class DrawTemporalChart extends ApplicationFrame
{
	private static final long serialVersionUID = 1L;
	static String s = null;
	static MyInfoClass lMyInfoInst;

	static double eventTotalFreq = 0;
	static double eventTotalFreqIntervals = 0;

	static int scale_width = 20;
	static int scale_height = 2;

	/*************************************************************************
	 * Constructor
	 * @param mMaxTime 
	 *************************************************************************/
	public DrawTemporalChart(MyInfoClass myInfoInst)
	{
		super("");
		lMyInfoInst = myInfoInst;
	}

	/*************************************************************************
	 * DrawYourChart
	 *************************************************************************/
	public JFreeChart DrawYourChart()
	{
		JFreeChart jfreechart = createTimeSeriesChart();

		ChartPanel chartpanel = new ChartPanel(jfreechart, true, true, true, true, true);
		chartpanel.setPreferredSize(new Dimension(scale_width * HashDefinesForED.Chart_Width, scale_height * HashDefinesForED.Chart_Height));
		chartpanel.setBackground(Color.white);
		chartpanel.setMouseWheelEnabled(true);
		chartpanel.setMouseZoomable(true);
		chartpanel.setRangeZoomable(true);
		//chartpanel.setDomainZoomable(true);
		setContentPane(chartpanel);
		return jfreechart;
	}

	/*************************************************************************
	 * CreateChart
	 * **********************************************************************/
	public JFreeChart createTimeSeriesChart()
	{
		NumberFormat formatter = new DecimalFormat("#0.00");

		JFreeChart jfreechart = null;
		XYDataset xydataset1;
		XYDataset xydataset2;
		try
		{

			XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, false); // Lines only
			renderer1.setSeriesPaint(0, Color.lightGray);
			renderer1.setSeriesStroke(0, new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 1.0f));

			XYItemRenderer renderer2 = new XYLineAndShapeRenderer(true, false); // Lines only
			renderer2.setSeriesPaint(0, Color.black);
			renderer2.setSeriesStroke(0, new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 1.0f));

			//Dataset STA
			xydataset2 = createSTADataset();
			//Dataset Original
			xydataset1 = createXYTimeSeriesDataset();

			jfreechart = ChartFactory.createTimeSeriesChart(s, "Time", "Tweets", xydataset1, true, false, false);
			XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
			xyPlot.setBackgroundPaint(Color.white);
			xyPlot.setRangePannable(true);
			xyPlot.setDomainPannable(true);
			//xyPlot.setDomainGridlinesVisible(true);
			xyPlot.setRangeGridlinesVisible(true);
			xyPlot.setRangeGridlinePaint(Color.black);
			//xyPlot.setDomainGridlinePaint(Color.black);

			//xyPlot.setDataset(1, xydataset1);
			//xyPlot.setRenderer(1, renderer1);

			xyPlot.setDataset(0, xydataset2);
			xyPlot.setRenderer(0, renderer2);

			//AddTemporalThresholds(xyPlot);
			//AddAnotherMarkers(xyPlot);

			xyPlot.setDomainCrosshairVisible(true);
			xyPlot.setRangeCrosshairVisible(true);

			AddPhaseMarkedEvents(xyPlot);

			String TimeUnit = "";
			if (lMyInfoInst.FREQ_IntervalTime < 3600)
				TimeUnit = formatter.format(1.0 * lMyInfoInst.FREQ_IntervalTime / 60) + " Min";
			else if (lMyInfoInst.FREQ_IntervalTime < (24 * 3600))
				TimeUnit = formatter.format(1.0 * lMyInfoInst.FREQ_IntervalTime / 3600) + " Hrs";
			else
				TimeUnit = formatter.format(1.0 * lMyInfoInst.FREQ_IntervalTime / (24 * 3600)) + " Days";

			int lifetime = (GetAlpha.myInfoInst.EndInterval - GetAlpha.myInfoInst.StartInterval) * lMyInfoInst.FREQ_IntervalTime / (24 * 3600);
			s = "Topic=" + lMyInfoInst.TopicName + ", Tweets=" + lMyInfoInst.Tweets + ", Events=" + GetAlpha.ClassifiedMergedEvents.size() + "/"
					+ lMyInfoInst.AllEventsCount + ", Life=" + lifetime + " Days" + ",\n TimeUnit=" + TimeUnit + ", STAWindow=" + (lMyInfoInst.STAWindow) + ", IAT="
					+ formatter.format(lMyInfoInst.IAT_NintyPercentileAvg) + ", Saved=" + formatter.format(100.0 * lMyInfoInst.SavedTweets / lMyInfoInst.Tweets) + " %";

			jfreechart.setTitle(s);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return jfreechart;

	}

	/*************************************************************************
	 * createXYTimeSeriesDataset
	 * @throws ParseException 
	 *************************************************************************/
	@SuppressWarnings({ "static-method", "deprecation" })
	public XYDataset createXYTimeSeriesDataset()
	{		
		TimeSeries ts = new TimeSeries("Original Data", Second.class);
		try
		{
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date firstDay = df.parse(HashDefinesForED.gStrFirstDay);

			String st;
			BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(HashDefinesForED.FreqDataFolder + GetAlpha.myInfoInst.TopicName + ".txt")));
			while((st = br_i.readLine()) != null)
			{

				int i = Integer.parseInt(st.split("\t")[0]);
				int mFreq = Integer.parseInt(st.split("\t")[1]);

				//int mFreq = GetAlpha.FreqMap[i];
				Date d = new Date(firstDay.getTime() + i * lMyInfoInst.FREQ_IntervalTime * 1000l);
				ts.add(new Second(d), mFreq);

				if ((d.getHours() > 1) && (d.getHours() < 8))
				{
					continue;
				}
				else
				{
					eventTotalFreq += mFreq;
					eventTotalFreqIntervals++;
				}
			}
			br_i.close();

		}
		catch (Exception e)
		{
			CommFunctForED.logAndPrint("Exception in createXYTimeSeriesDataset " + e.getMessage());
		}
		return new TimeSeriesCollection(ts);

	}

	/*************************************************************************
	 * createSTADataset
	 * @throws ParseException 
	 *************************************************************************/
	@SuppressWarnings({ "static-method", "deprecation" })
	public XYDataset createSTADataset()
	{
		TimeSeries ts2 = null;
		try
		{
			String st;
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date firstDay = df.parse(HashDefinesForED.gStrFirstDay);

			long kLong = 1l;
			ts2 = new TimeSeries("STA Data", Second.class);
			BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(HashDefinesForED.FreqDataFolder + GetAlpha.myInfoInst.TopicName + ".txt")));
			while((st = br_i.readLine()) != null)
			{
				int i = Integer.parseInt(st.split("\t")[0]);
				double mFreq = Double.parseDouble(st.split("\t")[2]);

				Date d = new Date(firstDay.getTime() + kLong * i * lMyInfoInst.FREQ_IntervalTime * 1000);
				ts2.add(new Second(d), mFreq);
				//CommFunctForED.logAndPrint("i= " + i + " === " + d.toString()+" Value = "+GetAlpha.SmoothedFreqMap[i]);
			}
			br_i.close();

		}
		catch (Exception e)
		{
			CommFunctForED.logAndPrint("DrawTemporalChart.createSmoothDataset()");
		}
		return new TimeSeriesCollection(ts2);
	}

	/*************************************************************************
	 * createLTADataset
	 * @throws ParseException 
	 *************************************************************************/
	@SuppressWarnings({ "static-method", "deprecation" })
	public XYDataset createLTADataset()
	{
		TimeSeries ts2 = null;
		try
		{
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date firstDay = df.parse(HashDefinesForED.gStrFirstDay);

			long kLong = 1l;
			ts2 = new TimeSeries("LTA Data", Second.class);
			String st = null;
			BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(HashDefinesForED.FreqDataFolder + GetAlpha.myInfoInst.TopicName + ".txt")));
			while((st = br_i.readLine()) != null)
			{
				int i = Integer.parseInt(st.split("\t")[0]);
				double mFreq = Double.parseDouble(st.split("\t")[3]);

				Date d = new Date(firstDay.getTime() + kLong * i * lMyInfoInst.FREQ_IntervalTime * 1000);
				ts2.add(new Second(d), mFreq);
				//CommFunctForED.logAndPrint("i= " + i + " === " + d.toString()+" Value = "+GetAlpha.SmoothedFreqMap[i]);
			}
			br_i.close();

		}
		catch (Exception e)
		{
			CommFunctForED.logAndPrint("DrawTemporalChart.createSmoothDataset()");
		}
		return new TimeSeriesCollection(ts2);
	}

	/*************************************************************************
	 * AddTemporalThresholds
	 *************************************************************************/
	@SuppressWarnings("static-method")
	public void AddTemporalThresholds(XYPlot mPlot)
	{
		double meanValue = lMyInfoInst.Interval_Mean;
		//mPlot.addRangeMarker(new ValueMarker(2.5 * meanValue, Color.orange, new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 1.0f)));
		mPlot.addRangeMarker(new ValueMarker(1 * meanValue, Color.black, new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 1.0f)));
		//mPlot.addRangeMarker(new ValueMarker(0.4 * meanValue, Color.green, new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 1.0f)));
	}

	/*************************************************************************
	 * AddAnotherMarkers
	 *************************************************************************/
	@SuppressWarnings("static-method")
	public void AddAnotherMarkers(XYPlot mPlot)
	{
		double meanValue = eventTotalFreq / eventTotalFreqIntervals;
		CommFunctForED.logAndPrint("  AddAnotherMarkers: meanValue=" + meanValue + "   eventTotalFreq=" + eventTotalFreq + " eventTotalFreqIntervals="
				+ eventTotalFreqIntervals);
		//mPlot.addRangeMarker(new ValueMarker(2.5 * meanValue, Color.orange, new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 1.0f)));
		mPlot.addRangeMarker(new ValueMarker(1 * meanValue, Color.red, new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 1.0f)));
		//mPlot.addRangeMarker(new ValueMarker(0.4 * meanValue, Color.green, new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 1.0f)));
	}

	/*************************************************************************
	 * AddRealEvents
	 * @param mPlot 
	 *************************************************************************/
	public static void AddRealEvents(XYPlot mPlot)
	{
		//CommFunctForED.logAndPrint("count = " + eventsCount);
		for (int i = 0; i < GetAlpha.ClassifiedMergedEvents.size(); i++)
		{
			String[] Event = GetAlpha.ClassifiedMergedEvents.get(i).split("_");

			Date d1 = new Date(Long.parseLong(Event[0]));
			Date d2 = new Date(Long.parseLong(Event[1]));
			//CommFunctForED.logAndPrint(lMyInfoInst.TopicID + ", " + d1.toString() + "  --  " + d2.toString());
			IntervalMarker mrkr = new IntervalMarker(d1.getTime(), d2.getTime());
			if (i % 2 == 0)
			{
				mrkr.setPaint(Color.red);
			}
			else
			{
				mrkr.setPaint(Color.blue);

			}
			mrkr.setAlpha(0.2f);
			mPlot.addDomainMarker(mrkr);
		}
	}

	/*************************************************************************
	 * AddMergedEvents
	 * @param mPlot 
	 *************************************************************************/
	public static void AddMergedEvents(XYPlot mPlot)
	{
		//CommFunctForED.logAndPrint("count = " + eventsCount);
		for (int i = 0; i < GetAlpha.ClassifiedMergedEvents.size(); i++)
		{
			String[] Event = GetAlpha.ClassifiedMergedEvents.get(i).split("_");

			Date d1 = new Date(Long.parseLong(Event[0]));
			Date d2 = new Date(Long.parseLong(Event[1]));
			//CommFunctForED.logAndPrint(lMyInfoInst.TopicID + ", " + d1.toString() + "  --  " + d2.toString());
			IntervalMarker mrkr = new IntervalMarker(d1.getTime(), d2.getTime());
			if (i % 2 == 0)
			{
				mrkr.setPaint(Color.red);
			}
			else
			{
				mrkr.setPaint(Color.blue);

			}
			mrkr.setAlpha(0.2f);
			mPlot.addDomainMarker(mrkr);
		}

	}

	/*************************************************************************
	 * AddPhaseMarkedEvents
	 * @param mPlot 
	 *************************************************************************/
	public static void AddPhaseMarkedEvents(XYPlot mPlot)
	{
		//CommFunctForED.logAndPrint("count = " + eventsCount);
		for (int i = 0; i < GetAlpha.ClassifiedMergedEvents.size(); i++)
		{

			String[] Event = GetAlpha.ClassifiedMergedEvents.get(i).split("_");

			Date d1 = new Date(Long.parseLong(Event[0]));
			Date d2 = new Date(Long.parseLong(Event[1]));
			Date d3 = new Date(Long.parseLong(Event[2]));
			Date d4 = new Date(Long.parseLong(Event[3]));
			//CommFunctForED.logAndPrint(lMyInfoInst.TopicID + ", " + d1.toString() + "  --  " + d2.toString());
			IntervalMarker mrkr1 = new IntervalMarker(d1.getTime(), d2.getTime());
			IntervalMarker mrkr2 = new IntervalMarker(d2.getTime(), d3.getTime());
			IntervalMarker mrkr3 = new IntervalMarker(d3.getTime(), d4.getTime());
			/*if (i % 2 == 0)
			{
				mrkr1.setPaint(Color.red);
				mrkr3.setPaint(Color.red);
			}
			else
			{
				mrkr1.setPaint(Color.blue);
				mrkr3.setPaint(Color.blue);
			}*/
			mrkr1.setPaint(Color.green);
			mrkr2.setPaint(Color.blue);
			mrkr3.setPaint(Color.red);

			mrkr1.setAlpha(0.2f);
			mrkr2.setAlpha(0.2f);
			mrkr3.setAlpha(0.2f);
			mPlot.addDomainMarker(mrkr1);			
			mPlot.addDomainMarker(mrkr2);
			mPlot.addDomainMarker(mrkr3);
		}
	}

	/*************************************************************************
	 * writeGraph
	*************************************************************************/
	public static void writeTemporalGraph(JFreeChart mChart, String topic)
	{
		try
		{
			String chartFileName = HashDefinesForED.OutputFolder + "Events/" + topic + GetAlpha.MidFix + ".png";
			File out = new File(chartFileName);
			ChartUtilities.saveChartAsPNG(out, mChart, scale_width * HashDefinesForED.Chart_Width, scale_height * HashDefinesForED.Chart_Height);
		}
		catch (Exception e)
		{
			CommFunctForED.logAndPrint("Exception in writeTemporalGraph" + e.getMessage());
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

	/*************************************************************************
	 * windowClosing Dispose problem resolved
	 *************************************************************************/
	/*public void windowClosing(final WindowEvent evt)
	{
		if (evt.getWindow() == this)
		{			
			dispose();			 
		}
	}*/

}
