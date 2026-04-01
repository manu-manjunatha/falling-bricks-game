package com.gic.fbg.controller;

import com.gic.fbg.model.Direction;
import com.gic.fbg.service.GameService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class GameWebSocketController {

    private final GameService service;
    private final SimpMessagingTemplate template;

    public GameWebSocketController(GameService service,
                                   SimpMessagingTemplate template) {
        this.service = service;
        this.template = template;
    }

    @MessageMapping("/start")
    public void start() {
        template.convertAndSend("/topic/game", service.startGame());
    }

    @MessageMapping("/restart")
    public void restart() {
        template.convertAndSend("/topic/game", service.restart());
    }

    @MessageMapping("/move")
    public void move(@Payload String direction) {
        template.convertAndSend("/topic/game",
                service.move(Direction.valueOf(direction)));
    }

    @MessageMapping("/pause")
    public void pause() {
        service.togglePause();
    }
}