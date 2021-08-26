package net.itskev.messagingframework.proxy;

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
        "rabbitmq-1.rabbitmq-headless.rabbitmq.svc.cluster.local:5672"
    );

    DefaultMessagingService defaultMessagingService = new DefaultMessagingService(addresses, "user", "testee");
    defaultMessagingService.startConsuming("testee2");
    for (int i = 0; i < 1_000_000; i++) {
      defaultMessagingService.sendMessageToExchange("testee2", String.valueOf(i));
    }
  }
}
