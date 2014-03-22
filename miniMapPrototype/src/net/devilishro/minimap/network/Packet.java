package net.devilishro.minimap.network;

/**
 * Dummy packet class
 * 
 * @author Daniel
 * 
 */
public class Packet {

	public Packet() {

	}

	public Packet(Opcode opcode) {

	}

	public void addPaddedString(String s, int pad) {

	}

	public void addInt(int number) {

	}

	public int getInt() {
		return 0;
	}

	public byte getByte() {
		return (byte) 0;
	}

	public short getOpcode() {
		return (byte) 0;
	}

	public short getShort() {
		return (short) 0;
	}

	public double getDouble() {
		return 0.0;
	}

	public float getFloat() {
		return 0f;
	}

	public String getString() {
		return "";
	}

	public byte[] getPacket() {
		return new byte[1];
	}
}
