package org.example.flowable.delegate;

import org.flowable.common.engine.api.async.AsyncTaskInvoker;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.FutureJavaDelegate;
import org.flowable.engine.delegate.JavaDelegate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class S2 implements FutureJavaDelegate<Map<String, Object>> {

    @Override
    public CompletableFuture<Map<String, Object>> execute(DelegateExecution execution, AsyncTaskInvoker taskInvoker) {
        // collect params from execution before async invoke
        return taskInvoker.submit(() -> {
            TimeUnit.SECONDS.sleep(10);
            System.out.println("invoked score by http and cost 10s");
            Map<String,Object> variables = new HashMap<>();
            variables.put("score", 5);
            return variables;
        });
    }

    @Override
    public void afterExecution(DelegateExecution execution, Map<String, Object> executionData) {
        execution.setVariables(executionData);
    }
}
