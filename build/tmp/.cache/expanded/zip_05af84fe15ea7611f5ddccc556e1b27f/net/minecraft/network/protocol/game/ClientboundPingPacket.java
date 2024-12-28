package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPingPacket implements Packet<ClientGamePacketListener> {
   private final int f_179014_;

   public ClientboundPingPacket(int p_179016_) {
      this.f_179014_ = p_179016_;
   }

   public ClientboundPingPacket(FriendlyByteBuf p_179018_) {
      this.f_179014_ = p_179018_.readInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf p_179020_) {
      p_179020_.writeInt(this.f_179014_);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener p_179024_) {
      p_179024_.m_141955_(this);
   }

   public int m_179025_() {
      return this.f_179014_;
   }
}