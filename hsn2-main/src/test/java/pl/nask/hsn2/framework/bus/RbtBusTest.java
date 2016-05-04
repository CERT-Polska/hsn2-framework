/*
 * Copyright (c) NASK, NCSC
 * 
 * This file is part of HoneySpider Network 2.1.
 * 
 * This is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.nask.hsn2.framework.bus;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import pl.nask.hsn2.bus.api.BusException;
import pl.nask.hsn2.bus.api.Destination;
import pl.nask.hsn2.bus.api.Message;
import pl.nask.hsn2.bus.api.endpoint.ConsumeEndPoint;
import pl.nask.hsn2.bus.api.endpoint.ConsumeEndPointHandler;
import pl.nask.hsn2.bus.api.endpoint.FireAndForgetEndPoint;
import pl.nask.hsn2.bus.operations.ObjectResponse;
import pl.nask.hsn2.bus.operations.ObjectResponse.ResponseType;
import pl.nask.hsn2.bus.operations.Operation;
import pl.nask.hsn2.bus.operations.builder.ObjectDataBuilder;
import pl.nask.hsn2.bus.operations.builder.ObjectResponseBuilder;
import pl.nask.hsn2.bus.rabbitmq.endpoint.RbtConsumeEndPoint;
import pl.nask.hsn2.bus.rabbitmq.endpoint.RbtFireAndForgetEndPoint;
import pl.nask.hsn2.bus.serializer.MessageSerializer;
import pl.nask.hsn2.bus.serializer.protobuf.ProtoBufMessageSerializer;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RbtBusTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(RbtBusTest.class);
	
	private final boolean testEnabler = false;
	
	@Test(enabled = testEnabler)
	public void simpleTest() throws InterruptedException, BusException, IOException {
		
	    RbtBusConfiguration busConfig = new RbtBusConfiguration()
    	.setAMQPServerAddress("195.187.238.85")
    	.setAMQPFrameworkLowQueue("serviceQueue")
    	.setServicesNames(new String[]{"S1", "S2"})
    	.setAMQPFrameworkHighQueue("fw:h")
    	.setOsLowQueueName("osLow")
    	.setOsHiQueueName("osHi");

		RbtFrameworkBus bus = new RbtFrameworkBus(busConfig);
		
		
		
		while (true) {
			Thread.sleep(1000);
			ConsumeEndPoint stub = setupStub();
			long id = bus.getObjectStoreConnector().sendObjectStoreData(1, new ObjectDataBuilder().build());
			LOGGER.info("Got obect id={}", id);
		}
	}


	public ConsumeEndPoint setupStub() throws IOException, BusException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("195.187.238.85");
		Connection connection = factory.newConnection();

		final FireAndForgetEndPoint responseEndPoint = new RbtFireAndForgetEndPoint(connection);
		final MessageSerializer<Operation> serializer = new ProtoBufMessageSerializer();
		return new RbtConsumeEndPoint(
				connection,
				new ConsumeEndPointHandler(){
					@Override
					public void handleMessage(Message message) {
						try {
							LOGGER.info("STUB got message {}.", message.getType());
							if ("ObjectRequest".equals(message.getType())) {
								ObjectResponse res =
										new ObjectResponseBuilder(ResponseType.SUCCESS_PUT)
											.addAllObjects(Arrays.asList(6L))
											.build();
								Message respMessage = serializer.serialize(res);
								respMessage.setDestination(message.getReplyTo());
								respMessage.setReplyTo(new Destination(""));
								responseEndPoint.sendNotify(respMessage);
							}
						} catch(Exception ex) {
							LOGGER.error("Error with processing message.");
						}
					}
				},
				"osHi", false, 10);
	}
}
