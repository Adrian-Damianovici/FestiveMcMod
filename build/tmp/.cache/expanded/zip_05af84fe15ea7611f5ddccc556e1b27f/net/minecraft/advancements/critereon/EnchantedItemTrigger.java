package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class EnchantedItemTrigger extends SimpleCriterionTrigger<EnchantedItemTrigger.TriggerInstance> {
   static final ResourceLocation f_27664_ = new ResourceLocation("enchanted_item");

   public ResourceLocation m_7295_() {
      return f_27664_;
   }

   public EnchantedItemTrigger.TriggerInstance createInstance(JsonObject p_286526_, ContextAwarePredicate p_286279_, DeserializationContext p_286881_) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(p_286526_.get("item"));
      MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(p_286526_.get("levels"));
      return new EnchantedItemTrigger.TriggerInstance(p_286279_, itempredicate, minmaxbounds$ints);
   }

   public void trigger(ServerPlayer pPlayer, ItemStack pItem, int pLevelsSpent) {
      this.trigger(pPlayer, (p_27675_) -> {
         return p_27675_.matches(pItem, pLevelsSpent);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;
      private final MinMaxBounds.Ints levels;

      public TriggerInstance(ContextAwarePredicate p_286871_, ItemPredicate p_286640_, MinMaxBounds.Ints pLevels) {
         super(EnchantedItemTrigger.f_27664_, p_286871_);
         this.item = p_286640_;
         this.levels = pLevels;
      }

      public static EnchantedItemTrigger.TriggerInstance enchantedItem() {
         return new EnchantedItemTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, ItemPredicate.f_45028_, MinMaxBounds.Ints.ANY);
      }

      public boolean matches(ItemStack pItem, int pLevels) {
         if (!this.item.matches(pItem)) {
            return false;
         } else {
            return this.levels.matches(pLevels);
         }
      }

      public JsonObject serializeToJson(SerializationContext p_27695_) {
         JsonObject jsonobject = super.serializeToJson(p_27695_);
         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("levels", this.levels.m_55328_());
         return jsonobject;
      }
   }
}