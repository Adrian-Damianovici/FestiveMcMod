package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class AdvancementList {
   private static final Logger f_139325_ = LogUtils.getLogger();
   private final Map<ResourceLocation, Advancement> f_139326_ = Maps.newHashMap();
   private final Set<Advancement> f_139327_ = Sets.newLinkedHashSet();
   private final Set<Advancement> f_139328_ = Sets.newLinkedHashSet();
   @Nullable
   private AdvancementList.Listener f_139329_;

   private void m_139339_(Advancement p_139340_) {
      for(Advancement advancement : p_139340_.m_138322_()) {
         this.m_139339_(advancement);
      }

      f_139325_.info("Forgot about advancement {}", (Object)p_139340_.m_138327_());
      this.f_139326_.remove(p_139340_.m_138327_());
      if (p_139340_.m_138319_() == null) {
         this.f_139327_.remove(p_139340_);
         if (this.f_139329_ != null) {
            this.f_139329_.m_5504_(p_139340_);
         }
      } else {
         this.f_139328_.remove(p_139340_);
         if (this.f_139329_ != null) {
            this.f_139329_.m_5516_(p_139340_);
         }
      }

   }

   public void m_139335_(Set<ResourceLocation> p_139336_) {
      for(ResourceLocation resourcelocation : p_139336_) {
         Advancement advancement = this.f_139326_.get(resourcelocation);
         if (advancement == null) {
            f_139325_.warn("Told to remove advancement {} but I don't know what that is", (Object)resourcelocation);
         } else {
            this.m_139339_(advancement);
         }
      }

   }

   public void m_139333_(Map<ResourceLocation, Advancement.Builder> p_139334_) {
      Map<ResourceLocation, Advancement.Builder> map = Maps.newHashMap(p_139334_);

      while(!map.isEmpty()) {
         boolean flag = false;
         Iterator<Map.Entry<ResourceLocation, Advancement.Builder>> iterator = map.entrySet().iterator();

         while(iterator.hasNext()) {
            Map.Entry<ResourceLocation, Advancement.Builder> entry = iterator.next();
            ResourceLocation resourcelocation = entry.getKey();
            Advancement.Builder advancement$builder = entry.getValue();
            if (advancement$builder.m_138392_(this.f_139326_::get)) {
               Advancement advancement = advancement$builder.build(resourcelocation);
               this.f_139326_.put(resourcelocation, advancement);
               flag = true;
               iterator.remove();
               if (advancement.m_138319_() == null) {
                  this.f_139327_.add(advancement);
                  if (this.f_139329_ != null) {
                     this.f_139329_.m_5513_(advancement);
                  }
               } else {
                  this.f_139328_.add(advancement);
                  if (this.f_139329_ != null) {
                     this.f_139329_.m_5505_(advancement);
                  }
               }
            }
         }

         if (!flag) {
            for(Map.Entry<ResourceLocation, Advancement.Builder> entry1 : map.entrySet()) {
               f_139325_.error("Couldn't load advancement {}: {}", entry1.getKey(), entry1.getValue());
            }
            break;
         }
      }

      f_139325_.info("Loaded {} advancements", (int)this.f_139326_.size());
   }

   public void m_139332_() {
      this.f_139326_.clear();
      this.f_139327_.clear();
      this.f_139328_.clear();
      if (this.f_139329_ != null) {
         this.f_139329_.m_7204_();
      }

   }

   public Iterable<Advancement> m_139343_() {
      return this.f_139327_;
   }

   public Collection<Advancement> m_139344_() {
      return this.f_139326_.values();
   }

   @Nullable
   public Advancement m_139337_(ResourceLocation p_139338_) {
      return this.f_139326_.get(p_139338_);
   }

   public void m_139341_(@Nullable AdvancementList.Listener p_139342_) {
      this.f_139329_ = p_139342_;
      if (p_139342_ != null) {
         for(Advancement advancement : this.f_139327_) {
            p_139342_.m_5513_(advancement);
         }

         for(Advancement advancement1 : this.f_139328_) {
            p_139342_.m_5505_(advancement1);
         }
      }

   }

   public interface Listener {
      void m_5513_(Advancement p_139345_);

      void m_5504_(Advancement p_139346_);

      void m_5505_(Advancement p_139347_);

      void m_5516_(Advancement p_139348_);

      void m_7204_();
   }
}