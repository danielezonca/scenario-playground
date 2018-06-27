package org.kie.scenarioplayground.scenario.model;

import org.junit.Assert;
import org.junit.Test;

public class FactMappingTypeTest {

    @Test
    public void testFromStringMatchFoundName() {
        Assert.assertEquals(FactMappingType.GIVEN, FactMappingType.fromString(" given ").get());
    }

    @Test
    public void testFromStringMatchFoundAlias() {
        Assert.assertEquals(FactMappingType.EXPECTED, FactMappingType.fromString(" when ").get());
    }

    @Test
    public void testFromStringMatchNotFound() {
        Assert.assertFalse(FactMappingType.fromString(" random ").isPresent());
    }
}
