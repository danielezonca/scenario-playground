package org.kie.scenarioplayground.scenario.model.marshaller;

import com.thoughtworks.xstream.XStream;
import org.kie.internal.runtime.helper.BatchExecutionHelper;
import org.kie.scenarioplayground.scenario.model.Simulation;

public class SimulationMarshaller {

    XStream xStream = BatchExecutionHelper.newXStreamMarshaller();

    public String toXML(Simulation simulation) {
        return xStream.toXML(simulation);
    }

    public Simulation fromXML(String simulationRaw) {
        return (Simulation) xStream.fromXML(simulationRaw);
    }

}
