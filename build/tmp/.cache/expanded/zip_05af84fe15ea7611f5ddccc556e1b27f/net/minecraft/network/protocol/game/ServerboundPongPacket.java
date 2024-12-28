package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPongPacket implements Packet<ServerGamePacketListener> {
   private final int f_179721_;

   public ServerboundPongPacket(int p_179723_) {
      this.f_179721_ = p_179723_;
   }

   public ServerboundPongPacket(FriendlyByteBuf p_179725_) {
      this.f_179721_ = p_179725_.readInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf p_179727_) {
      p_179727_.writeInt(this.f_179721_);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener p_179731_) {
      p_179731_.m_142110_(this);
   }

   public int m_179732_() {
      return this.f_179721_;
   }
}