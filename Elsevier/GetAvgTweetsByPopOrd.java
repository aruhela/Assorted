/*******************
Author  : Amit Ruhela
Purpose : Find how many tweets are posted by Celebrity and Non-Celebrity users during the events.

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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class GetAvgTweetsByPopOrd
{

	static String gTopic = "";
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

	// Regular Expression
	public static final String RE_Sep_file = "\\-<\\+>\\-";// File
	public static final String Sep_group = "\t";// Group
	public static final String RE_Sep_group = "\\t";// Group
	public static final String RE_Sep_record = ";";// Record

	static int CelebrityThreshold = 0;
	public static double CelebrityPercentile = 0;
	public static String gUserType = "";

	static boolean AllUsers = false;
	//static int Start_Percentile = 40;
	//static int End_Percentile = 50 + (50 - Start_Percentile);
	static int Start_Percentile = 70;
	static int End_Percentile = 95;

	static int Followers_Start_Value = 0;
	static int Followers_End_Value = 0;

	static TreeMap<Long, Long> TM_LifeTime_C = new TreeMap<>();
	static TreeMap<Long, Long> TM_LifeTime_NC = new TreeMap<>();

	static TreeMap<String, String> TM_Events = new TreeMap<>();

	static DescriptiveStatistics ds_event = new DescriptiveStatistics();
	static DescriptiveStatistics ds_C = new DescriptiveStatistics();
	static DescriptiveStatistics ds_NC = new DescriptiveStatistics();
	
	static DescriptiveStatistics bool_ds_C = new DescriptiveStatistics();
	static DescriptiveStatistics bool_ds_NC = new DescriptiveStatistics();

	static NumberFormat f = new DecimalFormat("#0.00");

	/***************************************************************
	 * MainFile
	 ***************************************************************/
	GetAvgTweetsByPopOrd(int cthresh, String mUserType)
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
		System.out.println("\n\nCelebrityThreshold=" + CelebrityThreshold + "\t" + "CelebrityPercentile=" + CelebrityPercentile);
		HashDefinesForED.SetUserType(mUserType, CelebrityPercentile);
		TM_LifeTime_C.clear();
		TM_LifeTime_NC.clear();

		TM_Events.clear();
	}

	/***************************************************************
	 * MainFile
	 * 
	 * @throws InterruptedException
	 ***************************************************************/
	public static void main(String[] args) throws IOException, InterruptedException
	{
		String myClassName = Thread.currentThread().getStackTrace()[1].getClassName();
		long startExecution = (new Long(System.currentTimeMillis())).longValue();

		String[] iii = { /*"B", "S",*/"P" };
		for (int i = 0; i < iii.length; i++)
		{
			GetAvgTweetsByPopOrd startFun = new GetAvgTweetsByPopOrd(2, iii[i]);
			startFun.StartApp();
		}

		System.out.println("Celeb    Avg tweet = " + ds_C.getMean() + " , Median = " + ds_C.getPercentile(50));
		System.out.println("NonCeleb Avg tweet = " + ds_NC.getMean() + " , Median = " + ds_NC.getPercentile(50));
		System.out.println("Events   Avg Size  = " + ds_event.getMean() + " , Median = " + ds_event.getPercentile(50));

		System.out.println(" bool_ds_C = " + bool_ds_C.getSum() );
		System.out.println(" bool_ds_NC = " + bool_ds_NC.getSum() );
		
		long endExecution = (new Long(System.currentTimeMillis())).longValue();
		long difference = (endExecution - startExecution) / 1000;
		System.out.println(myClassName + " finished at " + new Date().toString() + " The program has taken " + (difference / 60) + " minutes.");
	}

	//	Avg tweet by Celeb = 4.320885241068701 median = 3.5
	//	Avg tweet by Non Celeb = 8.75937512072102 median = 3.778036702592485
	//	Avg Size of event = 25016.666666666682 median = 14464.0

	// All users = true
	//	Celeb    Avg tweet = 4.320885241068701 , Median = 3.5
	//	NonCeleb Avg tweet = 10.10697153200288 , Median = 4.5105711147721035
	//	Events   Avg Size  = 25016.666666666682 , Median = 14464.0
	/*************************************************************************
	 * StartApp
	 * 
	 * @throws InterruptedException
	 *************************************************************************/
	public void StartApp() throws IOException, InterruptedException
	{
		ReadEvents();
		ReadUsersPercentile();

		BufferedWriter Out_C = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + "../EventsTweetsByPopOrd.txt"));
		Out_C.write("#Topic" + "\t" + "EID" + "\t" + "Tweets" + "\t" + "CelebTweets" + "\t" + "CelebCount" + "\t" + "OrdinaryTweets" + "\t" + "OrdinaryCount" + "\n");
		Out_C.close();

		Vector<File> mFileVector = new Vector<>();
		File[] mFile = new File(HashDefinesForED.DataFolder).listFiles();
		for (int j = 0; j < mFile.length; j++)
		{
			mFileVector.add(mFile[j]);
		}
		System.out.println("StartApp : Files count = " + mFile.length);

		int First = 0;
		// int Last = First + 10;//mFileVector.size()
		int Last = mFileVector.size();

		for (int j = First; j < Last; j++)
		{
			ReadNextTopic(j, Last, mFileVector.get(j));
		}

	}

	/***************************************************************
	 * ReadEvents
	 * For each topic, Get what events were there and when they happened
	 ***************************************************************/
	private void ReadEvents() throws IOException
	{
		String st = "";
		TM_Events.clear();
		BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(HashDefinesForED.EventsFolder + HashDefinesForED.FName_ClassifiedEvents)));
		br_i.readLine();
		while((st = br_i.readLine()) != null)
		{
			String eventattr[] = st.split("\t");
			String topicName = eventattr[0];
			Long StartTime = Long.valueOf(eventattr[1]);
			Long EndTime = Long.valueOf(eventattr[4]);
			String value = TM_Events.get(topicName);
			if (value == null)
			{
				TM_Events.put(topicName, StartTime + Sep_group + EndTime);
			}
			else
			{
				TM_Events.put(topicName, value + RE_Sep_record + StartTime + Sep_group + EndTime);
			}
		}
		br_i.close();
	}

	/*************************************************************************
	 * ReadNextTopic
	 *************************************************************************/
	private static boolean ReadNextTopic(int index, int tt, File mFile) throws NumberFormatException, IOException
	{
		gTopic = mFile.getName().substring(0, mFile.getName().length() - 4);
		System.out.println("ReadNextTopic : Topic=" + "" + "  " + index + "/" + tt);

		if (TM_Events.get(gTopic) == null)
		{
			//System.out.println("ReadNextTopic : Topic=" + gTopic + "  " + index + "/" + tt + " 0 events");
			return true;
		}

		String[] events = TM_Events.get(gTopic).split(RE_Sep_record);

		int eventIndex = 0;
		long eStrt = Long.parseLong(events[0].split(RE_Sep_group)[0]);
		long eEnd = Long.parseLong(events[0].split(RE_Sep_group)[1]);
		int MaxEvents = events.length;

		String st = "";
		long Time;
		int followersCount = 0;
		Long uid = null;

		int tweetsInEvent = 0;
		int CelebTweets = 0;
		int NonCelebTweets = 0;

		boolean isOrdinaryUser = false;

		Set<Long> Celeb_Set = new TreeSet<>();
		Set<Long> NonCeleb_Set = new TreeSet<>();

		BufferedWriter Out_C = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + "../EventsTweetsByPopOrd.txt", true));

		BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(mFile.getAbsolutePath())));
		st = br_i.readLine();
		while(true)
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
				if (followersCount > CelebrityThreshold)
				{
					CelebTweets++;
					Celeb_Set.add(uid);
				}
				else
				{
					if (AllUsers)
					{
						isOrdinaryUser = true;
					}
					else
					{
						if ((followersCount >= Followers_Start_Value) && (followersCount <= Followers_End_Value))
							isOrdinaryUser = true;
						else
							isOrdinaryUser = false;
					}

					if (isOrdinaryUser)
					{
						NonCelebTweets++;
						NonCeleb_Set.add(uid);
					}
				}
			}
			else if (Time > eEnd)
			{
				if (Celeb_Set.size() > 0)
				{
					Out_C.write(gTopic + "\t" + eventIndex + "\t" + tweetsInEvent + "\t" + CelebTweets + "\t" + Celeb_Set.size() + "\t" + NonCelebTweets + "\t"
							+ NonCeleb_Set.size() + "\t" + f.format(1.0 * CelebTweets / Celeb_Set.size()) + "\t" + f.format(1.0 * NonCelebTweets / NonCeleb_Set.size())
							+ "\n");

					ds_event.addValue(1.0 * tweetsInEvent);
					ds_C.addValue(1.0 * CelebTweets / Celeb_Set.size());
					ds_NC.addValue(1.0 * NonCelebTweets / NonCeleb_Set.size());
					
					if((1.0 * CelebTweets / Celeb_Set.size())>(1.0 * NonCelebTweets / NonCeleb_Set.size()))
						bool_ds_C.addValue(1);
					else
						bool_ds_NC.addValue(1);
					
				}

				// ---------------- Clear DS--------------
				Celeb_Set.clear();
				NonCeleb_Set.clear();

				CelebTweets = 0;
				NonCelebTweets = 0;

				tweetsInEvent = 0;

				// -- Read Next event
				eventIndex++;
				if (eventIndex < MaxEvents)
				{
					eStrt = Long.parseLong(events[eventIndex].split(RE_Sep_group)[0]);
					eEnd = Long.parseLong(events[eventIndex].split(RE_Sep_group)[1]);
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
		return true;
	}

	/*************************************************************************
	 * ReadUsersPercentile :
	 Find the followers count of this type of user.
	 *************************************************************************/
	public static void ReadUsersPercentile() throws IOException
	{
		String FName = "G:/MyWork/Output/Misc/FollowersDistribution" + gUserType + ".txt";
		System.out.println("gUserType=" + gUserType + " Fname=" + FName);

		double percentile[] = { Start_Percentile, End_Percentile };
		boolean[] percentileFlag = new boolean[percentile.length];
		for (int j = 1; j < percentileFlag.length; j++)
		{
			percentileFlag[j] = false;
		}

		int x[] = new int[percentile.length];

		String st = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(FName)));
		br.readLine();
		int followersCount = 0;
		while((st = br.readLine()) != null)
		{
			String arr[] = st.split("\\t");
			followersCount = Integer.parseInt(arr[0]);
			double lPercentile = Double.parseDouble(arr[2]);

			for (int j = 0; j < percentileFlag.length; j++)
			{
				if ((percentileFlag[j] == false) && (lPercentile > percentile[j]))
				{
					percentileFlag[j] = true;
					x[j] = followersCount;
				}
			}
		}
		br.close();

		Followers_Start_Value = x[0];
		Followers_End_Value = x[1] - 1;

		System.out.println("Followers_Start_Value[" + Start_Percentile + "]=" + Followers_Start_Value + " Followers_End_Value[" + End_Percentile + "]="
				+ Followers_End_Value);
	}

}