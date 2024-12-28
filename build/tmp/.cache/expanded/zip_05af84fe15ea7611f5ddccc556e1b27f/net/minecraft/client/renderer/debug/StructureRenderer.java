package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;
   private final Map<DimensionType, Map<String, BoundingBox>> postMainBoxes = Maps.newIdentityHashMap();
   private final Map<DimensionType, Map<String, BoundingBox>> f_113677_ = Maps.newIdentityHashMap();
   private final Map<DimensionType, Map<String, Boolean>> f_113678_ = Maps.newIdentityHashMap();
   private static final int MAX_RENDER_DIST = 500;

   public StructureRenderer(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, double pCamX, double pCamY, double pCamZ) {
      Camera camera = this.minecraft.gameRenderer.getMainCamera();
      LevelAccessor levelaccessor = this.minecraft.level;
      DimensionType dimensiontype = levelaccessor.dimensionType();
      BlockPos blockpos = BlockPos.containing(camera.getPosition().x, 0.0D, camera.getPosition().z);
      VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.lines());
      if (this.postMainBoxes.containsKey(dimensiontype)) {
         for(BoundingBox boundingbox : this.postMainBoxes.get(dimensiontype).values()) {
            if (blockpos.closerThan(boundingbox.getCenter(), 500.0D)) {
               LevelRenderer.renderLineBox(pPoseStack, vertexconsumer, (double)boundingbox.minX() - pCamX, (double)boundingbox.minY() - pCamY, (double)boundingbox.minZ() - pCamZ, (double)(boundingbox.maxX() + 1) - pCamX, (double)(boundingbox.maxY() + 1) - pCamY, (double)(boundingbox.maxZ() + 1) - pCamZ, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
            }
         }
      }

      if (this.f_113677_.containsKey(dimensiontype)) {
         for(Map.Entry<String, BoundingBox> entry : this.f_113677_.get(dimensiontype).entrySet()) {
            String s = entry.getKey();
            BoundingBox boundingbox1 = entry.getValue();
            Boolean obool = this.f_113678_.get(dimensiontype).get(s);
            if (blockpos.closerThan(boundingbox1.getCenter(), 500.0D)) {
               if (obool) {
                  LevelRenderer.renderLineBox(pPoseStack, vertexconsumer, (double)boundingbox1.minX() - pCamX, (double)boundingbox1.minY() - pCamY, (double)boundingbox1.minZ() - pCamZ, (double)(boundingbox1.maxX() + 1) - pCamX, (double)(boundingbox1.maxY() + 1) - pCamY, (double)(boundingbox1.maxZ() + 1) - pCamZ, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F);
               } else {
                  LevelRenderer.renderLineBox(pPoseStack, vertexconsumer, (double)boundingbox1.minX() - pCamX, (double)boundingbox1.minY() - pCamY, (double)boundingbox1.minZ() - pCamZ, (double)(boundingbox1.maxX() + 1) - pCamX, (double)(boundingbox1.maxY() + 1) - pCamY, (double)(boundingbox1.maxZ() + 1) - pCamZ, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F);
               }
            }
         }
      }

   }

   public void addBoundingBox(BoundingBox pBoundingBox, List<BoundingBox> pPieces, List<Boolean> p_113685_, DimensionType p_113686_) {
      if (!this.postMainBoxes.containsKey(p_113686_)) {
         this.postMainBoxes.put(p_113686_, Maps.newHashMap());
      }

      if (!this.f_113677_.containsKey(p_113686_)) {
         this.f_113677_.put(p_113686_, Maps.newHashMap());
         this.f_113678_.put(p_113686_, Maps.newHashMap());
      }

      this.postMainBoxes.get(p_113686_).put(pBoundingBox.toString(), pBoundingBox);

      for(int i = 0; i < pPieces.size(); ++i) {
         BoundingBox boundingbox = pPieces.get(i);
         Boolean obool = p_113685_.get(i);
         this.f_113677_.get(p_113686_).put(boundingbox.toString(), boundingbox);
         this.f_113678_.get(p_113686_).put(boundingbox.toString(), obool);
      }

   }

   public void clear() {
      this.postMainBoxes.clear();
      this.f_113677_.clear();
      this.f_113678_.clear();
   }
}