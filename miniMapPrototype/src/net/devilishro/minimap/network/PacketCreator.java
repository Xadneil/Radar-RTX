package net.devilishro.minimap.network;

public class PacketCreator {

	public static Packet login(String email, String password) {
		return loginImpl(SendOpcode.LOGIN, email, password);
	}

	public static Packet register(String email, String password) {
		return loginImpl(SendOpcode.REGISTER, email, password);
	}

	private static Packet loginImpl(Opcode opcode, String email, String password) {
		Packet ret = new Packet(opcode);
		ret.addPaddedString(email, 64);
		ret.addPaddedString(password, 64);
		return ret;
	}
}
