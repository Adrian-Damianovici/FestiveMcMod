package net.minecraft.world.level.storage.loot.entries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;

/**
 * Registration for {@link LootPoolEntryType}.
 */
public class LootPoolEntries {
   public static final LootPoolEntryType EMPTY = register("empty", new EmptyLootItem.Serializer());
   public static final LootPoolEntryType ITEM = register("item", new LootItem.Serializer());
   public static final LootPoolEntryType REFERENCE = register("loot_table", new LootTableReference.Serializer());
   public static final LootPoolEntryType DYNAMIC = register("dynamic", new DynamicLoot.Serializer());
   public static final LootPoolEntryType TAG = register("tag", new TagEntry.Serializer());
   public static final LootPoolEntryType ALTERNATIVES = register("alternatives", CompositeEntryBase.m_79435_(AlternativesEntry::new));
   public static final LootPoolEntryType SEQUENCE = register("sequence", CompositeEntryBase.m_79435_(SequentialEntry::new));
   public static final LootPoolEntryType GROUP = register("group", CompositeEntryBase.m_79435_(EntryGroup::new));

   private static LootPoolEntryType register(String pName, Serializer<? extends LootPoolEntryContainer> p_79631_) {
      return Registry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, new ResourceLocation(pName), new LootPoolEntryType(p_79631_));
   }

   public static Object m_79628_() {
      return GsonAdapterFactory.m_78801_(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, "entry", "type", LootPoolEntryContainer::getType).m_78822_();
   }
}