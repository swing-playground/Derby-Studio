package org.pavel.amos.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResultUtils {

	private ResultUtils() {
	}

	public static Map<String, List<Object>> parseResultMetaData(ResultSetMetaData rsm)
			throws SQLException {
		Map<String, List<Object>> res = new LinkedHashMap<>();
		int columnCount = rsm.getColumnCount();
		for (int i = 1; i <= columnCount; i++) {
			res.put(rsm.getColumnLabel(i), new ArrayList<>());
		}
		return res;
	}

	public static void addRow(ResultSet rs, ResultSetMetaData metaData,
			Map<String, List<Object>> res) throws SQLException {
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			String title = metaData.getColumnLabel(i);
			res.get(title).add(rs.getObject(i));
		}
	}
}
