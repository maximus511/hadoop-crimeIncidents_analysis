import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class CrimeIncidentsMultipleMapper{
	public static class Map extends Mapper<LongWritable, Text, Text, IntWritable>{
		private Text region = new Text();
		private final static IntWritable one = new IntWritable(1);

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
			String currentLine = value.toString();
			if(!(currentLine.startsWith("Crime ID"))){
				String[] data = currentLine.split(",");
				if(!(data[4].equals("") && data[5].equals("")) && data.length>=8){
					String eastCord = data[4];
					String northCord = data[5]; 
					String crimeType = data[7];
					region.set(eastCord.substring(0,3) + ", " + northCord.substring(0,3) + ", " + crimeType);
					context.write(region, one);
				}
			}
		}
	}

	public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable>
	{
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException{
			int crimes = 0;

			for (IntWritable i : values){
				crimes += i.get();
			}
			context.write(key, new IntWritable(crimes));
		}
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception{
		Configuration conf = new Configuration();
		Job job = new Job(conf, "CrimeIncidents");
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setJarByClass(CrimeIncidentsMultipleMapper.class);
		job.setMapperClass(Map.class);
		job.setCombinerClass(Reduce.class);
		job.setReducerClass(Reduce.class);
		job.setNumReduceTasks(2);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		TextInputFormat.setMaxInputSplitSize(job, 20000000);
		job.waitForCompletion(true);
	}
}
