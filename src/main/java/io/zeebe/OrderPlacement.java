package io.zeebe;

import java.util.HashMap;
import java.util.Map;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.DeploymentEvent;
import io.zeebe.client.api.response.WorkflowInstanceEvent;

public class OrderPlacement {

	public static void main(String[] args) {

		final ZeebeClient client = ZeebeClient.newClientBuilder()
				.brokerContactPoint("127.0.0.1:26500").build();
		System.out.println("Connected.");

		final DeploymentEvent deployment = client.newDeployCommand().addResourceFromClasspath("new-order-process.bpmn")
				.send().join();

		final int version = deployment.getWorkflows().get(0).getVersion();
		System.out.println("Workflow deployed. Version: " + version);

		Map<String, Object> variables = new HashMap<>();
		variables.put("orderId", Constants.ORDER_ID);
		variables.put("orderValue", Constants.ORDER_VALUE_INT_WITH_INSURANCE);
//		variables.put("orderValue", Constants.ORDER_VALUE_STRING);

		final WorkflowInstanceEvent wfInstance = client.newCreateInstanceCommand().bpmnProcessId("new-order-process")
				.latestVersion().variables(variables).send().join();
		final long workflowInstanceKey = wfInstance.getWorkflowInstanceKey();
		System.out.println("Workflow instance created. Key: " + workflowInstanceKey);

		client.close();
		System.out.println("Closed.");
	
		
	}
	
}
