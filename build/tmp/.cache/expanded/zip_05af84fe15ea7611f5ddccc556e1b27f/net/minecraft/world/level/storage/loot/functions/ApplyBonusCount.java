package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * LootItemFunction that modifies the stack's count based on an enchantment level on the {@linkplain
 * LootContextParams#TOOL tool} using various formulas.
 */
public class ApplyBonusCount extends LootItemConditionalFunction {
   static final Map<ResourceLocation, ApplyBonusCount.FormulaDeserializer> FORMULAS = Maps.newHashMap();
   final Enchantment enchantment;
   final ApplyBonusCount.Formula formula;

   ApplyBonusCount(LootItemCondition[] p_79903_, Enchantment p_79904_, ApplyBonusCount.Formula p_79905_) {
      super(p_79903_);
      this.enchantment = p_79904_;
      this.formula = p_79905_;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.APPLY_BONUS;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.TOOL);
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      ItemStack itemstack = pContext.getParamOrNull(LootContextParams.TOOL);
      if (itemstack != null) {
         int i = EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, itemstack);
         int j = this.formula.calculateNewCount(pContext.getRandom(), pStack.getCount(), i);
         pStack.setCount(j);
      }

      return pStack;
   }

   public static LootItemConditionalFunction.Builder<?> addBonusBinomialDistributionCount(Enchantment pEnchantment, float pProbability, int pExtraRounds) {
      return simpleBuilder((p_79928_) -> {
         return new ApplyBonusCount(p_79928_, pEnchantment, new ApplyBonusCount.BinomialWithBonusCount(pExtraRounds, pProbability));
      });
   }

   public static LootItemConditionalFunction.Builder<?> addOreBonusCount(Enchantment pEnchantment) {
      return simpleBuilder((p_79943_) -> {
         return new ApplyBonusCount(p_79943_, pEnchantment, new ApplyBonusCount.OreDrops());
      });
   }

   public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Enchantment pEnchantment) {
      return simpleBuilder((p_79935_) -> {
         return new ApplyBonusCount(p_79935_, pEnchantment, new ApplyBonusCount.UniformBonusCount(1));
      });
   }

   public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Enchantment pEnchantment, int pBonusMultiplier) {
      return simpleBuilder((p_79932_) -> {
         return new ApplyBonusCount(p_79932_, pEnchantment, new ApplyBonusCount.UniformBonusCount(pBonusMultiplier));
      });
   }

   static {
      FORMULAS.put(ApplyBonusCount.BinomialWithBonusCount.TYPE, ApplyBonusCount.BinomialWithBonusCount::m_79955_);
      FORMULAS.put(ApplyBonusCount.OreDrops.TYPE, ApplyBonusCount.OreDrops::m_79979_);
      FORMULAS.put(ApplyBonusCount.UniformBonusCount.TYPE, ApplyBonusCount.UniformBonusCount::m_80018_);
   }

   /**
    * Applies a bonus based on a binomial distribution with {@code n = enchantmentLevel + extraRounds} and {@code p =
    * probability}.
    */
   static final class BinomialWithBonusCount implements ApplyBonusCount.Formula {
      public static final ResourceLocation TYPE = new ResourceLocation("binomial_with_bonus_count");
      private final int extraRounds;
      private final float probability;

      public BinomialWithBonusCount(int p_79952_, float p_79953_) {
         this.extraRounds = p_79952_;
         this.probability = p_79953_;
      }

      public int calculateNewCount(RandomSource pRandom, int pOriginalCount, int pEnchantmentLevel) {
         for(int i = 0; i < pEnchantmentLevel + this.extraRounds; ++i) {
            if (pRandom.nextFloat() < this.probability) {
               ++pOriginalCount;
            }
         }

         return pOriginalCount;
      }

      public void m_6417_(JsonObject p_79959_, JsonSerializationContext p_79960_) {
         p_79959_.addProperty("extra", this.extraRounds);
         p_79959_.addProperty("probability", this.probability);
      }

      public static ApplyBonusCount.Formula m_79955_(JsonObject p_79956_, JsonDeserializationContext p_79957_) {
         int i = GsonHelper.getAsInt(p_79956_, "extra");
         float f = GsonHelper.getAsFloat(p_79956_, "probability");
         return new ApplyBonusCount.BinomialWithBonusCount(i, f);
      }

      public ResourceLocation getType() {
         return TYPE;
      }
   }

   interface Formula {
      int calculateNewCount(RandomSource pRandom, int pOriginalCount, int pEnchantmentLevel);

      void m_6417_(JsonObject p_79965_, JsonSerializationContext p_79966_);

      ResourceLocation getType();
   }

   interface FormulaDeserializer {
      ApplyBonusCount.Formula m_79970_(JsonObject p_79971_, JsonDeserializationContext p_79972_);
   }

   /**
    * Applies a bonus count with a special formula used for fortune ore drops.
    */
   static final class OreDrops implements ApplyBonusCount.Formula {
      public static final ResourceLocation TYPE = new ResourceLocation("ore_drops");

      public int calculateNewCount(RandomSource pRandom, int pOriginalCount, int pEnchantmentLevel) {
         if (pEnchantmentLevel > 0) {
            int i = pRandom.nextInt(pEnchantmentLevel + 2) - 1;
            if (i < 0) {
               i = 0;
            }

            return pOriginalCount * (i + 1);
         } else {
            return pOriginalCount;
         }
      }

      public void m_6417_(JsonObject p_79983_, JsonSerializationContext p_79984_) {
      }

      public static ApplyBonusCount.Formula m_79979_(JsonObject p_79980_, JsonDeserializationContext p_79981_) {
         return new ApplyBonusCount.OreDrops();
      }

      public ResourceLocation getType() {
         return TYPE;
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<ApplyBonusCount> {
      public void m_6170_(JsonObject p_79995_, ApplyBonusCount p_79996_, JsonSerializationContext p_79997_) {
         super.m_6170_(p_79995_, p_79996_, p_79997_);
         p_79995_.addProperty("enchantment", BuiltInRegistries.ENCHANTMENT.getKey(p_79996_.enchantment).toString());
         p_79995_.addProperty("formula", p_79996_.formula.getType().toString());
         JsonObject jsonobject = new JsonObject();
         p_79996_.formula.m_6417_(jsonobject, p_79997_);
         if (jsonobject.size() > 0) {
            p_79995_.add("parameters", jsonobject);
         }

      }

      public ApplyBonusCount m_6821_(JsonObject p_79991_, JsonDeserializationContext p_79992_, LootItemCondition[] p_79993_) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(p_79991_, "enchantment"));
         Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.getOptional(resourcelocation).orElseThrow(() -> {
            return new JsonParseException("Invalid enchantment id: " + resourcelocation);
         });
         ResourceLocation resourcelocation1 = new ResourceLocation(GsonHelper.getAsString(p_79991_, "formula"));
         ApplyBonusCount.FormulaDeserializer applybonuscount$formuladeserializer = ApplyBonusCount.FORMULAS.get(resourcelocation1);
         if (applybonuscount$formuladeserializer == null) {
            throw new JsonParseException("Invalid formula id: " + resourcelocation1);
         } else {
            ApplyBonusCount.Formula applybonuscount$formula;
            if (p_79991_.has("parameters")) {
               applybonuscount$formula = applybonuscount$formuladeserializer.m_79970_(GsonHelper.getAsJsonObject(p_79991_, "parameters"), p_79992_);
            } else {
               applybonuscount$formula = applybonuscount$formuladeserializer.m_79970_(new JsonObject(), p_79992_);
            }

            return new ApplyBonusCount(p_79993_, enchantment, applybonuscount$formula);
         }
      }
   }

   /**
    * Adds a bonus count based on the enchantment level scaled by a constant multiplier.
    */
   static final class UniformBonusCount implements ApplyBonusCount.Formula {
      public static final ResourceLocation TYPE = new ResourceLocation("uniform_bonus_count");
      private final int bonusMultiplier;

      public UniformBonusCount(int p_80016_) {
         this.bonusMultiplier = p_80016_;
      }

      public int calculateNewCount(RandomSource pRandom, int pOriginalCount, int pEnchantmentLevel) {
         return pOriginalCount + pRandom.nextInt(this.bonusMultiplier * pEnchantmentLevel + 1);
      }

      public void m_6417_(JsonObject p_80022_, JsonSerializationContext p_80023_) {
         p_80022_.addProperty("bonusMultiplier", this.bonusMultiplier);
      }

      public static ApplyBonusCount.Formula m_80018_(JsonObject p_80019_, JsonDeserializationContext p_80020_) {
         int i = GsonHelper.getAsInt(p_80019_, "bonusMultiplier");
         return new ApplyBonusCount.UniformBonusCount(i);
      }

      public ResourceLocation getType() {
         return TYPE;
      }
   }
}