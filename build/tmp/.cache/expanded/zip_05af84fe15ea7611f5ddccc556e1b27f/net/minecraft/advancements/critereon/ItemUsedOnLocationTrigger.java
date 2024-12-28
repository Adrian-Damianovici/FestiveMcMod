package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

public class ItemUsedOnLocationTrigger extends SimpleCriterionTrigger<ItemUsedOnLocationTrigger.TriggerInstance> {
   final ResourceLocation f_285601_;

   public ItemUsedOnLocationTrigger(ResourceLocation p_286779_) {
      this.f_285601_ = p_286779_;
   }

   public ResourceLocation m_7295_() {
      return this.f_285601_;
   }

   public ItemUsedOnLocationTrigger.TriggerInstance createInstance(JsonObject p_286301_, ContextAwarePredicate p_286748_, DeserializationContext p_286322_) {
      ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.fromElement("location", p_286322_, p_286301_.get("location"), LootContextParamSets.ADVANCEMENT_LOCATION);
      if (contextawarepredicate == null) {
         throw new JsonParseException("Failed to parse 'location' field");
      } else {
         return new ItemUsedOnLocationTrigger.TriggerInstance(this.f_285601_, p_286748_, contextawarepredicate);
      }
   }

   public void trigger(ServerPlayer pPlayer, BlockPos pPos, ItemStack pStack) {
      ServerLevel serverlevel = pPlayer.serverLevel();
      BlockState blockstate = serverlevel.getBlockState(pPos);
      LootParams lootparams = (new LootParams.Builder(serverlevel)).withParameter(LootContextParams.ORIGIN, pPos.getCenter()).withParameter(LootContextParams.THIS_ENTITY, pPlayer).withParameter(LootContextParams.BLOCK_STATE, blockstate).withParameter(LootContextParams.TOOL, pStack).create(LootContextParamSets.ADVANCEMENT_LOCATION);
      LootContext lootcontext = (new LootContext.Builder(lootparams)).create((ResourceLocation)null);
      this.trigger(pPlayer, (p_286596_) -> {
         return p_286596_.matches(lootcontext);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate location;

      public TriggerInstance(ResourceLocation p_286265_, ContextAwarePredicate p_286333_, ContextAwarePredicate p_286319_) {
         super(p_286265_, p_286333_);
         this.location = p_286319_;
      }

      public static ItemUsedOnLocationTrigger.TriggerInstance placedBlock(Block pBlock) {
         ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties(pBlock).build());
         return new ItemUsedOnLocationTrigger.TriggerInstance(CriteriaTriggers.PLACED_BLOCK.f_285601_, ContextAwarePredicate.f_285567_, contextawarepredicate);
      }

      public static ItemUsedOnLocationTrigger.TriggerInstance placedBlock(LootItemCondition.Builder... pConditions) {
         ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create(Arrays.stream(pConditions).map(LootItemCondition.Builder::build).toArray((p_286827_) -> {
            return new LootItemCondition[p_286827_];
         }));
         return new ItemUsedOnLocationTrigger.TriggerInstance(CriteriaTriggers.PLACED_BLOCK.f_285601_, ContextAwarePredicate.f_285567_, contextawarepredicate);
      }

      private static ItemUsedOnLocationTrigger.TriggerInstance itemUsedOnLocation(LocationPredicate.Builder pLocation, ItemPredicate.Builder pTool, ResourceLocation p_286742_) {
         ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create(LocationCheck.checkLocation(pLocation).build(), MatchTool.toolMatches(pTool).build());
         return new ItemUsedOnLocationTrigger.TriggerInstance(p_286742_, ContextAwarePredicate.f_285567_, contextawarepredicate);
      }

      public static ItemUsedOnLocationTrigger.TriggerInstance itemUsedOnBlock(LocationPredicate.Builder pLocation, ItemPredicate.Builder pTool) {
         return itemUsedOnLocation(pLocation, pTool, CriteriaTriggers.ITEM_USED_ON_BLOCK.f_285601_);
      }

      public static ItemUsedOnLocationTrigger.TriggerInstance allayDropItemOnBlock(LocationPredicate.Builder pLocation, ItemPredicate.Builder pTool) {
         return itemUsedOnLocation(pLocation, pTool, CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.f_285601_);
      }

      public boolean matches(LootContext pContext) {
         return this.location.matches(pContext);
      }

      public JsonObject serializeToJson(SerializationContext p_286870_) {
         JsonObject jsonobject = super.serializeToJson(p_286870_);
         jsonobject.add("location", this.location.toJson(p_286870_));
         return jsonobject;
      }
   }
}