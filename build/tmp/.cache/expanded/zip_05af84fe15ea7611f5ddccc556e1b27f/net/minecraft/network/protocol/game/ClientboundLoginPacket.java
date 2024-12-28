package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public record ClientboundLoginPacket(int playerId, boolean hardcore, GameType f_132363_, @Nullable GameType f_132364_, Set<ResourceKey<Level>> levels, RegistryAccess.Frozen f_132366_, ResourceKey<DimensionType> f_132367_, ResourceKey<Level> f_132368_, long f_132361_, int maxPlayers, int chunkRadius, int simulationDistance, boolean reducedDebugInfo, boolean showDeathScreen, boolean f_132373_, boolean f_132374_, Optional<GlobalPos> f_238174_, int f_286971_) implements Packet<ClientGamePacketListener> {
   private static final RegistryOps<Tag> f_266064_ = RegistryOps.create(NbtOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));

   public ClientboundLoginPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readInt(), pBuffer.readBoolean(), GameType.byId(pBuffer.readByte()), GameType.byNullableId(pBuffer.readByte()), pBuffer.readCollection(Sets::newHashSetWithExpectedSize, (p_258210_) -> {
         return p_258210_.readResourceKey(Registries.DIMENSION);
      }), pBuffer.readWithCodec(f_266064_, RegistrySynchronization.NETWORK_CODEC).freeze(), pBuffer.readResourceKey(Registries.DIMENSION_TYPE), pBuffer.readResourceKey(Registries.DIMENSION), pBuffer.readLong(), pBuffer.readVarInt(), pBuffer.readVarInt(), pBuffer.readVarInt(), pBuffer.readBoolean(), pBuffer.readBoolean(), pBuffer.readBoolean(), pBuffer.readBoolean(), pBuffer.readOptional(FriendlyByteBuf::readGlobalPos), pBuffer.readVarInt());
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeInt(this.playerId);
      pBuffer.writeBoolean(this.hardcore);
      pBuffer.writeByte(this.f_132363_.getId());
      pBuffer.writeByte(GameType.getNullableId(this.f_132364_));
      pBuffer.writeCollection(this.levels, FriendlyByteBuf::writeResourceKey);
      pBuffer.writeWithCodec(f_266064_, RegistrySynchronization.NETWORK_CODEC, this.f_132366_);
      pBuffer.writeResourceKey(this.f_132367_);
      pBuffer.writeResourceKey(this.f_132368_);
      pBuffer.writeLong(this.f_132361_);
      pBuffer.writeVarInt(this.maxPlayers);
      pBuffer.writeVarInt(this.chunkRadius);
      pBuffer.writeVarInt(this.simulationDistance);
      pBuffer.writeBoolean(this.reducedDebugInfo);
      pBuffer.writeBoolean(this.showDeathScreen);
      pBuffer.writeBoolean(this.f_132373_);
      pBuffer.writeBoolean(this.f_132374_);
      pBuffer.writeOptional(this.f_238174_, FriendlyByteBuf::writeGlobalPos);
      pBuffer.writeVarInt(this.f_286971_);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleLogin(this);
   }
}