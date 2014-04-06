package net.devilishro.minimap.network;

public enum RecvOpcode implements Opcode {

	LOGIN(0xa2), REGISTER(0xa4), EVENT_ADD(0xe2), EVENT_LIST(0xe4), EVENT_DISCONNECT(
			0xed), EVENT_CHOOSE(0xe6), TEAM_LIST_UPDATE(0xe7), EVENT_LEAVE(0xe9), EVENT_NOTIFICATION_CREATE(
			0xef), NOTIFICATION(0xf2), PLAYER_LIST(0xf4), PLAYER_INFO(0xf6), TEAM_JOIN(
			0xf8), FRIEND(0xfa), EVENT_SERVER_RESPONSE(0xeb), FIELD_CONNECT(
			0xb2), FIELD_PLAYER_ADD(0xb3), FIELD_PLAYER_DEL(0xb4), FIELD_DISCONNECT(
			0xb6), FIELD_UPDATE(0xb8);

	RecvOpcode(int value) {
		this.value = (short) value;
	}

	private final short value;

	public short getValue() {
		return value;
	}
}