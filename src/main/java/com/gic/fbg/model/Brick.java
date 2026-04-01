package com.gic.fbg.model;

public class Brick {
    public int[][] shape;
    public int x = 4;
    public int y = 0;

    public Brick(int[][] shape) {
        this.shape = shape;
    }

    public void rotate() {
        int rows = shape.length;
        int cols = shape[0].length;

        int[][] rotated = new int[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                rotated[j][rows - 1 - i] = shape[i][j];
            }
        }
        shape = rotated;
    }
}