package ch.imlee.maturarbeit.game.events.gameStateEvents;

import ch.imlee.maturarbeit.game.events.Event;

/**
 * Created by Lukas on 18.06.2015.
 */
public class GameCancelledEvent extends GameStateEvent {
    @Override
    public String toString(){
        return super.toString() + 'C';
    }
}
