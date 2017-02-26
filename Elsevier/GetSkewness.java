/*******************
Author  : Amit Ruhela
Purpose :  How earlier Popular users participate in the event?

*******************/
package Elsevier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.text.DecimalFormat;
//import java.text.NumberFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class GetSkewness
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
	static final int Index_User_isVerified = 10;

	// Regular Expression
	public static final String RE_Sep_file = "\\-<\\+>\\-";// File
	public static final String Sep_group = "\t";// Group
	public static final String RE_Sep_group = "\\t";// Group
	public static final String RE_Sep_record = ";";// Record

	static int CelebrityThreshold = 0;
	public static double CelebrityPercentile = 0;
	static TreeMap<String, String> TM_Events = new TreeMap<>();

	static int tweets_c = 0;
	static int tweets_nc = 0;

	static int tDiff = 0;

	static TreeMap<Double, Integer> TM_Tweets_C = new TreeMap<>();
	static TreeMap<Double, Integer> TM_Tweets_NC = new TreeMap<>();

	static boolean normalize = true;
	static int phaseIndex = 1;// End of growth Phase
	// static int phaseIndex = 2;// End of Peak Phase
	// static int phaseIndex = 3; // End of Event

	static boolean AllUsers = false;
	//static int Start_Percentile = 30;
	//static int End_Percentile = 50 + (50 - Start_Percentile);

	//Ordinary
	//static int Start_Percentile = 70;
	//static int End_Percentile = 95;

	//Medium
	static double Start_Percentile = 95;
	static double End_Percentile = 99.9;

	static int Followers_Start_Value = 0;
	static int Followers_End_Value = 0;
	public final static String MainDir = "G:/MyWork/Celebrity/Data/";

	/***************************************************************
	 * MainFile
	 ***************************************************************/
	GetSkewness(int cthresh, String mUserType)
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
		// CelebrityThreshold = 3000;
		System.out.println("CelebrityThreshold=" + CelebrityThreshold + "\t" + "CelebrityPercentile=" + CelebrityPercentile);
		HashDefinesForED.SetUserType(mUserType, CelebrityPercentile);

		tweets_c = 0;
		tweets_nc = 0;
		TM_Tweets_C.clear();
		TM_Tweets_NC.clear();
		tDiff = 0;
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

		String[] arr = { "P", "S", "B" };
		for (int i = 0; i < arr.length; i++)
		{
			System.out.println("\n\n");
			GetSkewness startFun = new GetSkewness(2, arr[i]);
			startFun.ReadUsersPercentile(arr[i]);
			startFun.ReadEvents();
			startFun.StartApp();
			startFun.WriteArrowInformation(arr[i]);
			startFun = null;
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
	@SuppressWarnings("boxing")
	public void StartApp() throws IOException, InterruptedException
	{
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
		System.out.println("tweets_c=" + tweets_c);
		System.out.println("tweets_nc=" + tweets_nc);

		BufferedWriter br_o = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_ParticipationSkew_2_C));
		Iterator<Double> iter = TM_Tweets_C.keySet().iterator();
		int count = 0;
		double oldCDF = 0.0;
		double newCDF = 0.0;
		// int mCount = 0;
		while(iter.hasNext())
		{
			// mCount++;
			Double Key = iter.next();
			Integer value = TM_Tweets_C.get(Key);
			count += value;
			newCDF = 1.0 * count / tweets_c;
			// if (((newCDF - oldCDF) > 0.01) && (mCount > 30))
			if ((newCDF - oldCDF) > 0.01)
			{
				br_o.write(Key + "\t" + newCDF + "\n");
				oldCDF = newCDF;
			}
		}
		br_o.close();

		br_o = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_ParticipationSkew_2_NC));
		iter = TM_Tweets_NC.keySet().iterator();
		count = 0;
		oldCDF = 0.0;
		newCDF = 0.0;
		// mCount = 0;
		while(iter.hasNext())
		{
			// mCount++;
			Double Key = iter.next();
			Integer value = TM_Tweets_NC.get(Key);
			count += value;
			newCDF = 1.0 * count / tweets_nc;
			// if (((newCDF - oldCDF) > 0.01) && (mCount > 30))
			if ((newCDF - oldCDF) > 0.01)
			{
				br_o.write(Key + "\t" + newCDF + "\n");
				oldCDF = newCDF;
			}
		}
		br_o.close();

	}

	/***************************************************************
	 * ReadEvents
	 ***************************************************************/
	private void ReadEvents() throws IOException
	{
		TM_Events.clear();
		String st = "";
		// System.out.println("GetSkewness.ReadEvents()  " + (HashDefinesForED.EventsFolder + HashDefinesForED.FName_ClassifiedEvents));
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

		Set<Long> USet = new HashSet<>();
		String[] events = TM_Events.get(gTopic).split(RE_Sep_record);
		int eventIndex = 0;
		long StartTime = Long.parseLong(events[0].split(RE_Sep_group)[0]);
		long endTime = Long.parseLong(events[0].split(RE_Sep_group)[phaseIndex]);
		long lifeOfEvent = endTime - StartTime;
		// System.out.println("ReadNextTopic : Topic=" + gTopic + "  " + index + "/" + tt + " " + events.length + " events");
		// System.out.println("  EventIndex=" + eventIndex);

		DescriptiveStatistics ds_C = new DescriptiveStatistics();
		DescriptiveStatistics ds_NC = new DescriptiveStatistics();

		String st = "";
		BufferedWriter br_o = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_ParticipationSkew, true));
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
			else if (Time >= endTime)
			{
				if (ds_C.getN() > 0)
				{
					int c = (int) (ds_C.getMean() / (1000));
					int nc = (int) (ds_NC.getMean() / (1000));
					br_o.write(gTopic + "\t" + eventIndex + "\t" + StartTime + "\t" + endTime + "\t" + c + "\t" + nc + "\t" + (1.0 * c / lifeOfEvent) + "\t"
							+ (1.0 * nc / lifeOfEvent) + "\n");

					tweets_c += ds_C.getN();
					tweets_nc += ds_NC.getN();

					Double Factor = 1.0 * 1000;
					//Double Factor = 60.0 * 1000;
					// ----------- Celebrity -------------
					for (int i = 0; i < ds_C.getN(); i++)
					{

						Double time = ds_C.getElement(i) / Factor;
						Integer value = TM_Tweets_C.get(time);
						if (value == null)
						{
							TM_Tweets_C.put(time, 1);
						}
						else
						{
							TM_Tweets_C.put(time, 1 + value);
						}
					}

					// ----------- Non Celebrity -------------
					for (int i = 0; i < ds_NC.getN(); i++)
					{
						Double time = ds_NC.getElement(i) / Factor;
						Integer value = TM_Tweets_NC.get(time);
						if (value == null)
						{
							TM_Tweets_NC.put(time, 1);
						}
						else
						{
							TM_Tweets_NC.put(time, 1 + value);
						}
					}
					// ------------------------ -------------
				}
				eventIndex++;
				ds_C.clear();
				ds_NC.clear();
				USet.clear();

				if (eventIndex == events.length)
				{
					break;
				}
				// System.out.println("  EventIndex=" + eventIndex);
				StartTime = Long.parseLong(events[eventIndex].split(RE_Sep_group)[0]);
				endTime = Long.parseLong(events[eventIndex].split(RE_Sep_group)[phaseIndex]);
				lifeOfEvent = endTime - StartTime;
				continue;
			}

			String User_Current[] = elements[Index_Tweet_User].split(RE_Sep_record);
			Long Uid = Long.parseLong(User_Current[Index_User_ID]);
			int followersCount = Integer.parseInt(User_Current[Index_User_Followers]);

			if (!USet.contains(Uid))
			{
				long timediff = Time - StartTime;
				if (timediff == 0)
				{
					tDiff++;
					System.out.println("Timedifference = 0; count=" + tDiff);
				}
				if (followersCount > CelebrityThreshold)
				{
					ds_C.addValue(timediff);
					USet.add(Uid);
				}
				else
				{
					if (AllUsers)
					{
						ds_NC.addValue(timediff);
						USet.add(Uid);
					}
					else
					{
						if ((followersCount >= Followers_Start_Value) && (followersCount <= Followers_End_Value))
						{
							ds_NC.addValue(timediff);
							USet.add(Uid);
						}
					}
				}
			}
		}
		br_i.close();
		br_o.close();
		return true;
	}

	/***************************************************************
	 * WriteArrowInformation
	 ***************************************************************/
	@SuppressWarnings("boxing")
	private void WriteArrowInformation(String gUserType) throws NumberFormatException, IOException
	{
		//NumberFormat formatter = new DecimalFormat("#0.000");

		Double MinX = Double.MAX_VALUE;
		Double MaxX = Double.MIN_VALUE;

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

		BufferedWriter br_o = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_ParticipationSkew_2_C));

		Iterator<Double> iter = TM_Tweets_C.keySet().iterator();
		int count = 0;
		double newcdf = 0.0;
		double oldcdf = -1.0;
		while(iter.hasNext())
		{
			Double time = iter.next();
			if (time < MinX)
			{
				if (time > 0)
					MinX = time;
			}
			if (time > MaxX)
				MaxX = time;

			Integer value = TM_Tweets_C.get(time);
			count += value.intValue();
			newcdf = 1.0 * count / tweets_c;
			for (int i = 0; i < p.length; i++)
			{
				if ((newcdf > p[i]) && (flag_p[i] == false))
				{
					x_celeb[i] = time.doubleValue();
					flag_p[i] = true;
				}
				if ((i < 5) || ((newcdf - oldcdf) > 0.01) || (i > (p.length - 5)))
				{
					br_o.write(time + "\t" + newcdf + "\n");
					oldcdf = newcdf;
				}
			}
		}
		br_o.close();

		// ---------------- NonCelebrities -----------------------
		br_o = new BufferedWriter(new FileWriter(HashDefinesForED.EventsFolder + HashDefinesForED.FName_ParticipationSkew_2_NC));
		for (int i = 0; i < flag_p.length; i++)
		{
			flag_p[i] = false;
		}
		iter = TM_Tweets_NC.keySet().iterator();
		count = 0;
		newcdf = 0.0;
		oldcdf = -1.0;
		while(iter.hasNext())
		{
			Double time = iter.next();
			if (time < MinX)
			{
				if (time > 0)
					MinX = time;
			}
			if (time > MaxX)
				MaxX = time;

			Integer value = TM_Tweets_NC.get(time);
			count += value.intValue();
			newcdf = 1.0 * count / tweets_nc;
			for (int i = 0; i < p.length; i++)
			{
				if ((newcdf > p[i]) && (flag_p[i] == false))
				{
					x_nonceleb[i] = time.doubleValue();
					flag_p[i] = true;
				}
				if ((i < 5) || ((newcdf - oldcdf) > 0.01) || (i > (p.length - 5)))
				{
					br_o.write(time + "\t" + newcdf + "\n");
					oldcdf = newcdf;
				}
			}
		}
		br_o.close();

		// ---------------- Find Difference -----------------------
		for (int i = 0; i < diff.length; i++)
		{
			diff[i] = (x_nonceleb[i] - x_celeb[i]);
			System.out.println("%=" + p[i] + "  x_celeb=" + x_celeb[i] + "  x_nonceleb=" + x_nonceleb[i] + "  diff=" + diff[i]);
		}

		// ------------ Write Arrow Information -----------
		String FileName = "G:/MyWork/Output/Plots_9990/Skew/" + "Arrow_Information" + gUserType + ".txt";
		BufferedWriter bw = new BufferedWriter(new FileWriter(FileName));
		bw.write("#CelebrityThreshold=" + CelebrityThreshold + "\t" + "CelebrityPercentile=" + CelebrityPercentile + "\n");
		bw.write("#Start_Percentile=" + Start_Percentile + ", End_Percentile=" + End_Percentile + "\n");
		bw.write("#Followers_Start_Value=" + Followers_Start_Value + " Followers_End_Value=" + Followers_End_Value + "\n\n\n");

		for (int i = 0; i < diff.length; i++)
		{
			bw.write("set arrow from " + MinX + " ," + p[i] + " \t to \t " + " " + MaxX + " ," + p[i] + " nohead front lt 2\n");
			bw.write("set arrow from " + Math.min(x_celeb[i], x_nonceleb[i]) + "," + p[i] + " \t to \t " + Math.max(x_celeb[i], x_nonceleb[i]) + "," + p[i] + " ls "
					+ (7) + " heads size screen 0.008,90 front\n");

			double xposition = Math.min(x_celeb[i], x_nonceleb[i]) + (Math.abs(diff[i]) / 3);
			xposition = Math.pow(2, (Math.log(xposition) / Math.log(2)) - 2);

			double yposition = p[i] + 0.05;

			int mMin = (int) (diff[i]/60);
			
			if(mMin>0)
			// bw.write("set label \"" + formatter.format(diff[i]) + " sec\" at " + xposition + "," + yposition + " tc rgb \"black\" font \",20\" front \n\n");
			bw.write("set label \"" + (int) (mMin) + " min\" at " + xposition + "," + yposition + " tc rgb \"black\" font \",20\" front \n\n");
			else
				bw.write("set label \"" + (int) (diff[i]) + " sec\" at " + xposition + "," + yposition + " tc rgb \"black\" font \",20\" front \n\n");
				

		}
		bw.write("set xrange [:" + MaxX + "]\n");
		bw.close();

	}

	/*************************************************************************
	 * ReadUsersPercentile
	 *************************************************************************/
	public static void ReadUsersPercentile(String gUserType) throws IOException
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