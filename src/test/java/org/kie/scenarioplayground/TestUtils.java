package org.kie.scenarioplayground;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.scenarioplayground.model.DetailProvided;
import org.kie.scenarioplayground.model.NextDetail;

import static org.junit.Assert.assertTrue;

public class TestUtils {

    static final String header = "package org.kie.scenarioplayground\n" +
                "import " + DetailProvided.class.getCanonicalName() + "\n" +
                "import " + NextDetail.class.getCanonicalName() + "\n";

    static final String drl1 = "global Integer outS;\n" +
                "rule R1 when\n" +
                "   s : DetailProvided()\n" +
                "then\n" +
                "    Integer o = (Integer) kcontext.getKnowledgeRuntime().getGlobal(\"outS\");\n" +
                "    NextDetail nd = new NextDetail();\n" +
                "    nd.setAnswer(\"Update 1\");\n" +
                "    nd.setType(\"Resolution\");\n" +
                "    insert(nd);\n" +
                "    kcontext.getKnowledgeRuntime().setGlobal(\"outS\", o == null ? 1 : o + 1);\n" +
                "end\n";

    public static final ReleaseId releaseId = createKJarWithMultipleResources("org.kie", new String[]{header + drl1}, new ResourceType[]{ResourceType.DRL});

    public static ReleaseId createKJarWithMultipleResources(String id,
                                                            String[] resources,
                                                            ResourceType[] types) {
        KieServices ks = KieServices.Factory.get();
        KieModuleModel kproj = ks.newKieModuleModel();
        KieFileSystem kfs = ks.newKieFileSystem();

        for ( int i = 0; i < resources.length; i++ ) {
            String res = resources[i];
            String type = types[i].getDefaultExtension();

            kfs.write( "src/main/resources/" + id.replaceAll( "\\.", "/" ) + "/org/test/res" + i + "." + type, res );
        }

        KieBaseModel kBase1 = kproj.newKieBaseModel(id )
                .setEqualsBehavior(EqualityBehaviorOption.EQUALITY )
                .setEventProcessingMode(EventProcessingOption.STREAM );

        KieSessionModel ksession1 = kBase1.newKieSessionModel(id + ".KSession1" )
                .setDefault(true)
                .setType(KieSessionModel.KieSessionType.STATEFUL)
                .setClockType(ClockTypeOption.get("pseudo" ) );

        kfs.writeKModuleXML(kproj.toXML());

        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        assertTrue(kieBuilder.getResults().getMessages().isEmpty());

        KieModule kieModule = kieBuilder.getKieModule();
        return kieModule.getReleaseId();
    }
}
