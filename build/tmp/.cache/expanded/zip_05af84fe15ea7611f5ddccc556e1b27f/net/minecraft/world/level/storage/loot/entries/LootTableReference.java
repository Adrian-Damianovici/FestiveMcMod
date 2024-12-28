package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * A loot pool entry container that generates loot by referencing another loot table.
 */
public class LootTableReference extends LootPoolSingletonContainer {
   final ResourceLocation name;

   LootTableReference(ResourceLocation p_79756_, int p_79757_, int p_79758_, LootItemCondition[] p_79759_, LootItemFunction[] p_79760_) {
      super(p_79757_, p_79758_, p_79759_, p_79760_);
      this.name = p_79756_;
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.REFERENCE;
   }

   /**
    * Generate the loot stacks of this entry.
    * Contrary to the method name this method does not always generate one stack, it can also generate zero or multiple
    * stacks.
    */
   public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
      LootTable loottable = pLootContext.getResolver().getLootTable(this.name);
      loottable.getRandomItemsRaw(pLootContext, pStackConsumer);
   }

   public void validate(ValidationContext pValidationContext) {
      LootDataId<LootTable> lootdataid = new LootDataId<>(LootDataType.TABLE, this.name);
      if (pValidationContext.hasVisitedElement(lootdataid)) {
         pValidationContext.reportProblem("Table " + this.name + " is recursively called");
      } else {
         super.validate(pValidationContext);
         pValidationContext.resolver().getElementOptional(lootdataid).ifPresentOrElse((p_279078_) -> {
            p_279078_.validate(pValidationContext.enterElement("->{" + this.name + "}", lootdataid));
         }, () -> {
            pValidationContext.reportProblem("Unknown loot table called " + this.name);
         });
      }
   }

   public static LootPoolSingletonContainer.Builder<?> lootTableReference(ResourceLocation pTable) {
      return simpleBuilder((p_79780_, p_79781_, p_79782_, p_79783_) -> {
         return new LootTableReference(pTable, p_79780_, p_79781_, p_79782_, p_79783_);
      });
   }

   public static class Serializer extends LootPoolSingletonContainer.Serializer<LootTableReference> {
      public void m_7219_(JsonObject p_79801_, LootTableReference p_79802_, JsonSerializationContext p_79803_) {
         super.m_7219_(p_79801_, p_79802_, p_79803_);
         p_79801_.addProperty("name", p_79802_.name.toString());
      }

      protected LootTableReference m_7267_(JsonObject p_79786_, JsonDeserializationContext p_79787_, int p_79788_, int p_79789_, LootItemCondition[] p_79790_, LootItemFunction[] p_79791_) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(p_79786_, "name"));
         return new LootTableReference(resourcelocation, p_79788_, p_79789_, p_79790_, p_79791_);
      }
   }
}