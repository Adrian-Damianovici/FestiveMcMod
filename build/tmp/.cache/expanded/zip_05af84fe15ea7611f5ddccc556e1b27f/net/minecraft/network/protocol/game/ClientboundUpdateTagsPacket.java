package net.minecraft.network.protocol.game;

import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization;

public class ClientboundUpdateTagsPacket implements Packet<ClientGamePacketListener> {
   private final Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> f_133649_;

   public ClientboundUpdateTagsPacket(Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> p_179473_) {
      this.f_133649_ = p_179473_;
   }

   public ClientboundUpdateTagsPacket(FriendlyByteBuf p_179475_) {
      this.f_133649_ = p_179475_.readMap((p_179484_) -> {
         return ResourceKey.createRegistryKey(p_179484_.readResourceLocation());
      }, TagNetworkSerialization.NetworkPayload::read);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf p_133661_) {
      p_133661_.writeMap(this.f_133649_, (p_179480_, p_179481_) -> {
         p_179480_.writeResourceLocation(p_179481_.location());
      }, (p_206653_, p_206654_) -> {
         p_206654_.write(p_206653_);
      });
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener p_133658_) {
      p_133658_.m_5859_(this);
   }

   public Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> m_179482_() {
      return this.f_133649_;
   }
}