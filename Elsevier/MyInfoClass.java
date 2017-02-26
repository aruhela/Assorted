/*******************
Author  : Amit Ruhela
Purpose : Contains information of each topic e.g InterAriival Time Characteristics

*******************/
package Elsevier;

public class MyInfoClass
{
	//Topic
	//public int TopicID;
	public String TopicName;
	public int Tweets;
	
	public long StartTime; // In seconds
	public long EndTime; // In Seconds

	//IAT
	public double IAT_Mean;
	public double IAT_Median;
	public double IAT_Skewness;
	public double IAT_STD;
	public double IAT_NintyPercentileAvg;
	public double IAT_Kurtosis;
	public double IAT_PopulationVariance;
	public double IAT_SumSquare;
	public double IAT_Variance;

	//Interval 
	public int StartInterval;
	public int EndInterval;
	
	public double Interval_Mean;
	public double Interval_Median;
	public double Interval_Skewness;
	public double Interval_STD;
	public double Interval_NintyPercentileAvg;
	public double Interval_Kurtosis;
	public double Interval_PopulationVariance;
	public double Interval_SumSquare;
	public double Interval_Variance;

	//Smoothing
	//public double Scale;
	public int STAWindow;
	public int LTAWindow;
	public int FREQ_IntervalTime;

	//Events
	public int BigEventsCount;
	public int AllEventsCount;
	public String EventsQuality;
	public int SavedTweets;
	public double SavedTweetsRatio;
}
