package com.scs.stetech1.unittests;

public class UnitTestsMain {

	public UnitTestsMain() {
		IUnitTestClass testPosCalc = new TestPositionCalculator();
		testPosCalc.runTests();
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new UnitTestsMain();

	}

}
