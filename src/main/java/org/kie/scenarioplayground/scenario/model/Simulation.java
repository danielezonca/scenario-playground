package org.kie.scenarioplayground.scenario.model;

import java.util.LinkedList;
import java.util.List;

public class Simulation {

    // TODO find a better way to extract assertions results
    public static String RESULT_MAP = "RESULT_MAP";

    private final SimulationDescriptor simulationDescriptor = new SimulationDescriptor();
    private final List<Scenario> scenarios = new LinkedList<>();

    // TODO add support to preconditions shared between scenarios (aka Background)

    public List<Scenario> getScenarios() {
        return scenarios;
    }

    public SimulationDescriptor getSimulationDescriptor() {
        return simulationDescriptor;
    }

    public Scenario addScenario(String name) {
        Scenario scenario = new Scenario(name);
        scenarios.add(scenario);
        return scenario;
    }

}
