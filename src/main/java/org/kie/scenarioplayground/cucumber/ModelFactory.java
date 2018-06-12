package org.kie.scenarioplayground.cucumber;

// TODO implement a DefaultModelFactory with a naming convention approach
public interface ModelFactory {

    Class<?> getInstance(String toMatch);

}
