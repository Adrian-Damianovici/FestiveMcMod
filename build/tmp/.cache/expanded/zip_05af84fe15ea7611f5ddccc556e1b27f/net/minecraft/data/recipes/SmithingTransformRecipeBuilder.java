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

public class SmithingTransformRecipeBuilder {
   private final Ingredient template;
   private final Ingredient base;
   private final Ingredient addition;
   private final RecipeCategory category;
   private final Item result;
   private final Advancement.Builder f_266090_ = Advancement.Builder.recipeAdvancement();
   private final RecipeSerializer<?> type;

   public SmithingTransformRecipeBuilder(RecipeSerializer<?> pType, Ingredient pTemplate, Ingredient pBase, Ingredient pAddition, RecipeCategory pCategory, Item pResult) {
      this.category = pCategory;
      this.type = pType;
      this.template = pTemplate;
      this.base = pBase;
      this.addition = pAddition;
      this.result = pResult;
   }

   public static SmithingTransformRecipeBuilder smithing(Ingredient pTemplate, Ingredient pBase, Ingredient pAddition, RecipeCategory pCategory, Item pResult) {
      return new SmithingTransformRecipeBuilder(RecipeSerializer.SMITHING_TRANSFORM, pTemplate, pBase, pAddition, pCategory, pResult);
   }

   public SmithingTransformRecipeBuilder unlocks(String pKey, CriterionTriggerInstance p_267277_) {
      this.f_266090_.m_138386_(pKey, p_267277_);
      return this;
   }

   public void save(Consumer<FinishedRecipe> p_267068_, String pRecipeId) {
      this.save(p_267068_, new ResourceLocation(pRecipeId));
   }

   public void save(Consumer<FinishedRecipe> p_267089_, ResourceLocation pRecipeId) {
      this.ensureValid(pRecipeId);
      this.f_266090_.parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT).m_138386_("has_the_recipe", RecipeUnlockedTrigger.unlocked(pRecipeId)).rewards(AdvancementRewards.Builder.recipe(pRecipeId)).requirements(RequirementsStrategy.f_15979_);
      p_267089_.accept(new SmithingTransformRecipeBuilder.Result(pRecipeId, this.type, this.template, this.base, this.addition, this.result, this.f_266090_, pRecipeId.withPrefix("recipes/" + this.category.getFolderName() + "/")));
   }

   private void ensureValid(ResourceLocation pLocation) {
      if (this.f_266090_.m_138405_().isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + pLocation);
      }
   }

   public static record Result(ResourceLocation id, RecipeSerializer<?> type, Ingredient template, Ingredient base, Ingredient addition, Item result, Advancement.Builder advancement, ResourceLocation f_266094_) implements FinishedRecipe {
      public void serializeRecipeData(JsonObject p_266713_) {
         p_266713_.add("template", this.template.toJson());
         p_266713_.add("base", this.base.toJson());
         p_266713_.add("addition", this.addition.toJson());
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());
         p_266713_.add("result", jsonobject);
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
         return this.f_266094_;
      }
   }
}