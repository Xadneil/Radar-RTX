package net.devilishro.minimap.network;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import net.devilishro.minimap.AppState;
import net.devilishro.minimap.EventActivity;
import net.devilishro.minimap.EventActivity.Event;
import net.devilishro.minimap.EventAdd;
import net.devilishro.minimap.EventJoinActivity;
import net.devilishro.minimap.MapActivity;
import net.devilishro.minimap.Minimap;
import net.devilishro.minimap.network.Network.Activities;
import android.app.Activity;
import android.os.Message;
import android.util.SparseArray;

import com.google.android.gms.maps.model.LatLng;

/**
 * Defines and aggregates
 * {@link net.devilishro.minimap.network.PacketHandlers.PacketHandler
 * PacketHandler}s
 * 
 * @author Daniel
 */
public class PacketHandlers {

	// keeps track of the number of handlers in each handler type
	private static int numHandlers[] = new int[Type.values().length];
	private static ArrayList<SparseArray<PacketHandler>> handlers = new ArrayList<SparseArray<PacketHandler>>(
			Type.values().length);

	/**
	 * The type of network connection. Used for grouping packet handlers by
	 * source and naming network connections.
	 * 
	 * @author Daniel
	 */
	public enum Type {
		LOGIN, EVENT, MAP;
	}

	// =========================================================================
	// begin packet handlers
	// =========================================================================

	/**
	 * Packet Handler for login responses
	 */
	public static PacketHandler login = new PacketHandler() {
		{
			type = Type.LOGIN;
			opcode = RecvOpcode.LOGIN;
		}

		@Override
		public void handlePacket(Packet packet, final Network n,
				final HashMap<Network.Activities, Activity> context) {
			short status;

			if (AppState.networkBypass) {
				status = 0x1DA;
			} else {
				status = packet.extract_short();
			}
			Minimap activity = (Minimap) context.get(Network.Activities.LOGIN);
			switch (status) {
			case 0x1D7:
				// Login failed
				activity.UIupdate.obtainMessage(2).sendToTarget();
				break;
			case 0x1D8:
				// Already Logged In
				activity.UIupdate.obtainMessage(3).sendToTarget();
				break;
			case 0x1DA:
				// Admin login
				AppState.setAdmin(true);
				// fall-through intended
			case 0x1D9:
				// User login
				AppState.setLoginOK(true);
				// set username in AppState
				activity.UIupdate.obtainMessage(4).sendToTarget();
				if (!AppState.getEventServer().isRunning()) {
					AppState.getEventServer().start();
				}
				AppState.getEventServer().registerContext(activity,
						Network.Activities.LOGIN);
				// This thread sends packets when the event server
				// connection is ready.
				new Thread() {
					public void run() {
						while (!AppState.getEventServer().isRunning()
								&& !AppState.getEventServer().isError()) {
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								throw new RuntimeException(
										"Was not able to initialize event server",
										e);
							}
						}
						if (AppState.getEventServer().isError()) {
							throw new RuntimeException(
									"Was not able to initialize event server");
						}
						AppState.getEventServer().send(
								PacketCreator.eventConnect());
						if (AppState.networkBypass) {
							eventServerResponse.handlePacket(null, n, context);
						}
					}
				}.start();
				break;
			}

		}
	};

	/**
	 * Packet Handler for registration responses
	 */
	public static PacketHandler register = new PacketHandler() {
		{
			type = Type.LOGIN;
			opcode = RecvOpcode.REGISTER;
		}

		@Override
		public void handlePacket(Packet packet, Network n,
				HashMap<Network.Activities, Activity> context) {
			short status;
			if (AppState.networkBypass) {
				status = 0x0001;
			} else {
				status = packet.extract_short();
			}
			Minimap activity = (Minimap) context.get(Network.Activities.LOGIN);

			if (status == 0x0001) {
				// Registration successful
				activity.UIupdate.obtainMessage(0).sendToTarget();
			} else {
				Message m = activity.UIupdate.obtainMessage(1);
				m.arg1 = status;
				m.sendToTarget();
			}
		}
	};

	/**
	 * Packet Handler for receiving the event list
	 */
	public static PacketHandler eventList = new PacketHandler() {
		{
			type = Type.EVENT;
			opcode = RecvOpcode.EVENT_LIST;
		}

		@Override
		public void handlePacket(Packet packet, Network n,
				HashMap<Network.Activities, Activity> context) {
			if (!AppState.networkBypass) {
				int numEvents = packet.extract_int();
				AppState.setEventNumber(numEvents);
				for (int i = 0; i < numEvents; i++) {
					int id = packet.extract_int();
					String title = packet.extract_string();
					short type = packet.extract_short();
					String message = packet.extract_string();
					Event event = new Event();
					event.id = id;
					event.title = title;
					event.message = message;
					event.type = type;
					AppState.getEvents()[i] = event;
				}
			} else {
				AppState.setEventNumber(1);
				AppState.getEvents()[0] = new EventActivity.Event(0,
						"Test Event", "Provider", new LatLng(28.059891,
								-82.416183), 17.0f);
			}
			Minimap minimap = (Minimap) context.get(Network.Activities.LOGIN);
			// this is the first time this handler has been used since login
			if (minimap != null) {
				minimap.startEventActivity(); // go from login screen to event
				// release reference for leak prevention
				n.unregisterContext(Network.Activities.LOGIN);
			} else {
				((EventActivity) context.get(Network.Activities.EVENT_LIST))
						.refresh();
			}
		}
	};

	/**
	 * Packet Handler for event choice affirmation
	 */
	public static PacketHandler eventChoose = new PacketHandler() {
		{
			type = Type.EVENT;
			opcode = RecvOpcode.EVENT_CHOOSE;
		}

		@Override
		public void handlePacket(Packet packet, Network n,
				HashMap<Network.Activities, Activity> context) {
			EventActivity activity = (EventActivity) context
					.get(Network.Activities.EVENT_LIST);
			short status;
			if (AppState.networkBypass) {
				status = 0x0001;
			} else {
				status = packet.extract_short();
				String team1 = packet.extract_string();
				String team2 = packet.extract_string();
				AppState.getCurrentEvent().team1 = team1;
				AppState.getCurrentEvent().team2 = team2;
			}
			if (status != 0x0001) {
				activity.eventFull();
			}
			activity.startJoinActivity();
		}
	};

	/**
	 * Packet Handler for single-event player list updates
	 */
	public static PacketHandler playerListUpdate = new PacketHandler() {
		{
			type = Type.EVENT;
			opcode = RecvOpcode.PLAYER_LIST_UPDATE;
		}

		@Override
		public void handlePacket(Packet packet, Network n,
				HashMap<Network.Activities, Activity> context) {
			short data = packet.extract_short();
			int whichTeam = (data & 0x0001) != 0 ? 0 : 1;
			// add players
			if ((data & 0x0010) != 0) {
				int numPlayers = packet.extract_int();
				for (int i = 0; i < numPlayers; i++) {
					String playerName = packet.extract_string();
					AppState.getTeamNames(whichTeam).add(playerName);
				}
			} else { // delete players
				int numPlayers = packet.extract_int();
				for (int i = 0; i < numPlayers; i++) {
					String playerName = packet.extract_string();
					AppState.getTeamNames(whichTeam).remove(playerName);
				}
			}
			((EventJoinActivity) context.get(Network.Activities.TEAM_JOIN))
					.refresh(whichTeam);
		}
	};

	/**
	 * Packet Handler for event addition responses
	 */
	public static PacketHandler eventAddResponse = new PacketHandler() {
		{
			type = Type.EVENT;
			opcode = RecvOpcode.EVENT_ADD;
		}

		@Override
		public void handlePacket(Packet packet, Network n,
				HashMap<Network.Activities, Activity> context) {
			short status;
			if (AppState.networkBypass) {
				status = 0x0001;
			} else {
				status = packet.extract_short();
			}
			EventAdd activity = (EventAdd) context
					.get(Network.Activities.EVENT_ADD);
			if (status == 0x0001) {
				// event added successfully
				activity.getHandler().obtainMessage(0).sendToTarget();
			} else {
				// some error occurred
				Message m = activity.getHandler().obtainMessage(1);
				m.arg1 = status;
				m.sendToTarget();
			}
		}
	};

	/**
	 * Packet Handler for Event Server initialization response
	 */
	public static PacketHandler eventServerResponse = new PacketHandler() {
		{
			type = Type.EVENT;
			opcode = RecvOpcode.EVENT_SERVER_RESPONSE;
		}

		@Override
		public void handlePacket(Packet packet, Network n,
				HashMap<Network.Activities, Activity> context) {
			short status;
			if (AppState.networkBypass) {
				status = 0x0001;
			} else {
				status = packet.extract_short();
			}
			if (status == 0x0001) {
				AppState.getEventServer()
						.send(PacketCreator.requestEventList());
				if (AppState.networkBypass) {
					eventList.handlePacket(null, n, context);
				}
			} else {
				throw new RuntimeException(
						"Event Server Connection Error (status:" + status + ")");
			}
		}
	};

	/**
	 * Packet Handler for Event Server initialization response
	 */
	public static PacketHandler eventLeave = new PacketHandler() {
		{
			type = Type.EVENT;
			opcode = RecvOpcode.EVENT_LEAVE;
		}

		@Override
		public void handlePacket(Packet packet, Network n,
				HashMap<Network.Activities, Activity> context) {
			((EventJoinActivity) context.get(Network.Activities.TEAM_JOIN)).handler
					.obtainMessage(0).sendToTarget();
		}
	};

	/**
	 * Packet Handler for receiving updated positions on the map
	 */
	public static PacketHandler mapPositions = new PacketHandler() {
		{
			type = Type.MAP;
			opcode = RecvOpcode.MAP_UPDATE;
		}

		@Override
		public void handlePacket(Packet packet, Network n,
				HashMap<Activities, Activity> context) {
			// TODO <j> <future> sync with packet specs
			int numUpdates = packet.extract_int();
			for (int i = 0; i < numUpdates; i++) {
				int playerId = packet.extract_int();
				double lat = packet.extract_double();
				double lng = packet.extract_double();
				LatLng ll = new LatLng(lat, lng);
				synchronized (AppState.getPositionsLock()) {
					AppState.getPositions()[playerId] = ll;
				}
			}
			// tell Map to update
			((MapActivity) context.get(Network.Activities.MAP)).getHandler()
					.obtainMessage(0).sendToTarget();
		}
	};

	// =========================================================================
	// end packet handlers
	// =========================================================================

	/**
	 * Class Initializer. Should happen after static field initialization
	 */
	static {
		// get all fields in this class
		Field fields[] = PacketHandlers.class.getDeclaredFields();
		// loop through the fields
		for (int i = 0; i < fields.length; i++) {
			try {
				Object field = fields[i].get(null);
				// if the field is a PacketHandler, increment its corresponding
				// numHandlers
				if (field instanceof PacketHandler) {
					PacketHandler handler = (PacketHandler) field;
					numHandlers[handler.type.ordinal()]++;
				}
			} catch (IllegalAccessException e) {
			} catch (IllegalArgumentException e) {
			} catch (NullPointerException e) {
			}
		}
		// initialize handlers with the numHandlers for each type
		for (int type = 0; type < Type.values().length; type++) {
			handlers.add(new SparseArray<PacketHandler>(numHandlers[type]));
		}

		// go through the fields again
		for (int i = 0; i < fields.length; i++) {
			try {
				Object field = fields[i].get(null);
				// if the field is a PacketHandler, add it to its corresponding
				// type list
				if (field instanceof PacketHandler) {
					PacketHandler handler = (PacketHandler) field;
					handlers.get(handler.type.ordinal()).put(
							handler.opcode.getValue(), handler);
				}
			} catch (IllegalAccessException e) {
			} catch (IllegalArgumentException e) {
			} catch (NullPointerException e) {
			}
		}
	}

	/**
	 * Gets the packet handlers associated with a connection type. Use
	 * <code>SparseArray.get(opcode)</code> to access the handlers.
	 * 
	 * @param type
	 *            the type of connection
	 * @return the packet handlers
	 */
	public static SparseArray<PacketHandler> getHandlers(Type type) {
		return handlers.get(type.ordinal());
	}

	/**
	 * Prototype for a packet Handler
	 * 
	 * @author Daniel
	 */
	public static abstract class PacketHandler {
		protected Type type;
		protected RecvOpcode opcode;

		public abstract void handlePacket(Packet packet, final Network n,
				final HashMap<Network.Activities, Activity> context);
	}
}
