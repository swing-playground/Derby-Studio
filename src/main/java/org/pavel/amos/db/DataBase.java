package org.pavel.amos.db;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.pavel.amos.util.ResultUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataBase {
	private static final String DB_PASSWORD = "dbPassword";
	private static final String DB_USER = "dbUser";
	private static final String DB_URL = "dbUrl";
	private static final String DB_PROPERTIES = "db.properties";
	private Properties globalProps;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public DataBase() {
		try {
			readProperties();
		}
		catch (IOException e) {
			logger.error("Failed to read properties");
		}
	}

	public void writeProperties() throws IOException {
		Properties prop = new Properties();
		prop.setProperty(DB_URL, "localhost");
		prop.setProperty(DB_USER, "username");
		prop.setProperty(DB_PASSWORD, "password");

		try (OutputStream out = new FileOutputStream("project.properties")) {
			prop.store(out, "Database Properties File");
		}
	}

	public void readProperties() throws IOException {
		try (InputStream in = new FileInputStream(DB_PROPERTIES)) {
			globalProps = new Properties();
			globalProps.load(in);
		}
	}

	public Connection connectToDatabase() throws SQLException {
		Properties connectionProps = new Properties();
		connectionProps.put("user", globalProps.getProperty(DB_USER));
		connectionProps.put("password", globalProps.getProperty(DB_PASSWORD));

		// registerDriver is optional for some drivers, required for others
		DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
		return DriverManager.getConnection(globalProps.getProperty(DB_URL),
				connectionProps);
	}

	public Connection connectUsingDataSource() throws SQLException {
		EmbeddedDataSource ds = new org.apache.derby.jdbc.EmbeddedDataSource();
		ds.setDatabaseName("test.db;create=true");
		ds.setUser(globalProps.getProperty(DB_USER));
		ds.setPassword(globalProps.getProperty(DB_PASSWORD));
		Connection conn = ds.getConnection();
		conn.setAutoCommit(true);
		return conn;
	}

	public boolean deleteTable(String table) {
		StringBuilder sb = new StringBuilder();
		sb.append("DROP TABLE ").append(table);
		return modifyScript(sb.toString());
	}

	public void createDatabaseTable(String title) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE").append(" ").append(title)
				.append(" (id int GENERATED ALWAYS AS IDENTITY)");
		modifyScript(sb.toString());
	}

	public List<String> getTables() {
		List<String> tables = new ArrayList<>();
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			try (ResultSet rs = metaData.getTables(null, null, "%",
					new String[] { "TABLE" })) {
				while (rs.next()) {
					tables.add(rs.getString(3));
				}
			}
		}
		catch (SQLException e) {
			logger.error("Failed to get tables", e);
		}
		catch (Exception e) {
			logger.error("Failed to get tables", e);
		}
		return tables;
	}

	public void closeDatabaseConnection() throws SQLException {
		conn.close();

		try {
			DriverManager.getConnection("jdbc:derby:test.db;shutdown=true");
		}
		catch (SQLException sqe) {
			if ((sqe.getErrorCode() == 45000) || (sqe.getErrorCode() == 50000)) {
			}
			else {
				throw sqe;
			}
		}
	}

	private Connection conn;

	public boolean connect() {
		try {
			conn = connectUsingDataSource();
		}
		catch (SQLException e) {
			return false;
		}
		return conn != null;
	}

	public boolean isConnected() {
		try {
			return conn != null && !conn.isClosed();
		}
		catch (SQLException e) {
			return false;
		}
	}

	/**
	 * @param sqlQuery
	 */
	public Map<String, List<Object>> selectFromDb(String sqlQuery) {
		Map<String, List<Object>> res = new HashMap<>();
		try (PreparedStatement s = conn.prepareStatement(sqlQuery)) {
			try (ResultSet executeQuery = s.executeQuery()) {
				logger.info("Executed: {}", sqlQuery);
				ResultSetMetaData metaData = executeQuery.getMetaData();
				res = ResultUtils.parseResultMetaData(metaData);
				while (executeQuery.next()) {
					ResultUtils.addRow(executeQuery, metaData, res);
				}
			}
		}
		catch (SQLException e) {
			logger.error("Failed to execute: {}", sqlQuery, e);
		}
		return res;
	}

	/**
	 * @param sqlQuery
	 */
	public boolean modifyScript(String sqlQuery) {
		try (PreparedStatement s = conn.prepareStatement(sqlQuery)) {
			s.executeUpdate();
			logger.info("Executed: {}", sqlQuery);
			return true;
		}
		catch (SQLException e) {
			logger.error("Failed to execute: {}", sqlQuery, e);
		}
		return false;
	}
}
