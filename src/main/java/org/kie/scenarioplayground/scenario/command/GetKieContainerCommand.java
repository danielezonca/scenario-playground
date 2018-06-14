package org.kie.scenarioplayground.scenario.command;

import org.drools.core.command.impl.ExecutableCommand;
import org.drools.core.command.impl.RegistryContext;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.KieContainer;

public class GetKieContainerCommand
        implements
        ExecutableCommand<KieContainer> {

    private static final long serialVersionUID = 2154383558130692888L;

    private ReleaseId releaseId;

    public GetKieContainerCommand(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    public KieContainer execute(Context context) {
        // use the new API to retrieve the session by ID
        KieServices  kieServices  = KieServices.Factory.get();

        KieContainer kieContainer = releaseId != null? kieServices.newKieContainer(releaseId) : kieServices.newKieClasspathContainer();

        ((RegistryContext)context).register( KieContainer.class, kieContainer );
        return kieContainer;
    }

    public ReleaseId getReleaseId() {
        return releaseId;
    }

    @Override
    public String toString() {
        return "GetKieContainerCommand{" +
                "releaseId=" + releaseId +
                '}';
    }
}
