package net.devilishro.minimap;

import java.util.ArrayList;
import java.util.List;

import net.devilishro.minimap.local.ReplayDatabase;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.database.CursorIndexOutOfBoundsException;
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
	private int counter = 0;
	private SparseArray<Pair> mark = new SparseArray<Pair>();
	protected boolean isGooglePlayConnected;
	private boolean is_running;
	private Handler handler;

	private static class Pair {
		public Marker marker;
		public boolean isUsed;

		public Pair(Marker marker, boolean isUsed) {
			this.marker = marker;
			this.isUsed = isUsed;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.rplay) {
			if (is_running)
				return true;
			new Thread() {
				public void run() {
					is_running = true;
					while (is_running) {
						try {
							Thread.sleep(2000);
							handler.obtainMessage(1).sendToTarget();
						} catch (Exception e) {
							Log.e(TAG, "Error", e);
							break;
						}
					}
				}
			}.start();
		} else if (item.getItemId() == R.id.resetter) {
			AppState.reset_db();
		} else if (item.getItemId() == R.id.rpause) {
			is_running = false;
		}
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_map);

		if (!setUpMap()) {
			finish();
			return;
		}
		// map.set

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
		map.moveCamera(CameraUpdateFactory.zoomTo(18));
	}

	private void next_post() {
		ArrayList<ContentValues> temp = null;
		try {
			temp = AppState.recv_points(counter);
		} catch (CursorIndexOutOfBoundsException e) {
			Toast.makeText(this, "Replay has stopped.", Toast.LENGTH_SHORT)
					.show();
			is_running = false;
			counter = 0;
			return;
		}
		ContentValues cur_pos = null;
		LatLng pos = new LatLng(0, 0);
		Marker val = null;
		int id = 0;
		int temp_two = -1;
		for (int i = 0; i < temp.size(); i++) {
			cur_pos = temp.get(i); // gets ith player
			if (i == 0) {
				set_map(new LatLng(
						cur_pos.getAsDouble(ReplayDatabase.Column_lat),
						cur_pos.getAsDouble(ReplayDatabase.Column_lng)));
			}
			id = cur_pos.getAsInteger(ReplayDatabase.Column_playerID);
			Pair pair = mark.get(id); // gets the marker

			if (pair == null) { // new player for the replay
				val = map.addMarker(new MarkerOptions().title("" + id)
						.position(pos));
			} else {
				val = pair.marker;
			}
			// mark as used, and initialization for new marker
			mark.put(id, new Pair(val, true));
			move_marker(val,
					new LatLng(cur_pos.getAsDouble(ReplayDatabase.Column_lat),
							cur_pos.getAsDouble(ReplayDatabase.Column_lng)));
		}

		// Delete markers which were not updated.
		List<Integer> toDelete = new ArrayList<Integer>();
		for (int i = 0; i < mark.size(); i++) {
			Pair p = mark.valueAt(i);
			if (!p.isUsed) {
				toDelete.add(mark.keyAt(i));
			}
		}

		for (int i : toDelete) {
			mark.delete(i);
		}

		// reset everything to false for next iteration
		for (int i = 0; i < mark.size(); i++) {
			int markerId = mark.keyAt(i);
			mark.put(markerId, new Pair(mark.get(markerId).marker, false));
		}

		temp_two = (Integer) cur_pos.get(ReplayDatabase.KEY_ID);
		counter = temp_two + 1;
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
