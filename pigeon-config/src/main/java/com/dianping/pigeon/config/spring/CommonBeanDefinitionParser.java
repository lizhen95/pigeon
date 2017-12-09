/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.config.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class CommonBeanDefinitionParser implements BeanDefinitionParser {

	/** Default placeholder prefix: "${" */
	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
	/** Default placeholder suffix: "}" */
	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	private final Class<?> beanClass;

	private final boolean required;

	public CommonBeanDefinitionParser(Class<?> beanClass, boolean required) {
		this.beanClass = beanClass;
		this.required = required;
	}

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		return parse(element, parserContext, beanClass, required);
	}

	private static BeanDefinition parse(Element element, ParserContext parserContext, Class<?> beanClass,
			boolean required) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		// beanDefinition.setBeanClass(beanClass);
		beanDefinition.setLazyInit(false);
		String id = element.getAttribute("id");
		parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

		return beanDefinition;
	}

}