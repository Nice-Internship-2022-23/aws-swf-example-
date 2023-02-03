package com.aws.demo.AWS;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClientBuilder;
import com.amazonaws.services.simpleworkflow.model.ActivityType;
import com.amazonaws.services.simpleworkflow.model.CompleteWorkflowExecutionDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.Decision;
import com.amazonaws.services.simpleworkflow.model.DecisionTask;
import com.amazonaws.services.simpleworkflow.model.DecisionType;
import com.amazonaws.services.simpleworkflow.model.HistoryEvent;
import com.amazonaws.services.simpleworkflow.model.PollForDecisionTaskRequest;
import com.amazonaws.services.simpleworkflow.model.RespondDecisionTaskCompletedRequest;
import com.amazonaws.services.simpleworkflow.model.ScheduleActivityTaskDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.aws.demo.Utils.HelloType;

public class WorkFlowWorker {

    private static final AmazonSimpleWorkflow swf = AmazonSimpleWorkflowClientBuilder.standard()
            .withRegion(Regions.DEFAULT_REGION).build();

    public static void main(String[] args) {

        PollForDecisionTaskRequest task_Request = new PollForDecisionTaskRequest()
                .withDomain(HelloType.DOMAIN)
                .withTaskList(new TaskList().withName(HelloType.TASKLIST));

        while (true) {
            System.out.println("Polling for decision task from the tasklist '" + HelloType.TASKLIST
                    + "' in the domain '" + HelloType.DOMAIN + "'.");

            DecisionTask task = swf.pollForDecisionTask(task_Request);

            String taskToken = task.getTaskToken();
            if (taskToken != null) {
                try {
                    executeDecisionTask(taskToken, task.getEvents());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void executeDecisionTask(String taskToken, List<HistoryEvent> events) throws Throwable {

        List<Decision> decisions = new ArrayList<>();
        String workflow_input = null;
        int scheduled_activites = 0;
        int open_activities = 0;
        boolean activity_completed = false;
        String result = null;

        System.out.println("Executing decision task for the history events [");

        for (HistoryEvent event : events) {
            System.out.println(" " + event);

            switch (event.getEventType()) {
                case "WorkflowExecutionStarted":
                    workflow_input = event.getWorkflowExecutionStartedEventAttributes().getInput();
                    break;
                case "ActivityTaskScheduled":
                    scheduled_activites++;
                    break;
                case "ScheduleActivityTaskFailed":
                    scheduled_activites--;
                    break;
                case "ActivityTaskStarted":
                    scheduled_activites--;
                    open_activities++;
                    break;
                case "ActivityTaskCompleted":
                    open_activities--;
                    activity_completed = true;
                    result = event.getActivityTaskCompletedEventAttributes().getResult();
                    break;
                case "ActivityTaskFailed":
                    open_activities--;
                    break;
                case "ActivityTaskTimeout":
                    open_activities--;
                    break;
            }
            System.out.println("]");

            if (activity_completed) {
                decisions.add(new Decision().withDecisionType(DecisionType.CompleteWorkflowExecution)
                        .withCompleteWorkflowExecutionDecisionAttributes(
                                new CompleteWorkflowExecutionDecisionAttributes().withResult(result)));
            } else {
                if (open_activities == 0 && scheduled_activites == 0) {
                    ScheduleActivityTaskDecisionAttributes attr = new ScheduleActivityTaskDecisionAttributes()
                            .withActivityType(new ActivityType().withName(HelloType.ACTIVITY)
                                    .withVersion(HelloType.ACTIVITY_VERSION))
                            .withActivityId(UUID.randomUUID().toString())
                            .withInput(workflow_input);
                    decisions.add(new Decision().withDecisionType(DecisionType.ScheduleActivityTask)
                            .withScheduleActivityTaskDecisionAttributes(attr));
                }
            }
        }

        System.out.println("Exiting the decision task with the decisions " + decisions);

        swf.respondDecisionTaskCompleted(new RespondDecisionTaskCompletedRequest().withTaskToken(taskToken).withDecisions(decisions));
    }
}
