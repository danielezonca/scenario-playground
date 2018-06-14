package org.kie.scenarioplayground;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import gherkin.ast.Feature;
import gherkin.ast.Scenario;
import org.junit.Test;
import org.kie.scenarioplayground.cucumber.Utils;
import org.kie.scenarioplayground.model.ModelFactoryImpl;
import org.kie.scenarioplayground.scenario.model.Simulation;
import org.kie.scenarioplayground.scenario.runner.ScenarioRunner;
import org.kie.scenarioplayground.scenario.runner.SimulationRunner;

import static org.junit.Assert.assertTrue;

public class SimulationRunnerTest {

    ScenarioRunner<List<Map<String, Boolean>>> runner = new SimulationRunner();

    @Test
    public void testSimulationRunner() throws IOException {
        Feature feature = Utils.toFeature(Utils.readFeatureFileFromResource("testSimpleScenario.feature"));

        List<Scenario> scenarioList = Utils.extractByClass(feature.getChildren(), Scenario.class);

        final Simulation simulation = Utils.convertScenario(scenarioList, ModelFactoryImpl.get());

        final List<Map<String, Boolean>> result = runner.accept(simulation);

        assertTrue("Some conditions are not satisfied", result.stream().flatMap(e -> e.values().stream()).allMatch(Boolean::booleanValue));
    }
}
