package com.scottmyers.app;

import com.amazonaws.services.s3.AmazonS3;
import org.apache.commons.cli.*;

import java.io.PrintWriter;

public class App 
{
    public static final Option ARG_ACCESSKEY = new Option("a", "accesskey",true, "Set Access Key.  Usage:  -a XXXXXXXXX");
    public static final Option ARG_SECRETKEY = new Option("s", "secretkey",true, "Set Access Key.  Usage:  -s ZZZZZZZZZ");
    public static final Option ARG_REGION = new Option("r", "region",true, "Set Region.  Usage:  -r US_WEST_1");
    public static final Option ARG_HELP = new Option("h", "help",false, "Show help for command.");

    public static Options options = new Options();

    public static void main( String[] args ) throws ParseException {

        options.addOption(ARG_ACCESSKEY);
        options.addOption(ARG_SECRETKEY);
        options.addOption(ARG_REGION);
        options.addOption(ARG_HELP);
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("h")) {printHelp(options); System.exit(0);}

        if(!cmd.hasOption("r") || !cmd.hasOption("a") || !cmd.hasOption("s")){
            printHelp(options); System.exit(0);
        }

        if(cmd.hasOption("r") && cmd.hasOption("a") && cmd.hasOption("s")){
            String regionAWS =  cmd.getOptionValue("r");
            String accessKey = cmd.getOptionValue("a");
            String secretKey = cmd.getOptionValue("s");

            AmazonS3 s3client = S3Operations.s3initiator(accessKey,secretKey,regionAWS);

            S3Operations.listBuckets(s3client);
            S3Operations.createBucket(s3client, "buckettest01201");
            S3Operations.listBuckets(s3client);
            S3Operations.deleteBucket(s3client, "buckettest01201");
            S3Operations.listBuckets(s3client);

        }

    }
        public static void printHelp(Options options) {
            HelpFormatter formatter = new HelpFormatter();
            PrintWriter pw = new PrintWriter(System.out);
            pw.println("\nS3 Tools v.0.5");
            pw.println("\ngithub.com/srslol");
            pw.println();
            formatter.printUsage(pw, 120, "java [-jar] application[.java] [Option] ");
            formatter.printWrapped(pw, 120, "Example: java App.java -w /tmp/watch-folder ");
            pw.println();
            formatter.printOptions(pw, 120, options, 2, 6);
            pw.close();
        }
}