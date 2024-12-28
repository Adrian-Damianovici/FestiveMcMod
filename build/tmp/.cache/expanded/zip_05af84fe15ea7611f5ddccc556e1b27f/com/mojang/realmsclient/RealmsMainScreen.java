package com.mojang.realmsclient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.mojang.realmsclient.client.Ping;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.RealmsNotification;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerList;
import com.mojang.realmsclient.dto.RegionPingResult;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.RealmsNewsManager;
import com.mojang.realmsclient.gui.RealmsServerList;
import com.mojang.realmsclient.gui.screens.RealmsClientOutdatedScreen;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsCreateRealmScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsParentalConsentScreen;
import com.mojang.realmsclient.gui.screens.RealmsPendingInvitesScreen;
import com.mojang.realmsclient.gui.task.DataFetcher;
import com.mojang.realmsclient.util.RealmsPersistence;
import com.mojang.realmsclient.util.RealmsUtil;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.CommonLinks;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsMainScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation f_86300_ = new ResourceLocation("realms", "textures/gui/realms/on_icon.png");
   private static final ResourceLocation f_86301_ = new ResourceLocation("realms", "textures/gui/realms/off_icon.png");
   private static final ResourceLocation f_86302_ = new ResourceLocation("realms", "textures/gui/realms/expired_icon.png");
   private static final ResourceLocation f_86303_ = new ResourceLocation("realms", "textures/gui/realms/expires_soon_icon.png");
   static final ResourceLocation f_86305_ = new ResourceLocation("realms", "textures/gui/realms/invitation_icons.png");
   static final ResourceLocation f_86306_ = new ResourceLocation("realms", "textures/gui/realms/invite_icon.png");
   static final ResourceLocation f_86307_ = new ResourceLocation("realms", "textures/gui/realms/world_icon.png");
   private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("realms", "textures/gui/title/realms.png");
   private static final ResourceLocation f_86311_ = new ResourceLocation("realms", "textures/gui/realms/news_icon.png");
   private static final ResourceLocation f_86312_ = new ResourceLocation("realms", "textures/gui/realms/popup.png");
   private static final ResourceLocation f_86231_ = new ResourceLocation("realms", "textures/gui/realms/darken.png");
   static final ResourceLocation f_86232_ = new ResourceLocation("realms", "textures/gui/realms/cross_icon.png");
   private static final ResourceLocation f_86233_ = new ResourceLocation("realms", "textures/gui/realms/trial_icon.png");
   static final ResourceLocation f_273868_ = new ResourceLocation("minecraft", "textures/gui/info_icon.png");
   static final List<Component> f_86237_ = ImmutableList.of(Component.translatable("mco.trial.message.line1"), Component.translatable("mco.trial.message.line2"));
   static final Component SERVER_UNITIALIZED_TEXT = Component.translatable("mco.selectServer.uninitialized");
   static final Component SUBSCRIPTION_EXPIRED_TEXT = Component.translatable("mco.selectServer.expiredList");
   private static final Component SUBSCRIPTION_RENEW_TEXT = Component.translatable("mco.selectServer.expiredRenew");
   static final Component TRIAL_EXPIRED_TEXT = Component.translatable("mco.selectServer.expiredTrial");
   static final Component SELECT_MINIGAME_PREFIX = Component.translatable("mco.selectServer.minigame").append(CommonComponents.SPACE);
   private static final Component f_86244_ = Component.translatable("mco.selectServer.popup");
   private static final Component PLAY_TEXT = Component.translatable("mco.selectServer.play");
   private static final Component LEAVE_SERVER_TEXT = Component.translatable("mco.selectServer.leave");
   private static final Component CONFIGURE_SERVER_TEXT = Component.translatable("mco.selectServer.configure");
   private static final Component SERVER_EXPIRED_TOOLTIP = Component.translatable("mco.selectServer.expired");
   private static final Component SERVER_EXPIRES_SOON_TOOLTIP = Component.translatable("mco.selectServer.expires.soon");
   private static final Component SERVER_EXPIRES_IN_DAY_TOOLTIP = Component.translatable("mco.selectServer.expires.day");
   private static final Component SERVER_OPEN_TOOLTIP = Component.translatable("mco.selectServer.open");
   private static final Component SERVER_CLOSED_TOOLTIP = Component.translatable("mco.selectServer.closed");
   private static final Component f_86253_ = Component.translatable("mco.news");
   static final Component UNITIALIZED_WORLD_NARRATION = Component.translatable("gui.narrate.button", SERVER_UNITIALIZED_TEXT);
   static final Component f_167173_ = CommonComponents.joinLines(f_86237_);
   private static final int BUTTON_WIDTH = 100;
   private static final int f_271412_ = 308;
   private static final int f_271271_ = 204;
   private static final int f_271314_ = 64;
   private static final int LOGO_WIDTH = 128;
   private static final int LOGO_HEIGHT = 34;
   private static final int LOGO_TEXTURE_WIDTH = 128;
   private static final int LOGO_TEXTURE_HEIGHT = 64;
   private static final int LOGO_PADDING = 5;
   private static final int HEADER_HEIGHT = 44;
   private static List<ResourceLocation> f_86254_ = ImmutableList.of();
   @Nullable
   private DataFetcher.Subscription dataSubscription;
   private RealmsServerList serverList;
   private final Set<UUID> handledSeenNotifications = new HashSet<>();
   private static boolean f_86256_;
   private static int f_86274_ = -1;
   static volatile boolean f_86275_;
   static volatile boolean f_86276_;
   static volatile boolean f_86277_;
   @Nullable
   static Screen f_86278_;
   private static boolean regionsPinged;
   private final RateLimiter inviteNarrationLimiter;
   private boolean f_86281_;
   final Screen lastScreen;
   RealmsMainScreen.RealmSelectionList realmSelectionList;
   private boolean f_167174_;
   private Button playButton;
   private Button backButton;
   private Button renewButton;
   private Button configureButton;
   private Button leaveButton;
   private List<RealmsServer> f_86291_ = ImmutableList.of();
   volatile int f_86292_;
   int f_86293_;
   private boolean f_86294_;
   boolean f_86295_;
   private boolean f_86296_;
   private volatile boolean trialsAvailable;
   private volatile boolean f_86298_;
   private volatile boolean f_86299_;
   volatile boolean f_86258_;
   @Nullable
   volatile String newsLink;
   private int f_86260_;
   private int f_86261_;
   private boolean f_86262_;
   private List<KeyCombo> f_86263_;
   long lastClickTime;
   private ReentrantLock f_86265_ = new ReentrantLock();
   private MultiLineLabel f_86266_ = MultiLineLabel.EMPTY;
   private final List<RealmsNotification> notifications = new ArrayList<>();
   private Button f_86268_;
   private RealmsMainScreen.PendingInvitesButton pendingInvitesButton;
   private Button newsButton;
   private Button f_86271_;
   private Button f_86272_;
   private Button f_86273_;

   public RealmsMainScreen(Screen pLastScreen) {
      super(GameNarrator.NO_TITLE);
      this.lastScreen = pLastScreen;
      this.inviteNarrationLimiter = RateLimiter.create((double)0.016666668F);
   }

   private boolean m_86318_() {
      if (m_86321_() && this.f_86294_) {
         if (this.trialsAvailable && !this.f_86298_) {
            return true;
         } else {
            for(RealmsServer realmsserver : this.f_86291_) {
               if (realmsserver.ownerUUID.equals(this.minecraft.getUser().m_92545_())) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public boolean m_86528_() {
      if (m_86321_() && this.f_86294_) {
         return this.f_86295_ ? true : this.f_86291_.isEmpty();
      } else {
         return false;
      }
   }

   public void init() {
      this.f_86263_ = Lists.newArrayList(new KeyCombo(new char[]{'3', '2', '1', '4', '5', '6'}, () -> {
         f_86256_ = !f_86256_;
      }), new KeyCombo(new char[]{'9', '8', '7', '1', '2', '3'}, () -> {
         if (RealmsClient.f_87157_ == RealmsClient.Environment.STAGE) {
            this.m_86351_();
         } else {
            this.m_86345_();
         }

      }), new KeyCombo(new char[]{'9', '8', '7', '4', '5', '6'}, () -> {
         if (RealmsClient.f_87157_ == RealmsClient.Environment.LOCAL) {
            this.m_86351_();
         } else {
            this.m_86348_();
         }

      }));
      if (f_86278_ != null) {
         this.minecraft.setScreen(f_86278_);
      } else {
         this.f_86265_ = new ReentrantLock();
         if (f_86277_ && !m_86321_()) {
            this.m_86342_();
         }

         this.m_86336_();
         if (!this.f_86281_) {
            this.minecraft.m_91372_(false);
         }

         this.f_86299_ = false;
         this.realmSelectionList = new RealmsMainScreen.RealmSelectionList();
         if (f_86274_ != -1) {
            this.realmSelectionList.setScrollAmount((double)f_86274_);
         }

         this.addWidget(this.realmSelectionList);
         this.f_167174_ = true;
         this.setInitialFocus(this.realmSelectionList);
         this.m_264302_();
         this.m_272189_();
         this.m_264589_();
         this.m_86513_((RealmsServer)null);
         this.f_86266_ = MultiLineLabel.create(this.font, f_86244_, 100);
         RealmsNewsManager realmsnewsmanager = this.minecraft.realmsDataFetcher().newsManager;
         this.f_86258_ = realmsnewsmanager.hasUnreadNews();
         this.newsLink = realmsnewsmanager.newsLink();
         if (this.serverList == null) {
            this.serverList = new RealmsServerList(this.minecraft);
         }

         if (this.dataSubscription != null) {
            this.dataSubscription.forceUpdate();
         }

      }
   }

   private static boolean m_86321_() {
      return f_86276_ && f_86275_;
   }

   public void m_264589_() {
      this.pendingInvitesButton = this.addRenderableWidget(new RealmsMainScreen.PendingInvitesButton());
      this.newsButton = this.addRenderableWidget(new RealmsMainScreen.NewsButton());
      this.f_86268_ = this.addRenderableWidget(Button.builder(Component.translatable("mco.selectServer.purchase"), (p_86597_) -> {
         this.f_86295_ = !this.f_86295_;
      }).bounds(this.width - 90, 12, 80, 20).build());
   }

   public void m_264302_() {
      this.f_86271_ = this.addWidget(Button.builder(Component.translatable("mco.selectServer.trial"), (p_280681_) -> {
         if (this.trialsAvailable && !this.f_86298_) {
            Util.getPlatform().openUri("https://aka.ms/startjavarealmstrial");
            this.minecraft.setScreen(this.lastScreen);
         }
      }).bounds(this.width / 2 + 52, this.m_86366_() + 137 - 20, 98, 20).build());
      this.f_86272_ = this.addWidget(Button.builder(Component.translatable("mco.selectServer.buy"), (p_231255_) -> {
         Util.getPlatform().openUri("https://aka.ms/BuyJavaRealms");
      }).bounds(this.width / 2 + 52, this.m_86366_() + 160 - 20, 98, 20).build());
      this.f_86273_ = this.addWidget(new RealmsMainScreen.CloseButton());
   }

   public void m_272189_() {
      this.playButton = Button.builder(PLAY_TEXT, (p_86659_) -> {
         this.play(this.getSelectedServer(), this);
      }).width(100).build();
      this.configureButton = Button.builder(CONFIGURE_SERVER_TEXT, (p_86672_) -> {
         this.configureClicked(this.getSelectedServer());
      }).width(100).build();
      this.renewButton = Button.builder(SUBSCRIPTION_RENEW_TEXT, (p_86622_) -> {
         this.onRenew(this.getSelectedServer());
      }).width(100).build();
      this.leaveButton = Button.builder(LEAVE_SERVER_TEXT, (p_86679_) -> {
         this.leaveClicked(this.getSelectedServer());
      }).width(100).build();
      this.backButton = Button.builder(CommonComponents.GUI_BACK, (p_280683_) -> {
         if (!this.f_86296_) {
            this.minecraft.setScreen(this.lastScreen);
         }

      }).width(100).build();
      GridLayout gridlayout = new GridLayout();
      GridLayout.RowHelper gridlayout$rowhelper = gridlayout.createRowHelper(1);
      LinearLayout linearlayout = gridlayout$rowhelper.addChild(new LinearLayout(308, 20, LinearLayout.Orientation.HORIZONTAL), gridlayout$rowhelper.newCellSettings().paddingBottom(4));
      linearlayout.addChild(this.playButton);
      linearlayout.addChild(this.configureButton);
      linearlayout.addChild(this.renewButton);
      LinearLayout linearlayout1 = gridlayout$rowhelper.addChild(new LinearLayout(204, 20, LinearLayout.Orientation.HORIZONTAL), gridlayout$rowhelper.newCellSettings().alignHorizontallyCenter());
      linearlayout1.addChild(this.leaveButton);
      linearlayout1.addChild(this.backButton);
      gridlayout.visitWidgets((p_272289_) -> {
         AbstractWidget abstractwidget = this.addRenderableWidget(p_272289_);
      });
      gridlayout.arrangeElements();
      FrameLayout.centerInRectangle(gridlayout, 0, this.height - 64, this.width, 64);
   }

   void m_86513_(@Nullable RealmsServer p_86514_) {
      this.backButton.active = true;
      if (m_86321_() && this.f_86294_) {
         boolean flag = this.m_86528_() && this.trialsAvailable && !this.f_86298_;
         this.f_86271_.visible = flag;
         this.f_86271_.active = flag;
         this.f_86272_.visible = this.m_86528_();
         this.f_86273_.visible = this.m_86528_();
         this.newsButton.active = true;
         this.newsButton.visible = this.newsLink != null;
         this.pendingInvitesButton.active = true;
         this.pendingInvitesButton.visible = true;
         this.f_86268_.active = !this.m_86528_();
         this.playButton.visible = !this.m_86528_();
         this.renewButton.visible = !this.m_86528_();
         this.leaveButton.visible = !this.m_86528_();
         this.configureButton.visible = !this.m_86528_();
         this.backButton.visible = !this.m_86528_();
         this.playButton.active = this.shouldPlayButtonBeActive(p_86514_);
         this.renewButton.active = this.shouldRenewButtonBeActive(p_86514_);
         this.leaveButton.active = this.shouldLeaveButtonBeActive(p_86514_);
         this.configureButton.active = this.shouldConfigureButtonBeActive(p_86514_);
      } else {
         hideWidgets(new AbstractWidget[]{this.playButton, this.renewButton, this.configureButton, this.f_86271_, this.f_86272_, this.f_86273_, this.newsButton, this.pendingInvitesButton, this.f_86268_, this.leaveButton});
      }
   }

   private boolean m_86324_() {
      return (!this.m_86528_() || this.f_86295_) && m_86321_() && this.f_86294_;
   }

   boolean shouldPlayButtonBeActive(@Nullable RealmsServer pRealmsServer) {
      return pRealmsServer != null && !pRealmsServer.expired && pRealmsServer.state == RealmsServer.State.OPEN;
   }

   private boolean shouldRenewButtonBeActive(@Nullable RealmsServer pRealmsServer) {
      return pRealmsServer != null && pRealmsServer.expired && this.isSelfOwnedServer(pRealmsServer);
   }

   private boolean shouldConfigureButtonBeActive(@Nullable RealmsServer pRealmsServer) {
      return pRealmsServer != null && this.isSelfOwnedServer(pRealmsServer);
   }

   private boolean shouldLeaveButtonBeActive(@Nullable RealmsServer pRealmsServer) {
      return pRealmsServer != null && !this.isSelfOwnedServer(pRealmsServer);
   }

   public void tick() {
      super.tick();
      if (this.pendingInvitesButton != null) {
         this.pendingInvitesButton.m_86821_();
      }

      this.f_86296_ = false;
      ++this.f_86293_;
      boolean flag = m_86321_();
      if (this.dataSubscription == null && flag) {
         this.dataSubscription = this.initDataFetcher(this.minecraft.realmsDataFetcher());
      } else if (this.dataSubscription != null && !flag) {
         this.dataSubscription = null;
      }

      if (this.dataSubscription != null) {
         this.dataSubscription.tick();
      }

      if (this.m_86528_()) {
         ++this.f_86261_;
      }

      if (this.f_86268_ != null) {
         this.f_86268_.visible = this.m_86324_();
         this.f_86268_.active = this.f_86268_.visible;
      }

   }

   private DataFetcher.Subscription initDataFetcher(RealmsDataFetcher pDataFetcher) {
      DataFetcher.Subscription datafetcher$subscription = pDataFetcher.dataFetcher.createSubscription();
      datafetcher$subscription.subscribe(pDataFetcher.serverListUpdateTask, (p_275856_) -> {
         List<RealmsServer> list = this.serverList.updateServersList(p_275856_);
         boolean flag = false;

         for(RealmsServer realmsserver : list) {
            if (this.isSelfOwnedNonExpiredServer(realmsserver)) {
               flag = true;
            }
         }

         this.f_86291_ = list;
         this.f_86294_ = true;
         this.refreshRealmsSelectionList();
         if (!regionsPinged && flag) {
            regionsPinged = true;
            this.pingRegions();
         }

      });
      callRealmsClient(RealmsClient::getNotifications, (p_274622_) -> {
         this.notifications.clear();
         this.notifications.addAll(p_274622_);
         this.refreshRealmsSelectionList();
      });
      datafetcher$subscription.subscribe(pDataFetcher.pendingInvitesTask, (p_280682_) -> {
         this.f_86292_ = p_280682_;
         if (this.f_86292_ > 0 && this.inviteNarrationLimiter.tryAcquire(1)) {
            this.minecraft.getNarrator().sayNow(Component.translatable("mco.configure.world.invite.narration", this.f_86292_));
         }

      });
      datafetcher$subscription.subscribe(pDataFetcher.trialAvailabilityTask, (p_238839_) -> {
         if (!this.f_86298_) {
            if (p_238839_ != this.trialsAvailable && this.m_86528_()) {
               this.trialsAvailable = p_238839_;
               this.f_86299_ = false;
            } else {
               this.trialsAvailable = p_238839_;
            }

         }
      });
      datafetcher$subscription.subscribe(pDataFetcher.f_87800_, (p_238847_) -> {
         for(RealmsServerPlayerList realmsserverplayerlist : p_238847_.servers) {
            for(RealmsServer realmsserver : this.f_86291_) {
               if (realmsserver.id == realmsserverplayerlist.serverId) {
                  realmsserver.updateServerPing(realmsserverplayerlist);
                  break;
               }
            }
         }

      });
      datafetcher$subscription.subscribe(pDataFetcher.newsTask, (p_231355_) -> {
         pDataFetcher.newsManager.updateUnreadNews(p_231355_);
         this.f_86258_ = pDataFetcher.newsManager.hasUnreadNews();
         this.newsLink = pDataFetcher.newsManager.newsLink();
         this.m_86513_((RealmsServer)null);
      });
      return datafetcher$subscription;
   }

   private static <T> void callRealmsClient(RealmsMainScreen.RealmsCall<T> pCall, Consumer<T> pOnFinish) {
      Minecraft minecraft = Minecraft.getInstance();
      CompletableFuture.supplyAsync(() -> {
         try {
            return pCall.request(RealmsClient.create(minecraft));
         } catch (RealmsServiceException realmsserviceexception) {
            throw new RuntimeException(realmsserviceexception);
         }
      }).thenAcceptAsync(pOnFinish, minecraft).exceptionally((p_274626_) -> {
         LOGGER.error("Failed to execute call to Realms Service", p_274626_);
         return null;
      });
   }

   private void refreshRealmsSelectionList() {
      boolean flag = !this.f_86294_;
      this.realmSelectionList.clear();
      List<UUID> list = new ArrayList<>();

      for(RealmsNotification realmsnotification : this.notifications) {
         this.addEntriesForNotification(this.realmSelectionList, realmsnotification);
         if (!realmsnotification.seen() && !this.handledSeenNotifications.contains(realmsnotification.uuid())) {
            list.add(realmsnotification.uuid());
         }
      }

      if (!list.isEmpty()) {
         callRealmsClient((p_274625_) -> {
            p_274625_.notificationsSeen(list);
            return null;
         }, (p_274630_) -> {
            this.handledSeenNotifications.addAll(list);
         });
      }

      if (this.m_86318_()) {
         this.realmSelectionList.addEntry(new RealmsMainScreen.TrialEntry());
      }

      RealmsMainScreen.Entry realmsmainscreen$entry = null;
      RealmsServer realmsserver1 = this.getSelectedServer();

      for(RealmsServer realmsserver : this.f_86291_) {
         RealmsMainScreen.ServerEntry realmsmainscreen$serverentry = new RealmsMainScreen.ServerEntry(realmsserver);
         this.realmSelectionList.addEntry(realmsmainscreen$serverentry);
         if (realmsserver1 != null && realmsserver1.id == realmsserver.id) {
            realmsmainscreen$entry = realmsmainscreen$serverentry;
         }
      }

      if (flag) {
         this.m_86513_((RealmsServer)null);
      } else {
         this.realmSelectionList.setSelected(realmsmainscreen$entry);
      }

   }

   private void addEntriesForNotification(RealmsMainScreen.RealmSelectionList pSelectionList, RealmsNotification pNotification) {
      if (pNotification instanceof RealmsNotification.VisitUrl realmsnotification$visiturl) {
         pSelectionList.addEntry(new RealmsMainScreen.NotificationMessageEntry(realmsnotification$visiturl.getMessage(), realmsnotification$visiturl));
         pSelectionList.addEntry(new RealmsMainScreen.ButtonEntry(realmsnotification$visiturl.buildOpenLinkButton(this)));
      }

   }

   void m_240107_() {
      if (this.dataSubscription != null) {
         this.dataSubscription.reset();
      }

   }

   private void pingRegions() {
      (new Thread(() -> {
         List<RegionPingResult> list = Ping.pingAllRegions();
         RealmsClient realmsclient = RealmsClient.create();
         PingResult pingresult = new PingResult();
         pingresult.pingResults = list;
         pingresult.worldIds = this.getOwnedNonExpiredWorldIds();

         try {
            realmsclient.sendPingResults(pingresult);
         } catch (Throwable throwable) {
            LOGGER.warn("Could not send ping result to Realms: ", throwable);
         }

      })).start();
   }

   private List<Long> getOwnedNonExpiredWorldIds() {
      List<Long> list = Lists.newArrayList();

      for(RealmsServer realmsserver : this.f_86291_) {
         if (this.isSelfOwnedNonExpiredServer(realmsserver)) {
            list.add(realmsserver.id);
         }
      }

      return list;
   }

   public void m_167190_(boolean p_167191_) {
      this.f_86298_ = p_167191_;
   }

   private void onRenew(@Nullable RealmsServer pRealmsServer) {
      if (pRealmsServer != null) {
         String s = CommonLinks.extendRealms(pRealmsServer.remoteSubscriptionId, this.minecraft.getUser().m_92545_(), pRealmsServer.expiredTrial);
         this.minecraft.keyboardHandler.setClipboard(s);
         Util.getPlatform().openUri(s);
      }

   }

   private void m_86336_() {
      if (!f_86277_) {
         f_86277_ = true;
         (new Thread("MCO Compatability Checker #1") {
            public void run() {
               RealmsClient realmsclient = RealmsClient.create();

               try {
                  RealmsClient.CompatibleVersionResponse realmsclient$compatibleversionresponse = realmsclient.clientCompatible();
                  if (realmsclient$compatibleversionresponse != RealmsClient.CompatibleVersionResponse.COMPATIBLE) {
                     RealmsMainScreen.f_86278_ = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen);
                     RealmsMainScreen.this.minecraft.execute(() -> {
                        RealmsMainScreen.this.minecraft.setScreen(RealmsMainScreen.f_86278_);
                     });
                     return;
                  }

                  RealmsMainScreen.this.m_86342_();
               } catch (RealmsServiceException realmsserviceexception) {
                  RealmsMainScreen.f_86277_ = false;
                  RealmsMainScreen.LOGGER.error("Couldn't connect to realms", (Throwable)realmsserviceexception);
                  if (realmsserviceexception.f_87773_ == 401) {
                     RealmsMainScreen.f_86278_ = new RealmsGenericErrorScreen(Component.translatable("mco.error.invalid.session.title"), Component.translatable("mco.error.invalid.session.message"), RealmsMainScreen.this.lastScreen);
                     RealmsMainScreen.this.minecraft.execute(() -> {
                        RealmsMainScreen.this.minecraft.setScreen(RealmsMainScreen.f_86278_);
                     });
                  } else {
                     RealmsMainScreen.this.minecraft.execute(() -> {
                        RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(realmsserviceexception, RealmsMainScreen.this.lastScreen));
                     });
                  }
               }

            }
         }).start();
      }

   }

   void m_86342_() {
      (new Thread("MCO Compatability Checker #1") {
         public void run() {
            RealmsClient realmsclient = RealmsClient.create();

            try {
               Boolean obool = realmsclient.m_87247_();
               if (obool) {
                  RealmsMainScreen.LOGGER.info("Realms is available for this user");
                  RealmsMainScreen.f_86275_ = true;
               } else {
                  RealmsMainScreen.LOGGER.info("Realms is not available for this user");
                  RealmsMainScreen.f_86275_ = false;
                  RealmsMainScreen.this.minecraft.execute(() -> {
                     RealmsMainScreen.this.minecraft.setScreen(new RealmsParentalConsentScreen(RealmsMainScreen.this.lastScreen));
                  });
               }

               RealmsMainScreen.f_86276_ = true;
            } catch (RealmsServiceException realmsserviceexception) {
               RealmsMainScreen.LOGGER.error("Couldn't connect to realms", (Throwable)realmsserviceexception);
               RealmsMainScreen.this.minecraft.execute(() -> {
                  RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(realmsserviceexception, RealmsMainScreen.this.lastScreen));
               });
            }

         }
      }).start();
   }

   private void m_86345_() {
      if (RealmsClient.f_87157_ != RealmsClient.Environment.STAGE) {
         (new Thread("MCO Stage Availability Checker #1") {
            public void run() {
               RealmsClient realmsclient = RealmsClient.create();

               try {
                  Boolean obool = realmsclient.m_87253_();
                  if (obool) {
                     RealmsClient.m_87206_();
                     RealmsMainScreen.LOGGER.info("Switched to stage");
                     RealmsMainScreen.this.m_240107_();
                  }
               } catch (RealmsServiceException realmsserviceexception) {
                  RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: {}", (Object)realmsserviceexception.toString());
               }

            }
         }).start();
      }

   }

   private void m_86348_() {
      if (RealmsClient.f_87157_ != RealmsClient.Environment.LOCAL) {
         (new Thread("MCO Local Availability Checker #1") {
            public void run() {
               RealmsClient realmsclient = RealmsClient.create();

               try {
                  Boolean obool = realmsclient.m_87253_();
                  if (obool) {
                     RealmsClient.m_87229_();
                     RealmsMainScreen.LOGGER.info("Switched to local");
                     RealmsMainScreen.this.m_240107_();
                  }
               } catch (RealmsServiceException realmsserviceexception) {
                  RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: {}", (Object)realmsserviceexception.toString());
               }

            }
         }).start();
      }

   }

   private void m_86351_() {
      RealmsClient.m_87221_();
      this.m_240107_();
   }

   private void configureClicked(@Nullable RealmsServer pRealmsServer) {
      if (pRealmsServer != null && (this.minecraft.getUser().m_92545_().equals(pRealmsServer.ownerUUID) || f_86256_)) {
         this.m_86357_();
         this.minecraft.setScreen(new RealmsConfigureWorldScreen(this, pRealmsServer.id));
      }

   }

   private void leaveClicked(@Nullable RealmsServer pRealmsServer) {
      if (pRealmsServer != null && !this.minecraft.getUser().m_92545_().equals(pRealmsServer.ownerUUID)) {
         this.m_86357_();
         Component component = Component.translatable("mco.configure.world.leave.question.line1");
         Component component1 = Component.translatable("mco.configure.world.leave.question.line2");
         this.minecraft.setScreen(new RealmsLongConfirmationScreen((p_231253_) -> {
            this.leaveServer(p_231253_, pRealmsServer);
         }, RealmsLongConfirmationScreen.Type.INFO, component, component1, true));
      }

   }

   private void m_86357_() {
      f_86274_ = (int)this.realmSelectionList.getScrollAmount();
   }

   @Nullable
   private RealmsServer getSelectedServer() {
      if (this.realmSelectionList == null) {
         return null;
      } else {
         RealmsMainScreen.Entry realmsmainscreen$entry = this.realmSelectionList.getSelected();
         return realmsmainscreen$entry != null ? realmsmainscreen$entry.getServer() : null;
      }
   }

   private void leaveServer(boolean pConfirmed, final RealmsServer pServer) {
      if (pConfirmed) {
         (new Thread("Realms-leave-server") {
            public void run() {
               try {
                  RealmsClient realmsclient = RealmsClient.create();
                  realmsclient.uninviteMyselfFrom(pServer.id);
                  RealmsMainScreen.this.minecraft.execute(() -> {
                     RealmsMainScreen.this.removeServer(pServer);
                  });
               } catch (RealmsServiceException realmsserviceexception) {
                  RealmsMainScreen.LOGGER.error("Couldn't configure world");
                  RealmsMainScreen.this.minecraft.execute(() -> {
                     RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(realmsserviceexception, RealmsMainScreen.this));
                  });
               }

            }
         }).start();
      }

      this.minecraft.setScreen(this);
   }

   void removeServer(RealmsServer pServer) {
      this.f_86291_ = this.serverList.removeItem(pServer);
      this.realmSelectionList.children().removeIf((p_231250_) -> {
         RealmsServer realmsserver = p_231250_.getServer();
         return realmsserver != null && realmsserver.id == pServer.id;
      });
      this.realmSelectionList.setSelected((RealmsMainScreen.Entry)null);
      this.m_86513_((RealmsServer)null);
      this.playButton.active = false;
   }

   void dismissNotification(UUID pUuid) {
      callRealmsClient((p_274628_) -> {
         p_274628_.notificationsDismiss(List.of(pUuid));
         return null;
      }, (p_274632_) -> {
         this.notifications.removeIf((p_274621_) -> {
            return p_274621_.dismissable() && pUuid.equals(p_274621_.uuid());
         });
         this.refreshRealmsSelectionList();
      });
   }

   public void resetScreen() {
      if (this.realmSelectionList != null) {
         this.realmSelectionList.setSelected((RealmsMainScreen.Entry)null);
      }

   }

   /**
    * Called when a keyboard key is pressed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the pressed key.
    * @param pScanCode the scan code of the pressed key.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean keyPressed(int p_86401_, int p_86402_, int p_86403_) {
      if (p_86401_ == 256) {
         this.f_86263_.forEach(KeyCombo::m_86227_);
         this.m_86360_();
         return true;
      } else {
         return super.keyPressed(p_86401_, p_86402_, p_86403_);
      }
   }

   void m_86360_() {
      if (this.m_86528_() && this.f_86295_) {
         this.f_86295_ = false;
      } else {
         this.minecraft.setScreen(this.lastScreen);
      }

   }

   /**
    * Called when a character is typed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pCodePoint the code point of the typed character.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean charTyped(char p_86388_, int p_86389_) {
      this.f_86263_.forEach((p_231245_) -> {
         p_231245_.m_86228_(p_86388_);
      });
      return true;
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pGuiGraphics);
      this.realmSelectionList.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.blit(LOGO_LOCATION, this.width / 2 - 64, 5, 0.0F, 0.0F, 128, 34, 128, 64);
      if (RealmsClient.f_87157_ == RealmsClient.Environment.STAGE) {
         this.m_86601_(pGuiGraphics);
      }

      if (RealmsClient.f_87157_ == RealmsClient.Environment.LOCAL) {
         this.m_86626_(pGuiGraphics);
      }

      if (this.m_86528_()) {
         pGuiGraphics.pose().pushPose();
         pGuiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
         this.m_280475_(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         pGuiGraphics.pose().popPose();
      } else {
         if (this.f_86299_) {
            this.m_86513_((RealmsServer)null);
            if (!this.f_167174_) {
               this.addWidget(this.realmSelectionList);
               this.f_167174_ = true;
            }

            this.playButton.active = this.shouldPlayButtonBeActive(this.getSelectedServer());
         }

         this.f_86299_ = false;
      }

      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      if (this.trialsAvailable && !this.f_86298_ && this.m_86528_()) {
         int i = 8;
         int j = 8;
         int k = 0;
         if ((Util.getMillis() / 800L & 1L) == 1L) {
            k = 8;
         }

         pGuiGraphics.pose().pushPose();
         pGuiGraphics.pose().translate(0.0F, 0.0F, 110.0F);
         pGuiGraphics.blit(f_86233_, this.f_86271_.getX() + this.f_86271_.getWidth() - 8 - 4, this.f_86271_.getY() + this.f_86271_.getHeight() / 2 - 4, 0.0F, (float)k, 8, 8, 8, 16);
         pGuiGraphics.pose().popPose();
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
   public boolean mouseClicked(double p_86397_, double p_86398_, int p_86399_) {
      if (this.updateButtonStates(p_86397_, p_86398_) && this.f_86295_) {
         this.f_86295_ = false;
         this.f_86296_ = true;
         return true;
      } else {
         return super.mouseClicked(p_86397_, p_86398_, p_86399_);
      }
   }

   private boolean updateButtonStates(double p_86394_, double p_86395_) {
      int i = this.m_86363_();
      int j = this.m_86366_();
      return p_86394_ < (double)(i - 5) || p_86394_ > (double)(i + 315) || p_86395_ < (double)(j - 5) || p_86395_ > (double)(j + 171);
   }

   private void m_280475_(GuiGraphics p_283329_, int p_290033_, int p_290032_, float p_290030_) {
      int i = this.m_86363_();
      int j = this.m_86366_();
      if (!this.f_86299_) {
         this.f_86260_ = 0;
         this.f_86261_ = 0;
         this.f_86262_ = true;
         this.m_86513_((RealmsServer)null);
         if (this.f_167174_) {
            this.removeWidget(this.realmSelectionList);
            this.f_167174_ = false;
         }

         this.minecraft.getNarrator().sayNow(f_86244_);
      }

      if (this.f_86294_) {
         this.f_86299_ = true;
      }

      p_283329_.setColor(1.0F, 1.0F, 1.0F, 0.7F);
      RenderSystem.enableBlend();
      p_283329_.blit(f_86231_, 0, 44, 0.0F, 0.0F, this.width, this.height - 44, 310, 166);
      RenderSystem.disableBlend();
      p_283329_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      p_283329_.blit(f_86312_, i, j, 0.0F, 0.0F, 310, 166, 310, 166);
      if (!f_86254_.isEmpty()) {
         p_283329_.blit(f_86254_.get(this.f_86260_), i + 7, j + 7, 0.0F, 0.0F, 195, 152, 195, 152);
         if (this.f_86261_ % 95 < 5) {
            if (!this.f_86262_) {
               this.f_86260_ = (this.f_86260_ + 1) % f_86254_.size();
               this.f_86262_ = true;
            }
         } else {
            this.f_86262_ = false;
         }
      }

      this.f_86266_.renderLeftAlignedNoShadow(p_283329_, this.width / 2 + 52, j + 7, 10, 16777215);
      this.f_86271_.render(p_283329_, p_290033_, p_290032_, p_290030_);
      this.f_86272_.render(p_283329_, p_290033_, p_290032_, p_290030_);
      this.f_86273_.render(p_283329_, p_290033_, p_290032_, p_290030_);
   }

   int m_86363_() {
      return (this.width - 310) / 2;
   }

   int m_86366_() {
      return this.height / 2 - 80;
   }

   public void play(@Nullable RealmsServer pRealmsServer, Screen pLastScreen) {
      if (pRealmsServer != null) {
         try {
            if (!this.f_86265_.tryLock(1L, TimeUnit.SECONDS)) {
               return;
            }

            if (this.f_86265_.getHoldCount() > 1) {
               return;
            }
         } catch (InterruptedException interruptedexception) {
            return;
         }

         this.f_86281_ = true;
         this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(pLastScreen, new GetServerDetailsTask(this, pLastScreen, pRealmsServer, this.f_86265_)));
      }

   }

   boolean isSelfOwnedServer(RealmsServer pServer) {
      return pServer.ownerUUID != null && pServer.ownerUUID.equals(this.minecraft.getUser().m_92545_());
   }

   private boolean isSelfOwnedNonExpiredServer(RealmsServer pServer) {
      return this.isSelfOwnedServer(pServer) && !pServer.expired;
   }

   void m_280597_(GuiGraphics p_282859_, int p_283367_, int p_283231_, int p_281593_, int p_281773_) {
      p_282859_.blit(f_86302_, p_283367_, p_283231_, 0.0F, 0.0F, 10, 28, 10, 28);
      if (p_281593_ >= p_283367_ && p_281593_ <= p_283367_ + 9 && p_281773_ >= p_283231_ && p_281773_ <= p_283231_ + 27 && p_281773_ < this.height - 40 && p_281773_ > 32 && !this.m_86528_()) {
         this.setTooltipForNextRenderPass(SERVER_EXPIRED_TOOLTIP);
      }

   }

   void m_280377_(GuiGraphics p_283382_, int p_282134_, int p_283200_, int p_281673_, int p_282920_, int p_282554_) {
      if (this.f_86293_ % 20 < 10) {
         p_283382_.blit(f_86303_, p_282134_, p_283200_, 0.0F, 0.0F, 10, 28, 20, 28);
      } else {
         p_283382_.blit(f_86303_, p_282134_, p_283200_, 10.0F, 0.0F, 10, 28, 20, 28);
      }

      if (p_281673_ >= p_282134_ && p_281673_ <= p_282134_ + 9 && p_282920_ >= p_283200_ && p_282920_ <= p_283200_ + 27 && p_282920_ < this.height - 40 && p_282920_ > 32 && !this.m_86528_()) {
         if (p_282554_ <= 0) {
            this.setTooltipForNextRenderPass(SERVER_EXPIRES_SOON_TOOLTIP);
         } else if (p_282554_ == 1) {
            this.setTooltipForNextRenderPass(SERVER_EXPIRES_IN_DAY_TOOLTIP);
         } else {
            this.setTooltipForNextRenderPass(Component.translatable("mco.selectServer.expires.days", p_282554_));
         }
      }

   }

   void m_280236_(GuiGraphics p_283235_, int p_281895_, int p_283564_, int p_281543_, int p_282977_) {
      p_283235_.blit(f_86300_, p_281895_, p_283564_, 0.0F, 0.0F, 10, 28, 10, 28);
      if (p_281543_ >= p_281895_ && p_281543_ <= p_281895_ + 9 && p_282977_ >= p_283564_ && p_282977_ <= p_283564_ + 27 && p_282977_ < this.height - 40 && p_282977_ > 32 && !this.m_86528_()) {
         this.setTooltipForNextRenderPass(SERVER_OPEN_TOOLTIP);
      }

   }

   void m_280129_(GuiGraphics p_281685_, int p_282388_, int p_282489_, int p_281732_, int p_283445_) {
      p_281685_.blit(f_86301_, p_282388_, p_282489_, 0.0F, 0.0F, 10, 28, 10, 28);
      if (p_281732_ >= p_282388_ && p_281732_ <= p_282388_ + 9 && p_283445_ >= p_282489_ && p_283445_ <= p_282489_ + 27 && p_283445_ < this.height - 40 && p_283445_ > 32 && !this.m_86528_()) {
         this.setTooltipForNextRenderPass(SERVER_CLOSED_TOOLTIP);
      }

   }

   void m_280162_(GuiGraphics p_282435_, int p_283627_, int p_282268_, boolean p_282717_, int p_282793_, int p_283443_, boolean p_282143_, boolean p_282764_) {
      boolean flag = false;
      if (p_283627_ >= p_282793_ && p_283627_ <= p_282793_ + 20 && p_282268_ >= p_283443_ && p_282268_ <= p_283443_ + 20) {
         flag = true;
      }

      if (!p_282764_) {
         p_282435_.setColor(0.5F, 0.5F, 0.5F, 1.0F);
      }

      boolean flag1 = p_282764_ && p_282143_;
      float f = flag1 ? 20.0F : 0.0F;
      p_282435_.blit(f_86311_, p_282793_, p_283443_, f, 0.0F, 20, 20, 40, 20);
      if (flag && p_282764_) {
         this.setTooltipForNextRenderPass(f_86253_);
      }

      p_282435_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      if (p_282717_ && p_282764_) {
         int i = flag ? 0 : (int)(Math.max(0.0F, Math.max(Mth.sin((float)(10 + this.f_86293_) * 0.57F), Mth.cos((float)this.f_86293_ * 0.35F))) * -6.0F);
         p_282435_.blit(f_86305_, p_282793_ + 10, p_283443_ + 2 + i, 40.0F, 0.0F, 8, 8, 48, 16);
      }

   }

   private void m_86626_(GuiGraphics p_282133_) {
      String s = "LOCAL!";
      p_282133_.pose().pushPose();
      p_282133_.pose().translate((float)(this.width / 2 - 25), 20.0F, 0.0F);
      p_282133_.pose().mulPose(Axis.ZP.rotationDegrees(-20.0F));
      p_282133_.pose().scale(1.5F, 1.5F, 1.5F);
      p_282133_.drawString(this.font, "LOCAL!", 0, 0, 8388479, false);
      p_282133_.pose().popPose();
   }

   private void m_86601_(GuiGraphics p_282858_) {
      String s = "STAGE!";
      p_282858_.pose().pushPose();
      p_282858_.pose().translate((float)(this.width / 2 - 25), 20.0F, 0.0F);
      p_282858_.pose().mulPose(Axis.ZP.rotationDegrees(-20.0F));
      p_282858_.pose().scale(1.5F, 1.5F, 1.5F);
      p_282858_.drawString(this.font, "STAGE!", 0, 0, -256, false);
      p_282858_.pose().popPose();
   }

   public RealmsMainScreen m_86660_() {
      RealmsMainScreen realmsmainscreen = new RealmsMainScreen(this.lastScreen);
      realmsmainscreen.init(this.minecraft, this.width, this.height);
      return realmsmainscreen;
   }

   public static void m_86406_(ResourceManager p_86407_) {
      Collection<ResourceLocation> collection = p_86407_.listResources("textures/gui/images", (p_193492_) -> {
         return p_193492_.getPath().endsWith(".png");
      }).keySet();
      f_86254_ = collection.stream().filter((p_231247_) -> {
         return p_231247_.getNamespace().equals("realms");
      }).toList();
   }

   @OnlyIn(Dist.CLIENT)
   class ButtonEntry extends RealmsMainScreen.Entry {
      private final Button button;
      private final int f_273836_ = RealmsMainScreen.this.width / 2 - 75;

      public ButtonEntry(Button pButton) {
         this.button = pButton;
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
         this.button.mouseClicked(pMouseX, pMouseY, pButton);
         return true;
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
         return this.button.keyPressed(pKeyCode, pScanCode, pModifiers) ? true : super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         this.button.setPosition(this.f_273836_, pTop + 4);
         this.button.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      }

      public Component getNarration() {
         return this.button.getMessage();
      }
   }

   @OnlyIn(Dist.CLIENT)
   class CloseButton extends RealmsMainScreen.CrossButton {
      public CloseButton() {
         super(RealmsMainScreen.this.m_86363_() + 4, RealmsMainScreen.this.m_86366_() + 4, (p_86775_) -> {
            RealmsMainScreen.this.m_86360_();
         }, Component.translatable("mco.selectServer.close"));
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class CrossButton extends Button {
      protected CrossButton(Button.OnPress pOnPress, Component pMessage) {
         this(0, 0, pOnPress, pMessage);
      }

      protected CrossButton(int p_275644_, int p_275716_, Button.OnPress p_275547_, Component p_275717_) {
         super(p_275644_, p_275716_, 14, 14, p_275717_, p_275547_, DEFAULT_NARRATION);
         this.setTooltip(Tooltip.create(p_275717_));
      }

      public void renderWidget(GuiGraphics p_281814_, int p_281517_, int p_282662_, float p_283217_) {
         float f = this.isHoveredOrFocused() ? 14.0F : 0.0F;
         p_281814_.blit(RealmsMainScreen.f_86232_, this.getX(), this.getY(), 0.0F, f, 14, 14, 14, 28);
      }
   }

   @OnlyIn(Dist.CLIENT)
   abstract class Entry extends ObjectSelectionList.Entry<RealmsMainScreen.Entry> {
      @Nullable
      public RealmsServer getServer() {
         return null;
      }
   }

   @OnlyIn(Dist.CLIENT)
   class NewsButton extends Button {
      private static final int f_278375_ = 20;

      public NewsButton() {
         super(RealmsMainScreen.this.width - 115, 12, 20, 20, Component.translatable("mco.news"), (p_274636_) -> {
            if (RealmsMainScreen.this.newsLink != null) {
               ConfirmLinkScreen.confirmLinkNow(RealmsMainScreen.this.newsLink, RealmsMainScreen.this, true);
               if (RealmsMainScreen.this.f_86258_) {
                  RealmsPersistence.RealmsPersistenceData realmspersistence$realmspersistencedata = RealmsPersistence.readFile();
                  realmspersistence$realmspersistencedata.hasUnreadNews = false;
                  RealmsMainScreen.this.f_86258_ = false;
                  RealmsPersistence.writeFile(realmspersistence$realmspersistencedata);
               }

            }
         }, DEFAULT_NARRATION);
      }

      public void renderWidget(GuiGraphics p_281287_, int p_282698_, int p_282096_, float p_283518_) {
         RealmsMainScreen.this.m_280162_(p_281287_, p_282698_, p_282096_, RealmsMainScreen.this.f_86258_, this.getX(), this.getY(), this.isHoveredOrFocused(), this.active);
      }
   }

   @OnlyIn(Dist.CLIENT)
   class NotificationMessageEntry extends RealmsMainScreen.Entry {
      private static final int SIDE_MARGINS = 40;
      private static final int f_273894_ = 36;
      private static final int OUTLINE_COLOR = -12303292;
      private final Component text;
      private final List<AbstractWidget> children = new ArrayList<>();
      @Nullable
      private final RealmsMainScreen.CrossButton dismissButton;
      private final MultiLineTextWidget textWidget;
      private final GridLayout gridLayout;
      private final FrameLayout textFrame;
      private int lastEntryWidth = -1;

      public NotificationMessageEntry(Component pText, RealmsNotification pNotification) {
         this.text = pText;
         this.gridLayout = new GridLayout();
         int i = 7;
         this.gridLayout.addChild(new ImageWidget(20, 20, RealmsMainScreen.f_273868_), 0, 0, this.gridLayout.newCellSettings().padding(7, 7, 0, 0));
         this.gridLayout.addChild(SpacerElement.width(40), 0, 0);
         this.textFrame = this.gridLayout.addChild(new FrameLayout(0, 9 * 3), 0, 1, this.gridLayout.newCellSettings().paddingTop(7));
         this.textWidget = this.textFrame.addChild((new MultiLineTextWidget(pText, RealmsMainScreen.this.font)).setCentered(true).setMaxRows(3), this.textFrame.newChildLayoutSettings().alignHorizontallyCenter().alignVerticallyTop());
         this.gridLayout.addChild(SpacerElement.width(40), 0, 2);
         if (pNotification.dismissable()) {
            this.dismissButton = this.gridLayout.addChild(new RealmsMainScreen.CrossButton((p_275478_) -> {
               RealmsMainScreen.this.dismissNotification(pNotification.uuid());
            }, Component.translatable("mco.notification.dismiss")), 0, 2, this.gridLayout.newCellSettings().alignHorizontallyRight().padding(0, 7, 7, 0));
         } else {
            this.dismissButton = null;
         }

         this.gridLayout.visitWidgets(this.children::add);
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
         return this.dismissButton != null && this.dismissButton.keyPressed(pKeyCode, pScanCode, pModifiers) ? true : super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }

      private void updateEntryWidth(int pEntryWidth) {
         if (this.lastEntryWidth != pEntryWidth) {
            this.refreshLayout(pEntryWidth);
            this.lastEntryWidth = pEntryWidth;
         }

      }

      private void refreshLayout(int pWidth) {
         int i = pWidth - 80;
         this.textFrame.setMinWidth(i);
         this.textWidget.setMaxWidth(i);
         this.gridLayout.arrangeElements();
      }

      public void renderBack(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
         super.renderBack(pGuiGraphics, pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, pIsMouseOver, pPartialTick);
         pGuiGraphics.renderOutline(pLeft - 2, pTop - 2, pWidth, 70, -12303292);
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         this.gridLayout.setPosition(pLeft, pTop);
         this.updateEntryWidth(pWidth - 4);
         this.children.forEach((p_280688_) -> {
            p_280688_.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         });
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
         if (this.dismissButton != null) {
            this.dismissButton.mouseClicked(pMouseX, pMouseY, pButton);
         }

         return true;
      }

      public Component getNarration() {
         return this.text;
      }
   }

   @OnlyIn(Dist.CLIENT)
   class PendingInvitesButton extends ImageButton {
      private static final Component f_278406_ = Component.translatable("mco.invites.title");
      private static final Tooltip f_278403_ = Tooltip.create(Component.translatable("mco.invites.nopending"));
      private static final Tooltip f_278506_ = Tooltip.create(Component.translatable("mco.invites.pending"));
      private static final int f_278435_ = 18;
      private static final int f_278376_ = 15;
      private static final int f_278408_ = 10;
      private static final int f_278385_ = 8;
      private static final int f_278453_ = 8;
      private static final int f_278437_ = 11;

      public PendingInvitesButton() {
         super(RealmsMainScreen.this.width / 2 + 64 + 10, 15, 18, 15, 0, 0, 15, RealmsMainScreen.f_86306_, 18, 30, (p_279110_) -> {
            RealmsMainScreen.this.minecraft.setScreen(new RealmsPendingInvitesScreen(RealmsMainScreen.this.lastScreen, f_278406_));
         }, f_278406_);
         this.setTooltip(f_278403_);
      }

      public void m_86821_() {
         this.setTooltip(RealmsMainScreen.this.f_86292_ == 0 ? f_278403_ : f_278506_);
      }

      public void renderWidget(GuiGraphics p_281409_, int p_282719_, int p_282753_, float p_281312_) {
         super.renderWidget(p_281409_, p_282719_, p_282753_, p_281312_);
         this.m_280587_(p_281409_);
      }

      private void m_280587_(GuiGraphics p_282293_) {
         boolean flag = this.active && RealmsMainScreen.this.f_86292_ != 0;
         if (flag) {
            int i = (Math.min(RealmsMainScreen.this.f_86292_, 6) - 1) * 8;
            int j = (int)(Math.max(0.0F, Math.max(Mth.sin((float)(10 + RealmsMainScreen.this.f_86293_) * 0.57F), Mth.cos((float)RealmsMainScreen.this.f_86293_ * 0.35F))) * -6.0F);
            float f = this.isHoveredOrFocused() ? 8.0F : 0.0F;
            p_282293_.blit(RealmsMainScreen.f_86305_, this.getX() + 11, this.getY() + j, (float)i, f, 8, 8, 48, 16);
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   class RealmSelectionList extends RealmsObjectSelectionList<RealmsMainScreen.Entry> {
      public RealmSelectionList() {
         super(RealmsMainScreen.this.width, RealmsMainScreen.this.height, 44, RealmsMainScreen.this.height - 64, 36);
      }

      public void setSelected(@Nullable RealmsMainScreen.Entry p_86849_) {
         super.setSelected(p_86849_);
         if (p_86849_ != null) {
            RealmsMainScreen.this.m_86513_(p_86849_.getServer());
         } else {
            RealmsMainScreen.this.m_86513_((RealmsServer)null);
         }

      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public int getRowWidth() {
         return 300;
      }
   }

   @OnlyIn(Dist.CLIENT)
   interface RealmsCall<T> {
      T request(RealmsClient pRealmsClient) throws RealmsServiceException;
   }

   @OnlyIn(Dist.CLIENT)
   class ServerEntry extends RealmsMainScreen.Entry {
      private static final int SKIN_HEAD_LARGE_WIDTH = 36;
      private final RealmsServer serverData;

      public ServerEntry(RealmsServer pServerData) {
         this.serverData = pServerData;
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         this.m_280291_(this.serverData, pGuiGraphics, pLeft, pTop, pMouseX, pMouseY);
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
         if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
            RealmsMainScreen.this.minecraft.setScreen(new RealmsCreateRealmScreen(this.serverData, RealmsMainScreen.this));
         } else if (RealmsMainScreen.this.shouldPlayButtonBeActive(this.serverData)) {
            if (Util.getMillis() - RealmsMainScreen.this.lastClickTime < 250L && this.isFocused()) {
               RealmsMainScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
               RealmsMainScreen.this.play(this.serverData, RealmsMainScreen.this);
            }

            RealmsMainScreen.this.lastClickTime = Util.getMillis();
         }

         return true;
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
         if (CommonInputs.selected(pKeyCode) && RealmsMainScreen.this.shouldPlayButtonBeActive(this.serverData)) {
            RealmsMainScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            RealmsMainScreen.this.play(this.serverData, RealmsMainScreen.this);
            return true;
         } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
         }
      }

      private void m_280291_(RealmsServer p_281434_, GuiGraphics p_283436_, int p_282392_, int p_283179_, int p_282272_, int p_281903_) {
         this.m_280176_(p_281434_, p_283436_, p_282392_ + 36, p_283179_, p_282272_, p_281903_);
      }

      private void m_280176_(RealmsServer p_282180_, GuiGraphics p_281405_, int p_281795_, int p_282842_, int p_283593_, int p_281798_) {
         if (p_282180_.state == RealmsServer.State.UNINITIALIZED) {
            p_281405_.blit(RealmsMainScreen.f_86307_, p_281795_ + 10, p_282842_ + 6, 0.0F, 0.0F, 40, 20, 40, 20);
            float f = 0.5F + (1.0F + Mth.sin((float)RealmsMainScreen.this.f_86293_ * 0.25F)) * 0.25F;
            int l = -16777216 | (int)(127.0F * f) << 16 | (int)(255.0F * f) << 8 | (int)(127.0F * f);
            p_281405_.drawCenteredString(RealmsMainScreen.this.font, RealmsMainScreen.SERVER_UNITIALIZED_TEXT, p_281795_ + 10 + 40 + 75, p_282842_ + 12, l);
         } else {
            int i = 225;
            int j = 2;
            this.renderStatusLights(p_282180_, p_281405_, p_281795_, p_282842_, p_283593_, p_281798_, 225, 2);
            if (!"0".equals(p_282180_.serverPing.nrOfPlayers)) {
               String s = ChatFormatting.GRAY + p_282180_.serverPing.nrOfPlayers;
               p_281405_.drawString(RealmsMainScreen.this.font, s, p_281795_ + 207 - RealmsMainScreen.this.font.width(s), p_282842_ + 3, 8421504, false);
               if (p_283593_ >= p_281795_ + 207 - RealmsMainScreen.this.font.width(s) && p_283593_ <= p_281795_ + 207 && p_281798_ >= p_282842_ + 1 && p_281798_ <= p_282842_ + 10 && p_281798_ < RealmsMainScreen.this.height - 40 && p_281798_ > 32 && !RealmsMainScreen.this.m_86528_()) {
                  RealmsMainScreen.this.setTooltipForNextRenderPass(Component.literal(p_282180_.serverPing.playerList));
               }
            }

            if (RealmsMainScreen.this.isSelfOwnedServer(p_282180_) && p_282180_.expired) {
               Component component = p_282180_.expiredTrial ? RealmsMainScreen.TRIAL_EXPIRED_TEXT : RealmsMainScreen.SUBSCRIPTION_EXPIRED_TEXT;
               int j1 = p_282842_ + 11 + 5;
               p_281405_.drawString(RealmsMainScreen.this.font, component, p_281795_ + 2, j1 + 1, 15553363, false);
            } else {
               if (p_282180_.worldType == RealmsServer.WorldType.MINIGAME) {
                  int i1 = 13413468;
                  int k = RealmsMainScreen.this.font.width(RealmsMainScreen.SELECT_MINIGAME_PREFIX);
                  p_281405_.drawString(RealmsMainScreen.this.font, RealmsMainScreen.SELECT_MINIGAME_PREFIX, p_281795_ + 2, p_282842_ + 12, 13413468, false);
                  p_281405_.drawString(RealmsMainScreen.this.font, p_282180_.getMinigameName(), p_281795_ + 2 + k, p_282842_ + 12, 7105644, false);
               } else {
                  p_281405_.drawString(RealmsMainScreen.this.font, p_282180_.getDescription(), p_281795_ + 2, p_282842_ + 12, 7105644, false);
               }

               if (!RealmsMainScreen.this.isSelfOwnedServer(p_282180_)) {
                  p_281405_.drawString(RealmsMainScreen.this.font, p_282180_.owner, p_281795_ + 2, p_282842_ + 12 + 11, 5000268, false);
               }
            }

            p_281405_.drawString(RealmsMainScreen.this.font, p_282180_.getName(), p_281795_ + 2, p_282842_ + 1, 16777215, false);
            RealmsUtil.renderPlayerFace(p_281405_, p_281795_ - 36, p_282842_, 32, p_282180_.ownerUUID);
         }
      }

      private void renderStatusLights(RealmsServer pRealmsServer, GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY, int pWidth, int pHeight) {
         int i = pX + pWidth + 22;
         if (pRealmsServer.expired) {
            RealmsMainScreen.this.m_280597_(pGuiGraphics, i, pY + pHeight, pMouseX, pMouseY);
         } else if (pRealmsServer.state == RealmsServer.State.CLOSED) {
            RealmsMainScreen.this.m_280129_(pGuiGraphics, i, pY + pHeight, pMouseX, pMouseY);
         } else if (RealmsMainScreen.this.isSelfOwnedServer(pRealmsServer) && pRealmsServer.daysLeft < 7) {
            RealmsMainScreen.this.m_280377_(pGuiGraphics, i, pY + pHeight, pMouseX, pMouseY, pRealmsServer.daysLeft);
         } else if (pRealmsServer.state == RealmsServer.State.OPEN) {
            RealmsMainScreen.this.m_280236_(pGuiGraphics, i, pY + pHeight, pMouseX, pMouseY);
         }

      }

      public Component getNarration() {
         return (Component)(this.serverData.state == RealmsServer.State.UNINITIALIZED ? RealmsMainScreen.UNITIALIZED_WORLD_NARRATION : Component.translatable("narrator.select", this.serverData.name));
      }

      @Nullable
      public RealmsServer getServer() {
         return this.serverData;
      }
   }

   @OnlyIn(Dist.CLIENT)
   class TrialEntry extends RealmsMainScreen.Entry {
      public void render(GuiGraphics p_282936_, int p_282868_, int p_282346_, int p_281297_, int p_282360_, int p_283241_, int p_282253_, int p_282299_, boolean p_282018_, float p_281364_) {
         this.m_86913_(p_282936_, p_282868_, p_281297_, p_282346_, p_282253_, p_282299_);
      }

      /**
       * Called when a mouse button is clicked within the GUI element.
       * <p>
       * @return {@code true} if the event is consumed, {@code false} otherwise.
       * @param pMouseX the X coordinate of the mouse.
       * @param pMouseY the Y coordinate of the mouse.
       * @param pButton the button that was clicked.
       */
      public boolean mouseClicked(double p_86910_, double p_86911_, int p_86912_) {
         RealmsMainScreen.this.f_86295_ = true;
         return true;
      }

      private void m_86913_(GuiGraphics p_283578_, int p_86915_, int p_86916_, int p_86917_, int p_86918_, int p_86919_) {
         int i = p_86917_ + 8;
         int j = 0;
         boolean flag = false;
         if (p_86916_ <= p_86918_ && p_86918_ <= (int)RealmsMainScreen.this.realmSelectionList.getScrollAmount() && p_86917_ <= p_86919_ && p_86919_ <= p_86917_ + 32) {
            flag = true;
         }

         int k = 8388479;
         if (flag && !RealmsMainScreen.this.m_86528_()) {
            k = 6077788;
         }

         for(Component component : RealmsMainScreen.f_86237_) {
            p_283578_.drawCenteredString(RealmsMainScreen.this.font, component, RealmsMainScreen.this.width / 2, i + j, k);
            j += 10;
         }

      }

      public Component getNarration() {
         return RealmsMainScreen.f_167173_;
      }
   }
}