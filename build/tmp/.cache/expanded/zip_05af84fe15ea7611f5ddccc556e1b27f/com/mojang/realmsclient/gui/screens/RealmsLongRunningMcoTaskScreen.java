package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.gui.ErrorCallback;
import com.mojang.realmsclient.util.task.LongRunningTask;
import java.time.Duration;
import javax.annotation.Nullable;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RepeatedNarrator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsLongRunningMcoTaskScreen extends RealmsScreen implements ErrorCallback {
   private static final RepeatedNarrator REPEATED_NARRATOR = new RepeatedNarrator(Duration.ofSeconds(5L));
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Screen lastScreen;
   private volatile Component title = CommonComponents.EMPTY;
   @Nullable
   private volatile Component f_88770_;
   private volatile boolean f_88771_;
   private int f_88772_;
   private final LongRunningTask task;
   private final int f_88774_ = 212;
   private Button f_167413_;
   public static final String[] f_88766_ = new String[]{"\u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583", "_ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584", "_ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585", "_ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586", "_ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587", "_ _ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588", "_ _ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587", "_ _ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586", "_ _ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585", "_ \u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584", "\u2583 \u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583", "\u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _", "\u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _", "\u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _", "\u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _", "\u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _ _", "\u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _ _", "\u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _ _", "\u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _ _", "\u2584 \u2585 \u2586 \u2587 \u2588 \u2587 \u2586 \u2585 \u2584 \u2583 _"};

   public RealmsLongRunningMcoTaskScreen(Screen pLastScreen, LongRunningTask pTask) {
      super(GameNarrator.NO_TITLE);
      this.lastScreen = pLastScreen;
      this.task = pTask;
      pTask.m_90400_(this);
      Thread thread = new Thread(pTask, "Realms-long-running-task");
      thread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
      thread.start();
   }

   public void tick() {
      super.tick();
      REPEATED_NARRATOR.narrate(this.minecraft.getNarrator(), this.title);
      ++this.f_88772_;
      this.task.tick();
   }

   /**
    * Called when a keyboard key is pressed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the pressed key.
    * @param pScanCode the scan code of the pressed key.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (pKeyCode == 256) {
         this.m_88799_();
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }
   }

   public void init() {
      this.task.init();
      this.f_167413_ = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_88795_) -> {
         this.m_88799_();
      }).bounds(this.width / 2 - 106, row(12), 212, 20).build());
   }

   private void m_88799_() {
      this.f_88771_ = true;
      this.task.abortTask();
      this.minecraft.setScreen(this.lastScreen);
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics p_282789_, int p_88786_, int p_88787_, float p_88788_) {
      this.renderBackground(p_282789_);
      p_282789_.drawCenteredString(this.font, this.title, this.width / 2, row(3), 16777215);
      Component component = this.f_88770_;
      if (component == null) {
         p_282789_.drawCenteredString(this.font, f_88766_[this.f_88772_ % f_88766_.length], this.width / 2, row(8), 8421504);
      } else {
         p_282789_.drawCenteredString(this.font, component, this.width / 2, row(8), 16711680);
      }

      super.render(p_282789_, p_88786_, p_88787_, p_88788_);
   }

   public void m_5673_(Component p_88792_) {
      this.f_88770_ = p_88792_;
      this.minecraft.getNarrator().sayNow(p_88792_);
      this.minecraft.execute(() -> {
         this.removeWidget(this.f_167413_);
         this.f_167413_ = this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (p_88790_) -> {
            this.m_88799_();
         }).bounds(this.width / 2 - 106, this.height / 4 + 120 + 12, 200, 20).build());
      });
   }

   public void setTitle(Component pTitle) {
      this.title = pTitle;
   }

   public boolean m_88779_() {
      return this.f_88771_;
   }
}