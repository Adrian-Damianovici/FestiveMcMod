package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Base class for loot pool entry containers. This class just stores a list of conditions that are checked before the
 * entry generates loot.
 */
public abstract class LootPoolEntryContainer implements ComposableEntryContainer {
   /** Conditions for the loot entry to be applied. */
   protected final LootItemCondition[] conditions;
   private final Predicate<LootContext> compositeCondition;

   protected LootPoolEntryContainer(LootItemCondition[] p_79638_) {
      this.conditions = p_79638_;
      this.compositeCondition = LootItemConditions.andConditions(p_79638_);
   }

   public void validate(ValidationContext pValidationContext) {
      for(int i = 0; i < this.conditions.length; ++i) {
         this.conditions[i].validate(pValidationContext.forChild(".condition[" + i + "]"));
      }

   }

   protected final boolean canRun(LootContext pLootContext) {
      return this.compositeCondition.test(pLootContext);
   }

   public abstract LootPoolEntryType getType();

   public abstract static class Builder<T extends LootPoolEntryContainer.Builder<T>> implements ConditionUserBuilder<T> {
      private final List<LootItemCondition> conditions = Lists.newArrayList();

      protected abstract T getThis();

      public T when(LootItemCondition.Builder pConditionBuilder) {
         this.conditions.add(pConditionBuilder.build());
         return this.getThis();
      }

      public final T unwrap() {
         return this.getThis();
      }

      protected LootItemCondition[] getConditions() {
         return this.conditions.toArray(new LootItemCondition[0]);
      }

      public AlternativesEntry.Builder otherwise(LootPoolEntryContainer.Builder<?> pChildBuilder) {
         return new AlternativesEntry.Builder(this, pChildBuilder);
      }

      public EntryGroup.Builder append(LootPoolEntryContainer.Builder<?> pChildBuilder) {
         return new EntryGroup.Builder(this, pChildBuilder);
      }

      public SequentialEntry.Builder then(LootPoolEntryContainer.Builder<?> pChildBuilder) {
         return new SequentialEntry.Builder(this, pChildBuilder);
      }

      public abstract LootPoolEntryContainer build();
   }

   public abstract static class Serializer<T extends LootPoolEntryContainer> implements net.minecraft.world.level.storage.loot.Serializer<T> {
      public final void m_6170_(JsonObject p_79670_, T p_79671_, JsonSerializationContext p_79672_) {
         if (!ArrayUtils.isEmpty((Object[])p_79671_.conditions)) {
            p_79670_.add("conditions", p_79672_.serialize(p_79671_.conditions));
         }

         this.m_7219_(p_79670_, p_79671_, p_79672_);
      }

      public final T m_7561_(JsonObject p_79664_, JsonDeserializationContext p_79665_) {
         LootItemCondition[] alootitemcondition = GsonHelper.getAsObject(p_79664_, "conditions", new LootItemCondition[0], p_79665_, LootItemCondition[].class);
         return this.m_5921_(p_79664_, p_79665_, alootitemcondition);
      }

      public abstract void m_7219_(JsonObject p_79656_, T p_79657_, JsonSerializationContext p_79658_);

      public abstract T m_5921_(JsonObject p_79666_, JsonDeserializationContext p_79667_, LootItemCondition[] p_79668_);
   }
}