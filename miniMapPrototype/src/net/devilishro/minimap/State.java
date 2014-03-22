package net.devilishro.minimap;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.devilishro.minimap.EventActivity.Event;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class State {
	private static final int MAX_PLAYERS = 8;

	private static String names[] = new String[MAX_PLAYERS];

    private static LatLng positions[] = new LatLng[MAX_PLAYERS];
    private static Marker markers[] = new Marker[MAX_PLAYERS];
    private static final Object positionsLock = new Object();
    
    private static Event events[];
    private static int currentEvent = -1;

    private static boolean loginOK = false;
    private static boolean admin = false;

    private static String authID;
    
    private static InetAddress serverAddress;
    
    public static boolean networkDebug = true;

    private static String TAG = "State";

    static {
    	try {
			serverAddress = InetAddress.getByName("50.62.212.171");
		} catch (UnknownHostException e) {
			Log.e(TAG, "Server IP Resolution Error", e);
		}
    }

    public static InetAddress getServerAddress() {
		return serverAddress;
	}

	public static boolean isLoginOK() {
		return loginOK;
	}

	public static void setLoginOK(boolean loginOK) {
		State.loginOK = loginOK;
	}

	public static String getAuthID() {
        return authID;
    }

    public static void setAuthID(String authID) {
        State.authID = authID;
    }

    

    public static String[] getNames() {
		return names;
	}

	public static boolean isAdmin() {
        return admin;
    }

    public static void setAdmin(boolean admin) {
        State.admin = admin;
    }

    public static Event[] getEvents() {
        return events;
    }

    public static void setEventNumber(int number) {
        events = new Event[number];
    }

    public static void setCurrentEvent(int position) {
        currentEvent = position;
    }

    public static Event getCurrentEvent() {
        if (currentEvent >= 0) {
            return events[currentEvent];
        } else {
            return null;
        }
    }

    public static LatLng[] getPositions() {
        return positions;
    }

    public static Object getPositionsLock() {
        return positionsLock;
    }

    public static Marker[] getMarkers() {
        return markers;
    }
}
