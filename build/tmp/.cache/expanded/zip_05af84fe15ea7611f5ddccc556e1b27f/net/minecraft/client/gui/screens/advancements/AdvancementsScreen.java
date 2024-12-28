package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AdvancementsScreen extends Screen implements ClientAdvancements.Listener {
   private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation("textures/gui/advancements/window.png");
   public static final ResourceLocation f_97330_ = new ResourceLocation("textures/gui/advancements/tabs.png");
   public static final int WINDOW_WIDTH = 252;
   public static final int WINDOW_HEIGHT = 140;
   private static final int WINDOW_INSIDE_X = 9;
   private static final int WINDOW_INSIDE_Y = 18;
   public static final int WINDOW_INSIDE_WIDTH = 234;
   public static final int WINDOW_INSIDE_HEIGHT = 113;
   private static final int WINDOW_TITLE_X = 8;
   private static final int WINDOW_TITLE_Y = 6;
   public static final int BACKGROUND_TILE_WIDTH = 16;
   public static final int BACKGROUND_TILE_HEIGHT = 16;
   public static final int BACKGROUND_TILE_COUNT_X = 14;
   public static final int BACKGROUND_TILE_COUNT_Y = 7;
   private static final Component VERY_SAD_LABEL = Component.translatable("advancements.sad_label");
   private static final Component NO_ADVANCEMENTS_LABEL = Component.translatable("advancements.empty");
   private static final Component TITLE = Component.translatable("gui.advancements");
   private final ClientAdvancements advancements;
   private final Map<Advancement, AdvancementTab> tabs = Maps.newLinkedHashMap();
   @Nullable
   private AdvancementTab selectedTab;
   private boolean isScrolling;
   private static int tabPage, maxPages;

   public AdvancementsScreen(ClientAdvancements pAdvancements) {
      super(GameNarrator.NO_TITLE);
      this.advancements = pAdvancements;
   }

   protected void init() {
      this.tabs.clear();
      this.selectedTab = null;
      this.advancements.setListener(this);
      if (this.selectedTab == null && !this.tabs.isEmpty()) {
         this.advancements.setSelectedTab(this.tabs.values().iterator().next().m_97182_(), true);
      } else {
         this.advancements.setSelectedTab(this.selectedTab == null ? null : this.selectedTab.m_97182_(), true);
      }
      if (this.tabs.size() > AdvancementTabType.MAX_TABS) {
          int guiLeft = (this.width - 252) / 2;
          int guiTop = (this.height - 140) / 2;
          addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("<"), b -> tabPage = Math.max(tabPage - 1, 0       ))
              .pos(guiLeft, guiTop - 50).size(20, 20).build());
          addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal(">"), b -> tabPage = Math.min(tabPage + 1, maxPages))
              .pos(guiLeft + WINDOW_WIDTH - 20, guiTop - 50).size(20, 20).build());
          maxPages = this.tabs.size() / AdvancementTabType.MAX_TABS;
      }
   }

   public void removed() {
      this.advancements.setListener((ClientAdvancements.Listener)null);
      ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
      if (clientpacketlistener != null) {
         clientpacketlistener.m_104955_(ServerboundSeenAdvancementsPacket.closedScreen());
      }

   }

   /**
    * Called when a mouse button is clicked within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that was clicked.
    */
   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if (pButton == 0) {
         int i = (this.width - 252) / 2;
         int j = (this.height - 140) / 2;

         for(AdvancementTab advancementtab : this.tabs.values()) {
            if (advancementtab.getPage() == tabPage && advancementtab.isMouseOver(i, j, pMouseX, pMouseY)) {
               this.advancements.setSelectedTab(advancementtab.m_97182_(), true);
               break;
            }
         }
      }

      return super.mouseClicked(pMouseX, pMouseY, pButton);
   }

   /**
    * Called when a keyboard key is pressed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the pressed key.
    * @param pScanCode the scan code of the pressed key.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (this.minecraft.options.keyAdvancements.matches(pKeyCode, pScanCode)) {
         this.minecraft.setScreen((Screen)null);
         this.minecraft.mouseHandler.grabMouse();
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      int i = (this.width - 252) / 2;
      int j = (this.height - 140) / 2;
      this.renderBackground(pGuiGraphics);
      if (maxPages != 0) {
         net.minecraft.network.chat.Component page = Component.literal(String.format("%d / %d", tabPage + 1, maxPages + 1));
         int width = this.font.width(page);
         pGuiGraphics.drawString(this.font, page.getVisualOrderText(), i + (252 / 2) - (width / 2), j - 44, -1);
      }
      this.renderInside(pGuiGraphics, pMouseX, pMouseY, i, j);
      this.renderWindow(pGuiGraphics, i, j);
      this.renderTooltips(pGuiGraphics, pMouseX, pMouseY, i, j);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   /**
    * Called when the mouse is dragged within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that is being dragged.
    * @param pDragX the X distance of the drag.
    * @param pDragY the Y distance of the drag.
    */
   public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
      if (pButton != 0) {
         this.isScrolling = false;
         return false;
      } else {
         if (!this.isScrolling) {
            this.isScrolling = true;
         } else if (this.selectedTab != null) {
            this.selectedTab.scroll(pDragX, pDragY);
         }

         return true;
      }
   }

   private void renderInside(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, int pOffsetX, int pOffsetY) {
      AdvancementTab advancementtab = this.selectedTab;
      if (advancementtab == null) {
         pGuiGraphics.fill(pOffsetX + 9, pOffsetY + 18, pOffsetX + 9 + 234, pOffsetY + 18 + 113, -16777216);
         int i = pOffsetX + 9 + 117;
         pGuiGraphics.drawCenteredString(this.font, NO_ADVANCEMENTS_LABEL, i, pOffsetY + 18 + 56 - 9 / 2, -1);
         pGuiGraphics.drawCenteredString(this.font, VERY_SAD_LABEL, i, pOffsetY + 18 + 113 - 9, -1);
      } else {
         advancementtab.drawContents(pGuiGraphics, pOffsetX + 9, pOffsetY + 18);
      }
   }

   public void renderWindow(GuiGraphics pGuiGraphics, int pOffsetX, int pOffsetY) {
      RenderSystem.enableBlend();
      pGuiGraphics.blit(WINDOW_LOCATION, pOffsetX, pOffsetY, 0, 0, 252, 140);
      if (this.tabs.size() > 1) {
         for(AdvancementTab advancementtab : this.tabs.values()) {
            if (advancementtab.getPage() == tabPage)
            advancementtab.drawTab(pGuiGraphics, pOffsetX, pOffsetY, advancementtab == this.selectedTab);
         }

         for(AdvancementTab advancementtab1 : this.tabs.values()) {
            if (advancementtab1.getPage() == tabPage)
            advancementtab1.drawIcon(pGuiGraphics, pOffsetX, pOffsetY);
         }
      }

      pGuiGraphics.drawString(this.font, TITLE, pOffsetX + 8, pOffsetY + 6, 4210752, false);
   }

   private void renderTooltips(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, int pOffsetX, int pOffsetY) {
      if (this.selectedTab != null) {
         pGuiGraphics.pose().pushPose();
         pGuiGraphics.pose().translate((float)(pOffsetX + 9), (float)(pOffsetY + 18), 400.0F);
         RenderSystem.enableDepthTest();
         this.selectedTab.drawTooltips(pGuiGraphics, pMouseX - pOffsetX - 9, pMouseY - pOffsetY - 18, pOffsetX, pOffsetY);
         RenderSystem.disableDepthTest();
         pGuiGraphics.pose().popPose();
      }

      if (this.tabs.size() > 1) {
         for(AdvancementTab advancementtab : this.tabs.values()) {
            if (advancementtab.getPage() == tabPage && advancementtab.isMouseOver(pOffsetX, pOffsetY, (double)pMouseX, (double)pMouseY)) {
               pGuiGraphics.renderTooltip(this.font, advancementtab.getTitle(), pMouseX, pMouseY);
            }
         }
      }

   }

   public void m_5513_(Advancement p_97366_) {
      AdvancementTab advancementtab = AdvancementTab.create(this.minecraft, this, this.tabs.size(), p_97366_);
      if (advancementtab != null) {
         this.tabs.put(p_97366_, advancementtab);
      }
   }

   public void m_5504_(Advancement p_97372_) {
   }

   public void m_5505_(Advancement p_97380_) {
      AdvancementTab advancementtab = this.getTab(p_97380_);
      if (advancementtab != null) {
         advancementtab.addAdvancement(p_97380_);
      }

   }

   public void m_5516_(Advancement p_97388_) {
   }

   public void onUpdateAdvancementProgress(Advancement p_97368_, AdvancementProgress pAdvancementProgress) {
      AdvancementWidget advancementwidget = this.getAdvancementWidget(p_97368_);
      if (advancementwidget != null) {
         advancementwidget.setProgress(pAdvancementProgress);
      }

   }

   public void onSelectedTabChanged(@Nullable Advancement p_97391_) {
      this.selectedTab = this.tabs.get(p_97391_);
   }

   public void m_7204_() {
      this.tabs.clear();
      this.selectedTab = null;
   }

   @Nullable
   public AdvancementWidget getAdvancementWidget(Advancement p_97393_) {
      AdvancementTab advancementtab = this.getTab(p_97393_);
      return advancementtab == null ? null : advancementtab.getWidget(p_97393_);
   }

   @Nullable
   private AdvancementTab getTab(Advancement p_97395_) {
      while(p_97395_.m_138319_() != null) {
         p_97395_ = p_97395_.m_138319_();
      }

      return this.tabs.get(p_97395_);
   }
}
