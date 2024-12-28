package net.minecraft.network.protocol.game;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public record ClientboundUpdateEnabledFeaturesPacket(Set<ResourceLocation> f_244610_) implements Packet<ClientGamePacketListener> {
   public ClientboundUpdateEnabledFeaturesPacket(FriendlyByteBuf p_250545_) {
      this(p_250545_.<ResourceLocation, Set<ResourceLocation>>readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf p_251972_) {
      p_251972_.writeCollection(this.f_244610_, FriendlyByteBuf::writeResourceLocation);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener p_250317_) {
      p_250317_.m_241155_(this);
   }
}