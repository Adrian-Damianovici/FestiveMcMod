package net.minecraft.util;

public class FrameTimer {
   public static final int f_144731_ = 240;
   private final long[] f_13749_ = new long[240];
   private int f_13750_;
   private int f_13751_;
   private int f_13752_;

   public void m_13755_(long p_13756_) {
      this.f_13749_[this.f_13752_] = p_13756_;
      ++this.f_13752_;
      if (this.f_13752_ == 240) {
         this.f_13752_ = 0;
      }

      if (this.f_13751_ < 240) {
         this.f_13750_ = 0;
         ++this.f_13751_;
      } else {
         this.f_13750_ = this.m_13762_(this.f_13752_ + 1);
      }

   }

   public long m_144732_(int p_144733_) {
      int i = (this.f_13750_ + p_144733_) % 240;
      int j = this.f_13750_;

      long k;
      for(k = 0L; j != i; ++j) {
         k += this.f_13749_[j];
      }

      return k / (long)p_144733_;
   }

   public int m_144734_(int p_144735_, int p_144736_) {
      return this.m_13757_(this.m_144732_(p_144735_), p_144736_, 60);
   }

   public int m_13757_(long p_13758_, int p_13759_, int p_13760_) {
      double d0 = (double)p_13758_ / (double)(1000000000L / (long)p_13760_);
      return (int)(d0 * (double)p_13759_);
   }

   public int m_13754_() {
      return this.f_13750_;
   }

   public int m_13761_() {
      return this.f_13752_;
   }

   public int m_13762_(int p_13763_) {
      return p_13763_ % 240;
   }

   public long[] m_13764_() {
      return this.f_13749_;
   }
}