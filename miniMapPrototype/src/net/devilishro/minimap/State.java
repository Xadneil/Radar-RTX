package net.devilishro.minimap;

import net.devilishro.minimap.EventActivity.Event;

public class State {
    private static Event events[];
    private static int currentEvent = -1;

    private static boolean admin = false;

    private static String authID;

    public static String getAuthID() {
        return authID;
    }

    public static void setAuthID(String authID) {
        State.authID = authID;
    }

    public static boolean networkDebug = true;

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
}
