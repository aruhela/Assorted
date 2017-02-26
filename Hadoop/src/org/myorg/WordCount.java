package org.myorg;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordCount
{
	static String[] stopWords = { "a", "about", "above", "across", "after", "afterwards", "again", "against", "all", "almost", "alone", "along", "already", "also",
			"although", "always", "am", "among", "amongst", "amoungst", "amount", "an", "and", "another", "any", "anyhow", "anyone", "anything", "anyway", "anywhere",
			"are", "around", "as", "at", "back", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "below",
			"beside", "besides", "between", "beyond", "bill", "both", "bottom", "but", "by", "call", "can", "cannot", "cant", "co", "computer", "con", "could", "couldnt",
			"cry", "de", "describe", "detail", "did", "do", "does", "doesn't", "done", "down", "due", "during", "each", "eg", "eight", "either", "eleven", "else",
			"elsewhere", "empty", "enough", "etc", "even", "ever", "every", "everyone", "everything", "everywhere", "except", "few", "fifteen", "fify", "fill", "find",
			"fire", "first", "five", "for", "former", "formerly", "forty", "found", "four", "from", "front", "full", "further", "get", "give", "go", "had", "has",
			"hasnt", "have", "he", "hence", "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers", "herse”", "him", "himse”", "his", "how", "however",
			"hundred", "i", "ie", "if", "i'm", "in", "inc", "indeed", "interest", "into", "is", "it", "its", "itse”", "keep", "just", "last", "latter", "latterly",
			"least", "less", "look", "ltd", "made", "many", "may", "me", "meanwhile", "might", "mill", "mine", "more", "moreover", "most", "mostly", "move", "much",
			"must", "my", "myse”", "name", "namely", "neither", "never", "nevertheless", "next", "nine", "no", "nobody", "none", "noone", "nor", "not", "nothing", "now",
			"nowhere", "of", "off", "often", "on", "once", "one", "only", "onto", "or", "other", "others", "otherwise", "our", "ours", "ourselves", "out", "over", "own",
			"part", "per", "perhaps", "please", "pls", "plz", "put", "rather", "re", "retweet", "rt", "said", "same", "see", "seem", "seemed", "seeming", "seems",
			"serious", "several", "she", "should", "show", "side", "since", "sincere", "six", "sixty", "so", "some", "somehow", "someone", "something", "sometime",
			"sometimes", "somewhere", "still", "such", "system", "take", "ten", "than", "that", "the", "their", "them", "themselves", "then", "thence", "there",
			"thereafter", "thereby", "therefore", "therein", "thereupon", "these", "they", "thick", "thin", "third", "this", "those", "though", "three", "through",
			"throughout", "thru", "thus", "to", "took", "together", "too", "top", "toward", "towards", "twelve", "tweet", "tweeter", "twenty", "twitter", "two", "un",
			"under", "until", "up", "upon", "us", "very", "via", "was", "we", "well", "were", "what", "whatever", "when", "whence", "whenever", "where", "whereafter",
			"whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", "whose", "why", "will",
			"with", "within", "without", "would", "yet", "you", "your", "yours", "yourself", "yourselves" };

	static Set<String> vSet = new HashSet<>();

	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable>
	{

		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException
		{
			StringTokenizer itr = new StringTokenizer(value.toString());
			while(itr.hasMoreTokens())
			{
				String mWord = itr.nextToken();
				String newWord = mWord.replaceAll("^[^\\p{L}\\s%]+|[^\\p{L}\\s%]+$", "").toLowerCase().trim();

				if ((newWord.length() > 2)&&(!vSet.contains(newWord)))
				{
					word.set(newWord);
					context.write(word, one);
				}
			}
		}
	}

	public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable>
	{
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
		{
			int sum = 0;
			for (IntWritable val : values)
			{
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception
	{
		// Read Stopwords
		for (int i = 0; i < stopWords.length; i++)
		{
			vSet.add(stopWords[i]);
		}

		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "word count");
		job.setJarByClass(WordCount.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}