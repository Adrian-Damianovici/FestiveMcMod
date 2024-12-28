package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ServerboundCustomPayloadPacket implements Packet<ServerGamePacketListener>, net.minecraftforge.network.ICustomPacket<ServerboundCustomPayloadPacket> {
   private static final int f_179586_ = 32767;
   public static final ResourceLocation f_133979_ = new ResourceLocation("brand");
   private final ResourceLocation f_133980_;
   private final FriendlyByteBuf f_133981_;

   public ServerboundCustomPayloadPacket(ResourceLocation p_133985_, FriendlyByteBuf p_133986_) {
      this.f_133980_ = p_133985_;
      this.f_133981_ = p_133986_;
   }

   public ServerboundCustomPayloadPacket(FriendlyByteBuf p_179588_) {
      this.f_133980_ = p_179588_.readResourceLocation();
      int i = p_179588_.readableBytes();
      if (i >= 0 && i <= 32767) {
         this.f_133981_ = new FriendlyByteBuf(p_179588_.readBytes(i));
      } else {
         throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
      }
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf p_133994_) {
      p_133994_.writeResourceLocation(this.f_133980_);
      p_133994_.writeBytes((ByteBuf)this.f_133981_.slice()); // Use Slice instead of copy, to not update the read index, allowing packet to be sent multiple times.
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener p_133992_) {
      p_133992_.m_7423_(this);
      this.f_133981_.release();
   }

   public ResourceLocation m_179589_() {
      return this.f_133980_;
   }

   public FriendlyByteBuf m_179590_() {
      return this.f_133981_;
   }

   @Override public int getIndex() { return Integer.MAX_VALUE; }
   @Override public ResourceLocation getName() { return m_179589_(); }
   @org.jetbrains.annotations.Nullable @Override public FriendlyByteBuf getInternalData() { return m_179590_(); }
}
