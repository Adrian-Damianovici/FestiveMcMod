package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Set;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * A LootItemCondition that checks whether the {@linkplain LootContextParams#BLOCK_STATE block state} matches a given
 * Block and {@link StatePropertiesPredicate}.
 */
public class LootItemBlockStatePropertyCondition implements LootItemCondition {
   final Block block;
   final StatePropertiesPredicate properties;

   LootItemBlockStatePropertyCondition(Block p_81762_, StatePropertiesPredicate p_81763_) {
      this.block = p_81762_;
      this.properties = p_81763_;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.BLOCK_STATE_PROPERTY;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.BLOCK_STATE);
   }

   public boolean test(LootContext pContext) {
      BlockState blockstate = pContext.getParamOrNull(LootContextParams.BLOCK_STATE);
      return blockstate != null && blockstate.is(this.block) && this.properties.matches(blockstate);
   }

   public static LootItemBlockStatePropertyCondition.Builder hasBlockStateProperties(Block pBlock) {
      return new LootItemBlockStatePropertyCondition.Builder(pBlock);
   }

   public static class Builder implements LootItemCondition.Builder {
      private final Block block;
      private StatePropertiesPredicate properties = StatePropertiesPredicate.f_67658_;

      public Builder(Block pBlock) {
         this.block = pBlock;
      }

      public LootItemBlockStatePropertyCondition.Builder setProperties(StatePropertiesPredicate.Builder pStatePredicateBuilder) {
         this.properties = pStatePredicateBuilder.build();
         return this;
      }

      public LootItemCondition build() {
         return new LootItemBlockStatePropertyCondition(this.block, this.properties);
      }
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemBlockStatePropertyCondition> {
      public void m_6170_(JsonObject p_81795_, LootItemBlockStatePropertyCondition p_81796_, JsonSerializationContext p_81797_) {
         p_81795_.addProperty("block", BuiltInRegistries.BLOCK.getKey(p_81796_.block).toString());
         p_81795_.add("properties", p_81796_.properties.serializeToJson());
      }

      public LootItemBlockStatePropertyCondition m_7561_(JsonObject p_81805_, JsonDeserializationContext p_81806_) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(p_81805_, "block"));
         Block block = BuiltInRegistries.BLOCK.getOptional(resourcelocation).orElseThrow(() -> {
            return new IllegalArgumentException("Can't find block " + resourcelocation);
         });
         StatePropertiesPredicate statepropertiespredicate = StatePropertiesPredicate.fromJson(p_81805_.get("properties"));
         statepropertiespredicate.checkState(block.getStateDefinition(), (p_81790_) -> {
            throw new JsonSyntaxException("Block " + block + " has no property " + p_81790_);
         });
         return new LootItemBlockStatePropertyCondition(block, statepropertiespredicate);
      }
   }
}