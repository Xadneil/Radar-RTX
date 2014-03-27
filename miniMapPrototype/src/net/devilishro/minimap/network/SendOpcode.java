package net.devilishro.minimap.network;

public enum SendOpcode implements Opcode {

	LOGIN(0xa1), REGISTER(0xa3), LOGOUT(0xa5), MAP_STATE(0xb1), SELECT_EVENT(
			0xe3), EVENT_LEAVE(0xe5), EVENT_LIST_REQUEST(0xe7), EVENT_SERVER_INIT(0xea);

	SendOpcode(int value) {
		this.value = (short) value;
	}

	private final short value;

	public short getValue() {
		return value;
	}
}