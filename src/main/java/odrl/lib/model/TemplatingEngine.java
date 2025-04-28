package odrl.lib.model;

import java.util.Map;

public interface TemplatingEngine {

	String reduce(String templateStr, Map<String, Object> interpolatedVariables,
			Map<String, Object> interpolatedFunctions);

}