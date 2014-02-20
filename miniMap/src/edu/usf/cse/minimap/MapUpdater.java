package edu.usf.cse.minimap;

import android.os.Handler;

public class MapUpdater extends Thread {
    private boolean isRunning = false;

    public MapUpdater() {
        super("PositionUpdater");
    }

    @Override
    public void run() {
        while (isRunning) {
            Network.getInstance().send(new Packet(SendOpcode.MAP_STATE));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start() {
        isRunning = true;
        super.start();
    }

    public void finish() {
        isRunning = false;
    }
}
