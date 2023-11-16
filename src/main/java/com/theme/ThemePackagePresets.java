package com.theme;

import java.awt.Color;
import java.awt.Font;

public class ThemePackagePresets {
	private ThemePackagePresets() {
	}

	public static ThemePackage cleanLight() {
		ThemePackage pack = new ThemePackage();

		WindowTheme window = new WindowTheme();
		window.background = new Color(255, 255, 255);
		window.foreground = new Color(160, 205, 250);
		window.opacity = 0.8f;
		window.width = 300;
		window.height = 100;

		TextTheme text = new TextTheme();
		text.title = new Font("Arial", Font.BOLD, 22);
		text.subtitle = new Font("Arial", Font.PLAIN, 16);
		text.titleColor = new Color(10, 10, 10);
		text.subtitleColor = new Color(10, 10, 10);

		pack.setTheme(WindowTheme.class, window);
		pack.setTheme(TextTheme.class, text);

		return pack;
	}

	public static ThemePackage cleanDark() {
		ThemePackage pack = new ThemePackage();

		WindowTheme window = new WindowTheme();
		window.background = new Color(0, 0, 0);
		window.foreground = new Color(16, 124, 162);
		window.opacity = 0.8f;
		window.width = 300;
		window.height = 100;

		TextTheme text = new TextTheme();
		text.title = new Font("Arial", Font.BOLD, 22);
		text.subtitle = new Font("Arial", Font.PLAIN, 16);
		text.titleColor = new Color(200, 200, 200);
		text.subtitleColor = new Color(200, 200, 200);

		pack.setTheme(WindowTheme.class, window);
		pack.setTheme(TextTheme.class, text);

		return pack;
	}

	public static ThemePackage aqua() {
		ThemePackage pack = new ThemePackage();

		WindowTheme window = new WindowTheme();
		window.background = new Color(0, 191, 255);
		window.foreground = new Color(0, 30, 255);
		window.opacity = 0.5f;
		window.width = 300;
		window.height = 100;

		TextTheme text = new TextTheme();
		text.title = new Font("Comic Sans MS", Font.BOLD, 22);
		text.subtitle = new Font("Comic Sans MS", Font.PLAIN, 16);
		text.titleColor = new Color(10, 10, 10);
		text.subtitleColor = new Color(10, 10, 10);

		pack.setTheme(WindowTheme.class, window);
		pack.setTheme(TextTheme.class, text);

		return pack;
	}
}
