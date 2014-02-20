package edu.usf.cse.minimap;

import java.io.IOException;
import java.net.Socket;

public class SocketReader extends Thread {

    private final Socket socket;
    private boolean active = false;

    public SocketReader(Socket s) {
        super(s.getInetAddress().getHostAddress() + " reader");
        this.socket = s;
    }

    public Socket getSocket() {
        return socket;
    }

    public void start() {
        active = true;
        super.start();
    }

    public void close() {
        active = false;
        try {
            socket.close();
        } catch (IOException ex) {
        }
    }

    public void run() {
        while (active) {
            byte[] buffer = new byte[4096];
            boolean dc;
            try {
                dc = socket.getInputStream().read(buffer) == -1;
            } catch (IOException ex) {
                close();
                break;
            }
            if (!dc) {
                delegatePacket(new Packet(buffer), socket);
            } else {
                close();
                //ChatServer.getInstance().remove(name);
            }
        }
    }

    private void delegatePacket(final Packet packet, final Socket s) {
        new Thread(socket.getInetAddress().getHostAddress() + " processor") {

            public void run() {
                PacketProcessor.getProcessor().getHandler(packet.getOpcode()).handlePacket(packet, s);
            }
        }.start();
    }
}