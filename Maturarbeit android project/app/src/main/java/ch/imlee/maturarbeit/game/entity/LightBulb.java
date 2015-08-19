package ch.imlee.maturarbeit.game.entity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import ch.imlee.maturarbeit.R;
import ch.imlee.maturarbeit.game.GameClient;
import ch.imlee.maturarbeit.game.GameThread;
import ch.imlee.maturarbeit.game.map.Map;
import ch.imlee.maturarbeit.game.views.GameSurface;

/**
 * Created by Sandro on 29.06.2015.
 */
public class LightBulb extends Entity{
    private final Bitmap LIGHT_BULB_OFF, LIGHT_BULB_ON;
    private final int SIDE;
    private Player possessor;

    public LightBulb(byte team, Map map){
        // so if team 0 it goes to start +1 on x and y and if team = 1 it goes to start -1 on x and y, so always one towards the center of the map
        this(Map.getStartX(team)+1-(2*team), Map.getStartY(team)+1-(2*team), map);
    }

    public LightBulb(float entityXCoordinate, float entityYCoordinate, Map map) {
        super(entityXCoordinate, entityYCoordinate);
        SIDE = map.TILE_SIDE;
        LIGHT_BULB_OFF = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(GameSurface.getRec(), R.drawable.light_bulb_off), SIDE, SIDE, false);
        LIGHT_BULB_ON = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(GameSurface.getRec(), R.drawable.light_bulb_on), SIDE, SIDE, false);
    }

    public void update(){
        if (possessor != null){
            setCoordinates(possessor.getXCoordinate(), possessor.getYCoordinate());
        }
    }

    public Canvas render(Canvas canvas){
        if(possessor != null){
            if (possessor.getClass() == Fluffy.class) {
                canvas.drawBitmap(LIGHT_BULB_ON, (xCoordinate - GameThread.getUser().getXCoordinate()) * SIDE + GameClient.getHalfScreenWidth() - SIDE / 2, (yCoordinate - GameThread.getUser().getYCoordinate()) * SIDE + GameClient.getHalfScreenHeight() - SIDE / 2, null);
            }
        }else{
            canvas.drawBitmap(LIGHT_BULB_OFF, (xCoordinate - GameThread.getUser().getXCoordinate()) * SIDE + GameClient.getHalfScreenWidth()- SIDE/2, (yCoordinate - GameThread.getUser().getYCoordinate()) * SIDE + GameClient.getHalfScreenHeight() - SIDE / 2, null);
        }
        return canvas;
    }

    public void pickUp(Player possessor){
        this.possessor = possessor;
    }

    public Player getPossessor() {
        return possessor;
    }
}
