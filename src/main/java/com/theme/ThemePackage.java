package com.theme;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains a collection of themes that can be accessed later.
 */
public class ThemePackage {
	private Map<Class<?>, Object> m_themes;

	public ThemePackage() {
		m_themes = new HashMap<Class<?>, Object>();
	}

	/**
	 * Sets the theme object related with the theme class. The first parameter should be the class of the second
	 * parameter.
	 *
	 * @param <T>
	 *            the Class of the theme
	 * @param themeClass
	 *            the Class of the theme to set
	 * @param theme
	 *            the theme to set
	 */
	public <T> void setTheme(Class<T> themeClass, T theme) {
		m_themes.put(themeClass, theme);
	}

	/**
	 * Gets the theme object related with the theme class.
	 *
	 * @param <T>
	 *            the Class of the theme
	 * @param themeClass
	 *            the Class of the theme to return
	 * @return the theme corresponding with the given Class
	 */
	public <T> T getTheme(Class<T> themeClass) {
		@SuppressWarnings("unchecked")
		T theme = (T) m_themes.get(themeClass);
		return theme;
	}
}
