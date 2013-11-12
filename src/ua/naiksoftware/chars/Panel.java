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

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.util.*;
import android.view.*;

public class Panel extends View {

    private static final String tag = "Panel";
    private int strHeight;
    private String text, scoreStr, timeStr;
    private Paint textPaint = new Paint();

    public Panel(Context context) {
        super(context);
        init();
    }

    public Panel(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    public Panel(Context context, AttributeSet attr, int style) {
        super(context, attr, style);
        init();
    }

    private void init() {
        Resources res = getResources();
        scoreStr = res.getString(R.string.score);
        timeStr = res.getString(R.string.time);
        text = res.getString(R.string.we_not_played);
        Rect rect = new Rect();
        textPaint.setAntiAlias(true);
        textPaint.setShadowLayer(1f, 1f, 1f, Color.BLACK);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(14f);
        textPaint.getTextBounds("888", 0, 3, rect);
        strHeight = rect.height();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(text, 20f, strHeight + 2, textPaint);

    }

    @Override
    public void onMeasure(int x, int y) {
        super.onMeasure(x, y);
        setMeasuredDimension(-1, (int) textPaint.getTextSize() + 4);
    }

    public void setText(int score, float time) {
        text = scoreStr + score + "    " + timeStr + time;
    }
}
