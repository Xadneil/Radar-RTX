package edu.usf.cse.minimap;

public class PacketCreator {

    public static Packet login(String email, String password) {
        Packet ret = new Packet(SendOpcode.LOGIN);
        ret.addPaddedString(email, 64);
        ret.addPaddedString(password, 64);
        return ret;
    }
}
