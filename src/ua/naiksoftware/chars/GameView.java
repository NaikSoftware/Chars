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

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

// TODO: заменить родителя на View чтобы зря не гонять процессор, а обновлять экран по надобности?
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String tag = "GameView";
    private GameManager gameManThread;
    private Context context;
    private SizeListener sizeListener;

    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attr) {
        super(context, attr);
        init(context);
    }

    public GameView(Context context, AttributeSet attr, int style) {
        super(context, attr, style);
        init(context);
    }

    public void setArea(char[][] gameArea) {
        if (gameManThread != null) {
            gameManThread.setArea(gameArea);
        }
    }

    private void init(Context context) {
        //Log.d(tag, "init");
        getHolder().addCallback(this);
        this.context = context;
    }

    public void surfaceCreated(SurfaceHolder mHolder) {
        //Log.d(tag, "surfaceCreated");
        if (sizeListener != null) {
            //L.write(tag, "onSizeChanged started interface not null");
            final int w = getWidth();
            final int h = getHeight();
            int znam = 20;
            final int min = Math.min(w, h);
            final int max = Math.max(w, h);
            int size = min / znam;
            final int diff = (max % size > size / 2) ? 1 : -1;
            while (max % size > 3) {
                znam += diff;
                size = min / znam;
            }
            int inRow = w / size;
            int inColumn = h / size;
            if (sizeListener.getState() == EngineActivity.STATE_NOT_INIT) {
                sizeListener.onSizeChanged(inRow, inColumn);
            }
            gameManThread = new GameManager(mHolder, w, h, inRow, inColumn, size);
            gameManThread.setRunning(true);
            gameManThread.start();
        }
    }

    public void surfaceChanged(SurfaceHolder mHolder, int p2, int p3, int p4) {
        //Log.d(tag, "surfaceChanged");
    }

    public void surfaceDestroyed(SurfaceHolder p1) {
        //Log.d(tag, "surfaceDestroyed");
        gameManThread.setRunning(false);
    }

    public void setOnSizeListener(SizeListener sizeListener) {
        this.sizeListener = sizeListener;
    }
}
