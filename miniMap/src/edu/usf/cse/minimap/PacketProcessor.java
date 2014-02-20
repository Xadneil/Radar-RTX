package edu.usf.cse.minimap;

import edu.usf.cse.minimap.handlers.*;

public class PacketProcessor {

    private static PacketProcessor instance;
    private PacketHandler[] handlers;

    private PacketProcessor() {
        int maxRecvOp = 0;
        for (RecvOpcode op : RecvOpcode.values()) {
            if (op.getValue() > maxRecvOp) {
                maxRecvOp = op.getValue();
            }
        }
        handlers = new PacketHandler[maxRecvOp + 1];
    }

    public PacketHandler getHandler(byte packetId) {
        if (packetId > handlers.length) {
            return null;
        }
        PacketHandler handler = handlers[packetId];
        if (handler != null) {
            return handler;
        }
        return null;
    }

    public void registerHandler(RecvOpcode code, PacketHandler handler) {
        try {
            handlers[code.getValue()] = handler;
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    public synchronized static PacketProcessor getProcessor() {
        if (instance == null) {
            instance = new PacketProcessor();
            instance.reset();
        }
        return instance;
    }

    public void reset() {
        handlers = new PacketHandler[handlers.length];
        registerHandler(RecvOpcode.LOGIN, new LoginHandler());
        registerHandler(RecvOpcode.MAP_UPDATE, new MapStateHandler());
    }
}