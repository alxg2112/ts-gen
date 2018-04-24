package com.alxg2112.tsgen.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

/**
 * @author Alexander Gryshchenko
 */
@Component
public class ReportFilenameGenerator {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM");

	private static final String DATE_PLACEHOLDER = "%DATE_TIME%";
	private static final String SURNAME_PLACEHOLDER = "%SURNAME%";

	private static final String REPORT_FILENAME_TEMPLATE = DATE_PLACEHOLDER + " - ТЗ " + SURNAME_PLACEHOLDER;

	public String generateReportFilename(String surname) {
		LocalDate localDate = LocalDate.now();
		String formattedDate = localDate.format(DATE_TIME_FORMATTER);
		return REPORT_FILENAME_TEMPLATE
				.replace(DATE_PLACEHOLDER, formattedDate)
				.replace(SURNAME_PLACEHOLDER, surname);
	}
}
