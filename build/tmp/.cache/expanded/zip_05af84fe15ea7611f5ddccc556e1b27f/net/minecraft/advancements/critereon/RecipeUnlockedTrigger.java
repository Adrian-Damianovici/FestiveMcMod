package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeUnlockedTrigger extends SimpleCriterionTrigger<RecipeUnlockedTrigger.TriggerInstance> {
   static final ResourceLocation f_63714_ = new ResourceLocation("recipe_unlocked");

   public ResourceLocation m_7295_() {
      return f_63714_;
   }

   public RecipeUnlockedTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate p_286739_, DeserializationContext pDeserializationContext) {
      ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pJson, "recipe"));
      return new RecipeUnlockedTrigger.TriggerInstance(p_286739_, resourcelocation);
   }

   public void trigger(ServerPlayer pPlayer, Recipe<?> p_63720_) {
      this.trigger(pPlayer, (p_63723_) -> {
         return p_63723_.matches(p_63720_);
      });
   }

   public static RecipeUnlockedTrigger.TriggerInstance unlocked(ResourceLocation pRecipeId) {
      return new RecipeUnlockedTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, pRecipeId);
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ResourceLocation recipe;

      public TriggerInstance(ContextAwarePredicate p_286461_, ResourceLocation pRecipe) {
         super(RecipeUnlockedTrigger.f_63714_, p_286461_);
         this.recipe = pRecipe;
      }

      public JsonObject serializeToJson(SerializationContext p_63742_) {
         JsonObject jsonobject = super.serializeToJson(p_63742_);
         jsonobject.addProperty("recipe", this.recipe.toString());
         return jsonobject;
      }

      public boolean matches(Recipe<?> p_63740_) {
         return this.recipe.equals(p_63740_.m_6423_());
      }
   }
}