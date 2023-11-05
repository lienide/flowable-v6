package org.example.flowable.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class SendRejectionMail implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("Send the rejection email for employee "
                + execution.getVariable("employee"));
    }
}
