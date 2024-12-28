package net.minecraft.client.gui.screens.reporting;

import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.GenericWaitingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.WarningScreen;
import net.minecraft.client.multiplayer.chat.report.ChatReportBuilder;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ThrowingComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ChatReportScreen extends Screen {
   private static final int BUTTON_WIDTH = 120;
   private static final int f_238565_ = 20;
   private static final int f_238807_ = 20;
   private static final int f_238678_ = 10;
   private static final int f_238671_ = 25;
   private static final int f_238745_ = 280;
   private static final int f_238558_ = 300;
   private static final Component f_238771_ = Component.translatable("gui.chatReport.observed_what");
   private static final Component f_238723_ = Component.translatable("gui.chatReport.select_reason");
   private static final Component f_238607_ = Component.translatable("gui.chatReport.more_comments");
   private static final Component f_238545_ = Component.translatable("gui.chatReport.describe");
   private static final Component f_238530_ = Component.translatable("gui.chatReport.report_sent_msg");
   private static final Component SELECT_CHAT_MESSAGE = Component.translatable("gui.chatReport.select_chat");
   private static final Component f_238783_ = Component.translatable("gui.abuseReport.sending.title").withStyle(ChatFormatting.BOLD);
   private static final Component f_240228_ = Component.translatable("gui.abuseReport.sent.title").withStyle(ChatFormatting.BOLD);
   private static final Component f_240232_ = Component.translatable("gui.abuseReport.error.title").withStyle(ChatFormatting.BOLD);
   private static final Component f_238555_ = Component.translatable("gui.abuseReport.send.generic_error");
   private static final Logger f_238568_ = LogUtils.getLogger();
   @Nullable
   final Screen f_238596_;
   private final ReportingContext f_238816_;
   @Nullable
   private MultiLineLabel f_238551_;
   @Nullable
   private MultiLineEditBox commentBox;
   private Button sendButton;
   private ChatReportBuilder f_252515_;
   @Nullable
   private ChatReportBuilder.CannotBuildReason f_238773_;

   private ChatReportScreen(@Nullable Screen pLastScreen, ReportingContext pReportContext, ChatReportBuilder p_254309_) {
      super(Component.translatable("gui.chatReport.title"));
      this.f_238596_ = pLastScreen;
      this.f_238816_ = pReportContext;
      this.f_252515_ = p_254309_;
   }

   public ChatReportScreen(@Nullable Screen pLastScreen, ReportingContext pReportingContext, UUID pReportId) {
      this(pLastScreen, pReportingContext, new ChatReportBuilder(pReportId, pReportingContext.sender().reportLimits()));
   }

   public ChatReportScreen(@Nullable Screen pLastScreen, ReportingContext pReportingContext, ChatReportBuilder.ChatReport p_253775_) {
      this(pLastScreen, pReportingContext, new ChatReportBuilder(p_253775_, pReportingContext.sender().reportLimits()));
   }

   protected void init() {
      AbuseReportLimits abusereportlimits = this.f_238816_.sender().reportLimits();
      int i = this.width / 2;
      ReportReason reportreason = this.f_252515_.m_239339_();
      if (reportreason != null) {
         this.f_238551_ = MultiLineLabel.create(this.font, reportreason.description(), 280);
      } else {
         this.f_238551_ = null;
      }

      IntSet intset = this.f_252515_.m_239716_();
      Component component;
      if (intset.isEmpty()) {
         component = SELECT_CHAT_MESSAGE;
      } else {
         component = Component.translatable("gui.chatReport.selected_chat", intset.size());
      }

      this.addRenderableWidget(Button.builder(component, (p_280882_) -> {
         this.minecraft.setScreen(new ChatSelectionScreen(this, this.f_238816_, this.f_252515_, (p_239697_) -> {
            this.f_252515_ = p_239697_;
            this.onReportChanged();
         }));
      }).bounds(this.m_239357_(), this.m_239320_(), 280, 20).build());
      Component component1 = Optionull.mapOrDefault(reportreason, ReportReason::title, f_238723_);
      this.addRenderableWidget(Button.builder(component1, (p_280881_) -> {
         this.minecraft.setScreen(new ReportReasonSelectionScreen(this, this.f_252515_.m_239339_(), (p_239513_) -> {
            this.f_252515_.m_239097_(p_239513_);
            this.onReportChanged();
         }));
      }).bounds(this.m_239357_(), this.m_239099_(), 280, 20).build());
      this.commentBox = this.addRenderableWidget(new MultiLineEditBox(this.minecraft.font, this.m_239357_(), this.m_240099_(), 280, this.m_239485_() - this.m_240099_(), f_238545_, Component.translatable("gui.chatReport.comments")));
      this.commentBox.setValue(this.f_252515_.m_238976_());
      this.commentBox.setCharacterLimit(abusereportlimits.maxOpinionCommentsLength());
      this.commentBox.setValueListener((p_240036_) -> {
         this.f_252515_.m_239079_(p_240036_);
         this.onReportChanged();
      });
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (p_239971_) -> {
         this.onClose();
      }).bounds(i - 120, this.m_239333_(), 120, 20).build());
      this.sendButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.chatReport.send"), (p_239742_) -> {
         this.m_240000_();
      }).bounds(i + 10, this.m_239333_(), 120, 20).build());
      this.onReportChanged();
   }

   private void onReportChanged() {
      this.f_238773_ = this.f_252515_.m_239332_();
      this.sendButton.active = this.f_238773_ == null;
      this.sendButton.setTooltip(Optionull.map(this.f_238773_, (p_258134_) -> {
         return Tooltip.create(p_258134_.f_238631_());
      }));
   }

   private void m_240000_() {
      this.f_252515_.m_240128_(this.f_238816_).ifLeft((p_280883_) -> {
         CompletableFuture<?> completablefuture = this.f_238816_.sender().send(p_280883_.f_238815_(), p_280883_.f_238727_());
         this.minecraft.setScreen(GenericWaitingScreen.createWaiting(f_238783_, CommonComponents.GUI_CANCEL, () -> {
            this.minecraft.setScreen(this);
            completablefuture.cancel(true);
         }));
         completablefuture.handleAsync((p_240236_, p_240237_) -> {
            if (p_240237_ == null) {
               this.m_240265_();
            } else {
               if (p_240237_ instanceof CancellationException) {
                  return null;
               }

               this.m_240313_(p_240237_);
            }

            return null;
         }, this.minecraft);
      }).ifRight((p_242967_) -> {
         this.m_242964_(p_242967_.f_238631_());
      });
   }

   private void m_240265_() {
      this.m_253119_();
      this.minecraft.setScreen(GenericWaitingScreen.createCompleted(f_240228_, f_238530_, CommonComponents.GUI_DONE, () -> {
         this.minecraft.setScreen((Screen)null);
      }));
   }

   private void m_240313_(Throwable p_240314_) {
      f_238568_.error("Encountered error while sending abuse report", p_240314_);
      Throwable throwable = p_240314_.getCause();
      Component component;
      if (throwable instanceof ThrowingComponent throwingcomponent) {
         component = throwingcomponent.getComponent();
      } else {
         component = f_238555_;
      }

      this.m_242964_(component);
   }

   private void m_242964_(Component p_242978_) {
      Component component = p_242978_.copy().withStyle(ChatFormatting.RED);
      this.minecraft.setScreen(GenericWaitingScreen.createCompleted(f_240232_, component, CommonComponents.GUI_BACK, () -> {
         this.minecraft.setScreen(this);
      }));
   }

   void m_252889_() {
      if (this.f_252515_.m_252870_()) {
         this.f_238816_.m_253037_(this.f_252515_.m_253002_().m_252798_());
      }

   }

   void m_253119_() {
      this.f_238816_.m_253037_((ChatReportBuilder.ChatReport)null);
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics p_283069_, int p_239923_, int p_239924_, float p_239925_) {
      int i = this.width / 2;
      this.renderBackground(p_283069_);
      p_283069_.drawCenteredString(this.font, this.title, i, 10, 16777215);
      p_283069_.drawCenteredString(this.font, f_238771_, i, this.m_239320_() - 9 - 6, 16777215);
      if (this.f_238551_ != null) {
         this.f_238551_.renderLeftAligned(p_283069_, this.m_239357_(), this.m_239099_() + 20 + 5, 9, 16777215);
      }

      p_283069_.drawString(this.font, f_238607_, this.m_239357_(), this.m_240099_() - 9 - 6, 16777215);
      super.render(p_283069_, p_239923_, p_239924_, p_239925_);
   }

   public void tick() {
      this.commentBox.m_239213_();
      super.tick();
   }

   public void onClose() {
      if (this.f_252515_.m_252870_()) {
         this.minecraft.setScreen(new ChatReportScreen.DiscardReportWarningScreen());
      } else {
         this.minecraft.setScreen(this.f_238596_);
      }

   }

   public void removed() {
      this.m_252889_();
      super.removed();
   }

   /**
    * Called when a mouse button is released within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that was released.
    */
   public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
      return super.mouseReleased(pMouseX, pMouseY, pButton) ? true : this.commentBox.mouseReleased(pMouseX, pMouseY, pButton);
   }

   private int m_239357_() {
      return this.width / 2 - 140;
   }

   private int m_239146_() {
      return this.width / 2 + 140;
   }

   private int m_239871_() {
      return Math.max((this.height - 300) / 2, 0);
   }

   private int m_239033_() {
      return Math.min((this.height + 300) / 2, this.height);
   }

   private int m_239320_() {
      return this.m_239871_() + 40;
   }

   private int m_239099_() {
      return this.m_239320_() + 10 + 20;
   }

   private int m_240099_() {
      int i = this.m_239099_() + 20 + 25;
      if (this.f_238551_ != null) {
         i += (this.f_238551_.getLineCount() + 1) * 9;
      }

      return i;
   }

   private int m_239485_() {
      return this.m_239333_() - 20;
   }

   private int m_239333_() {
      return this.m_239033_() - 20 - 10;
   }

   @OnlyIn(Dist.CLIENT)
   class DiscardReportWarningScreen extends WarningScreen {
      private static final Component f_238729_ = Component.translatable("gui.chatReport.discard.title").withStyle(ChatFormatting.BOLD);
      private static final Component f_238704_ = Component.translatable("gui.chatReport.discard.content");
      private static final Component f_238630_ = Component.translatable("gui.chatReport.discard.return");
      private static final Component f_252405_ = Component.translatable("gui.chatReport.discard.draft");
      private static final Component f_238679_ = Component.translatable("gui.chatReport.discard.discard");

      protected DiscardReportWarningScreen() {
         super(f_238729_, f_238704_, f_238704_);
      }

      protected void initButtons(int p_239753_) {
         int i = 150;
         this.addRenderableWidget(Button.builder(f_238630_, (p_239525_) -> {
            this.onClose();
         }).bounds(this.width / 2 - 155, 100 + p_239753_, 150, 20).build());
         this.addRenderableWidget(Button.builder(f_252405_, (p_280885_) -> {
            ChatReportScreen.this.m_252889_();
            this.minecraft.setScreen(ChatReportScreen.this.f_238596_);
         }).bounds(this.width / 2 + 5, 100 + p_239753_, 150, 20).build());
         this.addRenderableWidget(Button.builder(f_238679_, (p_280886_) -> {
            ChatReportScreen.this.m_253119_();
            this.minecraft.setScreen(ChatReportScreen.this.f_238596_);
         }).bounds(this.width / 2 - 75, 130 + p_239753_, 150, 20).build());
      }

      public void onClose() {
         this.minecraft.setScreen(ChatReportScreen.this);
      }

      public boolean shouldCloseOnEsc() {
         return false;
      }

      protected void m_280550_(GuiGraphics p_282506_) {
         p_282506_.drawString(this.font, this.title, this.width / 2 - 155, 30, 16777215);
      }
   }
}