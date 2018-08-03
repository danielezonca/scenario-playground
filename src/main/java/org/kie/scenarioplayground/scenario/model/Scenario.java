package org.kie.scenarioplayground.scenario.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class Scenario {

    private final String description;
    protected final Map<String, List<FactMappingValue>> factMappingValues = new LinkedHashMap<>();

    public Scenario(String description) {
        this.description = description;
    }

    public List<FactMappingValue> getFactMappingValues() {
        return factMappingValues.entrySet().stream().flatMap(e -> e.getValue().stream()).collect(toList());
    }

    public void addMappingValue(FactMappingValue factMappingValue) {
        factMappingValues.computeIfAbsent(factMappingValue.getFactName(), k -> new ArrayList<>()).add(factMappingValue);
    }

    public List<FactMappingValue> getFactMappingValuesByFactName(String factName) {
        return factMappingValues.get(factName);
    }

    public String getDescription() {
        return description;
    }

    public Collection<String> getFactNames() {
        return factMappingValues.keySet();
    }
}