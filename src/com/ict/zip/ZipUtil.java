package com.ict.zip;

import java.io.*;
import java.util.Enumeration;

import org.apache.tools.zip.*;
public class ZipUtil {
	 /** *//**
	    * ѹ��
	    * 
	    * @param zipFileName
	    *            ѹ��������zip���ļ���--��·��,���Ϊnull�����Ĭ�ϰ��ļ�������ѹ���ļ���
	    * @param relativePath
	    *            ���·����Ĭ��Ϊ��
	    * @param directory
	    *            �ļ���Ŀ¼�ľ���·��
	    * @throws FileNotFoundException
	    * @throws IOException
	    */
	   public static void zip(String zipFileName, String relativePath,
	           String directory) throws FileNotFoundException, IOException {
	       String fileName = zipFileName;
	       if (fileName == null || fileName.trim().equals("")) {
	           File temp = new File(directory);
	           if (temp.isDirectory()) {
	               fileName = directory + ".zip";
	           } else {
	               if (directory.indexOf(".") > 0) {
	                   fileName = directory.substring(0, directory
	                           .lastIndexOf("."))
	                           + "zip";
	               } else {
	                   fileName = directory + ".zip";
	               }
	           }
	       }
	       ZipOutputStream zos = new ZipOutputStream(
	               new FileOutputStream(fileName));
	       try {
	           zip(zos, relativePath, directory);
	       } catch (IOException ex) {
	           throw ex;
	       } finally {
	           if (null != zos) {
	               zos.close();
	           }
	       }
	   }

	   /** *//**
	    * ѹ��
	    * 
	    * @param zos
	    *            ѹ�������
	    * @param relativePath
	    *            ���·��
	    * @param absolutPath
	    *            �ļ����ļ��о���·��
	    * @throws IOException
	    */
	   private static void zip(ZipOutputStream zos, String relativePath,
	           String absolutPath) throws IOException {
	       File file = new File(absolutPath);
	       if (file.isDirectory()) {
	           File[] files = file.listFiles();
	           for (int i = 0; i < files.length; i++) {
	               File tempFile = files[i];
	               if (tempFile.isDirectory()) {
	                   String newRelativePath = relativePath + tempFile.getName()
	                           + File.separator;
	                   createZipNode(zos, newRelativePath);
	                   zip(zos, newRelativePath, tempFile.getPath());
	               } else {
	                   zipFile(zos, tempFile, relativePath);
	               }
	           }
	       } else {
	           zipFile(zos, file, relativePath);
	       }
	   }

	   /** *//**
	    * ѹ���ļ�
	    * 
	    * @param zos
	    *            ѹ�������
	    * @param file
	    *            �ļ�����
	    * @param relativePath
	    *            ���·��
	    * @throws IOException
	    */
	   private static void zipFile(ZipOutputStream zos, File file,
	           String relativePath) throws IOException {
	       ZipEntry entry = new ZipEntry(relativePath + file.getName());
	       zos.putNextEntry(entry);
	       InputStream is = null;
	       try {
	           is = new FileInputStream(file);
	           int BUFFERSIZE = 2 << 10;
	           int length = 0;
	           byte[] buffer = new byte[BUFFERSIZE];
	           while ((length = is.read(buffer, 0, BUFFERSIZE)) >= 0) {
	               zos.write(buffer, 0, length);
	           }
	           zos.flush();
	           zos.closeEntry();
	       } catch (IOException ex) {
	           throw ex;
	       } finally {
	           if (null != is) {
	               is.close();
	           }
	       }
	   }

	   /** *//**
	    * ����Ŀ¼
	    * 
	    * @param zos
	    *            zip�����
	    * @param relativePath
	    *            ���·��
	    * @throws IOException
	    */
	   private static void createZipNode(ZipOutputStream zos, String relativePath)
	           throws IOException {
	       ZipEntry zipEntry = new ZipEntry(relativePath);
	       zos.putNextEntry(zipEntry);
	       zos.closeEntry();
	   }

	   /** *//**
	    * ��ѹ��zip��
	    * 
	    * @param zipFilePath
	    *            zip�ļ�·��
	    * @param targetPath
	    *            ��ѹ������λ�ã����Ϊnull����ַ�����Ĭ�Ͻ�ѹ������zip��ͬĿ¼��zip��ͬ�����ļ�����
	    * @throws IOException
	    */
	   public static void unzip(String zipFilePath, String targetPath)
	           throws IOException {
	       OutputStream os = null;
	       InputStream is = null;
	       ZipFile zipFile = null;
	       try {
	           zipFile = new ZipFile(zipFilePath);
	           String directoryPath = "";
	           if (null == targetPath || "".equals(targetPath)) {
	               directoryPath = zipFilePath.substring(0, zipFilePath
	                       .lastIndexOf("."));
	           } else {
	               directoryPath = targetPath;
	           }
	           
	           if(!directoryPath.endsWith(File.separator))
        		   directoryPath = directoryPath + File.separator ;
	           Enumeration entryEnum = zipFile.getEntries();
	           if (null != entryEnum) {
	               ZipEntry zipEntry = null;
	               while (entryEnum.hasMoreElements()) {
	                   zipEntry = (ZipEntry) entryEnum.nextElement();
	                   
	                   if (zipEntry.isDirectory()) {
	                	   FileUtil.buildFile(directoryPath //+ File.separator
	                               + zipEntry.getName(), true);
//	                	   directoryPath += zipEntry.getName();
//	                       System.out.println("createing : " + directoryPath);
	                       continue;
	                   }
	                   if (zipEntry.getSize() > 0) {
	                       // �ļ�
	                       File targetFile = FileUtil.buildFile(directoryPath
	                               + zipEntry.getName(), false);
	                       os = new BufferedOutputStream(new FileOutputStream(
	                               targetFile));
	                       is = zipFile.getInputStream(zipEntry);
	                       byte[] buffer = new byte[4096];
	                       int readLen = 0;
	                       while ((readLen = is.read(buffer, 0, 4096)) >= 0) {
	                           os.write(buffer, 0, readLen);
	                       }

	                       os.flush();
	                       os.close();
	                   } else {
	                       // ��Ŀ¼
	                       FileUtil.buildFile(directoryPath //+ File.separator
	                               + zipEntry.getName(), true);
	                   }
	               }
	           }
	       } catch (IOException ex) {
	           throw ex;
	       } finally {
	           if(null != zipFile){
	               zipFile = null;
	           }
	           if (null != is) {
	               is.close();
	           }
	           if (null != os) {
	               os.close();
	           }
	       }
	   }
}
