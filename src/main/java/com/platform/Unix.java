package com.platform;

public class Unix implements OperatingSystem {
	@Override
	public boolean isSupported(String feature) {
		switch (feature) {
		case "fade":
			return false;
		default:
			return true;
		}
	}
}
