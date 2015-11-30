package com.github.cloudecho.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.xmlbean.util.DateUtils;
import org.xmlbean.util.PubUtils;

/**
 * 工具类
 * 
 * @author yong.ma
 * @since 2013-08-02
 */
public abstract class AppUtils {
	static final String STRING_GET = "get";
	static final String STRING_SET = "set";
	static final char CHAR_UNDERLINE = '_';

	static final String ENV_VAR_PREFIX = "$";
	public static final String SERVER_HOME = "SERVER_HOME";

	public static void notNull(Object object, String message) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * 取得当前时间字符串
	 */
	public static String getNowString() {
		return DateUtils.formatDate(new Date(), DateUtils.PATTERN_DATETIME);
	}

	/**
	 * 取得当前日期字符串
	 */
	public static String getTodayString() {
		return DateUtils.formatDate(new Date(), DateUtils.PATTERN_DEFAULT);
	}

	/**
	 * 转换日期字符串格式
	 */
	public static String getTodayStr() {
		return DateUtils.formatDate(new Date(), DateUtils.PATTERN_DATE_COMPACT);
	}

	/**
	 * 
	 * @deprecated Replace the {@code envName } with specified environment
	 *             variable value
	 */
	static String stripEnv(String original, String envName) {
		notNull(original, "original");
		notNull(envName, "envName");
		String val = System.getenv(envName);
		if (PubUtils.isEmpty(val)) {
			throw new ConfigException("Environment variable not found: "
					+ envName);
		}
		return original.replace(ENV_VAR_PREFIX + envName,
				val.replace('\\', '/'));
	}

	/**
	 * 自动查找并替换给定字符串里的环境变量.<br>
	 * 给定字符串<code>str</code>包含的环境变量格式：<br>
	 * $[VAR] 或 $VAR（变量名大写，由字母或下划线组成），例如：$[ORG_ID] 或 $ORG_ID
	 */
	public static String stripEnv(String str) {
		notNull(str, "str");
		char[] chars = str.toCharArray();
		// 1.查找环境变量
		Set<String> varSet = new HashSet<String>();
		int start = -1, m = 1;
		for (int i = 0; i < chars.length; i++) {
			char ch = chars[i];
			if (-1 == start) {
				if ('$' == ch) {
					start = i;
				}
			} else if ('[' == ch) {
				m++;
			} else if ('_' != ch && (ch < 'A' || ch > 'Z')) {
				if (i - start > m) {
					String var = str.substring(start, i + m - 1);
					varSet.add(var);
				}
				start = -1;
				m = 1;
			} // else continue
		}
		if (start > -1 && chars.length - start > m && m <= 1) { // $ found
			String var = str.substring(start, chars.length + m - 1);
			varSet.add(var);
		}
		// 2.替换环境变量
		for (String var : varSet) {
			String envName = var.replaceAll("\\$|\\[|\\]", "");
			String val = System.getenv(envName);
			if (PubUtils.isEmpty(val)) {
				continue;
			}
			str = str.replace(var, val.replace('\\', '/'));
		}
		return str;
	}

	/**
	 * 左补齐
	 */
	public static String leftPad(String str, int len, char pad) {
		notNull(str, "str");
		if (str.length() >= len) {
			return str;
		}

		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < len - str.length(); i++) {
			buf.append(pad);
		}
		buf.append(str);
		return buf.toString();
	}

	/**
	 * 右补齐
	 */
	public static String rightPad(String str, int len, char pad) {
		notNull(str, "str");
		if (str.length() >= len) {
			return str;
		}
		StringBuilder buf = new StringBuilder();
		buf.append(str);
		for (int i = 0; i < len - str.length(); i++) {
			buf.append(pad);
		}
		return buf.toString();
	}

	/**
	 * Populate the {@code bean} with {@code map} by {@code bean}'s
	 * {@code setter}.
	 */
	public static void copyFrom(Object bean, Map<String, Object> map) {
		notNull(bean, "bean");
		notNull(map, "map");
		for (Method m : bean.getClass().getMethods()) {
			String name = m.getName();
			if (name.startsWith(STRING_SET)) {
				String key = toDbField(name.substring(STRING_SET.length()));
				Object val = map.get(key);
				if (val == null) {
					continue;
				}
				try {
					Class<?> clazz = m.getParameterTypes()[0];
					val = clazz.getMethod("valueOf", Object.class).invoke(
							clazz, val);
					// set value
					m.invoke(bean, new Object[] { val });
				} catch (IllegalArgumentException e) {
					// do nothing
				} catch (IllegalAccessException e) {
					// do nothing
				} catch (InvocationTargetException e) {
					// do nothing
				} catch (SecurityException e) {
					// do nothing
				} catch (NoSuchMethodException e) {
					// do nothing
				}
			}
		}
	}

	/**
	 * e.g. <br/>
	 * getName --> GET_NAME
	 */
	public static String toDbField(String field) {
		notNull(field, "field");
		StringBuilder buf = new StringBuilder(10 + field.length());
		for (char ch : field.toCharArray()) {
			if (Character.isUpperCase(ch) && buf.length() > 0) {
				buf.append(CHAR_UNDERLINE);
			}
			buf.append(ch);
		}
		return buf.toString().toUpperCase();
	}

	/**
	 * Check the {@code host:port} available
	 * 
	 * @param host
	 *            the target host
	 * @param port
	 *            the target port
	 * @param timeout
	 *            the time, in milliseconds, before the call aborts
	 */
	public static boolean isReachable(String host, int port, int timeout) {
		notNull(host, "host");
		InetSocketAddress endPoint = new InetSocketAddress(host, port);
		if (endPoint.isUnresolved()) {
			return false;
		}

		Socket socket = new Socket();
		try {
			socket.connect(endPoint, timeout);
			return true;
		} catch (IOException ioe) {
			return false;
		} finally {
			try {
				socket.close();
			} catch (IOException ioe) {
			}
		}
	}

	public static String renameFile(String oldPath, String newPath,
			int timeoutSeconds) throws InterruptedException {
		File oldFile = new File(oldPath);
		File newFile = new File(newPath);
		long start = System.currentTimeMillis();
		while (!oldFile.renameTo(newFile)
				&& System.currentTimeMillis() - start < 1000 * timeoutSeconds) {
			Thread.sleep(200L);// sleep 0.2 seconds
		}
		return newPath;
	}

	public static boolean deleteFile(String path, int timeoutSeconds)
			throws InterruptedException {
		long start = System.currentTimeMillis();
		File file = new File(path);
		while (!file.delete()
				&& System.currentTimeMillis() - start < 1000 * timeoutSeconds) {
			Thread.sleep(200L);// sleep 0.2 seconds
		}
		return !file.exists();
	}

	/**
	 * 修改时间
	 * 
	 * @param d
	 * @param filed
	 *            e.g. Calendar.SECOND
	 * @param value
	 * @return 新的日期
	 */
	public static Date addDate(Date d, int filed, int value) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(filed, c.get(filed) + value);
		return c.getTime();
	}

	/**
	 * 判断两个时间是否在同一天
	 */
	public static boolean isSameDay(Date d1, Date d2) {
		String fmt = DateUtils.PATTERN_DATE_COMPACT;
		return DateUtils.formatDate(d1, fmt).equals(
				DateUtils.formatDate(d2, fmt));
	}

	/**
	 * 截取字符串前LEN个字符
	 */
	public static String trimByLength(Object o, int len) {
		String s = String.valueOf(o);
		if (s.length() > len) {
			return s.substring(0, len) + "...";
		} else {
			return s;
		}
	}

	public static Long valueOfLong(Object val) {
		if (val instanceof Long) {
			return (Long) val;
		} else if (val instanceof Integer) {
			return ((Integer) val).longValue();
		} else if (val instanceof BigInteger) {
			return ((BigInteger) val).longValue();
		} else if (val instanceof BigDecimal) {
			return ((BigDecimal) val).longValue();
		} else {
			String x = String.valueOf(val).trim();
			if ("null".equals(x) || "".equals(x)) {
				return 0L;
			}
			return Long.valueOf(x);
		}
	}

	public static String randomDigit(int len) {
		int n = new Random().nextInt((int) Math.pow(10, len));
		return leftPad(String.valueOf(n), len, '0');
	}
}
