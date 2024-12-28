package net.minecraft.world.inventory;

import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.crafting.Recipe;

public abstract class RecipeBookMenu<C extends Container> extends AbstractContainerMenu {
   public RecipeBookMenu(MenuType<?> pMenuType, int pContainerId) {
      super(pMenuType, pContainerId);
   }

   public void handlePlacement(boolean pPlaceAll, Recipe<?> p_40120_, ServerPlayer pPlayer) {
      new ServerPlaceRecipe(this).recipeClicked(pPlayer, p_40120_, pPlaceAll);
   }

   public abstract void fillCraftSlotsStackedContents(StackedContents pItemHelper);

   public abstract void clearCraftingContent();

   public abstract boolean recipeMatches(Recipe<? super C> p_40118_);

   public abstract int getResultSlotIndex();

   public abstract int getGridWidth();

   public abstract int getGridHeight();

   public abstract int getSize();

   public java.util.List<net.minecraft.client.RecipeBookCategories> getRecipeBookCategories() {
       return net.minecraft.client.RecipeBookCategories.getCategories(this.getRecipeBookType());
   }

   public abstract RecipeBookType getRecipeBookType();

   public abstract boolean shouldMoveToInventory(int pSlotIndex);
}
