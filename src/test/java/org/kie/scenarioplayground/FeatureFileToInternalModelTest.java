package org.kie.scenarioplayground;

import java.io.IOException;
import java.util.List;

import gherkin.ast.Feature;
import gherkin.ast.Scenario;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.scenarioplayground.cucumber.Utils;
import org.kie.scenarioplayground.model.ModelFactoryImpl;
import org.kie.scenarioplayground.scenario.model.Simulation;
import org.kie.scenarioplayground.scenario.model.marshaller.SimulationMarshaller;
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

        // Three scenarios declared in test.feature
        // Example 1, Example 2 and Example 3
        assertEquals(3, simulation.getScenarios().size());

        // Example 1
        assertEquals("Example 1", simulation.getScenarios().get(0).getDescription());
        final org.kie.scenarioplayground.scenario.model.Scenario scenarioOne = simulation.getScenarios().get(0);
        Assertions.assertThat(scenarioOne.getFactNames())
                .containsExactly("aCase:", "aProduct:", "someCaseDetail:", "IexpectNextDetail:");
        Assertions.assertThat(scenarioOne.getFactMappingValuesByFactName("aCase:")).hasSize(1);
        Assertions.assertThat(scenarioOne.getFactMappingValuesByFactName("aProduct:")).hasSize(2);
        Assertions.assertThat(scenarioOne.getFactMappingValuesByFactName("someCaseDetail:")).hasSize(18);
        Assertions.assertThat(scenarioOne.getFactMappingValuesByFactName("IexpectNextDetail:")).hasSize(2);

        // Example 2
        assertEquals("Example 2", simulation.getScenarios().get(1).getDescription());
        assertEquals(5, simulation.getScenarios().get(1).getFactNames().size());

        // Example 3
        assertEquals("Example 3", simulation.getScenarios().get(2).getDescription());
        assertEquals(6, simulation.getScenarios().get(2).getFactNames().size());
    }

    @Test
    public void testXmlSerialization() throws IOException {
        SimulationMarshaller simulationMarshaller = new SimulationMarshaller();

        Feature feature = Utils.toFeature(Utils.readFeatureFileFromResource("testSimpleScenario.feature"));

        List<Scenario> scenarioList = Utils.extractByClass(feature.getChildren(), Scenario.class);

        final Simulation simulation = Utils.convertScenario(scenarioList, ModelFactoryImpl.get());

        String simulationXml = simulationMarshaller.toXML(simulation);
        Simulation simulationRestored = simulationMarshaller.fromXML(simulationXml);

        System.out.println("runner.accept(simulation) = " + runner.accept(simulation));

        assertEquals(runner.accept(simulation), runner.accept(simulationRestored));
    }
}
