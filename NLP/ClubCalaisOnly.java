/*******************************************
Author : Amit Ruhela
Purpose : Manual identification of topic for each tweeet in a dataset of 196Million tweets is impossible. Therefore, OpenCalais web service was ussd to extract entities and tags for all the tweets. Since OpenCalais is rate limited, therefore optimiun size bundle of tweets are clubbed and queried to OpenCalais. The RDF response is parsed(different file) to get topics from the tweeets.

********************************************/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;

//Total OpenCalais Calls 	:	6535
//ClubCalaisOnly finished at 	:	10 Apr, 2012 8:34:16 PM The program has taken 1368 minutes.

public final class ClubCalaisOnly
{
	static String DirectoryName = null;
	static String TweetsInputFile = null;
	static String CalaisOutputFile = null;
	static String LogFile = null;

	static int COUNT_OF_TWEETS_TO_SKIP = 0;
	static int IC_Length = 40;
	static String IC_Name = "forty";
	static int MaxLength = IC_Length * 1000 - 700;// 700 for header
	//static int[] TweetIDArray = new int[50000];
	//static int COUNT_OF_READ_TWEET = 0;
	static int MAX_TWEET_COUNT = 1742828;
	//static int MAX_TWEET_COUNT = 600;
	static int TotalOpenCalaisCalls = 0;
	static BufferedWriter outLog = null;
	static int trial = 0;

	static int startIndex = 0;
	static int endIndex = 0;

	@SuppressWarnings("deprecation")
	public static void main(String args[])
	{

		Long startExecution = new Long(System.currentTimeMillis());
		try
		{
			TweetsInputFile = "Calais/S_TweetsContent.txt";
			CalaisOutputFile = "Calais/CalaisOut_" + IC_Name + ".txt";
			LogFile = "Calais/LogCalais_" + IC_Name + ".txt";
			log("\n>>>>>>>", "ClubCalaisOnly() is starting here");
			System.out.println("\nInput=" + TweetsInputFile + "\nCalaisOutputFile=" + CalaisOutputFile + "\nLogFile=" + LogFile);
			log("Start Time for ", new Date(startExecution).toLocaleString().toString());

			ProcessTweetFile();

		}
		catch (Exception e)
		{
			log("Exception", stack2string(e));
		}

		log("Total OpenCalais Calls ", TotalOpenCalaisCalls + "");
		Long endExecution = new Long(System.currentTimeMillis());
		long difference = (endExecution - startExecution) / 1000;
		log("ClubCalaisOnly finished at ", new Date(endExecution).toLocaleString().toString() + " The program has taken " + (difference / 60) + " minutes.");

	}

	private static void ProcessTweetFile() throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(TweetsInputFile)));
		String str = null;
		StringBuffer BigStr = new StringBuffer();
		int myIndex = 0;
		for (int i = 0; i < COUNT_OF_TWEETS_TO_SKIP; i++)
		{
			br.readLine();
		}

		System.out.println("Skipped Tweets = " + COUNT_OF_TWEETS_TO_SKIP);
		while (true)
		{
			str = br.readLine();
			if (str == null)
			{
				endIndex = myIndex;
				trial = 0;
				CallCalaisApi(BigStr);

				System.out.println("All Tweets are processed");
				break;
			}

			if ((URLEncoder.encode((BigStr + tweet + " \n "), "UTF-8").length()) > MaxLength) // Length Exceeded
			{
				endIndex = myIndex;

				trial = 0;
				CallCalaisApi(BigStr);

				BigStr.setLength(0);
				BigStr.append(" " + tweet);
				startIndex = myIndex + 1;
			}
			else
			{
				BigStr.append(" \n " + tweet);
			}
			myIndex++;
		}
		br.close();
	}

	private static void CallCalaisApi(StringBuffer tweet) throws IOException
	{
		String data;
		StringBuffer Response = new StringBuffer(100000);
		StringBuffer CalaisResult = new StringBuffer(100000);
		Response.append("CALAIS_META_DATA\t" + startIndex + "\t" + endIndex + "\t" + (endIndex - startIndex + 1) + "\t");
		try
		{
			data = URLEncoder.encode("licenseID", "UTF-8") + "=" + URLEncoder.encode("xvkzy211a9uugmnvwzdz3b8767", "UTF-8");
			data += "&" + URLEncoder.encode("content", "UTF-8") + "=" + URLEncoder.encode(tweet.toString(), "UTF-8");
			data += "&"
					+ URLEncoder.encode("paramsXML", "UTF-8")
					+ "="
					+ URLEncoder
							.encode("<c:params xmlns:c=\"http://s.opencalais.com/1/pred/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"><c:processingDirectives c:contentType=\"TEXT/TXT\" c:outputFormat=\"text/gnosis\"></c:processingDirectives><c:userDirectives c:allowDistribution=\"true\" c:allowSearch=\"true\" c:externalID=\"calaisbridge\" c:submitter=\"calaisbridge\"></c:userDirectives><c:externalMetadata c:caller=\"GnosisFirefox\"/></c:params>",
									"UTF-8");

			// Send data
			URL url = new URL("http://api.opencalais.com/enlighten/rest/");
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.flush();

			TotalOpenCalaisCalls++;
			System.out.println("TotalOpenCalaisCalls=" + TotalOpenCalaisCalls + "\tStartIndex=" + startIndex + "\tCount=" + (endIndex - startIndex + 1));
			// Get the response
			BufferedReader b = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String temp;
			while ((temp = b.readLine()) != null)
			{
				CalaisResult.append(temp);
			}
			b.close();

			Response.append("SUCCESS\t");
			Response.append(CalaisResult);
			Response.append("\n");
			WriteCalaisResponse(Response);

			CalaisResult = null; // Clear
			Response = null; // Clear
		}
		catch (UnsupportedEncodingException e)
		{
			log("Error in URLEncoder.encode", "");
			Response.append("FAILURE\n");
			WriteCalaisResponse(Response);

			CalaisResult = null; // Clear
			Response = null; // Clear
			return;

		}
		catch (MalformedURLException e)
		{
			log("Error MalformedURLException", "");
			Response.append("FAILURE\n");
			WriteCalaisResponse(Response);
			CalaisResult = null; // Clear
			Response = null; // Clear
			return;

		}
		catch (IOException e)
		{
			if (trial < 10)
			{
				trial += 1;
				try
				{
					System.out.println(Response + "SLEEP " + trial + "\n");
					Thread.sleep(trial * 10 * 1000);// multiple of 30 seconds
				}
				catch (InterruptedException e1)
				{
				}
				CalaisResult = null; // Clear
				Response = null; // Clear
				CallCalaisApi(tweet);
			}
			else
			{
				log("Error IOException", "");
				Response.append("FAILURE\n");
				WriteCalaisResponse(Response);
				CalaisResult = null; // Clear
				Response = null; // Clear
				throw e;
			}
		}

		CalaisResult = null; // Clear
		Response = null; // Clear
	}

	public static void log(String reason, String strLine)
	{
		try
		{
			System.out.println(reason + "\t:\t" + strLine + "\n");
			BufferedWriter out = new BufferedWriter(new FileWriter(LogFile, true));
			out.write(reason + "\t:\t" + strLine + "\n");
			out.close();
		}
		catch (IOException e)
		{
			System.out.println("Logging Error :" + strLine);
		}
		return;
	}

	public static void WriteCalaisResponse(StringBuffer strLine)
	{
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(CalaisOutputFile, true));
			out.write(strLine.toString());
			out.close();
		}
		catch (IOException e)
		{
			System.out.println("Logging Error :" + strLine);
		}
		return;
	}

	public static String stack2string(Exception e)
	{
		try
		{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return "\n------\n" + sw.toString() + "\n------\n";
		}
		catch (Exception e2)
		{
			return "bad stack2string";
		}
	}
}
