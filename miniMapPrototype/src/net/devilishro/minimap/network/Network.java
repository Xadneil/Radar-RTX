package net.devilishro.minimap.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

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

	private final InetAddress address;
	private final int port;
	private final Activity context;
	private final SparseArray<PacketHandler> handlers;
	private boolean isRunning = false;
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
	 *            a reference to an Activity that
	 *            {@link net.devilishro.minimap.network.PacketHandlers.PacketHandler
	 *            PacketHandler}s can use
	 */
	public Network(Type type, InetAddress address, int port, Activity context) {
		super(type.name());
		this.address = address;
		this.port = port;
		this.context = context;
		handlers = PacketHandlers.getHandlers(type);
		TAG = "Network for " + type.name();
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

	@Override
	public void run() {
		isRunning = true;

		if (net.devilishro.minimap.State.networkBypass) {
			return;
		}

		try {
			socket = new Socket(address, port);
		} catch (IOException e) {
			Log.e(TAG, "Socket Connection Error", e);
			isRunning = false;
			throw new RuntimeException(e);
		}

		byte buffer[] = new byte[BUFFER_SIZE];
		int bytesRead;
		while (isRunning && !socket.isClosed()) {
			try {
				bytesRead = socket.getInputStream().read(buffer);
				if (bytesRead == -1) {
					close();
				}
				// TODO when Packet is implemented
				Packet p = new Packet(buffer, bytesRead, true);
				handlers.get(p.extract_short()).handlePacket(p, socket, context);
			} catch (Exception e) {
				Log.e(TAG, "Socket Receive Error", e);
			}
		}
	}

	/**
	 * Sends a packet to the server.
	 * 
	 * @param p
	 *            the packet
	 */
	public void send(Packet p) {
		if (net.devilishro.minimap.State.networkBypass) {
			return;
		}
		try {
			if (isRunning && !socket.isClosed())
				socket.getOutputStream().write(p.get_packet());
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
	}
}
