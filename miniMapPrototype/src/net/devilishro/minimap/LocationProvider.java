package net.devilishro.minimap;

import net.devilishro.minimap.network.PacketCreator;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

// TODO possibly replace with ScheduledThreadPoolExecutor
/**
 * A location update thread. Sends the user's location to the map server every
 * interval
 * 
 * @author Daniel
 */
public class LocationProvider extends Thread {
	private static final int INTERVAL = 1000; // send location every INTERVAL ms
	private boolean isRunning = false;
	private final MapActivity act;

	public LocationProvider(MapActivity act) {
		this.act = act;
	}

	@Override
	public void run() {
		while (isRunning) {
			LatLng ll = act.getLocation();
			if (ll != null) {
				AppState.getMapServer()
						.send(PacketCreator.reportLocation(ll.latitude,
								ll.longitude));
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
