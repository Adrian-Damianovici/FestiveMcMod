package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * A loot pool entry container that will generate the dynamic drops with a given name.
 * 
 * @see LootContext.DynamicDrops
 */
public class DynamicLoot extends LootPoolSingletonContainer {
   final ResourceLocation name;

   DynamicLoot(ResourceLocation p_79465_, int p_79466_, int p_79467_, LootItemCondition[] p_79468_, LootItemFunction[] p_79469_) {
      super(p_79466_, p_79467_, p_79468_, p_79469_);
      this.name = p_79465_;
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.DYNAMIC;
   }

   /**
    * Generate the loot stacks of this entry.
    * Contrary to the method name this method does not always generate one stack, it can also generate zero or multiple
    * stacks.
    */
   public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
      pLootContext.addDynamicDrops(this.name, pStackConsumer);
   }

   public static LootPoolSingletonContainer.Builder<?> dynamicEntry(ResourceLocation pDynamicDropsName) {
      return simpleBuilder((p_79487_, p_79488_, p_79489_, p_79490_) -> {
         return new DynamicLoot(pDynamicDropsName, p_79487_, p_79488_, p_79489_, p_79490_);
      });
   }

   public static class Serializer extends LootPoolSingletonContainer.Serializer<DynamicLoot> {
      public void m_7219_(JsonObject p_79500_, DynamicLoot p_79501_, JsonSerializationContext p_79502_) {
         super.m_7219_(p_79500_, p_79501_, p_79502_);
         p_79500_.addProperty("name", p_79501_.name.toString());
      }

      protected DynamicLoot m_7267_(JsonObject p_79493_, JsonDeserializationContext p_79494_, int p_79495_, int p_79496_, LootItemCondition[] p_79497_, LootItemFunction[] p_79498_) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(p_79493_, "name"));
         return new DynamicLoot(resourcelocation, p_79495_, p_79496_, p_79497_, p_79498_);
      }
   }
}