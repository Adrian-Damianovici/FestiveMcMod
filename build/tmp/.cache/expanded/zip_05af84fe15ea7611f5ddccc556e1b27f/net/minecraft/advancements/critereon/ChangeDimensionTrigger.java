package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;

public class ChangeDimensionTrigger extends SimpleCriterionTrigger<ChangeDimensionTrigger.TriggerInstance> {
   static final ResourceLocation f_19753_ = new ResourceLocation("changed_dimension");

   public ResourceLocation m_7295_() {
      return f_19753_;
   }

   public ChangeDimensionTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate p_286295_, DeserializationContext pDeserializationContext) {
      ResourceKey<Level> resourcekey = pJson.has("from") ? ResourceKey.create(Registries.DIMENSION, new ResourceLocation(GsonHelper.getAsString(pJson, "from"))) : null;
      ResourceKey<Level> resourcekey1 = pJson.has("to") ? ResourceKey.create(Registries.DIMENSION, new ResourceLocation(GsonHelper.getAsString(pJson, "to"))) : null;
      return new ChangeDimensionTrigger.TriggerInstance(p_286295_, resourcekey, resourcekey1);
   }

   public void trigger(ServerPlayer pPlayer, ResourceKey<Level> pFromLevel, ResourceKey<Level> pToLevel) {
      this.trigger(pPlayer, (p_19768_) -> {
         return p_19768_.matches(pFromLevel, pToLevel);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      @Nullable
      private final ResourceKey<Level> from;
      @Nullable
      private final ResourceKey<Level> to;

      public TriggerInstance(ContextAwarePredicate p_286423_, @Nullable ResourceKey<Level> pFrom, @Nullable ResourceKey<Level> pTo) {
         super(ChangeDimensionTrigger.f_19753_, p_286423_);
         this.from = pFrom;
         this.to = pTo;
      }

      public static ChangeDimensionTrigger.TriggerInstance changedDimension() {
         return new ChangeDimensionTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, (ResourceKey<Level>)null, (ResourceKey<Level>)null);
      }

      public static ChangeDimensionTrigger.TriggerInstance changedDimension(ResourceKey<Level> p_147561_, ResourceKey<Level> p_147562_) {
         return new ChangeDimensionTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, p_147561_, p_147562_);
      }

      public static ChangeDimensionTrigger.TriggerInstance changedDimensionTo(ResourceKey<Level> pTo) {
         return new ChangeDimensionTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, (ResourceKey<Level>)null, pTo);
      }

      public static ChangeDimensionTrigger.TriggerInstance changedDimensionFrom(ResourceKey<Level> pFrom) {
         return new ChangeDimensionTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, pFrom, (ResourceKey<Level>)null);
      }

      public boolean matches(ResourceKey<Level> pFromLevel, ResourceKey<Level> pToLevel) {
         if (this.from != null && this.from != pFromLevel) {
            return false;
         } else {
            return this.to == null || this.to == pToLevel;
         }
      }

      public JsonObject serializeToJson(SerializationContext p_19781_) {
         JsonObject jsonobject = super.serializeToJson(p_19781_);
         if (this.from != null) {
            jsonobject.addProperty("from", this.from.location().toString());
         }

         if (this.to != null) {
            jsonobject.addProperty("to", this.to.location().toString());
         }

         return jsonobject;
      }
   }
}