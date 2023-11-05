package org.example.flowable;

import org.flowable.engine.RuntimeService;
import org.flowable.spring.impl.test.FlowableSpringExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(FlowableSpringExtension.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:flowable.cfg.xml")
public class SpringProcessTest {

    @Autowired
    private RuntimeService runtimeService;

    @Test
    public void test() {
        Assertions.assertNotNull(runtimeService);
    }
}
