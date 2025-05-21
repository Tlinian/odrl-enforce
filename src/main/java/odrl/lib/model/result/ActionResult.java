package odrl.lib.model.result;

import odrl.lib.model.Action;
import odrl.lib.model.Constraint;
import org.apache.commons.compress.utils.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionResult {
    private String target;
    private Map<String, ConstraintResult> actionAndConstraintResults = new HashMap<>();

    public Map<String, ConstraintResult> getActionAndConstraintResults() {
        return actionAndConstraintResults;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "ActionResult{\n" +
                "actionAndConstraintResults=" + actionAndConstraintResults +
                '}';
    }
}
