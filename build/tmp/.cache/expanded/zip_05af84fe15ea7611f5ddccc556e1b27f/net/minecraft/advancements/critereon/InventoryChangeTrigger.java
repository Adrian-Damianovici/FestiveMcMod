package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.ItemLike;

public class InventoryChangeTrigger extends SimpleCriterionTrigger<InventoryChangeTrigger.TriggerInstance> {
   static final ResourceLocation f_43145_ = new ResourceLocation("inventory_changed");

   public ResourceLocation m_7295_() {
      return f_43145_;
   }

   public InventoryChangeTrigger.TriggerInstance createInstance(JsonObject p_286555_, ContextAwarePredicate p_286704_, DeserializationContext p_286270_) {
      JsonObject jsonobject = GsonHelper.getAsJsonObject(p_286555_, "slots", new JsonObject());
      MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(jsonobject.get("occupied"));
      MinMaxBounds.Ints minmaxbounds$ints1 = MinMaxBounds.Ints.fromJson(jsonobject.get("full"));
      MinMaxBounds.Ints minmaxbounds$ints2 = MinMaxBounds.Ints.fromJson(jsonobject.get("empty"));
      ItemPredicate[] aitempredicate = ItemPredicate.fromJsonArray(p_286555_.get("items"));
      return new InventoryChangeTrigger.TriggerInstance(p_286704_, minmaxbounds$ints, minmaxbounds$ints1, minmaxbounds$ints2, aitempredicate);
   }

   public void trigger(ServerPlayer pPlayer, Inventory pInventory, ItemStack pStack) {
      int i = 0;
      int j = 0;
      int k = 0;

      for(int l = 0; l < pInventory.getContainerSize(); ++l) {
         ItemStack itemstack = pInventory.getItem(l);
         if (itemstack.isEmpty()) {
            ++j;
         } else {
            ++k;
            if (itemstack.getCount() >= itemstack.getMaxStackSize()) {
               ++i;
            }
         }
      }

      this.trigger(pPlayer, pInventory, pStack, i, j, k);
   }

   private void trigger(ServerPlayer pPlayer, Inventory pInventory, ItemStack pStack, int pFull, int pEmpty, int pOccupied) {
      this.trigger(pPlayer, (p_43166_) -> {
         return p_43166_.matches(pInventory, pStack, pFull, pEmpty, pOccupied);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final MinMaxBounds.Ints slotsOccupied;
      private final MinMaxBounds.Ints slotsFull;
      private final MinMaxBounds.Ints slotsEmpty;
      private final ItemPredicate[] predicates;

      public TriggerInstance(ContextAwarePredicate p_286286_, MinMaxBounds.Ints pSlotsOccupied, MinMaxBounds.Ints pSlotsFull, MinMaxBounds.Ints pSlotsEmpty, ItemPredicate[] p_286380_) {
         super(InventoryChangeTrigger.f_43145_, p_286286_);
         this.slotsOccupied = pSlotsOccupied;
         this.slotsFull = pSlotsFull;
         this.slotsEmpty = pSlotsEmpty;
         this.predicates = p_286380_;
      }

      public static InventoryChangeTrigger.TriggerInstance hasItems(ItemPredicate... pItems) {
         return new InventoryChangeTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, pItems);
      }

      public static InventoryChangeTrigger.TriggerInstance hasItems(ItemLike... p_43200_) {
         ItemPredicate[] aitempredicate = new ItemPredicate[p_43200_.length];

         for(int i = 0; i < p_43200_.length; ++i) {
            aitempredicate[i] = new ItemPredicate((TagKey<Item>)null, ImmutableSet.of(p_43200_[i].asItem()), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.f_30465_, EnchantmentPredicate.f_30465_, (Potion)null, NbtPredicate.f_57471_);
         }

         return hasItems(aitempredicate);
      }

      public JsonObject serializeToJson(SerializationContext p_43196_) {
         JsonObject jsonobject = super.serializeToJson(p_43196_);
         if (!this.slotsOccupied.isAny() || !this.slotsFull.isAny() || !this.slotsEmpty.isAny()) {
            JsonObject jsonobject1 = new JsonObject();
            jsonobject1.add("occupied", this.slotsOccupied.m_55328_());
            jsonobject1.add("full", this.slotsFull.m_55328_());
            jsonobject1.add("empty", this.slotsEmpty.m_55328_());
            jsonobject.add("slots", jsonobject1);
         }

         if (this.predicates.length > 0) {
            JsonArray jsonarray = new JsonArray();

            for(ItemPredicate itempredicate : this.predicates) {
               jsonarray.add(itempredicate.serializeToJson());
            }

            jsonobject.add("items", jsonarray);
         }

         return jsonobject;
      }

      public boolean matches(Inventory pInventory, ItemStack pStack, int pFull, int pEmpty, int pOccupied) {
         if (!this.slotsFull.matches(pFull)) {
            return false;
         } else if (!this.slotsEmpty.matches(pEmpty)) {
            return false;
         } else if (!this.slotsOccupied.matches(pOccupied)) {
            return false;
         } else {
            int i = this.predicates.length;
            if (i == 0) {
               return true;
            } else if (i != 1) {
               List<ItemPredicate> list = new ObjectArrayList<>(this.predicates);
               int j = pInventory.getContainerSize();

               for(int k = 0; k < j; ++k) {
                  if (list.isEmpty()) {
                     return true;
                  }

                  ItemStack itemstack = pInventory.getItem(k);
                  if (!itemstack.isEmpty()) {
                     list.removeIf((p_43194_) -> {
                        return p_43194_.matches(itemstack);
                     });
                  }
               }

               return list.isEmpty();
            } else {
               return !pStack.isEmpty() && this.predicates[0].matches(pStack);
            }
         }
      }
   }
}