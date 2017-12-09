/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.domain;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * 
 */
public class HostInfo {

	private String connect;
	private String host;
	private int port;
	private int weight;
	private String app;
	private String version;
	private byte heartBeatSupport;
	// serviceName --> support new protocol
	private Map<String, Boolean> serviceProtocols;
	// all referenced serviceName
	private Set<String> services = new HashSet<>();

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public HostInfo(String host, int port, int weight) {
		this.host = host;
		this.port = port;
		this.connect = host + ":" + port;
		this.weight = weight;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HostInfo) {
			HostInfo hp = (HostInfo) obj;
			return this.host.equals(hp.host) && this.port == hp.port;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return host.hashCode() + port;
	}

	@Override
	public String toString() {
		return "HostInfo [host=" + host + ", port=" + port + ", weight=" + weight + ", app=" + app + "]";
	}

	public String getConnect() {
		return connect;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public byte getHeartBeatSupport() {
		return heartBeatSupport;
	}

	public void setHeartBeatSupport(byte heartBeatSupport) {
		this.heartBeatSupport = heartBeatSupport;
	}

	public Map<String, Boolean> getServiceProtocols() {
		return serviceProtocols;
	}

	public void setServiceProtocols(Map<String, Boolean> serviceProtocols) {
		this.serviceProtocols = serviceProtocols;
	}

	public void addService(String serviceName) {
		services.add(serviceName);
	}

	public void removeService(String serviceName) {
		services.remove(serviceName);
	}

	public Set<String> getServices() {
		return services;
	}
}
