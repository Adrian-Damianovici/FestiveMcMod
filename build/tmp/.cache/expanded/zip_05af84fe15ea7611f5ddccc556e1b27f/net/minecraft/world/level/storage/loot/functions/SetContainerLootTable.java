package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * LootItemFunction that sets the LootTable and optionally the loot table seed on the stack's {@code BlockEntityTag}.
 * The effect of this is that containers such as chests will receive the given LootTable when placed.
 */
public class SetContainerLootTable extends LootItemConditionalFunction {
   final ResourceLocation name;
   final long seed;
   final BlockEntityType<?> type;

   SetContainerLootTable(LootItemCondition[] p_193045_, ResourceLocation p_193046_, long p_193047_, BlockEntityType<?> p_193048_) {
      super(p_193045_);
      this.name = p_193046_;
      this.seed = p_193047_;
      this.type = p_193048_;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_LOOT_TABLE;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      if (pStack.isEmpty()) {
         return pStack;
      } else {
         CompoundTag compoundtag = BlockItem.getBlockEntityData(pStack);
         if (compoundtag == null) {
            compoundtag = new CompoundTag();
         }

         compoundtag.putString("LootTable", this.name.toString());
         if (this.seed != 0L) {
            compoundtag.putLong("LootTableSeed", this.seed);
         }

         BlockItem.setBlockEntityData(pStack, this.type, compoundtag);
         return pStack;
      }
   }

   /**
    * Validate that this object is used correctly according to the given ValidationContext.
    */
   public void validate(ValidationContext pContext) {
      super.validate(pContext);
      LootDataId<LootTable> lootdataid = new LootDataId<>(LootDataType.TABLE, this.name);
      if (pContext.resolver().getElementOptional(lootdataid).isEmpty()) {
         pContext.reportProblem("Missing loot table used for container: " + this.name);
      }

   }

   public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> pType, ResourceLocation pName) {
      return simpleBuilder((p_193064_) -> {
         return new SetContainerLootTable(p_193064_, pName, 0L, pType);
      });
   }

   public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> pType, ResourceLocation pName, long pSeed) {
      return simpleBuilder((p_193060_) -> {
         return new SetContainerLootTable(p_193060_, pName, pSeed, pType);
      });
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetContainerLootTable> {
      public void m_6170_(JsonObject p_80986_, SetContainerLootTable p_80987_, JsonSerializationContext p_80988_) {
         super.m_6170_(p_80986_, p_80987_, p_80988_);
         p_80986_.addProperty("name", p_80987_.name.toString());
         p_80986_.addProperty("type", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(p_80987_.type).toString());
         if (p_80987_.seed != 0L) {
            p_80986_.addProperty("seed", p_80987_.seed);
         }

      }

      public SetContainerLootTable m_6821_(JsonObject p_80978_, JsonDeserializationContext p_80979_, LootItemCondition[] p_80980_) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(p_80978_, "name"));
         long i = GsonHelper.getAsLong(p_80978_, "seed", 0L);
         ResourceLocation resourcelocation1 = new ResourceLocation(GsonHelper.getAsString(p_80978_, "type"));
         BlockEntityType<?> blockentitytype = BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(resourcelocation1).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown block entity type id '" + resourcelocation1 + "'");
         });
         return new SetContainerLootTable(p_80980_, resourcelocation, i, blockentitytype);
      }
   }
}