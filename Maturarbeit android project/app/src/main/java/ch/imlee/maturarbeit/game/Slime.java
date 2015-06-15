package ch.imlee.maturarbeit.game;

import ch.imlee.maturarbeit.game.views.GameSurface;

/**
 * Created by Sandro on 11.06.2015.
 */
public class Slime extends User{

    public Slime(float entityXCoordinate, float entityYCoordinate, PlayerType type, Map map, GameSurface.GameThread gameThread, int team, byte playerId, User theUser) {
        super(entityXCoordinate, entityYCoordinate, type, map, gameThread, team, playerId, theUser);
    }

    @Override
    public void skillActivation() {

    }
}
