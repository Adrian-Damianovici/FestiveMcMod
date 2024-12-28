package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import com.mojang.realmsclient.util.task.LongRunningTask;
import com.mojang.realmsclient.util.task.ResettingGeneratedWorldTask;
import com.mojang.realmsclient.util.task.ResettingTemplateWorldTask;
import com.mojang.realmsclient.util.task.SwitchSlotTask;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsResetWorldScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   private final Screen lastScreen;
   private final RealmsServer serverData;
   private Component subtitle = Component.translatable("mco.reset.world.warning");
   private Component f_89321_ = CommonComponents.GUI_CANCEL;
   private int subtitleColor = 16711680;
   private static final ResourceLocation f_89323_ = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
   private static final ResourceLocation UPLOAD_LOCATION = new ResourceLocation("realms", "textures/gui/realms/upload.png");
   private static final ResourceLocation ADVENTURE_MAP_LOCATION = new ResourceLocation("realms", "textures/gui/realms/adventure.png");
   private static final ResourceLocation SURVIVAL_SPAWN_LOCATION = new ResourceLocation("realms", "textures/gui/realms/survival_spawn.png");
   private static final ResourceLocation NEW_WORLD_LOCATION = new ResourceLocation("realms", "textures/gui/realms/new_world.png");
   private static final ResourceLocation EXPERIENCE_LOCATION = new ResourceLocation("realms", "textures/gui/realms/experience.png");
   private static final ResourceLocation INSPIRATION_LOCATION = new ResourceLocation("realms", "textures/gui/realms/inspiration.png");
   WorldTemplatePaginatedList templates;
   WorldTemplatePaginatedList adventuremaps;
   WorldTemplatePaginatedList experiences;
   WorldTemplatePaginatedList inspirations;
   public int slot = -1;
   private Component f_89310_ = Component.translatable("mco.reset.world.resetting.screen.title");
   private final Runnable resetWorldRunnable;
   private final Runnable f_89312_;

   public RealmsResetWorldScreen(Screen p_167448_, RealmsServer p_167449_, Component p_167450_, Runnable p_167451_, Runnable p_167452_) {
      super(p_167450_);
      this.lastScreen = p_167448_;
      this.serverData = p_167449_;
      this.resetWorldRunnable = p_167451_;
      this.f_89312_ = p_167452_;
   }

   public RealmsResetWorldScreen(Screen pLastScreen, RealmsServer pServerData, Runnable pResetWorldRunnable, Runnable p_89332_) {
      this(pLastScreen, pServerData, Component.translatable("mco.reset.world.title"), pResetWorldRunnable, p_89332_);
   }

   public RealmsResetWorldScreen(Screen p_89334_, RealmsServer p_89335_, Component p_89336_, Component p_89337_, int p_89338_, Component p_89339_, Runnable p_89340_, Runnable p_89341_) {
      this(p_89334_, p_89335_, p_89336_, p_89340_, p_89341_);
      this.subtitle = p_89337_;
      this.subtitleColor = p_89338_;
      this.f_89321_ = p_89339_;
   }

   public void m_89343_(int p_89344_) {
      this.slot = p_89344_;
   }

   public void m_89389_(Component p_89390_) {
      this.f_89310_ = p_89390_;
   }

   public void init() {
      this.addRenderableWidget(Button.builder(this.f_89321_, (p_280741_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }).bounds(this.width / 2 - 40, row(14) - 10, 80, 20).build());
      (new Thread("Realms-reset-world-fetcher") {
         public void run() {
            RealmsClient realmsclient = RealmsClient.create();

            try {
               WorldTemplatePaginatedList worldtemplatepaginatedlist = realmsclient.fetchWorldTemplates(1, 10, RealmsServer.WorldType.NORMAL);
               WorldTemplatePaginatedList worldtemplatepaginatedlist1 = realmsclient.fetchWorldTemplates(1, 10, RealmsServer.WorldType.ADVENTUREMAP);
               WorldTemplatePaginatedList worldtemplatepaginatedlist2 = realmsclient.fetchWorldTemplates(1, 10, RealmsServer.WorldType.EXPERIENCE);
               WorldTemplatePaginatedList worldtemplatepaginatedlist3 = realmsclient.fetchWorldTemplates(1, 10, RealmsServer.WorldType.INSPIRATION);
               RealmsResetWorldScreen.this.minecraft.execute(() -> {
                  RealmsResetWorldScreen.this.templates = worldtemplatepaginatedlist;
                  RealmsResetWorldScreen.this.adventuremaps = worldtemplatepaginatedlist1;
                  RealmsResetWorldScreen.this.experiences = worldtemplatepaginatedlist2;
                  RealmsResetWorldScreen.this.inspirations = worldtemplatepaginatedlist3;
               });
            } catch (RealmsServiceException realmsserviceexception) {
               RealmsResetWorldScreen.LOGGER.error("Couldn't fetch templates in reset world", (Throwable)realmsserviceexception);
            }

         }
      }).start();
      this.addLabel(new RealmsLabel(this.subtitle, this.width / 2, 22, this.subtitleColor));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(1), row(0) + 10, Component.translatable("mco.reset.world.generate"), NEW_WORLD_LOCATION, (p_280746_) -> {
         this.minecraft.setScreen(new RealmsResetNormalWorldScreen(this::generationSelectionCallback, this.title));
      }));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(2), row(0) + 10, Component.translatable("mco.reset.world.upload"), UPLOAD_LOCATION, (p_280744_) -> {
         this.minecraft.setScreen(new RealmsSelectFileToUploadScreen(this.serverData.id, this.slot != -1 ? this.slot : this.serverData.activeSlot, this, this.f_89312_));
      }));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(3), row(0) + 10, Component.translatable("mco.reset.world.template"), SURVIVAL_SPAWN_LOCATION, (p_280742_) -> {
         this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.reset.world.template"), this::templateSelectionCallback, RealmsServer.WorldType.NORMAL, this.templates));
      }));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(1), row(6) + 20, Component.translatable("mco.reset.world.adventure"), ADVENTURE_MAP_LOCATION, (p_280739_) -> {
         this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.reset.world.adventure"), this::templateSelectionCallback, RealmsServer.WorldType.ADVENTUREMAP, this.adventuremaps));
      }));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(2), row(6) + 20, Component.translatable("mco.reset.world.experience"), EXPERIENCE_LOCATION, (p_280745_) -> {
         this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.reset.world.experience"), this::templateSelectionCallback, RealmsServer.WorldType.EXPERIENCE, this.experiences));
      }));
      this.addRenderableWidget(new RealmsResetWorldScreen.FrameButton(this.frame(3), row(6) + 20, Component.translatable("mco.reset.world.inspiration"), INSPIRATION_LOCATION, (p_280740_) -> {
         this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen(Component.translatable("mco.reset.world.inspiration"), this::templateSelectionCallback, RealmsServer.WorldType.INSPIRATION, this.inspirations));
      }));
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(this.getTitle(), this.createLabelNarration());
   }

   /**
    * Called when a keyboard key is pressed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the pressed key.
    * @param pScanCode the scan code of the pressed key.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean keyPressed(int p_89346_, int p_89347_, int p_89348_) {
      if (p_89346_ == 256) {
         this.minecraft.setScreen(this.lastScreen);
         return true;
      } else {
         return super.keyPressed(p_89346_, p_89347_, p_89348_);
      }
   }

   private int frame(int pIndex) {
      return this.width / 2 - 130 + (pIndex - 1) * 100;
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics p_282623_, int p_282142_, int p_281508_, float p_283104_) {
      this.renderBackground(p_282623_);
      p_282623_.drawCenteredString(this.font, this.title, this.width / 2, 7, 16777215);
      super.render(p_282623_, p_282142_, p_281508_, p_283104_);
   }

   void m_280014_(GuiGraphics p_283049_, int p_282569_, int p_282343_, Component p_281871_, ResourceLocation p_281613_, boolean p_282720_, boolean p_282971_) {
      if (p_282720_) {
         p_283049_.setColor(0.56F, 0.56F, 0.56F, 1.0F);
      }

      p_283049_.blit(p_281613_, p_282569_ + 2, p_282343_ + 14, 0.0F, 0.0F, 56, 56, 56, 56);
      p_283049_.blit(f_89323_, p_282569_, p_282343_ + 12, 0.0F, 0.0F, 60, 60, 60, 60);
      int i = p_282720_ ? 10526880 : 16777215;
      p_283049_.drawCenteredString(this.font, p_281871_, p_282569_ + 30, p_282343_, i);
      p_283049_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void startTask(LongRunningTask pTask) {
      this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, pTask));
   }

   public void switchSlot(Runnable pCallback) {
      this.startTask(new SwitchSlotTask(this.serverData.id, this.slot, () -> {
         this.minecraft.execute(pCallback);
      }));
   }

   private void templateSelectionCallback(@Nullable WorldTemplate p_167454_) {
      this.minecraft.setScreen(this);
      if (p_167454_ != null) {
         this.resetWorld(() -> {
            this.startTask(new ResettingTemplateWorldTask(p_167454_, this.serverData.id, this.f_89310_, this.resetWorldRunnable));
         });
      }

   }

   private void generationSelectionCallback(@Nullable WorldGenerationInfo p_167456_) {
      this.minecraft.setScreen(this);
      if (p_167456_ != null) {
         this.resetWorld(() -> {
            this.startTask(new ResettingGeneratedWorldTask(p_167456_, this.serverData.id, this.f_89310_, this.resetWorldRunnable));
         });
      }

   }

   private void resetWorld(Runnable pResetter) {
      if (this.slot == -1) {
         pResetter.run();
      } else {
         this.switchSlot(pResetter);
      }

   }

   @OnlyIn(Dist.CLIENT)
   class FrameButton extends Button {
      private final ResourceLocation image;

      public FrameButton(int pX, int pY, Component pMessage, ResourceLocation pImage, Button.OnPress pOnPress) {
         super(pX, pY, 60, 72, pMessage, pOnPress, DEFAULT_NARRATION);
         this.image = pImage;
      }

      public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
         RealmsResetWorldScreen.this.m_280014_(pGuiGraphics, this.getX(), this.getY(), this.getMessage(), this.image, this.isHoveredOrFocused(), this.isMouseOver((double)pMouseX, (double)pMouseY));
      }
   }
}