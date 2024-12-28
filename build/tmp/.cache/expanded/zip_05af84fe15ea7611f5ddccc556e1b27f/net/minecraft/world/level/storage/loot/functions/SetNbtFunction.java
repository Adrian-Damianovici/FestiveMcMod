package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * LootItemFunction that merges a given CompoundTag into the stack's NBT tag.
 */
public class SetNbtFunction extends LootItemConditionalFunction {
   final CompoundTag tag;

   SetNbtFunction(LootItemCondition[] p_81176_, CompoundTag p_81177_) {
      super(p_81176_);
      this.tag = p_81177_;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_NBT;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      pStack.getOrCreateTag().merge(this.tag);
      return pStack;
   }

   /** @deprecated */
   @Deprecated
   public static LootItemConditionalFunction.Builder<?> setTag(CompoundTag pTag) {
      return simpleBuilder((p_81191_) -> {
         return new SetNbtFunction(p_81191_, pTag);
      });
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetNbtFunction> {
      public void m_6170_(JsonObject p_81203_, SetNbtFunction p_81204_, JsonSerializationContext p_81205_) {
         super.m_6170_(p_81203_, p_81204_, p_81205_);
         p_81203_.addProperty("tag", p_81204_.tag.toString());
      }

      public SetNbtFunction m_6821_(JsonObject p_81195_, JsonDeserializationContext p_81196_, LootItemCondition[] p_81197_) {
         try {
            CompoundTag compoundtag = TagParser.parseTag(GsonHelper.getAsString(p_81195_, "tag"));
            return new SetNbtFunction(p_81197_, compoundtag);
         } catch (CommandSyntaxException commandsyntaxexception) {
            throw new JsonSyntaxException(commandsyntaxexception.getMessage());
         }
      }
   }
}