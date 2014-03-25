package net.devilishro.minimap.network;


/**
 * file: Packet.java desc: Java Packet class corresponding to Python struct
 * module packed byte sequence goal:<br>
 * [OK] pack java primitive types<br>
 * [OK] little or big endian support<br>
 * [OK] fast packet consumption (multiplex)<br>
 * [OK] python-to-java primitive data type (struct module translation)
 * 
 * @author trickyloki3
 */
public class Packet {
	private byte[] packet; // packet byte sequence
	private int packet_size; // packet size
	private int packet_consume; // current packet position byte
	private boolean packet_big_endian; // packet endianness

	// unpacking constructor
	public Packet(byte[] packet, int packet_size, boolean big_endian) {
		this.packet = new byte[packet_size]; // Allocate heap memory for packet
		// Copy packet into packet object
		System.arraycopy(packet, 0, this.packet, 0, packet_size); 
		this.packet_size = packet_size; // Set size
		this.packet_consume = 0; // Set consume
		this.packet_big_endian = big_endian;
	}

	// packing constructor
	public Packet(int packet_size) {
		this.packet = new byte[packet_size];
		this.packet_size = packet_size;
		this.packet_consume = 0;
		packet_big_endian = true;
	}

	byte[] get_packet() {
		return this.packet;
	}

	void packet_status() {
		System.out.println("info: packet size: " + packet_size);
		System.out.println("info: packet consumed: " + packet_consume);
		System.out.println("info: packet big endian: " + packet_big_endian);
		System.out.print("info: ");
		for (int i = 0; i < packet_size; i++)
			System.out.print(((int) packet[i] & 0xff) + " ");
		System.out.println();
	}

	short extract_short() {
		this.packet_consume += 2; // Consume two byte
		if (packet_big_endian)
			return (short) ( // Cast to short
			(this.packet[packet_consume - 2] & 0xff) << 8 | // MSB byte 1
			(this.packet[packet_consume - 1] & 0xff) // LSB byte 0
			);
		else
			return (short) ( // Cast to short
			(this.packet[packet_consume - 2] & 0xff) | // LSB byte 0
			(this.packet[packet_consume - 1] & 0xff) << 8 // MSB byte 1
			);
	}

	void pack_short(short value) {
		this.packet_consume += 2; // Consume two byte
		this.packet[packet_consume - 2] = (byte) (value >> 8 & 0xff);
		this.packet[packet_consume - 1] = (byte) (value & 0xff);
	}

	int extract_int() {
		this.packet_consume += 4; // Consume four byte
		if (packet_big_endian)
			return (int) ( // Cast to int
			(this.packet[packet_consume - 4] & 0xff) << 24 | // MSB byte 3
					(this.packet[packet_consume - 3] & 0xff) << 16 | // BBB byte
																		// 2
					(this.packet[packet_consume - 2] & 0xff) << 8 | // BBB byte
																	// 1
			(this.packet[packet_consume - 1] & 0xff) // LSB byte 0
			);
		else
			return (int) ( // Cast to int
			(this.packet[packet_consume - 4] & 0xff) | // lSB byte 0
					(this.packet[packet_consume - 3] & 0xff) << 8 | // BBB byte
																	// 1
					(this.packet[packet_consume - 2] & 0xff) << 16 | // BBB byte
																		// 2
			(this.packet[packet_consume - 1] & 0xff) << 24 // mSB byte 3
			);
	}

	void pack_int(int value) {
		this.packet_consume += 4;
		this.packet[packet_consume - 4] = (byte) (value >> 24 & 0xff);
		this.packet[packet_consume - 3] = (byte) (value >> 16 & 0xff);
		this.packet[packet_consume - 2] = (byte) (value >> 8 & 0xff);
		this.packet[packet_consume - 1] = (byte) (value & 0xff);
	}

	long extract_long() {
		this.packet_consume += 8; // Consume eight byte
		if (packet_big_endian)
			return (long) ( // Cast to long (standard default to int)
			(long) (this.packet[packet_consume - 8] & 0xff) << 56 | // MSB byte
																	// 7
					(long) (this.packet[packet_consume - 7] & 0xff) << 48 | // BBB
																			// byte
																			// 6
					(long) (this.packet[packet_consume - 6] & 0xff) << 40 | // BBB
																			// byte
																			// 5
					(long) (this.packet[packet_consume - 5] & 0xff) << 32 | // BBB
																			// byte
																			// 4
					(long) (this.packet[packet_consume - 4] & 0xff) << 24 | // BBB
																			// byte
																			// 3
					(long) (this.packet[packet_consume - 3] & 0xff) << 16 | // BBB
																			// byte
																			// 2
					(long) (this.packet[packet_consume - 2] & 0xff) << 8 | // BBB
																			// byte
																			// 1
			(long) (this.packet[packet_consume - 1] & 0xff) // LSB byte 0
			);
		else
			return (long) ( // Cast to long (standard default to int)
			(long) (this.packet[packet_consume - 8] & 0xff) | // lSB byte 0
					(long) (this.packet[packet_consume - 7] & 0xff) << 8 | // BBB
																			// byte
																			// 1
					(long) (this.packet[packet_consume - 6] & 0xff) << 16 | // BBB
																			// byte
																			// 2
					(long) (this.packet[packet_consume - 5] & 0xff) << 24 | // BBB
																			// byte
																			// 3
					(long) (this.packet[packet_consume - 4] & 0xff) << 32 | // BBB
																			// byte
																			// 4
					(long) (this.packet[packet_consume - 3] & 0xff) << 40 | // BBB
																			// byte
																			// 5
					(long) (this.packet[packet_consume - 2] & 0xff) << 48 | // BBB
																			// byte
																			// 6
			(long) (this.packet[packet_consume - 1] & 0xff) << 56 // MSB byte 7
			);
	}

	void pack_long(long value) {
		this.packet_consume += 8;
		this.packet[packet_consume - 8] = (byte) (value >> 56 & 0xff);
		this.packet[packet_consume - 7] = (byte) (value >> 48 & 0xff);
		this.packet[packet_consume - 6] = (byte) (value >> 40 & 0xff);
		this.packet[packet_consume - 5] = (byte) (value >> 32 & 0xff);
		this.packet[packet_consume - 4] = (byte) (value >> 24 & 0xff);
		this.packet[packet_consume - 3] = (byte) (value >> 16 & 0xff);
		this.packet[packet_consume - 2] = (byte) (value >> 8 & 0xff);
		this.packet[packet_consume - 1] = (byte) (value & 0xff);
	}

	float extract_float() {
		this.packet_consume += 4;
		if (packet_big_endian)
			return Float
					.intBitsToFloat((this.packet[packet_consume - 4] & 0xff) << 24
							| // MSB byte 3
							(this.packet[packet_consume - 3] & 0xff) << 16 | // BBB
																				// byte
																				// 2
							(this.packet[packet_consume - 2] & 0xff) << 8 | // BBB
																			// byte
																			// 1
							(this.packet[packet_consume - 1] & 0xff) // LSB byte
																		// 0
					);
		else
			return Float
					.intBitsToFloat((this.packet[packet_consume - 4] & 0xff) | // LSB
																				// byte
																				// 0
							(this.packet[packet_consume - 3] & 0xff) << 8 | // BBB
																			// byte
																			// 1
							(this.packet[packet_consume - 2] & 0xff) << 16 | // BBB
																				// byte
																				// 2
							(this.packet[packet_consume - 1] & 0xff) << 24 // MSB
																			// byte
																			// 3
					);
	}

	void pack_float(float value) {
		int fvalue = Float.floatToIntBits(value);
		this.packet_consume += 4;
		this.packet[packet_consume - 4] = (byte) (fvalue >> 24 & 0x7fffffff);
		this.packet[packet_consume - 3] = (byte) (fvalue >> 16 & 0x00ffffff);
		this.packet[packet_consume - 2] = (byte) (fvalue >> 8 & 0x0000ffff);
		this.packet[packet_consume - 1] = (byte) (fvalue & 0x000000ff);
	}

	double extract_double() {
		this.packet_consume += 8;
		if (packet_big_endian)
			return Double
					.longBitsToDouble((long) (this.packet[packet_consume - 8] & 0xff) << 56
							| // MSB byte 7
							(long) (this.packet[packet_consume - 7] & 0xff) << 48
							| // BBB byte 6
							(long) (this.packet[packet_consume - 6] & 0xff) << 40
							| // BBB byte 5
							(long) (this.packet[packet_consume - 5] & 0xff) << 32
							| // BBB byte 4
							(long) (this.packet[packet_consume - 4] & 0xff) << 24
							| // BBB byte 3
							(long) (this.packet[packet_consume - 3] & 0xff) << 16
							| // BBB byte 2
							(long) (this.packet[packet_consume - 2] & 0xff) << 8
							| // BBB byte 1
							(long) (this.packet[packet_consume - 1] & 0xff) // LSB
																			// byte
																			// 0
					);
		else
			return Double
					.longBitsToDouble((long) (this.packet[packet_consume - 8] & 0xff)
							| // lSB byte 0
							(long) (this.packet[packet_consume - 7] & 0xff) << 8
							| // BBB byte 1
							(long) (this.packet[packet_consume - 6] & 0xff) << 16
							| // BBB byte 2
							(long) (this.packet[packet_consume - 5] & 0xff) << 24
							| // BBB byte 3
							(long) (this.packet[packet_consume - 4] & 0xff) << 32
							| // BBB byte 4
							(long) (this.packet[packet_consume - 3] & 0xff) << 40
							| // BBB byte 5
							(long) (this.packet[packet_consume - 2] & 0xff) << 48
							| // BBB byte 6
							(long) (this.packet[packet_consume - 1] & 0xff) << 56 // mSB
																					// byte
																					// 7
					);
	}

	void pack_double(double value) {
		long dvalue = Double.doubleToLongBits(value);
		this.packet_consume += 8;
		this.packet[packet_consume - 8] = (byte) (dvalue >> 56 & 0xff);
		this.packet[packet_consume - 7] = (byte) (dvalue >> 48 & 0xff);
		this.packet[packet_consume - 6] = (byte) (dvalue >> 40 & 0xff);
		this.packet[packet_consume - 5] = (byte) (dvalue >> 32 & 0xff);
		this.packet[packet_consume - 4] = (byte) (dvalue >> 24 & 0xff);
		this.packet[packet_consume - 3] = (byte) (dvalue >> 16 & 0xff);
		this.packet[packet_consume - 2] = (byte) (dvalue >> 8 & 0xff);
		this.packet[packet_consume - 1] = (byte) (dvalue & 0xff);
	}

	String extract_string() {
		// strings are sent byte-order neutral (?)
		int strlen = extract_int(); // extract string length
		byte[] packet_string = new byte[strlen]; // extract string bytes
		for (int i = this.packet_consume; i < this.packet_consume + strlen; i++)
			packet_string[i - this.packet_consume] = this.packet[i];
		this.packet_consume += strlen; // consume string length and bytes
		return new String(packet_string);
	}

	void pack_string(String value) {
		byte[] svalue = value.getBytes();
		pack_int(svalue.length);
		for (int i = 0; i < svalue.length; i++)
			this.packet[packet_consume + i] = svalue[i];
		this.packet_consume += svalue.length;
	}
}