/*******************
Author  : Amit Ruhela
Purpose : 
Finds the key characteristics of topics used to determine events.
Smooth out the time series using Median filter
Find events using our designed algorithms
Merge events if they happen closely in time
Discard insignificant events which are just noises
Find various phases of events : Growth, Peak, Decay
Find the Growth rate of events
Find the participation of Celebrities and Non-Celebrities in the events
Find Average Length of Growth, Peak and Decay Phases :  StartApp()
Find number of tweets posted by distinguished in the events : : 
*******************/
package Elsevier;

/* 
 * Implemented by :	Amit Ruhela on July , 2013
 * This class prints the IAT only
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public final class GetAlpha
{

	/**************************** Configurable Variables *******************************/

	// Window related parameter
	static final int Min_FREQ_IntervalTime = 1 * 60; // In minutes
	static final int Max_FREQ_IntervalTime = 10 * 60; // In minutes

	// Smoothing parameters
	static final double Alpha_LTA = 0.01;
	static final int STAWindowSize = 9;

	// Events Detection
	static final int warmup_Scale = 2;
	static int PreEventSlots = 1;
	static int PostEventSlots = (int) ((1 / Alpha_LTA) - STAWindowSize) / 2;
	static double EventStart_TH = 1.50;
	static double EventEnd_Th = 0.5;
	static long preEventTime = 2 * 3600 * 1000l;// 2Hr
	// static int ReArmTime = 2 * 3600;

	// Merging of events
	static final long EventsGap_Threshold = 8 * 3600;
	static final long EventsGap_Threshold_2 = 2 * 24 * 3600;

	// Phases of events
	static int particiIntervals = 3;
	static final double PeakEventThreshold = 0.60;
	static final double PeakThrSTDCount = 1.2;
	static final int NightTime_Start = 0;
	static final int NightTime_End = 8;

	// Filtering events related
	// static final int Crossing_ThresholdsPerHour = 1;
	static final int MinTweets_BigEvents = 300;
	static final int MinTweets_SmallEvents = 100;
	static final double MinRate_BigEvents = 20;// per Hour
	static final double MinRate_SmallEvents = 20; // per Hour
	static double MinRate_SmoothedEventData = 20;
	static final int GoodEvent_Threshold = 3;
	static double Threshold_EventPeak = 0.25;
	static double Threshold_SmoothedEventPeak = 0.5;
	static final double Threshold_EventTime = 0.5; // Hrs

	// Display Related
	static final boolean display_Topic = false;
	// static final boolean display_Topic = true;

	static final boolean display_Event = false;
	// static final boolean display_Event = true;

	static boolean flag_Test = false;

	/**************************** Static Variables *******************************/
	public static int CelebrityThreshold = 0;
	public static double CelebrityPercentile = 0;
	public static final int ThreadSleepTime = 5000;// seconds

	public static final String RE_Sep_file = "\\-<\\+>\\-";// File
	public static final String RE_Sep_group = "\\t";// Group
	public static final String RE_Sep_record = ";";// Record

	// Tweet
	static final int Index_Tweet_ID = 0;
	static final int Index_Tweet_Time = 1;
	static final int Index_Tweet_isReTweet = 2;
	static final int Index_Tweet_RetweetsCount = 5;
	static final int Index_Tweet_LikesCount = 7;
	static final int Index_Tweet_User = 10;
	static final int Index_Tweet_InReplyToStatusId = 11;
	static final int Index_HashTag = 14;

	// User
	static final int Index_User_ID = 0;
	static final int Index_User_Followers = 2;
	// static final int Index_User_isVerified = 10;

	/**************************** Temporary Variables *******************************/
	static String gTopic = "";
	static String MidFix = null;
	static MyInfoClass myInfoInst = null;
	static StringBuffer eventsBuf = new StringBuffer();

	static long firstDayTime;
	static double MaxActFreq = 0d;
	static double MaxSmoothFreq = 0d;
	static int GlobalHighestPeak = 0;

	static int[] FreqMap = null;
	static double[] STAFreqMap = null;
	static double[] LTAFreqMap = null;

	static Vector<Long> TweetsTimeVector = new Vector<>();// In seconds
	static Vector<String> FinalEvents_Temp = new Vector<>(); // Format StartEventTime_EndEventTime In seconds
	static Vector<String> MergedEvents = new Vector<>(); // Format : StartEventTime_EndEventTime In seconds
	static Vector<String> FilteredEvents = new Vector<>(); // Format : StartEventTime_EndEventTime In seconds
	static Vector<String> ClassifiedMergedEvents = new Vector<>(); // Format : StartEventTime_EndEventTime In seconds ; growth/ and decay found

	// TreeMap<String, Integer> EventsMap = null;
	// SortedSet<Map.Entry<String, Integer>> mySortedEventsSet = null;

	static Vector<Double> GrowthVector = null;
	static Vector<Double> DecayVector = null;
	static Vector<Double> ThresholdVector = null;

	// static DescriptiveStatistics growthStats = new DescriptiveStatistics();
	// static DescriptiveStatistics decayStats = new DescriptiveStatistics();

	static DescriptiveStatistics Time_Growth = new DescriptiveStatistics();// Per Phase
	static DescriptiveStatistics Time_Peak = new DescriptiveStatistics();// Per Phase
	static DescriptiveStatistics Time_Decay = new DescriptiveStatistics();// Per Phase

	static DescriptiveStatistics Tweets_Growth = new DescriptiveStatistics();// Per Phase
	static DescriptiveStatistics Tweets_Peak = new DescriptiveStatistics();// Per Phase
	static DescriptiveStatistics Tweets_Decay = new DescriptiveStatistics();// Per Phase

	static DescriptiveStatistics Tweets_Growth_Smoothed = new DescriptiveStatistics();// Per Phase
	static DescriptiveStatistics Tweets_Peak_Smoothed = new DescriptiveStatistics();// Per Phase
	static DescriptiveStatistics Tweets_Decay_Smoothed = new DescriptiveStatistics();// Per Phase

	static int c_samples = 0;
	static int n_samples = 0;
	static int a_samples = 0;

	static double[] celebritiesParticipation = new double[particiIntervals];
	static double[] nonCelebritiesParticipation = new double[particiIntervals];
	static double[] allParticipation = new double[particiIntervals];

	static TreeMap<Long, String> TM_UserName = new TreeMap<>();

	static List<List<Long>> GrowthSets_C = null;
	static List<List<Long>> PeakSets_C = null;
	static List<List<Long>> DecaySets_C = null;

	static List<List<Long>> GrowthSets_NC = null;
	static List<List<Long>> PeakSets_NC = null;
	static List<List<Long>> DecaySets_NC = null;

	static double TimeScale = 0;
	static DescriptiveStatistics EventsCount_final = new DescriptiveStatistics();
	static DescriptiveStatistics EventsCount_Temp = new DescriptiveStatistics();

	// static TreeMap<String, String> TM_Engagement = new TreeMap<>();
	// static Set<Long> Set_UsersPoliticians = new HashSet<>();

	// static int[][] proportion = new int[4][4];

	static String[] DiscardedTopics_List = {};

	/*********************** Temporary variable ends here ************************************/

	/*************************************************************************
	 * Constructor
	 *************************************************************************/
	public GetAlpha(int cthresh, double mETT, double mEDT, double mTimeScale, String UserType)
	{
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		EventStart_TH = mETT;
		EventEnd_Th = mEDT;
		TimeScale = mTimeScale;
		if (UserType.equals("B"))
		{
			CelebrityThreshold = (int) HashDefinesForED.CelebThr_B[cthresh][1];
			CelebrityPercentile = HashDefinesForED.CelebThr_B[cthresh][0];
		}
		if (UserType.equals("S"))
		{
			CelebrityThreshold = (int) HashDefinesForED.CelebThr_S[cthresh][1];
			CelebrityPercentile = HashDefinesForED.CelebThr_S[cthresh][0];
		}
		if (UserType.equals("P"))
		{
			CelebrityThreshold = (int) HashDefinesForED.CelebThr_P[cthresh][1];
			CelebrityPercentile = HashDefinesForED.CelebThr_P[cthresh][0];
		}
		System.out.println("CelebrityThreshold=" + CelebrityThreshold + "\t" + "CelebrityPercentile=" + CelebrityPercentile);
		HashDefinesForED.SetUserType(UserType, CelebrityPercentile);
		GlobalHighestPeak = 0;

		/*
		 * String logFileName = HashDefinesForED.EventsFolder + HashDefinesForED.FName_Logs; try { CommFunctForED.out_Log = new BufferedWriter(new FileWriter(logFileName, false)); } catch (Exception
		 * e) { //DO nothing }
		 */
	}

	/*************************************************************************
	 * Main Function
	 * 
	 * @throws InterruptedException
	 *************************************************************************/
	public static void main(String args[]) throws IOException, InterruptedException
	{
		String myClassName = Thread.currentThread().getStackTrace()[1].getClassName();
		long startExecution = new Long(System.currentTimeMillis()).longValue();
		System.out.println(myClassName + " Started at " + new Date(startExecution).toString());

		GetAlpha gAlpha = new GetAlpha(1, 1.5, 0.5, 1, "S");
		gAlpha.StartApp();

		long endExecution = new Long(System.currentTimeMillis()).longValue();
		long difference = (endExecution - startExecution) / 1000;
		CommFunctForED.logAndPrint(myClassName + " finished at " + new Date(endExecution).toString() + " The program has taken " + (difference) + " seconds(" + (difference / 60) + " minutes)");
	}

	/*************************************************************************
	 * StartApp
	 * 
	 * @throws InterruptedException
	 *************************************************************************/
	public String StartApp() throws IOException, InterruptedException
	{
		CommFunctForED.InitializeFiles();
		// ReadPoliticians_2();

		CommFunctForED.logAndPrint("StartApp : DataFolder = " + HashDefinesForED.DataFolder);
		CommFunctForED.logAndPrint("StartApp : EventsFolder = " + HashDefinesForED.EventsFolder);
		CommFunctForED.logAndPrint("StartApp : OutputFolder = " + HashDefinesForED.OutputFolder);
		CommFunctForED.logAndPrint("StartApp : Celebrity Threshold = " + CelebrityThreshold + " Percentile=" + CelebrityPercentile);

		CommFunctForED.PrintResults(true, myInfoInst);
		CommFunctForED.LogEvents(true, eventsBuf.toString(), "");

		Vector<File> mFileVector = new Vector<>();
		File[] mFile = new File(HashDefinesForED.DataFolder).listFiles();
		for (int j = 0; j < mFile.length; j++)
		{
			mFileVector.add(mFile[j]);
		}
		CommFunctForED.logAndPrint("StartApp : Files count = " + mFile.length);

		// mFileVector.add(new File(HashDefinesForED.DataFolder + "t20wc2014.txt"));
		// mFileVector.add(new File(HashDefinesForED.DataFolder +
		// "aapinamethi.txt"));
		// mFileVector.add(new File(HashDefinesForED.DataFolder +
		// "isupportasarambapu.txt"));

		int First = 0;
		// int Last = First + 5;//mFileVector.size()
		int Last = mFileVector.size();

		boolean doProcess = true;
		for (int j = First; j < Last; j++)
		{
			CommFunctForED.out_Log.flush();
			CommFunctForED.logAndPrint("\n====================" + j + "/" + Last + " " + mFileVector.get(j).getName() + " " + HashDefinesForED.GGUserType + "====================================");
			doProcess = ReadNextTopic(mFileVector.get(j));
			if (doProcess == false)
			{
				continue;
			}
			// Thread.sleep(500);
			ProcessTopic(mFileVector.get(j));
		}

		if (!flag_Test)
		{
			WriteConsolidatedParticipation();
			GetCelebritiesParticipation();
			// SegregateTopicClasses_New();
			PostCalculations();

			// MyEventsPlotter();
		}

		CommFunctForED.out_Log.flush();
		CommFunctForED.out_Log.close();
		return EventsCount_Temp.getMean() + "\t" + EventsCount_final.getMean() + "\t" + EventsCount_Temp.getPercentile(50) + "\t" + EventsCount_final.getPercentile(50) + "\t" + TimeScale;
	}

	/*************************************************************************
	 * ReadNextTopic
	 *************************************************************************/
	@SuppressWarnings("boxing")
	private static boolean ReadNextTopic(File mFile) throws NumberFormatException, IOException
	{
		TweetsTimeVector.clear();
		GrowthVector = null;
		DecayVector = null;
		ThresholdVector = null;

		String st = "";
		Long Time;
		long StartTime = Long.MAX_VALUE;
		long EndTime = Long.MIN_VALUE;

		gTopic = mFile.getName().substring(0, mFile.getName().length() - 4);
		CommFunctForED.logAndPrint("ReadNextTopic : Topic=" + gTopic);

		for (int i = 0; i < DiscardedTopics_List.length; i++)
		{
			if (DiscardedTopics_List[i].trim().matches(gTopic))
			{
				CommFunctForED.logAndPrint("  Ignorning topic = " + gTopic);
				return false;
			}
		}

		// Compute StartTime and EndTime
		BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(mFile.getAbsolutePath())));
		while ((st = br_i.readLine()) != null)
		{
			if (st.length() < 1)
				continue;
			String Tweet[] = st.split(RE_Sep_file, 2);
			String[] elements = Tweet[0].split(RE_Sep_group);
			Time = Long.parseLong(elements[Index_Tweet_Time]);
			TweetsTimeVector.add((Time - GetAlpha.firstDayTime) / 1000);
			if (Time < StartTime)
				StartTime = Time;
			if (Time > EndTime)
				EndTime = Time;
		}
		br_i.close();
		CommFunctForED.logAndPrint("  StartTime=" + StartTime + "\t" + "EndTime=" + EndTime);
		Collections.sort(TweetsTimeVector);

		CommFunctForED.logAndPrint("  Size of TweetsTimeVector=" + TweetsTimeVector.size());
		return true;
	}

	/*************************************************************************
	 * 
	 * ProcessTopic()
	 * 
	 * @throws IOException
	 *************************************************************************/
	@SuppressWarnings("boxing")
	private void ProcessTopic(File mFile) throws IOException
	{
		CommFunctForED.logAndPrint("ProcessTopic");
		// GetAlpha dEvents = new GetAlpha(EventStart_TH, EventEnd_Th,
		// TimeScale, HashDefinesForED.GGUserType);
		InitializeVariables();
		GetIATCharacteristics();

		int lTime;
		int FREQ_IntervalTime = (int) (9 * myInfoInst.IAT_Median);// seconds
		// int FREQ_IntervalTime = (int) (20 *
		// myInfoInst.IAT_NintyPercentileAvg);// seconds
		CommFunctForED.logAndPrint("  Before : FREQ_IntervalTime=" + FREQ_IntervalTime);
		if (FREQ_IntervalTime < Min_FREQ_IntervalTime)
			FREQ_IntervalTime = Min_FREQ_IntervalTime;

		if (FREQ_IntervalTime > Max_FREQ_IntervalTime)
			FREQ_IntervalTime = Max_FREQ_IntervalTime;
		CommFunctForED.logAndPrint("  After : FREQ_IntervalTime=" + FREQ_IntervalTime);

		// int FREQ_IntervalTime = (int) (myInfoInst.IAT_Mean * TimeScale);

		myInfoInst.STAWindow = STAWindowSize;
		myInfoInst.FREQ_IntervalTime = FREQ_IntervalTime;
		myInfoInst.StartInterval = (int) (TweetsTimeVector.get(0) / myInfoInst.FREQ_IntervalTime) - 1;
		myInfoInst.EndInterval = (int) (TweetsTimeVector.get(TweetsTimeVector.size() - 1) / myInfoInst.FREQ_IntervalTime) + 1;

		MidFix = "_LTAW" + myInfoInst.LTAWindow + "_STAWw" + myInfoInst.STAWindow;

		FreqMap = new int[HashDefinesForED.NoOfDays * 24 * 3600 / myInfoInst.FREQ_IntervalTime + 3];
		STAFreqMap = new double[FreqMap.length];
		LTAFreqMap = new double[FreqMap.length];

		CommFunctForED.logAndPrint("  FreqMap.length=" + FreqMap.length);
		for (int k = 0; k < TweetsTimeVector.size(); k++)
		{
			lTime = (int) ((TweetsTimeVector.get(k)) / myInfoInst.FREQ_IntervalTime);
			FreqMap[lTime] += 1;
		}
		TweetsTimeVector.clear();

		CommFunctForED.ComputeTweetsFrequencyPerTimeInterval();
		Smooth();
		CommFunctForED.WriteFreqData();
		
		if(gTopic.equals("asiacup2014"))
		{
			System.out.println("GetAlpha.ProcessTopic()");
		}

		FindEvents();
		MergeEvents();
		DiscardBadEvents();

		if (!flag_Test)
		{
			GetGrowthOfEvents();
			FindCelebritiesEngagement(mFile);
			CleanDataset();

			CommFunctForED.LogEvents(false, eventsBuf.toString(), "");
			CommFunctForED.Plot_Temporal(myInfoInst);
			CommFunctForED.PrintResults(false, myInfoInst);

			for (int i = 0; i < ClassifiedMergedEvents.size(); i++)
			{
				GrowthSets_C.add(new LinkedList<Long>());
				PeakSets_C.add(new LinkedList<Long>());
				DecaySets_C.add(new LinkedList<Long>());

				GrowthSets_NC.add(new LinkedList<Long>());
				PeakSets_NC.add(new LinkedList<Long>());
				DecaySets_NC.add(new LinkedList<Long>());
			}
			for (int i = 0; i < ClassifiedMergedEvents.size(); i++)
			{
				FindCelebritiesParticipation(mFile, i, ClassifiedMergedEvents.size());
			}
			ProcessEventsOverlapWithinEvents();
			// ProcessEventsOverlapAcrossEvents();

		}
		else
		{
			// double Hrs = (myInfoInst.EndTime - myInfoInst.StartTime) / 3600;
			// EventsCount_Temp.addValue(1.0 * FinalEvents_Temp.size());
			// EventsCount_final.addValue(1.0 * FilteredEvents.size());
			// EventsCount_Temp.addValue(1.0 * FinalEvents_Temp.size() / Hrs);
			// EventsCount_final.addValue(1.0 * FilteredEvents.size() / Hrs);
		}

	}

	/*************************************************************************
	 * Initialize Variables
	 *************************************************************************/
	private void CleanDataset()
	{
		FreqMap = null;
		STAFreqMap = null;
		LTAFreqMap = null;
	}

	/*************************************************************************
	 * 
	 * Initialize Variables
	 *************************************************************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void InitializeVariables() throws NumberFormatException
	{
		myInfoInst = new MyInfoClass();
		eventsBuf.setLength(0);

		myInfoInst.StartInterval = 0;
		myInfoInst.EndInterval = 0;
		myInfoInst.Interval_Mean = 0;
		myInfoInst.Interval_Median = 0;
		myInfoInst.Interval_Skewness = 0;
		myInfoInst.Interval_STD = 0;
		myInfoInst.Interval_NintyPercentileAvg = 0;
		myInfoInst.Interval_Kurtosis = 0;
		myInfoInst.Interval_PopulationVariance = 0;
		myInfoInst.Interval_SumSquare = 0;
		myInfoInst.Interval_Variance = 0;

		// Smoothing
		// myInfoInst.Scale = 0;
		myInfoInst.STAWindow = 0;
		myInfoInst.LTAWindow = 0;

		// Events
		myInfoInst.BigEventsCount = 0;
		myInfoInst.AllEventsCount = 0;
		myInfoInst.EventsQuality = null;
		myInfoInst.SavedTweets = 0;
		myInfoInst.SavedTweetsRatio = 0;

		GrowthSets_C = null;
		PeakSets_C = null;
		DecaySets_C = null;

		GrowthSets_NC = null;
		PeakSets_NC = null;
		DecaySets_NC = null;

		GrowthSets_C = new ArrayList();
		PeakSets_C = new ArrayList();
		DecaySets_C = new ArrayList();

		GrowthSets_NC = new ArrayList();
		PeakSets_NC = new ArrayList();
		DecaySets_NC = new ArrayList();

		MaxActFreq = 0d;
		MaxSmoothFreq = 0d;

		// TM_Engagement.clear();
		FinalEvents_Temp.clear();
		MergedEvents.clear();
		FilteredEvents.clear();
		ClassifiedMergedEvents.clear();
	}

	/*************************************************************************
	 * ProcessTopic()
	 *************************************************************************/
	private static boolean GetIATCharacteristics()
	{
		CommFunctForED.logAndPrint("GetIATCharacteristics");
		DescriptiveStatistics ds = new DescriptiveStatistics();
		for (int k = 1; k < TweetsTimeVector.size(); k++)
		{
			ds.addValue(TweetsTimeVector.get(k).intValue() - TweetsTimeVector.get(k - 1).intValue());
		}

		myInfoInst.Tweets = TweetsTimeVector.size();
		myInfoInst.TopicName = gTopic;

		myInfoInst.IAT_Mean = ds.getMean();
		myInfoInst.IAT_Median = ds.getPercentile(50);
		myInfoInst.IAT_Skewness = ds.getSkewness();
		myInfoInst.IAT_STD = ds.getStandardDeviation();
		myInfoInst.IAT_NintyPercentileAvg = CommFunctForED.GetPercentileAverage(80, ds, true);
		myInfoInst.IAT_Kurtosis = ds.getKurtosis();
		myInfoInst.IAT_PopulationVariance = ds.getPopulationVariance();
		myInfoInst.IAT_SumSquare = ds.getSumsq();
		myInfoInst.IAT_Variance = ds.getVariance();

		myInfoInst.StartTime = TweetsTimeVector.get(0).longValue();
		myInfoInst.EndTime = TweetsTimeVector.get(TweetsTimeVector.size() - 1).longValue();

		CommFunctForED.logAndPrint("  Mean=" + myInfoInst.IAT_Mean + "  IAT_PercentileAvg(50)=" + CommFunctForED.GetPercentileAverage(50, ds, true) + "  IAT_PercentileAvg(80)="
				+ CommFunctForED.GetPercentileAverage(80, ds, true));
		CommFunctForED.logAndPrint("  IAT_Median=" + myInfoInst.IAT_Median + "  IAT_Skewness=" + myInfoInst.IAT_Skewness);
		CommFunctForED.logAndPrint("  IAT_STD=" + myInfoInst.IAT_STD + "  IAT_Variance=" + myInfoInst.IAT_Variance);

		return true;
	}

	/*************************************************************************
	 * Smooth() Weighted Mean Filter
	 *************************************************************************/
	private void Smooth()
	{
		DescriptiveStatistics stats = new DescriptiveStatistics();
		stats.setWindowSize(myInfoInst.STAWindow);

		// Find Weights
		double[] Weight = new double[myInfoInst.STAWindow];
		int half = (int) Math.ceil(1.0 * myInfoInst.STAWindow / 2);
		for (int j = 1; j < half; j++)
		{
			Weight[j - 1] = 1.0;
		}
		for (int j = (half); j <= myInfoInst.STAWindow; j++)
		{
			Weight[j - 1] = 1 - ((1.0 * j - 1.0 * myInfoInst.STAWindow / 2) / (1.0 + 1.0 * myInfoInst.STAWindow / 2));
		}

		/*
		 * for (int i = 0; i < Weight.length; i++) { CommFunctForED.logAndPrint(i + "=" + Weight[i]); }
		 */

		// Find STA and LTA values
		double prevLTA = Alpha_LTA * myInfoInst.Interval_NintyPercentileAvg;
		double sum = 0.0;
		double WeightSum = 0;
		// int index = 0;
		for (int i = GetAlpha.myInfoInst.StartInterval; i <= GetAlpha.myInfoInst.EndInterval; i++)
		{
			// Find STA
			sum = 0.0;
			WeightSum = 0;
			for (int count = 0; count < myInfoInst.STAWindow; count++)
			{
				if ((i - count) < GetAlpha.myInfoInst.StartInterval)
					break;
				sum += FreqMap[i - count] * Weight[count];
				WeightSum += Weight[count];
			}
			STAFreqMap[i] = 1.0 * sum / WeightSum;
			// CommFunctForED.logAndPrint("WeightSum="+WeightSum);

			// Find LTA
			// ==================================
			/*
			 * sum = 0.0; WeightSum = 0; for (int count = 0; count < myInfoInst.LTAWindow; count++) { if ((i - count) < GetAlpha.myInfoInst.StartInterval) break; //sum += LTAFreqMap[i - count]; sum +=
			 * FreqMap[i - count]; WeightSum++; } prevLTA = sum / WeightSum; LTAFreqMap[i] = Alpha_LTA * FreqMap[i] + (1 - Alpha_LTA) * prevLTA;
			 */
			// ==================================
			// LTA with infinite window
			/*
			 * if (index == 0) { LTAFreqMap[i] = FreqMap[i]; } else
			 */
			{
				LTAFreqMap[i] = Alpha_LTA * FreqMap[i] + (1 - Alpha_LTA) * prevLTA;
			}
			prevLTA = LTAFreqMap[i];
			// prevLTA = (FreqMap[i] + index * prevLTA) / (index + 1);
			// CommFunctForED.logAndPrint(FreqMap[i] + " " + LTAFreqMap[i] + " "
			// + prevLTA);
			// index++;
			// ==================================
		}
	}

	/*************************************************************************
	 * JoinGoodEvents Returns the good events Format : StartTimeOfEvent_EndTimeOfEvent In seconds
	 *************************************************************************/
	public void FindEvents()
	{
		CommFunctForED.logAndPrint("FindEvents()");
		myInfoInst.BigEventsCount = 0;
		myInfoInst.AllEventsCount = 0;

		double Ratio = 0.0;
		boolean monitoring = false;
		int eventStartTime = 0;
		int eventEndTime = 0;
		// int savedTweets = 0;

		int WarmUpSlots = 0;// warmup_Scale * STAWindowSize;
		double EventStartSTA = 0;
		// double EventStartLTA = 0;

		int prevEventEndTime = GetAlpha.myInfoInst.StartInterval;

		for (int i = WarmUpSlots + GetAlpha.myInfoInst.StartInterval; i <= GetAlpha.myInfoInst.EndInterval; i++)
		{
			// System.out.println(i);
			Ratio = 1.0 * STAFreqMap[i] / LTAFreqMap[i];
			/*
			 * Date temp_d1 = new Date(GetAlpha.firstDayTime + i * myInfoInst.FREQ_IntervalTime * 1000l); CommFunctForED.logAndPrint(formatter1.format(i) + "  Ratio=" + formatter.format(Ratio) +
			 * "  EventStart_TH=" + EventStart_TH + " EventEnd_Th=" + EventEnd_Th + "  " + temp_d1.toString());
			 */
			/*
			 * if (i == 2957) CommFunctForED.logAndPrint("GetAlpha.FindEvents()");
			 */

			if (Ratio > EventStart_TH)
			{
				if (monitoring == false)
				{
					eventStartTime = i;
					// EventStartLTA = LTAFreqMap[i];
					EventStartSTA = STAFreqMap[i];
					monitoring = true;
				}
			}
			else if (Ratio < EventEnd_Th)
			{
				if ((monitoring == true) && (STAFreqMap[i] < (1 * EventStartSTA)))
				// if ((monitoring == true) && (LTAFreqMap[i] < (1 *
				// EventStartLTA)))
				// if ((monitoring == true) && (violations >
				// ReArmIntervalsCount))
				// if (monitoring == true)
				{
					eventEndTime = i;
					monitoring = false;
					while (true)
					{
						if (eventStartTime <= prevEventEndTime)
							break;

						if (STAFreqMap[eventStartTime - 1] == 0)
							break;

						if (STAFreqMap[eventStartTime - 1] < 0.5)
							break;

						// if (FreqMap[eventStartTime - 1] < 1)
						// break;

						eventStartTime--;
					}

					while (true)
					{
						if (eventEndTime >= (GetAlpha.myInfoInst.EndInterval - 1))
							break;

						if (STAFreqMap[eventEndTime + 1] == 0)
							break;

						if (STAFreqMap[eventEndTime + 1] < 0.5)
							break;

						// if (FreqMap[eventEndTime + 1] < 1)
						// break;

						eventEndTime++;
					}

					AddEvent(eventStartTime - PreEventSlots, eventEndTime);
					prevEventEndTime = eventEndTime;

					// eventStartTime = 0;
					// eventEndTime = 0;
					i = (eventEndTime - 1);
				}
			}
			else
			{
				// do nothing
			}
		}
		System.out.println("GetAlpha.FindEvents() finished");

		// DOn't add unfinished event
		/*
		 * if (monitoring) { while(true) { if (eventStartTime <= prevEventEndTime) break;
		 * 
		 * if (STAFreqMap[eventStartTime - 1] == 0) break;
		 * 
		 * if (STAFreqMap[eventStartTime - 1] < 1) break;
		 * 
		 * eventStartTime--; } savedTweets += AddEvent(eventStartTime - PreEventSlots, GetAlpha.myInfoInst.EndInterval); prevEventEndTime = eventEndTime;
		 * 
		 * }
		 */
		// myInfoInst.SavedTweets = savedTweets;
	}

	/*************************************************************************
	 * JoinGoodEvents Returns the good events Format : StartTimeOfEvent_EndTimeOfEvent in seconds
	 *************************************************************************/
	public int AddEvent(int mEventStartInterval_in, int mEventEndInterval_in)
	{
		int mEventStartInterval = mEventStartInterval_in;
		int mEventEndInterval = mEventEndInterval_in;

		while (true)
		{
			if (FreqMap[mEventStartInterval] == 0)
			{
				mEventStartInterval++;
			}
			else
				break;
		}

		while (true)
		{
			if (FreqMap[mEventEndInterval] == 0)
			{
				mEventEndInterval--;
			}
			else
				break;
		}

		int mTweets = 0;
		DescriptiveStatistics ds = new DescriptiveStatistics();
		for (int i = mEventStartInterval; i < mEventEndInterval; i++)
		{
			mTweets += FreqMap[i];
			ds.addValue(STAFreqMap[i]);
		}
		double meanOfEvent = ds.getMean();
		double stdOfEvent = ds.getStandardDeviation();
		ds.clear();

		double AvgRate = 3600.0 * mTweets / ((mEventEndInterval - mEventStartInterval + 1) * (myInfoInst.FREQ_IntervalTime));// per hour
		double AvgRateInUnitTime = mTweets / ((mEventEndInterval - mEventStartInterval + 1));// per interval

		NumberFormat formatter = new DecimalFormat("#0.00");
		String msg1 = "  AddEvent : ";
		String msg2 = " Good:";
		String msg3 = "Error:";
		String msg5 = (AvgRate > MinRate_SmallEvents) ? " > " : " < ";
		String msg6 = " | " + IntervalToTxtDate(mEventStartInterval) + "  to " + IntervalToTxtDate(mEventEndInterval) + ", | Interval=(" + mEventStartInterval + "-" + mEventEndInterval + ")";
		String msg7 = " Tweets=" + mTweets + " ( AvgRate=" + formatter.format(AvgRate) + msg5 + MinRate_SmallEvents + "  )";

		// CommFunctForED.logAndPrint("AddEvent :mEventStartTime=" +
		// mEventStartTime + " mEventEndTime=" + mEventEndTime);
		// myInfoInst.AllEventsCount++;

		if (mTweets < MinTweets_SmallEvents)
		{
			// CommFunctForED.logAndPrint(msg1 + msg3 + msg6 + msg7);
			return 0;
		}

		if (AvgRate < MinRate_SmallEvents)
		{
			CommFunctForED.logAndPrint(msg1 + msg3 + msg6 + msg7);
			return 0;
		}

		if (AvgRateInUnitTime < (myInfoInst.Interval_Mean * 0.75))
		{
			CommFunctForED.logAndPrint(msg1 + msg3 + msg6 + msg7);
			return 0;
		}

		boolean GoodEvent = false;
		for (int i = mEventStartInterval; i < mEventEndInterval; i++)
		{
			if (FreqMap[i] > (GoodEvent_Threshold * myInfoInst.Interval_Mean))
			{
				GoodEvent = true;
				break;
			}
		}
		if (!GoodEvent)
		{
			CommFunctForED.logAndPrint(msg1 + msg3 + msg6 + msg7 + " Not a single value is above mean");
			return 0;
		}

		FinalEvents_Temp.add(mEventStartInterval + "_" + mEventEndInterval + "_" + mTweets + "_" + meanOfEvent + "_" + stdOfEvent);
		CommFunctForED.logAndPrint(msg1 + msg2 + msg6 + msg7);
		// CommFunctForED.logAndPrint(d1 + "_" + d2 + "_" + mTweets);
		// myInfoInst.BigEventsCount++;

		// CommFunctForED.WriteEvent(d1, d2, AvgRate, mTweets, myInfoInst,
		// eventsBuf, STAWindowSize);
		return mTweets;
	}


	/*************************************************************************
	 * MergeEvents
	 *************************************************************************/
	@SuppressWarnings("boxing")
	public void MergeEvents()
	{
		// Find Events in sorted order
		Set<String> tEvents = new TreeSet<>();
		int MaxTweets = 0;
		for (int i = 0; i < FinalEvents_Temp.size(); i++)
		{
			int sTime = Integer.parseInt(FinalEvents_Temp.get(i).split("_")[0]);
			int eTime = Integer.parseInt(FinalEvents_Temp.get(i).split("_")[1]);
			int lMax = 0;
			int pT = 0;
			for (int j = sTime; j <= eTime; j++)
			{
				if (STAFreqMap[j] > lMax)
				{
					lMax = (int) STAFreqMap[j];
					pT = j;
				}
			}
			tEvents.add(lMax + "_" + pT + "_" + sTime + "_" + eTime);
			if (MaxTweets < lMax)
				MaxTweets = lMax;
		}
		System.out.println("Events count before merging = " + tEvents.size());

		// Take events which are above 50% of highest event
		Set<MergeClass> GoodEventsSet = new TreeSet<>();
		Set<String> Temp = new TreeSet<>();
		Iterator<String> mIter = tEvents.iterator();
		while (mIter.hasNext())
		{
			String gEvent = mIter.next();
			String[] eArr = gEvent.split("_");

			int lMax = Integer.parseInt(eArr[0]);
			int pT = Integer.parseInt(eArr[1]);
			int sTime = Integer.parseInt(eArr[2]);
			int eTime = Integer.parseInt(eArr[3]);

			if (lMax > (Threshold_SmoothedEventPeak * MaxTweets))
			{
				MergeClass m = new MergeClass();
				m.PeakTime = pT;
				m.CentralEventStartTime = sTime;
				m.CentralEventEndTime = eTime;
				m.BeforeMainEventStartTime = sTime;
				m.AfterMainEventEndTime = eTime;
				m.Events.add(gEvent);

				GoodEventsSet.add(m);
				Temp.add(gEvent);
			}

		}
		System.out.println("GoodEventsSet count before merging = " + GoodEventsSet.size());

		// Remove Good events from tevents
		mIter = Temp.iterator();
		while (mIter.hasNext())
		{
			tEvents.remove(mIter.next());
		}
		System.out.println("tEvents count after removing good events = " + tEvents.size());

		// Combine smaller events with bigger events if they happened within 2 days and have time gap of 8 Hrs
		String TheBestLocalEvent = null;
		int leastValue = Integer.MAX_VALUE;
		MergeClass TheBestMergeClass = null;

		boolean flag_changed = false;
		while (true)
		{
			flag_changed = false;
			TheBestLocalEvent = null;
			TheBestMergeClass = null;
			leastValue = Integer.MAX_VALUE;

			mIter = tEvents.iterator();
			while (mIter.hasNext())
			{
				String gEvent = mIter.next();
				String[] eArr = gEvent.split("_");

				int lMax = Integer.parseInt(eArr[0]);
				int pT = Integer.parseInt(eArr[1]);
				int sTime = Integer.parseInt(eArr[2]);
				int eTime = Integer.parseInt(eArr[3]);

				// Read which good event is closer to them
				Iterator<MergeClass> mIter_good = GoodEventsSet.iterator();
				while (mIter_good.hasNext())
				{
					MergeClass mc = mIter_good.next();

					// whether local event is before the central event
					if (pT < mc.PeakTime)
					{
						int tGap1 = (mc.CentralEventStartTime - eTime) * myInfoInst.FREQ_IntervalTime;
						int tGap2 = (mc.PeakTime - eTime) * myInfoInst.FREQ_IntervalTime;
						if ((tGap1 < EventsGap_Threshold) && (tGap2 < EventsGap_Threshold_2))
						{
							if (tGap1 < leastValue)
							{
								TheBestLocalEvent = gEvent;
								TheBestMergeClass = mc;
								leastValue = tGap1;
							}
							flag_changed = true;
						}
					}
					else if (pT > mc.PeakTime)
					{
						int tGap1 = (sTime - mc.CentralEventEndTime) * myInfoInst.FREQ_IntervalTime;
						int tGap2 = (sTime - mc.PeakTime) * myInfoInst.FREQ_IntervalTime;
						if ((tGap1 < EventsGap_Threshold) && (tGap2 < EventsGap_Threshold_2))
						{
							if (tGap1 < leastValue)
							{
								TheBestLocalEvent = gEvent;
								TheBestMergeClass = mc;
								leastValue = tGap1;
							}
							flag_changed = true;
						}
					}
				}
			}

			if (flag_changed == true)
			{
				String[] eArr = TheBestLocalEvent.split("_");

				int pT = Integer.parseInt(eArr[1]);
				int sTime = Integer.parseInt(eArr[2]);
				int eTime = Integer.parseInt(eArr[3]);

				TheBestMergeClass.Events.add(TheBestLocalEvent);
				if (pT < TheBestMergeClass.PeakTime)
				{
					TheBestMergeClass.BeforeMainEventStartTime = sTime;
				}
				else
				{
					TheBestMergeClass.AfterMainEventEndTime = eTime;
				}
				tEvents.remove(TheBestLocalEvent);
				String GoodEvMsg = " | (" + TheBestMergeClass.CentralEventStartTime + "-" + TheBestMergeClass.CentralEventEndTime + ")" + IntervalToTxtDate(TheBestMergeClass.CentralEventStartTime)
						+ "  to " + IntervalToTxtDate(TheBestMergeClass.CentralEventEndTime) + " | ";
				CommFunctForED.logAndPrint("ge  MergeEvents : Merged TheBestLocalEvent =" + TheBestLocalEvent + " with " + GoodEvMsg);
				continue;

			}
			break;
		}

		System.out.println("tEvents count after merging local events = " + tEvents.size());

		// Add Events to Merged List

		Iterator<MergeClass> mIter2 = GoodEventsSet.iterator();
		while (mIter2.hasNext())
		{
			MergeClass mc = mIter2.next();
			MergedEvents.add(mc.CentralEventStartTime + "_" + mc.AfterMainEventEndTime);
			CommFunctForED.logAndPrint("  " + mc.CentralEventStartTime + "_" + mc.AfterMainEventEndTime + "  or " + IntervalToTxtDate(mc.CentralEventStartTime) + "  : "
					+ IntervalToTxtDate(mc.AfterMainEventEndTime));
		}
		
		
		CommFunctForED.logAndPrint("  MergeEvents : Events Count after Merging " + FinalEvents_Temp.size() + " is " + MergedEvents.size());

	}

	/*************************************************************************
	 * Convert Interval number to Date String
	 *************************************************************************/
	@SuppressWarnings("boxing")
	public static String IntervalToTxtDate(int interval)
	{
		Long d1 = GetAlpha.firstDayTime + interval * myInfoInst.FREQ_IntervalTime * 1000l;
		return new Date(d1).toString();
	}

	/*************************************************************************
	 * DiscardBadEvents
	 *************************************************************************/
	@SuppressWarnings("deprecation")
	public int DiscardBadEvents()
	{
		NumberFormat formatter = new DecimalFormat("#0.00");

		int discardedTweets = 0;
		CommFunctForED.logAndPrint("DiscardBadEvents  : ");
		for (int i = 0; i < MergedEvents.size(); i++)
		{
			boolean GoodEvent = false;
			String mEvent = MergedEvents.get(i);
			int jEventStartInterval = Integer.parseInt(mEvent.split("_")[0]); // In
																				// Freq
																				// Interval
			int jEventEndInterval = Integer.parseInt(mEvent.split("_")[1]);

			int TweetsCount = 0;
			int TimeIntervalsInDay = 0;

			int HighestActFreqInterval = 0;
			int HighestActFreqValue = 0;

			// int HighestSmoothedFreqInterval = 0;
			int HighestSmoothedFreqValue = 0;

			// double STASUM = 0;
			for (int j = jEventStartInterval; j <= jEventEndInterval; j++)
			{
				if (STAFreqMap[j] > HighestSmoothedFreqValue)
				{
					HighestSmoothedFreqValue = (int) STAFreqMap[j];
					// HighestSmoothedFreqInterval = j;
				}
				if (FreqMap[j] > HighestActFreqValue)
				{
					HighestActFreqValue = FreqMap[j];
					HighestActFreqInterval = j;
				}

				Date IntervalTime = new Date(GetAlpha.firstDayTime + j * myInfoInst.FREQ_IntervalTime * 1000l); // Discard
																												// Night
																												// Time
																												// Data
				if (IntervalTime.getHours() >= NightTime_End)
				{
					TimeIntervalsInDay++;
				}
				else
				{
					continue;
				}

				TweetsCount += FreqMap[j];
				// STASUM += STAFreqMap[j];
			}

			// double avg_1 = STASUM / (TimeIntervalsInDay);
			/*
			 * int crossing = 0; for (int j = jEventStartInterval; j <= (jEventEndInterval - 1); j++) { Date IntervalTime = new Date(GetAlpha.firstDayTime + j * myInfoInst.FREQ_IntervalTime * 1000l);
			 * //Discard Night Time Data if (IntervalTime.getHours() >= NightTime_End) { if ((STAFreqMap[j] < avg_1) && (STAFreqMap[j + 1] > avg_1)) { crossing++; } } }
			 */

			double AvgRate = 3660.0 * TweetsCount / ((jEventEndInterval - jEventStartInterval + 1) * myInfoInst.FREQ_IntervalTime);// per
																																	// hour
			double LifeOfEvent = myInfoInst.FREQ_IntervalTime * (jEventEndInterval - jEventStartInterval + 1) / 3600.0;// In
																														// Hrs

			String msg1 = "  DiscardBadEvents : Discarding " + i;
			String msg2 = "  DiscardBadEvents : **Adding** " + i;
			String msg3 = " | " + IntervalToTxtDate(jEventStartInterval) + "  to " + IntervalToTxtDate(jEventEndInterval) + ", | ";
			String msg4 = " Tweets=" + TweetsCount + " AvgRate=" + formatter.format(AvgRate) + "hrs";

			/*
			 * if (crossing > (Crossing_ThresholdsPerHour*TimeIntervalsInDay)) { CommFunctForED.logAndPrint("  DiscardBadEvents : Discarding event=" + i + " crossing =" + crossing + " > " +
			 * Crossing_ThresholdsPerHour + msg1); } else
			 */if (TweetsCount < MinTweets_BigEvents)
			{
				String error1 = " Tweets  < " + MinTweets_BigEvents;
				CommFunctForED.logAndPrint(msg1 + msg3 + msg4 + error1);
			}
			else if (AvgRate < MinRate_BigEvents)
			{
				String error1 = " AvgRate < " + MinRate_BigEvents;
				CommFunctForED.logAndPrint(msg1 + msg3 + msg4 + error1);
			}
			else if (LifeOfEvent < Threshold_EventTime)// 1Hr
			{
				String error1 = " ShortLived Spent " + LifeOfEvent + " Hrs < " + Threshold_EventTime;
				CommFunctForED.logAndPrint(msg1 + msg3 + msg4 + error1);
			}
			else if (HighestActFreqValue < (Threshold_EventPeak * MaxActFreq))
			{
				String error1 = " HighestActFreqValue < " + (Threshold_EventPeak * MaxActFreq);
				CommFunctForED.logAndPrint(msg1 + msg3 + msg4 + error1);
			}
			else if (HighestSmoothedFreqValue <= (Threshold_SmoothedEventPeak * MaxSmoothFreq))
			{
				String error1 = " HighestSmoothedFreqValue < " + (Threshold_SmoothedEventPeak * MaxSmoothFreq);
				CommFunctForED.logAndPrint(msg1 + msg3 + msg4 + error1);
			}
			else if (HighestSmoothedFreqValue <= MinRate_SmoothedEventData)
			{
				String error1 = " HighestSmoothedFreqValue < " + (MinRate_SmoothedEventData);
				CommFunctForED.logAndPrint(msg1 + msg3 + msg4 + error1);
			}
			else if ((HighestActFreqInterval == jEventStartInterval) || (HighestActFreqInterval == jEventEndInterval))
			{
				String error1 = " Highest Act Peak at Start or End Interval ";
				CommFunctForED.logAndPrint(msg1 + msg3 + msg4 + error1);
			}
			else if (TimeIntervalsInDay == 0)
			{
				String error1 = " Event happened in night only ";
				CommFunctForED.logAndPrint(msg1 + msg3 + msg4 + error1);
			}
			else
			{
				GoodEvent = true;
				FilteredEvents.add(mEvent);
				CommFunctForED.logAndPrint(msg2 + msg3 + msg4);
			}

			if (GoodEvent == false)
				discardedTweets += TweetsCount;
		}
		CommFunctForED.logAndPrint("  DiscardBadEvents :  Events Count after filtering =" + FilteredEvents.size());

		return discardedTweets;
	}

	/*************************************************************************
	 * GetGrowthOfEvents
	 * 
	 * @throws IOException
	 *************************************************************************/
	@SuppressWarnings({ "deprecation", "boxing" })
	public void GetGrowthOfEvents() throws IOException
	{
		CommFunctForED.logAndPrint("GetGrowthOfEvents()  :Topic=" + myInfoInst.TopicName);
		GrowthVector = new Vector<>();
		DecayVector = new Vector<>();
		ThresholdVector = new Vector<>();

		int savedTweets = 0;
		for (int i = 0; i < FilteredEvents.size(); i++)
		{
			int jEventStartInterval = Integer.parseInt(FilteredEvents.get(i).split("_")[0]); // In Freq Interval
			int jEventEndInterval = Integer.parseInt(FilteredEvents.get(i).split("_")[1]);

			DescriptiveStatistics ds = new DescriptiveStatistics();
			double HighestPeakAmplitue_Smoothed = 0;
			int HighestPeakInterval_Smoothed = 0;
			/*
			 * if (i == 8) { CommFunctForED.logAndPrint("GetAlpha.FindPeakThreshold()"); }
			 */

			for (int j = (jEventStartInterval); j <= jEventEndInterval; j++)
			{
				int mFreq_Smmothed = (int) STAFreqMap[j];
				Date TweetTime = new Date(GetAlpha.firstDayTime + j * myInfoInst.FREQ_IntervalTime * 1000l); // Discard NightTime Data
				if ((TweetTime.getHours() > NightTime_Start) && (TweetTime.getHours() < NightTime_End))
				{
					if (mFreq_Smmothed > HighestPeakAmplitue_Smoothed)
					{
						HighestPeakAmplitue_Smoothed = mFreq_Smmothed;
						HighestPeakInterval_Smoothed = j;
					}
					continue;
				}
				ds.addValue(STAFreqMap[j]);
				if (mFreq_Smmothed > HighestPeakAmplitue_Smoothed)
				{
					HighestPeakAmplitue_Smoothed = STAFreqMap[j];
					HighestPeakInterval_Smoothed = j;
				}
			}

			// Get Thresholds to define the peak

			double threshold = FindPeakThreshold(i, jEventStartInterval, jEventEndInterval, ds);
			ThresholdVector.add(threshold);

			// -------- Find duration of each phase of the event
			// Find Growth Point
			int Start_Steady = jEventStartInterval;
			for (int j = jEventStartInterval; j <= jEventEndInterval; j++)
			{
				int mFreq = (int) STAFreqMap[j];
				if (mFreq > threshold)
				{
					Start_Steady = j;
					break;
				}
			}
			// Find Decay Point
			int End_Steady = jEventEndInterval;
			for (int j = jEventEndInterval; j >= HighestPeakInterval_Smoothed; j--)
			{
				int mFreq = (int) STAFreqMap[j];
				if (mFreq > threshold)
				{
					End_Steady = j;
					break;
				}
			}
			CommFunctForED.logAndPrint("  GetGrowthOfEvents: Event Interval" + i + ": " + jEventStartInterval + "_" + Start_Steady + ":" + HighestPeakInterval_Smoothed + ":" + End_Steady + "_"
					+ jEventEndInterval);
			CommFunctForED.logAndPrint("  GetGrowthOfEvents: Event Value   " + i + ": " + (int) STAFreqMap[jEventStartInterval] + "_" + (int) STAFreqMap[Start_Steady] + ":"
					+ (int) STAFreqMap[HighestPeakInterval_Smoothed] + ":" + (int) STAFreqMap[End_Steady] + "_" + (int) STAFreqMap[jEventEndInterval]);

			// 1-4-6-10 = 1-3; 4-6; 7-10 = 3,3,4 = 4-1, 6-4+1, 10-6

			Time_Growth.addValue((Start_Steady - jEventStartInterval) * myInfoInst.FREQ_IntervalTime);
			Time_Peak.addValue((End_Steady - Start_Steady + 1) * myInfoInst.FREQ_IntervalTime);
			Time_Decay.addValue((jEventEndInterval - Start_Steady) * myInfoInst.FREQ_IntervalTime);

			int tG = 0;
			int tP = 0;
			int tD = 0;

			for (int j = jEventStartInterval; j < Start_Steady; j++)
			{
				tG += FreqMap[j];
			}
			for (int j = Start_Steady; j <= End_Steady; j++)
			{
				tP += FreqMap[j];
			}
			for (int j = (End_Steady + 1); j <= jEventEndInterval; j++)
			{
				tD += FreqMap[j];
			}
			Tweets_Growth.addValue(tG);
			Tweets_Peak.addValue(tP);
			Tweets_Decay.addValue(tD);
			savedTweets += (tG + tP + tD);

			int tGS = 0;
			int tPS = 0;
			int tDS = 0;

			for (int j = jEventStartInterval; j < Start_Steady; j++)
			{
				tGS += STAFreqMap[j];
			}
			for (int j = Start_Steady; j <= End_Steady; j++)
			{
				tPS += STAFreqMap[j];
			}
			for (int j = (End_Steady + 1); j <= jEventEndInterval; j++)
			{
				tDS += STAFreqMap[j];
			}
			Tweets_Growth_Smoothed.addValue(tGS);
			Tweets_Peak_Smoothed.addValue(tPS);
			Tweets_Decay_Smoothed.addValue(tDS);

			StringBuffer lBuf = new StringBuffer();
			lBuf.append((jEventStartInterval * 1000l * myInfoInst.FREQ_IntervalTime + GetAlpha.firstDayTime));
			lBuf.append("_");
			lBuf.append((Start_Steady * 1000l * myInfoInst.FREQ_IntervalTime + GetAlpha.firstDayTime));
			lBuf.append("_");
			lBuf.append((End_Steady * 1000l * myInfoInst.FREQ_IntervalTime + GetAlpha.firstDayTime));
			lBuf.append("_");
			lBuf.append((jEventEndInterval * 1000l * myInfoInst.FREQ_IntervalTime + GetAlpha.firstDayTime));
			lBuf.append("_");
			lBuf.append(threshold);
			ClassifiedMergedEvents.add(lBuf.toString()); // Time in seconds

			// Calculate Growth and Decay Rate
			double gRate = CalculateGrowthRate(jEventStartInterval, Start_Steady, HighestPeakInterval_Smoothed, End_Steady, jEventEndInterval, true, "Average");
			double dRate = CalculateGrowthRate(jEventStartInterval, Start_Steady, HighestPeakInterval_Smoothed, End_Steady, jEventEndInterval, false, "Average");
			CommFunctForED.logAndPrint("  GetGrowthOfEvents: Event " + i + " @Growth=" + gRate + " @Decay=" + dRate + " @threshold=" + threshold);
			GrowthVector.add(gRate);
			DecayVector.add(dRate);

			// .addValue(gRate); // For Global analysis in SegregateTopicClasses
			// decayStats.addValue(dRate); // For Global analysis in SegregateTopicClasses

			BufferedWriter out_ev = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_ClassifiedEvents, true));
			String[] t = lBuf.toString().split("_");
			out_ev.write(myInfoInst.TopicName + "\t" + t[0] + "\t" + t[1] + "\t" + t[2] + "\t" + t[3] + "\t" + gRate + "\t" + dRate + "\t" + t[4] + "\n");
			out_ev.close();

			NumberFormat formatter = new DecimalFormat("#0.00");
			out_ev = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_Peak2Avg, true));
			out_ev.write(myInfoInst.TopicName + "\t" + threshold + "\t" + HighestPeakAmplitue_Smoothed + "\t" + formatter.format(1.0 * HighestPeakAmplitue_Smoothed / ds.getMean()) + "\t" + gRate
					+ "\t" + dRate + "\t" + formatter.format(PeakEventThreshold * HighestPeakAmplitue_Smoothed) + "\t" + formatter.format(ds.getMean() + 2 * ds.getStandardDeviation()) + "\n");
			out_ev.close();
		}
		myInfoInst.SavedTweets = savedTweets;
		myInfoInst.SavedTweetsRatio = 1.0 * savedTweets / myInfoInst.Tweets;
		myInfoInst.BigEventsCount = FilteredEvents.size();
		myInfoInst.AllEventsCount = FinalEvents_Temp.size();
	}

	/*************************************************************************
	 * FindPeakThreshold
	 *************************************************************************/
	private double CalculateGrowthRate(int StartInterval, int StartSteadyInterval, int PeakInterval, int EndSteadyInterval, int EndInterval, boolean isGrowthFormulae, String Formula)
	{
		// while calculating growth, we can include the interval
		// StartSteadyInterval

		// http://econ.worldbank.org/WBSITE/EXTERNAL/DATASTATISTICS/0,,contentMDK:20452034~menuPK:64133156~pagePK:64133150~piPK:64133175~theSitePK:239419,00.html
		double EventRate = 0.0;
		if (Formula.matches("CompoundRate"))
		{
			int Time = (PeakInterval - StartInterval + 1) * (myInfoInst.FREQ_IntervalTime) / 60; // per minute
			double Ratio = 1.0 * STAFreqMap[StartSteadyInterval];
			if (!isGrowthFormulae)// For Decay
			{
				Ratio = 1.0 / STAFreqMap[EndSteadyInterval];
			}
			EventRate = Math.pow(Ratio, 1.0 / Time) - 1;
		}
		else if (Formula.matches("UptoThreshold"))
		{
			int Time = (StartSteadyInterval - StartInterval + 1) * (myInfoInst.FREQ_IntervalTime) / 60; // per minute
			double Ratio = 1.0 * STAFreqMap[StartSteadyInterval];
			if (!isGrowthFormulae)// For Decay
			{
				Time = (EndInterval - EndSteadyInterval + 1) * (myInfoInst.FREQ_IntervalTime) / 60; // per minute
				Ratio = 1.0 / STAFreqMap[EndSteadyInterval];
			}
			EventRate = Math.pow(Ratio, 1.0 / Time) - 1;
		}
		else if (Formula.matches("Average"))
		{
			int Time = (StartSteadyInterval - StartInterval + 1) * (myInfoInst.FREQ_IntervalTime) / 60; // per minute
			double Ratio = 1.0 * STAFreqMap[StartSteadyInterval];
			if (!isGrowthFormulae)// For Decay
			{
				Time = (EndInterval - EndSteadyInterval + 1) * (myInfoInst.FREQ_IntervalTime) / 60; // per minute
				Ratio = -1.0 * STAFreqMap[EndSteadyInterval];
			}
			EventRate = Ratio / Time;
		}

		/*
		 * else if (Formula.matches("ExponentialRate")) { EventRate = Math.log(Ratio / Time); } else if (Formula.matches("GeometricRate")) { EventRate = Math.exp((Math.log(Ratio)) / Time) - 1; } else
		 * if (Formula.matches("OLS"))// Ordinary Least Square { EventRate = Math.exp((Math.log(Ratio)) / Time) - 1; } else if (Formula.matches("Average"))// Ordinary Least Square { int start =
		 * jEventStartInterval; int end = HighestPeakInterval_Smoothed + 1; if (!isGrowthFormulae) { start = HighestPeakInterval_Smoothed; end = JEventEndInterval; } DescriptiveStatistics
		 * AvgGrowthStats = new DescriptiveStatistics(); for (int i = (start + 1); i < end; i++) { //double temp_rate = (STAFreqMap[i] - STAFreqMap[i - 1]) / STAFreqMap[i - 1]; Ratio = 1.0 *
		 * STAFreqMap[i] / STAFreqMap[i - 1]; double lTime = 1.0 * (myInfoInst.FREQ_IntervalTime) / 60; // per minute double temp_rate = Math.pow(Ratio, 1.0 / lTime) - 1;
		 * AvgGrowthStats.addValue(temp_rate); } EventRate = AvgGrowthStats.getMean(); }
		 */

		if (isGrowthFormulae && (EventRate <= 0))
		{
			CommFunctForED.logAndPrint("  CalculateGrowthRate: Growth Rate =" + EventRate + " cannot be 0 or negative ");
			System.exit(0);
		}

		if (!isGrowthFormulae && (EventRate > 0))
		{
			CommFunctForED.logAndPrint("  CalculateGrowthRate: Decay Rate =" + EventRate + " cannot be > 0 ");
			System.exit(0);
		}

		return EventRate;
	}

	/*************************************************************************
	 * FindPeakThreshold
	 *************************************************************************/
	private double FindPeakThreshold(int eventIndex, int mJEventStartInterval, int mJEventEndInterval, DescriptiveStatistics mDs)
	{

		double CurrentThreshold = mDs.getMean() + PeakThrSTDCount * mDs.getStandardDeviation();
		double retThreshold = CurrentThreshold;

		// Find threshold of individual events if they are merged
		for (int i = 0; i < FinalEvents_Temp.size(); i++)
		{
			String event[] = FinalEvents_Temp.get(i).split("_");
			// System.out.println(FinalEvents_Temp.get(i));
			int startInterval = Integer.parseInt(event[0]);
			int endInterval = Integer.parseInt(event[1]);
			double mean = Double.parseDouble(event[3]);
			double std = Double.parseDouble(event[4]);

			if ((startInterval >= mJEventStartInterval) && (endInterval <= mJEventEndInterval))
			{
				double lThreshold = mean + PeakThrSTDCount * std;
				if (lThreshold > retThreshold)
					retThreshold = lThreshold;
			}
		}

		double max = mDs.getMax();
		if ((PeakEventThreshold * max) < retThreshold)
		{
			retThreshold = PeakEventThreshold * max;
		}

		CommFunctForED.logAndPrint("\n  GetAlpha.FindPeakThreshold() eventIndex=" + eventIndex + ", MaxFreq=" + max + ", CurrentThreshold=" + CurrentThreshold + " retThreshold=" + retThreshold);
		return retThreshold;
	}

	/*************************************************************************
	 * FindCelebritiesParticipation
	 * 
	 * @throws IOException
	 * @throws NumberFormatException
	 *************************************************************************/
	@SuppressWarnings({ "boxing", "deprecation" })
	public void FindCelebritiesParticipation(File mFile, int EventIndex, int totalEvents) throws NumberFormatException, IOException
	{
		CommFunctForED.logAndPrint("FindCelebritiesParticipation()   : " + EventIndex + "/" + totalEvents);
		int lTotal = 0;
		int lCelebrities = 0;
		int lNonCelebrities_Total = 0;

		int lNonCelebrities_Original = 0;
		int lNonCelebrities_Forwards = 0;

		int lNonCelebrities_Forwards_Celebrity = 0;
		int lNonCelebrities_Forwards_NonCelebrity = 0;

		// GrowthSets.clear();
		// PeakSets.clear();
		// DecaySets.clear();

		String[] Event = ClassifiedMergedEvents.get(EventIndex).split("_");

		Date d1 = new Date(Long.parseLong(Event[0]));
		Date d2 = new Date(Long.parseLong(Event[1]));
		Date d3 = new Date(Long.parseLong(Event[2]));
		Date d4 = new Date(Long.parseLong(Event[3]));

		// int EventStartInterval = (int) (((d1.getTime() -
		// GetAlpha.firstDayTime) / 1000) / myInfoInst.FREQ_IntervalTime);
		// int EventEndInterval = (int) (((d4.getTime() - GetAlpha.firstDayTime)
		// / 1000) / myInfoInst.FREQ_IntervalTime);

		String st = null;
		long Time;
		double[] c_participation = new double[particiIntervals];
		double[] n_participation = new double[particiIntervals];
		double[] a_participation = new double[particiIntervals];

		BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(mFile.getAbsolutePath())));
		BufferedWriter out_ev = new BufferedWriter(new FileWriter(HashDefinesForED.EvenstsCelebDataFolder + "Celebrity_" + GetAlpha.myInfoInst.TopicName + ".txt", true));

		while ((st = br_i.readLine()) != null)
		{
			if (st.length() < 1)
				continue;

			String Tweet[] = st.split(RE_Sep_file, 2);
			String[] elements = Tweet[0].split(RE_Sep_group);
			Time = Long.parseLong(elements[Index_Tweet_Time]);

			if ((Time >= d1.getTime()) && (Time <= d4.getTime()))
			{
				int k = 1;
				if (Time < d2.getTime())
					k = 0;
				if (Time > d3.getTime())
					k = 2;
				a_participation[k] += 1;

				// int retweetCount = Integer.parseInt(elements[Index_Tweet_RetweetsCount]);
				// int favCount = Integer.parseInt(elements[Index_Tweet_LikesCount]);

				String User_Current[] = elements[Index_Tweet_User].split(RE_Sep_record);
				Long Uid = Long.parseLong(User_Current[Index_User_ID]);
				int followersCount = Integer.parseInt(User_Current[Index_User_Followers]);
				// int isVerified = Integer.parseInt(User_Current[Index_User_isVerified]);

				// if ((followersCount > CelebrityThreshold) || (isVerified == 1) || (Set_UsersPoliticians.contains(Uid)))
				if (followersCount > CelebrityThreshold)
				{
					c_participation[k] += 1;
					if (k == 0)
					{
						GrowthSets_C.get(EventIndex).add(Long.parseLong(User_Current[Index_User_ID]));
					}
					if (k == 1)
					{
						PeakSets_C.get(EventIndex).add(Long.parseLong(User_Current[Index_User_ID]));
					}
					if (k == 2)
					{
						DecaySets_C.get(EventIndex).add(Long.parseLong(User_Current[Index_User_ID]));
					}
				}
				else
				{
					n_participation[k] += 1;
					if (k == 0)
					{
						GrowthSets_NC.get(EventIndex).add(Long.parseLong(User_Current[Index_User_ID]));
					}
					if (k == 1)
					{
						PeakSets_NC.get(EventIndex).add(Long.parseLong(User_Current[Index_User_ID]));
					}
					if (k == 2)
					{
						DecaySets_NC.get(EventIndex).add(Long.parseLong(User_Current[Index_User_ID]));
					}
				}

				// Celebrity Analysis
				lTotal++;
				// if ((followersCount > CelebrityThreshold) || (User_Current[Index_User_isVerified].equals("1")) || Set_UsersPoliticians.contains(Uid)))
				if (followersCount > CelebrityThreshold)
				{
					lCelebrities++;
				}
				else
				{
					lNonCelebrities_Total++;
					if (Tweet.length > 1)
					{
						lNonCelebrities_Forwards++;
						String User_Original[] = Tweet[1].split(RE_Sep_group)[Index_Tweet_User].split(RE_Sep_record);
						int followersCount_Original = Integer.parseInt(User_Original[Index_User_Followers]);
						// if ((followersCount_Original > CelebrityThreshold) || (User_Original[Index_User_isVerified].equals("1")) || (Set_UsersPoliticians.contains(Uid)))
						if (followersCount > CelebrityThreshold)
						{
							lNonCelebrities_Forwards_Celebrity++;
						}
						else
						{
							lNonCelebrities_Forwards_NonCelebrity++;
						}
					}
					else
					{
						lNonCelebrities_Original++;
					}
				}
			}

			if (Time > d4.getTime())
				break;

		}
		// File Reading
		br_i.close();
		out_ev.close();

		out_ev = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_Celebrity, true));
		StringBuffer lBuf = new StringBuffer();
		NumberFormat formatter = new DecimalFormat("#0.0000");

		long eStrt = d1.getTime();
		long eEnd = d4.getTime();

		lBuf.append(myInfoInst.TopicName + "\t");// 0
		lBuf.append(EventIndex + "\t");

		lBuf.append(eStrt + "\t");
		lBuf.append(new Date(eStrt).toLocaleString() + "\t");

		lBuf.append(eEnd + "\t");// 4
		lBuf.append(new Date(eEnd).toLocaleString() + "\t");

		lBuf.append(lTotal + "\t");
		lBuf.append(formatter.format(100.0 * lCelebrities / lTotal) + "\t");// 7
		lBuf.append(formatter.format(100.0 * lNonCelebrities_Total / lTotal) + "\t");// 8

		lBuf.append(formatter.format(100.0 * lNonCelebrities_Original / lTotal) + "\t");// 9
		lBuf.append(formatter.format(100.0 * (lNonCelebrities_Forwards) / lTotal) + "\t");//

		lBuf.append(formatter.format(100.0 * (lNonCelebrities_Forwards_Celebrity) / lTotal) + "\t");// 11
		lBuf.append(formatter.format(100.0 * (lNonCelebrities_Forwards_NonCelebrity) / lTotal) + "\t");

		lBuf.append(formatter.format(100.0 * lNonCelebrities_Forwards_Celebrity / (lNonCelebrities_Forwards)) + "\t");// 13
		lBuf.append(formatter.format(100.0 * lNonCelebrities_Forwards_NonCelebrity / (lNonCelebrities_Forwards)) + "\t");

		lBuf.append(formatter.format(GrowthVector.get(EventIndex)) + "\t");// 15
		lBuf.append(formatter.format(DecayVector.get(EventIndex)) + "\t");// 16

		lBuf.append(ClassifiedMergedEvents.get(EventIndex) + "\t");// 17
		lBuf.append(myInfoInst.FREQ_IntervalTime + "\t");// 18
		lBuf.append(formatter.format(ThresholdVector.get(EventIndex)));// 19
		out_ev.write(lBuf.toString() + "\n");
		out_ev.close();

		// String FName = HashDefinesForED.EventsFolder + HashDefinesForED.FName_participationIndiv;
		// StringBuffer pBuf = new StringBuffer();
		// BufferedWriter out = new BufferedWriter(new FileWriter(FName, true));
		// pBuf.append(myInfoInst.TopicName + "\t");
		// pBuf.append(EventIndex + "\t");

		int cPartInAnEvent = 0;
		int nPartInAnEvent = 0;
		int aPartInAnEvent = 0;
		// Find total part of c, nc, all in an event
		for (int k = 0; k < particiIntervals; k++)
		{
			cPartInAnEvent += c_participation[k];
			nPartInAnEvent += n_participation[k];
			aPartInAnEvent += a_participation[k];
		}
		// pBuf.append(csum + "\t");
		// for (int j = 0; j < particiIntervals; j++)
		// {
		// pBuf.append((100.0 * c_participation[j] / csum) + "\t");
		// }
		// out.write(pBuf.toString() + "\n");
		// out.close();

		if (cPartInAnEvent != 0)
		{
			c_samples += 1;
		}
		if (nPartInAnEvent != 0)
		{
			n_samples += 1;
		}
		if (aPartInAnEvent != 0)
		{
			a_samples += 1;
		}
		CommFunctForED.logAndPrint("  EventIndex=" + EventIndex + " csum=" + cPartInAnEvent + " asum=" + aPartInAnEvent);
		for (int j = 0; j < particiIntervals; j++)
		{
			if (cPartInAnEvent != 0)
			{
				celebritiesParticipation[j] += c_participation[j] / cPartInAnEvent;
			}
			if (nPartInAnEvent != 0)
			{
				nonCelebritiesParticipation[j] += n_participation[j] / nPartInAnEvent;
			}
			if (aPartInAnEvent != 0)
			{
				allParticipation[j] += a_participation[j] / aPartInAnEvent;
			}
			// CommFunctForED.logAndPrint("allParticipation[j]=" +
			// allParticipation[j]);
			// CommFunctForED.logAndPrint(j + "\t" + celebritiesParticipation[j]
			// + "\t" + allParticipation[j]);
		}
		// CommFunctForED.logAndPrint("");
	}

	/*************************************************************************
	 * Sort the Array
	 * 
	 * @throws IOException
	 *************************************************************************/
	public static void WriteConsolidatedParticipation() throws IOException
	{
		CommFunctForED.logAndPrint("WriteConsolidatedParticipation()  :");
		String FName = HashDefinesForED.EventsFolder + HashDefinesForED.FName_participationAll;
		BufferedWriter out = new BufferedWriter(new FileWriter(FName));
		out.write("#PhaseOfEvent" + "\t" + "celebritiesParticipation" + "\t" + "nonCelebritiesParticipation" + "\t" + "allParticipation" + "\n");
		for (int j = 0; j < particiIntervals; j++)
		{
			out.write(j + "\t" + celebritiesParticipation[j] / c_samples + "\t" + nonCelebritiesParticipation[j] / n_samples + "\t" + allParticipation[j] / a_samples + "\n");
		}
		out.close();
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
				return res != 0 ? res : 1; // Special fix to preserve items with
											// equal values
			}
		});
		sortedEntries.addAll(map.entrySet());
		return sortedEntries;
	}

	

	/*************************************************************************
	 * FindCelebritiesParticipation
	 *************************************************************************/
	@SuppressWarnings("boxing")
	public void FindCelebritiesEngagement(File mFile) throws NumberFormatException, IOException
	{
		CommFunctForED.logAndPrint("FindCelebritiesEngagement()  :");
		if (ClassifiedMergedEvents.size() <= 0)
		{
			return;
		}

		NumberFormat formatter = new DecimalFormat("#0.00");

		TreeMap<Long, String> Celeb_Tree = new TreeMap<>();
		TreeMap<Long, String> NonCeleb_Tree = new TreeMap<>();

		BufferedWriter Out_C = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_Engagement_C, true));
		BufferedWriter Out_NC = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_Engagement_NC, true));

		String st = null;
		long Time;
		int followersCount = 0;
		Long uid = null;
		int eventIndex = 0;

		String[] Event = GetAlpha.ClassifiedMergedEvents.get(eventIndex).split("_");
		Date d1 = new Date(Long.parseLong(Event[0]));
		Date d4 = new Date(Long.parseLong(Event[3]));
		long eStrt = d1.getTime();
		long eEnd = d4.getTime();
		int MaxEvents = GetAlpha.ClassifiedMergedEvents.size();

		int tweetsInEvent = 0;
		int CelebTweets = 0;
		int NonCelebTweets = 0;

		BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(mFile.getAbsolutePath())));
		st = br_i.readLine();
		while (true)
		{
			if (st == null)
			{
				break;
			}
			if (st.length() < 1)
			{
				st = br_i.readLine();
				continue;
			}
			String Tweet[] = st.split(RE_Sep_file, 2);
			String[] elements = Tweet[0].split(RE_Sep_group);
			Time = Long.parseLong(elements[Index_Tweet_Time]);

			String User_Current[] = elements[Index_Tweet_User].split(RE_Sep_record);
			uid = Long.valueOf(User_Current[Index_User_ID]);
			followersCount = Integer.parseInt(User_Current[Index_User_Followers]);

			if (Time < eStrt)
			{
				// Do nothing
			}
			else if ((Time >= eStrt) && (Time <= eEnd))
			{
				tweetsInEvent++;
				// if ((followersCount > CelebrityThreshold) || (User_Current[Index_User_isVerified].equals("1")) || (Set_UsersPoliticians.contains(uid)))
				if (followersCount > CelebrityThreshold)
				{
					String value = Celeb_Tree.get(uid);
					if (value == null)
					{
						Celeb_Tree.put(uid, 1 + " " + Time);
					}
					else
					{
						int count = Integer.parseInt(value.split("\\s+")[0]);
						String StartTime = value.split("\\s+")[1];
						Celeb_Tree.put(uid, (count + 1) + " " + StartTime + " " + Time);
					}
					CelebTweets++;
				}
				else
				{
					String value = NonCeleb_Tree.get(uid);
					if (value == null)
					{
						NonCeleb_Tree.put(uid, 1 + " " + Time);
					}
					else
					{
						int count = Integer.parseInt(value.split("\\s+")[0]);
						String StartTime = value.split("\\s+")[1];
						NonCeleb_Tree.put(uid, (count + 1) + " " + StartTime + " " + Time);
					}
					NonCelebTweets++;
				}
			}
			else if (Time > eEnd)
			{
				// ----------- Find Time between first and last tweet of celeb and Tweets by celebs
				DescriptiveStatistics ds_Count = new DescriptiveStatistics();
				DescriptiveStatistics ds_Time = new DescriptiveStatistics(); // Time diff between first and last tweet
				long time = 0;
				Iterator<Long> iter = Celeb_Tree.keySet().iterator();
				while (iter.hasNext())
				{
					Long Uid = iter.next();
					String value[] = Celeb_Tree.get(Uid).split("\\s+");
					int count = Integer.parseInt(value[0]);
					if (count == 1)
					{
						time = 0;
					}
					else
					{
						time = Long.parseLong(value[2]) - Long.parseLong(value[1]);
					}
					ds_Count.addValue(count);
					ds_Time.addValue(time);
				}

				Double mean_count = ds_Count.getMean();
				Double mean_time = ds_Time.getMean();
				if (mean_count.isNaN())
				{
					mean_count = 0d;
				}
				if (mean_time.isNaN())
				{
					mean_time = 0d;
				}

				// TM_Engagement.put(myInfoInst.TopicName + "_" + eventIndex, ds_Count.getMean() + "");

				Out_C.write(myInfoInst.TopicName + "\t" + eventIndex + "\t" + tweetsInEvent + "\t" + CelebTweets + "\t" + Celeb_Tree.size() + "\t" + formatter.format(mean_count) + "\t"
						+ formatter.format(mean_time) + "\n");

				// ----------- Find Time between first and last tweet of non-celeb and Tweets by non-celebs
				ds_Count.clear();
				ds_Time.clear(); // Time diff between first and last tweet
				iter = NonCeleb_Tree.keySet().iterator();
				while (iter.hasNext())
				{
					Long Uid = iter.next();
					String value[] = NonCeleb_Tree.get(Uid).split("\\s+");
					int count = Integer.parseInt(value[0]);
					if (count == 1)
					{
						time = 0;
					}
					else
					{
						time = Long.parseLong(value[2]) - Long.parseLong(value[1]);
					}
					ds_Count.addValue(count);
					ds_Time.addValue(time);
				}

				mean_count = ds_Count.getMean();
				mean_time = ds_Time.getMean();
				if (mean_count.isNaN())
				{
					mean_count = 0d;
				}
				if (mean_time.isNaN())
				{
					mean_time = 0d;
				}
				Out_NC.write(myInfoInst.TopicName + "\t" + eventIndex + "\t" + tweetsInEvent + "\t" + NonCelebTweets + "\t" + NonCeleb_Tree.size() + "\t" + formatter.format(mean_count) + "\t"
						+ formatter.format(mean_time) + "\n");

				// ---------------- Clear DS--------------
				Celeb_Tree.clear();
				NonCeleb_Tree.clear();
				CelebTweets = 0;
				NonCelebTweets = 0;
				tweetsInEvent = 0;

				// -- Read Next event
				eventIndex++;
				if (eventIndex < MaxEvents)
				{
					Event = GetAlpha.ClassifiedMergedEvents.get(eventIndex).split("_");
					d1 = new Date(Long.parseLong(Event[0]));
					d4 = new Date(Long.parseLong(Event[3]));
					eStrt = d1.getTime();
					eEnd = d4.getTime();
				}
				else
					break;

				continue;// don't read next tweet here
			}

			// Read Next Tweet
			st = br_i.readLine();
			continue;
		}
		br_i.close();

		Out_C.close();
		Out_NC.close();
	}

	/*************************************************************************
	 * ProcessEventsOverlap
	 * 
	 * @throws IOException
	 *************************************************************************/
	private void ProcessEventsOverlapWithinEvents() throws IOException
	{
		// CommFunctForED.logAndPrint("ProcessEventsOverlap()  : WithinEvent");
		// WithinEvent
		for (int i = 0; i < ClassifiedMergedEvents.size(); i++)
		{

			// -----------For Celebrities------------------------------
			int Celebrities_PeopleOfGrowthPhaseSize = 0;
			int Celebrities_PeopleOfPeakPhaseSize_Old = 0;
			int Celebrities_PeopleOfPeakPhaseSize_New = 0;
			int Celebrities_PeopleOfDecayPhaseSize_Old = 0;
			int Celebrities_PeopleOfDecayPhaseSize_New = 0;

			int Celebrities_TweetsOfGrowthPhaseSize = 0;
			int Celebrities_TweetsOfPeakPhaseSize_Old = 0;
			int Celebrities_TweetsOfPeakPhaseSize_New = 0;
			int Celebrities_TweetsOfDecayPhaseSize_Old = 0;
			int Celebrities_TweetsOfDecayPhaseSize_New = 0;

			Set<Long> PeopleOfGrowthPhase = new TreeSet<>();
			Set<Long> PeopleOfPeakPhase_Old = new TreeSet<>();
			Set<Long> PeopleOfPeakPhase_New = new TreeSet<>();
			Set<Long> PeopleOfDecayPhase_Old = new TreeSet<>();
			Set<Long> PeopleOfDecayPhase_New = new TreeSet<>();

			Iterator<Long> iter = GrowthSets_C.get(i).iterator();
			while (iter.hasNext())
			{
				PeopleOfGrowthPhase.add(iter.next());
				Celebrities_TweetsOfGrowthPhaseSize++;
			}

			iter = PeakSets_C.get(i).iterator();
			while (iter.hasNext())
			{
				Long id = iter.next();
				if (PeopleOfGrowthPhase.contains(id))
				{
					PeopleOfPeakPhase_Old.add(id);
					Celebrities_TweetsOfPeakPhaseSize_Old++;
				}
				else
				{
					PeopleOfPeakPhase_New.add(id);
					Celebrities_TweetsOfPeakPhaseSize_New++;
				}
			}

			iter = DecaySets_C.get(i).iterator();
			while (iter.hasNext())
			{
				Long id = iter.next();
				if (PeopleOfGrowthPhase.contains(id) || PeopleOfPeakPhase_Old.contains(id) || PeopleOfPeakPhase_New.contains(id))
				{
					PeopleOfDecayPhase_Old.add(id);
					Celebrities_TweetsOfDecayPhaseSize_Old++;
				}
				else
				{
					PeopleOfDecayPhase_New.add(id);
					Celebrities_TweetsOfDecayPhaseSize_New++;
				}
			}
			Celebrities_PeopleOfGrowthPhaseSize = PeopleOfGrowthPhase.size();
			Celebrities_PeopleOfPeakPhaseSize_Old = PeopleOfPeakPhase_Old.size();
			Celebrities_PeopleOfPeakPhaseSize_New = PeopleOfPeakPhase_New.size();
			Celebrities_PeopleOfDecayPhaseSize_Old = PeopleOfDecayPhase_Old.size();
			Celebrities_PeopleOfDecayPhaseSize_New = PeopleOfDecayPhase_New.size();

			// ---------------------- Non Celebrities --------------------------------------------------
			// ==========================================================================================

			int Non_Celebrities_PeopleOfGrowthPhaseSize = 0;
			int Non_Celebrities_PeopleOfPeakPhaseSize_Old = 0;
			int Non_Celebrities_PeopleOfPeakPhaseSize_New = 0;
			int Non_Celebrities_PeopleOfDecayPhaseSize_Old = 0;
			int Non_Celebrities_PeopleOfDecayPhaseSize_New = 0;

			int Non_Celebrities_TweetsOfGrowthPhaseSize = 0;
			int Non_Celebrities_TweetsOfPeakPhaseSize_Old = 0;
			int Non_Celebrities_TweetsOfPeakPhaseSize_New = 0;
			int Non_Celebrities_TweetsOfDecayPhaseSize_Old = 0;
			int Non_Celebrities_TweetsOfDecayPhaseSize_New = 0;

			PeopleOfGrowthPhase.clear();
			PeopleOfPeakPhase_Old.clear();
			PeopleOfPeakPhase_New.clear();
			PeopleOfDecayPhase_Old.clear();
			PeopleOfDecayPhase_New.clear();

			iter = GrowthSets_NC.get(i).iterator();
			while (iter.hasNext())
			{
				PeopleOfGrowthPhase.add(iter.next());
				Non_Celebrities_TweetsOfGrowthPhaseSize++;
			}

			iter = PeakSets_NC.get(i).iterator();
			while (iter.hasNext())
			{
				Long id = iter.next();
				if (PeopleOfGrowthPhase.contains(id))
				{
					PeopleOfPeakPhase_Old.add(id);
					Non_Celebrities_TweetsOfPeakPhaseSize_Old++;
				}
				else
				{
					PeopleOfPeakPhase_New.add(id);
					Non_Celebrities_TweetsOfPeakPhaseSize_New++;
				}
			}

			iter = DecaySets_NC.get(i).iterator();
			while (iter.hasNext())
			{
				Long id = iter.next();
				if (PeopleOfGrowthPhase.contains(id) || PeopleOfPeakPhase_Old.contains(id) || PeopleOfPeakPhase_New.contains(id))
				{
					PeopleOfDecayPhase_Old.add(id);
					Non_Celebrities_TweetsOfDecayPhaseSize_Old++;
				}
				else
				{
					PeopleOfDecayPhase_New.add(id);
					Non_Celebrities_TweetsOfDecayPhaseSize_New++;
				}
			}
			Non_Celebrities_PeopleOfGrowthPhaseSize = PeopleOfGrowthPhase.size();
			Non_Celebrities_PeopleOfPeakPhaseSize_Old = PeopleOfPeakPhase_Old.size();
			Non_Celebrities_PeopleOfPeakPhaseSize_New = PeopleOfPeakPhase_New.size();
			Non_Celebrities_PeopleOfDecayPhaseSize_Old = PeopleOfDecayPhase_Old.size();
			Non_Celebrities_PeopleOfDecayPhaseSize_New = PeopleOfDecayPhase_New.size();

			PeopleOfGrowthPhase.clear();
			PeopleOfPeakPhase_Old.clear();
			PeopleOfPeakPhase_New.clear();
			PeopleOfDecayPhase_Old.clear();
			PeopleOfDecayPhase_New.clear();

			BufferedWriter out_ev = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_SubsequentInvolvementWithinEvent, true));

			StringBuffer sBuf = new StringBuffer();
			sBuf.append(myInfoInst.TopicName + "\t");// 0
			sBuf.append(i + "\t");// eventid 1
			sBuf.append(Celebrities_PeopleOfGrowthPhaseSize + "\t");// 2
			sBuf.append(Celebrities_PeopleOfPeakPhaseSize_Old + "\t");//
			sBuf.append(Celebrities_PeopleOfPeakPhaseSize_New + "\t"); //
			sBuf.append(Celebrities_PeopleOfDecayPhaseSize_Old + "\t"); //
			sBuf.append(Celebrities_PeopleOfDecayPhaseSize_New + "\t"); //

			sBuf.append(Celebrities_TweetsOfGrowthPhaseSize + "\t"); // 7
			sBuf.append(Celebrities_TweetsOfPeakPhaseSize_Old + "\t"); //
			sBuf.append(Celebrities_TweetsOfPeakPhaseSize_New + "\t"); //
			sBuf.append(Celebrities_TweetsOfDecayPhaseSize_Old + "\t"); //
			sBuf.append(Celebrities_TweetsOfDecayPhaseSize_New + "\t"); //

			sBuf.append(Non_Celebrities_PeopleOfGrowthPhaseSize + "\t"); // 12
			sBuf.append(Non_Celebrities_PeopleOfPeakPhaseSize_Old + "\t"); //
			sBuf.append(Non_Celebrities_PeopleOfPeakPhaseSize_New + "\t"); //
			sBuf.append(Non_Celebrities_PeopleOfDecayPhaseSize_Old + "\t"); //
			sBuf.append(Non_Celebrities_PeopleOfDecayPhaseSize_New + "\t"); //

			sBuf.append(Non_Celebrities_TweetsOfGrowthPhaseSize + "\t"); // 17
			sBuf.append(Non_Celebrities_TweetsOfPeakPhaseSize_Old + "\t"); //
			sBuf.append(Non_Celebrities_TweetsOfPeakPhaseSize_New + "\t"); //
			sBuf.append(Non_Celebrities_TweetsOfDecayPhaseSize_Old + "\t"); //
			sBuf.append(Non_Celebrities_TweetsOfDecayPhaseSize_New + "\t"); // 21
			out_ev.write(sBuf.toString() + "\n");
			out_ev.close();
		}
	}

	
	/***************************************************************
	 * ReadScreenNames
	 ***************************************************************/
	private static void GetCelebritiesParticipation() throws IOException
	{

		CommFunctForED.logAndPrint("GetCelebritiesParticipation()  :" + HashDefinesForED.GGUserType);
		BufferedWriter br_o = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_participationByCelebs));
		br_o.write("#Topic\tEventId\tCelebPartVal\tNonCelebPartVal\tTimeInHrs\tTweetsCount\tGrowthRate\tDecayRate\n");

		String readStr = "";
		NumberFormat formatter = new DecimalFormat("#0.00");
		NumberFormat formatter_2 = new DecimalFormat("#0.000000");
		NumberFormat formatter_3 = new DecimalFormat("#000000");

		BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(HashDefinesForED.EventsFolder + HashDefinesForED.FName_Celebrity)));
		br_i.readLine();// Skip Header
		while ((readStr = br_i.readLine()) != null)
		{
			String st1[] = readStr.split("\t");
			String sTopic = st1[0];
			String sEventId = st1[1];

			int tweetsCount = Integer.parseInt(st1[6]);
			double Celeb = Double.parseDouble(st1[7]);
			double NonCeleb = Double.parseDouble(st1[8]);

			double GrowthRate = Double.parseDouble(st1[15]);
			double DecayRate = Double.parseDouble(st1[16]);

			int CelebPart = (int) (tweetsCount * (Celeb) / 100);// Celebrities
			int NonCelebPart = (int) (tweetsCount * (NonCeleb) / 100);// Non
																		// Celebrities

			double TimeInHrs = (Long.parseLong(st1[4]) - Long.parseLong(st1[2])) / (1000 * 3600);

			br_o.write(String.format("%10s", sTopic) + "\t" + sEventId + "\t" + formatter_3.format(CelebPart) + "\t" + formatter_3.format(NonCelebPart) + "\t" + formatter.format(TimeInHrs) + "\t"
					+ formatter_2.format(GrowthRate) + "\t" + formatter_2.format(DecayRate) + "\t" + tweetsCount + "\n");

		}
		br_i.close();
		br_o.close();
	}

	/***************************************************************
	 * PostCalculations
	 ***************************************************************/
	private void PostCalculations() throws NumberFormatException, IOException
	{
		NumberFormat formatter2 = new DecimalFormat("#0.00");

		CommFunctForED.logAndPrint("\nPostCalculations : " + HashDefinesForED.GGUserType);

		String st = null;
		String fName = HashDefinesForED.EventsFolder + HashDefinesForED.FName_PhasesLengthInTime;
		BufferedWriter br_o = new BufferedWriter(new FileWriter(fName));
		br_o.write("#Index\tGrowthPhaseTime\tPeakPhaseTime\tDecayPhaseTime\n");
		System.out.println("Writer : FileName = " + fName);
		for (int i = 0; i < Time_Growth.getN(); i++)
		{
			br_o.write(i + "\t" + Time_Growth.getElement(i) + "\t" + Time_Peak.getElement(i) + "\t" + Time_Decay.getElement(i) + "\n");
		}
		br_o.close();
		double TimeTotal = (Time_Peak.getMean() + Time_Decay.getMean() + Time_Growth.getMean());

		CommFunctForED.logAndPrint("PostCalculations : Average time of Growth Phase in seconds=" + Time_Growth.getMean() + " " + (100.0 * Time_Growth.getMean() / TimeTotal));
		CommFunctForED.logAndPrint("PostCalculations : Average time of Peak Phase in seconds=" + Time_Peak.getMean() + " " + (100.0 * Time_Peak.getMean() / TimeTotal));
		CommFunctForED.logAndPrint("PostCalculations : Average time of Decay Phase in seconds=" + Time_Decay.getMean() + " " + (100.0 * Time_Decay.getMean() / TimeTotal));

		fName = HashDefinesForED.EventsFolder + HashDefinesForED.FName_PhasesLengthInTweets;
		br_o = new BufferedWriter(new FileWriter(fName));
		br_o.write("#Index\tGrowthPhaseTweets\tPeakPhaseTweets\tDecayPhaseTweets\n");
		System.out.println("Writer : FileName = " + fName);
		for (int i = 0; i < Tweets_Growth.getN(); i++)
		{
			br_o.write(i + "\t" + Tweets_Growth.getElement(i) + "\t" + Tweets_Peak.getElement(i) + "\t" + Tweets_Decay.getElement(i) + "\n");
		}
		br_o.close();

		double TweetsTotal = (Tweets_Growth.getMean() + Tweets_Peak.getMean() + Tweets_Decay.getMean());
		CommFunctForED.logAndPrint("PostCalculations : Average tweets in Growth Phase=" + Tweets_Growth.getMean() + " " + (100.0 * Tweets_Growth.getMean() / TweetsTotal));
		CommFunctForED.logAndPrint("PostCalculations : Average tweets in Peak Phase =" + Tweets_Peak.getMean() + " " + (100.0 * Tweets_Peak.getMean() / TweetsTotal));
		CommFunctForED.logAndPrint("PostCalculations : Average tweets in Decay Phase =" + Tweets_Decay.getMean() + " " + (100.0 * Tweets_Decay.getMean() / TweetsTotal));

		fName = HashDefinesForED.EventsFolder + HashDefinesForED.FName_PhasesLengthInTweets_Smoothed;
		br_o = new BufferedWriter(new FileWriter(fName));
		br_o.write("#Index\tGrowthPhaseTweets\tPeakPhaseTweets\tDecayPhaseTweets\n");
		System.out.println("Writer : FileName = " + fName);
		for (int i = 0; i < Tweets_Growth_Smoothed.getN(); i++)
		{
			br_o.write(i + "\t" + Tweets_Growth_Smoothed.getElement(i) + "\t" + Tweets_Peak_Smoothed.getElement(i) + "\t" + Tweets_Decay_Smoothed.getElement(i) + "\n");
		}
		br_o.close();
				
		// ----------------------- ENGAGEMENT----------------------------------------------------------------------------------

		DescriptiveStatistics ds_C = new DescriptiveStatistics();
		DescriptiveStatistics ds_NC = new DescriptiveStatistics();

		fName = HashDefinesForED.EventsFolder + HashDefinesForED.FName_Engagement_C;
		BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(fName)));
		br_i.readLine();
		while ((st = br_i.readLine()) != null)
		{
			ds_C.addValue(Double.parseDouble(st.split("\\t")[5]));
		}
		br_i.close();
		System.out.println("FileName = " + fName);
		CommFunctForED.logAndPrint("\nCelebrities Engagement(TweetsCount) in all phases of events has Mean= " + formatter2.format(ds_C.getMean()) + " (STD="
				+ formatter2.format(ds_C.getStandardDeviation()) + ")");

		fName = HashDefinesForED.EventsFolder + HashDefinesForED.FName_Engagement_NC;
		br_i = new BufferedReader(new InputStreamReader(new FileInputStream(fName)));
		System.out.println("FileName = " + fName);
		br_i.readLine();
		while ((st = br_i.readLine()) != null)
		{
			ds_NC.addValue(Double.parseDouble(st.split("\\t")[5]));
		}
		br_i.close();
		CommFunctForED.logAndPrint("Non-Celebrities Engagement(TweetsCount) in all phases of events has Mean= " + formatter2.format(ds_NC.getMean()) + " (STD="
				+ formatter2.format(ds_NC.getStandardDeviation()) + ")");

		// ........................................................................................
		fName = HashDefinesForED.EventsFolder + HashDefinesForED.FName_Engagement_C;
		br_i = new BufferedReader(new InputStreamReader(new FileInputStream(fName)));
		System.out.println("FileName = " + fName);
		br_i.readLine();
		while ((st = br_i.readLine()) != null)
		{
			ds_C.addValue(Double.parseDouble(st.split("\\t")[6]));
		}
		br_i.close();
		CommFunctForED.logAndPrint("Celebrities Engagement(TweetsTime) in all phases of events has Mean=" + formatter2.format(ds_C.getMean()) + " (STD="
				+ formatter2.format(ds_C.getStandardDeviation()) + ")");

		fName = HashDefinesForED.EventsFolder + HashDefinesForED.FName_Engagement_NC;
		br_i = new BufferedReader(new InputStreamReader(new FileInputStream(fName)));
		System.out.println("FileName = " + fName);
		br_i.readLine();
		while ((st = br_i.readLine()) != null)
		{
			ds_NC.addValue(Double.parseDouble(st.split("\\t")[6]));
		}
		br_i.close();
		CommFunctForED.logAndPrint("Non-Celebrities Engagement(TweetsTime) in all phases of events has Mean=" + formatter2.format(ds_NC.getMean()) + " (STD="
				+ formatter2.format(ds_NC.getStandardDeviation()) + ")");

		CommFunctForED.logAndPrint("\nCelebrities Involvement across 100% events has Mean=" + formatter2.format(ds_C.getMean()) + " (STD=" + formatter2.format(ds_C.getStandardDeviation()) + ")");
		CommFunctForED.logAndPrint("Non-Celebrities Involvement across 100% events has Mean=" + formatter2.format(ds_NC.getMean()) + " (STD=" + formatter2.format(ds_NC.getStandardDeviation()) + ")");
		// -------------------------------------------
	}
}
