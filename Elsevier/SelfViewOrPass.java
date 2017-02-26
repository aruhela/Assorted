/*******************
Author  : Amit Ruhela
Purpose :

*******************/
package Elsevier;

//Includes CDF as well
//Used for four or more categories of users : Popular, Medium-Popular and Non-Popular
// can do the work of 3 percentile as well

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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

//Used Latest
public class SelfViewOrPass
{
	// /###################### Static Variables #######################

	public static final String Sep_file = "-<+>-";// File
	public static final String Sep_group = "\t";// Group
	public static final String Sep_record = ";";// Record

	public static final String RE_Sep_file = "\\-<\\+>\\-";// File
	public static final String RE_Sep_group = Sep_group;// Group
	public static final String RE_Sep_record = ";";// Record

	// Tweet
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

	static int CelebrityThreshold = 0;
	public static double CelebrityPercentile = 0;

	static final String TopDir = "G:/MyWork/";
	public final static String MainDir = TopDir + "Data/";
	public final static String EclipseDir = "G:/eclipse/workspaceLatex/Tikz_InfoFlow/ViewPass/";

	//static int MinFollowersCount = 1; // used when all users is false
	static int GlobalUserTypeCount = 4;
	static double[] percentile = { 0, 70, 95, 99.90, 100 };
	static String[] NameUser = { "N", "L", "M", "H" };//Non,Low,Med,High

	//	static int GlobalUserTypeCount = 4;
	//	static double[] percentile = { 0, 50, 80, 99.85, 100 };
	//	static String[] NameUser = { "N", "L", "M", "H" };//Non,Low,Med,High

	// ##################### Dynamic Variables ########################
	static String gUserType = "";
	static String OutDir = null;

	static int[] Followers_Start_Value = new int[GlobalUserTypeCount];//inclusive
	static int[] Followers_End_Value = new int[GlobalUserTypeCount];//inclusive

	static long[] Tweets_User = new long[GlobalUserTypeCount];
	static long[] Tweets_User_Orig = new long[GlobalUserTypeCount];

	static long[][] PrevUseris_X_Y = new long[GlobalUserTypeCount][GlobalUserTypeCount];
	static DescriptiveStatistics[][] ds_X_Y_Timelag = new DescriptiveStatistics[GlobalUserTypeCount][GlobalUserTypeCount];

	/***************************************************************
	 * InitializeVariables
	 ***************************************************************/
	private void InitializeVariables()
	{
		for (int i = 0; i < GlobalUserTypeCount; i++)
		{
			Followers_Start_Value[i] = 0;
			Followers_End_Value[i] = 0;
			Tweets_User[i] = 0;
			Tweets_User_Orig[i] = 0;

			for (int j = 0; j < GlobalUserTypeCount; j++)
			{
				PrevUseris_X_Y[i][j] = 0;
				ds_X_Y_Timelag[i][j] = null;
				ds_X_Y_Timelag[i][j] = new DescriptiveStatistics();
			}
		}

		OutDir = TopDir + "Output/ViewAndPass/" + CelebrityPercentile + "/";
		System.out.println("OutDir=" + OutDir);
	}

	/***************************************************************
	 * MainFile
	 ***************************************************************/
	SelfViewOrPass(int cthresh, String mUserType)
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
	}

	public static void main(String[] args) throws NumberFormatException
	{
		String myClassName = Thread.currentThread().getStackTrace()[1].getClassName();
		long startExecution = (new Long(System.currentTimeMillis())).longValue();
		System.out.println(myClassName + "\n==========================");
		System.out.println(myClassName + " Starting at " + new Date(startExecution).toString());

		try
		{
			String[] UserTypeArray = { "B", "P", "S" };
			for (int i = 0; i < UserTypeArray.length; i++)
			{
				System.out.println("\n\n===========================" + UserTypeArray[i] + "=======================\n");
				gUserType = UserTypeArray[i];
				System.out.println("\n");

				SelfViewOrPass CB = new SelfViewOrPass(2, UserTypeArray[i]);
				CB.InitializeVariables();
				ReadUsersPercentile();
				CB.MProcess();
				CB.WriteOutput();
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

	/*************************************************************************
	 * ReadUsersPercentile
	 *************************************************************************/
	public static void ReadUsersPercentile() throws IOException
	{
		String FName = "G:/MyWork/Output/Misc/FollowersDistribution" + gUserType + ".txt";
		System.out.println("gUserType=" + gUserType + " Fname=" + FName);

			
		boolean[] percentileFlag = new boolean[percentile.length];
		int[] UC = new int[percentile.length];
		int prevCount=0;
		
		percentileFlag[0] = true;
		for (int j = 1; j < percentileFlag.length; j++)
		{
			percentileFlag[j] = false;
		}

		Followers_Start_Value[0] = 0;

		String st = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(FName)));
		br.readLine();
		int followersCount = 0;
		int lCount = 0;
		while((st = br.readLine()) != null)
		{
			String arr[] = st.split("\\t");
			followersCount = Integer.parseInt(arr[0]);
			double lPercentile = Double.parseDouble(arr[2]);
			lCount =  Integer.parseInt(arr[3]);

			for (int j = 0; j < percentileFlag.length; j++)
			{
				if ((percentileFlag[j] == false) && (lPercentile > percentile[j]))
				{
					percentileFlag[j] = true;
					Followers_Start_Value[j] = followersCount;
					UC[j] = lCount-prevCount;
					prevCount = lCount;
				}
			}
		}
		br.close();

		for (int i = 0; i < (GlobalUserTypeCount - 1); i++)
		{
			Followers_End_Value[i] = Followers_Start_Value[i + 1] - 1;			
		}
		Followers_End_Value[GlobalUserTypeCount - 1] = followersCount;
		UC[percentile.length - 1] = lCount-prevCount;

		for (int i = 0; i < (GlobalUserTypeCount); i++)
		{
			System.out.println("Followers_Start_Value[" + percentile[i] + "]=" + Followers_Start_Value[i] + " Followers_End_Value[" + percentile[i + 1] + "]="
					+ Followers_End_Value[i]+"\t"+UC[i+1]);
		}
	}

	/***************************************************************
	 * MainFile
	 ***************************************************************/
	private void MProcess() throws IOException, NumberFormatException
	{
		String TopicsDir = TopDir + "SelectedTopics/Sorted_Topics_SelectedUsers_" + gUserType + "/";

		File[] mFiles = new File(TopicsDir).listFiles();
		System.out.println("Number of files in " + gUserType + " is " + mFiles.length);
		for (int i = 0; i < mFiles.length; i++)
		{
			ReadFile(mFiles[i], i, mFiles.length);
		}
	}

	/***************************************************************
	 * GetEntities
	 ****************************************************************/
	@SuppressWarnings({ "boxing", "unused" })
	private void ReadFile(File FileName, int cur, int tot) throws FileNotFoundException, IOException
	{
		// System.out.println("Processing " + FileName.getName() + " " + cur + "/" + tot + " UserType=" + gUserType);
		String st;

		String ElementsOfTweets_1[] = null;
		String ElementsOfTweets_2[] = null;

		Long Time_Current = 0l;

		String[] PreviousUser = null;
		Integer PreviousUser_Followers = 0;
		Integer CurrentUser_Followers = 0;
		Long Time_Previous = 0L;
		Long lagInSeconds = 0L;

		//		Long InReplyToStatusId = null;
		Integer isReTweet = 0;

		int typeOfCurrentUsers = 0; // 0-NonCeleb, 1=MedCeleb, 2=Celeb
		int typeOfPrevUsers = 0;// 0-NonCeleb, 1=MedCeleb, 2=Celeb

		BufferedReader br_i = new BufferedReader(new InputStreamReader(new FileInputStream(FileName.getAbsolutePath())));
		br_i.readLine();
		while((st = br_i.readLine()) != null)
		{
			if (st.length() == 0)
				continue;

			typeOfCurrentUsers = 0;
			typeOfPrevUsers = 0;

			// ######################################################################################################################
			// --------------------------------------- Pre-Analysis ------------------------------------------------------------
			// ######################################################################################################################

			String[] Tweet = st.split(RE_Sep_file);
			ElementsOfTweets_1 = Tweet[0].split(RE_Sep_group);

			Time_Current = Long.parseLong(ElementsOfTweets_1[Index_Tweet_time]);
			if (Tweet.length > 1)
				isReTweet = 1;
			else
				isReTweet = 0;

			//Testing
			if (Long.parseLong(ElementsOfTweets_1[Index_Tweet_InReplyToStatusId]) > 0)
			{
				continue;
			}

			// Current User
			String CurrentUser[] = ElementsOfTweets_1[Index_Tweet_User].split("" + RE_Sep_record);
			CurrentUser_Followers = Integer.parseInt(CurrentUser[Index_User_FollowersCount]);

			typeOfCurrentUsers = GetUserType(CurrentUser_Followers.intValue());
			// Previous User
			if (isReTweet == 1)
			{
				try
				{
					ElementsOfTweets_2 = Tweet[1].split("" + RE_Sep_group);

					PreviousUser = ElementsOfTweets_2[Index_Tweet_User].split("" + RE_Sep_record);
					PreviousUser_Followers = Integer.parseInt(PreviousUser[Index_User_FollowersCount]);
					// if (PreviousUser_Followers < 1)
					// System.out.println("PreviousUser_Followers=" + PreviousUser_Followers+"  PreviousUser="+ ElementsOfTweets_2[Index_Tweet_User]);
					Time_Previous = Long.parseLong(ElementsOfTweets_2[Index_Tweet_time]);
					lagInSeconds = (Time_Current - Time_Previous) / (1000);// Seconds

					typeOfPrevUsers = GetUserType(PreviousUser_Followers.intValue());
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.exit(0);
				}
			}

			// ######################################################################################################################
			// --------------------------------------- Tweets Analysis ------------------------------------------------------------
			// ######################################################################################################################

			Tweets_User[typeOfCurrentUsers]++;
			if (isReTweet == 0)
			{
				Tweets_User_Orig[typeOfCurrentUsers]++;
			}
			else
			{
				PrevUseris_X_Y[typeOfPrevUsers][typeOfCurrentUsers]++;
			}

			// ######################################################################################################################
			// --------------------------------------- TimeLag Before ------------------------------------------------------------
			// ######################################################################################################################

			if (isReTweet == 1)
			{
				ds_X_Y_Timelag[typeOfPrevUsers][typeOfCurrentUsers].addValue(lagInSeconds);// X to Y tweet				
			}
		}
		br_i.close();
	}

	/***************************************************************
	 * GetUserType
	 ***************************************************************/
	private int GetUserType(int fCount)
	{
		int type = 0;// Least Popular or Non-Popular User
		for (int i = 0; i < GlobalUserTypeCount; i++)
		{
			if ((fCount >= Followers_Start_Value[i]) && (fCount <= Followers_End_Value[i]))
			{
				type = i;
				break;
			}
		}
		return type;
	}

	/***************************************************************
	 * WriteOutput
	 ***************************************************************/
	private void WriteOutput() throws IOException, NumberFormatException
	{
		BufferedWriter out = new BufferedWriter(new FileWriter(EclipseDir + gUserType + "_GnuData.txt"));

		// -------------- Tweets -------------------

		System.out.println("\n\n");
		for (int i = 0; i < (GlobalUserTypeCount); i++)
		{
			MyPrint("Tweets" + NameUser[i], (100.0 * Tweets_User_Orig[i] / Tweets_User[i]), "", out, "");
			for (int j = 0; j < (GlobalUserTypeCount); j++)//Prev User
			{
				MyPrint("TweetsPrevUserIs" + NameUser[j] + NameUser[i], (100.0 * PrevUseris_X_Y[j][i] / Tweets_User[i]), "", out, "");
			}
			System.out.println("\n");
			out.write("\n");
		}
		System.out.println("\n\n\n");
		out.write("\n\n\n");

		// -------------- Time -------------------
		double factor = 60.0;

		for (int i = 0; i < (GlobalUserTypeCount); i++)
		{
			for (int j = 0; j < (GlobalUserTypeCount); j++)//Prev User
			{
				MyPrint(NameUser[i] + NameUser[j] + "Thirty", ds_X_Y_Timelag[i][j].getPercentile(30) / factor, "", out, "");
				MyPrint(NameUser[i] + NameUser[j] + "Fifty", ds_X_Y_Timelag[i][j].getPercentile(50) / factor, "", out, "");
				MyPrint(NameUser[i] + NameUser[j] + "Eighty", ds_X_Y_Timelag[i][j].getPercentile(80) / factor, "", out, "");
			}
			System.out.println("\n");
			out.write("\n");
		}

		int TweetsTotal = 0;
		int ReTweetsTotal = 0;
		for (int i = 0; i < (GlobalUserTypeCount); i++)
		{
			TweetsTotal += (Tweets_User[i]);
			ReTweetsTotal += (Tweets_User[i] - Tweets_User_Orig[i]);
		}
		for (int i = 0; i < (GlobalUserTypeCount); i++)
		{
			int tPercentile = (int) Math.round(100.0 * Tweets_User[i] / TweetsTotal);
			MyPrint("T" + NameUser[i], tPercentile, "", out, "");
		}
		System.out.println("\n");
		out.write("\n");

		for (int i = 0; i < (GlobalUserTypeCount); i++)
		{
			int rPercentile = (int) Math.round(100.0 * (Tweets_User[i] - Tweets_User_Orig[i]) / ReTweetsTotal);
			MyPrint("R" + NameUser[i], rPercentile, "", out, "");
		}
		System.out.println("\n");
		out.write("\n");

		//WritePercentile
		for (int i = 0; i < (GlobalUserTypeCount); i++)
		{
			MyPrint4Double("StartPercentile" + NameUser[i], percentile[i], "", out, "");
		}
		System.out.println("\n");
		out.write("\n");

		//WritePercentile
		for (int i = 0; i < (GlobalUserTypeCount); i++)
		{
			MyPrint("StartFollCount" + NameUser[i], Followers_Start_Value[i], "", out, "");
			MyPrint("EndFollCount" + NameUser[i], Followers_End_Value[i], "", out, "\n");
		}
		out.close();
	}

	public static void MyPrint(String Text, double value, String ValueText, BufferedWriter out, String delim) throws IOException
	{
		NumberFormat f = new DecimalFormat("#0");
		out.write("\\def\\" + Text + "{" + f.format(Math.round(value)) + ValueText + "}\n" + delim);
		System.out.println("\\def\\" + Text + "{" + f.format(Math.round(value)) + ValueText + "}" + delim);
	}

	public static void MyPrint4Double(String Text, double value, String ValueText, BufferedWriter out, String delim) throws IOException
	{
		NumberFormat f = new DecimalFormat("#0.00");
		out.write("\\def\\" + Text + "{" + f.format(value) + ValueText + "}\n" + delim);
		System.out.println("\\def\\" + Text + "{" + f.format(value) + ValueText + "}" + delim);
	}

}
