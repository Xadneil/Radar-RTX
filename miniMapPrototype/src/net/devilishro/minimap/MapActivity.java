package net.devilishro.minimap;

import net.devilishro.minimap.EventActivity.Event;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
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
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

//TODO re-lock map fragment movement in layout xml
public class MapActivity extends Activity {
	private GoogleMap map;
	private static Handler handler;
	private LocationProvider provider;
	private LocationClient location;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// fullscreen activity
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_map);
		
		if (!setUpMapIfNeeded()) {
			finish();
			return;
		}
		initialize();

		Event e = State.getCurrentEvent();
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
						// TODO Auto-generated method stub

					}

					@Override
					public void onConnected(Bundle arg0) {
						// TODO Auto-generated method stub

					}
				}, new GooglePlayServicesClient.OnConnectionFailedListener() {

					@Override
					public void onConnectionFailed(ConnectionResult arg0) {
						// TODO Auto-generated method stub

					}
				});
	}

	public static Handler getHandler() {
		return handler;
	}

	private void initialize() {
		synchronized (State.getPositionsLock()) {
			/*
			 * for (int i = 0; i < State.getPositions().length; i++) {
			 * State.getMarkers()[i] = map.addMarker(new MarkerOptions()
			 * .snippet(State.getNames()[i]).position(
			 * State.getPositions()[i])); }
			 */
		}
	}

	public LatLng getLocation() {
		//TODO location stuff is broken due to Google
		//Location loc = location.getLastLocation();
		return null; //new LatLng(loc.getLatitude(), loc.getLongitude());
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (provider != null)
			provider.start();
	}

	@Override
	protected void onPause() {
		if (provider != null)
			provider.finish();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (provider != null)
			provider.finish(); // just to make sure
		super.onDestroy();
	}

	/**
	 * Code based on Google tutorial
	 */
	private boolean setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (map == null) {
			Fragment temp = getFragmentManager().findFragmentById(R.id.map);
			if (temp == null) {
				Toast.makeText(this, "The map failed to load.",
						Toast.LENGTH_LONG).show();
				Log.d("Map Debug", "Fragment null");
				return false;
			}
			map = ((MapFragment) temp).getMap();
			// Check if we were successful in obtaining the map.
			if (map == null) {
				Toast.makeText(this, "The map failed to load.",
						Toast.LENGTH_LONG).show();
				Log.d("Map Debug", "getMap null");
				return false;
			}
		}
		return true;
	}

	private void displayPositions() {
		synchronized (State.getPositionsLock()) {
			for (int i = 0; i < State.getPositions().length; i++) {
				Marker m = State.getMarkers()[i];
				LatLng position = State.getPositions()[i];
				if (m != null && position != null) {
					animateMarker(m, position, false);
				}
			}
		}
		Log.d("MapActivity", "positions displayed");
	}

	/**
	 * Code from Google tutorials
	 * 
	 * @param marker
	 * @param toPosition
	 * @param hideMarker
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
