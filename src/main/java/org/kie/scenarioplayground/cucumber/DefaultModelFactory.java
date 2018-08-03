package org.kie.scenarioplayground.cucumber;

import java.util.Arrays;
import java.util.List;

public class DefaultModelFactory implements ModelFactory {

    final List<Class<?>> classToMatch;

    public DefaultModelFactory(List<Class<?>> classToMatch) {
        this.classToMatch = classToMatch;
    }

    public DefaultModelFactory(Class<?> ... args) {
        this.classToMatch = Arrays.asList(args);
    }

    @Override
    public Class<?> getInstance(String toMatch) {
        return classToMatch.stream().filter(e -> e.getSimpleName().contains(toMatch)).findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
