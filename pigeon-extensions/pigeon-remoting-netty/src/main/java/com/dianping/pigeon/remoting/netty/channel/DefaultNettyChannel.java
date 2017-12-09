package com.dianping.pigeon.remoting.netty.channel;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.util.NetUtils;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author qi.yin
 *         2016/09/23  上午10:31.
 */
public class DefaultNettyChannel implements NettyChannel {

    private static final Logger logger = LoggerLoader.getLogger(NettyChannel.class);

    private ReentrantLock connectLock = new ReentrantLock();

    private int timeout;

    private volatile Channel channel;

    private ClientBootstrap bootstrap;

    private InetSocketAddress localAddress;

    private InetSocketAddress remoteAddress;

    private String remoteAddressString;

    public DefaultNettyChannel(ClientBootstrap bootstrap, String remoteHost, int remotePort, int timeout) {
        this.bootstrap = bootstrap;
        this.remoteAddress = new InetSocketAddress(remoteHost, remotePort);
        this.remoteAddressString = NetUtils.toAddress(remoteHost, remotePort);
        this.timeout = timeout;
    }

    @Override
    public void connect() throws NetworkException {
        connectLock.lock();
        try {
            if (isAvaliable()) {
                logger.info("[connect] is connected to remote " + remoteAddress + ".");
                return;
            }

            ChannelFuture future = bootstrap.connect(remoteAddress);

            try {
                if (future.awaitUninterruptibly(timeout, TimeUnit.MILLISECONDS)) {

                    if (future.isSuccess()) {
                        disConnect();
                        this.channel = future.getChannel();
                        localAddress = (InetSocketAddress) this.channel.getLocalAddress();
                    } else {
                        throw new NetworkException("connected to remote " + remoteAddress + " failed.");
                    }
                } else {
                    throw new NetworkException("timeout connecting to remote " + remoteAddress + ".");
                }

            } catch (Throwable e) {
                throw new NetworkException("error connecting to remote " + remoteAddress + ".", e);
            } finally {
                if (!isConnected()) {
                    future.cancel();
                }
            }
        } finally {
            connectLock.unlock();
        }

    }

    @Override
    public void disConnect() {
        connectLock.lock();
        try {
            if (this.channel != null) {
                this.channel.close();
            }
        } catch (Throwable e) {
            logger.error("[disConnect] error disConnecting channel. ", e);
        } finally {
            connectLock.unlock();
        }
    }

    @Override
    public ChannelFuture write0(Object message) throws NetworkException {
        if (!isAvaliable()) {
            throw new NetworkException("[write0] channel is null or channel is close.");
        }

        return channel.write(message);

    }

    @Override
    public void write(Object message) throws NetworkException {
        write0(message);
    }

    private boolean isConnected() {
        if (this.channel != null) {
            return this.channel.isConnected();
        }
        return false;
    }

    @Override
    public boolean isAvaliable() {
        return channel != null && channel.isConnected();
    }

    public boolean isWritable() {
        return channel.isWritable();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public String getRemoteAddressString() {
        return this.remoteAddressString;
    }

    public int getTimeout() {
        return timeout;
    }

    public String toString() {
        return "NettyChannel[avaliable = " + isAvaliable() + "localAddress=" + localAddress.toString() + "remoteAddress= " + remoteAddress.toString() + "]";
    }

}
