package net.devilishro.minimap.network;

import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;

import net.devilishro.minimap.EventActivity;
import net.devilishro.minimap.EventActivity.Event;
import net.devilishro.minimap.Minimap;
import net.devilishro.minimap.State;
import android.app.Activity;
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
		LOGIN, EVENT, /* MAP */;
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
		public void handlePacket(Packet packet, Socket s, Activity context) {
			short status = packet.getShort();
			Minimap activity = (Minimap) context;
			switch (status) {
			case 401:
				// Login failed
				activity.UIupdate.obtainMessage(2).sendToTarget();
				break;
			case 471:
				// Already Logged In
				activity.UIupdate.obtainMessage(3).sendToTarget();
				break;
			case 201:
				// Admin login
				State.setAdmin(true);
			case 200:
				// User login
				String authID = packet.getString(); // get auth id
				State.setAuthID(authID);
				State.setLoginOK(true);
				activity.startEventActivity(); // go from login screen to event
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
		public void handlePacket(Packet packet, Socket s, Activity context) {
			short status = packet.getShort();
			Minimap activity = (Minimap) context;
			switch (status) {
			case 128:
				// Registration failure
				activity.UIupdate.obtainMessage(0).sendToTarget();
				break;
			case 137:
				// Registration success
				activity.UIupdate.obtainMessage(1).sendToTarget();
				break;
			}
		}
	};

	public static PacketHandler eventList = new PacketHandler() {
		{
			type = Type.EVENT;
			opcode = RecvOpcode.EVENT_LIST;
		}

		@Override
		public void handlePacket(Packet packet, Socket s, Activity context) {
			int numEvents = packet.getShort();
			State.setEventNumber(numEvents);
			for (int i = 0; i < numEvents; i++) {
				int id = packet.getInt();
				String title = packet.getString();
				String provider = packet.getString();
				// TODO decide if following info is in event server or map
				// server
				double latitude = packet.getDouble();
				double longitude = packet.getDouble();
				float zoom = packet.getFloat();
				Event event = new Event(id, title, provider, new LatLng(
						latitude, longitude), zoom);
				State.getEvents()[i] = event;
			}
		}
	};

	public static PacketHandler eventChoose = new PacketHandler() {
		{
			type = Type.EVENT;
			opcode = RecvOpcode.EVENT_CHOOSE;
		}

		@Override
		public void handlePacket(Packet packet, Socket s, Activity context) {
			// map port
			packet.getShort();
			((EventActivity) context).startJoinActivity();
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

		public abstract void handlePacket(Packet packet, Socket s,
				Activity context);
	}
}
