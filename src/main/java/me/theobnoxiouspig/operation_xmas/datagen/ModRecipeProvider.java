package me.theobnoxiouspig.operation_xmas.datagen;

import me.theobnoxiouspig.operation_xmas.Operation_xmas;
import me.theobnoxiouspig.operation_xmas.block.ModBlocks;
import me.theobnoxiouspig.operation_xmas.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    private static final List<ItemLike> SAPPHIRE_SMELTABLES = List.of(ModBlocks.NETHER_SAPPHIRE_ORE.get(),
            ModBlocks.SAPPHIRE_ORE.get());
    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        oreSmelting(pWriter, SAPPHIRE_SMELTABLES, RecipeCategory.MISC, ModItems.ANCIENT_GEM.get(), 0.25f, 300, "sapphire");
        oreBlasting(pWriter, SAPPHIRE_SMELTABLES, RecipeCategory.MISC, ModItems.ANCIENT_GEM.get(), 0.25f, 150, "sapphire");
        simpleCookingRecipe(pWriter, "campfire_cooking", RecipeSerializer.CAMPFIRE_COOKING_RECIPE, 600, Blocks.NETHERITE_BLOCK, ModItems.XMAS_PRESENT.get(), 0.65f);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BRASS_BLOCK.get())
                .define('#', Blocks.GOLD_BLOCK)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .unlockedBy(getHasName(ModBlocks.BRASS_BLOCK.get()), has(ModBlocks.BRASS_BLOCK.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Blocks.GOLD_BLOCK, 9)
                .requires(ModBlocks.BRASS_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.BRASS_BLOCK.get()), has(ModBlocks.BRASS_BLOCK.get()))
                .save(pWriter);
    }

    protected static void oreSmelting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.SMELTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.BLASTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static void oreCooking(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeSerializer<? extends AbstractCookingRecipe> pSerializer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pSuffix) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult,
                    pExperience, pCookingTime, pSerializer)
                    .group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(pFinishedRecipeConsumer, Operation_xmas.MOD_ID + ":" + getItemName(pResult) + pSuffix + "_" + getItemName(itemlike));
        }
    }
}