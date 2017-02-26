/**********************
Author : Amit Ruhela
Purpose : Convert the Latitude and Longitude into City, State, and 
Country formt by using Yahoo PlaceFinder Web Service
************************/



package Clustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import Defination.globalConstants;

public class YahooReversePlaceFinder_Db
{

	public class City
	{
		int id;
		int instanceId;

		String fullName;
		int error;
		int found;

		int qualityGlobal;
		int qualityIndivi;

		String lat;
		String lng;

		int woeid;
		int woetype;

		String timeZone;
		String postal;
		String uzip;

		String city;
		String state;
		String country;
		String countryCode;
		String stateCode;

	}

	static String Directory = globalConstants.Name_OutputFolder + "/Clusters/";
	static String InputFile1 = Directory + "ClustersLocation_Uniform.txt";
	static String OutputFile1 = Directory + "ClustersLocation_Uniform_WithCityName.txt";
	static int Count = 53;
	static InputStream in2 = null;

	public static void main(String[] args)
	{
		System.out.println("YahooReversePlaceFinder_Db() is starting here");
		YahooReversePlaceFinder_Db ix = new YahooReversePlaceFinder_Db();
		ix.runExample(InputFile1);
	}

	public void runExample(String iFilaName)
	{
		try
		{
			// Read the input file line by line
			FileInputStream fstream = new FileInputStream(iFilaName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;
			String AppId = "XXXXXXX";

			//Skip Header Line
			br.readLine();

			for (int i = 0; i < Count; i++)
			{

				strLine = br.readLine(); //ClusterId	Latitude	Longitude

				String[] array = strLine.split("\t");
				String FullName = array[1] + "," + array[2];

				String v;
				URL myUrl;

				try
				{
					v = URLEncoder.encode(FullName, "UTF-8");
					myUrl = new URL("http://where.yahooapis.com/geocode?q=" + v + "&flags=T&gflags=R&appid=" + AppId);
				}
				catch (UnsupportedEncodingException e1)
				{
					System.out.println("Encoding Error");
					continue;
				}
				catch (MalformedURLException e1)
				{
					System.out.println("MalformedURLException Error");
					continue;
				}

				try
				{
					in2 = myUrl.openStream();
					System.out.println(strLine);
					parseDocument(strLine);
					in2.close();
				}
				catch (ConnectException e)
				{
					e.printStackTrace();
					System.out.println("Network Connection Error");
					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e1)
					{
						System.out.println("YahooReversePlaceFinder_Db.runExample() 1");
					}
					in2.close();
					continue;
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.out.println("BufferedWriter Error");
					in2.close();
					continue;
				}
				//break;
			}
		}
		catch (IOException e2)
		{
			System.out.println("InferenceCity_1.runExample(): File Error");
			e2.printStackTrace();
		}
	}

	// Parse the XML output of Yahoo Response
	private void parseDocument(String strLine) throws IOException
	{
		String[] array = strLine.split("\t");
		String id = array[0];
		String FullName = array[1] + "," + array[2];

		Document dom = null;
		Vector<City> v = new Vector<>();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.parse(in2);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Parsing Error");
			return;
		}
		// get the root element
		Element docEle = dom.getDocumentElement();
		String Status = getTextValue(docEle, "Error");
		if (Status.compareTo("0") == 0)
		{
			// get a node list of result
			NodeList nl = docEle.getElementsByTagName("Result");
			if (nl != null && nl.getLength() > 0)
			{
				// add all the elements to vector
				for (int i = 0; i < nl.getLength(); i++)
				{
					Element el = (Element) nl.item(i);
					City e = getCity(el);
					if (e != null)
					{
						e.id = Integer.parseInt(id);
						e.fullName = FullName;
						e.instanceId = i;
						try
						{
							e.error = Integer.parseInt(Status);
						}
						catch (NumberFormatException e1)
						{
							e.error = -1;
						}
						try
						{
							e.found = Integer.parseInt(getTextValue(docEle, "Found"));
						}
						catch (NumberFormatException e1)
						{
							e.found = -1;
						}
						try
						{
							e.qualityGlobal = Integer.parseInt(getTextValue(docEle, "Quality"));
						}
						catch (NumberFormatException e1)
						{
							e.qualityGlobal = -1;
						}
						v.add(e);
					}
				}

				if (v.size() > 0)
				{

					BufferedWriter out = new BufferedWriter(new FileWriter(OutputFile1, true));
					for (int i = 0; i < v.size(); i++)
					{

						City c = v.get(i);
						/*out.write("Id=" + c.id);
						out.write("\tinstanceId=" + c.instanceId);

						out.write("\tfullName=" + c.fullName);
						out.write("\terror=" + c.error);
						out.write("\tfound=" + c.found);

						out.write("\tgQuality=" + c.qualityGlobal);
						out.write("\tlQuality=" + c.qualityIndivi);

						out.write("\tLAT=" + c.lat);
						out.write("\tLON=" + c.lng);

						out.write("\tWoeID=" + c.woeid);
						out.write("\tWoeType=" + c.woetype);

						out.write("\tTimeZone=" + c.timeZone);
						out.write("\tpostal=" + c.postal);
						out.write("\tZip=" + c.uzip);

						out.write("\tcity=" + c.city);
						out.write("\tstate=" + c.state);
						out.write("\tcountry=" + c.country);
						out.write("\tcountrycode=" + c.countryCode);
						out.write("\tstatecode=" + c.stateCode + "\n");*/

						out.write("" + c.id);
						out.write("\t" + c.lat);
						out.write("\t" + c.lng);
						out.write("\t" + c.city);
						out.write(";" + c.state);
						out.write(";" + c.country + "\n");

					}
					out.close();

				}
			}
		}
		else
		{
			System.out.println("Invalid Entry");
			return;
		}

	}

	// Extract various attributes of a location from XML output
	private City getCity(Element ele)
	{
		City c = new City();
		try
		{
			c.lat = getTextValue(ele, "latitude");
			c.lng = getTextValue(ele, "longitude");

			try
			{
				c.woeid = Integer.parseInt(getTextValue(ele, "woeid"));
			}
			catch (Exception e)
			{
				c.woeid = -1;
			}

			try
			{
				c.woetype = Integer.parseInt(getTextValue(ele, "woetype"));
			}
			catch (Exception e)
			{
				c.woetype = -1;
			}

			try
			{
				c.qualityIndivi = Integer.parseInt(getTextValue(ele, "quality"));
			}
			catch (Exception e)
			{
				c.qualityIndivi = -1;
			}

			c.city = getTextValue(ele, "city");
			c.state = getTextValue(ele, "state");
			c.country = getTextValue(ele, "country");
			c.countryCode = getTextValue(ele, "countrycode");
			c.stateCode = getTextValue(ele, "statecode");

			c.uzip = getTextValue(ele, "uzip");
			c.postal = getTextValue(ele, "postal");
			c.timeZone = getTextValue(ele, "timezone");
		}
		catch (Exception e)
		{
			System.out.println("Error in getCity");
			e.printStackTrace();
			return null;
		}
		return c;
	}

	// Get text value of tagName from the XML Element
	private String getTextValue(Element ele, String tagName)
	{
		String textVal = "";
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0)
		{
			try
			{
				Element el = (Element) nl.item(0);
				if (el != null)
					textVal = el.getFirstChild().getNodeValue();
			}
			catch (Exception e)
			{
				System.out.println("YahooReversePlaceFinder_Db.getTextValue() 2");
			}
		}
		return textVal;
	}

	/* remove leading whitespace */
	public static String ltrim(String source)
	{
		return source.replaceAll("^\\s+", "");
	}

	/* remove trailing whitespace */
	public static String rtrim(String source)
	{
		return source.replaceAll("\\s+$", "");
	}

	/* replace multiple whitespace between words with single blank */
	public static String itrim(String source)
	{
		return source.replaceAll("\\b\\s{2,}\\b", " ");
	}

	/* remove all superfluous whitespace in source string */
	public static String trim(String source)
	{
		return itrim(ltrim(rtrim(source)));
	}

}
