/*******************
Author  : Amit Ruhela
Purpose : This class find the count of events in which the celebrity and non celebrity users have participated. 
Basically give whether celebrity and non celebrity users are interested in diverse contents on Twitter.

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
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

public class GetCelebritiesParticipationAcrossEvents
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

	static int CelebrityThreshold = 0;
	public static double CelebrityPercentile = 0;
	static Set<Long> Set_ManualCelebrites = new HashSet<>();
	static TreeMap<Long, Integer> CelebrityInvolvement = new TreeMap<>();
	static TreeMap<Long, Integer> NonCelebrityInvolvement = new TreeMap<>();

	static TreeMap<String, String> TM_Events = new TreeMap<>();

	/***************************************************************
	 * MainFile
	 ***************************************************************/
	GetCelebritiesParticipationAcrossEvents(int cthresh, String mUserType)
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
		System.out.println("CelebrityThreshold=" + CelebrityThreshold + "\t" + "CelebrityPercentile=" + CelebrityPercentile);
		HashDefinesForED.SetUserType(mUserType, CelebrityPercentile);
		CelebrityInvolvement.clear();
		NonCelebrityInvolvement.clear();
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

		GetCelebritiesParticipationAcrossEvents startFun = new GetCelebritiesParticipationAcrossEvents(1, "S");
		startFun.StartApp();

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

		BufferedWriter br_o = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_SubsequentInvolvementAcrossEvent));
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
		String st = "";
		TreeMap<Long, Integer> TM_C = new TreeMap<>();
		TreeMap<Long, Integer> TM_NC = new TreeMap<>();

		Set<Long> Set_C = new HashSet<>();
		Set<Long> Set_NC = new HashSet<>();

		gTopic = mFile.getName().substring(0, mFile.getName().length() - 4);

		if (TM_Events.get(gTopic) == null)
		{
			System.out.println("ReadNextTopic : Topic=" + gTopic + "  " + index + "/" + tt + " 0 events");
			return true;
		}

		String[] events = TM_Events.get(gTopic).split(RE_Sep_record);
		if (events.length <= 1)
		{
			System.out.println("ReadNextTopic : Topic=" + gTopic + "  " + index + "/" + tt + " 1 events");
			return true;
		}

		BufferedWriter br_o = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_SubsequentInvolvementAcrossEvent, true));

		int tweets = 0;
		int eventIndex = 0;
		long StartTime = Long.parseLong(events[0].split(RE_Sep_group)[0]);
		long endTime = Long.parseLong(events[0].split(RE_Sep_group)[1]);
		System.out.println("ReadNextTopic : Topic=" + gTopic + "  " + index + "/" + tt + " " + events.length + " events");

		System.out.println("  EventIndex=" + eventIndex);

		//Compute StartTime and EndTime		
		BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(mFile.getAbsolutePath())));
		while((st = br_i.readLine()) != null)
		{
			if (st.length() < 1)
				continue;

			String Tweet[] = st.split(RE_Sep_file, 2);
			String[] elements = Tweet[0].split(RE_Sep_group);
			long Time = Long.parseLong(elements[Index_Tweet_Time]);

			if (Time < StartTime)
			{
				continue;
			}

			if (Time >= endTime)
			{
				System.out.println("    Tweets["+eventIndex+"]="+tweets);
				tweets=0;
				eventIndex++;
				Iterator<Long> Iter = Set_C.iterator();
				while(Iter.hasNext())
				{
					Long user = Iter.next();
					Integer Count = TM_C.get(user);
					if (Count == null)
					{
						TM_C.put(user, 1);
					}
					else
					{
						TM_C.put(user, 1 + Count);
					}
				}

				Iter = Set_NC.iterator();
				while(Iter.hasNext())
				{
					Long user = Iter.next();
					Integer Count = TM_NC.get(user);
					if (Count == null)
					{
						TM_NC.put(user, 1);
					}
					else
					{
						TM_NC.put(user, 1 + Count);
					}
				}
				Set_C.clear();
				Set_NC.clear();

				if (eventIndex == events.length)
				{
					System.out.println("Reading events completed at index=" + (eventIndex - 1));
					break;
				}
				System.out.println("  EventIndex=" + eventIndex);
				StartTime = Long.parseLong(events[eventIndex].split(RE_Sep_group)[0]);
				endTime = Long.parseLong(events[eventIndex].split(RE_Sep_group)[1]);
				continue;
			}

			tweets++;
			String User_Current[] = elements[Index_Tweet_User].split(RE_Sep_record);
			Long Uid = Long.parseLong(User_Current[Index_User_ID]);
			int followersCount = Integer.parseInt(User_Current[Index_User_Followers]);
			int isVerified = Integer.parseInt(User_Current[Index_User_isVerified]);

			if ((followersCount > CelebrityThreshold) || (isVerified == 1) || (Set_ManualCelebrites.contains(Uid)))
			{
				Set_C.add(Uid);
			}
			else
			{
				Set_NC.add(Uid);
			}
		}
		br_i.close();

		StringBuffer stbuf = new StringBuffer();
		stbuf.append(gTopic + "\t");
		stbuf.append(events.length + "\t");
		stbuf.append(TM_C.size() + "\t");
		stbuf.append(TM_NC.size() + "\t");

		int[] part_C = new int[events.length];
		int[] part_NC = new int[events.length];

		Iterator<Integer> Iter = TM_C.values().iterator();
		while(Iter.hasNext())
		{
			part_C[Iter.next() - 1]++;
		}

		Iter = TM_NC.values().iterator();
		while(Iter.hasNext())
		{
			part_NC[Iter.next() - 1]++;
		}

		for (int i = 0; i < part_NC.length; i++)
		{
			stbuf.append(part_C[i] + "," + part_NC[i] + "\t");
		}

		br_o.write(stbuf.toString().trim() + "\n");
		System.out.println(stbuf.toString().trim() + "\n");
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
		String st = "";
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

}