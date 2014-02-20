package edu.usf.cse.minimap.handlers;

import java.net.Socket;

import edu.usf.cse.minimap.LoginActivity;
import edu.usf.cse.minimap.Packet;
import edu.usf.cse.minimap.PacketHandler;
import edu.usf.cse.minimap.State;

public class LoginHandler extends PacketHandler {

    @Override
    public void handlePacket(Packet packet, Socket s) {
        short status = packet.getShort();
        if (status == 401) {
            // TODO login failed
        } else {
            String authID = packet.getString(16);
            State.setAuthID(authID);
            short eventPort = packet.getShort();
            
            State.setLoginOK(true);
            State.setAdmin(status == 201); //201 = admin, 200 = regular
            LoginActivity.mAuthTask.notify(); // wake up the login thread to look at the status
        }
    }
}
