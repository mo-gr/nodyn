/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nodyn.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.nodyn.netty.EOFEventHandler;
import io.nodyn.netty.UnrefHandler;
import io.nodyn.NodeProcess;
import io.nodyn.stream.StreamWrap;

import java.net.SocketAddress;

/**
 * @author Bob McWhirter
 */
public class TCPWrap extends StreamWrap {

    private String addr;
    private int port = -1;

    public TCPWrap(NodeProcess process) {
        super(process, false);
    }

    public TCPWrap(NodeProcess process, ChannelFuture channelFuture) {
        super(process, channelFuture);
    }

    public void bind(String addr, int port) {
        this.addr = addr;
        this.port = port;
    }

    public void listen(int backlog) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(getEventLoopGroup());
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.config().setAutoRead(false);
                //ch.pipeline().addLast("debug", new DebugHandler("server"));
                ch.pipeline().addLast("emit.connection", new ConnectionEventHandler(TCPWrap.this.process, TCPWrap.this));
                ch.pipeline().addLast("handle", new UnrefHandler(TCPWrap.this));
            }
        });
        this.channelFuture = bootstrap.bind(this.addr, this.port);
        this.channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                // TODO callback error
            }
        });
        ref();
    }

    public void connect(String addr, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(getEventLoopGroup());
        bootstrap.channel(NioSocketChannel.class);
        if (this.port >= 0) {
            if (this.addr != null) {
                bootstrap.localAddress(this.addr, this.port);
            } else {
                bootstrap.localAddress(this.port);
            }
        }

        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.config().setAutoRead(false);
                //ch.pipeline().addLast("debug", new DebugHandler("client"));
                ch.pipeline().addLast("emit.afterConnect", new AfterConnectEventHandler(TCPWrap.this.process, TCPWrap.this));
                ch.pipeline().addLast("emit.eof", new EOFEventHandler(TCPWrap.this.process, TCPWrap.this));
                ch.pipeline().addLast("handle", new UnrefHandler(TCPWrap.this));
            }
        });

        this.channelFuture = bootstrap.connect(addr, port);
        this.channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                // TODO callback error
            }
        });
        ref();
    }

    @Override
    public void shutdown() {
        ((NioSocketChannel) this.channelFuture.channel()).shutdownOutput();
    }

    public SocketAddress getRemoteAddress() {
        return this.channelFuture.channel().remoteAddress();
    }

    public SocketAddress getLocalAddress() {
        return this.channelFuture.channel().localAddress();
    }

}
