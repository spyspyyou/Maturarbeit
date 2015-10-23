package ch.imlee.maturarbeit.game;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

import ch.imlee.maturarbeit.activities.GameClient;
import ch.imlee.maturarbeit.events.gameActionEvents.ParticleHitEvent;
import ch.imlee.maturarbeit.events.gameActionEvents.TickEvent;
import ch.imlee.maturarbeit.events.gameStateEvents.GameCancelledEvent;
import ch.imlee.maturarbeit.events.gameStateEvents.RestartGameEvent;
import ch.imlee.maturarbeit.game.entity.Particle;
import ch.imlee.maturarbeit.game.entity.Player;
import ch.imlee.maturarbeit.game.entity.Sweet;
import ch.imlee.maturarbeit.events.gameActionEvents.SweetSpawnEvent;

/**
 * Created by Sandro on 14.08.2015.
 */
public class GameServerThread extends GameThread{

    private static final int SWEET_SPAWN_RATE = Tick.TICK * 2;
    private static double lastSweetSpawn = 0;
    private static int currentSweetId = 0;
    private int resendCount = 0;


    private static LinearLayout layout = new LinearLayout(GameClient.getContext());
    private static LinearLayout.LayoutParams layoutParams;
    private static Button restartButton = new Button(GameClient.getContext());
    private static Button endButton = new Button(GameClient.getContext());

    public GameServerThread(SurfaceHolder holder, Context context) {
        super(holder, context);
        layoutParams = new LinearLayout.LayoutParams(holder.getSurfaceFrame().width(), holder.getSurfaceFrame().height());
        layoutParams.setMargins(10, 168, holder.getSurfaceFrame().width(), holder.getSurfaceFrame().height());
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RestartGameEvent().send();
                new RestartGameEvent().apply();
            }
        });
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GameCancelledEvent().send();
                new GameCancelledEvent().apply();
            }
        });
        restartButton.setText("restart Game");
        endButton.setText("end Game");
        layout.addView(restartButton);
        layout.addView(endButton);
        layout.setVisibility(View.GONE);
        layout.setLayoutParams(layoutParams);
    }

    @Override
    protected void update() {
        super.update();
        if(lastSweetSpawn + SWEET_SPAWN_RATE <= getSynchronizedTick()){
            spawnSweet();
            lastSweetSpawn = getSynchronizedTick();
        }
        particlePhysics();
        resendCount++;
        if (resendCount >= TICK * 5){
            new TickEvent().send();
            resendCount = 0;
        }
    }

    //todo:review
    public void particlePhysics(){
        ArrayList<Particle> particleList;
        for (int i = 0; i < particleListArray.length; i++) {
            particleList = particleListArray[i];
            for (Particle particle:particleList) {
                if (particle != null) {
                    // the particles can get an X or Y coordinate below zero, so we have to check that first to not get an ArrayIndexOutOfBoundsException
                    if ((int) particle.getXCoordinate() < 0 || (int) particle.getYCoordinate() >= map.TILES_IN_MAP_HEIGHT || (int) particle.getXCoordinate() >= map.TILES_IN_MAP_WIDTH || (int) particle.getYCoordinate() < 0 || map.getSolid((int) particle.getXCoordinate(), (int) particle.getYCoordinate())) {
                        ParticleHitEvent particleHitEvent = new ParticleHitEvent(particle.getID(), (byte)-1, (byte)i);
                        particleHitEvent.send();
                        particleHitEvent.apply();
                        continue;
                    }
                    for (Player player : playerArray) {
                        if (player.TEAM != particle.TEAM && Math.pow(player.getXCoordinate() - particle.getXCoordinate(), 2) + Math.pow(player.getYCoordinate() - particle.getYCoordinate(), 2) <= Math.pow(player.getPlayerRadius(), 2)) {
                            ParticleHitEvent particleHitEvent = new ParticleHitEvent(particle.getID(), player.getID(), (byte)i);
                            particleHitEvent.send();
                            particleHitEvent.apply();
                            break;
                        }
                    }
                }
            }
        }
    }

    public static void spawnSweet(){
        int tempX, tempY;
        Sweet tempSweet;
        boolean possible;
        do{
            tempX = (int)(Math.random() * map.TILES_IN_MAP_WIDTH);
            tempY = (int)(Math.random() * map.TILES_IN_MAP_HEIGHT);
            possible = !map.getSolid(tempX, tempY);
        }while(!possible);
        tempSweet = new Sweet(tempX, tempY, currentSweetId);
        sweets.add(tempSweet);
        new SweetSpawnEvent(tempSweet).send();
        ++currentSweetId;
    }

    public static void setEndGameLayoutVisibility(int visibility){
        layout.setVisibility(visibility);
    }
}
