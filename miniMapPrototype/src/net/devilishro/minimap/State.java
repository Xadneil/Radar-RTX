package net.devilishro.minimap;

import net.devilishro.minimap.EventActivity.Event;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class State {
    private static String names[];

    private static LatLng positions[];
    private static Marker markers[];
    private static boolean hasNewPositions = false;
    private static final Object positionsLock = new Object();

    private static Event events[];
    private static int currentEvent = -1;

    private static LatLng ping;
    private static final Object pingLock = new Object();

    private static boolean loginOK = false;
    private static boolean admin = false;

    private static String authID;

    public static String getAuthID() {
        return authID;
    }

    public static void setAuthID(String authID) {
        State.authID = authID;
    }

    public static boolean networkDebug = true;

    public static boolean isLoginOK() {
        return loginOK;
    }

    public static void setLoginOK(boolean loginOK) {
        State.loginOK = loginOK;
    }

    public static boolean isAdmin() {
        return admin;
    }

    public static void setAdmin(boolean admin) {
        State.admin = admin;
    }

    public static LatLng getPing() {
        return ping;
    }

    public static void setPing(LatLng ping) {
        State.ping = ping;
    }

    public static Object getPingLock() {
        return pingLock;
    }

    public static String[] getNames() {
        return names;
    }

    public static boolean hasNewPositions() {
        return hasNewPositions;
    }

    public static void setHasNewPositions(boolean hasNewPositions) {
        State.hasNewPositions = hasNewPositions;
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

    public static void setNumber(int number) {
        names = new String[number];
        synchronized (positionsLock) {
            positions = new LatLng[number];
            markers = new Marker[number];
        }
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
}
