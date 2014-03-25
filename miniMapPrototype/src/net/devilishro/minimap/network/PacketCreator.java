package net.devilishro.minimap.network;

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
		Packet ret = new Packet(4 + email.length() + 4 + password.length() + 2);
		ret.pack_short((short) opcode.getValue());
		ret.pack_string(email);
		ret.pack_string(password);
		return ret;
	}

	/**
	 * Creates a packet for choosing an event
	 * 
	 * @param id
	 *            the event id
	 * @return the event choosing packet
	 */
	public static Packet selectEvent(int id) {
		Packet ret = new Packet(2 + 4);
		ret.pack_short((short) SendOpcode.SELECT_EVENT.getValue());
		ret.pack_int(id);
		return ret;
	}
}
