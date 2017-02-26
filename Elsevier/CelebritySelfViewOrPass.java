/*******************
Author  : Amit Ruhela
Purpose : This file compare the patterns of posting tweets by Celebrity and regular users:
This class analyzes all three datasets : Bollywood, Politics and Sports

Following information is evaluated from this class
[1] How many Tweets, ReTweets, and Replies are posted by Celebrities and Regular usesr
[2] How soon the two user classes retweets the other user tweets
[3] How much time later the tweets of these user classes tweets are retweets by other user classes
*******************/

package Elsevier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import twitter4j.TwitterException;

//Used Latest
public class CelebritySelfViewOrPass
{
	///#############################################

	public static final String Sep_file = "-<+>-";// File
	public static final String Sep_group = "\t";// Group
	public static final String Sep_record = ";";// Record

	public static final String RE_Sep_file = "\\-<\\+>\\-";// File
	public static final String RE_Sep_group = Sep_group;// Group
	public static final String RE_Sep_record = ";";// Record

	//Tweet
	public static int Index_Tweet_id = 0;
	public static int Index_Tweet_time = 1;
	public static int Index_Tweet_isRetweet = 2;
	public static int Index_Tweet_isRetweeted = 3;
	public static int Index_Tweet_isRetweetedByMe = 4;
	public static int Index_Tweet_RetweetCount = 5;
	public static int Index_Tweet_isFavorited = 6;
	public static int Index_Tweet_FavoriteCount = 7;
	public static int Index_Tweet_isTruncated = 8;
	public static int Index_Tweet_isPossiblySensitive = 9;
	public static int Index_Tweet_User = 10;
	public static int Index_Tweet_InReplyToStatusId = 11;
	public static int Index_Tweet_InReplyToUserId = 12;
	public static int Index_Tweet_GeoLocation = 13;
	public static int Index_Tweet_HashtagEntities = 14;
	public static int Index_Tweet_URLEntities = 15;
	public static int Index_Tweet_text = 16;

	public static int Index_User_id = 0;
	public static int Index_User_isProtected = 1;
	public static int Index_User_FollowersCount = 2;
	public static int Index_User_FriendsCount = 3;
	public static int Index_User_CreatedAt = 4;
	public static int Index_User_StatusesCount = 5;
	public static int Index_User_ListedCount = 6;
	public static int Index_User_FavouritesCount = 7;
	public static int Index_User_UtcOffset = 8;
	public static int Index_User_isGeoEnabled = 9;
	public static int Index_User_isVerified = 10;
	public static int Index_User_TimeZone = 11;
	public static int Index_User_Location = 12;

	static final int Threshold_Followers_3000 = 3_000;
	static final int typesCount = 3;

	static final int Index_3000 = 0;
	static final int Index_Verified = 1;
	static final int Index_Politicians = 2;
	static String Name[] = { "Index_3000", "Index_Verified", "Index_Celebrity" };
	static final String TopDir = "/home/amit/Celebrity/";
	static String OutDir = null;

	///#############################################

	static long Retweets_Cele[];
	static long Replies_Cele[];
	static long Retweets_NonCele[];
	static long Replies_NonCele[];
	static long TweetsCount_Cele[];
	static long TweetsCount_NonCele[];
	static long SecTweetsCount_Cele[];
	static long SecTweetsCount_NonCele[];

	static TreeMap<Long, Long> Timelag_Before_Cele[];
	static TreeMap<Long, Long> Timelag_Before_NonCele[];
	static TreeMap<Long, Long> Timelag_After_Cele[];
	static TreeMap<Long, Long> Timelag_After_NonCele[];
	static Long MaxTimeLag = 0L;

	static long TotalTweets_Count = 0;
	static long TotalReTweets_Count = 0;
	static long BadTweets_Count_Curr = 0;
	static long BadTweets_Count_Prev = 0;

	static long previousUserStatus_Celeb[][];
	static long previousUserStatus_NonCeleb[][];

	static Set<Long> Set_UsersPoliticians;
	static TreeMap<Integer, Integer> TM_DistributionFollowers;
	static String gUserType = null;

	/***************************************************************
	* InitializeVariables
	 * @throws FileNotFoundException 
	***************************************************************/
	private void InitializeVariables() throws FileNotFoundException
	{
		Retweets_Cele = new long[typesCount];
		Replies_Cele = new long[typesCount];
		Retweets_NonCele = new long[typesCount];
		Replies_NonCele = new long[typesCount];

		TweetsCount_Cele = new long[typesCount];
		TweetsCount_NonCele = new long[typesCount];

		SecTweetsCount_Cele = new long[typesCount];
		SecTweetsCount_NonCele = new long[typesCount];

		Timelag_Before_Cele = new TreeMap[typesCount];
		Timelag_Before_NonCele = new TreeMap[typesCount];

		Timelag_After_Cele = new TreeMap[typesCount];
		Timelag_After_NonCele = new TreeMap[typesCount];
		MaxTimeLag = 0L;

		TotalTweets_Count = 0;
		TotalReTweets_Count = 0;
		BadTweets_Count_Curr = 0;
		BadTweets_Count_Prev = 0;

		previousUserStatus_Celeb = new long[typesCount][typesCount + 1];
		previousUserStatus_NonCeleb = new long[typesCount][typesCount + 1];

		Set_UsersPoliticians = new HashSet<>();
		TM_DistributionFollowers = new TreeMap<>();

		OutDir = TopDir + "Output/ViewAndPass/" + gUserType + "/";
		System.out.println("OutDir=" + OutDir);
		EmptyDirectory(new File(OutDir));
		new File(OutDir).mkdir();
	}

	/***************************************************************
	* MainFile
	***************************************************************/
	public static void main(String[] args) throws NumberFormatException
	{
		String myClassName = Thread.currentThread().getStackTrace()[1].getClassName();
		long startExecution = (new Long(System.currentTimeMillis())).longValue();
		System.out.println(myClassName + "\n==========================");
		System.out.println(myClassName + " Starting at " + new Date(startExecution).toString());

		try
		{
			String[] UserTypeArray = { "B", "S", "P" }; // Bollywood, Politics and Sports Datasets
			for (int i = 0; i < UserTypeArray.length; i++)
			{
				CelebritySelfViewOrPass CB = new CelebritySelfViewOrPass();
				gUserType = UserTypeArray[i];

				CB.InitializeVariables();
				CB.MProcess();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		long endExecution = (new Long(System.currentTimeMillis())).longValue();
		long difference = (endExecution - startExecution) / 1000;
		System.out.println(myClassName + " finished at " + new Date(endExecution).toString() + " The program has taken " + (difference / 60) + " minutes.");
	}

	/***************************************************************
	* MainFile
	***************************************************************/
	private void MProcess() throws IOException, NumberFormatException
	{
		
		ReadPoliticians_2();
		for (int i = 0; i < typesCount; i++)
		{
			Timelag_Before_Cele[i] = new TreeMap<>();
			Timelag_After_Cele[i] = new TreeMap<>();

			Timelag_Before_NonCele[i] = new TreeMap<>();
			Timelag_After_NonCele[i] = new TreeMap<>();
		}

		String TopicsDir = TopDir + "SelectedTopics/Sorted_Topics_SelectedUsers_" + gUserType + "/";
		File[] mFiles = new File(TopicsDir).listFiles();
		for (int i = 0; i < mFiles.length; i++)
		{
			ReadFile(mFiles[i], i, mFiles.length);
		}

		BufferedWriter out = new BufferedWriter(new FileWriter(OutDir + "SelfOrPaas.txt"));
		out.write("TotalTweets_Count    \t" + TotalTweets_Count + "\n");
		out.write("-------------------------------------------------\n\n");

		StringBuffer myBuf = new StringBuffer();
		myBuf.append("Type" + "\t");
		myBuf.append("TweetsCount_Cele" + "\t\t" + "Retweets_Cele" + "\t" + "RatioRetweets_Cele" + "\t\t");
		myBuf.append("Replies_Cele" + "\t" + "RatioReplies_Cele" + "\t\t");

		myBuf.append("TweetsCount_NonCele" + "\t\t" + "Retweets_NonCele" + "\t" + "RatioRetweets_NonCele" + "\t\t");
		myBuf.append("Replies_NonCele" + "\t" + "RatioReplies_NonCele" + "\t\n");

		out.write(myBuf.toString());

		for (int i = 0; i < typesCount; i++)
		{
			myBuf.setLength(0);
			myBuf.append(Name[i] + "\t");

			myBuf.append(TweetsCount_Cele[i] + "\t\t" + Retweets_Cele[i] + "\t" + (100.0 * Retweets_Cele[i] / TweetsCount_Cele[i]) + "\t\t");
			myBuf.append(Replies_Cele[i] + "\t" + (100.0 * Replies_Cele[i] / TweetsCount_Cele[i]) + "\t\t");

			myBuf.append(TweetsCount_NonCele[i] + "\t\t" + Retweets_NonCele[i] + "\t" + (100.0 * Retweets_NonCele[i] / TweetsCount_NonCele[i]) + "\t\t");
			myBuf.append(Replies_NonCele[i] + "\t" + (100.0 * Replies_NonCele[i] / TweetsCount_NonCele[i]) + "\t\n");

			/*	myBuf.append(UsersCount_Cele[i] + "\t" + UsersCount_NonCele[i] + "\t");
				myBuf.append(SecUsersCount_Cele[i] + "\t" + SecUsersCount_NonCele[i] + "\n");
			*/
			out.write(myBuf.toString());
		}
		out.close();

		//####################### TimeLag Before ################################
		for (int i = 0; i < typesCount; i++)
		{
			WriteTimeLagBefore(i);
			WriteTimeLagAfter(i);
		}
		WritePreviousUserStatus();

	}

	/***************************************************************
	* WriteTimeLagBefore : Write output on how soon the users retweets.
	 * @throws IOException 
	***************************************************************/
	@SuppressWarnings("boxing")
	private void WriteTimeLagBefore(int pp) throws IOException
	{
		BufferedWriter out_Celeb = new BufferedWriter(new FileWriter(OutDir + "TimeLag_Before_Celeb_" + Name[pp].substring(6) + ".txt"));
		out_Celeb.write("BeforeMinutes\tCDF_Celebrity" + Name[pp].substring(6) + "\n");
		long total = 0l;
		Iterator<Long> mIter = Timelag_Before_Cele[pp].keySet().iterator();
		while(mIter.hasNext())
		{
			Long key = mIter.next();
			long pdf = Timelag_Before_Cele[pp].get(key);
			total += pdf;
			long cdf = (long) (100.0 * total / Retweets_Cele[pp]);
			out_Celeb.write((key + 1) + "\t" + cdf + "\n");
		}
		out_Celeb.close();

		BufferedWriter out_NonCeleb = new BufferedWriter(new FileWriter(OutDir + "TimeLag_Before_NonCeleb_" + Name[pp].substring(6) + ".txt"));
		out_NonCeleb.write("BeforeMinutes\tCDF_NonCelebrity" + Name[pp].substring(6) + "\n");
		total = 0l;
		mIter = Timelag_Before_NonCele[pp].keySet().iterator();
		while(mIter.hasNext())
		{
			Long key = mIter.next();
			long pdf = Timelag_Before_NonCele[pp].get(key);
			total += pdf;
			long cdf = (long) (100.0 * total / Retweets_NonCele[pp]);
			out_NonCeleb.write((key + 1) + "\t" + cdf + "\n");
		}
		out_NonCeleb.close();

	}

	/***************************************************************
	* WriteTimeLagAfter : Write output on how soon the tweets are further retweeted
	 * @throws IOException 
	***************************************************************/
	private void WriteTimeLagAfter(int pp) throws IOException
	{
		BufferedWriter out_Celeb = new BufferedWriter(new FileWriter(OutDir + "TimeLag_After_Celeb_" + Name[pp].substring(6) + ".txt"));
		out_Celeb.write("AfterMinutes\tCDF_Celebrity" + Name[pp].substring(6) + "\n");
		long total = 0l;
		Iterator<Long> mIter = Timelag_After_Cele[pp].keySet().iterator();
		while(mIter.hasNext())
		{
			Long key = mIter.next();
			long pdf = Timelag_After_Cele[pp].get(key);
			total += pdf;
			long cdf = (long) (100.0 * total / SecTweetsCount_Cele[pp]);
			out_Celeb.write((key + 1) + "\t" + cdf + "\n");
		}
		out_Celeb.close();

		BufferedWriter out_NonCeleb = new BufferedWriter(new FileWriter(OutDir + "TimeLag_After_NonCeleb_" + Name[pp].substring(6) + ".txt"));
		out_NonCeleb.write("AfterMinutes\tCDF_NonCelebrity" + Name[pp].substring(6) + "\n");
		total = 0l;
		mIter = Timelag_After_NonCele[pp].keySet().iterator();
		while(mIter.hasNext())
		{
			Long key = mIter.next();
			long pdf = Timelag_After_NonCele[pp].get(key);
			total += pdf;
			long cdf = (long) (100.0 * total / SecTweetsCount_NonCele[pp]);
			out_NonCeleb.write((key + 1) + "\t" + cdf + "\n");
		}
		out_NonCeleb.close();
	}

	/***************************************************************
	* WritePreviousUserStatus
	 * @throws IOException 
	***************************************************************/
	@SuppressWarnings("boxing")
	private void WritePreviousUserStatus() throws IOException
	{
		NumberFormat formatter = new DecimalFormat("#0.00");
		BufferedWriter out_Celeb = new BufferedWriter(new FileWriter(OutDir + "PreviousUserStatus.txt"));
		out_Celeb.write("CelebrityType\tCelebrity\tNon Celebrity\n");
		for (int i = 0; i < typesCount; i++)
		{
			String s = Name[i] + "\t";
			for (int j = 0; j < typesCount + 1; j++)
			{
				s = s + formatter.format(100.0 * previousUserStatus_Celeb[i][j] / Retweets_Cele[i]) + "/";
			}
			s = s + "\t";
			for (int j = 0; j < typesCount + 1; j++)
			{
				s = s + formatter.format(100.0 * previousUserStatus_NonCeleb[i][j] / Retweets_NonCele[i]) + "/";
			}
			out_Celeb.write(s + "\n");
		}
		out_Celeb.close();
	}

	/***************************************************************
	* ReadPoliticians_2 : Read the politician file
	 * @throws IOException 
	***************************************************************/
	@SuppressWarnings("boxing")
	private void ReadPoliticians_2() throws IOException
	{
		String st = "";
		BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(TopDir + "Data/Celebrities_" + gUserType + ".txt")));
		br_i.readLine();
		while((st = br_i.readLine()) != null)
		{
			String st1[] = st.split("\t");
			Set_UsersPoliticians.add(Long.parseLong(st1[1]));
		}
		br_i.close();
	}

	/***************************************************************
	 * GetEntities : Read Tweets File
	 ****************************************************************/
	@SuppressWarnings("boxing")
	private void ReadFile(File FileName, int cur, int tot) throws FileNotFoundException, IOException
	{
		System.out.println("Processing " + FileName.getName() + " " + cur + "/" + tot + " UserType=" + gUserType);
		String st;

		Long Time_Previous = 0L;
		Long lagInMinute = 0L;

		Long id = 0l;
		Long InReplyToStatusId = null;
		Integer isReTweet = 0;
		Long Time_Current = 0l;

		Long CurrentUser_ID = null;
		Integer CurrentUser_Followers = 0;
		Integer CurrentUser_isVerified = 0;

		String ElementsOfTweets_1[] = null;
		String ElementsOfTweets_2[] = null;
		String[] PreviousUser = null;
		Long PreviousUser_ID = 0L;
		Integer PreviousUser_Followers = 0;
		Integer PreviousUser_isVerified = 0;

		//Read Tweets file
		BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(FileName.getAbsolutePath())));
		br_i.readLine();
		while((st = br_i.readLine()) != null)
		{
			if (st.length() == 0)
				continue;

			//-------------------------------- Current Tweet --------------
			String[] Tweet = st.split(RE_Sep_file);
			//First Tweet
			ElementsOfTweets_1 = Tweet[0].split(RE_Sep_group);

			Time_Current = Long.parseLong(ElementsOfTweets_1[Index_Tweet_time]);
			id = Long.parseLong(ElementsOfTweets_1[Index_Tweet_id]);
			isReTweet = Integer.parseInt(ElementsOfTweets_1[Index_Tweet_isRetweet]);

			if (ElementsOfTweets_1[Index_Tweet_InReplyToStatusId].equals("-1"))
				InReplyToStatusId = 0L;
			else
				InReplyToStatusId = Long.parseLong(ElementsOfTweets_1[Index_Tweet_InReplyToStatusId]);

			//User
			String CurrentUser[] = ElementsOfTweets_1[Index_Tweet_User].split("" + RE_Sep_record);
			CurrentUser_ID = Long.parseLong(CurrentUser[Index_User_id]);
			CurrentUser_Followers = Integer.parseInt(CurrentUser[Index_User_FollowersCount]);
			CurrentUser_isVerified = Integer.parseInt(CurrentUser[Index_User_isVerified]);

			//-------------------------------- Previous Tweet --------------
			if ((isReTweet == 1) && (Tweet.length < 2))
			{
				System.out.println("------------\n");
				continue;
			}

			if (isReTweet == 1)
			{
				try
				{
					ElementsOfTweets_2 = Tweet[1].split("" + RE_Sep_group, -1);

					PreviousUser = ElementsOfTweets_2[Index_Tweet_User].split("" + RE_Sep_record, -1);
					PreviousUser_ID = Long.parseLong(PreviousUser[Index_User_id]);
					PreviousUser_Followers = Integer.parseInt(PreviousUser[Index_User_FollowersCount]);
					PreviousUser_isVerified = Integer.parseInt(PreviousUser[Index_User_isVerified]);

					Time_Previous = Long.parseLong(ElementsOfTweets_2[Index_Tweet_time]);
					lagInMinute = (Time_Current - Time_Previous) / (1000 * 60);//minutes
					if (lagInMinute > MaxTimeLag)
						MaxTimeLag = lagInMinute;
				}
				catch (Exception e)
				{
					BadTweets_Count_Prev++;
					System.out.println("Error2 : " + ElementsOfTweets_2[0] + "  BadTweets_Count_Curr=" + BadTweets_Count_Curr + "  BadTweets_Count_Prev="
							+ BadTweets_Count_Prev + "  TotalTweets_Count=" + TotalTweets_Count + " InReplyToStatusId=" + InReplyToStatusId + " isReTweet=" + isReTweet);
					continue;
				}
			}

			TotalTweets_Count++;
			if (isReTweet == 1)
			{
				TotalReTweets_Count++;
			}

			//############################################################################################
			//---------------  3000 --------------------------------
			if (CurrentUser_Followers.intValue() > Threshold_Followers_3000)
			{
				TweetsCount_Cele[Index_3000]++;
				//Set_Followers5000.add(CurrentUser_ID);

				if (InReplyToStatusId > 0)
				{
					Replies_Cele[Index_3000]++;
				}
				if (isReTweet == 1)
				{
					Retweets_Cele[Index_3000]++;

					Long Count = (Long) Timelag_Before_Cele[Index_3000].get(lagInMinute);
					if (Count == null)
						Timelag_Before_Cele[Index_3000].put(lagInMinute, 1l);
					else
						Timelag_Before_Cele[Index_3000].put(lagInMinute, 1 + Count);

					if (PreviousUser_Followers > Threshold_Followers_3000)
					{
						previousUserStatus_Celeb[Index_3000][Index_3000] += 1;
					}
					if (PreviousUser_isVerified.intValue() == 1)
					{
						previousUserStatus_Celeb[Index_3000][Index_Verified] += 1;
					}
					if (Set_UsersPoliticians.contains(PreviousUser_ID))
					{
						previousUserStatus_Celeb[Index_3000][Index_Politicians] += 1;
					}
					if ((PreviousUser_Followers > Threshold_Followers_3000) || (PreviousUser_isVerified.intValue() == 1)
							|| (Set_UsersPoliticians.contains(PreviousUser_ID)))
					{
						previousUserStatus_Celeb[Index_3000][Index_Politicians + 1] += 1;
					}
				}

			}
			else
			{
				TweetsCount_NonCele[Index_3000]++;
				if (InReplyToStatusId > 0)
				{
					Replies_NonCele[Index_3000]++;
				}
				if (isReTweet == 1)
				{
					Retweets_NonCele[Index_3000]++;
					Long Count = Timelag_Before_NonCele[Index_3000].get(lagInMinute);
					if (Count == null)
						Timelag_Before_NonCele[Index_3000].put(lagInMinute, 1l);
					else
						Timelag_Before_NonCele[Index_3000].put(lagInMinute, 1 + Count);

					if (PreviousUser_Followers > Threshold_Followers_3000)
					{
						previousUserStatus_NonCeleb[Index_3000][Index_3000] += 1;
					}
					if (PreviousUser_isVerified.intValue() == 1)
					{
						previousUserStatus_NonCeleb[Index_3000][Index_Verified] += 1;
					}
					if (Set_UsersPoliticians.contains(PreviousUser_ID))
					{
						previousUserStatus_NonCeleb[Index_3000][Index_Politicians] += 1;
					}
					if ((PreviousUser_Followers > Threshold_Followers_3000) || (PreviousUser_isVerified.intValue() == 1)
							|| (Set_UsersPoliticians.contains(PreviousUser_ID)))
					{
						previousUserStatus_NonCeleb[Index_3000][Index_Politicians + 1] += 1;
					}
				}
			}

			//---------------  Verified --------------------------------	
			if (CurrentUser_isVerified.intValue() == 1)
			{
				TweetsCount_Cele[Index_Verified]++;
				//Set_UserVeried.add(CurrentUser_ID);
				if (InReplyToStatusId > 0)
				{
					Replies_Cele[Index_Verified]++;
				}
				if (isReTweet == 1)
				{
					Retweets_Cele[Index_Verified]++;
					Long Count = Timelag_Before_Cele[Index_Verified].get(lagInMinute);
					if (Count == null)
						Timelag_Before_Cele[Index_Verified].put(lagInMinute, 1L);
					else
						Timelag_Before_Cele[Index_Verified].put(lagInMinute, 1 + Count);

					if (PreviousUser_Followers > Threshold_Followers_3000)
					{
						previousUserStatus_Celeb[Index_Verified][Index_3000] += 1;
					}
					if (PreviousUser_isVerified.intValue() == 1)
					{
						previousUserStatus_Celeb[Index_Verified][Index_Verified] += 1;
					}
					if (Set_UsersPoliticians.contains(PreviousUser_ID))
					{
						previousUserStatus_Celeb[Index_Verified][Index_Politicians] += 1;
					}
					if ((PreviousUser_Followers > Threshold_Followers_3000) || (PreviousUser_isVerified.intValue() == 1)
							|| (Set_UsersPoliticians.contains(PreviousUser_ID)))
					{
						previousUserStatus_Celeb[Index_Verified][Index_Politicians + 1] += 1;
					}
				}
			}
			else
			{
				TweetsCount_NonCele[Index_Verified]++;
				if (InReplyToStatusId > 0)
				{
					Replies_NonCele[Index_Verified]++;
				}
				if (isReTweet == 1)
				{
					Retweets_NonCele[Index_Verified]++;
					Long Count = Timelag_Before_NonCele[Index_Verified].get(lagInMinute);
					if (Count == null)
						Timelag_Before_NonCele[Index_Verified].put(lagInMinute, 1l);
					else
						Timelag_Before_NonCele[Index_Verified].put(lagInMinute, 1 + Count);

					if (PreviousUser_Followers > Threshold_Followers_3000)
					{
						previousUserStatus_NonCeleb[Index_Verified][Index_3000] += 1;
					}
					if (PreviousUser_isVerified.intValue() == 1)
					{
						previousUserStatus_NonCeleb[Index_Verified][Index_Verified] += 1;
					}
					if (Set_UsersPoliticians.contains(PreviousUser_ID))
					{
						previousUserStatus_NonCeleb[Index_Verified][Index_Politicians] += 1;
					}
					if ((PreviousUser_Followers > Threshold_Followers_3000) || (PreviousUser_isVerified.intValue() == 1)
							|| (Set_UsersPoliticians.contains(PreviousUser_ID)))
					{
						previousUserStatus_NonCeleb[Index_Verified][Index_Politicians + 1] += 1;
					}
				}
			}

			//---------------  Politicians --------------------------------	
			if (Set_UsersPoliticians.contains(CurrentUser_ID))
			{
				TweetsCount_Cele[Index_Politicians]++;
				if (InReplyToStatusId > 0)
				{
					Replies_Cele[Index_Politicians]++;
				}
				if (isReTweet == 1)
				{
					Retweets_Cele[Index_Politicians]++;
					Long Count = Timelag_Before_Cele[Index_Politicians].get(lagInMinute);
					if (Count == null)
						Timelag_Before_Cele[Index_Politicians].put(lagInMinute, 1l);
					else
						Timelag_Before_Cele[Index_Politicians].put(lagInMinute, 1 + Count);

					if (PreviousUser_Followers > Threshold_Followers_3000)
					{
						previousUserStatus_Celeb[Index_Politicians][Index_3000] += 1;
					}
					if (PreviousUser_isVerified.intValue() == 1)
					{
						previousUserStatus_Celeb[Index_Politicians][Index_Verified] += 1;
					}
					if (Set_UsersPoliticians.contains(PreviousUser_ID))
					{
						previousUserStatus_Celeb[Index_Politicians][Index_Politicians] += 1;
					}
					if ((PreviousUser_Followers > Threshold_Followers_3000) || (PreviousUser_isVerified.intValue() == 1)
							|| (Set_UsersPoliticians.contains(PreviousUser_ID)))
					{
						previousUserStatus_Celeb[Index_Politicians][Index_Politicians + 1] += 1;
					}
				}
			}
			else
			{
				TweetsCount_NonCele[Index_Politicians]++;
				if (InReplyToStatusId > 0)
				{
					Replies_NonCele[Index_Politicians]++;
				}
				if (isReTweet == 1)
				{
					Retweets_NonCele[Index_Politicians]++;
					Long Count = Timelag_Before_NonCele[Index_Politicians].get(lagInMinute);
					if (Count == null)
						Timelag_Before_NonCele[Index_Politicians].put(lagInMinute, 1l);
					else
						Timelag_Before_NonCele[Index_Politicians].put(lagInMinute, 1 + Count);

					if (PreviousUser_Followers > Threshold_Followers_3000)
					{
						previousUserStatus_NonCeleb[Index_Politicians][Index_3000] += 1;
					}
					if (PreviousUser_isVerified.intValue() == 1)
					{
						previousUserStatus_NonCeleb[Index_Politicians][Index_Verified] += 1;
					}
					if (Set_UsersPoliticians.contains(PreviousUser_ID))
					{
						previousUserStatus_NonCeleb[Index_Politicians][Index_Politicians] += 1;
					}
					if ((PreviousUser_Followers > Threshold_Followers_3000) || (PreviousUser_isVerified.intValue() == 1)
							|| (Set_UsersPoliticians.contains(PreviousUser_ID)))
					{
						previousUserStatus_NonCeleb[Index_Politicians][Index_Politicians + 1] += 1;
					}
				}
			}

			//######################################################################################################################
			//---------------------------------------  TimeLag After ------------------------------------------------------------
			//######################################################################################################################

			if (isReTweet == 1)
			{
				//---------------  5000 --------------------------------
				if (PreviousUser_Followers.intValue() > Threshold_Followers_3000)
				{
					SecTweetsCount_Cele[Index_3000]++;
					//SecSet_Followers5000.add(PreviousUser_ID);
					Long Count = Timelag_After_Cele[Index_3000].get(lagInMinute);
					if (Count == null)
						Timelag_After_Cele[Index_3000].put(lagInMinute, 1l);
					else
						Timelag_After_Cele[Index_3000].put(lagInMinute, 1 + Count);
				}
				else
				{
					SecTweetsCount_NonCele[Index_3000]++;
					Long Count = Timelag_After_NonCele[Index_3000].get(lagInMinute);
					if (Count == null)
						Timelag_After_NonCele[Index_3000].put(lagInMinute, 1l);
					else
						Timelag_After_NonCele[Index_3000].put(lagInMinute, 1 + Count);
				}

				//---------------  Verified --------------------------------	
				if (PreviousUser_isVerified.intValue() == 1)
				{
					SecTweetsCount_Cele[Index_Verified]++;
					//SecSet_UserVeried.add(PreviousUser_ID);
					Long Count = Timelag_After_Cele[Index_Verified].get(lagInMinute);
					if (Count == null)
						Timelag_After_Cele[Index_Verified].put(lagInMinute, 1l);
					else
						Timelag_After_Cele[Index_Verified].put(lagInMinute, 1 + Count);
				}
				else
				{
					SecTweetsCount_NonCele[Index_Verified]++;
					Long Count = Timelag_After_NonCele[Index_Verified].get(lagInMinute);
					if (Count == null)
						Timelag_After_NonCele[Index_Verified].put(lagInMinute, 1l);
					else
						Timelag_After_NonCele[Index_Verified].put(lagInMinute, 1 + Count);
				}

				//---------------  Politicians --------------------------------	
				if (Set_UsersPoliticians.contains(PreviousUser_ID))
				{
					SecTweetsCount_Cele[Index_Politicians]++;
					Long Count = Timelag_After_Cele[Index_Politicians].get(lagInMinute);
					if (Count == null)
						Timelag_After_Cele[Index_Politicians].put(lagInMinute, 1l);
					else
						Timelag_After_Cele[Index_Politicians].put(lagInMinute, 1 + Count);
				}
				else
				{
					SecTweetsCount_NonCele[Index_Politicians]++;
					Long Count = Timelag_After_NonCele[Index_Politicians].get(lagInMinute);
					if (Count == null)
						Timelag_After_NonCele[Index_Politicians].put(lagInMinute, 1l);
					else
						Timelag_After_NonCele[Index_Politicians].put(lagInMinute, 1 + Count);
				}
			}
		}
		br_i.close();
		//System.out.println("Completed CelebritySelfViewOrPass.ReadFile() " + FileName);
	}

	/*************************************************************************
	 * Recursively delete files from a folder
	 * @throws FileNotFoundException 
	 *************************************************************************/
	public void EmptyDirectory(File path) throws FileNotFoundException
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
}
