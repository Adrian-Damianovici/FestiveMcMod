package net.minecraft.server.packs.repository;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.flag.FeatureFlagSet;
import org.slf4j.Logger;

public class Pack {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String id;
   private final Pack.ResourcesSupplier resources;
   private final Component title;
   private final Component f_10404_;
   private final PackCompatibility f_10405_;
   private final FeatureFlagSet f_244623_;
   private final Pack.Position defaultPosition;
   private final boolean required;
   private final boolean fixedPosition;
   private final boolean hidden; // Forge: Allow packs to be hidden from the UI entirely
   private final PackSource packSource;

   @Nullable
   public static Pack readMetaAndCreate(String pId, Component pTitle, boolean pRequired, Pack.ResourcesSupplier pResources, PackType pPackType, Pack.Position pDefaultPosition, PackSource pPackSource) {
      Pack.Info pack$info = readPackInfo(pId, pResources);
      return pack$info != null ? create(pId, pTitle, pRequired, pResources, pack$info, pPackType, pDefaultPosition, false, pPackSource) : null;
   }

   public static Pack create(String pId, Component pTitle, boolean pRequired, Pack.ResourcesSupplier pResources, Pack.Info pInfo, PackType p_250264_, Pack.Position pDefaultPosition, boolean pFixedPosition, PackSource pPackSource) {
      return new Pack(pId, pRequired, pResources, pTitle, pInfo, pInfo.m_246438_(p_250264_), pDefaultPosition, pFixedPosition, pPackSource);
   }

   private Pack(String pId, boolean pRequired, Pack.ResourcesSupplier pResources, Component pTitle, Pack.Info pInfo, PackCompatibility p_250361_, Pack.Position pDefaultPosition, boolean pFixedPosition, PackSource pPackSource) {
      this.id = pId;
      this.resources = pResources;
      this.title = pTitle;
      this.f_10404_ = pInfo.description();
      this.f_10405_ = p_250361_;
      this.f_244623_ = pInfo.requestedFeatures();
      this.required = pRequired;
      this.defaultPosition = pDefaultPosition;
      this.fixedPosition = pFixedPosition;
      this.packSource = pPackSource;
      this.hidden = pInfo.hidden();
   }

   @Nullable
   public static Pack.Info readPackInfo(String pId, Pack.ResourcesSupplier pResources) {
      try (PackResources packresources = pResources.openFull(pId)) {
         PackMetadataSection packmetadatasection = packresources.getMetadataSection(PackMetadataSection.TYPE);
         if (packmetadatasection == null) {
            LOGGER.warn("Missing metadata in pack {}", (Object)pId);
            return null;
         } else {
            FeatureFlagsMetadataSection featureflagsmetadatasection = packresources.getMetadataSection(FeatureFlagsMetadataSection.TYPE);
            FeatureFlagSet featureflagset = featureflagsmetadatasection != null ? featureflagsmetadatasection.flags() : FeatureFlagSet.of();
            // Forge: Allow separate pack formats for server data and client resources and setting isHidden
            return new Pack.Info(packmetadatasection.m_10373_(), packmetadatasection.getPackFormat(PackType.SERVER_DATA), packmetadatasection.getPackFormat(PackType.CLIENT_RESOURCES), featureflagset, packresources.isHidden());
         }
      } catch (Exception exception) {
         LOGGER.warn("Failed to read pack metadata", (Throwable)exception);
         return null;
      }
   }

   public Component getTitle() {
      return this.title;
   }

   public Component getDescription() {
      return this.f_10404_;
   }

   /**
    * 
    * @param pGreen used to indicate either a successful operation or datapack enabled status
    */
   public Component getChatLink(boolean pGreen) {
      return ComponentUtils.wrapInSquareBrackets(this.packSource.decorate(Component.literal(this.id))).withStyle((p_10441_) -> {
         return p_10441_.withColor(pGreen ? ChatFormatting.GREEN : ChatFormatting.RED).withInsertion(StringArgumentType.escapeIfRequired(this.id)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.empty().append(this.title).append("\n").append(this.f_10404_)));
      });
   }

   public PackCompatibility getCompatibility() {
      return this.f_10405_;
   }

   public FeatureFlagSet getRequestedFeatures() {
      return this.f_244623_;
   }

   public PackResources open() {
      return this.resources.openFull(this.id);
   }

   public String getId() {
      return this.id;
   }

   public boolean isRequired() {
      return this.required;
   }

   public boolean isFixedPosition() {
      return this.fixedPosition;
   }

   public Pack.Position getDefaultPosition() {
      return this.defaultPosition;
   }

   public PackSource getPackSource() {
      return this.packSource;
   }

   public boolean isHidden() { return hidden; }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (!(pOther instanceof Pack)) {
         return false;
      } else {
         Pack pack = (Pack)pOther;
         return this.id.equals(pack.id);
      }
   }

   public int hashCode() {
      return this.id.hashCode();
   }

   public static record Info(Component description, int dataFormat, int resourceFormat, FeatureFlagSet requestedFeatures, boolean hidden) {
      public Info(Component description, int f_244194_, FeatureFlagSet requestedFeatures) {
         this(description, f_244194_, f_244194_, requestedFeatures, false);
      }

      public int getFormat(PackType type) {
         return type == PackType.SERVER_DATA ? this.dataFormat : this.resourceFormat;
      }

      public PackCompatibility m_246438_(PackType p_249204_) {
         return PackCompatibility.m_143882_(getFormat(p_249204_), p_249204_);
      }
   }

   public static enum Position {
      TOP,
      BOTTOM;

      public <T> int insert(List<T> pList, T pElement, Function<T, Pack> pPackFactory, boolean pFlipPosition) {
         Pack.Position pack$position = pFlipPosition ? this.opposite() : this;
         if (pack$position == BOTTOM) {
            int j;
            for(j = 0; j < pList.size(); ++j) {
               Pack pack1 = pPackFactory.apply(pList.get(j));
               if (!pack1.isFixedPosition() || pack1.getDefaultPosition() != this) {
                  break;
               }
            }

            pList.add(j, pElement);
            return j;
         } else {
            int i;
            for(i = pList.size() - 1; i >= 0; --i) {
               Pack pack = pPackFactory.apply(pList.get(i));
               if (!pack.isFixedPosition() || pack.getDefaultPosition() != this) {
                  break;
               }
            }

            pList.add(i + 1, pElement);
            return i + 1;
         }
      }

      public Pack.Position opposite() {
         return this == TOP ? BOTTOM : TOP;
      }
   }

   @FunctionalInterface
   public interface ResourcesSupplier {
      PackResources openFull(String pId);
   }
}
