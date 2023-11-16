package com.platform;

public class Mac implements OperatingSystem {
	@Override
	public boolean isSupported(String feature) {
		switch (feature) {
		case "fade":
			return true;
		default:
			return true;
		}
	}

}
