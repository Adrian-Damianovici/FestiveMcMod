package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPongResponsePacket implements Packet<ClientStatusPacketListener> {
   private final long time;

   public ClientboundPongResponsePacket(long pTime) {
      this.time = pTime;
   }

   public ClientboundPongResponsePacket(FriendlyByteBuf pBuffer) {
      this.time = pBuffer.readLong();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeLong(this.time);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientStatusPacketListener p_134882_) {
      p_134882_.m_7017_(this);
   }

   public long getTime() {
      return this.time;
   }
}