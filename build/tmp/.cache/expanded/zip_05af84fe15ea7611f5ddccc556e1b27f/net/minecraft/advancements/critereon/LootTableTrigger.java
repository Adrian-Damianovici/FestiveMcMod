package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class LootTableTrigger extends SimpleCriterionTrigger<LootTableTrigger.TriggerInstance> {
   static final ResourceLocation f_54593_ = new ResourceLocation("player_generates_container_loot");

   public ResourceLocation m_7295_() {
      return f_54593_;
   }

   protected LootTableTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate p_286259_, DeserializationContext pDeserializationContext) {
      ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pJson, "loot_table"));
      return new LootTableTrigger.TriggerInstance(p_286259_, resourcelocation);
   }

   public void trigger(ServerPlayer pPlayer, ResourceLocation pLootTable) {
      this.trigger(pPlayer, (p_54606_) -> {
         return p_54606_.matches(pLootTable);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ResourceLocation lootTable;

      public TriggerInstance(ContextAwarePredicate p_286834_, ResourceLocation pLootTable) {
         super(LootTableTrigger.f_54593_, p_286834_);
         this.lootTable = pLootTable;
      }

      public static LootTableTrigger.TriggerInstance lootTableUsed(ResourceLocation pLootTable) {
         return new LootTableTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, pLootTable);
      }

      public boolean matches(ResourceLocation pLootTable) {
         return this.lootTable.equals(pLootTable);
      }

      public JsonObject serializeToJson(SerializationContext p_54617_) {
         JsonObject jsonobject = super.serializeToJson(p_54617_);
         jsonobject.addProperty("loot_table", this.lootTable.toString());
         return jsonobject;
      }
   }
}