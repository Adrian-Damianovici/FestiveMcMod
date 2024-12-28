package net.minecraft.client.gui.layouts;

import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LinearLayout extends AbstractLayout {
   private final LinearLayout.Orientation orientation;
   private final List<LinearLayout.ChildContainer> f_263711_ = new ArrayList<>();
   private final LayoutSettings f_263686_ = LayoutSettings.defaults();

   public LinearLayout(int pWidth, int pHeight, LinearLayout.Orientation pOrientation) {
      this(0, 0, pWidth, pHeight, pOrientation);
   }

   public LinearLayout(int p_265489_, int p_265500_, int p_265233_, int p_265301_, LinearLayout.Orientation pOrientation) {
      super(p_265489_, p_265500_, p_265233_, p_265301_);
      this.orientation = pOrientation;
   }

   public void arrangeElements() {
      super.arrangeElements();
      if (!this.f_263711_.isEmpty()) {
         int i = 0;
         int j = this.orientation.m_264137_(this);

         for(LinearLayout.ChildContainer linearlayout$childcontainer : this.f_263711_) {
            i += this.orientation.m_264173_(linearlayout$childcontainer);
            j = Math.max(j, this.orientation.m_264503_(linearlayout$childcontainer));
         }

         int k = this.orientation.m_264056_(this) - i;
         int l = this.orientation.m_264407_(this);
         Iterator<LinearLayout.ChildContainer> iterator = this.f_263711_.iterator();
         LinearLayout.ChildContainer linearlayout$childcontainer1 = iterator.next();
         this.orientation.m_264587_(linearlayout$childcontainer1, l);
         l += this.orientation.m_264173_(linearlayout$childcontainer1);
         LinearLayout.ChildContainer linearlayout$childcontainer2;
         if (this.f_263711_.size() >= 2) {
            for(Divisor divisor = new Divisor(k, this.f_263711_.size() - 1); divisor.hasNext(); l += this.orientation.m_264173_(linearlayout$childcontainer2)) {
               l += divisor.nextInt();
               linearlayout$childcontainer2 = iterator.next();
               this.orientation.m_264587_(linearlayout$childcontainer2, l);
            }
         }

         int i1 = this.orientation.m_264117_(this);

         for(LinearLayout.ChildContainer linearlayout$childcontainer3 : this.f_263711_) {
            this.orientation.m_264630_(linearlayout$childcontainer3, i1, j);
         }

         switch (this.orientation) {
            case HORIZONTAL:
               this.height = j;
               break;
            case VERTICAL:
               this.width = j;
         }

      }
   }

   public void visitChildren(Consumer<LayoutElement> pVisitor) {
      this.f_263711_.forEach((p_265178_) -> {
         pVisitor.accept(p_265178_.child);
      });
   }

   public LayoutSettings m_264453_() {
      return this.f_263686_.copy();
   }

   public LayoutSettings m_264286_() {
      return this.f_263686_;
   }

   public <T extends LayoutElement> T addChild(T pChild) {
      return this.addChild(pChild, this.m_264453_());
   }

   public <T extends LayoutElement> T addChild(T pChild, LayoutSettings pLayoutSettings) {
      this.f_263711_.add(new LinearLayout.ChildContainer(pChild, pLayoutSettings));
      return pChild;
   }

   @OnlyIn(Dist.CLIENT)
   static class ChildContainer extends AbstractLayout.AbstractChildWrapper {
      protected ChildContainer(LayoutElement p_265706_, LayoutSettings p_265131_) {
         super(p_265706_, p_265131_);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Orientation {
      HORIZONTAL,
      VERTICAL;

      int m_264056_(LayoutElement p_265322_) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = p_265322_.getWidth();
               break;
            case VERTICAL:
               i = p_265322_.getHeight();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }

      int m_264173_(LinearLayout.ChildContainer p_265173_) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = p_265173_.getWidth();
               break;
            case VERTICAL:
               i = p_265173_.getHeight();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }

      int m_264137_(LayoutElement p_265570_) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = p_265570_.getHeight();
               break;
            case VERTICAL:
               i = p_265570_.getWidth();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }

      int m_264503_(LinearLayout.ChildContainer p_265345_) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = p_265345_.getHeight();
               break;
            case VERTICAL:
               i = p_265345_.getWidth();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }

      void m_264587_(LinearLayout.ChildContainer p_265660_, int p_265194_) {
         switch (this) {
            case HORIZONTAL:
               p_265660_.setX(p_265194_, p_265660_.getWidth());
               break;
            case VERTICAL:
               p_265660_.setY(p_265194_, p_265660_.getHeight());
         }

      }

      void m_264630_(LinearLayout.ChildContainer p_265536_, int p_265313_, int p_265295_) {
         switch (this) {
            case HORIZONTAL:
               p_265536_.setY(p_265313_, p_265295_);
               break;
            case VERTICAL:
               p_265536_.setX(p_265313_, p_265295_);
         }

      }

      int m_264407_(LayoutElement p_265209_) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = p_265209_.getX();
               break;
            case VERTICAL:
               i = p_265209_.getY();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }

      int m_264117_(LayoutElement p_265676_) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = p_265676_.getY();
               break;
            case VERTICAL:
               i = p_265676_.getX();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }
   }
}