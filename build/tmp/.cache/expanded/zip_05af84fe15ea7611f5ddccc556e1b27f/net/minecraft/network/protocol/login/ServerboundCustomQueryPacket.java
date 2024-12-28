package net.minecraft.network.protocol.login;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundCustomQueryPacket implements Packet<ServerLoginPacketListener>, net.minecraftforge.network.ICustomPacket<ServerboundCustomQueryPacket>
{
   private static final int f_179821_ = 1048576;
   private final int f_134825_;
   @Nullable
   private final FriendlyByteBuf f_134826_;

   public ServerboundCustomQueryPacket(int p_134829_, @Nullable FriendlyByteBuf p_134830_) {
      this.f_134825_ = p_134829_;
      this.f_134826_ = p_134830_;
   }

   public ServerboundCustomQueryPacket(FriendlyByteBuf p_179823_) {
      this.f_134825_ = p_179823_.readVarInt();
      this.f_134826_ = p_179823_.readNullable((p_238039_) -> {
         int i = p_238039_.readableBytes();
         if (i >= 0 && i <= 1048576) {
            return new FriendlyByteBuf(p_238039_.readBytes(i));
         } else {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
         }
      });
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf p_134838_) {
      p_134838_.writeVarInt(this.f_134825_);
      p_134838_.writeNullable(this.f_134826_, (p_238036_, p_238037_) -> {
         p_238036_.writeBytes(p_238037_.slice());
      });
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerLoginPacketListener p_134836_) {
      p_134836_.handleLoginAcknowledgement(this);
   }

   public int m_179824_() {
      return this.f_134825_;
   }

   @Nullable
   public FriendlyByteBuf m_179825_() {
      return this.f_134826_;
   }

   @Override public int getIndex() { return m_179824_(); }
   @Override public net.minecraft.resources.ResourceLocation getName() { return net.minecraftforge.network.LoginWrapper.WRAPPER; }
   @org.jetbrains.annotations.Nullable @Override public FriendlyByteBuf getInternalData() { return m_179825_(); }
}
