package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SpecialRecipeBuilder extends CraftingRecipeBuilder {
   final RecipeSerializer<?> serializer;

   public SpecialRecipeBuilder(RecipeSerializer<?> pSerializer) {
      this.serializer = pSerializer;
   }

   public static SpecialRecipeBuilder special(RecipeSerializer<? extends CraftingRecipe> pSerializer) {
      return new SpecialRecipeBuilder(pSerializer);
   }

   public void save(Consumer<FinishedRecipe> p_126360_, final String p_126361_) {
      p_126360_.accept(new CraftingRecipeBuilder.CraftingResult(CraftingBookCategory.MISC) {
         public RecipeSerializer<?> m_6637_() {
            return SpecialRecipeBuilder.this.serializer;
         }

         public ResourceLocation m_6445_() {
            return new ResourceLocation(p_126361_);
         }

         @Nullable
         public JsonObject m_5860_() {
            return null;
         }

         public ResourceLocation m_6448_() {
            return new ResourceLocation("");
         }
      });
   }
}