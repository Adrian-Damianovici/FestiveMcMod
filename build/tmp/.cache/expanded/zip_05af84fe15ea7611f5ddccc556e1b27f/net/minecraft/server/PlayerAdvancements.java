package net.minecraft.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.advancements.AdvancementVisibilityEvaluator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;

public class PlayerAdvancements {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(AdvancementProgress.class, new AdvancementProgress.Serializer()).registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).setPrettyPrinting().create();
   private static final TypeToken<Map<ResourceLocation, AdvancementProgress>> f_135960_ = new TypeToken<Map<ResourceLocation, AdvancementProgress>>() {
   };
   private final DataFixer f_135961_;
   private final PlayerList playerList;
   private final Path playerSavePath;
   private final Map<Advancement, AdvancementProgress> progress = new LinkedHashMap<>();
   private final Set<Advancement> visible = new HashSet<>();
   private final Set<Advancement> progressChanged = new HashSet<>();
   private final Set<Advancement> rootsToUpdate = new HashSet<>();
   private ServerPlayer player;
   @Nullable
   private Advancement lastSelectedTab;
   private boolean isFirstPacket = true;

   public PlayerAdvancements(DataFixer pDataFixer, PlayerList pPlayerList, ServerAdvancementManager pManager, Path pPlayerSavePath, ServerPlayer pPlayer) {
      this.f_135961_ = pDataFixer;
      this.playerList = pPlayerList;
      this.playerSavePath = pPlayerSavePath;
      this.player = pPlayer;
      this.load(pManager);
   }

   public void setPlayer(ServerPlayer pPlayer) {
      this.player = pPlayer;
   }

   public void stopListening() {
      for(CriterionTrigger<?> criteriontrigger : CriteriaTriggers.all()) {
         criteriontrigger.removePlayerListeners(this);
      }

   }

   public void reload(ServerAdvancementManager pManager) {
      this.stopListening();
      this.progress.clear();
      this.visible.clear();
      this.rootsToUpdate.clear();
      this.progressChanged.clear();
      this.isFirstPacket = true;
      this.lastSelectedTab = null;
      this.load(pManager);
   }

   private void registerListeners(ServerAdvancementManager pManager) {
      for(Advancement advancement : pManager.getAllAdvancements()) {
         this.registerListeners(advancement);
      }

   }

   private void checkForAutomaticTriggers(ServerAdvancementManager pManager) {
      for(Advancement advancement : pManager.getAllAdvancements()) {
         if (advancement.m_138325_().isEmpty()) {
            this.award(advancement, "");
            advancement.m_138321_().grant(this.player);
         }
      }

   }

   private void load(ServerAdvancementManager pManager) {
      if (Files.isRegularFile(this.playerSavePath)) {
         try (JsonReader jsonreader = new JsonReader(Files.newBufferedReader(this.playerSavePath, StandardCharsets.UTF_8))) {
            jsonreader.setLenient(false);
            Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, Streams.parse(jsonreader));
            int i = dynamic.get("DataVersion").asInt(1343);
            dynamic = dynamic.remove("DataVersion");
            dynamic = DataFixTypes.ADVANCEMENTS.updateToCurrentVersion(this.f_135961_, dynamic, i);
            Map<ResourceLocation, AdvancementProgress> map = GSON.getAdapter(f_135960_).fromJsonTree(dynamic.getValue());
            if (map == null) {
               throw new JsonParseException("Found null for advancements");
            }

            map.entrySet().stream().sorted(Entry.comparingByValue()).forEach((p_265663_) -> {
               Advancement advancement = pManager.m_136041_(p_265663_.getKey());
               if (advancement == null) {
                  LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", p_265663_.getKey(), this.playerSavePath);
               } else {
                  this.startProgress(advancement, p_265663_.getValue());
                  this.progressChanged.add(advancement);
                  this.markForVisibilityUpdate(advancement);
               }
            });
         } catch (JsonParseException jsonparseexception) {
            LOGGER.error("Couldn't parse player advancements in {}", this.playerSavePath, jsonparseexception);
         } catch (IOException ioexception) {
            LOGGER.error("Couldn't access player advancements in {}", this.playerSavePath, ioexception);
         }
      }

      this.checkForAutomaticTriggers(pManager);
      this.registerListeners(pManager);
   }

   public void save() {
      Map<ResourceLocation, AdvancementProgress> map = new LinkedHashMap<>();

      for(Map.Entry<Advancement, AdvancementProgress> entry : this.progress.entrySet()) {
         AdvancementProgress advancementprogress = entry.getValue();
         if (advancementprogress.hasProgress()) {
            map.put(entry.getKey().m_138327_(), advancementprogress);
         }
      }

      JsonElement jsonelement = GSON.toJsonTree(map);
      jsonelement.getAsJsonObject().addProperty("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());

      try {
         FileUtil.createDirectoriesSafe(this.playerSavePath.getParent());

         try (Writer writer = Files.newBufferedWriter(this.playerSavePath, StandardCharsets.UTF_8)) {
            GSON.toJson(jsonelement, writer);
         }
      } catch (IOException ioexception) {
         LOGGER.error("Couldn't save player advancements to {}", this.playerSavePath, ioexception);
      }

   }

   public boolean award(Advancement p_135989_, String pCriterionKey) {
      // Forge: don't grant advancements for fake players
      if (this.player instanceof net.minecraftforge.common.util.FakePlayer) return false;
      boolean flag = false;
      AdvancementProgress advancementprogress = this.getOrStartProgress(p_135989_);
      boolean flag1 = advancementprogress.isDone();
      if (advancementprogress.grantProgress(pCriterionKey)) {
         this.unregisterListeners(p_135989_);
         this.progressChanged.add(p_135989_);
         flag = true;
         net.minecraftforge.event.ForgeEventFactory.onAdvancementProgressedEvent(this.player, p_135989_, advancementprogress, pCriterionKey, net.minecraftforge.event.entity.player.AdvancementEvent.AdvancementProgressEvent.ProgressType.GRANT);
         if (!flag1 && advancementprogress.isDone()) {
            p_135989_.m_138321_().grant(this.player);
            if (p_135989_.m_138320_() != null && p_135989_.m_138320_().shouldAnnounceChat() && this.player.level().getGameRules().getBoolean(GameRules.RULE_ANNOUNCE_ADVANCEMENTS)) {
               this.playerList.broadcastSystemMessage(Component.translatable("chat.type.advancement." + p_135989_.m_138320_().getFrame().getName(), this.player.getDisplayName(), p_135989_.m_138330_()), false);
            }
            net.minecraftforge.event.ForgeEventFactory.onAdvancementEarnedEvent(this.player, p_135989_);
         }
      }

      if (!flag1 && advancementprogress.isDone()) {
         this.markForVisibilityUpdate(p_135989_);
      }

      return flag;
   }

   public boolean revoke(Advancement p_135999_, String pCriterionKey) {
      boolean flag = false;
      AdvancementProgress advancementprogress = this.getOrStartProgress(p_135999_);
      boolean flag1 = advancementprogress.isDone();
      if (advancementprogress.revokeProgress(pCriterionKey)) {
         this.registerListeners(p_135999_);
         this.progressChanged.add(p_135999_);
         flag = true;
         net.minecraftforge.event.ForgeEventFactory.onAdvancementProgressedEvent(this.player, p_135999_, advancementprogress, pCriterionKey, net.minecraftforge.event.entity.player.AdvancementEvent.AdvancementProgressEvent.ProgressType.REVOKE);
      }

      if (flag1 && !advancementprogress.isDone()) {
         this.markForVisibilityUpdate(p_135999_);
      }

      return flag;
   }

   private void markForVisibilityUpdate(Advancement p_265528_) {
      this.rootsToUpdate.add(p_265528_.m_264348_());
   }

   private void registerListeners(Advancement p_136005_) {
      AdvancementProgress advancementprogress = this.getOrStartProgress(p_136005_);
      if (!advancementprogress.isDone()) {
         for(Map.Entry<String, Criterion> entry : p_136005_.m_138325_().entrySet()) {
            CriterionProgress criterionprogress = advancementprogress.getCriterion(entry.getKey());
            if (criterionprogress != null && !criterionprogress.isDone()) {
               CriterionTriggerInstance criteriontriggerinstance = entry.getValue().m_11416_();
               if (criteriontriggerinstance != null) {
                  CriterionTrigger<CriterionTriggerInstance> criteriontrigger = CriteriaTriggers.getCriterion(criteriontriggerinstance.m_7294_());
                  if (criteriontrigger != null) {
                     criteriontrigger.addPlayerListener(this, new CriterionTrigger.Listener<>(criteriontriggerinstance, p_136005_, entry.getKey()));
                  }
               }
            }
         }

      }
   }

   private void unregisterListeners(Advancement p_136009_) {
      AdvancementProgress advancementprogress = this.getOrStartProgress(p_136009_);

      for(Map.Entry<String, Criterion> entry : p_136009_.m_138325_().entrySet()) {
         CriterionProgress criterionprogress = advancementprogress.getCriterion(entry.getKey());
         if (criterionprogress != null && (criterionprogress.isDone() || advancementprogress.isDone())) {
            CriterionTriggerInstance criteriontriggerinstance = entry.getValue().m_11416_();
            if (criteriontriggerinstance != null) {
               CriterionTrigger<CriterionTriggerInstance> criteriontrigger = CriteriaTriggers.getCriterion(criteriontriggerinstance.m_7294_());
               if (criteriontrigger != null) {
                  criteriontrigger.removePlayerListener(this, new CriterionTrigger.Listener<>(criteriontriggerinstance, p_136009_, entry.getKey()));
               }
            }
         }
      }

   }

   public void flushDirty(ServerPlayer pServerPlayer) {
      if (this.isFirstPacket || !this.rootsToUpdate.isEmpty() || !this.progressChanged.isEmpty()) {
         Map<ResourceLocation, AdvancementProgress> map = new HashMap<>();
         Set<Advancement> set = new HashSet<>();
         Set<ResourceLocation> set1 = new HashSet<>();

         for(Advancement advancement : this.rootsToUpdate) {
            this.updateTreeVisibility(advancement, set, set1);
         }

         this.rootsToUpdate.clear();

         for(Advancement advancement1 : this.progressChanged) {
            if (this.visible.contains(advancement1)) {
               map.put(advancement1.m_138327_(), this.progress.get(advancement1));
            }
         }

         this.progressChanged.clear();
         if (!map.isEmpty() || !set.isEmpty() || !set1.isEmpty()) {
            pServerPlayer.connection.m_9829_(new ClientboundUpdateAdvancementsPacket(this.isFirstPacket, set, set1, map));
         }
      }

      this.isFirstPacket = false;
   }

   public void setSelectedTab(@Nullable Advancement p_135984_) {
      Advancement advancement = this.lastSelectedTab;
      if (p_135984_ != null && p_135984_.m_138319_() == null && p_135984_.m_138320_() != null) {
         this.lastSelectedTab = p_135984_;
      } else {
         this.lastSelectedTab = null;
      }

      if (advancement != this.lastSelectedTab) {
         this.player.connection.m_9829_(new ClientboundSelectAdvancementsTabPacket(this.lastSelectedTab == null ? null : this.lastSelectedTab.m_138327_()));
      }

   }

   public AdvancementProgress getOrStartProgress(Advancement p_135997_) {
      AdvancementProgress advancementprogress = this.progress.get(p_135997_);
      if (advancementprogress == null) {
         advancementprogress = new AdvancementProgress();
         this.startProgress(p_135997_, advancementprogress);
      }

      return advancementprogress;
   }

   private void startProgress(Advancement p_135986_, AdvancementProgress pAdvancementProgress) {
      pAdvancementProgress.update(p_135986_.m_138325_(), p_135986_.m_138329_());
      this.progress.put(p_135986_, pAdvancementProgress);
   }

   private void updateTreeVisibility(Advancement p_265158_, Set<Advancement> pAdvancementOutput, Set<ResourceLocation> pIdOutput) {
      AdvancementVisibilityEvaluator.evaluateVisibility(p_265158_, (p_265787_) -> {
         return this.getOrStartProgress(p_265787_).isDone();
      }, (p_265247_, p_265330_) -> {
         if (p_265330_) {
            if (this.visible.add(p_265247_)) {
               pAdvancementOutput.add(p_265247_);
               if (this.progress.containsKey(p_265247_)) {
                  this.progressChanged.add(p_265247_);
               }
            }
         } else if (this.visible.remove(p_265247_)) {
            pIdOutput.add(p_265247_.m_138327_());
         }

      });
   }
}
