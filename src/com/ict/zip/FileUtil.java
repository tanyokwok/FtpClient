package com.ict.zip;

import java.io.File;

import org.apache.log4j.Logger;

public class FileUtil {
	private static Logger logger = Logger.getLogger(FileUtil.class);
	
	
	 /** 生产文件 如果文件所在路径不存在则生成路径
     * @param fileName 文件名 带路径
     * @param isDirectory 是否为路径
     * @return
     */
    public static File buildFile(String fileName, boolean isDirectory) {
        File target = new File(fileName);
        if (isDirectory) {
        	logger.info("Creating : " + fileName);
            target.mkdirs();
        } else {
            if (!target.getParentFile().exists()) {
            	logger.info("Creating : " + target.getParent());
                target.getParentFile().mkdirs();
                target = new File(target.getAbsolutePath());
            }
            logger.info("Inflating : " + fileName);
        }
        return target;
    }
}
