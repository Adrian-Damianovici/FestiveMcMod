package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundCustomPayloadPacket implements Packet<ClientGamePacketListener>, net.minecraftforge.network.ICustomPacket<ClientboundCustomPayloadPacket> {
   private static final int f_178834_ = 1048576;
   public static final ResourceLocation f_132012_ = new ResourceLocation("brand");
   public static final ResourceLocation f_132013_ = new ResourceLocation("debug/path");
   public static final ResourceLocation f_132014_ = new ResourceLocation("debug/neighbors_update");
   public static final ResourceLocation f_132016_ = new ResourceLocation("debug/structures");
   public static final ResourceLocation f_132017_ = new ResourceLocation("debug/worldgen_attempt");
   public static final ResourceLocation f_132018_ = new ResourceLocation("debug/poi_ticket_count");
   public static final ResourceLocation f_132019_ = new ResourceLocation("debug/poi_added");
   public static final ResourceLocation f_132020_ = new ResourceLocation("debug/poi_removed");
   public static final ResourceLocation f_132021_ = new ResourceLocation("debug/village_sections");
   public static final ResourceLocation f_132022_ = new ResourceLocation("debug/goal_selector");
   public static final ResourceLocation f_132023_ = new ResourceLocation("debug/brain");
   public static final ResourceLocation f_132024_ = new ResourceLocation("debug/bee");
   public static final ResourceLocation f_132025_ = new ResourceLocation("debug/hive");
   public static final ResourceLocation f_132026_ = new ResourceLocation("debug/game_test_add_marker");
   public static final ResourceLocation f_132027_ = new ResourceLocation("debug/game_test_clear");
   public static final ResourceLocation f_132028_ = new ResourceLocation("debug/raids");
   public static final ResourceLocation f_178832_ = new ResourceLocation("debug/game_event");
   public static final ResourceLocation f_178833_ = new ResourceLocation("debug/game_event_listeners");
   private final ResourceLocation f_132029_;
   private final FriendlyByteBuf f_132030_;
   private final boolean shouldRelease;

   public ClientboundCustomPayloadPacket(ResourceLocation p_132034_, FriendlyByteBuf p_132035_) {
      this.f_132029_ = p_132034_;
      this.f_132030_ = p_132035_;
      if (p_132035_.writerIndex() > 1048576) {
         throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
      }
      this.shouldRelease = false; //We are not the owner of the buffer, don't release it, it might be reused.
   }

   public ClientboundCustomPayloadPacket(FriendlyByteBuf p_178836_) {
      this.f_132029_ = p_178836_.readResourceLocation();
      int i = p_178836_.readableBytes();
      if (i >= 0 && i <= 1048576) {
         this.f_132030_ = new FriendlyByteBuf(p_178836_.readBytes(i));
      } else {
         throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
      }
      this.shouldRelease = true; //We are the owner of the buffer, release it when we are done.
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf p_132044_) {
      p_132044_.writeResourceLocation(this.f_132029_);
      p_132044_.writeBytes(this.f_132030_.slice()); // Use Slice instead of copy, to not update the read index, allowing packet to be sent multiple times.
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener p_132041_) {
      p_132041_.m_7413_(this);
      if (this.shouldRelease) this.f_132030_.release(); // FORGE: Fix impl buffer leaks (MC-121884), can only be fixed partially, because else you get problems in LAN-Worlds
   }

   public ResourceLocation m_132042_() {
      return this.f_132029_;
   }

   public FriendlyByteBuf m_132045_() {
      return new FriendlyByteBuf(this.f_132030_.copy());
   }

   @Override public int getIndex() { return Integer.MAX_VALUE; }
   @Override public ResourceLocation getName() { return m_132042_(); }
   @Override public FriendlyByteBuf getInternalData() { return m_132045_(); }
}
