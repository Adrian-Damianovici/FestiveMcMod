package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class UsedEnderEyeTrigger extends SimpleCriterionTrigger<UsedEnderEyeTrigger.TriggerInstance> {
   static final ResourceLocation f_73928_ = new ResourceLocation("used_ender_eye");

   public ResourceLocation m_7295_() {
      return f_73928_;
   }

   public UsedEnderEyeTrigger.TriggerInstance createInstance(JsonObject p_286292_, ContextAwarePredicate p_286488_, DeserializationContext p_286702_) {
      MinMaxBounds.Doubles minmaxbounds$doubles = MinMaxBounds.Doubles.fromJson(p_286292_.get("distance"));
      return new UsedEnderEyeTrigger.TriggerInstance(p_286488_, minmaxbounds$doubles);
   }

   public void trigger(ServerPlayer pPlayer, BlockPos pPos) {
      double d0 = pPlayer.getX() - (double)pPos.getX();
      double d1 = pPlayer.getZ() - (double)pPos.getZ();
      double d2 = d0 * d0 + d1 * d1;
      this.trigger(pPlayer, (p_73934_) -> {
         return p_73934_.matches(d2);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final MinMaxBounds.Doubles level;

      public TriggerInstance(ContextAwarePredicate p_286567_, MinMaxBounds.Doubles pLevel) {
         super(UsedEnderEyeTrigger.f_73928_, p_286567_);
         this.level = pLevel;
      }

      public boolean matches(double pDistanceSq) {
         return this.level.matchesSqr(pDistanceSq);
      }
   }
}