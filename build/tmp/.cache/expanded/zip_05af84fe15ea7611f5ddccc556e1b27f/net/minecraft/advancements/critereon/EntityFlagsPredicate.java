package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EntityFlagsPredicate {
   public static final EntityFlagsPredicate f_33682_ = (new EntityFlagsPredicate.Builder()).build();
   @Nullable
   private final Boolean isOnFire;
   @Nullable
   private final Boolean isCrouching;
   @Nullable
   private final Boolean isSprinting;
   @Nullable
   private final Boolean isSwimming;
   @Nullable
   private final Boolean isBaby;

   public EntityFlagsPredicate(@Nullable Boolean p_33690_, @Nullable Boolean p_33691_, @Nullable Boolean p_33692_, @Nullable Boolean p_33693_, @Nullable Boolean p_33694_) {
      this.isOnFire = p_33690_;
      this.isCrouching = p_33691_;
      this.isSprinting = p_33692_;
      this.isSwimming = p_33693_;
      this.isBaby = p_33694_;
   }

   public boolean matches(Entity pEntity) {
      if (this.isOnFire != null && pEntity.isOnFire() != this.isOnFire) {
         return false;
      } else if (this.isCrouching != null && pEntity.isCrouching() != this.isCrouching) {
         return false;
      } else if (this.isSprinting != null && pEntity.isSprinting() != this.isSprinting) {
         return false;
      } else if (this.isSwimming != null && pEntity.isSwimming() != this.isSwimming) {
         return false;
      } else {
         return this.isBaby == null || !(pEntity instanceof LivingEntity) || ((LivingEntity)pEntity).isBaby() == this.isBaby;
      }
   }

   @Nullable
   private static Boolean m_33700_(JsonObject p_33701_, String p_33702_) {
      return p_33701_.has(p_33702_) ? GsonHelper.getAsBoolean(p_33701_, p_33702_) : null;
   }

   public static EntityFlagsPredicate m_33698_(@Nullable JsonElement p_33699_) {
      if (p_33699_ != null && !p_33699_.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(p_33699_, "entity flags");
         Boolean obool = m_33700_(jsonobject, "is_on_fire");
         Boolean obool1 = m_33700_(jsonobject, "is_sneaking");
         Boolean obool2 = m_33700_(jsonobject, "is_sprinting");
         Boolean obool3 = m_33700_(jsonobject, "is_swimming");
         Boolean obool4 = m_33700_(jsonobject, "is_baby");
         return new EntityFlagsPredicate(obool, obool1, obool2, obool3, obool4);
      } else {
         return f_33682_;
      }
   }

   private void m_33703_(JsonObject p_33704_, String p_33705_, @Nullable Boolean p_33706_) {
      if (p_33706_ != null) {
         p_33704_.addProperty(p_33705_, p_33706_);
      }

   }

   public JsonElement m_33695_() {
      if (this == f_33682_) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         this.m_33703_(jsonobject, "is_on_fire", this.isOnFire);
         this.m_33703_(jsonobject, "is_sneaking", this.isCrouching);
         this.m_33703_(jsonobject, "is_sprinting", this.isSprinting);
         this.m_33703_(jsonobject, "is_swimming", this.isSwimming);
         this.m_33703_(jsonobject, "is_baby", this.isBaby);
         return jsonobject;
      }
   }

   public static class Builder {
      @Nullable
      private Boolean isOnFire;
      @Nullable
      private Boolean isCrouching;
      @Nullable
      private Boolean isSprinting;
      @Nullable
      private Boolean isSwimming;
      @Nullable
      private Boolean isBaby;

      public static EntityFlagsPredicate.Builder flags() {
         return new EntityFlagsPredicate.Builder();
      }

      public EntityFlagsPredicate.Builder setOnFire(@Nullable Boolean pOnFire) {
         this.isOnFire = pOnFire;
         return this;
      }

      public EntityFlagsPredicate.Builder setCrouching(@Nullable Boolean pIsCrouching) {
         this.isCrouching = pIsCrouching;
         return this;
      }

      public EntityFlagsPredicate.Builder setSprinting(@Nullable Boolean pIsSprinting) {
         this.isSprinting = pIsSprinting;
         return this;
      }

      public EntityFlagsPredicate.Builder setSwimming(@Nullable Boolean pIsSwimming) {
         this.isSwimming = pIsSwimming;
         return this;
      }

      public EntityFlagsPredicate.Builder setIsBaby(@Nullable Boolean pIsBaby) {
         this.isBaby = pIsBaby;
         return this;
      }

      public EntityFlagsPredicate build() {
         return new EntityFlagsPredicate(this.isOnFire, this.isCrouching, this.isSprinting, this.isSwimming, this.isBaby);
      }
   }
}