package com.aws.demo.AWS;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClientBuilder;
import com.amazonaws.services.simpleworkflow.model.Run;
import com.amazonaws.services.simpleworkflow.model.StartWorkflowExecutionRequest;
import com.amazonaws.services.simpleworkflow.model.WorkflowType;
import com.aws.demo.Utils.HelloType;

public class WorkFlowStarter {

    private static final AmazonSimpleWorkflow swf = AmazonSimpleWorkflowClientBuilder.standard()
            .withRegion(Regions.DEFAULT_REGION).build();

    public static final String WORKFLOW_EXECUTION = "HelloWorldWorkFlowExecution";

    public static void main(String[] args) {
        String workflow_input = "Amazon SWF";
        if(args.length > 0){
            workflow_input = args[0];
        }

        System.out.println("Starting the workflow execution '" + WORKFLOW_EXECUTION + "' with input '" + workflow_input + "'.");

        WorkflowType wType = new WorkflowType().withName(HelloType.WORKFLOW).withVersion(HelloType.WORKFLOW_VERSION);

        Run run = swf.startWorkflowExecution(new StartWorkflowExecutionRequest().withDomain(HelloType.DOMAIN).withWorkflowType(wType).withWorkflowId(WORKFLOW_EXECUTION).withInput(workflow_input).withExecutionStartToCloseTimeout("90"));
    
        System.out.println("WorkFlow execution started with run Id : " + run.getRunId());
    }
}
