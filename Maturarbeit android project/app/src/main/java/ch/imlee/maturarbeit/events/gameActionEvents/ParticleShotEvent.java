package ch.imlee.maturarbeit.events.gameActionEvents;

import ch.imlee.maturarbeit.game.GameThread;

/**
 * Created by Sandro on 04.10.2015.
 */
public class ParticleShotEvent extends GameActionEvent{

    public final int PARTICLE_ID;
    public final float X_COORDINATE, Y_COORDINATE;
    public final double ANGLE, SPAWN_TICK;

    public ParticleShotEvent(String eventString){
        super(Byte.valueOf(eventString.substring(eventString.length() - 1)));
        X_COORDINATE = Float.valueOf(eventString.substring(eventString.indexOf("x") + 1, eventString.indexOf("y")));
        Y_COORDINATE = Float.valueOf(eventString.substring(eventString.indexOf("y") + 1, eventString.indexOf("a")));
        ANGLE = Double.valueOf(eventString.substring(eventString.indexOf("a") + 1, eventString.indexOf("s")));
        SPAWN_TICK = Double.valueOf(eventString.substring(eventString.indexOf("s") + 1, eventString.indexOf("p")));
        PARTICLE_ID = Integer.valueOf(eventString.substring(eventString.indexOf("p") + 1, eventString.indexOf("i")));
    }

    public ParticleShotEvent(float xCoordinate, float yCoordinate, double angle, double spawnTick, int particleID){
        super(GameThread.getUser().getID());
        X_COORDINATE = xCoordinate;
        Y_COORDINATE = yCoordinate;
        ANGLE = angle;
        SPAWN_TICK = spawnTick;
        PARTICLE_ID = particleID;
    }

    @Override
    public String toString() {
        return super.toString() + 'P' + 'x' + X_COORDINATE + 'y' + Y_COORDINATE + 'a' + ANGLE + 's' + SPAWN_TICK + 'p' + PARTICLE_ID + 'i' + senderID;
    }

    @Override
    public void apply() {
        GameThread.addParticle(this);
    }
}
