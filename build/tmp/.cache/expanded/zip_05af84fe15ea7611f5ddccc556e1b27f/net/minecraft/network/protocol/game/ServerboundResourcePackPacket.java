package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundResourcePackPacket implements Packet<ServerGamePacketListener> {
   private final ServerboundResourcePackPacket.Action f_134406_;

   public ServerboundResourcePackPacket(ServerboundResourcePackPacket.Action p_134409_) {
      this.f_134406_ = p_134409_;
   }

   public ServerboundResourcePackPacket(FriendlyByteBuf p_179740_) {
      this.f_134406_ = p_179740_.readEnum(ServerboundResourcePackPacket.Action.class);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf p_134417_) {
      p_134417_.writeEnum(this.f_134406_);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener p_134415_) {
      p_134415_.m_7529_(this);
   }

   public ServerboundResourcePackPacket.Action m_179741_() {
      return this.f_134406_;
   }

   public static enum Action {
      SUCCESSFULLY_LOADED,
      DECLINED,
      FAILED_DOWNLOAD,
      ACCEPTED;
   }
}