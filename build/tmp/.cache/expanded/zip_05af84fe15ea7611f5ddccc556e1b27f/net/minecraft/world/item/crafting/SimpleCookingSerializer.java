package net.minecraft.world.item.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class SimpleCookingSerializer<T extends AbstractCookingRecipe> implements RecipeSerializer<T> {
   private final int f_44327_;
   private final SimpleCookingSerializer.CookieBaker<T> factory;

   public SimpleCookingSerializer(SimpleCookingSerializer.CookieBaker<T> pFactory, int pDefaultCookingTime) {
      this.f_44327_ = pDefaultCookingTime;
      this.factory = pFactory;
   }

   public T m_6729_(ResourceLocation p_44347_, JsonObject p_44348_) {
      String s = GsonHelper.getAsString(p_44348_, "group", "");
      CookingBookCategory cookingbookcategory = CookingBookCategory.CODEC.byName(GsonHelper.getAsString(p_44348_, "category", (String)null), CookingBookCategory.MISC);
      JsonElement jsonelement = (JsonElement)(GsonHelper.isArrayNode(p_44348_, "ingredient") ? GsonHelper.getAsJsonArray(p_44348_, "ingredient") : GsonHelper.getAsJsonObject(p_44348_, "ingredient"));
      Ingredient ingredient = Ingredient.m_288218_(jsonelement, false);
      //Forge: Check if primitive string to keep vanilla or a object which can contain a count field.
      if (!p_44348_.has("result")) throw new com.google.gson.JsonSyntaxException("Missing result, expected to find a string or object");
      ItemStack itemstack;
      if (p_44348_.get("result").isJsonObject()) itemstack = ShapedRecipe.m_151274_(GsonHelper.getAsJsonObject(p_44348_, "result"));
      else {
      String s1 = GsonHelper.getAsString(p_44348_, "result");
      ResourceLocation resourcelocation = new ResourceLocation(s1);
      itemstack = new ItemStack(BuiltInRegistries.ITEM.getOptional(resourcelocation).orElseThrow(() -> {
         return new IllegalStateException("Item: " + s1 + " does not exist");
      }));
      }
      float f = GsonHelper.getAsFloat(p_44348_, "experience", 0.0F);
      int i = GsonHelper.getAsInt(p_44348_, "cookingtime", this.f_44327_);
      return this.factory.create(p_44347_, s, cookingbookcategory, ingredient, itemstack, f, i);
   }

   public T fromNetwork(ResourceLocation p_44350_, FriendlyByteBuf pBuffer) {
      String s = pBuffer.readUtf();
      CookingBookCategory cookingbookcategory = pBuffer.readEnum(CookingBookCategory.class);
      Ingredient ingredient = Ingredient.fromNetwork(pBuffer);
      ItemStack itemstack = pBuffer.readItem();
      float f = pBuffer.readFloat();
      int i = pBuffer.readVarInt();
      return this.factory.create(p_44350_, s, cookingbookcategory, ingredient, itemstack, f, i);
   }

   public void toNetwork(FriendlyByteBuf pBuffer, T pRecipe) {
      pBuffer.writeUtf(pRecipe.group);
      pBuffer.writeEnum(pRecipe.category());
      pRecipe.ingredient.toNetwork(pBuffer);
      pBuffer.writeItem(pRecipe.result);
      pBuffer.writeFloat(pRecipe.experience);
      pBuffer.writeVarInt(pRecipe.cookingTime);
   }

   interface CookieBaker<T extends AbstractCookingRecipe> {
      T create(ResourceLocation p_44353_, String pGroup, CookingBookCategory pCategory, Ingredient pIngredient, ItemStack pResult, float pExperience, int pCookingTime);
   }
}
