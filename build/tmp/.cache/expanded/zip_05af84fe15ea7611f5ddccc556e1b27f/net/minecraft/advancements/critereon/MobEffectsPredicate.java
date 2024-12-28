package net.minecraft.advancements.critereon;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class MobEffectsPredicate {
   public static final MobEffectsPredicate f_56547_ = new MobEffectsPredicate(Collections.emptyMap());
   private final Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> f_56548_;

   public MobEffectsPredicate(Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> p_56551_) {
      this.f_56548_ = p_56551_;
   }

   public static MobEffectsPredicate m_56552_() {
      return new MobEffectsPredicate(Maps.newLinkedHashMap());
   }

   public MobEffectsPredicate m_56553_(MobEffect p_56554_) {
      this.f_56548_.put(p_56554_, new MobEffectsPredicate.MobEffectInstancePredicate());
      return this;
   }

   public MobEffectsPredicate m_154977_(MobEffect p_154978_, MobEffectsPredicate.MobEffectInstancePredicate p_154979_) {
      this.f_56548_.put(p_154978_, p_154979_);
      return this;
   }

   public boolean matches(Entity pEntity) {
      if (this == f_56547_) {
         return true;
      } else {
         return pEntity instanceof LivingEntity ? this.matches(((LivingEntity)pEntity).getActiveEffectsMap()) : false;
      }
   }

   public boolean matches(LivingEntity pEntity) {
      return this == f_56547_ ? true : this.matches(pEntity.getActiveEffectsMap());
   }

   public boolean matches(Map<MobEffect, MobEffectInstance> pEffects) {
      if (this == f_56547_) {
         return true;
      } else {
         for(Map.Entry<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> entry : this.f_56548_.entrySet()) {
            MobEffectInstance mobeffectinstance = pEffects.get(entry.getKey());
            if (!entry.getValue().matches(mobeffectinstance)) {
               return false;
            }
         }

         return true;
      }
   }

   public static MobEffectsPredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "effects");
         Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> map = Maps.newLinkedHashMap();

         for(Map.Entry<String, JsonElement> entry : jsonobject.entrySet()) {
            ResourceLocation resourcelocation = new ResourceLocation(entry.getKey());
            MobEffect mobeffect = BuiltInRegistries.MOB_EFFECT.getOptional(resourcelocation).orElseThrow(() -> {
               return new JsonSyntaxException("Unknown effect '" + resourcelocation + "'");
            });
            MobEffectsPredicate.MobEffectInstancePredicate mobeffectspredicate$mobeffectinstancepredicate = MobEffectsPredicate.MobEffectInstancePredicate.m_56579_(GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey()));
            map.put(mobeffect, mobeffectspredicate$mobeffectinstancepredicate);
         }

         return new MobEffectsPredicate(map);
      } else {
         return f_56547_;
      }
   }

   public JsonElement serializeToJson() {
      if (this == f_56547_) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();

         for(Map.Entry<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> entry : this.f_56548_.entrySet()) {
            jsonobject.add(BuiltInRegistries.MOB_EFFECT.getKey(entry.getKey()).toString(), entry.getValue().m_56576_());
         }

         return jsonobject;
      }
   }

   public static class MobEffectInstancePredicate {
      private final MinMaxBounds.Ints amplifier;
      private final MinMaxBounds.Ints duration;
      @Nullable
      private final Boolean ambient;
      @Nullable
      private final Boolean visible;

      public MobEffectInstancePredicate(MinMaxBounds.Ints p_56572_, MinMaxBounds.Ints p_56573_, @Nullable Boolean p_56574_, @Nullable Boolean p_56575_) {
         this.amplifier = p_56572_;
         this.duration = p_56573_;
         this.ambient = p_56574_;
         this.visible = p_56575_;
      }

      public MobEffectInstancePredicate() {
         this(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, (Boolean)null, (Boolean)null);
      }

      public boolean matches(@Nullable MobEffectInstance pEffect) {
         if (pEffect == null) {
            return false;
         } else if (!this.amplifier.matches(pEffect.getAmplifier())) {
            return false;
         } else if (!this.duration.matches(pEffect.getDuration())) {
            return false;
         } else if (this.ambient != null && this.ambient != pEffect.isAmbient()) {
            return false;
         } else {
            return this.visible == null || this.visible == pEffect.isVisible();
         }
      }

      public JsonElement m_56576_() {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("amplifier", this.amplifier.m_55328_());
         jsonobject.add("duration", this.duration.m_55328_());
         jsonobject.addProperty("ambient", this.ambient);
         jsonobject.addProperty("visible", this.visible);
         return jsonobject;
      }

      public static MobEffectsPredicate.MobEffectInstancePredicate m_56579_(JsonObject p_56580_) {
         MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(p_56580_.get("amplifier"));
         MinMaxBounds.Ints minmaxbounds$ints1 = MinMaxBounds.Ints.fromJson(p_56580_.get("duration"));
         Boolean obool = p_56580_.has("ambient") ? GsonHelper.getAsBoolean(p_56580_, "ambient") : null;
         Boolean obool1 = p_56580_.has("visible") ? GsonHelper.getAsBoolean(p_56580_, "visible") : null;
         return new MobEffectsPredicate.MobEffectInstancePredicate(minmaxbounds$ints, minmaxbounds$ints1, obool, obool1);
      }
   }
}