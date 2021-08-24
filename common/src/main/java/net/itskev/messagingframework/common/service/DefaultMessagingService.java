package net.itskev.messagingframework.common.service;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.impl.DefaultCredentialsProvider;
import lombok.Data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

public class DefaultMessagingService {

  private static final Map<String, Object> arguments = Map.of("x-queue-type", "quorum");

  private final Connection connection;
  private final Queue<LostMessage> queuedMessages = new LinkedList<>();
  private final Channel consumerChannel;
  private Channel publishChannel;

  public DefaultMessagingService() throws IOException, TimeoutException {
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setCredentialsProvider(new DefaultCredentialsProvider("user", "testee"));
    connectionFactory.setNetworkRecoveryInterval(250);

    List<Address> addresses = List.of(new Address("rabbitmq-0.rabbitmq-headless.rabbitmq.svc.cluster.local", 5672),
        new Address("rabbitmq-1.rabbitmq-headless.rabbitmq.svc.cluster.local", 5672));
    connection = connectionFactory.newConnection(addresses);
    consumerChannel = connection.createChannel();
    publishChannel = connection.createChannel();
  }

  public void sendMessage(String queue, String message) {
    if (!queuedMessages.isEmpty()) {
      queuedMessages.add(LostMessage.create(queue, message));
      LostMessage lostMessage = queuedMessages.remove();
      sendMessageToQueue(lostMessage.getQueue(), lostMessage.getMessage());
    } else {
      sendMessageToQueue(queue, message);
    }
  }

  private void sendMessageToQueue(String queue, String message) {
    try {
      reopenPublishChannelIfClosed();
      publishChannel.queueDeclare(queue, true, false, false, arguments);
      publishChannel.exchangeDeclare("test", BuiltinExchangeType.DIRECT);
      publishChannel.queueBind(queue, "test", "");
      publishChannel.basicPublish("test", "", null, message.getBytes(StandardCharsets.UTF_8));
    } catch (IOException | AlreadyClosedException e) {
      queuedMessages.add(LostMessage.create(queue, message));
    }
  }

  public void startConsuming(String queue) {
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      System.out.println(new String(delivery.getBody(), StandardCharsets.UTF_8));
    };
    try {
      publishChannel.queueDeclare(queue, true, false, false, arguments);
      consumerChannel.basicConsume(queue, true, deliverCallback, (consumerTag, sig) -> System.out.println("Shutdown"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void reopenPublishChannelIfClosed() throws IOException {
    if (!publishChannel.isOpen()) {
      publishChannel = connection.createChannel();
    }
  }

  @Data(staticConstructor = "create")
  private static class LostMessage {
    private final String queue;
    private final String message;
  }
}
