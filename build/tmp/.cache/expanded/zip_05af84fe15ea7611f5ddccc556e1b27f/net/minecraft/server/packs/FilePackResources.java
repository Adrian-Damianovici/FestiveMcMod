package net.minecraft.server.packs;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class FilePackResources extends AbstractPackResources {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Splitter f_10232_ = Splitter.on('/').omitEmptyStrings().limit(3);
   private final File f_243750_;
   @Nullable
   private ZipFile f_10233_;
   private boolean f_243662_;

   public FilePackResources(String pName, File p_255707_, boolean pIsBuiltin) {
      super(pName, pIsBuiltin);
      this.f_243750_ = p_255707_;
   }

   @Nullable
   private ZipFile m_10247_() {
      if (this.f_243662_) {
         return null;
      } else {
         if (this.f_10233_ == null) {
            try {
               this.f_10233_ = new ZipFile(this.f_243750_);
            } catch (IOException ioexception) {
               LOGGER.error("Failed to open pack {}", this.f_243750_, ioexception);
               this.f_243662_ = true;
               return null;
            }
         }

         return this.f_10233_;
      }
   }

   private static String getPathFromLocation(PackType pPackType, ResourceLocation pLocation) {
      return String.format(Locale.ROOT, "%s/%s/%s", pPackType.getDirectory(), pLocation.getNamespace(), pLocation.getPath());
   }

   @Nullable
   public IoSupplier<InputStream> getRootResource(String... pElements) {
      return this.getResource(String.join("/", pElements));
   }

   public IoSupplier<InputStream> getResource(PackType pPackType, ResourceLocation pLocation) {
      return this.getResource(getPathFromLocation(pPackType, pLocation));
   }

   @Nullable
   private IoSupplier<InputStream> getResource(String pResourcePath) {
      ZipFile zipfile = this.m_10247_();
      if (zipfile == null) {
         return null;
      } else {
         ZipEntry zipentry = zipfile.getEntry(pResourcePath);
         return zipentry == null ? null : IoSupplier.create(zipfile, zipentry);
      }
   }

   public Set<String> getNamespaces(PackType pType) {
      ZipFile zipfile = this.m_10247_();
      if (zipfile == null) {
         return Set.of();
      } else {
         Enumeration<? extends ZipEntry> enumeration = zipfile.entries();
         Set<String> set = Sets.newHashSet();

         while(enumeration.hasMoreElements()) {
            ZipEntry zipentry = enumeration.nextElement();
            String s = zipentry.getName();
            if (s.startsWith(pType.getDirectory() + "/")) {
               List<String> list = Lists.newArrayList(f_10232_.split(s));
               if (list.size() > 1) {
                  String s1 = list.get(1);
                  if (s1.equals(s1.toLowerCase(Locale.ROOT))) {
                     set.add(s1);
                  } else {
                     LOGGER.warn("Ignored non-lowercase namespace: {} in {}", s1, this.f_243750_);
                  }
               }
            }
         }

         return set;
      }
   }

   protected void finalize() throws Throwable {
      this.close();
      super.finalize();
   }

   public void close() {
      if (this.f_10233_ != null) {
         IOUtils.closeQuietly((Closeable)this.f_10233_);
         this.f_10233_ = null;
      }

   }

   public void listResources(PackType pPackType, String pNamespace, String pPath, PackResources.ResourceOutput pResourceOutput) {
      ZipFile zipfile = this.m_10247_();
      if (zipfile != null) {
         Enumeration<? extends ZipEntry> enumeration = zipfile.entries();
         String s = pPackType.getDirectory() + "/" + pNamespace + "/";
         String s1 = s + pPath + "/";

         while(enumeration.hasMoreElements()) {
            ZipEntry zipentry = enumeration.nextElement();
            if (!zipentry.isDirectory()) {
               String s2 = zipentry.getName();
               if (s2.startsWith(s1)) {
                  String s3 = s2.substring(s.length());
                  ResourceLocation resourcelocation = ResourceLocation.tryBuild(pNamespace, s3);
                  if (resourcelocation != null) {
                     pResourceOutput.accept(resourcelocation, IoSupplier.create(zipfile, zipentry));
                  } else {
                     LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", pNamespace, s3);
                  }
               }
            }
         }

      }
   }
}