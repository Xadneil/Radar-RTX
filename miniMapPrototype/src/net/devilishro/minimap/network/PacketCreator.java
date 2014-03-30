package net.devilishro.minimap.network;

import net.devilishro.minimap.AppState;

/**
 * Class for creating packets to send to the servers
 * 
 * @author Daniel
 */
public class PacketCreator {

	/**
	 * Creates a login packet
	 * 
	 * @param email
	 *            the email used to log in
	 * @param password
	 *            the password used to log in
	 * @return the login packet
	 */
	public static Packet login(String email, String password) {
		return loginImpl(SendOpcode.LOGIN, email, password);
	}

	/**
	 * Creates a registration packet
	 * 
	 * @param email
	 *            the email used to register
	 * @param password
	 *            the password used to register
	 * @return the login packet
	 */
	public static Packet register(String email, String password) {
		return loginImpl(SendOpcode.REGISTER, email, password);
	}

	private static Packet loginImpl(Opcode opcode, String email, String password) {
		Packet ret = new Packet(4 + email.getBytes().length + 4
				+ password.getBytes().length + 2);
		ret.pack_short(opcode.getValue());
		ret.pack_string(email);
		ret.pack_string(password);
		return ret;
	}

	/**
	 * Creates a logout packet containing the email and auth id
	 * 
	 * @param email
	 * @return the logout packet
	 */
	public static Packet logout() {
		Packet ret = new Packet(
				2 + 4 + AppState.getUsername().getBytes().length);
		ret.pack_short(SendOpcode.LOGOUT.getValue());
		ret.pack_string(AppState.getUsername());
		return ret;
	}

	/**
	 * Creates a packet for choosing an event
	 * 
	 * @param id
	 *            the event id
	 * @return the event-choosing packet
	 */
	public static Packet selectEvent(int id) {
		Packet ret = new Packet(2 + 4);
		ret.pack_short(SendOpcode.SELECT_EVENT.getValue());
		ret.pack_int(id);
		return ret;
	}

	/**
	 * Creates a packet for adding an event
	 * 
	 * @param name
	 *            event title
	 * @param team1
	 *            team 1 name
	 * @param team2
	 *            team 2 name
	 * @param type
	 *            type of event
	 * @param message
	 *            event details
	 * @return the event-adding packet
	 */
	public static Packet addEvent(String name, String team1, String team2,
			int type, String message) {
		Packet ret = new Packet(2 + 4 + name.getBytes().length + 4
				+ team1.getBytes().length + 4 + team2.getBytes().length + 4 + 4
				+ message.getBytes().length);
		ret.pack_short(SendOpcode.EVENT_ADD.getValue());
		ret.pack_string(name);
		ret.pack_string(team1);
		ret.pack_string(team2);
		ret.pack_int(type);
		ret.pack_string(message);
		return ret;
	}

	/**
	 * Creates a simple packet that requests the event list
	 * 
	 * @return the event list request packet
	 */
	public static Packet requestEventList() {
		Packet ret = new Packet(2);
		ret.pack_short(SendOpcode.EVENT_LIST_REQUEST.getValue());
		return ret;
	}

	/**
	 * Creates a packet for leaving an event
	 * 
	 * @param id
	 *            the event id
	 * @return the packet for leaving an event
	 */
	public static Packet leaveEvent(int id) {
		Packet ret = new Packet(2 + 4);
		ret.pack_short(SendOpcode.EVENT_LEAVE.getValue());
		ret.pack_int(id);
		return ret;
	}

	/**
	 * Creates a simple packet for initializing the event server
	 * 
	 * @return the event server packet
	 */
	public static Packet eventConnect() {
		Packet ret = new Packet(
				2 + 4 + AppState.getUsername().getBytes().length);
		ret.pack_short(SendOpcode.EVENT_SERVER_CONNECT.getValue());
		ret.pack_string(AppState.getUsername());
		return ret;
	}

	/**
	 * Creates a packet to update the server of your position
	 * 
	 * @param lat
	 *            latitude
	 * @param lng
	 *            longitude
	 * @return the update packet
	 */
	public static Packet reportLocation(double lat, double lng) {
		Packet ret = new Packet(2 + 8 + 8);
		ret.pack_short(SendOpcode.MAP_STATE.getValue());
		ret.pack_double(lat);
		ret.pack_double(lng);
		return ret;
	}
}
