package ch.imlee.maturarbeit.events.gameActionEvents;

import android.util.Log;
import android.view.View;

import ch.imlee.maturarbeit.activities.DeviceType;
import ch.imlee.maturarbeit.activities.StartActivity;
import ch.imlee.maturarbeit.game.GameServerThread;
import ch.imlee.maturarbeit.game.GameThread;

/**
 * Created by Lukas on 12.09.2015.
 */
public class GameWinEvent extends GameActionEvent {
    private final byte TEAM;

    public GameWinEvent(byte team){
        TEAM = team;
    }
    public GameWinEvent(String eventString){
        TEAM = Byte.parseByte(eventString.substring(2));
    }

    @Override
    public String toString(){
        return super.toString() + 'G' + TEAM;
    }
    @Override
    public void apply(){
        Log.i("game ended", "Team " + TEAM + " won!");
        GameThread.setGameRunning(false);
        GameThread.setWinningTeam(TEAM);
        if(StartActivity.deviceType == DeviceType.HOST){
            GameServerThread.setEndGameLayoutVisibility(View.VISIBLE);
        }
    }
}