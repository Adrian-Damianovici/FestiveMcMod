package net.minecraft.world.level.saveddata.maps;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class MapDecoration {
   private final MapDecoration.Type type;
   private final byte x;
   private final byte y;
   private final byte rot;
   @Nullable
   private final Component name;

   public MapDecoration(MapDecoration.Type p_77797_, byte p_77798_, byte p_77799_, byte p_77800_, @Nullable Component p_77801_) {
      this.type = p_77797_;
      this.x = p_77798_;
      this.y = p_77799_;
      this.rot = p_77800_;
      this.name = p_77801_;
   }

   public byte getImage() {
      return this.type.getIcon();
   }

   public MapDecoration.Type m_77803_() {
      return this.type;
   }

   public byte m_77804_() {
      return this.x;
   }

   public byte m_77805_() {
      return this.y;
   }

   public byte m_77806_() {
      return this.rot;
   }

   public boolean renderOnFrame() {
      return this.type.isRenderedOnFrame();
   }

   @Nullable
   public Component m_77810_() {
      return this.name;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (!(pOther instanceof MapDecoration)) {
         return false;
      } else {
         MapDecoration mapdecoration = (MapDecoration)pOther;
         return this.type == mapdecoration.type && this.rot == mapdecoration.rot && this.x == mapdecoration.x && this.y == mapdecoration.y && Objects.equals(this.name, mapdecoration.name);
      }
   }

   public int hashCode() {
      int i = this.type.getIcon();
      i = 31 * i + this.x;
      i = 31 * i + this.y;
      i = 31 * i + this.rot;
      return 31 * i + Objects.hashCode(this.name);
   }

   /**
    * Renders this decoration, useful for custom sprite sheets.
    * @param index The index of this icon in the MapData's list. Used by vanilla to offset the Z-coordinate to prevent Z-fighting
    * @return false to run vanilla logic for this decoration, true to skip it
    */
   public boolean render(int index) {
      return false;
   }

   public static enum Type {
      PLAYER(false, true),
      FRAME(true, true),
      RED_MARKER(false, true),
      BLUE_MARKER(false, true),
      TARGET_X(true, false),
      TARGET_POINT(true, false),
      PLAYER_OFF_MAP(false, true),
      PLAYER_OFF_LIMITS(false, true),
      MANSION(true, 5393476, false),
      MONUMENT(true, 3830373, false),
      BANNER_WHITE(true, true),
      BANNER_ORANGE(true, true),
      BANNER_MAGENTA(true, true),
      BANNER_LIGHT_BLUE(true, true),
      BANNER_YELLOW(true, true),
      BANNER_LIME(true, true),
      BANNER_PINK(true, true),
      BANNER_GRAY(true, true),
      BANNER_LIGHT_GRAY(true, true),
      BANNER_CYAN(true, true),
      BANNER_PURPLE(true, true),
      BANNER_BLUE(true, true),
      BANNER_BROWN(true, true),
      BANNER_GREEN(true, true),
      BANNER_RED(true, true),
      BANNER_BLACK(true, true),
      RED_X(true, false);

      private final byte icon;
      private final boolean renderedOnFrame;
      private final int mapColor;
      private final boolean trackCount;

      private Type(boolean pRenderedOnFrame, boolean pTrackCount) {
         this(pRenderedOnFrame, -1, pTrackCount);
      }

      private Type(boolean pRenderedOnFrame, int p_181299_, boolean pTrackCount) {
         this.trackCount = pTrackCount;
         this.icon = (byte)this.ordinal();
         this.renderedOnFrame = pRenderedOnFrame;
         this.mapColor = p_181299_;
      }

      public byte getIcon() {
         return this.icon;
      }

      public boolean isRenderedOnFrame() {
         return this.renderedOnFrame;
      }

      public boolean hasMapColor() {
         return this.mapColor >= 0;
      }

      public int getMapColor() {
         return this.mapColor;
      }

      public static MapDecoration.Type byIcon(byte pIconId) {
         return values()[Mth.clamp(pIconId, 0, values().length - 1)];
      }

      public boolean shouldTrackCount() {
         return this.trackCount;
      }
   }
}
