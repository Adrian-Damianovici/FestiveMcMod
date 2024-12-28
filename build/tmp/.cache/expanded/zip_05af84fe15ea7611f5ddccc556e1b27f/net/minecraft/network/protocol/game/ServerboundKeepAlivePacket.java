package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundKeepAlivePacket implements Packet<ServerGamePacketListener> {
   private final long f_134092_;

   public ServerboundKeepAlivePacket(long p_134095_) {
      this.f_134092_ = p_134095_;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener p_134101_) {
      p_134101_.m_5683_(this);
   }

   public ServerboundKeepAlivePacket(FriendlyByteBuf p_179671_) {
      this.f_134092_ = p_179671_.readLong();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf p_134104_) {
      p_134104_.writeLong(this.f_134092_);
   }

   public long m_134102_() {
      return this.f_134092_;
   }
}