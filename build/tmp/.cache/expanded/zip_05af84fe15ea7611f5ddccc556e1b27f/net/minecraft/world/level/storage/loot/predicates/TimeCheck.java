package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

/**
 * A LootItemCondition that checks the {@linkplain ServerLevel#getDayTime day time} against an {@link IntRange} after
 * applying an optional modulo division.
 */
public class TimeCheck implements LootItemCondition {
   @Nullable
   final Long period;
   final IntRange value;

   TimeCheck(@Nullable Long p_165507_, IntRange p_165508_) {
      this.period = p_165507_;
      this.value = p_165508_;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.TIME_CHECK;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.value.getReferencedContextParams();
   }

   public boolean test(LootContext pContext) {
      ServerLevel serverlevel = pContext.getLevel();
      long i = serverlevel.getDayTime();
      if (this.period != null) {
         i %= this.period;
      }

      return this.value.test(pContext, (int)i);
   }

   public static TimeCheck.Builder time(IntRange pTimeRange) {
      return new TimeCheck.Builder(pTimeRange);
   }

   public static class Builder implements LootItemCondition.Builder {
      @Nullable
      private Long period;
      private final IntRange value;

      public Builder(IntRange pTimeRange) {
         this.value = pTimeRange;
      }

      public TimeCheck.Builder setPeriod(long pPeriod) {
         this.period = pPeriod;
         return this;
      }

      public TimeCheck build() {
         return new TimeCheck(this.period, this.value);
      }
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<TimeCheck> {
      public void m_6170_(JsonObject p_82046_, TimeCheck p_82047_, JsonSerializationContext p_82048_) {
         p_82046_.addProperty("period", p_82047_.period);
         p_82046_.add("value", p_82048_.serialize(p_82047_.value));
      }

      public TimeCheck m_7561_(JsonObject p_82054_, JsonDeserializationContext p_82055_) {
         Long olong = p_82054_.has("period") ? GsonHelper.getAsLong(p_82054_, "period") : null;
         IntRange intrange = GsonHelper.getAsObject(p_82054_, "value", p_82055_, IntRange.class);
         return new TimeCheck(olong, intrange);
      }
   }
}