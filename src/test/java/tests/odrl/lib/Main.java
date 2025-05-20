package tests.odrl.lib;

import odrl.lib.model.OdrlLib;
import odrl.lib.model.exceptions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws UnsupportedFunctionException, OperandException, EvaluationException, OdrlRegistrationException, OperatorException, IllegalAccessException {
        OdrlLib odrl = new OdrlLib();
        String policy = readPolicy("data/policy.json");
        System.out.println(odrl.solveResultToJson(policy, new HashMap<>()));
    }
    public static String readPolicy(String name) {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(name), StandardCharsets.UTF_8)) {

            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }
}
