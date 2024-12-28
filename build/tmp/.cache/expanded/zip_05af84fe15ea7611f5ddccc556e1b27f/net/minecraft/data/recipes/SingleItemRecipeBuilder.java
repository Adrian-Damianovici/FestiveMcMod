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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class SingleItemRecipeBuilder implements RecipeBuilder {
   private final RecipeCategory category;
   private final Item result;
   private final Ingredient ingredient;
   private final int count;
   private final Advancement.Builder f_126305_ = Advancement.Builder.recipeAdvancement();
   @Nullable
   private String group;
   private final RecipeSerializer<?> type;

   public SingleItemRecipeBuilder(RecipeCategory pCategory, RecipeSerializer<?> pType, Ingredient pIngredient, ItemLike pResult, int pCount) {
      this.category = pCategory;
      this.type = pType;
      this.result = pResult.asItem();
      this.ingredient = pIngredient;
      this.count = pCount;
   }

   public static SingleItemRecipeBuilder stonecutting(Ingredient pIngredient, RecipeCategory pCategory, ItemLike pResult) {
      return new SingleItemRecipeBuilder(pCategory, RecipeSerializer.STONECUTTER, pIngredient, pResult, 1);
   }

   public static SingleItemRecipeBuilder stonecutting(Ingredient pIngredient, RecipeCategory pCategory, ItemLike pResult, int pCount) {
      return new SingleItemRecipeBuilder(pCategory, RecipeSerializer.STONECUTTER, pIngredient, pResult, pCount);
   }

   public SingleItemRecipeBuilder unlockedBy(String pName, CriterionTriggerInstance p_176811_) {
      this.f_126305_.m_138386_(pName, p_176811_);
      return this;
   }

   public SingleItemRecipeBuilder group(@Nullable String pGroupName) {
      this.group = pGroupName;
      return this;
   }

   public Item getResult() {
      return this.result;
   }

   public void save(Consumer<FinishedRecipe> p_126327_, ResourceLocation pId) {
      this.ensureValid(pId);
      this.f_126305_.parent(ROOT_RECIPE_ADVANCEMENT).m_138386_("has_the_recipe", RecipeUnlockedTrigger.unlocked(pId)).rewards(AdvancementRewards.Builder.recipe(pId)).requirements(RequirementsStrategy.f_15979_);
      p_126327_.accept(new SingleItemRecipeBuilder.Result(pId, this.type, this.group == null ? "" : this.group, this.ingredient, this.result, this.count, this.f_126305_, pId.withPrefix("recipes/" + this.category.getFolderName() + "/")));
   }

   private void ensureValid(ResourceLocation pId) {
      if (this.f_126305_.m_138405_().isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + pId);
      }
   }

   public static class Result implements FinishedRecipe {
      private final ResourceLocation id;
      private final String group;
      private final Ingredient ingredient;
      private final Item result;
      private final int count;
      private final Advancement.Builder advancement;
      private final ResourceLocation f_126337_;
      private final RecipeSerializer<?> type;

      public Result(ResourceLocation p_126340_, RecipeSerializer<?> p_126341_, String p_126342_, Ingredient p_126343_, Item p_126344_, int p_126345_, Advancement.Builder p_126346_, ResourceLocation p_126347_) {
         this.id = p_126340_;
         this.type = p_126341_;
         this.group = p_126342_;
         this.ingredient = p_126343_;
         this.result = p_126344_;
         this.count = p_126345_;
         this.advancement = p_126346_;
         this.f_126337_ = p_126347_;
      }

      public void serializeRecipeData(JsonObject pJson) {
         if (!this.group.isEmpty()) {
            pJson.addProperty("group", this.group);
         }

         pJson.add("ingredient", this.ingredient.toJson());
         pJson.addProperty("result", BuiltInRegistries.ITEM.getKey(this.result).toString());
         pJson.addProperty("count", this.count);
      }

      public ResourceLocation m_6445_() {
         return this.id;
      }

      public RecipeSerializer<?> m_6637_() {
         return this.type;
      }

      @Nullable
      public JsonObject m_5860_() {
         return this.advancement.m_138400_();
      }

      @Nullable
      public ResourceLocation m_6448_() {
         return this.f_126337_;
      }
   }
}