package edu.usf.cse.minimap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import edu.usf.cse.minimap.EventActivity.Event;
import edu.usf.cse.minimap.GroupActivity.Group;

public class State {
    private static String names[];

    private static LatLng positions[];
    private static Marker markers[];
    private static boolean hasNewPositions = false;
    private static final Object positionsLock = new Object();

    private static Event events[];
    private static int currentEvent = -1;

    private static boolean hasNewPing = false;
    private static LatLng ping;
    private static final Object pingLock = new Object();

    private static Group groups[];
    private static int currentGroup = -1;

    public static boolean networkDebug = true;

    public static LatLng getPing() {
        return ping;
    }

    public static void setPing(LatLng ping) {
        State.ping = ping;
    }

    public static boolean hasNewPing() {
        return hasNewPing;
    }

    public static void setHasNewPing(boolean hasNewPing) {
        State.hasNewPing = hasNewPing;
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

    public static void setGroupNumber(int number) {
        groups = new Group[number];
    }

    public static Group getCurrentGroup() {
        if (currentGroup >= 0) {
            return groups[currentGroup];
        } else {
            return null;
        }
    }

    public static void setCurrentGroup(int currentGroup) {
        State.currentGroup = currentGroup;
    }

    public static Group[] getGroups() {
        return groups;
    }
}
