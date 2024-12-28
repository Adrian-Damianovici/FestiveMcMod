package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundDisconnectPacket implements Packet<ClientGamePacketListener> {
   private final Component f_132075_;

   public ClientboundDisconnectPacket(Component p_132078_) {
      this.f_132075_ = p_132078_;
   }

   public ClientboundDisconnectPacket(FriendlyByteBuf p_178841_) {
      this.f_132075_ = p_178841_.readComponent();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf p_132087_) {
      p_132087_.writeComponent(this.f_132075_);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener p_132084_) {
      p_132084_.m_6008_(this);
   }

   public Component m_132085_() {
      return this.f_132075_;
   }
}