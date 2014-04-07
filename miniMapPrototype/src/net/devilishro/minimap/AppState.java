package net.devilishro.minimap;

import java.util.ArrayList;
import java.util.HashSet;

import net.devilishro.minimap.EventActivity.Event;
import net.devilishro.minimap.local.ReplayDatabase;
import net.devilishro.minimap.network.Network;
import net.devilishro.minimap.network.PacketHandlers.Type;
import android.content.ContentValues;
import android.content.Context;
import android.util.SparseArray;

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
	private static SparseArray<String> names = new SparseArray<String>(
			MAX_PLAYERS);
	private static SparseArray<LatLng> positions = new SparseArray<LatLng>(
			MAX_PLAYERS);
	private static SparseArray<Marker> markers = new SparseArray<Marker>(
			MAX_PLAYERS);
	private static SparseArray<Float> bearing = new SparseArray<Float>(
			MAX_PLAYERS);
	private static int myId = -1;
	private static final Object positionsLock = new Object();

	// event information
	private static Event events[];
	private static int currentEvent = -1;
	private static ArrayList<HashSet<String>> teamNames = new ArrayList<HashSet<String>>(
			2);

	// login information
	private static boolean loginOK = false;
	private static boolean admin = false;

	// general information
	private static Context applicationContext;
	private static String username;
	private static String serverAddress = "50.62.212.171";
    //private static String serverAddress = "192.168.1.13";
	// private static String serverAddress = "192.168.16.2";

	// Networks
	private static Network eventServer = new Network(Type.EVENT, serverAddress,
			33630);
	private static Network fieldServer = new Network(Type.MAP, serverAddress,
			33640);

	public static boolean networkBypass = false;

	private static ReplayDatabase db = new ReplayDatabase(applicationContext);

	static {
		for (int i = 0; i < 2; i++) {
			teamNames.add(i, new HashSet<String>(MAX_PLAYERS));
		}
	}

	@SuppressWarnings("unused")
	private static String TAG = "State";

	public static void resetEventServer() {
		eventServer.close();
		eventServer = new Network(Type.EVENT, serverAddress, 33630);
	}

	public static void resetFieldServer() {
		fieldServer.close();
		fieldServer = new Network(Type.MAP, serverAddress, 33640);
	}

	public static String getServerAddress() {
		return serverAddress;
	}

	public static Context getApplicationContext() {
		return applicationContext;
	}

	public static void setApplicationContext(Context applicationContext) {
		AppState.applicationContext = applicationContext;
	}

	public static SparseArray<Float> getBearings() {
		return bearing;
	}

	public static Network getEventServer() {
		return eventServer;
	}

	public static Network getFieldServer() {
		return fieldServer;
	}

	public static boolean isLoginOK() {
		return loginOK;
	}

	public static void setLoginOK(boolean loginOK) {
		AppState.loginOK = loginOK;
	}

	public static SparseArray<String> getNames() {
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

	public static SparseArray<LatLng> getPositions() {
		return positions;
	}

	public static Object getPositionsLock() {
		return positionsLock;
	}

	public static SparseArray<Marker> getMarkers() {
		return markers;
	}

	public static String getUsername() {
		return username;
	}

	public static void setUsername(String username) {
		AppState.username = username;
	}

	public static void setMyId(int myId) {
		AppState.myId = myId;
	}

	public static int getMyId() {
		return myId;
	}

	public static void add_db(LatLng point, int play_num, int div) {
		// TODO Auto-generated method stub
		db.addPoints(point, play_num, div);
	}
	
	public static ArrayList<ContentValues> recv_points(int counter)
	{
		return db.readPoints(counter);
	}
}
