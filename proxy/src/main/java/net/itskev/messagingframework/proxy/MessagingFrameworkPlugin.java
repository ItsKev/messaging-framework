package net.itskev.messagingframework.proxy;

import com.rabbitmq.client.BuiltinExchangeType;
import lombok.SneakyThrows;
import net.itskev.messagingframework.common.service.DefaultMessagingService;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.List;

public class MessagingFrameworkPlugin extends Plugin {

  @SneakyThrows
  @Override
  public void onEnable() {
    List<String> addresses = List.of(
        "rabbitmq-0.rabbitmq-headless.rabbitmq.svc.cluster.local:5672",
        "rabbitmq-1.rabbitmq-headless.rabbitmq.svc.cluster.local:5672",
        "rabbitmq-2.rabbitmq-headless.rabbitmq.svc.cluster.local:5672"
    );

    DefaultMessagingService defaultMessagingService = new DefaultMessagingService(addresses, "user", "testee");
    defaultMessagingService.startConsuming("testee2");
    defaultMessagingService.setupExchange("testee", BuiltinExchangeType.DIRECT);
    defaultMessagingService.bindQueueToExchange("testee2", "testee", "");
    for (int i = 0; i < 50_000; i++) {
      Thread.sleep(1);
      defaultMessagingService.sendMessageToExchange("testee", String.valueOf(i));
    }
  }
}
