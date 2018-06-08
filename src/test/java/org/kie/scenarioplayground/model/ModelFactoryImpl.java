package org.kie.scenarioplayground.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.scenarioplayground.cucumber.ModelFactory;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class ModelFactoryImpl implements ModelFactory {

    Map<String, Class<?>> stringToClass = new HashMap<>();
    {
        stringToClass.put("Case", Case.class);
        stringToClass.put("Case Detail", CaseDetail.class);
        stringToClass.put("Detail Provided", DetailProvided.class);
        stringToClass.put("Next Detail", NextDetail.class);
        stringToClass.put("Product", Product.class);
    }

    private final static ModelFactoryImpl instance = new ModelFactoryImpl();

    public static ModelFactoryImpl get() {
        return instance;
    }

    @Override
    public Class<?> getInstance(String toMatch) {
        List<Map.Entry<String, Class<?>>> matchList = stringToClass.entrySet().stream().filter(e -> toMatch.matches(".*" + e.getKey() + ":$")).collect(toList());
        if(matchList.size() > 1) {
            throw new IllegalStateException("Too many matches for '" + toMatch + "': " + matchList.stream().map(Map.Entry::getKey).collect(joining(",")));
        }
        return matchList.stream().findFirst().map(Map.Entry::getValue).orElseThrow(() -> new IllegalArgumentException("Impossible to find " + toMatch));
    }
}
