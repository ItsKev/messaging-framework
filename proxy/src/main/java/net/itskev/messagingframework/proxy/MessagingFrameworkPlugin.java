package net.itskev.messagingframework.proxy;

import lombok.SneakyThrows;
import net.itskev.messagingframework.common.service.DefaultMessagingService;
import net.md_5.bungee.api.plugin.Plugin;

public class MessagingFrameworkPlugin extends Plugin {

  @SneakyThrows
  @Override
  public void onEnable() {
    DefaultMessagingService defaultMessagingService = new DefaultMessagingService();
    defaultMessagingService.startConsuming("testee2");
    for (int i = 0; i < 1_000_000; i++) {
      defaultMessagingService.sendMessage("testee2", String.valueOf(i));
    }
  }
}
