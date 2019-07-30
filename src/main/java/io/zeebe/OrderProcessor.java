package io.zeebe;

import java.time.Duration;
import java.util.Map;

import io.zeebe.client.ZeebeClient;

public class OrderProcessor {

	public static void main(String[] args) throws InterruptedException {

		final ZeebeClient client = ZeebeClient.newClientBuilder()
				.brokerContactPoint("127.0.0.1:26500").build();
		System.out.println("Connected.");

//		final JobWorker jobWorker = 
		client.newWorker().jobType("initiate-payment").handler((jobClient, job) -> {
			final Map<String, Object> jobVariables = job.getVariablesAsMap();
			System.out.println("initiate-payment for order id: " + jobVariables.get("orderId"));
			jobClient.newCompleteCommand(job.getKey()).send().join();
		}).open();
		Thread.sleep(1000);

		client.newPublishMessageCommand().messageName("payment-received").correlationKey(Constants.ORDER_ID)
				.timeToLive(Duration.ofMinutes(30)).send().join();
		System.out.println("payment-received for : " + Constants.ORDER_ID);
		
		client.newWorker().jobType("ship-with-insurance").handler((jobClient, job) -> {
			final Map<String, Object> jobVariables = job.getVariablesAsMap();
			System.out.println("ship-with-insurance for order id: " + jobVariables.get("orderId"));
			jobClient.newCompleteCommand(job.getKey()).send().join();
		}).open();
		Thread.sleep(1000);

		client.newWorker().jobType("ship-without-insurance").handler((jobClient, job) -> {
			final Map<String, Object> jobVariables = job.getVariablesAsMap();
			System.out.println("ship-without-insurance for order id: " + jobVariables.get("orderId"));
			jobClient.newCompleteCommand(job.getKey()).send().join();
		}).open();
		Thread.sleep(1000);

		client.close();
		System.out.println("Closed.");
	}
}
