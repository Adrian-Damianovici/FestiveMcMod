package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class FilledBucketTrigger extends SimpleCriterionTrigger<FilledBucketTrigger.TriggerInstance> {
   static final ResourceLocation f_38768_ = new ResourceLocation("filled_bucket");

   public ResourceLocation m_7295_() {
      return f_38768_;
   }

   public FilledBucketTrigger.TriggerInstance createInstance(JsonObject p_286783_, ContextAwarePredicate p_286776_, DeserializationContext p_286812_) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(p_286783_.get("item"));
      return new FilledBucketTrigger.TriggerInstance(p_286776_, itempredicate);
   }

   public void trigger(ServerPlayer pPlayer, ItemStack pStack) {
      this.trigger(pPlayer, (p_38777_) -> {
         return p_38777_.matches(pStack);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;

      public TriggerInstance(ContextAwarePredicate p_286231_, ItemPredicate p_286845_) {
         super(FilledBucketTrigger.f_38768_, p_286231_);
         this.item = p_286845_;
      }

      public static FilledBucketTrigger.TriggerInstance filledBucket(ItemPredicate p_38794_) {
         return new FilledBucketTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, p_38794_);
      }

      public boolean matches(ItemStack pStack) {
         return this.item.matches(pStack);
      }

      public JsonObject serializeToJson(SerializationContext p_38796_) {
         JsonObject jsonobject = super.serializeToJson(p_38796_);
         jsonobject.add("item", this.item.serializeToJson());
         return jsonobject;
      }
   }
}