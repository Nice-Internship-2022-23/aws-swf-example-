package com.aws.demo.Utils;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClientBuilder;
import com.amazonaws.services.simpleworkflow.model.ChildPolicy;
import com.amazonaws.services.simpleworkflow.model.RegisterActivityTypeRequest;
import com.amazonaws.services.simpleworkflow.model.RegisterDomainRequest;
import com.amazonaws.services.simpleworkflow.model.RegisterWorkflowTypeRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;

public class HelloType {
    
    public static final String DOMAIN = "HelloDomain";
    public static final String TASKLIST = "HelloTaskList";
    public static final String WORKFLOW = "HelloWorkFlow";
    public static final String WORKFLOW_VERSION = "1.0.0";
    public static final String ACTIVITY = "HelloActivity";
    public static final String ACTIVITY_VERSION = "1.0.0";

    private static final AmazonSimpleWorkflow swf = AmazonSimpleWorkflowClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();

    public static void registerDomain() {
        try {
            System.out.println("Registarign Domain with domain name : " + DOMAIN);
            swf.registerDomain(new RegisterDomainRequest()
                .withName(DOMAIN)
                .withWorkflowExecutionRetentionPeriodInDays("1"));

        } catch (Exception e) {
            System.out.println("Domain already existed...");
        }
    }

    public static void registerActivityType(){
        try {

            swf.registerActivityType(new RegisterActivityTypeRequest()
            .withDomain(DOMAIN)
            .withName(ACTIVITY)
            .withVersion(ACTIVITY_VERSION)
            .withDefaultTaskList(new TaskList().withName(TASKLIST))
            .withDefaultTaskScheduleToStartTimeout("30")
            .withDefaultTaskStartToCloseTimeout("600")
            .withDefaultTaskStartToCloseTimeout("630")
            .withDefaultTaskHeartbeatTimeout("10")
            );

        } catch (Exception e) {
            System.out.println("Activity type already exist!");
        }
    }

    public static void registerWorkFlowType(){
        try {
            System.out.println("Registering workflow type '" + WORKFLOW + "' - '" + WORKFLOW_VERSION + "'");

            swf.registerWorkflowType(new RegisterWorkflowTypeRequest()
                .withDomain(DOMAIN)
                .withName(WORKFLOW)
                .withVersion(WORKFLOW_VERSION)
                .withDefaultChildPolicy(ChildPolicy.TERMINATE)
                .withDefaultTaskList(new TaskList().withName(TASKLIST))
                .withDefaultTaskStartToCloseTimeout("30")
                );
                
        } catch (Exception e) {
            System.out.println("Workflow type already exist!");
        }
    }



    public static void main(String[] args) {
        registerDomain();
        registerActivityType();
        registerWorkFlowType();
    }
}
