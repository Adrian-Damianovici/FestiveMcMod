package net.minecraft.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;

public interface CriterionTrigger<T extends CriterionTriggerInstance> {
   ResourceLocation m_7295_();

   void addPlayerListener(PlayerAdvancements pPlayerAdvancements, CriterionTrigger.Listener<T> pListener);

   void removePlayerListener(PlayerAdvancements pPlayerAdvancements, CriterionTrigger.Listener<T> pListener);

   void removePlayerListeners(PlayerAdvancements pPlayerAdvancements);

   T createInstance(JsonObject pJson, DeserializationContext pContext);

   public static class Listener<T extends CriterionTriggerInstance> {
      private final T trigger;
      private final Advancement advancement;
      private final String criterion;

      public Listener(T p_13682_, Advancement p_13683_, String p_13684_) {
         this.trigger = p_13682_;
         this.advancement = p_13683_;
         this.criterion = p_13684_;
      }

      public T m_13685_() {
         return this.trigger;
      }

      public void run(PlayerAdvancements pPlayerAdvancements) {
         pPlayerAdvancements.award(this.advancement, this.criterion);
      }

      public boolean equals(Object pOther) {
         if (this == pOther) {
            return true;
         } else if (pOther != null && this.getClass() == pOther.getClass()) {
            CriterionTrigger.Listener<?> listener = (CriterionTrigger.Listener)pOther;
            if (!this.trigger.equals(listener.trigger)) {
               return false;
            } else {
               return !this.advancement.equals(listener.advancement) ? false : this.criterion.equals(listener.criterion);
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         int i = this.trigger.hashCode();
         i = 31 * i + this.advancement.hashCode();
         return 31 * i + this.criterion.hashCode();
      }
   }
}