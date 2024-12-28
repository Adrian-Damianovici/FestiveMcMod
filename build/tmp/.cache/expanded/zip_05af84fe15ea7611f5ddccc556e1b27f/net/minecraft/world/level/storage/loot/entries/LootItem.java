package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * A loot pool entry that always generates a given item.
 */
public class LootItem extends LootPoolSingletonContainer {
   final Item item;

   LootItem(Item p_79566_, int p_79567_, int p_79568_, LootItemCondition[] p_79569_, LootItemFunction[] p_79570_) {
      super(p_79567_, p_79568_, p_79569_, p_79570_);
      this.item = p_79566_;
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.ITEM;
   }

   /**
    * Generate the loot stacks of this entry.
    * Contrary to the method name this method does not always generate one stack, it can also generate zero or multiple
    * stacks.
    */
   public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
      pStackConsumer.accept(new ItemStack(this.item));
   }

   public static LootPoolSingletonContainer.Builder<?> lootTableItem(ItemLike pItem) {
      return simpleBuilder((p_79583_, p_79584_, p_79585_, p_79586_) -> {
         return new LootItem(pItem.asItem(), p_79583_, p_79584_, p_79585_, p_79586_);
      });
   }

   public static class Serializer extends LootPoolSingletonContainer.Serializer<LootItem> {
      public void m_7219_(JsonObject p_79601_, LootItem p_79602_, JsonSerializationContext p_79603_) {
         super.m_7219_(p_79601_, p_79602_, p_79603_);
         ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(p_79602_.item);
         if (resourcelocation == null) {
            throw new IllegalArgumentException("Can't serialize unknown item " + p_79602_.item);
         } else {
            p_79601_.addProperty("name", resourcelocation.toString());
         }
      }

      protected LootItem m_7267_(JsonObject p_79594_, JsonDeserializationContext p_79595_, int p_79596_, int p_79597_, LootItemCondition[] p_79598_, LootItemFunction[] p_79599_) {
         Item item = GsonHelper.getAsItem(p_79594_, "name");
         return new LootItem(item, p_79596_, p_79597_, p_79598_, p_79599_);
      }
   }
}