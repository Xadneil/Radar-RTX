package edu.usf.cse.minimap;

import java.util.ArrayList;

public final class Packet extends ArrayList<Byte> {
    private static final long serialVersionUID = 1L;
    int position = 1;

    public Packet(Opcode opcode) {
        super();
        super.add(opcode.getValue());
    }

    public Packet() {
        super();
    }

    public Packet(byte[] data) {
        super();
        for (byte b : data) {
            super.add(b);
        }
    }

    public void addByte(byte b) {
        super.add(b);
    }

    public void addInt(int i) {
        super.add((byte) (i & 0xFF));
        super.add((byte) ((i >>> 8) & 0xFF));
        super.add((byte) ((i >>> 16) & 0xFF));
        super.add((byte) ((i >>> 24) & 0xFF));
    }

    public void addBytes(byte[] a) {
        for (int i = 0; i < a.length; i++) {
            super.add(a[i]);
        }
    }

    public void addString(String s) {
        addInt(s.length());
        addBytes(s.getBytes());
    }

    /**
     * Adds a string into a zero padded packet area
     * @param s the string to add
     * @param length the total length of the packet area
     */
    public void addPaddedString(String s, int length) {
        byte bytes[] = s.getBytes();
        byte temp[] = new byte[length];
        System.arraycopy(bytes, 0, temp, 0, bytes.length);
        for (int i = bytes.length; i < length; i++) {
            temp[i] = 0;
        }
        addBytes(bytes);
    }

    public byte[] toByteArray() {
        byte[] ret = new byte[size()];
        for (int i = 0; i < size(); i++) {
            ret[i] = get(i);
        }
        return ret;
    }

    public byte getOpcode() {
        return get(0);
    }

    public long getLong() {
        long byte1 = getByte();
        long byte2 = getByte();
        long byte3 = getByte();
        long byte4 = getByte();
        long byte5 = getByte();
        long byte6 = getByte();
        long byte7 = getByte();
        long byte8 = getByte();
        return (byte8 << 56) + (byte7 << 48) + (byte6 << 40) + (byte5 << 32) + (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
    }

    public float getFloat() {
        return Float.intBitsToFloat(getInt());
    }

    public double getDouble() {
        return Double.longBitsToDouble(getLong());
    }
    
    public String getString() {
        int size = getInt();
        int termination = size + position;
        byte[] temp = new byte[size];
        int i = 0;
        while (position < termination) {
            temp[i] = get(position);
            position++;
            i++;
        }
        return new String(temp);
    }

    public String getString(int size) {
        byte[] temp = new byte[size];
        for (int i = 0; i < size; i++) {
            temp[i] = getByte();
        }
        return new String(temp);
    }

    public int getInt() {
        int i0 = getByte();
        int i1 = getByte();
        int i2 = getByte();
        int i3 = getByte();
        return i0 + i1 + i2 + i3;
    }

    public byte getByte() {
        return get(position++);
    }

    public short getShort() {
        int i0 = getByte();
        int i1 = getByte() >> 8;
        return (short) (i0 + i1);
    }

    public void skip(int n) {
        position += n;
    }

    public boolean getBoolean() {
        return getByte() == 1;
    }
}