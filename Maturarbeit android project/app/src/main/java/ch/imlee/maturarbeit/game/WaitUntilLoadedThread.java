package ch.imlee.maturarbeit.game;

import ch.imlee.maturarbeit.activities.GameClient;
import ch.imlee.maturarbeit.bluetooth.Host;
import ch.imlee.maturarbeit.events.gameStateEvents.GameLoadedEvent;

// this thread is used by the Server to wait until all the Clients have loaded their game
public class WaitUntilLoadedThread extends Thread {
    private static int ready=0;

    public static synchronized void incrementReady(){
        ++ready;
    }
    private static synchronized int getReady(){
        return ready;
    }

    public void run(){
        while(getReady() <= Host.sockets.size()){
            try {
                Thread.sleep(10);
            } catch(Exception e){
                e.printStackTrace();
                System.exit(1);
            }
        }
        new GameLoadedEvent().send();
        GameClient.startSynchronizedTick();
    }

    public static void reset(){
        ready = 0;
    }
}
