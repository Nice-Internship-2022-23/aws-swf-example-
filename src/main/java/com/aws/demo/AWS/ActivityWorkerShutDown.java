package com.aws.demo.AWS;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClientBuilder;
import com.amazonaws.services.simpleworkflow.model.ActivityTask;
import com.amazonaws.services.simpleworkflow.model.PollForActivityTaskRequest;
import com.amazonaws.services.simpleworkflow.model.RespondActivityTaskCompletedRequest;
import com.amazonaws.services.simpleworkflow.model.RespondActivityTaskFailedRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.aws.demo.Utils.HelloType;

public class ActivityWorkerShutDown {
    
    private static final AmazonSimpleWorkflow swf = AmazonSimpleWorkflowClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();

    private static CountDownLatch waitForTermination = new CountDownLatch(1);
    private static boolean terminate = false;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                try {
                    terminate = true;
                    System.out.println("Waiting for the current poll request to return before shutting down.");
                    waitForTermination.await(60, TimeUnit.SECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        try{
            pollAndExecute();
        }
        finally{
            waitForTermination.countDown();
        }
    }

    private static void pollAndExecute() {
        while(!terminate){
            System.out.println("Polling for an activity task from tasklist '" + HelloType.TASKLIST + "' in the Domain '"
                    + HelloType.DOMAIN + "'.");

            ActivityTask task = swf.pollForActivityTask(new PollForActivityTaskRequest().withDomain(HelloType.DOMAIN)
                    .withTaskList(new TaskList().withName(HelloType.TASKLIST)));

            String taskToken = task.getTaskToken();

            if (taskToken != null) {
                String result = null;
                Throwable error = null;

                try {
                    System.out.println("Executing the activity task with input '" + task.getInput() + "'.");
                    result = sayHello(task.getInput());
                } catch (Throwable e) {
                    error = e;
                }

                if (error == null) {
                    System.out.println("Activity task succeeded with result '" + result + "'.");
                    swf.respondActivityTaskCompleted(
                            new RespondActivityTaskCompletedRequest().withTaskToken(taskToken).withResult(result));
                } else {
                    System.out.println("Activity task failed with error '" + error.getClass().getSimpleName() + "'.");

                    swf.respondActivityTaskFailed(new RespondActivityTaskFailedRequest().withTaskToken(taskToken)
                            .withReason(error.getClass().getSimpleName()).withDetails(error.getMessage()));
                }
            }
        }
    }

    private static String sayHello(String input) throws Throwable {
        return "Hello, " + input + "!";
    }
}
