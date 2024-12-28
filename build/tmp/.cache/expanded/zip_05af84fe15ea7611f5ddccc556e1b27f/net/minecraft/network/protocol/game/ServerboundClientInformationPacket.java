package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;

public record ServerboundClientInformationPacket(String f_133863_, int f_133864_, ChatVisiblity f_133865_, boolean f_133866_, int f_133867_, HumanoidArm f_133868_, boolean f_179550_, boolean f_195812_) implements Packet<ServerGamePacketListener> {
   public static final int f_179549_ = 16;

   public ServerboundClientInformationPacket(FriendlyByteBuf p_179560_) {
      this(p_179560_.readUtf(16), p_179560_.readByte(), p_179560_.readEnum(ChatVisiblity.class), p_179560_.readBoolean(), p_179560_.readUnsignedByte(), p_179560_.readEnum(HumanoidArm.class), p_179560_.readBoolean(), p_179560_.readBoolean());
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf p_133884_) {
      p_133884_.writeUtf(this.f_133863_);
      p_133884_.writeByte(this.f_133864_);
      p_133884_.writeEnum(this.f_133865_);
      p_133884_.writeBoolean(this.f_133866_);
      p_133884_.writeByte(this.f_133867_);
      p_133884_.writeEnum(this.f_133868_);
      p_133884_.writeBoolean(this.f_179550_);
      p_133884_.writeBoolean(this.f_195812_);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener p_133882_) {
      p_133882_.m_5617_(this);
   }
}