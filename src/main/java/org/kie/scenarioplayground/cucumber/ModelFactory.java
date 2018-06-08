package org.kie.scenarioplayground.cucumber;

public interface ModelFactory {

    Class<?> getInstance(String toMatch);

}
