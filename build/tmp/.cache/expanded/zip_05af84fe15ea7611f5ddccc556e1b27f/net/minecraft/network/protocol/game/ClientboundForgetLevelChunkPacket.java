package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundForgetLevelChunkPacket implements Packet<ClientGamePacketListener> {
   private final int f_132137_;
   private final int f_132138_;

   public ClientboundForgetLevelChunkPacket(int p_132141_, int p_132142_) {
      this.f_132137_ = p_132141_;
      this.f_132138_ = p_132142_;
   }

   public ClientboundForgetLevelChunkPacket(FriendlyByteBuf pBuffer) {
      this.f_132137_ = pBuffer.readInt();
      this.f_132138_ = pBuffer.readInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeInt(this.f_132137_);
      pBuffer.writeInt(this.f_132138_);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleForgetLevelChunk(this);
   }

   public int m_132149_() {
      return this.f_132137_;
   }

   public int m_132152_() {
      return this.f_132138_;
   }
}