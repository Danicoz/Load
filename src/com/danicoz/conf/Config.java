package com.danicoz.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.danicoz.SysConstant;
import com.danicoz.utils.DBUtil;

public class Config {

	private static Logger logger = LoggerFactory.getLogger(Config.class);
	
	private static Config conf = new Config();
	
	public Config() {}
	
	public static Config getInstance() {
		return conf;
	}
	
	public void init_dbconns(){
		Map<String, Properties> pros = new HashMap<String, Properties>();
		for (Entry<String, T_Db_Info> entry : SysConstant.DB_INFOS.entrySet()) {
			String key = entry.getKey();
			T_Db_Info dbinfo = entry.getValue();
			try {
				// Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
				logger.info("开始装载[" + dbinfo.getDbname() + "]DB配置");
				Properties info = new Properties();
				info.setProperty("url", dbinfo.getUrl());
				info.setProperty("username", dbinfo.getUsername());
				info.setProperty("password", dbinfo.getPassword());
				info.setProperty("initialSize", "1");
				info.setProperty("minIdle", "1");
				info.setProperty("maxActive", "50");
				info.setProperty("maxWait", "172800000");
				info.setProperty("timeBetweenEvictionRunsMillis", "60000");
				info.setProperty("minEvictableIdleTimeMillis", "60000");
				info.setProperty("testWhileIdle", "true");
				info.setProperty("testOnBorrow", "false");
				info.setProperty("testOnReturn", "false");
				info.setProperty("maxPoolPreparedStatementPerConnectionSize", "10");
				info.setProperty("filters", "stat");
				info.setProperty("removeAbandoned", "true");
				info.setProperty("removeAbandonedTimeout", "18000");//查询最长时间6小时
				String driverClass = dbinfo.getDriverclass();
				if (T_Db_Info.DBTYPE_MYSQL.equalsIgnoreCase(dbinfo.getDbtype())) {
					driverClass = "com.mysql.jdbc.Driver";
					info.setProperty("validationQuery", "select 1");
					info.setProperty("poolPreparedStatements", "false");
				} else if (T_Db_Info.DBTYPE_ORACLE.equalsIgnoreCase(dbinfo.getDbtype())) {
					driverClass = "oracle.jdbc.driver.OracleDriver";
					info.setProperty("validationQuery", "select 1 FROM DUAL");
					info.setProperty("poolPreparedStatements", "true");
					info.setProperty("maxOpenPreparedStatements", "100");
				}
				info.setProperty("driverClassName", driverClass);
				pros.put(dbinfo.getDbname(), info);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		DBUtil.init(pros);
	}
	
	public static void loadLogBackConfig() {
		ch.qos.logback.classic.LoggerContext lc = 
				(ch.qos.logback.classic.LoggerContext) 
				LoggerFactory.getILoggerFactory();
		ch.qos.logback.classic.joran.JoranConfigurator configurator = 
				new ch.qos.logback.classic.joran.JoranConfigurator();
		configurator.setContext(lc);
		lc.reset();
		String DEFAULT_WORK_DIR = System.getProperties().getProperty("user.dir") + File.separator + "conf" + File.separator;
		String LOG_CONF = "Logback.xml";
		try {
			configurator.doConfigure(DEFAULT_WORK_DIR + LOG_CONF);
		} catch (Exception e) {
			System.err.println("加载日志配置文件失败: [" + 
					DEFAULT_WORK_DIR + LOG_CONF + "]." + e);
		}
	}
	
	public static void initConf() {
		String confPath = System.getProperty("user.dir") + File.separator
				+ "conf" + File.separator + "config.xml";
		SAXBuilder builder = null;
		Reader reader = null;
		Document doc = null;
		try {
			File file = new File(confPath);
			logger.info("开始读取Config配置文件：" + confPath);
			reader = new InputStreamReader(new FileInputStream(file), "GB2312");
			builder = new SAXBuilder();
			doc = builder.build(reader);
			Element rootEle = doc.getRootElement();
			Element loadEle = rootEle.getChild("loadInfo"); 
			
			SysConstant.TYPE = loadEle.getChildText("type");
			SysConstant.midLoad = loadEle.getChildText("mysqlLoad");
			SysConstant.tarLoad = loadEle.getChildText("oracleLoad");
			
		} catch (Exception e) {
			logger.error("读取config配置文件出错！", e);
			System.exit(1);
		}
	}
}
