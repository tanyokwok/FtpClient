package com.ict.ftp;

/**
 * ö���������ڱ�ʾFtp�����״̬ 
 * @author Administrator
 *
 */
public enum DownloadStatus { 
	/**
	 * �����ļ���С��Զ���ļ�
	 */
	Local_Bigger_Remote,
	/**
	 * Զ���ļ�������
	 */
	Remote_File_Noexist,
	/**
	 * �ϵ������ɹ�
	 */
	Download_From_Break_Success, 
	/**
	 * �ϵ�����ʧ��
	 */
	Download_From_Break_Failed, 
	/**
	 * �������ļ��ɹ�
	 */
	Download_New_Success,
	/**
	 * �������ļ�ʧ��
	 */
	Download_New_Failed,
	/**
	 * �����ļ����ļ��д���
	 */
	Local_Not_Directory,
	/**
	 * �����ļ��гɹ�
	 */
	Download_Directory_Success,
	/**
	 * �ļ������ع��̲�����
	 */
	Download_Directory_Warn
}
