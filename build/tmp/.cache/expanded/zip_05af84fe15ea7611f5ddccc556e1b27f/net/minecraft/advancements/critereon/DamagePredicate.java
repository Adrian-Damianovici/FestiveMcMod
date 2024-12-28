package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;

public class DamagePredicate {
   public static final DamagePredicate f_24902_ = DamagePredicate.Builder.damageInstance().build();
   private final MinMaxBounds.Doubles dealtDamage;
   private final MinMaxBounds.Doubles takenDamage;
   private final EntityPredicate sourceEntity;
   @Nullable
   private final Boolean blocked;
   private final DamageSourcePredicate type;

   public DamagePredicate() {
      this.dealtDamage = MinMaxBounds.Doubles.ANY;
      this.takenDamage = MinMaxBounds.Doubles.ANY;
      this.sourceEntity = EntityPredicate.f_36550_;
      this.blocked = null;
      this.type = DamageSourcePredicate.f_25420_;
   }

   public DamagePredicate(MinMaxBounds.Doubles p_24911_, MinMaxBounds.Doubles p_24912_, EntityPredicate p_24913_, @Nullable Boolean p_24914_, DamageSourcePredicate p_24915_) {
      this.dealtDamage = p_24911_;
      this.takenDamage = p_24912_;
      this.sourceEntity = p_24913_;
      this.blocked = p_24914_;
      this.type = p_24915_;
   }

   public boolean matches(ServerPlayer pPlayer, DamageSource pSource, float pDealtDamage, float pTakenDamage, boolean pBlocked) {
      if (this == f_24902_) {
         return true;
      } else if (!this.dealtDamage.matches((double)pDealtDamage)) {
         return false;
      } else if (!this.takenDamage.matches((double)pTakenDamage)) {
         return false;
      } else if (!this.sourceEntity.matches(pPlayer, pSource.getEntity())) {
         return false;
      } else if (this.blocked != null && this.blocked != pBlocked) {
         return false;
      } else {
         return this.type.matches(pPlayer, pSource);
      }
   }

   public static DamagePredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "damage");
         MinMaxBounds.Doubles minmaxbounds$doubles = MinMaxBounds.Doubles.fromJson(jsonobject.get("dealt"));
         MinMaxBounds.Doubles minmaxbounds$doubles1 = MinMaxBounds.Doubles.fromJson(jsonobject.get("taken"));
         Boolean obool = jsonobject.has("blocked") ? GsonHelper.getAsBoolean(jsonobject, "blocked") : null;
         EntityPredicate entitypredicate = EntityPredicate.fromJson(jsonobject.get("source_entity"));
         DamageSourcePredicate damagesourcepredicate = DamageSourcePredicate.fromJson(jsonobject.get("type"));
         return new DamagePredicate(minmaxbounds$doubles, minmaxbounds$doubles1, entitypredicate, obool, damagesourcepredicate);
      } else {
         return f_24902_;
      }
   }

   public JsonElement serializeToJson() {
      if (this == f_24902_) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("dealt", this.dealtDamage.m_55328_());
         jsonobject.add("taken", this.takenDamage.m_55328_());
         jsonobject.add("source_entity", this.sourceEntity.serializeToJson());
         jsonobject.add("type", this.type.serializeToJson());
         if (this.blocked != null) {
            jsonobject.addProperty("blocked", this.blocked);
         }

         return jsonobject;
      }
   }

   public static class Builder {
      private MinMaxBounds.Doubles dealtDamage = MinMaxBounds.Doubles.ANY;
      private MinMaxBounds.Doubles takenDamage = MinMaxBounds.Doubles.ANY;
      private EntityPredicate sourceEntity = EntityPredicate.f_36550_;
      @Nullable
      private Boolean blocked;
      private DamageSourcePredicate type = DamageSourcePredicate.f_25420_;

      public static DamagePredicate.Builder damageInstance() {
         return new DamagePredicate.Builder();
      }

      public DamagePredicate.Builder dealtDamage(MinMaxBounds.Doubles pDealtDamage) {
         this.dealtDamage = pDealtDamage;
         return this;
      }

      public DamagePredicate.Builder takenDamage(MinMaxBounds.Doubles pTakenDamage) {
         this.takenDamage = pTakenDamage;
         return this;
      }

      public DamagePredicate.Builder sourceEntity(EntityPredicate pSourceEntity) {
         this.sourceEntity = pSourceEntity;
         return this;
      }

      public DamagePredicate.Builder blocked(Boolean pBlocked) {
         this.blocked = pBlocked;
         return this;
      }

      public DamagePredicate.Builder type(DamageSourcePredicate pType) {
         this.type = pType;
         return this;
      }

      public DamagePredicate.Builder type(DamageSourcePredicate.Builder pTypeBuilder) {
         this.type = pTypeBuilder.build();
         return this;
      }

      public DamagePredicate build() {
         return new DamagePredicate(this.dealtDamage, this.takenDamage, this.sourceEntity, this.blocked, this.type);
      }
   }
}