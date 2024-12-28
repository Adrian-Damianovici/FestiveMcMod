package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class UsingItemTrigger extends SimpleCriterionTrigger<UsingItemTrigger.TriggerInstance> {
   static final ResourceLocation f_163861_ = new ResourceLocation("using_item");

   public ResourceLocation m_7295_() {
      return f_163861_;
   }

   public UsingItemTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate p_286670_, DeserializationContext pDeserializationContext) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      return new UsingItemTrigger.TriggerInstance(p_286670_, itempredicate);
   }

   public void trigger(ServerPlayer pPlayer, ItemStack pItem) {
      this.trigger(pPlayer, (p_163870_) -> {
         return p_163870_.matches(pItem);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;

      public TriggerInstance(ContextAwarePredicate p_286652_, ItemPredicate p_286296_) {
         super(UsingItemTrigger.f_163861_, p_286652_);
         this.item = p_286296_;
      }

      public static UsingItemTrigger.TriggerInstance lookingAt(EntityPredicate.Builder pPlayer, ItemPredicate.Builder pItem) {
         return new UsingItemTrigger.TriggerInstance(EntityPredicate.wrap(pPlayer.build()), pItem.build());
      }

      public boolean matches(ItemStack pItem) {
         return this.item.matches(pItem);
      }

      public JsonObject serializeToJson(SerializationContext p_163889_) {
         JsonObject jsonobject = super.serializeToJson(p_163889_);
         jsonobject.add("item", this.item.serializeToJson());
         return jsonobject;
      }
   }
}