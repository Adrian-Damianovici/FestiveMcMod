package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.ItemLike;

public class ConsumeItemTrigger extends SimpleCriterionTrigger<ConsumeItemTrigger.TriggerInstance> {
   static final ResourceLocation f_23678_ = new ResourceLocation("consume_item");

   public ResourceLocation m_7295_() {
      return f_23678_;
   }

   public ConsumeItemTrigger.TriggerInstance createInstance(JsonObject p_286724_, ContextAwarePredicate p_286492_, DeserializationContext p_286887_) {
      return new ConsumeItemTrigger.TriggerInstance(p_286492_, ItemPredicate.fromJson(p_286724_.get("item")));
   }

   public void trigger(ServerPlayer pPlayer, ItemStack pItem) {
      this.trigger(pPlayer, (p_23687_) -> {
         return p_23687_.matches(pItem);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;

      public TriggerInstance(ContextAwarePredicate p_286663_, ItemPredicate p_286533_) {
         super(ConsumeItemTrigger.f_23678_, p_286663_);
         this.item = p_286533_;
      }

      public static ConsumeItemTrigger.TriggerInstance usedItem() {
         return new ConsumeItemTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, ItemPredicate.f_45028_);
      }

      public static ConsumeItemTrigger.TriggerInstance usedItem(ItemPredicate p_148082_) {
         return new ConsumeItemTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, p_148082_);
      }

      public static ConsumeItemTrigger.TriggerInstance usedItem(ItemLike p_23704_) {
         return new ConsumeItemTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, new ItemPredicate((TagKey<Item>)null, ImmutableSet.of(p_23704_.asItem()), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.f_30465_, EnchantmentPredicate.f_30465_, (Potion)null, NbtPredicate.f_57471_));
      }

      public boolean matches(ItemStack pItem) {
         return this.item.matches(pItem);
      }

      public JsonObject serializeToJson(SerializationContext p_23706_) {
         JsonObject jsonobject = super.serializeToJson(p_23706_);
         jsonobject.add("item", this.item.serializeToJson());
         return jsonobject;
      }
   }
}