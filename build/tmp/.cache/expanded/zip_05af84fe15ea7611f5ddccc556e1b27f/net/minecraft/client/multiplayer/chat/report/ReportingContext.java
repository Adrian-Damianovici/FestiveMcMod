package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.UserApiService;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.reporting.ChatReportScreen;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ReportingContext {
   private static final int LOG_CAPACITY = 1024;
   private final AbuseReportSender sender;
   private final ReportEnvironment environment;
   private final ChatLog chatLog;
   @Nullable
   private ChatReportBuilder.ChatReport f_252449_;

   public ReportingContext(AbuseReportSender pSender, ReportEnvironment pEnviroment, ChatLog pChatLog) {
      this.sender = pSender;
      this.environment = pEnviroment;
      this.chatLog = pChatLog;
   }

   public static ReportingContext create(ReportEnvironment pEnvironment, UserApiService pUserApiService) {
      ChatLog chatlog = new ChatLog(1024);
      AbuseReportSender abusereportsender = AbuseReportSender.create(pEnvironment, pUserApiService);
      return new ReportingContext(abusereportsender, pEnvironment, chatlog);
   }

   public void draftReportHandled(Minecraft pMinecraft, @Nullable Screen pScreen, Runnable pQuitter, boolean pQuitToTitle) {
      if (this.f_252449_ != null) {
         ChatReportBuilder.ChatReport chatreportbuilder$chatreport = this.f_252449_.m_252798_();
         pMinecraft.setScreen(new ConfirmScreen((p_261387_) -> {
            this.m_253037_((ChatReportBuilder.ChatReport)null);
            if (p_261387_) {
               pMinecraft.setScreen(new ChatReportScreen(pScreen, this, chatreportbuilder$chatreport));
            } else {
               pQuitter.run();
            }

         }, Component.translatable(pQuitToTitle ? "gui.chatReport.draft.quittotitle.title" : "gui.chatReport.draft.title"), Component.translatable(pQuitToTitle ? "gui.chatReport.draft.quittotitle.content" : "gui.chatReport.draft.content"), Component.translatable("gui.chatReport.draft.edit"), Component.translatable("gui.chatReport.draft.discard")));
      } else {
         pQuitter.run();
      }

   }

   public AbuseReportSender sender() {
      return this.sender;
   }

   public ChatLog chatLog() {
      return this.chatLog;
   }

   public boolean matches(ReportEnvironment pEnvironment) {
      return Objects.equals(this.environment, pEnvironment);
   }

   public void m_253037_(@Nullable ChatReportBuilder.ChatReport p_254293_) {
      this.f_252449_ = p_254293_;
   }

   public boolean hasDraftReport() {
      return this.f_252449_ != null;
   }

   public boolean hasDraftReportFor(UUID pUuid) {
      return this.hasDraftReport() && this.f_252449_.m_252787_(pUuid);
   }
}