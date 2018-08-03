package org.kie.scenarioplayground.scenario.runner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.drools.core.command.impl.ExecutableCommand;
import org.drools.core.command.runtime.rule.GetObjectsCommand;
import org.drools.core.fluent.impl.BaseBatchFluent;
import org.drools.core.fluent.impl.ExecutableImpl;
import org.drools.core.fluent.impl.KieContainerFluentImpl;
import org.drools.core.fluent.impl.KieSessionFluentImpl;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.ExecutableRunner;
import org.kie.api.runtime.RequestContext;
import org.kie.api.runtime.builder.ExecutableBuilder;
import org.kie.api.runtime.builder.KieSessionFluent;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.scenarioplayground.scenario.command.AssertConditionCommand;
import org.kie.scenarioplayground.scenario.command.GetKieContainerCommand;
import org.kie.scenarioplayground.scenario.command.NewKieSessionCommand;
import org.kie.scenarioplayground.scenario.model.Expression;
import org.kie.scenarioplayground.scenario.model.FactMapping;
import org.kie.scenarioplayground.scenario.model.FactMappingType;
import org.kie.scenarioplayground.scenario.model.FactMappingValue;
import org.kie.scenarioplayground.scenario.model.Scenario;
import org.kie.scenarioplayground.scenario.model.Simulation;
import org.kie.scenarioplayground.scenario.model.SimulationDescriptor;

import static java.util.stream.Collectors.toList;

public class SimulationRunner implements ScenarioRunner<List<Map<String, Boolean>>> {

    private final ReleaseId releaseId;
    private ExecutableBuilder executableBuilder;

    public SimulationRunner(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    public SimulationRunner() {
        this.releaseId = null;
    }

    // TODO extend to support single scenario run and/or a publish/subscribe mechanism to expose hooks
    @Override
    public List<Map<String, Boolean>> accept(Simulation simulation) {

        List<Map<String, Boolean>> toReturn = new ArrayList<>();

        SimulationDescriptor simulationDescriptor = simulation.getSimulationDescriptor();

        Map<String, List<Object>> given = new HashMap<>();

        // TODO refactor to a single container object
        Map<FactMapping, Map<String, Object>> expected = new HashMap<>();
        Map<FactMapping, Map<String, FactMappingValue.Operator>> expectedOperator = new HashMap<>();

        for (Scenario scenario : simulation.getScenarios()) {

            final KieSessionFluent kieSessionFluent = create();

            for (String factName : scenario.getFactNames()) {

                Map<Expression, Object> params = new HashMap<>();
                Map<Expression, FactMappingValue.Operator> operators = new HashMap<>();
                FactMapping factMapping = simulationDescriptor.getFactMappingsByName(factName);

                for (FactMappingValue factMappingValue : scenario.getFactMappingValuesByFactName(factName)) {

                    Expression expressionsByName = factMapping.getExpressionsByExpressionIdentifier(factMappingValue.getExpressionIdentifier());

                    final Function<Object, ?> converter = expressionsByName.getConverter();

                    params.put(expressionsByName, converter.apply(factMappingValue.getRawValue()));
                    operators.put(expressionsByName, factMappingValue.getOperator());
                }

                Map<String, Object> paramsGiven = filterByType(params, FactMappingType.GIVEN);
                Map<String, Object> paramsExpected = filterByType(params, FactMappingType.EXPECTED);
                Map<String, FactMappingValue.Operator> operatorsExpected = filterByType(operators, FactMappingType.EXPECTED);

                if(!paramsGiven.isEmpty()) {
                    try {
                        Object instanceToInsert = fillBean(factMapping.getClazz(), paramsGiven);
                        given.computeIfAbsent(factName, k -> new ArrayList<>()).add(instanceToInsert);
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalArgumentException("Impossible to populate bean '" + factMapping.getClazz().getCanonicalName() + "'");
                    }
                }
                if(!paramsExpected.isEmpty()) {
                    expected.put(factMapping, paramsExpected);
                    expectedOperator.put(factMapping, operatorsExpected);
                }

            }

            for (Object o : given.values().stream().flatMap(Collection::stream).collect(toList())) {
                kieSessionFluent.insert(o);
            }

            kieSessionFluent.fireAllRules()
                    .getGlobal("outS")
                    .out("outS");

            for (Map.Entry<FactMapping, Map<String, Object>> factMappingMapEntry : expected.entrySet()) {
                // TODO merge in a single step/command
                addGenericCommand((BaseBatchFluent<?, ?>) kieSessionFluent, new GetObjectsCommand(new ClassObjectFilter(factMappingMapEntry.getKey().getClazz())));
                addGenericCommand((BaseBatchFluent<?, ?>) kieSessionFluent, new AssertConditionCommand(factMappingMapEntry.getKey().getFactName(), factMappingMapEntry.getValue(), expectedOperator.get(factMappingMapEntry.getKey())));
            }

            kieSessionFluent.dispose();

            final RequestContext run = run();

            final Map<String, Boolean> expectedResults = (Map<String, Boolean>) run.getOutputs().get(Simulation.RESULT_MAP);
            toReturn.add(expectedResults);
        }

        return toReturn;
    }

    private <X> Map<String, X> filterByType(Map<Expression, X> input, FactMappingType filter) {
        return input.entrySet().stream()
                .filter(e -> filter.equals(e.getKey().getExpressionIdentifier().getType()))
                .collect(Collectors.toMap(e -> e.getKey().getFullExpression(), Map.Entry::getValue));
    }

    private <T> T fillBean(Class<T> clazz, Map<String, Object> params) throws ReflectiveOperationException {
        final T newInstance = clazz.newInstance();
        // TODO evaluate if implement an alternative to BeanUtils
        BeanUtils.populate(newInstance, params);
        return newInstance;
    }

    private KieSessionFluent create() {
        executableBuilder = ExecutableBuilder.create();

        final ExecutableBuilder executableBuilder = this.executableBuilder.newApplicationContext("app1");

        final KieContainerFluentImpl kieContainerFluent = addGenericCommand((BaseBatchFluent<?, ?>) executableBuilder, new GetKieContainerCommand(releaseId), KieContainerFluentImpl::new);
        final NewKieSessionCommand newKieSessionCommand = new NewKieSessionCommand(null);
        newKieSessionCommand.setClockTypeOption(ClockTypeOption.get("pseudo"));
        return addGenericCommand(kieContainerFluent, newKieSessionCommand, KieSessionFluentImpl::new);
    }

    private RequestContext run() {
        return ExecutableRunner.create().execute(executableBuilder.getExecutable());
    }

    // TODO extend KieSessionFluent to support additional commands and then remove this hack
    private <T> T addGenericCommand(BaseBatchFluent<?, ?> batchFluent, ExecutableCommand<?> t, Function<ExecutableImpl, T> additionalBehavior) {

        try {
            final Field fluentCtxField = resolveClass(batchFluent.getClass()).getDeclaredField("fluentCtx");
            fluentCtxField.setAccessible(true);
            ExecutableImpl fluentCtx = (ExecutableImpl) fluentCtxField.get(batchFluent);
            fluentCtx.addCommand(t);
            return additionalBehavior.apply(fluentCtx);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Impossible to retrieve fluentCtx from '" + batchFluent.getClass().getCanonicalName() + "'");
        }
    }

    private void addGenericCommand(BaseBatchFluent<?, ?> batchFluent, ExecutableCommand<?> t) {
        addGenericCommand(batchFluent, t, k -> null);
    }

    private Class<BaseBatchFluent<?, ?>> resolveClass(Class<?> clazz) {
        if(BaseBatchFluent.class.equals(clazz)) {
            return (Class<BaseBatchFluent<?, ?>>) clazz;
        }
        if(clazz == null) {
            throw new IllegalArgumentException();
        }
        return resolveClass(clazz.getSuperclass());
    }
}
