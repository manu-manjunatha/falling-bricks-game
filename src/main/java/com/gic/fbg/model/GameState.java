package com.gic.fbg.model;

import java.util.*;

public class GameState {
    public int[][] board = new int[20][10];
    public Brick currentBrick;
    public boolean gameOver = false;
    public int score = 0;
    public int level = 1;

    public List<Integer> clearedRows = new ArrayList<>();
}