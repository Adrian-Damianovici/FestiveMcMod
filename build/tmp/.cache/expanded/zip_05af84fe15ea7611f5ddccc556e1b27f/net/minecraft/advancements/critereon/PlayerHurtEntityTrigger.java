package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerHurtEntityTrigger extends SimpleCriterionTrigger<PlayerHurtEntityTrigger.TriggerInstance> {
   static final ResourceLocation f_60108_ = new ResourceLocation("player_hurt_entity");

   public ResourceLocation m_7295_() {
      return f_60108_;
   }

   public PlayerHurtEntityTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate p_286426_, DeserializationContext pDeserializationContext) {
      DamagePredicate damagepredicate = DamagePredicate.fromJson(pJson.get("damage"));
      ContextAwarePredicate contextawarepredicate = EntityPredicate.fromJson(pJson, "entity", pDeserializationContext);
      return new PlayerHurtEntityTrigger.TriggerInstance(p_286426_, damagepredicate, contextawarepredicate);
   }

   public void trigger(ServerPlayer pPlayer, Entity pEntity, DamageSource pSource, float pAmountDealt, float pAmountTaken, boolean pBlocked) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
      this.trigger(pPlayer, (p_60126_) -> {
         return p_60126_.matches(pPlayer, lootcontext, pSource, pAmountDealt, pAmountTaken, pBlocked);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final DamagePredicate damage;
      private final ContextAwarePredicate entity;

      public TriggerInstance(ContextAwarePredicate p_286866_, DamagePredicate p_286225_, ContextAwarePredicate p_286266_) {
         super(PlayerHurtEntityTrigger.f_60108_, p_286866_);
         this.damage = p_286225_;
         this.entity = p_286266_;
      }

      public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity() {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, DamagePredicate.f_24902_, ContextAwarePredicate.f_285567_);
      }

      public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate p_156062_) {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, p_156062_, ContextAwarePredicate.f_285567_);
      }

      public static PlayerHurtEntityTrigger.TriggerInstance m_60149_(DamagePredicate.Builder p_60150_) {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, p_60150_.build(), ContextAwarePredicate.f_285567_);
      }

      public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(EntityPredicate p_156067_) {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, DamagePredicate.f_24902_, EntityPredicate.wrap(p_156067_));
      }

      public static PlayerHurtEntityTrigger.TriggerInstance m_156063_(DamagePredicate p_156064_, EntityPredicate p_156065_) {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, p_156064_, EntityPredicate.wrap(p_156065_));
      }

      public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate.Builder p_156059_, EntityPredicate p_156060_) {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, p_156059_.build(), EntityPredicate.wrap(p_156060_));
      }

      public boolean matches(ServerPlayer pPlayer, LootContext pContext, DamageSource pDamage, float pDealt, float pTaken, boolean pBlocked) {
         if (!this.damage.matches(pPlayer, pDamage, pDealt, pTaken, pBlocked)) {
            return false;
         } else {
            return this.entity.matches(pContext);
         }
      }

      public JsonObject serializeToJson(SerializationContext p_60152_) {
         JsonObject jsonobject = super.serializeToJson(p_60152_);
         jsonobject.add("damage", this.damage.serializeToJson());
         jsonobject.add("entity", this.entity.toJson(p_60152_));
         return jsonobject;
      }
   }
}