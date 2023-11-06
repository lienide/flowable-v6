package org.example.flowable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.common.engine.api.delegate.FlowableFunctionDelegate;
import org.flowable.common.engine.api.variable.VariableContainer;
import org.flowable.common.engine.impl.el.DefaultExpressionManager;
import org.flowable.common.engine.impl.el.ExpressionManager;
import org.flowable.common.engine.impl.el.FlowableAstFunctionCreator;
import org.flowable.common.engine.impl.el.VariableContainerWrapper;
import org.flowable.common.engine.impl.el.function.*;
import org.flowable.engine.impl.el.FlowableDateFunctionDelegate;
import org.flowable.engine.impl.function.TaskGetFunctionDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpressionTest {

    ExpressionManager expressionManager;

    VariableContainer variableContainer = new VariableContainerWrapper(null);;

    @BeforeEach
    public void setup() {
        expressionManager = new DefaultExpressionManager(new HashMap<>());

        List<FlowableFunctionDelegate> flowableFunctionDelegates = new ArrayList<>();

        flowableFunctionDelegates.add(new FlowableDateFunctionDelegate());

        flowableFunctionDelegates.add(new VariableGetExpressionFunction());
        flowableFunctionDelegates.add(new VariableGetOrDefaultExpressionFunction());

        flowableFunctionDelegates.add(new VariableContainsAnyExpressionFunction());
        flowableFunctionDelegates.add(new VariableContainsExpressionFunction());

        flowableFunctionDelegates.add(new VariableEqualsExpressionFunction());
        flowableFunctionDelegates.add(new VariableNotEqualsExpressionFunction());

        flowableFunctionDelegates.add(new VariableExistsExpressionFunction());
        flowableFunctionDelegates.add(new VariableIsEmptyExpressionFunction());
        flowableFunctionDelegates.add(new VariableIsNotEmptyExpressionFunction());

        flowableFunctionDelegates.add(new VariableLowerThanExpressionFunction());
        flowableFunctionDelegates.add(new VariableLowerThanOrEqualsExpressionFunction());
        flowableFunctionDelegates.add(new VariableGreaterThanExpressionFunction());
        flowableFunctionDelegates.add(new VariableGreaterThanOrEqualsExpressionFunction());

        flowableFunctionDelegates.add(new VariableBase64ExpressionFunction());

        flowableFunctionDelegates.add(new TaskGetFunctionDelegate());

        expressionManager.setFunctionDelegates(flowableFunctionDelegates);

        List<FlowableAstFunctionCreator> astFunctionCreators = new ArrayList<>();
        for (FlowableFunctionDelegate flowableFunctionDelegate : flowableFunctionDelegates) {
            if (flowableFunctionDelegate instanceof FlowableAstFunctionCreator) {
                astFunctionCreators.add((FlowableAstFunctionCreator) flowableFunctionDelegate);
            }
        }

        expressionManager.setAstFunctionCreators(astFunctionCreators);
    }

    @Test
    public void test() throws Exception { // TODO: resolve miss val when vars:get(age) is null => {'name':}
        long l = System.currentTimeMillis();

        variableContainer.setVariable("name", null);

        Expression expression = expressionManager.createExpression("{'name': ${name}}");
        String value = (String) expression.getValue(variableContainer);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(value);

        System.out.println("cost: " + (System.currentTimeMillis() - l) + " " + jsonNode);
    }
}
