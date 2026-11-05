package com.re3.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import java.util.HashMap;
import java.util.Map;

public final class TouchPadView extends View {
    interface Sink { void setMask(int mask); }

    private static final int UP=0x0010, RIGHT=0x0020, DOWN=0x0040, LEFT=0x0080;
    private static final int L1=0x0400, R1=0x0800;
    private static final int TRIANGLE=0x1000, CIRCLE=0x2000, CROSS=0x4000, SQUARE=0x8000;
    private static final int START=0x0008, SELECT=0x0001;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Sink sink;
    private final Map<Integer, Integer> fingers = new HashMap<>();
    private int mask;

    public TouchPadView(Context context, Sink sink) {
        super(context);
        this.sink = sink;
        setClickable(true);
    }

    private RectF buttonRect(int id) {
        float density = getResources().getDisplayMetrics().density;
        float margin = 18f * density;
        float size = Math.max(46f * density, getHeight() * 0.105f);
        float x = 0, y = 0;
        switch (id) {
            case UP: x=margin+size; y=getHeight()-margin-size*2.1f; break;
            case DOWN: x=margin+size; y=getHeight()-margin; break;
            case LEFT: x=margin; y=getHeight()-margin-size*1.05f; break;
            case RIGHT: x=margin+size*2; y=getHeight()-margin-size*1.05f; break;
            case TRIANGLE: x=getWidth()-margin-size; y=getHeight()-margin-size*2.1f; break;
            case CIRCLE: x=getWidth()-margin; y=getHeight()-margin-size*1.05f; break;
            case CROSS: x=getWidth()-margin-size*2; y=getHeight()-margin; break;
            case SQUARE: x=getWidth()-margin-size*2; y=getHeight()-margin-size*2.1f; break;
            case L1: x=getWidth()*0.27f; y=margin+size*0.5f; break;
            case R1: x=getWidth()*0.73f; y=margin+size*0.5f; break;
            case SELECT: x=getWidth()*0.43f; y=getHeight()*0.83f; break;
            case START: x=getWidth()*0.57f; y=getHeight()*0.83f; break;
        }
        return new RectF(x-size*.42f, y-size*.42f, x+size*.42f, y+size*.42f);
    }

    private int[] ids() {
        return new int[]{UP,DOWN,LEFT,RIGHT,TRIANGLE,CIRCLE,CROSS,SQUARE,L1,R1,SELECT,START};
    }

    private String label(int id) {
        switch (id) {
            case UP: return "▲"; case DOWN: return "▼";
            case LEFT: return "◀"; case RIGHT: return "▶";
            case TRIANGLE: return "△"; case CIRCLE: return "○";
            case CROSS: return "×"; case SQUARE: return "□";
            case L1: return "L1"; case R1: return "R1";
            case SELECT: return "SELECT"; default: return "START";
        }
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int id : ids()) {
            RectF r = buttonRect(id);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor((mask & id) != 0 ? 0xCC55CCFF : 0x66777777);
            canvas.drawRoundRect(r, 14f, 14f, paint);
            paint.setColor(0xFFFFFFFF);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(id == SELECT || id == START ? 10f * getResources().getDisplayMetrics().density : r.height()*.42f);
            canvas.drawText(label(id), r.centerX(), r.centerY()+paint.getTextSize()*.34f, paint);
        }
    }

    private int hit(float x, float y) {
        for (int id : ids()) if (buttonRect(id).contains(x,y)) return id;
        return 0;
    }

    private void recalc() {
        int next = 0;
        for (int value : fingers.values()) next |= value;
        if (next != mask) {
            mask = next;
            sink.setMask(mask);
            invalidate();
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int index = event.getActionIndex();
        int pointerId = event.getPointerId(index);

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            fingers.put(pointerId, hit(event.getX(index), event.getY(index)));
            recalc();
            return true;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            for (int i=0; i<event.getPointerCount(); i++)
                fingers.put(event.getPointerId(i), hit(event.getX(i), event.getY(i)));
            recalc();
            return true;
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP ||
            action == MotionEvent.ACTION_CANCEL) {
            fingers.remove(pointerId);
            recalc();
            return true;
        }
        return true;
    }
}
