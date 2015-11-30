package com.github.cloudecho.config;


import org.junit.Test;

public class AppUtilsTest {

	@Test
	public void testRandomDigit() {
		for (int i = 0; i < 20; i++)
			System.out.println("testRandomDigit: " + AppUtils.randomDigit(6));
	}

}
