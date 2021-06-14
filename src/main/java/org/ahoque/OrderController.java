package org.ahoque;

import org.kie.api.runtime.KieSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class OrderController {

    @PostMapping("/order")
    public Order processRules(@RequestBody Order order) throws IOException {
        DroolsBuilder builder = new DroolsBuilder();
        KieSession kieSession = builder.getKieSession();
        kieSession.insert(order);
        kieSession.fireAllRules();
        return order;
    }
}
