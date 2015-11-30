package com.github.cloudecho.config;

import org.junit.Test;

public class AppConfigsTest {

	@Test
	public void test() {
		AppConfigs cfg = AppConfigs.getInstance("/appconfig-sample.xml");
		System.out.println(cfg.get("SOFT_VERSION"));
	}

}
