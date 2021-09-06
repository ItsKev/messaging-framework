package net.itskev.messagingframework.common.service;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import lombok.Data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class DefaultMessagingService {

  private static final Map<String, Object> arguments = Map.of("x-queue-type", "quorum");

  private final Connection connection;
  private final Queue<LostMessage> queuedMessages = new LinkedList<>();
  private final Channel consumerChannel;
  private Channel publishChannel;
  private int count;

  public DefaultMessagingService(List<String> addresses, String username, String password)
      throws IOException, TimeoutException {
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setUsername(username);
    connectionFactory.setPassword(password);
    connectionFactory.setNetworkRecoveryInterval(250);

    connection = connectionFactory.newConnection(mapAddresses(addresses));
    System.out.println(connection.getAddress());
    consumerChannel = connection.createChannel();
    publishChannel = connection.createChannel();
  }

  private List<Address> mapAddresses(List<String> addresses) {
    return addresses.stream()
        .map(address -> {
          String[] splitAddress = address.split(":");
          return new Address(splitAddress[0], Integer.parseInt(splitAddress[1]));
        })
        .collect(Collectors.toList());
  }

  public void setupExchange(String exchange, BuiltinExchangeType exchangeType) throws IOException {
    reopenPublishChannelIfClosed();
    publishChannel.exchangeDeclare(exchange, exchangeType);
  }

  public void bindQueueToExchange(String queue, String exchange, String routingKey) throws IOException {
    reopenPublishChannelIfClosed();
    publishChannel.queueDeclare(queue, true, false, false, arguments);
    publishChannel.queueBind(queue, exchange, routingKey);
  }

  public void sendMessageToExchange(String exchange, String message) {
    sendMessageToExchange(exchange, "", message);
  }

  public void sendMessageToExchange(String exchange, String routingKey, String message) {
    if (!queuedMessages.isEmpty()) {
      queuedMessages.add(LostMessage.create(exchange, message));
      LostMessage lostMessage = queuedMessages.remove();
      sendMessage(lostMessage.getQueue(), routingKey, lostMessage.getMessage());
    } else {
      sendMessage(exchange, routingKey, message);
    }
  }

  private void sendMessage(String exchange, String routingKey, String message) {
    try {
      reopenPublishChannelIfClosed();
      publishChannel.basicPublish(exchange, routingKey, null, message.getBytes(StandardCharsets.UTF_8));
    } catch (IOException | AlreadyClosedException e) {
      queuedMessages.add(LostMessage.create(exchange, message));
    }
  }

  public void startConsuming(String queue) throws IOException {
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      count++;
      if (count % 1000 == 0 || count > 45_000) {
        System.out.println(count);
      }
    };
    publishChannel.queueDeclare(queue, true, false, false, arguments);
    consumerChannel.basicConsume(queue, true, deliverCallback, (consumerTag, sig) -> System.out.println("Shutdown"));
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
