package com.alxg2112.tsgen.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Alexander Gryshchenko
 */
@Data
@Component
@ConfigurationProperties("report.generator")
public class ReportGeneratorProperties {

	private String templateFilename;
	private String reportSheetName;
	private int rowOffset;
	private int columnOffset;
}
