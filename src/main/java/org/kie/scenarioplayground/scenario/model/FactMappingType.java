package org.kie.scenarioplayground.scenario.model;

import java.util.Arrays;
import java.util.Optional;

public enum FactMappingType {
    GIVEN,
    EXPECTED("when");

    private String alias;

    FactMappingType() {
    }

    FactMappingType(final String alias) {
        this.alias = alias;
    }

    public static Optional<FactMappingType> fromString(final String toMatch) {
        return Arrays.stream(values())
                .filter(value -> enumNameOrAliasMatchesTheString(value, toMatch.trim()))
                .findFirst();
    }

    private static boolean enumNameOrAliasMatchesTheString(final FactMappingType enumValue, final String stringValue) {
        final boolean nameMatches = enumValue.name().equalsIgnoreCase(stringValue);
        final boolean aliasMatches = enumValue.alias != null && enumValue.alias.equalsIgnoreCase(stringValue);
        return nameMatches || aliasMatches;
    }
}
