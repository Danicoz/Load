package com.danicoz.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.danicoz.SysConstant;
import com.danicoz.conf.PropertiesHelper;

public class DBUtil {
	private final static Logger LOG = LoggerFactory.getLogger(DBUtil.class);
	private static Map<String, DruidDataSource> dataSources = new ConcurrentHashMap<String, DruidDataSource>();
	private static String DEFAULT_DB_ALIAS = null;

	static {
		if (SysConstant.DB_CONN_PROPERTIES_PATH != null) {
			try {
				Properties props = PropertiesHelper
						.getProperties(SysConstant.DB_CONN_PROPERTIES_PATH);
				Map<String, Properties> hs_pros = new HashMap<String, Properties>();
				Set set = props.keySet();
				Iterator it = set.iterator();
				while (it.hasNext()) {
					String key = (String) it.next();
					if (key.startsWith("DBNAME")) {
						if (key.endsWith(".DEFAULT")) {
							DEFAULT_DB_ALIAS = props.getProperty(key);
						} else {
							continue;
						}
					}
					String dbname = key.substring(0, key.indexOf("."));
					if (hs_pros.get(dbname) == null) {
						hs_pros.put(dbname, new Properties());
					}
					((Properties) hs_pros.get(dbname)).setProperty(
							key.substring(key.indexOf(".") + 1),
							props.getProperty(key));
				}
				init(hs_pros);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static Connection getDBConn(Object alias) {

		if (alias == null) {
			alias = DEFAULT_DB_ALIAS;
		} else {
			if (alias instanceof Connection) {
				return (Connection) alias;
			}
		}
		// LOG.info("获取链接:" + alias);
		Connection conn = null;
		try {
			if (dataSources.keySet().contains(alias)) {
				conn = dataSources.get(alias).getConnection();
			} else {
				throw new SQLException("===数据源配置有误===" + alias);
			}
		} catch (Exception e) {
			LOG.error("获取连接【" + alias + "】出现异常," + e.getMessage(), e);
			try {
				Thread.sleep(1000);
				LOG.error("再次尝试获取【" + alias + "】连接");
				conn = dataSources.get(alias).getConnection();
			} catch (Exception e1) {
				LOG.error("再次获取连接【" + alias + "】失败，放弃尝试" + e1.getMessage(), e1);
			}

		}
		return conn;
	}

	public static void init(Map<String, Properties> pros) {
		try {
			if (pros != null) {
				Set<String> keys = pros.keySet();
				for (String key : keys) {
					DruidDataSource dds = (DruidDataSource) DruidDataSourceFactory
							.createDataSource(pros.get(key));
					dataSources.put(key, dds);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void close(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	public static void close(Connection conn, Statement stmt, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	public static Connection getConn(String driver, String url,
			String username, String password) {
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
		} catch (SQLException e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
		}
		return conn;
	}

	public static int update(Connection connection, String sql, Object params[])
			throws SQLException {
		int n = 0;
		Connection conn = null;
		try {
			conn = getDBConn(connection);
			QueryRunner qRunner = new QueryRunner();
			n = qRunner.update(conn, sql, params);
		} finally {
			close(conn);
		}
		return n;
	}

	public static int update(Connection connection, String sql)
			throws SQLException {
		int n = 0;
		Connection conn = null;
		try {
			conn = getDBConn(connection);
			// Statement stmt=conn.createStatement();
			// stmt.execute(sql);
			QueryRunner qRunner = new QueryRunner();
			n = qRunner.update(conn, sql);
		} finally {
			close(conn);
		}
		return n;
	}

	public static int update(Connection connection, String sql,
			Object params[], boolean isClosed) throws SQLException {
		int n = 0;
		Connection conn = null;
		try {
			conn = getDBConn(connection);
			QueryRunner qRunner = new QueryRunner();
			n = qRunner.update(conn, sql, params);
		} finally {
			if (isClosed) {
				close(conn);
			}
		}
		return n;
	}

	// 正在使用
	public static int batch(Connection connection, List sqlArray)
			throws SQLException {
		int n = 0;
		Connection conn = null;
		Statement st = null;
		try {
			conn = getDBConn(connection);
			// conn.setAutoCommit(false);
			st = conn.createStatement();
			int length = sqlArray.size();
			for (int i = 0; i < length; i++) {
				st.addBatch((String) sqlArray.get(i));
			}
			int[] arr = st.executeBatch();
			n = arr.length;
			// conn.commit();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			// conn.rollback();
			throw new SQLException(e);
		} finally {
			close(conn, st, null);
		}
		return n;
	}

	public static int batch(Connection connection, String[] sqlArray)
			throws SQLException {
		int n = 0;
		Connection conn = null;
		Statement st = null;
		try {
			conn = getDBConn(connection);
			conn.setAutoCommit(false);
			st = conn.createStatement();
			int length = sqlArray.length;
			for (int i = 0; i < length; i++) {
				st.addBatch(sqlArray[i]);
			}
			st.executeBatch();
			conn.commit();
		} catch (Exception e) {
			conn.rollback();
			throw new SQLException(e);
		} finally {
			close(conn, st, null);
		}
		return n;
	}

	// 批量支持sybase4的回滚
	public static int batch(Connection connection, String sql,
			Object params[][]) throws SQLException {
		int n = 0;
		Connection conn = null;
		try {
			conn = getDBConn(connection);
			conn.setAutoCommit(false);
			QueryRunner qRunner = new QueryRunner();
			n = qRunner.batch(conn, sql, params).length;
			conn.commit();
		} catch (Exception e) {
			conn.rollback();
			throw new SQLException(e);
		} finally {
			close(conn);
		}
		return n;
	}

	public static int batch(Connection connection, String sql,
			Object params[][], int sliptSize) throws SQLException {
		int n = 0;
		Connection conn = null;
		int size = params.length;
		try {
			conn = getDBConn(connection);
			QueryRunner qRunner = new QueryRunner();
			int offset = 0;
			int index = (size > sliptSize) ? sliptSize : size;
			while (offset < index) {
				Object _params[][] = Arrays.copyOfRange(params, offset, index);
				n = n + qRunner.batch(conn, sql, _params).length;
				offset = offset + sliptSize;
				index = ((index + sliptSize) > size) ? size : index + sliptSize;
				_params = null;
			}
		} finally {
			close(conn);
		}
		return n;
	}

	public static Object queryForObject(Connection connection, String sql,
			Object params[], Class clazz) throws SQLException {
		Object obj = null;
		Connection conn = null;
		try {
			conn = getDBConn(connection);
			QueryRunner qRunner = new QueryRunner();
			obj = qRunner.query(conn, sql, params, new BeanHandler(clazz));
		} finally {
			close(conn);
		}
		return obj;
	}

	public static List queryForOList(Connection connection, String sql,
			Object params[], Class clazz) throws SQLException {
		List list = null;
		Connection conn = null;
		try {
			conn = getDBConn(connection);
			QueryRunner qRunner = new QueryRunner();
			list = (List) qRunner.query(conn, sql, params, new BeanListHandler(
					clazz));
		} finally {
			close(conn);
		}
		return list;
	}

	public static List queryForOList(Connection connection, String sql,
			Object params[], Class clazz, boolean closeConn)
			throws SQLException {
		List list = null;
		Connection conn = null;
		try {
			conn = getDBConn(connection);
			QueryRunner qRunner = new QueryRunner();
			list = (List) qRunner.query(conn, sql, params, new BeanListHandler(
					clazz));
		} finally {
			if (closeConn) {
				close(conn);
			}
		}
		return list;
	}

	public static long getRowValue(Connection connection, String sql,
			Object params[]) throws SQLException {
		long count = 0;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = getDBConn(connection);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				count = rs.getLong(1); // 记录总数
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				LOG.error(e.getMessage(), e);
				e.printStackTrace();
			}
			if (conn != null) {
				close(conn);
			}
		}
		return count;
	}

	public static List queryForList(Connection connection, String sql,
			Object params[]) throws SQLException {
		List resultList = null;
		Connection conn = null;
		try {
			conn = getDBConn(connection);
			QueryRunner qRunner = new QueryRunner();

			ResultSetHandler rsh = new ResultSetHandler() {
				@Override
				public Object handle(ResultSet rs) throws SQLException {
					List<Map> tempList = new ArrayList<Map>();
					ResultSetMetaData meta = rs.getMetaData();
					int columnCount = meta.getColumnCount();
					String colName = null;
					String colValue = null;
					if (rs != null) {
						while (rs.next()) {
							Map<String, String> map = new LinkedHashMap<String, String>();
							for (int i = 1; i <= columnCount; i++) {
								colName = meta.getColumnLabel(i);
								colValue = rs.getString(i);
								map.put(colName, colValue);
							}
							tempList.add(map);
						}
					}
					return tempList;
				}
			};
			resultList = (List) qRunner.query(conn, sql, rsh, params);
		} finally {
			close(conn);
		}
		return resultList;
	}

	public static HashMap queryHashMapObject(Connection connection, String sql,
			Object params[]) throws SQLException {
		Connection conn = null;
		try {
			conn = getDBConn(connection);
			QueryRunner qRunner = new QueryRunner();

			ResultSetHandler rsh = new ResultSetHandler() {
				@Override
				public Object handle(ResultSet rs) throws SQLException {
					HashMap hashmap = new HashMap();
					ResultSetMetaData meta = rs.getMetaData();
					int columnCount = meta.getColumnCount();
					String colName = null;
					String colValue = null;
					if (rs != null) {
						while (rs.next()) {
							for (int i = 1; i <= columnCount; i++) {
								colName = meta.getColumnLabel(i);
								colValue = rs.getString(i);
								hashmap.put(colName, colValue);
							}
							break;
						}
					}
					return hashmap;
				}
			};
			return (HashMap) qRunner.query(conn, sql, rsh, params);
		} finally {
			close(conn);
		}
	}

	public static List queryHashMapList(Connection connection, String sql,
			Object params[]) throws SQLException {
		Connection conn = null;
		try {
			conn = getDBConn(connection);
			QueryRunner qRunner = new QueryRunner();

			ResultSetHandler rsh = new ResultSetHandler() {
				List list = new LinkedList();

				@Override
				public Object handle(ResultSet rs) throws SQLException {
					ResultSetMetaData meta = rs.getMetaData();
					int columnCount = meta.getColumnCount();
					String colName = null;
					Object colValue = null;
					if (rs != null) {
						HashMap hashmap = null;
						while (rs.next()) {
							hashmap = new HashMap();
							for (int i = 1; i <= columnCount; i++) {
								colName = meta.getColumnLabel(i);
								colValue = rs.getObject(i);
								hashmap.put(colName, colValue);
							}
							list.add(hashmap);
						}
					}
					return list;
				}
			};
			return (List) qRunner.query(conn, sql, rsh, params);
		} finally {
			close(conn);
		}
	}

	public static List<Object[]> queryForArrayList(Connection connection, String sql, Object params[]) throws SQLException {
	
		List<Object[]> list = null;
		Connection conn = null;
		try {
			conn = getDBConn(connection);
			QueryRunner qRunner = new QueryRunner();
			list = qRunner.query(conn, sql, new ArrayListHandler());
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			e.printStackTrace();
		} finally {
			if (conn != null) {
				close(conn);
			}
		}
		return list;
	}

	public static int execute(Connection conn, String sql, List<String> warns)
			throws SQLException {
		PreparedStatement stat = null;
		int num = 0;
		try {
			stat = conn.prepareStatement(sql);
			num = stat.executeUpdate(sql);
			SQLWarning w = stat.getWarnings();
			while (w != null) {
				warns.add(w.getMessage());
				w = w.getNextWarning();
			}

		} finally {
			close(conn);
		}
		return num;
	}

	public static List<String> getColumnName(Connection conn, String sql) {
		//List list = new ArrayList();
		List<String> columnName = new ArrayList<String>();
		//List columnType = new ArrayList();
		Statement stmt = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				columnName.add(rsmd.getColumnName(i).toUpperCase());
				// columnType.add(rsmd.getColumnType(i));
			}
		} catch (Exception e) {
			LOG.error("查询表结构【" + sql + "】表结构存在异常,，异常原因：" + e.getMessage(), e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				LOG.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
		//list.add(columnName);
		//list.add(columnType);
		return columnName;
	}
}