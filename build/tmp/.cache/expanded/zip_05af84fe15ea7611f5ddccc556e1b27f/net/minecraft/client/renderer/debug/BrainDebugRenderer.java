package net.minecraft.client.renderer.debug;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BrainDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final boolean SHOW_NAME_FOR_ALL = true;
   private static final boolean SHOW_PROFESSION_FOR_ALL = false;
   private static final boolean SHOW_BEHAVIORS_FOR_ALL = false;
   private static final boolean SHOW_ACTIVITIES_FOR_ALL = false;
   private static final boolean SHOW_INVENTORY_FOR_ALL = false;
   private static final boolean SHOW_GOSSIPS_FOR_ALL = false;
   private static final boolean SHOW_PATH_FOR_ALL = false;
   private static final boolean SHOW_HEALTH_FOR_ALL = false;
   private static final boolean SHOW_WANTS_GOLEM_FOR_ALL = true;
   private static final boolean SHOW_ANGER_LEVEL_FOR_ALL = false;
   private static final boolean SHOW_NAME_FOR_SELECTED = true;
   private static final boolean SHOW_PROFESSION_FOR_SELECTED = true;
   private static final boolean SHOW_BEHAVIORS_FOR_SELECTED = true;
   private static final boolean SHOW_ACTIVITIES_FOR_SELECTED = true;
   private static final boolean SHOW_MEMORIES_FOR_SELECTED = true;
   private static final boolean SHOW_INVENTORY_FOR_SELECTED = true;
   private static final boolean SHOW_GOSSIPS_FOR_SELECTED = true;
   private static final boolean SHOW_PATH_FOR_SELECTED = true;
   private static final boolean SHOW_HEALTH_FOR_SELECTED = true;
   private static final boolean SHOW_WANTS_GOLEM_FOR_SELECTED = true;
   private static final boolean SHOW_ANGER_LEVEL_FOR_SELECTED = true;
   private static final boolean SHOW_POI_INFO = true;
   private static final int MAX_RENDER_DIST_FOR_BRAIN_INFO = 30;
   private static final int MAX_RENDER_DIST_FOR_POI_INFO = 30;
   private static final int MAX_TARGETING_DIST = 8;
   private static final float TEXT_SCALE = 0.02F;
   private static final int WHITE = -1;
   private static final int YELLOW = -256;
   private static final int CYAN = -16711681;
   private static final int GREEN = -16711936;
   private static final int GRAY = -3355444;
   private static final int PINK = -98404;
   private static final int RED = -65536;
   private static final int ORANGE = -23296;
   private final Minecraft minecraft;
   private final Map<BlockPos, BrainDebugRenderer.PoiInfo> pois = Maps.newHashMap();
   private final Map<UUID, BrainDebugRenderer.BrainDump> brainDumpsPerEntity = Maps.newHashMap();
   @Nullable
   private UUID lastLookedAtUuid;

   public BrainDebugRenderer(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void clear() {
      this.pois.clear();
      this.brainDumpsPerEntity.clear();
      this.lastLookedAtUuid = null;
   }

   public void addPoi(BrainDebugRenderer.PoiInfo pPoiInfo) {
      this.pois.put(pPoiInfo.pos, pPoiInfo);
   }

   public void removePoi(BlockPos pPos) {
      this.pois.remove(pPos);
   }

   public void setFreeTicketCount(BlockPos pPos, int pFreeTicketCount) {
      BrainDebugRenderer.PoiInfo braindebugrenderer$poiinfo = this.pois.get(pPos);
      if (braindebugrenderer$poiinfo == null) {
         LOGGER.warn("Strange, setFreeTicketCount was called for an unknown POI: {}", (Object)pPos);
      } else {
         braindebugrenderer$poiinfo.freeTicketCount = pFreeTicketCount;
      }
   }

   public void addOrUpdateBrainDump(BrainDebugRenderer.BrainDump p_113220_) {
      this.brainDumpsPerEntity.put(p_113220_.f_113293_, p_113220_);
   }

   public void removeBrainDump(int pId) {
      this.brainDumpsPerEntity.values().removeIf((p_173814_) -> {
         return p_173814_.f_113294_ == pId;
      });
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, double pCamX, double pCamY, double pCamZ) {
      this.clearRemovedEntities();
      this.doRender(pPoseStack, pBuffer, pCamX, pCamY, pCamZ);
      if (!this.minecraft.player.isSpectator()) {
         this.updateLastLookedAtUuid();
      }

   }

   private void clearRemovedEntities() {
      this.brainDumpsPerEntity.entrySet().removeIf((p_113263_) -> {
         Entity entity = this.minecraft.level.getEntity((p_113263_.getValue()).f_113294_);
         return entity == null || entity.isRemoved();
      });
   }

   private void doRender(PoseStack pPoseStack, MultiBufferSource pBuffer, double pX, double pY, double pZ) {
      BlockPos blockpos = BlockPos.containing(pX, pY, pZ);
      this.brainDumpsPerEntity.values().forEach((p_269714_) -> {
         if (this.isPlayerCloseEnoughToMob(p_269714_)) {
            this.renderBrainInfo(pPoseStack, pBuffer, p_269714_, pX, pY, pZ);
         }

      });

      for(BlockPos blockpos1 : this.pois.keySet()) {
         if (blockpos.closerThan(blockpos1, 30.0D)) {
            highlightPoi(pPoseStack, pBuffer, blockpos1);
         }
      }

      this.pois.values().forEach((p_269718_) -> {
         if (blockpos.closerThan(p_269718_.pos, 30.0D)) {
            this.renderPoiInfo(pPoseStack, pBuffer, p_269718_);
         }

      });
      this.getGhostPois().forEach((p_269707_, p_269708_) -> {
         if (blockpos.closerThan(p_269707_, 30.0D)) {
            this.renderGhostPoi(pPoseStack, pBuffer, p_269707_, p_269708_);
         }

      });
   }

   private static void highlightPoi(PoseStack pPoseStack, MultiBufferSource pBuffer, BlockPos pPos) {
      float f = 0.05F;
      DebugRenderer.renderFilledBox(pPoseStack, pBuffer, pPos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
   }

   private void renderGhostPoi(PoseStack pPoseStack, MultiBufferSource pBuffer, BlockPos pPos, List<String> p_270882_) {
      float f = 0.05F;
      DebugRenderer.renderFilledBox(pPoseStack, pBuffer, pPos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
      renderTextOverPos(pPoseStack, pBuffer, "" + p_270882_, pPos, 0, -256);
      renderTextOverPos(pPoseStack, pBuffer, "Ghost POI", pPos, 1, -65536);
   }

   private void renderPoiInfo(PoseStack pPoseStack, MultiBufferSource pBuffer, BrainDebugRenderer.PoiInfo pPoiInfo) {
      int i = 0;
      Set<String> set = this.getTicketHolderNames(pPoiInfo);
      if (set.size() < 4) {
         renderTextOverPoi(pPoseStack, pBuffer, "Owners: " + set, pPoiInfo, i, -256);
      } else {
         renderTextOverPoi(pPoseStack, pBuffer, set.size() + " ticket holders", pPoiInfo, i, -256);
      }

      ++i;
      Set<String> set1 = this.getPotentialTicketHolderNames(pPoiInfo);
      if (set1.size() < 4) {
         renderTextOverPoi(pPoseStack, pBuffer, "Candidates: " + set1, pPoiInfo, i, -23296);
      } else {
         renderTextOverPoi(pPoseStack, pBuffer, set1.size() + " potential owners", pPoiInfo, i, -23296);
      }

      ++i;
      renderTextOverPoi(pPoseStack, pBuffer, "Free tickets: " + pPoiInfo.freeTicketCount, pPoiInfo, i, -256);
      ++i;
      renderTextOverPoi(pPoseStack, pBuffer, pPoiInfo.type, pPoiInfo, i, -1);
   }

   private void renderPath(PoseStack pPoseStack, MultiBufferSource pBuffer, BrainDebugRenderer.BrainDump p_270979_, double pX, double pY, double pZ) {
      if (p_270979_.f_113302_ != null) {
         PathfindingRenderer.renderPath(pPoseStack, pBuffer, p_270979_.f_113302_, 0.5F, false, false, pX, pY, pZ);
      }

   }

   private void renderBrainInfo(PoseStack pPoseStack, MultiBufferSource pBuffer, BrainDebugRenderer.BrainDump p_270259_, double pX, double pY, double pZ) {
      boolean flag = this.isMobSelected(p_270259_);
      int i = 0;
      renderTextOverMob(pPoseStack, pBuffer, p_270259_.f_113300_, i, p_270259_.f_113295_, -1, 0.03F);
      ++i;
      if (flag) {
         renderTextOverMob(pPoseStack, pBuffer, p_270259_.f_113300_, i, p_270259_.f_113296_ + " " + p_270259_.f_113297_ + " xp", -1, 0.02F);
         ++i;
      }

      if (flag) {
         int j = p_270259_.f_113298_ < p_270259_.f_113299_ ? -23296 : -1;
         renderTextOverMob(pPoseStack, pBuffer, p_270259_.f_113300_, i, "health: " + String.format(Locale.ROOT, "%.1f", p_270259_.f_113298_) + " / " + String.format(Locale.ROOT, "%.1f", p_270259_.f_113299_), j, 0.02F);
         ++i;
      }

      if (flag && !p_270259_.f_113301_.equals("")) {
         renderTextOverMob(pPoseStack, pBuffer, p_270259_.f_113300_, i, p_270259_.f_113301_, -98404, 0.02F);
         ++i;
      }

      if (flag) {
         for(String s : p_270259_.f_113305_) {
            renderTextOverMob(pPoseStack, pBuffer, p_270259_.f_113300_, i, s, -16711681, 0.02F);
            ++i;
         }
      }

      if (flag) {
         for(String s1 : p_270259_.f_113304_) {
            renderTextOverMob(pPoseStack, pBuffer, p_270259_.f_113300_, i, s1, -16711936, 0.02F);
            ++i;
         }
      }

      if (p_270259_.f_113303_) {
         renderTextOverMob(pPoseStack, pBuffer, p_270259_.f_113300_, i, "Wants Golem", -23296, 0.02F);
         ++i;
      }

      if (flag && p_270259_.f_234495_ != -1) {
         renderTextOverMob(pPoseStack, pBuffer, p_270259_.f_113300_, i, "Anger Level: " + p_270259_.f_234495_, -98404, 0.02F);
         ++i;
      }

      if (flag) {
         for(String s2 : p_270259_.f_113307_) {
            if (s2.startsWith(p_270259_.f_113295_)) {
               renderTextOverMob(pPoseStack, pBuffer, p_270259_.f_113300_, i, s2, -1, 0.02F);
            } else {
               renderTextOverMob(pPoseStack, pBuffer, p_270259_.f_113300_, i, s2, -23296, 0.02F);
            }

            ++i;
         }
      }

      if (flag) {
         for(String s3 : Lists.reverse(p_270259_.f_113306_)) {
            renderTextOverMob(pPoseStack, pBuffer, p_270259_.f_113300_, i, s3, -3355444, 0.02F);
            ++i;
         }
      }

      if (flag) {
         this.renderPath(pPoseStack, pBuffer, p_270259_, pX, pY, pZ);
      }

   }

   private static void renderTextOverPoi(PoseStack pPoseStack, MultiBufferSource pBuffer, String pText, BrainDebugRenderer.PoiInfo pPoiInfo, int pLayer, int pColor) {
      renderTextOverPos(pPoseStack, pBuffer, pText, pPoiInfo.pos, pLayer, pColor);
   }

   private static void renderTextOverPos(PoseStack pPoseStack, MultiBufferSource pBuffer, String pText, BlockPos pPos, int pLayer, int pColor) {
      double d0 = 1.3D;
      double d1 = 0.2D;
      double d2 = (double)pPos.getX() + 0.5D;
      double d3 = (double)pPos.getY() + 1.3D + (double)pLayer * 0.2D;
      double d4 = (double)pPos.getZ() + 0.5D;
      DebugRenderer.renderFloatingText(pPoseStack, pBuffer, pText, d2, d3, d4, pColor, 0.02F, true, 0.0F, true);
   }

   private static void renderTextOverMob(PoseStack pPoseStack, MultiBufferSource pBuffer, Position pPos, int pLayer, String pText, int pColor, float pScale) {
      double d0 = 2.4D;
      double d1 = 0.25D;
      BlockPos blockpos = BlockPos.containing(pPos);
      double d2 = (double)blockpos.getX() + 0.5D;
      double d3 = pPos.y() + 2.4D + (double)pLayer * 0.25D;
      double d4 = (double)blockpos.getZ() + 0.5D;
      float f = 0.5F;
      DebugRenderer.renderFloatingText(pPoseStack, pBuffer, pText, d2, d3, d4, pColor, pScale, false, 0.5F, true);
   }

   private Set<String> getTicketHolderNames(BrainDebugRenderer.PoiInfo pPoiInfo) {
      return this.getTicketHolders(pPoiInfo.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
   }

   private Set<String> getPotentialTicketHolderNames(BrainDebugRenderer.PoiInfo pPoiInfo) {
      return this.getPotentialTicketHolders(pPoiInfo.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
   }

   private boolean isMobSelected(BrainDebugRenderer.BrainDump p_113266_) {
      return Objects.equals(this.lastLookedAtUuid, p_113266_.f_113293_);
   }

   private boolean isPlayerCloseEnoughToMob(BrainDebugRenderer.BrainDump p_113281_) {
      Player player = this.minecraft.player;
      BlockPos blockpos = BlockPos.containing(player.getX(), p_113281_.f_113300_.y(), player.getZ());
      BlockPos blockpos1 = BlockPos.containing(p_113281_.f_113300_);
      return blockpos.closerThan(blockpos1, 30.0D);
   }

   private Collection<UUID> getTicketHolders(BlockPos pPos) {
      return this.brainDumpsPerEntity.values().stream().filter((p_113278_) -> {
         return p_113278_.m_113326_(pPos);
      }).map(BrainDebugRenderer.BrainDump::m_113322_).collect(Collectors.toSet());
   }

   private Collection<UUID> getPotentialTicketHolders(BlockPos pPos) {
      return this.brainDumpsPerEntity.values().stream().filter((p_113235_) -> {
         return p_113235_.m_113331_(pPos);
      }).map(BrainDebugRenderer.BrainDump::m_113322_).collect(Collectors.toSet());
   }

   private Map<BlockPos, List<String>> getGhostPois() {
      Map<BlockPos, List<String>> map = Maps.newHashMap();

      for(BrainDebugRenderer.BrainDump braindebugrenderer$braindump : this.brainDumpsPerEntity.values()) {
         for(BlockPos blockpos : Iterables.concat(braindebugrenderer$braindump.f_113308_, braindebugrenderer$braindump.f_113309_)) {
            if (!this.pois.containsKey(blockpos)) {
               map.computeIfAbsent(blockpos, (p_113292_) -> {
                  return Lists.newArrayList();
               }).add(braindebugrenderer$braindump.f_113295_);
            }
         }
      }

      return map;
   }

   private void updateLastLookedAtUuid() {
      DebugRenderer.getTargetedEntity(this.minecraft.getCameraEntity(), 8).ifPresent((p_113212_) -> {
         this.lastLookedAtUuid = p_113212_.getUUID();
      });
   }

   @OnlyIn(Dist.CLIENT)
   public static class BrainDump {
      public final UUID f_113293_;
      public final int f_113294_;
      public final String f_113295_;
      public final String f_113296_;
      public final int f_113297_;
      public final float f_113298_;
      public final float f_113299_;
      public final Position f_113300_;
      public final String f_113301_;
      public final Path f_113302_;
      public final boolean f_113303_;
      public final int f_234495_;
      public final List<String> f_113304_ = Lists.newArrayList();
      public final List<String> f_113305_ = Lists.newArrayList();
      public final List<String> f_113306_ = Lists.newArrayList();
      public final List<String> f_113307_ = Lists.newArrayList();
      public final Set<BlockPos> f_113308_ = Sets.newHashSet();
      public final Set<BlockPos> f_113309_ = Sets.newHashSet();

      public BrainDump(UUID p_234497_, int p_234498_, String p_234499_, String p_234500_, int p_234501_, float p_234502_, float p_234503_, Position p_234504_, String p_234505_, @Nullable Path p_234506_, boolean p_234507_, int p_234508_) {
         this.f_113293_ = p_234497_;
         this.f_113294_ = p_234498_;
         this.f_113295_ = p_234499_;
         this.f_113296_ = p_234500_;
         this.f_113297_ = p_234501_;
         this.f_113298_ = p_234502_;
         this.f_113299_ = p_234503_;
         this.f_113300_ = p_234504_;
         this.f_113301_ = p_234505_;
         this.f_113302_ = p_234506_;
         this.f_113303_ = p_234507_;
         this.f_234495_ = p_234508_;
      }

      boolean m_113326_(BlockPos p_113327_) {
         return this.f_113308_.stream().anyMatch(p_113327_::equals);
      }

      boolean m_113331_(BlockPos p_113332_) {
         return this.f_113309_.contains(p_113332_);
      }

      public UUID m_113322_() {
         return this.f_113293_;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class PoiInfo {
      public final BlockPos pos;
      public String type;
      public int freeTicketCount;

      public PoiInfo(BlockPos pPos, String pType, int pFreeTicketCount) {
         this.pos = pPos;
         this.type = pType;
         this.freeTicketCount = pFreeTicketCount;
      }
   }
}