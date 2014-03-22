package net.devilishro.minimap.network;

public enum RecvOpcode implements Opcode {

    LOGIN(0xa2), REGISTER(0xa4), MAP_UPDATE(0xb2), EVENT_LIST(0xe2), EVENT_CHOOSE(0xe4);

    RecvOpcode(int value) {
        this.value = value;
    }
    private final int value;

    public int getValue() {
        return value;
    }
}