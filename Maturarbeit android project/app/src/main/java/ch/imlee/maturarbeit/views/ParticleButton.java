package ch.imlee.maturarbeit.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

import ch.imlee.maturarbeit.game.GameThread;
import ch.imlee.maturarbeit.game.special_screens.EndGameScreen;

public class ParticleButton extends Button {

    public ParticleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("Particle Button", String.valueOf(GameThread.getSynchronizedTick()));
        if (GameThread.getEndGameActive()){
            return EndGameScreen.onTouch(event);
        }
        // if the GameThread wasn't done loading yet it would cause an error because there would be no User
        if (GameThread.getLoading()){
            return false;
        }
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            GameThread.getUser().setShooting(true);
        }else if (event.getAction() == MotionEvent.ACTION_UP){
            GameThread.getUser().setShooting(false);
        }
        return super.onTouchEvent(event);
    }

}

