package org.kie.scenarioplayground;

import java.io.IOException;
import java.util.List;

import gherkin.ast.Feature;
import gherkin.ast.Scenario;
import org.junit.Test;
import org.kie.scenarioplayground.cucumber.Utils;
import org.kie.scenarioplayground.model.ModelFactoryImpl;
import org.kie.scenarioplayground.scenario.model.Simulation;
import org.kie.scenarioplayground.scenario.runner.ScenarioRunner;
import org.kie.scenarioplayground.scenario.runner.ToStringRunner;

import static org.junit.Assert.assertEquals;

public class FeatureFileToInternalModelTest {

    ScenarioRunner<String> runner = new ToStringRunner();

    @Test
    public void testSingleScenario() throws IOException {

        Feature feature = Utils.toFeature(Utils.readFeatureFileFromResource("test.feature"));

        List<Scenario> scenarioList = Utils.extractByClass(feature.getChildren(), Scenario.class);

        final Simulation simulation = Utils.convertScenario(scenarioList, ModelFactoryImpl.get());

        final long originalFacts = scenarioList.stream().flatMap(e -> e.getSteps().stream())
                // filter unsupported "When" clause
                .filter(e -> !"when".equalsIgnoreCase(e.getKeyword().trim())).map(Utils::getFactName).distinct().count();
        final long convertedFacts = simulation.getSimulationDescriptor().getAllFactMappings().size();

        assertEquals(originalFacts, convertedFacts);
    }

    @Test
    public void testXmlSerialization() throws IOException {
        Feature feature = Utils.toFeature(Utils.readFeatureFileFromResource("testSimpleScenario.feature"));

        List<Scenario> scenarioList = Utils.extractByClass(feature.getChildren(), Scenario.class);

        final Simulation simulation = Utils.convertScenario(scenarioList, ModelFactoryImpl.get());

        String simulationXml = TestUtils.toXml(simulation);
        Simulation simulationRestored = TestUtils.fromXml(simulationXml);

        System.out.println("runner.accept(simulation) = " + runner.accept(simulation));

        assertEquals(runner.accept(simulation), runner.accept(simulationRestored));
    }
}
