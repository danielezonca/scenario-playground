package org.kie.scenarioplayground.scenario.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Simulation {

    private final SimulationDescriptor simulationDescriptor = new SimulationDescriptor();
    private final List<Scenario> scenarios = new LinkedList<>();

    public List<Scenario> getScenarios() {
        return Collections.unmodifiableList(scenarios);
    }

    public SimulationDescriptor getSimulationDescriptor() {
        return simulationDescriptor;
    }

    public Scenario getScenarioByIndex(int index) {
        return scenarios.get(index);
    }

    public Scenario addScenario(String name) {
        Scenario scenario = new Scenario(name);
        scenarios.add(scenario);
        return scenario;
    }

}