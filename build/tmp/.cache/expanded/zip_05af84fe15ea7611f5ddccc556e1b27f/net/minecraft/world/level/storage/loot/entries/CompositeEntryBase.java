package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Base class for loot pool entry containers that delegate to one or more children.
 * The actual functionality is provided by composing the children into one composed container (see {@link #compose}).
 */
public abstract class CompositeEntryBase extends LootPoolEntryContainer {
   protected final LootPoolEntryContainer[] children;
   private final ComposableEntryContainer composedChildren;

   protected CompositeEntryBase(LootPoolEntryContainer[] p_79431_, LootItemCondition[] p_79432_) {
      super(p_79432_);
      this.children = p_79431_;
      this.composedChildren = this.compose(p_79431_);
   }

   public void validate(ValidationContext pValidationContext) {
      super.validate(pValidationContext);
      if (this.children.length == 0) {
         pValidationContext.reportProblem("Empty children list");
      }

      for(int i = 0; i < this.children.length; ++i) {
         this.children[i].validate(pValidationContext.forChild(".entry[" + i + "]"));
      }

   }

   protected abstract ComposableEntryContainer compose(ComposableEntryContainer[] p_79437_);

   /**
    * Expand this loot pool entry container by calling {@code entryConsumer} with any applicable entries
    * 
    * @return whether this loot pool entry container successfully expanded or not
    */
   public final boolean expand(LootContext pLootContext, Consumer<LootPoolEntry> pEntryConsumer) {
      return !this.canRun(pLootContext) ? false : this.composedChildren.expand(pLootContext, pEntryConsumer);
   }

   public static <T extends CompositeEntryBase> LootPoolEntryContainer.Serializer<T> m_79435_(final CompositeEntryBase.CompositeEntryConstructor<T> p_79436_) {
      return new LootPoolEntryContainer.Serializer<T>() {
         public void m_7219_(JsonObject p_79449_, T p_79450_, JsonSerializationContext p_79451_) {
            p_79449_.add("children", p_79451_.serialize(p_79450_.children));
         }

         public final T m_5921_(JsonObject p_79445_, JsonDeserializationContext p_79446_, LootItemCondition[] p_79447_) {
            LootPoolEntryContainer[] alootpoolentrycontainer = GsonHelper.getAsObject(p_79445_, "children", p_79446_, LootPoolEntryContainer[].class);
            return p_79436_.create(alootpoolentrycontainer, p_79447_);
         }
      };
   }

   @FunctionalInterface
   public interface CompositeEntryConstructor<T extends CompositeEntryBase> {
      T create(LootPoolEntryContainer[] p_79461_, LootItemCondition[] p_79462_);
   }
}