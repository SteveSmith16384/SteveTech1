package com.scs.stetech1.unittests;

import com.scs.stetech1.server.Settings;

public class UnitTestsMain {

	public UnitTestsMain() {
		TestPositionCalculator testPosCalc = new TestPositionCalculator();
		testPosCalc.runTests();
		Settings.p("Tests finished");
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new UnitTestsMain();

	}

}
