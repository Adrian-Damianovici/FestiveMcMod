package net.minecraft.client.multiplayer.chat.report;

import com.google.common.collect.Lists;
import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportChatMessage;
import com.mojang.authlib.minecraft.report.ReportEvidence;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageLink;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class ChatReportBuilder {
   private final ChatReportBuilder.ChatReport f_252499_;
   private final AbuseReportLimits f_238736_;

   public ChatReportBuilder(ChatReportBuilder.ChatReport p_254092_, AbuseReportLimits p_254265_) {
      this.f_252499_ = p_254092_;
      this.f_238736_ = p_254265_;
   }

   public ChatReportBuilder(UUID p_239528_, AbuseReportLimits p_239529_) {
      this.f_252499_ = new ChatReportBuilder.ChatReport(UUID.randomUUID(), Instant.now(), p_239528_);
      this.f_238736_ = p_239529_;
   }

   public ChatReportBuilder.ChatReport m_253002_() {
      return this.f_252499_;
   }

   public UUID m_239436_() {
      return this.f_252499_.f_252536_;
   }

   public IntSet m_239716_() {
      return this.f_252499_.f_252475_;
   }

   public String m_238976_() {
      return this.f_252499_.f_252421_;
   }

   public void m_239079_(String p_239080_) {
      this.f_252499_.f_252421_ = p_239080_;
   }

   @Nullable
   public ReportReason m_239339_() {
      return this.f_252499_.f_252479_;
   }

   public void m_239097_(ReportReason p_239098_) {
      this.f_252499_.f_252479_ = p_239098_;
   }

   public void m_239051_(int p_239052_) {
      this.f_252499_.m_252761_(p_239052_, this.f_238736_);
   }

   public boolean m_240221_(int p_243333_) {
      return this.f_252499_.f_252475_.contains(p_243333_);
   }

   public boolean m_252870_() {
      return StringUtils.isNotEmpty(this.m_238976_()) || !this.m_239716_().isEmpty() || this.m_239339_() != null;
   }

   @Nullable
   public ChatReportBuilder.CannotBuildReason m_239332_() {
      if (this.f_252499_.f_252475_.isEmpty()) {
         return ChatReportBuilder.CannotBuildReason.f_238619_;
      } else if (this.f_252499_.f_252475_.size() > this.f_238736_.maxReportedMessageCount()) {
         return ChatReportBuilder.CannotBuildReason.f_238799_;
      } else if (this.f_252499_.f_252479_ == null) {
         return ChatReportBuilder.CannotBuildReason.f_238819_;
      } else {
         return this.f_252499_.f_252421_.length() > this.f_238736_.maxOpinionCommentsLength() ? ChatReportBuilder.CannotBuildReason.f_238583_ : null;
      }
   }

   public Either<ChatReportBuilder.Result, ChatReportBuilder.CannotBuildReason> m_240128_(ReportingContext p_240129_) {
      ChatReportBuilder.CannotBuildReason chatreportbuilder$cannotbuildreason = this.m_239332_();
      if (chatreportbuilder$cannotbuildreason != null) {
         return Either.right(chatreportbuilder$cannotbuildreason);
      } else {
         String s = Objects.requireNonNull(this.f_252499_.f_252479_).backendName();
         ReportEvidence reportevidence = this.m_239182_(p_240129_.chatLog());
         ReportedEntity reportedentity = new ReportedEntity(this.f_252499_.f_252536_);
         AbuseReport abusereport = new AbuseReport(this.f_252499_.f_252421_, s, reportevidence, reportedentity, this.f_252499_.f_252413_);
         return Either.left(new ChatReportBuilder.Result(this.f_252499_.f_252481_, abusereport));
      }
   }

   private ReportEvidence m_239182_(ChatLog p_239183_) {
      List<ReportChatMessage> list = new ArrayList<>();
      ChatReportContextBuilder chatreportcontextbuilder = new ChatReportContextBuilder(this.f_238736_.leadingContextMessageCount());
      chatreportcontextbuilder.collectAllContext(p_239183_, this.f_252499_.f_252475_, (p_247891_, p_247892_) -> {
         list.add(this.m_246289_(p_247892_, this.m_240221_(p_247891_)));
      });
      return new ReportEvidence(Lists.reverse(list));
   }

   private ReportChatMessage m_246289_(LoggedChatMessage.Player p_251321_, boolean p_252182_) {
      SignedMessageLink signedmessagelink = p_251321_.message().link();
      SignedMessageBody signedmessagebody = p_251321_.message().signedBody();
      List<ByteBuffer> list = signedmessagebody.lastSeen().entries().stream().map(MessageSignature::asByteBuffer).toList();
      ByteBuffer bytebuffer = Optionull.map(p_251321_.message().signature(), MessageSignature::asByteBuffer);
      return new ReportChatMessage(signedmessagelink.index(), signedmessagelink.sender(), signedmessagelink.sessionId(), signedmessagebody.timeStamp(), signedmessagebody.salt(), list, signedmessagebody.content(), bytebuffer, p_252182_);
   }

   public ChatReportBuilder m_239582_() {
      return new ChatReportBuilder(this.f_252499_.m_252798_(), this.f_238736_);
   }

   @OnlyIn(Dist.CLIENT)
   public static record CannotBuildReason(Component f_238631_) {
      public static final ChatReportBuilder.CannotBuildReason f_238819_ = new ChatReportBuilder.CannotBuildReason(Component.translatable("gui.chatReport.send.no_reason"));
      public static final ChatReportBuilder.CannotBuildReason f_238619_ = new ChatReportBuilder.CannotBuildReason(Component.translatable("gui.chatReport.send.no_reported_messages"));
      public static final ChatReportBuilder.CannotBuildReason f_238799_ = new ChatReportBuilder.CannotBuildReason(Component.translatable("gui.chatReport.send.too_many_messages"));
      public static final ChatReportBuilder.CannotBuildReason f_238583_ = new ChatReportBuilder.CannotBuildReason(Component.translatable("gui.chatReport.send.comments_too_long"));
   }

   @OnlyIn(Dist.CLIENT)
   public class ChatReport {
      final UUID f_252481_;
      final Instant f_252413_;
      final UUID f_252536_;
      final IntSet f_252475_ = new IntOpenHashSet();
      String f_252421_ = "";
      @Nullable
      ReportReason f_252479_;

      ChatReport(UUID p_254298_, Instant p_253854_, UUID p_253630_) {
         this.f_252481_ = p_254298_;
         this.f_252413_ = p_253854_;
         this.f_252536_ = p_253630_;
      }

      public void m_252761_(int p_254375_, AbuseReportLimits p_254456_) {
         if (this.f_252475_.contains(p_254375_)) {
            this.f_252475_.remove(p_254375_);
         } else if (this.f_252475_.size() < p_254456_.maxReportedMessageCount()) {
            this.f_252475_.add(p_254375_);
         }

      }

      public ChatReportBuilder.ChatReport m_252798_() {
         ChatReportBuilder.ChatReport chatreportbuilder$chatreport = ChatReportBuilder.this.new ChatReport(this.f_252481_, this.f_252413_, this.f_252536_);
         chatreportbuilder$chatreport.f_252475_.addAll(this.f_252475_);
         chatreportbuilder$chatreport.f_252421_ = this.f_252421_;
         chatreportbuilder$chatreport.f_252479_ = this.f_252479_;
         return chatreportbuilder$chatreport;
      }

      public boolean m_252787_(UUID p_253762_) {
         return p_253762_.equals(this.f_252536_);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static record Result(UUID f_238815_, AbuseReport f_238727_) {
   }
}