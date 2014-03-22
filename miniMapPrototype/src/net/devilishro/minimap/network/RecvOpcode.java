package net.devilishro.minimap.network;

public enum RecvOpcode implements Opcode {

    LOGIN(0xa2), MAP_UPDATE(0xb2);

    RecvOpcode(int value) {
        this.value = value;
    }
    private final int value;

    public int getValue() {
        return value;
    }
}