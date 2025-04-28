package odrl.lib.model.result;

import odrl.lib.model.Constraint;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class ConstraintResult {
    private final List<Constraint> passCons = Lists.newArrayList();
    private final List<Constraint> failCons = Lists.newArrayList();

    public List<Constraint> getFailCons() {
        return failCons;
    }

    public List<Constraint> getPassCons() {
        return passCons;
    }

    @Override
    public String toString() {
        return "ConstraintResult{\n" +
                "passCons=" + passCons +
                "\n, failCons=" + failCons +
                '}';
    }
}
