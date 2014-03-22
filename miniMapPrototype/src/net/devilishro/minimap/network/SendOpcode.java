package net.devilishro.minimap.network;

public enum SendOpcode implements Opcode {

    LOGIN(0xa1), REGISTER(0xa3), MAP_STATE(0xb1);

    SendOpcode(int value) {
        this.value = value;
    }
    private final int value;

    public int getValue() {
        return value;
    }
}