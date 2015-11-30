package com.github.cloudecho.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmlbean.XmlBeanHelper;
import org.xmlbean.util.PubUtils;

import com.github.cloudecho.config.AppConfigBean.Param;

/**
 * Application configuration utilities.<br>
 * There are tow configuration file: <br>
 * 1. /confiappconfig.xml <br>
 * 2. server-config.xml which located by the {@link #SERVER_CONFIG} key in the
 * appconfig.xml
 * 
 * @author yong.ma
 * @since 2013-08-01
 */
public class AppConfigs {
	private static final Log LOG = LogFactory.getLog(AppConfigs.class);

	private final Map<String, Param> config;

	/**
	 * The key which locates the server configuration file <br>
	 * Server configuration extends from the default
	 * configuration("/config/appconfig.xml")
	 */
	static final String SERVER_CONFIG = "SERVER_CONFIG";

	private AppConfigs(String configfile) {
		config = loadConfig(configfile);
		Map<String, Param> serverConfig = loadServerConfig(configfile);
		if (serverConfig != null) {
			config.putAll(serverConfig);
		}

	}

	/**
	 * 获取AppConfigs实例
	 * 
	 * @param configfile
	 *            JAR中配置文件路径
	 */
	public static AppConfigs getInstance(String configfile) {
		return new AppConfigs(configfile);
	}

	static final String RESOURCES_DIR = "/resources";

	private Map<String, Param> loadConfig(final String file) {
		try {
			InputStream in = getResourceAsStream(file);
			return XmlBeanHelper.getBean(in, AppConfigBean.class).asMap();
		} catch (Exception e) {
			throw new ConfigException("Load configuration [" + file + "] fail.", e);
		}
	}

	public static InputStream getResourceAsStream(final String file) {
		InputStream in = AppConfigs.class.getResourceAsStream(file);
		if (in == null) {
			in = AppConfigs.class.getResourceAsStream(RESOURCES_DIR + file);
		}
		return in;
	}

	/**
	 * 读取属性文件
	 * 
	 * @param file
	 *            属性文件路径
	 * @return Properties 对象（保证非null）
	 */
	public static Properties getResourceAsProperties(String file) {
		Properties p = new Properties();
		InputStream in = getResourceAsStream(file);
		if (in == null) {
			return p;
		}

		try {
			p.load(in);
		} catch (IOException e) {
			throw new RuntimeException("IOException: " + e.getMessage());
		}

		return p;
	}

	private Map<String, Param> loadServerConfig(String appConfigfile) {
		String path = get(SERVER_CONFIG);
		if (!PubUtils.hasText(path)) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Server config key[" + SERVER_CONFIG + "] not found in \"" + appConfigfile
						+ "\"");
			}
			return null;
		}

		File file = new File(path);
		if (!file.exists()) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Server config file not exists: " + path);
			}
			return null;
		}

		try {
			return XmlBeanHelper.getBean(file, AppConfigBean.class).asMap();
		} catch (Exception e) {
			throw new ConfigException("Load configuration [" + path + "] fail.", e);
		}
	}

	public String get(String key) {
		return getConfig(key).getParamVal();
	}

	public int getInt(String key) {
		return Integer.parseInt(get(key));
	}

	public long getLong(String key) {
		return Long.parseLong(get(key));
	}

	public boolean getBoolean(String key) {
		return Boolean.parseBoolean(get(key));
	}

	public <T> T getBeanFromXML(String key, Class<T> target) {
		try {
			return XmlBeanHelper.getBean(get(key), target);
		} catch (Exception ex) {
			throw new ConfigException(ex);
		}
	}

	public Param getConfig(String key) {
		Param p = config.get(key);
		if (p == null) {
			throw new NullPointerException("defaultConfig." + key);
		}
		return p;
	}

	/**
	 * 数据缓存时间
	 */
	public int getCacheSeconds() {
		return Integer.parseInt(get("CACHE_SECONDS"));
	}

	// -- DEFAULT_APPCONFIGS

	public static final class DEFAULT {
		private static final String CONFIG_PROPFILE = "/appconfig.properties";

		private static final String KEY_CONFIGFILE = "CONFIGFILE";

		private static final String DEFAULT_CONFIGFILE = "/appconfig.xml";

		private static final AppConfigs DEFAULT_APPCONFIG;

		static {
			Properties p = getResourceAsProperties(CONFIG_PROPFILE);
			String configfile = p.getProperty(KEY_CONFIGFILE) == null ? DEFAULT_CONFIGFILE : p
					.getProperty(KEY_CONFIGFILE);
			DEFAULT_APPCONFIG = new AppConfigs(configfile);
		}

		public static String get(String key) {
			return DEFAULT_APPCONFIG.get(key);
		}

		public static int getInt(String key) {
			return DEFAULT_APPCONFIG.getInt(key);
		}

		public static long getLong(String key) {
			return DEFAULT_APPCONFIG.getLong(key);
		}

		public static boolean getBoolean(String key) {
			return DEFAULT_APPCONFIG.getBoolean(key);
		}

		public static <T> T getBeanFromXML(String key, Class<T> target) {
			return DEFAULT_APPCONFIG.getBeanFromXML(key, target);
		}

		public static Param getConfig(String key) {
			return DEFAULT_APPCONFIG.getConfig(key);
		}

		/**
		 * 数据缓存时间
		 */
		public static int getCacheSeconds() {
			return DEFAULT_APPCONFIG.getCacheSeconds();
		}
	}
}
