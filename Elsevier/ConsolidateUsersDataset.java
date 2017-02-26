/*******************
Author  : Amit Ruhela
Purpose : This file consolidate the Tweets database. Instead of storing everthing written in the tweets, we can write
only those hashtags/topics that we would like to study further.
Huge space reduction in storage of Tweets database
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

public class ConsolidateUsersDataset
{
	String GDir = "";

	public static final String Sep_file = "-<+>-";// File
	public static final String Sep_group = "\t";// Group
	public static final String Sep_record = ";";// Record

	public static final String RE_Sep_file = "\\-<\\+>\\-";// File
	public static final String RE_Sep_group = Sep_group;// Group
	public static final String RE_Sep_record = ";";// Record

	public static Set<Long> Users = new HashSet<>();
	public static Set<String> SelectedHT = new HashSet<>();
	public static BufferedWriter[] Out_Topics_AllUsers = null;
	public static BufferedWriter[] Out_Topics_SelectedUsers = null;
	public static TreeMap<String, Integer> TM_Topics = new TreeMap<>();
	public static int TweetsCount = 0;
	public static String OutDir = "C:/Consolidated/";

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

	/***************************************************************
	 * MainFile
	 ***************************************************************/
	@SuppressWarnings("boxing")
	public static void main(String[] args) throws IOException
	{
		String myClassName = Thread.currentThread().getStackTrace()[1].getClassName();
		long startExecution = (new Long(System.currentTimeMillis())).longValue();

		// Read FileNames
		Vector<File> FileVector = new Vector<>();
		File[] dir = new File("G:/BSP/Processing/Output/").listFiles();
		for (int j = 0; j < dir.length; j++)
		{
			FileVector.add(dir[j]);
		}
		dir = new File("F:/BSP/Processing/Tweets/").listFiles();
		for (int j = 0; j < dir.length; j++)
		{
			FileVector.add(dir[j]);
		}
		System.out.println("Count of Tweets Files = " + FileVector.size());

		String[] UserTypeArray = { "B" };
		//String[] UserTypeArray = { "P", "S", "B" };
		for (int i = 0; i < UserTypeArray.length; i++)
		{
			Users.clear();
			SelectedHT.clear();
			TM_Topics.clear();

			//Read Users who are this type of celebrities
			ReadUsers(UserTypeArray[i]);

			//Read Selected Hashtags
			ReadSelectedTopics(UserTypeArray[i]);

			//Clear the output file
			String fName = OutDir + "ConsolidatesTweets_" + UserTypeArray[i] + ".txt";
			BufferedWriter bw = new BufferedWriter(new FileWriter(fName));
			bw.close();

			//Read Tweets
			for (int j = 0; j < FileVector.size(); j++)
			{
				ReadTweets(j, FileVector.get(j), UserTypeArray[i]);
			}

			Iterator<String> mIter = SelectedHT.iterator();
			while(mIter.hasNext())
			{
				String Topic = mIter.next();
				Out_Topics_AllUsers[TM_Topics.get(Topic)].close();
				Out_Topics_SelectedUsers[TM_Topics.get(Topic)].close();
			}
			System.out.println("ConsolidateUsersDataset.main() " + new Date().toString());

		}

		long endExecution = (new Long(System.currentTimeMillis())).longValue();
		long difference = (endExecution - startExecution) / 1000;
		System.out.println(myClassName + " finished at " + new Date().toString() + " The program has taken " + (difference / 60) + " minutes.");
	}

	/***************************************************************
	 * ReadSelectedTopics : Read hashtags corresponding to different datasets and create file handle for each topic to write
	***************************************************************/
	@SuppressWarnings("boxing")
	private static void ReadSelectedTopics(String mUserType) throws IOException
	{
		System.out.println("ReadSelectedTopics() " + mUserType);
		String fName = "F:/BSP/Important/SelectedTags/SelectedHashTags_" + mUserType + ".txt";
		String readStr = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fName)));
		SelectedHT.clear();
		int i = 0;
		while((readStr = br.readLine()) != null)
		{
			SelectedHT.add(readStr.split("\\t")[0]);
			System.out.println(i++ + " "+readStr.split("\\t")[0]);
		}
		br.close();
		Out_Topics_AllUsers = new BufferedWriter[SelectedHT.size()];
		Out_Topics_SelectedUsers = new BufferedWriter[SelectedHT.size()];

		Iterator<String> mIter = SelectedHT.iterator();
		i = 0;
		while(mIter.hasNext())
		{
			String Topic = mIter.next();
			TM_Topics.put(Topic, i);
			Out_Topics_AllUsers[i] = new BufferedWriter(new FileWriter((OutDir + "Topics_AllUsers/" + mUserType + "/" + Topic + ".txt")));
			Out_Topics_SelectedUsers[i] = new BufferedWriter(new FileWriter((OutDir + "Topics_SelectedUsers/" + mUserType + "/" + Topic + ".txt")));
			i++;
		}
	}

	/***************************************************************
	 * ReadUsers : Read UiD information of all users  
	***************************************************************/
	@SuppressWarnings("boxing")
	private static void ReadUsers(String mUserType) throws IOException
	{
		System.out.println("ReadUsers() " + mUserType);
		String fName = "G:/Data/" + mUserType + "/TotalUsers" + mUserType + ".txt";
		String readStr = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fName)));
		while((readStr = br.readLine()) != null)
		{
			Users.add(Long.parseLong(readStr));
		}
		br.close();
	}

	/***************************************************************
	 * ReadTweets : Disacard text of tweets that is not required and keep topic information only.
	***************************************************************/
	@SuppressWarnings("boxing")
	private static void ReadTweets(int fId, File mFile, String mUserType) throws IOException
	{
		//	443807874746044416	1394647017000	0	0	0	0	0	0	0	0	455;0;786;376;1152670287000;527;18;6;-25200;1;0;Pacific Time (US & Canada);San Francisco, CA;	-1	-1	NullGeo	SoundCloud	https://soundcloud.com/letsbefriendsuk/lets-be-friends-only-time?utm_source=soundcloud&utm_campaign=share&utm_medium=twitter	Have you heard Lets Be Friends | Only Time [GlobalGathering Anthem 2014] by LetsBeFriends on #SoundCloud? https://t.co/keXZ7xJPKT	
		//	441266949780029440	1394041213000	0	0	0	0	0	0	0	0	455;0;786;376;1152670287000;527;18;6;-25200;1;0;Pacific Time (US & Canada);San Francisco, CA;	-1	-1	NullGeo	NullH	http://tech.fortune.cnn.com/2014/03/03/bonobos-allen-company/?source=yahoo_quote	http://t.co/Hy5F8Drczc	

		System.out.println("ConsolidateUsersDataset.ReadTweets() : i=" + fId + " " + mFile + "  " + mUserType+" "+ new Date().toString());
		String readStr = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(mFile)));

		String fName = OutDir + "ConsolidatesTweets_" + mUserType + ".txt";
		BufferedWriter bw = new BufferedWriter(new FileWriter(fName, true));

		StringBuffer sBuf = new StringBuffer();
		while((readStr = br.readLine()) != null)
		{
			if (readStr.length() == 0)
				continue;
			sBuf.setLength(0);

			String[] k = readStr.split(RE_Sep_file);
			//First Tweet
			String[] Args_Out = k[0].split(RE_Sep_group);
			Long Uid = Long.parseLong(Args_Out[Index_Tweet_User].split(RE_Sep_record)[0]);

			//Write Selected Topics
			Set<String> NewTagsSet = new HashSet<>();
			String[] HashTags = Args_Out[Index_Tweet_HashtagEntities].split(RE_Sep_record);
			String[] Words = Args_Out[Index_Tweet_text].toLowerCase().split("\\s+");

			for (int i = 0; i < Words.length; i++)
			{
				if (Words[i].startsWith("#"))
				{
					if (SelectedHT.contains(Words[i].substring(1)))
					{
						NewTagsSet.add(Words[i].substring(1));
					}
				}
				else
				{
					if (SelectedHT.contains(Words[i]))
					{
						NewTagsSet.add(Words[i]);
					}
				}
			}
			for (int i = 0; i < HashTags.length; i++)
			{
				String Tag = HashTags[i].toLowerCase();
				if (SelectedHT.contains(Tag))
				{
					NewTagsSet.add(Tag);
				}
			}

			if (NewTagsSet.size() > 0)
			{
				Iterator<String> mIter = NewTagsSet.iterator();
				while(mIter.hasNext())
				{
					String Topic = mIter.next();
					Integer FileIndex = TM_Topics.get(Topic);
					if (FileIndex != null)
					{
						if (Users.contains(Uid))
						{
							Out_Topics_SelectedUsers[FileIndex].write(readStr + "\n\n");
						}
						Out_Topics_AllUsers[FileIndex].write(readStr + "\n\n");
					}
				}
			}

			//Write ConsolidatedDS
			if (!Users.contains(Uid))
			{
				continue;
			}
			TweetsCount++;

			for (int i = 0; i < k.length; i++)
			{
				String[] Args_In = k[i].split(RE_Sep_group);
				for (int j = 0; j < (Args_In.length - 1); j++)
				{
					sBuf.append(Args_In[j] + "\t");
				}
				if ((k.length > 1) && (i != (k.length - 1)))
				{
					sBuf.append(Sep_file + " ");
				}
			}
			bw.write(sBuf.toString().trim() + "\n");

		}
		bw.close();
		br.close();
	}

}
