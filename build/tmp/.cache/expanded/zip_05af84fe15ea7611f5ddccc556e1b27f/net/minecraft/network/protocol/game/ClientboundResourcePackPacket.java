package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundResourcePackPacket implements Packet<ClientGamePacketListener> {
   public static final int f_179178_ = 40;
   private final String f_132912_;
   private final String f_132913_;
   private final boolean f_179179_;
   @Nullable
   private final Component f_179180_;

   public ClientboundResourcePackPacket(String p_179182_, String p_179183_, boolean p_179184_, @Nullable Component p_179185_) {
      if (p_179183_.length() > 40) {
         throw new IllegalArgumentException("Hash is too long (max 40, was " + p_179183_.length() + ")");
      } else {
         this.f_132912_ = p_179182_;
         this.f_132913_ = p_179183_;
         this.f_179179_ = p_179184_;
         this.f_179180_ = p_179185_;
      }
   }

   public ClientboundResourcePackPacket(FriendlyByteBuf p_179187_) {
      this.f_132912_ = p_179187_.readUtf();
      this.f_132913_ = p_179187_.readUtf(40);
      this.f_179179_ = p_179187_.readBoolean();
      this.f_179180_ = p_179187_.readNullable(FriendlyByteBuf::readComponent);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf p_132926_) {
      p_132926_.writeUtf(this.f_132912_);
      p_132926_.writeUtf(this.f_132913_);
      p_132926_.writeBoolean(this.f_179179_);
      p_132926_.writeNullable(this.f_179180_, FriendlyByteBuf::writeComponent);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener p_132923_) {
      p_132923_.m_5587_(this);
   }

   public String m_132924_() {
      return this.f_132912_;
   }

   public String m_132927_() {
      return this.f_132913_;
   }

   public boolean m_179188_() {
      return this.f_179179_;
   }

   @Nullable
   public Component m_179189_() {
      return this.f_179180_;
   }
}