package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class BlastingRecipe extends AbstractCookingRecipe {
   public BlastingRecipe(ResourceLocation p_249728_, String pGroup, CookingBookCategory pCategory, Ingredient pIngredient, ItemStack pResult, float pExperience, int pCookingTime) {
      super(RecipeType.BLASTING, p_249728_, pGroup, pCategory, pIngredient, pResult, pExperience, pCookingTime);
   }

   public ItemStack getToastSymbol() {
      return new ItemStack(Blocks.BLAST_FURNACE);
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.BLASTING_RECIPE;
   }
}