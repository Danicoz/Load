package com.danicoz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.danicoz.conf.Config;
import com.danicoz.loadservice.LoadService;

public class Main {

	public static Logger logger = LoggerFactory.getLogger(Main.class);

	/**
	 * 初始化动作
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
		System.out.println("测试 Git 日志！");
		System.out.println("测试 Git 分支日志");
		System.out.println("测试 Git 分支日志2");
		System.out.println("测试 Git 分支日志3");
		System.out.println("测试 Git 分支日志4");
		
		/*
		 * 通过钩子函数来清空资源，主要关闭连接，释放列表等。
		 */
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				logger.info("++++++++++> 开始关闭连接及释放资源 ......");
				logger.info("++++++++++> clearSource结束! 程序退出!");
			}
		});
	}
}
