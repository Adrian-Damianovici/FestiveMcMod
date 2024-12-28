package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ConstructBeaconTrigger extends SimpleCriterionTrigger<ConstructBeaconTrigger.TriggerInstance> {
   static final ResourceLocation f_22742_ = new ResourceLocation("construct_beacon");

   public ResourceLocation m_7295_() {
      return f_22742_;
   }

   public ConstructBeaconTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate p_286914_, DeserializationContext pDeserializationContext) {
      MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(pJson.get("level"));
      return new ConstructBeaconTrigger.TriggerInstance(p_286914_, minmaxbounds$ints);
   }

   public void trigger(ServerPlayer pPlayer, int pLevel) {
      this.trigger(pPlayer, (p_148028_) -> {
         return p_148028_.matches(pLevel);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final MinMaxBounds.Ints level;

      public TriggerInstance(ContextAwarePredicate p_286868_, MinMaxBounds.Ints pLevel) {
         super(ConstructBeaconTrigger.f_22742_, p_286868_);
         this.level = pLevel;
      }

      public static ConstructBeaconTrigger.TriggerInstance constructedBeacon() {
         return new ConstructBeaconTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, MinMaxBounds.Ints.ANY);
      }

      public static ConstructBeaconTrigger.TriggerInstance constructedBeacon(MinMaxBounds.Ints pLevel) {
         return new ConstructBeaconTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, pLevel);
      }

      public boolean matches(int pLevel) {
         return this.level.matches(pLevel);
      }

      public JsonObject serializeToJson(SerializationContext p_22770_) {
         JsonObject jsonobject = super.serializeToJson(p_22770_);
         jsonobject.add("level", this.level.m_55328_());
         return jsonobject;
      }
   }
}