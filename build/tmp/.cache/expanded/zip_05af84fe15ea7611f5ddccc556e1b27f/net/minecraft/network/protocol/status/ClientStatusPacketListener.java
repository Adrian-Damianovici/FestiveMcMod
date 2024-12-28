package net.minecraft.network.protocol.status;

import net.minecraft.network.PacketListener;

/**
 * PacketListener for the client side of the STATUS protocol.
 */
public interface ClientStatusPacketListener extends PacketListener {
   void handleStatusResponse(ClientboundStatusResponsePacket pPacket);

   void m_7017_(ClientboundPongResponsePacket p_134871_);
}