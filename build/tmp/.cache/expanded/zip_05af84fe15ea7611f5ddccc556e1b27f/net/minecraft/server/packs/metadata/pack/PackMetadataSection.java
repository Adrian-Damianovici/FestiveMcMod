package net.minecraft.server.packs.metadata.pack;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public class PackMetadataSection {
   public static final MetadataSectionType<PackMetadataSection> TYPE = new PackMetadataSectionSerializer();
   private final Component description;
   private final int packFormat;
   private final java.util.Map<net.minecraft.server.packs.PackType, Integer> packTypeVersions;

   public PackMetadataSection(Component p_10371_, int p_10372_) {
      this.description = p_10371_;
      this.packFormat = p_10372_;
      this.packTypeVersions = java.util.Map.of();
   }
   public PackMetadataSection(Component p_10371_, int p_10372_, java.util.Map<net.minecraft.server.packs.PackType, Integer> packTypeVersions) {
      this.description = p_10371_;
      this.packFormat = p_10372_;
      this.packTypeVersions = packTypeVersions;
   }

   public Component m_10373_() {
      return this.description;
   }

   /** @deprecated Forge: Use {@link #getPackFormat(net.minecraft.server.packs.PackType)} instead.*/
   @Deprecated
   public int m_10374_() {
      return this.packFormat;
   }
   public int getPackFormat(net.minecraft.server.packs.PackType packType) {
      return packTypeVersions.getOrDefault(packType, this.packFormat);
   }
}
