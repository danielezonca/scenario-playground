package org.kie.scenarioplayground.scenario.model;

import java.util.LinkedList;
import java.util.List;

public class Simulation {

    // TODO find a better way to extract assertions results
    public static String RESULT_MAP = "RESULT_MAP";
    public static String SCENARIO_EXTENSION = "scenario.xml";

    private final SimulationDescriptor simulationDescriptor = new SimulationDescriptor();
    private final List<Scenario> scenarios = new LinkedList<>();
    private final List<Precondition> preconditions = new LinkedList<>();

    public List<Scenario> getScenarios() {
        return scenarios;
    }

    public List<Precondition> getPreconditions() {
        return preconditions;
    }

    public SimulationDescriptor getSimulationDescriptor() {
        return simulationDescriptor;
    }

    public Scenario addScenario(String name) {
        Scenario scenario = new Scenario(name);
        scenarios.add(scenario);
        return scenario;
    }

    public Precondition addPrecondition(String name) {
        Precondition precondition = new Precondition(name, simulationDescriptor);
        preconditions.add(precondition);
        return precondition;
    }

}
