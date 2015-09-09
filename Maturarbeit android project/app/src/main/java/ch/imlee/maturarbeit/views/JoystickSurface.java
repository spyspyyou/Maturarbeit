package ch.imlee.maturarbeit.views;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import ch.imlee.maturarbeit.game.Controller.JoystickController;
import ch.imlee.maturarbeit.game.GameThread;

/**
 * Created by Sandro on 23.08.2015.
 */
public class JoystickSurface extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder;
    private static Resources rec;
    private static int width;
    private static int height;

    public JoystickSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
        rec = getRec();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("JoystickSurface", "creating surface");
        invalidate();
        width = getWidth();
        height = getHeight();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        invalidate();
        this.width = getWidth();
        this.height = getHeight();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (GameThread.getLoading()){
            return false;
        }
        return JoystickController.onTouchEvent(event);
    }

    public static int getJoystickSurfaceWidth(){
        return width;
    }

    public static int getJoystickSurfaceHeight(){
        return height;
    }

    public static Resources getRec(){
        return rec;
    }
}
