package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;

public class LighthingBoltPredicate implements EntitySubPredicate {
   private static final String f_153233_ = "blocks_set_on_fire";
   private static final String f_153234_ = "entity_struck";
   private final MinMaxBounds.Ints f_153235_;
   private final EntityPredicate f_153236_;

   private LighthingBoltPredicate(MinMaxBounds.Ints p_153239_, EntityPredicate p_153240_) {
      this.f_153235_ = p_153239_;
      this.f_153236_ = p_153240_;
   }

   public static LighthingBoltPredicate m_153250_(MinMaxBounds.Ints p_153251_) {
      return new LighthingBoltPredicate(p_153251_, EntityPredicate.f_36550_);
   }

   public static LighthingBoltPredicate m_220332_(JsonObject p_220333_) {
      return new LighthingBoltPredicate(MinMaxBounds.Ints.fromJson(p_220333_.get("blocks_set_on_fire")), EntityPredicate.fromJson(p_220333_.get("entity_struck")));
   }

   public JsonObject m_213616_() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("blocks_set_on_fire", this.f_153235_.m_55328_());
      jsonobject.add("entity_struck", this.f_153236_.serializeToJson());
      return jsonobject;
   }

   public EntitySubPredicate.Type type() {
      return EntitySubPredicate.Types.LIGHTNING;
   }

   public boolean m_153246_(Entity p_153247_, ServerLevel p_153248_, @Nullable Vec3 p_153249_) {
      if (!(p_153247_ instanceof LightningBolt lightningbolt)) {
         return false;
      } else {
         return this.f_153235_.matches(lightningbolt.getBlocksSetOnFire()) && (this.f_153236_ == EntityPredicate.f_36550_ || lightningbolt.getHitEntities().anyMatch((p_153245_) -> {
            return this.f_153236_.matches(p_153248_, p_153249_, p_153245_);
         }));
      }
   }
}