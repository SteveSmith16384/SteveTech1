package com.scs.stetech1.unittests;

import com.scs.stetech1.server.Settings;

public class UnitTestsMain {

	public UnitTestsMain() {
		try {
			TestPositionCalculator testPosCalc = new TestPositionCalculator();
			testPosCalc.runTests();

			TestAveragePingTime aptTests = new TestAveragePingTime();
			aptTests.runTests();

			TestAvatarPositionCalcs tapc = new TestAvatarPositionCalcs();
			tapc.runTests();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Settings.p("Tests finished.  Any errors are shown above");
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new UnitTestsMain();

	}

}
