package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsBrokenWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsTermsScreen;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.net.URL;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class GetServerDetailsTask extends LongRunningTask {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final RealmsServer server;
   private final Screen lastScreen;
   private final RealmsMainScreen f_90329_;
   private final ReentrantLock f_90330_;

   public GetServerDetailsTask(RealmsMainScreen p_90332_, Screen pLastScreen, RealmsServer pServer, ReentrantLock p_90335_) {
      this.lastScreen = pLastScreen;
      this.f_90329_ = p_90332_;
      this.server = pServer;
      this.f_90330_ = p_90335_;
   }

   public void run() {
      this.m_90409_(Component.translatable("mco.connect.connecting"));

      RealmsServerAddress realmsserveraddress;
      try {
         realmsserveraddress = this.fetchServerAddress();
      } catch (CancellationException cancellationexception) {
         LOGGER.info("User aborted connecting to realms");
         return;
      } catch (RealmsServiceException realmsserviceexception) {
         switch (realmsserviceexception.m_200945_(-1)) {
            case 6002:
               setScreen(new RealmsTermsScreen(this.lastScreen, this.f_90329_, this.server));
               return;
            case 6006:
               boolean flag1 = this.server.ownerUUID.equals(Minecraft.getInstance().getUser().m_92545_());
               setScreen((Screen)(flag1 ? new RealmsBrokenWorldScreen(this.lastScreen, this.f_90329_, this.server.id, this.server.worldType == RealmsServer.WorldType.MINIGAME) : new RealmsGenericErrorScreen(Component.translatable("mco.brokenworld.nonowner.title"), Component.translatable("mco.brokenworld.nonowner.error"), this.lastScreen)));
               return;
            default:
               this.m_87791_(realmsserviceexception.toString());
               LOGGER.error("Couldn't connect to world", (Throwable)realmsserviceexception);
               return;
         }
      } catch (TimeoutException timeoutexception) {
         this.m_5673_(Component.translatable("mco.errorMessage.connectionFailure"));
         return;
      } catch (Exception exception) {
         LOGGER.error("Couldn't connect to world", (Throwable)exception);
         this.m_87791_(exception.getLocalizedMessage());
         return;
      }

      boolean flag = realmsserveraddress.resourcePackUrl != null && realmsserveraddress.resourcePackHash != null;
      Screen screen = (Screen)(flag ? this.resourcePackDownloadConfirmationScreen(realmsserveraddress, this::connectScreen) : this.connectScreen(realmsserveraddress));
      setScreen(screen);
   }

   private RealmsServerAddress fetchServerAddress() throws RealmsServiceException, TimeoutException, CancellationException {
      RealmsClient realmsclient = RealmsClient.create();

      for(int i = 0; i < 40; ++i) {
         if (this.aborted()) {
            throw new CancellationException();
         }

         try {
            return realmsclient.join(this.server.id);
         } catch (RetryCallException retrycallexception) {
            pause((long)retrycallexception.delaySeconds);
         }
      }

      throw new TimeoutException();
   }

   public RealmsLongRunningMcoTaskScreen connectScreen(RealmsServerAddress p_167638_) {
      return new RealmsLongRunningMcoTaskScreen(this.lastScreen, new ConnectTask(this.lastScreen, this.server, p_167638_));
   }

   private RealmsLongConfirmationScreen resourcePackDownloadConfirmationScreen(RealmsServerAddress pServerAddress, Function<RealmsServerAddress, Screen> pScreenGetter) {
      BooleanConsumer booleanconsumer = (p_167645_) -> {
         try {
            if (p_167645_) {
               this.scheduleResourcePackDownload(pServerAddress).thenRun(() -> {
                  setScreen(pScreenGetter.apply(pServerAddress));
               }).exceptionally((p_287306_) -> {
                  Minecraft.getInstance().getDownloadedPackSource().clearServerPack();
                  LOGGER.error("Failed to download resource pack from {}", pServerAddress, p_287306_);
                  setScreen(new RealmsGenericErrorScreen(Component.translatable("mco.download.resourcePack.fail"), this.lastScreen));
                  return null;
               });
               return;
            }

            setScreen(this.lastScreen);
         } finally {
            if (this.f_90330_.isHeldByCurrentThread()) {
               this.f_90330_.unlock();
            }

         }

      };
      return new RealmsLongConfirmationScreen(booleanconsumer, RealmsLongConfirmationScreen.Type.INFO, Component.translatable("mco.configure.world.resourcepack.question.line1"), Component.translatable("mco.configure.world.resourcepack.question.line2"), true);
   }

   private CompletableFuture<?> scheduleResourcePackDownload(RealmsServerAddress pServerAddress) {
      try {
         return Minecraft.getInstance().getDownloadedPackSource().downloadAndSelectResourcePack(new URL(pServerAddress.resourcePackUrl), pServerAddress.resourcePackHash, false);
      } catch (Exception exception) {
         CompletableFuture<Void> completablefuture = new CompletableFuture<>();
         completablefuture.completeExceptionally(exception);
         return completablefuture;
      }
   }
}