package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class SimpleCraftingRecipeSerializer<T extends CraftingRecipe> implements RecipeSerializer<T> {
   private final SimpleCraftingRecipeSerializer.Factory<T> constructor;

   public SimpleCraftingRecipeSerializer(SimpleCraftingRecipeSerializer.Factory<T> pConstructor) {
      this.constructor = pConstructor;
   }

   public T m_6729_(ResourceLocation p_249786_, JsonObject p_252161_) {
      CraftingBookCategory craftingbookcategory = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(p_252161_, "category", (String)null), CraftingBookCategory.MISC);
      return this.constructor.create(p_249786_, craftingbookcategory);
   }

   public T fromNetwork(ResourceLocation p_251508_, FriendlyByteBuf pBuffer) {
      CraftingBookCategory craftingbookcategory = pBuffer.readEnum(CraftingBookCategory.class);
      return this.constructor.create(p_251508_, craftingbookcategory);
   }

   public void toNetwork(FriendlyByteBuf pBuffer, T pRecipe) {
      pBuffer.writeEnum(pRecipe.category());
   }

   @FunctionalInterface
   public interface Factory<T extends CraftingRecipe> {
      T create(ResourceLocation p_250892_, CraftingBookCategory pCategory);
   }
}