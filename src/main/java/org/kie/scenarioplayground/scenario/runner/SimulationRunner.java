package org.kie.scenarioplayground.scenario.runner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.beanutils.BeanUtils;
import org.drools.core.command.impl.ExecutableCommand;
import org.drools.core.command.runtime.rule.GetObjectsCommand;
import org.drools.core.fluent.impl.ExecutableImpl;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.ExecutableRunner;
import org.kie.api.runtime.RequestContext;
import org.kie.api.runtime.builder.ExecutableBuilder;
import org.kie.api.runtime.builder.KieSessionFluent;
import org.kie.scenarioplayground.scenario.command.AssertConditionCommand;
import org.kie.scenarioplayground.scenario.model.Expression;
import org.kie.scenarioplayground.scenario.model.FactMapping;
import org.kie.scenarioplayground.scenario.model.FactMappingType;
import org.kie.scenarioplayground.scenario.model.FactMappingValue;
import org.kie.scenarioplayground.scenario.model.Scenario;
import org.kie.scenarioplayground.scenario.model.Simulation;
import org.kie.scenarioplayground.scenario.model.SimulationDescriptor;

import static java.util.stream.Collectors.*;

public class SimulationRunner implements ScenarioRunner<List<Map<String, Boolean>>> {

    private final ReleaseId releaseId;
    private ExecutableBuilder executableBuilder;

    public SimulationRunner(ReleaseId releaseId) {
        this.releaseId = releaseId;
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

                Map<String, Object> params = new HashMap<>();
                Map<String, FactMappingValue.Operator> operators = new HashMap<>();
                FactMapping factMapping = simulationDescriptor.getFactMappingsByName(factName);

                for (FactMappingValue factMappingValue : scenario.getFactMappingValuesByFactName(factName)) {

                    Expression expressionsByName = factMapping.getExpressionsByName(factMappingValue.getBindingName());

                    final Function<Object, ?> converter = expressionsByName.getConverter();

                    params.put(expressionsByName.getFullExpression(), converter.apply(factMappingValue.getRawValue()));
                    operators.put(expressionsByName.getFullExpression(), factMappingValue.getOperator());
                }

                if (FactMappingType.given.equals(factMapping.getType())) {
                    try {
                        Object instanceToInsert = fillBean(factMapping.getClazz(), params);
                        given.computeIfAbsent(factName, k -> new ArrayList<>()).add(instanceToInsert);
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalArgumentException("Impossible to populate bean '" + factMapping.getClazz().getCanonicalName() + "'");
                    }
                }
                else if(FactMappingType.expected.equals(factMapping.getType())) {
                    expected.put(factMapping, params);
                    expectedOperator.put(factMapping, operators);
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
                addGenericCommand(kieSessionFluent, new GetObjectsCommand(new ClassObjectFilter(factMappingMapEntry.getKey().getClazz())));
                addGenericCommand(kieSessionFluent, new AssertConditionCommand(factMappingMapEntry.getKey().getFactName(), factMappingMapEntry.getValue(), expectedOperator.get(factMappingMapEntry.getKey())));
            }

            kieSessionFluent.dispose();

            final RequestContext run = run();

            final Object outS = run.getOutputs().get("outS");

            final Map<String, Boolean> expectedResults = (Map<String, Boolean>) run.getOutputs().get(Simulation.RESULT_MAP);
            toReturn.add(expectedResults);
        }

        return toReturn;
    }

    private <T> T fillBean(Class<T> clazz, Map<String, Object> params) throws ReflectiveOperationException {
        final T newInstance = clazz.newInstance();
        // TODO evaluate if implement an alternative to BeanUtils
        BeanUtils.populate(newInstance, params);
        return newInstance;
    }

    private KieSessionFluent create() {
        executableBuilder = ExecutableBuilder.create();

        return executableBuilder.newApplicationContext("app1")
                .getKieContainer(releaseId).newSession();
    }

    private RequestContext run() {
        return ExecutableRunner.create().execute(executableBuilder.getExecutable());
    }

    // TODO extend KieSessionFluent to support additional commands and then remove this hack
    private void addGenericCommand(KieSessionFluent kieSessionFluent, ExecutableCommand<?> t) {

        try {
            final Field fluentCtxField = kieSessionFluent.getClass().getSuperclass().getDeclaredField("fluentCtx");
            fluentCtxField.setAccessible(true);
            ExecutableImpl fluentCtx = (ExecutableImpl) fluentCtxField.get(kieSessionFluent);
            fluentCtx.addCommand(t);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Impossible to retrieve fluentCtx from '" + kieSessionFluent.getClass().getCanonicalName() + "'");
        }
    }
}
