package org.example.flowable.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

import java.util.concurrent.TimeUnit;

public class S1 implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("invoked phone by http and cost 1s");
        execution.setVariable("phone", 1);
    }
}
