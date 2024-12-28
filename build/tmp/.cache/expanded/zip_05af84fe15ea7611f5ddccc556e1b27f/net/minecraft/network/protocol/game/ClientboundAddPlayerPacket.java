package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;

public class ClientboundAddPlayerPacket implements Packet<ClientGamePacketListener> {
   private final int f_131587_;
   private final UUID f_131588_;
   private final double f_131589_;
   private final double f_131590_;
   private final double f_131591_;
   private final byte f_131592_;
   private final byte f_131593_;

   public ClientboundAddPlayerPacket(Player p_131596_) {
      this.f_131587_ = p_131596_.getId();
      this.f_131588_ = p_131596_.getGameProfile().getId();
      this.f_131589_ = p_131596_.getX();
      this.f_131590_ = p_131596_.getY();
      this.f_131591_ = p_131596_.getZ();
      this.f_131592_ = (byte)((int)(p_131596_.getYRot() * 256.0F / 360.0F));
      this.f_131593_ = (byte)((int)(p_131596_.getXRot() * 256.0F / 360.0F));
   }

   public ClientboundAddPlayerPacket(FriendlyByteBuf p_178570_) {
      this.f_131587_ = p_178570_.readVarInt();
      this.f_131588_ = p_178570_.readUUID();
      this.f_131589_ = p_178570_.readDouble();
      this.f_131590_ = p_178570_.readDouble();
      this.f_131591_ = p_178570_.readDouble();
      this.f_131592_ = p_178570_.readByte();
      this.f_131593_ = p_178570_.readByte();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf p_131605_) {
      p_131605_.writeVarInt(this.f_131587_);
      p_131605_.writeUUID(this.f_131588_);
      p_131605_.writeDouble(this.f_131589_);
      p_131605_.writeDouble(this.f_131590_);
      p_131605_.writeDouble(this.f_131591_);
      p_131605_.writeByte(this.f_131592_);
      p_131605_.writeByte(this.f_131593_);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener p_131602_) {
      p_131602_.m_6482_(this);
   }

   public int m_131603_() {
      return this.f_131587_;
   }

   public UUID m_131606_() {
      return this.f_131588_;
   }

   public double m_131607_() {
      return this.f_131589_;
   }

   public double m_131608_() {
      return this.f_131590_;
   }

   public double m_131609_() {
      return this.f_131591_;
   }

   public byte m_131610_() {
      return this.f_131592_;
   }

   public byte m_131611_() {
      return this.f_131593_;
   }
}