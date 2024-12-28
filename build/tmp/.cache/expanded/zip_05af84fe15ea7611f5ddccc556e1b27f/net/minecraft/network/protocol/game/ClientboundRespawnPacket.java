package net.minecraft.network.protocol.game;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ClientboundRespawnPacket implements Packet<ClientGamePacketListener> {
   public static final byte KEEP_ATTRIBUTES = 1;
   public static final byte KEEP_ENTITY_DATA = 2;
   public static final byte KEEP_ALL_DATA = 3;
   private final ResourceKey<DimensionType> f_132928_;
   private final ResourceKey<Level> f_132929_;
   private final long f_132930_;
   private final GameType f_132931_;
   @Nullable
   private final GameType f_132932_;
   private final boolean f_132933_;
   private final boolean f_132934_;
   private final byte dataToKeep;
   private final Optional<GlobalPos> f_238183_;
   private final int f_286981_;

   public ClientboundRespawnPacket(ResourceKey<DimensionType> p_287723_, ResourceKey<Level> p_287745_, long p_287746_, GameType p_287624_, @Nullable GameType p_287780_, boolean p_287655_, boolean p_287735_, byte p_287694_, Optional<GlobalPos> p_287615_, int p_287636_) {
      this.f_132928_ = p_287723_;
      this.f_132929_ = p_287745_;
      this.f_132930_ = p_287746_;
      this.f_132931_ = p_287624_;
      this.f_132932_ = p_287780_;
      this.f_132933_ = p_287655_;
      this.f_132934_ = p_287735_;
      this.dataToKeep = p_287694_;
      this.f_238183_ = p_287615_;
      this.f_286981_ = p_287636_;
   }

   public ClientboundRespawnPacket(FriendlyByteBuf pBuffer) {
      this.f_132928_ = pBuffer.readResourceKey(Registries.DIMENSION_TYPE);
      this.f_132929_ = pBuffer.readResourceKey(Registries.DIMENSION);
      this.f_132930_ = pBuffer.readLong();
      this.f_132931_ = GameType.byId(pBuffer.readUnsignedByte());
      this.f_132932_ = GameType.byNullableId(pBuffer.readByte());
      this.f_132933_ = pBuffer.readBoolean();
      this.f_132934_ = pBuffer.readBoolean();
      this.dataToKeep = pBuffer.readByte();
      this.f_238183_ = pBuffer.readOptional(FriendlyByteBuf::readGlobalPos);
      this.f_286981_ = pBuffer.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeResourceKey(this.f_132928_);
      pBuffer.writeResourceKey(this.f_132929_);
      pBuffer.writeLong(this.f_132930_);
      pBuffer.writeByte(this.f_132931_.getId());
      pBuffer.writeByte(GameType.getNullableId(this.f_132932_));
      pBuffer.writeBoolean(this.f_132933_);
      pBuffer.writeBoolean(this.f_132934_);
      pBuffer.writeByte(this.dataToKeep);
      pBuffer.writeOptional(this.f_238183_, FriendlyByteBuf::writeGlobalPos);
      pBuffer.writeVarInt(this.f_286981_);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleRespawn(this);
   }

   public ResourceKey<DimensionType> m_237794_() {
      return this.f_132928_;
   }

   public ResourceKey<Level> m_132955_() {
      return this.f_132929_;
   }

   public long m_132956_() {
      return this.f_132930_;
   }

   public GameType m_132957_() {
      return this.f_132931_;
   }

   @Nullable
   public GameType m_132958_() {
      return this.f_132932_;
   }

   public boolean m_132959_() {
      return this.f_132933_;
   }

   public boolean m_132960_() {
      return this.f_132934_;
   }

   public boolean shouldKeep(byte pData) {
      return (this.dataToKeep & pData) != 0;
   }

   public Optional<GlobalPos> m_237785_() {
      return this.f_238183_;
   }

   public int m_287149_() {
      return this.f_286981_;
   }
}