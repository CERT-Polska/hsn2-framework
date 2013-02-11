package pl.nask.hsn2.framework.bus.monitoring;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.bus.api.Destination;
import pl.nask.hsn2.bus.api.Message;
import pl.nask.hsn2.bus.operations.Operation;
import pl.nask.hsn2.bus.operations.TaskAccepted;
import pl.nask.hsn2.bus.operations.TaskCompleted;
import pl.nask.hsn2.bus.operations.TaskError;
import pl.nask.hsn2.bus.operations.TaskErrorReasonType;
import pl.nask.hsn2.bus.operations.TaskRequest;
import pl.nask.hsn2.bus.rabbitmq.RbtDestination;
import pl.nask.hsn2.bus.serializer.MessageSerializer;
import pl.nask.hsn2.bus.serializer.MessageSerializerException;
import pl.nask.hsn2.bus.serializer.protobuf.ProtoBufMessageSerializer;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class BusMsgInjection {
	private static final String DEFAULT_CONTENT_TYPE = "application/hsn2+protobuf";
	private static final String RBT_HOST = "localhost";
	private static final String RBT_MAIN_EXCHANGE_NAME = "main";
	private static final String RBT_FRAMEWORK_QUEUE = "fw:l";
	private static final Logger LOG = LoggerFactory.getLogger(BusMsgInjection.class);
	private static final MessageSerializer<Operation> DEFAULT_SERIALIZER = new ProtoBufMessageSerializer();

	public static void main(String[] args) throws Exception {
		new BusMsgInjection().start();
	}

	private void sendOut(Operation operation, Channel channel, Destination dest, String replyTo) {
		try {
			// Prepare message.
			Message message = DEFAULT_SERIALIZER.serialize(operation);
			message.setDestination(dest);
			message.setCorrelationId(null);
			message.setReplyTo(new RbtDestination(RBT_MAIN_EXCHANGE_NAME, replyTo));

			// Properties below uses only service name (String, not Destination) as replyTo parameter.
			String replyToQueueName = message.getReplyTo().getService();

			HashMap<String, Object> headers = new HashMap<String, Object>();
			// headers.put("x-retries", message.getRetries());
			BasicProperties.Builder propertiesBuilder = new BasicProperties.Builder().headers(headers).contentType(DEFAULT_CONTENT_TYPE)
					.replyTo(replyToQueueName).type(message.getType());

			// setup correct correlation id if provided
			if (message.getCorrelationId() != null && !"".equals(message.getCorrelationId())) {
				propertiesBuilder.correlationId(message.getCorrelationId());
			}
			String destinationRoutingKey = ((RbtDestination) (message.getDestination())).getService();
			String destinationExchange = ((RbtDestination) (message.getDestination())).getExchange();
			channel.basicPublish(destinationExchange, destinationRoutingKey, propertiesBuilder.build(), message.getBody());
		} catch (MessageSerializerException e) {
			LOG.error("Serialization error.", e);
		} catch (IOException e) {
			LOG.error("IO error.", e);
		}
	}

	void start() throws Exception {
		ConnectionFactory cf = new ConnectionFactory();
		cf.setHost(RBT_HOST);
		Connection c = cf.newConnection();
		Channel ch = c.createChannel();
		ch.exchangeDeclare(RBT_MAIN_EXCHANGE_NAME, "fanout");

		Destination toFramework = new RbtDestination(RBT_MAIN_EXCHANGE_NAME, RBT_FRAMEWORK_QUEUE);

		TaskAccepted ta = new TaskAccepted(1111, 666);
		sendOut(ta, ch, toFramework, "");
		TaskCompleted tc = new TaskCompleted(1112, 666);
		sendOut(tc, ch, toFramework, "");
		TaskError te = new TaskError(1113, 666, TaskErrorReasonType.DEFUNCT);
		sendOut(te, ch, toFramework, "");

		Thread.sleep(1000);
		
		Destination toReporter = new RbtDestination(RBT_MAIN_EXCHANGE_NAME, "srv-reporter:l");
		TaskRequest tr = new TaskRequest(1114, 666, 1);
		sendOut(tr, ch, toReporter, RBT_FRAMEWORK_QUEUE);

		Destination toFileFeeder = new RbtDestination(RBT_MAIN_EXCHANGE_NAME, "srv-feeder-list:l");
		TaskRequest tr2 = new TaskRequest(1115, 666, 1);
		sendOut(tr2, ch, toFileFeeder, RBT_FRAMEWORK_QUEUE);

		ch.close();
		c.close();
	}
}
