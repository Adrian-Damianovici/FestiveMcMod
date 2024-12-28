package net.minecraft.data.recipes;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public interface RecipeBuilder {
   ResourceLocation ROOT_RECIPE_ADVANCEMENT = new ResourceLocation("recipes/root");

   RecipeBuilder unlockedBy(String pName, CriterionTriggerInstance p_176497_);

   RecipeBuilder group(@Nullable String pGroupName);

   Item getResult();

   void save(Consumer<FinishedRecipe> p_176503_, ResourceLocation pId);

   default void save(Consumer<FinishedRecipe> p_176499_) {
      this.save(p_176499_, getDefaultRecipeId(this.getResult()));
   }

   default void save(Consumer<FinishedRecipe> p_176501_, String pId) {
      ResourceLocation resourcelocation = getDefaultRecipeId(this.getResult());
      ResourceLocation resourcelocation1 = new ResourceLocation(pId);
      if (resourcelocation1.equals(resourcelocation)) {
         throw new IllegalStateException("Recipe " + pId + " should remove its 'save' argument as it is equal to default one");
      } else {
         this.save(p_176501_, resourcelocation1);
      }
   }

   static ResourceLocation getDefaultRecipeId(ItemLike pItemLike) {
      return BuiltInRegistries.ITEM.getKey(pItemLike.asItem());
   }
}