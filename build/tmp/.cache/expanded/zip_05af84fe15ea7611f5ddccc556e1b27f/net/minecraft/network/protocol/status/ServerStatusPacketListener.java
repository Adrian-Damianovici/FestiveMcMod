package net.minecraft.network.protocol.status;

import net.minecraft.network.protocol.game.ServerPacketListener;

/**
 * PacketListener for the server side of the STATUS protocol.
 */
public interface ServerStatusPacketListener extends ServerPacketListener {
   void m_7883_(ServerboundPingRequestPacket p_134986_);

   void handleStatusRequest(ServerboundStatusRequestPacket pPacket);
}