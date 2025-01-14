package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EnterBlockTrigger extends SimpleCriterionTrigger<EnterBlockTrigger.TriggerInstance> {
   static final ResourceLocation f_31265_ = new ResourceLocation("enter_block");

   public ResourceLocation m_7295_() {
      return f_31265_;
   }

   public EnterBlockTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate p_286595_, DeserializationContext pDeserializationContext) {
      Block block = deserializeBlock(pJson);
      StatePropertiesPredicate statepropertiespredicate = StatePropertiesPredicate.fromJson(pJson.get("state"));
      if (block != null) {
         statepropertiespredicate.checkState(block.getStateDefinition(), (p_31274_) -> {
            throw new JsonSyntaxException("Block " + block + " has no property " + p_31274_);
         });
      }

      return new EnterBlockTrigger.TriggerInstance(p_286595_, block, statepropertiespredicate);
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

   public void trigger(ServerPlayer pPlayer, BlockState pState) {
      this.trigger(pPlayer, (p_31277_) -> {
         return p_31277_.matches(pState);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      @Nullable
      private final Block block;
      private final StatePropertiesPredicate state;

      public TriggerInstance(ContextAwarePredicate p_286269_, @Nullable Block pBlock, StatePropertiesPredicate p_286864_) {
         super(EnterBlockTrigger.f_31265_, p_286269_);
         this.block = pBlock;
         this.state = p_286864_;
      }

      public static EnterBlockTrigger.TriggerInstance entersBlock(Block pBlock) {
         return new EnterBlockTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, pBlock, StatePropertiesPredicate.f_67658_);
      }

      public JsonObject serializeToJson(SerializationContext p_31302_) {
         JsonObject jsonobject = super.serializeToJson(p_31302_);
         if (this.block != null) {
            jsonobject.addProperty("block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
         }

         jsonobject.add("state", this.state.serializeToJson());
         return jsonobject;
      }

      public boolean matches(BlockState pState) {
         if (this.block != null && !pState.is(this.block)) {
            return false;
         } else {
            return this.state.matches(pState);
         }
      }
   }
}