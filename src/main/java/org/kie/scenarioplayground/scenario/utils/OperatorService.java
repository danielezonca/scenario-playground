package org.kie.scenarioplayground.scenario.utils;

import java.util.Objects;
import java.util.function.BiFunction;

import org.kie.scenarioplayground.scenario.model.FactMappingValueOperator;

public class OperatorService {

    public static Boolean evaluate(FactMappingValueOperator operator, Object resultValue, Object expectedValue) {
        switch(operator) {
            case EQUALS:
                return Objects.equals(resultValue, expectedValue);
            default:
                throw new UnsupportedOperationException("Operator " + operator.name() + " is not supported");
        }
    }

    private static BiFunction<Object, Object, Integer> defaultComparator = (a, b) -> {
        if (Comparable.class.isAssignableFrom(a.getClass()) && Comparable.class.isAssignableFrom(b.getClass())
                && a.getClass().equals(b.getClass())) {
            Comparable comparableA = (Comparable) a;
            return comparableA.compareTo(b);
        }
        throw new IllegalArgumentException("Object cannot be compared '" + a.getClass().getCanonicalName() + "'");
    };
}
