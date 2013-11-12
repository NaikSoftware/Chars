/**
 * Game create for competition on http://annimon.com
 *
 * Copyright (C) 2012, 2013 NaikSoftware
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ua.naiksoftware.chars;

import android.graphics.*;
import android.util.Log;
import android.view.SurfaceHolder;
import java.util.ArrayList;
import ua.naiksoftware.chars.games.*;

/**
 *
 * @author Naik
 */
public class GameManager extends Thread {

    private static final String tag = "GameManager";
    private SurfaceHolder holder;
    private boolean running = false;
    private final int width, height;
    private int inRow, inColumn;
    private int size;
    private int sizeHead, skipHead;
    private char[][] gameArea;
    private Bitmap blue, red, black, green, slow, surprise;
    private Bitmap headLeft1, headUp1, headRight1, headDown1;
    private Bitmap headLeft2, headUp2, headRight2, headDown2;

    public GameManager(SurfaceHolder holder, int w, int h, int inRowParam, int inColumnParam, int sizeParam) {
        // Log.d(tag, "constructor");
        // Log.d(tag, "column: " + inColumnParam + ", row: " + inRowParam + ", w: " + w + ", h: " + h);
        this.holder = holder;
        width = w;
        height = h;
        inRow = inRowParam;
        inColumn = inColumnParam;
        size = sizeParam;
        sizeHead = size * 2;
        skipHead = (int) (sizeHead * 0.25f);// смещение координат головы
        createElems();
    }

    public void setArea(char[][] gameAreaParam) {
        gameArea = gameAreaParam;
    }

    public void setRunning(boolean b) {
        // Log.d(tag, "setRunning " + b);
        running = b;
    }

    @Override
    public void run() {
        //Log.d(tag, "run");
        long startFps = System.currentTimeMillis();
        int fps_count = 0, fps = 0;
        Paint paintFps = new Paint();
        paintFps.setColor(Color.BLACK);
        int fpsHeight = (int) paintFps.getTextSize();
        restart:
        while (running) {
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                synchronized (holder) {
                    if (canvas == null) {
                        continue restart;
                    }
                    drawGame(canvas);
                    canvas.drawText(String.valueOf(fps), 5, fpsHeight, paintFps);
                    Thread.sleep(5);
                    if ((System.currentTimeMillis() - startFps) < 1000) {// секунда еще не прошла
                        fps_count++;
                    } else {
                        fps = fps_count;
                        fps_count = 0;
                        startFps = System.currentTimeMillis();
                    }
                }
            } catch (Exception ex) {
                Log.e(tag, "run()", ex);
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    private void drawGame(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (gameArea != null) {
            for (int j = 0; j < inColumn; j++) {
                for (int i = 0; i < inRow; i++) {
                    switch (gameArea[j][i]) {
                        case EngineActivity.FON:
                            break;
                        case SnakeSingleActivity.BLOCK:
                            canvas.drawBitmap(black, i * size, j * size, null);
                            break;
                        case SnakeSingleActivity.EAT:
                            canvas.drawBitmap(green, i * size, j * size, null);
                            break;
                        case SnakeSingleActivity.SNAKE_1:
                            canvas.drawBitmap(blue, i * size, j * size, null);
                            break;
                        case SnakeSingleActivity.HEAD_LEFT:
                            canvas.drawBitmap(headLeft1, i * size - skipHead, j * size - skipHead, null);
                            break;
                        case SnakeSingleActivity.HEAD_UP:
                            canvas.drawBitmap(headUp1, i * size - skipHead, j * size - skipHead, null);
                            break;
                        case SnakeSingleActivity.HEAD_RIGHT:
                            canvas.drawBitmap(headRight1, i * size - skipHead, j * size - skipHead, null);
                            break;
                        case SnakeSingleActivity.HEAD_DOWN:
                            canvas.drawBitmap(headDown1, i * size - skipHead, j * size - skipHead, null);
                            break;
                        case SnakeSingleActivity.SURPRISE:
                            canvas.drawBitmap(surprise, i * size, j * size, null);
                            break;
                        case SnakeSingleActivity.SLOW:
                            canvas.drawBitmap(slow, i * size, j * size, null);
                            break;
                    }
                }
            }
        }
    }

    private static Path getPolygon(int corners, int radius, int x, int y) {
        float to_x, to_y;
        Path path = new Path();
        int angle = 360 / corners;
        path.moveTo(x + radius, y);
        for (int j = 1; j < corners; j++) {
            to_x = (float) (x + radius * Math.cos(Math.toRadians(j * angle)));
            to_y = (float) (y + radius * Math.sin(Math.toRadians(j * angle)));
            path.lineTo(to_x, to_y);
        }
        return path;
    }

    private void createElems() {
        Matrix matrix = new Matrix();
        Paint paint = new Paint(); // краска для фона елементов
        Paint paint2 = new Paint();// краска для прочих линий
        paint2.setTextSize(size);
        paint2.setColor(Color.BLACK);
        paint2.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        RectF rect = new RectF();
        rect.set(0, 0, size, size);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        /*black*/
        paint.setShader(new LinearGradient(0, 0, size, size, Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect, 2, 2, paint);
        black = Bitmap.createBitmap(bitmap);
        /*blue*/
        paint.setShader(new RadialGradient(size / 2, size / 2, size / 2, Color.WHITE, Color.BLUE, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect, 2, 2, paint);
        blue = Bitmap.createBitmap(bitmap);
        /*red*/
        swapTwoChannels(bitmap, Color.BLUE, Color.RED);
        red = Bitmap.createBitmap(bitmap);
        /*green*/
        swapTwoChannels(bitmap, Color.RED, Color.GREEN);
        green = Bitmap.createBitmap(bitmap);
        /*headUp1*/
        bitmap = Bitmap.createBitmap(sizeHead, sizeHead, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawArc(new RectF(sizeHead / (-2), 0, sizeHead / 2, sizeHead / 2), 0, -180, false, paint2);
        canvas.drawArc(new RectF(sizeHead / 2, 0, sizeHead + (sizeHead / 2), sizeHead / 2), 0, -180, false, paint2);
        paint.setShader(new RadialGradient(sizeHead / 2, sizeHead / 2, sizeHead / 2, Color.WHITE, Color.BLUE, Shader.TileMode.CLAMP));
        canvas.drawCircle(sizeHead / 2, sizeHead / 2, size * 0.7f, paint);
        canvas.drawCircle(sizeHead / 3, sizeHead / 2, 3, paint2);
        canvas.drawCircle(sizeHead / 3 * 2, sizeHead / 2, 3, paint2);
        headUp1 = Bitmap.createBitmap(bitmap);
        /*headRight1*/
        matrix.preRotate(90, sizeHead / 2, sizeHead / 2);
        headRight1 = Bitmap.createBitmap(bitmap, 0, 0, sizeHead, sizeHead, matrix, false);
        /*headDown1*/
        matrix.preRotate(90);
        headDown1 = Bitmap.createBitmap(bitmap, 0, 0, sizeHead, sizeHead, matrix, false);
        /*headLeft1*/
        matrix.preRotate(90);
        headLeft1 = Bitmap.createBitmap(bitmap, 0, 0, sizeHead, sizeHead, matrix, false);
        /*headLeft2*/
        swapTwoChannels(bitmap, Color.BLUE, Color.RED);
        headLeft2 = Bitmap.createBitmap(bitmap);
        /*headUp2*/
        matrix.preRotate(90);
        headUp2 = Bitmap.createBitmap(bitmap, 0, 0, sizeHead, sizeHead, matrix, false);
        /*headRight2*/
        matrix.preRotate(90);
        headRight2 = Bitmap.createBitmap(bitmap, 0, 0, sizeHead, sizeHead, matrix, false);
        /*headDown2*/
        matrix.preRotate(90);
        headDown2 = Bitmap.createBitmap(bitmap, 0, 0, sizeHead, sizeHead, matrix, false);
        /*surprize*/
        bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint.setShader(null);
        paint.setColor(0xFFFF0000);
        canvas.drawPath(getPolygon(6, size / 2, size / 2, size / 2), paint);
        paint2.setColor(Color.WHITE);
        canvas.drawText("+", size / 4, size / 5 * 4, paint2);
        surprise = Bitmap.createBitmap(bitmap);
        /*slow*/
        paint.setColor(0xFFFFCCCC);
        canvas.drawPath(getPolygon(6, size / 2, size / 2, size / 2), paint);
        paint2.setColor(Color.BLACK);
        canvas.drawText("-", size / 4, size / 5 * 4, paint2);
        slow = Bitmap.createBitmap(bitmap);
    }

    private static void swapTwoChannels(Bitmap bitmap, int oneCh, int twoCh) {
        //Log.d(tag, "replaceColor");
        ArrayList<int[]> sort = new ArrayList<int[]>();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int one[] = new int[2];
        int two[] = new int[2];
        int three[] = new int[2];
        one[0] = oneCh == Color.RED ? 16 : oneCh == Color.GREEN ? 8 : 0;
        two[0] = twoCh == Color.RED ? 16 : twoCh == Color.GREEN ? 8 : 0;
        three[0] = 24 - (one[0] + two[0]);
        sort.add(0, one[0] == 16 ? one : two[0] == 16 ? two : three);
        sort.add(1, one[0] == 8 ? one : two[0] == 8 ? two : three);
        sort.add(2, one[0] == 0 ? one : two[0] == 0 ? two : three);
        int pixel, tmp;
        //Log.d(tag, "oneSkip="+one[0]+", twoSkip="+two[0]+", threeSkip="+three[0]);
        for (int i = 0; i < pixels.length; i++) {
            pixel = pixels[i];
            one[1] = (pixel >> one[0]) & 0xFF;
            two[1] = (pixel >> two[0]) & 0xFF;
            three[1] = (pixel >> three[0]) & 0xFF;
            //L.write(tag, "OLD one="+one[1]+", two="+two[1]+", three="+three[1]);
            tmp = one[1];
            one[1] = two[1];
            two[1] = tmp;
            //L.write(tag, "NEW one="+one[1]+", two="+two[1]+", three="+three[1]);
            pixels[i] = (pixel & 0xFF000000) | (sort.get(0)[1] << 16) | (sort.get(1)[1] << 8) | sort.get(2)[1];
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
    }
}
