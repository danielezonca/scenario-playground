package org.kie.scenarioplayground.cucumber;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.kie.scenarioplayground.scenario.model.FactMappingType;

public class CucumberFactMappingTypeFactory {

    static Map<String, FactMappingType> mapping = new HashMap<>();

    static {
        mapping.put("given", FactMappingType.GIVEN);
        mapping.put("expected", FactMappingType.EXPECTED);
        mapping.put("when", FactMappingType.EXPECTED);
    }

    public static Optional<FactMappingType> stringToFactMappingType(final String toMatch) {
        return Optional.ofNullable(toMatch)
                .map(value -> mapping.get(value.toLowerCase().trim()));
    }
}
