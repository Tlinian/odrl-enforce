package odrl.lib.model.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnforcePolicyResult {
    List<ActionResult> actionResults = new ArrayList<>();

    public List<ActionResult> getActionResults() {
        return actionResults;
    }

    @Override
    public String toString() {
        return "EnforcePolicyResult{\n" +
                "actionResults=\n" + actionResults +
                '}';
    }
}
