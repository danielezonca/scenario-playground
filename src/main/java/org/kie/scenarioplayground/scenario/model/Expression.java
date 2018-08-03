package org.kie.scenarioplayground.scenario.model;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Expression {

    private final List<ExpressionElement> expressionElements = new LinkedList<>();

    private final ExpressionIdentifier expressionIdentifier;

    private Class<?> clazz;

    public Expression(ExpressionIdentifier expressionIdentifier, Class<?> rootClass) {
        this.expressionIdentifier = expressionIdentifier;
        this.clazz = rootClass;
    }

    public String getFullExpression() {
        return expressionElements.stream().map(ExpressionElement::getStep).collect(Collectors.joining("."));
    }

    public List<ExpressionElement> getExpressionElements() {
        return expressionElements;
    }

    public void addExpressionElement(String stepName, Class<?> clazz) {
        this.clazz = clazz;
        expressionElements.add(new ExpressionElement(stepName));
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public ExpressionIdentifier getExpressionIdentifier() {
        return expressionIdentifier;
    }
}
