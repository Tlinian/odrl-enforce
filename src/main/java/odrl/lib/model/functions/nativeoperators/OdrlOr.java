package odrl.lib.model.functions.nativeoperators;

import odrl.lib.model.functions.IFunction;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;

import java.util.List;

public class OdrlOr extends FunctionBase implements IFunction {

	@Override
	public String getName() {
		return "or";
	}

	@Override
	public final NodeValue exec(List<NodeValue> args) {
		if (args == null) {
			throw new ARQInternalErrorException(Lib.className(this) + ": Null args list");
		} else if (args.size() < 2) {
			String var10002 = Lib.className(this);
			throw new ExprEvalException(var10002 + ": Wrong number of arguments: Wanted 2, got " + args.size());
		} else if (args.size() == 2){
			NodeValue v1 = (NodeValue)args.get(0);
			NodeValue v2 = (NodeValue)args.get(1);
			return this.exec(v1, v2);
		}else {
			for (NodeValue v : args) {
				if (v.getBoolean()) {
					return NodeValue.TRUE;
				}
			}
			return NodeValue.FALSE;
		}
	}

	@Override
	public void checkBuild(String uri, ExprList args) {
		if (args.size() < 2) {
			throw new QueryBuildException("Function '" + Lib.className(this) + "' takes two arguments");
		}
	}


	public NodeValue exec(NodeValue v1, NodeValue v2) {
		return NodeValue.makeNodeBoolean(true);
	}
}
