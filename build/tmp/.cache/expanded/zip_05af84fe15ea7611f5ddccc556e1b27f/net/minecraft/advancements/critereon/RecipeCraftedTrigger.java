package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class RecipeCraftedTrigger extends SimpleCriterionTrigger<RecipeCraftedTrigger.TriggerInstance> {
   static final ResourceLocation f_279610_ = new ResourceLocation("recipe_crafted");

   public ResourceLocation m_7295_() {
      return f_279610_;
   }

   protected RecipeCraftedTrigger.TriggerInstance createInstance(JsonObject p_286541_, ContextAwarePredicate p_286267_, DeserializationContext p_286402_) {
      ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(p_286541_, "recipe_id"));
      ItemPredicate[] aitempredicate = ItemPredicate.fromJsonArray(p_286541_.get("ingredients"));
      return new RecipeCraftedTrigger.TriggerInstance(p_286267_, resourcelocation, List.of(aitempredicate));
   }

   public void trigger(ServerPlayer pPlayer, ResourceLocation pRecipeId, List<ItemStack> pItems) {
      this.trigger(pPlayer, (p_282798_) -> {
         return p_282798_.matches(pRecipeId, pItems);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ResourceLocation recipeId;
      private final List<ItemPredicate> predicates;

      public TriggerInstance(ContextAwarePredicate p_286913_, ResourceLocation pRecipeId, List<ItemPredicate> pPredicates) {
         super(RecipeCraftedTrigger.f_279610_, p_286913_);
         this.recipeId = pRecipeId;
         this.predicates = pPredicates;
      }

      public static RecipeCraftedTrigger.TriggerInstance craftedItem(ResourceLocation pRecipeId, List<ItemPredicate> p_281369_) {
         return new RecipeCraftedTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, pRecipeId, p_281369_);
      }

      public static RecipeCraftedTrigger.TriggerInstance craftedItem(ResourceLocation pRecipeId) {
         return new RecipeCraftedTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, pRecipeId, List.of());
      }

      boolean matches(ResourceLocation pRecipeId, List<ItemStack> pItems) {
         if (!pRecipeId.equals(this.recipeId)) {
            return false;
         } else {
            List<ItemStack> list = new ArrayList<>(pItems);

            for(ItemPredicate itempredicate : this.predicates) {
               boolean flag = false;
               Iterator<ItemStack> iterator = list.iterator();

               while(iterator.hasNext()) {
                  if (itempredicate.matches(iterator.next())) {
                     iterator.remove();
                     flag = true;
                     break;
                  }
               }

               if (!flag) {
                  return false;
               }
            }

            return true;
         }
      }

      public JsonObject serializeToJson(SerializationContext p_281942_) {
         JsonObject jsonobject = super.serializeToJson(p_281942_);
         jsonobject.addProperty("recipe_id", this.recipeId.toString());
         if (this.predicates.size() > 0) {
            JsonArray jsonarray = new JsonArray();

            for(ItemPredicate itempredicate : this.predicates) {
               jsonarray.add(itempredicate.serializeToJson());
            }

            jsonobject.add("ingredients", jsonarray);
         }

         return jsonobject;
      }
   }
}