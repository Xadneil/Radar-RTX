package edu.usf.cse.minimap;

import java.net.Socket;

public abstract class PacketHandler {

    public abstract void handlePacket(Packet packet, Socket s);
}