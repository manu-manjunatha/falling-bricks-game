package com.gic.fbg.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class GameLoop {

    private final GameService service;
    private final org.springframework.messaging.simp.SimpMessagingTemplate template;

    public GameLoop(GameService service,
                    org.springframework.messaging.simp.SimpMessagingTemplate template) {
        this.service = service;
        this.template = template;
    }

    @PostConstruct
    public void startLoop() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(service.getSpeed());

                    if (!service.isPaused()) {
                        var state = service.move(com.gic.fbg.model.Direction.DROP);
                        template.convertAndSend("/topic/game", state);
                    }
                } catch (Exception ignored) {}
            }
        }).start();
    }
}