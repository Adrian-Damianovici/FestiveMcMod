package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class EntityHurtPlayerTrigger extends SimpleCriterionTrigger<EntityHurtPlayerTrigger.TriggerInstance> {
   static final ResourceLocation f_35170_ = new ResourceLocation("entity_hurt_player");

   public ResourceLocation m_7295_() {
      return f_35170_;
   }

   public EntityHurtPlayerTrigger.TriggerInstance createInstance(JsonObject p_286446_, ContextAwarePredicate p_286687_, DeserializationContext p_286799_) {
      DamagePredicate damagepredicate = DamagePredicate.fromJson(p_286446_.get("damage"));
      return new EntityHurtPlayerTrigger.TriggerInstance(p_286687_, damagepredicate);
   }

   public void trigger(ServerPlayer pPlayer, DamageSource pSource, float pDealtDamage, float pTakenDamage, boolean pBlocked) {
      this.trigger(pPlayer, (p_35186_) -> {
         return p_35186_.matches(pPlayer, pSource, pDealtDamage, pTakenDamage, pBlocked);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final DamagePredicate damage;

      public TriggerInstance(ContextAwarePredicate p_286419_, DamagePredicate p_286408_) {
         super(EntityHurtPlayerTrigger.f_35170_, p_286419_);
         this.damage = p_286408_;
      }

      public static EntityHurtPlayerTrigger.TriggerInstance entityHurtPlayer() {
         return new EntityHurtPlayerTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, DamagePredicate.f_24902_);
      }

      public static EntityHurtPlayerTrigger.TriggerInstance entityHurtPlayer(DamagePredicate pDamage) {
         return new EntityHurtPlayerTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, pDamage);
      }

      public static EntityHurtPlayerTrigger.TriggerInstance entityHurtPlayer(DamagePredicate.Builder pDamage) {
         return new EntityHurtPlayerTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, pDamage.build());
      }

      public boolean matches(ServerPlayer pPlayer, DamageSource pSource, float pDealtDamage, float pTakenDamage, boolean pBlocked) {
         return this.damage.matches(pPlayer, pSource, pDealtDamage, pTakenDamage, pBlocked);
      }

      public JsonObject serializeToJson(SerializationContext p_35209_) {
         JsonObject jsonobject = super.serializeToJson(p_35209_);
         jsonobject.add("damage", this.damage.serializeToJson());
         return jsonobject;
      }
   }
}