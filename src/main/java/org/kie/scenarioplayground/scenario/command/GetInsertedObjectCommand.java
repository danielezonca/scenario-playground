package org.kie.scenarioplayground.scenario.command;

import org.drools.core.command.impl.ExecutableCommand;
import org.drools.core.command.impl.RegistryContext;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.RequestContext;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.FactHandle;

public class GetInsertedObjectCommand
        implements
        ExecutableCommand<Object> {

    private static final long serialVersionUID = 1666811352927472030L;

    @Override
    public Object execute(Context context) {
        return null;
    }

    @Override
    public boolean canRunInTransaction() {
        return false;
    }
//
//    @Override
//    public Object execute(Context context) {
//        Object returned = ((RequestContext)context).getResult();
//
//        if(!(returned instanceof FactHandle)) {
//
//        }
//
//        KieContainer kieContainer;
//
//        if (releaseId != null) {
//            // use the new API to retrieve the session by ID
//            KieServices kieServices = KieServices.Factory.get();
//            kieContainer = kieServices.newKieContainer(releaseId);
//        } else {
//            kieContainer = ((RegistryContext) context).lookup(KieContainer.class);
//            if (kieContainer == null) {
//                throw new RuntimeException("ReleaseId was not specfied, nor was an existing KieContainer assigned to the Registry");
//            }
//        }
//
//        // TODO is it correct to override user defined session with pseudo clock?
//        if (getClockTypeOption() != null) {
//            final KieSessionModel kieSessionModel = kieContainer.getKieSessionModel(sessionId);
//            kieSessionModel.setClockType(getClockTypeOption());
//        }
//
//        KieSession ksession = sessionId != null ? kieContainer.newKieSession(sessionId) : kieContainer.newKieSession();
//
//        ((RegistryContext) context).register(KieSession.class, ksession);
//
//        return ksession;
//    }
//
//    @Override
//    public String toString() {
//        return "NewKieSessionCommand{" +
//                "sessionId='" + sessionId + '\'' +
//                ", releaseId=" + releaseId +
//                '}';
//    }
}
