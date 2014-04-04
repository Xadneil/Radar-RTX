package net.devilishro.minimap.network;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import net.devilishro.minimap.AppState;
import net.devilishro.minimap.network.PacketHandlers.PacketHandler;
import net.devilishro.minimap.network.PacketHandlers.Type;
import android.app.Activity;
import android.util.Log;
import android.util.SparseArray;

/**
 * A network connection class. Contains functionality for sending and receiving
 * packets.
 * 
 * @author Daniel
 */
public class Network extends Thread {

	private final int BUFFER_SIZE = 1024;

	private final String address;
	private final int port;
	private final HashMap<Activities, Activity> context = new HashMap<Activities, Activity>();
	private final SparseArray<PacketHandler> handlers;
	private boolean isRunning = false, isError = false, hasRun = false;
	private Socket socket;
	private final String TAG;

	/**
	 * Class Constructor
	 * 
	 * @param type
	 *            the {@link net.devilishro.minimap.network.PacketHandlers.Type
	 *            type} of network connection
	 * @param address
	 *            the IP address of the destination
	 * @param port
	 *            the port of the destination
	 * @param context
	 *            a group of references to Activities that
	 *            {@link net.devilishro.minimap.network.PacketHandlers.PacketHandler
	 *            PacketHandler}s can use
	 */
	public Network(Type type, String address, int port) {
		super(type.name());
		this.address = address;
		this.port = port;
		handlers = PacketHandlers.getHandlers(type);
		TAG = "Network for " + type.name();
	}

	public boolean hasRun() {
		return hasRun;
	}

	/**
	 * Returns the running status of this network connection.
	 * 
	 * @return <code>true</code> if the connection is active, <code>false</code>
	 *         otherwise
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * Returns the error status of this network connection.
	 * 
	 * @return <code>true</code> if the connection has errors,
	 *         <code>false</code> otherwise
	 */
	public boolean isError() {
		return isError;
	}

	@Override
	public void run() {
		if (AppState.networkBypass) {
			isRunning = true;
			return;
		}

		try {
			socket = new Socket(address, port);
		} catch (IOException e) {
			Log.e(TAG, "Socket Connection Error", e);
			isRunning = false;
			isError = true;
			throw new RuntimeException(e);
		}

		byte buffer[] = new byte[BUFFER_SIZE];
		int bytesRead;
		isRunning = true;
		hasRun = true;
		while (isRunning && !socket.isClosed()) {
			try {
				bytesRead = socket.getInputStream().read(buffer);
				if (bytesRead == -1) {
					Log.e(TAG, "Stream ended");
					close();
					return;
				} else if (bytesRead == BUFFER_SIZE) {
					Log.e(TAG, "Buffer Size Too Small!");
				}
				byte[] smaller = new byte[bytesRead];
				System.arraycopy(buffer, 0, smaller, 0, bytesRead);
				Packet p = new Packet(smaller, bytesRead, true);
				try {
					short opcode = p.extract_short();
					Log.d(TAG,
							"Got packet opcode " + Integer.toHexString(opcode));
					PacketHandler handler = handlers.get(opcode);
					// spin off a new thread to deal with handling
					new PacketThread(handler, p, this, context).start();
				} catch (Exception e) {
					Log.e(TAG, "Error decoding packet", e);
				}
			} catch (IOException e) {
				Log.e(TAG, "Socket Receive Error", e);
			}
			if (Thread.interrupted()) {
				Log.e(TAG, "Thread Interrupted");
				close();
			}
		}
	}

	private class PacketThread extends Thread {

		private final Packet packet;
		private final Network network;
		private final HashMap<Activities, Activity> context;
		private final PacketHandler handler;

		public PacketThread(PacketHandler handler, Packet packet,
				Network network, HashMap<Activities, Activity> context) {
			super(handler.opcode.name());
			this.handler = handler;
			this.packet = packet;
			this.network = network;
			this.context = context;
		}

		@Override
		public void run() {
			try {
				handler.handlePacket(packet, network, context);
			} catch (Exception e) {
				Log.e(TAG, "Error handling packet " + getName(), e);
			}
		}
	}

	/**
	 * Makes an Activity available to be used in a PacketHandler
	 * 
	 * @param activity
	 *            reference to the activity
	 * @param type
	 *            the type of activity
	 */
	public void registerContext(Activity activity, Activities type) {
		context.put(type, activity);
	}

	/**
	 * Makes an Activity unavailable to be used in a PacketHandler
	 * 
	 * @param type
	 *            the type of Activity to remove
	 */
	public void unregisterContext(Activities type) {
		context.remove(type);
	}

	/**
	 * Sends a packet to the server.
	 * 
	 * @param p
	 *            the packet
	 */
	public void send(Packet p) {
		if (AppState.networkBypass) {
			return;
		}
		try {
			if (isRunning && !socket.isClosed())
				socket.getOutputStream().write(p.get_packet());
			else if (!isRunning) {
				Log.e(TAG, "Message tried to send when network wasn't ready!");
			}
		} catch (IOException e) {
			Log.e(TAG, "Socket Send Error", e);
		}
	}

	/**
	 * Stops the socket listening thread and closes the socket.
	 * 
	 * @author Daniel
	 */
	public void close() {
		isRunning = false;
		try {
			socket.close();
		} catch (Exception e) {
			// ignore any IOException or NullPointerException
		}
		context.clear();
		Log.d(TAG, "Network closed");
	}

	/**
	 * 
	 * @return the map of activities to be used in packet handlers
	 */
	public HashMap<Activities, Activity> getContext() {
		return context;
	}

	/**
	 * Enumeration of Activity types for mapping in the context map
	 * 
	 * @author Daniel
	 */
	public enum Activities {
		LOGIN, EVENT_LIST, EVENT_ADD, TEAM_JOIN, MAP, NOTIFICATION, REPLAY, PLAYER_LIST;
	}
}
