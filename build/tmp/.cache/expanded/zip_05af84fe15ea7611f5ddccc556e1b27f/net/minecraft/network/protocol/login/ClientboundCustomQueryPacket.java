package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundCustomQueryPacket implements Packet<ClientLoginPacketListener>, net.minecraftforge.network.ICustomPacket<ClientboundCustomQueryPacket> {
   private static final int MAX_PAYLOAD_SIZE = 1048576;
   private final int transactionId;
   private final ResourceLocation f_134746_;
   private final FriendlyByteBuf f_134747_;

   public ClientboundCustomQueryPacket(int p_179806_, ResourceLocation p_179807_, FriendlyByteBuf p_179808_) {
      this.transactionId = p_179806_;
      this.f_134746_ = p_179807_;
      this.f_134747_ = p_179808_;
   }

   public ClientboundCustomQueryPacket(FriendlyByteBuf pBuffer) {
      this.transactionId = pBuffer.readVarInt();
      this.f_134746_ = pBuffer.readResourceLocation();
      int i = pBuffer.readableBytes();
      if (i >= 0 && i <= 1048576) {
         this.f_134747_ = new FriendlyByteBuf(pBuffer.readBytes(i));
      } else {
         throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
      }
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.transactionId);
      pBuffer.writeResourceLocation(this.f_134746_);
      pBuffer.writeBytes(this.f_134747_.slice()); // Use Slice instead of copy, to not update the read index, allowing packet to be sent multiple times.
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientLoginPacketListener pHandler) {
      pHandler.handleCustomQuery(this);
   }

   public int m_134755_() {
      return this.transactionId;
   }

   public ResourceLocation m_179811_() {
      return this.f_134746_;
   }

   public FriendlyByteBuf m_179812_() {
      return this.f_134747_;
   }

   @Override public int getIndex() { return m_134755_(); }
   @Override public ResourceLocation getName() { return m_179811_(); }
   @org.jetbrains.annotations.Nullable @Override public FriendlyByteBuf getInternalData() { return m_179812_(); }
}
