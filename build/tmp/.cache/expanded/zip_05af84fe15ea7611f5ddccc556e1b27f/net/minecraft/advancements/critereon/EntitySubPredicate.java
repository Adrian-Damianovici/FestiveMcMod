package net.minecraft.advancements.critereon;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;

public interface EntitySubPredicate {
   EntitySubPredicate f_218826_ = new EntitySubPredicate() {
      public boolean m_153246_(Entity p_218841_, ServerLevel p_218842_, @Nullable Vec3 p_218843_) {
         return true;
      }

      public JsonObject m_213616_() {
         return new JsonObject();
      }

      public EntitySubPredicate.Type type() {
         return EntitySubPredicate.Types.ANY;
      }
   };

   static EntitySubPredicate m_218835_(@Nullable JsonElement p_218836_) {
      if (p_218836_ != null && !p_218836_.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(p_218836_, "type_specific");
         String s = GsonHelper.getAsString(jsonobject, "type", (String)null);
         if (s == null) {
            return f_218826_;
         } else {
            EntitySubPredicate.Type entitysubpredicate$type = EntitySubPredicate.Types.TYPES.get(s);
            if (entitysubpredicate$type == null) {
               throw new JsonSyntaxException("Unknown sub-predicate type: " + s);
            } else {
               return entitysubpredicate$type.m_218845_(jsonobject);
            }
         }
      } else {
         return f_218826_;
      }
   }

   boolean m_153246_(Entity pEntity, ServerLevel pLevel, @Nullable Vec3 pPosition);

   JsonObject m_213616_();

   default JsonElement m_218837_() {
      if (this.type() == EntitySubPredicate.Types.ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = this.m_213616_();
         String s = EntitySubPredicate.Types.TYPES.inverse().get(this.type());
         jsonobject.addProperty("type", s);
         return jsonobject;
      }
   }

   EntitySubPredicate.Type type();

   static EntitySubPredicate variant(CatVariant pVariant) {
      return EntitySubPredicate.Types.CAT.createPredicate(pVariant);
   }

   static EntitySubPredicate variant(FrogVariant pVariant) {
      return EntitySubPredicate.Types.FROG.createPredicate(pVariant);
   }

   public interface Type {
      EntitySubPredicate m_218845_(JsonObject p_218846_);
   }

   public static final class Types {
      public static final EntitySubPredicate.Type ANY = (p_218860_) -> {
         return EntitySubPredicate.f_218826_;
      };
      public static final EntitySubPredicate.Type LIGHTNING = LighthingBoltPredicate::m_220332_;
      public static final EntitySubPredicate.Type FISHING_HOOK = FishingHookPredicate::m_219719_;
      public static final EntitySubPredicate.Type PLAYER = PlayerPredicate::m_222491_;
      public static final EntitySubPredicate.Type SLIME = SlimePredicate::m_223428_;
      public static final EntityVariantPredicate<CatVariant> CAT = EntityVariantPredicate.create(BuiltInRegistries.CAT_VARIANT, (p_218862_) -> {
         Optional optional;
         if (p_218862_ instanceof Cat cat) {
            optional = Optional.of(cat.getVariant());
         } else {
            optional = Optional.empty();
         }

         return optional;
      });
      public static final EntityVariantPredicate<FrogVariant> FROG = EntityVariantPredicate.create(BuiltInRegistries.FROG_VARIANT, (p_218858_) -> {
         Optional optional;
         if (p_218858_ instanceof Frog frog) {
            optional = Optional.of(frog.getVariant());
         } else {
            optional = Optional.empty();
         }

         return optional;
      });
      public static final EntityVariantPredicate<Axolotl.Variant> AXOLOTL = EntityVariantPredicate.create(Axolotl.Variant.CODEC, (p_262508_) -> {
         Optional optional;
         if (p_262508_ instanceof Axolotl axolotl) {
            optional = Optional.of(axolotl.getVariant());
         } else {
            optional = Optional.empty();
         }

         return optional;
      });
      public static final EntityVariantPredicate<Boat.Type> BOAT = EntityVariantPredicate.create(Boat.Type.CODEC, (p_262507_) -> {
         Optional optional;
         if (p_262507_ instanceof Boat boat) {
            optional = Optional.of(boat.getVariant());
         } else {
            optional = Optional.empty();
         }

         return optional;
      });
      public static final EntityVariantPredicate<Fox.Type> FOX = EntityVariantPredicate.create(Fox.Type.CODEC, (p_262510_) -> {
         Optional optional;
         if (p_262510_ instanceof Fox fox) {
            optional = Optional.of(fox.getVariant());
         } else {
            optional = Optional.empty();
         }

         return optional;
      });
      public static final EntityVariantPredicate<MushroomCow.MushroomType> MOOSHROOM = EntityVariantPredicate.create(MushroomCow.MushroomType.CODEC, (p_262513_) -> {
         Optional optional;
         if (p_262513_ instanceof MushroomCow mushroomcow) {
            optional = Optional.of(mushroomcow.getVariant());
         } else {
            optional = Optional.empty();
         }

         return optional;
      });
      public static final EntityVariantPredicate<Holder<PaintingVariant>> PAINTING = EntityVariantPredicate.create(BuiltInRegistries.PAINTING_VARIANT.holderByNameCodec(), (p_262509_) -> {
         Optional optional;
         if (p_262509_ instanceof Painting painting) {
            optional = Optional.of(painting.getVariant());
         } else {
            optional = Optional.empty();
         }

         return optional;
      });
      public static final EntityVariantPredicate<Rabbit.Variant> RABBIT = EntityVariantPredicate.create(Rabbit.Variant.CODEC, (p_262511_) -> {
         Optional optional;
         if (p_262511_ instanceof Rabbit rabbit) {
            optional = Optional.of(rabbit.getVariant());
         } else {
            optional = Optional.empty();
         }

         return optional;
      });
      public static final EntityVariantPredicate<Variant> HORSE = EntityVariantPredicate.create(Variant.CODEC, (p_262516_) -> {
         Optional optional;
         if (p_262516_ instanceof Horse horse) {
            optional = Optional.of(horse.getVariant());
         } else {
            optional = Optional.empty();
         }

         return optional;
      });
      public static final EntityVariantPredicate<Llama.Variant> LLAMA = EntityVariantPredicate.create(Llama.Variant.CODEC, (p_262515_) -> {
         Optional optional;
         if (p_262515_ instanceof Llama llama) {
            optional = Optional.of(llama.getVariant());
         } else {
            optional = Optional.empty();
         }

         return optional;
      });
      public static final EntityVariantPredicate<VillagerType> VILLAGER = EntityVariantPredicate.create(BuiltInRegistries.VILLAGER_TYPE.byNameCodec(), (p_262512_) -> {
         Optional optional;
         if (p_262512_ instanceof VillagerDataHolder villagerdataholder) {
            optional = Optional.of(villagerdataholder.getVariant());
         } else {
            optional = Optional.empty();
         }

         return optional;
      });
      public static final EntityVariantPredicate<Parrot.Variant> PARROT = EntityVariantPredicate.create(Parrot.Variant.CODEC, (p_262506_) -> {
         Optional optional;
         if (p_262506_ instanceof Parrot parrot) {
            optional = Optional.of(parrot.getVariant());
         } else {
            optional = Optional.empty();
         }

         return optional;
      });
      public static final EntityVariantPredicate<TropicalFish.Pattern> TROPICAL_FISH = EntityVariantPredicate.create(TropicalFish.Pattern.CODEC, (p_262517_) -> {
         Optional optional;
         if (p_262517_ instanceof TropicalFish tropicalfish) {
            optional = Optional.of(tropicalfish.getVariant());
         } else {
            optional = Optional.empty();
         }

         return optional;
      });
      public static final BiMap<String, EntitySubPredicate.Type> TYPES = ImmutableBiMap.<String, EntitySubPredicate.Type>builder().put("any", ANY).put("lightning", LIGHTNING).put("fishing_hook", FISHING_HOOK).put("player", PLAYER).put("slime", SLIME).put("cat", CAT.type()).put("frog", FROG.type()).put("axolotl", AXOLOTL.type()).put("boat", BOAT.type()).put("fox", FOX.type()).put("mooshroom", MOOSHROOM.type()).put("painting", PAINTING.type()).put("rabbit", RABBIT.type()).put("horse", HORSE.type()).put("llama", LLAMA.type()).put("villager", VILLAGER.type()).put("parrot", PARROT.type()).put("tropical_fish", TROPICAL_FISH.type()).buildOrThrow();
   }
}