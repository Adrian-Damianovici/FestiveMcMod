package net.minecraft.advancements.critereon;

import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;

public abstract class EntityTypePredicate {
   public static final EntityTypePredicate f_37636_ = new EntityTypePredicate() {
      public boolean matches(EntityType<?> p_37652_) {
         return true;
      }

      public JsonElement m_5908_() {
         return JsonNull.INSTANCE;
      }
   };
   private static final Joiner f_37637_ = Joiner.on(", ");

   public abstract boolean matches(EntityType<?> pType);

   public abstract JsonElement m_5908_();

   public static EntityTypePredicate m_37643_(@Nullable JsonElement p_37644_) {
      if (p_37644_ != null && !p_37644_.isJsonNull()) {
         String s = GsonHelper.convertToString(p_37644_, "type");
         if (s.startsWith("#")) {
            ResourceLocation resourcelocation1 = new ResourceLocation(s.substring(1));
            return new EntityTypePredicate.TagPredicate(TagKey.create(Registries.ENTITY_TYPE, resourcelocation1));
         } else {
            ResourceLocation resourcelocation = new ResourceLocation(s);
            EntityType<?> entitytype = BuiltInRegistries.ENTITY_TYPE.getOptional(resourcelocation).orElseThrow(() -> {
               return new JsonSyntaxException("Unknown entity type '" + resourcelocation + "', valid types are: " + f_37637_.join(BuiltInRegistries.ENTITY_TYPE.keySet()));
            });
            return new EntityTypePredicate.TypePredicate(entitytype);
         }
      } else {
         return f_37636_;
      }
   }

   public static EntityTypePredicate of(EntityType<?> pType) {
      return new EntityTypePredicate.TypePredicate(pType);
   }

   public static EntityTypePredicate of(TagKey<EntityType<?>> pTag) {
      return new EntityTypePredicate.TagPredicate(pTag);
   }

   static class TagPredicate extends EntityTypePredicate {
      private final TagKey<EntityType<?>> f_37653_;

      public TagPredicate(TagKey<EntityType<?>> p_204084_) {
         this.f_37653_ = p_204084_;
      }

      public boolean matches(EntityType<?> p_37658_) {
         return p_37658_.is(this.f_37653_);
      }

      public JsonElement m_5908_() {
         return new JsonPrimitive("#" + this.f_37653_.location());
      }
   }

   static class TypePredicate extends EntityTypePredicate {
      private final EntityType<?> f_37659_;

      public TypePredicate(EntityType<?> p_37661_) {
         this.f_37659_ = p_37661_;
      }

      public boolean matches(EntityType<?> p_37664_) {
         return this.f_37659_ == p_37664_;
      }

      public JsonElement m_5908_() {
         return new JsonPrimitive(BuiltInRegistries.ENTITY_TYPE.getKey(this.f_37659_).toString());
      }
   }
}