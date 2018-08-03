package org.kie.scenarioplayground.scenario.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class SimulationDescriptor {

    private final Map<String, FactMapping> factMappings = new LinkedHashMap<>();

    public List<FactMapping> getAllFactMappings() {
        return factMappings.entrySet().stream().map(Map.Entry::getValue).collect(toList());
    }
    
    public FactMapping getFactMappingsByName(String factName) {
        return factMappings.get(factName);
    }

    public FactMapping addGenericObject(String factName, Class<?> clazz) {
        FactMapping column = new FactMapping(factName, clazz);
        if(factMappings.containsKey(factName)) {
            throw new IllegalArgumentException("Duplicated fact name, name '" + factName + "' already exists");
        }
        factMappings.put(factName, column);
        return column;
    }

}
