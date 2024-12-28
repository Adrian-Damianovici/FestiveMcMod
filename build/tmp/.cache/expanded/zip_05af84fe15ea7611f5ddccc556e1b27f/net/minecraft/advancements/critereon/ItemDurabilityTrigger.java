package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ItemDurabilityTrigger extends SimpleCriterionTrigger<ItemDurabilityTrigger.TriggerInstance> {
   static final ResourceLocation f_43665_ = new ResourceLocation("item_durability_changed");

   public ResourceLocation m_7295_() {
      return f_43665_;
   }

   public ItemDurabilityTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate p_286383_, DeserializationContext pDeserializationContext) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(pJson.get("durability"));
      MinMaxBounds.Ints minmaxbounds$ints1 = MinMaxBounds.Ints.fromJson(pJson.get("delta"));
      return new ItemDurabilityTrigger.TriggerInstance(p_286383_, itempredicate, minmaxbounds$ints, minmaxbounds$ints1);
   }

   public void trigger(ServerPlayer pPlayer, ItemStack pItem, int pNewDurability) {
      this.trigger(pPlayer, (p_43676_) -> {
         return p_43676_.matches(pItem, pNewDurability);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;
      private final MinMaxBounds.Ints durability;
      private final MinMaxBounds.Ints delta;

      public TriggerInstance(ContextAwarePredicate p_286731_, ItemPredicate p_286447_, MinMaxBounds.Ints pDurability, MinMaxBounds.Ints pDelta) {
         super(ItemDurabilityTrigger.f_43665_, p_286731_);
         this.item = p_286447_;
         this.durability = pDurability;
         this.delta = pDelta;
      }

      public static ItemDurabilityTrigger.TriggerInstance changedDurability(ItemPredicate p_151287_, MinMaxBounds.Ints pDurability) {
         return changedDurability(ContextAwarePredicate.f_285567_, p_151287_, pDurability);
      }

      public static ItemDurabilityTrigger.TriggerInstance changedDurability(ContextAwarePredicate p_286720_, ItemPredicate p_286288_, MinMaxBounds.Ints pDurability) {
         return new ItemDurabilityTrigger.TriggerInstance(p_286720_, p_286288_, pDurability, MinMaxBounds.Ints.ANY);
      }

      public boolean matches(ItemStack pItem, int pDurability) {
         if (!this.item.matches(pItem)) {
            return false;
         } else if (!this.durability.matches(pItem.getMaxDamage() - pDurability)) {
            return false;
         } else {
            return this.delta.matches(pItem.getDamageValue() - pDurability);
         }
      }

      public JsonObject serializeToJson(SerializationContext p_43702_) {
         JsonObject jsonobject = super.serializeToJson(p_43702_);
         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("durability", this.durability.m_55328_());
         jsonobject.add("delta", this.delta.m_55328_());
         return jsonobject;
      }
   }
}