package odrl.lib.model;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import odrl.lib.model.functions.Time;
import odrl.lib.model.functions.nativeoperators.OdrlEq;
import odrl.lib.model.functions.nativeoperators.OdrlGt;
import odrl.lib.model.functions.nativeoperators.OdrlGteq;
import odrl.lib.model.functions.nativeoperators.OdrlIn;
import odrl.lib.model.functions.nativeoperators.OdrlIsAnyOf;
import odrl.lib.model.functions.nativeoperators.OdrlLt;
import odrl.lib.model.functions.nativeoperators.OdrlLteq;
import odrl.lib.model.functions.nativeoperators.OdrlNeq;
import odrl.lib.model.functions.nativeoperators.OdrlOr;
import odrl.lib.model.nodes.IOperand;
import odrl.lib.model.nodes.OperandFactory;
import odrl.lib.model.nodes.OperandFunction;
import odrl.lib.model.nodes.OperandValue;
import odrl.lib.model.nodes.OrOperandFunction;
import odrl.lib.model.result.ActionResult;
import odrl.lib.model.result.ConstraintResult;
import odrl.lib.model.result.EnforcePolicyResult;
import org.apache.commons.compress.utils.Lists;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.function.FunctionRegistry;
import com.google.gson.JsonObject;
import odrl.lib.model.exceptions.EvaluationException;
import odrl.lib.model.exceptions.OdrlRegistrationException;
import odrl.lib.model.exceptions.OperandException;
import odrl.lib.model.exceptions.OperatorException;
import odrl.lib.model.exceptions.UnsupportedFunctionException;
import odrl.lib.model.functions.DateTime;
import odrl.lib.model.functions.IFunction;
import odrl.lib.model.functions.Spatial;
import static odrl.lib.model.nodes.OperandFactory.shortenURI;

public class OdrlLib {
    private static final String QUERY_REPLACEMENT = "#RULE_ID#";
    private static final String ODRL_URL =  "http://www.w3.org/ns/odrl/2/";

    private static final String CONSTRAINTS_QUERY_STRING = "PREFIX odrl: <http://www.w3.org/ns/odrl/2/>\n"
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "SELECT ?operator ?left_operand ?right_operand " +
            "WHERE { <#RULE_ID#> odrl:operator ?operator;  " +
            " odrl:leftOperand ?left_operand;  " +
            " odrl:rightOperand ?right_operand . }";
    private static final String RESTRICTIONS_QUERY_ARG_OPERATOR = "operator";
    private static final String RESTRICTIONS_QUERY_ARG_LEFTOPERAND = "left_operand";
    private static final String RESTRICTIONS_QUERY_ARG_RIGHTOPERAND = "right_operand";

    private static final String PERMISSION_EXCLUDE_IN = "PREFIX odrl: <http://www.w3.org/ns/odrl/2/>\n"
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "SELECT ?action ?target ?constraint ?left ?right ?op ?childConstraint ?childLeft ?childOp ?childRight WHERE { \n"
            + " ?policy odrl:permission ?permission . \n"
            + " ?permission odrl:action ?action .  \n"
            + " ?permission odrl:target ?target .\n"
            + " ?permission odrl:constraint ?constraint .\n"
            + " OPTIONAL { \n" +
            "        ?constraint odrl:leftOperand ?left .\n" +
            "        ?constraint odrl:rightOperand ?right .\n" +
            "    }\n"
            + " ?constraint odrl:operator ?op .\n"
            + " OPTIONAL { \n" +
            "        ?constraint odrl:constraint+ ?childConstraint .\n" +
            "        ?childConstraint odrl:leftOperand ?childLeft ;\n" +
            "    odrl:operator ?childOp ;\n" +
            "    odrl:rightOperand ?childRight ." +
            "    }\n"
            + "}";

    private static final String PERMISSION_INCLUDE_IN = "PREFIX odrl: <http://www.w3.org/ns/odrl/2/>\n"
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "SELECT ?action ?target ?constraint ?left (GROUP_CONCAT( " +
            "IF(\n" +
            "            ?isString, \n" +
            "            CONCAT(\"'\", ?right1, \"'\"), " +
            "            STR(?right1) " +
            "        );  separator=\",\") AS ?right) ?op ?childConstraint ?childLeft ?childOp ?childRight WHERE { \n"
            + " ?policy odrl:permission ?permission . \n"
            + " ?permission odrl:action ?action .  \n"
            + " ?permission odrl:target ?target .\n"
            + " ?permission odrl:constraint ?constraint .\n"
            + " OPTIONAL { \n" +
            "        ?constraint odrl:leftOperand ?left .\n" +
            "        ?constraint odrl:rightOperand ?right1 .\n" +
            " BIND(\n" +
            "            IF( \n" +
            "                DATATYPE(?right1) = xsd:string || \n" +
            "                (!isURI(?right1) && DATATYPE(?right1) = 'null'), \n" +
            "                true, \n" +
            "                false \n" +
            "            ) AS ?isString\n" +
            "        )" +
            "    }\n"
            + " ?constraint odrl:operator ?op .\n" +
            " FILTER (?op = odrl:isAnyOf) "
            + " OPTIONAL { \n" +
            "        ?constraint odrl:constraint+ ?childConstraint .\n" +
            "        ?childConstraint odrl:leftOperand ?childLeft ;\n" +
            "    odrl:operator ?childOp ;\n" +
            "    odrl:rightOperand ?childRight ." +
            "    }\n"
            + "} GROUP BY ?action ?target ?constraint ?left ?op ?childConstraint ?childLeft ?childOp ?childRight\n";

    protected static boolean debug = false;

    private Map<String, String> prefixes = Maps.newHashMap();
    private List<String> functions = Lists.newArrayList();
    public static FreemarkerEngine engine = new FreemarkerEngine();

    public OdrlLib() {
        registerNative();
    }

    public void registerPrefix(String prefix, String uri) {
        this.prefixes.put(prefix, uri);
    }

    public Map<String, String> getPrefixes() {
        return prefixes;
    }

    public List<String> getFunctions() {
        return functions;
    }

    public void register(String prefix, IFunction function) throws OdrlRegistrationException {
        if (!prefixes.containsKey(prefix))
            throw new OdrlRegistrationException("Provided prefix (" + prefix
                    + ") does not exist, register first a URI with that prefix using addPrefix method.");
        String uri = prefixes.get(prefix);
        FunctionRegistry.get().put(uri + function.getName(), function.getClass());
        functions.add(prefix + ":" + function.getName());
    }

    public void register(String sparqlFunction) throws OdrlRegistrationException {
        if (!sparqlFunction.contains(":"))
            throw new OdrlRegistrationException("Provided function must be prefixed, provide a valid function name that follows the convention [prefix]:[name]");
        String[] splitted = sparqlFunction.split(":");
        if (!prefixes.containsKey(splitted[0]))
            throw new OdrlRegistrationException("Provided prefix (" + splitted[0] + ") does not exist, register first a URI with that prefix using addPrefix method.");

        functions.add(sparqlFunction);
    }

    public Map<String, List<String>> solve(JsonObject policyJson)
            throws UnsupportedFunctionException, OperandException, OperatorException, EvaluationException {
        Map<String, List<String>> allowedTo = Maps.newHashMap();
        List<Permission> permissions = mapToPermissions(policyJson);
        for (Permission permission : permissions) {
            List<String> actions = permission.solve(this.prefixes);
            if (!actions.isEmpty())
                allowedTo.put(permission.getTarget(), actions);
        }
        return allowedTo;
    }

    public EnforcePolicyResult solveResult(JsonObject policyJson)
            throws UnsupportedFunctionException, OperandException, OperatorException, EvaluationException {
        EnforcePolicyResult allowedTo = new EnforcePolicyResult();
        List<Permission> permissions = mapToPermissions(policyJson);
        for (Permission permission : permissions) {
            ActionResult actions = permission.solveResult(this.prefixes);
            actions.setTarget(permission.getTarget());
            allowedTo.getActionResults().add(actions);
        }
        return allowedTo;
    }

    public String solveResultToJson(String policy, Map<String, Object> interpolation)
            throws UnsupportedFunctionException, OperandException, OperatorException, EvaluationException, OdrlRegistrationException, IllegalAccessException {
        policy = engine.reduce(policy, interpolation, interpolation);
        JsonObject policyJson = Policies.fromJsonld11String(policy);
        OdrlLib odrl = new OdrlLib();
        odrl.registerPrefix("ops", "http://upm.es/operands#");
        odrl.register("ops", new Time());
        odrl.registerNative();
        EnforcePolicyResult allowedTo = new EnforcePolicyResult();
        List<Permission> permissions = mapToPermissions(policyJson); 
        for (Permission permission : permissions) {
            ActionResult actions = permission.solveResult(this.prefixes);
            allowedTo.getActionResults().add(actions);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(allowedTo);
    }

    public boolean solveResultForAction(String policy, Map<String, Object> interpolation, String target, String action)
            throws UnsupportedFunctionException, OperandException, OperatorException, EvaluationException, OdrlRegistrationException, IllegalAccessException {
        policy = engine.reduce(policy, interpolation, interpolation);
        JsonObject policyJson = Policies.fromJsonld11String(policy);
        OdrlLib odrl = new OdrlLib();
        odrl.registerPrefix("ops", "http://upm.es/operands#");
        odrl.register("ops", new Time());
        odrl.registerNative();
        List<Permission> permissions = mapToPermissions(policyJson);
        for (Permission permission : permissions) {
            if (permission.getTarget().equals(target)){
                ActionResult actions = permission.solveResult(this.prefixes);
                ConstraintResult constraintResult = actions.getActionAndConstraintResults().get(ODRL_URL+action);
                if (constraintResult == null){
                    return false;
                }else {
                    return constraintResult.getFailCons().size() <= 0;
                }
            }
        }
        return false;
    }


    public EnforcePolicyResult solveResult(String policy, Map<String, Object> interpolation)
            throws UnsupportedFunctionException, OperandException, OperatorException, EvaluationException {
        policy = engine.reduce(policy, interpolation, interpolation);
        JsonObject policyJson = Policies.fromJsonld11String(policy);
        EnforcePolicyResult allowedTo = new EnforcePolicyResult();
        List<Permission> permissions = mapToPermissions(policyJson);
        for (Permission permission : permissions) {
            ActionResult actions = permission.solveResult(this.prefixes);
            allowedTo.getActionResults().add(actions);
        }
        return allowedTo;
    }


    public List<Permission> mapToPermissions(JsonObject policy) throws UnsupportedFunctionException, OperandException, OperatorException, EvaluationException {
        List<Permission> permissions = Lists.newArrayList();
        Model model = toRDFModel(policy);
        model.write(System.out, "turtle");
        ResultSet rs = QueryExecutionFactory.create(QueryFactory.create(PERMISSION_EXCLUDE_IN), model).execSelect();
        Map<String, OrOperandFunction> orConstraints = Maps.newHashMap();
        AtomicReference<Boolean> isHasIn = new AtomicReference<>(false);
        Set<Permission> inPermissions = new HashSet<>();
        while (rs.hasNext()) {
            QuerySolution qs = rs.next();
            Permission permission = mapToPermission(model, qs, orConstraints);
            if (permissions.contains(permission)) {
                int oldIndex = permissions.indexOf(permission);
                Permission permissionOld = permissions.remove(oldIndex);
                permissionOld.getActions().forEach(permission::addAction);
            }
            permission.getActions().forEach(action -> {
                if (action.getConstraints().isEmpty())
                    return;
                for (Constraint constraint : action.getConstraints()) {
                    if (constraint.getFunction().equals("odrl:isAnyOf")){
                        isHasIn.set(true);
                        inPermissions.add(permission);
                    }
                }
            });
            permissions.add(permission);
        }
        if (isHasIn.get()) {
            List<Permission> permissions1 = queryInConstraint(model);
            Map<String,Constraint> constraintMap = new HashMap<>();
            permissions1.forEach(permission -> {
                permission.getActions().forEach(action -> {
                    if (action.getConstraints().isEmpty())
                        return;
                    for (Constraint constraint : action.getConstraints()) {
                        if (constraint.getFunction().equals("odrl:isAnyOf")){
                            constraintMap.put(constraint.getConstraintId(),constraint);
                        }
                    }
                });
                    });
            Map<String,Action> actionMap  = new HashMap<>();
            permissions.forEach(permission -> {
                for (Action action : permission.getActions()) {
                    if (action.getConstraints().isEmpty())
                        continue;
                    for (Constraint constraint : new ArrayList<>(action.getConstraints())) {
                        if (constraint.getFunction().equals("odrl:isAnyOf")) {
                            actionMap.put(constraint.getConstraintId(), action);
                            action.getConstraints().remove(constraint);
                        }

                    }
                }
            });
            actionMap.entrySet().stream().forEach(entry -> {
                Constraint constraint = constraintMap.get(entry.getKey());
                entry.getValue().getConstraints().add(constraint);
            });
        }
        if (permissions.isEmpty())
            throw new EvaluationException("Provided policy seems to be malformed since it lacks of correly expressed permissions");
        correctConstraints(permissions);
        return permissions;
    }

    private void correctConstraints(List<Permission> permissions) throws UnsupportedFunctionException, OperandException, EvaluationException, OperatorException {
        permissions.stream().forEach(permission -> {
            permission.getActions().stream().forEach(action -> {
                List<Constraint> constraints = action.getConstraints();
                Map<String, Constraint> constraintsMap = Maps.newHashMap();

                for (Constraint constraint : constraints) {
                    Map<String, StringBuilder> rightNodes = Maps.newHashMap();
                    Map<String, OperandFunction> operandFunctionMap = Maps.newHashMap();
                    if (constraint.getFunction().equals("odrl:or")) {
                        OrOperandFunction operatorNode = (OrOperandFunction) constraint.getOperatorNode();
                        operatorNode.getOperands().forEach(operandFunction -> {
                            if (operandFunction.getFunction().equals("odrl:isAnyOf")) {
                                if (rightNodes.containsKey(operandFunction.getFunctionId())){
                                    StringBuilder stringBuilder = rightNodes.get(operandFunction.getFunctionId());
                                    stringBuilder.append(",").append(getValue(operandFunction));
                                }else {
                                    rightNodes.put(operandFunction.getFunctionId(),new StringBuilder(getValue(operandFunction)));
                                }

                            }
                        });
                        for (OperandFunction operandFunction : new ArrayList<>(operatorNode.getOperands())) {
                            if (operandFunction.getFunction().equals("odrl:isAnyOf")) {
                                operandFunctionMap.put(operandFunction.getFunctionId(), operandFunction);
                                operatorNode.getOperands().remove(operandFunction);
                            }
                        }
                        operandFunctionMap.forEach((key, value) -> {
                            OperandValue iOperand = (OperandValue) value.getArguments().get(1);
                            iOperand.setValue(rightNodes.get(key).toString());
                            operatorNode.getOperands().add(value);
                        });
                    }
                }
            });
        });
    }

    private String getValue(OperandFunction operandFunction) {
        IOperand iOperand = operandFunction.getArguments().get(1);
        if (iOperand instanceof OperandValue) {
            OperandValue operandValue = (OperandValue) iOperand;
            if (operandValue.getType().endsWith("string")) {
                return "'"+operandValue.getValue()+"'";
            } else {
                return operandValue.getValue();
           }
        }
        return null;
    }

    private List<Permission> queryInConstraint(Model model) throws UnsupportedFunctionException, OperandException, EvaluationException, OperatorException {
        List<Permission> permissions = Lists.newArrayList();
        ResultSet rs = QueryExecutionFactory.create(QueryFactory.create(PERMISSION_INCLUDE_IN), model).execSelect();
        Map<String, OrOperandFunction> orConstraints = Maps.newHashMap();
        AtomicReference<Boolean> isHasIn = new AtomicReference<>(false);
        Set<Permission> inPermissions = new HashSet<>();
        while (rs.hasNext()) {
            QuerySolution qs = rs.next();
            Permission permission = mapToPermission(model, qs, orConstraints);
            if (permissions.contains(permission)) {
                int oldIndex = permissions.indexOf(permission);
                Permission permissionOld = permissions.remove(oldIndex);
                permissionOld.getActions().forEach(actionOld -> permission.addAction(actionOld));
            }
            permission.getActions().forEach(action -> {
                if (action.getConstraints().isEmpty())
                    return;
                for (Constraint constraint : action.getConstraints()) {
                    if (constraint.getFunction().equals("odrl:isAnyOf")){
                        isHasIn.set(true);
                        inPermissions.add(permission);
                    }
                }
            });
            permissions.add(permission);
        }

        return permissions;
    }


    private Model toRDFModel(JsonObject policy) {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefixes(prefixes);
        model.read(new ByteArrayInputStream(policy.toString().getBytes()), null, "JSONLD11");
        return model;
    }

    private Permission mapToPermission(Model model, QuerySolution qs, Map<String, OrOperandFunction> orConstraints) throws OperandException, UnsupportedFunctionException, OperatorException, EvaluationException {
        String target = qs.get("?target").toString();
        String actionStr = qs.get("?action").toString();
        RDFNode leftNode = qs.get("?left");
        RDFNode rightNode = qs.get("?right");
        RDFNode opNode = qs.get("?op");
        RDFNode parentConstraint = qs.get("?constraint");
        RDFNode childConstraint = qs.get("?childConstraint");
        RDFNode childLeftNode = qs.get("?childLeft");
        RDFNode childRightNode = qs.get("?childRight");
        RDFNode childOpNode = qs.get("?childOp");

        Permission permission = new Permission(target);
        Action action = new Action(actionStr);
        if (opNode.toString().equals("http://www.w3.org/ns/odrl/2/or")) {
            // TODO: or constraint
            // 将constraint_chidren里的left和op和right提取出来，使用jena
            IOperand left = OperandFactory.createOperand(model, childLeftNode, functions);
            IOperand right = OperandFactory.createOperand(model, childRightNode, functions);
            OperandFunction operator = OperandFactory.createOperandFunction(model, childOpNode, functions,childConstraint.toString());
            operator.getArguments().add(left);
            operator.getArguments().add(right);
            OrOperandFunction operandFunction = new OrOperandFunction(shortenURI(model, opNode.asResource().getURI()), new ArrayList<>() {
                {
                    add(operator);
                }
            }, false, parentConstraint.toString());
            if (orConstraints.containsKey(parentConstraint.toString())) {
                orConstraints.get(parentConstraint.toString()).getOperands().add(operator);
            } else {
                Constraint constraint = new Constraint(operandFunction,parentConstraint.toString());
                orConstraints.put(parentConstraint.toString(), operandFunction);
                action.addConstraint(constraint);
            }
        } else {
            IOperand left = OperandFactory.createOperand(model, leftNode, functions);
            IOperand right = OperandFactory.createOperand(model, rightNode, functions);
            OperandFunction operator = OperandFactory.createOperandFunction(model, opNode, functions);
            operator.getArguments().add(left);
            operator.getArguments().add(right);
            Constraint constraint = new Constraint(operator,parentConstraint.toString());
            action.addConstraint(constraint);
        }
        permission.addAction(action);
        return permission;
    }

    public static List<RDFNode[]> constraints2(RDFNode restriction) throws EvaluationException {
        List<RDFNode[]> restrictions = new ArrayList<>();
        StmtIterator stmtIterator = ((ResourceImpl) restriction).listProperties();
        while (stmtIterator.hasNext()) {
            Statement next = stmtIterator.next();
            RDFNode object = next.getObject();
            System.out.println(object);
            restrictions.add(new RDFNode[]{object});
        }
        return restrictions;
    }


    public static List<RDFNode[]> constraints(Model model, RDFNode restriction) throws EvaluationException {
        List<RDFNode[]> restrictions = new ArrayList<>();
        // Instantiate CONSTRAINTS QUEY
        // 查询出restriction里的RDFNode节点
        if (!restriction.isResource())
            throw new EvaluationException("Provided policy seems to be malformed since it lacks of correly expressed constraints");
        String constraintsQueryInstantiated = CONSTRAINTS_QUERY_STRING.replace(QUERY_REPLACEMENT, restriction.asResource().toString());

        Query constraintsQuery = QueryFactory.create(constraintsQueryInstantiated);
        QueryExecution qe = QueryExecutionFactory.create(constraintsQuery, model);
        ResultSet rs = qe.execSelect();
        // Gather constraints
        while (rs.hasNext()) {
            QuerySolution querySolution = rs.nextSolution();
            RDFNode operator = querySolution.get(RESTRICTIONS_QUERY_ARG_OPERATOR);
            RDFNode leftOperand = querySolution.get(RESTRICTIONS_QUERY_ARG_LEFTOPERAND);
            RDFNode rightOperand = querySolution.get(RESTRICTIONS_QUERY_ARG_RIGHTOPERAND);

            restrictions.add(new RDFNode[]{operator, leftOperand, rightOperand});
        }
        qe.close();
        if (restrictions.isEmpty())
            throw new EvaluationException("Provided policy seems to be malformed since it lacks of correly expressed constraints");
        return restrictions;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        OdrlLib.debug = debug;
    }

    public void registerNative() {
        registerPrefix("odrl", "http://www.w3.org/ns/odrl/2/");
        try {
            // Operators
            register("odrl", new OdrlEq());
            register("odrl", new OdrlNeq());
            register("odrl", new OdrlIn());
            register("odrl", new OdrlGt());
            register("odrl", new OdrlGteq());
            register("odrl", new OdrlLt());
            register("odrl", new OdrlLteq());
            register("odrl", new OdrlIsAnyOf());
            register("odrl", new OdrlOr());
            // Operands
            register("odrl", new DateTime());
            register("odrl", new Spatial());
        } catch (Exception e) {
            // skip
        }
    }

    public void registerGeof() {
        registerPrefix("geof", OdrlLib.GEOF);
        try {
            register("geof:sfContains");
            register("geof:boundary");
            register("geof:buffer");
            register("geof:ehContains");
            register("geof:sfContains");
            register("geof:convexHull");
            register("geof:ehCoveredBy");
            register("geof:ehCovers");
            register("geof:sfCrosses");
            register("geof:difference");
            register("geof:rcc8dc");
            register("geof:ehDisjoint");
            register("geof:sfDisjoint");
            register("geof:distance");
            register("geof:envelope");
            register("geof:ehEquals");
            register("geof:rcc8eq");
            register("geof:sfEquals");
            register("geof:rcc8ec");
            register("geof:getSRID");
            register("geof:ehInside");
            register("geof:intersection");
            register("geof:sfIntersects");
            register("geof:ehMeet");
            register("geof:rcc8ntpp");
            register("geof:rcc8ntppi");
            register("geof:ehOverlap");
            register("geof:sfOverlaps");
            register("geof:rcc8po");
            register("geof:relate");
            register("geof:symDifference");
            register("geof:rcc8tpp");
            register("geof:rcc8tppi");
            register("geof:sfTouches");
            register("geof:union");
            register("geof:sfWithin");
            register("geof:asGeoJSON");
        } catch (Exception e) {

        }
    }

    protected static final String GEOF = "http://www.opengis.net/def/function/geosparql/";
    protected static final String SPATIALF = "http://jena.apache.org/function/spatial#";
    protected static final String SPATIAL = "http://jena.apache.org/spatial#";
    protected static final String UNITS = "http://www.opengis.net/def/uom/OGC/1.0/";
    protected static final String xsd = "http://www.w3.org/2001/XMLSchema#";
//	protected static final String xsd = "http://www.w3.org/2001/XMLSchema#";

    public void registerSpatial() {
        registerPrefix("spatialF", SPATIALF);
        registerPrefix("spatial", SPATIAL);
        registerPrefix("units", UNITS);
        registerPrefix("xsd", xsd);
        registerPrefix("geosp", "http://www.opengis.net/ont/geosparql#");
        try {
            register("spatialF:convertLatLon");
            register("spatialF:convertLatLonBox");
            register("spatialF:equals");
            register("spatialF:nearby");
            register("spatialF:withinCircle");
            register("spatialF:angle");
            register("spatialF:angleDeg");
            register("spatialF:distance");
            register("spatialF:azimuth");
            register("spatialF:azimuthDeg");
            register("spatialF:greatCircle");
            register("spatialF:greatCircleGeom");
            register("spatialF:transform");
            register("spatialF:transformDatatype");
            register("spatialF:transformSRS");

            register("spatial:intersectBox");
            register("spatial:intersectBoxGeom");
            register("spatial:withinBox");
            register("spatial:withinBoxGeom");
            register("spatial:nearby");
            register("spatial:nearbyGeom");
            register("spatial:withinCircle");
            register("spatial:withinCircleGeom");
            register("spatial:north");
            register("spatial:northGeom");
            register("spatial:south");
            register("spatial:southGeom");
            register("spatial:east");
            register("spatial:eastGeom");
            register("spatial:west");
            register("spatial:westGeom");

        } catch (Exception e) {

        }
    }

}
