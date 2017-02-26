/*******************
Author  : Amit Ruhela
Purpose : Common Function are wriiten here.

*******************/
package Elsevier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.RefineryUtilities;

public class CommFunctForED
{
	public static BufferedWriter out_Log = null;
	public static int Dataset = -1;

	public CommFunctForED(int ds)
	{
		super();
		Dataset = ds;
	}

	/*************************************************************************
	 * Find the count of tweet for all the intervals.
	 *************************************************************************/
	public static void ComputeTweetsFrequencyPerTimeInterval()
	{
		DescriptiveStatistics Freq_stats = new DescriptiveStatistics();
		for (int i = GetAlpha.myInfoInst.StartInterval; i <= GetAlpha.myInfoInst.EndInterval; i++)
		{
			Freq_stats.addValue(GetAlpha.FreqMap[i]);
		}
		GetAlpha.myInfoInst.Interval_Mean = Freq_stats.getMean();
		GetAlpha.myInfoInst.Interval_Median = Freq_stats.getPercentile(50);
		GetAlpha.myInfoInst.Interval_Skewness = Freq_stats.getSkewness();
		GetAlpha.myInfoInst.Interval_STD = Freq_stats.getStandardDeviation();
		GetAlpha.myInfoInst.Interval_NintyPercentileAvg = GetPercentileAverage(90, Freq_stats, false);
		GetAlpha.myInfoInst.Interval_Kurtosis = Freq_stats.getKurtosis();
		GetAlpha.myInfoInst.Interval_PopulationVariance = Freq_stats.getPopulationVariance();
		GetAlpha.myInfoInst.Interval_SumSquare = Freq_stats.getSumsq();
		GetAlpha.myInfoInst.Interval_Variance = Freq_stats.getVariance();

		if (GetAlpha.GlobalHighestPeak < Freq_stats.getMax())
		{
			GetAlpha.GlobalHighestPeak = (int) Freq_stats.getMax();
		}

		CommFunctForED.logAndPrint("ComputeTweetsFrequencyPerTimeInterval : Interval Mean=" + Freq_stats.getMean() + " Interval_NintyPercentileAvg="
				+ GetAlpha.myInfoInst.Interval_NintyPercentileAvg);
	}

	/*************************************************************************
	 * infer and write the event
	 *************************************************************************/
	public static void WriteEvent(long StartEventTime, long EndEventTime, double avg, int netSaved, MyInfoClass myInfoInst, StringBuffer eventsBuf, int STA_WINDOW)
	{

		NumberFormat formatter = new DecimalFormat("#0.00");

		int StartFreqInterval = (int) ((StartEventTime) / (1000L * myInfoInst.FREQ_IntervalTime));
		int EndFreqInterval = (int) ((EndEventTime) / (1000L * myInfoInst.FREQ_IntervalTime));

		long lStartTime = StartEventTime;
		long lEndTime = EndEventTime;

		double RateOfConsumption = avg;// Per minute

		eventsBuf.append(HashDefinesForED.EVENT_START + "\t" + myInfoInst.TopicName + "\t" + lStartTime + "\t" + RateOfConsumption + "\t" + netSaved + "\t"
				+ StartFreqInterval);
		if (HashDefinesForED.PerServerED)
		{
			//eventsBuf.append("\t" + myInfoInst.server + "\n");
		}
		else
		{
			eventsBuf.append("\n");
		}

		eventsBuf
				.append(HashDefinesForED.EVENT_END + "\t" + myInfoInst.TopicName + "\t" + lEndTime + "\t" + RateOfConsumption + "\t" + netSaved + "\t" + EndFreqInterval);
		if (HashDefinesForED.PerServerED)
		{
			//eventsBuf.append("\t" + myInfoInst.server + "\n\n");
		}
		else
		{
			eventsBuf.append("\n\n");
		}

		logWithoutNewLine(myInfoInst.BigEventsCount + ") +\t" + StartFreqInterval + "\t-\t " + EndFreqInterval + "\tSTA=" + STA_WINDOW + "\tStartEventTime="
				+ (StartEventTime + HashDefinesForED.TimeIndex) + "\tEndEventTime=" + (EndEventTime + HashDefinesForED.TimeIndex));
		logWithoutNewLine("\tConsumptionRate=" + formatter.format(RateOfConsumption));
		logWithoutNewLine("\tSavedTweets=" + netSaved + "\n");

	}

	/*************************************************************************
	 * Writes events in a file
	 *************************************************************************/
	public static void LogEvents(boolean printHdr, String EventsBufStr, String Prefix) throws IOException
	{
		BufferedWriter out = null;
		String FName = HashDefinesForED.EventsFolder + Prefix + HashDefinesForED.FName_Events;

		if (HashDefinesForED.PerServerED)
		{
			FName = HashDefinesForED.EventsFolder + Prefix + HashDefinesForED.FName_Events;
		}
		out = new BufferedWriter(new FileWriter(FName, !printHdr));

		if (printHdr)
		{
			out.write("#EventState\t");// Event Started or finished
			out.write("Topic\t");
			out.write("EventTime\t");
			out.write("RateOfProduction(PerMinute)\t");
			out.write("AbsoluteMagnitude\t");

			if (HashDefinesForED.PerServerED)
			{
				out.write("FreqInterval\t");
				out.write("Server\n");
			}
			else
			{
				out.write("FreqInterval\n");
			}

		}
		else
		{
			out.write(EventsBufStr);
		}
		out.close();
	}

	/*************************************************************************
	 * Returns True if the file is sorted
	 *************************************************************************/
	public static void PrintResults(boolean isHeaderLine, MyInfoClass myInfoInst) throws IOException
	{
		BufferedWriter out = null;
		String FName = HashDefinesForED.EventsFolder + HashDefinesForED.FName_TopicsInfo;
		out = new BufferedWriter(new FileWriter(FName, !isHeaderLine));

		if (isHeaderLine)
		{
			StringBuffer stBuf = new StringBuffer();

			//stBuf.append("TopicID\t");
			stBuf.append("#TopicName\t");
			stBuf.append("Tweets\t");

			stBuf.append("StartTime\t");
			stBuf.append("EndTime\t");

			stBuf.append("IAT_Mean" + "\t");
			stBuf.append("IAT_Median" + "\t");
			stBuf.append("IAT_Skewness" + "\t");
			stBuf.append("IAT_STD" + "\t");
			stBuf.append("IAT_NintyPercentileAvg" + "\t");
			stBuf.append("IAT_Kurtosis" + "\t");
			stBuf.append("IAT_PopulationVariance" + "\t");
			stBuf.append("IAT_SumSquare" + "\t");
			stBuf.append("IAT_Variance" + "\t");

			stBuf.append("StartInterval" + "\t");
			stBuf.append("EndInterval" + "\t");

			stBuf.append("Interval_Mean" + "\t");
			stBuf.append("Interval_Median" + "\t");
			stBuf.append("Interval_Skewness" + "\t");
			stBuf.append("Interval_STD" + "\t");
			stBuf.append("Interval_NintyPercentileAvg" + "\t");
			stBuf.append("Interval_Kurtosis" + "\t");
			stBuf.append("Interval_PopulationVariance" + "\t");
			stBuf.append("Interval_SumSquare" + "\t");
			stBuf.append("Interval_Variance" + "\t");

			//stBuf.append("Scale" + "\t");
			stBuf.append("STAWindow" + "\t");
			stBuf.append("LTAWindow" + "\t");
			stBuf.append("FREQ_IntervalTime" + "\t");

			stBuf.append("BigEventsCount" + "\t");
			stBuf.append("AllEventsCount" + "\t");
			stBuf.append("EventsQuality" + "\t");
			stBuf.append("SavedTweets" + "\t");
			stBuf.append("SavedTweetsRatio" + "\n");

			out.write(stBuf.toString().trim() + "\n");
		}
		else
		{
			NumberFormat formatter = new DecimalFormat("#0.0000");

			StringBuffer stBuf = new StringBuffer();
			//stBuf.append(myInfoInst.TopicID + "\t");
			stBuf.append(myInfoInst.TopicName + "\t");
			stBuf.append(myInfoInst.Tweets + "\t");

			stBuf.append(myInfoInst.StartTime + "\t");
			stBuf.append(myInfoInst.EndTime + "\t");

			stBuf.append(formatter.format(myInfoInst.IAT_Mean) + "\t");
			stBuf.append(formatter.format(myInfoInst.IAT_Median) + "\t");
			stBuf.append(formatter.format(myInfoInst.IAT_Skewness) + "\t");
			stBuf.append(formatter.format(myInfoInst.IAT_STD) + "\t");
			stBuf.append(formatter.format(myInfoInst.IAT_NintyPercentileAvg) + "\t");
			stBuf.append(formatter.format(myInfoInst.IAT_Kurtosis) + "\t");
			stBuf.append(formatter.format(myInfoInst.IAT_PopulationVariance) + "\t");
			stBuf.append(formatter.format(myInfoInst.IAT_SumSquare) + "\t");
			stBuf.append(formatter.format(myInfoInst.IAT_Variance) + "\t");

			stBuf.append(myInfoInst.StartInterval + "\t");
			stBuf.append(myInfoInst.EndInterval + "\t");

			stBuf.append(formatter.format(myInfoInst.Interval_Mean) + "\t");
			stBuf.append(formatter.format(myInfoInst.Interval_Median) + "\t");
			stBuf.append(formatter.format(myInfoInst.Interval_Skewness) + "\t");
			stBuf.append(formatter.format(myInfoInst.Interval_STD) + "\t");
			stBuf.append(formatter.format(myInfoInst.Interval_NintyPercentileAvg) + "\t");
			stBuf.append(formatter.format(myInfoInst.Interval_Kurtosis) + "\t");
			stBuf.append(formatter.format(myInfoInst.Interval_PopulationVariance) + "\t");
			stBuf.append(formatter.format(myInfoInst.Interval_SumSquare) + "\t");
			stBuf.append(formatter.format(myInfoInst.Interval_Variance) + "\t");

			//stBuf.append(myInfoInst.Scale + "\t");
			stBuf.append(myInfoInst.STAWindow + "\t");
			stBuf.append(myInfoInst.LTAWindow + "\t");
			stBuf.append(myInfoInst.FREQ_IntervalTime + "\t");

			stBuf.append(myInfoInst.BigEventsCount + "\t");
			stBuf.append(myInfoInst.AllEventsCount + "\t");
			stBuf.append(myInfoInst.EventsQuality + "\t");
			stBuf.append(myInfoInst.SavedTweets + "\t");
			stBuf.append(myInfoInst.SavedTweetsRatio + "\n");

			out.write(stBuf.toString().trim() + "\n");
		}
		out.close();
	}

	/*************************************************************************
	 * File Filter
	 *************************************************************************/
	public static void log(String strLine)
	{
		try
		{
			out_Log.write(strLine + "\n");
		}
		catch (IOException e)
		{
			CommFunctForED.logAndPrint("Logging Error :" + strLine);
		}
		return;
	}

	/*************************************************************************
	 * File Filter
	 *************************************************************************/
	public static void logAndPrint(String strLine)
	{
		try
		{
			out_Log.write(strLine + "\n");
			System.out.println(strLine);
		}
		catch (IOException e)
		{
			System.out.println("Logging Error :" + strLine);
		}
		return;
	}

	/*************************************************************************
	 * File Filter
	 *************************************************************************/
	public static void logWithoutNewLine(String strLine)
	{
		try
		{
			out_Log.write(strLine);
		}
		catch (IOException e)
		{
			System.out.println("Logging Error :" + strLine);
		}
		return;
	}

	/*************************************************************************
	 * File Filter
	 *************************************************************************/
	public static void logAndFlush(String strLine)
	{
		try
		{
			out_Log.write(strLine + "\n");
			out_Log.flush();
		}
		catch (IOException e)
		{
			System.out.println("Logging Error :" + strLine);
		}
		return;
	}

	/*************************************************************************
	 * File Filter
	 *************************************************************************/
	public static void InitializeFiles()
	{
		try
		{
			String delDir = HashDefinesForED.OutputFolder;
			System.out.println("delDir=" + delDir);
			System.out.println("Going to Empty Directory " + delDir);
			EmptyDirectoryCompletely(new File(delDir));

			System.out.println("Creating Folders ");
			new File(HashDefinesForED.OutputFolder).mkdirs();
			new File(HashDefinesForED.EventsFolder).mkdir();
			new File(HashDefinesForED.EventsFolder_Events).mkdir();
			new File(HashDefinesForED.FreqDataFolder).mkdir();
			new File(HashDefinesForED.EvenstsCelebDataFolder).mkdir();
			Thread.sleep(1000);

			String logFileName = HashDefinesForED.EventsFolder + HashDefinesForED.FName_Logs;
			out_Log = new BufferedWriter(new FileWriter(logFileName, false));
			
			/*String eventPhaseFileName = HashDefinesForED.EventsFolder + HashDefinesForED.FName_EvPhases;
			BufferedWriter out_ev = new BufferedWriter(new FileWriter(eventPhaseFileName, false));
			out_ev.close();*/

			BufferedWriter out_ev = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_Celebrity, false));
			out_ev.write("#ToicName" + "\t");
			out_ev.write("Index" + "\t");

			out_ev.write("StartMsec" + "\t");
			out_ev.write("StartTime" + "\t");

			out_ev.write("EndMsec" + "\t");
			out_ev.write("EndTime" + "\t");

			out_ev.write("Total" + "\t");
			out_ev.write("Celeb" + "\t");
			out_ev.write("NonCeleb" + "\t");

			out_ev.write("NonCelebOrig" + "\t");
			out_ev.write("NonCelebFwdTot" + "\t");

			out_ev.write("NonCelebFwdofCelebInTotal" + "\t");
			out_ev.write("NonCelebFwdofNonCelebInTotal" + "\t");

			out_ev.write("NonCelebFwdCelebInFwd" + "\t");
			out_ev.write("NonCelebFwdNonCelebInFwd" + "\t");
			out_ev.write("GrowthEvent" + "\t");
			out_ev.write("DecayEvent" + "\t");
			out_ev.write("PhasesTime" + "\t");
			out_ev.write("FREQ_IntervalTime");
			out_ev.write("\n");

			out_ev.close();

			out_ev = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_Engagement_C, false));
			out_ev.write("#ToicName" + "\t");
			out_ev.write("EventIndex" + "\t");
			out_ev.write("TweetsInEvent" + "\t");
			out_ev.write("TweetsByCeleb" + "\t");
			out_ev.write("CelebrityCount" + "\t");
			out_ev.write("AvgTweByCeleb");
			out_ev.write("AvgTimeByCeleb\n");
			out_ev.close();

			out_ev = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_Engagement_NC, false));
			out_ev.write("#ToicName" + "\t");
			out_ev.write("EventIndex" + "\t");
			out_ev.write("TweetsInEvent" + "\t");
			out_ev.write("TweetsByNonCeleb" + "\t");
			out_ev.write("NonCelebrityCount" + "\t");
			out_ev.write("AvgTweByNonCeleb");
			out_ev.write("AvgTimeByNonCeleb\n");
			out_ev.close();

			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date firstDay = df.parse(HashDefinesForED.gStrFirstDay);
			GetAlpha.firstDayTime = firstDay.getTime();

			out_ev = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_ClassifiedEvents));
			out_ev.write("#Topic\tGrowthStartTime\tPeakStartTime\tDecayStartTime\tEventEndTime\tGrowthRate\tDecayRate\tThreshold\n");
			out_ev.close();

			out_ev = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_SubsequentInvolvementWithinEvent));
			StringBuffer sBuf = new StringBuffer();
			sBuf.append("TopicName" + "\t");
			sBuf.append("EventId" + "\t");//eventid
			sBuf.append("C_PeopleOfGrowthPhaseSize" + "\t");//0
			sBuf.append("C_PeopleOfPeakPhaseSize_Old" + "\t");//1
			sBuf.append("C_PeopleOfPeakPhaseSize_New" + "\t"); //2
			sBuf.append("C_PeopleOfDecayPhaseSize_Old" + "\t"); //3
			sBuf.append("C_PeopleOfDecayPhaseSize_New" + "\t"); //4
			sBuf.append("C_TweetsOfGrowthPhaseSize" + "\t"); //5
			sBuf.append("C_TweetsOfPeakPhaseSize_Old" + "\t"); //6
			sBuf.append("C_TweetsOfPeakPhaseSize_New" + "\t"); //7
			sBuf.append("C_TweetsOfDecayPhaseSize_Old" + "\t"); //8
			sBuf.append("C_TweetsOfDecayPhaseSize_New" + "\t"); //9

			sBuf.append("NC_PeopleOfGrowthPhaseSize" + "\t"); //10
			sBuf.append("NC_PeopleOfPeakPhaseSize_Old" + "\t"); //11
			sBuf.append("NC_PeopleOfPeakPhaseSize_New" + "\t"); //12
			sBuf.append("NC_PeopleOfDecayPhaseSize_Old" + "\t"); //13
			sBuf.append("NC_PeopleOfDecayPhaseSize_New" + "\t"); //14
			sBuf.append("NC_TweetsOfGrowthPhaseSize" + "\t"); //15
			sBuf.append("NC_TweetsOfPeakPhaseSize_Old" + "\t"); //16
			sBuf.append("NC_TweetsOfPeakPhaseSize_New" + "\t"); //17
			sBuf.append("NC_TweetsOfDecayPhaseSize_Old" + "\t"); //18
			sBuf.append("NC_TweetsOfDecayPhaseSize_New" + "\t"); //19
			out_ev.write(sBuf.toString() + "\n");
			out_ev.close();

			out_ev = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_SubsequentInvolvementAcrossEvent));
			out_ev.write("#Topic\tEventsCount\tCelebrityCount\t80%\t90%\t100%\tNonCelebrityCount\t80%\t90%\t100%\n");
			out_ev.close();

			/*out_ev = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_participationIndiv));
			out_ev.write("#Topic\tevent\ttotalParticipation\tpInGrowth\tpInPeak\tpInDecay\n");
			out_ev.close();
			*/
			out_ev = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_participationAll));
			out_ev.write("#Phase\tCelebPart\tNonCelebPart\tAllPart\n");
			out_ev.close();

			out_ev = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_Peak2Avg));
			out_ev.write("#TopicName" + "\t" + "threshold" + "\t" + "HighestPeakAmplitue" + "\t" + "Peak2Avg" + "\t" + "gRate" + "\t" + "dRate"
					+ "(PeakEventThreshold * HighestPeakAmplitue)" + "\t" + "MeanPlusSTD" + "\n");
			out_ev.close();

			/*out_ev = new BufferedWriter(new FileWriter(HashDefinesForED.EvenstsCelebDataFolder + GetAlpha.myInfoInst.TopicName + ".txt"));
			out_ev.close();
			
			out_ev = new BufferedWriter(new FileWriter(HashDefinesForED.EvenstsCelebDataFolder + "Celebrity_" + GetAlpha.myInfoInst.TopicName + ".txt"));
			out_ev.close();*/

			/*for (int i = 0; i < GetAlpha.proportion.length; i++)
			{
				for (int j = 0; j < GetAlpha.proportion.length; j++)
				{
					GetAlpha.proportion[i][j] = 0;
				}
			}*/
			GetAlpha.EventsCount_final.clear();
			GetAlpha.EventsCount_Temp.clear();
			GetAlpha.c_samples = 0;
			GetAlpha.n_samples = 0;
			GetAlpha.a_samples = 0;
			for (int i = 0; i < GetAlpha.particiIntervals; i++)
			{
				GetAlpha.celebritiesParticipation[i] = 0;
				GetAlpha.nonCelebritiesParticipation[i] = 0;
				GetAlpha.allParticipation[i] = 0;
			}

			//GetAlpha.growthStats.clear();
			//GetAlpha.decayStats.clear();

			GetAlpha.Time_Growth.clear();
			GetAlpha.Time_Peak.clear();
			GetAlpha.Time_Decay.clear();

			GetAlpha.Tweets_Growth.clear();
			GetAlpha.Tweets_Peak.clear();
			GetAlpha.Tweets_Decay.clear();
			
			GetAlpha.Tweets_Growth_Smoothed.clear();
			GetAlpha.Tweets_Peak_Smoothed.clear();
			GetAlpha.Tweets_Decay_Smoothed.clear();
		}
		catch (Exception e)
		{
			CommFunctForED.logAndPrint("Exception !!!!!!!!!!!!!  in CommFunctForED.InitializeFiles()");
			e.printStackTrace();
		}
	}
	
	
	public static void InitializeFiles_Partial()
	{
		try
		{
			String logFileName = HashDefinesForED.EventsFolder + HashDefinesForED.FName_Logs;
			out_Log = new BufferedWriter(new FileWriter(logFileName, true));
						

			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date firstDay = df.parse(HashDefinesForED.gStrFirstDay);
			GetAlpha.firstDayTime = firstDay.getTime();

			
		}
		catch (Exception e)
		{
			CommFunctForED.logAndPrint("Exception !!!!!!!!!!!!!  in CommFunctForED.InitializeFiles()");
			e.printStackTrace();
		}
	}
	
	

	/*************************************************************************
	 * Recursively delete files from a folder
	 * @throws FileNotFoundException 
	 *************************************************************************/
	public static void EmptyDirectory(File path) throws FileNotFoundException
	{
		File[] files = path.listFiles();
		if (files != null)
		{ //some JVMs return null for empty dirs
			for (File f : files)
			{
				if (f.isDirectory())
				{
					EmptyDirectory(f);
				}
				else
				{
					f.delete();
				}
			}
		}
		//path.delete();
	}

	/*************************************************************************
	 * Recursively delete files from a folder
	 * @throws FileNotFoundException 
	 *************************************************************************/
	public static void EmptyDirectoryCompletely(File path) throws FileNotFoundException
	{
		File[] files = path.listFiles();
		if (files != null)
		{
			//some JVMs return null for empty dirs
			for (File f : files)
			{
				if (f.isDirectory())
				{
					EmptyDirectory(f);
				}
				else
				{
					f.delete();
				}
			}
		}
		path.delete();
	}

	/*************************************************************************
	 * Generate the gnuplot files which can be executed using gnuplot.exe
	 *************************************************************************/
	public static void Plot_Temporal(MyInfoClass myInfoInst)
	{
		CommFunctForED.logAndPrint("PlotValues()");
		try
		{
			DrawTemporalChart myChart = new DrawTemporalChart(myInfoInst);
			JFreeChart jc = myChart.DrawYourChart();
			myChart.pack();
			DrawTemporalChart.writeTemporalGraph(jc, myInfoInst.TopicName);

			//Display Chart
			if (GetAlpha.display_Topic)
			{
				RefineryUtilities.centerFrameOnScreen(myChart);
				myChart.setVisible(true);

				try
				{
					CommFunctForED.logAndPrint("sleeping");
					try
					{
						while(true)
						{
							Thread.sleep(GetAlpha.ThreadSleepTime * 1000);
							CommFunctForED.logAndPrint("CommFunctForED.Plot_Temporal()");
							if (myChart.isEnabled())
								break;

						}
					}
					catch (NumberFormatException e)
					{
						return;
					}
					CommFunctForED.logAndPrint("not sleeping");
				}
				catch (Exception e)
				{
					myChart.dispose();
				}
			}
			myChart.dispose();
		}
		catch (Exception e)
		{
			CommFunctForED.logAndPrint("Exception Outer");
			e.printStackTrace();
		}
	}

	/*************************************************************************
	 * Generate the gnuplot files which can be executed using gnuplot.exe
	 *************************************************************************/

	public static void Plot_Events(String st)
	{
		try
		{
			PlotEventsChart myChart = new PlotEventsChart(st);
			JFreeChart jc = myChart.DrawYourChart();
			myChart.pack();
			PlotEventsChart.writeEventGraph(jc);

			//Display Chart
			if (GetAlpha.display_Event)
			{
				RefineryUtilities.centerFrameOnScreen(myChart);
				myChart.setVisible(true);

				Thread.sleep(GetAlpha.ThreadSleepTime * 1000);
			}
			myChart.dispose();
		}
		catch (Exception e)
		{
			CommFunctForED.logAndPrint("Exception in Plot_Events");
			e.printStackTrace();
		}
	}

	/*************************************************************************
	 * Get Percentile Average
	 * at least one should be null
	 *************************************************************************/
	public static int GetPercentileAverage(int percentile, DescriptiveStatistics ds, boolean isAscendingOrder)
	{
		int elementsCount = (int) ds.getN();
		int NumberofElements = (int) (ds.getN() * percentile / 100);

		double sum = 0.0;
		double[] values = ds.getSortedValues();
		if (isAscendingOrder)
		{
			for (int i = 0; i < NumberofElements; i++)
			{
				sum += values[i];
			}
		}
		else
		{
			for (int i = 0; i < NumberofElements; i++)
			{
				sum += values[elementsCount - 1 - i];
			}
		}
		return (int) (sum / NumberofElements);
	}

	/* ***********************************************************************
	 * WriteFreqData
	 *************************************************************************/

	public static void WriteFreqData() throws IOException
	{
		double MaxActFreq = 0d;
		double MaxSmoothFreq = 0d;
		NumberFormat formatter = new DecimalFormat("#00.0000");
		BufferedWriter out_ev = new BufferedWriter(new FileWriter(HashDefinesForED.FreqDataFolder + GetAlpha.myInfoInst.TopicName + ".txt"));
		for (int i = GetAlpha.myInfoInst.StartInterval; i <= GetAlpha.myInfoInst.EndInterval; i++)
		{
			String mdate = new Date(GetAlpha.firstDayTime + i * GetAlpha.myInfoInst.FREQ_IntervalTime * 1000l).toString();

			out_ev.write(i + "\t" + GetAlpha.FreqMap[i] + "\t" + formatter.format(GetAlpha.STAFreqMap[i]) + "\t" + formatter.format(GetAlpha.LTAFreqMap[i]) + "\t"
					+ formatter.format(GetAlpha.STAFreqMap[i] / GetAlpha.LTAFreqMap[i]) + "\t" + mdate + "\n");

			if (GetAlpha.FreqMap[i] > MaxActFreq)
				MaxActFreq = GetAlpha.FreqMap[i];
			if (GetAlpha.STAFreqMap[i] > MaxSmoothFreq)
				MaxSmoothFreq = GetAlpha.STAFreqMap[i];
		}
		out_ev.close();

		GetAlpha.MaxActFreq = MaxActFreq;
		GetAlpha.MaxSmoothFreq = MaxSmoothFreq;
		CommFunctForED.logAndPrint("MaxActFreq=" + MaxActFreq + " MaxSmoothFreq=" + formatter.format(MaxSmoothFreq) + " Threshold_EventPeak * MaxActFreq = "
				+ formatter.format(GetAlpha.Threshold_EventPeak * MaxActFreq) + "  Threshold_SmoothedEventPeak * MaxSmoothFreq="
				+ formatter.format(GetAlpha.Threshold_SmoothedEventPeak * MaxSmoothFreq));

	}
}
