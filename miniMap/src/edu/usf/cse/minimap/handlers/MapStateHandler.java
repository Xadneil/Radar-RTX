package edu.usf.cse.minimap.handlers;

import java.net.Socket;

import com.google.android.gms.maps.model.LatLng;

import edu.usf.cse.minimap.MapActivity;
import edu.usf.cse.minimap.Packet;
import edu.usf.cse.minimap.PacketHandler;
import edu.usf.cse.minimap.State;

public class MapStateHandler extends PacketHandler {

    @Override
    public void handlePacket(Packet packet, Socket s) {
        boolean update = false;
        boolean hasPositions, hasPing, hasDraw;
        hasPositions = packet.getBoolean();
        hasPing = packet.getBoolean();
        hasDraw = packet.getBoolean();
        if (hasPositions) {
            synchronized (State.getPositionsLock()) {
                State.setHasNewPositions(true);
                for (int i = 0; i < packet.getInt() /* number of positions */; i++) {
                    int number = packet.getInt();
                    double latitude = packet.getDouble();
                    double longitude = packet.getDouble();
                    LatLng l = new LatLng(latitude, longitude);
                    State.getPositions()[number] = l;
                    update = true;
                }
            }
        }
        if (hasPing) {
            update = true;
            synchronized (State.getPingLock()) {
                double latitude = packet.getDouble();
                double longitude = packet.getDouble();
                State.setPing(new LatLng(latitude, longitude));
                State.setHasNewPing(true);
            }
        }
        if (hasDraw) {

        }
        if (update) {
            MapActivity.getHandler().sendEmptyMessage(0);
        }
    }

}
