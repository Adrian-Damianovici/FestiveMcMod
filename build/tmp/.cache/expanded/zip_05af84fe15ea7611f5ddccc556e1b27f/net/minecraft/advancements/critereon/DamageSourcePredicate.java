package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.phys.Vec3;

public class DamageSourcePredicate {
   public static final DamageSourcePredicate f_25420_ = DamageSourcePredicate.Builder.damageType().build();
   private final List<TagPredicate<DamageType>> tags;
   private final EntityPredicate directEntity;
   private final EntityPredicate sourceEntity;

   public DamageSourcePredicate(List<TagPredicate<DamageType>> p_270233_, EntityPredicate p_270167_, EntityPredicate p_270429_) {
      this.tags = p_270233_;
      this.directEntity = p_270167_;
      this.sourceEntity = p_270429_;
   }

   public boolean matches(ServerPlayer pPlayer, DamageSource pSource) {
      return this.matches(pPlayer.serverLevel(), pPlayer.position(), pSource);
   }

   public boolean matches(ServerLevel pLevel, Vec3 pPosition, DamageSource pSource) {
      if (this == f_25420_) {
         return true;
      } else {
         for(TagPredicate<DamageType> tagpredicate : this.tags) {
            if (!tagpredicate.matches(pSource.typeHolder())) {
               return false;
            }
         }

         if (!this.directEntity.matches(pLevel, pPosition, pSource.getDirectEntity())) {
            return false;
         } else {
            return this.sourceEntity.matches(pLevel, pPosition, pSource.getEntity());
         }
      }
   }

   public static DamageSourcePredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "damage type");
         JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "tags", (JsonArray)null);
         List<TagPredicate<DamageType>> list;
         if (jsonarray != null) {
            list = new ArrayList<>(jsonarray.size());

            for(JsonElement jsonelement : jsonarray) {
               list.add(TagPredicate.m_269409_(jsonelement, Registries.DAMAGE_TYPE));
            }
         } else {
            list = List.of();
         }

         EntityPredicate entitypredicate = EntityPredicate.fromJson(jsonobject.get("direct_entity"));
         EntityPredicate entitypredicate1 = EntityPredicate.fromJson(jsonobject.get("source_entity"));
         return new DamageSourcePredicate(list, entitypredicate, entitypredicate1);
      } else {
         return f_25420_;
      }
   }

   public JsonElement serializeToJson() {
      if (this == f_25420_) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         if (!this.tags.isEmpty()) {
            JsonArray jsonarray = new JsonArray(this.tags.size());

            for(int i = 0; i < this.tags.size(); ++i) {
               jsonarray.add(this.tags.get(i).m_269579_());
            }

            jsonobject.add("tags", jsonarray);
         }

         jsonobject.add("direct_entity", this.directEntity.serializeToJson());
         jsonobject.add("source_entity", this.sourceEntity.serializeToJson());
         return jsonobject;
      }
   }

   public static class Builder {
      private final ImmutableList.Builder<TagPredicate<DamageType>> tags = ImmutableList.builder();
      private EntityPredicate directEntity = EntityPredicate.f_36550_;
      private EntityPredicate sourceEntity = EntityPredicate.f_36550_;

      public static DamageSourcePredicate.Builder damageType() {
         return new DamageSourcePredicate.Builder();
      }

      public DamageSourcePredicate.Builder tag(TagPredicate<DamageType> pTag) {
         this.tags.add(pTag);
         return this;
      }

      public DamageSourcePredicate.Builder m_148229_(EntityPredicate p_148230_) {
         this.directEntity = p_148230_;
         return this;
      }

      public DamageSourcePredicate.Builder direct(EntityPredicate.Builder pDirectEntity) {
         this.directEntity = pDirectEntity.build();
         return this;
      }

      public DamageSourcePredicate.Builder m_148233_(EntityPredicate p_148234_) {
         this.sourceEntity = p_148234_;
         return this;
      }

      public DamageSourcePredicate.Builder source(EntityPredicate.Builder pSourceEntity) {
         this.sourceEntity = pSourceEntity.build();
         return this;
      }

      public DamageSourcePredicate build() {
         return new DamageSourcePredicate(this.tags.build(), this.directEntity, this.sourceEntity);
      }
   }
}