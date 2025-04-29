package tests.odrl.lib.data;

import com.google.gson.JsonObject;
import odrl.lib.model.OdrlLib;
import odrl.lib.model.Policies;
import odrl.lib.model.exceptions.OdrlRegistrationException;
import odrl.lib.model.explorer.ODRLExtractor;
import odrl.lib.model.functions.Time;
import odrl.lib.model.result.EnforcePolicyResult;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tests.odrl.lib.Tests;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

public class TestExtensionParsing {



	private StringBuilder dir = new StringBuilder("./src/test/resources/data/");

	@Before
	public void setup() throws OdrlRegistrationException {
		Tests.odrl.registerGeof();
		Tests.odrl.registerSpatial();
		OdrlLib.setDebug(true);
	}



	@Test
	public void test01() throws Exception  {

		String policy = Tests.readPolicy(dir.append("in-cons.json").toString());
//		Map<String, List<String>> result = Tests.solvePolicy(policy);
//		System.out.println(result);
//		Assert.assertTrue(result.isEmpty());
		EnforcePolicyResult enforcePolicyResult = Tests.solvePolicyResult(policy);
		System.out.println(enforcePolicyResult);
	}

	@Test
	public void test02() throws Exception  {

		String policy = Tests.readPolicy(dir.append("or-cons.json").toString());
//		Map<String, List<String>> result = Tests.solvePolicy(policy);
//		System.out.println(result);
//		Assert.assertTrue(result.isEmpty());
		Model model = parsePolicy(policy);
		ODRLExtractor.extractConstraints(model);
	}

	private static Lang policySerialization = Lang.JSONLD11;

	private static Model parsePolicy(String policy)  {
		Model model = ModelFactory.createDefaultModel();
		// JSON-LD to RDF
//		policy = policy.replace("\"http://www.w3.org/ns/odrl.jsonld\"", Context.ODRL_CONTEXT);
		// 修改后（使用完整的 ODRL 2.2 上下文）
//		policy = policy.replaceAll(
//				"\"http://www.w3.org/ns/odrl.jsonld\"",
//				"\"http://www.w3.org/ns/odrl/2/ODRL22.jsonld\""
//		);

		try {
			RDFParser.source(new ByteArrayInputStream(policy.getBytes()))
					.forceLang(policySerialization).parse(model);
		} catch (Exception e) {
			throw e;
		}
		model.write(System.out, "TURTLE");
		return model;
	}
}
