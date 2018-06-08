package org.kie.scenarioplayground.scenario.runner;

import org.kie.scenarioplayground.scenario.model.Simulation;

public interface ScenarioRunner<T> {

    T accept(Simulation simulation);

}
