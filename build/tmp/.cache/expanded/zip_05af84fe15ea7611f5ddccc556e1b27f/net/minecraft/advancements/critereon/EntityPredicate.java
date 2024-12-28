package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public class EntityPredicate {
   public static final EntityPredicate f_36550_ = new EntityPredicate(EntityTypePredicate.f_37636_, DistancePredicate.f_26241_, LocationPredicate.f_52592_, LocationPredicate.f_52592_, MobEffectsPredicate.f_56547_, NbtPredicate.f_57471_, EntityFlagsPredicate.f_33682_, EntityEquipmentPredicate.f_32176_, EntitySubPredicate.f_218826_, (String)null);
   private final EntityTypePredicate entityType;
   private final DistancePredicate distanceToPlayer;
   private final LocationPredicate location;
   private final LocationPredicate steppingOnLocation;
   private final MobEffectsPredicate effects;
   private final NbtPredicate nbt;
   private final EntityFlagsPredicate flags;
   private final EntityEquipmentPredicate equipment;
   private final EntitySubPredicate subPredicate;
   private final EntityPredicate vehicle;
   private final EntityPredicate passenger;
   private final EntityPredicate targetedEntity;
   @Nullable
   private final String team;

   private EntityPredicate(EntityTypePredicate p_218789_, DistancePredicate p_218790_, LocationPredicate p_218791_, LocationPredicate p_218792_, MobEffectsPredicate p_218793_, NbtPredicate p_218794_, EntityFlagsPredicate p_218795_, EntityEquipmentPredicate p_218796_, EntitySubPredicate p_218797_, @Nullable String p_218798_) {
      this.entityType = p_218789_;
      this.distanceToPlayer = p_218790_;
      this.location = p_218791_;
      this.steppingOnLocation = p_218792_;
      this.effects = p_218793_;
      this.nbt = p_218794_;
      this.flags = p_218795_;
      this.equipment = p_218796_;
      this.subPredicate = p_218797_;
      this.passenger = this;
      this.vehicle = this;
      this.targetedEntity = this;
      this.team = p_218798_;
   }

   EntityPredicate(EntityTypePredicate p_218775_, DistancePredicate p_218776_, LocationPredicate p_218777_, LocationPredicate p_218778_, MobEffectsPredicate p_218779_, NbtPredicate p_218780_, EntityFlagsPredicate p_218781_, EntityEquipmentPredicate p_218782_, EntitySubPredicate p_218783_, EntityPredicate p_218784_, EntityPredicate p_218785_, EntityPredicate p_218786_, @Nullable String p_218787_) {
      this.entityType = p_218775_;
      this.distanceToPlayer = p_218776_;
      this.location = p_218777_;
      this.steppingOnLocation = p_218778_;
      this.effects = p_218779_;
      this.nbt = p_218780_;
      this.flags = p_218781_;
      this.equipment = p_218782_;
      this.subPredicate = p_218783_;
      this.vehicle = p_218784_;
      this.passenger = p_218785_;
      this.targetedEntity = p_218786_;
      this.team = p_218787_;
   }

   public static ContextAwarePredicate fromJson(JsonObject p_286877_, String p_286245_, DeserializationContext p_286427_) {
      JsonElement jsonelement = p_286877_.get(p_286245_);
      return fromElement(p_286245_, p_286427_, jsonelement);
   }

   public static ContextAwarePredicate[] fromJsonArray(JsonObject pJson, String pKey, DeserializationContext pContext) {
      JsonElement jsonelement = pJson.get(pKey);
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         JsonArray jsonarray = GsonHelper.convertToJsonArray(jsonelement, pKey);
         ContextAwarePredicate[] acontextawarepredicate = new ContextAwarePredicate[jsonarray.size()];

         for(int i = 0; i < jsonarray.size(); ++i) {
            acontextawarepredicate[i] = fromElement(pKey + "[" + i + "]", pContext, jsonarray.get(i));
         }

         return acontextawarepredicate;
      } else {
         return new ContextAwarePredicate[0];
      }
   }

   private static ContextAwarePredicate fromElement(String pKey, DeserializationContext pContext, @Nullable JsonElement pElement) {
      ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.fromElement(pKey, pContext, pElement, LootContextParamSets.ADVANCEMENT_ENTITY);
      if (contextawarepredicate != null) {
         return contextawarepredicate;
      } else {
         EntityPredicate entitypredicate = fromJson(pElement);
         return wrap(entitypredicate);
      }
   }

   public static ContextAwarePredicate wrap(EntityPredicate p_286570_) {
      if (p_286570_ == f_36550_) {
         return ContextAwarePredicate.f_285567_;
      } else {
         LootItemCondition lootitemcondition = LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, p_286570_).build();
         return new ContextAwarePredicate(new LootItemCondition[]{lootitemcondition});
      }
   }

   public boolean matches(ServerPlayer pPlayer, @Nullable Entity pEntity) {
      return this.matches(pPlayer.serverLevel(), pPlayer.position(), pEntity);
   }

   public boolean matches(ServerLevel pLevel, @Nullable Vec3 pPosition, @Nullable Entity pEntity) {
      if (this == f_36550_) {
         return true;
      } else if (pEntity == null) {
         return false;
      } else if (!this.entityType.matches(pEntity.getType())) {
         return false;
      } else {
         if (pPosition == null) {
            if (this.distanceToPlayer != DistancePredicate.f_26241_) {
               return false;
            }
         } else if (!this.distanceToPlayer.matches(pPosition.x, pPosition.y, pPosition.z, pEntity.getX(), pEntity.getY(), pEntity.getZ())) {
            return false;
         }

         if (!this.location.matches(pLevel, pEntity.getX(), pEntity.getY(), pEntity.getZ())) {
            return false;
         } else {
            if (this.steppingOnLocation != LocationPredicate.f_52592_) {
               Vec3 vec3 = Vec3.atCenterOf(pEntity.getOnPos());
               if (!this.steppingOnLocation.matches(pLevel, vec3.x(), vec3.y(), vec3.z())) {
                  return false;
               }
            }

            if (!this.effects.matches(pEntity)) {
               return false;
            } else if (!this.nbt.matches(pEntity)) {
               return false;
            } else if (!this.flags.matches(pEntity)) {
               return false;
            } else if (!this.equipment.matches(pEntity)) {
               return false;
            } else if (!this.subPredicate.m_153246_(pEntity, pLevel, pPosition)) {
               return false;
            } else if (!this.vehicle.matches(pLevel, pPosition, pEntity.getVehicle())) {
               return false;
            } else if (this.passenger != f_36550_ && pEntity.getPassengers().stream().noneMatch((p_150322_) -> {
               return this.passenger.matches(pLevel, pPosition, p_150322_);
            })) {
               return false;
            } else if (!this.targetedEntity.matches(pLevel, pPosition, pEntity instanceof Mob ? ((Mob)pEntity).getTarget() : null)) {
               return false;
            } else {
               if (this.team != null) {
                  Team team = pEntity.getTeam();
                  if (team == null || !this.team.equals(team.getName())) {
                     return false;
                  }
               }

               return true;
            }
         }
      }
   }

   public static EntityPredicate fromJson(@Nullable JsonElement p_36615_) {
      if (p_36615_ != null && !p_36615_.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(p_36615_, "entity");
         EntityTypePredicate entitytypepredicate = EntityTypePredicate.m_37643_(jsonobject.get("type"));
         DistancePredicate distancepredicate = DistancePredicate.fromJson(jsonobject.get("distance"));
         LocationPredicate locationpredicate = LocationPredicate.fromJson(jsonobject.get("location"));
         LocationPredicate locationpredicate1 = LocationPredicate.fromJson(jsonobject.get("stepping_on"));
         MobEffectsPredicate mobeffectspredicate = MobEffectsPredicate.fromJson(jsonobject.get("effects"));
         NbtPredicate nbtpredicate = NbtPredicate.m_57481_(jsonobject.get("nbt"));
         EntityFlagsPredicate entityflagspredicate = EntityFlagsPredicate.m_33698_(jsonobject.get("flags"));
         EntityEquipmentPredicate entityequipmentpredicate = EntityEquipmentPredicate.m_32195_(jsonobject.get("equipment"));
         EntitySubPredicate entitysubpredicate = EntitySubPredicate.m_218835_(jsonobject.get("type_specific"));
         EntityPredicate entitypredicate = fromJson(jsonobject.get("vehicle"));
         EntityPredicate entitypredicate1 = fromJson(jsonobject.get("passenger"));
         EntityPredicate entitypredicate2 = fromJson(jsonobject.get("targeted_entity"));
         String s = GsonHelper.getAsString(jsonobject, "team", (String)null);
         return (new EntityPredicate.Builder()).entityType(entitytypepredicate).distance(distancepredicate).located(locationpredicate).steppingOn(locationpredicate1).effects(mobeffectspredicate).nbt(nbtpredicate).flags(entityflagspredicate).equipment(entityequipmentpredicate).subPredicate(entitysubpredicate).team(s).vehicle(entitypredicate).passenger(entitypredicate1).targetedEntity(entitypredicate2).build();
      } else {
         return f_36550_;
      }
   }

   public JsonElement serializeToJson() {
      if (this == f_36550_) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("type", this.entityType.m_5908_());
         jsonobject.add("distance", this.distanceToPlayer.serializeToJson());
         jsonobject.add("location", this.location.serializeToJson());
         jsonobject.add("stepping_on", this.steppingOnLocation.serializeToJson());
         jsonobject.add("effects", this.effects.serializeToJson());
         jsonobject.add("nbt", this.nbt.m_57476_());
         jsonobject.add("flags", this.flags.m_33695_());
         jsonobject.add("equipment", this.equipment.m_32192_());
         jsonobject.add("type_specific", this.subPredicate.m_218837_());
         jsonobject.add("vehicle", this.vehicle.serializeToJson());
         jsonobject.add("passenger", this.passenger.serializeToJson());
         jsonobject.add("targeted_entity", this.targetedEntity.serializeToJson());
         jsonobject.addProperty("team", this.team);
         return jsonobject;
      }
   }

   public static LootContext createContext(ServerPlayer pPlayer, Entity pEntity) {
      LootParams lootparams = (new LootParams.Builder(pPlayer.serverLevel())).withParameter(LootContextParams.THIS_ENTITY, pEntity).withParameter(LootContextParams.ORIGIN, pPlayer.position()).create(LootContextParamSets.ADVANCEMENT_ENTITY);
      return (new LootContext.Builder(lootparams)).create((ResourceLocation)null);
   }

   public static class Builder {
      private EntityTypePredicate entityType = EntityTypePredicate.f_37636_;
      private DistancePredicate distanceToPlayer = DistancePredicate.f_26241_;
      private LocationPredicate location = LocationPredicate.f_52592_;
      private LocationPredicate steppingOnLocation = LocationPredicate.f_52592_;
      private MobEffectsPredicate effects = MobEffectsPredicate.f_56547_;
      private NbtPredicate nbt = NbtPredicate.f_57471_;
      private EntityFlagsPredicate flags = EntityFlagsPredicate.f_33682_;
      private EntityEquipmentPredicate equipment = EntityEquipmentPredicate.f_32176_;
      private EntitySubPredicate subPredicate = EntitySubPredicate.f_218826_;
      private EntityPredicate vehicle = EntityPredicate.f_36550_;
      private EntityPredicate passenger = EntityPredicate.f_36550_;
      private EntityPredicate targetedEntity = EntityPredicate.f_36550_;
      @Nullable
      private String team;

      public static EntityPredicate.Builder entity() {
         return new EntityPredicate.Builder();
      }

      public EntityPredicate.Builder of(EntityType<?> pEntityType) {
         this.entityType = EntityTypePredicate.of(pEntityType);
         return this;
      }

      public EntityPredicate.Builder of(TagKey<EntityType<?>> pEntityTypeTag) {
         this.entityType = EntityTypePredicate.of(pEntityTypeTag);
         return this;
      }

      public EntityPredicate.Builder entityType(EntityTypePredicate pEntityType) {
         this.entityType = pEntityType;
         return this;
      }

      public EntityPredicate.Builder distance(DistancePredicate pDistanceToPlayer) {
         this.distanceToPlayer = pDistanceToPlayer;
         return this;
      }

      public EntityPredicate.Builder located(LocationPredicate p_36651_) {
         this.location = p_36651_;
         return this;
      }

      public EntityPredicate.Builder steppingOn(LocationPredicate p_150331_) {
         this.steppingOnLocation = p_150331_;
         return this;
      }

      public EntityPredicate.Builder effects(MobEffectsPredicate p_36653_) {
         this.effects = p_36653_;
         return this;
      }

      public EntityPredicate.Builder nbt(NbtPredicate pNbt) {
         this.nbt = pNbt;
         return this;
      }

      public EntityPredicate.Builder flags(EntityFlagsPredicate p_36643_) {
         this.flags = p_36643_;
         return this;
      }

      public EntityPredicate.Builder equipment(EntityEquipmentPredicate pEquipment) {
         this.equipment = pEquipment;
         return this;
      }

      public EntityPredicate.Builder subPredicate(EntitySubPredicate pSubPredicate) {
         this.subPredicate = pSubPredicate;
         return this;
      }

      public EntityPredicate.Builder vehicle(EntityPredicate p_36645_) {
         this.vehicle = p_36645_;
         return this;
      }

      public EntityPredicate.Builder passenger(EntityPredicate p_150329_) {
         this.passenger = p_150329_;
         return this;
      }

      public EntityPredicate.Builder targetedEntity(EntityPredicate p_36664_) {
         this.targetedEntity = p_36664_;
         return this;
      }

      public EntityPredicate.Builder team(@Nullable String pTeam) {
         this.team = pTeam;
         return this;
      }

      public EntityPredicate build() {
         return new EntityPredicate(this.entityType, this.distanceToPlayer, this.location, this.steppingOnLocation, this.effects, this.nbt, this.flags, this.equipment, this.subPredicate, this.vehicle, this.passenger, this.targetedEntity, this.team);
      }
   }
}