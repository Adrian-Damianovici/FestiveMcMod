package net.minecraft.server.network;

import com.google.common.primitives.Ints;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class ServerLoginPacketListenerImpl implements ServerLoginPacketListener, TickablePacketListener {
   private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_TICKS_BEFORE_LOGIN = 600;
   private static final RandomSource f_10016_ = RandomSource.create();
   private final byte[] challenge;
   final MinecraftServer server;
   final Connection connection;
   ServerLoginPacketListenerImpl.State state = ServerLoginPacketListenerImpl.State.HELLO;
   /** How long has player been trying to login into the server. */
   private int tick;
   @Nullable
   public GameProfile f_10021_;
   private final String serverId = "";
   @Nullable
   private ServerPlayer f_10024_;

   public ServerLoginPacketListenerImpl(MinecraftServer pServer, Connection pConnection) {
      this.server = pServer;
      this.connection = pConnection;
      this.challenge = Ints.toByteArray(f_10016_.nextInt());
   }

   public void tick() {
      if (this.state == State.NEGOTIATING) {
         // We force the state into "NEGOTIATING" which is otherwise unused. Once we're completed we move the negotiation onto "READY_TO_ACCEPT"
         // Might want to promote player object creation to here as well..
         boolean negotiationComplete = net.minecraftforge.network.NetworkHooks.tickNegotiation(this, this.connection, this.f_10024_);
         if (negotiationComplete)
            this.state = State.READY_TO_ACCEPT;
      } else if (this.state == ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT) {
         this.m_10055_();
      } else if (this.state == ServerLoginPacketListenerImpl.State.DELAY_ACCEPT) {
         ServerPlayer serverplayer = this.server.getPlayerList().getPlayer(this.f_10021_.getId());
         if (serverplayer == null) {
            this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
            this.m_143699_(this.f_10024_);
            this.f_10024_ = null;
         }
      }

      if (this.tick++ == 600) {
         this.disconnect(Component.translatable("multiplayer.disconnect.slow_login"));
      }

   }

   public boolean isAcceptingMessages() {
      return this.connection.isConnected();
   }

   public void disconnect(Component pReason) {
      try {
         LOGGER.info("Disconnecting {}: {}", this.getUserName(), pReason.getString());
         this.connection.send(new ClientboundLoginDisconnectPacket(pReason));
         this.connection.disconnect(pReason);
      } catch (Exception exception) {
         LOGGER.error("Error whilst disconnecting player", (Throwable)exception);
      }

   }

   public void m_10055_() {
      if (!this.f_10021_.isComplete()) {
         this.f_10021_ = this.m_10038_(this.f_10021_);
      }

      Component component = this.server.getPlayerList().canPlayerLogin(this.connection.getRemoteAddress(), this.f_10021_);
      if (component != null) {
         this.disconnect(component);
      } else {
         this.state = ServerLoginPacketListenerImpl.State.ACCEPTED;
         if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
            this.connection.send(new ClientboundLoginCompressionPacket(this.server.getCompressionThreshold()), PacketSendListener.thenRun(() -> {
               this.connection.setupCompression(this.server.getCompressionThreshold(), true);
            }));
         }

         this.connection.send(new ClientboundGameProfilePacket(this.f_10021_));
         ServerPlayer serverplayer = this.server.getPlayerList().getPlayer(this.f_10021_.getId());

         try {
            ServerPlayer serverplayer1 = this.server.getPlayerList().getPlayerForLogin(this.f_10021_);
            if (serverplayer != null) {
               this.state = ServerLoginPacketListenerImpl.State.DELAY_ACCEPT;
               this.f_10024_ = serverplayer1;
            } else {
               this.m_143699_(serverplayer1);
            }
         } catch (Exception exception) {
            LOGGER.error("Couldn't place player in world", (Throwable)exception);
            Component component1 = Component.translatable("multiplayer.disconnect.invalid_player_data");
            this.connection.send(new ClientboundDisconnectPacket(component1));
            this.connection.disconnect(component1);
         }
      }

   }

   private void m_143699_(ServerPlayer p_143700_) {
      this.server.getPlayerList().placeNewPlayer(this.connection, p_143700_);
   }

   /**
    * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
    */
   public void onDisconnect(Component pReason) {
      LOGGER.info("{} lost connection: {}", this.getUserName(), pReason.getString());
   }

   public String getUserName() {
      final String addressString = net.minecraftforge.network.DualStackUtils.getAddressString(this.connection.getRemoteAddress());
      return this.f_10021_ != null ? this.f_10021_ + " (" + addressString + ")" : addressString;
   }

   public void handleHello(ServerboundHelloPacket pPacket) {
      Validate.validState(this.state == ServerLoginPacketListenerImpl.State.HELLO, "Unexpected hello packet");
      Validate.validState(isValidUsername(pPacket.name()), "Invalid characters in username");
      GameProfile gameprofile = this.server.getSingleplayerProfile();
      if (gameprofile != null && pPacket.name().equalsIgnoreCase(gameprofile.getName())) {
         this.f_10021_ = gameprofile;
         this.state = ServerLoginPacketListenerImpl.State.NEGOTIATING; // FORGE: continue NEGOTIATING, we move to READY_TO_ACCEPT after Forge is ready
      } else {
         this.f_10021_ = new GameProfile((UUID)null, pPacket.name());
         if (this.server.usesAuthentication() && !this.connection.isMemoryConnection()) {
            this.state = ServerLoginPacketListenerImpl.State.KEY;
            this.connection.send(new ClientboundHelloPacket("", this.server.getKeyPair().getPublic().getEncoded(), this.challenge));
         } else {
            this.state = ServerLoginPacketListenerImpl.State.NEGOTIATING; // FORGE: continue NEGOTIATING, we move to READY_TO_ACCEPT after Forge is ready
         }

      }
   }

   public static boolean isValidUsername(String pUsername) {
      return pUsername.chars().filter((p_203791_) -> {
         return p_203791_ <= 32 || p_203791_ >= 127;
      }).findAny().isEmpty();
   }

   public void handleKey(ServerboundKeyPacket pPacket) {
      Validate.validState(this.state == ServerLoginPacketListenerImpl.State.KEY, "Unexpected key packet");

      final String s;
      try {
         PrivateKey privatekey = this.server.getKeyPair().getPrivate();
         if (!pPacket.isChallengeValid(this.challenge, privatekey)) {
            throw new IllegalStateException("Protocol error");
         }

         SecretKey secretkey = pPacket.getSecretKey(privatekey);
         Cipher cipher = Crypt.getCipher(2, secretkey);
         Cipher cipher1 = Crypt.getCipher(1, secretkey);
         s = (new BigInteger(Crypt.digestData("", this.server.getKeyPair().getPublic(), secretkey))).toString(16);
         this.state = ServerLoginPacketListenerImpl.State.AUTHENTICATING;
         this.connection.setEncryptionKey(cipher, cipher1);
      } catch (CryptException cryptexception) {
         throw new IllegalStateException("Protocol error", cryptexception);
      }

      Thread thread = new Thread(net.minecraftforge.fml.util.thread.SidedThreadGroups.SERVER, "User Authenticator #" + UNIQUE_THREAD_ID.incrementAndGet()) {
         public void run() {
            GameProfile gameprofile = ServerLoginPacketListenerImpl.this.f_10021_;

            try {
               ServerLoginPacketListenerImpl.this.f_10021_ = ServerLoginPacketListenerImpl.this.server.getSessionService().hasJoinedServer(new GameProfile((UUID)null, gameprofile.getName()), s, this.getAddress());
               if (ServerLoginPacketListenerImpl.this.f_10021_ != null) {
                  ServerLoginPacketListenerImpl.LOGGER.info("UUID of player {} is {}", ServerLoginPacketListenerImpl.this.f_10021_.getName(), ServerLoginPacketListenerImpl.this.f_10021_.getId());
                  ServerLoginPacketListenerImpl.this.state = ServerLoginPacketListenerImpl.State.NEGOTIATING; // FORGE: continue NEGOTIATING, we move to READY_TO_ACCEPT after Forge is ready
               } else if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
                  ServerLoginPacketListenerImpl.LOGGER.warn("Failed to verify username but will let them in anyway!");
                  ServerLoginPacketListenerImpl.this.f_10021_ = gameprofile;
                  ServerLoginPacketListenerImpl.this.state = ServerLoginPacketListenerImpl.State.NEGOTIATING; // FORGE: continue NEGOTIATING, we move to READY_TO_ACCEPT after Forge is ready
               } else {
                  ServerLoginPacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.unverified_username"));
                  ServerLoginPacketListenerImpl.LOGGER.error("Username '{}' tried to join with an invalid session", (Object)gameprofile.getName());
               }
            } catch (AuthenticationUnavailableException authenticationunavailableexception) {
               if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
                  ServerLoginPacketListenerImpl.LOGGER.warn("Authentication servers are down but will let them in anyway!");
                  ServerLoginPacketListenerImpl.this.f_10021_ = gameprofile;
                  ServerLoginPacketListenerImpl.this.state = ServerLoginPacketListenerImpl.State.NEGOTIATING; // FORGE: continue NEGOTIATING, we move to READY_TO_ACCEPT after Forge is ready
               } else {
                  ServerLoginPacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.authservers_down"));
                  ServerLoginPacketListenerImpl.LOGGER.error("Couldn't verify username because servers are unavailable");
               }
            }

         }

         @Nullable
         private InetAddress getAddress() {
            SocketAddress socketaddress = ServerLoginPacketListenerImpl.this.connection.getRemoteAddress();
            return ServerLoginPacketListenerImpl.this.server.getPreventProxyConnections() && socketaddress instanceof InetSocketAddress ? ((InetSocketAddress)socketaddress).getAddress() : null;
         }
      };
      thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
      thread.start();
   }

   public void handleLoginAcknowledgement(ServerboundCustomQueryPacket p_10045_) {
      if (!net.minecraftforge.network.NetworkHooks.onCustomPayload(p_10045_, this.connection))
      this.disconnect(Component.translatable("multiplayer.disconnect.unexpected_query_response"));
   }

   protected GameProfile m_10038_(GameProfile p_10039_) {
      UUID uuid = UUIDUtil.createOfflinePlayerUUID(p_10039_.getName());
      return new GameProfile(uuid, p_10039_.getName());
   }

   static enum State {
      HELLO,
      KEY,
      AUTHENTICATING,
      NEGOTIATING,
      READY_TO_ACCEPT,
      DELAY_ACCEPT,
      ACCEPTED;
   }
}
