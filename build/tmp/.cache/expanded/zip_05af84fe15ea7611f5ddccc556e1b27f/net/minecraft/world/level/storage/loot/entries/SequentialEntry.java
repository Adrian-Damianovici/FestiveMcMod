package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * A composite loot pool entry container that expands all its children in order until one of them fails.
 * This container succeeds if all children succeed.
 */
public class SequentialEntry extends CompositeEntryBase {
   SequentialEntry(LootPoolEntryContainer[] p_79812_, LootItemCondition[] p_79813_) {
      super(p_79812_, p_79813_);
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.SEQUENCE;
   }

   protected ComposableEntryContainer compose(ComposableEntryContainer[] p_79816_) {
      switch (p_79816_.length) {
         case 0:
            return ALWAYS_TRUE;
         case 1:
            return p_79816_[0];
         case 2:
            return p_79816_[0].and(p_79816_[1]);
         default:
            return (p_79819_, p_79820_) -> {
               for(ComposableEntryContainer composableentrycontainer : p_79816_) {
                  if (!composableentrycontainer.expand(p_79819_, p_79820_)) {
                     return false;
                  }
               }

               return true;
            };
      }
   }

   public static SequentialEntry.Builder sequential(LootPoolEntryContainer.Builder<?>... pChildren) {
      return new SequentialEntry.Builder(pChildren);
   }

   public static class Builder extends LootPoolEntryContainer.Builder<SequentialEntry.Builder> {
      private final List<LootPoolEntryContainer> entries = Lists.newArrayList();

      public Builder(LootPoolEntryContainer.Builder<?>... pChildren) {
         for(LootPoolEntryContainer.Builder<?> builder : pChildren) {
            this.entries.add(builder.build());
         }

      }

      protected SequentialEntry.Builder getThis() {
         return this;
      }

      public SequentialEntry.Builder then(LootPoolEntryContainer.Builder<?> pChildBuilder) {
         this.entries.add(pChildBuilder.build());
         return this;
      }

      public LootPoolEntryContainer build() {
         return new SequentialEntry(this.entries.toArray(new LootPoolEntryContainer[0]), this.getConditions());
      }
   }
}