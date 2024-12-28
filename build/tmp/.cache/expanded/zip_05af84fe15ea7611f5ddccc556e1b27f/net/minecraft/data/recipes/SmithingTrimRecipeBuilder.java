package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SmithingTrimRecipeBuilder {
   private final RecipeCategory category;
   private final Ingredient template;
   private final Ingredient base;
   private final Ingredient addition;
   private final Advancement.Builder f_265957_ = Advancement.Builder.recipeAdvancement();
   private final RecipeSerializer<?> type;

   public SmithingTrimRecipeBuilder(RecipeSerializer<?> pType, RecipeCategory pCategory, Ingredient pTemplate, Ingredient pBase, Ingredient pAddition) {
      this.category = pCategory;
      this.type = pType;
      this.template = pTemplate;
      this.base = pBase;
      this.addition = pAddition;
   }

   public static SmithingTrimRecipeBuilder smithingTrim(Ingredient pTemplate, Ingredient pBase, Ingredient pAddition, RecipeCategory pCategory) {
      return new SmithingTrimRecipeBuilder(RecipeSerializer.SMITHING_TRIM, pCategory, pTemplate, pBase, pAddition);
   }

   public SmithingTrimRecipeBuilder unlocks(String pKey, CriterionTriggerInstance p_267233_) {
      this.f_265957_.m_138386_(pKey, p_267233_);
      return this;
   }

   public void save(Consumer<FinishedRecipe> p_267231_, ResourceLocation pRecipeId) {
      this.ensureValid(pRecipeId);
      this.f_265957_.parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT).m_138386_("has_the_recipe", RecipeUnlockedTrigger.unlocked(pRecipeId)).rewards(AdvancementRewards.Builder.recipe(pRecipeId)).requirements(RequirementsStrategy.f_15979_);
      p_267231_.accept(new SmithingTrimRecipeBuilder.Result(pRecipeId, this.type, this.template, this.base, this.addition, this.f_265957_, pRecipeId.withPrefix("recipes/" + this.category.getFolderName() + "/")));
   }

   private void ensureValid(ResourceLocation pLocation) {
      if (this.f_265957_.m_138405_().isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + pLocation);
      }
   }

   public static record Result(ResourceLocation id, RecipeSerializer<?> type, Ingredient template, Ingredient base, Ingredient addition, Advancement.Builder advancement, ResourceLocation f_265882_) implements FinishedRecipe {
      public void serializeRecipeData(JsonObject p_267008_) {
         p_267008_.add("template", this.template.toJson());
         p_267008_.add("base", this.base.toJson());
         p_267008_.add("addition", this.addition.toJson());
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
         return this.f_265882_;
      }
   }
}