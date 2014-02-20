package edu.usf.cse.minimap;

public enum SendOpcode implements Opcode {

    LOGIN(0xa1), MAP_STATE(0xb1);

    SendOpcode(int value) {
        this.value = value;
    }
    private final int value;

    public byte getValue() {
        return (byte) value;
    }
}