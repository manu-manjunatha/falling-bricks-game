package com.gic.fbg.service;

import com.gic.fbg.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameService {

    private GameState state = new GameState();
    private final Random random = new Random();

    private boolean paused = false;
    private int speed = 700;

    private final int[][][] SHAPES = {
            {{1,1,1}},
            {{1},{1},{1}},
            {{1,0},{1,1}}
    };

    public GameState startGame() {
        state = new GameState();
        paused = false;
        speed = 700;
        spawnBrick();
        return state;
    }

    public GameState restart() {
        return startGame();
    }

    public GameState move(Direction dir) {
        if (state.gameOver || paused) return state;

        switch (dir) {
            case LEFT -> moveBrick(-1, 0);
            case RIGHT -> moveBrick(1, 0);
            case DROP -> dropBrick();
            case ROTATE -> rotateBrick();
            case FAST_DROP -> fastDrop();
        }
        return state;
    }

    public void togglePause() { paused = !paused; }
    public boolean isPaused() { return paused; }
    public int getSpeed() { return speed; }

    private void spawnBrick() {
        state.currentBrick = new Brick(SHAPES[random.nextInt(SHAPES.length)]);
        state.currentBrick.x = 4;
        state.currentBrick.y = 0;

        if (collision()) state.gameOver = true;
    }

    private void moveBrick(int dx, int dy) {
        Brick b = state.currentBrick;
        b.x += dx;
        b.y += dy;

        if (collision()) {
            b.x -= dx;
            b.y -= dy;
        }
    }

    private void rotateBrick() {
        Brick b = state.currentBrick;
        int[][] original = b.shape;

        b.rotate();

        int[] kicks = {0, 1, -1, 2, -2};

        for (int dx : kicks) {
            b.x += dx;
            if (!collision()) return;
            b.x -= dx;
        }

        b.shape = original;
    }

    private void dropBrick() {
        Brick b = state.currentBrick;
        b.y++;

        if (collision()) {
            b.y--;
            merge();
            clearLines();
            spawnBrick();
        }
    }

    private void fastDrop() {
        while (!collision()) {
            state.currentBrick.y++;
        }
        state.currentBrick.y--;
        merge();
        clearLines();
        spawnBrick();
    }

    private boolean collision() {
        Brick b = state.currentBrick;

        for (int i = 0; i < b.shape.length; i++) {
            for (int j = 0; j < b.shape[i].length; j++) {

                if (b.shape[i][j] == 0) continue;

                int x = b.x + j;
                int y = b.y + i;

                if (x < 0 || x >= 10 || y >= 20) return true;
                if (y >= 0 && state.board[y][x] == 1) return true;
            }
        }
        return false;
    }

    private void merge() {
        Brick b = state.currentBrick;

        for (int i = 0; i < b.shape.length; i++) {
            for (int j = 0; j < b.shape[i].length; j++) {
                if (b.shape[i][j] == 1) {
                    state.board[b.y + i][b.x + j] = 1;
                }
            }
        }
    }

    private void clearLines() {
        List<Integer> clearedRows = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            boolean full = true;

            for (int j = 0; j < 10; j++) {
                if (state.board[i][j] == 0) {
                    full = false;
                    break;
                }
            }

            if (full) {
                clearedRows.add(i);

                for (int k = i; k > 0; k--) {
                    state.board[k] = state.board[k - 1].clone();
                }
            }
        }

        if (!clearedRows.isEmpty()) {
            state.score += clearedRows.size() * 100;
            state.level = state.score / 500 + 1;
            speed = Math.max(150, 700 - state.level * 50);
            state.clearedRows = clearedRows;
        } else {
            state.clearedRows = new ArrayList<>();
        }
    }
}