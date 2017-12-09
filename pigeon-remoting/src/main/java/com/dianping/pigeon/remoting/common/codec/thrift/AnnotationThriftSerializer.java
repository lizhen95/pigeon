package com.dianping.pigeon.remoting.common.codec.thrift;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;

import com.dianping.pigeon.remoting.common.codec.thrift.annotation.ThriftClientMetadata;
import com.dianping.pigeon.remoting.common.codec.thrift.annotation.ThriftMethodHandler;
import com.dianping.pigeon.remoting.common.codec.thrift.annotation.ThriftMethodProcessor;
import com.dianping.pigeon.remoting.common.codec.thrift.annotation.ThriftServerMetadata;
import com.dianping.pigeon.remoting.common.domain.generic.GenericRequest;
import com.dianping.pigeon.remoting.common.domain.generic.GenericResponse;
import com.dianping.pigeon.remoting.common.domain.generic.StatusCode;
import com.dianping.pigeon.remoting.common.domain.generic.ThriftMapper;
import com.dianping.pigeon.remoting.common.domain.generic.thrift.Header;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.util.ClassUtils;

/**
 * @author qi.yin 2016/05/23 下午4:28.
 */
public class AnnotationThriftSerializer extends AbstractThriftSerializer {

	private ConcurrentMap<String, ThriftClientMetadata> clientMetadatas = new ConcurrentHashMap<String, ThriftClientMetadata>();

	private ConcurrentMap<String, ThriftServerMetadata> serverMetadatas = new ConcurrentHashMap<String, ThriftServerMetadata>();

	@Override
	protected void doDeserializeRequest(GenericRequest request, TProtocol protocol) throws Exception {
		// body
		TMessage message = protocol.readMessageBegin();

		ThriftMethodProcessor methodProcessor = getMethodProcessor(request.getServiceInterface().getName(),
				message.name);

		if (methodProcessor == null) {
			throw new SerializationException("@ThriftMethod annotation is required for " + request.getServiceInterface().getName()
					+ "#" + message.name);
		}
		
		Object[] parameters = methodProcessor.readArguments(protocol);

		request.setSeqId(message.seqid);
		request.setMethodName(message.name);
		request.setParameters(parameters);

		protocol.readMessageEnd();
	}

	protected void doSerializeRequest(GenericRequest request, TProtocol protocol) throws Exception {

		ThriftMethodHandler methodHandler = getMethodHandler(request.getServiceInterface().getName(),
				request.getMethodName());
		if (methodHandler == null) {
			throw new SerializationException("@ThriftMethod annotation is required for " + request.getServiceInterface().getName()
					+ "#" + request.getMethodName());
		}
		// body
		methodHandler.writeArguments(protocol, getSequenceId(), request.getParameters());
	}

	public void doDeserializeResponse(GenericResponse response, GenericRequest request, TProtocol protocol,
			Header header) throws Exception {
		// body
		TMessage message = protocol.readMessageBegin();

		ThriftMethodHandler methodHandler = getMethodHandler(request.getServiceInterface().getName(), message.name);
		response.setSeqId(message.seqid);

		// body
		if (message.type == TMessageType.REPLY) {
			Object result = null;
			try {
				result = methodHandler.readResponse(protocol);
			} catch (TApplicationException e) {
				if (e.getType() == TApplicationException.MISSING_RESULT) {
					result = null;
				}
			}
			response.setReturn(result);
		} else if (message.type == TMessageType.EXCEPTION) {
			TApplicationException exception = TApplicationException.read(protocol);
			ThriftMapper.mapException(header, response, exception.getMessage());
		}

		protocol.readMessageEnd();
	}

	protected void doSerializeResponse(GenericResponse response, TProtocol protocol, Header header,
			DynamicByteArrayOutputStream bos) throws Exception {

		ThriftMethodProcessor methodProcessor = getMethodProcessor(response.getServiceInterface().getName(),
				response.getMethodName());

		TApplicationException applicationException = null;
		TMessage message;

		boolean isUserException = false;

		if (response.hasException()) {

			if (methodProcessor.isUserException(response.getReturn())) {
				header.responseInfo.setStatus(StatusCode.ApplicationException.getCode());
				isUserException = true;
			} else {
				applicationException = new TApplicationException(((Throwable) response.getReturn()).getMessage());
			}

		}

		if (applicationException != null) {
			message = new TMessage(response.getMethodName(), TMessageType.EXCEPTION, response.getSeqId());
		} else {
			message = new TMessage(response.getMethodName(), TMessageType.REPLY, response.getSeqId());
		}

		// header
		header.write(protocol);

		short headerLength = (short) (bos.size() - HEADER_FIELD_LENGTH);

		protocol.writeMessageBegin(message);
		switch (message.type) {
		case TMessageType.EXCEPTION:
			applicationException.write(protocol);
			break;
		case TMessageType.REPLY:
			methodProcessor.writeResponse(protocol, response.getReturn(), isUserException);
			break;
		}

		protocol.writeMessageEnd();
		protocol.getTransport().flush();

		int messageLength = bos.size();

		try {
			bos.setWriteIndex(0);
			protocol.writeI16(headerLength);
		} finally {
			bos.setWriteIndex(messageLength);
		}
	}

	private ThriftMethodProcessor getMethodProcessor(String serviceName, String methodName)
			throws ClassNotFoundException {

		ThriftServerMetadata serverMetadata = serverMetadatas.get(serviceName);

		if (serverMetadata == null) {
			Class<?> serverType = ClassUtils.loadClass(serviceName);

			serverMetadata = new ThriftServerMetadata(serverType, serviceName);
			serverMetadatas.putIfAbsent(serviceName, serverMetadata);
		}

		return serverMetadata.getMethodProcessor(methodName);
	}

	private ThriftMethodHandler getMethodHandler(String serviceName, String methodName) throws ClassNotFoundException {

		ThriftClientMetadata clientMetadata = clientMetadatas.get(serviceName);

		if (clientMetadata == null) {
			Class<?> serverType = ClassUtils.loadClass(serviceName);

			clientMetadata = new ThriftClientMetadata(serverType, serviceName);
			clientMetadatas.putIfAbsent(serviceName, clientMetadata);
		}

		return clientMetadata.getMethodHandler(methodName);
	}
}
