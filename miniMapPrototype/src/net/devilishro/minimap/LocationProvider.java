package net.devilishro.minimap;

import net.devilishro.minimap.network.PacketCreator;
import android.location.Location;
import android.util.Log;

/**
 * A location update thread. Sends the user's location to the map server every
 * interval
 * 
 * @author Daniel
 */
public class LocationProvider extends Thread {
	private static final int INTERVAL = 2000; // send location every INTERVAL ms
	private boolean isRunning = false;
	private final MapActivity act;

	public LocationProvider(MapActivity act) {
		this.act = act;
	}

	@Override
	public void run() {
		while (isRunning) {
			Location loc = act.getLocation();
			if (loc != null) {
				double lat = loc.getLatitude();
				double lng = loc.getLongitude();
				float bearing = loc.getBearing();
				AppState.getFieldServer().send(
						PacketCreator.reportLocation(lat, lng, bearing));
			}
			try {
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e) {
				Log.w("LocationProvider", "Exception reporting location", e);
				close();
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

	public void close() {
		isRunning = false;
	}
}
