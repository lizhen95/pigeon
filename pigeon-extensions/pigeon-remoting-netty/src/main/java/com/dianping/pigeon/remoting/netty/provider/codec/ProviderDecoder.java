package com.dianping.pigeon.remoting.netty.provider.codec;

import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.netty.codec.AbstractDecoder;
import com.dianping.pigeon.remoting.netty.provider.NettyServerChannel;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

import java.io.InputStream;

/**
 * @author qi.yin
 *         2016/06/21  下午3:34.
 */
public class ProviderDecoder extends AbstractDecoder {

    @Override
    public Object doInitMsg(Object message, Channel channel, long receiveTime) {
        if (message == null) {
            return null;
        }
        InvocationRequest request = (InvocationRequest) message;
        request.setCreateMillisTime(receiveTime);
        return request;
    }

    @Override
    public void doFailResponse(ChannelHandlerContext ctx, Channel channel, InvocationResponse response) {
        NettyServerChannel nettyChannel = new NettyServerChannel(channel);
        nettyChannel.write(null, response);
    }

    @Override
    public Object deserialize(byte serializerType, InputStream is) {
        Object decoded = SerializerFactory.getSerializer(serializerType).deserializeRequest(is);
        return decoded;
    }
}
