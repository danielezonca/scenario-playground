package org.kie.scenarioplayground.scenario.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SimulationDescriptor {

    private final List<FactMapping> factMappings = new ArrayList<>();

    public List<FactMapping> getFactMappings() {
        return Collections.unmodifiableList(factMappings);
    }

    public Set<FactIdentifier> getFactIdentifiers() {
        return factMappings.stream().map(FactMapping::getFactIdentifier).collect(Collectors.toSet());
    }
    
    public List<FactMapping> getFactMappingsByFactName(String factName) {
        return internalFilter(e -> e.getFactIdentifier().getName().equalsIgnoreCase(factName));
    }

    public FactMapping getFactMapping(ExpressionIdentifier ei) {
        return internalFilter(e -> e.getExpressionIdentifier().getName().equalsIgnoreCase(ei.getName()) &&
                e.getExpressionIdentifier().getType().equals(ei.getType()))
                .get(0);
    }

    public FactIdentifier newFactIdentifier(String factName, Class<?> clazz) {
        return new FactIdentifier(factName, clazz);
    }

    private List<FactMapping> internalFilter(Predicate<FactMapping> predicate) {
        return factMappings.stream().filter(predicate).collect(Collectors.toList());
    }

    public FactMapping addFactMapping(ExpressionIdentifier expressionIdentifier, FactIdentifier factIdentifier) {
        return addFactMapping(factMappings.size(), expressionIdentifier, factIdentifier);
    }

    public FactMapping addFactMapping(int index, ExpressionIdentifier expressionIdentifier, FactIdentifier factIdentifier) {
        FactMapping factMapping = new FactMapping(expressionIdentifier, factIdentifier);
        factMappings.add(index, factMapping);
        return factMapping;
    }

//
//    public FactMapping addGenericObject(String factName, Class<?> clazz) {
//        FactMapping column = new FactMapping(factName, clazz);
//        if(factMappings.containsKey(factName)) {
//            throw new IllegalArgumentException("Duplicated fact name, name '" + factName + "' already exists");
//        }
//        factMappings.put(factName, column);
//        return column;
//    }
//
//    public Expression addExpression(int index, String factName, Class<?> clazz, String bindingName, FactMappingType factMappingType) {
//
//        FactMapping factMapping = addGenericObject(factName, clazz);
//        factMapping.addExpression()
//    }
//
//    public Expression addExpression(int index, String factName) {
//
//    }
//
//    private FactMapping getOrCreateFactMapping(String factName, Class<?> clazz) {
//        Optional<FactMapping> factMappingsByName = Optional.ofNullable(getFactMappingsByFactName(factName));
//        return factMappingsByName.orElseGet(() -> addGenericObject(factName, clazz));
//    }

}
