package net.minecraft.client.renderer;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ViewArea {
   protected final LevelRenderer levelRenderer;
   protected final Level level;
   protected int f_110840_;
   protected int f_110841_;
   protected int f_110842_;
   public ChunkRenderDispatcher.RenderChunk[] f_110843_;

   public ViewArea(ChunkRenderDispatcher p_110845_, Level pLevel, int pViewDistance, LevelRenderer pLevelRenderer) {
      this.levelRenderer = pLevelRenderer;
      this.level = pLevel;
      this.setViewDistance(pViewDistance);
      this.m_110864_(p_110845_);
   }

   protected void m_110864_(ChunkRenderDispatcher p_110865_) {
      if (!Minecraft.getInstance().isSameThread()) {
         throw new IllegalStateException("createChunks called from wrong thread: " + Thread.currentThread().getName());
      } else {
         int i = this.f_110841_ * this.f_110840_ * this.f_110842_;
         this.f_110843_ = new ChunkRenderDispatcher.RenderChunk[i];

         for(int j = 0; j < this.f_110841_; ++j) {
            for(int k = 0; k < this.f_110840_; ++k) {
               for(int l = 0; l < this.f_110842_; ++l) {
                  int i1 = this.m_110855_(j, k, l);
                  this.f_110843_[i1] = p_110865_.new RenderChunk(i1, j * 16, k * 16, l * 16);
               }
            }
         }

      }
   }

   public void releaseAllBuffers() {
      for(ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk : this.f_110843_) {
         chunkrenderdispatcher$renderchunk.m_112838_();
      }

   }

   private int m_110855_(int p_110856_, int p_110857_, int p_110858_) {
      return (p_110858_ * this.f_110840_ + p_110857_) * this.f_110841_ + p_110856_;
   }

   protected void setViewDistance(int pRenderDistanceChunks) {
      int i = pRenderDistanceChunks * 2 + 1;
      this.f_110841_ = i;
      this.f_110840_ = this.level.getSectionsCount();
      this.f_110842_ = i;
   }

   public void repositionCamera(double pViewEntityX, double pViewEntityZ) {
      int i = Mth.ceil(pViewEntityX);
      int j = Mth.ceil(pViewEntityZ);

      for(int k = 0; k < this.f_110841_; ++k) {
         int l = this.f_110841_ * 16;
         int i1 = i - 8 - l / 2;
         int j1 = i1 + Math.floorMod(k * 16 - i1, l);

         for(int k1 = 0; k1 < this.f_110842_; ++k1) {
            int l1 = this.f_110842_ * 16;
            int i2 = j - 8 - l1 / 2;
            int j2 = i2 + Math.floorMod(k1 * 16 - i2, l1);

            for(int k2 = 0; k2 < this.f_110840_; ++k2) {
               int l2 = this.level.getMinBuildHeight() + k2 * 16;
               ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.f_110843_[this.m_110855_(k, k2, k1)];
               BlockPos blockpos = chunkrenderdispatcher$renderchunk.m_112839_();
               if (j1 != blockpos.getX() || l2 != blockpos.getY() || j2 != blockpos.getZ()) {
                  chunkrenderdispatcher$renderchunk.m_112801_(j1, l2, j2);
               }
            }
         }
      }

   }

   public void setDirty(int pSectionX, int pSectionY, int pSectionZ, boolean pReRenderOnMainThread) {
      int i = Math.floorMod(pSectionX, this.f_110841_);
      int j = Math.floorMod(pSectionY - this.level.getMinSection(), this.f_110840_);
      int k = Math.floorMod(pSectionZ, this.f_110842_);
      ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.f_110843_[this.m_110855_(i, j, k)];
      chunkrenderdispatcher$renderchunk.m_112828_(pReRenderOnMainThread);
   }

   @Nullable
   protected ChunkRenderDispatcher.RenderChunk m_110866_(BlockPos p_110867_) {
      int i = Mth.floorDiv(p_110867_.getX(), 16);
      int j = Mth.floorDiv(p_110867_.getY() - this.level.getMinBuildHeight(), 16);
      int k = Mth.floorDiv(p_110867_.getZ(), 16);
      if (j >= 0 && j < this.f_110840_) {
         i = Mth.positiveModulo(i, this.f_110841_);
         k = Mth.positiveModulo(k, this.f_110842_);
         return this.f_110843_[this.m_110855_(i, j, k)];
      } else {
         return null;
      }
   }
}