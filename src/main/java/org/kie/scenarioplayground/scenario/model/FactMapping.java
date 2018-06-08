package org.kie.scenarioplayground.scenario.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class FactMapping {

    private final Map<String, Expression> expressions = new LinkedHashMap<>();
    private final String factName;
    private final FactMappingType type;
    private final Class<?> clazz;

    public FactMapping(String factName, FactMappingType type, Class<?> clazz) {
        this.factName = factName;
        this.type = type;
        this.clazz = clazz;
    }

    public FactMappingType getType() {
        return type;
    }

    public String getFactName() {
        return factName;
    }

    public Expression addExpression(String bindingName) {
        Expression expression = new Expression(bindingName, clazz);
        if(expressions.containsKey(bindingName)) {
            throw new IllegalArgumentException("Duplicated binding, name '" + bindingName + "' already exists");
        }
        expressions.put(bindingName, expression);
        return expression;
    }

    public List<Expression> getAllExpressions() {
        return expressions.entrySet().stream().map(Map.Entry::getValue).collect(toList());
    }

    public Expression getExpressionsByName(String bindingName) {
        return expressions.get(bindingName);
    }

    public Class<?> getClazz() {
        return clazz;
    }

}
