package com.scs.stevetech1.unittests;

import com.scs.stevetech1.server.Globals;

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
		Globals.p("Tests finished.  Any errors are shown above");
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new UnitTestsMain();

	}

}
