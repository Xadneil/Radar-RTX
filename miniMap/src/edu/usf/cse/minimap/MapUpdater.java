package edu.usf.cse.minimap;

import android.os.Handler;

public class MapUpdater extends Thread {
    private boolean isRunning = false;
    private Handler handler;

    public MapUpdater(Handler handler) {
        super("PositionUpdater");
        this.handler = handler;
    }

    @Override
    public void run() {
        while (isRunning) {
            if (Network.getInstance().requestMapState()) {
                handler.sendEmptyMessage(0);
            }
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
