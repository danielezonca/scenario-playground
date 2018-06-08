package org.kie.scenarioplayground;

import java.io.IOException;
import java.util.List;

import cucumber.runtime.model.CucumberFeature;
import gherkin.ast.Background;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.Scenario;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import org.junit.Test;
import org.kie.scenarioplayground.cucumber.Utils;

import static org.junit.Assert.*;

public class LoadFeatureFileTest {

    @Test
    public void testLoadFeatureFile() throws IOException {
        CucumberFeature cucumberFeature = Utils.readFeatureFileFromResource("test.feature");
        GherkinDocument gherkinFeature = cucumberFeature.getGherkinFeature();

        assertNotNull(gherkinFeature);
    }

    @Test
    public void testScenarioTransformation() throws IOException {
        Feature feature = Utils.toFeature(Utils.readFeatureFileFromResource("test.feature"));
        List<ScenarioDefinition> scenarioDefinitionList = feature.getChildren();

        List<Scenario> scenarioList = Utils.extractByClass(scenarioDefinitionList, Scenario.class);
        List<ScenarioOutline> scenarioOutlineList = Utils.extractByClass(scenarioDefinitionList, ScenarioOutline.class);
        List<Background> backgroundList = Utils.extractByClass(scenarioDefinitionList, Background.class);

        assertTrue(scenarioList.size() > 0);
        assertTrue(scenarioOutlineList.size() == 0);
        assertTrue(backgroundList.size() == 1);
    }
}
