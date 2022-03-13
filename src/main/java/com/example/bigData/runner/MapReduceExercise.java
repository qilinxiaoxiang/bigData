package com.example.bigData.runner;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @Author: 项峥
 * @Date: 2022/3/13 16:01
 */
public class MapReduceExercise {
    public static class FlowMapper
            extends Mapper<Object, Text, Text, FlowWritable>{
        private final Text word = new Text();
        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            String line = value.toString();
            String[] split = line.split(" ");
            int length = split.length;
            word.set(split[1]);
            context.write(word, new FlowWritable(split[length-3], split[length-2]));
        }
    }

    public static class FlowReducer
            extends Reducer<Text,FlowWritable,Text,FlowWritable> {
        public void reduce(Text key, Iterable<FlowWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            FlowWritable sum = new FlowWritable();
            for (FlowWritable val : values) {
                sum.upload += val.upload;
                sum.download += val.download;
                sum.sum += val.sum;
            }
            context.write(key, sum);
        }
    }

    public static class FlowWritable implements WritableComparable<FlowWritable> {
        public long upload;
        public long download;
        public long sum;

        public FlowWritable() {
            this.upload = 0L;
            this.download = 0L;
            this.sum = 0L;
        }

        public FlowWritable(String upload, String download) {
            this.upload = Long.parseLong(upload);
            this.download = Long.parseLong(download);
            this.sum = this.upload + this.download;
        }

        @Override
        public void write(DataOutput out) throws IOException {
            out.writeChars(this.upload + " " + this.download + " " + sum);
        }

        @Override
        public void readFields(DataInput in) throws IOException {
            String line = in.readLine();
            String[] split = line.split(" ");
            this.upload = Long.parseLong(split[0]);
            this.download = Long.parseLong(split[1]);
            this.sum = Long.parseLong(split[2]);

        }

        @Override
        public int compareTo(FlowWritable that) {
            return  (Long.compare(this.sum, that.sum));
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(MapReduceExercise.class);
        job.setMapperClass(FlowMapper.class);
        job.setCombinerClass(FlowReducer.class);
        job.setReducerClass(FlowReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}