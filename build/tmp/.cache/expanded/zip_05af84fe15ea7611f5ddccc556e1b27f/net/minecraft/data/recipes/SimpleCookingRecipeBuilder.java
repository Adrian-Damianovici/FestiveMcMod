package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class SimpleCookingRecipeBuilder implements RecipeBuilder {
   private final RecipeCategory category;
   private final CookingBookCategory bookCategory;
   private final Item result;
   private final Ingredient ingredient;
   private final float experience;
   private final int cookingTime;
   private final Advancement.Builder f_126239_ = Advancement.Builder.recipeAdvancement();
   @Nullable
   private String group;
   private final RecipeSerializer<? extends AbstractCookingRecipe> serializer;

   private SimpleCookingRecipeBuilder(RecipeCategory pCategory, CookingBookCategory pBookCategory, ItemLike pResult, Ingredient pIngredient, float pExperience, int pCookingTime, RecipeSerializer<? extends AbstractCookingRecipe> pSerializer) {
      this.category = pCategory;
      this.bookCategory = pBookCategory;
      this.result = pResult.asItem();
      this.ingredient = pIngredient;
      this.experience = pExperience;
      this.cookingTime = pCookingTime;
      this.serializer = pSerializer;
   }

   public static SimpleCookingRecipeBuilder generic(Ingredient pIngredient, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, RecipeSerializer<? extends AbstractCookingRecipe> pSerializer) {
      return new SimpleCookingRecipeBuilder(pCategory, determineRecipeCategory(pSerializer, pResult), pResult, pIngredient, pExperience, pCookingTime, pSerializer);
   }

   public static SimpleCookingRecipeBuilder campfireCooking(Ingredient pIngredient, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime) {
      return new SimpleCookingRecipeBuilder(pCategory, CookingBookCategory.FOOD, pResult, pIngredient, pExperience, pCookingTime, RecipeSerializer.CAMPFIRE_COOKING_RECIPE);
   }

   public static SimpleCookingRecipeBuilder blasting(Ingredient pIngredient, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime) {
      return new SimpleCookingRecipeBuilder(pCategory, determineBlastingRecipeCategory(pResult), pResult, pIngredient, pExperience, pCookingTime, RecipeSerializer.BLASTING_RECIPE);
   }

   public static SimpleCookingRecipeBuilder smelting(Ingredient pIngredient, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime) {
      return new SimpleCookingRecipeBuilder(pCategory, determineSmeltingRecipeCategory(pResult), pResult, pIngredient, pExperience, pCookingTime, RecipeSerializer.SMELTING_RECIPE);
   }

   public static SimpleCookingRecipeBuilder smoking(Ingredient pIngredient, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime) {
      return new SimpleCookingRecipeBuilder(pCategory, CookingBookCategory.FOOD, pResult, pIngredient, pExperience, pCookingTime, RecipeSerializer.SMOKING_RECIPE);
   }

   public SimpleCookingRecipeBuilder unlockedBy(String p_126255_, CriterionTriggerInstance p_126256_) {
      this.f_126239_.m_138386_(p_126255_, p_126256_);
      return this;
   }

   public SimpleCookingRecipeBuilder group(@Nullable String pGroupName) {
      this.group = pGroupName;
      return this;
   }

   public Item getResult() {
      return this.result;
   }

   public void save(Consumer<FinishedRecipe> p_126263_, ResourceLocation pId) {
      this.ensureValid(pId);
      this.f_126239_.parent(ROOT_RECIPE_ADVANCEMENT).m_138386_("has_the_recipe", RecipeUnlockedTrigger.unlocked(pId)).rewards(AdvancementRewards.Builder.recipe(pId)).requirements(RequirementsStrategy.f_15979_);
      p_126263_.accept(new SimpleCookingRecipeBuilder.Result(pId, this.group == null ? "" : this.group, this.bookCategory, this.ingredient, this.result, this.experience, this.cookingTime, this.f_126239_, pId.withPrefix("recipes/" + this.category.getFolderName() + "/"), this.serializer));
   }

   private static CookingBookCategory determineSmeltingRecipeCategory(ItemLike pResult) {
      if (pResult.asItem().isEdible()) {
         return CookingBookCategory.FOOD;
      } else {
         return pResult.asItem() instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
      }
   }

   private static CookingBookCategory determineBlastingRecipeCategory(ItemLike pResult) {
      return pResult.asItem() instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
   }

   private static CookingBookCategory determineRecipeCategory(RecipeSerializer<? extends AbstractCookingRecipe> pSerializer, ItemLike pResult) {
      if (pSerializer == RecipeSerializer.SMELTING_RECIPE) {
         return determineSmeltingRecipeCategory(pResult);
      } else if (pSerializer == RecipeSerializer.BLASTING_RECIPE) {
         return determineBlastingRecipeCategory(pResult);
      } else if (pSerializer != RecipeSerializer.SMOKING_RECIPE && pSerializer != RecipeSerializer.CAMPFIRE_COOKING_RECIPE) {
         throw new IllegalStateException("Unknown cooking recipe type");
      } else {
         return CookingBookCategory.FOOD;
      }
   }

   /**
    * Makes sure that this obtainable.
    */
   private void ensureValid(ResourceLocation pId) {
      if (this.f_126239_.m_138405_().isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + pId);
      }
   }

   static class Result implements FinishedRecipe {
      private final ResourceLocation id;
      private final String group;
      private final CookingBookCategory category;
      private final Ingredient ingredient;
      private final Item result;
      private final float experience;
      private final int cookingTime;
      private final Advancement.Builder advancement;
      private final ResourceLocation f_126284_;
      private final RecipeSerializer<? extends AbstractCookingRecipe> f_126285_;

      public Result(ResourceLocation p_252275_, String p_248665_, CookingBookCategory p_251944_, Ingredient p_249473_, Item p_252028_, float p_249206_, int p_251002_, Advancement.Builder p_249151_, ResourceLocation p_252090_, RecipeSerializer<? extends AbstractCookingRecipe> p_249537_) {
         this.id = p_252275_;
         this.group = p_248665_;
         this.category = p_251944_;
         this.ingredient = p_249473_;
         this.result = p_252028_;
         this.experience = p_249206_;
         this.cookingTime = p_251002_;
         this.advancement = p_249151_;
         this.f_126284_ = p_252090_;
         this.f_126285_ = p_249537_;
      }

      public void serializeRecipeData(JsonObject pJson) {
         if (!this.group.isEmpty()) {
            pJson.addProperty("group", this.group);
         }

         pJson.addProperty("category", this.category.getSerializedName());
         pJson.add("ingredient", this.ingredient.toJson());
         pJson.addProperty("result", BuiltInRegistries.ITEM.getKey(this.result).toString());
         pJson.addProperty("experience", this.experience);
         pJson.addProperty("cookingtime", this.cookingTime);
      }

      public RecipeSerializer<?> m_6637_() {
         return this.f_126285_;
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
         return this.f_126284_;
      }
   }
}