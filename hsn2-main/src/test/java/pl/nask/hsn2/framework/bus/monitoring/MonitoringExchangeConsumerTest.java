package pl.nask.hsn2.framework.bus.monitoring;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.bus.api.Message;
import pl.nask.hsn2.bus.operations.JobFinished;
import pl.nask.hsn2.bus.operations.JobFinishedReminder;
import pl.nask.hsn2.bus.operations.JobStarted;
import pl.nask.hsn2.bus.operations.Operation;
import pl.nask.hsn2.bus.operations.TaskAccepted;
import pl.nask.hsn2.bus.operations.TaskCompleted;
import pl.nask.hsn2.bus.operations.TaskError;
import pl.nask.hsn2.bus.operations.TaskRequest;
import pl.nask.hsn2.bus.rabbitmq.RbtDestination;
import pl.nask.hsn2.bus.serializer.MessageSerializer;
import pl.nask.hsn2.bus.serializer.MessageSerializerException;
import pl.nask.hsn2.bus.serializer.protobuf.ProtoBufMessageSerializer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

public class MonitoringExchangeConsumerTest {
	public static void main(String[] args) throws Exception {
		new MonitoringExchangeConsumerTest().start();
	}

	private static final boolean AUTO_ACK = true;
	private static final String RBT_HOST = "localhost";
	private static final String RBT_EXCHANGE_NAME = "notify";
	private static final Logger LOG = LoggerFactory.getLogger(MonitoringExchangeConsumerTest.class);
	private static final MessageSerializer<Operation> DEFAULT_SERIALIZER = new ProtoBufMessageSerializer();

	/**
	 * key string "jobId-taskId", value "serviceName"
	 */
	private HashMap<String, String> jobsAndTasks = new HashMap<String, String>();

	void start() throws Exception {

		ConnectionFactory cf = new ConnectionFactory();
		cf.setHost(RBT_HOST);
		Connection c = cf.newConnection();
		Channel ch = c.createChannel();
		ch.exchangeDeclare(RBT_EXCHANGE_NAME, "fanout");

		String queueName = ch.queueDeclare().getQueue();
		ch.queueBind(queueName, RBT_EXCHANGE_NAME, "");
		LOG.info("waiting for message");

		QueueingConsumer consumer = new QueueingConsumer(ch);
		ch.basicConsume(queueName, AUTO_ACK, consumer);
		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			String deliveryInfo = getInfoAboutDelivery(delivery);
			System.out.println(deliveryInfo);
		}
	}

	private String getInfoAboutDelivery(Delivery delivery) {
		String routingKey = delivery.getEnvelope().getRoutingKey();
		String exchange = delivery.getEnvelope().getExchange();
		String type = delivery.getProperties().getType();

		StringBuilder info = new StringBuilder();
		info.append(getCommonInfoAboutDelivery(exchange, routingKey, type));
		try {
			Operation messageOperation = getOperation(exchange, routingKey, type, delivery.getBody());
			info.append(getDetailedInfoAboutDelivery(messageOperation, routingKey));
		} catch (MessageSerializerException e) {
			e.printStackTrace();
			info.append("Couldn't serialize delivery");
		}
		return info.toString();
	}

	private Operation getOperation(String exchange, String routingKey, String type, byte[] body) throws MessageSerializerException {
		Message message = new Message(type, body, new RbtDestination(exchange, routingKey));
		return DEFAULT_SERIALIZER.deserialize(message);
	}

	private StringBuilder getCommonInfoAboutDelivery(String exchange, String routingKey, String type) {
		StringBuilder sb = new StringBuilder();

		// add date
		sb.append(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date())).append(" ");

		// add type
		sb.append("[").append(type).append("] ");

		return sb;
	}

	String getDetailedInfoAboutDelivery(Operation operation, String service) {
		if (operation instanceof TaskRequest) {
			return getTaskInfo((TaskRequest) operation, service);
		} else if (operation instanceof JobStarted) {
			return getTaskInfo((JobStarted) operation);
		} else if (operation instanceof JobFinished) {
			return getTaskInfo((JobFinished) operation);
		} else if (operation instanceof JobFinishedReminder) {
			return getTaskInfo((JobFinishedReminder) operation);
		} else if (operation instanceof TaskAccepted) {
			return getTaskInfo((TaskAccepted) operation);
		} else if (operation instanceof TaskCompleted) {
			return getTaskInfo((TaskCompleted) operation);
		} else if (operation instanceof TaskError) {
			return getTaskInfo((TaskError) operation);
		} else {
			return "";
		}
	}

	private String getTaskInfo(TaskRequest task, String service) {
		jobsAndTasks.put(task.getJob() + "-" + task.getTaskId(), getServiceNameFromQueueName(service));
		return String.format("(send to %s) job=%d,task=%d", getServiceNameFromQueueName(getServiceNameFromQueueName(service)),
				task.getJob(), task.getTaskId());
	}

	private String getTaskInfo(JobStarted task) {
		return String.format("job=%d", task.getJobId());
	}

	private String getTaskInfo(JobFinished task) {
		return String.format("job=%d, status=%s", task.getJobId(), task.getStatus());
	}

	private String getTaskInfo(JobFinishedReminder task) {
		return String.format("job=%d, status=%s, offendedTask=%d", task.getJobId(), task.getStatus(), task.getOffendingTask());
	}

	private String getTaskInfo(TaskAccepted task) {
		String service = jobsAndTasks.get(task.getJobId() + "-" + task.getTaskId());
		return String.format("(info from %s) job=%d, task=%d", service, task.getJobId(), task.getTaskId());
	}

	private String getTaskInfo(TaskError task) {
		String service = jobsAndTasks.get(task.getJobId() + "-" + task.getTaskId());
		return String.format("(info from %s) job=%d, task=%d, reason=%s, desc=%s", service, task.getJobId(), task.getTaskId(),
				task.getReason(), task.getDescription());
	}

	private String getTaskInfo(TaskCompleted task) {
		String taskIdentifier = task.getJobId() + "-" + task.getTaskId();
		String service = jobsAndTasks.get(taskIdentifier);
		jobsAndTasks.remove(taskIdentifier);
		String warnings;
		if (task.getWarnings().isEmpty()) {
			warnings = "{no warn}";
		} else {
			StringBuilder sb = new StringBuilder("{");
			for (String w : task.getWarnings()) {
				sb.append(w).append(",");
			}
			warnings = sb.append("}").toString();
		}
		String objects;
		if (task.getObjects().isEmpty()) {
			objects = "{no obj}";
		} else {
			StringBuilder sb = new StringBuilder("{");
			for (Long o : task.getObjects()) {
				sb.append(o).append(",");
			}
			objects = sb.append("}").toString();
		}
		return String.format("(info from %s) job=%d, task=%d, warn=%s, obj=%s", service, task.getJobId(), task.getTaskId(), warnings,
				objects);
	}

	private String getServiceNameFromQueueName(String queueName) {
		int start = 0;
		int end = queueName.length();
		if (queueName.startsWith("srv-")) {
			start += 4;
		}
		if (queueName.endsWith(":l") || queueName.endsWith(":h")) {
			end -= 2;
		}
		return queueName.substring(start, end);
	}
}
