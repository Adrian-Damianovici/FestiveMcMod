package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PendingInvite;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RowButton;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsPendingInvitesScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   static final ResourceLocation f_88875_ = new ResourceLocation("realms", "textures/gui/realms/accept_icon.png");
   static final ResourceLocation f_88876_ = new ResourceLocation("realms", "textures/gui/realms/reject_icon.png");
   private static final Component NO_PENDING_INVITES_TEXT = Component.translatable("mco.invites.nopending");
   static final Component f_88878_ = Component.translatable("mco.invites.button.accept");
   static final Component f_88879_ = Component.translatable("mco.invites.button.reject");
   private final Screen lastScreen;
   @Nullable
   Component toolTip;
   boolean f_88882_;
   RealmsPendingInvitesScreen.PendingInvitationSelectionList pendingInvitationSelectionList;
   int selectedInvite = -1;
   private Button acceptButton;
   private Button rejectButton;

   public RealmsPendingInvitesScreen(Screen pLastScreen, Component pTitle) {
      super(pTitle);
      this.lastScreen = pLastScreen;
   }

   public void init() {
      this.pendingInvitationSelectionList = new RealmsPendingInvitesScreen.PendingInvitationSelectionList();
      (new Thread("Realms-pending-invitations-fetcher") {
         public void run() {
            RealmsClient realmsclient = RealmsClient.create();

            try {
               List<PendingInvite> list = realmsclient.pendingInvites().pendingInvites;
               List<RealmsPendingInvitesScreen.Entry> list1 = list.stream().map((p_88969_) -> {
                  return RealmsPendingInvitesScreen.this.new Entry(p_88969_);
               }).collect(Collectors.toList());
               RealmsPendingInvitesScreen.this.minecraft.execute(() -> {
                  RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.replaceEntries(list1);
               });
            } catch (RealmsServiceException realmsserviceexception) {
               RealmsPendingInvitesScreen.LOGGER.error("Couldn't list invites");
            } finally {
               RealmsPendingInvitesScreen.this.f_88882_ = true;
            }

         }
      }).start();
      this.addWidget(this.pendingInvitationSelectionList);
      this.acceptButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.invites.button.accept"), (p_88940_) -> {
         this.m_88932_(this.selectedInvite);
         this.selectedInvite = -1;
         this.updateButtonStates();
      }).bounds(this.width / 2 - 174, this.height - 32, 100, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_280731_) -> {
         this.minecraft.setScreen(new RealmsMainScreen(this.lastScreen));
      }).bounds(this.width / 2 - 50, this.height - 32, 100, 20).build());
      this.rejectButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.invites.button.reject"), (p_88920_) -> {
         this.m_88922_(this.selectedInvite);
         this.selectedInvite = -1;
         this.updateButtonStates();
      }).bounds(this.width / 2 + 74, this.height - 32, 100, 20).build());
      this.updateButtonStates();
   }

   /**
    * Called when a keyboard key is pressed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the pressed key.
    * @param pScanCode the scan code of the pressed key.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean keyPressed(int p_88895_, int p_88896_, int p_88897_) {
      if (p_88895_ == 256) {
         this.minecraft.setScreen(new RealmsMainScreen(this.lastScreen));
         return true;
      } else {
         return super.keyPressed(p_88895_, p_88896_, p_88897_);
      }
   }

   void m_88892_(int p_88893_) {
      this.pendingInvitationSelectionList.removeAtIndex(p_88893_);
   }

   void m_88922_(final int p_88923_) {
      if (p_88923_ < this.pendingInvitationSelectionList.getItemCount()) {
         (new Thread("Realms-reject-invitation") {
            public void run() {
               try {
                  RealmsClient realmsclient = RealmsClient.create();
                  realmsclient.rejectInvitation((RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.children().get(p_88923_)).pendingInvite.invitationId);
                  RealmsPendingInvitesScreen.this.minecraft.execute(() -> {
                     RealmsPendingInvitesScreen.this.m_88892_(p_88923_);
                  });
               } catch (RealmsServiceException realmsserviceexception) {
                  RealmsPendingInvitesScreen.LOGGER.error("Couldn't reject invite");
               }

            }
         }).start();
      }

   }

   void m_88932_(final int p_88933_) {
      if (p_88933_ < this.pendingInvitationSelectionList.getItemCount()) {
         (new Thread("Realms-accept-invitation") {
            public void run() {
               try {
                  RealmsClient realmsclient = RealmsClient.create();
                  realmsclient.acceptInvitation((RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.children().get(p_88933_)).pendingInvite.invitationId);
                  RealmsPendingInvitesScreen.this.minecraft.execute(() -> {
                     RealmsPendingInvitesScreen.this.m_88892_(p_88933_);
                  });
               } catch (RealmsServiceException realmsserviceexception) {
                  RealmsPendingInvitesScreen.LOGGER.error("Couldn't accept invite");
               }

            }
         }).start();
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
      this.toolTip = null;
      this.renderBackground(pGuiGraphics);
      this.pendingInvitationSelectionList.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 16777215);
      if (this.toolTip != null) {
         this.m_280517_(pGuiGraphics, this.toolTip, pMouseX, pMouseY);
      }

      if (this.pendingInvitationSelectionList.getItemCount() == 0 && this.f_88882_) {
         pGuiGraphics.drawCenteredString(this.font, NO_PENDING_INVITES_TEXT, this.width / 2, this.height / 2 - 20, 16777215);
      }

      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   protected void m_280517_(GuiGraphics p_282344_, @Nullable Component p_283454_, int p_281609_, int p_283288_) {
      if (p_283454_ != null) {
         int i = p_281609_ + 12;
         int j = p_283288_ - 12;
         int k = this.font.width(p_283454_);
         p_282344_.fillGradient(i - 3, j - 3, i + k + 3, j + 8 + 3, -1073741824, -1073741824);
         p_282344_.drawString(this.font, p_283454_, i, j, 16777215);
      }
   }

   void updateButtonStates() {
      this.acceptButton.visible = this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite);
      this.rejectButton.visible = this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite);
   }

   private boolean shouldAcceptAndRejectButtonBeVisible(int pSelectedInvite) {
      return pSelectedInvite != -1;
   }

   @OnlyIn(Dist.CLIENT)
   class Entry extends ObjectSelectionList.Entry<RealmsPendingInvitesScreen.Entry> {
      private static final int TEXT_LEFT = 38;
      final PendingInvite pendingInvite;
      private final List<RowButton> rowButtons;

      Entry(PendingInvite pPendingInvite) {
         this.pendingInvite = pPendingInvite;
         this.rowButtons = Arrays.asList(new RealmsPendingInvitesScreen.Entry.AcceptRowButton(), new RealmsPendingInvitesScreen.Entry.RejectRowButton());
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         this.renderPendingInvitationItem(pGuiGraphics, this.pendingInvite, pLeft, pTop, pMouseX, pMouseY);
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
         RowButton.rowButtonMouseClicked(RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, this, this.rowButtons, pButton, pMouseX, pMouseY);
         return true;
      }

      private void renderPendingInvitationItem(GuiGraphics pGuiGraphics, PendingInvite pPendingInvite, int pX, int pY, int pMouseX, int pMouseY) {
         pGuiGraphics.drawString(RealmsPendingInvitesScreen.this.font, pPendingInvite.worldName, pX + 38, pY + 1, 16777215, false);
         pGuiGraphics.drawString(RealmsPendingInvitesScreen.this.font, pPendingInvite.worldOwnerName, pX + 38, pY + 12, 7105644, false);
         pGuiGraphics.drawString(RealmsPendingInvitesScreen.this.font, RealmsUtil.convertToAgePresentationFromInstant(pPendingInvite.date), pX + 38, pY + 24, 7105644, false);
         RowButton.drawButtonsInRow(pGuiGraphics, this.rowButtons, RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, pX, pY, pMouseX, pMouseY);
         RealmsUtil.renderPlayerFace(pGuiGraphics, pX, pY, 32, pPendingInvite.worldOwnerUuid);
      }

      public Component getNarration() {
         Component component = CommonComponents.joinLines(Component.literal(this.pendingInvite.worldName), Component.literal(this.pendingInvite.worldOwnerName), RealmsUtil.convertToAgePresentationFromInstant(this.pendingInvite.date));
         return Component.translatable("narrator.select", component);
      }

      @OnlyIn(Dist.CLIENT)
      class AcceptRowButton extends RowButton {
         AcceptRowButton() {
            super(15, 15, 215, 5);
         }

         protected void draw(GuiGraphics p_282151_, int p_283695_, int p_282436_, boolean p_282168_) {
            float f = p_282168_ ? 19.0F : 0.0F;
            p_282151_.blit(RealmsPendingInvitesScreen.f_88875_, p_283695_, p_282436_, f, 0.0F, 18, 18, 37, 18);
            if (p_282168_) {
               RealmsPendingInvitesScreen.this.toolTip = RealmsPendingInvitesScreen.f_88878_;
            }

         }

         public void onClick(int p_89029_) {
            RealmsPendingInvitesScreen.this.m_88932_(p_89029_);
         }
      }

      @OnlyIn(Dist.CLIENT)
      class RejectRowButton extends RowButton {
         RejectRowButton() {
            super(15, 15, 235, 5);
         }

         protected void draw(GuiGraphics p_282457_, int p_281421_, int p_281260_, boolean p_281476_) {
            float f = p_281476_ ? 19.0F : 0.0F;
            p_282457_.blit(RealmsPendingInvitesScreen.f_88876_, p_281421_, p_281260_, f, 0.0F, 18, 18, 37, 18);
            if (p_281476_) {
               RealmsPendingInvitesScreen.this.toolTip = RealmsPendingInvitesScreen.f_88879_;
            }

         }

         public void onClick(int p_89039_) {
            RealmsPendingInvitesScreen.this.m_88922_(p_89039_);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   class PendingInvitationSelectionList extends RealmsObjectSelectionList<RealmsPendingInvitesScreen.Entry> {
      public PendingInvitationSelectionList() {
         super(RealmsPendingInvitesScreen.this.width, RealmsPendingInvitesScreen.this.height, 32, RealmsPendingInvitesScreen.this.height - 40, 36);
      }

      public void removeAtIndex(int pIndex) {
         this.remove(pIndex);
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public int getRowWidth() {
         return 260;
      }

      public void m_7733_(GuiGraphics p_282494_) {
         RealmsPendingInvitesScreen.this.renderBackground(p_282494_);
      }

      public void selectItem(int pIndex) {
         super.selectItem(pIndex);
         this.selectInviteListItem(pIndex);
      }

      public void selectInviteListItem(int pIndex) {
         RealmsPendingInvitesScreen.this.selectedInvite = pIndex;
         RealmsPendingInvitesScreen.this.updateButtonStates();
      }

      public void setSelected(@Nullable RealmsPendingInvitesScreen.Entry pSelected) {
         super.setSelected(pSelected);
         RealmsPendingInvitesScreen.this.selectedInvite = this.children().indexOf(pSelected);
         RealmsPendingInvitesScreen.this.updateButtonStates();
      }
   }
}