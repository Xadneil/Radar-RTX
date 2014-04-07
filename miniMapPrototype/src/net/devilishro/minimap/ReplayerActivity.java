package net.devilishro.minimap;

import java.util.ArrayList;

import net.devilishro.minimap.local.ReplayDatabase;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

//import net.devilishro.minimap.EventActivity.Event;

public class ReplayerActivity extends Activity {

	GoogleMap map;
	private final int GOOGLE_PLAY_SERVICES = 0;
	private final String TAG = "ReplayerActivity";
	private int counter = 1;
	private SparseArray<Marker> mark = new SparseArray<Marker>();
	protected boolean isGooglePlayConnected;
	private boolean test;
	private Handler handler;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.rplay) {
			new Thread() {
				public void run() {
					int i = 0;

					while (true) {
						try {
							Thread.sleep(200);
							handler.obtainMessage(1).sendToTarget();
							i++;
						} catch (Exception e) {
							Log.e(TAG, "pos's displayed: " + i);
							Log.e(TAG, "Error", e);
							break;
						}
					}
				}
			}.start();
			Log.d(TAG, "This at least works");
			return true;
		} else
			return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_map);

		if (!setUpMap()) {
			finish();
			return;
		}

		handler = new Handler() {
			@Override
			public void handleMessage(Message m) {
				if (m.what == 0) {
					set_map_impl((LatLng) m.obj);
				} else if (m.what == 1) {
					next_post();
				}
			}
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.replayer, menu);
		return true;
	}

	private void replay_start() {
		final Handler handler = new Handler();
		handler.post(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						// wait(200);
						// next_post();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

	// straight copypasta
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

	private void set_map(LatLng latLng) {
		handler.obtainMessage(0, latLng).sendToTarget();
	}
	private void set_map_impl(LatLng latLng) {
		map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		map.moveCamera(CameraUpdateFactory.zoomTo(3));
		map.setMyLocationEnabled(true);
		map.getUiSettings().setMyLocationButtonEnabled(false);
	}

	private void next_post() {
		ArrayList<ContentValues> temp = AppState.recv_points(counter); // list
																		// of
																		// info
																		// from
																		// all
																		// players
																		// on
																		// the
																		// filed
																		// at a
																		// certain
																		// time
		ContentValues cur_pos = null;
		LatLng pos = new LatLng(0, 0);
		Marker val = null;
		int id = 0;
		int temp_two = -1;
		for (int i = 0; i < temp.size(); i++) {
			cur_pos = temp.get(i); // gets ith player
			if (i == 0)
				set_map(new LatLng(
						cur_pos.getAsDouble(ReplayDatabase.KEY_NAME2),
						cur_pos.getAsDouble(ReplayDatabase.KEY_NAME3)));
			id = (Integer) cur_pos.get(ReplayDatabase.KEY_NAME1);
			val = mark.get(id); // gets the marker
			if (val == null) { // new player for the replay
				val = map.addMarker(new MarkerOptions().snippet(
						AppState.getNames().get(id)).position(pos));
			}
			move_marker(val,
					new LatLng(cur_pos.getAsDouble(ReplayDatabase.KEY_NAME2),
							cur_pos.getAsDouble(ReplayDatabase.KEY_NAME3)));
		}

		temp_two = (Integer) cur_pos.get(ReplayDatabase.KEY_ID);
		counter = temp_two + 2;
	}

	private void move_marker(final Marker m, final LatLng new_pos) {
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		Projection proj = map.getProjection();
		Point startPoint = proj.toScreenLocation(m.getPosition());
		final LatLng startLatLng = proj.fromScreenLocation(startPoint);
		final long duration = 500;

		final Interpolator interpolator = new LinearInterpolator();

		handler.post(new Runnable() {
			@Override
			public void run() {
				long elapsed = SystemClock.uptimeMillis() - start;
				float t = interpolator.getInterpolation((float) elapsed
						/ duration);
				double lng = t * new_pos.longitude + (1 - t)
						* startLatLng.longitude;
				double lat = t * new_pos.latitude + (1 - t)
						* startLatLng.latitude;
				m.setPosition(new LatLng(lat, lng));

				if (t < 1.0) {
					// Post again 16ms later.
					handler.postDelayed(this, 16);
				} else {
					m.setVisible(true);
				}
			}
		});
	}
}
