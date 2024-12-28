package com.mojang.realmsclient.client;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsError {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String f_87296_;
   private final int f_87297_;

   private RealmsError(String p_87300_, int p_87301_) {
      this.f_87296_ = p_87300_;
      this.f_87297_ = p_87301_;
   }

   @Nullable
   public static RealmsError parse(String pPayload) {
      if (Strings.isNullOrEmpty(pPayload)) {
         return null;
      } else {
         try {
            JsonObject jsonobject = JsonParser.parseString(pPayload).getAsJsonObject();
            String s = JsonUtils.getStringOr("errorMsg", jsonobject, "");
            int i = JsonUtils.getIntOr("errorCode", jsonobject, -1);
            return new RealmsError(s, i);
         } catch (Exception exception) {
            LOGGER.error("Could not parse RealmsError: {}", (Object)exception.getMessage());
            LOGGER.error("The error was: {}", (Object)pPayload);
            return null;
         }
      }
   }

   public String m_87302_() {
      return this.f_87296_;
   }

   public int m_87305_() {
      return this.f_87297_;
   }
}