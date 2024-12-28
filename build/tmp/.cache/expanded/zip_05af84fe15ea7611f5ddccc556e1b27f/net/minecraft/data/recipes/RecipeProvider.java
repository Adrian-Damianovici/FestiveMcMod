package net.minecraft.data.recipes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public abstract class RecipeProvider implements DataProvider {
   protected final PackOutput.PathProvider recipePathProvider;
   protected final PackOutput.PathProvider advancementPathProvider;
   private static final Map<BlockFamily.Variant, BiFunction<ItemLike, ItemLike, RecipeBuilder>> SHAPE_BUILDERS = ImmutableMap.<BlockFamily.Variant, BiFunction<ItemLike, ItemLike, RecipeBuilder>>builder().put(BlockFamily.Variant.BUTTON, (p_176733_, p_176734_) -> {
      return buttonBuilder(p_176733_, Ingredient.of(p_176734_));
   }).put(BlockFamily.Variant.CHISELED, (p_248037_, p_248038_) -> {
      return chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, p_248037_, Ingredient.of(p_248038_));
   }).put(BlockFamily.Variant.CUT, (p_248026_, p_248027_) -> {
      return cutBuilder(RecipeCategory.BUILDING_BLOCKS, p_248026_, Ingredient.of(p_248027_));
   }).put(BlockFamily.Variant.DOOR, (p_176714_, p_176715_) -> {
      return doorBuilder(p_176714_, Ingredient.of(p_176715_));
   }).put(BlockFamily.Variant.CUSTOM_FENCE, (p_176708_, p_176709_) -> {
      return fenceBuilder(p_176708_, Ingredient.of(p_176709_));
   }).put(BlockFamily.Variant.FENCE, (p_248031_, p_248032_) -> {
      return fenceBuilder(p_248031_, Ingredient.of(p_248032_));
   }).put(BlockFamily.Variant.CUSTOM_FENCE_GATE, (p_176698_, p_176699_) -> {
      return fenceGateBuilder(p_176698_, Ingredient.of(p_176699_));
   }).put(BlockFamily.Variant.FENCE_GATE, (p_248035_, p_248036_) -> {
      return fenceGateBuilder(p_248035_, Ingredient.of(p_248036_));
   }).put(BlockFamily.Variant.SIGN, (p_176688_, p_176689_) -> {
      return signBuilder(p_176688_, Ingredient.of(p_176689_));
   }).put(BlockFamily.Variant.SLAB, (p_248017_, p_248018_) -> {
      return slabBuilder(RecipeCategory.BUILDING_BLOCKS, p_248017_, Ingredient.of(p_248018_));
   }).put(BlockFamily.Variant.STAIRS, (p_176674_, p_176675_) -> {
      return stairBuilder(p_176674_, Ingredient.of(p_176675_));
   }).put(BlockFamily.Variant.PRESSURE_PLATE, (p_248039_, p_248040_) -> {
      return pressurePlateBuilder(RecipeCategory.REDSTONE, p_248039_, Ingredient.of(p_248040_));
   }).put(BlockFamily.Variant.POLISHED, (p_248019_, p_248020_) -> {
      return polishedBuilder(RecipeCategory.BUILDING_BLOCKS, p_248019_, Ingredient.of(p_248020_));
   }).put(BlockFamily.Variant.TRAPDOOR, (p_176638_, p_176639_) -> {
      return trapdoorBuilder(p_176638_, Ingredient.of(p_176639_));
   }).put(BlockFamily.Variant.WALL, (p_248024_, p_248025_) -> {
      return wallBuilder(RecipeCategory.DECORATIONS, p_248024_, Ingredient.of(p_248025_));
   }).build();

   public RecipeProvider(PackOutput pOutput) {
      this.recipePathProvider = pOutput.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
      this.advancementPathProvider = pOutput.createPathProvider(PackOutput.Target.DATA_PACK, "advancements");
   }

   public CompletableFuture<?> run(CachedOutput pOutput) {
      Set<ResourceLocation> set = Sets.newHashSet();
      List<CompletableFuture<?>> list = new ArrayList<>();
      this.buildRecipes((p_253413_) -> {
         if (!set.add(p_253413_.m_6445_())) {
            throw new IllegalStateException("Duplicate recipe " + p_253413_.m_6445_());
         } else {
            list.add(DataProvider.saveStable(pOutput, p_253413_.serializeRecipe(), this.recipePathProvider.json(p_253413_.m_6445_())));
            JsonObject jsonobject = p_253413_.m_5860_();
            if (jsonobject != null) {
               var saveAdvancementFuture = saveAdvancement(pOutput, p_253413_, jsonobject);
               if (saveAdvancementFuture != null)
                  list.add(saveAdvancementFuture);
            }

         }
      });
      return CompletableFuture.allOf(list.toArray((p_253414_) -> {
         return new CompletableFuture[p_253414_];
      }));
   }

   /**
    * Called every time a recipe is saved to also save the advancement JSON if it exists.
    *
    * @return A completable future that saves the advancement to disk, or null to cancel saving the advancement.
    */
   @org.jetbrains.annotations.Nullable
   protected CompletableFuture<?> saveAdvancement(CachedOutput output, FinishedRecipe finishedRecipe, JsonObject advancementJson) {
      return DataProvider.saveStable(output, advancementJson, this.advancementPathProvider.json(finishedRecipe.m_6448_()));
   }

   protected CompletableFuture<?> buildAdvancement(CachedOutput pOutput, ResourceLocation p_254102_, Advancement.Builder p_253712_) {
      return DataProvider.saveStable(pOutput, p_253712_.m_138400_(), this.advancementPathProvider.json(p_254102_));
   }

   protected abstract void buildRecipes(Consumer<FinishedRecipe> p_251297_);

   protected void generateForEnabledBlockFamilies(Consumer<FinishedRecipe> p_249188_, FeatureFlagSet pEnabledFeatures) {
      BlockFamilies.getAllFamilies().filter((p_248034_) -> {
         return p_248034_.shouldGenerateRecipe(pEnabledFeatures);
      }).forEach((p_176624_) -> {
         generateRecipes(p_249188_, p_176624_);
      });
   }

   protected static void oneToOneConversionRecipe(Consumer<FinishedRecipe> p_176552_, ItemLike pResult, ItemLike pIngredient, @Nullable String pGroup) {
      oneToOneConversionRecipe(p_176552_, pResult, pIngredient, pGroup, 1);
   }

   protected static void oneToOneConversionRecipe(Consumer<FinishedRecipe> p_176557_, ItemLike pResult, ItemLike pIngredient, @Nullable String pGroup, int pResultCount) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, pResult, pResultCount).requires(pIngredient).group(pGroup).unlockedBy(getHasName(pIngredient), has(pIngredient)).save(p_176557_, getConversionRecipeName(pResult, pIngredient));
   }

   protected static void oreSmelting(Consumer<FinishedRecipe> p_250654_, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
      oreCooking(p_250654_, RecipeSerializer.SMELTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_smelting");
   }

   protected static void oreBlasting(Consumer<FinishedRecipe> p_248775_, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
      oreCooking(p_248775_, RecipeSerializer.BLASTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
   }

   protected static void oreCooking(Consumer<FinishedRecipe> p_250791_, RecipeSerializer<? extends AbstractCookingRecipe> pSerializer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pSuffix) {
      for(ItemLike itemlike : pIngredients) {
         SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime, pSerializer).group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike)).save(p_250791_, getItemName(pResult) + pSuffix + "_" + getItemName(itemlike));
      }

   }

   protected static void netheriteSmithing(Consumer<FinishedRecipe> p_251614_, Item pIngredientItem, RecipeCategory pCategory, Item pResultItem) {
      SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(pIngredientItem), Ingredient.of(Items.NETHERITE_INGOT), pCategory, pResultItem).unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT)).save(p_251614_, getItemName(pResultItem) + "_smithing");
   }

   protected static void trimSmithing(Consumer<FinishedRecipe> p_285086_, Item pIngredientItem, ResourceLocation pLocation) {
      SmithingTrimRecipeBuilder.smithingTrim(Ingredient.of(pIngredientItem), Ingredient.of(ItemTags.TRIMMABLE_ARMOR), Ingredient.of(ItemTags.TRIM_MATERIALS), RecipeCategory.MISC).unlocks("has_smithing_trim_template", has(pIngredientItem)).save(p_285086_, pLocation);
   }

   protected static void twoByTwoPacker(Consumer<FinishedRecipe> p_248860_, RecipeCategory pCategory, ItemLike pPacked, ItemLike pUnpacked) {
      ShapedRecipeBuilder.shaped(pCategory, pPacked, 1).define('#', pUnpacked).pattern("##").pattern("##").unlockedBy(getHasName(pUnpacked), has(pUnpacked)).save(p_248860_);
   }

   protected static void threeByThreePacker(Consumer<FinishedRecipe> p_259036_, RecipeCategory pCategory, ItemLike pPacked, ItemLike pUnpacked, String pCriterionName) {
      ShapelessRecipeBuilder.shapeless(pCategory, pPacked).requires(pUnpacked, 9).unlockedBy(pCriterionName, has(pUnpacked)).save(p_259036_);
   }

   protected static void threeByThreePacker(Consumer<FinishedRecipe> p_260012_, RecipeCategory pCategory, ItemLike pPacked, ItemLike pUnpacked) {
      threeByThreePacker(p_260012_, pCategory, pPacked, pUnpacked, getHasName(pUnpacked));
   }

   protected static void planksFromLog(Consumer<FinishedRecipe> p_259712_, ItemLike pPlanks, TagKey<Item> pLogs, int pResultCount) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, pPlanks, pResultCount).requires(pLogs).group("planks").unlockedBy("has_log", has(pLogs)).save(p_259712_);
   }

   protected static void planksFromLogs(Consumer<FinishedRecipe> p_259910_, ItemLike pPlanks, TagKey<Item> pLogs, int pResult) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, pPlanks, pResult).requires(pLogs).group("planks").unlockedBy("has_logs", has(pLogs)).save(p_259910_);
   }

   protected static void woodFromLogs(Consumer<FinishedRecipe> p_126003_, ItemLike pWood, ItemLike pLog) {
      ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, pWood, 3).define('#', pLog).pattern("##").pattern("##").group("bark").unlockedBy("has_log", has(pLog)).save(p_126003_);
   }

   protected static void woodenBoat(Consumer<FinishedRecipe> p_126022_, ItemLike pBoat, ItemLike pMaterial) {
      ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, pBoat).define('#', pMaterial).pattern("# #").pattern("###").group("boat").unlockedBy("in_water", insideOf(Blocks.WATER)).save(p_126022_);
   }

   protected static void chestBoat(Consumer<FinishedRecipe> p_236372_, ItemLike pBoat, ItemLike pMaterial) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION, pBoat).requires(Blocks.CHEST).requires(pMaterial).group("chest_boat").unlockedBy("has_boat", has(ItemTags.BOATS)).save(p_236372_);
   }

   protected static RecipeBuilder buttonBuilder(ItemLike pButton, Ingredient pMaterial) {
      return ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, pButton).requires(pMaterial);
   }

   protected static RecipeBuilder doorBuilder(ItemLike pDoor, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, pDoor, 3).define('#', pMaterial).pattern("##").pattern("##").pattern("##");
   }

   protected static RecipeBuilder fenceBuilder(ItemLike pFence, Ingredient pMaterial) {
      int i = pFence == Blocks.NETHER_BRICK_FENCE ? 6 : 3;
      Item item = pFence == Blocks.NETHER_BRICK_FENCE ? Items.NETHER_BRICK : Items.STICK;
      return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pFence, i).define('W', pMaterial).define('#', item).pattern("W#W").pattern("W#W");
   }

   protected static RecipeBuilder fenceGateBuilder(ItemLike pFenceGate, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, pFenceGate).define('#', Items.STICK).define('W', pMaterial).pattern("#W#").pattern("#W#");
   }

   protected static void pressurePlate(Consumer<FinishedRecipe> p_176691_, ItemLike pPressurePlate, ItemLike pMaterial) {
      pressurePlateBuilder(RecipeCategory.REDSTONE, pPressurePlate, Ingredient.of(pMaterial)).unlockedBy(getHasName(pMaterial), has(pMaterial)).save(p_176691_);
   }

   protected static RecipeBuilder pressurePlateBuilder(RecipeCategory pCategory, ItemLike pPressurePlate, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(pCategory, pPressurePlate).define('#', pMaterial).pattern("##");
   }

   protected static void slab(Consumer<FinishedRecipe> p_248880_, RecipeCategory pCategory, ItemLike pSlab, ItemLike pMaterial) {
      slabBuilder(pCategory, pSlab, Ingredient.of(pMaterial)).unlockedBy(getHasName(pMaterial), has(pMaterial)).save(p_248880_);
   }

   protected static RecipeBuilder slabBuilder(RecipeCategory pCategory, ItemLike pSlab, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(pCategory, pSlab, 6).define('#', pMaterial).pattern("###");
   }

   protected static RecipeBuilder stairBuilder(ItemLike pStairs, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, pStairs, 4).define('#', pMaterial).pattern("#  ").pattern("## ").pattern("###");
   }

   protected static RecipeBuilder trapdoorBuilder(ItemLike pTrapdoor, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, pTrapdoor, 2).define('#', pMaterial).pattern("###").pattern("###");
   }

   protected static RecipeBuilder signBuilder(ItemLike pSign, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pSign, 3).group("sign").define('#', pMaterial).define('X', Items.STICK).pattern("###").pattern("###").pattern(" X ");
   }

   protected static void hangingSign(Consumer<FinishedRecipe> p_250663_, ItemLike pSign, ItemLike pMaterial) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pSign, 6).group("hanging_sign").define('#', pMaterial).define('X', Items.CHAIN).pattern("X X").pattern("###").pattern("###").unlockedBy("has_stripped_logs", has(pMaterial)).save(p_250663_);
   }

   protected static void colorBlockWithDye(Consumer<FinishedRecipe> p_289666_, List<Item> pDyes, List<Item> pDyeableItems, String pGroup) {
      for(int i = 0; i < pDyes.size(); ++i) {
         Item item = pDyes.get(i);
         Item item1 = pDyeableItems.get(i);
         ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, item1).requires(item).requires(Ingredient.of(pDyeableItems.stream().filter((p_288265_) -> {
            return !p_288265_.equals(item1);
         }).map(ItemStack::new))).group(pGroup).unlockedBy("has_needed_dye", has(item)).save(p_289666_, "dye_" + getItemName(item1));
      }

   }

   protected static void carpet(Consumer<FinishedRecipe> p_176717_, ItemLike pCarpet, ItemLike pMaterial) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pCarpet, 3).define('#', pMaterial).pattern("##").group("carpet").unlockedBy(getHasName(pMaterial), has(pMaterial)).save(p_176717_);
   }

   protected static void bedFromPlanksAndWool(Consumer<FinishedRecipe> p_126074_, ItemLike pBed, ItemLike pWool) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pBed).define('#', pWool).define('X', ItemTags.PLANKS).pattern("###").pattern("XXX").group("bed").unlockedBy(getHasName(pWool), has(pWool)).save(p_126074_);
   }

   protected static void banner(Consumer<FinishedRecipe> p_126082_, ItemLike pBanner, ItemLike pMaterial) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pBanner).define('#', pMaterial).define('|', Items.STICK).pattern("###").pattern("###").pattern(" | ").group("banner").unlockedBy(getHasName(pMaterial), has(pMaterial)).save(p_126082_);
   }

   protected static void stainedGlassFromGlassAndDye(Consumer<FinishedRecipe> p_126086_, ItemLike pStainedGlass, ItemLike pDye) {
      ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, pStainedGlass, 8).define('#', Blocks.GLASS).define('X', pDye).pattern("###").pattern("#X#").pattern("###").group("stained_glass").unlockedBy("has_glass", has(Blocks.GLASS)).save(p_126086_);
   }

   protected static void stainedGlassPaneFromStainedGlass(Consumer<FinishedRecipe> p_126090_, ItemLike pStainedGlassPane, ItemLike pStainedGlass) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pStainedGlassPane, 16).define('#', pStainedGlass).pattern("###").pattern("###").group("stained_glass_pane").unlockedBy("has_glass", has(pStainedGlass)).save(p_126090_);
   }

   protected static void stainedGlassPaneFromGlassPaneAndDye(Consumer<FinishedRecipe> p_126094_, ItemLike pStainedGlassPane, ItemLike pDye) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pStainedGlassPane, 8).define('#', Blocks.GLASS_PANE).define('$', pDye).pattern("###").pattern("#$#").pattern("###").group("stained_glass_pane").unlockedBy("has_glass_pane", has(Blocks.GLASS_PANE)).unlockedBy(getHasName(pDye), has(pDye)).save(p_126094_, getConversionRecipeName(pStainedGlassPane, Blocks.GLASS_PANE));
   }

   protected static void coloredTerracottaFromTerracottaAndDye(Consumer<FinishedRecipe> p_126098_, ItemLike pTerracotta, ItemLike pDye) {
      ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, pTerracotta, 8).define('#', Blocks.TERRACOTTA).define('X', pDye).pattern("###").pattern("#X#").pattern("###").group("stained_terracotta").unlockedBy("has_terracotta", has(Blocks.TERRACOTTA)).save(p_126098_);
   }

   protected static void concretePowder(Consumer<FinishedRecipe> p_126102_, ItemLike pConcretePowder, ItemLike pDye) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, pConcretePowder, 8).requires(pDye).requires(Blocks.SAND, 4).requires(Blocks.GRAVEL, 4).group("concrete_powder").unlockedBy("has_sand", has(Blocks.SAND)).unlockedBy("has_gravel", has(Blocks.GRAVEL)).save(p_126102_);
   }

   protected static void candle(Consumer<FinishedRecipe> p_176543_, ItemLike pCandle, ItemLike pDye) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, pCandle).requires(Blocks.CANDLE).requires(pDye).group("dyed_candle").unlockedBy(getHasName(pDye), has(pDye)).save(p_176543_);
   }

   protected static void wall(Consumer<FinishedRecipe> p_251034_, RecipeCategory pCategory, ItemLike pWall, ItemLike pMaterial) {
      wallBuilder(pCategory, pWall, Ingredient.of(pMaterial)).unlockedBy(getHasName(pMaterial), has(pMaterial)).save(p_251034_);
   }

   protected static RecipeBuilder wallBuilder(RecipeCategory pCategory, ItemLike pWall, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(pCategory, pWall, 6).define('#', pMaterial).pattern("###").pattern("###");
   }

   protected static void polished(Consumer<FinishedRecipe> p_251348_, RecipeCategory pCategory, ItemLike pResult, ItemLike pMaterial) {
      polishedBuilder(pCategory, pResult, Ingredient.of(pMaterial)).unlockedBy(getHasName(pMaterial), has(pMaterial)).save(p_251348_);
   }

   protected static RecipeBuilder polishedBuilder(RecipeCategory pCategory, ItemLike pResult, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(pCategory, pResult, 4).define('S', pMaterial).pattern("SS").pattern("SS");
   }

   protected static void cut(Consumer<FinishedRecipe> p_248712_, RecipeCategory pCategory, ItemLike pCutResult, ItemLike pMaterial) {
      cutBuilder(pCategory, pCutResult, Ingredient.of(pMaterial)).unlockedBy(getHasName(pMaterial), has(pMaterial)).save(p_248712_);
   }

   protected static ShapedRecipeBuilder cutBuilder(RecipeCategory pCategory, ItemLike pCutResult, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(pCategory, pCutResult, 4).define('#', pMaterial).pattern("##").pattern("##");
   }

   protected static void chiseled(Consumer<FinishedRecipe> p_250120_, RecipeCategory pCategory, ItemLike pChiseledResult, ItemLike pMaterial) {
      chiseledBuilder(pCategory, pChiseledResult, Ingredient.of(pMaterial)).unlockedBy(getHasName(pMaterial), has(pMaterial)).save(p_250120_);
   }

   protected static void mosaicBuilder(Consumer<FinishedRecipe> p_249200_, RecipeCategory pCategory, ItemLike pResult, ItemLike pMaterial) {
      ShapedRecipeBuilder.shaped(pCategory, pResult).define('#', pMaterial).pattern("#").pattern("#").unlockedBy(getHasName(pMaterial), has(pMaterial)).save(p_249200_);
   }

   protected static ShapedRecipeBuilder chiseledBuilder(RecipeCategory pCategory, ItemLike pChiseledResult, Ingredient pMaterial) {
      return ShapedRecipeBuilder.shaped(pCategory, pChiseledResult).define('#', pMaterial).pattern("#").pattern("#");
   }

   protected static void stonecutterResultFromBase(Consumer<FinishedRecipe> p_251589_, RecipeCategory pCategory, ItemLike pResult, ItemLike pMaterial) {
      stonecutterResultFromBase(p_251589_, pCategory, pResult, pMaterial, 1);
   }

   protected static void stonecutterResultFromBase(Consumer<FinishedRecipe> p_249145_, RecipeCategory pCategory, ItemLike pResult, ItemLike pMaterial, int p_251462_) {
      SingleItemRecipeBuilder.stonecutting(Ingredient.of(pMaterial), pCategory, pResult, p_251462_).unlockedBy(getHasName(pMaterial), has(pMaterial)).save(p_249145_, getConversionRecipeName(pResult, pMaterial) + "_stonecutting");
   }

   protected static void smeltingResultFromBase(Consumer<FinishedRecipe> p_176740_, ItemLike pResult, ItemLike pIngredient) {
      SimpleCookingRecipeBuilder.smelting(Ingredient.of(pIngredient), RecipeCategory.BUILDING_BLOCKS, pResult, 0.1F, 200).unlockedBy(getHasName(pIngredient), has(pIngredient)).save(p_176740_);
   }

   protected static void nineBlockStorageRecipes(Consumer<FinishedRecipe> p_249580_, RecipeCategory pUnpackedCategory, ItemLike pUnpacked, RecipeCategory pPackedCategory, ItemLike pPacked) {
      nineBlockStorageRecipes(p_249580_, pUnpackedCategory, pUnpacked, pPackedCategory, pPacked, getSimpleRecipeName(pPacked), (String)null, getSimpleRecipeName(pUnpacked), (String)null);
   }

   protected static void nineBlockStorageRecipesWithCustomPacking(Consumer<FinishedRecipe> p_250488_, RecipeCategory pUnpackedCategory, ItemLike pUnpacked, RecipeCategory pPackedCategory, ItemLike pPacked, String pPackedName, String pPackedGroup) {
      nineBlockStorageRecipes(p_250488_, pUnpackedCategory, pUnpacked, pPackedCategory, pPacked, pPackedName, pPackedGroup, getSimpleRecipeName(pUnpacked), (String)null);
   }

   protected static void nineBlockStorageRecipesRecipesWithCustomUnpacking(Consumer<FinishedRecipe> p_250320_, RecipeCategory pUnpackedCategory, ItemLike pUnpacked, RecipeCategory pPackedCategory, ItemLike pPacked, String pUnpackedName, String pUnpackedGroup) {
      nineBlockStorageRecipes(p_250320_, pUnpackedCategory, pUnpacked, pPackedCategory, pPacked, getSimpleRecipeName(pPacked), (String)null, pUnpackedName, pUnpackedGroup);
   }

   protected static void nineBlockStorageRecipes(Consumer<FinishedRecipe> p_250423_, RecipeCategory pUnpackedCategory, ItemLike pUnpacked, RecipeCategory pPackedCategory, ItemLike pPacked, String pPackedName, @Nullable String pPackedGroup, String pUnpackedName, @Nullable String pUnpackedGroup) {
      ShapelessRecipeBuilder.shapeless(pUnpackedCategory, pUnpacked, 9).requires(pPacked).group(pUnpackedGroup).unlockedBy(getHasName(pPacked), has(pPacked)).save(p_250423_, new ResourceLocation(pUnpackedName));
      ShapedRecipeBuilder.shaped(pPackedCategory, pPacked).define('#', pUnpacked).pattern("###").pattern("###").pattern("###").group(pPackedGroup).unlockedBy(getHasName(pUnpacked), has(pUnpacked)).save(p_250423_, new ResourceLocation(pPackedName));
   }

   protected static void copySmithingTemplate(Consumer<FinishedRecipe> p_267061_, ItemLike pTemplate, TagKey<Item> p_267283_) {
      ShapedRecipeBuilder.shaped(RecipeCategory.MISC, pTemplate, 2).define('#', Items.DIAMOND).define('C', p_267283_).define('S', pTemplate).pattern("#S#").pattern("#C#").pattern("###").unlockedBy(getHasName(pTemplate), has(pTemplate)).save(p_267061_);
   }

   protected static void copySmithingTemplate(Consumer<FinishedRecipe> p_266734_, ItemLike pTemplate, ItemLike p_267023_) {
      ShapedRecipeBuilder.shaped(RecipeCategory.MISC, pTemplate, 2).define('#', Items.DIAMOND).define('C', p_267023_).define('S', pTemplate).pattern("#S#").pattern("#C#").pattern("###").unlockedBy(getHasName(pTemplate), has(pTemplate)).save(p_266734_);
   }

   protected static void cookRecipes(Consumer<FinishedRecipe> p_126007_, String pCookingMethod, RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, int pCookingTime) {
      simpleCookingRecipe(p_126007_, pCookingMethod, pCookingSerializer, pCookingTime, Items.BEEF, Items.COOKED_BEEF, 0.35F);
      simpleCookingRecipe(p_126007_, pCookingMethod, pCookingSerializer, pCookingTime, Items.CHICKEN, Items.COOKED_CHICKEN, 0.35F);
      simpleCookingRecipe(p_126007_, pCookingMethod, pCookingSerializer, pCookingTime, Items.COD, Items.COOKED_COD, 0.35F);
      simpleCookingRecipe(p_126007_, pCookingMethod, pCookingSerializer, pCookingTime, Items.KELP, Items.DRIED_KELP, 0.1F);
      simpleCookingRecipe(p_126007_, pCookingMethod, pCookingSerializer, pCookingTime, Items.SALMON, Items.COOKED_SALMON, 0.35F);
      simpleCookingRecipe(p_126007_, pCookingMethod, pCookingSerializer, pCookingTime, Items.MUTTON, Items.COOKED_MUTTON, 0.35F);
      simpleCookingRecipe(p_126007_, pCookingMethod, pCookingSerializer, pCookingTime, Items.PORKCHOP, Items.COOKED_PORKCHOP, 0.35F);
      simpleCookingRecipe(p_126007_, pCookingMethod, pCookingSerializer, pCookingTime, Items.POTATO, Items.BAKED_POTATO, 0.35F);
      simpleCookingRecipe(p_126007_, pCookingMethod, pCookingSerializer, pCookingTime, Items.RABBIT, Items.COOKED_RABBIT, 0.35F);
   }

   protected static void simpleCookingRecipe(Consumer<FinishedRecipe> p_249398_, String pCookingMethod, RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, int pCookingTime, ItemLike pMaterial, ItemLike pResult, float pExperience) {
      SimpleCookingRecipeBuilder.generic(Ingredient.of(pMaterial), RecipeCategory.FOOD, pResult, pExperience, pCookingTime, pCookingSerializer).unlockedBy(getHasName(pMaterial), has(pMaterial)).save(p_249398_, getItemName(pResult) + "_from_" + pCookingMethod);
   }

   protected static void waxRecipes(Consumer<FinishedRecipe> p_176611_) {
      HoneycombItem.WAXABLES.get().forEach((p_248022_, p_248023_) -> {
         ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, p_248023_).requires(p_248022_).requires(Items.HONEYCOMB).group(getItemName(p_248023_)).unlockedBy(getHasName(p_248022_), has(p_248022_)).save(p_176611_, getConversionRecipeName(p_248023_, Items.HONEYCOMB));
      });
   }

   protected static void generateRecipes(Consumer<FinishedRecipe> p_176581_, BlockFamily pBlockFamily) {
      pBlockFamily.getVariants().forEach((p_176529_, p_176530_) -> {
         BiFunction<ItemLike, ItemLike, RecipeBuilder> bifunction = SHAPE_BUILDERS.get(p_176529_);
         ItemLike itemlike = getBaseBlock(pBlockFamily, p_176529_);
         if (bifunction != null) {
            RecipeBuilder recipebuilder = bifunction.apply(p_176530_, itemlike);
            pBlockFamily.getRecipeGroupPrefix().ifPresent((p_176601_) -> {
               recipebuilder.group(p_176601_ + (p_176529_ == BlockFamily.Variant.CUT ? "" : "_" + p_176529_.m_176020_()));
            });
            recipebuilder.unlockedBy(pBlockFamily.getRecipeUnlockedBy().orElseGet(() -> {
               return getHasName(itemlike);
            }), has(itemlike));
            recipebuilder.save(p_176581_);
         }

         if (p_176529_ == BlockFamily.Variant.CRACKED) {
            smeltingResultFromBase(p_176581_, p_176530_, itemlike);
         }

      });
   }

   protected static Block getBaseBlock(BlockFamily pFamily, BlockFamily.Variant pVariant) {
      if (pVariant == BlockFamily.Variant.CHISELED) {
         if (!pFamily.getVariants().containsKey(BlockFamily.Variant.SLAB)) {
            throw new IllegalStateException("Slab is not defined for the family.");
         } else {
            return pFamily.get(BlockFamily.Variant.SLAB);
         }
      } else {
         return pFamily.getBaseBlock();
      }
   }

   protected static EnterBlockTrigger.TriggerInstance insideOf(Block pBlock) {
      return new EnterBlockTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, pBlock, StatePropertiesPredicate.f_67658_);
   }

   protected static InventoryChangeTrigger.TriggerInstance has(MinMaxBounds.Ints pCount, ItemLike pItem) {
      return inventoryTrigger(ItemPredicate.Builder.item().of(pItem).withCount(pCount).build());
   }

   protected static InventoryChangeTrigger.TriggerInstance has(ItemLike p_125978_) {
      return inventoryTrigger(ItemPredicate.Builder.item().of(p_125978_).build());
   }

   protected static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> p_206407_) {
      return inventoryTrigger(ItemPredicate.Builder.item().of(p_206407_).build());
   }

   protected static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate... p_126012_) {
      return new InventoryChangeTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, p_126012_);
   }

   protected static String getHasName(ItemLike pItemLike) {
      return "has_" + getItemName(pItemLike);
   }

   protected static String getItemName(ItemLike pItemLike) {
      return BuiltInRegistries.ITEM.getKey(pItemLike.asItem()).getPath();
   }

   protected static String getSimpleRecipeName(ItemLike pItemLike) {
      return getItemName(pItemLike);
   }

   protected static String getConversionRecipeName(ItemLike pResult, ItemLike pIngredient) {
      return getItemName(pResult) + "_from_" + getItemName(pIngredient);
   }

   protected static String getSmeltingRecipeName(ItemLike pItemLike) {
      return getItemName(pItemLike) + "_from_smelting";
   }

   protected static String getBlastingRecipeName(ItemLike pItemLike) {
      return getItemName(pItemLike) + "_from_blasting";
   }

   /**
    * Gets a name for this provider, to use in logging.
    */
   public final String getName() {
      return "Recipes";
   }
}
