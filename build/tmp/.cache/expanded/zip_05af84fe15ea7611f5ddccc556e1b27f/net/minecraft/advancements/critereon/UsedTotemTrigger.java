package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class UsedTotemTrigger extends SimpleCriterionTrigger<UsedTotemTrigger.TriggerInstance> {
   static final ResourceLocation f_74427_ = new ResourceLocation("used_totem");

   public ResourceLocation m_7295_() {
      return f_74427_;
   }

   public UsedTotemTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate p_286597_, DeserializationContext pDeserializationContext) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      return new UsedTotemTrigger.TriggerInstance(p_286597_, itempredicate);
   }

   public void trigger(ServerPlayer pPlayer, ItemStack pItem) {
      this.trigger(pPlayer, (p_74436_) -> {
         return p_74436_.matches(pItem);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;

      public TriggerInstance(ContextAwarePredicate p_286406_, ItemPredicate p_286462_) {
         super(UsedTotemTrigger.f_74427_, p_286406_);
         this.item = p_286462_;
      }

      public static UsedTotemTrigger.TriggerInstance usedTotem(ItemPredicate p_163725_) {
         return new UsedTotemTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, p_163725_);
      }

      public static UsedTotemTrigger.TriggerInstance usedTotem(ItemLike p_74453_) {
         return new UsedTotemTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, ItemPredicate.Builder.item().of(p_74453_).build());
      }

      public boolean matches(ItemStack pItem) {
         return this.item.matches(pItem);
      }

      public JsonObject serializeToJson(SerializationContext p_74455_) {
         JsonObject jsonobject = super.serializeToJson(p_74455_);
         jsonobject.add("item", this.item.serializeToJson());
         return jsonobject;
      }
   }
}