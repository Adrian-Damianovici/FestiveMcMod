package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BeeNestDestroyedTrigger extends SimpleCriterionTrigger<BeeNestDestroyedTrigger.TriggerInstance> {
   static final ResourceLocation f_17473_ = new ResourceLocation("bee_nest_destroyed");

   public ResourceLocation m_7295_() {
      return f_17473_;
   }

   public BeeNestDestroyedTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate p_286621_, DeserializationContext pDeserializationContext) {
      Block block = deserializeBlock(pJson);
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(pJson.get("num_bees_inside"));
      return new BeeNestDestroyedTrigger.TriggerInstance(p_286621_, block, itempredicate, minmaxbounds$ints);
   }

   @Nullable
   private static Block deserializeBlock(JsonObject pJson) {
      if (pJson.has("block")) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pJson, "block"));
         return BuiltInRegistries.BLOCK.getOptional(resourcelocation).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown block type '" + resourcelocation + "'");
         });
      } else {
         return null;
      }
   }

   public void trigger(ServerPlayer pPlayer, BlockState pState, ItemStack pStack, int pNumBees) {
      this.trigger(pPlayer, (p_146660_) -> {
         return p_146660_.matches(pState, pStack, pNumBees);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      @Nullable
      private final Block block;
      private final ItemPredicate item;
      private final MinMaxBounds.Ints numBees;

      public TriggerInstance(ContextAwarePredicate p_286609_, @Nullable Block pBlock, ItemPredicate p_286572_, MinMaxBounds.Ints pNumBees) {
         super(BeeNestDestroyedTrigger.f_17473_, p_286609_);
         this.block = pBlock;
         this.item = p_286572_;
         this.numBees = pNumBees;
      }

      public static BeeNestDestroyedTrigger.TriggerInstance destroyedBeeNest(Block pBlock, ItemPredicate.Builder pItem, MinMaxBounds.Ints pNumBees) {
         return new BeeNestDestroyedTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, pBlock, pItem.build(), pNumBees);
      }

      public boolean matches(BlockState pState, ItemStack pStack, int pNumBees) {
         if (this.block != null && !pState.is(this.block)) {
            return false;
         } else {
            return !this.item.matches(pStack) ? false : this.numBees.matches(pNumBees);
         }
      }

      public JsonObject serializeToJson(SerializationContext p_17517_) {
         JsonObject jsonobject = super.serializeToJson(p_17517_);
         if (this.block != null) {
            jsonobject.addProperty("block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
         }

         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("num_bees_inside", this.numBees.m_55328_());
         return jsonobject;
      }
   }
}