package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;

public class LightPredicate {
   public static final LightPredicate f_51335_ = new LightPredicate(MinMaxBounds.Ints.ANY);
   private final MinMaxBounds.Ints composite;

   LightPredicate(MinMaxBounds.Ints p_51339_) {
      this.composite = p_51339_;
   }

   public boolean matches(ServerLevel pLevel, BlockPos pPos) {
      if (this == f_51335_) {
         return true;
      } else if (!pLevel.isLoaded(pPos)) {
         return false;
      } else {
         return this.composite.matches(pLevel.getMaxLocalRawBrightness(pPos));
      }
   }

   public JsonElement m_51340_() {
      if (this == f_51335_) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("light", this.composite.m_55328_());
         return jsonobject;
      }
   }

   public static LightPredicate m_51344_(@Nullable JsonElement p_51345_) {
      if (p_51345_ != null && !p_51345_.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(p_51345_, "light");
         MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(jsonobject.get("light"));
         return new LightPredicate(minmaxbounds$ints);
      } else {
         return f_51335_;
      }
   }

   public static class Builder {
      private MinMaxBounds.Ints composite = MinMaxBounds.Ints.ANY;

      public static LightPredicate.Builder light() {
         return new LightPredicate.Builder();
      }

      public LightPredicate.Builder setComposite(MinMaxBounds.Ints pComposite) {
         this.composite = pComposite;
         return this;
      }

      public LightPredicate build() {
         return new LightPredicate(this.composite);
      }
   }
}