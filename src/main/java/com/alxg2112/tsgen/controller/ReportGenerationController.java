package com.alxg2112.tsgen.controller;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import com.alxg2112.tsgen.util.ReportFilenameGenerator;
import com.alxg2112.tsgen.util.ReportGenerator;
import com.google.common.io.ByteStreams;
import com.google.common.net.UrlEscapers;
import one.util.streamex.EntryStream;
import org.springframework.web.bind.annotation.*;

/**
 * @author Alexander Gryshchenko
 */
@RestController
public class ReportGenerationController {

	private static final String SAMPLE_CSV_FILE = "Sample.csv";
	private static final String REPORT_FILENAME_PLACEHOLDER = "%FILENAME%";
	private static final String XLSX_EXTENSION = "xlsx";

	// TODO: generate report filename dynamically
	private static final String DEFAULT_REPORT_FILENAME = "YourReportSir";

	private final ReportGenerationControllerProperties properties;
	private final ReportGenerator reportGenerator;
	private final ReportFilenameGenerator reportFilenameGenerator;

	public ReportGenerationController(ReportGenerationControllerProperties properties,
									  ReportGenerator reportGenerator,
									  ReportFilenameGenerator reportFilenameGenerator) {
		this.properties = properties;
		this.reportGenerator = reportGenerator;
		this.reportFilenameGenerator = reportFilenameGenerator;
	}

	@RequestMapping(value = "/generate-report", method = RequestMethod.GET)
	public void generateReportFromCsvRequestParam(@RequestParam(name = "raw-csv") String rawCsv,
												  @RequestParam(name = "surname", required = false) String surname,
												  HttpServletResponse httpServletResponse) throws IOException {
		String reportName = surname == null
				? DEFAULT_REPORT_FILENAME
				: reportFilenameGenerator.generateReportFilename(surname);
		writeReportToResponse(rawCsv, reportName, httpServletResponse);
	}

	@Deprecated
	@RequestMapping(value = "/generate-report", method = RequestMethod.POST)
	public void generateReportFromCsvBody(@RequestBody String rawCsv, HttpServletResponse httpServletResponse) throws IOException {
		writeReportToResponse(rawCsv, DEFAULT_REPORT_FILENAME, httpServletResponse);
	}

	@RequestMapping(value = "/sample-report", method = RequestMethod.GET)
	public void sampleReport(HttpServletResponse httpServletResponse) throws IOException {
		String rawCsv = Files.readAllLines(Paths.get(SAMPLE_CSV_FILE)).stream()
				.collect(Collectors.joining("\n"));

		writeReportToResponse(rawCsv, DEFAULT_REPORT_FILENAME, httpServletResponse);
	}

	private void writeReportToResponse(String rawCsv,
									   String reportFilename,
									   HttpServletResponse httpServletResponse) throws IOException {
		byte[] reportBytes = reportGenerator.generateReport(rawCsv);

		httpServletResponse.setContentType(properties.getResponseContentType());

		String filenameWithExtension = reportFilename + '.' + XLSX_EXTENSION;
		String escapedFilenameWithExtension = UrlEscapers.urlFragmentEscaper().escape(filenameWithExtension);

		EntryStream.of(properties.getResponseHeaders())
				.mapValues(headerValue -> headerValue.replace(REPORT_FILENAME_PLACEHOLDER, escapedFilenameWithExtension))
				.forKeyValue(httpServletResponse::setHeader);

		httpServletResponse.setContentLength(reportBytes.length);
		ByteStreams.copy(new ByteArrayInputStream(reportBytes), httpServletResponse.getOutputStream());
	}
}
