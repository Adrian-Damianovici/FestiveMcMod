package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public abstract class SingleItemRecipe implements Recipe<Container> {
   protected final Ingredient ingredient;
   protected final ItemStack result;
   private final RecipeType<?> type;
   private final RecipeSerializer<?> serializer;
   protected final ResourceLocation f_44411_;
   protected final String group;

   public SingleItemRecipe(RecipeType<?> pType, RecipeSerializer<?> pSerializer, ResourceLocation p_44418_, String pGroup, Ingredient pIngredient, ItemStack pResult) {
      this.type = pType;
      this.serializer = pSerializer;
      this.f_44411_ = p_44418_;
      this.group = pGroup;
      this.ingredient = pIngredient;
      this.result = pResult;
   }

   public RecipeType<?> getType() {
      return this.type;
   }

   public RecipeSerializer<?> getSerializer() {
      return this.serializer;
   }

   public ResourceLocation m_6423_() {
      return this.f_44411_;
   }

   /**
    * Recipes with equal group are combined into one button in the recipe book
    */
   public String getGroup() {
      return this.group;
   }

   public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
      return this.result;
   }

   public NonNullList<Ingredient> getIngredients() {
      NonNullList<Ingredient> nonnulllist = NonNullList.create();
      nonnulllist.add(this.ingredient);
      return nonnulllist;
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return true;
   }

   public ItemStack assemble(Container pContainer, RegistryAccess pRegistryAccess) {
      return this.result.copy();
   }

   public static class Serializer<T extends SingleItemRecipe> implements RecipeSerializer<T> {
      final SingleItemRecipe.Serializer.SingleItemMaker<T> factory;

      protected Serializer(SingleItemRecipe.Serializer.SingleItemMaker<T> pFactory) {
         this.factory = pFactory;
      }

      public T m_6729_(ResourceLocation p_44449_, JsonObject p_44450_) {
         String s = GsonHelper.getAsString(p_44450_, "group", "");
         Ingredient ingredient;
         if (GsonHelper.isArrayNode(p_44450_, "ingredient")) {
            ingredient = Ingredient.m_288218_(GsonHelper.getAsJsonArray(p_44450_, "ingredient"), false);
         } else {
            ingredient = Ingredient.m_288218_(GsonHelper.getAsJsonObject(p_44450_, "ingredient"), false);
         }

         String s1 = GsonHelper.getAsString(p_44450_, "result");
         int i = GsonHelper.getAsInt(p_44450_, "count");
         ItemStack itemstack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(s1)), i);
         return this.factory.create(p_44449_, s, ingredient, itemstack);
      }

      public T fromNetwork(ResourceLocation p_44452_, FriendlyByteBuf p_44453_) {
         String s = p_44453_.readUtf();
         Ingredient ingredient = Ingredient.fromNetwork(p_44453_);
         ItemStack itemstack = p_44453_.readItem();
         return this.factory.create(p_44452_, s, ingredient, itemstack);
      }

      public void toNetwork(FriendlyByteBuf pBuffer, T pRecipe) {
         pBuffer.writeUtf(pRecipe.group);
         pRecipe.ingredient.toNetwork(pBuffer);
         pBuffer.writeItem(pRecipe.result);
      }

      interface SingleItemMaker<T extends SingleItemRecipe> {
         T create(ResourceLocation p_44455_, String pGroup, Ingredient pIngredient, ItemStack p_44458_);
      }
   }
}