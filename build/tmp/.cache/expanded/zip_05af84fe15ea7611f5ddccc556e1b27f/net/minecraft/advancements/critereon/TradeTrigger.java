package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class TradeTrigger extends SimpleCriterionTrigger<TradeTrigger.TriggerInstance> {
   static final ResourceLocation f_70955_ = new ResourceLocation("villager_trade");

   public ResourceLocation m_7295_() {
      return f_70955_;
   }

   public TradeTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate p_286835_, DeserializationContext pDeserializationContext) {
      ContextAwarePredicate contextawarepredicate = EntityPredicate.fromJson(pJson, "villager", pDeserializationContext);
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      return new TradeTrigger.TriggerInstance(p_286835_, contextawarepredicate, itempredicate);
   }

   public void trigger(ServerPlayer pPlayer, AbstractVillager pVillager, ItemStack pStack) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pVillager);
      this.trigger(pPlayer, (p_70970_) -> {
         return p_70970_.matches(lootcontext, pStack);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate villager;
      private final ItemPredicate item;

      public TriggerInstance(ContextAwarePredicate p_286523_, ContextAwarePredicate p_286395_, ItemPredicate p_286263_) {
         super(TradeTrigger.f_70955_, p_286523_);
         this.villager = p_286395_;
         this.item = p_286263_;
      }

      public static TradeTrigger.TriggerInstance tradedWithVillager() {
         return new TradeTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, ContextAwarePredicate.f_285567_, ItemPredicate.f_45028_);
      }

      public static TradeTrigger.TriggerInstance tradedWithVillager(EntityPredicate.Builder pVillager) {
         return new TradeTrigger.TriggerInstance(EntityPredicate.wrap(pVillager.build()), ContextAwarePredicate.f_285567_, ItemPredicate.f_45028_);
      }

      public boolean matches(LootContext pContext, ItemStack pStack) {
         if (!this.villager.matches(pContext)) {
            return false;
         } else {
            return this.item.matches(pStack);
         }
      }

      public JsonObject serializeToJson(SerializationContext p_70983_) {
         JsonObject jsonobject = super.serializeToJson(p_70983_);
         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("villager", this.villager.toJson(p_70983_));
         return jsonobject;
      }
   }
}