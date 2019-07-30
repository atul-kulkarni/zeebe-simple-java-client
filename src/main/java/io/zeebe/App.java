package io.zeebe;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.DeploymentEvent;
import io.zeebe.client.api.response.WorkflowInstanceEvent;

/**
 *
 */
public class App {
	
	private static final String ORDER_ID = "4567";

	public static void main(String[] args) {

		final ZeebeClient client = ZeebeClient.newClientBuilder()
				// change the contact point if needed
				.brokerContactPoint("127.0.0.1:26500").build();
		System.out.println("Connected.");

		final DeploymentEvent deployment = client.newDeployCommand().addResourceFromClasspath("new-order-process.bpmn")
				.send().join();

		final int version = deployment.getWorkflows().get(0).getVersion();
		System.out.println("Workflow deployed. Version: " + version);

		Map<String, Object> variables = new HashMap<>();
		variables.put("orderId", ORDER_ID);
//		variables.put("orderValue", "101");
		variables.put("orderValue", 99);

		final WorkflowInstanceEvent wfInstance = client.newCreateInstanceCommand().bpmnProcessId("new-order-process")
				.latestVersion().variables(variables).send().join();
		final long workflowInstanceKey = wfInstance.getWorkflowInstanceKey();
		System.out.println("Workflow instance created. Key: " + workflowInstanceKey);

//		final JobWorker jobWorker = 
		client.newWorker().jobType("initiate-payment").handler((jobClient, job) -> {
			final Map<String, Object> jobVariables = job.getVariablesAsMap();
			System.out.println("Collect money for order id: " + jobVariables.get("orderId"));
			jobClient.newCompleteCommand(job.getKey()).send().join();
		}).open();

		client.newPublishMessageCommand().messageName("payment-received").correlationKey(ORDER_ID)
				.timeToLive(Duration.ofMinutes(30)).send().join();

		client.newWorker().jobType("ship-with-insurance").handler((jobClient, job) -> {
			final Map<String, Object> jobVariables = job.getVariablesAsMap();
			System.out.println("Collect money for order id: " + jobVariables.get("orderId"));
			jobClient.newCompleteCommand(job.getKey()).send().join();
		}).open();

		client.newWorker().jobType("ship-without-insurance").handler((jobClient, job) -> {
			final Map<String, Object> jobVariables = job.getVariablesAsMap();
			System.out.println("Collect money for order id: " + jobVariables.get("orderId"));
			jobClient.newCompleteCommand(job.getKey()).send().join();
		}).open();

		client.close();
		System.out.println("Closed.");
	}
}
