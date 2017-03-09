/*******************************************
Author : Amit Ruhela
Purpose : This file process the Opencalais XML output and extract entity, topic and social tags and store them in a MySQL database.  
********************************************/


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mysql.jdbc.PreparedStatement;

public final class ProcessCalaisOutput
{
	//static int IC_Length = 1;
	static String url = "jdbc:mysql://localhost/"+IC_Name;

	static String Directory = "F:/OpenCalaisComparision/";
	static String TweetsInputFile = Directory + "Tweets.txt";
	static String CalaisOutputFile = Directory + "CalaisOutput.txt";
	static String LogFile = Directory + "Logs.txt";
	
	static BufferedWriter outLog = null;
	static int COUNT_OF_TWEETS_TO_SKIP = 0;
	static int Headerlength = 700;
	staic int BundleLength = 40_000;
	static int MaxLength = BundleLength - Headerlength;
	static String[] TweetsArray = new String[1500];
	static int[] TweetIDArray = new int[1500];
	static int COUNT_OF_READ_TWEET = 0;
	static int MAX_TWEET_COUNT = 120000;
	static int TotalOpenCalaisCalls = 0;
	static int[] StartIndex = new int[50000];

	
	static Connection connection = null;

	// List of stop words. These can be ignored if Opencalais output them as topics.
	static String[] stopWords = { "a", "about", "above", "across", "after", "afterwards", "again", "against", "all", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "amoungst", "amount", "an", "and", "another", "any", "anyhow", "anyone", "anything", "anyway", "anywhere", "are", "around", "as", "at", "back", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "below", "beside", "besides", "between", "beyond", "bill", "both", "bottom", "but", "by", "call", "can", "cannot", "cant", "co", "computer", "con", "could", "couldnt", "cry", "de", "describe", "detail", "do", "done", "down", "due", "during", "each", "eg", "eight", "either", "eleven", "else", "elsewhere", "empty", "enough", "etc", "even", "ever", "every", "everyone", "everything", "everywhere", "except", "few", "fifteen", "fify", "fill", "find", "fire", "first", "five", "for", "former", "formerly", "forty", "found", "four", "from", "front", "full", "further", "get", "give", "go", "had", "has", "hasnt", "have", "he", "hence", "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers", "herse”", "him", "himse”", "his", "how", "however", "hundred", "i", "ie", "if", "in", "inc", "indeed", "interest", "into", "is", "it", "its", "itse”", "keep", "last", "latter", "latterly", "least", "less", "ltd", "made", "many", "may", "me", "meanwhile", "might", "mill", "mine", "more", "moreover", "most", "mostly", "move", "much", "must", "my", "myse”", "name", "namely", "neither", "never", "nevertheless", "next", "nine", "no", "nobody", "none", "noone", "nor", "not", "nothing", "now", "nowhere", "of", "off", "often", "on", "once", "one", "only", "onto", "or", "other", "others", "otherwise", "our", "ours", "ourselves", "out", "over", "own", "part", "per", "perhaps", "please", "put", "rather", "re", "same", "see", "seem", "seemed", "seeming", "seems", "serious", "several", "she", "should", "show", "side", "since", "sincere", "six", "sixty", "so", "some", "somehow", "someone", "something", "sometime", "sometimes", "somewhere", "still", "such", "system", "take", "ten", "than", "that", "the", "their", "them", "themselves", "then", "thence", "there", "thereafter", "thereby", "therefore", "therein", "thereupon", "these", "they", "thick", "thin", "third", "this", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "top", "toward", "towards", "twelve", "twenty", "two", "un", "under", "until", "up", "upon", "us", "very", "via", "was", "we", "well", "were", "what", "whatever", "when", "whence", "whenever", "where", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", "whose", "why", "will", "with", "within", "without", "would", "yet", "you", "your", "yours", "yourself", "yourselves" };
	
	static Set<String> vSet = new HashSet<String>(); // HashSet to store the stop words.

	// Class for stroing the attributes of a Topic 
	static class WordData
	{
		String mKey;// Tag Name
		double mValue;// Relevance for Entity, Score for Topic and Importance
						// for SocialTag

		char mClass;// E-Entity, T-Topic, S-SocialTag
		String mEntityType;// Type for Entity
		String mEntityFullName;// Full Name of Entity

		WordData(String iKey, double iValue, char iClass, String iEntityType, String iEntityFullName) // Constructor
		{
			mKey = iKey;
			mValue = iValue;
			mClass = iClass;
			mEntityType = iEntityType;
			mEntityFullName = iEntityFullName;
		}
	}

	@SuppressWarnings("deprecation")
	public static void main(String args[]) throws IOException
	{
		log("", "ClubCalaisOnly() is starting here");
		Long startExecution = new Long(System.currentTimeMillis());
		log("\nStart Time for ", new Date(startExecution).toLocaleString().toString());

		//Add stopwords in HashSet
		for (int i = 0; i < stopWords.length; i++)
		{
			vSet.add(stopWords[i]);
		}

		// Initialize the database and process OpenCalais Output.
		try
		{
			initconnectiontoDB();
			ProcessTweetFile();
		} catch (Exception e)
		{
			e.printStackTrace();
			log("Exception", e.getCause().toString());
		}

		log("Total OpenCalais Calls ", TotalOpenCalaisCalls + "");

		Long endExecution = new Long(System.currentTimeMillis());
		long difference = (endExecution - startExecution) / 1000;
		log("ClubCalaisOnly finished at ", new Date(startExecution).toLocaleString().toString() + " The program has taken " + difference + " seconds or " + (difference / 60) + " minutes.");
	}

	private static void ProcessTweetFile() throws IOException, ClassNotFoundException
	{
		BufferedReader br_tweets = new BufferedReader(new InputStreamReader(new FileInputStream(TweetsInputFile))); // Raw file containg Tweets 
		BufferedReader br_calais = new BufferedReader(new InputStreamReader(new FileInputStream(CalaisOutputFile))); // OpenCalais Output in a file
		
		String str = null;
		while ((str = br_calais.readLine()) != null)
		{
			// CALAIS_META_DATA is keyword added by me while storing OpenCalais Output. Otherwise there is failure in OpenCalais query.
			if (!str.startsWith("CALAIS_META_DATA")) 
				continue;

			for (int i = 0; i < StartIndex.length; i++)
			{
				StartIndex[i] = 0;
			}
			int myIndex = 0;

/* ---------- Example of stored OpenCalais output in a file ------------

CALAIS_META_DATA	401713103869460483	401713103869460483	1	SUCCESS	<Results><Entity type="City" name="New Delhi,Delhi,India" relevance="0.524"><appear>New Delhi</appear></Entity><Entity type="Position" name="RT" relevance="0.524"><appear>RT</appear></Entity><Entity type="URL" name="http://t.co/q6jmutF7jT" relevance="0.381"><appear>http://t.co/q6jmutF7jT</appear></Entity><Topics><Topic Taxonomy="Calais" Score="0.511">Human Interest</Topic></Topics></Results>

CALAIS_META_DATA	401713103869460483	401713103869460483	1	SUCCESS	<Results><Entity type="City" name="New Delhi,Delhi,India" relevance="0.524"><appear>New Delhi</appear></Entity><Entity type="Position" name="RT" relevance="0.524"><appear>RT</appear></Entity><Entity type="URL" name="http://t.co/q6jmutF7jT" relevance="0.381"><appear>http://t.co/q6jmutF7jT</appear></Entity><SocialTags><SocialTag importance="2">.co<originalValue>.co</originalValue></SocialTag><SocialTag importance="2">New Delhi<originalValue>New Delhi</originalValue></SocialTag><SocialTag importance="2">Television<originalValue>Television</originalValue></SocialTag><SocialTag importance="2">Mass media<originalValue>Mass media</originalValue></SocialTag><SocialTag importance="2">Broadcasting<originalValue>Broadcasting</originalValue></SocialTag><SocialTag importance="1">T.co<originalValue>T.co</originalValue></SocialTag><SocialTag importance="1">Twitter<originalValue>Twitter</originalValue></SocialTag><SocialTag importance="1">RT<originalValue>RT (TV network)</originalValue></SocialTag></SocialTags><Topics><Topic Taxonomy="Calais" Score="0.511">Human Interest</Topic></Topics></Results>			

*/
			//CALAIS_META_DATA	StartTweetId	EndTweetID	TweetsCount	Result	OpenCalaisOutput
			//0					1				2			3			4		5	
			String[] array = str.split("\t");
			String StartTweetId = array[1];
			int COUNT_OF_READ_TWEET = Integer.parseInt(array[3]);
			String CalaisOutput = array[5];

			//Read bundle of Tweet
			String mLine = null;
			String[] mArray = null;
			while (true)
			{
				mLine = br_tweets.readLine();
				mArray = mLine.split("\\s+\\|\\s+");
				if (!mArray[0].contentEquals(StartTweetId))
					continue;
				else
				{
					myIndex = 0;
					TweetsArray[myIndex] = mArray[3];
					TweetIDArray[myIndex] = Integer.parseInt(mArray[0]);// One tweet read
					myIndex++;
					break;
				}
			}
			// Two example of Tweets 	
			//78      |       8843086 |       2009-06-08 22:00:28     |       Politico: New polls suggest Deeds surge in Va.: A SurveyUSA poll shows Deeds leading the Democratic pri.. http://tinyurl.com/ljjp53     |       0
			//79      |       9212584 |       2009-06-08 22:00:28     |       I need ctrl-f on paper articles.        |       0
			// Read COUNT_OF_READ_TWEET of tweets from the Tweets file
			for (int i = 0; i < (COUNT_OF_READ_TWEET -1); i++) // Therefore -1
			{
				mLine = br_tweets.readLine();
				mArray = mLine.split("\\s+\\|\\s+");
				TweetsArray[myIndex] = mArray[3]; // Index 3 contain the tweets content
				TweetIDArray[myIndex] = Integer.parseInt(mArray[0]);
				myIndex++;
			}
			
			//Process XML output of OpenCalais
			Document doc = null;
			try
			{
				InputStream iStream = new ByteArrayInputStream(CalaisOutput.getBytes("utf-8"));
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse(iStream);
				doc.getDocumentElement().normalize();
			} catch (Exception e)
			{
				// e.printStackTrace();
				log("ParsingError", str);
				return;
				// if error in reading, proceed to  next set of tweets.
			}
						
			TreeMap<String, WordData> myWords = new TreeMap<String, WordData>(); // Create a Hashmap of Topics for each OpenCalais output
			NodeList nodeLst = doc.getElementsByTagName("Entity");
			// System.out.println("Number of EntityTags are " + nodeLst.getLength());
			for (int s = 0; s < nodeLst.getLength(); s++)
			{
				try
				{
					Node fstNode = nodeLst.item(s);
					String FullName = fstNode.getAttributes().getNamedItem("name").getNodeValue();// type,name,relevance
					double value = Double.parseDouble(fstNode.getAttributes().getNamedItem("relevance").getNodeValue());
					String type = fstNode.getAttributes().getNamedItem("type").getNodeValue();
					NodeList fstNode2 = fstNode.getChildNodes();
					for (int t = 0; t < fstNode2.getLength(); t++)
					{
						String key = fstNode2.item(t).getFirstChild().getNodeValue();
						// fstNode2.getNodeValue();
						if (vSet.contains(key.toLowerCase())) // Discard Stop words
						{
							System.out.println("Discarding " + key);
							continue;
						}
						// Insert the topic in the topic class.
						if (myWords.get(key) == null)
						{
							myWords.put(key, new WordData(key, value, 'E', type, FullName));
						}
					}
				} catch (Exception e)
				{
					log("EntityError", str);
				}
			}

			// Insert the topics in MySQL Database
			try
			{
				String getQuery1 = "select id from topic where  name = ? and class = ? and entityType = ? and entityFullName= ?";
				String getQuery2 = "select max(id) from topic";
				String insertQueryForTopic = "insert into topic (id,name,class,entityType,entityFullName) values (?,?,?,?,?)";
				String insertQueryForTweet = "insert into tweet_has_topic (tweet_id,tweet_user_id,topic_id,value) values (?,?,?,?)";

				PreparedStatement Stmt1 = (PreparedStatement) connection.prepareStatement(getQuery1);
				Statement Stmt2 = connection.createStatement();
				PreparedStatement Stmt3 = (PreparedStatement) connection.prepareStatement(insertQueryForTopic);
				PreparedStatement Stmt4 = (PreparedStatement) connection.prepareStatement(insertQueryForTweet);

				List<WordData> mList = new ArrayList<WordData>(myWords.values());
				Iterator<WordData> iter = mList.iterator();
				while (iter.hasNext())// Per Topic
				{
					WordData w = iter.next();
					// Check if topic exist
					Stmt1.setString(1, w.mKey);
					Stmt1.setObject(2, w.mClass, java.sql.Types.CHAR);
					Stmt1.setString(3, w.mEntityType);
					Stmt1.setString(4, w.mEntityFullName);

					int topicId = 0;
					try
					{
						ResultSet rs1 = Stmt1.executeQuery();
						if (rs1.first() == false)
						{
							// Topic doesn't exist in table. Get Max id and insert
							// into table
							ResultSet rs2 = Stmt2.executeQuery(getQuery2);
							if (rs2.next())
							{
								int MaxId = rs2.getInt(1);
								topicId = MaxId + 1;

								// Insert topic in topic table
								Stmt3.setInt(1, topicId);
								Stmt3.setString(2, w.mKey);
								Stmt3.setObject(3, w.mClass, java.sql.Types.CHAR);
								Stmt3.setString(4, w.mEntityType);
								Stmt3.setString(5, w.mEntityFullName);
								Stmt3.executeUpdate();
							} else
							{
								System.out.println("Error Rs2 : ");
								continue;
							}
						} else
						{
							topicId = rs1.getInt(1);
						}
					} catch (Exception e)
					{
						log("MysqlInternalError", str);
						continue;
					}

					// Find the tweets which contain OpenCalais topics and insert them in tweet_has_topic Table
					for (int i = 0; i < COUNT_OF_READ_TWEET; i++)
					{
						String Orig_tweet = TweetsArray[i];
						if (TweetsArray[i].contains(w.mKey))
						{
							// Insert topic in tweet_has_topic table
							Stmt4.setInt(1, TweetIDArray[i]);
							Stmt4.setInt(2, 1);
							Stmt4.setInt(3, topicId);
							Stmt4.setDouble(4, w.mValue);
							try
							{
								Stmt4.executeUpdate();
							} catch (java.sql.SQLException e)
							{
								System.out.println("ERROR : Duplicate entry for Tweet_Has_Topic");
								continue;
							}
						}
					}
				}
				Stmt1.close();
				Stmt2.close();
				Stmt3.close();
				Stmt4.close();
			} catch (java.sql.SQLException e)
			{
				System.out.println("Get_Calais.createDOMObject(): Exception");
				e.printStackTrace();
			}
		}
		br_calais.close();
		br_tweets.close();

	}

	/**************************************************************
		Connect with MySQL database
	***************************************************************/
	
	private static void initconnectiontoDB()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager.getConnection(url, "root", "root");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Database Initialized");
	}

	/**************************************************************
		Write Logs to file
	***************************************************************/

	public static void log(String reason, String strLine)
	{
		try
		{
			System.out.println(reason + "\t:\t" + strLine + "\n");
			BufferedWriter out = new BufferedWriter(new FileWriter(LogFile, true));
			out.write(reason + "\t:\t" + strLine + "\n");
			out.close();
		} catch (IOException e)
		{
			System.out.println("Logging Error :" + strLine);
		}
		return;
	}

}