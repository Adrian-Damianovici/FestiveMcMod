package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.storage.loot.LootContext;

public class LightningStrikeTrigger extends SimpleCriterionTrigger<LightningStrikeTrigger.TriggerInstance> {
   static final ResourceLocation f_153384_ = new ResourceLocation("lightning_strike");

   public ResourceLocation m_7295_() {
      return f_153384_;
   }

   public LightningStrikeTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate p_286650_, DeserializationContext pDeserializationContext) {
      ContextAwarePredicate contextawarepredicate = EntityPredicate.fromJson(pJson, "lightning", pDeserializationContext);
      ContextAwarePredicate contextawarepredicate1 = EntityPredicate.fromJson(pJson, "bystander", pDeserializationContext);
      return new LightningStrikeTrigger.TriggerInstance(p_286650_, contextawarepredicate, contextawarepredicate1);
   }

   public void trigger(ServerPlayer pPlayer, LightningBolt pLightning, List<Entity> pNearbyEntities) {
      List<LootContext> list = pNearbyEntities.stream().map((p_153390_) -> {
         return EntityPredicate.createContext(pPlayer, p_153390_);
      }).collect(Collectors.toList());
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pLightning);
      this.trigger(pPlayer, (p_153402_) -> {
         return p_153402_.matches(lootcontext, list);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate lightning;
      private final ContextAwarePredicate bystander;

      public TriggerInstance(ContextAwarePredicate p_286747_, ContextAwarePredicate p_286287_, ContextAwarePredicate p_286566_) {
         super(LightningStrikeTrigger.f_153384_, p_286747_);
         this.lightning = p_286287_;
         this.bystander = p_286566_;
      }

      public static LightningStrikeTrigger.TriggerInstance m_153413_(EntityPredicate p_153414_, EntityPredicate p_153415_) {
         return new LightningStrikeTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, EntityPredicate.wrap(p_153414_), EntityPredicate.wrap(p_153415_));
      }

      public boolean matches(LootContext pPlayerContext, List<LootContext> pEntityContexts) {
         if (!this.lightning.matches(pPlayerContext)) {
            return false;
         } else {
            return this.bystander == ContextAwarePredicate.f_285567_ || !pEntityContexts.stream().noneMatch(this.bystander::matches);
         }
      }

      public JsonObject serializeToJson(SerializationContext p_153417_) {
         JsonObject jsonobject = super.serializeToJson(p_153417_);
         jsonobject.add("lightning", this.lightning.toJson(p_153417_));
         jsonobject.add("bystander", this.bystander.toJson(p_153417_));
         return jsonobject;
      }
   }
}