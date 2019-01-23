package com.danicoz.loadservice;

import java.sql.Connection;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.danicoz.Main;
import com.danicoz.SysConstant;
import com.danicoz.utils.DBUtil;
import com.danicoz.utils.LoadUtil;

public class LoadService {

	private static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void load(String type){
		
		if("oracle".equals(type)){
			String loadSql = SysConstant.tarLoad;
			try {
				LoadUtil.execuSqlldr(loadSql);
			} catch (Exception e) {
				logger.error("������:" + e);
				e.printStackTrace();
			}
		}else{
			String sql = SysConstant.midLoad;
			Connection conn = DBUtil.getDBConn(SysConstant.SRC_DBALIAS);
			try {
				logger.info("��������SQL=" + sql);
				Integer i = DBUtil.execute(conn, sql, new ArrayList<String>());
				logger.info("���ص���������" + i);
			} catch (Exception e) {
				logger.error("mysql���ݼ��س���", e);
				e.printStackTrace();
			}
		}
	}
	
}
