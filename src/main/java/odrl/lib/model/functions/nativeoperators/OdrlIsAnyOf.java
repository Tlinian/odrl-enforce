package odrl.lib.model.functions.nativeoperators;

import odrl.lib.model.Sparql;
import odrl.lib.model.exceptions.EvaluationException;
import odrl.lib.model.exceptions.RuntimeEvaluationException;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.resultset.ResultsFormat;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

public class OdrlIsAnyOf extends OdrlNative{

	@Override
	public String getName() {
		return "isAnyOf";
	}


	@Override
	public NodeValue exec(NodeValue v1, NodeValue v2) {
		Boolean result = solveOperator(v1, v2, getName(), " in ");
		return NodeValue.makeNodeBoolean(result);
	}

	@Override
	protected Boolean solveOperator(NodeValue v1, NodeValue v2, String opName, String op) throws RuntimeEvaluationException {
		Boolean result = false;
		try {
			String v2Target = Arrays.stream(v2.getString().split(",")).map(s -> "\"" + s + "\"").collect(Collectors.joining(","));
			String formattedQuery = QUERY.replace(QUERY_REPLACEMENT_1, v1.toString()).replace(QUERY_REPLACEMENT_2, "("+v2Target+")").replace(QUERY_REPLACEMENT_3, op);
			ByteArrayOutputStream out = Sparql.queryModel(formattedQuery, ModelFactory.createDefaultModel(), ResultsFormat.FMT_RS_CSV, null);
			String rawString = new String(out.toByteArray());
			String rawBoolean = rawString.split("\n")[1].trim();
			if(rawBoolean.isEmpty() && v1.getDatatypeURI().equals(v2.getDatatypeURI()) )
				throw new EvaluationException("Provided operands have datatypes incompatible for the operand "+opName);
			result = Boolean.valueOf(rawBoolean);
			out.close();
		}catch(Exception e) {
			throw new RuntimeEvaluationException(e.getMessage());
		}
		return result;
	}

}
