package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ShotCrossbowTrigger extends SimpleCriterionTrigger<ShotCrossbowTrigger.TriggerInstance> {
   static final ResourceLocation f_65458_ = new ResourceLocation("shot_crossbow");

   public ResourceLocation m_7295_() {
      return f_65458_;
   }

   public ShotCrossbowTrigger.TriggerInstance createInstance(JsonObject p_286679_, ContextAwarePredicate p_286410_, DeserializationContext p_286233_) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(p_286679_.get("item"));
      return new ShotCrossbowTrigger.TriggerInstance(p_286410_, itempredicate);
   }

   public void trigger(ServerPlayer pShooter, ItemStack pStack) {
      this.trigger(pShooter, (p_65467_) -> {
         return p_65467_.matches(pStack);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;

      public TriggerInstance(ContextAwarePredicate p_286262_, ItemPredicate p_286755_) {
         super(ShotCrossbowTrigger.f_65458_, p_286262_);
         this.item = p_286755_;
      }

      public static ShotCrossbowTrigger.TriggerInstance shotCrossbow(ItemPredicate p_159432_) {
         return new ShotCrossbowTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, p_159432_);
      }

      public static ShotCrossbowTrigger.TriggerInstance shotCrossbow(ItemLike pItem) {
         return new ShotCrossbowTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, ItemPredicate.Builder.item().of(pItem).build());
      }

      public boolean matches(ItemStack pItem) {
         return this.item.matches(pItem);
      }

      public JsonObject serializeToJson(SerializationContext p_65486_) {
         JsonObject jsonobject = super.serializeToJson(p_65486_);
         jsonobject.add("item", this.item.serializeToJson());
         return jsonobject;
      }
   }
}