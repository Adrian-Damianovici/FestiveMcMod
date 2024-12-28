package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.slf4j.Logger;

public class LootDataManager implements PreparableReloadListener, LootDataResolver {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final LootDataId<LootTable> EMPTY_LOOT_TABLE_KEY = new LootDataId<>(LootDataType.TABLE, BuiltInLootTables.EMPTY);
   private Map<LootDataId<?>, ?> elements = Map.of();
   private Multimap<LootDataType<?>, ResourceLocation> typeKeys = ImmutableMultimap.of();

   public final CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier pPreparationBarrier, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
      Map<LootDataType<?>, Map<ResourceLocation, ?>> map = new HashMap<>();
      CompletableFuture<?>[] completablefuture = LootDataType.values().map((p_279242_) -> {
         return scheduleElementParse(p_279242_, pResourceManager, pBackgroundExecutor, map);
      }).toArray((p_279126_) -> {
         return new CompletableFuture[p_279126_];
      });
      return CompletableFuture.allOf(completablefuture).thenCompose(pPreparationBarrier::wait).thenAcceptAsync((p_279096_) -> {
         this.apply(map);
      }, pGameExecutor);
   }

   private static <T> CompletableFuture<?> scheduleElementParse(LootDataType<T> pLootDataType, ResourceManager pResourceManager, Executor pBackgroundExecutor, Map<LootDataType<?>, Map<ResourceLocation, ?>> pElementCollector) {
      Map<ResourceLocation, T> map = new HashMap<>();
      pElementCollector.put(pLootDataType, map);
      return CompletableFuture.runAsync(() -> {
         Map<ResourceLocation, JsonElement> map1 = new HashMap<>();
         SimpleJsonResourceReloadListener.scanDirectory(pResourceManager, pLootDataType.directory(), pLootDataType.m_278857_(), map1);
         map1.forEach((p_279416_, p_279151_) -> {
            pLootDataType.deserialize(p_279416_, p_279151_, pResourceManager).ifPresent((p_279295_) -> {
               map.put(p_279416_, p_279295_);
            });
         });
      }, pBackgroundExecutor);
   }

   private void apply(Map<LootDataType<?>, Map<ResourceLocation, ?>> pCollectedElements) {
      Object object = pCollectedElements.get(LootDataType.TABLE).remove(BuiltInLootTables.EMPTY);
      if (object != null) {
         LOGGER.warn("Datapack tried to redefine {} loot table, ignoring", (Object)BuiltInLootTables.EMPTY);
      }

      ImmutableMap.Builder<LootDataId<?>, Object> builder = ImmutableMap.builder();
      ImmutableMultimap.Builder<LootDataType<?>, ResourceLocation> builder1 = ImmutableMultimap.builder();
      pCollectedElements.forEach((p_279449_, p_279262_) -> {
         p_279262_.forEach((p_279130_, p_279313_) -> {
            builder.put(new LootDataId(p_279449_, p_279130_), p_279313_);
            builder1.put(p_279449_, p_279130_);
         });
      });
      builder.put(EMPTY_LOOT_TABLE_KEY, LootTable.EMPTY);
      final Map<LootDataId<?>, ?> map = builder.build();
      ValidationContext validationcontext = new ValidationContext(LootContextParamSets.ALL_PARAMS, new LootDataResolver() {
         @Nullable
         public <T> T getElement(LootDataId<T> p_279194_) {
            return (T)map.get(p_279194_);
         }
      });
      map.forEach((p_279387_, p_279087_) -> {
         castAndValidate(validationcontext, p_279387_, p_279087_);
      });
      validationcontext.getProblems().forEach((p_279487_, p_279312_) -> {
         LOGGER.warn("Found loot table element validation problem in {}: {}", p_279487_, p_279312_);
      });
      this.elements = map;
      this.typeKeys = builder1.build();
   }

   private static <T> void castAndValidate(ValidationContext pContext, LootDataId<T> pId, Object pElement) {
      pId.type().runValidation(pContext, pId, (T)pElement);
   }

   @Nullable
   public <T> T getElement(LootDataId<T> pId) {
      return (T)this.elements.get(pId);
   }

   public Collection<ResourceLocation> getKeys(LootDataType<?> pType) {
      return this.typeKeys.get(pType);
   }

   public static LootItemCondition m_278755_(LootItemCondition[] p_279415_) {
      return new LootDataManager.CompositePredicate(p_279415_);
   }

   public static LootItemFunction m_278704_(LootItemFunction[] p_279450_) {
      return new LootDataManager.FunctionSequence(p_279450_);
   }

   static class CompositePredicate implements LootItemCondition {
      private final LootItemCondition[] f_278503_;
      private final Predicate<LootContext> f_278433_;

      CompositePredicate(LootItemCondition[] p_279376_) {
         this.f_278503_ = p_279376_;
         this.f_278433_ = LootItemConditions.andConditions(p_279376_);
      }

      public final boolean test(LootContext p_279232_) {
         return this.f_278433_.test(p_279232_);
      }

      /**
       * Validate that this object is used correctly according to the given ValidationContext.
       */
      public void validate(ValidationContext p_279208_) {
         LootItemCondition.super.validate(p_279208_);

         for(int i = 0; i < this.f_278503_.length; ++i) {
            this.f_278503_[i].validate(p_279208_.forChild(".term[" + i + "]"));
         }

      }

      public LootItemConditionType getType() {
         throw new UnsupportedOperationException();
      }
   }

   static class FunctionSequence implements LootItemFunction {
      protected final LootItemFunction[] f_278417_;
      private final BiFunction<ItemStack, LootContext, ItemStack> f_278409_;

      public FunctionSequence(LootItemFunction[] p_279339_) {
         this.f_278417_ = p_279339_;
         this.f_278409_ = LootItemFunctions.compose(p_279339_);
      }

      public ItemStack apply(ItemStack p_279166_, LootContext p_279343_) {
         return this.f_278409_.apply(p_279166_, p_279343_);
      }

      /**
       * Validate that this object is used correctly according to the given ValidationContext.
       */
      public void validate(ValidationContext p_279400_) {
         LootItemFunction.super.validate(p_279400_);

         for(int i = 0; i < this.f_278417_.length; ++i) {
            this.f_278417_[i].validate(p_279400_.forChild(".function[" + i + "]"));
         }

      }

      public LootItemFunctionType getType() {
         throw new UnsupportedOperationException();
      }
   }
}
