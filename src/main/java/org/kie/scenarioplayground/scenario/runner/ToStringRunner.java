package org.kie.scenarioplayground.scenario.runner;

import java.util.List;

import org.kie.scenarioplayground.scenario.model.FactMapping;
import org.kie.scenarioplayground.scenario.model.Simulation;
import org.kie.scenarioplayground.scenario.model.SimulationDescriptor;

import static java.util.stream.Collectors.toList;

public class ToStringRunner implements ScenarioRunner<String> {

    @Override
    public String accept(Simulation simulation) {
        StringBuilder result = new StringBuilder();
        SimulationDescriptor simulationDescriptor = simulation.getSimulationDescriptor();


        List<FactMapping> allFactMappings = simulationDescriptor.getFactMappings();
        List<String> descriptors = allFactMappings.stream().map(
                        ex -> ex.getExpressionIdentifier().getType().name() + " " + ex.getFactIdentifier().getName() +
                                " " + ex.getExpressionIdentifier().getName() + "=" + ex.getClazz().getSimpleName() + "." +
                                ex.getFullExpression()).collect(toList());

        result.append("------- SIMULATION DESCRIPTOR -------\n");
        for (String descriptor : descriptors) {
            result.append(descriptor);
            result.append("\n");
        }

        result.append("\n------- SCENARIO -------\n");
        List<String> scenarios = simulation.getScenarios().stream().flatMap(scenario -> scenario.getFactMappingValues().stream().map(e -> e.getFactName() +
                " " + e.getExpressionIdentifier().getName() + "=" + e.getRawValue())).collect(toList());

        for (String scenario : scenarios) {
            result.append(scenario);
            result.append("\n");
        }

        return result.toString();

    }
}
