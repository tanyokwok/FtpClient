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
	 * ftp server��ip
	 */
	private String serverIp;
	/**
	 * ����Ķ˿�
	 */
	private int port;
	/**
	 * ��ʹ�õ��˺�
	 */
	private String userName;
	/**
	 * ��ʹ�õ��˺�����
	 */
	private String password;
	
	private FTPClient ftpClient = new FTPClient();
	
	
	public FtpHelper() {
		//���ý�������ʹ�õ����������������̨ 
		ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out))); 
	}
	
	
	/**
	 * @param serverIp ftp server��ip
	 * @param userName ��ʹ�õ��˺�
	 * @param password ��ʹ�õ��˺�����
	 * @return �Ƿ����ӳɹ� 
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
	 * @param serverIp ftp server��ip
	 * @param port  ����Ķ˿�
	 * @param userName ��ʹ�õ��˺�
	 * @param password ��ʹ�õ��˺�����
	 * @throws IOException 
	 * @return �Ƿ����ӳɹ� 
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
				//���ñ���ģʽ   
		        ftpClient.enterLocalPassiveMode();   
		        //�����Զ����Ʒ�ʽ����   
		        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);   
                return true;   
            }   
		}
		disconnect();
		return false;
	}
	
	/** 
     * �Ͽ���Զ�̷����������� 
     * @throws IOException 
     */  
	 public void disconnect() throws IOException{   
	        if(ftpClient.isConnected()){   
	        	ftpClient.logout();
	            ftpClient.disconnect();   
	        }   
	}
	 
	 /**
	  * �����ļ�·���Զ��ж������ļ�����Ŀ¼
	  * @param remote Զ��·��
	  * @param local ����·��
	  * @return ״̬
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
      * �鿴Ŀ¼�Ƿ����
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
      * ��ȡ�ļ���
      * @param path
      * @return
      */
     private String getName(String path){
    	 int idx = path.lastIndexOf("/");
    	 return path.substring(idx+1, path.length());
     }
     /**
      * ��ȡ����Ŀ¼
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
      * �ƶ����������ļ���·��������Unixϵͳ�µ�mvָ�
      * @param from ԭ����·��
      * @param to Ŀ��·����Ŀ���ļ���
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
			    	 //���������to����ļ���,Ҳ�������ļ�
					 ftpClient.rename(from, to);
					 return MoveStatus.Change_Name_Success;
				}
		    	//error ����һ���ļ��У���һ���ļ�
		    	return MoveStatus.Move_To_Not_Directory;
		    }else
		    {
		    	if(!to.endsWith("/")) to += "/";
		    	ftpClient.rename(from, to + dirname );
		    	return MoveStatus.Move_To_Success;
		    }
		  }else{
			//���Զ���ļ��Ƿ����   
			 FTPFile[] files = ftpClient.listFiles(from);

			 String filename = getName(from);
			 if(files.length <= 0){
				 //������
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
				    	//���������to����ļ��У��Ҳ�����to����ļ�
						 ftpClient.rename(from, to);
						 return MoveStatus.Change_Name_Success;
					}
				  //error���Ѿ������ļ�
				  return MoveStatus.Chage_To_File_Exist;
			  }
		  }
	 }
 /**
     * ��FTP�������������ļ�,֧�ֶϵ��������ϴ��ٷֱȻ㱨 
     * @param remote Զ���ļ�·�� 
     * @param local �����ļ�·�� 
     * @return �ϴ���״̬ 
     * @throws IOException 
     */  
    public DownloadStatus downloadFile(String remote,String local) throws IOException{   
//        //���ñ���ģʽ   
//        ftpClient.enterLocalPassiveMode();   
//        //�����Զ����Ʒ�ʽ����   
//        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);   
        DownloadStatus result;
           
        //���Զ���ļ��Ƿ����   
        FTPFile[] files = ftpClient.listFiles(new String(remote.getBytes("GBK"),"iso-8859-1"));   
        if(files.length != 1){   
            logger.warn("Զ���ļ�������");   
            return DownloadStatus.Remote_File_Noexist;
        }   
           
        long lRemoteSize = files[0].getSize();   
        File f = new File(local);   
        //���ش����ļ������жϵ�����   
        if(f.exists()){   
            long localSize = f.length();   
            //�жϱ����ļ���С�Ƿ����Զ���ļ���С   
            if(localSize >= lRemoteSize){   
                logger.info("�����ļ�����Զ���ļ���������ֹ");   
                return DownloadStatus.Local_Bigger_Remote;   
            }   
               
            //���жϵ�����������¼״̬   
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
                        logger.info("���ؽ��ȣ�"+process);   
                    //TODO �����ļ����ؽ���,ֵ�����process������   
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
                        logger.info("���ؽ��ȣ�"+process);   
                    //TODO �����ļ����ؽ���,ֵ�����process������   
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
     * �ϴ��ļ���FTP��������֧�ֶϵ����� 
     * @param local �����ļ����ƣ�����·�� 
     * @param remote Զ���ļ�·����ʹ��/home/directory1/subdirectory/file.ext���� http://www.guihua.org /subdirectory/file.ext ����Linux�ϵ�·��ָ����ʽ��֧�ֶ༶Ŀ¼Ƕ�ף�֧�ֵݹ鴴�������ڵ�Ŀ¼�ṹ 
     * @return �ϴ���� 
     * @throws IOException 
     */  
    public UploadStatus upload(String local,String remote) throws IOException{   
//        //����PassiveMode����   
//        ftpClient.enterLocalPassiveMode();   
//        //�����Զ��������ķ�ʽ����   
//        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);   
        ftpClient.setControlEncoding("GBK");   
        UploadStatus result;   
        //��Զ��Ŀ¼�Ĵ���   
        String remoteFileName = remote;   
        if(remote.contains("/")){   
            remoteFileName = remote.substring(remote.lastIndexOf("/")+1);   
            //����������Զ��Ŀ¼�ṹ������ʧ��ֱ�ӷ���   
            if(createDirecroty(remote, ftpClient)==UploadStatus.Create_Directory_Fail){   
                return UploadStatus.Create_Directory_Fail;   
            }   
        }   
        
        //���Զ���Ƿ�����ļ�   
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
               
            //�����ƶ��ļ��ڶ�ȡָ��,ʵ�ֶϵ�����   
            result = uploadFile(remoteFileName, f, ftpClient, remoteSize);   
               
            //����ϵ�����û�гɹ�����ɾ�����������ļ��������ϴ�   
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
     * �ݹ鴴��Զ�̷�����Ŀ¼ 
     * @param remote Զ�̷������ļ�����·�� 
     * @param ftpClient FTPClient���� 
     * @return Ŀ¼�����Ƿ�ɹ� 
     * @throws IOException 
     */  
    public UploadStatus createDirecroty(String remote,FTPClient ftpClient) throws IOException{   
        UploadStatus status = UploadStatus.Create_Directory_Success;   
        String directory = remote.substring(0,remote.lastIndexOf("/")+1);   
        if(!directory.equalsIgnoreCase("/")&&!ftpClient.changeWorkingDirectory(new String(directory.getBytes("GBK"),"iso-8859-1"))){   
            //���Զ��Ŀ¼�����ڣ���ݹ鴴��Զ�̷�����Ŀ¼   
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
                        logger.warn("����Ŀ¼ʧ��");   
                        return UploadStatus.Create_Directory_Fail;   
                    }   
                }   
                   
                start = end + 1;   
                end = directory.indexOf("/",start);   
                   
                //�������Ŀ¼�Ƿ񴴽����   
                if(end <= start){   
                    break;   
                }   
            }   
        }   
        return status;   
    }   
       
    /**
     * �ϴ��ļ���������,���ϴ��Ͷϵ����� 
     * @param remoteFile Զ���ļ��������ϴ�֮ǰ�Ѿ�������������Ŀ¼���˸ı� 
     * @param localFile �����ļ�File���������·�� 
     * @param processStep ��Ҫ��ʾ�Ĵ�����Ȳ���ֵ 
     * @param ftpClient FTPClient���� 
     * @return 
     * @throws IOException 
     */  
    public UploadStatus uploadFile(String remoteFile,File localFile,FTPClient ftpClient,long remoteSize) throws IOException{   
	        UploadStatus status;   
	        //��ʾ���ȵ��ϴ�   
	        long step = localFile.length() / 100;   
	        long process = 0;   
	        long localreadbytes = 0L;   
	        RandomAccessFile raf = new RandomAccessFile(localFile,"r");   
	        OutputStream out = ftpClient.appendFileStream(new String(remoteFile.getBytes("GBK"),"iso-8859-1"));   
	        //�ϵ�����   
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
	                logger.info("�ϴ�����:" + process);   
	                //TODO �㱨�ϴ�״̬   
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
