package com.alxg2112.tsgen.controller;

import java.util.Map;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Alexander Gryshchenko
 */
@Data
@Component
@ConfigurationProperties("generation.endpoint")
public class ReportGenerationEndpointProperties {

	private String responseContentType;
	private Map<String, String> responseHeaders;

}
