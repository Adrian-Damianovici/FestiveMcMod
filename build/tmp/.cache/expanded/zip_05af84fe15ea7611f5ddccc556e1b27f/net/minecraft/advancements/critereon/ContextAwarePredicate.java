package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class ContextAwarePredicate {
   public static final ContextAwarePredicate f_285567_ = new ContextAwarePredicate(new LootItemCondition[0]);
   private final LootItemCondition[] conditions;
   private final Predicate<LootContext> compositePredicates;

   ContextAwarePredicate(LootItemCondition[] p_286308_) {
      this.conditions = p_286308_;
      this.compositePredicates = LootItemConditions.andConditions(p_286308_);
   }

   public static ContextAwarePredicate create(LootItemCondition... pConditions) {
      return new ContextAwarePredicate(pConditions);
   }

   @Nullable
   public static ContextAwarePredicate fromElement(String p_286647_, DeserializationContext pContext, @Nullable JsonElement pJson, LootContextParamSet pLootContextParams) {
      if (pJson != null && pJson.isJsonArray()) {
         LootItemCondition[] alootitemcondition = pContext.deserializeConditions(pJson.getAsJsonArray(), pContext.getAdvancementId() + "/" + p_286647_, pLootContextParams);
         return new ContextAwarePredicate(alootitemcondition);
      } else {
         return null;
      }
   }

   public boolean matches(LootContext pContext) {
      return this.compositePredicates.test(pContext);
   }

   public JsonElement toJson(SerializationContext p_286222_) {
      return (JsonElement)(this.conditions.length == 0 ? JsonNull.INSTANCE : p_286222_.m_64772_(this.conditions));
   }

   public static JsonElement toJson(ContextAwarePredicate[] p_286611_, SerializationContext p_286638_) {
      if (p_286611_.length == 0) {
         return JsonNull.INSTANCE;
      } else {
         JsonArray jsonarray = new JsonArray();

         for(ContextAwarePredicate contextawarepredicate : p_286611_) {
            jsonarray.add(contextawarepredicate.toJson(p_286638_));
         }

         return jsonarray;
      }
   }
}