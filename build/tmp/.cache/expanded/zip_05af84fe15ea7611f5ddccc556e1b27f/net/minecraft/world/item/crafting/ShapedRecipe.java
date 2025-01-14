package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ShapedRecipe implements CraftingRecipe, net.minecraftforge.common.crafting.IShapedRecipe<CraftingContainer> {
   static int MAX_WIDTH = 3;
   static int MAX_HEIGHT = 3;
   /**
    * Expand the max width and height allowed in the deserializer.
    * This should be called by modders who add custom crafting tables that are larger than the vanilla 3x3.
    * @param width your max recipe width
    * @param height your max recipe height
    */
   public static void setCraftingSize(int width, int height) {
      if (MAX_WIDTH < width) MAX_WIDTH = width;
      if (MAX_HEIGHT < height) MAX_HEIGHT = height;
   }

   final int width;
   final int height;
   final NonNullList<Ingredient> recipeItems;
   final ItemStack result;
   private final ResourceLocation f_44150_;
   final String group;
   final CraftingBookCategory category;
   final boolean showNotification;

   public ShapedRecipe(ResourceLocation p_273203_, String pGroup, CraftingBookCategory pCategory, int pWidth, int pHeight, NonNullList<Ingredient> pRecipeItems, ItemStack pResult, boolean pShowNotification) {
      this.f_44150_ = p_273203_;
      this.group = pGroup;
      this.category = pCategory;
      this.width = pWidth;
      this.height = pHeight;
      this.recipeItems = pRecipeItems;
      this.result = pResult;
      this.showNotification = pShowNotification;
   }

   public ShapedRecipe(ResourceLocation p_250963_, String pGroup, CraftingBookCategory pCategory, int pWidth, int pHeight, NonNullList<Ingredient> pRecipeItems, ItemStack pResult) {
      this(p_250963_, pGroup, pCategory, pWidth, pHeight, pRecipeItems, pResult, true);
   }

   public ResourceLocation m_6423_() {
      return this.f_44150_;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.SHAPED_RECIPE;
   }

   /**
    * Recipes with equal group are combined into one button in the recipe book
    */
   public String getGroup() {
      return this.group;
   }

   public CraftingBookCategory category() {
      return this.category;
   }

   public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
      return this.result;
   }

   public NonNullList<Ingredient> getIngredients() {
      return this.recipeItems;
   }

   public boolean showNotification() {
      return this.showNotification;
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth >= this.width && pHeight >= this.height;
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(CraftingContainer pInv, Level pLevel) {
      for(int i = 0; i <= pInv.getWidth() - this.width; ++i) {
         for(int j = 0; j <= pInv.getHeight() - this.height; ++j) {
            if (this.matches(pInv, i, j, true)) {
               return true;
            }

            if (this.matches(pInv, i, j, false)) {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Checks if the region of a crafting inventory is match for the recipe.
    */
   private boolean matches(CraftingContainer pCraftingInventory, int pWidth, int pHeight, boolean pMirrored) {
      for(int i = 0; i < pCraftingInventory.getWidth(); ++i) {
         for(int j = 0; j < pCraftingInventory.getHeight(); ++j) {
            int k = i - pWidth;
            int l = j - pHeight;
            Ingredient ingredient = Ingredient.EMPTY;
            if (k >= 0 && l >= 0 && k < this.width && l < this.height) {
               if (pMirrored) {
                  ingredient = this.recipeItems.get(this.width - k - 1 + l * this.width);
               } else {
                  ingredient = this.recipeItems.get(k + l * this.width);
               }
            }

            if (!ingredient.test(pCraftingInventory.getItem(i + j * pCraftingInventory.getWidth()))) {
               return false;
            }
         }
      }

      return true;
   }

   public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
      return this.getResultItem(pRegistryAccess).copy();
   }

   public int getWidth() {
      return this.width;
   }

   @Override
   public int getRecipeWidth() {
      return getWidth();
   }

   public int getHeight() {
      return this.height;
   }

   @Override
   public int getRecipeHeight() {
      return getHeight();
   }

   static NonNullList<Ingredient> m_44202_(String[] p_44203_, Map<String, Ingredient> p_44204_, int p_44205_, int p_44206_) {
      NonNullList<Ingredient> nonnulllist = NonNullList.withSize(p_44205_ * p_44206_, Ingredient.EMPTY);
      Set<String> set = Sets.newHashSet(p_44204_.keySet());
      set.remove(" ");

      for(int i = 0; i < p_44203_.length; ++i) {
         for(int j = 0; j < p_44203_[i].length(); ++j) {
            String s = p_44203_[i].substring(j, j + 1);
            Ingredient ingredient = p_44204_.get(s);
            if (ingredient == null) {
               throw new JsonSyntaxException("Pattern references symbol '" + s + "' but it's not defined in the key");
            }

            set.remove(s);
            nonnulllist.set(j + p_44205_ * i, ingredient);
         }
      }

      if (!set.isEmpty()) {
         throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + set);
      } else {
         return nonnulllist;
      }
   }

   @VisibleForTesting
   static String[] shrink(String... p_44187_) {
      int i = Integer.MAX_VALUE;
      int j = 0;
      int k = 0;
      int l = 0;

      for(int i1 = 0; i1 < p_44187_.length; ++i1) {
         String s = p_44187_[i1];
         i = Math.min(i, firstNonSpace(s));
         int j1 = lastNonSpace(s);
         j = Math.max(j, j1);
         if (j1 < 0) {
            if (k == i1) {
               ++k;
            }

            ++l;
         } else {
            l = 0;
         }
      }

      if (p_44187_.length == l) {
         return new String[0];
      } else {
         String[] astring = new String[p_44187_.length - l - k];

         for(int k1 = 0; k1 < astring.length; ++k1) {
            astring[k1] = p_44187_[k1 + k].substring(i, j + 1);
         }

         return astring;
      }
   }

   public boolean isIncomplete() {
      NonNullList<Ingredient> nonnulllist = this.getIngredients();
      return nonnulllist.isEmpty() || nonnulllist.stream().filter((p_151277_) -> {
         return !p_151277_.isEmpty();
      }).anyMatch((p_151273_) -> {
         return net.minecraftforge.common.ForgeHooks.hasNoElements(p_151273_);
      });
   }

   private static int firstNonSpace(String pEntry) {
      int i;
      for(i = 0; i < pEntry.length() && pEntry.charAt(i) == ' '; ++i) {
      }

      return i;
   }

   private static int lastNonSpace(String pEntry) {
      int i;
      for(i = pEntry.length() - 1; i >= 0 && pEntry.charAt(i) == ' '; --i) {
      }

      return i;
   }

   static String[] m_44196_(JsonArray p_44197_) {
      String[] astring = new String[p_44197_.size()];
      if (astring.length > MAX_HEIGHT) {
         throw new JsonSyntaxException("Invalid pattern: too many rows, " + MAX_HEIGHT + " is maximum");
      } else if (astring.length == 0) {
         throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
      } else {
         for(int i = 0; i < astring.length; ++i) {
            String s = GsonHelper.convertToString(p_44197_.get(i), "pattern[" + i + "]");
            if (s.length() > MAX_WIDTH) {
               throw new JsonSyntaxException("Invalid pattern: too many columns, " + MAX_WIDTH + " is maximum");
            }

            if (i > 0 && astring[0].length() != s.length()) {
               throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
            }

            astring[i] = s;
         }

         return astring;
      }
   }

   static Map<String, Ingredient> m_44210_(JsonObject p_44211_) {
      Map<String, Ingredient> map = Maps.newHashMap();

      for(Map.Entry<String, JsonElement> entry : p_44211_.entrySet()) {
         if (entry.getKey().length() != 1) {
            throw new JsonSyntaxException("Invalid key entry: '" + (String)entry.getKey() + "' is an invalid symbol (must be 1 character only).");
         }

         if (" ".equals(entry.getKey())) {
            throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
         }

         map.put(entry.getKey(), Ingredient.m_288218_(entry.getValue(), false));
      }

      map.put(" ", Ingredient.EMPTY);
      return map;
   }

   public static ItemStack m_151274_(JsonObject p_151275_) {
      return net.minecraftforge.common.crafting.CraftingHelper.getItemStack(p_151275_, true, true);
   }

   public static Item m_151278_(JsonObject p_151279_) {
      String s = GsonHelper.getAsString(p_151279_, "item");
      Item item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.tryParse(s)).orElseThrow(() -> {
         return new JsonSyntaxException("Unknown item '" + s + "'");
      });
      if (item == Items.AIR) {
         throw new JsonSyntaxException("Empty ingredient not allowed here");
      } else {
         return item;
      }
   }

   public static class Serializer implements RecipeSerializer<ShapedRecipe> {
      private static final ResourceLocation NAME = new ResourceLocation("minecraft", "crafting_shaped");
      public ShapedRecipe m_6729_(ResourceLocation p_44236_, JsonObject p_44237_) {
         String s = GsonHelper.getAsString(p_44237_, "group", "");
         CraftingBookCategory craftingbookcategory = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(p_44237_, "category", (String)null), CraftingBookCategory.MISC);
         Map<String, Ingredient> map = ShapedRecipe.m_44210_(GsonHelper.getAsJsonObject(p_44237_, "key"));
         String[] astring = ShapedRecipe.shrink(ShapedRecipe.m_44196_(GsonHelper.getAsJsonArray(p_44237_, "pattern")));
         int i = astring[0].length();
         int j = astring.length;
         NonNullList<Ingredient> nonnulllist = ShapedRecipe.m_44202_(astring, map, i, j);
         ItemStack itemstack = ShapedRecipe.m_151274_(GsonHelper.getAsJsonObject(p_44237_, "result"));
         boolean flag = GsonHelper.getAsBoolean(p_44237_, "show_notification", true);
         return new ShapedRecipe(p_44236_, s, craftingbookcategory, i, j, nonnulllist, itemstack, flag);
      }

      public ShapedRecipe fromNetwork(ResourceLocation p_44239_, FriendlyByteBuf p_44240_) {
         int i = p_44240_.readVarInt();
         int j = p_44240_.readVarInt();
         String s = p_44240_.readUtf();
         CraftingBookCategory craftingbookcategory = p_44240_.readEnum(CraftingBookCategory.class);
         NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i * j, Ingredient.EMPTY);

         for(int k = 0; k < nonnulllist.size(); ++k) {
            nonnulllist.set(k, Ingredient.fromNetwork(p_44240_));
         }

         ItemStack itemstack = p_44240_.readItem();
         boolean flag = p_44240_.readBoolean();
         return new ShapedRecipe(p_44239_, s, craftingbookcategory, i, j, nonnulllist, itemstack, flag);
      }

      public void toNetwork(FriendlyByteBuf pBuffer, ShapedRecipe pRecipe) {
         pBuffer.writeVarInt(pRecipe.width);
         pBuffer.writeVarInt(pRecipe.height);
         pBuffer.writeUtf(pRecipe.group);
         pBuffer.writeEnum(pRecipe.category);

         for(Ingredient ingredient : pRecipe.recipeItems) {
            ingredient.toNetwork(pBuffer);
         }

         pBuffer.writeItem(pRecipe.result);
         pBuffer.writeBoolean(pRecipe.showNotification);
      }
   }
}
