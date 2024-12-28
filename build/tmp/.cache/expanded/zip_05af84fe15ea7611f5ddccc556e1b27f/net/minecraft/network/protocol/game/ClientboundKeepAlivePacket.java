package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundKeepAlivePacket implements Packet<ClientGamePacketListener> {
   private final long f_132209_;

   public ClientboundKeepAlivePacket(long p_132212_) {
      this.f_132209_ = p_132212_;
   }

   public ClientboundKeepAlivePacket(FriendlyByteBuf p_178895_) {
      this.f_132209_ = p_178895_.readLong();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf p_132221_) {
      p_132221_.writeLong(this.f_132209_);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener p_132218_) {
      p_132218_.m_7231_(this);
   }

   public long m_132219_() {
      return this.f_132209_;
   }
}