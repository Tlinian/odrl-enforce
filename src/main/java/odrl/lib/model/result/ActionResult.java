package odrl.lib.model.result;

import odrl.lib.model.Action;
import odrl.lib.model.Constraint;
import org.apache.commons.compress.utils.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionResult {
    private Map<List<String>, ConstraintResult> actionAndConstraintResults = new HashMap<>();

    public Map<List<String>, ConstraintResult> getActionAndConstraintResults() {
        return actionAndConstraintResults;
    }

    @Override
    public String toString() {
        return "ActionResult{\n" +
                "actionAndConstraintResults=" + actionAndConstraintResults +
                '}';
    }
}
