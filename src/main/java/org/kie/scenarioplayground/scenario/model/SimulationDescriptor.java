package org.kie.scenarioplayground.scenario.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class SimulationDescriptor {

    private final Map<String, FactMapping> factMappings = new HashMap<>();
    private final List<FactMapping> indexFactMappings = new ArrayList<>();

    public List<FactMapping> getAllFactMappings() {
        return Collections.unmodifiableList(indexFactMappings);
        // return factMappings.entrySet().stream().map(Map.Entry::getValue).collect(toList());
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
        return addGenericObject(indexFactMappings.size(), type, factName, clazz);
    }

    public FactMapping addGenericObject(int index, FactMappingType type, String factName, Class<?> clazz) {
        FactMapping column = new FactMapping(factName, type, clazz);
        if (factMappings.containsKey(factName)) {
            throw new IllegalArgumentException("Duplicated fact name, name '" + factName + "' already exists");
        }
        factMappings.put(factName, column);
        indexFactMappings.add(index, column);
        return column;
    }

    public void clear() {
        factMappings.clear();
        indexFactMappings.clear();
    }
}
