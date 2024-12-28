package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;

public class FishingHookPredicate implements EntitySubPredicate {
   public static final FishingHookPredicate ANY = new FishingHookPredicate(false);
   private static final String f_150706_ = "in_open_water";
   private final boolean inOpenWater;

   private FishingHookPredicate(boolean p_39760_) {
      this.inOpenWater = p_39760_;
   }

   public static FishingHookPredicate inOpenWater(boolean pInOpenWater) {
      return new FishingHookPredicate(pInOpenWater);
   }

   public static FishingHookPredicate m_219719_(JsonObject p_219720_) {
      JsonElement jsonelement = p_219720_.get("in_open_water");
      return jsonelement != null ? new FishingHookPredicate(GsonHelper.convertToBoolean(jsonelement, "in_open_water")) : ANY;
   }

   public JsonObject m_213616_() {
      if (this == ANY) {
         return new JsonObject();
      } else {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("in_open_water", new JsonPrimitive(this.inOpenWater));
         return jsonobject;
      }
   }

   public EntitySubPredicate.Type type() {
      return EntitySubPredicate.Types.FISHING_HOOK;
   }

   public boolean m_153246_(Entity pEntity, ServerLevel pLevel, @Nullable Vec3 pPosition) {
      if (this == ANY) {
         return true;
      } else if (!(pEntity instanceof FishingHook)) {
         return false;
      } else {
         FishingHook fishinghook = (FishingHook)pEntity;
         return this.inOpenWater == fishinghook.isOpenWaterFishing();
      }
   }
}