package com.rockontrol.client;

public class Sample {
   
   public static void main(String[] args) throws Exception {
      String localPath = "/Users/helebest/Downloads/TR-86.1.pdf";
      String localPath2 = "/Users/helebest/Downloads/TR-86.1.bak.pdf";
      String remotePath = "/mybooks/mal/stanford/TR-86.1.pdf";
      
      FileOperation fo = new FileOperation("10.2.1.38:48080", "testuser");
      fo.putFile(localPath, remotePath);
      try {
         fo.putFile(localPath, remotePath);
      }catch(Exception e) {
         System.out.println("It's OK we got the exception :-)");
      }
      fo.getFile(remotePath, localPath2);
      fo.deleteFile(remotePath);
      fo.deleteBucket("mybooks");
   }

}
