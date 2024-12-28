package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public abstract class CustomRecipe implements CraftingRecipe {
   private final ResourceLocation f_43831_;
   private final CraftingBookCategory category;

   public CustomRecipe(ResourceLocation p_252125_, CraftingBookCategory pCategory) {
      this.f_43831_ = p_252125_;
      this.category = pCategory;
   }

   public ResourceLocation m_6423_() {
      return this.f_43831_;
   }

   /**
    * If true, this recipe does not appear in the recipe book and does not respect recipe unlocking (and the
    * doLimitedCrafting gamerule)
    */
   public boolean isSpecial() {
      return true;
   }

   public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
      return ItemStack.EMPTY;
   }

   public CraftingBookCategory category() {
      return this.category;
   }
}