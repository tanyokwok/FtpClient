package com.ict.ftp;

public enum MoveStatus {
	/**
	 * �ļ��Ѿ�����
	 */
	Chage_To_File_Exist,
	/**
	 * Ŀ���ַ����һ��Ŀ¼
	 */
	Move_To_Not_Directory,
	/**
	 * �����ɹ�
	 */
	Change_Name_Success,
	/**
	 * �ƶ��ļ��гɹ�
	 */
	Move_To_Success,
	/**
	 * Ŀ¼�ļ�������
	 */
	File_Not_Exist
}
