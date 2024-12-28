package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SymlinkWarningScreen extends Screen {
   private static final Component f_289817_ = Component.translatable("symlink_warning.title").withStyle(ChatFormatting.BOLD);
   private static final Component f_289825_ = Component.translatable("symlink_warning.message", "https://aka.ms/MinecraftSymLinks");
   @Nullable
   private final Screen f_289827_;
   private final GridLayout f_289819_ = (new GridLayout()).rowSpacing(10);

   public SymlinkWarningScreen(@Nullable Screen p_289989_) {
      super(f_289817_);
      this.f_289827_ = p_289989_;
   }

   protected void init() {
      super.init();
      this.f_289819_.defaultCellSetting().alignHorizontallyCenter();
      GridLayout.RowHelper gridlayout$rowhelper = this.f_289819_.createRowHelper(1);
      gridlayout$rowhelper.addChild(new StringWidget(this.title, this.font));
      gridlayout$rowhelper.addChild((new MultiLineTextWidget(f_289825_, this.font)).setMaxWidth(this.width - 50).setCentered(true));
      int i = 120;
      GridLayout gridlayout = (new GridLayout()).columnSpacing(5);
      GridLayout.RowHelper gridlayout$rowhelper1 = gridlayout.createRowHelper(3);
      gridlayout$rowhelper1.addChild(Button.builder(CommonComponents.GUI_OPEN_IN_BROWSER, (p_289977_) -> {
         Util.getPlatform().openUri("https://aka.ms/MinecraftSymLinks");
      }).size(120, 20).build());
      gridlayout$rowhelper1.addChild(Button.builder(CommonComponents.GUI_COPY_LINK_TO_CLIPBOARD, (p_289939_) -> {
         this.minecraft.keyboardHandler.setClipboard("https://aka.ms/MinecraftSymLinks");
      }).size(120, 20).build());
      gridlayout$rowhelper1.addChild(Button.builder(CommonComponents.GUI_BACK, (p_289963_) -> {
         this.onClose();
      }).size(120, 20).build());
      gridlayout$rowhelper.addChild(gridlayout);
      this.repositionElements();
      this.f_289819_.visitWidgets(this::addRenderableWidget);
   }

   protected void repositionElements() {
      this.f_289819_.arrangeElements();
      FrameLayout.centerInRectangle(this.f_289819_, this.getRectangle());
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics p_289954_, int p_289981_, int p_289931_, float p_289925_) {
      this.renderBackground(p_289954_);
      super.render(p_289954_, p_289981_, p_289931_, p_289925_);
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(super.getNarrationMessage(), f_289825_);
   }

   public void onClose() {
      this.minecraft.setScreen(this.f_289827_);
   }
}