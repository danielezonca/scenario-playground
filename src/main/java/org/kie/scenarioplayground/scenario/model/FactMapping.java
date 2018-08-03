package org.kie.scenarioplayground.scenario.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class FactMapping {

    private final Map<ExpressionIdentifier, Expression> expressions = new LinkedHashMap<>();
    private final String factName;
    private final Class<?> clazz;

    public FactMapping(String factName, Class<?> clazz) {
        this.factName = factName;
        this.clazz = clazz;
    }

    public String getFactName() {
        return factName;
    }

    public Expression addExpression(String bindingName, FactMappingType type) {
        ExpressionIdentifier expressionIdentifier = ExpressionIdentifier.identifier(bindingName, type);
        Expression expression = new Expression(expressionIdentifier, clazz);
        if(expressions.containsKey(expressionIdentifier)) {
            throw new IllegalArgumentException("Duplicated binding, name '" + bindingName + "' and type '" + type.name() + "' already exists");
        }
        expressions.put(expressionIdentifier, expression);
        return expression;
    }

    public List<Expression> getAllExpressions() {
        return expressions.entrySet().stream().map(Map.Entry::getValue).collect(toList());
    }

    public Expression getExpressionsByExpressionIdentifier(ExpressionIdentifier expressionIdentifier) {
        return expressions.get(expressionIdentifier);
    }

    public Class<?> getClazz() {
        return clazz;
    }

}
