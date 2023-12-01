package org.example.flowable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.bpmn.deployer.BpmnDeployer;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableTest;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@FlowableTest
public class ProcessTest {

    private ProcessEngine processEngine;
    private RuntimeService runtimeService;
    private TaskService taskService;

    @BeforeEach
    void setUp(ProcessEngine processEngine) {
        this.processEngine = processEngine;
        this.runtimeService = processEngine.getRuntimeService();
        this.taskService = processEngine.getTaskService();
    }

    @Test
    @Deployment(resources = "processes/testSimpleProcess.bpmn20.xml")
    void testSimpleProcess() {
        runtimeService.startProcessInstanceByKey("testSimpleProcess");

        Task task = taskService.createTaskQuery().singleResult();
        assertEquals("My Task", task.getName());

        taskService.complete(task.getId());
        assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    }

    @Test
    @Deployment(resources = "processes/testExpressionFunctions.bpmn20.xml")
    public void testExpressionFunctions() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testExpressionFunctions");
        System.out.println(processInstance.getProcessVariables());
    }

    @Test
    @Deployment(resources = "processes/testAsyncInvoker.bpmn20.xml")
    public void testAsyncInvoker() {
        long l = System.currentTimeMillis();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testAsyncInvoker");
        System.out.println(System.currentTimeMillis() - l + "ms " + processInstance.getProcessVariables());
    }

    @Test
    @Deployment(resources = "processes/testAsyncHttpTask.bpmn20.xml")
    public void testAsyncHttpTask() {
        long l = System.currentTimeMillis();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testAsyncHttpTask");
        System.out.println(System.currentTimeMillis() - l + "ms " + processInstance.getProcessVariables());
    }

    @Test
    @Deployment(resources = "processes/testSkipExpression.bpmn20.xml")
    public void testSkipExpression() {
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("skipTask1", true);
        variables.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
        // variables.put("_FLOWABLE_SKIP_EXPRESSION_ENABLED", true);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSkipExpression", variables);
        System.out.println(processInstance.getProcessVariables());
    }

    @Test
    @Deployment(resources = "processes/simple.bpmn20.xml")
    public void testSimple() {
        long l = System.currentTimeMillis();
        HashMap<String, Object> variables = new HashMap<>();
        HashMap<String, Object> params = new HashMap<>();
        params.put("k1", "v1");
        variables.put("other", params);
        variables.put("name", "test");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simple", variables);
        System.out.println(System.currentTimeMillis() - l + "ms " + processInstance.getProcessVariables());
    }

    @Test
    @Deployment(resources = "processes/simpleHttpTask.bpmn20.xml")
    public void testSimpleHttpTask() {
        long l = System.currentTimeMillis();
        HashMap<String, Object> variables = new HashMap<>();
        HashMap<String, Object> params = new HashMap<>();
        params.put("k1", "v1");
        variables.put("name", params);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleHttpTask", variables);
        System.out.println(System.currentTimeMillis() - l + "ms " + processInstance.getProcessVariables());
    }
}
