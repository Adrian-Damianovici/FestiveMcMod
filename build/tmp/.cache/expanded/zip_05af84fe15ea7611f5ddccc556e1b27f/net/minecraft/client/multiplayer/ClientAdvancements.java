package net.minecraft.client.multiplayer;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientAdvancements {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Minecraft minecraft;
   private final WorldSessionTelemetryManager telemetryManager;
   private final AdvancementList f_104389_ = new AdvancementList();
   private final Map<Advancement, AdvancementProgress> progress = Maps.newHashMap();
   @Nullable
   private ClientAdvancements.Listener listener;
   @Nullable
   private Advancement selectedTab;

   public ClientAdvancements(Minecraft pMinecraft, WorldSessionTelemetryManager pTelemetryManager) {
      this.minecraft = pMinecraft;
      this.telemetryManager = pTelemetryManager;
   }

   public void update(ClientboundUpdateAdvancementsPacket pPacket) {
      if (pPacket.shouldReset()) {
         this.f_104389_.m_139332_();
         this.progress.clear();
      }

      this.f_104389_.m_139335_(pPacket.getRemoved());
      this.f_104389_.m_139333_(pPacket.getAdded());

      for(Map.Entry<ResourceLocation, AdvancementProgress> entry : pPacket.getProgress().entrySet()) {
         Advancement advancement = this.f_104389_.m_139337_(entry.getKey());
         if (advancement != null) {
            AdvancementProgress advancementprogress = entry.getValue();
            advancementprogress.update(advancement.m_138325_(), advancement.m_138329_());
            this.progress.put(advancement, advancementprogress);
            if (this.listener != null) {
               this.listener.onUpdateAdvancementProgress(advancement, advancementprogress);
            }

            if (!pPacket.shouldReset() && advancementprogress.isDone()) {
               if (this.minecraft.level != null) {
                  this.telemetryManager.onAdvancementDone(this.minecraft.level, advancement);
               }

               if (advancement.m_138320_() != null && advancement.m_138320_().shouldShowToast()) {
                  this.minecraft.getToasts().addToast(new AdvancementToast(advancement));
               }
            }
         } else {
            LOGGER.warn("Server informed client about progress for unknown advancement {}", entry.getKey());
         }
      }

   }

   public AdvancementList m_104396_() {
      return this.f_104389_;
   }

   public void setSelectedTab(@Nullable Advancement p_104402_, boolean pTellServer) {
      ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
      if (clientpacketlistener != null && p_104402_ != null && pTellServer) {
         clientpacketlistener.m_104955_(ServerboundSeenAdvancementsPacket.openedTab(p_104402_));
      }

      if (this.selectedTab != p_104402_) {
         this.selectedTab = p_104402_;
         if (this.listener != null) {
            this.listener.onSelectedTabChanged(p_104402_);
         }
      }

   }

   public void setListener(@Nullable ClientAdvancements.Listener pListener) {
      this.listener = pListener;
      this.f_104389_.m_139341_(pListener);
      if (pListener != null) {
         for(Map.Entry<Advancement, AdvancementProgress> entry : this.progress.entrySet()) {
            pListener.onUpdateAdvancementProgress(entry.getKey(), entry.getValue());
         }

         pListener.onSelectedTabChanged(this.selectedTab);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public interface Listener extends AdvancementList.Listener {
      void onUpdateAdvancementProgress(Advancement p_104404_, AdvancementProgress pAdvancementProgress);

      void onSelectedTabChanged(@Nullable Advancement p_104406_);
   }
}