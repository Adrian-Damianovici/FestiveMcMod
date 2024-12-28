package com.mojang.realmsclient.exception;

import com.mojang.realmsclient.client.RealmsError;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsServiceException extends Exception {
   public final int f_87773_;
   public final String f_200940_;
   @Nullable
   public final RealmsError realmsError;

   public RealmsServiceException(int p_87783_, String p_87784_, RealmsError p_87785_) {
      super(p_87784_);
      this.f_87773_ = p_87783_;
      this.f_200940_ = p_87784_;
      this.realmsError = p_87785_;
   }

   public RealmsServiceException(int p_200943_, String p_200944_) {
      super(p_200944_);
      this.f_87773_ = p_200943_;
      this.f_200940_ = p_200944_;
      this.realmsError = null;
   }

   public String getMessage() {
      if (this.realmsError != null) {
         String s = "mco.errorMessage." + this.realmsError.m_87305_();
         String s1 = I18n.exists(s) ? I18n.get(s) : this.realmsError.m_87302_();
         return String.format(Locale.ROOT, "Realms service error (%d/%d) %s", this.f_87773_, this.realmsError.m_87305_(), s1);
      } else {
         return String.format(Locale.ROOT, "Realms service error (%d) %s", this.f_87773_, this.f_200940_);
      }
   }

   public int m_200945_(int p_200946_) {
      return this.realmsError != null ? this.realmsError.m_87305_() : p_200946_;
   }
}