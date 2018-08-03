package org.kie.scenarioplayground.scenario.model;

public class ExpressionIdentifier {

    private final String name;
    private final FactMappingType type;

    public ExpressionIdentifier(String name, FactMappingType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public FactMappingType getType() {
        return type;
    }

    public static ExpressionIdentifier identifier(String name, FactMappingType type) {
        return new ExpressionIdentifier(name, type);
    }
}
