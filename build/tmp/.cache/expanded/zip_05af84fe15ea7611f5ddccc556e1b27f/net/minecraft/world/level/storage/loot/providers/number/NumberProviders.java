package net.minecraft.world.level.storage.loot.providers.number;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;

/**
 * Registration for {@link LootNumberProviderType}.
 * 
 * @see LootNumberProviderType
 * @see NumberProvider
 */
public class NumberProviders {
   public static final LootNumberProviderType CONSTANT = register("constant", new ConstantValue.Serializer());
   public static final LootNumberProviderType UNIFORM = register("uniform", new UniformGenerator.Serializer());
   public static final LootNumberProviderType BINOMIAL = register("binomial", new BinomialDistributionGenerator.Serializer());
   public static final LootNumberProviderType SCORE = register("score", new ScoreboardValue.Serializer());

   private static LootNumberProviderType register(String pName, Serializer<? extends NumberProvider> p_165740_) {
      return Registry.register(BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE, new ResourceLocation(pName), new LootNumberProviderType(p_165740_));
   }

   public static Object m_165737_() {
      return GsonAdapterFactory.m_78801_(BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE, "provider", "type", NumberProvider::getType).m_164986_(CONSTANT, new ConstantValue.InlineSerializer()).m_164984_(UNIFORM).m_78822_();
   }
}