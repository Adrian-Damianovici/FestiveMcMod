package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.phys.Vec3;

public class SlimePredicate implements EntitySubPredicate {
   private final MinMaxBounds.Ints size;

   private SlimePredicate(MinMaxBounds.Ints p_223420_) {
      this.size = p_223420_;
   }

   public static SlimePredicate sized(MinMaxBounds.Ints pSize) {
      return new SlimePredicate(pSize);
   }

   public static SlimePredicate m_223428_(JsonObject p_223429_) {
      MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(p_223429_.get("size"));
      return new SlimePredicate(minmaxbounds$ints);
   }

   public JsonObject m_213616_() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("size", this.size.m_55328_());
      return jsonobject;
   }

   public boolean m_153246_(Entity pEntity, ServerLevel pLevel, @Nullable Vec3 pPosition) {
      if (pEntity instanceof Slime slime) {
         return this.size.matches(slime.getSize());
      } else {
         return false;
      }
   }

   public EntitySubPredicate.Type type() {
      return EntitySubPredicate.Types.SLIME;
   }
}