package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class DistanceTrigger extends SimpleCriterionTrigger<DistanceTrigger.TriggerInstance> {
   final ResourceLocation f_186161_;

   public DistanceTrigger(ResourceLocation p_186163_) {
      this.f_186161_ = p_186163_;
   }

   public ResourceLocation m_7295_() {
      return this.f_186161_;
   }

   public DistanceTrigger.TriggerInstance createInstance(JsonObject p_286540_, ContextAwarePredicate p_286753_, DeserializationContext p_286709_) {
      LocationPredicate locationpredicate = LocationPredicate.fromJson(p_286540_.get("start_position"));
      DistancePredicate distancepredicate = DistancePredicate.fromJson(p_286540_.get("distance"));
      return new DistanceTrigger.TriggerInstance(this.f_186161_, p_286753_, locationpredicate, distancepredicate);
   }

   public void trigger(ServerPlayer pPlayer, Vec3 pPosition) {
      Vec3 vec3 = pPlayer.position();
      this.trigger(pPlayer, (p_284572_) -> {
         return p_284572_.matches(pPlayer.serverLevel(), pPosition, vec3);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final LocationPredicate startPosition;
      private final DistancePredicate distance;

      public TriggerInstance(ResourceLocation p_286369_, ContextAwarePredicate p_286587_, LocationPredicate p_286563_, DistancePredicate p_286818_) {
         super(p_286369_, p_286587_);
         this.startPosition = p_286563_;
         this.distance = p_286818_;
      }

      public static DistanceTrigger.TriggerInstance fallFromHeight(EntityPredicate.Builder pPlayer, DistancePredicate pDistance, LocationPredicate p_186200_) {
         return new DistanceTrigger.TriggerInstance(CriteriaTriggers.FALL_FROM_HEIGHT.f_186161_, EntityPredicate.wrap(pPlayer.build()), p_186200_, pDistance);
      }

      public static DistanceTrigger.TriggerInstance rideEntityInLava(EntityPredicate.Builder pPlayer, DistancePredicate pDistance) {
         return new DistanceTrigger.TriggerInstance(CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER.f_186161_, EntityPredicate.wrap(pPlayer.build()), LocationPredicate.f_52592_, pDistance);
      }

      public static DistanceTrigger.TriggerInstance travelledThroughNether(DistancePredicate pDistance) {
         return new DistanceTrigger.TriggerInstance(CriteriaTriggers.NETHER_TRAVEL.f_186161_, ContextAwarePredicate.f_285567_, LocationPredicate.f_52592_, pDistance);
      }

      public JsonObject serializeToJson(SerializationContext p_186202_) {
         JsonObject jsonobject = super.serializeToJson(p_186202_);
         jsonobject.add("start_position", this.startPosition.serializeToJson());
         jsonobject.add("distance", this.distance.serializeToJson());
         return jsonobject;
      }

      public boolean matches(ServerLevel pLevel, Vec3 pStartPosition, Vec3 pCurrentPosition) {
         if (!this.startPosition.matches(pLevel, pStartPosition.x, pStartPosition.y, pStartPosition.z)) {
            return false;
         } else {
            return this.distance.matches(pStartPosition.x, pStartPosition.y, pStartPosition.z, pCurrentPosition.x, pCurrentPosition.y, pCurrentPosition.z);
         }
      }
   }
}