package com.ict.ftp;

/**
 * 枚举类型用于表示Ftp服务的状态 
 * @author Administrator
 *
 */
public enum DownloadStatus { 
	/**
	 * 本地文件不小于远程文件
	 */
	Local_Bigger_Remote,
	/**
	 * 远程文件不存在
	 */
	Remote_File_Noexist,
	/**
	 * 断点续传成功
	 */
	Download_From_Break_Success, 
	/**
	 * 断点续传失败
	 */
	Download_From_Break_Failed, 
	/**
	 * 下载新文件成功
	 */
	Download_New_Success,
	/**
	 * 下载新文件失败
	 */
	Download_New_Failed,
	/**
	 * 本地文件非文件夹错误
	 */
	Local_Not_Directory,
	/**
	 * 下载文件夹成功
	 */
	Download_Directory_Success,
	/**
	 * 文件夹下载过程不完整
	 */
	Download_Directory_Warn
}
