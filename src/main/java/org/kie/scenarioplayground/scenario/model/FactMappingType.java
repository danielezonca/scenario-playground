package org.kie.scenarioplayground.scenario.model;

import java.util.Optional;

public enum FactMappingType {
    given, expected {
        @Override
        String getAlias() {
            return "when";
        }
    };

    public static Optional<FactMappingType> fromString(String toMatch) {
        for (FactMappingType mappingElementType : values()) {
            if(mappingElementType.getAlias().equalsIgnoreCase(toMatch.trim())) {
                return Optional.of(mappingElementType);
            }
        }
        return Optional.empty();
    }

    // TODO define a better mechanism for "alias"
    String getAlias() {
        return name();
    }
}
