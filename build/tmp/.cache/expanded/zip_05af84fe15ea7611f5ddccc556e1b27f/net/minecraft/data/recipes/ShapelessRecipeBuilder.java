package net.minecraft.data.recipes;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class ShapelessRecipeBuilder extends CraftingRecipeBuilder implements RecipeBuilder {
   private final RecipeCategory category;
   private final Item result;
   private final int count;
   private final List<Ingredient> ingredients = Lists.newArrayList();
   private final Advancement.Builder f_126176_ = Advancement.Builder.recipeAdvancement();
   @Nullable
   private String group;

   public ShapelessRecipeBuilder(RecipeCategory pCategory, ItemLike pResult, int pCount) {
      this.category = pCategory;
      this.result = pResult.asItem();
      this.count = pCount;
   }

   /**
    * Creates a new builder for a shapeless recipe.
    */
   public static ShapelessRecipeBuilder shapeless(RecipeCategory pCategory, ItemLike pResult) {
      return new ShapelessRecipeBuilder(pCategory, pResult, 1);
   }

   /**
    * Creates a new builder for a shapeless recipe.
    */
   public static ShapelessRecipeBuilder shapeless(RecipeCategory pCategory, ItemLike pResult, int pCount) {
      return new ShapelessRecipeBuilder(pCategory, pResult, pCount);
   }

   /**
    * Adds an ingredient that can be any item in the given tag.
    */
   public ShapelessRecipeBuilder requires(TagKey<Item> pTag) {
      return this.requires(Ingredient.of(pTag));
   }

   /**
    * Adds an ingredient of the given item.
    */
   public ShapelessRecipeBuilder requires(ItemLike pItem) {
      return this.requires(pItem, 1);
   }

   /**
    * Adds the given ingredient multiple times.
    */
   public ShapelessRecipeBuilder requires(ItemLike pItem, int pQuantity) {
      for(int i = 0; i < pQuantity; ++i) {
         this.requires(Ingredient.of(pItem));
      }

      return this;
   }

   /**
    * Adds an ingredient.
    */
   public ShapelessRecipeBuilder requires(Ingredient pIngredient) {
      return this.requires(pIngredient, 1);
   }

   /**
    * Adds an ingredient multiple times.
    */
   public ShapelessRecipeBuilder requires(Ingredient pIngredient, int pQuantity) {
      for(int i = 0; i < pQuantity; ++i) {
         this.ingredients.add(pIngredient);
      }

      return this;
   }

   public ShapelessRecipeBuilder unlockedBy(String p_126197_, CriterionTriggerInstance p_126198_) {
      this.f_126176_.m_138386_(p_126197_, p_126198_);
      return this;
   }

   public ShapelessRecipeBuilder group(@Nullable String pGroupName) {
      this.group = pGroupName;
      return this;
   }

   public Item getResult() {
      return this.result;
   }

   public void save(Consumer<FinishedRecipe> p_126205_, ResourceLocation pId) {
      this.ensureValid(pId);
      this.f_126176_.parent(ROOT_RECIPE_ADVANCEMENT).m_138386_("has_the_recipe", RecipeUnlockedTrigger.unlocked(pId)).rewards(AdvancementRewards.Builder.recipe(pId)).requirements(RequirementsStrategy.f_15979_);
      p_126205_.accept(new ShapelessRecipeBuilder.Result(pId, this.result, this.count, this.group == null ? "" : this.group, determineBookCategory(this.category), this.ingredients, this.f_126176_, pId.withPrefix("recipes/" + this.category.getFolderName() + "/")));
   }

   /**
    * Makes sure that this recipe is valid and obtainable.
    */
   private void ensureValid(ResourceLocation pId) {
      if (this.f_126176_.m_138405_().isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + pId);
      }
   }

   public static class Result extends CraftingRecipeBuilder.CraftingResult {
      private final ResourceLocation id;
      private final Item result;
      private final int count;
      private final String group;
      private final List<Ingredient> ingredients;
      private final Advancement.Builder advancement;
      private final ResourceLocation f_126220_;

      public Result(ResourceLocation pId, Item pResult, int pCount, String pGroup, CraftingBookCategory pCategory, List<Ingredient> pIngredients, Advancement.Builder p_249909_, ResourceLocation p_249109_) {
         super(pCategory);
         this.id = pId;
         this.result = pResult;
         this.count = pCount;
         this.group = pGroup;
         this.ingredients = pIngredients;
         this.advancement = p_249909_;
         this.f_126220_ = p_249109_;
      }

      public void serializeRecipeData(JsonObject pJson) {
         super.serializeRecipeData(pJson);
         if (!this.group.isEmpty()) {
            pJson.addProperty("group", this.group);
         }

         JsonArray jsonarray = new JsonArray();

         for(Ingredient ingredient : this.ingredients) {
            jsonarray.add(ingredient.toJson());
         }

         pJson.add("ingredients", jsonarray);
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());
         if (this.count > 1) {
            jsonobject.addProperty("count", this.count);
         }

         pJson.add("result", jsonobject);
      }

      public RecipeSerializer<?> m_6637_() {
         return RecipeSerializer.SHAPELESS_RECIPE;
      }

      public ResourceLocation m_6445_() {
         return this.id;
      }

      @Nullable
      public JsonObject m_5860_() {
         return this.advancement.m_138400_();
      }

      @Nullable
      public ResourceLocation m_6448_() {
         return this.f_126220_;
      }
   }
}