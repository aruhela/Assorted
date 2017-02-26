/*******************
Author  : Amit Ruhela
Purpose :

*******************/
package Elsevier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

public class GetUserInfluence
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
	static Set<Long> Set_ManualCelebrites = new HashSet<>();
	static TreeMap<String, String> TM_Events = new TreeMap<>();
	static TreeMap<Long, Double> TM_UserInfl = new TreeMap<>();

	/***************************************************************
	 * MainFile
	 ***************************************************************/
	GetUserInfluence(int cthresh, String mUserType)
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
		//CelebrityThreshold = 3000;
		System.out.println("CelebrityThreshold=" + CelebrityThreshold + "\t" + "CelebrityPercentile=" + CelebrityPercentile);
		HashDefinesForED.SetUserType(mUserType, CelebrityPercentile);

		Set_ManualCelebrites.clear();
	}

	/***************************************************************
	 * MainFile
	 * @throws InterruptedException 
	 ***************************************************************/
	public static void main(String[] args) throws IOException, InterruptedException
	{
		String myClassName = Thread.currentThread().getStackTrace()[1].getClassName();
		long startExecution = (new Long(System.currentTimeMillis())).longValue();

		ReadUserInfluence();

		String[] arr = { "S", "P", "B" };
		for (int i = 0; i < arr.length; i++)
		{
			GetUserInfluence startFun = new GetUserInfluence(1, arr[i]);
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
	public void StartApp() throws IOException, InterruptedException
	{
		ReadEvents();
		ReadPoliticians_2();

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

		BufferedWriter br_o = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_UserInfVsTweets));
		br_o.write("#Influence" + "\t" + "FollCount" + "\t" + "tweets_Growth" + "\t" + "tweets_Total" + "\t" + "growthTimeInMinutes" + "\t" + "lifeOfEvent" + "\t"
				+ "CelebCount" + "\n");
		br_o.close();

		for (int j = First; j < Last; j++)
		{
			ReadNextTopic(j, Last, mFileVector.get(j));
		}

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

		String[] events = TM_Events.get(gTopic).split(RE_Sep_record);
		//System.out.println(events[0]);
		int tweets_Total = 0;
		int tweets_Growth = 0;
		Long follCount = 0l;
		int celebCount = 0;
		int eventIndex = 0;
		double uInf = 0.0;
		long StartTime = Long.parseLong(events[0].split(RE_Sep_group)[0]);
		long endTime_Growth = Long.parseLong(events[0].split(RE_Sep_group)[1]);
		long endTime_Event = Long.parseLong(events[0].split(RE_Sep_group)[3]);
		int lifeOfEvent = (int) ((endTime_Event - StartTime) / (1000 * 60));

		String st = "";
		BufferedWriter br_o = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_UserInfVsTweets, true));
		BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(mFile.getAbsolutePath())));
		while((st = br_i.readLine()) != null)
		{
			if (st.length() < 1)
				continue;

			String Tweet[] = st.split(RE_Sep_file, 2);
			String[] elements = Tweet[0].split(RE_Sep_group);
			long Time = Long.parseLong(elements[Index_Tweet_Time]);
			int followersCount = Integer.parseInt(elements[Index_Tweet_User].split(RE_Sep_record)[Index_User_Followers]);
			int isVerified = Integer.parseInt(elements[Index_Tweet_User].split(RE_Sep_record)[Index_User_isVerified]);

			if (Time < StartTime)
			{
				continue;
			}

			if (Time >= endTime_Event)
			{
				int growthTimeInMinutes = Math.round((endTime_Growth - StartTime) / (1000 * 60));
				br_o.write(uInf + "\t" + (follCount / tweets_Growth) + "\t" + tweets_Growth + "\t" + tweets_Total + "\t" + growthTimeInMinutes + "\t" + lifeOfEvent
						+ "\t" + celebCount + "\n");
				eventIndex++;
				if (eventIndex == events.length)
				{
					System.out.println(index + "/" + tt + " " + gTopic);
					break;
				}

				tweets_Growth = 0;
				tweets_Total = 0;
				uInf = 0;
				follCount = 0l;
				celebCount = 0;

				//System.out.println("  EventIndex=" + eventIndex);
				StartTime = Long.parseLong(events[eventIndex].split(RE_Sep_group)[0]);
				endTime_Growth = Long.parseLong(events[eventIndex].split(RE_Sep_group)[1]);
				endTime_Event = Long.parseLong(events[eventIndex].split(RE_Sep_group)[3]);
				lifeOfEvent = (int) ((endTime_Event - StartTime) / (1000 * 60));
				continue;
			}
			tweets_Total++;
			if ((Time > StartTime) && (Time <= endTime_Growth))
			{
				tweets_Growth++;
				String User_Current[] = elements[Index_Tweet_User].split(RE_Sep_record);
				Long Uid = Long.parseLong(User_Current[Index_User_ID]);
				uInf += TM_UserInfl.get(Uid);
				follCount += followersCount;
				if ((followersCount > CelebrityThreshold) || (isVerified == 1) || (Set_ManualCelebrites.contains(Uid)))
				{
					celebCount++;
				}
			}
		}
		br_i.close();
		br_o.close();
		return true;
	}

	/***************************************************************
	* ReadPoliticians_2  
	***************************************************************/
	@SuppressWarnings("boxing")
	private static void ReadPoliticians_2() throws IOException
	{
		String st = "";
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

	/***************************************************************
	* ReadPoliticians_2  
	***************************************************************/
	@SuppressWarnings({ "boxing", "unused" })
	private static void ReadUserInfluence() throws IOException
	{
		String st = "";
		BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(HashDefinesForED.MainDir + "Data/UserWeights.txt")));
		while((st = br_i.readLine()) != null)
		{
			String st1[] = st.split("\t");
			Long Uid = Long.valueOf(st1[0]);

			Integer followerscount = Integer.parseInt(st1[1]);
			Integer Tweets = Integer.parseInt(st1[2]);
			Integer Retweets = Integer.parseInt(st1[3]);
			double ratio = 0.0;
			if (Tweets > 0)
			{
				//ratio = Retweets / (followerscount * Tweets); will not be required as you have to multiply again in above processing
				ratio = Retweets / (Tweets);
			}
			TM_UserInfl.put(Uid, ratio);
		}
		br_i.close();
	}
}