package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class EffectsChangedTrigger extends SimpleCriterionTrigger<EffectsChangedTrigger.TriggerInstance> {
   static final ResourceLocation f_26756_ = new ResourceLocation("effects_changed");

   public ResourceLocation m_7295_() {
      return f_26756_;
   }

   public EffectsChangedTrigger.TriggerInstance createInstance(JsonObject p_286892_, ContextAwarePredicate p_286547_, DeserializationContext p_286271_) {
      MobEffectsPredicate mobeffectspredicate = MobEffectsPredicate.fromJson(p_286892_.get("effects"));
      ContextAwarePredicate contextawarepredicate = EntityPredicate.fromJson(p_286892_, "source", p_286271_);
      return new EffectsChangedTrigger.TriggerInstance(p_286547_, mobeffectspredicate, contextawarepredicate);
   }

   public void trigger(ServerPlayer pPlayer, @Nullable Entity pSource) {
      LootContext lootcontext = pSource != null ? EntityPredicate.createContext(pPlayer, pSource) : null;
      this.trigger(pPlayer, (p_149268_) -> {
         return p_149268_.matches(pPlayer, lootcontext);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final MobEffectsPredicate effects;
      private final ContextAwarePredicate source;

      public TriggerInstance(ContextAwarePredicate p_286580_, MobEffectsPredicate p_286820_, ContextAwarePredicate p_286703_) {
         super(EffectsChangedTrigger.f_26756_, p_286580_);
         this.effects = p_286820_;
         this.source = p_286703_;
      }

      public static EffectsChangedTrigger.TriggerInstance hasEffects(MobEffectsPredicate p_26781_) {
         return new EffectsChangedTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, p_26781_, ContextAwarePredicate.f_285567_);
      }

      public static EffectsChangedTrigger.TriggerInstance gotEffectsFrom(EntityPredicate p_149278_) {
         return new EffectsChangedTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, MobEffectsPredicate.f_56547_, EntityPredicate.wrap(p_149278_));
      }

      public boolean matches(ServerPlayer pPlayer, @Nullable LootContext pLootContext) {
         if (!this.effects.matches(pPlayer)) {
            return false;
         } else {
            return this.source == ContextAwarePredicate.f_285567_ || pLootContext != null && this.source.matches(pLootContext);
         }
      }

      public JsonObject serializeToJson(SerializationContext p_26783_) {
         JsonObject jsonobject = super.serializeToJson(p_26783_);
         jsonobject.add("effects", this.effects.serializeToJson());
         jsonobject.add("source", this.source.toJson(p_26783_));
         return jsonobject;
      }
   }
}