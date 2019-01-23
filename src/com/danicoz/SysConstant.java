package com.danicoz;

import java.util.HashMap;
import java.util.Map;

import com.danicoz.conf.T_Db_Info;

public class SysConstant {
    public static final String DB_CONN_PROPERTIES_PATH = "conf/conn.properties";

	public static Map<String, T_Db_Info> DB_INFOS = new HashMap<String, T_Db_Info>();
    
    /** 目标库 **/
	public static String TAR_DBALIAS = "tarDB";
	
	
	/** 来源库  **/
	public static String SRC_DBALIAS = "midDB";
	
	public static String TYPE = "";
	public static String midLoad = "";
	public static String tarLoad = "";
}
