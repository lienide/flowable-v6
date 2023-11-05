package org.example.flowable.delegate;

import org.flowable.common.engine.impl.el.ExpressionManager;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.util.CommandContextUtil;

import java.util.concurrent.TimeUnit;

public class S3 implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // get input params
        ExpressionManager expressionManager = CommandContextUtil.getProcessEngineConfiguration().getExpressionManager();
        Object phone = expressionManager.createExpression("#{vars:get(phone)}").getValue(execution);

        System.out.println("invoked model by http and cost 10s");
        execution.setVariable("model", 10);
    }
}
