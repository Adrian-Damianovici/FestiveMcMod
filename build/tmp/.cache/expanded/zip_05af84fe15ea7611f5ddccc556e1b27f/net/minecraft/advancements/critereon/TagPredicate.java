package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;

public class TagPredicate<T> {
   private final TagKey<T> tag;
   private final boolean expected;

   public TagPredicate(TagKey<T> p_270819_, boolean p_270913_) {
      this.tag = p_270819_;
      this.expected = p_270913_;
   }

   public static <T> TagPredicate<T> is(TagKey<T> pTag) {
      return new TagPredicate<>(pTag, true);
   }

   public static <T> TagPredicate<T> isNot(TagKey<T> pTag) {
      return new TagPredicate<>(pTag, false);
   }

   public boolean matches(Holder<T> pValue) {
      return pValue.is(this.tag) == this.expected;
   }

   public JsonElement m_269579_() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("id", this.tag.location().toString());
      jsonobject.addProperty("expected", this.expected);
      return jsonobject;
   }

   public static <T> TagPredicate<T> m_269409_(@Nullable JsonElement p_270982_, ResourceKey<? extends Registry<T>> p_270978_) {
      if (p_270982_ == null) {
         throw new JsonParseException("Expected a tag predicate");
      } else {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(p_270982_, "Tag Predicate");
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "id"));
         boolean flag = GsonHelper.getAsBoolean(jsonobject, "expected");
         return new TagPredicate<>(TagKey.create(p_270978_, resourcelocation), flag);
      }
   }
}