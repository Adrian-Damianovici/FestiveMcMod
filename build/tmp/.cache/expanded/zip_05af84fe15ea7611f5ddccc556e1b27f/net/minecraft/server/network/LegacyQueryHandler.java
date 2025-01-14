package net.minecraft.server.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

public class LegacyQueryHandler extends ChannelInboundHandlerAdapter {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int f_143586_ = 127;
   private final ServerConnectionListener f_9676_;

   public LegacyQueryHandler(ServerConnectionListener p_9679_) {
      this.f_9676_ = p_9679_;
   }

   public void channelRead(ChannelHandlerContext pContext, Object pMessage) {
      ByteBuf bytebuf = (ByteBuf)pMessage;
      bytebuf.markReaderIndex();
      boolean flag = true;

      try {
         if (bytebuf.readUnsignedByte() == 254) {
            InetSocketAddress inetsocketaddress = (InetSocketAddress)pContext.channel().remoteAddress();
            MinecraftServer minecraftserver = this.f_9676_.getServer();
            int i = bytebuf.readableBytes();
            switch (i) {
               case 0:
                  LOGGER.debug("Ping: (<1.3.x) from {}:{}", inetsocketaddress.getAddress(), inetsocketaddress.getPort());
                  String s2 = String.format(Locale.ROOT, "%s\u00a7%d\u00a7%d", minecraftserver.getMotd(), minecraftserver.m_7416_(), minecraftserver.m_7418_());
                  this.sendFlushAndClose(pContext, this.m_9683_(s2));
                  break;
               case 1:
                  if (bytebuf.readUnsignedByte() != 1) {
                     return;
                  }

                  LOGGER.debug("Ping: (1.4-1.5.x) from {}:{}", inetsocketaddress.getAddress(), inetsocketaddress.getPort());
                  String s = String.format(Locale.ROOT, "\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, minecraftserver.m_7630_(), minecraftserver.getMotd(), minecraftserver.m_7416_(), minecraftserver.m_7418_());
                  this.sendFlushAndClose(pContext, this.m_9683_(s));
                  break;
               default:
                  boolean flag1 = bytebuf.readUnsignedByte() == 1;
                  flag1 &= bytebuf.readUnsignedByte() == 250;
                  flag1 &= "MC|PingHost".equals(new String(bytebuf.readBytes(bytebuf.readShort() * 2).array(), StandardCharsets.UTF_16BE));
                  int j = bytebuf.readUnsignedShort();
                  flag1 &= bytebuf.readUnsignedByte() >= 73;
                  flag1 &= 3 + bytebuf.readBytes(bytebuf.readShort() * 2).array().length + 4 == j;
                  flag1 &= bytebuf.readInt() <= 65535;
                  flag1 &= bytebuf.readableBytes() == 0;
                  if (!flag1) {
                     return;
                  }

                  LOGGER.debug("Ping: (1.6) from {}:{}", inetsocketaddress.getAddress(), inetsocketaddress.getPort());
                  String s1 = String.format(Locale.ROOT, "\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, minecraftserver.m_7630_(), minecraftserver.getMotd(), minecraftserver.m_7416_(), minecraftserver.m_7418_());
                  ByteBuf bytebuf1 = this.m_9683_(s1);

                  try {
                     this.sendFlushAndClose(pContext, bytebuf1);
                  } finally {
                     bytebuf1.release();
                  }
            }

            bytebuf.release();
            flag = false;
            return;
         }
      } catch (RuntimeException runtimeexception) {
         return;
      } finally {
         if (flag) {
            bytebuf.resetReaderIndex();
            pContext.channel().pipeline().remove("legacy_query");
            pContext.fireChannelRead(pMessage);
         }

      }

   }

   private void sendFlushAndClose(ChannelHandlerContext pContext, ByteBuf pBuffer) {
      pContext.pipeline().firstContext().writeAndFlush(pBuffer).addListener(ChannelFutureListener.CLOSE);
   }

   private ByteBuf m_9683_(String p_9684_) {
      ByteBuf bytebuf = Unpooled.buffer();
      bytebuf.writeByte(255);
      char[] achar = p_9684_.toCharArray();
      bytebuf.writeShort(achar.length);

      for(char c0 : achar) {
         bytebuf.writeChar(c0);
      }

      return bytebuf;
   }
}