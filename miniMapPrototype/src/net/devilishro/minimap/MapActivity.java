package net.devilishro.minimap;

import net.devilishro.minimap.EventActivity.Event;
import net.devilishro.minimap.network.Network;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

//TODO re-lock map fragment movement in layout xml
/**
 * Map Activity. Displays the radar.
 * 
 * @author Daniel
 */
public class MapActivity extends Activity {
	private GoogleMap map;
	private Handler handler;
	private LocationProvider provider;
	private LocationClient location;
	private boolean isGooglePlayConnected = false;
	private final int GOOGLE_PLAY_SERVICES = 0;
	private final String TAG = "MapActivity";

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// fullscreen activity
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_map);

		// initialize the map and markers
		if (!setUpMap()) {
			finish();
			return;
		}
		initialize();

		// configure the map
		Event e = AppState.getCurrentEvent();
		if (e == null) {
			finish();
			return;
		}
		map.moveCamera(CameraUpdateFactory.newLatLng(e.position));
		map.moveCamera(CameraUpdateFactory.zoomTo(e.zoom));
		map.setMyLocationEnabled(true);
		map.getUiSettings().setMyLocationButtonEnabled(false);

		handler = new Handler() {
			@Override
			public void handleMessage(Message m) {
				displayPositions();
			}
		};
		provider = new LocationProvider(this);
		location = new LocationClient(this,
				new GooglePlayServicesClient.ConnectionCallbacks() {

					@Override
					public void onDisconnected() {
						isGooglePlayConnected = false;
						Log.d(TAG, "Google Play Services disconnected.");
					}

					@Override
					public void onConnected(Bundle arg0) {
						isGooglePlayConnected = true;
						Log.d(TAG, "Google Play Services connected.");
					}
				}, new GooglePlayServicesClient.OnConnectionFailedListener() {

					@Override
					public void onConnectionFailed(ConnectionResult arg0) {
						Log.e(TAG,
								"Google Play Services reports connection failed.");
						Toast.makeText(MapActivity.this, "Google Play Error",
								Toast.LENGTH_LONG).show();
						finish();
					}
				});
	}

	/**
	 * Gets the handler for this map activity in order to have the map update
	 * 
	 * @return the handler
	 */
	public Handler getHandler() {
		return handler;
	}

	private void initialize() {
		synchronized (AppState.getPositionsLock()) {
			for (int i = 0; i < AppState.getPositions().size(); i++) {
				int id = AppState.getPositions().keyAt(i);
				if (AppState.getNames().get(id) != null
						&& AppState.getPositions().get(id) != null) {
					AppState.getMarkers()[i] = map
							.addMarker(new MarkerOptions().snippet(
									AppState.getNames().get(id)).position(
									AppState.getPositions().get(id)));
				}
			}
		}
	}

	/**
	 * Gets the last location measured
	 * 
	 * @return a LatLng for the location
	 */
	public LatLng getLocation() {
		if (!isGooglePlayConnected)
			return null;
		Location loc;
		try {
			loc = location.getLastLocation();
		} catch (Exception e) {
			Log.e(TAG, "location failed", e);
			return null;
		}
		return new LatLng(loc.getLatitude(), loc.getLongitude());
	}

	@Override
	protected void onResume() {
		super.onResume();
		AppState.getMapServer().registerContext(this, Network.Activities.MAP);
		if (provider != null && !provider.isRunning()) {
			provider.start();
		} else if (provider == null) {
			provider = new LocationProvider(this);
			provider.start();
		}
		location.connect();
	}

	@Override
	protected void onPause() {
		AppState.getMapServer().unregisterContext(Network.Activities.MAP);
		if (provider != null) {
			provider.close();
			provider = null;
		}
		location.disconnect();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// in case finish() is called
		AppState.getMapServer().unregisterContext(Network.Activities.MAP);
		if (provider != null) {
			provider.close();
			provider = null;
		}
		location.disconnect();
		super.onDestroy();
	}

	/**
	 * Code based on Google tutorial
	 */
	private boolean setUpMap() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (map == null) {
			int gPlayStatus;
			if ((gPlayStatus = GooglePlayServicesUtil
					.isGooglePlayServicesAvailable(this)) != ConnectionResult.SUCCESS) {
				GooglePlayServicesUtil.getErrorDialog(gPlayStatus, this,
						GOOGLE_PLAY_SERVICES).show();
			}
			Fragment fragment = getFragmentManager().findFragmentById(R.id.map);
			if (fragment == null) {
				Toast.makeText(this, "The map failed to load.",
						Toast.LENGTH_LONG).show();
				Log.e(TAG, "Fragment null");
				return false;
			}
			map = ((MapFragment) fragment).getMap();
			// Check if we were successful in obtaining the map.
			if (map == null) {
				Toast.makeText(this, "The map failed to load.",
						Toast.LENGTH_LONG).show();
				Log.e(TAG, "getMap null");
				return false;
			}
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GOOGLE_PLAY_SERVICES) {
			if (resultCode == RESULT_OK) {
				// TODO need to test, find out what's in data
				Log.w("MapActivity", data.toString());
			}
		}
	}

	private void displayPositions() {
		synchronized (AppState.getPositionsLock()) {
			for (int i = 0; i < AppState.getPositions().size(); i++) {
				int id = AppState.getPositions().keyAt(i);
				Marker m = AppState.getMarkers()[i];
				LatLng position = AppState.getPositions().get(id);
				if (m != null && position != null) {
					animateMarker(m, position, false);
				}
			}
		}
		Log.d("MapActivity", "positions displayed");
	}

	/**
	 * Code from Google tutorials to smoothly move a
	 * {@link com.google.android.gms.maps.model.Marker Marker}
	 * 
	 * @param marker
	 *            the marker to move
	 * @param toPosition
	 *            the destination of the marker
	 * @param hideMarker
	 *            whether or not to hide the marker
	 */
	public void animateMarker(final Marker marker, final LatLng toPosition,
			final boolean hideMarker) {
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		Projection proj = map.getProjection();
		Point startPoint = proj.toScreenLocation(marker.getPosition());
		final LatLng startLatLng = proj.fromScreenLocation(startPoint);
		final long duration = 500;

		final Interpolator interpolator = new LinearInterpolator();

		handler.post(new Runnable() {
			@Override
			public void run() {
				long elapsed = SystemClock.uptimeMillis() - start;
				float t = interpolator.getInterpolation((float) elapsed
						/ duration);
				double lng = t * toPosition.longitude + (1 - t)
						* startLatLng.longitude;
				double lat = t * toPosition.latitude + (1 - t)
						* startLatLng.latitude;
				marker.setPosition(new LatLng(lat, lng));

				if (t < 1.0) {
					// Post again 16ms later.
					handler.postDelayed(this, 16);
				} else {
					if (hideMarker) {
						marker.setVisible(false);
					} else {
						marker.setVisible(true);
					}
				}
			}
		});
	}
}
