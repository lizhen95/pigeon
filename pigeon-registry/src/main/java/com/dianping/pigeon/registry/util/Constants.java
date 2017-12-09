/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.util;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;

public final class Constants {

	public static final String CHARSET = "UTF-8";
	public static final String DP_PATH = "/DP";
	public static final String CONFIG_PATH = "/DP/CONFIG";
	public static final String CONFIG_TIMESTAMP = "TIMESTAMP";
	public static final String SERVICE_PATH = "/DP/SERVER";
	public static final String WEIGHT_PATH = "/DP/WEIGHT";
	public static final String APP_PATH = "/DP/APP";
	public static final String VERSION_PATH = "/DP/VERSION";
	public static final String TOKEN_PATH = "/DP/TOKEN";
	public static final String HEARTBEAT_PATH = "/DP/HEARTBEAT";
	public static final String PROTOCOL_PATH = "/DP/PROTOCOL";
	public static final String CONSOLE_PATH="/DP/CONSOLE";
	public static final String HOST_CONFIG_PATH = "/pigeon/config";
	public static final String PATH_SEPARATOR = "/";
	public static final String PLACEHOLDER = "^";

	public static final String KEY_GROUP = "swimlane";
	public static final String DEFAULT_GROUP = "";
	public static final String KEY_WEIGHT = "weight";
	public static final int MIN_WEIGHT = 0;
	public static final int MAX_WEIGHT = 100;
	public static final String KEY_AUTO_REGISTER = "auto.register";
	public static final String DEFAULT_AUTO_REGISTER = "true";
	public static final boolean DEFAULT_AUTO_REGISTER_BOOL = Boolean.parseBoolean(DEFAULT_AUTO_REGISTER);
	public static final String KEY_LOCAL_IP = "local.ip";

	public static final String REGISTRY_MNS_NAME = "mns";
	public static final String REGISTRY_CURATOR_NAME = "curator";
	public static final String REGISTRY_COMPOSITE_NAME = "composite";
	public static final String REGISTRY_MIX_NAME = "mix";

	private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	public static final String KEY_WEIGHT_DEFAULT = "pigeon.weight.default";
	public static final int WEIGHT_ON = 1;
	public static final int DEFAULT_WEIGHT = configManager.getIntValue(KEY_WEIGHT_DEFAULT, WEIGHT_ON);
}
