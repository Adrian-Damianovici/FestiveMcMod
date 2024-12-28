package net.minecraft.world.inventory;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public interface RecipeHolder {
   void m_6029_(@Nullable Recipe<?> p_40134_);

   @Nullable
   Recipe<?> m_7928_();

   default void awardUsedRecipes(Player p_281647_, List<ItemStack> p_282578_) {
      Recipe<?> recipe = this.m_7928_();
      if (recipe != null) {
         p_281647_.triggerRecipeCrafted(recipe, p_282578_);
         if (!recipe.isSpecial()) {
            p_281647_.awardRecipes(Collections.singleton(recipe));
            this.m_6029_((Recipe<?>)null);
         }
      }

   }

   default boolean m_40135_(Level p_40136_, ServerPlayer p_40137_, Recipe<?> p_40138_) {
      if (!p_40138_.isSpecial() && p_40136_.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) && !p_40137_.getRecipeBook().contains(p_40138_)) {
         return false;
      } else {
         this.m_6029_(p_40138_);
         return true;
      }
   }
}