package org.kie.scenarioplayground.scenario.model;

import org.junit.Assert;
import org.junit.Test;

import static org.kie.scenarioplayground.cucumber.CucumberFactMappingTypeFactory.stringToFactMappingType;

public class CucumberFactMappingTypeTest {

    @Test
    public void testFromStringMatchFoundName() {
        Assert.assertEquals(FactMappingType.GIVEN, stringToFactMappingType(" given ").get());
    }

    @Test
    public void testFromStringMatchFoundAlias() {
        Assert.assertEquals(FactMappingType.EXPECTED, stringToFactMappingType(" when ").get());
    }

    @Test
    public void testFromStringMatchNotFound() {
        Assert.assertFalse(stringToFactMappingType(" random ").isPresent());
    }
}
