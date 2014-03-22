package net.devilishro.minimap;

import com.google.android.gms.maps.model.LatLng;

public class LocationProvider extends Thread {
    private boolean isRunning = false;
    private final MapActivity act;

    public LocationProvider(MapActivity act) {
        this.act = act;
    }

    @Override
    public void run() {
        while (isRunning) {
            @SuppressWarnings("unused")
			LatLng ll = act.getLocation();
            //TODO Network.getInstance().reportLocation(ll.latitude, ll.longitude);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isRunning() {
		return isRunning;
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
