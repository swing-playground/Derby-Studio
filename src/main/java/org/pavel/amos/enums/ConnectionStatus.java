package org.pavel.amos.enums;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public enum ConnectionStatus {

	OK, FAILED;

	private static final Map<ConnectionStatus, Color> colors;

	static {
		colors = new HashMap<>();
		colors.put(OK, new Color(24, 183, 22));
		colors.put(FAILED, new Color(255, 0, 0));
	}

	public Color getStatusColor() {
		return colors.get(this);
	}
}
