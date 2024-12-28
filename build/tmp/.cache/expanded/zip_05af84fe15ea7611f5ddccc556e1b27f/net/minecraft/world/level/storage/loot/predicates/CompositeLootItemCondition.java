package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;

public abstract class CompositeLootItemCondition implements LootItemCondition {
   final LootItemCondition[] terms;
   private final Predicate<LootContext> composedPredicate;

   protected CompositeLootItemCondition(LootItemCondition[] p_286437_, Predicate<LootContext> pComposedPredicate) {
      this.terms = p_286437_;
      this.composedPredicate = pComposedPredicate;
   }

   public final boolean test(LootContext pContext) {
      return this.composedPredicate.test(pContext);
   }

   /**
    * Validate that this object is used correctly according to the given ValidationContext.
    */
   public void validate(ValidationContext pContext) {
      LootItemCondition.super.validate(pContext);

      for(int i = 0; i < this.terms.length; ++i) {
         this.terms[i].validate(pContext.forChild(".term[" + i + "]"));
      }

   }

   public abstract static class Builder implements LootItemCondition.Builder {
      private final List<LootItemCondition> terms = new ArrayList<>();

      public Builder(LootItemCondition.Builder... pConditions) {
         for(LootItemCondition.Builder lootitemcondition$builder : pConditions) {
            this.terms.add(lootitemcondition$builder.build());
         }

      }

      public void addTerm(LootItemCondition.Builder pCondition) {
         this.terms.add(pCondition.build());
      }

      public LootItemCondition build() {
         LootItemCondition[] alootitemcondition = this.terms.toArray((p_286455_) -> {
            return new LootItemCondition[p_286455_];
         });
         return this.create(alootitemcondition);
      }

      protected abstract LootItemCondition create(LootItemCondition[] p_286469_);
   }

   public abstract static class Serializer<T extends CompositeLootItemCondition> implements net.minecraft.world.level.storage.loot.Serializer<T> {
      public void m_6170_(JsonObject p_286342_, CompositeLootItemCondition p_286412_, JsonSerializationContext p_286331_) {
         p_286342_.add("terms", p_286331_.serialize(p_286412_.terms));
      }

      public T m_7561_(JsonObject p_286509_, JsonDeserializationContext p_286321_) {
         LootItemCondition[] alootitemcondition = GsonHelper.getAsObject(p_286509_, "terms", p_286321_, LootItemCondition[].class);
         return this.m_285830_(alootitemcondition);
      }

      protected abstract T m_285830_(LootItemCondition[] p_286604_);
   }
}