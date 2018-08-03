package org.kie.scenarioplayground.cucumber;

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import cucumber.runtime.model.CucumberFeature;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.DataTable;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.Node;
import gherkin.ast.Scenario;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
import gherkin.ast.TableCell;
import gherkin.ast.TableRow;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.kie.scenarioplayground.scenario.model.ExpressionElement;
import org.kie.scenarioplayground.scenario.model.ExpressionIdentifier;
import org.kie.scenarioplayground.scenario.model.FactIdentifier;
import org.kie.scenarioplayground.scenario.model.FactMapping;
import org.kie.scenarioplayground.scenario.model.FactMappingType;
import org.kie.scenarioplayground.scenario.model.FactMappingValue;
import org.kie.scenarioplayground.scenario.model.Simulation;
import org.kie.scenarioplayground.scenario.model.SimulationDescriptor;

import static java.util.stream.Collectors.toList;

public class Utils {

    public static <T extends ScenarioDefinition> List<T> extractByClass(List<ScenarioDefinition> inputList, Class<T> tClass) {
        return inputList.stream().filter(tClass::isInstance).map(tClass::cast).collect(toList());
    }

    public static CucumberFeature readFeatureFileFromResource(final String path) throws IOException {
        Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
        TokenMatcher matcher = new TokenMatcher();

        Scanner scanner = new Scanner(new File(Utils.class.getClassLoader().getResource(path).getFile()));
        String source = scanner.useDelimiter("\\Z").next();

        GherkinDocument gherkinDocument = parser.parse(source, matcher);
        return new CucumberFeature(gherkinDocument, path, source);
    }

    public static Feature toFeature(CucumberFeature cucumberFeature) {
        return cucumberFeature.getGherkinFeature().getFeature();
    }

    public static Simulation convertScenario(List<Scenario> scenarioList, ModelFactory modelFactory) {

        Simulation simulation = new Simulation();
        for (Scenario scenario : scenarioList) {

            List<Step> steps = scenario.getSteps();

            org.kie.scenarioplayground.scenario.model.Scenario internalScenario = simulation.addScenario(scenario.getName());

            FactMappingType mainKeyword = null;
            for (Step step : steps) {
                String keyword = step.getKeyword();
                mainKeyword = computeMainKeyword(mainKeyword, keyword);
                Node argument = step.getArgument();

                if (!(argument instanceof DataTable)) {
                    continue;
                }

                DataTable values = (DataTable) argument;
                List<TableRow> rows = values.getRows();
                if (rows.size() < 1) {
                    throw new IllegalArgumentException("Malformed line: " + values);
                }

                TableRow header = rows.get(0);
                generateFactMapping(header, simulation, step, mainKeyword, modelFactory);

                String factName = getFactName(step);

                List<FactMapping> factMappings = simulation.getSimulationDescriptor().getFactMappingsByFactName(factName);

                for (TableRow tableRow : rows.subList(1, rows.size())) {

                    List<TableCell> row = tableRow.getCells();
                    for (int i = 0; i < factMappings.size(); i += 1) {
                        FactMapping factMapping = factMappings.get(i);
                        TableCell tableCell = row.get(i);

                        tableCell.getValue();

                        if (!isCompatible(factMapping.getFactIdentifier().getClazz(), factMapping.getExpressionElements(), tableCell.getValue())) {
                            throw new IllegalArgumentException("Value '" + tableCell.getValue() + "' is not compatible with '" + factMapping.getClazz().getCanonicalName() + "'");
                        }

                        if(internalScenario.getFactMappingValue(factName, factMapping.getExpressionIdentifier()).isPresent()) {
                            // TODO manage duplicated mapping value?
                            continue;
                        }

                        internalScenario.addMappingValue(factName, factMapping.getExpressionIdentifier(), tableCell.getValue());
                    }
                }
            }
        }
        return simulation;
    }

    static private void generateFactMapping(TableRow header, Simulation simulation, Step step, FactMappingType factMappingType, ModelFactory modelFactory) {
        List<String> errors = new ArrayList<>();
        Class<?> classMatched = modelFactory.getInstance(step.getText());
        SimulationDescriptor simulationDescriptor = simulation.getSimulationDescriptor();
        List<FactMapping> factMappingsByFactName = simulationDescriptor.getFactMappingsByFactName(getFactName(step));
        if (factMappingsByFactName != null && factMappingsByFactName.size() > 0) {
            return;
        }
        FactIdentifier factIdentifier = simulationDescriptor.newFactIdentifier(getFactName(step), classMatched);
        for (TableCell tableCell : header.getCells()) {
            String fieldBindingName = tableCell.getValue();
            String fieldName = Introspector.decapitalize(WordUtils.capitalizeFully(fieldBindingName).replaceAll("\\s+", ""));
            ExpressionIdentifier expressionIdentifier = ExpressionIdentifier.identifier(fieldBindingName, factMappingType);
            if (checkExpressionStep(classMatched, fieldName) &&
                    !simulationDescriptor.getFactMapping(expressionIdentifier, factIdentifier).isPresent()) {
                FactMapping factMapping = simulationDescriptor.addFactMapping(expressionIdentifier, factIdentifier);
                Class<?> currentClazz = factMapping.getClazz();
                try {
                    Class<?> clazz = currentClazz.getDeclaredField(fieldName).getType();
                    factMapping.addExpressionElement(fieldName, clazz);
                } catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException("Impossible to find a field with name '" + fieldName + "' in class '" + currentClazz.getCanonicalName() + "'");
                }
            } else {
                errors.add(fieldName);
            }
        }
        if (errors.size() > 0) {
            throw new IllegalArgumentException("Impossible to find in '" +
                                                       classMatched.getCanonicalName() + "' the following fields: " + String.join(", ", errors));
        }
    }

    static private boolean checkExpressionStep(Class<?> clazz, String fieldName) {
        try {
            clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return false;
        }
        return true;
    }

    static private FactMappingType computeMainKeyword(FactMappingType current, String toCompute) {
        Optional<FactMappingType> newMain = CucumberFactMappingTypeFactory.stringToFactMappingType(toCompute);
        if (current == null) {
            return newMain.orElseThrow(IllegalArgumentException::new);
        }
        return newMain.orElse(current);
    }

    static public String getFactName(Step step) {
        return step.getText().replaceAll("\\s+", "");
    }

    static private boolean isCompatible(Class<?> clazz, List<ExpressionElement> expressions, String value) {
        if (expressions.size() < 1) {
            throw new IllegalArgumentException("No expression defined");
        }
        return canBeAssigned(clazz, expressions.get(expressions.size() - 1).getStep(), value);
    }

    static private boolean canBeAssigned(Class<?> clazz, String fieldName, String value) {
        if (clazz.isAssignableFrom(String.class)) {
            return true;
        }
        try {
            Class<?> type = clazz.getDeclaredField(fieldName).getType();
            // FIXME support primitive types
            if (Number.class.isAssignableFrom(type) && StringUtils.isNumeric(value)) {
                return true;
            }
            type.getConstructor(String.class);
            return true;
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            return false;
        }
    }
}
