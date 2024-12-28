package net.minecraft.advancements.critereon;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class DeserializationContext {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final ResourceLocation id;
   private final LootDataManager lootData;
   private final Gson f_25868_ = Deserializers.m_78798_().create();

   public DeserializationContext(ResourceLocation pId, LootDataManager pLootData) {
      this.id = pId;
      this.lootData = pLootData;
   }

   public final LootItemCondition[] deserializeConditions(JsonArray pJson, String pLocation, LootContextParamSet pLootContextParams) {
      LootItemCondition[] alootitemcondition = this.f_25868_.fromJson(pJson, LootItemCondition[].class);
      ValidationContext validationcontext = new ValidationContext(pLootContextParams, this.lootData);

      for(LootItemCondition lootitemcondition : alootitemcondition) {
         lootitemcondition.validate(validationcontext);
         validationcontext.getProblems().forEach((p_25880_, p_25881_) -> {
            LOGGER.warn("Found validation problem in advancement trigger {}/{}: {}", pLocation, p_25880_, p_25881_);
         });
      }

      return alootitemcondition;
   }

   public ResourceLocation getAdvancementId() {
      return this.id;
   }
}