package org.kie.scenarioplayground.scenario.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class Precondition extends Scenario {

    private final SimulationDescriptor simulationDescriptor;

    public Precondition(String description, SimulationDescriptor simulationDescriptor) {
        super(description);
        this.simulationDescriptor = simulationDescriptor;
    }

    @Override
    public void addMappingValue(FactMappingValue factMappingValue) {
        final String factName = factMappingValue.getFactName();
        final FactMapping factMappingsByName = simulationDescriptor.getFactMappingsByName(factName);
        final FactMappingType type = factMappingsByName.getType();

        if(!FactMappingType.GIVEN.equals(type)) {
            throw new IllegalArgumentException("Precondition must map facts of type 'given'");
        }
        factMappingValues.computeIfAbsent(factMappingValue.getFactName(), k -> new ArrayList<>()).add(factMappingValue);
    }
}