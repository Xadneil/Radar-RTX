package net.devilishro.minimap.network;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.android.gms.maps.model.LatLng;

import net.devilishro.minimap.AppState;
import net.devilishro.minimap.EventActivity;
import net.devilishro.minimap.EventActivity.Event;
import net.devilishro.minimap.Minimap;
import net.devilishro.minimap.network.Network.Activities;
import android.app.Activity;
import android.os.Message;
import android.util.SparseArray;

/**
 * Defines and aggregates
 * {@link net.devilishro.minimap.network.PacketHandlers.PacketHandler
 * PacketHandler}s
 * 
 * @author Daniel
 */
public class PacketHandlers {

	private static int numHandlers[] = new int[Type.values().length];
	private static ArrayList<SparseArray<PacketHandler>> handlers = new ArrayList<SparseArray<PacketHandler>>(
			Type.values().length);

	/**
	 * The type of network connection. Used for grouping packet handlers by
	 * destination and naming network connections.
	 * 
	 * @author Daniel
	 */
	public enum Type {
		LOGIN, EVENT, /* MAP */; // Disable unused type(s) for safety
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
			short status = packet.extract_short();
			Minimap activity = (Minimap) context.get(Activities.LOGIN);
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
				// no break intended
			case 0x1D9:
				// User login
				AppState.setLoginOK(true);

				if (!AppState.getEventServer().isRunning()) {
					AppState.getEventServer().start();
				}
				AppState.getEventServer().registerContext(activity,
						Activities.LOGIN);
				// This thread sends packets when the event server connection is
				// ready.
				new Thread() {
					public void run() {
						while (!AppState.getEventServer().isRunning()
								&& !AppState.getEventServer().isError()) {
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
							}
						}
						AppState.getEventServer().send(
								PacketCreator.eventInit());
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
			short status = packet.extract_short();
			Minimap activity = (Minimap) context.get(Activities.LOGIN);

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
					String message = packet.extract_string();
					short type = packet.extract_short();
					Event event = new Event();
					event.id = id;
					event.title = title;
					event.message = message;
					event.type = type;
					AppState.getEvents()[i] = event;
				}
			} else {
				AppState.setEventNumber(1);
				AppState.getEvents()[0] = new EventActivity.Event(0, "Test Event",
						"Provider", new LatLng(28.059891, -82.416183), 17.0f);
				// TODO is this right here?
				AppState.setAdmin(true);
			}
			Minimap activity = (Minimap) context.get(Activities.LOGIN);
			if (activity != null) {
				activity.startEventActivity(); // go from login screen to event
				// release reference for leak prevention
				n.unregisterContext(Activities.LOGIN);
			} else {
				//TODO here
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
			if (!AppState.networkBypass) {
				short status = packet.extract_short();
				if (/* status == some value */true) {
					// TODO team1, team2
				}
			} else
				((EventActivity) context.get(Activities.EVENT_LIST))
						.startJoinActivity();

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
			short whichTeam = packet.extract_short();
			// TODO get add/delete
			int numPlayers = packet.extract_int();
			for (int i = 0; i < numPlayers; i++) {
				String playerName = packet.extract_string();
				AppState.getTeamNames(whichTeam)[i] = playerName;
				// TODO possibly update UI?
			}
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
			short status = packet.extract_short();
			// TODO process status code
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
			if (!AppState.networkBypass) {
				short status = packet.extract_short();
				// TODO process status code
			}
			AppState.getEventServer().send(PacketCreator.requestEventList());
			if (AppState.networkBypass) {
				eventList.handlePacket(null, n, context);
			}
		}
	};

	// =========================================================================
	// end packet handlers
	// =========================================================================

	/**
	 * Class Initializer. Should happen after static field initialization
	 */
	static {
		Field fields[] = PacketHandlers.class.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			try {
				Object field = fields[i].get(null);
				if (field instanceof PacketHandler) {
					PacketHandler handler = (PacketHandler) field;
					numHandlers[handler.type.ordinal()]++;
				}
			} catch (IllegalAccessException e) {
			} catch (IllegalArgumentException e) {
			} catch (NullPointerException e) {
			}
		}
		for (int type = 0; type < Type.values().length; type++) {
			handlers.add(new SparseArray<PacketHandler>(numHandlers[type]));
		}

		for (int i = 0; i < fields.length; i++) {
			try {
				Object field = fields[i].get(null);
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
		protected Opcode opcode;

		public abstract void handlePacket(Packet packet, final Network n,
				final HashMap<Network.Activities, Activity> context);
	}
}
