package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public interface FinishedRecipe {
   void serializeRecipeData(JsonObject pJson);

   /**
    * Gets the JSON for the recipe.
    */
   default JsonObject serializeRecipe() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("type", BuiltInRegistries.RECIPE_SERIALIZER.getKey(this.m_6637_()).toString());
      this.serializeRecipeData(jsonobject);
      return jsonobject;
   }

   ResourceLocation m_6445_();

   RecipeSerializer<?> m_6637_();

   @Nullable
   JsonObject m_5860_();

   @Nullable
   ResourceLocation m_6448_();
}