package ch.imlee.maturarbeit.game.entity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import ch.imlee.maturarbeit.R;
import ch.imlee.maturarbeit.game.map.Map;
import ch.imlee.maturarbeit.events.gameActionEvents.InvisibilityEvent;
import ch.imlee.maturarbeit.views.GameSurface;

/**
 * Created by Sandro on 11.06.2015.
 */
public class Ghost extends User {

    private final int MANA_CONSUMPTION = MAX_MANA / 100;

    private final Bitmap INVISIBLE_GHOST;

    public Ghost(Map map, byte team, byte playerId) {
        super(PlayerType.GHOST, map, team, playerId);
        INVISIBLE_GHOST = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(GameSurface.getRec(), R.drawable.ghost_invisible), Map.TILE_SIDE, Map.TILE_SIDE, false);
    }

    @Override
    public void update() {
        super.update();
        if (invisible){
            if (mana <= 0){
                setInvisible(false);
            }else {
                mana -= MANA_CONSUMPTION;
            }
        }
        mana += 5;
        if (mana >= MAX_MANA){
            mana = MAX_MANA;
        }
    }

    @Override
    public Canvas render(Canvas canvas) {
        canvas = super.render(canvas);
        if (invisible){
            Matrix matrix = new Matrix();
            matrix.postRotate((float) (angle / 2 / Math.PI * 360) - 90);
            Bitmap rotated = Bitmap.createBitmap(INVISIBLE_GHOST, 0, 0, INVISIBLE_GHOST.getWidth(), INVISIBLE_GHOST.getHeight(), matrix, true);
            canvas.drawBitmap(rotated, (xCoordinate - user.getXCoordinate()) * Map.TILE_SIDE + GameSurface.getSurfaceWidth() / 2 - rotated.getWidth() / 2, (yCoordinate - user.getYCoordinate()) * Map.TILE_SIDE + GameSurface.getSurfaceHeight() / 2 - rotated.getHeight() / 2, null);
        }
        return canvas;
    }

    @Override
    public void skillActivation() {
        if(invisible){
            setInvisible(false);
        }else{
            setInvisible(true);
        }
    }
    @Override
    public void setInvisible(boolean invisible){
        this.invisible = invisible;
        new InvisibilityEvent(ID, invisible).send();
    }
}
