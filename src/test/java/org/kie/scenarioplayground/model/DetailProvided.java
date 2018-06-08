package org.kie.scenarioplayground.model;

public class DetailProvided {

    String description, reproducer, expectedResult;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReproducer() {
        return reproducer;
    }

    public void setReproducer(String reproducer) {
        this.reproducer = reproducer;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }
}
