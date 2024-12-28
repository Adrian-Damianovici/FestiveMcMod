package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GoalSelectorDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private static final int MAX_RENDER_DIST = 160;
   private final Minecraft minecraft;
   private final Map<Integer, List<GoalSelectorDebugRenderer.DebugGoal>> goalSelectors = Maps.newHashMap();

   public void clear() {
      this.goalSelectors.clear();
   }

   public void addGoalSelector(int pMobId, List<GoalSelectorDebugRenderer.DebugGoal> pGoals) {
      this.goalSelectors.put(pMobId, pGoals);
   }

   public void removeGoalSelector(int pMobId) {
      this.goalSelectors.remove(pMobId);
   }

   public GoalSelectorDebugRenderer(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, double pCamX, double pCamY, double pCamZ) {
      Camera camera = this.minecraft.gameRenderer.getMainCamera();
      BlockPos blockpos = BlockPos.containing(camera.getPosition().x, 0.0D, camera.getPosition().z);
      this.goalSelectors.forEach((p_269742_, p_269743_) -> {
         for(int i = 0; i < p_269743_.size(); ++i) {
            GoalSelectorDebugRenderer.DebugGoal goalselectordebugrenderer$debuggoal = p_269743_.get(i);
            if (blockpos.closerThan(goalselectordebugrenderer$debuggoal.f_113561_, 160.0D)) {
               double d0 = (double)goalselectordebugrenderer$debuggoal.f_113561_.getX() + 0.5D;
               double d1 = (double)goalselectordebugrenderer$debuggoal.f_113561_.getY() + 2.0D + (double)i * 0.25D;
               double d2 = (double)goalselectordebugrenderer$debuggoal.f_113561_.getZ() + 0.5D;
               int j = goalselectordebugrenderer$debuggoal.f_113564_ ? -16711936 : -3355444;
               DebugRenderer.renderFloatingText(pPoseStack, pBuffer, goalselectordebugrenderer$debuggoal.f_113563_, d0, d1, d2, j);
            }
         }

      });
   }

   @OnlyIn(Dist.CLIENT)
   public static class DebugGoal {
      public final BlockPos f_113561_;
      public final int f_113562_;
      public final String f_113563_;
      public final boolean f_113564_;

      public DebugGoal(BlockPos p_113566_, int p_113567_, String p_113568_, boolean p_113569_) {
         this.f_113561_ = p_113566_;
         this.f_113562_ = p_113567_;
         this.f_113563_ = p_113568_;
         this.f_113564_ = p_113569_;
      }
   }
}