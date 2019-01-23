package com.danicoz.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadUtil {

	private static Logger logger = LoggerFactory.getLogger(LoadUtil.class);
	
	public static boolean execuSqlldr(String sqlldr) {
		boolean status = true;
		Process process = null;
		logger.info("执行加载语句：" + sqlldr);
		try {
			process = Runtime.getRuntime().exec(sqlldr);
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "Error");
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "Output");
			errorGobbler.start();
			outputGobbler.start();

			process.waitFor();
		} catch (Exception e) {
			status = false;
			logger.error("执行sqlldr异常", e);
		} finally {
			process.destroy();
		}
		return status;
	}
	
	static class StreamGobbler extends Thread {
		InputStream is;

		String type;

		StreamGobbler(InputStream is, String type) {
			this.is = is;
			this.type = type;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is, "GBK");
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					if (type.equals("Error"))
						logger.error(line);
					else
						logger.debug(line);
				}
			} catch (Exception ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
}
