/**********************
Author : Amit Ruhela
Purpose : Convert the non-formatted geo locations specified on twitter to 
latitude and longitude format using Yahoo Placefilder web service
************************/

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
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.mysql.jdbc.PreparedStatement;

public class YahooPlaceFinder_Db
{

	static Connection connection = null;
	static String url = "jdbc:mysql://localhost/locationdb";
	static int linestoskip = 942;
	static String Directory = "F:/Location/Processing1/";
	static String InputFile = Directory + "DetailsOfResidualLocations.txt";
	static String ErrorFile = Directory + "Log.txt";

	public class City
	{
		int id;
		int instanceId;

		String FullName;
		String PrunedName;
		int error;
		int found;
		int QualityGlobal;

		String lat;
		String lng;

		String offsetLat;
		String offsetLng;

		int radius;
		int woeid;
		int woetype;

		String timeZone;
		int qualityIndivi;

		String city;
		String state;
		String country;
		int Postal;
	};

	InputStream in2 = null;

	@SuppressWarnings("deprecation")
	public static void main(String[] args)
	{
		System.out.println("YahooPlaceFinder_Db.main() is starting here");
		Long startExecution = new Long(System.currentTimeMillis());
		Long endExecution;
		log(new Date(startExecution).toLocaleString().toString(), "\nStart Time for " + InputFile);

		initconnectiontoDB();
		// System.setProperty("http.proxyHost", "XX.XX.XX.XX.XX");
		// System.setProperty("http.proxyPort", "3128");
		// System.setProperty("http.proxyUser", "XXXXXXXX");
		// System.setProperty("http.proxyPassward", "XXXXXXX");

		try
		{
			YahooPlaceFinder_Db ix = new YahooPlaceFinder_Db();
			ix.runExample();
		} catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}

		endExecution = new Long(System.currentTimeMillis());
		long difference = (endExecution - startExecution) / 1000;
		System.out.println("\nYahooPlaceFinder_Db.main() Program Finished in " + difference + " seconds");
		log(new Date(endExecution).toLocaleString().toString(), "End Time for " + InputFile + "  ");
		log((difference / 60) + " minutes", "Time Taken");
	}

	public void runExample()
	{
		try
		{
			// Read the input file line by line
			FileInputStream fstream = new FileInputStream(InputFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;

			String AppId = "XXXXXX";
			for (int i = 0; i < linestoskip; i++)
			{
				br.readLine();
			}

			int count = 0;
			while ((strLine = br.readLine()) != null)// <ID> <LocationName>
			{

				count++;
				String[] array = strLine.split("\t:\t");
				String PrunedName = array[1];

				String v;
				URL myUrl;
				try
				{
					v = URLEncoder.encode(PrunedName, "UTF-8");
					myUrl = new URL("http://where.yahooapis.com/geocode?location=" + v + "&flags=T&appid=" + AppId);
				} catch (UnsupportedEncodingException e1)
				{
					log(strLine, "Encoding Error");
					continue;
				} catch (MalformedURLException e1)
				{
					log(strLine, "MalformedURLException Error");
					continue;
				}

				try
				{
					in2 = myUrl.openStream();// exception java.net.ConnectException: Connection timed out: connect
					parseDocument(strLine, in2);
					in2.close();
				} catch (ConnectException e)
				{
					e.printStackTrace();
					log(strLine, "Network Connection Error");
					try
					{
						Thread.sleep(1000);
					} catch (InterruptedException e1)
					{
					}
					in2.close();
					continue;

				} catch (Exception e)
				{
					e.printStackTrace();
					log(strLine, "BufferedWriter Error");
					in2.close();
					continue;
				}

				if (count > 50000)
					break;
			}
		} catch (IOException e2)
		{
			System.out.println("InferenceCity_1.runExample(): File Error");
			e2.printStackTrace();
		}
	}

	private void parseDocument(String strLine, InputStream in2) throws IOException
	{
		String[] array = strLine.split("\t:\t");
		String id = array[0];
		String PrunedName = array[1];
		String FullName = array[2];

		Document dom = null;
		Vector<City> v = new Vector<City>();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.parse(in2);
		} catch (Exception e)
		{
			e.printStackTrace();
			log(strLine, "Parsing Error");
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
					City e = getCity(el, strLine);
					if (e != null)
					{
						e.id = Integer.parseInt(id);
						e.PrunedName = PrunedName;
						e.FullName = FullName;
						e.instanceId = i;
						try
						{
							e.error = Integer.parseInt(Status);
						} catch (NumberFormatException e1)
						{
							e.error = -1;
						}
						try
						{
							e.found = Integer.parseInt(getTextValue(docEle, "Found"));
						} catch (NumberFormatException e1)
						{
							e.found = -1;
						}
						try
						{
							e.QualityGlobal = Integer.parseInt(getTextValue(docEle, "Quality"));
						} catch (NumberFormatException e1)
						{
							e.QualityGlobal = -1;
						}
						v.add(e);
					}
				}

				if (v.size() > 0)
				{
					try
					{
						String insertQueryForLocation = "insert into CityInfo (id,instance,FullName,PrunedName,Error,Found,GlobalQuality,Lat,Lng,oLat,oLng,Radius,WoeId, WoeType,TimeZone,IndivQuality,City,State,Country,Postal) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						PreparedStatement Stmt = (PreparedStatement) connection.prepareStatement(insertQueryForLocation);

						for (int i = 0; i < v.size(); i++)
						{
							City c = v.get(i);
							Stmt.setInt(1, c.id);
							Stmt.setInt(2, c.instanceId);
							Stmt.setString(3, c.FullName);
							Stmt.setString(4, c.PrunedName);
							Stmt.setInt(5, c.error);
							Stmt.setInt(6, c.found);
							Stmt.setInt(7, c.QualityGlobal);
							Stmt.setString(8, c.lat);
							Stmt.setString(9, c.lng);
							Stmt.setString(10, c.offsetLat);
							Stmt.setString(11, c.offsetLng);
							Stmt.setInt(12, c.radius);
							Stmt.setInt(13, c.woeid);
							Stmt.setInt(14, c.woetype);
							Stmt.setString(15, c.timeZone);
							Stmt.setInt(16, c.qualityIndivi);
							Stmt.setString(17, c.city);
							Stmt.setString(18, c.state);
							Stmt.setString(19, c.country);
							Stmt.setInt(20, c.Postal);

							Stmt.executeUpdate();
						}
						Stmt.close();
						insertQueryForLocation = null;
					} catch (Exception e)
					{
						log(strLine, "Mysql Error");
						// e.printStackTrace();
					}
				}
			} else
			{
				log(strLine, "Zero Element");
				return;
			}
		} else
		{
			log(strLine, "Invalid Entry");
			return;
		}

	}

	private City getCity(Element ele, String strLine)
	{
		City c = new City();

		try
		{
			c.lat = getTextValue(ele, "latitude");
			c.lng = getTextValue(ele, "longitude");
			c.offsetLat = getTextValue(ele, "offsetlat");
			c.offsetLng = getTextValue(ele, "offsetlon");

			try
			{
				c.radius = Integer.parseInt(getTextValue(ele, "radius"));
			} catch (Exception e)
			{
				c.radius = -1;
			}

			try
			{
				c.woeid = Integer.parseInt(getTextValue(ele, "woeid"));
			} catch (Exception e)
			{
				c.radius = -1;
			}

			try
			{
				c.woetype = Integer.parseInt(getTextValue(ele, "woetype"));
			} catch (Exception e)
			{
				c.woetype = -1;
			}

			try
			{
				c.qualityIndivi = Integer.parseInt(getTextValue(ele, "quality"));
			} catch (Exception e)
			{
				c.qualityIndivi = -1;
			}

			c.city = getTextValue(ele, "city");
			c.state = getTextValue(ele, "state");
			c.country = getTextValue(ele, "country");
			try
			{
				c.Postal = Integer.parseInt(getTextValue(ele, "postal"));
			} catch (Exception e)
			{
				c.Postal = -1;
			}

			c.timeZone = getTextValue(ele, "timezone");
		} catch (Exception e)
		{
			log(strLine, "Error in getCity");
			e.printStackTrace();
			return null;
		}
		return c;
	}

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
			} catch (Exception e)
			{
			}
		}
		return textVal;
	}

	public static void log(String strLine, String reason)
	{
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(ErrorFile, true));
			out.write(reason + "\t:\t" + strLine + "\n");
			out.close();

			System.out.println(reason + "\t:\t" + strLine + "\n");
		} catch (IOException e)
		{
			System.out.println("Logging Error :" + strLine);
		}

		return;
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

}
