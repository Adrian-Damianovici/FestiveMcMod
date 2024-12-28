package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * A loot pool entry that does not generate any items.
 */
public class EmptyLootItem extends LootPoolSingletonContainer {
   EmptyLootItem(int p_79519_, int p_79520_, LootItemCondition[] p_79521_, LootItemFunction[] p_79522_) {
      super(p_79519_, p_79520_, p_79521_, p_79522_);
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.EMPTY;
   }

   /**
    * Generate the loot stacks of this entry.
    * Contrary to the method name this method does not always generate one stack, it can also generate zero or multiple
    * stacks.
    */
   public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
   }

   public static LootPoolSingletonContainer.Builder<?> emptyItem() {
      return simpleBuilder(EmptyLootItem::new);
   }

   public static class Serializer extends LootPoolSingletonContainer.Serializer<EmptyLootItem> {
      public EmptyLootItem m_7267_(JsonObject p_79536_, JsonDeserializationContext p_79537_, int p_79538_, int p_79539_, LootItemCondition[] p_79540_, LootItemFunction[] p_79541_) {
         return new EmptyLootItem(p_79538_, p_79539_, p_79540_, p_79541_);
      }
   }
}