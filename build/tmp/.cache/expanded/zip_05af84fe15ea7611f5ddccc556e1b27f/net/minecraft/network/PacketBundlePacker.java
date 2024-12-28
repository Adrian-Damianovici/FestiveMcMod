package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

public class PacketBundlePacker extends MessageToMessageDecoder<Packet<?>> {
   @Nullable
   private BundlerInfo.Bundler currentBundler;
   @Nullable
   private BundlerInfo infoForCurrentBundler;
   private final PacketFlow f_263798_;

   public PacketBundlePacker(PacketFlow p_265129_) {
      this.f_263798_ = p_265129_;
   }

   protected void decode(ChannelHandlerContext pContext, Packet<?> pPacket, List<Object> p_265368_) throws Exception {
      BundlerInfo.Provider bundlerinfo$provider = pContext.channel().attr(BundlerInfo.f_263730_).get();
      if (bundlerinfo$provider == null) {
         throw new DecoderException("Bundler not configured: " + pPacket);
      } else {
         BundlerInfo bundlerinfo = bundlerinfo$provider.m_264121_(this.f_263798_);
         if (this.currentBundler != null) {
            if (this.infoForCurrentBundler != bundlerinfo) {
               throw new DecoderException("Bundler handler changed during bundling");
            }

            Packet<?> packet = this.currentBundler.addPacket(pPacket);
            if (packet != null) {
               this.infoForCurrentBundler = null;
               this.currentBundler = null;
               p_265368_.add(packet);
            }
         } else {
            BundlerInfo.Bundler bundlerinfo$bundler = bundlerinfo.startPacketBundling(pPacket);
            if (bundlerinfo$bundler != null) {
               this.currentBundler = bundlerinfo$bundler;
               this.infoForCurrentBundler = bundlerinfo;
            } else {
               p_265368_.add(pPacket);
            }
         }

      }
   }
}