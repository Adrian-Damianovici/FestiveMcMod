package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ChunkRenderDispatcher {
   private static final Logger f_112672_ = LogUtils.getLogger();
   private static final int f_173707_ = 4;
   private static final VertexFormat f_173708_ = DefaultVertexFormat.BLOCK;
   private static final int f_194400_ = 2;
   private final PriorityBlockingQueue<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask> f_194401_ = Queues.newPriorityBlockingQueue();
   private final Queue<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask> f_194402_ = Queues.newLinkedBlockingDeque();
   private int f_194403_ = 2;
   private final Queue<ChunkBufferBuilderPack> f_112674_;
   private final Queue<Runnable> f_112675_ = Queues.newConcurrentLinkedQueue();
   private volatile int f_112676_;
   private volatile int f_112677_;
   final ChunkBufferBuilderPack f_112678_;
   private final ProcessorMailbox<Runnable> f_112679_;
   private final Executor f_112680_;
   ClientLevel f_112681_;
   final LevelRenderer f_112682_;
   private Vec3 f_112683_ = Vec3.ZERO;

   public ChunkRenderDispatcher(ClientLevel p_194405_, LevelRenderer p_194406_, Executor p_194407_, boolean p_194408_, ChunkBufferBuilderPack p_194409_) {
      this(p_194405_, p_194406_, p_194407_, p_194408_, p_194409_, -1);
   }
   public ChunkRenderDispatcher(ClientLevel p_194405_, LevelRenderer p_194406_, Executor p_194407_, boolean p_194408_, ChunkBufferBuilderPack p_194409_, int countRenderBuilders) {
      this.f_112681_ = p_194405_;
      this.f_112682_ = p_194406_;
      int i = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3D) / (RenderType.chunkBufferLayers().stream().mapToInt(RenderType::bufferSize).sum() * 4) - 1);
      int j = Runtime.getRuntime().availableProcessors();
      int k = p_194408_ ? j : Math.min(j, 4);
      int l = countRenderBuilders < 0 ? Math.max(1, Math.min(k, i)) : countRenderBuilders;
      this.f_112678_ = p_194409_;
      List<ChunkBufferBuilderPack> list = Lists.newArrayListWithExpectedSize(l);

      try {
         for(int i1 = 0; i1 < l; ++i1) {
            list.add(new ChunkBufferBuilderPack());
         }
      } catch (OutOfMemoryError outofmemoryerror) {
         f_112672_.warn("Allocated only {}/{} buffers", list.size(), l);
         int j1 = Math.min(list.size() * 2 / 3, list.size() - 1);

         for(int k1 = 0; k1 < j1; ++k1) {
            list.remove(list.size() - 1);
         }

         System.gc();
      }

      this.f_112674_ = Queues.newArrayDeque(list);
      this.f_112677_ = this.f_112674_.size();
      this.f_112680_ = p_194407_;
      this.f_112679_ = ProcessorMailbox.create(p_194407_, "Chunk Renderer");
      this.f_112679_.tell(this::m_112734_);
   }

   public void m_194410_(ClientLevel p_194411_) {
      this.f_112681_ = p_194411_;
   }

   private void m_112734_() {
      if (!this.f_112674_.isEmpty()) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask = this.m_194418_();
         if (chunkrenderdispatcher$renderchunk$chunkcompiletask != null) {
            ChunkBufferBuilderPack chunkbufferbuilderpack = this.f_112674_.poll();
            this.f_112676_ = this.f_194401_.size() + this.f_194402_.size();
            this.f_112677_ = this.f_112674_.size();
            CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName(chunkrenderdispatcher$renderchunk$chunkcompiletask.m_183497_(), () -> {
               return chunkrenderdispatcher$renderchunk$chunkcompiletask.m_5869_(chunkbufferbuilderpack);
            }), this.f_112680_).thenCompose((p_194416_) -> {
               return p_194416_;
            }).whenComplete((p_234458_, p_234459_) -> {
               if (p_234459_ != null) {
                  Minecraft.getInstance().delayCrash(CrashReport.forThrowable(p_234459_, "Batching chunks"));
               } else {
                  this.f_112679_.tell(() -> {
                     if (p_234458_ == ChunkRenderDispatcher.ChunkTaskResult.SUCCESSFUL) {
                        chunkbufferbuilderpack.m_108838_();
                     } else {
                        chunkbufferbuilderpack.m_108841_();
                     }

                     this.f_112674_.add(chunkbufferbuilderpack);
                     this.f_112677_ = this.f_112674_.size();
                     this.m_112734_();
                  });
               }
            });
         }
      }
   }

   @Nullable
   private ChunkRenderDispatcher.RenderChunk.ChunkCompileTask m_194418_() {
      if (this.f_194403_ <= 0) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask = this.f_194402_.poll();
         if (chunkrenderdispatcher$renderchunk$chunkcompiletask != null) {
            this.f_194403_ = 2;
            return chunkrenderdispatcher$renderchunk$chunkcompiletask;
         }
      }

      ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask1 = this.f_194401_.poll();
      if (chunkrenderdispatcher$renderchunk$chunkcompiletask1 != null) {
         --this.f_194403_;
         return chunkrenderdispatcher$renderchunk$chunkcompiletask1;
      } else {
         this.f_194403_ = 2;
         return this.f_194402_.poll();
      }
   }

   public String m_112719_() {
      return String.format(Locale.ROOT, "pC: %03d, pU: %02d, aB: %02d", this.f_112676_, this.f_112675_.size(), this.f_112677_);
   }

   public int m_173712_() {
      return this.f_112676_;
   }

   public int m_173713_() {
      return this.f_112675_.size();
   }

   public int m_173714_() {
      return this.f_112677_;
   }

   public void m_112693_(Vec3 p_112694_) {
      this.f_112683_ = p_112694_;
   }

   public Vec3 m_112727_() {
      return this.f_112683_;
   }

   public void m_194417_() {
      Runnable runnable;
      while((runnable = this.f_112675_.poll()) != null) {
         runnable.run();
      }

   }

   public void m_200431_(ChunkRenderDispatcher.RenderChunk p_200432_, RenderRegionCache p_200433_) {
      p_200432_.m_200439_(p_200433_);
   }

   public void m_112731_() {
      this.m_112735_();
   }

   public void m_112709_(ChunkRenderDispatcher.RenderChunk.ChunkCompileTask p_112710_) {
      this.f_112679_.tell(() -> {
         if (p_112710_.f_194420_) {
            this.f_194401_.offer(p_112710_);
         } else {
            this.f_194402_.offer(p_112710_);
         }

         this.f_112676_ = this.f_194401_.size() + this.f_194402_.size();
         this.m_112734_();
      });
   }

   public CompletableFuture<Void> m_234450_(BufferBuilder.RenderedBuffer p_234451_, VertexBuffer p_234452_) {
      return CompletableFuture.runAsync(() -> {
         if (!p_234452_.isInvalid()) {
            p_234452_.bind();
            p_234452_.upload(p_234451_);
            VertexBuffer.unbind();
         }
      }, this.f_112675_::add);
   }

   private void m_112735_() {
      while(!this.f_194401_.isEmpty()) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask = this.f_194401_.poll();
         if (chunkrenderdispatcher$renderchunk$chunkcompiletask != null) {
            chunkrenderdispatcher$renderchunk$chunkcompiletask.m_6204_();
         }
      }

      while(!this.f_194402_.isEmpty()) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask1 = this.f_194402_.poll();
         if (chunkrenderdispatcher$renderchunk$chunkcompiletask1 != null) {
            chunkrenderdispatcher$renderchunk$chunkcompiletask1.m_6204_();
         }
      }

      this.f_112676_ = 0;
   }

   public boolean m_112732_() {
      return this.f_112676_ == 0 && this.f_112675_.isEmpty();
   }

   public void m_112733_() {
      this.m_112735_();
      this.f_112679_.close();
      this.f_112674_.clear();
   }

   @OnlyIn(Dist.CLIENT)
   static enum ChunkTaskResult {
      SUCCESSFUL,
      CANCELLED;
   }

   @OnlyIn(Dist.CLIENT)
   public static class CompiledChunk {
      public static final ChunkRenderDispatcher.CompiledChunk f_112748_ = new ChunkRenderDispatcher.CompiledChunk() {
         public boolean m_7259_(Direction p_112782_, Direction p_112783_) {
            return false;
         }
      };
      final Set<RenderType> f_112749_ = new ObjectArraySet<>(RenderType.chunkBufferLayers().size());
      final List<BlockEntity> f_112752_ = Lists.newArrayList();
      VisibilitySet f_112753_ = new VisibilitySet();
      @Nullable
      BufferBuilder.SortState f_112754_;

      public boolean m_112757_() {
         return this.f_112749_.isEmpty();
      }

      public boolean m_112758_(RenderType p_112759_) {
         return !this.f_112749_.contains(p_112759_);
      }

      public List<BlockEntity> m_112773_() {
         return this.f_112752_;
      }

      public boolean m_7259_(Direction p_112771_, Direction p_112772_) {
         return this.f_112753_.visibilityBetween(p_112771_, p_112772_);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public class RenderChunk {
      public static final int f_173716_ = 16;
      public final int f_173717_;
      public final AtomicReference<ChunkRenderDispatcher.CompiledChunk> f_112784_ = new AtomicReference<>(ChunkRenderDispatcher.CompiledChunk.f_112748_);
      final AtomicInteger f_202433_ = new AtomicInteger(0);
      @Nullable
      private ChunkRenderDispatcher.RenderChunk.RebuildTask f_112787_;
      @Nullable
      private ChunkRenderDispatcher.RenderChunk.ResortTransparencyTask f_112788_;
      private final Set<BlockEntity> f_112789_ = Sets.newHashSet();
      private final Map<RenderType, VertexBuffer> f_112790_ = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap((p_112837_) -> {
         return p_112837_;
      }, (p_286178_) -> {
         return new VertexBuffer(VertexBuffer.Usage.STATIC);
      }));
      private AABB f_112785_;
      private boolean f_112792_ = true;
      final BlockPos.MutableBlockPos f_112793_ = new BlockPos.MutableBlockPos(-1, -1, -1);
      private final BlockPos.MutableBlockPos[] f_112794_ = Util.make(new BlockPos.MutableBlockPos[6], (p_112831_) -> {
         for(int i = 0; i < p_112831_.length; ++i) {
            p_112831_[i] = new BlockPos.MutableBlockPos();
         }

      });
      private boolean f_112795_;

      public RenderChunk(int p_202436_, int p_202437_, int p_202438_, int p_202439_) {
         this.f_173717_ = p_202436_;
         this.m_112801_(p_202437_, p_202438_, p_202439_);
      }

      private boolean m_112822_(BlockPos p_112823_) {
         return ChunkRenderDispatcher.this.f_112681_.getChunk(SectionPos.blockToSectionCoord(p_112823_.getX()), SectionPos.blockToSectionCoord(p_112823_.getZ()), ChunkStatus.FULL, false) != null;
      }

      public boolean m_112798_() {
         int i = 24;
         if (!(this.m_112832_() > 576.0D)) {
            return true;
         } else {
            return this.m_112822_(this.f_112794_[Direction.WEST.ordinal()]) && this.m_112822_(this.f_112794_[Direction.NORTH.ordinal()]) && this.m_112822_(this.f_112794_[Direction.EAST.ordinal()]) && this.m_112822_(this.f_112794_[Direction.SOUTH.ordinal()]);
         }
      }

      public AABB m_202440_() {
         return this.f_112785_;
      }

      public VertexBuffer m_112807_(RenderType p_112808_) {
         return this.f_112790_.get(p_112808_);
      }

      public void m_112801_(int p_112802_, int p_112803_, int p_112804_) {
         this.m_112846_();
         this.f_112793_.set(p_112802_, p_112803_, p_112804_);
         this.f_112785_ = new AABB((double)p_112802_, (double)p_112803_, (double)p_112804_, (double)(p_112802_ + 16), (double)(p_112803_ + 16), (double)(p_112804_ + 16));

         for(Direction direction : Direction.values()) {
            this.f_112794_[direction.ordinal()].set(this.f_112793_).move(direction, 16);
         }

      }

      protected double m_112832_() {
         Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
         double d0 = this.f_112785_.minX + 8.0D - camera.getPosition().x;
         double d1 = this.f_112785_.minY + 8.0D - camera.getPosition().y;
         double d2 = this.f_112785_.minZ + 8.0D - camera.getPosition().z;
         return d0 * d0 + d1 * d1 + d2 * d2;
      }

      void m_112805_(BufferBuilder p_112806_) {
         p_112806_.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
      }

      public ChunkRenderDispatcher.CompiledChunk m_112835_() {
         return this.f_112784_.get();
      }

      private void m_112846_() {
         this.m_194419_();
         this.f_112784_.set(ChunkRenderDispatcher.CompiledChunk.f_112748_);
         this.f_112792_ = true;
      }

      public void m_112838_() {
         this.m_112846_();
         this.f_112790_.values().forEach(VertexBuffer::close);
      }

      public BlockPos m_112839_() {
         return this.f_112793_;
      }

      public void m_112828_(boolean p_112829_) {
         boolean flag = this.f_112792_;
         this.f_112792_ = true;
         this.f_112795_ = p_112829_ | (flag && this.f_112795_);
      }

      public void m_112840_() {
         this.f_112792_ = false;
         this.f_112795_ = false;
      }

      public boolean m_112841_() {
         return this.f_112792_;
      }

      public boolean m_112842_() {
         return this.f_112792_ && this.f_112795_;
      }

      public BlockPos m_112824_(Direction p_112825_) {
         return this.f_112794_[p_112825_.ordinal()];
      }

      public boolean m_112809_(RenderType p_112810_, ChunkRenderDispatcher p_112811_) {
         ChunkRenderDispatcher.CompiledChunk chunkrenderdispatcher$compiledchunk = this.m_112835_();
         if (this.f_112788_ != null) {
            this.f_112788_.m_6204_();
         }

         if (!chunkrenderdispatcher$compiledchunk.f_112749_.contains(p_112810_)) {
            return false;
         } else {
            this.f_112788_ = new ChunkRenderDispatcher.RenderChunk.ResortTransparencyTask(new net.minecraft.world.level.ChunkPos(m_112839_()), this.m_112832_(), chunkrenderdispatcher$compiledchunk);
            p_112811_.m_112709_(this.f_112788_);
            return true;
         }
      }

      protected boolean m_194419_() {
         boolean flag = false;
         if (this.f_112787_ != null) {
            this.f_112787_.m_6204_();
            this.f_112787_ = null;
            flag = true;
         }

         if (this.f_112788_ != null) {
            this.f_112788_.m_6204_();
            this.f_112788_ = null;
         }

         return flag;
      }

      public ChunkRenderDispatcher.RenderChunk.ChunkCompileTask m_200437_(RenderRegionCache p_200438_) {
         boolean flag = this.m_194419_();
         BlockPos blockpos = this.f_112793_.immutable();
         int i = 1;
         RenderChunkRegion renderchunkregion = p_200438_.createRegion(ChunkRenderDispatcher.this.f_112681_, blockpos.offset(-1, -1, -1), blockpos.offset(16, 16, 16), 1);
         boolean flag1 = this.f_112784_.get() == ChunkRenderDispatcher.CompiledChunk.f_112748_;
         if (flag1 && flag) {
            this.f_202433_.incrementAndGet();
         }

         this.f_112787_ = new ChunkRenderDispatcher.RenderChunk.RebuildTask(new net.minecraft.world.level.ChunkPos(m_112839_()), this.m_112832_(), renderchunkregion, flag || this.f_112784_.get() != ChunkRenderDispatcher.CompiledChunk.f_112748_);
         return this.f_112787_;
      }

      public void m_200434_(ChunkRenderDispatcher p_200435_, RenderRegionCache p_200436_) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask = this.m_200437_(p_200436_);
         p_200435_.m_112709_(chunkrenderdispatcher$renderchunk$chunkcompiletask);
      }

      void m_234465_(Collection<BlockEntity> p_234466_) {
         Set<BlockEntity> set = Sets.newHashSet(p_234466_);
         Set<BlockEntity> set1;
         synchronized(this.f_112789_) {
            set1 = Sets.newHashSet(this.f_112789_);
            set.removeAll(this.f_112789_);
            set1.removeAll(p_234466_);
            this.f_112789_.clear();
            this.f_112789_.addAll(p_234466_);
         }

         ChunkRenderDispatcher.this.f_112682_.updateGlobalBlockEntities(set1, set);
      }

      public void m_200439_(RenderRegionCache p_200440_) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask = this.m_200437_(p_200440_);
         chunkrenderdispatcher$renderchunk$chunkcompiletask.m_5869_(ChunkRenderDispatcher.this.f_112678_);
      }

      @OnlyIn(Dist.CLIENT)
      abstract class ChunkCompileTask implements Comparable<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask> {
         protected final double f_112847_;
         protected final AtomicBoolean f_112848_ = new AtomicBoolean(false);
         protected final boolean f_194420_;
         protected java.util.Map<net.minecraft.core.BlockPos, net.minecraftforge.client.model.data.ModelData> modelData;

         public ChunkCompileTask(double p_194423_, boolean p_194424_) {
            this(null, p_194423_, p_194424_);
         }

         public ChunkCompileTask(@Nullable net.minecraft.world.level.ChunkPos pos, double p_194423_, boolean p_194424_) {
            this.f_112847_ = p_194423_;
            this.f_194420_ = p_194424_;
            if (pos == null) {
               this.modelData = java.util.Collections.emptyMap();
            } else {
               this.modelData = net.minecraft.client.Minecraft.getInstance().level.getModelDataManager().getAt(pos);
            }
         }

         public abstract CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> m_5869_(ChunkBufferBuilderPack p_112853_);

         public abstract void m_6204_();

         protected abstract String m_183497_();

         public int compareTo(ChunkRenderDispatcher.RenderChunk.ChunkCompileTask p_112855_) {
            return Doubles.compare(this.f_112847_, p_112855_.f_112847_);
         }

         public net.minecraftforge.client.model.data.ModelData getModelData(net.minecraft.core.BlockPos pos) {
            return modelData.getOrDefault(pos, net.minecraftforge.client.model.data.ModelData.EMPTY);
         }
      }

      @OnlyIn(Dist.CLIENT)
      class RebuildTask extends ChunkRenderDispatcher.RenderChunk.ChunkCompileTask {
         @Nullable
         protected RenderChunkRegion f_112858_;

         @Deprecated
         public RebuildTask(double p_194427_, @Nullable RenderChunkRegion p_194428_, boolean p_194429_) {
            this(null, p_194427_, p_194428_, p_194429_);
         }

         public RebuildTask(@Nullable net.minecraft.world.level.ChunkPos pos, double p_194427_, @Nullable RenderChunkRegion p_194428_, boolean p_194429_) {
            super(pos, p_194427_, p_194429_);
            this.f_112858_ = p_194428_;
         }

         protected String m_183497_() {
            return "rend_chk_rebuild";
         }

         public CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> m_5869_(ChunkBufferBuilderPack p_112872_) {
            if (this.f_112848_.get()) {
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else if (!RenderChunk.this.m_112798_()) {
               this.f_112858_ = null;
               RenderChunk.this.m_112828_(false);
               this.f_112848_.set(true);
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else if (this.f_112848_.get()) {
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else {
               Vec3 vec3 = ChunkRenderDispatcher.this.m_112727_();
               float f = (float)vec3.x;
               float f1 = (float)vec3.y;
               float f2 = (float)vec3.z;
               ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults chunkrenderdispatcher$renderchunk$rebuildtask$compileresults = this.m_234467_(f, f1, f2, p_112872_);
               RenderChunk.this.m_234465_(chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.f_234484_);
               if (this.f_112848_.get()) {
                  chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.f_234486_.values().forEach(BufferBuilder.RenderedBuffer::release);
                  return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
               } else {
                  ChunkRenderDispatcher.CompiledChunk chunkrenderdispatcher$compiledchunk = new ChunkRenderDispatcher.CompiledChunk();
                  chunkrenderdispatcher$compiledchunk.f_112753_ = chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.f_234487_;
                  chunkrenderdispatcher$compiledchunk.f_112752_.addAll(chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.f_234485_);
                  chunkrenderdispatcher$compiledchunk.f_112754_ = chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.f_234488_;
                  List<CompletableFuture<Void>> list = Lists.newArrayList();
                  chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.f_234486_.forEach((p_234482_, p_234483_) -> {
                     list.add(ChunkRenderDispatcher.this.m_234450_(p_234483_, RenderChunk.this.m_112807_(p_234482_)));
                     chunkrenderdispatcher$compiledchunk.f_112749_.add(p_234482_);
                  });
                  return Util.sequenceFailFast(list).handle((p_234474_, p_234475_) -> {
                     if (p_234475_ != null && !(p_234475_ instanceof CancellationException) && !(p_234475_ instanceof InterruptedException)) {
                        Minecraft.getInstance().delayCrash(CrashReport.forThrowable(p_234475_, "Rendering chunk"));
                     }

                     if (this.f_112848_.get()) {
                        return ChunkRenderDispatcher.ChunkTaskResult.CANCELLED;
                     } else {
                        RenderChunk.this.f_112784_.set(chunkrenderdispatcher$compiledchunk);
                        RenderChunk.this.f_202433_.set(0);
                        ChunkRenderDispatcher.this.f_112682_.m_194352_(RenderChunk.this);
                        return ChunkRenderDispatcher.ChunkTaskResult.SUCCESSFUL;
                     }
                  });
               }
            }
         }

         private ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults m_234467_(float p_234468_, float p_234469_, float p_234470_, ChunkBufferBuilderPack p_234471_) {
            ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults chunkrenderdispatcher$renderchunk$rebuildtask$compileresults = new ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults();
            int i = 1;
            BlockPos blockpos = RenderChunk.this.f_112793_.immutable();
            BlockPos blockpos1 = blockpos.offset(15, 15, 15);
            VisGraph visgraph = new VisGraph();
            RenderChunkRegion renderchunkregion = this.f_112858_;
            this.f_112858_ = null;
            PoseStack posestack = new PoseStack();
            if (renderchunkregion != null) {
               ModelBlockRenderer.enableCaching();
               Set<RenderType> set = new ReferenceArraySet<>(RenderType.chunkBufferLayers().size());
               RandomSource randomsource = RandomSource.create();
               BlockRenderDispatcher blockrenderdispatcher = Minecraft.getInstance().getBlockRenderer();

               for(BlockPos blockpos2 : BlockPos.betweenClosed(blockpos, blockpos1)) {
                  BlockState blockstate = renderchunkregion.getBlockState(blockpos2);
                  if (blockstate.isSolidRender(renderchunkregion, blockpos2)) {
                     visgraph.setOpaque(blockpos2);
                  }

                  if (blockstate.hasBlockEntity()) {
                     BlockEntity blockentity = renderchunkregion.getBlockEntity(blockpos2);
                     if (blockentity != null) {
                        this.m_234476_(chunkrenderdispatcher$renderchunk$rebuildtask$compileresults, blockentity);
                     }
                  }

                  BlockState blockstate1 = renderchunkregion.getBlockState(blockpos2);
                  FluidState fluidstate = blockstate1.getFluidState();
                  if (!fluidstate.isEmpty()) {
                     RenderType rendertype = ItemBlockRenderTypes.getRenderLayer(fluidstate);
                     BufferBuilder bufferbuilder = p_234471_.m_108839_(rendertype);
                     if (set.add(rendertype)) {
                        RenderChunk.this.m_112805_(bufferbuilder);
                     }

                     blockrenderdispatcher.renderLiquid(blockpos2, renderchunkregion, bufferbuilder, blockstate1, fluidstate);
                  }

                  if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                     var model = blockrenderdispatcher.getBlockModel(blockstate);
                     var modelData = model.getModelData(renderchunkregion, blockpos2, blockstate, getModelData(blockpos2));
                     randomsource.setSeed(blockstate.getSeed(blockpos2));
                     for (RenderType rendertype2 : model.getRenderTypes(blockstate, randomsource, modelData)) {
                     BufferBuilder bufferbuilder2 = p_234471_.m_108839_(rendertype2);
                     if (set.add(rendertype2)) {
                        RenderChunk.this.m_112805_(bufferbuilder2);
                     }

                     posestack.pushPose();
                     posestack.translate((float)(blockpos2.getX() & 15), (float)(blockpos2.getY() & 15), (float)(blockpos2.getZ() & 15));
                     blockrenderdispatcher.renderBatched(blockstate, blockpos2, renderchunkregion, posestack, bufferbuilder2, true, randomsource, modelData, rendertype2);
                     posestack.popPose();
                     }
                  }
               }

               if (set.contains(RenderType.translucent())) {
                  BufferBuilder bufferbuilder1 = p_234471_.m_108839_(RenderType.translucent());
                  if (!bufferbuilder1.isCurrentBatchEmpty()) {
                     bufferbuilder1.setQuadSorting(VertexSorting.byDistance(p_234468_ - (float)blockpos.getX(), p_234469_ - (float)blockpos.getY(), p_234470_ - (float)blockpos.getZ()));
                     chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.f_234488_ = bufferbuilder1.getSortState();
                  }
               }

               for(RenderType rendertype1 : set) {
                  BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = p_234471_.m_108839_(rendertype1).endOrDiscardIfEmpty();
                  if (bufferbuilder$renderedbuffer != null) {
                     chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.f_234486_.put(rendertype1, bufferbuilder$renderedbuffer);
                  }
               }

               ModelBlockRenderer.clearCache();
            }

            chunkrenderdispatcher$renderchunk$rebuildtask$compileresults.f_234487_ = visgraph.resolve();
            return chunkrenderdispatcher$renderchunk$rebuildtask$compileresults;
         }

         private <E extends BlockEntity> void m_234476_(ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults p_234477_, E p_234478_) {
            BlockEntityRenderer<E> blockentityrenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(p_234478_);
            if (blockentityrenderer != null) {
               if (blockentityrenderer.shouldRenderOffScreen(p_234478_)) {
                  p_234477_.f_234484_.add(p_234478_);
               }
               else p_234477_.f_234485_.add(p_234478_); //FORGE: Fix MC-112730
            }

         }

         public void m_6204_() {
            this.f_112858_ = null;
            if (this.f_112848_.compareAndSet(false, true)) {
               RenderChunk.this.m_112828_(false);
            }

         }

         @OnlyIn(Dist.CLIENT)
         static final class CompileResults {
            public final List<BlockEntity> f_234484_ = new ArrayList<>();
            public final List<BlockEntity> f_234485_ = new ArrayList<>();
            public final Map<RenderType, BufferBuilder.RenderedBuffer> f_234486_ = new Reference2ObjectArrayMap<>();
            public VisibilitySet f_234487_ = new VisibilitySet();
            @Nullable
            public BufferBuilder.SortState f_234488_;
         }
      }

      @OnlyIn(Dist.CLIENT)
      class ResortTransparencyTask extends ChunkRenderDispatcher.RenderChunk.ChunkCompileTask {
         private final ChunkRenderDispatcher.CompiledChunk f_112886_;

         @Deprecated
         public ResortTransparencyTask(double p_112889_, ChunkRenderDispatcher.CompiledChunk p_112890_) {
            this(null, p_112889_, p_112890_);
         }

         public ResortTransparencyTask(@Nullable net.minecraft.world.level.ChunkPos pos, double p_112889_, ChunkRenderDispatcher.CompiledChunk p_112890_) {
            super(pos, p_112889_, true);
            this.f_112886_ = p_112890_;
         }

         protected String m_183497_() {
            return "rend_chk_sort";
         }

         public CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> m_5869_(ChunkBufferBuilderPack p_112893_) {
            if (this.f_112848_.get()) {
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else if (!RenderChunk.this.m_112798_()) {
               this.f_112848_.set(true);
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else if (this.f_112848_.get()) {
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else {
               Vec3 vec3 = ChunkRenderDispatcher.this.m_112727_();
               float f = (float)vec3.x;
               float f1 = (float)vec3.y;
               float f2 = (float)vec3.z;
               BufferBuilder.SortState bufferbuilder$sortstate = this.f_112886_.f_112754_;
               if (bufferbuilder$sortstate != null && !this.f_112886_.m_112758_(RenderType.translucent())) {
                  BufferBuilder bufferbuilder = p_112893_.m_108839_(RenderType.translucent());
                  RenderChunk.this.m_112805_(bufferbuilder);
                  bufferbuilder.restoreSortState(bufferbuilder$sortstate);
                  bufferbuilder.setQuadSorting(VertexSorting.byDistance(f - (float)RenderChunk.this.f_112793_.getX(), f1 - (float)RenderChunk.this.f_112793_.getY(), f2 - (float)RenderChunk.this.f_112793_.getZ()));
                  this.f_112886_.f_112754_ = bufferbuilder.getSortState();
                  BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = bufferbuilder.end();
                  if (this.f_112848_.get()) {
                     bufferbuilder$renderedbuffer.release();
                     return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                  } else {
                     CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> completablefuture = ChunkRenderDispatcher.this.m_234450_(bufferbuilder$renderedbuffer, RenderChunk.this.m_112807_(RenderType.translucent())).thenApply((p_112898_) -> {
                        return ChunkRenderDispatcher.ChunkTaskResult.CANCELLED;
                     });
                     return completablefuture.handle((p_234491_, p_234492_) -> {
                        if (p_234492_ != null && !(p_234492_ instanceof CancellationException) && !(p_234492_ instanceof InterruptedException)) {
                           Minecraft.getInstance().delayCrash(CrashReport.forThrowable(p_234492_, "Rendering chunk"));
                        }

                        return this.f_112848_.get() ? ChunkRenderDispatcher.ChunkTaskResult.CANCELLED : ChunkRenderDispatcher.ChunkTaskResult.SUCCESSFUL;
                     });
                  }
               } else {
                  return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
               }
            }
         }

         public void m_6204_() {
            this.f_112848_.set(true);
         }
      }
   }
}
