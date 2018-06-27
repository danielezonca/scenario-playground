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

    public List<FactMapping> getFactMappingByType(FactMappingType type) {
        return getAllFactMappings().stream().filter(fm -> fm.getType().equals(type)).collect(toList());
    }
    
    public FactMapping getFactMappingsByName(String factName) {
        return factMappings.get(factName);
    }

    public FactMapping addGivenObject(String factName, Class<?> clazz) {
        return addGenericObject(FactMappingType.GIVEN, factName, clazz);
    }

    public FactMapping addExpectedObject(String factName, Class<?> clazz) {
        return addGenericObject(FactMappingType.EXPECTED, factName, clazz);
    }

    public FactMapping addGenericObject(FactMappingType type, String factName, Class<?> clazz) {
        FactMapping column = new FactMapping(factName, type, clazz);
        if(factMappings.containsKey(factName)) {
            throw new IllegalArgumentException("Duplicated fact name, name '" + factName + "' already exists");
        }
        factMappings.put(factName, column);
        return column;
    }

}
