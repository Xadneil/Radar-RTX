package net.devilishro.minimap.network;

public enum RecvOpcode implements Opcode {

	LOGIN(0xa2), REGISTER(0xa4), MAP_UPDATE(0xb2), EVENT_LIST(0xe2), EVENT_CHOOSE(
			0xe4), EVENT_ADD(0xe6), PLAYER_LIST_UPDATE(0xe8), EVENT_LEAVE(0xe9), EVENT_SERVER_RESPONSE(0xeb);

	RecvOpcode(int value) {
		this.value = (short) value;
	}

	private final short value;

	public short getValue() {
		return value;
	}
}