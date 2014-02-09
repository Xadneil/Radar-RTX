package edu.usf.cse.minimap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.usf.cse.minimap.EventActivity.Event;

//TODO re-lock map fragment movement in layout xml
public class MapActivity extends Activity {
    private GoogleMap map;
    private Handler handler;
    private MapUpdater updater;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialize();

        // fullscreen activity
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_map);

        if (!setUpMapIfNeeded()) {
            finish();
        }

        Event e = State.getCurrentEvent();
        if (e == null) {
            finish();
        }
        map.moveCamera(CameraUpdateFactory.newLatLng(e.position));
        map.moveCamera(CameraUpdateFactory.zoomTo(e.zoom));

//        Group g = State.getCurrentGroup();
//        if (g == null) {
//            finish();
//        }

        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);

        handler = new Handler() {
            @Override
            public void handleMessage(Message m) {
                displayPositions();
            }
        };

        updater = new MapUpdater(handler);
    }

    private void initialize() {
        synchronized (State.getPositionsLock()) {
            for (int i = 0; i < State.getPositions().length; i++) {
                State.getMarkers()[i] = map.addMarker(new MarkerOptions()
                        .snippet(State.getNames()[i]).position(
                                State.getPositions()[i]));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updater.start();
    }

    @Override
    protected void onPause() {
        updater.finish();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        updater.finish(); // just to make sure
        super.onDestroy();
    }

    /**
     * Code based on Google tutorial
     */
    private boolean setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (map == null) {
            map = ((MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (map == null) {
                Toast.makeText(this, "The map failed to load.",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    private void displayPositions() {
        synchronized (State.getPositionsLock()) {
            if (State.hasNewPositions()) {
                for (int i = 0; i < State.getPositions().length; i++) {
                    Marker m = State.getMarkers()[i];
                    LatLng position = State.getPositions()[i];
                    if (m != null && position != null) {
                        animateMarker(m, position, false);
                    }
                }
            }
        }
        synchronized (State.getPingLock()) {
            if (State.hasNewPing()) {
                final Marker m = map.addMarker(new MarkerOptions().position(
                        State.getPing()).snippet("Ping"));
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        m.remove();
                    }
                }, 3000);
                State.setHasNewPing(false);
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
