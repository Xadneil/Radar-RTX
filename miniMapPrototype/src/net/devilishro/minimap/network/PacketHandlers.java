package net.devilishro.minimap.network;

import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;

import net.devilishro.minimap.Minimap;
import net.devilishro.minimap.State;
import android.app.Activity;
import android.util.SparseArray;

/**
 * Defines and aggregates {@link net.devilishro.minimap.network.PacketHandlers.PacketHandler PacketHandler}s
 * @author Daniel
 */
public class PacketHandlers {

	private static int numHandlers[] = new int[Type.values().length];
	private static ArrayList<SparseArray<PacketHandler>> handlers = new ArrayList<SparseArray<PacketHandler>>(
			Type.values().length);

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
	 * Gets the packet handlers associated with a connection type
	 * @param type the type of connection
	 * @return the packet handlers
	 */
	public static SparseArray<PacketHandler> getHandlers(Type type) {
		return handlers.get(type.ordinal());
	}

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
			if (status == 401) {
				// Login failed
				activity.UIupdate.obtainMessage(2).sendToTarget();
			} else {
				String authID = packet.getString(); // get auth id
				State.setAuthID(authID);

				State.setLoginOK(true);
				State.setAdmin(status == 201); // 201 = admin, 200 = regular
				activity.startEventActivity(); // go from login screen to event screen
			}
		}
	};

	/**
	 * Prototype for a packet Handler
	 * @author Daniel
	 */
	public static abstract class PacketHandler {
		protected Type type;
		protected Opcode opcode;

		public abstract void handlePacket(Packet packet, Socket s,
				Activity context);
	}

	/**
	 * The type of network connection. Used for grouping packet handlers by
	 * destination and naming network connections.
	 * 
	 * @author Daniel
	 */
	public enum Type {
		LOGIN, /* FIELD, MAP */;
	}
}
