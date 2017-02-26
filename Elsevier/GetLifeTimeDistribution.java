/*******************
Author  : Amit Ruhela
Purpose : Time spent by users in the events : We find whether there is a difference in the 
degree to which popular and ordinary users engage with an event. We find the time difference
between the first time and the last time a user tweets throughout the event lifetime, and
compare the distribution for these time differences between the sets of popular and ordinary users

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
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class GetLifeTimeDistribution
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

	/***************************************************************
	 * MainFile
	 ***************************************************************/
	GetLifeTimeDistribution(int cthresh, String mUserType)
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

		String[] iii = { "B", "S", "P" };
		for (int i = 0; i < iii.length; i++)
		{
			GetLifeTimeDistribution startFun = new GetLifeTimeDistribution(2, iii[i]);
			startFun.StartApp();
		}

		long endExecution = (new Long(System.currentTimeMillis())).longValue();
		long difference = (endExecution - startExecution) / 1000;
		System.out.println(myClassName + " finished at " + new Date().toString() + " The program has taken " + (difference / 60) + " minutes.");
	}

	/*************************************************************************
	 * StartApp
	 * 
	 * @throws InterruptedException
	 *************************************************************************/
	public void StartApp() throws IOException, InterruptedException
	{
		ReadEvents();
		ReadUsersPercentile();

		BufferedWriter Out_C = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + "../LifeTime_C.txt"));
		BufferedWriter Out_NC = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + "../LifeTime_NC.txt"));
		Out_C.write("#Topic" + "\t" + "EID" + "\t" + "Tweets" + "\t" + "NonCelebTweets" + "\t" + "NonCelebCount" + "\t" + "mean_count" + "\t" + "mean_time" + "\t"
				+ "EventLifeTime" + "\n");
		Out_NC.write("#Topic" + "\t" + "EID" + "\t" + "Tweets" + "\t" + "NonCelebTweets" + "\t" + "NonCelebCount" + "\t" + "mean_count" + "\t" + "mean_time" + "\t"
				+ "EventLifeTime" + "\n");
		Out_C.close();
		Out_NC.close();

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

		WriteCDF("LifeTime_C", TM_LifeTime_C);
		WriteCDF("LifeTime_NC", TM_LifeTime_NC);
		WriteArrowInformation();

	}

	/***************************************************************
	 * ReadEvents
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
	@SuppressWarnings("boxing")
	private static boolean ReadNextTopic(int index, int tt, File mFile) throws NumberFormatException, IOException
	{
		gTopic = mFile.getName().substring(0, mFile.getName().length() - 4);
		if (TM_Events.get(gTopic) == null)
		{
			// System.out.println("ReadNextTopic : Topic=" + gTopic + "  " + index + "/" + tt + " 0 events");
			return true;
		}

		String[] events = TM_Events.get(gTopic).split(RE_Sep_record);

		int eventIndex = 0;
		long eStrt = Long.parseLong(events[0].split(RE_Sep_group)[0]);
		long eEnd = Long.parseLong(events[0].split(RE_Sep_group)[1]);
		long eventLIfe = eEnd - eStrt + 1;
		int MaxEvents = events.length;

		String st = "";
		long Time;
		int followersCount = 0;
		Long uid = null;

		int tweetsInEvent = 0;
		int CelebTweets = 0;
		int NonCelebTweets = 0;

		boolean isOrdinaryUser = false;

		NumberFormat formatter = new DecimalFormat("#0.00");

		TreeMap<Long, String> Celeb_Tree = new TreeMap<>();
		TreeMap<Long, String> NonCeleb_Tree = new TreeMap<>();

		BufferedWriter Out_C = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + "../LifeTime_C.txt", true));
		BufferedWriter Out_NC = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + "../LifeTime_NC.txt", true));

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
					String value = Celeb_Tree.get(uid);
					if (value == null)
					{
						Celeb_Tree.put(uid, followersCount + " " + 1 + " " + Time);
					}
					else
					{
						String fCount = value.split("\\s+")[0];
						int count = Integer.parseInt(value.split("\\s+")[1]);
						String StartTime = value.split("\\s+")[2];
						Celeb_Tree.put(uid, fCount + " " + (count + 1) + " " + StartTime + " " + Time);
					}
					CelebTweets++;
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
						String value = NonCeleb_Tree.get(uid);
						// System.out.println(value);
						if (value == null)
						{
							NonCeleb_Tree.put(uid, followersCount + " " + 1 + " " + Time);
						}
						else
						{
							String fCount = value.split("\\s+")[0];
							int count = Integer.parseInt(value.split("\\s+")[1]);
							String StartTime = value.split("\\s+")[2];
							NonCeleb_Tree.put(uid, fCount + " " + (count + 1) + " " + StartTime + " " + Time);
						}
						NonCelebTweets++;
					}
				}
			}
			else if (Time > eEnd)
			{
				// ----------- Find Time between first and last tweet of celeb and Tweets by celebs
				DescriptiveStatistics ds_Count = new DescriptiveStatistics();
				DescriptiveStatistics ds_Time = new DescriptiveStatistics(); // Time diff between first and last tweet
				long time = 0;

				// Celeb Tree
				Iterator<Long> iter = Celeb_Tree.keySet().iterator();
				while(iter.hasNext())
				{
					Long Uid = iter.next();
					String value[] = Celeb_Tree.get(Uid).split("\\s+");
					int count = Integer.parseInt(value[1]);
					if (count == 1)
					{
						time = 0;
					}
					else
					{
						time = Long.parseLong(value[3]) - Long.parseLong(value[2]);
					}

					Long x = TM_LifeTime_C.get(time);
					if (x == null)
					{
						TM_LifeTime_C.put(time, 1l);
					}
					else
					{
						TM_LifeTime_C.put(time, 1 + x);
					}

					ds_Count.addValue(count);
					ds_Time.addValue(time);
				}

				Double mean_count = ds_Count.getMean();
				Double mean_time = ds_Time.getMean();
				if ((mean_count.isNaN()) || (mean_time.isNaN()))
				{
					mean_count = 0d;
					mean_time = 0d;
				}
				else
				{
					Out_C.write(gTopic + "\t" + eventIndex + "\t" + tweetsInEvent + "\t" + CelebTweets + "\t" + Celeb_Tree.size() + "\t" + formatter.format(mean_count)
							+ "\t" + formatter.format(mean_time) + "\t" + eventLIfe + "\n");
				}
				// ----------- Find Time between first and last tweet of non-celeb and Tweets by non-celebs
				ds_Count.clear();
				ds_Time.clear(); // Time diff between first and last tweet
				iter = NonCeleb_Tree.keySet().iterator();
				while(iter.hasNext())
				{
					Long Uid = iter.next();
					String value[] = NonCeleb_Tree.get(Uid).split("\\s+");
					int count = Integer.parseInt(value[1]);

					if (count == 1)
					{
						time = 0;
					}
					else
					{
						time = Long.parseLong(value[3]) - Long.parseLong(value[2]);
					}

					Long x = TM_LifeTime_NC.get(time);
					if (x == null)
					{
						TM_LifeTime_NC.put(time, 1l);
					}
					else
					{
						TM_LifeTime_NC.put(time, 1 + x);
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
				Out_NC.write(gTopic + "\t" + eventIndex + "\t" + tweetsInEvent + "\t" + NonCelebTweets + "\t" + NonCeleb_Tree.size() + "\t"
						+ formatter.format(mean_count) + "\t" + formatter.format(mean_time) + "\t" + eventLIfe + "\n");

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
					eStrt = Long.parseLong(events[eventIndex].split(RE_Sep_group)[0]);
					eEnd = Long.parseLong(events[eventIndex].split(RE_Sep_group)[1]);
					eventLIfe = eEnd - eStrt + 1;
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

		return true;
	}

	/***************************************************************
	 * WriteTimeLagBefore
	 ***************************************************************/
	@SuppressWarnings("boxing")
	private void WriteCDF(String fName, TreeMap<Long, Long> mTM) throws IOException
	{
		String OutDir = HashDefinesForED.MainDir+"Output/Plots_9990/LifeTime/";
		String FileName = "CDF_" + gUserType + "_" + fName + ".txt";
		BufferedWriter out = new BufferedWriter(new FileWriter(OutDir + FileName));
		out.write("#LifeTime" + "\tCDF_" + gUserType + "\n");

		// Get Sum of elements
		long total = 0l;
		Iterator<Long> mIter = mTM.values().iterator();
		while(mIter.hasNext())
		{
			total += mIter.next();
		}
		System.out.println(FileName + "  total=" + total);
		long talkedOnce = mTM.get(0l);
		System.out.println("Users having talked only once = " + (100.0 * talkedOnce / total));

		total -= talkedOnce;
		mTM.remove(0l);

		// Write CDF
		long sum = 0l;
		Iterator<Long> mIter2 = mTM.keySet().iterator();
		double prevCDF = 0.0;
		while(mIter2.hasNext())
		{
			long key = mIter2.next();
			long pdf = mTM.get(key);
			sum += pdf;
			double cdf = 1.0 * sum / total;
			if ((cdf - prevCDF) > 0.01)
			{
				out.write((key / 1000) + "\t" + cdf + "\n");// to convert in seconds
				prevCDF = cdf;
			}
		}
		out.close();
	}

	/***************************************************************
	 * WriteArrowInformation
	 ***************************************************************/
	private void WriteArrowInformation() throws NumberFormatException, IOException
	{
		String st = "";
		String fName_C = "LifeTime_C";
		String fName_NC = "LifeTime_NC";

		double[] p = { 0.1, 0.3, 0.5, 0.8 };
		boolean[] flag_p = new boolean[p.length];
		double[] x_celeb = new double[p.length];
		double[] x_nonceleb = new double[p.length];
		double[] diff = new double[p.length];

		// ---------------- Celebrities -----------------------
		for (int i = 0; i < flag_p.length; i++)
		{
			flag_p[i] = false;
		}
		String OutDir = HashDefinesForED.MainDir + "Output/Plots_9990/LifeTime/";
		String FileName = "CDF_" + gUserType + "_" + fName_C + ".txt";
		BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(OutDir + FileName)));
		br_i.readLine();
		while((st = br_i.readLine()) != null)
		{
			String arr[] = st.split("\t");
			int time = Integer.parseInt(arr[0]);
			double cdf = Double.parseDouble(arr[1]);
			for (int i = 0; i < p.length; i++)
			{
				if ((cdf > p[i]) && (flag_p[i] == false))
				{
					x_celeb[i] = time / 60;
					flag_p[i] = true;
				}
			}
		}
		br_i.close();

		// ---------------- NonCelebrities -----------------------
		for (int i = 0; i < flag_p.length; i++)
		{
			flag_p[i] = false;
		}
		FileName = "CDF_" + gUserType + "_" + fName_NC + ".txt";
		br_i = new BufferedReader(new InputStreamReader(new FileInputStream(OutDir + FileName)));
		br_i.readLine();
		while((st = br_i.readLine()) != null)
		{
			String arr[] = st.split("\t");
			int time = Integer.parseInt(arr[0]);
			double cdf = Double.parseDouble(arr[1]);
			for (int i = 0; i < p.length; i++)
			{
				if ((cdf > p[i]) && (flag_p[i] == false))
				{
					x_nonceleb[i] = time / 60;
					flag_p[i] = true;
				}
			}
		}
		br_i.close();

		// ------------ Write Arrow Information -----------
		FileName = "ArrowInfo_CDF_" + gUserType + ".txt";
		BufferedWriter bw = new BufferedWriter(new FileWriter(OutDir + FileName));
		bw.write("set style rect fc lt -1 fs solid 0.15 noborder\n\n");

		for (int i = 0; i < diff.length; i++)
		{
			diff[i] = Math.abs(x_celeb[i] - x_nonceleb[i]);

			bw.write("set arrow from " + Math.min(x_celeb[i], x_nonceleb[i]) + "," + p[i] + " \t to \t " + Math.max(x_celeb[i], x_nonceleb[i]) + "," + p[i] + " ls "
					+ (7) + " heads size screen 0.008,90 front\n");

			double xposition = Math.min(x_celeb[i], x_nonceleb[i]) + (diff[i] / 3);
			double yposition = p[i] + 0.05;

			bw.write("Label" + (i + 1) + "= \"" + (int) (diff[i]) + " minutes\"\n");
			bw.write("set label Label" + (i + 1) + " at " + xposition + "," + yposition + " tc rgb \"black\" font \",20\" front \n");
			bw.write("set object " + (i + 1) + " rect center " + xposition + "," + yposition + " size char strlen(Label" + (i + 1) + ")+2,char 1 fc rgb \"#FFFFFF\"\n");
			bw.write("set object " + (i + 1) + " front\n\n");
		}
		bw.close();
	}

	/*************************************************************************
	 * ReadUsersPercentile
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
		Followers_End_Value = x[1]-1;

		System.out.println("Followers_Start_Value[" + Start_Percentile + "]=" + Followers_Start_Value + " Followers_End_Value[" + End_Percentile + "]="
				+ Followers_End_Value);
	}

}