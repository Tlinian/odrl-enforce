package tests.odrl.lib.data;

import odrl.lib.model.OdrlLib;
import odrl.lib.model.exceptions.OdrlRegistrationException;
import org.junit.Before;
import org.junit.Test;
import tests.odrl.lib.Tests;

public class TestPolicyEnd2 {



	private StringBuilder dir = new StringBuilder("./src/test/resources/data/");

	@Before
	public void setup() throws OdrlRegistrationException {
		Tests.odrl.registerGeof();
		Tests.odrl.registerSpatial();
		OdrlLib.setDebug(true);
	}



	@Test
	public void test01() throws Exception  {

		String policy = Tests.readPolicy(dir.append("all-policy-end-2.json").toString());
//		Map<String, List<String>> result = Tests.solvePolicy(policy);
//		System.out.println(result);
//		Assert.assertTrue(result.isEmpty());
//		EnforcePolicyResult enforcePolicyResult = Tests.solvePolicyResult(policy);
		System.out.println(Tests.solvePolicyResultToJson(policy));
	}
}
