package net.devilishro.minimap;

import java.util.ArrayList;
import java.util.HashSet;

import net.devilishro.minimap.EventActivity.Event;
import net.devilishro.minimap.network.Network;
import net.devilishro.minimap.network.PacketHandlers.Type;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Global data storage.
 * 
 * @author Daniel
 */
public class AppState {
	private static final int MAX_PLAYERS = 8;

	// map information
	private static String names[] = new String[MAX_PLAYERS];
	private static LatLng positions[] = new LatLng[MAX_PLAYERS];
	private static Marker markers[] = new Marker[MAX_PLAYERS];
	private static final Object positionsLock = new Object();

	// event information
	private static Event events[];
	private static int currentEvent = -1;
	private static ArrayList<HashSet<String>> teamNames = new ArrayList<HashSet<String>>(2);

	// login information
	private static boolean loginOK = false;
	private static boolean admin = false;

	// general information
	private static String username;
	private static String serverAddress = "50.62.212.171";

	//Networks
	private static Network eventServer = new Network(Type.EVENT, serverAddress, 33630);
	private static Network mapServer;

	public static boolean networkBypass = true;

	static {
		// may not ever get packet that starts server
		if (networkBypass) {
			mapServer = new Network(Type.EVENT, serverAddress, 33630);
			mapServer.start();
		}
		for (int i = 0; i < 2; i++) {
			teamNames.add(i, new HashSet<String>(MAX_PLAYERS));
		}
	}

	@SuppressWarnings("unused")
	private static String TAG = "State";

	public static void initMapServer(int port) {
		mapServer = new Network(Type.MAP, serverAddress, port);
	}

	public static String getServerAddress() {
		return serverAddress;
	}

	public static Network getEventServer() {
		return eventServer;
	}

	public static Network getMapServer() {
		return mapServer;
	}

	public static boolean isLoginOK() {
		return loginOK;
	}

	public static void setLoginOK(boolean loginOK) {
		AppState.loginOK = loginOK;
	}

	public static String[] getNames() {
		return names;
	}

	public static HashSet<String> getTeamNames(int team) {
		return teamNames.get(team);
	}

	public static boolean isAdmin() {
		return admin;
	}

	public static void setAdmin(boolean admin) {
		AppState.admin = admin;
	}

	public static Event[] getEvents() {
		return events;
	}

	/**
	 * Initializes the events array (static array)
	 * 
	 * @param number
	 *            the number of events
	 */
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

	public static String getUsername() {
		return username;
	}

	public static void setUsername(String username) {
		AppState.username = username;
	}
}
