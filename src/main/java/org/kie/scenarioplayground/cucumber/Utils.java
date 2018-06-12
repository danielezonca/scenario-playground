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
import org.kie.scenarioplayground.scenario.model.Expression;
import org.kie.scenarioplayground.scenario.model.FactMapping;
import org.kie.scenarioplayground.scenario.model.FactMappingType;
import org.kie.scenarioplayground.scenario.model.FactMappingValue;
import org.kie.scenarioplayground.scenario.model.Simulation;
import org.kie.scenarioplayground.scenario.model.SimulationDescriptor;

import static java.util.stream.Collectors.joining;
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

                FactMapping factMapping = simulation.getSimulationDescriptor().getFactMappingsByName(getFactName(step));
                List<Expression> expressions = factMapping.getAllExpressions();

                for (TableRow tableRow : rows.subList(1, rows.size())) {

                    List<TableCell> row = tableRow.getCells();
                    for (int i = 0; i < expressions.size(); i += 1) {
                        Expression expression = expressions.get(i);
                        TableCell tableCell = row.get(i);

                        tableCell.getValue();

                        if (!isCompatible(factMapping.getClazz(), expression.getExpressionElements(), tableCell.getValue())) {
                            throw new IllegalArgumentException("Value '" + tableCell.getValue() + "' is not compatible with '" + factMapping.getClazz().getCanonicalName() + "'");
                        }

                        FactMappingValue factMappingValue = new FactMappingValue(factName, expression.getName(), tableCell.getValue());

                        internalScenario.addMappingValue(factMappingValue);
                    }
                }
            }
        }
        return simulation;
    }

    static private void generateFactMapping(TableRow header, Simulation simulation, Step step, FactMappingType keyword, ModelFactory modelFactory) {
        List<String> errors = new ArrayList<>();
        Class<?> classMatched = modelFactory.getInstance(step.getText());
        SimulationDescriptor simulationDescriptor = simulation.getSimulationDescriptor();
        if (simulationDescriptor.getFactMappingsByName(getFactName(step)) != null) {
            return;
        }
        FactMapping mappingElement = simulationDescriptor.addGenericObject(keyword, getFactName(step), classMatched);
        for (TableCell tableCell : header.getCells()) {
            String fieldBindingName = tableCell.getValue();
            String fieldName = Introspector.decapitalize(WordUtils.capitalizeFully(fieldBindingName).replaceAll("\\s+", ""));
            if (checkExpressionStep(classMatched, fieldName)) {
                Expression expression = mappingElement.addExpression(fieldBindingName);
                expression.addExpressionElement(fieldName);
            } else {
                errors.add(fieldName);
            }
        }
        if (errors.size() > 0) {
            throw new IllegalArgumentException("Impossible to find in '" + classMatched.getCanonicalName() + "' the following fields: " + errors.stream().collect(joining(", ")));
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
        Optional<FactMappingType> newMain = FactMappingType.fromString(toCompute);
        if (current == null) {
            return newMain.orElseThrow(IllegalArgumentException::new);
        }
        return newMain.orElse(current);
    }

    static public String getFactName(Step step) {
        return step.getText().replaceAll("\\s+", "");
    }

    static private boolean isCompatible(Class<?> clazz, List<Expression.ExpressionElement> expressions, String value) {
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
