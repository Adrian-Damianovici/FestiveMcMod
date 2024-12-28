package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.TextRenderingUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsSelectWorldTemplateScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   static final ResourceLocation f_89606_ = new ResourceLocation("realms", "textures/gui/realms/link_icons.png");
   static final ResourceLocation f_89607_ = new ResourceLocation("realms", "textures/gui/realms/trailer_icons.png");
   static final ResourceLocation f_89608_ = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
   static final Component f_89609_ = Component.translatable("mco.template.info.tooltip");
   static final Component f_89610_ = Component.translatable("mco.template.trailer.tooltip");
   private final Consumer<WorldTemplate> callback;
   RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList worldTemplateObjectSelectionList;
   int selectedTemplate = -1;
   private Button selectButton;
   private Button trailerButton;
   private Button publisherButton;
   @Nullable
   Component f_89618_;
   @Nullable
   String currentLink;
   private final RealmsServer.WorldType worldType;
   int f_89599_;
   @Nullable
   private Component[] warning;
   private String f_89601_;
   boolean f_89602_;
   private boolean f_89603_;
   @Nullable
   List<TextRenderingUtils.Line> noTemplatesMessage;

   public RealmsSelectWorldTemplateScreen(Component pTitle, Consumer<WorldTemplate> pCallback, RealmsServer.WorldType pWorldType) {
      this(pTitle, pCallback, pWorldType, (WorldTemplatePaginatedList)null);
   }

   public RealmsSelectWorldTemplateScreen(Component pTitle, Consumer<WorldTemplate> pCallback, RealmsServer.WorldType pWorldType, @Nullable WorldTemplatePaginatedList pWorldTemplatePaginatedList) {
      super(pTitle);
      this.callback = pCallback;
      this.worldType = pWorldType;
      if (pWorldTemplatePaginatedList == null) {
         this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList();
         this.fetchTemplatesAsync(new WorldTemplatePaginatedList(10));
      } else {
         this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(Lists.newArrayList(pWorldTemplatePaginatedList.templates));
         this.fetchTemplatesAsync(pWorldTemplatePaginatedList);
      }

   }

   public void setWarning(Component... pWarning) {
      this.warning = pWarning;
      this.f_89602_ = true;
   }

   /**
    * Called when a mouse button is clicked within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that was clicked.
    */
   public boolean mouseClicked(double p_89629_, double p_89630_, int p_89631_) {
      if (this.f_89603_ && this.f_89601_ != null) {
         Util.getPlatform().openUri("https://www.minecraft.net/realms/adventure-maps-in-1-9");
         return true;
      } else {
         return super.mouseClicked(p_89629_, p_89630_, p_89631_);
      }
   }

   public void init() {
      this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(this.worldTemplateObjectSelectionList.getTemplates());
      this.trailerButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.template.button.trailer"), (p_89701_) -> {
         this.onTrailer();
      }).bounds(this.width / 2 - 206, this.height - 32, 100, 20).build());
      this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.template.button.select"), (p_89696_) -> {
         this.selectTemplate();
      }).bounds(this.width / 2 - 100, this.height - 32, 100, 20).build());
      Component component = this.worldType == RealmsServer.WorldType.MINIGAME ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_BACK;
      Button button = Button.builder(component, (p_89691_) -> {
         this.onClose();
      }).bounds(this.width / 2 + 6, this.height - 32, 100, 20).build();
      this.addRenderableWidget(button);
      this.publisherButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.template.button.publisher"), (p_89679_) -> {
         this.onPublish();
      }).bounds(this.width / 2 + 112, this.height - 32, 100, 20).build());
      this.selectButton.active = false;
      this.trailerButton.visible = false;
      this.publisherButton.visible = false;
      this.addWidget(this.worldTemplateObjectSelectionList);
      this.magicalSpecialHackyFocus(this.worldTemplateObjectSelectionList);
   }

   public Component getNarrationMessage() {
      List<Component> list = Lists.newArrayListWithCapacity(2);
      if (this.title != null) {
         list.add(this.title);
      }

      if (this.warning != null) {
         list.addAll(Arrays.asList(this.warning));
      }

      return CommonComponents.joinLines(list);
   }

   void updateButtonStates() {
      this.publisherButton.visible = this.m_89724_();
      this.trailerButton.visible = this.m_89730_();
      this.selectButton.active = this.m_89721_();
   }

   private boolean m_89721_() {
      return this.selectedTemplate != -1;
   }

   private boolean m_89724_() {
      return this.selectedTemplate != -1 && !this.m_89727_().link.isEmpty();
   }

   private WorldTemplate m_89727_() {
      return this.worldTemplateObjectSelectionList.m_89811_(this.selectedTemplate);
   }

   private boolean m_89730_() {
      return this.selectedTemplate != -1 && !this.m_89727_().trailer.isEmpty();
   }

   public void tick() {
      super.tick();
      --this.f_89599_;
      if (this.f_89599_ < 0) {
         this.f_89599_ = 0;
      }

   }

   public void onClose() {
      this.callback.accept((WorldTemplate)null);
   }

   void selectTemplate() {
      if (this.m_89737_()) {
         this.callback.accept(this.m_89727_());
      }

   }

   private boolean m_89737_() {
      return this.selectedTemplate >= 0 && this.selectedTemplate < this.worldTemplateObjectSelectionList.getItemCount();
   }

   private void onTrailer() {
      if (this.m_89737_()) {
         WorldTemplate worldtemplate = this.m_89727_();
         if (!"".equals(worldtemplate.trailer)) {
            Util.getPlatform().openUri(worldtemplate.trailer);
         }
      }

   }

   private void onPublish() {
      if (this.m_89737_()) {
         WorldTemplate worldtemplate = this.m_89727_();
         if (!"".equals(worldtemplate.link)) {
            Util.getPlatform().openUri(worldtemplate.link);
         }
      }

   }

   private void fetchTemplatesAsync(final WorldTemplatePaginatedList pOutput) {
      (new Thread("realms-template-fetcher") {
         public void run() {
            WorldTemplatePaginatedList worldtemplatepaginatedlist = pOutput;

            RealmsClient realmsclient = RealmsClient.create();
            while (worldtemplatepaginatedlist != null) {
               Either<WorldTemplatePaginatedList, String> either = RealmsSelectWorldTemplateScreen.this.fetchTemplates(worldtemplatepaginatedlist, realmsclient);
               worldtemplatepaginatedlist = RealmsSelectWorldTemplateScreen.this.minecraft.submit(() -> {
               if (either.right().isPresent()) {
                  RealmsSelectWorldTemplateScreen.LOGGER.error("Couldn't fetch templates: {}", either.right().get());
                  if (RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.isEmpty()) {
                     RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(I18n.get("mco.template.select.failure"));
                  }

                  return null;
               } else {
                  WorldTemplatePaginatedList worldtemplatepaginatedlist1 = either.left().get();

                  for(WorldTemplate worldtemplate : worldtemplatepaginatedlist1.templates) {
                     RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.addEntry(worldtemplate);
                  }

                  if (worldtemplatepaginatedlist1.templates.isEmpty()) {
                     if (RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.isEmpty()) {
                        String s = I18n.get("mco.template.select.none", "%link");
                        TextRenderingUtils.LineSegment textrenderingutils$linesegment = TextRenderingUtils.LineSegment.link(I18n.get("mco.template.select.none.linkTitle"), "https://aka.ms/MinecraftRealmsContentCreator");
                        RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(s, textrenderingutils$linesegment);
                     }

                     return null;
                  } else {
                     return worldtemplatepaginatedlist1;
                  }
               }
            }).join();
            }

         }
      }).start();
   }

   Either<WorldTemplatePaginatedList, String> fetchTemplates(WorldTemplatePaginatedList p_89656_, RealmsClient pRealmsClient) {
      try {
         return Either.left(pRealmsClient.fetchWorldTemplates(p_89656_.page + 1, p_89656_.size, this.worldType));
      } catch (RealmsServiceException realmsserviceexception) {
         return Either.right(realmsserviceexception.getMessage());
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
      this.f_89618_ = null;
      this.currentLink = null;
      this.f_89603_ = false;
      this.renderBackground(pGuiGraphics);
      this.worldTemplateObjectSelectionList.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      if (this.noTemplatesMessage != null) {
         this.renderMultilineMessage(pGuiGraphics, pMouseX, pMouseY, this.noTemplatesMessage);
      }

      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 13, 16777215);
      if (this.f_89602_) {
         Component[] acomponent = this.warning;

         for(int i = 0; i < acomponent.length; ++i) {
            int j = this.font.width(acomponent[i]);
            int k = this.width / 2 - j / 2;
            int l = row(-1 + i);
            if (pMouseX >= k && pMouseX <= k + j && pMouseY >= l && pMouseY <= l + 9) {
               this.f_89603_ = true;
            }
         }

         for(int i1 = 0; i1 < acomponent.length; ++i1) {
            Component component = acomponent[i1];
            int j1 = 10526880;
            if (this.f_89601_ != null) {
               if (this.f_89603_) {
                  j1 = 7107012;
                  component = component.copy().withStyle(ChatFormatting.STRIKETHROUGH);
               } else {
                  j1 = 3368635;
               }
            }

            pGuiGraphics.drawCenteredString(this.font, component, this.width / 2, row(-1 + i1), j1);
         }
      }

      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.m_280015_(pGuiGraphics, this.f_89618_, pMouseX, pMouseY);
   }

   private void renderMultilineMessage(GuiGraphics pGuiGraphics, int pX, int pY, List<TextRenderingUtils.Line> pLines) {
      for(int i = 0; i < pLines.size(); ++i) {
         TextRenderingUtils.Line textrenderingutils$line = pLines.get(i);
         int j = row(4 + i);
         int k = textrenderingutils$line.segments.stream().mapToInt((p_280748_) -> {
            return this.font.width(p_280748_.renderedText());
         }).sum();
         int l = this.width / 2 - k / 2;

         for(TextRenderingUtils.LineSegment textrenderingutils$linesegment : textrenderingutils$line.segments) {
            int i1 = textrenderingutils$linesegment.isLink() ? 3368635 : 16777215;
            int j1 = pGuiGraphics.drawString(this.font, textrenderingutils$linesegment.renderedText(), l, j, i1);
            if (textrenderingutils$linesegment.isLink() && pX > l && pX < j1 && pY > j - 3 && pY < j + 8) {
               this.f_89618_ = Component.literal(textrenderingutils$linesegment.getLinkUrl());
               this.currentLink = textrenderingutils$linesegment.getLinkUrl();
            }

            l = j1;
         }
      }

   }

   protected void m_280015_(GuiGraphics p_281524_, @Nullable Component p_281755_, int p_282387_, int p_281491_) {
      if (p_281755_ != null) {
         int i = p_282387_ + 12;
         int j = p_281491_ - 12;
         int k = this.font.width(p_281755_);
         p_281524_.fillGradient(i - 3, j - 3, i + k + 3, j + 8 + 3, -1073741824, -1073741824);
         p_281524_.drawString(this.font, p_281755_, i, j, 16777215);
      }
   }

   @OnlyIn(Dist.CLIENT)
   class Entry extends ObjectSelectionList.Entry<RealmsSelectWorldTemplateScreen.Entry> {
      final WorldTemplate template;

      public Entry(WorldTemplate pTemplate) {
         this.template = pTemplate;
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         this.m_280486_(pGuiGraphics, this.template, pLeft, pTop, pMouseX, pMouseY);
      }

      private void m_280486_(GuiGraphics p_282991_, WorldTemplate p_281775_, int p_281335_, int p_282289_, int p_281708_, int p_281391_) {
         int i = p_281335_ + 45 + 20;
         p_282991_.drawString(RealmsSelectWorldTemplateScreen.this.font, p_281775_.name, i, p_282289_ + 2, 16777215, false);
         p_282991_.drawString(RealmsSelectWorldTemplateScreen.this.font, p_281775_.author, i, p_282289_ + 15, 7105644, false);
         p_282991_.drawString(RealmsSelectWorldTemplateScreen.this.font, p_281775_.version, i + 227 - RealmsSelectWorldTemplateScreen.this.font.width(p_281775_.version), p_282289_ + 1, 7105644, false);
         if (!"".equals(p_281775_.link) || !"".equals(p_281775_.trailer) || !"".equals(p_281775_.recommendedPlayers)) {
            this.m_280395_(p_282991_, i - 1, p_282289_ + 25, p_281708_, p_281391_, p_281775_.link, p_281775_.trailer, p_281775_.recommendedPlayers);
         }

         this.m_280563_(p_282991_, p_281335_, p_282289_ + 1, p_281708_, p_281391_, p_281775_);
      }

      private void m_280563_(GuiGraphics p_282450_, int p_281877_, int p_282680_, int p_281921_, int p_283193_, WorldTemplate p_282405_) {
         p_282450_.blit(RealmsTextureManager.worldTemplate(p_282405_.id, p_282405_.image), p_281877_ + 1, p_282680_ + 1, 0.0F, 0.0F, 38, 38, 38, 38);
         p_282450_.blit(RealmsSelectWorldTemplateScreen.f_89608_, p_281877_, p_282680_, 0.0F, 0.0F, 40, 40, 40, 40);
      }

      private void m_280395_(GuiGraphics p_281993_, int p_281797_, int p_281328_, int p_283015_, int p_281905_, String p_281390_, String p_281552_, String p_281807_) {
         if (!"".equals(p_281807_)) {
            p_281993_.drawString(RealmsSelectWorldTemplateScreen.this.font, p_281807_, p_281797_, p_281328_ + 4, 5000268, false);
         }

         int i = "".equals(p_281807_) ? 0 : RealmsSelectWorldTemplateScreen.this.font.width(p_281807_) + 2;
         boolean flag = false;
         boolean flag1 = false;
         boolean flag2 = "".equals(p_281390_);
         if (p_283015_ >= p_281797_ + i && p_283015_ <= p_281797_ + i + 32 && p_281905_ >= p_281328_ && p_281905_ <= p_281328_ + 15 && p_281905_ < RealmsSelectWorldTemplateScreen.this.height - 15 && p_281905_ > 32) {
            if (p_283015_ <= p_281797_ + 15 + i && p_283015_ > i) {
               if (flag2) {
                  flag1 = true;
               } else {
                  flag = true;
               }
            } else if (!flag2) {
               flag1 = true;
            }
         }

         if (!flag2) {
            float f = flag ? 15.0F : 0.0F;
            p_281993_.blit(RealmsSelectWorldTemplateScreen.f_89606_, p_281797_ + i, p_281328_, f, 0.0F, 15, 15, 30, 15);
         }

         if (!"".equals(p_281552_)) {
            int j = p_281797_ + i + (flag2 ? 0 : 17);
            float f1 = flag1 ? 15.0F : 0.0F;
            p_281993_.blit(RealmsSelectWorldTemplateScreen.f_89607_, j, p_281328_, f1, 0.0F, 15, 15, 30, 15);
         }

         if (flag) {
            RealmsSelectWorldTemplateScreen.this.f_89618_ = RealmsSelectWorldTemplateScreen.f_89609_;
            RealmsSelectWorldTemplateScreen.this.currentLink = p_281390_;
         } else if (flag1 && !"".equals(p_281552_)) {
            RealmsSelectWorldTemplateScreen.this.f_89618_ = RealmsSelectWorldTemplateScreen.f_89610_;
            RealmsSelectWorldTemplateScreen.this.currentLink = p_281552_;
         }

      }

      public Component getNarration() {
         Component component = CommonComponents.joinLines(Component.literal(this.template.name), Component.translatable("mco.template.select.narrate.authors", this.template.author), Component.literal(this.template.recommendedPlayers), Component.translatable("mco.template.select.narrate.version", this.template.version));
         return Component.translatable("narrator.select", component);
      }
   }

   @OnlyIn(Dist.CLIENT)
   class WorldTemplateObjectSelectionList extends RealmsObjectSelectionList<RealmsSelectWorldTemplateScreen.Entry> {
      public WorldTemplateObjectSelectionList() {
         this(Collections.emptyList());
      }

      public WorldTemplateObjectSelectionList(Iterable<WorldTemplate> pTemplates) {
         super(RealmsSelectWorldTemplateScreen.this.width, RealmsSelectWorldTemplateScreen.this.height, RealmsSelectWorldTemplateScreen.this.f_89602_ ? RealmsSelectWorldTemplateScreen.row(1) : 32, RealmsSelectWorldTemplateScreen.this.height - 40, 46);
         pTemplates.forEach(this::addEntry);
      }

      public void addEntry(WorldTemplate p_89805_) {
         this.addEntry(RealmsSelectWorldTemplateScreen.this.new Entry(p_89805_));
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
         if (pButton == 0 && pMouseY >= (double)this.y0 && pMouseY <= (double)this.y1) {
            int i = this.width / 2 - 150;
            if (RealmsSelectWorldTemplateScreen.this.currentLink != null) {
               Util.getPlatform().openUri(RealmsSelectWorldTemplateScreen.this.currentLink);
            }

            int j = (int)Math.floor(pMouseY - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
            int k = j / this.itemHeight;
            if (pMouseX >= (double)i && pMouseX < (double)this.getScrollbarPosition() && k >= 0 && j >= 0 && k < this.getItemCount()) {
               this.selectItem(k);
               this.m_7980_(j, k, pMouseX, pMouseY, this.width, pButton);
               if (k >= RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.getItemCount()) {
                  return super.mouseClicked(pMouseX, pMouseY, pButton);
               }

               RealmsSelectWorldTemplateScreen.this.f_89599_ += 7;
               if (RealmsSelectWorldTemplateScreen.this.f_89599_ >= 10) {
                  RealmsSelectWorldTemplateScreen.this.selectTemplate();
               }

               return true;
            }
         }

         return super.mouseClicked(pMouseX, pMouseY, pButton);
      }

      public void setSelected(@Nullable RealmsSelectWorldTemplateScreen.Entry pSelected) {
         super.setSelected(pSelected);
         RealmsSelectWorldTemplateScreen.this.selectedTemplate = this.children().indexOf(pSelected);
         RealmsSelectWorldTemplateScreen.this.updateButtonStates();
      }

      public int getMaxPosition() {
         return this.getItemCount() * 46;
      }

      public int getRowWidth() {
         return 300;
      }

      public void m_7733_(GuiGraphics p_282384_) {
         RealmsSelectWorldTemplateScreen.this.renderBackground(p_282384_);
      }

      public boolean isEmpty() {
         return this.getItemCount() == 0;
      }

      public WorldTemplate m_89811_(int p_89812_) {
         return (this.children().get(p_89812_)).template;
      }

      public List<WorldTemplate> getTemplates() {
         return this.children().stream().map((p_89814_) -> {
            return p_89814_.template;
         }).collect(Collectors.toList());
      }
   }
}