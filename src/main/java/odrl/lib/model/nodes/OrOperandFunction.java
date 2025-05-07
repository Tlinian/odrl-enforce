package odrl.lib.model.nodes;

import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class OrOperandFunction extends OperandFunction {

	private String function;
	private List<OperandFunction> operandFunctions;
	private Boolean isStringFunction = false;

	public OrOperandFunction(String function)  {
		super(function);
		this.function = function;
		this.operandFunctions = Lists.newArrayList();
	}

	public OrOperandFunction(String function, List<OperandFunction> operandFunctions, boolean isStringFunction) {
		super(function);
		this.function = function;
		this.operandFunctions = operandFunctions;
		this.isStringFunction = isStringFunction;
	}



	public List<OperandFunction> getOperands() {
		return operandFunctions;
	}

	public void setOperandFunctions(List<OperandFunction> operandFunctions) {
		this.operandFunctions = operandFunctions;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function)  {
		this.function = function;
	}

	private static final char TOKEN_PAR_1 = '(';
	private static final char TOKEN_PAR_2 = ')';
	private static final char TOKEN_COMMA = ',';

	@Override
	public String toSPARQL() {
		StringBuilder sparqlRepresentation = new StringBuilder();
		sparqlRepresentation.append(function);
		if(!isStringFunction) {
			sparqlRepresentation.append(TOKEN_PAR_1);
			for(int index = 0; index < operandFunctions.size(); index++) {
				 sparqlRepresentation.append(operandFunctions.get(index).toSPARQL());
				if(index+1< operandFunctions.size())
					sparqlRepresentation.append(TOKEN_COMMA);
			}
		    sparqlRepresentation.append(TOKEN_PAR_2);
		}
		return sparqlRepresentation.toString();
	}

	@Override
	public String toString() {
		return "OperandFunction{" +
				"function='" + function + '\'' +
				", arguments=" + operandFunctions +
				", isStringFunction=" + isStringFunction +
				'}';
	}
}
