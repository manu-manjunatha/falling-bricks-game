package com.gic.fbg.controller;

import com.gic.fbg.model.Direction;
import com.gic.fbg.model.GameState;
import com.gic.fbg.service.GameService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
@CrossOrigin
public class GameController {

    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    @PostMapping("/start")
    public GameState start() {
        return service.startGame();
    }

    @PostMapping("/move")
    public GameState move(@RequestParam String direction) {
        return service.move(Direction.valueOf(direction.toUpperCase()));
    }

    @PostMapping("/reset")
    public GameState reset() {
        return service.restart();
    }
}