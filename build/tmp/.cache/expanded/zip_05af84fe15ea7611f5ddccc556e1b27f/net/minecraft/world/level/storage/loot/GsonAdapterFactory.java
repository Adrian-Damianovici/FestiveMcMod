package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import java.lang.reflect.Type;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class GsonAdapterFactory {
   public static <E, T extends SerializerType<E>> GsonAdapterFactory.Builder<E, T> m_78801_(Registry<T> p_78802_, String p_78803_, String p_78804_, Function<E, T> p_78805_) {
      return new GsonAdapterFactory.Builder<>(p_78802_, p_78803_, p_78804_, p_78805_);
   }

   public static class Builder<E, T extends SerializerType<E>> {
      private final Registry<T> f_78806_;
      private final String f_78807_;
      private final String f_78808_;
      private final Function<E, T> f_78809_;
      @Nullable
      private Pair<T, GsonAdapterFactory.InlineSerializer<? extends E>> f_164983_;
      @Nullable
      private T f_78810_;

      Builder(Registry<T> p_78812_, String p_78813_, String p_78814_, Function<E, T> p_78815_) {
         this.f_78806_ = p_78812_;
         this.f_78807_ = p_78813_;
         this.f_78808_ = p_78814_;
         this.f_78809_ = p_78815_;
      }

      public GsonAdapterFactory.Builder<E, T> m_164986_(T p_164987_, GsonAdapterFactory.InlineSerializer<? extends E> p_164988_) {
         this.f_164983_ = Pair.of(p_164987_, p_164988_);
         return this;
      }

      public GsonAdapterFactory.Builder<E, T> m_164984_(T p_164985_) {
         this.f_78810_ = p_164985_;
         return this;
      }

      public Object m_78822_() {
         return new GsonAdapterFactory.JsonAdapter<>(this.f_78806_, this.f_78807_, this.f_78808_, this.f_78809_, this.f_78810_, this.f_164983_);
      }
   }

   public interface InlineSerializer<T> {
      JsonElement m_142413_(T p_164991_, JsonSerializationContext p_164992_);

      T m_142268_(JsonElement p_164989_, JsonDeserializationContext p_164990_);
   }

   static class JsonAdapter<E, T extends SerializerType<E>> implements JsonDeserializer<E>, JsonSerializer<E> {
      private final Registry<T> f_78829_;
      private final String f_78830_;
      private final String f_78831_;
      private final Function<E, T> f_78832_;
      @Nullable
      private final T f_78833_;
      @Nullable
      private final Pair<T, GsonAdapterFactory.InlineSerializer<? extends E>> f_164993_;

      JsonAdapter(Registry<T> p_164995_, String p_164996_, String p_164997_, Function<E, T> p_164998_, @Nullable T p_164999_, @Nullable Pair<T, GsonAdapterFactory.InlineSerializer<? extends E>> p_165000_) {
         this.f_78829_ = p_164995_;
         this.f_78830_ = p_164996_;
         this.f_78831_ = p_164997_;
         this.f_78832_ = p_164998_;
         this.f_78833_ = p_164999_;
         this.f_164993_ = p_165000_;
      }

      public E deserialize(JsonElement p_78848_, Type p_78849_, JsonDeserializationContext p_78850_) throws JsonParseException {
         if (p_78848_.isJsonObject()) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(p_78848_, this.f_78830_);
            String s = GsonHelper.getAsString(jsonobject, this.f_78831_, "");
            T t;
            if (s.isEmpty()) {
               t = this.f_78833_;
            } else {
               ResourceLocation resourcelocation = new ResourceLocation(s);
               t = this.f_78829_.get(resourcelocation);
            }

            if (t == null) {
               throw new JsonSyntaxException("Unknown type '" + s + "'");
            } else {
               return t.m_79331_().m_7561_(jsonobject, p_78850_);
            }
         } else if (this.f_164993_ == null) {
            throw new UnsupportedOperationException("Object " + p_78848_ + " can't be deserialized");
         } else {
            return this.f_164993_.getSecond().m_142268_(p_78848_, p_78850_);
         }
      }

      public JsonElement serialize(E p_78852_, Type p_78853_, JsonSerializationContext p_78854_) {
         T t = this.f_78832_.apply(p_78852_);
         if (this.f_164993_ != null && this.f_164993_.getFirst() == t) {
            return ((InlineSerializer<E>)this.f_164993_.getSecond()).m_142413_(p_78852_, p_78854_);
         } else if (t == null) {
            throw new JsonSyntaxException("Unknown type: " + p_78852_);
         } else {
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty(this.f_78831_, this.f_78829_.getKey(t).toString());
            ((Serializer<E>)t.m_79331_()).m_6170_(jsonobject, p_78852_, p_78854_);
            return jsonobject;
         }
      }
   }
}