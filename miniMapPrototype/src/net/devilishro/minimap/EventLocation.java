package net.devilishro.minimap;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;

public class EventLocation extends Activity implements OnClickListener {

	private GoogleMap map;
	private static final String TAG = "EventLocation";
	private final int GOOGLE_PLAY_SERVICES = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// fullscreen activity
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_event_location);
		
		if (!setUpMap()) {
			finish();
			return;
		}
		
		map.setMyLocationEnabled(true);
		((Button) findViewById(R.id.event_location_button)).setOnClickListener(this);
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
			Fragment fragment = getFragmentManager().findFragmentById(R.id.location_map);
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

	@Override
	public void onClick(View v) {
		CameraPosition pos = map.getCameraPosition();
		Intent intent = new Intent();
		intent.putExtra("CAMERA", pos);
		this.setResult(Activity.RESULT_OK, intent);
		finish();
	}
}
