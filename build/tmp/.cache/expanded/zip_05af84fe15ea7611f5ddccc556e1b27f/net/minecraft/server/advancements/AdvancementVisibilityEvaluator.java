package net.minecraft.server.advancements;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;

public class AdvancementVisibilityEvaluator {
   private static final int VISIBILITY_DEPTH = 2;

   private static AdvancementVisibilityEvaluator.VisibilityRule evaluateVisibilityRule(Advancement pAdvancement, boolean pAlwaysShow) {
      DisplayInfo displayinfo = pAdvancement.m_138320_();
      if (displayinfo == null) {
         return AdvancementVisibilityEvaluator.VisibilityRule.HIDE;
      } else if (pAlwaysShow) {
         return AdvancementVisibilityEvaluator.VisibilityRule.SHOW;
      } else {
         return displayinfo.isHidden() ? AdvancementVisibilityEvaluator.VisibilityRule.HIDE : AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE;
      }
   }

   private static boolean evaluateVisiblityForUnfinishedNode(Stack<AdvancementVisibilityEvaluator.VisibilityRule> pVisibilityRules) {
      for(int i = 0; i <= 2; ++i) {
         AdvancementVisibilityEvaluator.VisibilityRule advancementvisibilityevaluator$visibilityrule = pVisibilityRules.peek(i);
         if (advancementvisibilityevaluator$visibilityrule == AdvancementVisibilityEvaluator.VisibilityRule.SHOW) {
            return true;
         }

         if (advancementvisibilityevaluator$visibilityrule == AdvancementVisibilityEvaluator.VisibilityRule.HIDE) {
            return false;
         }
      }

      return false;
   }

   private static boolean evaluateVisibility(Advancement p_265202_, Stack<AdvancementVisibilityEvaluator.VisibilityRule> p_265086_, Predicate<Advancement> pPredicate, AdvancementVisibilityEvaluator.Output pOutput) {
      boolean flag = pPredicate.test(p_265202_);
      AdvancementVisibilityEvaluator.VisibilityRule advancementvisibilityevaluator$visibilityrule = evaluateVisibilityRule(p_265202_, flag);
      boolean flag1 = flag;
      p_265086_.push(advancementvisibilityevaluator$visibilityrule);

      for(Advancement advancement : p_265202_.m_138322_()) {
         flag1 |= evaluateVisibility(advancement, p_265086_, pPredicate, pOutput);
      }

      boolean flag2 = flag1 || evaluateVisiblityForUnfinishedNode(p_265086_);
      p_265086_.pop();
      pOutput.accept(p_265202_, flag2);
      return flag1;
   }

   public static void evaluateVisibility(Advancement p_265578_, Predicate<Advancement> pPredicate, AdvancementVisibilityEvaluator.Output pOutput) {
      Advancement advancement = p_265578_.m_264348_();
      Stack<AdvancementVisibilityEvaluator.VisibilityRule> stack = new ObjectArrayList<>();

      for(int i = 0; i <= 2; ++i) {
         stack.push(AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE);
      }

      evaluateVisibility(advancement, stack, pPredicate, pOutput);
   }

   public static boolean isVisible(Advancement advancement, Predicate<Advancement> test) {
      Stack<AdvancementVisibilityEvaluator.VisibilityRule> stack = new ObjectArrayList<>();

      for(int i = 0; i <= 2; ++i) {
         stack.push(AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE);
      }
      return evaluateVisibility(advancement.m_264348_(), stack, test, (p_265639_, pVisible) -> {});
   }

   @FunctionalInterface
   public interface Output {
      void accept(Advancement p_265639_, boolean pVisible);
   }

   static enum VisibilityRule {
      SHOW,
      HIDE,
      NO_CHANGE;
   }
}
