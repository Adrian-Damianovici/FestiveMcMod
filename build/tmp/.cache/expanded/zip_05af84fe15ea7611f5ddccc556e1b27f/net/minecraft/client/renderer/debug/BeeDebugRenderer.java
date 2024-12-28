package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeeDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private static final boolean SHOW_GOAL_FOR_ALL_BEES = true;
   private static final boolean SHOW_NAME_FOR_ALL_BEES = true;
   private static final boolean SHOW_HIVE_FOR_ALL_BEES = true;
   private static final boolean SHOW_FLOWER_POS_FOR_ALL_BEES = true;
   private static final boolean SHOW_TRAVEL_TICKS_FOR_ALL_BEES = true;
   private static final boolean SHOW_PATH_FOR_ALL_BEES = false;
   private static final boolean SHOW_GOAL_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_NAME_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_HIVE_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_FLOWER_POS_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_TRAVEL_TICKS_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_PATH_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_HIVE_MEMBERS = true;
   private static final boolean SHOW_BLACKLISTS = true;
   private static final int MAX_RENDER_DIST_FOR_HIVE_OVERLAY = 30;
   private static final int MAX_RENDER_DIST_FOR_BEE_OVERLAY = 30;
   private static final int MAX_TARGETING_DIST = 8;
   private static final int HIVE_TIMEOUT = 20;
   private static final float TEXT_SCALE = 0.02F;
   private static final int WHITE = -1;
   private static final int YELLOW = -256;
   private static final int ORANGE = -23296;
   private static final int GREEN = -16711936;
   private static final int GRAY = -3355444;
   private static final int PINK = -98404;
   private static final int RED = -65536;
   private final Minecraft minecraft;
   private final Map<BlockPos, BeeDebugRenderer.HiveInfo> hives = Maps.newHashMap();
   private final Map<UUID, BeeDebugRenderer.BeeInfo> beeInfosPerEntity = Maps.newHashMap();
   private UUID lastLookedAtUuid;

   public BeeDebugRenderer(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void clear() {
      this.hives.clear();
      this.beeInfosPerEntity.clear();
      this.lastLookedAtUuid = null;
   }

   public void addOrUpdateHiveInfo(BeeDebugRenderer.HiveInfo p_113072_) {
      this.hives.put(p_113072_.f_113180_, p_113072_);
   }

   public void addOrUpdateBeeInfo(BeeDebugRenderer.BeeInfo p_113067_) {
      this.beeInfosPerEntity.put(p_113067_.f_113157_, p_113067_);
   }

   public void removeBeeInfo(int pId) {
      this.beeInfosPerEntity.values().removeIf((p_173767_) -> {
         return p_173767_.f_113158_ == pId;
      });
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, double pCamX, double pCamY, double pCamZ) {
      this.clearRemovedHives();
      this.clearRemovedBees();
      this.doRender(pPoseStack, pBuffer);
      if (!this.minecraft.player.isSpectator()) {
         this.updateLastLookedAtUuid();
      }

   }

   private void clearRemovedBees() {
      this.beeInfosPerEntity.entrySet().removeIf((p_113132_) -> {
         return this.minecraft.level.getEntity((p_113132_.getValue()).f_113158_) == null;
      });
   }

   private void clearRemovedHives() {
      long i = this.minecraft.level.getGameTime() - 20L;
      this.hives.entrySet().removeIf((p_113057_) -> {
         return (p_113057_.getValue()).f_113185_ < i;
      });
   }

   private void doRender(PoseStack pPoseStack, MultiBufferSource pBuffer) {
      BlockPos blockpos = this.getCamera().getBlockPosition();
      this.beeInfosPerEntity.values().forEach((p_269703_) -> {
         if (this.isPlayerCloseEnoughToMob(p_269703_)) {
            this.renderBeeInfo(pPoseStack, pBuffer, p_269703_);
         }

      });
      this.renderFlowerInfos(pPoseStack, pBuffer);

      for(BlockPos blockpos1 : this.hives.keySet()) {
         if (blockpos.closerThan(blockpos1, 30.0D)) {
            highlightHive(pPoseStack, pBuffer, blockpos1);
         }
      }

      Map<BlockPos, Set<UUID>> map = this.createHiveBlacklistMap();
      this.hives.values().forEach((p_269692_) -> {
         if (blockpos.closerThan(p_269692_.f_113180_, 30.0D)) {
            Set<UUID> set = map.get(p_269692_.f_113180_);
            this.renderHiveInfo(pPoseStack, pBuffer, p_269692_, (Collection<UUID>)(set == null ? Sets.newHashSet() : set));
         }

      });
      this.getGhostHives().forEach((p_269699_, p_269700_) -> {
         if (blockpos.closerThan(p_269699_, 30.0D)) {
            this.renderGhostHive(pPoseStack, pBuffer, p_269699_, p_269700_);
         }

      });
   }

   private Map<BlockPos, Set<UUID>> createHiveBlacklistMap() {
      Map<BlockPos, Set<UUID>> map = Maps.newHashMap();
      this.beeInfosPerEntity.values().forEach((p_113135_) -> {
         p_113135_.f_113165_.forEach((p_173771_) -> {
            map.computeIfAbsent(p_173771_, (p_173777_) -> {
               return Sets.newHashSet();
            }).add(p_113135_.m_113174_());
         });
      });
      return map;
   }

   private void renderFlowerInfos(PoseStack pPoseStack, MultiBufferSource pBuffer) {
      Map<BlockPos, Set<UUID>> map = Maps.newHashMap();
      this.beeInfosPerEntity.values().stream().filter(BeeDebugRenderer.BeeInfo::m_113178_).forEach((p_113121_) -> {
         map.computeIfAbsent(p_113121_.f_113162_, (p_173775_) -> {
            return Sets.newHashSet();
         }).add(p_113121_.m_113174_());
      });
      map.entrySet().forEach((p_269695_) -> {
         BlockPos blockpos = p_269695_.getKey();
         Set<UUID> set = p_269695_.getValue();
         Set<String> set1 = set.stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
         int i = 1;
         renderTextOverPos(pPoseStack, pBuffer, set1.toString(), blockpos, i++, -256);
         renderTextOverPos(pPoseStack, pBuffer, "Flower", blockpos, i++, -1);
         float f = 0.05F;
         DebugRenderer.renderFilledBox(pPoseStack, pBuffer, blockpos, 0.05F, 0.8F, 0.8F, 0.0F, 0.3F);
      });
   }

   private static String getBeeUuidsAsString(Collection<UUID> pBeeUuids) {
      if (pBeeUuids.isEmpty()) {
         return "-";
      } else {
         return pBeeUuids.size() > 3 ? pBeeUuids.size() + " bees" : pBeeUuids.stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet()).toString();
      }
   }

   private static void highlightHive(PoseStack pPoseStack, MultiBufferSource pBuffer, BlockPos pHivePos) {
      float f = 0.05F;
      DebugRenderer.renderFilledBox(pPoseStack, pBuffer, pHivePos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
   }

   private void renderGhostHive(PoseStack pPoseStack, MultiBufferSource pBuffer, BlockPos pHivePos, List<String> p_270221_) {
      float f = 0.05F;
      DebugRenderer.renderFilledBox(pPoseStack, pBuffer, pHivePos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
      renderTextOverPos(pPoseStack, pBuffer, "" + p_270221_, pHivePos, 0, -256);
      renderTextOverPos(pPoseStack, pBuffer, "Ghost Hive", pHivePos, 1, -65536);
   }

   private void renderHiveInfo(PoseStack pPoseStack, MultiBufferSource pBuffer, BeeDebugRenderer.HiveInfo p_270658_, Collection<UUID> pBeeUuids) {
      int i = 0;
      if (!pBeeUuids.isEmpty()) {
         renderTextOverHive(pPoseStack, pBuffer, "Blacklisted by " + getBeeUuidsAsString(pBeeUuids), p_270658_, i++, -65536);
      }

      renderTextOverHive(pPoseStack, pBuffer, "Out: " + getBeeUuidsAsString(this.getHiveMembers(p_270658_.f_113180_)), p_270658_, i++, -3355444);
      if (p_270658_.f_113182_ == 0) {
         renderTextOverHive(pPoseStack, pBuffer, "In: -", p_270658_, i++, -256);
      } else if (p_270658_.f_113182_ == 1) {
         renderTextOverHive(pPoseStack, pBuffer, "In: 1 bee", p_270658_, i++, -256);
      } else {
         renderTextOverHive(pPoseStack, pBuffer, "In: " + p_270658_.f_113182_ + " bees", p_270658_, i++, -256);
      }

      renderTextOverHive(pPoseStack, pBuffer, "Honey: " + p_270658_.f_113183_, p_270658_, i++, -23296);
      renderTextOverHive(pPoseStack, pBuffer, p_270658_.f_113181_ + (p_270658_.f_113184_ ? " (sedated)" : ""), p_270658_, i++, -1);
   }

   private void renderPath(PoseStack pPoseStack, MultiBufferSource pBuffer, BeeDebugRenderer.BeeInfo p_270137_) {
      if (p_270137_.f_113160_ != null) {
         PathfindingRenderer.renderPath(pPoseStack, pBuffer, p_270137_.f_113160_, 0.5F, false, false, this.getCamera().getPosition().x(), this.getCamera().getPosition().y(), this.getCamera().getPosition().z());
      }

   }

   private void renderBeeInfo(PoseStack pPoseStack, MultiBufferSource pBuffer, BeeDebugRenderer.BeeInfo p_270783_) {
      boolean flag = this.isBeeSelected(p_270783_);
      int i = 0;
      renderTextOverMob(pPoseStack, pBuffer, p_270783_.f_113159_, i++, p_270783_.toString(), -1, 0.03F);
      if (p_270783_.f_113161_ == null) {
         renderTextOverMob(pPoseStack, pBuffer, p_270783_.f_113159_, i++, "No hive", -98404, 0.02F);
      } else {
         renderTextOverMob(pPoseStack, pBuffer, p_270783_.f_113159_, i++, "Hive: " + this.getPosDescription(p_270783_, p_270783_.f_113161_), -256, 0.02F);
      }

      if (p_270783_.f_113162_ == null) {
         renderTextOverMob(pPoseStack, pBuffer, p_270783_.f_113159_, i++, "No flower", -98404, 0.02F);
      } else {
         renderTextOverMob(pPoseStack, pBuffer, p_270783_.f_113159_, i++, "Flower: " + this.getPosDescription(p_270783_, p_270783_.f_113162_), -256, 0.02F);
      }

      for(String s : p_270783_.f_113164_) {
         renderTextOverMob(pPoseStack, pBuffer, p_270783_.f_113159_, i++, s, -16711936, 0.02F);
      }

      if (flag) {
         this.renderPath(pPoseStack, pBuffer, p_270783_);
      }

      if (p_270783_.f_113163_ > 0) {
         int j = p_270783_.f_113163_ < 600 ? -3355444 : -23296;
         renderTextOverMob(pPoseStack, pBuffer, p_270783_.f_113159_, i++, "Travelling: " + p_270783_.f_113163_ + " ticks", j, 0.02F);
      }

   }

   private static void renderTextOverHive(PoseStack pPoseStack, MultiBufferSource pBuffer, String pText, BeeDebugRenderer.HiveInfo p_270243_, int pLayer, int pColor) {
      BlockPos blockpos = p_270243_.f_113180_;
      renderTextOverPos(pPoseStack, pBuffer, pText, blockpos, pLayer, pColor);
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

   private Camera getCamera() {
      return this.minecraft.gameRenderer.getMainCamera();
   }

   private Set<String> getHiveMemberNames(BeeDebugRenderer.HiveInfo p_173773_) {
      return this.getHiveMembers(p_173773_.f_113180_).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
   }

   private String getPosDescription(BeeDebugRenderer.BeeInfo p_113069_, BlockPos pPos) {
      double d0 = Math.sqrt(pPos.distToCenterSqr(p_113069_.f_113159_));
      double d1 = (double)Math.round(d0 * 10.0D) / 10.0D;
      return pPos.toShortString() + " (dist " + d1 + ")";
   }

   private boolean isBeeSelected(BeeDebugRenderer.BeeInfo p_113143_) {
      return Objects.equals(this.lastLookedAtUuid, p_113143_.f_113157_);
   }

   private boolean isPlayerCloseEnoughToMob(BeeDebugRenderer.BeeInfo p_113148_) {
      Player player = this.minecraft.player;
      BlockPos blockpos = BlockPos.containing(player.getX(), p_113148_.f_113159_.y(), player.getZ());
      BlockPos blockpos1 = BlockPos.containing(p_113148_.f_113159_);
      return blockpos.closerThan(blockpos1, 30.0D);
   }

   private Collection<UUID> getHiveMembers(BlockPos pPos) {
      return this.beeInfosPerEntity.values().stream().filter((p_113087_) -> {
         return p_113087_.m_113175_(pPos);
      }).map(BeeDebugRenderer.BeeInfo::m_113174_).collect(Collectors.toSet());
   }

   private Map<BlockPos, List<String>> getGhostHives() {
      Map<BlockPos, List<String>> map = Maps.newHashMap();

      for(BeeDebugRenderer.BeeInfo beedebugrenderer$beeinfo : this.beeInfosPerEntity.values()) {
         if (beedebugrenderer$beeinfo.f_113161_ != null && !this.hives.containsKey(beedebugrenderer$beeinfo.f_113161_)) {
            map.computeIfAbsent(beedebugrenderer$beeinfo.f_113161_, (p_113140_) -> {
               return Lists.newArrayList();
            }).add(beedebugrenderer$beeinfo.m_113177_());
         }
      }

      return map;
   }

   private void updateLastLookedAtUuid() {
      DebugRenderer.getTargetedEntity(this.minecraft.getCameraEntity(), 8).ifPresent((p_113059_) -> {
         this.lastLookedAtUuid = p_113059_.getUUID();
      });
   }

   @OnlyIn(Dist.CLIENT)
   public static class BeeInfo {
      public final UUID f_113157_;
      public final int f_113158_;
      public final Position f_113159_;
      @Nullable
      public final Path f_113160_;
      @Nullable
      public final BlockPos f_113161_;
      @Nullable
      public final BlockPos f_113162_;
      public final int f_113163_;
      public final List<String> f_113164_ = Lists.newArrayList();
      public final Set<BlockPos> f_113165_ = Sets.newHashSet();

      public BeeInfo(UUID p_113167_, int p_113168_, Position p_113169_, @Nullable Path p_113170_, @Nullable BlockPos p_113171_, @Nullable BlockPos p_113172_, int p_113173_) {
         this.f_113157_ = p_113167_;
         this.f_113158_ = p_113168_;
         this.f_113159_ = p_113169_;
         this.f_113160_ = p_113170_;
         this.f_113161_ = p_113171_;
         this.f_113162_ = p_113172_;
         this.f_113163_ = p_113173_;
      }

      public boolean m_113175_(BlockPos p_113176_) {
         return this.f_113161_ != null && this.f_113161_.equals(p_113176_);
      }

      public UUID m_113174_() {
         return this.f_113157_;
      }

      public String m_113177_() {
         return DebugEntityNameGenerator.getEntityName(this.f_113157_);
      }

      public String toString() {
         return this.m_113177_();
      }

      public boolean m_113178_() {
         return this.f_113162_ != null;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class HiveInfo {
      public final BlockPos f_113180_;
      public final String f_113181_;
      public final int f_113182_;
      public final int f_113183_;
      public final boolean f_113184_;
      public final long f_113185_;

      public HiveInfo(BlockPos p_113187_, String p_113188_, int p_113189_, int p_113190_, boolean p_113191_, long p_113192_) {
         this.f_113180_ = p_113187_;
         this.f_113181_ = p_113188_;
         this.f_113182_ = p_113189_;
         this.f_113183_ = p_113190_;
         this.f_113184_ = p_113191_;
         this.f_113185_ = p_113192_;
      }
   }
}