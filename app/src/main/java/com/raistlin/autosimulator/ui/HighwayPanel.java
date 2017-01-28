package com.raistlin.autosimulator.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.raistlin.autosimulator.logic.Highway;
import com.raistlin.autosimulator.logic.IHighwayPanel;
import com.raistlin.autosimulator.logic.IHighwayPanelCallback;

public class HighwayPanel extends SurfaceView implements SurfaceHolder.Callback, IHighwayPanel {

    private SurfaceHolder mSurfaceHolder;
    private IHighwayPanelCallback mCallback;

    public HighwayPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public void setCallback(IHighwayPanelCallback callback) {
        mCallback = callback;
    }

    @Override
    public void update(Highway highway) {
        if (mSurfaceHolder == null)
            return;

        Canvas c = null;
        try {
            c = mSurfaceHolder.lockCanvas();
            if (c != null)
                highway.render(c);
        } finally {
            // do this in a finally so that if an exception is thrown
            // during the above, we don't leave the Surface in an
            // inconsistent state
            if (c != null) {
                mSurfaceHolder.unlockCanvasAndPost(c);
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        if (mCallback != null) {
            mCallback.onSizeChanged(width, height);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        mSurfaceHolder = getHolder();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        mSurfaceHolder = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mCallback != null)
                mCallback.onTouchEvent(event.getX(), event.getY());
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }
}
