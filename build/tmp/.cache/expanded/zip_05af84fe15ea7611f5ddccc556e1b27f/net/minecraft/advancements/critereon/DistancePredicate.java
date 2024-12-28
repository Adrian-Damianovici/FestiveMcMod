package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

public class DistancePredicate {
   public static final DistancePredicate f_26241_ = new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
   private final MinMaxBounds.Doubles x;
   private final MinMaxBounds.Doubles y;
   private final MinMaxBounds.Doubles z;
   private final MinMaxBounds.Doubles horizontal;
   private final MinMaxBounds.Doubles absolute;

   public DistancePredicate(MinMaxBounds.Doubles p_26249_, MinMaxBounds.Doubles p_26250_, MinMaxBounds.Doubles p_26251_, MinMaxBounds.Doubles p_26252_, MinMaxBounds.Doubles p_26253_) {
      this.x = p_26249_;
      this.y = p_26250_;
      this.z = p_26251_;
      this.horizontal = p_26252_;
      this.absolute = p_26253_;
   }

   public static DistancePredicate horizontal(MinMaxBounds.Doubles pHorizontal) {
      return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, pHorizontal, MinMaxBounds.Doubles.ANY);
   }

   public static DistancePredicate vertical(MinMaxBounds.Doubles pVertical) {
      return new DistancePredicate(MinMaxBounds.Doubles.ANY, pVertical, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
   }

   public static DistancePredicate absolute(MinMaxBounds.Doubles pAbsolute) {
      return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, pAbsolute);
   }

   public boolean matches(double pX1, double pY1, double pZ1, double pX2, double pY2, double pZ2) {
      float f = (float)(pX1 - pX2);
      float f1 = (float)(pY1 - pY2);
      float f2 = (float)(pZ1 - pZ2);
      if (this.x.matches((double)Mth.abs(f)) && this.y.matches((double)Mth.abs(f1)) && this.z.matches((double)Mth.abs(f2))) {
         if (!this.horizontal.matchesSqr((double)(f * f + f2 * f2))) {
            return false;
         } else {
            return this.absolute.matchesSqr((double)(f * f + f1 * f1 + f2 * f2));
         }
      } else {
         return false;
      }
   }

   public static DistancePredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "distance");
         MinMaxBounds.Doubles minmaxbounds$doubles = MinMaxBounds.Doubles.fromJson(jsonobject.get("x"));
         MinMaxBounds.Doubles minmaxbounds$doubles1 = MinMaxBounds.Doubles.fromJson(jsonobject.get("y"));
         MinMaxBounds.Doubles minmaxbounds$doubles2 = MinMaxBounds.Doubles.fromJson(jsonobject.get("z"));
         MinMaxBounds.Doubles minmaxbounds$doubles3 = MinMaxBounds.Doubles.fromJson(jsonobject.get("horizontal"));
         MinMaxBounds.Doubles minmaxbounds$doubles4 = MinMaxBounds.Doubles.fromJson(jsonobject.get("absolute"));
         return new DistancePredicate(minmaxbounds$doubles, minmaxbounds$doubles1, minmaxbounds$doubles2, minmaxbounds$doubles3, minmaxbounds$doubles4);
      } else {
         return f_26241_;
      }
   }

   public JsonElement serializeToJson() {
      if (this == f_26241_) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("x", this.x.m_55328_());
         jsonobject.add("y", this.y.m_55328_());
         jsonobject.add("z", this.z.m_55328_());
         jsonobject.add("horizontal", this.horizontal.m_55328_());
         jsonobject.add("absolute", this.absolute.m_55328_());
         return jsonobject;
      }
   }
}