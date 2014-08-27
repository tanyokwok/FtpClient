package com.ict.test;

import java.io.IOException;
import java.net.SocketException;

import com.ict.ftp.FtpHelper;
import com.ict.zip.ZipUtil;

public class Test {

	public static void main(String [] args) throws IOException{
		if(args[2].equals("ftp")){
			test(args);
		}else{
			ZipUtil.unzip(args[0], args[1]);
		}
	}
	
	
	public static void test(String args[]) throws SocketException, IOException{
		FtpHelper ftpHelper = new FtpHelper();
		ftpHelper.connect("221.0.111.130",18507,"user2","user2@321");
		System.out.println(ftpHelper.download(args[0],args[1]));  
		System.out.println(ftpHelper.move(args[0], args[2]));
		ftpHelper.disconnect(); 
	}
}
