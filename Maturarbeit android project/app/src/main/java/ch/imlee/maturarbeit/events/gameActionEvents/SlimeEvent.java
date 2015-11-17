package ch.imlee.maturarbeit.events.gameActionEvents;

import ch.imlee.maturarbeit.game.GameThread;

// this Event gets sent when a Slime activates his special skill and thus leaves a continuous SlimeTrail
public class SlimeEvent extends GameActionEvent{

    private final boolean SLIMY;

    public SlimeEvent(byte playerId, boolean slimy){
        super(playerId);
        SLIMY = slimy;
    }
    public SlimeEvent(String eventString){
        super(Byte.valueOf(eventString.substring(eventString.length() - 1)));
        senderID = Byte.valueOf(eventString.substring(2, 3));
        SLIMY = eventString.charAt(3) == '0';
    }

    @Override
    public String toString() {
        return super.toString() + 'L' + senderID + ((SLIMY) ? 1 : 0) + 'i' + senderID;
    }

    @Override
    public void apply() {
        GameThread.getPlayerArray()[senderID].setSlimy(SLIMY);
        /*
        if(slimy){
            GameThread.getPlayerArray()[senderID].getSlimeSound().start();
        } else{
            GameThread.getPlayerArray()[senderID].getSlimeSound().stop();
        }
        */
    }
}
