package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.DebugQueryHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.DemoIntroScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.achievement.StatsUpdateListener;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.debug.BeeDebugRenderer;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.renderer.debug.NeighborsUpdateRenderer;
import net.minecraft.client.renderer.debug.WorldGenAttemptRenderer;
import net.minecraft.client.resources.sounds.BeeAggressiveSoundInstance;
import net.minecraft.client.resources.sounds.BeeFlyingSoundInstance;
import net.minecraft.client.resources.sounds.BeeSoundInstance;
import net.minecraft.client.resources.sounds.GuardianAttackSoundInstance;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.client.resources.sounds.SnifferSoundInstance;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.LocalChatSession;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.chat.SignedMessageLink;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundDeleteChatPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.Crypt;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfileKeyPair;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientPacketListener implements TickablePacketListener, ClientGamePacketListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Component f_104884_ = Component.translatable("disconnect.lost");
   private static final Component UNSECURE_SERVER_TOAST_TITLE = Component.translatable("multiplayer.unsecureserver.toast.title");
   private static final Component UNSERURE_SERVER_TOAST = Component.translatable("multiplayer.unsecureserver.toast");
   private static final Component INVALID_PACKET = Component.translatable("multiplayer.disconnect.invalid_packet");
   private static final Component CHAT_VALIDATION_FAILED_ERROR = Component.translatable("multiplayer.disconnect.chat_validation_failed");
   private static final int PENDING_OFFSET_THRESHOLD = 64;
   private final Connection f_104885_;
   private final List<ClientPacketListener.DeferredPacket> f_268637_ = new ArrayList<>();
   @Nullable
   private final ServerData f_244115_;
   private final GameProfile localGameProfile;
   private final Screen f_104887_;
   private final Minecraft f_104888_;
   /** Reference to the current ClientWorld instance, which many handler methods operate on */
   private ClientLevel level;
   private ClientLevel.ClientLevelData levelData;
   /** A mapping from player names to their respective GuiPlayerInfo (specifies the clients response time to the server) */
   private final Map<UUID, PlayerInfo> playerInfoMap = Maps.newHashMap();
   private final Set<PlayerInfo> listedPlayers = new ReferenceOpenHashSet<>();
   private final ClientAdvancements advancements;
   private final ClientSuggestionProvider suggestionsProvider;
   private final DebugQueryHandler debugQueryHandler = new DebugQueryHandler(this);
   private int serverChunkRadius = 3;
   private int serverSimulationDistance = 3;
   /**
    * Just an ordinary random number generator, used to randomize audio pitch of item/orb pickup and randomize both
    * particlespawn offset and velocity
    */
   private final RandomSource random = RandomSource.createThreadSafe();
   public CommandDispatcher<SharedSuggestionProvider> commands = new CommandDispatcher<>();
   private final RecipeManager recipeManager = new RecipeManager();
   private final UUID id = UUID.randomUUID();
   private Set<ResourceKey<Level>> levels;
   private LayeredRegistryAccess<ClientRegistryLayer> registryAccess = ClientRegistryLayer.createRegistryAccess();
   private FeatureFlagSet enabledFeatures = FeatureFlags.DEFAULT_FLAGS;
   private final WorldSessionTelemetryManager f_194191_;
   @Nullable
   private LocalChatSession chatSession;
   private SignedMessageChain.Encoder signedMessageEncoder = SignedMessageChain.Encoder.UNSIGNED;
   private LastSeenMessagesTracker lastSeenMessages = new LastSeenMessagesTracker(20);
   private MessageSignatureCache messageSignatureCache = MessageSignatureCache.createDefault();

   public ClientPacketListener(Minecraft pMinecraft, Screen p_254239_, Connection pConnection, @Nullable ServerData p_254072_, GameProfile p_254079_, WorldSessionTelemetryManager p_262115_) {
      this.f_104888_ = pMinecraft;
      this.f_104887_ = p_254239_;
      this.f_104885_ = pConnection;
      this.f_244115_ = p_254072_;
      this.localGameProfile = p_254079_;
      this.advancements = new ClientAdvancements(pMinecraft, p_262115_);
      this.suggestionsProvider = new ClientSuggestionProvider(this, pMinecraft);
      this.f_194191_ = p_262115_;
   }

   public ClientSuggestionProvider getSuggestionsProvider() {
      return this.suggestionsProvider;
   }

   public void close() {
      this.level = null;
      this.f_194191_.onDisconnect();
   }

   public RecipeManager getRecipeManager() {
      return this.recipeManager;
   }

   /**
    * Registers some server properties (gametype,hardcore-mode,terraintype,difficulty,player limit), creates a new
    * WorldClient and sets the player initial dimension
    */
   public void handleLogin(ClientboundLoginPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.gameMode = new MultiPlayerGameMode(this.f_104888_, this);
      this.registryAccess = this.registryAccess.replaceFrom(ClientRegistryLayer.REMOTE, pPacket.f_132366_());
      if (!this.f_104885_.isMemoryConnection()) {
         this.registryAccess.compositeAccess().registries().forEach((p_205542_) -> {
            p_205542_.value().resetTags();
         });
      }

      List<ResourceKey<Level>> list = Lists.newArrayList(pPacket.levels());
      Collections.shuffle(list);
      this.levels = Sets.newLinkedHashSet(list);
      ResourceKey<Level> resourcekey = pPacket.f_132368_();
      Holder<DimensionType> holder = this.registryAccess.compositeAccess().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(pPacket.f_132367_());
      this.serverChunkRadius = pPacket.chunkRadius();
      this.serverSimulationDistance = pPacket.simulationDistance();
      boolean flag = pPacket.f_132373_();
      boolean flag1 = pPacket.f_132374_();
      ClientLevel.ClientLevelData clientlevel$clientleveldata = new ClientLevel.ClientLevelData(Difficulty.NORMAL, pPacket.hardcore(), flag1);
      this.levelData = clientlevel$clientleveldata;
      this.level = new ClientLevel(this, clientlevel$clientleveldata, resourcekey, holder, this.serverChunkRadius, this.serverSimulationDistance, this.f_104888_::getProfiler, this.f_104888_.levelRenderer, flag, pPacket.f_132361_());
      this.f_104888_.setLevel(this.level);
      if (this.f_104888_.player == null) {
         this.f_104888_.player = this.f_104888_.gameMode.createPlayer(this.level, new StatsCounter(), new ClientRecipeBook());
         this.f_104888_.player.setYRot(-180.0F);
         if (this.f_104888_.getSingleplayerServer() != null) {
            this.f_104888_.getSingleplayerServer().setUUID(this.f_104888_.player.getUUID());
         }
      }

      this.f_104888_.debugRenderer.clear();
      this.f_104888_.player.resetPos();
      net.minecraftforge.client.ForgeHooksClient.firePlayerLogin(this.f_104888_.gameMode, this.f_104888_.player, this.f_104888_.getConnection().f_104885_);
      int i = pPacket.playerId();
      this.f_104888_.player.setId(i);
      this.level.m_104630_(i, this.f_104888_.player);
      this.f_104888_.player.input = new KeyboardInput(this.f_104888_.options);
      this.f_104888_.gameMode.adjustPlayer(this.f_104888_.player);
      this.f_104888_.cameraEntity = this.f_104888_.player;
      this.f_104888_.setScreen(new ReceivingLevelScreen());
      this.f_104888_.player.setReducedDebugInfo(pPacket.reducedDebugInfo());
      this.f_104888_.player.setShowDeathScreen(pPacket.showDeathScreen());
      this.f_104888_.player.setLastDeathLocation(pPacket.f_238174_());
      this.f_104888_.player.setPortalCooldown(pPacket.f_286971_());
      this.f_104888_.gameMode.setLocalMode(pPacket.f_132363_(), pPacket.f_132364_());
      this.f_104888_.options.setServerRenderDistance(pPacket.chunkRadius());
      net.minecraftforge.network.NetworkHooks.sendMCRegistryPackets(f_104885_, "PLAY_TO_SERVER");
      this.f_104888_.options.broadcastOptions();
      this.f_104885_.send(new ServerboundCustomPayloadPacket(ServerboundCustomPayloadPacket.f_133979_, (new FriendlyByteBuf(Unpooled.buffer())).writeUtf(ClientBrandRetriever.getClientModName())));
      this.chatSession = null;
      this.lastSeenMessages = new LastSeenMessagesTracker(20);
      this.messageSignatureCache = MessageSignatureCache.createDefault();
      if (this.f_104885_.isEncrypted()) {
         this.f_104888_.getProfileKeyPairManager().prepareKeyPair().thenAcceptAsync((p_253341_) -> {
            p_253341_.ifPresent(this::setKeyPair);
         }, this.f_104888_);
      }

      this.f_194191_.onPlayerInfoReceived(pPacket.f_132363_(), pPacket.hardcore());
      this.f_104888_.quickPlayLog().log(this.f_104888_);
   }

   /**
    * Spawns an instance of the objecttype indicated by the packet and sets its position and momentum
    */
   public void handleAddEntity(ClientboundAddEntityPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      EntityType<?> entitytype = pPacket.getType();
      Entity entity = entitytype.create(this.level);
      if (entity != null) {
         entity.recreateFromPacket(pPacket);
         int i = pPacket.getId();
         this.level.m_104627_(i, entity);
         this.postAddEntitySoundInstance(entity);
      } else {
         LOGGER.warn("Skipping Entity with id {}", (Object)entitytype);
      }

   }

   private void postAddEntitySoundInstance(Entity pEntity) {
      if (pEntity instanceof AbstractMinecart) {
         this.f_104888_.getSoundManager().play(new MinecartSoundInstance((AbstractMinecart)pEntity));
      } else if (pEntity instanceof Bee) {
         boolean flag = ((Bee)pEntity).isAngry();
         BeeSoundInstance beesoundinstance;
         if (flag) {
            beesoundinstance = new BeeAggressiveSoundInstance((Bee)pEntity);
         } else {
            beesoundinstance = new BeeFlyingSoundInstance((Bee)pEntity);
         }

         this.f_104888_.getSoundManager().queueTickingSound(beesoundinstance);
      }

   }

   /**
    * Spawns an experience orb and sets its value (amount of XP)
    */
   public void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      double d0 = pPacket.getX();
      double d1 = pPacket.getY();
      double d2 = pPacket.getZ();
      Entity entity = new ExperienceOrb(this.level, d0, d1, d2, pPacket.getValue());
      entity.syncPacketPositionCodec(d0, d1, d2);
      entity.setYRot(0.0F);
      entity.setXRot(0.0F);
      entity.setId(pPacket.getId());
      this.level.m_104627_(pPacket.getId(), entity);
   }

   /**
    * Sets the velocity of the specified entity to the specified value
    */
   public void handleSetEntityMotion(ClientboundSetEntityMotionPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = this.level.getEntity(pPacket.getId());
      if (entity != null) {
         entity.lerpMotion((double)pPacket.getXa() / 8000.0D, (double)pPacket.getYa() / 8000.0D, (double)pPacket.getZa() / 8000.0D);
      }
   }

   /**
    * Invoked when the server registers new proximate objects in your watchlist or when objects in your watchlist have
    * changed -> Registers any changes locally
    */
   public void handleSetEntityData(ClientboundSetEntityDataPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = this.level.getEntity(pPacket.id());
      if (entity != null) {
         entity.getEntityData().assignValues(pPacket.packedItems());
      }

   }

   public void m_6482_(ClientboundAddPlayerPacket p_104966_) {
      PacketUtils.ensureRunningOnSameThread(p_104966_, this, this.f_104888_);
      PlayerInfo playerinfo = this.getPlayerInfo(p_104966_.m_131606_());
      if (playerinfo == null) {
         LOGGER.warn("Server attempted to add player prior to sending player info (Player id {})", (Object)p_104966_.m_131606_());
      } else {
         double d0 = p_104966_.m_131607_();
         double d1 = p_104966_.m_131608_();
         double d2 = p_104966_.m_131609_();
         float f = (float)(p_104966_.m_131610_() * 360) / 256.0F;
         float f1 = (float)(p_104966_.m_131611_() * 360) / 256.0F;
         int i = p_104966_.m_131603_();
         RemotePlayer remoteplayer = new RemotePlayer(this.f_104888_.level, playerinfo.getProfile());
         remoteplayer.setId(i);
         remoteplayer.syncPacketPositionCodec(d0, d1, d2);
         remoteplayer.absMoveTo(d0, d1, d2, f, f1);
         remoteplayer.setOldPosAndRot();
         this.level.m_104630_(i, remoteplayer);
      }
   }

   /**
    * Updates an entity's position and rotation as specified by the packet
    */
   public void handleTeleportEntity(ClientboundTeleportEntityPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = this.level.getEntity(pPacket.getId());
      if (entity != null) {
         double d0 = pPacket.getX();
         double d1 = pPacket.getY();
         double d2 = pPacket.getZ();
         entity.syncPacketPositionCodec(d0, d1, d2);
         if (!entity.isControlledByLocalInstance()) {
            float f = (float)(pPacket.getyRot() * 360) / 256.0F;
            float f1 = (float)(pPacket.getxRot() * 360) / 256.0F;
            entity.lerpTo(d0, d1, d2, f, f1, 3, true);
            entity.setOnGround(pPacket.isOnGround());
         }

      }
   }

   /**
    * Updates which hotbar slot of the player is currently selected
    */
   public void handleSetCarriedItem(ClientboundSetCarriedItemPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      if (Inventory.isHotbarSlot(pPacket.getSlot())) {
         this.f_104888_.player.getInventory().selected = pPacket.getSlot();
      }

   }

   /**
    * Updates the specified entity's position by the specified relative moment and absolute rotation. Note that
    * subclassing of the packet allows for the specification of a subset of this data (e.g. only rel. position, abs.
    * rotation or both).
    */
   public void handleMoveEntity(ClientboundMoveEntityPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = pPacket.getEntity(this.level);
      if (entity != null) {
         if (!entity.isControlledByLocalInstance()) {
            if (pPacket.hasPosition()) {
               VecDeltaCodec vecdeltacodec = entity.getPositionCodec();
               Vec3 vec3 = vecdeltacodec.decode((long)pPacket.getXa(), (long)pPacket.getYa(), (long)pPacket.getZa());
               vecdeltacodec.setBase(vec3);
               float f = pPacket.hasRotation() ? (float)(pPacket.getyRot() * 360) / 256.0F : entity.getYRot();
               float f1 = pPacket.hasRotation() ? (float)(pPacket.getxRot() * 360) / 256.0F : entity.getXRot();
               entity.lerpTo(vec3.x(), vec3.y(), vec3.z(), f, f1, 3, false);
            } else if (pPacket.hasRotation()) {
               float f2 = (float)(pPacket.getyRot() * 360) / 256.0F;
               float f3 = (float)(pPacket.getxRot() * 360) / 256.0F;
               entity.lerpTo(entity.getX(), entity.getY(), entity.getZ(), f2, f3, 3, false);
            }

            entity.setOnGround(pPacket.isOnGround());
         }

      }
   }

   /**
    * Updates the direction in which the specified entity is looking, normally this head rotation is independent of the
    * rotation of the entity itself
    */
   public void handleRotateMob(ClientboundRotateHeadPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = pPacket.getEntity(this.level);
      if (entity != null) {
         float f = (float)(pPacket.getYHeadRot() * 360) / 256.0F;
         entity.lerpHeadTo(f, 3);
      }
   }

   public void handleRemoveEntities(ClientboundRemoveEntitiesPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      pPacket.getEntityIds().forEach((int p_205521_) -> {
         this.level.removeEntity(p_205521_, Entity.RemovalReason.DISCARDED);
      });
   }

   public void handleMovePlayer(ClientboundPlayerPositionPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Player player = this.f_104888_.player;
      Vec3 vec3 = player.getDeltaMovement();
      boolean flag = pPacket.getRelativeArguments().contains(RelativeMovement.X);
      boolean flag1 = pPacket.getRelativeArguments().contains(RelativeMovement.Y);
      boolean flag2 = pPacket.getRelativeArguments().contains(RelativeMovement.Z);
      double d0;
      double d1;
      if (flag) {
         d0 = vec3.x();
         d1 = player.getX() + pPacket.getX();
         player.xOld += pPacket.getX();
         player.xo += pPacket.getX();
      } else {
         d0 = 0.0D;
         d1 = pPacket.getX();
         player.xOld = d1;
         player.xo = d1;
      }

      double d2;
      double d3;
      if (flag1) {
         d2 = vec3.y();
         d3 = player.getY() + pPacket.getY();
         player.yOld += pPacket.getY();
         player.yo += pPacket.getY();
      } else {
         d2 = 0.0D;
         d3 = pPacket.getY();
         player.yOld = d3;
         player.yo = d3;
      }

      double d4;
      double d5;
      if (flag2) {
         d4 = vec3.z();
         d5 = player.getZ() + pPacket.getZ();
         player.zOld += pPacket.getZ();
         player.zo += pPacket.getZ();
      } else {
         d4 = 0.0D;
         d5 = pPacket.getZ();
         player.zOld = d5;
         player.zo = d5;
      }

      player.setPos(d1, d3, d5);
      player.setDeltaMovement(d0, d2, d4);
      float f = pPacket.getYRot();
      float f1 = pPacket.getXRot();
      if (pPacket.getRelativeArguments().contains(RelativeMovement.X_ROT)) {
         player.setXRot(player.getXRot() + f1);
         player.xRotO += f1;
      } else {
         player.setXRot(f1);
         player.xRotO = f1;
      }

      if (pPacket.getRelativeArguments().contains(RelativeMovement.Y_ROT)) {
         player.setYRot(player.getYRot() + f);
         player.yRotO += f;
      } else {
         player.setYRot(f);
         player.yRotO = f;
      }

      this.f_104885_.send(new ServerboundAcceptTeleportationPacket(pPacket.getId()));
      this.f_104885_.send(new ServerboundMovePlayerPacket.PosRot(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), false));
   }

   /**
    * Received from the servers PlayerManager if between 1 and 64 blocks in a chunk are changed. If only one block
    * requires an update, the server sends S23PacketBlockChange and if 64 or more blocks are changed, the server sends
    * S21PacketChunkData
    */
   public void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      pPacket.runUpdates((p_284633_, p_284634_) -> {
         this.level.setServerVerifiedBlockState(p_284633_, p_284634_, 19);
      });
   }

   public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      int i = pPacket.getX();
      int j = pPacket.getZ();
      this.updateLevelChunk(i, j, pPacket.getChunkData());
      ClientboundLightUpdatePacketData clientboundlightupdatepacketdata = pPacket.getLightData();
      this.level.queueLightUpdate(() -> {
         this.applyLightData(i, j, clientboundlightupdatepacketdata);
         LevelChunk levelchunk = this.level.getChunkSource().getChunk(i, j, false);
         if (levelchunk != null) {
            this.enableChunkLight(levelchunk, i, j);
         }

      });
   }

   public void handleChunksBiomes(ClientboundChunksBiomesPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);

      for(ClientboundChunksBiomesPacket.ChunkBiomeData clientboundchunksbiomespacket$chunkbiomedata : pPacket.chunkBiomeData()) {
         this.level.getChunkSource().replaceBiomes(clientboundchunksbiomespacket$chunkbiomedata.pos().x, clientboundchunksbiomespacket$chunkbiomedata.pos().z, clientboundchunksbiomespacket$chunkbiomedata.getReadBuffer());
      }

      for(ClientboundChunksBiomesPacket.ChunkBiomeData clientboundchunksbiomespacket$chunkbiomedata1 : pPacket.chunkBiomeData()) {
         this.level.onChunkLoaded(new ChunkPos(clientboundchunksbiomespacket$chunkbiomedata1.pos().x, clientboundchunksbiomespacket$chunkbiomedata1.pos().z));
      }

      for(ClientboundChunksBiomesPacket.ChunkBiomeData clientboundchunksbiomespacket$chunkbiomedata2 : pPacket.chunkBiomeData()) {
         for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
               for(int k = this.level.getMinSection(); k < this.level.getMaxSection(); ++k) {
                  this.f_104888_.levelRenderer.setSectionDirty(clientboundchunksbiomespacket$chunkbiomedata2.pos().x + i, k, clientboundchunksbiomespacket$chunkbiomedata2.pos().z + j);
               }
            }
         }
      }

   }

   private void updateLevelChunk(int pX, int pZ, ClientboundLevelChunkPacketData pData) {
      this.level.getChunkSource().replaceWithPacketData(pX, pZ, pData.getReadBuffer(), pData.getHeightmaps(), pData.getBlockEntitiesTagsConsumer(pX, pZ));
   }

   private void enableChunkLight(LevelChunk pChunk, int pX, int pZ) {
      LevelLightEngine levellightengine = this.level.getChunkSource().getLightEngine();
      LevelChunkSection[] alevelchunksection = pChunk.getSections();
      ChunkPos chunkpos = pChunk.getPos();

      for(int i = 0; i < alevelchunksection.length; ++i) {
         LevelChunkSection levelchunksection = alevelchunksection[i];
         int j = this.level.getSectionYFromSectionIndex(i);
         levellightengine.updateSectionStatus(SectionPos.of(chunkpos, j), levelchunksection.hasOnlyAir());
         this.level.setSectionDirtyWithNeighbors(pX, j, pZ);
      }

   }

   public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      int i = pPacket.m_132149_();
      int j = pPacket.m_132152_();
      ClientChunkCache clientchunkcache = this.level.getChunkSource();
      clientchunkcache.drop(i, j);
      this.queueLightRemoval(pPacket);
   }

   private void queueLightRemoval(ClientboundForgetLevelChunkPacket pPacket) {
      ChunkPos chunkpos = new ChunkPos(pPacket.m_132149_(), pPacket.m_132152_());
      this.level.queueLightUpdate(() -> {
         LevelLightEngine levellightengine = this.level.getLightEngine();
         levellightengine.setLightEnabled(chunkpos, false);

         for(int i = levellightengine.getMinLightSection(); i < levellightengine.getMaxLightSection(); ++i) {
            SectionPos sectionpos = SectionPos.of(chunkpos, i);
            levellightengine.queueSectionData(LightLayer.BLOCK, sectionpos, (DataLayer)null);
            levellightengine.queueSectionData(LightLayer.SKY, sectionpos, (DataLayer)null);
         }

         for(int j = this.level.getMinSection(); j < this.level.getMaxSection(); ++j) {
            levellightengine.updateSectionStatus(SectionPos.of(chunkpos, j), true);
         }

      });
   }

   /**
    * Updates the block and metadata and generates a blockupdate (and notify the clients)
    */
   public void handleBlockUpdate(ClientboundBlockUpdatePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.level.setServerVerifiedBlockState(pPacket.getPos(), pPacket.getBlockState(), 19);
   }

   public void m_6008_(ClientboundDisconnectPacket p_105008_) {
      this.f_104885_.disconnect(p_105008_.m_132085_());
   }

   /**
    * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
    */
   public void onDisconnect(Component p_104954_) {
      this.f_104888_.m_91399_();
      this.f_194191_.onDisconnect();
      if (this.f_104887_ != null) {
         if (this.f_104887_ instanceof RealmsScreen) {
            this.f_104888_.setScreen(new DisconnectedRealmsScreen(this.f_104887_, f_104884_, p_104954_));
         } else {
            this.f_104888_.setScreen(new DisconnectedScreen(this.f_104887_, f_104884_, p_104954_));
         }
      } else {
         this.f_104888_.setScreen(new DisconnectedScreen(new JoinMultiplayerScreen(new TitleScreen()), f_104884_, p_104954_));
      }

   }

   public void m_104955_(Packet<?> p_104956_) {
      this.f_104885_.send(p_104956_);
   }

   public void handleTakeItemEntity(ClientboundTakeItemEntityPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = this.level.getEntity(pPacket.getItemId());
      LivingEntity livingentity = (LivingEntity)this.level.getEntity(pPacket.getPlayerId());
      if (livingentity == null) {
         livingentity = this.f_104888_.player;
      }

      if (entity != null) {
         if (entity instanceof ExperienceOrb) {
            this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1F, (this.random.nextFloat() - this.random.nextFloat()) * 0.35F + 0.9F, false);
         } else {
            this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, (this.random.nextFloat() - this.random.nextFloat()) * 1.4F + 2.0F, false);
         }

         this.f_104888_.particleEngine.add(new ItemPickupParticle(this.f_104888_.getEntityRenderDispatcher(), this.f_104888_.renderBuffers(), this.level, entity, livingentity));
         if (entity instanceof ItemEntity) {
            ItemEntity itementity = (ItemEntity)entity;
            ItemStack itemstack = itementity.getItem();
            if (!itemstack.isEmpty()) {
               itemstack.shrink(pPacket.getAmount());
            }

            if (itemstack.isEmpty()) {
               this.level.removeEntity(pPacket.getItemId(), Entity.RemovalReason.DISCARDED);
            }
         } else if (!(entity instanceof ExperienceOrb)) {
            this.level.removeEntity(pPacket.getItemId(), Entity.RemovalReason.DISCARDED);
         }
      }

   }

   public void handleSystemChat(ClientboundSystemChatPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.getChatListener().handleSystemMessage(pPacket.content(), pPacket.overlay());
   }

   public void handlePlayerChat(ClientboundPlayerChatPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Optional<SignedMessageBody> optional = pPacket.body().unpack(this.messageSignatureCache);
      Optional<ChatType.Bound> optional1 = pPacket.chatType().resolve(this.registryAccess.compositeAccess());
      if (!optional.isEmpty() && !optional1.isEmpty()) {
         UUID uuid = pPacket.sender();
         PlayerInfo playerinfo = this.getPlayerInfo(uuid);
         if (playerinfo == null) {
            this.f_104885_.disconnect(CHAT_VALIDATION_FAILED_ERROR);
         } else {
            RemoteChatSession remotechatsession = playerinfo.getChatSession();
            SignedMessageLink signedmessagelink;
            if (remotechatsession != null) {
               signedmessagelink = new SignedMessageLink(pPacket.index(), uuid, remotechatsession.sessionId());
            } else {
               signedmessagelink = SignedMessageLink.unsigned(uuid);
            }

            PlayerChatMessage playerchatmessage = new PlayerChatMessage(signedmessagelink, pPacket.signature(), optional.get(), pPacket.unsignedContent(), pPacket.filterMask());
            if (!playerinfo.getMessageValidator().updateAndValidate(playerchatmessage)) {
               this.f_104885_.disconnect(CHAT_VALIDATION_FAILED_ERROR);
            } else {
               this.f_104888_.getChatListener().handlePlayerChatMessage(playerchatmessage, playerinfo.getProfile(), optional1.get());
               this.messageSignatureCache.push(playerchatmessage);
            }
         }
      } else {
         this.f_104885_.disconnect(INVALID_PACKET);
      }
   }

   public void handleDisguisedChat(ClientboundDisguisedChatPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Optional<ChatType.Bound> optional = pPacket.chatType().resolve(this.registryAccess.compositeAccess());
      if (optional.isEmpty()) {
         this.f_104885_.disconnect(INVALID_PACKET);
      } else {
         this.f_104888_.getChatListener().handleDisguisedChatMessage(pPacket.message(), optional.get());
      }
   }

   public void handleDeleteChat(ClientboundDeleteChatPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Optional<MessageSignature> optional = pPacket.messageSignature().unpack(this.messageSignatureCache);
      if (optional.isEmpty()) {
         this.f_104885_.disconnect(INVALID_PACKET);
      } else {
         this.lastSeenMessages.ignorePending(optional.get());
         if (!this.f_104888_.getChatListener().removeFromDelayedMessageQueue(optional.get())) {
            this.f_104888_.gui.getChat().deleteMessage(optional.get());
         }

      }
   }

   /**
    * Renders a specified animation: Waking up a player, a living entity swinging its currently held item, being hurt or
    * receiving a critical hit by normal or magical means
    */
   public void handleAnimate(ClientboundAnimatePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = this.level.getEntity(pPacket.getId());
      if (entity != null) {
         if (pPacket.getAction() == 0) {
            LivingEntity livingentity = (LivingEntity)entity;
            livingentity.swing(InteractionHand.MAIN_HAND);
         } else if (pPacket.getAction() == 3) {
            LivingEntity livingentity1 = (LivingEntity)entity;
            livingentity1.swing(InteractionHand.OFF_HAND);
         } else if (pPacket.getAction() == 2) {
            Player player = (Player)entity;
            player.stopSleepInBed(false, false);
         } else if (pPacket.getAction() == 4) {
            this.f_104888_.particleEngine.createTrackingEmitter(entity, ParticleTypes.CRIT);
         } else if (pPacket.getAction() == 5) {
            this.f_104888_.particleEngine.createTrackingEmitter(entity, ParticleTypes.ENCHANTED_HIT);
         }

      }
   }

   public void handleHurtAnimation(ClientboundHurtAnimationPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = this.level.getEntity(pPacket.id());
      if (entity != null) {
         entity.animateHurt(pPacket.yaw());
      }
   }

   public void handleSetTime(ClientboundSetTimePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.level.setGameTime(pPacket.getGameTime());
      this.f_104888_.level.setDayTime(pPacket.getDayTime());
      this.f_194191_.setTime(pPacket.getGameTime());
   }

   public void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.level.setDefaultSpawnPos(pPacket.getPos(), pPacket.getAngle());
      Screen screen = this.f_104888_.screen;
      if (screen instanceof ReceivingLevelScreen receivinglevelscreen) {
         receivinglevelscreen.loadingPacketsReceived();
      }

   }

   public void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = this.level.getEntity(pPacket.getVehicle());
      if (entity == null) {
         LOGGER.warn("Received passengers for unknown entity");
      } else {
         boolean flag = entity.hasIndirectPassenger(this.f_104888_.player);
         entity.ejectPassengers();

         for(int i : pPacket.getPassengers()) {
            Entity entity1 = this.level.getEntity(i);
            if (entity1 != null) {
               entity1.startRiding(entity, true);
               if (entity1 == this.f_104888_.player && !flag) {
                  if (entity instanceof Boat) {
                     this.f_104888_.player.yRotO = entity.getYRot();
                     this.f_104888_.player.setYRot(entity.getYRot());
                     this.f_104888_.player.setYHeadRot(entity.getYRot());
                  }

                  Component component = Component.translatable("mount.onboard", this.f_104888_.options.keyShift.getTranslatedKeyMessage());
                  this.f_104888_.gui.setOverlayMessage(component, false);
                  this.f_104888_.getNarrator().sayNow(component);
               }
            }
         }

      }
   }

   public void handleEntityLinkPacket(ClientboundSetEntityLinkPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = this.level.getEntity(pPacket.getSourceId());
      if (entity instanceof Mob) {
         ((Mob)entity).setDelayedLeashHolderId(pPacket.getDestId());
      }

   }

   private static ItemStack findTotem(Player pPlayer) {
      for(InteractionHand interactionhand : InteractionHand.values()) {
         ItemStack itemstack = pPlayer.getItemInHand(interactionhand);
         if (itemstack.is(Items.TOTEM_OF_UNDYING)) {
            return itemstack;
         }
      }

      return new ItemStack(Items.TOTEM_OF_UNDYING);
   }

   /**
    * Invokes the entities' handleUpdateHealth method which is implemented in LivingBase (hurt/death),
    * MinecartMobSpawner (spawn delay), FireworkRocket & MinecartTNT (explosion), IronGolem (throwing,...), Witch (spawn
    * particles), Zombie (villager transformation), Animal (breeding mode particles), Horse (breeding/smoke particles),
    * Sheep (...), Tameable (...), Villager (particles for breeding mode, angry and happy), Wolf (...)
    */
   public void handleEntityEvent(ClientboundEntityEventPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = pPacket.getEntity(this.level);
      if (entity != null) {
         switch (pPacket.getEventId()) {
            case 21:
               this.f_104888_.getSoundManager().play(new GuardianAttackSoundInstance((Guardian)entity));
               break;
            case 35:
               int i = 40;
               this.f_104888_.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
               this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);
               if (entity == this.f_104888_.player) {
                  this.f_104888_.gameRenderer.displayItemActivation(findTotem(this.f_104888_.player));
               }
               break;
            case 63:
               this.f_104888_.getSoundManager().play(new SnifferSoundInstance((Sniffer)entity));
               break;
            default:
               entity.handleEntityEvent(pPacket.getEventId());
         }
      }

   }

   public void handleDamageEvent(ClientboundDamageEventPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = this.level.getEntity(pPacket.entityId());
      if (entity != null) {
         entity.handleDamageEvent(pPacket.getSource(this.level));
      }
   }

   public void handleSetHealth(ClientboundSetHealthPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.player.hurtTo(pPacket.getHealth());
      this.f_104888_.player.getFoodData().setFoodLevel(pPacket.getFood());
      this.f_104888_.player.getFoodData().setSaturation(pPacket.getSaturation());
   }

   public void handleSetExperience(ClientboundSetExperiencePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.player.setExperienceValues(pPacket.getExperienceProgress(), pPacket.getTotalExperience(), pPacket.getExperienceLevel());
   }

   public void handleRespawn(ClientboundRespawnPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      ResourceKey<Level> resourcekey = pPacket.m_132955_();
      Holder<DimensionType> holder = this.registryAccess.compositeAccess().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(pPacket.m_237794_());
      LocalPlayer localplayer = this.f_104888_.player;
      int i = localplayer.getId();
      if (resourcekey != localplayer.level().dimension()) {
         Scoreboard scoreboard = this.level.getScoreboard();
         Map<String, MapItemSavedData> map = this.level.getAllMapData();
         boolean flag = pPacket.m_132959_();
         boolean flag1 = pPacket.m_132960_();
         ClientLevel.ClientLevelData clientlevel$clientleveldata = new ClientLevel.ClientLevelData(this.levelData.getDifficulty(), this.levelData.isHardcore(), flag1);
         this.levelData = clientlevel$clientleveldata;
         this.level = new ClientLevel(this, clientlevel$clientleveldata, resourcekey, holder, this.serverChunkRadius, this.serverSimulationDistance, this.f_104888_::getProfiler, this.f_104888_.levelRenderer, flag, pPacket.m_132956_());
         this.level.setScoreboard(scoreboard);
         this.level.addMapData(map);
         this.f_104888_.setLevel(this.level);
         this.f_104888_.setScreen(new ReceivingLevelScreen());
      }

      String s = localplayer.m_108629_();
      this.f_104888_.cameraEntity = null;
      if (localplayer.hasContainerOpen()) {
         localplayer.closeContainer();
      }

      LocalPlayer localplayer1;
      if (pPacket.shouldKeep((byte)2)) {
         localplayer1 = this.f_104888_.gameMode.createPlayer(this.level, localplayer.getStats(), localplayer.getRecipeBook(), localplayer.isShiftKeyDown(), localplayer.isSprinting());
      } else {
         localplayer1 = this.f_104888_.gameMode.createPlayer(this.level, localplayer.getStats(), localplayer.getRecipeBook());
      }

      localplayer1.setId(i);
      this.f_104888_.player = localplayer1;
      if (resourcekey != localplayer.level().dimension()) {
         this.f_104888_.getMusicManager().stopPlaying();
      }

      this.f_104888_.cameraEntity = localplayer1;
      if (pPacket.shouldKeep((byte)2)) {
         List<SynchedEntityData.DataValue<?>> list = localplayer.getEntityData().getNonDefaultValues();
         if (list != null) {
            localplayer1.getEntityData().assignValues(list);
         }
      }

      if (pPacket.shouldKeep((byte)1)) {
         localplayer1.getAttributes().assignValues(localplayer.getAttributes());
      }

      localplayer1.updateSyncFields(localplayer); // Forge: fix MC-10657
      localplayer1.resetPos();
      localplayer1.m_108748_(s);
      net.minecraftforge.client.ForgeHooksClient.firePlayerRespawn(this.f_104888_.gameMode, localplayer, localplayer1, localplayer1.connection.f_104885_);
      this.level.m_104630_(i, localplayer1);
      localplayer1.setYRot(-180.0F);
      localplayer1.input = new KeyboardInput(this.f_104888_.options);
      this.f_104888_.gameMode.adjustPlayer(localplayer1);
      localplayer1.setReducedDebugInfo(localplayer.isReducedDebugInfo());
      localplayer1.setShowDeathScreen(localplayer.shouldShowDeathScreen());
      localplayer1.setLastDeathLocation(pPacket.m_237785_());
      localplayer1.setPortalCooldown(pPacket.m_287149_());
      localplayer1.spinningEffectIntensity = localplayer.spinningEffectIntensity;
      localplayer1.oSpinningEffectIntensity = localplayer.oSpinningEffectIntensity;
      if (this.f_104888_.screen instanceof DeathScreen || this.f_104888_.screen instanceof DeathScreen.TitleConfirmScreen) {
         this.f_104888_.setScreen((Screen)null);
      }

      this.f_104888_.gameMode.setLocalMode(pPacket.m_132957_(), pPacket.m_132958_());
   }

   /**
    * Initiates a new explosion (sound, particles, drop spawn) for the affected blocks indicated by the packet.
    */
   public void handleExplosion(ClientboundExplodePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Explosion explosion = new Explosion(this.f_104888_.level, (Entity)null, pPacket.getX(), pPacket.getY(), pPacket.getZ(), pPacket.getPower(), pPacket.getToBlow());
      explosion.finalizeExplosion(true);
      this.f_104888_.player.setDeltaMovement(this.f_104888_.player.getDeltaMovement().add((double)pPacket.getKnockbackX(), (double)pPacket.getKnockbackY(), (double)pPacket.getKnockbackZ()));
   }

   public void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = this.level.getEntity(pPacket.getEntityId());
      if (entity instanceof AbstractHorse) {
         LocalPlayer localplayer = this.f_104888_.player;
         AbstractHorse abstracthorse = (AbstractHorse)entity;
         SimpleContainer simplecontainer = new SimpleContainer(pPacket.getSize());
         HorseInventoryMenu horseinventorymenu = new HorseInventoryMenu(pPacket.getContainerId(), localplayer.getInventory(), simplecontainer, abstracthorse);
         localplayer.containerMenu = horseinventorymenu;
         this.f_104888_.setScreen(new HorseInventoryScreen(horseinventorymenu, localplayer.getInventory(), abstracthorse));
      }

   }

   public void handleOpenScreen(ClientboundOpenScreenPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      MenuScreens.create(pPacket.getType(), this.f_104888_, pPacket.getContainerId(), pPacket.getTitle());
   }

   /**
    * Handles picking up an ItemStack or dropping one in your inventory or an open (non-creative) container
    */
   public void handleContainerSetSlot(ClientboundContainerSetSlotPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Player player = this.f_104888_.player;
      ItemStack itemstack = pPacket.getItem();
      int i = pPacket.getSlot();
      this.f_104888_.getTutorial().onGetItem(itemstack);
      if (pPacket.getContainerId() == -1) {
         if (!(this.f_104888_.screen instanceof CreativeModeInventoryScreen)) {
            player.containerMenu.setCarried(itemstack);
         }
      } else if (pPacket.getContainerId() == -2) {
         player.getInventory().setItem(i, itemstack);
      } else {
         boolean flag = false;
         Screen screen = this.f_104888_.screen;
         if (screen instanceof CreativeModeInventoryScreen) {
            CreativeModeInventoryScreen creativemodeinventoryscreen = (CreativeModeInventoryScreen)screen;
            flag = !creativemodeinventoryscreen.isInventoryOpen();
         }

         if (pPacket.getContainerId() == 0 && InventoryMenu.isHotbarSlot(i)) {
            if (!itemstack.isEmpty()) {
               ItemStack itemstack1 = player.inventoryMenu.getSlot(i).getItem();
               if (itemstack1.isEmpty() || itemstack1.getCount() < itemstack.getCount()) {
                  itemstack.setPopTime(5);
               }
            }

            player.inventoryMenu.setItem(i, pPacket.getStateId(), itemstack);
         } else if (pPacket.getContainerId() == player.containerMenu.containerId && (pPacket.getContainerId() != 0 || !flag)) {
            player.containerMenu.setItem(i, pPacket.getStateId(), itemstack);
         }
      }

   }

   /**
    * Handles the placement of a specified ItemStack in a specified container/inventory slot
    */
   public void handleContainerContent(ClientboundContainerSetContentPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Player player = this.f_104888_.player;
      if (pPacket.getContainerId() == 0) {
         player.inventoryMenu.initializeContents(pPacket.getStateId(), pPacket.getItems(), pPacket.getCarriedItem());
      } else if (pPacket.getContainerId() == player.containerMenu.containerId) {
         player.containerMenu.initializeContents(pPacket.getStateId(), pPacket.getItems(), pPacket.getCarriedItem());
      }

   }

   /**
    * Creates a sign in the specified location if it didn't exist and opens the GUI to edit its text
    */
   public void handleOpenSignEditor(ClientboundOpenSignEditorPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      BlockPos blockpos = pPacket.getPos();
      BlockEntity $$3 = this.level.getBlockEntity(blockpos);
      if ($$3 instanceof SignBlockEntity signblockentity) {
         this.f_104888_.player.openTextEdit(signblockentity, pPacket.isFrontText());
      } else {
         BlockState blockstate = this.level.getBlockState(blockpos);
         SignBlockEntity signblockentity1 = new SignBlockEntity(blockpos, blockstate);
         signblockentity1.setLevel(this.level);
         this.f_104888_.player.openTextEdit(signblockentity1, pPacket.isFrontText());
      }

   }

   /**
    * Updates the NBTTagCompound metadata of instances of the following entitytypes: Mob spawners, command blocks,
    * beacons, skulls, flowerpot
    */
   public void handleBlockEntityData(ClientboundBlockEntityDataPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      BlockPos blockpos = pPacket.getPos();
      this.f_104888_.level.getBlockEntity(blockpos, pPacket.getType()).ifPresent((p_205557_) -> {
         p_205557_.onDataPacket(f_104885_, pPacket);

         if (p_205557_ instanceof CommandBlockEntity && this.f_104888_.screen instanceof CommandBlockEditScreen) {
            ((CommandBlockEditScreen)this.f_104888_.screen).updateGui();
         }

      });
   }

   /**
    * Sets the progressbar of the opened window to the specified value
    */
   public void handleContainerSetData(ClientboundContainerSetDataPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Player player = this.f_104888_.player;
      if (player.containerMenu != null && player.containerMenu.containerId == pPacket.getContainerId()) {
         player.containerMenu.setData(pPacket.getId(), pPacket.getValue());
      }

   }

   public void handleSetEquipment(ClientboundSetEquipmentPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = this.level.getEntity(pPacket.getEntity());
      if (entity != null) {
         pPacket.getSlots().forEach((p_205528_) -> {
            entity.setItemSlot(p_205528_.getFirst(), p_205528_.getSecond());
         });
      }

   }

   /**
    * Resets the ItemStack held in hand and closes the window that is opened
    */
   public void handleContainerClose(ClientboundContainerClosePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.player.clientSideCloseContainer();
   }

   /**
    * Triggers Block.onBlockEventReceived, which is implemented in BlockPistonBase for extension/retraction, BlockNote
    * for setting the instrument (including audiovisual feedback) and in BlockContainer to set the number of players
    * accessing a (Ender)Chest
    */
   public void handleBlockEvent(ClientboundBlockEventPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.level.blockEvent(pPacket.getPos(), pPacket.getBlock(), pPacket.getB0(), pPacket.getB1());
   }

   /**
    * Updates all registered IWorldAccess instances with destroyBlockInWorldPartially
    */
   public void handleBlockDestruction(ClientboundBlockDestructionPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.level.destroyBlockProgress(pPacket.getId(), pPacket.getPos(), pPacket.getProgress());
   }

   public void handleGameEvent(ClientboundGameEventPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Player player = this.f_104888_.player;
      ClientboundGameEventPacket.Type clientboundgameeventpacket$type = pPacket.getEvent();
      float f = pPacket.getParam();
      int i = Mth.floor(f + 0.5F);
      if (clientboundgameeventpacket$type == ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE) {
         player.displayClientMessage(Component.translatable("block.minecraft.spawn.not_valid"), false);
      } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.START_RAINING) {
         this.level.getLevelData().setRaining(true);
         this.level.setRainLevel(0.0F);
      } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.STOP_RAINING) {
         this.level.getLevelData().setRaining(false);
         this.level.setRainLevel(1.0F);
      } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.CHANGE_GAME_MODE) {
         this.f_104888_.gameMode.setLocalMode(GameType.byId(i));
      } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.WIN_GAME) {
         if (i == 0) {
            this.f_104888_.player.connection.m_104955_(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
            this.f_104888_.setScreen(new ReceivingLevelScreen());
         } else if (i == 1) {
            this.f_104888_.setScreen(new WinScreen(true, () -> {
               this.f_104888_.player.connection.m_104955_(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
               this.f_104888_.setScreen((Screen)null);
            }));
         }
      } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.DEMO_EVENT) {
         Options options = this.f_104888_.options;
         if (f == 0.0F) {
            this.f_104888_.setScreen(new DemoIntroScreen());
         } else if (f == 101.0F) {
            this.f_104888_.gui.getChat().addMessage(Component.translatable("demo.help.movement", options.keyUp.getTranslatedKeyMessage(), options.keyLeft.getTranslatedKeyMessage(), options.keyDown.getTranslatedKeyMessage(), options.keyRight.getTranslatedKeyMessage()));
         } else if (f == 102.0F) {
            this.f_104888_.gui.getChat().addMessage(Component.translatable("demo.help.jump", options.keyJump.getTranslatedKeyMessage()));
         } else if (f == 103.0F) {
            this.f_104888_.gui.getChat().addMessage(Component.translatable("demo.help.inventory", options.keyInventory.getTranslatedKeyMessage()));
         } else if (f == 104.0F) {
            this.f_104888_.gui.getChat().addMessage(Component.translatable("demo.day.6", options.keyScreenshot.getTranslatedKeyMessage()));
         }
      } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.ARROW_HIT_PLAYER) {
         this.level.playSound(player, player.getX(), player.getEyeY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.18F, 0.45F);
      } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE) {
         this.level.setRainLevel(f);
      } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE) {
         this.level.setThunderLevel(f);
      } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.PUFFER_FISH_STING) {
         this.level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.PUFFER_FISH_STING, SoundSource.NEUTRAL, 1.0F, 1.0F);
      } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT) {
         this.level.addParticle(ParticleTypes.ELDER_GUARDIAN, player.getX(), player.getY(), player.getZ(), 0.0D, 0.0D, 0.0D);
         if (i == 1) {
            this.level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.0F, 1.0F);
         }
      } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.IMMEDIATE_RESPAWN) {
         this.f_104888_.player.setShowDeathScreen(f == 0.0F);
      }

   }

   /**
    * Updates the worlds MapStorage with the specified MapData for the specified map-identifier and invokes a
    * MapItemRenderer for it
    */
   public void handleMapItemData(ClientboundMapItemDataPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      MapRenderer maprenderer = this.f_104888_.gameRenderer.getMapRenderer();
      int i = pPacket.getMapId();
      String s = MapItem.makeKey(i);
      MapItemSavedData mapitemsaveddata = this.f_104888_.level.getMapData(s);
      if (mapitemsaveddata == null) {
         mapitemsaveddata = MapItemSavedData.createForClient(pPacket.getScale(), pPacket.isLocked(), this.f_104888_.level.dimension());
         this.f_104888_.level.overrideMapData(s, mapitemsaveddata);
      }

      pPacket.applyToMap(mapitemsaveddata);
      maprenderer.update(i, mapitemsaveddata);
   }

   public void handleLevelEvent(ClientboundLevelEventPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      if (pPacket.isGlobalEvent()) {
         this.f_104888_.level.globalLevelEvent(pPacket.getType(), pPacket.getPos(), pPacket.getData());
      } else {
         this.f_104888_.level.levelEvent(pPacket.getType(), pPacket.getPos(), pPacket.getData());
      }

   }

   public void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.advancements.update(pPacket);
   }

   public void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      ResourceLocation resourcelocation = pPacket.getTab();
      if (resourcelocation == null) {
         this.advancements.setSelectedTab((Advancement)null, false);
      } else {
         Advancement advancement = this.advancements.m_104396_().m_139337_(resourcelocation);
         this.advancements.setSelectedTab(advancement, false);
      }

   }

   public void handleCommands(ClientboundCommandsPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      var context = CommandBuildContext.simple(this.registryAccess.compositeAccess(), this.enabledFeatures);
      this.commands = new CommandDispatcher<>(pPacket.getRoot(context));
      this.commands = net.minecraftforge.client.ClientCommandHandler.mergeServerCommands(this.commands, context);
   }

   public void handleStopSoundEvent(ClientboundStopSoundPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.getSoundManager().stop(pPacket.getName(), pPacket.getSource());
   }

   /**
    * This method is only called for manual tab-completion (the {@link
    * net.minecraft.commands.synchronization.SuggestionProviders#ASK_SERVER minecraft:ask_server} suggestion provider).
    */
   public void handleCommandSuggestions(ClientboundCommandSuggestionsPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.suggestionsProvider.completeCustomSuggestions(pPacket.getId(), pPacket.getSuggestions());
   }

   public void handleUpdateRecipes(ClientboundUpdateRecipesPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.recipeManager.replaceRecipes(pPacket.getRecipes());
      ClientRecipeBook clientrecipebook = this.f_104888_.player.getRecipeBook();
      clientrecipebook.setupCollections(this.recipeManager.getRecipes(), this.f_104888_.level.registryAccess());
      this.f_104888_.populateSearchTree(SearchRegistry.f_119943_, clientrecipebook.getCollections());
      net.minecraftforge.client.ForgeHooksClient.onRecipesUpdated(this.recipeManager);
   }

   public void handleLookAt(ClientboundPlayerLookAtPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Vec3 vec3 = pPacket.getPosition(this.level);
      if (vec3 != null) {
         this.f_104888_.player.lookAt(pPacket.getFromAnchor(), vec3);
      }

   }

   public void handleTagQueryPacket(ClientboundTagQueryPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      if (!this.debugQueryHandler.handleResponse(pPacket.getTransactionId(), pPacket.getTag())) {
         LOGGER.debug("Got unhandled response to tag query {}", (int)pPacket.getTransactionId());
      }

   }

   /**
    * Updates the players statistics or achievements
    */
   public void handleAwardStats(ClientboundAwardStatsPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);

      for(Map.Entry<Stat<?>, Integer> entry : pPacket.getStats().entrySet()) {
         Stat<?> stat = entry.getKey();
         int i = entry.getValue();
         this.f_104888_.player.getStats().setValue(this.f_104888_.player, stat, i);
      }

      if (this.f_104888_.screen instanceof StatsUpdateListener) {
         ((StatsUpdateListener)this.f_104888_.screen).onStatsUpdated();
      }

   }

   public void handleAddOrRemoveRecipes(ClientboundRecipePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      ClientRecipeBook clientrecipebook = this.f_104888_.player.getRecipeBook();
      clientrecipebook.setBookSettings(pPacket.getBookSettings());
      ClientboundRecipePacket.State clientboundrecipepacket$state = pPacket.getState();
      switch (clientboundrecipepacket$state) {
         case REMOVE:
            for(ResourceLocation resourcelocation3 : pPacket.getRecipes()) {
               this.recipeManager.byKey(resourcelocation3).ifPresent(clientrecipebook::remove);
            }
            break;
         case INIT:
            for(ResourceLocation resourcelocation1 : pPacket.getRecipes()) {
               this.recipeManager.byKey(resourcelocation1).ifPresent(clientrecipebook::add);
            }

            for(ResourceLocation resourcelocation2 : pPacket.getHighlights()) {
               this.recipeManager.byKey(resourcelocation2).ifPresent(clientrecipebook::addHighlight);
            }
            break;
         case ADD:
            for(ResourceLocation resourcelocation : pPacket.getRecipes()) {
               this.recipeManager.byKey(resourcelocation).ifPresent((p_272314_) -> {
                  clientrecipebook.add(p_272314_);
                  clientrecipebook.addHighlight(p_272314_);
                  if (p_272314_.showNotification()) {
                     RecipeToast.addOrUpdate(this.f_104888_.getToasts(), p_272314_);
                  }

               });
            }
      }

      clientrecipebook.getCollections().forEach((p_205540_) -> {
         p_205540_.updateKnownRecipes(clientrecipebook);
      });
      if (this.f_104888_.screen instanceof RecipeUpdateListener) {
         ((RecipeUpdateListener)this.f_104888_.screen).recipesUpdated();
      }

   }

   public void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = this.level.getEntity(pPacket.getEntityId());
      if (entity instanceof LivingEntity) {
         MobEffect mobeffect = pPacket.getEffect();
         if (mobeffect != null) {
            MobEffectInstance mobeffectinstance = new MobEffectInstance(mobeffect, pPacket.getEffectDurationTicks(), pPacket.getEffectAmplifier(), pPacket.isEffectAmbient(), pPacket.isEffectVisible(), pPacket.effectShowsIcon(), (MobEffectInstance)null, Optional.ofNullable(pPacket.getFactorData()));
            ((LivingEntity)entity).forceAddEffect(mobeffectinstance, (Entity)null);
         }
      }
   }

   public void m_5859_(ClientboundUpdateTagsPacket p_105134_) {
      PacketUtils.ensureRunningOnSameThread(p_105134_, this, this.f_104888_);
      p_105134_.m_179482_().forEach(this::m_205560_);
      if (!this.f_104885_.isMemoryConnection()) {
         Blocks.rebuildCache();
      }

      CreativeModeTabs.allTabs().stream().filter(net.minecraft.world.item.CreativeModeTab::hasSearchBar).forEach(net.minecraft.world.item.CreativeModeTab::rebuildSearchTree);
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.TagsUpdatedEvent(this.registryAccess.compositeAccess(), true, f_104885_.isMemoryConnection()));
   }

   public void m_241155_(ClientboundUpdateEnabledFeaturesPacket p_251591_) {
      PacketUtils.ensureRunningOnSameThread(p_251591_, this, this.f_104888_);
      this.enabledFeatures = FeatureFlags.REGISTRY.fromNames(p_251591_.f_244610_());
   }

   private <T> void m_205560_(ResourceKey<? extends Registry<? extends T>> p_205561_, TagNetworkSerialization.NetworkPayload p_205562_) {
      if (!p_205562_.isEmpty()) {
         Registry<T> registry = this.registryAccess.compositeAccess().registry(p_205561_).orElseThrow(() -> {
            return new IllegalStateException("Unknown registry " + p_205561_);
         });
         Map<TagKey<T>, List<Holder<T>>> map = new HashMap<>();
         TagNetworkSerialization.deserializeTagsFromNetwork((ResourceKey<? extends Registry<T>>)p_205561_, registry, p_205562_, map::put);
         registry.bindTags(map);
      }
   }

   public void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket pPacket) {
   }

   public void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket pPacket) {
   }

   public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = this.level.getEntity(pPacket.getPlayerId());
      if (entity == this.f_104888_.player) {
         if (this.f_104888_.player.shouldShowDeathScreen()) {
            this.f_104888_.setScreen(new DeathScreen(pPacket.getMessage(), this.level.getLevelData().isHardcore()));
         } else {
            this.f_104888_.player.respawn();
         }
      }

   }

   public void handleChangeDifficulty(ClientboundChangeDifficultyPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.levelData.setDifficulty(pPacket.getDifficulty());
      this.levelData.setDifficultyLocked(pPacket.isLocked());
   }

   public void handleSetCamera(ClientboundSetCameraPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = pPacket.getEntity(this.level);
      if (entity != null) {
         this.f_104888_.setCameraEntity(entity);
      }

   }

   public void handleInitializeBorder(ClientboundInitializeBorderPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      WorldBorder worldborder = this.level.getWorldBorder();
      worldborder.setCenter(pPacket.getNewCenterX(), pPacket.getNewCenterZ());
      long i = pPacket.getLerpTime();
      if (i > 0L) {
         worldborder.lerpSizeBetween(pPacket.getOldSize(), pPacket.getNewSize(), i);
      } else {
         worldborder.setSize(pPacket.getNewSize());
      }

      worldborder.setAbsoluteMaxSize(pPacket.getNewAbsoluteMaxSize());
      worldborder.setWarningBlocks(pPacket.getWarningBlocks());
      worldborder.setWarningTime(pPacket.getWarningTime());
   }

   public void handleSetBorderCenter(ClientboundSetBorderCenterPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.level.getWorldBorder().setCenter(pPacket.getNewCenterX(), pPacket.getNewCenterZ());
   }

   public void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.level.getWorldBorder().lerpSizeBetween(pPacket.getOldSize(), pPacket.getNewSize(), pPacket.getLerpTime());
   }

   public void handleSetBorderSize(ClientboundSetBorderSizePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.level.getWorldBorder().setSize(pPacket.getSize());
   }

   public void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.level.getWorldBorder().setWarningBlocks(pPacket.getWarningBlocks());
   }

   public void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.level.getWorldBorder().setWarningTime(pPacket.getWarningDelay());
   }

   public void handleTitlesClear(ClientboundClearTitlesPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.gui.clear();
      if (pPacket.shouldResetTimes()) {
         this.f_104888_.gui.resetTitleTimes();
      }

   }

   public void handleServerData(ClientboundServerDataPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      if (this.f_244115_ != null) {
         this.f_244115_.motd = pPacket.getMotd();
         pPacket.getIconBytes().ifPresent(this.f_244115_::setIconBytes);
         this.f_244115_.setEnforcesSecureChat(pPacket.enforcesSecureChat());
         ServerList.saveSingleServer(this.f_244115_);
         if (!pPacket.enforcesSecureChat()) {
            SystemToast systemtoast = SystemToast.multiline(this.f_104888_, SystemToast.SystemToastIds.UNSECURE_SERVER_WARNING, UNSECURE_SERVER_TOAST_TITLE, UNSERURE_SERVER_TOAST);
            this.f_104888_.getToasts().addToast(systemtoast);
         }

      }
   }

   public void handleCustomChatCompletions(ClientboundCustomChatCompletionsPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.suggestionsProvider.modifyCustomCompletions(pPacket.action(), pPacket.entries());
   }

   public void setActionBarText(ClientboundSetActionBarTextPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.gui.setOverlayMessage(pPacket.getText(), false);
   }

   public void setTitleText(ClientboundSetTitleTextPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.gui.setTitle(pPacket.getText());
   }

   public void setSubtitleText(ClientboundSetSubtitleTextPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.gui.setSubtitle(pPacket.getText());
   }

   public void setTitlesAnimation(ClientboundSetTitlesAnimationPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.gui.setTimes(pPacket.getFadeIn(), pPacket.getStay(), pPacket.getFadeOut());
   }

   public void handleTabListCustomisation(ClientboundTabListPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.gui.getTabList().setHeader(pPacket.getHeader().getString().isEmpty() ? null : pPacket.getHeader());
      this.f_104888_.gui.getTabList().setFooter(pPacket.getFooter().getString().isEmpty() ? null : pPacket.getFooter());
   }

   public void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = pPacket.getEntity(this.level);
      if (entity instanceof LivingEntity) {
         ((LivingEntity)entity).removeEffectNoUpdate(pPacket.getEffect());
      }

   }

   public void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);

      for(UUID uuid : pPacket.profileIds()) {
         this.f_104888_.getPlayerSocialManager().removePlayer(uuid);
         PlayerInfo playerinfo = this.playerInfoMap.remove(uuid);
         if (playerinfo != null) {
            this.listedPlayers.remove(playerinfo);
         }
      }

   }

   public void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);

      for(ClientboundPlayerInfoUpdatePacket.Entry clientboundplayerinfoupdatepacket$entry : pPacket.newEntries()) {
         PlayerInfo playerinfo = new PlayerInfo(clientboundplayerinfoupdatepacket$entry.profile(), this.enforcesSecureChat());
         if (this.playerInfoMap.putIfAbsent(clientboundplayerinfoupdatepacket$entry.profileId(), playerinfo) == null) {
            this.f_104888_.getPlayerSocialManager().addPlayer(playerinfo);
         }
      }

      for(ClientboundPlayerInfoUpdatePacket.Entry clientboundplayerinfoupdatepacket$entry1 : pPacket.entries()) {
         PlayerInfo playerinfo1 = this.playerInfoMap.get(clientboundplayerinfoupdatepacket$entry1.profileId());
         if (playerinfo1 == null) {
            LOGGER.warn("Ignoring player info update for unknown player {}", (Object)clientboundplayerinfoupdatepacket$entry1.profileId());
         } else {
            for(ClientboundPlayerInfoUpdatePacket.Action clientboundplayerinfoupdatepacket$action : pPacket.actions()) {
               this.applyPlayerInfoUpdate(clientboundplayerinfoupdatepacket$action, clientboundplayerinfoupdatepacket$entry1, playerinfo1);
            }
         }
      }

   }

   private void applyPlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket.Action pAction, ClientboundPlayerInfoUpdatePacket.Entry pEntry, PlayerInfo pPlayerInfo) {
      switch (pAction) {
         case INITIALIZE_CHAT:
            this.initializeChatSession(pEntry, pPlayerInfo);
            break;
         case UPDATE_GAME_MODE:
            if (pPlayerInfo.getGameMode() != pEntry.gameMode() && this.f_104888_.player != null && this.f_104888_.player.getUUID().equals(pEntry.profileId())) {
               this.f_104888_.player.onGameModeChanged(pEntry.gameMode());
            }

            pPlayerInfo.setGameMode(pEntry.gameMode());
            break;
         case UPDATE_LISTED:
            if (pEntry.listed()) {
               this.listedPlayers.add(pPlayerInfo);
            } else {
               this.listedPlayers.remove(pPlayerInfo);
            }
            break;
         case UPDATE_LATENCY:
            pPlayerInfo.setLatency(pEntry.latency());
            break;
         case UPDATE_DISPLAY_NAME:
            pPlayerInfo.setTabListDisplayName(pEntry.displayName());
      }

   }

   private void initializeChatSession(ClientboundPlayerInfoUpdatePacket.Entry pEntry, PlayerInfo pPlayerInfo) {
      GameProfile gameprofile = pPlayerInfo.getProfile();
      SignatureValidator signaturevalidator = this.f_104888_.getProfileKeySignatureValidator();
      if (signaturevalidator == null) {
         LOGGER.warn("Ignoring chat session from {} due to missing Services public key", (Object)gameprofile.getName());
         pPlayerInfo.clearChatSession(this.enforcesSecureChat());
      } else {
         RemoteChatSession.Data remotechatsession$data = pEntry.chatSession();
         if (remotechatsession$data != null) {
            try {
               RemoteChatSession remotechatsession = remotechatsession$data.validate(gameprofile, signaturevalidator, ProfilePublicKey.EXPIRY_GRACE_PERIOD);
               pPlayerInfo.setChatSession(remotechatsession);
            } catch (ProfilePublicKey.ValidationException profilepublickey$validationexception) {
               LOGGER.error("Failed to validate profile key for player: '{}'", gameprofile.getName(), profilepublickey$validationexception);
               pPlayerInfo.clearChatSession(this.enforcesSecureChat());
            }
         } else {
            pPlayerInfo.clearChatSession(this.enforcesSecureChat());
         }

      }
   }

   private boolean enforcesSecureChat() {
      return this.f_244115_ != null && this.f_244115_.enforcesSecureChat();
   }

   public void m_7231_(ClientboundKeepAlivePacket p_105020_) {
      this.m_269234_(new ServerboundKeepAlivePacket(p_105020_.m_132219_()), () -> {
         return !RenderSystem.isFrozenAtPollEvents();
      }, Duration.ofMinutes(1L));
   }

   private void m_269234_(Packet<ServerGamePacketListener> p_270433_, BooleanSupplier p_270843_, Duration p_270497_) {
      if (p_270843_.getAsBoolean()) {
         this.m_104955_(p_270433_);
      } else {
         this.f_268637_.add(new ClientPacketListener.DeferredPacket(p_270433_, p_270843_, Util.getMillis() + p_270497_.toMillis()));
      }

   }

   private void m_269212_() {
      Iterator<ClientPacketListener.DeferredPacket> iterator = this.f_268637_.iterator();

      while(iterator.hasNext()) {
         ClientPacketListener.DeferredPacket clientpacketlistener$deferredpacket = iterator.next();
         if (clientpacketlistener$deferredpacket.f_268477_().getAsBoolean()) {
            this.m_104955_(clientpacketlistener$deferredpacket.f_268574_);
            iterator.remove();
         } else if (clientpacketlistener$deferredpacket.f_268654_() <= Util.getMillis()) {
            iterator.remove();
         }
      }

   }

   public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Player player = this.f_104888_.player;
      player.getAbilities().flying = pPacket.isFlying();
      player.getAbilities().instabuild = pPacket.canInstabuild();
      player.getAbilities().invulnerable = pPacket.isInvulnerable();
      player.getAbilities().mayfly = pPacket.canFly();
      player.getAbilities().setFlyingSpeed(pPacket.getFlyingSpeed());
      player.getAbilities().setWalkingSpeed(pPacket.getWalkingSpeed());
   }

   public void handleSoundEvent(ClientboundSoundPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.level.playSeededSound(this.f_104888_.player, pPacket.getX(), pPacket.getY(), pPacket.getZ(), pPacket.getSound(), pPacket.getSource(), pPacket.getVolume(), pPacket.getPitch(), pPacket.getSeed());
   }

   public void handleSoundEntityEvent(ClientboundSoundEntityPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = this.level.getEntity(pPacket.getId());
      if (entity != null) {
         this.f_104888_.level.playSeededSound(this.f_104888_.player, entity, pPacket.getSound(), pPacket.getSource(), pPacket.getVolume(), pPacket.getPitch(), pPacket.getSeed());
      }
   }

   public void m_5587_(ClientboundResourcePackPacket p_105064_) {
      URL url = m_233709_(p_105064_.m_132924_());
      if (url == null) {
         this.m_105135_(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
      } else {
         String s = p_105064_.m_132927_();
         boolean flag = p_105064_.m_179188_();
         if (this.f_244115_ != null && this.f_244115_.getResourcePackStatus() == ServerData.ServerPackStatus.ENABLED) {
            this.m_105135_(ServerboundResourcePackPacket.Action.ACCEPTED);
            this.m_104951_(this.f_104888_.getDownloadedPackSource().downloadAndSelectResourcePack(url, s, true));
         } else if (this.f_244115_ != null && this.f_244115_.getResourcePackStatus() != ServerData.ServerPackStatus.PROMPT && (!flag || this.f_244115_.getResourcePackStatus() != ServerData.ServerPackStatus.DISABLED)) {
            this.m_105135_(ServerboundResourcePackPacket.Action.DECLINED);
            if (flag) {
               this.f_104885_.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
            }
         } else {
            this.f_104888_.execute(() -> {
               this.f_104888_.setScreen(new ConfirmScreen((p_233690_) -> {
                  this.f_104888_.setScreen((Screen)null);
                  if (p_233690_) {
                     if (this.f_244115_ != null) {
                        this.f_244115_.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
                     }

                     this.m_105135_(ServerboundResourcePackPacket.Action.ACCEPTED);
                     this.m_104951_(this.f_104888_.getDownloadedPackSource().downloadAndSelectResourcePack(url, s, true));
                  } else {
                     this.m_105135_(ServerboundResourcePackPacket.Action.DECLINED);
                     if (flag) {
                        this.f_104885_.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
                     } else if (this.f_244115_ != null) {
                        this.f_244115_.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
                     }
                  }

                  if (this.f_244115_ != null) {
                     ServerList.saveSingleServer(this.f_244115_);
                  }

               }, flag ? Component.translatable("multiplayer.requiredTexturePrompt.line1") : Component.translatable("multiplayer.texturePrompt.line1"), m_171759_(flag ? Component.translatable("multiplayer.requiredTexturePrompt.line2").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD) : Component.translatable("multiplayer.texturePrompt.line2"), p_105064_.m_179189_()), flag ? CommonComponents.GUI_PROCEED : CommonComponents.GUI_YES, (Component)(flag ? Component.translatable("menu.disconnect") : CommonComponents.GUI_NO)));
            });
         }

      }
   }

   private static Component m_171759_(Component p_171760_, @Nullable Component p_171761_) {
      return (Component)(p_171761_ == null ? p_171760_ : Component.translatable("multiplayer.texturePrompt.serverPrompt", p_171760_, p_171761_));
   }

   @Nullable
   private static URL m_233709_(String p_233710_) {
      try {
         URL url = new URL(p_233710_);
         String s = url.getProtocol();
         return !"http".equals(s) && !"https".equals(s) ? null : url;
      } catch (MalformedURLException malformedurlexception) {
         return null;
      }
   }

   private void m_104951_(CompletableFuture<?> p_104952_) {
      p_104952_.thenRun(() -> {
         this.m_105135_(ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED);
      }).exceptionally((p_233680_) -> {
         this.m_105135_(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
         return null;
      });
   }

   private void m_105135_(ServerboundResourcePackPacket.Action p_105136_) {
      this.f_104885_.send(new ServerboundResourcePackPacket(p_105136_));
   }

   public void handleBossUpdate(ClientboundBossEventPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.f_104888_.gui.getBossOverlay().update(pPacket);
   }

   public void handleItemCooldown(ClientboundCooldownPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      if (pPacket.getDuration() == 0) {
         this.f_104888_.player.getCooldowns().removeCooldown(pPacket.getItem());
      } else {
         this.f_104888_.player.getCooldowns().addCooldown(pPacket.getItem(), pPacket.getDuration());
      }

   }

   public void handleMoveVehicle(ClientboundMoveVehiclePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = this.f_104888_.player.getRootVehicle();
      if (entity != this.f_104888_.player && entity.isControlledByLocalInstance()) {
         entity.absMoveTo(pPacket.getX(), pPacket.getY(), pPacket.getZ(), pPacket.getYRot(), pPacket.getXRot());
         this.f_104885_.send(new ServerboundMoveVehiclePacket(entity));
      }

   }

   public void handleOpenBook(ClientboundOpenBookPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      ItemStack itemstack = this.f_104888_.player.getItemInHand(pPacket.getHand());
      if (itemstack.is(Items.WRITTEN_BOOK)) {
         this.f_104888_.setScreen(new BookViewScreen(new BookViewScreen.WrittenBookAccess(itemstack)));
      }

   }

   public void m_7413_(ClientboundCustomPayloadPacket p_105004_) {
      if (net.minecraftforge.network.NetworkHooks.onCustomPayload(p_105004_, this.f_104885_)) return;
      PacketUtils.ensureRunningOnSameThread(p_105004_, this, this.f_104888_);
      ResourceLocation resourcelocation = p_105004_.m_132042_();
      FriendlyByteBuf friendlybytebuf = null;

      try {
         friendlybytebuf = p_105004_.m_132045_();
         if (ClientboundCustomPayloadPacket.f_132012_.equals(resourcelocation)) {
            String s = friendlybytebuf.readUtf();
            this.f_104888_.player.m_108748_(s);
            this.f_194191_.onServerBrandReceived(s);
         } else if (ClientboundCustomPayloadPacket.f_132013_.equals(resourcelocation)) {
            int k1 = friendlybytebuf.readInt();
            float f = friendlybytebuf.readFloat();
            Path path = Path.createFromStream(friendlybytebuf);
            this.f_104888_.debugRenderer.pathfindingRenderer.addPath(k1, path, f);
         } else if (ClientboundCustomPayloadPacket.f_132014_.equals(resourcelocation)) {
            long l1 = friendlybytebuf.readVarLong();
            BlockPos blockpos8 = friendlybytebuf.readBlockPos();
            ((NeighborsUpdateRenderer)this.f_104888_.debugRenderer.neighborsUpdateRenderer).addUpdate(l1, blockpos8);
         } else if (ClientboundCustomPayloadPacket.f_132016_.equals(resourcelocation)) {
            DimensionType dimensiontype = this.registryAccess.compositeAccess().registryOrThrow(Registries.DIMENSION_TYPE).get(friendlybytebuf.readResourceLocation());
            BoundingBox boundingbox = new BoundingBox(friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt());
            int l3 = friendlybytebuf.readInt();
            List<BoundingBox> list = Lists.newArrayList();
            List<Boolean> list1 = Lists.newArrayList();

            for(int i = 0; i < l3; ++i) {
               list.add(new BoundingBox(friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt()));
               list1.add(friendlybytebuf.readBoolean());
            }

            this.f_104888_.debugRenderer.structureRenderer.addBoundingBox(boundingbox, list, list1, dimensiontype);
         } else if (ClientboundCustomPayloadPacket.f_132017_.equals(resourcelocation)) {
            ((WorldGenAttemptRenderer)this.f_104888_.debugRenderer.worldGenAttemptRenderer).addPos(friendlybytebuf.readBlockPos(), friendlybytebuf.readFloat(), friendlybytebuf.readFloat(), friendlybytebuf.readFloat(), friendlybytebuf.readFloat(), friendlybytebuf.readFloat());
         } else if (ClientboundCustomPayloadPacket.f_132021_.equals(resourcelocation)) {
            int i2 = friendlybytebuf.readInt();

            for(int k2 = 0; k2 < i2; ++k2) {
               this.f_104888_.debugRenderer.villageSectionsDebugRenderer.setVillageSection(friendlybytebuf.readSectionPos());
            }

            int l2 = friendlybytebuf.readInt();

            for(int i4 = 0; i4 < l2; ++i4) {
               this.f_104888_.debugRenderer.villageSectionsDebugRenderer.setNotVillageSection(friendlybytebuf.readSectionPos());
            }
         } else if (ClientboundCustomPayloadPacket.f_132019_.equals(resourcelocation)) {
            BlockPos blockpos2 = friendlybytebuf.readBlockPos();
            String s9 = friendlybytebuf.readUtf();
            int j4 = friendlybytebuf.readInt();
            BrainDebugRenderer.PoiInfo braindebugrenderer$poiinfo = new BrainDebugRenderer.PoiInfo(blockpos2, s9, j4);
            this.f_104888_.debugRenderer.brainDebugRenderer.addPoi(braindebugrenderer$poiinfo);
         } else if (ClientboundCustomPayloadPacket.f_132020_.equals(resourcelocation)) {
            BlockPos blockpos3 = friendlybytebuf.readBlockPos();
            this.f_104888_.debugRenderer.brainDebugRenderer.removePoi(blockpos3);
         } else if (ClientboundCustomPayloadPacket.f_132018_.equals(resourcelocation)) {
            BlockPos blockpos4 = friendlybytebuf.readBlockPos();
            int i3 = friendlybytebuf.readInt();
            this.f_104888_.debugRenderer.brainDebugRenderer.setFreeTicketCount(blockpos4, i3);
         } else if (ClientboundCustomPayloadPacket.f_132022_.equals(resourcelocation)) {
            BlockPos blockpos5 = friendlybytebuf.readBlockPos();
            int j3 = friendlybytebuf.readInt();
            int k4 = friendlybytebuf.readInt();
            List<GoalSelectorDebugRenderer.DebugGoal> list2 = Lists.newArrayList();

            for(int i6 = 0; i6 < k4; ++i6) {
               int j6 = friendlybytebuf.readInt();
               boolean flag = friendlybytebuf.readBoolean();
               String s1 = friendlybytebuf.readUtf(255);
               list2.add(new GoalSelectorDebugRenderer.DebugGoal(blockpos5, j6, s1, flag));
            }

            this.f_104888_.debugRenderer.goalSelectorRenderer.addGoalSelector(j3, list2);
         } else if (ClientboundCustomPayloadPacket.f_132028_.equals(resourcelocation)) {
            int j2 = friendlybytebuf.readInt();
            Collection<BlockPos> collection = Lists.newArrayList();

            for(int l4 = 0; l4 < j2; ++l4) {
               collection.add(friendlybytebuf.readBlockPos());
            }

            this.f_104888_.debugRenderer.raidDebugRenderer.setRaidCenters(collection);
         } else if (ClientboundCustomPayloadPacket.f_132023_.equals(resourcelocation)) {
            double d0 = friendlybytebuf.readDouble();
            double d2 = friendlybytebuf.readDouble();
            double d4 = friendlybytebuf.readDouble();
            Position position = new PositionImpl(d0, d2, d4);
            UUID uuid = friendlybytebuf.readUUID();
            int j = friendlybytebuf.readInt();
            String s2 = friendlybytebuf.readUtf();
            String s3 = friendlybytebuf.readUtf();
            int k = friendlybytebuf.readInt();
            float f1 = friendlybytebuf.readFloat();
            float f2 = friendlybytebuf.readFloat();
            String s4 = friendlybytebuf.readUtf();
            Path path1 = friendlybytebuf.readNullable(Path::createFromStream);
            boolean flag1 = friendlybytebuf.readBoolean();
            int l = friendlybytebuf.readInt();
            BrainDebugRenderer.BrainDump braindebugrenderer$braindump = new BrainDebugRenderer.BrainDump(uuid, j, s2, s3, k, f1, f2, position, s4, path1, flag1, l);
            int i1 = friendlybytebuf.readVarInt();

            for(int j1 = 0; j1 < i1; ++j1) {
               String s5 = friendlybytebuf.readUtf();
               braindebugrenderer$braindump.f_113304_.add(s5);
            }

            int i8 = friendlybytebuf.readVarInt();

            for(int j8 = 0; j8 < i8; ++j8) {
               String s6 = friendlybytebuf.readUtf();
               braindebugrenderer$braindump.f_113305_.add(s6);
            }

            int k8 = friendlybytebuf.readVarInt();

            for(int l8 = 0; l8 < k8; ++l8) {
               String s7 = friendlybytebuf.readUtf();
               braindebugrenderer$braindump.f_113306_.add(s7);
            }

            int i9 = friendlybytebuf.readVarInt();

            for(int j9 = 0; j9 < i9; ++j9) {
               BlockPos blockpos = friendlybytebuf.readBlockPos();
               braindebugrenderer$braindump.f_113308_.add(blockpos);
            }

            int k9 = friendlybytebuf.readVarInt();

            for(int l9 = 0; l9 < k9; ++l9) {
               BlockPos blockpos1 = friendlybytebuf.readBlockPos();
               braindebugrenderer$braindump.f_113309_.add(blockpos1);
            }

            int i10 = friendlybytebuf.readVarInt();

            for(int j10 = 0; j10 < i10; ++j10) {
               String s8 = friendlybytebuf.readUtf();
               braindebugrenderer$braindump.f_113307_.add(s8);
            }

            this.f_104888_.debugRenderer.brainDebugRenderer.addOrUpdateBrainDump(braindebugrenderer$braindump);
         } else if (ClientboundCustomPayloadPacket.f_132024_.equals(resourcelocation)) {
            double d1 = friendlybytebuf.readDouble();
            double d3 = friendlybytebuf.readDouble();
            double d5 = friendlybytebuf.readDouble();
            Position position1 = new PositionImpl(d1, d3, d5);
            UUID uuid1 = friendlybytebuf.readUUID();
            int k6 = friendlybytebuf.readInt();
            BlockPos blockpos9 = friendlybytebuf.readNullable(FriendlyByteBuf::readBlockPos);
            BlockPos blockpos10 = friendlybytebuf.readNullable(FriendlyByteBuf::readBlockPos);
            int l6 = friendlybytebuf.readInt();
            Path path2 = friendlybytebuf.readNullable(Path::createFromStream);
            BeeDebugRenderer.BeeInfo beedebugrenderer$beeinfo = new BeeDebugRenderer.BeeInfo(uuid1, k6, position1, path2, blockpos9, blockpos10, l6);
            int i7 = friendlybytebuf.readVarInt();

            for(int j7 = 0; j7 < i7; ++j7) {
               String s12 = friendlybytebuf.readUtf();
               beedebugrenderer$beeinfo.f_113164_.add(s12);
            }

            int k7 = friendlybytebuf.readVarInt();

            for(int l7 = 0; l7 < k7; ++l7) {
               BlockPos blockpos11 = friendlybytebuf.readBlockPos();
               beedebugrenderer$beeinfo.f_113165_.add(blockpos11);
            }

            this.f_104888_.debugRenderer.beeDebugRenderer.addOrUpdateBeeInfo(beedebugrenderer$beeinfo);
         } else if (ClientboundCustomPayloadPacket.f_132025_.equals(resourcelocation)) {
            BlockPos blockpos6 = friendlybytebuf.readBlockPos();
            String s10 = friendlybytebuf.readUtf();
            int i5 = friendlybytebuf.readInt();
            int k5 = friendlybytebuf.readInt();
            boolean flag2 = friendlybytebuf.readBoolean();
            BeeDebugRenderer.HiveInfo beedebugrenderer$hiveinfo = new BeeDebugRenderer.HiveInfo(blockpos6, s10, i5, k5, flag2, this.level.getGameTime());
            this.f_104888_.debugRenderer.beeDebugRenderer.addOrUpdateHiveInfo(beedebugrenderer$hiveinfo);
         } else if (ClientboundCustomPayloadPacket.f_132027_.equals(resourcelocation)) {
            this.f_104888_.debugRenderer.gameTestDebugRenderer.clear();
         } else if (ClientboundCustomPayloadPacket.f_132026_.equals(resourcelocation)) {
            BlockPos blockpos7 = friendlybytebuf.readBlockPos();
            int k3 = friendlybytebuf.readInt();
            String s11 = friendlybytebuf.readUtf();
            int l5 = friendlybytebuf.readInt();
            this.f_104888_.debugRenderer.gameTestDebugRenderer.addMarker(blockpos7, k3, s11, l5);
         } else if (ClientboundCustomPayloadPacket.f_178832_.equals(resourcelocation)) {
            GameEvent gameevent = BuiltInRegistries.GAME_EVENT.get(new ResourceLocation(friendlybytebuf.readUtf()));
            Vec3 vec3 = new Vec3(friendlybytebuf.readDouble(), friendlybytebuf.readDouble(), friendlybytebuf.readDouble());
            this.f_104888_.debugRenderer.gameEventListenerRenderer.trackGameEvent(gameevent, vec3);
         } else if (ClientboundCustomPayloadPacket.f_178833_.equals(resourcelocation)) {
            ResourceLocation resourcelocation1 = friendlybytebuf.readResourceLocation();
            PositionSource positionsource = BuiltInRegistries.POSITION_SOURCE_TYPE.getOptional(resourcelocation1).orElseThrow(() -> {
               return new IllegalArgumentException("Unknown position source type " + resourcelocation1);
            }).read(friendlybytebuf);
            int j5 = friendlybytebuf.readVarInt();
            this.f_104888_.debugRenderer.gameEventListenerRenderer.trackListener(positionsource, j5);
         } else {
            LOGGER.warn("Unknown custom packet identifier: {}", (Object)resourcelocation);
         }
      } finally {
         if (friendlybytebuf != null) {
            friendlybytebuf.release();
         }

      }

   }

   /**
    * May create a scoreboard objective, remove an objective from the scoreboard or update an objectives' displayname
    */
   public void handleAddObjective(ClientboundSetObjectivePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Scoreboard scoreboard = this.level.getScoreboard();
      String s = pPacket.getObjectiveName();
      if (pPacket.getMethod() == 0) {
         scoreboard.addObjective(s, ObjectiveCriteria.DUMMY, pPacket.getDisplayName(), pPacket.getRenderType());
      } else if (scoreboard.m_83459_(s)) {
         Objective objective = scoreboard.getObjective(s);
         if (pPacket.getMethod() == 1) {
            scoreboard.removeObjective(objective);
         } else if (pPacket.getMethod() == 2) {
            objective.setRenderType(pPacket.getRenderType());
            objective.setDisplayName(pPacket.getDisplayName());
         }
      }

   }

   /**
    * Either updates the score with a specified value or removes the score for an objective
    */
   public void handleSetScore(ClientboundSetScorePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Scoreboard scoreboard = this.level.getScoreboard();
      String s = pPacket.getObjectiveName();
      switch (pPacket.getMethod()) {
         case CHANGE:
            Objective objective = scoreboard.m_83469_(s);
            Score score = scoreboard.getOrCreatePlayerScore(pPacket.getOwner(), objective);
            score.setScore(pPacket.getScore());
            break;
         case REMOVE:
            scoreboard.resetPlayerScore(pPacket.getOwner(), scoreboard.getObjective(s));
      }

   }

   /**
    * Removes or sets the ScoreObjective to be displayed at a particular scoreboard position (list, sidebar, below name)
    */
   public void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Scoreboard scoreboard = this.level.getScoreboard();
      String s = pPacket.getObjectiveName();
      Objective objective = s == null ? null : scoreboard.m_83469_(s);
      scoreboard.setDisplayObjective(pPacket.getSlot(), objective);
   }

   /**
    * Updates a team managed by the scoreboard: Create/Remove the team registration, Register/Remove the player-team-
    * memberships, Set team displayname/prefix/suffix and/or whether friendly fire is enabled
    */
   public void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Scoreboard scoreboard = this.level.getScoreboard();
      ClientboundSetPlayerTeamPacket.Action clientboundsetplayerteampacket$action = pPacket.getTeamAction();
      PlayerTeam playerteam;
      if (clientboundsetplayerteampacket$action == ClientboundSetPlayerTeamPacket.Action.ADD) {
         playerteam = scoreboard.addPlayerTeam(pPacket.getName());
      } else {
         playerteam = scoreboard.getPlayerTeam(pPacket.getName());
         if (playerteam == null) {
            LOGGER.warn("Received packet for unknown team {}: team action: {}, player action: {}", pPacket.getName(), pPacket.getTeamAction(), pPacket.getPlayerAction());
            return;
         }
      }

      Optional<ClientboundSetPlayerTeamPacket.Parameters> optional = pPacket.getParameters();
      optional.ifPresent((p_233670_) -> {
         playerteam.setDisplayName(p_233670_.getDisplayName());
         playerteam.setColor(p_233670_.getColor());
         playerteam.unpackOptions(p_233670_.getOptions());
         Team.Visibility team$visibility = Team.Visibility.byName(p_233670_.getNametagVisibility());
         if (team$visibility != null) {
            playerteam.setNameTagVisibility(team$visibility);
         }

         Team.CollisionRule team$collisionrule = Team.CollisionRule.byName(p_233670_.getCollisionRule());
         if (team$collisionrule != null) {
            playerteam.setCollisionRule(team$collisionrule);
         }

         playerteam.setPlayerPrefix(p_233670_.getPlayerPrefix());
         playerteam.setPlayerSuffix(p_233670_.getPlayerSuffix());
      });
      ClientboundSetPlayerTeamPacket.Action clientboundsetplayerteampacket$action1 = pPacket.getPlayerAction();
      if (clientboundsetplayerteampacket$action1 == ClientboundSetPlayerTeamPacket.Action.ADD) {
         for(String s : pPacket.getPlayers()) {
            scoreboard.addPlayerToTeam(s, playerteam);
         }
      } else if (clientboundsetplayerteampacket$action1 == ClientboundSetPlayerTeamPacket.Action.REMOVE) {
         for(String s1 : pPacket.getPlayers()) {
            scoreboard.removePlayerFromTeam(s1, playerteam);
         }
      }

      if (clientboundsetplayerteampacket$action == ClientboundSetPlayerTeamPacket.Action.REMOVE) {
         scoreboard.removePlayerTeam(playerteam);
      }

   }

   /**
    * Spawns a specified number of particles at the specified location with a randomized displacement according to
    * specified bounds
    */
   public void handleParticleEvent(ClientboundLevelParticlesPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      if (pPacket.getCount() == 0) {
         double d0 = (double)(pPacket.getMaxSpeed() * pPacket.getXDist());
         double d2 = (double)(pPacket.getMaxSpeed() * pPacket.getYDist());
         double d4 = (double)(pPacket.getMaxSpeed() * pPacket.getZDist());

         try {
            this.level.addParticle(pPacket.getParticle(), pPacket.isOverrideLimiter(), pPacket.getX(), pPacket.getY(), pPacket.getZ(), d0, d2, d4);
         } catch (Throwable throwable1) {
            LOGGER.warn("Could not spawn particle effect {}", (Object)pPacket.getParticle());
         }
      } else {
         for(int i = 0; i < pPacket.getCount(); ++i) {
            double d1 = this.random.nextGaussian() * (double)pPacket.getXDist();
            double d3 = this.random.nextGaussian() * (double)pPacket.getYDist();
            double d5 = this.random.nextGaussian() * (double)pPacket.getZDist();
            double d6 = this.random.nextGaussian() * (double)pPacket.getMaxSpeed();
            double d7 = this.random.nextGaussian() * (double)pPacket.getMaxSpeed();
            double d8 = this.random.nextGaussian() * (double)pPacket.getMaxSpeed();

            try {
               this.level.addParticle(pPacket.getParticle(), pPacket.isOverrideLimiter(), pPacket.getX() + d1, pPacket.getY() + d3, pPacket.getZ() + d5, d6, d7, d8);
            } catch (Throwable throwable) {
               LOGGER.warn("Could not spawn particle effect {}", (Object)pPacket.getParticle());
               return;
            }
         }
      }

   }

   public void m_141955_(ClientboundPingPacket p_171769_) {
      PacketUtils.ensureRunningOnSameThread(p_171769_, this, this.f_104888_);
      this.m_104955_(new ServerboundPongPacket(p_171769_.m_179025_()));
   }

   /**
    * Updates en entity's attributes and their respective modifiers, which are used for speed bonuses (player sprinting,
    * animals fleeing, baby speed), weapon/tool attackDamage, hostiles followRange randomization, zombie maxHealth and
    * knockback resistance as well as reinforcement spawning chance.
    */
   public void handleUpdateAttributes(ClientboundUpdateAttributesPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      Entity entity = this.level.getEntity(pPacket.getEntityId());
      if (entity != null) {
         if (!(entity instanceof LivingEntity)) {
            throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + entity + ")");
         } else {
            AttributeMap attributemap = ((LivingEntity)entity).getAttributes();

            for(ClientboundUpdateAttributesPacket.AttributeSnapshot clientboundupdateattributespacket$attributesnapshot : pPacket.getValues()) {
               AttributeInstance attributeinstance = attributemap.getInstance(clientboundupdateattributespacket$attributesnapshot.getAttribute());
               if (attributeinstance == null) {
                  LOGGER.warn("Entity {} does not have attribute {}", entity, BuiltInRegistries.ATTRIBUTE.getKey(clientboundupdateattributespacket$attributesnapshot.getAttribute()));
               } else {
                  attributeinstance.setBaseValue(clientboundupdateattributespacket$attributesnapshot.getBase());
                  attributeinstance.removeModifiers();

                  for(AttributeModifier attributemodifier : clientboundupdateattributespacket$attributesnapshot.getModifiers()) {
                     attributeinstance.addTransientModifier(attributemodifier);
                  }
               }
            }

         }
      }
   }

   public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      AbstractContainerMenu abstractcontainermenu = this.f_104888_.player.containerMenu;
      if (abstractcontainermenu.containerId == pPacket.getContainerId()) {
         this.recipeManager.byKey(pPacket.getRecipe()).ifPresent((p_233667_) -> {
            if (this.f_104888_.screen instanceof RecipeUpdateListener) {
               RecipeBookComponent recipebookcomponent = ((RecipeUpdateListener)this.f_104888_.screen).getRecipeBookComponent();
               recipebookcomponent.setupGhostRecipe(p_233667_, abstractcontainermenu.slots);
            }

         });
      }
   }

   public void handleLightUpdatePacket(ClientboundLightUpdatePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      int i = pPacket.getX();
      int j = pPacket.getZ();
      ClientboundLightUpdatePacketData clientboundlightupdatepacketdata = pPacket.getLightData();
      this.level.queueLightUpdate(() -> {
         this.applyLightData(i, j, clientboundlightupdatepacketdata);
      });
   }

   private void applyLightData(int pX, int pZ, ClientboundLightUpdatePacketData pData) {
      LevelLightEngine levellightengine = this.level.getChunkSource().getLightEngine();
      BitSet bitset = pData.getSkyYMask();
      BitSet bitset1 = pData.getEmptySkyYMask();
      Iterator<byte[]> iterator = pData.getSkyUpdates().iterator();
      this.readSectionList(pX, pZ, levellightengine, LightLayer.SKY, bitset, bitset1, iterator);
      BitSet bitset2 = pData.getBlockYMask();
      BitSet bitset3 = pData.getEmptyBlockYMask();
      Iterator<byte[]> iterator1 = pData.getBlockUpdates().iterator();
      this.readSectionList(pX, pZ, levellightengine, LightLayer.BLOCK, bitset2, bitset3, iterator1);
      levellightengine.setLightEnabled(new ChunkPos(pX, pZ), true);
   }

   public void handleMerchantOffers(ClientboundMerchantOffersPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      AbstractContainerMenu abstractcontainermenu = this.f_104888_.player.containerMenu;
      if (pPacket.getContainerId() == abstractcontainermenu.containerId && abstractcontainermenu instanceof MerchantMenu merchantmenu) {
         merchantmenu.setOffers(new MerchantOffers(pPacket.getOffers().createTag()));
         merchantmenu.setXp(pPacket.getVillagerXp());
         merchantmenu.setMerchantLevel(pPacket.getVillagerLevel());
         merchantmenu.setShowProgressBar(pPacket.showProgress());
         merchantmenu.setCanRestock(pPacket.canRestock());
      }

   }

   public void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.serverChunkRadius = pPacket.getRadius();
      this.f_104888_.options.setServerRenderDistance(this.serverChunkRadius);
      this.level.getChunkSource().updateViewRadius(pPacket.getRadius());
   }

   public void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.serverSimulationDistance = pPacket.simulationDistance();
      this.level.setServerSimulationDistance(this.serverSimulationDistance);
   }

   public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.level.getChunkSource().updateViewCenter(pPacket.getX(), pPacket.getZ());
   }

   public void handleBlockChangedAck(ClientboundBlockChangedAckPacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);
      this.level.handleBlockChangedAck(pPacket.sequence());
   }

   public void handleBundlePacket(ClientboundBundlePacket pPacket) {
      PacketUtils.ensureRunningOnSameThread(pPacket, this, this.f_104888_);

      for(Packet<ClientGamePacketListener> packet : pPacket.subPackets()) {
         packet.handle(this);
      }

   }

   private void readSectionList(int pX, int pZ, LevelLightEngine pLightEngine, LightLayer pLightLayer, BitSet pSkyYMask, BitSet pEmptySkyYMask, Iterator<byte[]> pSkyUpdates) {
      for(int i = 0; i < pLightEngine.getLightSectionCount(); ++i) {
         int j = pLightEngine.getMinLightSection() + i;
         boolean flag = pSkyYMask.get(i);
         boolean flag1 = pEmptySkyYMask.get(i);
         if (flag || flag1) {
            pLightEngine.queueSectionData(pLightLayer, SectionPos.of(pX, j, pZ), flag ? new DataLayer((byte[])pSkyUpdates.next().clone()) : new DataLayer());
            this.level.setSectionDirtyWithNeighbors(pX, j, pZ);
         }
      }

   }

   /**
    * Returns this the NetworkManager instance registered with this NetworkHandlerPlayClient
    */
   public Connection getConnection() {
      return this.f_104885_;
   }

   public boolean isAcceptingMessages() {
      return this.f_104885_.isConnected();
   }

   public Collection<PlayerInfo> getListedOnlinePlayers() {
      return this.listedPlayers;
   }

   public Collection<PlayerInfo> getOnlinePlayers() {
      return this.playerInfoMap.values();
   }

   public Collection<UUID> getOnlinePlayerIds() {
      return this.playerInfoMap.keySet();
   }

   @Nullable
   public PlayerInfo getPlayerInfo(UUID pUniqueId) {
      return this.playerInfoMap.get(pUniqueId);
   }

   /**
    * Gets the client's description information about another player on the server.
    */
   @Nullable
   public PlayerInfo getPlayerInfo(String pName) {
      for(PlayerInfo playerinfo : this.playerInfoMap.values()) {
         if (playerinfo.getProfile().getName().equals(pName)) {
            return playerinfo;
         }
      }

      return null;
   }

   public GameProfile getLocalGameProfile() {
      return this.localGameProfile;
   }

   public ClientAdvancements getAdvancements() {
      return this.advancements;
   }

   public CommandDispatcher<SharedSuggestionProvider> getCommands() {
      return this.commands;
   }

   public ClientLevel getLevel() {
      return this.level;
   }

   public DebugQueryHandler getDebugQueryHandler() {
      return this.debugQueryHandler;
   }

   public UUID getId() {
      return this.id;
   }

   public Set<ResourceKey<Level>> levels() {
      return this.levels;
   }

   public RegistryAccess registryAccess() {
      return this.registryAccess.compositeAccess();
   }

   public void markMessageAsProcessed(PlayerChatMessage pChatMessage, boolean pAcknowledged) {
      MessageSignature messagesignature = pChatMessage.signature();
      if (messagesignature != null && this.lastSeenMessages.addPending(messagesignature, pAcknowledged) && this.lastSeenMessages.offset() > 64) {
         this.sendChatAcknowledgement();
      }

   }

   private void sendChatAcknowledgement() {
      int i = this.lastSeenMessages.getAndClearOffset();
      if (i > 0) {
         this.m_104955_(new ServerboundChatAckPacket(i));
      }

   }

   public void sendChat(String pMessage) {
      pMessage = net.minecraftforge.client.ForgeHooksClient.onClientSendMessage(pMessage);
      if (pMessage.isEmpty()) return;
      Instant instant = Instant.now();
      long i = Crypt.SaltSupplier.getLong();
      LastSeenMessagesTracker.Update lastseenmessagestracker$update = this.lastSeenMessages.generateAndApplyUpdate();
      MessageSignature messagesignature = this.signedMessageEncoder.pack(new SignedMessageBody(pMessage, instant, i, lastseenmessagestracker$update.lastSeen()));
      this.m_104955_(new ServerboundChatPacket(pMessage, instant, i, messagesignature, lastseenmessagestracker$update.update()));
   }

   public void sendCommand(String pCommand) {
      if (net.minecraftforge.client.ClientCommandHandler.runCommand(pCommand)) return;
      Instant instant = Instant.now();
      long i = Crypt.SaltSupplier.getLong();
      LastSeenMessagesTracker.Update lastseenmessagestracker$update = this.lastSeenMessages.generateAndApplyUpdate();
      ArgumentSignatures argumentsignatures = ArgumentSignatures.signCommand(SignableCommand.of(this.parseCommand(pCommand)), (p_247875_) -> {
         SignedMessageBody signedmessagebody = new SignedMessageBody(p_247875_, instant, i, lastseenmessagestracker$update.lastSeen());
         return this.signedMessageEncoder.pack(signedmessagebody);
      });
      this.m_104955_(new ServerboundChatCommandPacket(pCommand, instant, i, argumentsignatures, lastseenmessagestracker$update.update()));
   }

   public boolean sendUnsignedCommand(String pCommand) {
      if (SignableCommand.of(this.parseCommand(pCommand)).arguments().isEmpty()) {
         LastSeenMessagesTracker.Update lastseenmessagestracker$update = this.lastSeenMessages.generateAndApplyUpdate();
         this.m_104955_(new ServerboundChatCommandPacket(pCommand, Instant.now(), 0L, ArgumentSignatures.EMPTY, lastseenmessagestracker$update.update()));
         return true;
      } else {
         return false;
      }
   }

   private ParseResults<SharedSuggestionProvider> parseCommand(String pCommand) {
      return this.commands.parse(pCommand, this.suggestionsProvider);
   }

   public void tick() {
      if (this.f_104885_.isEncrypted()) {
         ProfileKeyPairManager profilekeypairmanager = this.f_104888_.getProfileKeyPairManager();
         if (profilekeypairmanager.shouldRefreshKeyPair()) {
            profilekeypairmanager.prepareKeyPair().thenAcceptAsync((p_253339_) -> {
               p_253339_.ifPresent(this::setKeyPair);
            }, this.f_104888_);
         }
      }

      this.m_269212_();
      this.f_194191_.tick();
   }

   public void setKeyPair(ProfileKeyPair p_261475_) {
      if (this.localGameProfile.getId().equals(this.f_104888_.getUser().getProfileId())) {
         if (this.chatSession == null || !this.chatSession.keyPair().equals(p_261475_)) {
            this.chatSession = LocalChatSession.create(p_261475_);
            this.signedMessageEncoder = this.chatSession.createMessageEncoder(this.localGameProfile.getId());
            this.m_104955_(new ServerboundChatSessionUpdatePacket(this.chatSession.asRemote().asData()));
         }
      }
   }

   @Nullable
   public ServerData getServerData() {
      return this.f_244115_;
   }

   public FeatureFlagSet enabledFeatures() {
      return this.enabledFeatures;
   }

   public boolean isFeatureEnabled(FeatureFlagSet pEnabledFeatures) {
      return pEnabledFeatures.isSubsetOf(this.enabledFeatures());
   }

   @OnlyIn(Dist.CLIENT)
   static record DeferredPacket(Packet<ServerGamePacketListener> f_268574_, BooleanSupplier f_268477_, long f_268654_) {
   }
}
