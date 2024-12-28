package net.minecraft.data.advancements;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

public interface AdvancementSubProvider {
   static Advancement m_266597_(String p_267076_) {
      return Advancement.Builder.advancement().build(new ResourceLocation(p_267076_));
   }

   void generate(HolderLookup.Provider pRegistries, Consumer<Advancement> pWriter);
}