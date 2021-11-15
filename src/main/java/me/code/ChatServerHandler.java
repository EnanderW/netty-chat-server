package me.code;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChatServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final static AttributeKey<String> USERNAME = AttributeKey.valueOf("username");

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channels.add(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        byte packetId = buf.readByte();

        if (packetId == 0) { // set name
            String name = readString(buf);

            System.out.println(name);

            ctx.channel().attr(USERNAME).set(name);
            System.out.println(name);
        } else if (packetId == 1) { // message
            String message = readString(buf);
            String name = ctx.channel().attr(USERNAME).get();

            System.out.println("Message: " + name + " - " + message);

            for (Channel channel : channels) {
                ByteBuf writeBuf = Unpooled.buffer();
                writeBuf.writeByte(0);
                writeBuf.writeInt(name.length());
                writeBuf.writeBytes(name.getBytes());
                writeBuf.writeInt(message.length());
                writeBuf.writeBytes(message.getBytes());

                channel.writeAndFlush(writeBuf);
            }
        }
    }

    private static String readString(ByteBuf buf) {
        int length = buf.readInt();

        byte[] content = new byte[length];
        buf.readBytes(content, 0, length);

        return new String(content, 0, length);
    }
}
