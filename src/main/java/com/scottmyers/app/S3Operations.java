package com.scottmyers.app;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.StringUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class S3Operations {


    public static AmazonS3 s3initiator(String accessKey, String secretKey, String regionAWS) {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        // AmazonS3 s3client = new AmazonS3Client(credentials);
        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.valueOf(regionAWS))  //.fromName(regionAWS))
                .build();
        return s3client;
    }
    public static boolean checkStringUppercase (String str) {
        char ch;
        boolean capitalFlag = false;
        boolean lowerCaseFlag = false;
        boolean numberFlag = false;
        for(int i=0;i < str.length();i++) {
            ch = str.charAt(i);
            if( Character.isDigit(ch)) {
                numberFlag = true;
            }
            else if (Character.isUpperCase(ch)) {
                capitalFlag = true;
            } else if (Character.isLowerCase(ch)) {
                lowerCaseFlag = true;
            }
            if(numberFlag && capitalFlag && lowerCaseFlag)
                return true;
        }
        return false;
    }

    public static boolean s3CheckifValidBucketName (String name) {
        int len = name.length();
        if (len<2 || len>64) {writeLog("Invalid filename length"); return false;}
        if (name.contains("..")) {writeLog("No adjacent periods"); return false;}
        if (name.contains("_")) {writeLog("No underscores");return false;}
        if (name.contains("-.")) {writeLog("Period next to dash not allowed"); return false;}
        if (name.contains(".-")) {writeLog("Period next to dash not allowed"); return false;}
        if (checkStringUppercase(name)) {writeLog("No Uppercase allowed"); return false;}
        return true;
    }

    public static void listBuckets(AmazonS3 s3client) {
        try {
            List<Bucket> buckets = s3client.listBuckets();
            for (Bucket bucket : buckets) {
                writeLog("Bucket Name:  "+ bucket.getName());
            }
        }
        catch (AmazonServiceException e) {
            System.out.println(e.getErrorMessage());
        }
    }

    public static void listBucketsDetailed(AmazonS3 s3client) {
        try {
            List<Bucket> buckets = s3client.listBuckets();
            for (Bucket bucket : buckets) {
                System.out.println(bucket.getName());
                System.out.println("-------------------------");
                System.out.println("Bucket Name:"+bucket.getName());
                System.out.println("Created:"+ StringUtils.fromDate(bucket.getCreationDate()));
                System.out.println("Region:"+s3client.getRegionName());
                System.out.println("Owner:"+s3client.getS3AccountOwner());
                // System.out.println("URL:"+s3client.getUrl(String.valueOf(bucket),accessKey));
            }
        }
        catch (AmazonServiceException e) {
            System.out.println(e.getErrorMessage());
        }
    }

    public static void createBucket(AmazonS3 s3client, String bucketName) {
        if (s3CheckifValidBucketName(bucketName)) {
            try {
                if(!s3client.doesBucketExist(bucketName)) {
                    s3client.createBucket(bucketName);
                }
                else {
                    writeLog("Bucket name is not available.");
                    return;
                }
            }
            catch (AmazonServiceException e) {
                writeLog(e.getErrorMessage());
            }
        }
        else {
            writeLog("Invalid bucket name.");
        }
    }

    public static void deleteMultipleBuckets(AmazonS3 s3client, ArrayList<String> bucketNames) {
        try {
            for (String bucket : bucketNames) {
                deleteBucket(s3client, bucket);
            }
        }
        catch (AmazonServiceException e) {
            writeLog(e.getErrorMessage());
        }


    }
    public static void deleteBucket(AmazonS3 s3client, String bucketName) {
        try {
            if(!s3client.doesBucketExist(bucketName)) {
                writeLog("Bucket name is not available.");
            }
            else {
                s3client.deleteBucket(bucketName);
            }
        }
        catch (AmazonServiceException e) {
            writeLog(e.getErrorMessage());
        }
    }

    public static void uploadObject (AmazonS3 s3client, String bucketName, String destinationFilename, String localFilename) {
        try {
            if(!s3client.doesObjectExist(bucketName, destinationFilename)) {
                s3client.putObject(bucketName,destinationFilename,new File(localFilename));
            }
            else {
                writeLog("Cannot upload this object to bucket.");
            }
        }
        catch (AmazonServiceException e) {
            writeLog(e.getErrorMessage());
        }

    }

    public static void listObjectsInBucket (AmazonS3 s3client, String bucketName) {
        try {
            ObjectListing objectListing = s3client.listObjects(bucketName);
            for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
                writeLog(os.getKey());
            }
        }
        catch (AmazonServiceException e) {
            writeLog("Unable to list objects: " + bucketName + " " + e.getErrorMessage());
        }
    }

    public static void downloadObject(AmazonS3 s3client, String bucketName, String sourceFile, String destinationFile) throws IOException {
        try {
            S3Object s3object = s3client.getObject(bucketName, sourceFile);
            S3ObjectInputStream inputStream = s3object.getObjectContent();
            FileUtils.copyInputStreamToFile(inputStream, new File(destinationFile));
            writeLog("File Downloaded: " + destinationFile);
        }
        catch (AmazonServiceException e) {
            writeLog("Unable to download file: " + destinationFile + " " + e.getErrorMessage());
        }
    }

    public static void copyObject(AmazonS3 s3client, String sourceBucketName,String destinationBucketName,  String sourceFile, String destinationFile) {
        try {
            s3client.copyObject(sourceBucketName, sourceFile, destinationBucketName, destinationFile);
        }
        catch (AmazonServiceException e) {
            writeLog("Unable to download file: " + destinationFile + " " + e.getErrorMessage());
        }
    }

    public static void moveObject(AmazonS3 s3client, String sourceBucketName,String destinationBucketName,  String sourceFile, String destinationFile) {
        try {
            s3client.copyObject(sourceBucketName, sourceFile, destinationBucketName, destinationFile);
            deleteObject(s3client, sourceBucketName, sourceFile);
        }
        catch (AmazonServiceException e) {
            writeLog("Unable to download file: " + destinationFile + " " + e.getErrorMessage());
        }
    }

    // Rename by copy to new filename in same bucket then delete original.
    public static void renameObject(AmazonS3 s3client, String sourceBucketName,  String sourceFile, String destinationFile) {
        try {
            s3client.copyObject(sourceBucketName, sourceFile, sourceBucketName, destinationFile);
        }
        catch (AmazonServiceException e) {
            writeLog("Unable to download file: " + destinationFile + " " + e.getErrorMessage());
        }
    }

    public static void deleteObject (AmazonS3 s3client, String sourceBucketName, String sourceFile) {
        try {
            s3client.deleteObject(sourceBucketName, sourceFile);
        }
        catch (AmazonServiceException e) {
            writeLog("Unable to download file: " + sourceFile + " | Bucket Name: " + sourceBucketName +  e.getErrorMessage());
        }
    }

    public static void writeLog(String output) {
        System.out.println("Log --> " + output);
    }

    enum region {
        AF_SOUTH_1	,
        AP_EAST_1	,
        AP_NORTHEAST_1	,
        AP_NORTHEAST_2	,
        AP_NORTHEAST_3	,
        AP_SOUTH_1	,
        AP_SOUTHEAST_1	,
        AP_SOUTHEAST_2	,
        AP_SOUTHEAST_3	,
        CA_CENTRAL_1	,
        CN_NORTH_1	,
        CN_NORTHWEST_1	,
        EU_CENTRAL_1	,
        EU_NORTH_1	,
        EU_SOUTH_1	,
        EU_WEST_1	,
        EU_WEST_2	,
        EU_WEST_3	,
        GovCloud	,
        ME_SOUTH_1	,
        SA_EAST_1	,
        US_EAST_1	,
        US_EAST_2	,
        US_GOV_EAST_1	,
        US_ISO_EAST_1	,
        US_ISO_WEST_1	,
        US_ISOB_EAST_1,
        US_WEST_1	,
        US_WEST_2	,
    }
}