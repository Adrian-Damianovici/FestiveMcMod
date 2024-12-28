package net.minecraft.world.level.storage.loot;

import com.google.gson.GsonBuilder;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProviders;

public class Deserializers {
   public static GsonBuilder m_78798_() {
      return (new GsonBuilder()).registerTypeAdapter(IntRange.class, new IntRange.Serializer()).registerTypeHierarchyAdapter(NumberProvider.class, NumberProviders.m_165737_()).registerTypeHierarchyAdapter(LootItemCondition.class, LootItemConditions.m_81828_()).registerTypeHierarchyAdapter(ScoreboardNameProvider.class, ScoreboardNameProviders.m_165872_()).registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer());
   }

   public static GsonBuilder m_78799_() {
      return m_78798_().registerTypeHierarchyAdapter(LootPoolEntryContainer.class, LootPoolEntries.m_79628_()).registerTypeHierarchyAdapter(LootItemFunction.class, LootItemFunctions.m_80758_()).registerTypeHierarchyAdapter(NbtProvider.class, NbtProviders.m_165627_());
   }

   public static GsonBuilder m_78800_() {
      return m_78799_().registerTypeAdapter(LootPool.class, new LootPool.Serializer()).registerTypeAdapter(LootTable.class, new LootTable.Serializer());
   }
}