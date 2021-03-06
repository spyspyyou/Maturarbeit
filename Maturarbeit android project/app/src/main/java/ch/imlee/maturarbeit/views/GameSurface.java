package ch.imlee.maturarbeit.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import ch.imlee.maturarbeit.R;
import ch.imlee.maturarbeit.activities.DeviceType;
import ch.imlee.maturarbeit.activities.StartActivity;
import ch.imlee.maturarbeit.events.gameStateEvents.RestartGameEvent;
import ch.imlee.maturarbeit.game.ControllerState;
import ch.imlee.maturarbeit.activities.GameClient;
import ch.imlee.maturarbeit.game.GameServerThread;
import ch.imlee.maturarbeit.game.GameThread;
import ch.imlee.maturarbeit.game.WaitUntilLoadedThread;
import ch.imlee.maturarbeit.game.entity.Player;
import ch.imlee.maturarbeit.game.entity.PlayerType;
import ch.imlee.maturarbeit.game.map.Map;
import ch.imlee.maturarbeit.game.special_screens.EndGameScreen;
import ch.imlee.maturarbeit.game.special_screens.LoadingScreen;

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback{

    private static int width, height;

    private static SurfaceHolder holder;
    // the surface controls the lifecycle of the main thread
    private static GameThread gameThread;
    private static GameSurfaceController gameSurfaceController;
    private static Resources rec;

    public GameSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // needs to be called to get the real width and height
        invalidate();
        width = getWidth();
        height = getHeight();
        rec = getResources();
        setupThread();
        gameThread.start();
        GameClient.gameSurfaceLoaded();
    }

    // the surface does not change in size during the game
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        destroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // if the endGame is active the touchEvent is redirected
        if (GameThread.getEndGameActive()){
            return EndGameScreen.onTouch(event);
        }
        // if the GameThread hasn't done loading yet it would cause an error because there would be no User
        if (GameThread.getLoading()){
            return false;
        }
        // call the controller
        return gameSurfaceController.onTouch(event);
    }

    // create the GameThread
    private static void setupThread(){
        // the server has an extended GameThread called GameServerThread
        if (StartActivity.deviceType == DeviceType.HOST){
            gameThread = new GameServerThread(holder);
        }else{
            gameThread = new GameThread(holder);
        }
    }

    // reset the data to start the game again
    public static void restart(){
        Log.e("GameSurface", "restart soon");

        // assure that the currently running GameThread is completely stopped
        destroy();
        LoadingScreen.setRestart();
        setupThread();
        gameThread.start();
        if(StartActivity.deviceType == DeviceType.HOST) {
            WaitUntilLoadedThread.reset();
            new WaitUntilLoadedThread().start();
            // tell the other devices that a new gameIsAbout to start and that they should also reset
            new RestartGameEvent().send();
        }
        GameClient.initializeStartData();
    }

    // update the Controller
    public static void update(){
        gameSurfaceController.update();
    }

    // render the Controller
    public static void render(Canvas canvas){
        gameSurfaceController.render(canvas);
    }

    public static void setup(){
        if (GameThread.getUser().getType() == PlayerType.FLUFFY){
            gameSurfaceController = new FluffyGameSurfaceController();
        }else{
            gameSurfaceController = new GameSurfaceController();
        }
    }

    // completely stop the GameThread
    public static void destroy(){
        if(gameThread == null){
            return;
        }
        gameThread.setRunning(false);
        gameThread.stopEndGame();
        while(true) {
            try {
                gameThread.join();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // reset the focus of the Controller
    public static void nullFocusedPlayer(){
        gameSurfaceController.nullFocusedPlayer();
    }

    public static Player getFocusedPlayer(){
        return gameSurfaceController.getFocusedPlayer();
    }

    public static Resources getRec(){
        return rec;
    }

    public static int getSurfaceWidth(){
        return width;
    }

    public static int getSurfaceHeight(){
        return height;
    }

    public static GameThread getGameThread(){
        return gameThread;
    }

    private static class GameSurfaceController {

        // if the finger  has moved
        private boolean posChanged;

        protected double halfSurfaceWidth, halfSurfaceHeight;

        // finger distance relative to the middle of the screen
        private double xFingerDistance, yFingerDistance;

        protected ControllerState controllerState = ControllerState.NULL;

        public GameSurfaceController() {
            halfSurfaceWidth = width / 2;
            halfSurfaceHeight = height / 2;
        }

        public void update() {
            synchronized (gameSurfaceController) {
                if (controllerState == ControllerState.AIMING && posChanged) {
                    // some trigonometry to calculate the User's new angle
                    double angle = Math.acos(xFingerDistance / Math.sqrt(Math.pow(xFingerDistance, 2) + Math.pow(yFingerDistance, 2)));
                    if (yFingerDistance <= 0) {
                        angle *= -1;
                    }
                    // change the direction the User is facing
                    GameThread.getUser().setAngle(angle);
                    posChanged = false;
                }
            }
        }

        // required for the subclass called FluffyGameSurfaceController so we can easily call it's render method in the GameThread
        public void render(Canvas canvas){

        }

        public boolean onTouch(MotionEvent event) {
            synchronized (gameSurfaceController) {
                // when the player lifts the finger the controllerState resets
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    controllerState = ControllerState.NULL;
                    return false;
                }
                // when touching down on the game surface AIMING is activated
                else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    controllerState = ControllerState.AIMING;
                }

                // the position relative to the middle of the screen
                xFingerDistance = event.getX() - halfSurfaceWidth;
                yFingerDistance = event.getY() - halfSurfaceHeight;

                // telling the user that his angle was changed.
                posChanged = true;
                return true;
            }
        }

        public void nullFocusedPlayer(){
            return;
        }

        public Player getFocusedPlayer(){
            return null;
        }
    }

    private static class FluffyGameSurfaceController extends GameSurfaceController{
        // if out of this range Fluffy looses the focus
        private final float MAX_FOCUS_RANGE = 5.0f;
        // to make it easier to click on the player
        private final float CLICK_TOLERANCE = 1.2f;
        // detect changes of the playerRadius of the focused Player
        private float lastPlayerRadius;

        private final Bitmap FOCUS_BMP;
        private Bitmap scaledFocusBmp;
        private Player focusedPlayer;

        public FluffyGameSurfaceController() {
            FOCUS_BMP = BitmapFactory.decodeResource(rec, R.drawable.focus_overlay);
            scaledFocusBmp = Bitmap.createScaledBitmap(FOCUS_BMP, Map.TILE_SIDE, Map.TILE_SIDE, false);
        }

        // this method is only used when the user is playing as Fluffy and it checks if the player who is focused is still in range.
        // also it has to match the size of the overlay to the player size.
        @Override
        public void update() {
            super.update();
            if (focusedPlayer!= null){
                // if out of range the focus is interrupted
                if(Math.pow(focusedPlayer.getXCoordinate() - GameThread.getUser().getXCoordinate(), 2) + Math.pow(focusedPlayer.getYCoordinate() - GameThread.getUser().getYCoordinate(), 2) > MAX_FOCUS_RANGE * MAX_FOCUS_RANGE) {
                    focusedPlayer = null;
                }
                // if the focusedPlayer's radius changed the overlay has to be updated
                else if (lastPlayerRadius != focusedPlayer.getPlayerRadius()) {
                    lastPlayerRadius = focusedPlayer.getPlayerRadius();
                    scaledFocusBmp = Bitmap.createScaledBitmap(FOCUS_BMP, (int) (lastPlayerRadius * Map.TILE_SIDE * 2), (int) (lastPlayerRadius * Map.TILE_SIDE * 2), false);
                }
            }
        }

        // only the stun overlay has to be rendered here
        @Override
        public void render(Canvas canvas) {
            if (focusedPlayer != null){
                canvas.drawBitmap(scaledFocusBmp, (float) ((focusedPlayer.getXCoordinate() - GameThread.getUser().getXCoordinate() - lastPlayerRadius) * Map.TILE_SIDE + halfSurfaceWidth), (float) ((focusedPlayer.getYCoordinate() - GameThread.getUser().getYCoordinate() - lastPlayerRadius) * Map.TILE_SIDE + halfSurfaceHeight), null);
            }
        }

        // it's checked whether the user has clicked on an enemy player.
        @Override
        public boolean onTouch(MotionEvent event) {
            synchronized (gameSurfaceController) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // it checks for every player if he is an enemy and in range.
                    for (Player player : GameThread.getPlayerArray()) {
                        // the second condition calculates if the touch event did hit a player by converting the coordinates of the touch into the map coordinates
                        if (player.TEAM != GameThread.getUser().TEAM && Math.pow(GameThread.getUser().getXCoordinate() + (event.getX() - halfSurfaceWidth) / Map.TILE_SIDE - player.getXCoordinate(), 2) + Math.pow(GameThread.getUser().getYCoordinate() + (event.getY() - halfSurfaceHeight) / Map.TILE_SIDE - player.getYCoordinate(), 2) <= player.getPlayerRadius() * player.getPlayerRadius() *  CLICK_TOLERANCE) {
                            controllerState = ControllerState.FOCUS;
                            focusedPlayer = player;
                            // we don't have to follow this event further
                            return false;
                        }
                    }
                }
            }
            // if no Player was clicked or the Controller is in a different state the super method is called
            return super.onTouch(event);
        }

        public Player getFocusedPlayer(){
            return focusedPlayer;
        }

        // to reset the focus when the skill is activated
        public void nullFocusedPlayer(){
            focusedPlayer = null;
        }
    }
}