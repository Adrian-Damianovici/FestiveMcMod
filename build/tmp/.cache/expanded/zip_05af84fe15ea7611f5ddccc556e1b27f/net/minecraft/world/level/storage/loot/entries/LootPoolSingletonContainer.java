package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.commons.lang3.ArrayUtils;

/**
 * A LootPoolEntryContainer that expands into a single LootPoolEntry.
 */
public abstract class LootPoolSingletonContainer extends LootPoolEntryContainer {
   public static final int DEFAULT_WEIGHT = 1;
   public static final int DEFAULT_QUALITY = 0;
   /** The weight of the entry. */
   protected final int weight;
   /** The quality of the entry. */
   protected final int quality;
   /** Functions that are ran on the entry. */
   protected final LootItemFunction[] functions;
   final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
   private final LootPoolEntry entry = new LootPoolSingletonContainer.EntryBase() {
      /**
       * Generate the loot stacks of this entry.
       * Contrary to the method name this method does not always generate one stack, it can also generate zero or
       * multiple stacks.
       */
      public void createItemStack(Consumer<ItemStack> p_79700_, LootContext p_79701_) {
         LootPoolSingletonContainer.this.createItemStack(LootItemFunction.decorate(LootPoolSingletonContainer.this.compositeFunction, p_79700_, p_79701_), p_79701_);
      }
   };

   protected LootPoolSingletonContainer(int pWeight, int pQuality, LootItemCondition[] p_79683_, LootItemFunction[] p_79684_) {
      super(p_79683_);
      this.weight = pWeight;
      this.quality = pQuality;
      this.functions = p_79684_;
      this.compositeFunction = LootItemFunctions.compose(p_79684_);
   }

   public void validate(ValidationContext pValidationContext) {
      super.validate(pValidationContext);

      for(int i = 0; i < this.functions.length; ++i) {
         this.functions[i].validate(pValidationContext.forChild(".functions[" + i + "]"));
      }

   }

   /**
    * Generate the loot stacks of this entry.
    * Contrary to the method name this method does not always generate one stack, it can also generate zero or multiple
    * stacks.
    */
   protected abstract void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext);

   /**
    * Expand this loot pool entry container by calling {@code entryConsumer} with any applicable entries
    * 
    * @return whether this loot pool entry container successfully expanded or not
    */
   public boolean expand(LootContext pLootContext, Consumer<LootPoolEntry> pEntryConsumer) {
      if (this.canRun(pLootContext)) {
         pEntryConsumer.accept(this.entry);
         return true;
      } else {
         return false;
      }
   }

   public static LootPoolSingletonContainer.Builder<?> simpleBuilder(LootPoolSingletonContainer.EntryConstructor pEntryBuilder) {
      return new LootPoolSingletonContainer.DummyBuilder(pEntryBuilder);
   }

   public abstract static class Builder<T extends LootPoolSingletonContainer.Builder<T>> extends LootPoolEntryContainer.Builder<T> implements FunctionUserBuilder<T> {
      protected int weight = 1;
      protected int quality = 0;
      private final List<LootItemFunction> functions = Lists.newArrayList();

      public T apply(LootItemFunction.Builder pFunctionBuilder) {
         this.functions.add(pFunctionBuilder.build());
         return this.getThis();
      }

      protected LootItemFunction[] getFunctions() {
         return this.functions.toArray(new LootItemFunction[0]);
      }

      public T setWeight(int pWeight) {
         this.weight = pWeight;
         return this.getThis();
      }

      public T setQuality(int pQuality) {
         this.quality = pQuality;
         return this.getThis();
      }
   }

   static class DummyBuilder extends LootPoolSingletonContainer.Builder<LootPoolSingletonContainer.DummyBuilder> {
      private final LootPoolSingletonContainer.EntryConstructor constructor;

      public DummyBuilder(LootPoolSingletonContainer.EntryConstructor pConstructor) {
         this.constructor = pConstructor;
      }

      protected LootPoolSingletonContainer.DummyBuilder getThis() {
         return this;
      }

      public LootPoolEntryContainer build() {
         return this.constructor.build(this.weight, this.quality, this.getConditions(), this.getFunctions());
      }
   }

   protected abstract class EntryBase implements LootPoolEntry {
      /**
       * Gets the effective weight based on the loot entry's weight and quality multiplied by looter's luck.
       */
      public int getWeight(float pLuck) {
         return Math.max(Mth.floor((float)LootPoolSingletonContainer.this.weight + (float)LootPoolSingletonContainer.this.quality * pLuck), 0);
      }
   }

   @FunctionalInterface
   protected interface EntryConstructor {
      LootPoolSingletonContainer build(int pWeight, int pQuality, LootItemCondition[] p_79729_, LootItemFunction[] p_79730_);
   }

   public abstract static class Serializer<T extends LootPoolSingletonContainer> extends LootPoolEntryContainer.Serializer<T> {
      public void m_7219_(JsonObject p_79741_, T p_79742_, JsonSerializationContext p_79743_) {
         if (p_79742_.weight != 1) {
            p_79741_.addProperty("weight", p_79742_.weight);
         }

         if (p_79742_.quality != 0) {
            p_79741_.addProperty("quality", p_79742_.quality);
         }

         if (!ArrayUtils.isEmpty((Object[])p_79742_.functions)) {
            p_79741_.add("functions", p_79743_.serialize(p_79742_.functions));
         }

      }

      public final T m_5921_(JsonObject p_79733_, JsonDeserializationContext p_79734_, LootItemCondition[] p_79735_) {
         int i = GsonHelper.getAsInt(p_79733_, "weight", 1);
         int j = GsonHelper.getAsInt(p_79733_, "quality", 0);
         LootItemFunction[] alootitemfunction = GsonHelper.getAsObject(p_79733_, "functions", new LootItemFunction[0], p_79734_, LootItemFunction[].class);
         return this.m_7267_(p_79733_, p_79734_, i, j, p_79735_, alootitemfunction);
      }

      protected abstract T m_7267_(JsonObject p_79744_, JsonDeserializationContext p_79745_, int p_79746_, int p_79747_, LootItemCondition[] p_79748_, LootItemFunction[] p_79749_);
   }
}