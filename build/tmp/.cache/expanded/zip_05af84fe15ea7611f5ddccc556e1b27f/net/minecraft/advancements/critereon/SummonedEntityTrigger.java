package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class SummonedEntityTrigger extends SimpleCriterionTrigger<SummonedEntityTrigger.TriggerInstance> {
   static final ResourceLocation f_68252_ = new ResourceLocation("summoned_entity");

   public ResourceLocation m_7295_() {
      return f_68252_;
   }

   public SummonedEntityTrigger.TriggerInstance createInstance(JsonObject p_286669_, ContextAwarePredicate p_286745_, DeserializationContext p_286637_) {
      ContextAwarePredicate contextawarepredicate = EntityPredicate.fromJson(p_286669_, "entity", p_286637_);
      return new SummonedEntityTrigger.TriggerInstance(p_286745_, contextawarepredicate);
   }

   public void trigger(ServerPlayer pPlayer, Entity pEntity) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
      this.trigger(pPlayer, (p_68265_) -> {
         return p_68265_.matches(lootcontext);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate entity;

      public TriggerInstance(ContextAwarePredicate p_286853_, ContextAwarePredicate p_286838_) {
         super(SummonedEntityTrigger.f_68252_, p_286853_);
         this.entity = p_286838_;
      }

      public static SummonedEntityTrigger.TriggerInstance summonedEntity(EntityPredicate.Builder pEntity) {
         return new SummonedEntityTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, EntityPredicate.wrap(pEntity.build()));
      }

      public boolean matches(LootContext pLootContext) {
         return this.entity.matches(pLootContext);
      }

      public JsonObject serializeToJson(SerializationContext p_68278_) {
         JsonObject jsonobject = super.serializeToJson(p_68278_);
         jsonobject.add("entity", this.entity.toJson(p_68278_));
         return jsonobject;
      }
   }
}