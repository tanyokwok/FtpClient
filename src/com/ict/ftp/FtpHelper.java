package com.ict.ftp;

import java.awt.geom.Path2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

public class FtpHelper {
	
	private static Logger logger = Logger.getLogger(FtpHelper.class);
	/**
	 * ftp server的ip
	 */
	private String serverIp;
	/**
	 * 服务的端口
	 */
	private int port;
	/**
	 * 所使用的账号
	 */
	private String userName;
	/**
	 * 所使用的账号密码
	 */
	private String password;
	
	private FTPClient ftpClient = new FTPClient();
	
	
	public FtpHelper() {
		//设置将过程中使用到的命令输出到控制台 
		ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out))); 
	}
	
	
	/**
	 * @param serverIp ftp server的ip
	 * @param userName 所使用的账号
	 * @param password 所使用的账号密码
	 * @return 是否连接成功 
	 * @throws IOException 
	 * @throws SocketException 
	 */
	public boolean connect(String serverIp,String userName,String password) throws SocketException, IOException{
		this.serverIp = serverIp;
		this.userName = userName;
		this.password = password;
		ftpClient.connect(this.serverIp);
		ftpClient.setControlEncoding("GBK"); 
		if(FTPReply.isPositiveCompletion(ftpClient.getReplyCode())){
			if(ftpClient.login(this.userName, this.password)){   
                return true;   
            }   
		}
		disconnect();
		return false;
	}
	/**
	 * 
	 * @param serverIp ftp server的ip
	 * @param port  服务的端口
	 * @param userName 所使用的账号
	 * @param password 所使用的账号密码
	 * @throws IOException 
	 * @return 是否连接成功 
	 * @throws SocketException 
	 */
	public boolean connect(String serverIp,int port,String userName,String password) throws SocketException, IOException{
		this.serverIp = serverIp;
		this.port = port;
		this.userName = userName;
		this.password = password;
		ftpClient.connect(this.serverIp,this.port);
		ftpClient.setControlEncoding("GBK"); 
		if(FTPReply.isPositiveCompletion(ftpClient.getReplyCode())){
			if(ftpClient.login(this.userName, this.password)){ 
				//设置被动模式   
		        ftpClient.enterLocalPassiveMode();   
		        //设置以二进制方式传输   
		        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);   
                return true;   
            }   
		}
		disconnect();
		return false;
	}
	
	/** 
     * 断开与远程服务器的连接 
     * @throws IOException 
     */  
	 public void disconnect() throws IOException{   
	        if(ftpClient.isConnected()){   
	        	ftpClient.logout();
	            ftpClient.disconnect();   
	        }   
	}
	 
	 /**
	  * 根据文件路径自动判断下载文件或者目录
	  * @param remote 远程路径
	  * @param local 本地路径
	  * @return 状态
	  * @throws IOException
	  */
	 public DownloadStatus download(String remote,String local) throws IOException{
		
	    
	    if(isDirectoryExists(remote)){
	    	logger.info("remote Directory: " + getName(remote));
	    	return downloadDirectory(remote,local);
	    }else
	     return downloadFile(remote, local);
		
	 }
	 
	 /**
      * 查看目录是否存在
      * @param path
      * @return
      * @throws IOException
      */

     public boolean isDirectoryExists(String path) throws IOException {
            boolean flag = false;
            String filename = getName(path);
            FTPFile[] ftpFileArr = ftpClient.listFiles(getParentPath(path));
            for (FTPFile ftpFile : ftpFileArr) {
            	
//            	logger.info(ftpFile.getName());
                   if (ftpFile.isDirectory()
                                 && ftpFile.getName().equalsIgnoreCase(filename)) {
                          flag = true;
                          break;
                   }
            }
            return flag;
     }

     /**
      * 获取文件名
      * @param path
      * @return
      */
     private String getName(String path){
    	 int idx = path.lastIndexOf("/");
    	 return path.substring(idx+1, path.length());
     }
     /**
      * 获取所在目录
      * @param path
      * @return
      */
     private String getParentPath(String path){
    	 if(!path.startsWith("/"))
    		 path = "/" + path;
    	 int idx = path.lastIndexOf("/");
    	 return path.substring(0,idx);
     }
     
     /**
      * 移动、重命名文件、路径（类似Unix系统下的mv指令）
      * @param from 原来的路径
      * @param to 目标路径或目标文件名
      * @return
      * @throws IOException
      */
     public MoveStatus move(String from,String to) throws IOException{
		
		 if( isDirectoryExists(from)){
//		    logger.info("remote Directory: " + files[0].getName());
		    String dirname = getName(from);
		    
		    
		    if(!isDirectoryExists(to)){
		    	FTPFile [] files = ftpClient.listFiles(to);
			    
			   
			    if(files.length <= 0){
			    	 //如果不存在to这个文件夹,也不存在文件
					 ftpClient.rename(from, to);
					 return MoveStatus.Change_Name_Success;
				}
		    	//error 不是一个文件夹，是一个文件
		    	return MoveStatus.Move_To_Not_Directory;
		    }else
		    {
		    	if(!to.endsWith("/")) to += "/";
		    	ftpClient.rename(from, to + dirname );
		    	return MoveStatus.Move_To_Success;
		    }
		  }else{
			//检查远程文件是否存在   
			 FTPFile[] files = ftpClient.listFiles(from);

			 String filename = getName(from);
			 if(files.length <= 0){
				 //不存在
				 return MoveStatus.File_Not_Exist;
			 }
				
			  if(files[0].isDirectory()){
				  if(!to.endsWith("/")) to += "/";
				   	ftpClient.rename(from, to + filename );
				   	return MoveStatus.Move_To_Success;
				
			  }else
			  {
				  files = ftpClient.listFiles(to);
				
				    if(files.length <= 0){
				    	//如果不存在to这个文件夹，且不存在to这个文件
						 ftpClient.rename(from, to);
						 return MoveStatus.Change_Name_Success;
					}
				  //error，已经存在文件
				  return MoveStatus.Chage_To_File_Exist;
			  }
		  }
	 }
 /**
     * 从FTP服务器上下载文件,支持断点续传，上传百分比汇报 
     * @param remote 远程文件路径 
     * @param local 本地文件路径 
     * @return 上传的状态 
     * @throws IOException 
     */  
    public DownloadStatus downloadFile(String remote,String local) throws IOException{   
//        //设置被动模式   
//        ftpClient.enterLocalPassiveMode();   
//        //设置以二进制方式传输   
//        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);   
        DownloadStatus result;
           
        //检查远程文件是否存在   
        FTPFile[] files = ftpClient.listFiles(new String(remote.getBytes("GBK"),"iso-8859-1"));   
        if(files.length != 1){   
            logger.warn("远程文件不存在");   
            return DownloadStatus.Remote_File_Noexist;
        }   
           
        long lRemoteSize = files[0].getSize();   
        File f = new File(local);   
        //本地存在文件，进行断点下载   
        if(f.exists()){   
            long localSize = f.length();   
            //判断本地文件大小是否大于远程文件大小   
            if(localSize >= lRemoteSize){   
                logger.info("本地文件大于远程文件，下载中止");   
                return DownloadStatus.Local_Bigger_Remote;   
            }   
               
            //进行断点续传，并记录状态   
            FileOutputStream out = new FileOutputStream(f,true);   
            ftpClient.setRestartOffset(localSize);   
            InputStream in = ftpClient.retrieveFileStream(new String(remote.getBytes("GBK"),"iso-8859-1"));   
            byte[] bytes = new byte[1024];   
            long step = lRemoteSize /100;   
            long process=localSize /step;   
            int c;   
            while((c = in.read(bytes))!= -1){   
                out.write(bytes,0,c);   
                localSize+=c;   
                long nowProcess = localSize /step;   
                if(nowProcess > process){   
                    process = nowProcess;   
                    if(process % 10 == 0)   
                        logger.info("下载进度："+process);   
                    //TODO 更新文件下载进度,值存放在process变量中   
                }   
            }   
            in.close();   
            out.close();   
            boolean isDo = ftpClient.completePendingCommand();   
            if(isDo){   
                result = DownloadStatus.Download_From_Break_Success;   
            }else {   
                result = DownloadStatus.Download_From_Break_Failed;   
            }   
        }else {   
            OutputStream out = new FileOutputStream(f);   
            
            InputStream in= ftpClient.retrieveFileStream(new String(remote.getBytes("GBK"),"iso-8859-1"));   
            byte[] bytes = new byte[1024];
            long step = lRemoteSize /100;   
            long process=0;   
            long localSize = 0L;   
            int c;   
            while((c = in.read(bytes))!= -1){   
                out.write(bytes, 0, c);   
                localSize+=c;   
                long nowProcess = localSize /step;   
                if(nowProcess > process){   
                    process = nowProcess;   
                    if(process % 10 == 0)   
                        logger.info("下载进度："+process);   
                    //TODO 更新文件下载进度,值存放在process变量中   
                }   
            }   
            in.close();   
            out.close();   
            boolean upNewStatus = ftpClient.completePendingCommand();   
            if(upNewStatus){   
                result = DownloadStatus.Download_New_Success;   
            }else {   
                result = DownloadStatus.Download_New_Failed;   
            }   
        }   
        return result;   
    }
    
    public DownloadStatus downloadDirectory(String remote,String local) throws IOException{
    	String dirName = new File(remote).getName();
    	if(!local.endsWith("/")) local += "/";
    	if(!remote.endsWith("/")) remote += "/";
    	local += dirName +"/";
    	
    	File localDir = new File(local);
    	if( localDir.exists()){
    		if( !localDir.isDirectory())
    			return DownloadStatus.Local_Not_Directory;
    	}else {
    		
    		logger.info("Create dir: " + localDir.getAbsolutePath());
			localDir.mkdirs();
		}
    	
    	boolean warn_flag = false;
    	DownloadStatus status ;
    	FTPFile [] files = ftpClient.listFiles(remote);
    	for( FTPFile f: files){
    		if( f.isFile() ){
    			logger.info("remote: " + remote + f.getName() +" local: " + local + f.getName());
    			status = downloadFile(remote + f.getName(), local + f.getName());
    			if( !status.equals(DownloadStatus.Download_New_Success) 
					|| !status.equals(DownloadStatus.Download_From_Break_Success)
					|| !status.equals(DownloadStatus.Local_Bigger_Remote) )
    			{
    				warn_flag = true;
    				logger.warn(status);
    			}
    			
    		}else if( f.isDirectory() ){
    			logger.info("remote: " + remote + f.getName() +" local: " + local);
    			status = downloadDirectory(remote + f.getName(),local);
    			if( !status.equals(DownloadStatus.Download_Directory_Success) )
    			{
    				warn_flag = true;
    				logger.warn(status);
    			}
    		}
    	}
    	
    	return warn_flag ? DownloadStatus.Download_Directory_Warn:DownloadStatus.Download_Directory_Success;
    	
    }
    /**
     * 上传文件到FTP服务器，支持断点续传 
     * @param local 本地文件名称，绝对路径 
     * @param remote 远程文件路径，使用/home/directory1/subdirectory/file.ext或是 http://www.guihua.org /subdirectory/file.ext 按照Linux上的路径指定方式，支持多级目录嵌套，支持递归创建不存在的目录结构 
     * @return 上传结果 
     * @throws IOException 
     */  
    public UploadStatus upload(String local,String remote) throws IOException{   
//        //设置PassiveMode传输   
//        ftpClient.enterLocalPassiveMode();   
//        //设置以二进制流的方式传输   
//        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);   
        ftpClient.setControlEncoding("GBK");   
        UploadStatus result;   
        //对远程目录的处理   
        String remoteFileName = remote;   
        if(remote.contains("/")){   
            remoteFileName = remote.substring(remote.lastIndexOf("/")+1);   
            //创建服务器远程目录结构，创建失败直接返回   
            if(createDirecroty(remote, ftpClient)==UploadStatus.Create_Directory_Fail){   
                return UploadStatus.Create_Directory_Fail;   
            }   
        }   
        
        //检查远程是否存在文件   
        FTPFile[] files = ftpClient.listFiles(new String(remoteFileName.getBytes("GBK"),"iso-8859-1"));   
        if(files.length == 1){   
            long remoteSize = files[0].getSize();   
            File f = new File(local);   
            long localSize = f.length();   
            if(remoteSize==localSize){   
                return UploadStatus.File_Exits;   
            }else if(remoteSize > localSize){   
                return UploadStatus.Remote_Bigger_Local;   
            }   
               
            //尝试移动文件内读取指针,实现断点续传   
            result = uploadFile(remoteFileName, f, ftpClient, remoteSize);   
               
            //如果断点续传没有成功，则删除服务器上文件，重新上传   
            if(result == UploadStatus.Upload_From_Break_Failed){
                if(!ftpClient.deleteFile(remoteFileName)){   
                    return UploadStatus.Delete_Remote_Faild;   
                }   
                result = uploadFile(remoteFileName, f, ftpClient, 0);   
            }   
        }else {   
            result = uploadFile(remoteFileName, new File(local), ftpClient, 0);   
        }   
        return result;
    }   

      
    /**
     * 递归创建远程服务器目录 
     * @param remote 远程服务器文件绝对路径 
     * @param ftpClient FTPClient对象 
     * @return 目录创建是否成功 
     * @throws IOException 
     */  
    public UploadStatus createDirecroty(String remote,FTPClient ftpClient) throws IOException{   
        UploadStatus status = UploadStatus.Create_Directory_Success;   
        String directory = remote.substring(0,remote.lastIndexOf("/")+1);   
        if(!directory.equalsIgnoreCase("/")&&!ftpClient.changeWorkingDirectory(new String(directory.getBytes("GBK"),"iso-8859-1"))){   
            //如果远程目录不存在，则递归创建远程服务器目录   
            int start=0;   
            int end = 0;   
            if(directory.startsWith("/")){   
                start = 1;   
            }else{   
                start = 0;   
            }   
            end = directory.indexOf("/",start);   
            while(true){   
                String subDirectory = new String(remote.substring(start,end).getBytes("GBK"),"iso-8859-1");   
                if(!ftpClient.changeWorkingDirectory(subDirectory)){   
                    if(ftpClient.makeDirectory(subDirectory)){   
                        ftpClient.changeWorkingDirectory(subDirectory);   
                    }else {   
                        logger.warn("创建目录失败");   
                        return UploadStatus.Create_Directory_Fail;   
                    }   
                }   
                   
                start = end + 1;   
                end = directory.indexOf("/",start);   
                   
                //检查所有目录是否创建完毕   
                if(end <= start){   
                    break;   
                }   
            }   
        }   
        return status;   
    }   
       
    /**
     * 上传文件到服务器,新上传和断点续传 
     * @param remoteFile 远程文件名，在上传之前已经将服务器工作目录做了改变 
     * @param localFile 本地文件File句柄，绝对路径 
     * @param processStep 需要显示的处理进度步进值 
     * @param ftpClient FTPClient引用 
     * @return 
     * @throws IOException 
     */  
    public UploadStatus uploadFile(String remoteFile,File localFile,FTPClient ftpClient,long remoteSize) throws IOException{   
	        UploadStatus status;   
	        //显示进度的上传   
	        long step = localFile.length() / 100;   
	        long process = 0;   
	        long localreadbytes = 0L;   
	        RandomAccessFile raf = new RandomAccessFile(localFile,"r");   
	        OutputStream out = ftpClient.appendFileStream(new String(remoteFile.getBytes("GBK"),"iso-8859-1"));   
	        //断点续传   
	        if(remoteSize>0){   
	            ftpClient.setRestartOffset(remoteSize);   
	            process = remoteSize /step;   
	            raf.seek(remoteSize);   
	            localreadbytes = remoteSize;   
	        }   
	        byte[] bytes = new byte[1024];   
	        int c;   
	        while((c = raf.read(bytes))!= -1){   
	            out.write(bytes,0,c);   
	            localreadbytes+=c;   
	            if(localreadbytes / step != process){   
	                process = localreadbytes / step;   
	                logger.info("上传进度:" + process);   
	                //TODO 汇报上传状态   
	            }   
	        }   
	        out.flush();   
	        raf.close();   
	        out.close();   
	        boolean result =ftpClient.completePendingCommand();   
	        if(remoteSize > 0){   
	            status = result?UploadStatus.Upload_From_Break_Success:UploadStatus.Upload_From_Break_Failed;   
	        }else {   
	            status = result?UploadStatus.Upload_New_File_Success:UploadStatus.Upload_New_File_Failed;   
	        }   
	        return status;   
	    }   



}
