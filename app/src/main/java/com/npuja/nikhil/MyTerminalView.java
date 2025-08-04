package com.npuja.nikhil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;

public class MyTerminalView extends View {

    private Paint paint = new Paint();
    private int charWidth, charHeight;
    private int cols = 80;
    private int rows = 40;
    private int cursorX = 0, cursorY = 0;

    private char[][] screen = new char[rows][cols];
    private int[][] colors = new int[rows][cols];
    private int currentColor = Color.GREEN;

    public MyTerminalView(Context context) {
        super(context);
        init();
    }
    public MyTerminalView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
}

    private void init() {
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setTextSize(32);
        paint.setAntiAlias(true);
        paint.setColor(Color.GREEN);

        Paint.FontMetrics fm = paint.getFontMetrics();
        charHeight = (int) (fm.descent - fm.ascent);
        charWidth = (int) paint.measureText("W");

        clearScreen();
    }

    private void clearScreen() {
        for (int y = 0; y < rows; y++) {
            Arrays.fill(screen[y], ' ');
            Arrays.fill(colors[y], Color.GREEN);
        }
        cursorX = 0;
        cursorY = 0;
    }

    public void appendOutput(String raw) {
        StringBuilder ansiBuffer = new StringBuilder();
        boolean inAnsi = false;

        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);

            if (inAnsi) {
                ansiBuffer.append(ch);
                if (ch == 'm' || ch == 'H' || ch == 'J') {
                    handleAnsi(ansiBuffer.toString());
                    ansiBuffer.setLength(0);
                    inAnsi = false;
                }
            } else {
                if (ch == 27 && i + 1 < raw.length() && raw.charAt(i + 1) == '[') {
                    inAnsi = true;
                    ansiBuffer.setLength(0);
                    ansiBuffer.append("[");
                    i++; // Skip [
                } else if (ch == '\n') {
                    cursorY++;
                    cursorX = 0;
                } else {
                    if (cursorX >= cols) {
                        cursorX = 0;
                        cursorY++;
                    }
                    if (cursorY >= rows) {
                        scrollUp();
                        cursorY = rows - 1;
                    }
                    screen[cursorY][cursorX] = ch;
                    colors[cursorY][cursorX] = currentColor;
                    cursorX++;
                }
            }
        }

        postInvalidate();
    }

    private void scrollUp() {
        for (int y = 1; y < rows; y++) {
            screen[y - 1] = screen[y].clone();
            colors[y - 1] = colors[y].clone();
        }
        Arrays.fill(screen[rows - 1], ' ');
        Arrays.fill(colors[rows - 1], currentColor);
    }

    private void handleAnsi(String code) {
        if (code.endsWith("m")) {
            // Color codes
            String c = code.substring(0, code.length() - 1);
            for (String part : c.split(";")) {
                try {
                    int val = Integer.parseInt(part.replaceAll("\\D", ""));
                    switch (val) {
                        case 30: currentColor = Color.BLACK; break;
                        case 31: currentColor = Color.RED; break;
                        case 32: currentColor = Color.GREEN; break;
                        case 33: currentColor = Color.YELLOW; break;
                        case 34: currentColor = Color.BLUE; break;
                        case 35: currentColor = Color.MAGENTA; break;
                        case 36: currentColor = Color.CYAN; break;
                        case 37: currentColor = Color.WHITE; break;
                        case 0:  currentColor = Color.GREEN; break;
                    }
                } catch (NumberFormatException e) {}
            }
        } else if (code.endsWith("H")) {
            // Cursor position
            String[] pos = code.substring(0, code.length() - 1).split(";");
            try {
                int row = Integer.parseInt(pos[0]) - 1;
                int col = Integer.parseInt(pos[1]) - 1;
                cursorY = Math.max(0, Math.min(row, rows - 1));
                cursorX = Math.max(0, Math.min(col, cols - 1));
            } catch (Exception e) {}
        } else if (code.equals("[2J")) {
            clearScreen();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                paint.setColor(colors[y][x]);
                canvas.drawText(Character.toString(screen[y][x]), x * charWidth + 5, (y + 1) * charHeight, paint);
            }
        }
    }
}