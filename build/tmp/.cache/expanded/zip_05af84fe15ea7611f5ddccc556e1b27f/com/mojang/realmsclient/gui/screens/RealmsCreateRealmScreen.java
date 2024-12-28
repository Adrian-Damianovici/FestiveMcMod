package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.util.task.WorldCreationTask;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsCreateRealmScreen extends RealmsScreen {
   private static final Component NAME_LABEL = Component.translatable("mco.configure.world.name");
   private static final Component DESCRIPTION_LABEL = Component.translatable("mco.configure.world.description");
   private final RealmsServer server;
   private final RealmsMainScreen lastScreen;
   private EditBox nameBox;
   private EditBox descriptionBox;
   private Button f_88570_;

   public RealmsCreateRealmScreen(RealmsServer pServer, RealmsMainScreen pLastScreen) {
      super(Component.translatable("mco.selectServer.create"));
      this.server = pServer;
      this.lastScreen = pLastScreen;
   }

   public void tick() {
      if (this.nameBox != null) {
         this.nameBox.m_94120_();
      }

      if (this.descriptionBox != null) {
         this.descriptionBox.m_94120_();
      }

   }

   public void init() {
      this.f_88570_ = this.addRenderableWidget(Button.builder(Component.translatable("mco.create.world"), (p_88592_) -> {
         this.createWorld();
      }).bounds(this.width / 2 - 100, this.height / 4 + 120 + 17, 97, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_280726_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }).bounds(this.width / 2 + 5, this.height / 4 + 120 + 17, 95, 20).build());
      this.f_88570_.active = false;
      this.nameBox = new EditBox(this.minecraft.font, this.width / 2 - 100, 65, 200, 20, (EditBox)null, Component.translatable("mco.configure.world.name"));
      this.addWidget(this.nameBox);
      this.setInitialFocus(this.nameBox);
      this.descriptionBox = new EditBox(this.minecraft.font, this.width / 2 - 100, 115, 200, 20, (EditBox)null, Component.translatable("mco.configure.world.description"));
      this.addWidget(this.descriptionBox);
   }

   /**
    * Called when a character is typed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pCodePoint the code point of the typed character.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean charTyped(char p_88577_, int p_88578_) {
      boolean flag = super.charTyped(p_88577_, p_88578_);
      this.f_88570_.active = this.m_88596_();
      return flag;
   }

   /**
    * Called when a keyboard key is pressed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the pressed key.
    * @param pScanCode the scan code of the pressed key.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean keyPressed(int p_88580_, int p_88581_, int p_88582_) {
      if (p_88580_ == 256) {
         this.minecraft.setScreen(this.lastScreen);
         return true;
      } else {
         boolean flag = super.keyPressed(p_88580_, p_88581_, p_88582_);
         this.f_88570_.active = this.m_88596_();
         return flag;
      }
   }

   private void createWorld() {
      if (this.m_88596_()) {
         RealmsResetWorldScreen realmsresetworldscreen = new RealmsResetWorldScreen(this.lastScreen, this.server, Component.translatable("mco.selectServer.create"), Component.translatable("mco.create.world.subtitle"), 10526880, Component.translatable("mco.create.world.skip"), () -> {
            this.minecraft.execute(() -> {
               this.minecraft.setScreen(this.lastScreen.m_86660_());
            });
         }, () -> {
            this.minecraft.setScreen(this.lastScreen.m_86660_());
         });
         realmsresetworldscreen.m_89389_(Component.translatable("mco.create.world.reset.title"));
         this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new WorldCreationTask(this.server.id, this.nameBox.getValue(), this.descriptionBox.getValue(), realmsresetworldscreen)));
      }

   }

   private boolean m_88596_() {
      return !this.nameBox.getValue().trim().isEmpty();
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics p_283245_, int p_283409_, int p_282805_, float p_282071_) {
      this.renderBackground(p_283245_);
      p_283245_.drawCenteredString(this.font, this.title, this.width / 2, 11, 16777215);
      p_283245_.drawString(this.font, NAME_LABEL, this.width / 2 - 100, 52, 10526880, false);
      p_283245_.drawString(this.font, DESCRIPTION_LABEL, this.width / 2 - 100, 102, 10526880, false);
      if (this.nameBox != null) {
         this.nameBox.render(p_283245_, p_283409_, p_282805_, p_282071_);
      }

      if (this.descriptionBox != null) {
         this.descriptionBox.render(p_283245_, p_283409_, p_282805_, p_282071_);
      }

      super.render(p_283245_, p_283409_, p_282805_, p_282071_);
   }
}