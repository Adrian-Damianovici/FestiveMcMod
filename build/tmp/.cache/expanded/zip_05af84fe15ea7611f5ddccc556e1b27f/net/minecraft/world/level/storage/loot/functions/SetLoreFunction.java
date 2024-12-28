package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * LootItemFunction that sets a stack's lore tag, optionally replacing any previously present lore.
 * The Components for the lore tag are optionally resolved relative to a given {@link LootContext.EntityTarget} for
 * entity-sensitive component data such as scoreboard scores.
 */
public class SetLoreFunction extends LootItemConditionalFunction {
   final boolean replace;
   final List<Component> lore;
   @Nullable
   final LootContext.EntityTarget resolutionContext;

   public SetLoreFunction(LootItemCondition[] p_81083_, boolean p_81084_, List<Component> p_81085_, @Nullable LootContext.EntityTarget p_81086_) {
      super(p_81083_);
      this.replace = p_81084_;
      this.lore = ImmutableList.copyOf(p_81085_);
      this.resolutionContext = p_81086_;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_LORE;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.resolutionContext != null ? ImmutableSet.of(this.resolutionContext.getParam()) : ImmutableSet.of();
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      ListTag listtag = this.getLoreTag(pStack, !this.lore.isEmpty());
      if (listtag != null) {
         if (this.replace) {
            listtag.clear();
         }

         UnaryOperator<Component> unaryoperator = SetNameFunction.createResolver(pContext, this.resolutionContext);
         this.lore.stream().map(unaryoperator).map(Component.Serializer::toJson).map(StringTag::valueOf).forEach(listtag::add);
      }

      return pStack;
   }

   @Nullable
   private ListTag getLoreTag(ItemStack pStack, boolean pCreateIfMissing) {
      CompoundTag compoundtag;
      if (pStack.hasTag()) {
         compoundtag = pStack.getTag();
      } else {
         if (!pCreateIfMissing) {
            return null;
         }

         compoundtag = new CompoundTag();
         pStack.setTag(compoundtag);
      }

      CompoundTag compoundtag1;
      if (compoundtag.contains("display", 10)) {
         compoundtag1 = compoundtag.getCompound("display");
      } else {
         if (!pCreateIfMissing) {
            return null;
         }

         compoundtag1 = new CompoundTag();
         compoundtag.put("display", compoundtag1);
      }

      if (compoundtag1.contains("Lore", 9)) {
         return compoundtag1.getList("Lore", 8);
      } else if (pCreateIfMissing) {
         ListTag listtag = new ListTag();
         compoundtag1.put("Lore", listtag);
         return listtag;
      } else {
         return null;
      }
   }

   public static SetLoreFunction.Builder setLore() {
      return new SetLoreFunction.Builder();
   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetLoreFunction.Builder> {
      private boolean replace;
      private LootContext.EntityTarget resolutionContext;
      private final List<Component> lore = Lists.newArrayList();

      public SetLoreFunction.Builder setReplace(boolean pReplace) {
         this.replace = pReplace;
         return this;
      }

      public SetLoreFunction.Builder setResolutionContext(LootContext.EntityTarget pResolutionContext) {
         this.resolutionContext = pResolutionContext;
         return this;
      }

      public SetLoreFunction.Builder addLine(Component pLine) {
         this.lore.add(pLine);
         return this;
      }

      protected SetLoreFunction.Builder getThis() {
         return this;
      }

      public LootItemFunction build() {
         return new SetLoreFunction(this.getConditions(), this.replace, this.lore, this.resolutionContext);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetLoreFunction> {
      public void m_6170_(JsonObject p_81111_, SetLoreFunction p_81112_, JsonSerializationContext p_81113_) {
         super.m_6170_(p_81111_, p_81112_, p_81113_);
         p_81111_.addProperty("replace", p_81112_.replace);
         JsonArray jsonarray = new JsonArray();

         for(Component component : p_81112_.lore) {
            jsonarray.add(Component.Serializer.toJsonTree(component));
         }

         p_81111_.add("lore", jsonarray);
         if (p_81112_.resolutionContext != null) {
            p_81111_.add("entity", p_81113_.serialize(p_81112_.resolutionContext));
         }

      }

      public SetLoreFunction m_6821_(JsonObject p_81103_, JsonDeserializationContext p_81104_, LootItemCondition[] p_81105_) {
         boolean flag = GsonHelper.getAsBoolean(p_81103_, "replace", false);
         List<Component> list = Streams.stream(GsonHelper.getAsJsonArray(p_81103_, "lore")).map(Component.Serializer::fromJson).collect(ImmutableList.toImmutableList());
         LootContext.EntityTarget lootcontext$entitytarget = GsonHelper.getAsObject(p_81103_, "entity", (LootContext.EntityTarget)null, p_81104_, LootContext.EntityTarget.class);
         return new SetLoreFunction(p_81105_, flag, list, lootcontext$entitytarget);
      }
   }
}