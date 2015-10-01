package ch.imlee.maturarbeit.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import ch.imlee.maturarbeit.R;
import ch.imlee.maturarbeit.activities.GameClient;
import ch.imlee.maturarbeit.events.gameActionEvents.PlayerMotionEvent;
import ch.imlee.maturarbeit.game.Controller.FluffyGameSurfaceController;
import ch.imlee.maturarbeit.game.Controller.GameSurfaceController;
import ch.imlee.maturarbeit.game.Controller.JoystickController;
import ch.imlee.maturarbeit.game.Sound.BackgroundMusic;
import ch.imlee.maturarbeit.game.entity.Fluffy;
import ch.imlee.maturarbeit.game.entity.Ghost;
import ch.imlee.maturarbeit.game.entity.LightBulb;
import ch.imlee.maturarbeit.game.entity.Particle;
import ch.imlee.maturarbeit.game.entity.Player;
import ch.imlee.maturarbeit.game.entity.PlayerType;
import ch.imlee.maturarbeit.game.entity.Slime;
import ch.imlee.maturarbeit.game.entity.SlimeTrail;
import ch.imlee.maturarbeit.game.entity.Sweet;
import ch.imlee.maturarbeit.game.entity.User;
import ch.imlee.maturarbeit.events.Event;
import ch.imlee.maturarbeit.events.EventReceiver;
import ch.imlee.maturarbeit.events.gameStateEvents.GameLoadedEvent;
import ch.imlee.maturarbeit.events.gameStateEvents.GameStartEvent;
import ch.imlee.maturarbeit.game.map.LightBulbStand;
import ch.imlee.maturarbeit.game.map.Map;
import ch.imlee.maturarbeit.views.GameSurface;
import ch.imlee.maturarbeit.views.JoystickSurface;
import ch.imlee.maturarbeit.views.MiniMap;
import ch.imlee.maturarbeit.views.ParticleButton;
import ch.imlee.maturarbeit.views.SkillButton;
import ch.imlee.maturarbeit.activities.DeviceType;
import ch.imlee.maturarbeit.activities.StartActivity;

/**
 * Created by Sandro on 04.07.2015.
 */
public class GameThread extends Thread implements Tick{

    /**
     * The running variable determines if the game loop goes on or not.
     * @see #run()
     */
    private static boolean running;
    private static boolean loading;
    private static long lastTime;
    private static long timeLeft;

    private static double synchronizedTick;
    private static double predictedDelay;
    private static boolean gameRunning = true;
    private static byte winningTeam = -1;

    private static ParticleButton particleButton;
    private static SkillButton skillButton;
    protected static Map map;
    protected static User user;
    protected static Player[] playerArray;
    protected static ArrayList<SlimeTrail> slimeTrailList = new ArrayList<>();
    protected static ArrayList<Particle> particleList = new ArrayList<>();
    protected static ArrayList<Integer> freeParticleIDs = new ArrayList<>();
    public static ArrayList<Sweet> sweets = new ArrayList<>();
    public static Set<Integer> sweetsToRemove = new HashSet<>();
    private static LightBulb[] lightBulbArray;
    private static LightBulbStand[] lightBulbStandArray;
    private static SurfaceHolder holder;
    private static BackgroundMusic backgroundMusic;
    private static JoystickController joystickController;
    private static GameSurfaceController gameSurfaceController;
    private static MiniMap miniMap;

    public GameThread(SurfaceHolder holder, Context context){
        this.holder = holder;
    }
    /**
     * The method called when the gameThread is started. It contains the main game loop.
     */
    @Override
    public void run() {
        loading = true;
        particleButton = GameClient.getParticleButton();
        skillButton = GameClient.getSkillButton();
        backgroundMusic = new BackgroundMusic();
        backgroundMusic.start();
        miniMap = GameClient.getMiniMap();
        displayLoadingScreen();
        while(running){
            update();
            render();
            if((timeLeft = TIME_PER_TICK - (System.currentTimeMillis() - lastTime)) > 0) {
                try {
                    sleep(timeLeft);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else{
                synchronizedTick -= timeLeft / TIME_PER_TICK;
                predictedDelay -= timeLeft;
                if(predictedDelay >= 0.5){
                    //todo: request a fresh synchronizedTick
                    //todo: on receiving a new Tick, the old Data has to be reviewed
                }
            }
            lastTime = System.currentTimeMillis();
            synchronizedTick++;
        }
        backgroundMusic.stop();
    }

    /**
     * This method calls all the update methods of particles, players, etc...
     */
    protected void update(){
        for(Queue<Event> eventQueue:EventReceiver.events){
            while(!eventQueue.isEmpty()){
                eventQueue.remove().apply();
            }
        }
        for(int i = 0; i < sweets.size(); ++i){
            if(sweetsToRemove.contains(sweets.get(i).getID())){
                sweets.remove(i);
            }
        }
        joystickController.update();
        gameSurfaceController.update();
        for (Player player:playerArray){
            player.update();
        }
        for(int i = 0; i < slimeTrailList.size(); ++i){
            slimeTrailList.get(i).update();
            if(slimeTrailList.get(i).removable){
                slimeTrailList.remove(i);
                --i;
            }
        }
        for (Particle particle:particleList) {
            if(particle != null) {
                particle.update();
            }
        }
        for (LightBulb lightBulb: lightBulbArray){
            lightBulb.update();
        }
    }

    /**
     * This method does everything required for the game graphics.
     */
    private void render(){
        Canvas c = null;
        try {
            c = holder.lockCanvas(null);
            synchronized (holder) {
                if(c!=null) {
                    if(gameRunning) {
                        if (!getUser().getDead()) {
                            c.drawColor(Color.BLACK);
                            map.render(c);
                            for (SlimeTrail slimeTrail : slimeTrailList) {
                                slimeTrail.render(c);
                            }
                            for (Sweet sweet : sweets) {
                                sweet.render(c);
                            }
                            for (Particle particle : particleList) {
                                if (particle != null) {
                                    particle.render(c);
                                }
                            }
                            for (Player player : playerArray) {
                                player.render(c);
                            }

                            for (LightBulb lightBulb : lightBulbArray) {
                                lightBulb.render(c);
                            }
                            joystickController.render(c);
                            gameSurfaceController.render(c);
                            miniMap.render(c);
                            //todo:display pause button
                        } else {
                            c.drawRect(0, 0, c.getWidth(), c.getHeight(), new Paint());
                            Paint textPaint = new Paint();
                            textPaint.setTextSize(64);
                            textPaint.setColor(0xffffffff);
                            c.drawText("YOU ARE DEAD", 20, 120, textPaint);
                            textPaint.setTextSize(20);
                            c.drawText("Respawn in " + (int) (getUser().reviveTick - getSynchronizedTick()) / TICK + " seconds", 20, 200, textPaint);
                        }
                    } else {
                        c.drawRect(0, 0, c.getWidth(), c.getHeight(), new Paint());
                        Paint paint = new Paint();
                        paint.setTextSize(64);
                        paint.setColor(0xffffffff);
                        c.drawText("Game Finished!", 10, 10 + paint.getTextSize(), paint);
                        if(winningTeam == 0){
                            c.drawText("Team Green won!", 10, 20 + 2*paint.getTextSize(), paint);
                        } else if(winningTeam == 1){
                            c.drawText("Team Blue won!", 10, 20 + 2*paint.getTextSize(), paint);
                        }
                    }
                }
            }
        } finally {
            if (c != null) {
                holder.unlockCanvasAndPost(c);
            }
        }
    }

    private void displayLoadingScreen(){
        String loadingText = "Loading...";
        int i = 3;
        int halfSurfaceHeight = GameSurface.getSurfaceHeight() / 2;
        int thirdSurfaceWidth = GameSurface.getSurfaceWidth() / 3;
        int ninthSurfaceWidth = thirdSurfaceWidth / 3;
        Bitmap fluffy, slime, ghost;
        Paint textPaint = new Paint();
        textPaint.setColor(0xffffffff);
        int textSize = 64;
        textPaint.setTextSize(textSize);
        float[] textWidths = new float[loadingText.length()];
        textPaint.getTextWidths(loadingText, textWidths);
        float textWidth = 0;
        for(int j = 0; j < loadingText.length(); ++j){
            textWidth += textWidths[i];
        }
        fluffy = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(GameSurface.getRec(), R.drawable.fluffy), ninthSurfaceWidth, ninthSurfaceWidth, false);
        slime = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(GameSurface.getRec(), R.drawable.slime), ninthSurfaceWidth, ninthSurfaceWidth, false);
        ghost = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(GameSurface.getRec(), R.drawable.ghost), ninthSurfaceWidth, ninthSurfaceWidth, false);
        while(loading) {
            Canvas c = null;
            try {
                c = holder.lockCanvas(null);
                synchronized (holder) {
                    if (c != null) {
                        c.drawColor(Color.BLACK);
                        switch (i) {
                            case 0:
                                c.drawBitmap(fluffy, thirdSurfaceWidth + 2 * ninthSurfaceWidth, halfSurfaceHeight, null);
                            case 1:
                                c.drawBitmap(slime, thirdSurfaceWidth + ninthSurfaceWidth, halfSurfaceHeight, null);
                            case 2:
                                c.drawBitmap(ghost, thirdSurfaceWidth, halfSurfaceHeight, null);
                            default:
                                c.drawText(loadingText, GameSurface.getSurfaceWidth()/2-textWidth/2, halfSurfaceHeight - textSize, textPaint);
                        }
                    }
                }
            } finally {
                if (c != null) {
                    holder.unlockCanvasAndPost(c);
                }
            }
            i++;
            if (i > 3) i = 0;
            if((timeLeft = TIME_PER_LOADING_TICK - (System.currentTimeMillis() - lastTime)) > 0) {
                try {
                    sleep(timeLeft);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            lastTime = System.currentTimeMillis();
        }
    }

    private Canvas displayPauseButton(Canvas c){
        //TODO: add pause button and functionality
        return c;
    }

     public  void setStartData(GameStartEvent startData){
         Log.i("initialization", "Start data is initialized");
         map = new Map(GameSurface.getRec(), startData.getMapID());
         playerArray = new Player[startData.getPlayerCount()];
         for (byte i = 0; i < startData.getPlayerCount(); i++){
            if (i == startData.getUserID()){
                Log.i("user", "The user is being initialized.");
                switch (startData.getPlayerTypes().get(i)){
                    case FLUFFY:user = new Fluffy(map, startData.getTeams().get(i), i);
                        break;
                    case GHOST:user = new Ghost(map, startData.getTeams().get(i), i);
                        break;
                    case SLIME:user = new Slime(map, startData.getTeams().get(i), i);
                        break;
                    case NULL: Log.i("fail", "user PlayerType is NULL");
                }
                playerArray[i] = user;
            }else {
                Log.i("player", "A Player is being initialized");
                playerArray[i] = new Player(startData.getPlayerTypes().get(i), map, startData.getTeams().get(i), i);
            }
         }
         lightBulbArray = new LightBulb[2];
         lightBulbArray[0] = new LightBulb((byte) 0, (byte) 0);
         lightBulbArray[1] = new LightBulb((byte) 1, (byte) 1);
         joystickController = new JoystickController(user, JoystickSurface.getJoystickSurfaceWidth(), JoystickSurface.getJoystickSurfaceHeight());
         if (user.getType() == PlayerType.FLUFFY){
             gameSurfaceController = new FluffyGameSurfaceController(user, GameSurface.getSurfaceWidth(), GameSurface.getSurfaceHeight(), GameSurface.getRec());
             Log.i("GameThread", "FluffySurfaceController initialized.");
         } else {
             gameSurfaceController = new GameSurfaceController(user, GameSurface.getSurfaceWidth(), GameSurface.getSurfaceHeight());
             Log.i("GameThread", "SurfaceController initialized.");
         }
         MiniMap.setup();
         GameSurface.setup(gameSurfaceController);
         if(StartActivity.deviceType == DeviceType.CLIENT) {
            new GameLoadedEvent().send();
         } else {
            WaitUntilLoadedThread.incrementReady();
         }
     }

    public static void endLoading(){
        synchronizedTick = 0;
        loading = false;
        particleButton.setUser(user);
        skillButton.setUser(user);
        new PlayerMotionEvent(user).send();
    }

    public void addParticle(Particle newParticle){
        // if particleList has too few indexes, add nulls until it has enough to save the newParticle at its right place
        if(newParticle.getID() == particleList.size()){
            particleList.add(newParticle);
            return;
        }
        particleList.set(newParticle.getID(), newParticle);
    }

    public void removeParticle(int ID){
        particleList.set(ID, null);
    }

    public void playerHit(byte playerID){
        if (playerID < 0)return;
        playerArray[playerID].particleHit();
    }

    public static void addSlimeTrail(SlimeTrail slimeTrail) {
        slimeTrailList.add(slimeTrail);
        Log.v("slime", "SlimeTrail added");
    }

    /**
     * Simple setter for the running variable.
     * @see #running
     * @param running - the value the running variable is set to
     */
    public void setRunning(boolean running){
        this.running = running;
    }

    public static double getSynchronizedTick(){
        return synchronizedTick;
    }

    public static User getUser(){
        return user;
    }

    public static Player[] getPlayerArray() {
        return playerArray;
    }

    public  void setHolder(SurfaceHolder holder){
        this.holder = holder;
    }

    public static LightBulb[] getLightBulbArray() {
        return lightBulbArray;
    }

    public static ArrayList<Particle> getParticleList(){
        return particleList;
    }

    public static ArrayList<SlimeTrail> getSlimeTrailList(){
        return slimeTrailList;
    }

    public static  boolean getLoading(){
        return  loading;
    }

    public static void setGameRunning(boolean gameRunning1){
        gameRunning = gameRunning1;
    }

    public static void setWinningTeam(byte team){
        winningTeam = team;
    }
}