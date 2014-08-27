package com.ict.ftp;

public enum MoveStatus {
	/**
	 * 文件已经存在
	 */
	Chage_To_File_Exist,
	/**
	 * 目标地址不是一个目录
	 */
	Move_To_Not_Directory,
	/**
	 * 更名成功
	 */
	Change_Name_Success,
	/**
	 * 移动文件夹成功
	 */
	Move_To_Success,
	/**
	 * 目录文件不存在
	 */
	File_Not_Exist
}
