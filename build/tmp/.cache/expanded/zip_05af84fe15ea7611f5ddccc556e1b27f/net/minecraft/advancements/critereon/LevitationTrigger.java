package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class LevitationTrigger extends SimpleCriterionTrigger<LevitationTrigger.TriggerInstance> {
   static final ResourceLocation f_49112_ = new ResourceLocation("levitation");

   public ResourceLocation m_7295_() {
      return f_49112_;
   }

   public LevitationTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate p_286588_, DeserializationContext pDeserializationContext) {
      DistancePredicate distancepredicate = DistancePredicate.fromJson(pJson.get("distance"));
      MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(pJson.get("duration"));
      return new LevitationTrigger.TriggerInstance(p_286588_, distancepredicate, minmaxbounds$ints);
   }

   public void trigger(ServerPlayer pPlayer, Vec3 pStartPos, int pDuration) {
      this.trigger(pPlayer, (p_49124_) -> {
         return p_49124_.matches(pPlayer, pStartPos, pDuration);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final DistancePredicate distance;
      private final MinMaxBounds.Ints duration;

      public TriggerInstance(ContextAwarePredicate p_286511_, DistancePredicate p_286806_, MinMaxBounds.Ints pDuration) {
         super(LevitationTrigger.f_49112_, p_286511_);
         this.distance = p_286806_;
         this.duration = pDuration;
      }

      public static LevitationTrigger.TriggerInstance levitated(DistancePredicate pDistance) {
         return new LevitationTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, pDistance, MinMaxBounds.Ints.ANY);
      }

      public boolean matches(ServerPlayer pPlayer, Vec3 pStartPos, int pDuration) {
         if (!this.distance.matches(pStartPos.x, pStartPos.y, pStartPos.z, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ())) {
            return false;
         } else {
            return this.duration.matches(pDuration);
         }
      }

      public JsonObject serializeToJson(SerializationContext p_49147_) {
         JsonObject jsonobject = super.serializeToJson(p_49147_);
         jsonobject.add("distance", this.distance.serializeToJson());
         jsonobject.add("duration", this.duration.m_55328_());
         return jsonobject;
      }
   }
}