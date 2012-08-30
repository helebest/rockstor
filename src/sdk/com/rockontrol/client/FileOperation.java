package com.rockontrol.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rockstor.client.Bucket;
import com.rockstor.client.Contents;
import com.rockstor.client.ListAllMyBucketsResult;
import com.rockstor.client.ListBucketResult;
import com.rockstor.client.RockStor;
import com.rockstor.client.RockStorException;

public class FileOperation {

   private static final Pattern pattern = Pattern.compile("^/(\\S+?)/(.+)");

   private RockStor rs = null;

   private static class PathPair {
      private final String bucketName;
      private final String filePath;

      PathPair(String bkt, String fp) {
         bucketName = bkt;
         filePath = fp;
      }
   }

   /**
    * FileOperation constructor
    * 
    * @param address
    *           : rockstor servers's ip:port
    * @param username
    *           : login user name
    * @throws RockStorException
    */
   public FileOperation(String address, String username) throws RockStorException {
      rs = new RockStor(address, username);
   }

   // parser target directory
   private PathPair parser(String remotePath) {
      PathPair p = null;
      Matcher matcher = pattern.matcher(remotePath);
      String bucketname = null;
      String FilePath = null;
      if (matcher.find()) {
         bucketname = matcher.group(1);
         FilePath = matcher.group(2);
         p = new PathPair(bucketname, FilePath);
      }

      if (p == null)
         throw new IllegalArgumentException("cannot parse path=" + remotePath);

      return p;
   }

   // check whether bucket exist
   private boolean containBucket(String bucketname) throws RockStorException {
      boolean contain = false;
      ListAllMyBucketsResult r1 = rs.getService();
      ArrayList<Bucket> buckets = r1.getBuckets();
      if (buckets != null) {
         for (int i = 0; i < buckets.size(); i++) {
            if (buckets.get(i).getName().equals(bucketname)) {
               contain = true;
               break;
            }
         }
      }
      return contain;
   }

   // check whether file exist;
   private boolean containFile(String filePath, String bucketName)
         throws RockStorException {
      boolean contain = false;
      ListBucketResult r2 = rs.getBucket(bucketName);
      ArrayList<Contents> contents = r2.getContents();
      if (contents != null) {
         String fullFilePath = "/" + filePath;
         for (int i = 0; i < contents.size(); i++) {
            if (contents.get(i).getKey().equals(fullFilePath)) {
               contain = true;
               break;
            }
         }
      }
      return contain;
   }

   /**
    * upload local file to remote rockstor server
    * 
    * @param localPath
    *           local full path you want to upload
    * @param remotePath
    *           specified remote full path at rockstor server
    * @throws RockStorException
    * @throws IOException
    */
   public void putFile(String localPath, String remotePath) throws RockStorException,
         IOException {

      PathPair p = parser(remotePath);
      String bucketname = p.bucketName;
      String filePath = p.filePath;
      if (bucketname != null && filePath != null) {

         if (!containBucket(bucketname)) {
            rs.createBucket(bucketname);
         }
         if (containFile(filePath, bucketname)) {
            throw new IOException("FilePath = " + filePath + " already exists!");
         } else {
            FileInputStream fis = null;
            try {
               File testFile = new File(localPath);
               fis = new FileInputStream(testFile);
               rs.putObject(bucketname, filePath, null, null, null, (int) testFile
                     .length(), fis);
            } finally {
               if (fis != null) {
                  fis.close();
               }
            }
         }

      } else {
         throw new IllegalArgumentException("illegal path=" + remotePath);
      }
   }

   /**
    * delete file at rockstor server
    * 
    * @param remotePath
    *           remote full path at rockstor server
    * @throws RockStorException
    */
   public void deleteFile(String remotePath) throws RockStorException {
      PathPair p = parser(remotePath);
      String bucketname = p.bucketName;
      String filePath = p.filePath;
      if (bucketname != null && filePath != null) {
         rs.deleteObject(bucketname, filePath);
      } else {
         throw new IllegalArgumentException("illegal path=" + remotePath);
      }
   }

   /**
    * delete bucket at rockstor server
    * 
    * @param bucketname
    *           bucket is the first directory entry of the path at rockstor
    * @throws RockStorException
    * @throws IOException
    */
   public void deleteBucket(String bucketname) throws RockStorException, IOException {
      if (containBucket(bucketname)) {
         rs.deleteBucket(bucketname);
      } else {
         throw new IOException("Bucket=" + bucketname + " don't exist");
      }
   }

   /**
    * get remote file at rockstor
    * 
    * @param remotePath
    *           file's remote file path at rockstor
    * @param localPath
    *           specified local full path
    * @throws RockStorException
    * @throws IOException
    */
   public void getFile(String remotePath, String localPath) throws RockStorException,
         IOException {
      InputStream is = null;
      PathPair p = parser(remotePath);
      String bucketname = p.bucketName;
      String filePath = p.filePath;
      if (bucketname != null && filePath != null) {
         is = rs.getObject(bucketname, filePath);
         FileOutputStream fos = new FileOutputStream(new File(localPath));
         byte[] buf = new byte[4096];
         int len = 0;
         while ((len = is.read(buf)) > -1) {
            fos.write(buf, 0, len);
         }
         fos.close();
         is.close();
      }
   }
}
