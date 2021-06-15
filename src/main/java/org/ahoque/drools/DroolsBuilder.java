package org.ahoque.drools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroolsBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DroolsBuilder.class);

    private KieServices kieServices = KieServices.Factory.get();

    public KieSession getKieSession() throws IOException {
        LOGGER.info("Session created...");
        KieSession kieSession = getKieContainer().newKieSession();
        kieSession.setGlobal("showResults", new OutputDisplay());
        kieSession.setGlobal("sh", new OutputDisplay());

        kieSession.setGlobal("MASTER_CARD_DISCOUNT_PRICE_THRESHOLD", 10000);
        kieSession.setGlobal("VISA_CARD_DISCOUNT_PRICE_THRESHOLD", 5000);
        kieSession.setGlobal("ICICI_CARD_DISCOUNT_PRICE_THRESHOLD", 3000);

        kieSession.addEventListener(new RuleRuntimeEventListener() {
            @Override
            public void objectInserted(ObjectInsertedEvent event) {
                System.out.println("Object inserted \n "
                        + event.getObject().toString());
            }

            @Override
            public void objectUpdated(ObjectUpdatedEvent event) {
                System.out.println("Object was updated \n"
                        + "New Content \n"
                        + event.getObject().toString());
            }

            @Override
            public void objectDeleted(ObjectDeletedEvent event) {
                System.out.println("Object retracted \n"
                        + event.getOldObject().toString());
            }
        });
        return kieSession;
    }

    public KieContainer getKieContainer() throws IOException {
        LOGGER.info("Container created...");
        getKieRepository();
        KieBuilder kb = kieServices.newKieBuilder(getKieFileSystem());
        kb.buildAll();
        KieModule kieModule = kb.getKieModule();
        return kieServices.newKieContainer(kieModule.getReleaseId());
    }

    private void getKieRepository() {
        final KieRepository kieRepository = kieServices.getRepository();
        kieRepository.addKieModule(new KieModule() {
            @Override
            public ReleaseId getReleaseId() {
                return kieRepository.getDefaultReleaseId();
            }
        });
    }

    private KieFileSystem getKieFileSystem() throws IOException {

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        Resource resource = ResourceFactory.newClassPathResource("pre-process/order.drl");
        List<String> applyDroolsList = readFromInputStream(resource.getInputStream());
        for(String fileName: applyDroolsList){
            kieFileSystem.write(ResourceFactory.newClassPathResource(fileName));
        }
        kieFileSystem.write(resource);
        return kieFileSystem;
    }

    private List<String> readFromInputStream(InputStream inputStream)
            throws IOException {
        List<String> drlFileNames = new ArrayList<>();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(line.contains("// include ")){
                    drlFileNames.add(line.replace("// include ", "").trim());
                }
            }
        }
        return drlFileNames;
    }
}
