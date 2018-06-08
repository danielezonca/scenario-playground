package org.kie.scenarioplayground.scenario.command;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.drools.core.command.RequestContextImpl;
import org.drools.core.command.impl.ExecutableCommand;
import org.kie.api.runtime.Context;
import org.kie.scenarioplayground.scenario.model.FactMappingValue;
import org.kie.scenarioplayground.scenario.model.Simulation;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class AssertConditionCommand implements ExecutableCommand<Boolean> {

    private String factName;
    private Map<String, Object> expectedValues;
    private Map<String, FactMappingValue.Operator> operatorMap;
    private Map<String, Boolean> matched;

    public AssertConditionCommand(String factName, Map<String, Object> expectedValues, Map<String, FactMappingValue.Operator> operatorMap) {
        this.factName = factName;
        this.expectedValues = expectedValues;
        this.operatorMap = operatorMap;
        matched = expectedValues.keySet().stream().collect(toMap(identity(), a -> false));
    }

    @Override
    public Boolean execute(Context context) {
        RequestContextImpl reqContext = (RequestContextImpl)context;
        @SuppressWarnings("unchecked")
        final Collection<Object> result = (Collection<Object>) reqContext.getResult();
        for (Map.Entry<String, FactMappingValue.Operator> operator : operatorMap.entrySet()) {
            final String propertyPath = operator.getKey();
            for (Object o : result) {
                try {
                    // TODO evaluate if implement an alternative to PropertyUtils
                    Object resultValue = PropertyUtils.getProperty(o, propertyPath);
                    Object expectedValue = expectedValues.get(propertyPath);
                    FactMappingValue.Operator op = operator.getValue();
                    matched.compute(propertyPath, (k, v) -> v || op.evaluate(resultValue, expectedValue));
                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException("Cannot retrieve propertyPath '" + propertyPath + "' from '" + o.getClass().getCanonicalName() + "'");
                }
            }
        }

        final boolean factVerified = matched.values().stream().allMatch(Boolean::booleanValue);

        // TODO improve outputs method (getOutput(key), contains?)
        final Map<String, Object> actualResult = (Map<String, Object>) reqContext.getOutputs().get(Simulation.RESULT_MAP);
        if(actualResult == null) {
            reqContext.setOutput(Simulation.RESULT_MAP, new HashMap<>());
        }

        Map<String, Object> resultMap = (Map<String, Object>) reqContext.getOutputs().get(Simulation.RESULT_MAP);

        // TODO extend to support detailed results
        resultMap.put(factName, factVerified);

        return factVerified;
    }
}
