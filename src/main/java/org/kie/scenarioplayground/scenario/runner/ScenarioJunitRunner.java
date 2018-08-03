package org.kie.scenarioplayground.scenario.runner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.kie.scenarioplayground.scenario.model.Simulation;
import org.kie.scenarioplayground.scenario.model.marshaller.SimulationMarshaller;
import org.kie.scenarioplayground.scenario.utils.Constants;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class ScenarioJunitRunner extends ParentRunner<Simulation> {

    List<Simulation> simulations = new ArrayList<>();
    SimulationRunner simulationRunner = new SimulationRunner();
    SimulationMarshaller simulationMarshaller = new SimulationMarshaller();

    public ScenarioJunitRunner(Class<?> clazz) throws InitializationError, IOException {
        super(clazz);
        System.out.println("clazz = " + clazz);

        // TODO evaluate if keep this method or implement an alternative
        final PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());
        final Resource[] resources = pathMatchingResourcePatternResolver.getResources("*." + Constants.SCENARIO_EXTENSION);

        for (Resource resource : resources) {
            Scanner scanner = new Scanner(new File(resource.getURI()));
            final String simulationRaw = scanner.useDelimiter("\\Z").next();
            final Simulation simulation = simulationMarshaller.fromXML(simulationRaw);
            simulations.add(simulation);
        }
    }

    @Override
    protected List<Simulation> getChildren() {
        return simulations;
    }

    @Override
    protected Description describeChild(Simulation child) {
        return Description.createSuiteDescription(child.toString());
    }

    @Override
    protected void runChild(Simulation child, RunNotifier notifier) {

        List<Map<String, Boolean>> result = simulationRunner.accept(child);
        notifier.fireTestStarted(describeChild(child));

        final boolean allMatch = result.stream().flatMap(e -> e.values().stream()).allMatch(Boolean::booleanValue);
        if(allMatch) {
            notifier.fireTestFinished(describeChild(child));
        }
        else {
            notifier.fireTestFailure(new Failure(describeChild(child), new IllegalArgumentException("Wrong value")));
        }

    }
}
