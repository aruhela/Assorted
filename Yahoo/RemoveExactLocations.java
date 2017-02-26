/**********************
Author : Amit Ruhela
Purpose : Filter out locations that are written in bad format.
Since Yahoo Service is rate-limited, therefore locations are parsed first 
to clean any junk locations.
************************/


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public final class RemoveExactLocations
{
	static String Directory = "";
	static String InputFile = Directory + "LocationsData_1_NoJunk.txt";
	static String oGoodFile = Directory + "LocationsData_1_NoJunkAndExactL.txt";
	static String oExactFile = Directory + "ExactLocations.txt";
	static String oBadLocationFile = Directory + "ExactLocations.txt";

	public static void main(String args[]) throws Exception
	{

		System.out.println("RemoveExactLocations() is starting here");
		Long startExecution = new Long(System.currentTimeMillis());
		Long endExecution;

		ProcessLocationInfo();

		endExecution = new Long(System.currentTimeMillis());
		long difference = (endExecution - startExecution) / 1000;
		System.out.println("RemoveExactLocations() Program Finished in " + difference + " seconds");
	}

	private static void ProcessLocationInfo() throws IOException, ClassNotFoundException
	{
		// Read the input file line by line
		FileInputStream fin = new FileInputStream(InputFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fin));

		BufferedWriter bGoodFile = new BufferedWriter(new FileWriter(oGoodFile));
		BufferedWriter bBadLocationFile = new BufferedWriter(new FileWriter(oBadLocationFile));
		BufferedWriter bExactFile = new BufferedWriter(new FileWriter(oExactFile));

		String str = null;
		int count = 0;
		try
		{
			while ((str = br.readLine()) != null)
			{
				count++;
				/*
				 * 19.053737,72.826761
				 * 28°37'N, 77°13'E
				 * IPHONE: 36.150379,-86.791801
				 * N 19°8' 0'' / E 72°49' 0''
				 * ÜT: 12.972381,77.607881
				 */
				String regExp1 = "(.*)\\d{1,2}[.]\\d{1,6}(.*)\\d{1,2}[.]{1,6}(.*)";
				String regExp2 = "(.*)\\d{1,2}[°](.*)\\d{1,2}[''](.*)\\d{1,2}[°](.*)\\d{1,2}[''](.*)";
				String regExp3 = "(.*)\\d{1,2}[°](.*)\\d{1,2}['](.*)\\d{1,2}[°](.*)\\d{1,2}['](.*)";
				if (str.matches(regExp1))
				{
					bExactFile.write(str + "\n");
					// System.out.println(str);
					continue;
				} else if ((str.matches(regExp2)) || (str.matches(regExp3)))
				{
					bExactFile.write(str + "\n");
					System.out.println(str);
					continue;
				} else if ((str.startsWith("WHERE")) || (str.startsWith("WHEREVER")) || (str.startsWith("ON ")) || (str.startsWith("IN "))
				        || (str.startsWith("I'M")) || (str.startsWith("EVERYWHERE")) || (str.startsWith("HTTP ")) || (str.startsWith("ÜT:"))
				        || (str.startsWith("ÜT:")) || (str.startsWith("YOUR")) || (str.startsWith("ALWAYS")) || (str.startsWith("ALMOST"))
				        || (str.matches("\\?*")) || (str.matches("\\W*")) || (str.startsWith("ÜT")))
				{
					bBadLocationFile.write(str + "\n");
					// System.out.println(str);
					continue;
				}

				else
				{
					bGoodFile.write(str + "\n");
				}

			}
		} catch (EOFException ex)
		{
			System.out.println("End of file reached.");
		}
		br.close();
		fin.close();

		bExactFile.close();
		bGoodFile.close();
		bBadLocationFile.close();
	}

}