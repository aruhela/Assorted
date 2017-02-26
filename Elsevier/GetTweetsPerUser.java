/*******************
Author  : Amit Ruhela
Purpose : Find how many tweets are posted by Celebrity and Non Celebrity user in different phases of events.

*******************/

package Elsevier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class GetTweetsPerUser
{

	static String gTopic = "";
	//Tweet
	static final int Index_Tweet_ID = 0;
	static final int Index_Tweet_Time = 1;
	static final int Index_Tweet_isReTweet = 2;
	static final int Index_Tweet_RetweetsCount = 5;
	static final int Index_Tweet_LikesCount = 7;
	static final int Index_Tweet_User = 10;
	static final int Index_Tweet_InReplyToStatusId = 11;
	static final int Index_HashTag = 14;

	//User
	static final int Index_User_ID = 0;
	static final int Index_User_Followers = 2;
	static final int Index_User_isVerified = 10;

	//Regular Expression
	public static final String RE_Sep_file = "\\-<\\+>\\-";// File
	public static final String Sep_group = "\t";// Group
	public static final String RE_Sep_group = "\\t";// Group
	public static final String RE_Sep_record = ";";// Record

	static int CelebrityThreshold = 3000;
	public static double CelebrityPercentile = 0;

	public static String gUserType = "";
	public static String gFileName = "/home/amit/Celebrity/Output/Plots_9990/TweetsVolume/Info.txt";

	static Set<Long> Set_ManualCelebrites = new HashSet<>();
	static TreeMap<String, String> TM_Events = new TreeMap<>();

	public static DescriptiveStatistics ds_Total_C = new DescriptiveStatistics();
	public static DescriptiveStatistics ds_Peak_C = new DescriptiveStatistics();
	public static DescriptiveStatistics ds_Growth_C = new DescriptiveStatistics();
	public static DescriptiveStatistics ds_Decay_C = new DescriptiveStatistics();

	public static DescriptiveStatistics ds_Total_NC = new DescriptiveStatistics();
	public static DescriptiveStatistics ds_Growth_NC = new DescriptiveStatistics();
	public static DescriptiveStatistics ds_Peak_NC = new DescriptiveStatistics();
	public static DescriptiveStatistics ds_Decay_NC = new DescriptiveStatistics();

	public static DescriptiveStatistics ds_TweetsPercentage_Total = new DescriptiveStatistics();
	public static DescriptiveStatistics ds_TweetsPercentage_Growth = new DescriptiveStatistics();
	public static DescriptiveStatistics ds_TweetsPercentage_Peak = new DescriptiveStatistics();
	public static DescriptiveStatistics ds_TweetsPercentage_Decay = new DescriptiveStatistics();

	public static DescriptiveStatistics ds_usersPercentage_Total = new DescriptiveStatistics();
	public static DescriptiveStatistics ds_usersPercentage_Growth = new DescriptiveStatistics();
	public static DescriptiveStatistics ds_usersPercentage_Peak = new DescriptiveStatistics();
	public static DescriptiveStatistics ds_usersPercentage_Decay = new DescriptiveStatistics();

	/***************************************************************
	 * MainFile
	 ***************************************************************/
	GetTweetsPerUser(int cthresh, String mUserType)
	{
		if (mUserType.equals("B"))
		{
			CelebrityThreshold = (int) HashDefinesForED.CelebThr_B[cthresh][1];
			CelebrityPercentile = HashDefinesForED.CelebThr_B[cthresh][0];
		}
		if (mUserType.equals("S"))
		{
			CelebrityThreshold = (int) HashDefinesForED.CelebThr_S[cthresh][1];
			CelebrityPercentile = HashDefinesForED.CelebThr_S[cthresh][0];
		}
		if (mUserType.equals("P"))
		{
			CelebrityThreshold = (int) HashDefinesForED.CelebThr_P[cthresh][1];
			CelebrityPercentile = HashDefinesForED.CelebThr_P[cthresh][0];
		}
		gUserType = mUserType;
		//CelebrityThreshold = 3000;
		System.out.println("CelebrityThreshold=" + CelebrityThreshold + "\t" + "CelebrityPercentile=" + CelebrityPercentile);
		HashDefinesForED.SetUserType(mUserType, CelebrityPercentile);

		Set_ManualCelebrites.clear();
		ds_Total_C.clear();
		ds_Peak_C.clear();
		ds_Growth_C.clear();
		ds_Decay_C.clear();

		ds_Total_NC.clear();
		ds_Growth_NC.clear();
		ds_Peak_NC.clear();
		ds_Decay_NC.clear();

		ds_TweetsPercentage_Total.clear();
		ds_TweetsPercentage_Growth.clear();
		ds_TweetsPercentage_Peak.clear();
		ds_TweetsPercentage_Decay.clear();

		ds_usersPercentage_Total.clear();
		ds_usersPercentage_Growth.clear();
		ds_usersPercentage_Peak.clear();
		ds_usersPercentage_Decay.clear();
	}

	/***************************************************************
	 * MainFile
	 * @throws InterruptedException 
	 ***************************************************************/
	public static void main(String[] args) throws IOException, InterruptedException
	{
		String myClassName = Thread.currentThread().getStackTrace()[1].getClassName();
		long startExecution = (new Long(System.currentTimeMillis())).longValue();

		BufferedWriter out = new BufferedWriter(new FileWriter(gFileName));
		out.write("Mean_C\tMean_C\tProportionofPopUserTweets\n");
		out.close();

		String[] arr = { "B", "P", "S" };
		for (int i = 0; i < arr.length; i++)
		{
			GetTweetsPerUser startFun = new GetTweetsPerUser(1, arr[i]);
			startFun.StartApp();
			startFun = null;
		}

		long endExecution = (new Long(System.currentTimeMillis())).longValue();
		long difference = (endExecution - startExecution) / 1000;
		System.out.println(myClassName + " finished at " + new Date().toString() + " The program has taken " + (difference / 60) + " minutes.");
	}

	/*************************************************************************
	 * StartApp
	 * @throws InterruptedException 
	 *************************************************************************/
	@SuppressWarnings("boxing")
	public void StartApp() throws IOException, InterruptedException
	{
		ReadCelebrities();
		ReadEvents();

		Vector<File> mFileVector = new Vector<>();
		File[] mFile = new File(HashDefinesForED.DataFolder).listFiles();
		for (int j = 0; j < mFile.length; j++)
		{
			mFileVector.add(mFile[j]);
		}
		System.out.println("StartApp : Files count = " + mFile.length);

		int First = 0;
		//int Last = First + 10;//mFileVector.size()
		int Last = mFileVector.size();

		for (int j = First; j < Last; j++)
		{
			ReadNextTopic(j, Last, mFileVector.get(j));
		}

		NumberFormat f = new DecimalFormat("#0.0");
		BufferedWriter out = new BufferedWriter(new FileWriter(gFileName, true));
		out.write("\n\n----------" + gUserType + "---------\n");
		out.write("C Total \t" + f.format(ds_Total_C.getMean()) + "\t" + f.format(ds_Total_NC.getMean()) + "\t" + f.format(ds_usersPercentage_Total.getMean()) + "\t"
				+ f.format(ds_TweetsPercentage_Total.getMean()) + "\n");
		out.write("C Growth\t" + f.format(ds_Growth_C.getMean()) + "\t" + f.format(ds_Growth_NC.getMean()) + "\t" + f.format(ds_usersPercentage_Growth.getMean()) + "\t"
				+ f.format(ds_TweetsPercentage_Growth.getMean()) + "\n");
		out.write("C Peak  \t" + f.format(ds_Peak_C.getMean()) + "\t" + f.format(ds_Peak_NC.getMean()) + "\t" + f.format(ds_usersPercentage_Peak.getMean()) + "\t"
				+ f.format(ds_TweetsPercentage_Peak.getMean()) + "\n");
		out.write("C Decay \t" + f.format(ds_Decay_C.getMean()) + "\t" + f.format(ds_Decay_NC.getMean()) + "\t" + f.format(ds_usersPercentage_Decay.getMean()) + "\t"
				+ f.format(ds_TweetsPercentage_Decay.getMean()) + "\n");

		out.close();
	}

	/***************************************************************
	* ReadPoliticians_2  
	***************************************************************/
	@SuppressWarnings("boxing")
	private static void ReadCelebrities() throws IOException
	{
		String st = "";
		System.out.println("GetSkewness.ReadPoliticians_2() " + HashDefinesForED.FName_Celebrities);
		BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(HashDefinesForED.FName_Celebrities)));
		br_i.readLine();
		Set_ManualCelebrites.clear();
		while((st = br_i.readLine()) != null)
		{
			String st1[] = st.split("\t");
			Set_ManualCelebrites.add(Long.parseLong(st1[1]));
		}
		br_i.close();
	}

	/***************************************************************
	* ReadEvents
	***************************************************************/
	private void ReadEvents() throws IOException
	{
		TM_Events.clear();
		String st = "";
		System.out.println("GetSkewness.ReadEvents()  " + (HashDefinesForED.EventsFolder + HashDefinesForED.FName_ClassifiedEvents));
		BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(HashDefinesForED.EventsFolder + HashDefinesForED.FName_ClassifiedEvents)));
		br_i.readLine();
		while((st = br_i.readLine()) != null)
		{
			String eventattr[] = st.split("\t");
			String topicName = eventattr[0];
			Long StartTime_Growth = Long.valueOf(eventattr[1]);
			Long StartTime_Peak = Long.valueOf(eventattr[2]);
			Long StartTime_Decay = Long.valueOf(eventattr[3]);
			Long EndTime_Event = Long.valueOf(eventattr[4]);
			String e = StartTime_Growth + Sep_group + StartTime_Peak + Sep_group + StartTime_Decay + Sep_group + EndTime_Event;
			String value = TM_Events.get(topicName);
			if (value == null)
			{
				TM_Events.put(topicName, e);
			}
			else
			{
				TM_Events.put(topicName, value + RE_Sep_record + e);
			}
		}
		br_i.close();
	}

	/*************************************************************************
	 * ReadNextTopic
	 *************************************************************************/
	@SuppressWarnings("boxing")
	private static boolean ReadNextTopic(int index, int tt, File mFile) throws NumberFormatException, IOException
	{
		gTopic = mFile.getName().substring(0, mFile.getName().length() - 4);
		if (TM_Events.get(gTopic) == null)
		{
			System.out.println("ReadNextTopic : Topic=" + gTopic + "  " + index + "/" + tt + " 0 events in the topic");
			return true;
		}
		if (gTopic.equals("9xmtop9"))
			return true;

		String[] events = TM_Events.get(gTopic).split(RE_Sep_record);

		int tweetsInEvent_C = 0;
		int tweetsInGrowthPhase_C = 0;
		int tweetsInPeakPhase_C = 0;
		int tweetsInDecayPhase_C = 0;
		Set<Long> Set_Growth_C = new HashSet<>();
		Set<Long> Set_Peak_C = new HashSet<>();
		Set<Long> Set_Decay_C = new HashSet<>();
		Set<Long> Set_Total_C = new HashSet<>();

		int tweetsInEvent_NC = 0;
		int tweetsInGrowthPhase_NC = 0;
		int tweetsInPeakPhase_NC = 0;
		int tweetsInDecayPhase_NC = 0;
		Set<Long> Set_Growth_NC = new HashSet<>();
		Set<Long> Set_Peak_NC = new HashSet<>();
		Set<Long> Set_Decay_NC = new HashSet<>();
		Set<Long> Set_Total_NC = new HashSet<>();

		boolean isPopularUser = false;

		//Load parameters of first event
		int eventIndex = 0;
		Long StartTime_Growth = Long.valueOf(events[eventIndex].split(RE_Sep_group)[0]);
		Long StartTime_Peak = Long.valueOf(events[eventIndex].split(RE_Sep_group)[1]);
		Long StartTime_Decay = Long.valueOf(events[eventIndex].split(RE_Sep_group)[2]);
		Long EndTime_Event = Long.valueOf(events[eventIndex].split(RE_Sep_group)[3]);

		System.out.println("ReadNextTopic : Topic=" + gTopic + "  " + index + "/" + tt + " EventsCount=" + events.length + "  EventIndex=" + eventIndex);

		String st = "";
		BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(mFile.getAbsolutePath())));
		while((st = br_i.readLine()) != null)
		{
			if (st.length() < 1)
				continue;

			String Tweet[] = st.split(RE_Sep_file, 2);
			String[] elements = Tweet[0].split(RE_Sep_group);
			long Time = Long.parseLong(elements[Index_Tweet_Time]);

			if (Time < StartTime_Growth)
			{
				continue;
			}

			if (Time >= EndTime_Event)
			{

				if (Set_Growth_C.size() > 0)
					ds_Growth_C.addValue(1.0 * tweetsInGrowthPhase_C / Set_Growth_C.size());

				if (Set_Peak_C.size() > 0)
					ds_Peak_C.addValue(1.0 * tweetsInPeakPhase_C / Set_Peak_C.size());

				if (Set_Decay_C.size() > 0)
					ds_Decay_C.addValue(1.0 * tweetsInDecayPhase_C / Set_Decay_C.size());

				if (Set_Total_C.size() > 0)
					ds_Total_C.addValue(1.0 * tweetsInEvent_C / Set_Total_C.size());

				if (Set_Growth_NC.size() > 0)
					ds_Growth_NC.addValue(1.0 * tweetsInGrowthPhase_NC / Set_Growth_NC.size());

				if (Set_Peak_NC.size() > 0)
					ds_Peak_NC.addValue(1.0 * tweetsInPeakPhase_NC / Set_Peak_NC.size());

				if (Set_Decay_NC.size() > 0)
					ds_Decay_NC.addValue(1.0 * tweetsInDecayPhase_NC / Set_Decay_NC.size());

				if (Set_Total_NC.size() > 0)
					ds_Total_NC.addValue(1.0 * tweetsInEvent_NC / Set_Total_NC.size());

				//--------------------------------Tweets

				if ((tweetsInGrowthPhase_C + tweetsInGrowthPhase_NC) > 0)
					ds_TweetsPercentage_Growth.addValue(100.0 * tweetsInGrowthPhase_C / (tweetsInGrowthPhase_C + tweetsInGrowthPhase_NC));

				if ((tweetsInPeakPhase_C + tweetsInPeakPhase_NC) > 0)
					ds_TweetsPercentage_Peak.addValue(100.0 * tweetsInPeakPhase_C / (tweetsInPeakPhase_C + tweetsInPeakPhase_NC));

				if ((tweetsInDecayPhase_C + tweetsInDecayPhase_NC) > 0)
					ds_TweetsPercentage_Decay.addValue(100.0 * tweetsInDecayPhase_C / (tweetsInDecayPhase_C + tweetsInDecayPhase_NC));

				if ((tweetsInEvent_C + tweetsInEvent_NC) > 0)
					ds_TweetsPercentage_Total.addValue(100.0 * tweetsInEvent_C / (tweetsInEvent_C + tweetsInEvent_NC));

				//--------------------------------users
				if ((Set_Total_C.size() + Set_Total_NC.size()) > 0)
					ds_usersPercentage_Total.addValue(100.0 * Set_Total_C.size() / (Set_Total_C.size() + Set_Total_NC.size()));

				if ((Set_Growth_C.size() + Set_Growth_NC.size()) > 0)
					ds_usersPercentage_Growth.addValue(100.0 * Set_Growth_C.size() / (Set_Growth_C.size() + Set_Growth_NC.size()));

				if ((Set_Peak_C.size() + Set_Peak_NC.size()) > 0)
					ds_usersPercentage_Peak.addValue(100.0 * Set_Peak_C.size() / (Set_Peak_C.size() + Set_Peak_NC.size()));

				if ((Set_Decay_C.size() + Set_Decay_NC.size()) > 0)
					ds_usersPercentage_Decay.addValue(100.0 * Set_Decay_C.size() / (Set_Decay_C.size() + Set_Decay_NC.size()));

				eventIndex++;
				if (eventIndex == events.length)
				{
					//		System.out.println("Reading events completed at index=" + (eventIndex - 1));
					break;
				}
				//	System.out.println("  EventIndex=" + eventIndex);

				StartTime_Growth = Long.valueOf(events[eventIndex].split(RE_Sep_group)[0]);
				StartTime_Peak = Long.valueOf(events[eventIndex].split(RE_Sep_group)[1]);
				StartTime_Decay = Long.valueOf(events[eventIndex].split(RE_Sep_group)[2]);
				EndTime_Event = Long.valueOf(events[eventIndex].split(RE_Sep_group)[3]);

				tweetsInEvent_C = 0;
				tweetsInGrowthPhase_C = 0;
				tweetsInPeakPhase_C = 0;
				tweetsInDecayPhase_C = 0;
				Set_Total_C.clear();
				Set_Growth_C.clear();
				Set_Peak_C.clear();
				Set_Decay_C.clear();

				tweetsInEvent_NC = 0;
				tweetsInGrowthPhase_NC = 0;
				tweetsInPeakPhase_NC = 0;
				tweetsInDecayPhase_NC = 0;
				Set_Total_NC.clear();
				Set_Growth_NC.clear();
				Set_Peak_NC.clear();
				Set_Decay_NC.clear();

				continue;
			}

			String User_Current[] = elements[Index_Tweet_User].split(RE_Sep_record);
			Long Uid = Long.parseLong(User_Current[Index_User_ID]);
			int followersCount = Integer.parseInt(User_Current[Index_User_Followers]);
			int isVerified = Integer.parseInt(User_Current[Index_User_isVerified]);
			if ((followersCount > CelebrityThreshold) || (isVerified == 1) || (Set_ManualCelebrites.contains(Uid)))
			{
				isPopularUser = true;
				tweetsInEvent_C++;
				Set_Total_C.add(Uid);
				if ((Time >= StartTime_Growth) && (Time < StartTime_Peak))
				{
					tweetsInGrowthPhase_C++;
					Set_Growth_C.add(Uid);
				}
				else if ((Time >= StartTime_Peak) && (Time < StartTime_Decay))
				{
					tweetsInPeakPhase_C++;
					Set_Peak_C.add(Uid);
				}
				else if ((Time >= StartTime_Decay) && (Time <= EndTime_Event))
				{
					tweetsInDecayPhase_C++;
					Set_Decay_C.add(Uid);
				}
			}
			else
			{
				isPopularUser = false;
				tweetsInEvent_NC++;
				Set_Total_NC.add(Uid);
				if ((Time >= StartTime_Growth) && (Time < StartTime_Peak))
				{
					tweetsInGrowthPhase_NC++;
					Set_Growth_NC.add(Uid);
				}
				else if ((Time >= StartTime_Peak) && (Time < StartTime_Decay))
				{
					tweetsInPeakPhase_NC++;
					Set_Peak_NC.add(Uid);
				}
				else if ((Time >= StartTime_Decay) && (Time <= EndTime_Event))
				{
					tweetsInDecayPhase_NC++;
					Set_Decay_NC.add(Uid);
				}
			}
		}
		br_i.close();
		return true;
	}
}