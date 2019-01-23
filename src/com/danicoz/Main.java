package com.danicoz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.danicoz.conf.Config;
import com.danicoz.loadservice.LoadService;

public class Main {

	public static Logger logger = LoggerFactory.getLogger(Main.class);

	/**
	 * ��ʼ������
	 **/
	private void init() {
		Config.getInstance().loadLogBackConfig();
		Config.getInstance().initConf();
		Config.getInstance().init_dbconns();
	}

	private void excute() {
		LoadService.load(SysConstant.TYPE);
	}

	public static void main(String[] args) {
		final Main main = new Main();
		main.init();
		main.excute();
		System.out.println("���� Git ��־��");
		System.out.println("���� Git ��֧��־");
		System.out.println("���� Git ��֧��־2");
		System.out.println("���� Git ��֧��־3");
		System.out.println("���� Git ��֧��־4");
		
		/*
		 * ͨ�����Ӻ����������Դ����Ҫ�ر����ӣ��ͷ��б�ȡ�
		 */
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				logger.info("++++++++++> ��ʼ�ر����Ӽ��ͷ���Դ ......");
				logger.info("++++++++++> clearSource����! �����˳�!");
			}
		});
	}
}
