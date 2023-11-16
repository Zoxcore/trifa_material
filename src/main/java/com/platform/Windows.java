package com.platform;

public class Windows implements OperatingSystem {
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
