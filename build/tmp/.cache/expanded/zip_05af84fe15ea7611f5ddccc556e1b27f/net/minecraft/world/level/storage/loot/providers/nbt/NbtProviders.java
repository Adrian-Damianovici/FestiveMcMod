package net.minecraft.world.level.storage.loot.providers.nbt;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;

/**
 * Registry for {@link NbtProvider}
 */
public class NbtProviders {
   public static final LootNbtProviderType STORAGE = register("storage", new StorageNbtProvider.Serializer());
   public static final LootNbtProviderType CONTEXT = register("context", new ContextNbtProvider.Serializer());

   private static LootNbtProviderType register(String pName, Serializer<? extends NbtProvider> p_165630_) {
      return Registry.register(BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE, new ResourceLocation(pName), new LootNbtProviderType(p_165630_));
   }

   public static Object m_165627_() {
      return GsonAdapterFactory.m_78801_(BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE, "provider", "type", NbtProvider::getType).m_164986_(CONTEXT, new ContextNbtProvider.InlineSerializer()).m_78822_();
   }
}