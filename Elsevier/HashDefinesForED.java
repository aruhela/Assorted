/*******************
Author  : Amit Ruhela
Purpose : File contain all the constants and variable used in this project
*******************/
package Elsevier;

import java.util.Vector;

public class HashDefinesForED
{
	public static final boolean PerServerED = false;
	public static final String EDPrefix = "";

	public static final int Dataset = 1;
	public static int NoOfDays = 95;

	public static String MainDir = "G:/MyWork/";
	public static String eclipseDir = "G:/eclipse/workspaceLatex/";
	public static String GGUserType = null;

	public static String DataFolder = null;
	public static String OutputFolder = null;
	public static String FName_Celebrities = null;
	public static String EventsFolder = null;
	public static String FreqDataFolder = null;
	public static String EvenstsCelebDataFolder = null;
	public static String EventsFolder_Events = null;

	public static final String ScreenNamesFile = MainDir + "Analysis/Important/UserNames.txt";

	public static final String FName_TopicsInfo = "../Info.txt";
	public static final String FName_Events = "../Events.txt";
	public static final String FName_Logs = "../Logs.txt";
	public static final String FName_EvPhases = "../EventPhases.txt";
	public static final String FName_participationAll = "../ParticipationAll.txt";
	public static final String FName_participationByCelebs = "../ParticipationByCelebs.txt";
	//public static final String FName_participationIndiv = "../ParticipationIndiv.txt";
	public static final String FName_Celebrity = "../Celebrity.txt";
	public static final String FName_Proportion = "../Proportion.txt";
	public static final String FName_EventClasses = "../EventClasses.txt";
	public static final String FName_Engagement_C = "../Engagement_C.txt";
	public static final String FName_Engagement_NC = "../Engagement_NC.txt";
	public static final String FName_ClassifiedEvents = "../ClassifiedEvents.txt";
	public static final String FName_Peak2Avg = "../Peak2Avg.txt";
	public static final String FName_TestEttAndEdt = "../TestEttAndEdt.txt";
	
	public static final String FName_PhasesLengthInTime = "../PhasesLengthInTime.txt";
	public static final String FName_PhasesLengthInTweets = "../PhasesLengthInTweets.txt";
	public static final String FName_PhasesLengthInTweets_Smoothed = "../PhasesLengthInTweets_Smoothed.txt";
	
	public static final String FName_CPartAcrTopics = "../CPartAcrTopics.txt";
	public static final String FName_NCPartAcrTopics = "../NCPartAcrTopics.txt";

	public static final String FName_SubsequentInvolvementWithinEvent = "../CelebritySubsequentInvolvementWithinEvent.txt";
	public static final String FName_SubsequentInvolvementAcrossEvent = "../ParticipationAcrossEvent.txt";
	public static final String FName_ParticipationSkew = "../ParticipationSkew.txt";
	public static final String FName_ParticipationSkew_2_C = "../ParticipationSkew_2_C.txt";
	public static final String FName_ParticipationSkew_2_NC = "../ParticipationSkew_2_NC.txt";
	public static final String FName_UserInfVsTweets = "../UserInfVsTweets.txt";
	
	public static final String FName_UserInfluence = "../CelebritiesInfluence.txt";
	


	public final static int EVENT_START = 1;
	public final static int EVENT_END = 2;

	public static final int TimeOut = 3600 * 48; // 4 Hrs
	public static int TimeIndex = 0;

	//public static TreeMap<Integer, Double> EventPhase_Alpha = new TreeMap<>();
	/********   Chart Arguments *******/
	public final static int Chart_Width = 500;
	public final static int Chart_Height = 270;

	/******************* Filter *****************************/
	public static int FW_Type_WWP = 1;
	public static int FW_Type_WPW = 2;
	public static int F_Type_AVG = 1;
	public static int F_Type_MED = 2;

	/*******************Final Variables*****************************/
	public final static int INDEX_TIME = 0;
	public final static int INDEX_UID = 1;

	public final static int INDEX_ORIG = 0;
	public final static int INDEX_MEDIAN = 1;
	public final static int Index_Columns = 2;

	/*******************Peaks************************/
	public static Vector<Integer> PeakMarkers = new Vector<>();
	public static Vector<Integer> ValleysMarkers = new Vector<>();
	public static Vector<Double> EventTimeMarkers = new Vector<>();

	public final static double Threshold_up = 3;
	public final static double Threshold_down = 1.0 / 3;

	public final static String gStrFirstDay = "2013-12-22";
//	public final static double[][] CelebThr_B = { { 99.8, 2044 }, { 99.85, 3046 }, { 99.9, 5667 }, { 99.95, 16846 } };
//	public final static double[][] CelebThr_S = { { 99.8, 2170 }, { 99.85, 3226 }, { 99.9, 5746 }, { 99.95, 15964 } };
//	public final static double[][] CelebThr_P = { { 99.8, 2488 }, { 99.85, 3742 }, { 99.9, 6829 }, { 99.95, 18539 } };

	public final static double[][] CelebThr_B = { { 99.8, 2044 }, { 99.85, 3046 }, { 99.9, 5667 }, { 99.95, 16846 } };
	public final static double[][] CelebThr_S = { { 99.8, 2170 }, { 99.85, 3226 }, { 99.9, 5746 }, { 99.95, 15964 } };
	public final static double[][] CelebThr_P = { { 99.8, 2488 }, { 99.85, 3742 }, { 99.9, 6829 }, { 99.95, 18539 } };
	

	public static void SetUserType(String m, double percentile)
	{
		GGUserType = m;

		DataFolder = MainDir + "SelectedTopics/Sorted_Topics_SelectedUsers_" + GGUserType + "/";
		//OutputFolder = MainDir + "Output/EventsAnalysis_Old/" + GGUserType + "/";
		OutputFolder = MainDir + "Output/EventsAnalysis/" + percentile + "/" + GGUserType + "/";
		EventsFolder = OutputFolder + "Events/";
		FreqDataFolder = OutputFolder + "TopicFreqData/";
		EvenstsCelebDataFolder = OutputFolder + "EventsCelebrityData/";
		EventsFolder_Events = OutputFolder + "PrintofGoodEvents/";

		FName_Celebrities = MainDir + "Data/Celebrities_" + GGUserType + ".txt";

	}

}
