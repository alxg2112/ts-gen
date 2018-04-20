package com.alxg2112.tsgen.controller;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import com.alxg2112.tsgen.util.ReportGenerator;
import com.google.common.io.ByteStreams;
import one.util.streamex.EntryStream;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Alexander Gryshchenko
 */
@RestController
public class ReportGenerationEndpoint {

	private static final String SAMPLE_CSV_FILE = "Sample.csv";
	private static final String REPORT_FILENAME_PLACEHOLDER = "%FILENAME%";

	// TODO: generate report filename dynamically
	private static final String REPORT_FILENAME = "YourReportSir.xlsx";

	private final ReportGenerationEndpointProperties properties;
	private final ReportGenerator reportGenerator;

	public ReportGenerationEndpoint(ReportGenerationEndpointProperties properties,
									ReportGenerator reportGenerator) {
		this.properties = properties;
		this.reportGenerator = reportGenerator;
	}

	@RequestMapping(value = "/generate-report", method = RequestMethod.POST)
	public void generateReport(@RequestBody String rawCsv, HttpServletResponse httpServletResponse) throws IOException {
		byte[] reportBytes = reportGenerator.generateReport(rawCsv);

		writeReportToResponse(reportBytes, REPORT_FILENAME, httpServletResponse);
	}

	@RequestMapping(value = "/sample-report", method = RequestMethod.GET)
	public void sampleReport(HttpServletResponse httpServletResponse) throws IOException {
		String rawCsv = Files.readAllLines(Paths.get(SAMPLE_CSV_FILE)).stream()
				.collect(Collectors.joining("\n"));

		byte[] reportBytes = reportGenerator.generateReport(rawCsv);

		writeReportToResponse(reportBytes, REPORT_FILENAME, httpServletResponse);
	}

	private void writeReportToResponse(byte[] reportBytes,
									   String reportFilename,
									   HttpServletResponse httpServletResponse) throws IOException {
		httpServletResponse.setContentType(properties.getResponseContentType());

		EntryStream.of(properties.getResponseHeaders())
				.mapValues(headerValue -> headerValue.replace(REPORT_FILENAME_PLACEHOLDER, reportFilename))
				.forKeyValue(httpServletResponse::setHeader);

		httpServletResponse.setContentLength(reportBytes.length);
		ByteStreams.copy(new ByteArrayInputStream(reportBytes), httpServletResponse.getOutputStream());
	}
}
