package com.alxg2112.tsgen.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Alexander Gryshchenko
 */
@Log4j2
@Component
public class ReportGenerator {

	private static final Splitter LF_SPLITTER = Splitter.on('\n');
	private static final Splitter CSV_COMMA_SPLITTER = Splitter.on(',');

	private final ReportGeneratorProperties properties;

	@Autowired
	public ReportGenerator(ReportGeneratorProperties properties) {
		this.properties = properties;
	}

	public byte[] generateReport(String rawCsvContent) throws IOException {
		List<String> csvLines = LF_SPLITTER.omitEmptyStrings().splitToList(rawCsvContent);

		List<List<String>> csvContent = csvLines.stream()
				.map(CSV_COMMA_SPLITTER::split)
				.map(Lists::newArrayList)
				.collect(Collectors.toList());

		FileInputStream excelTemplateFile = new FileInputStream(new File(properties.getTemplateFilename()));
		try (Workbook workbook = new XSSFWorkbook(excelTemplateFile)) {
			Sheet tsSheet = workbook.getSheet(properties.getReportSheetName());

			Iterator<Row> rowIterator = tsSheet.iterator();

			skipFirst(rowIterator, properties.getRowOffset());

			Iterator<List<String>> csvContentIterator = csvContent.iterator();

			while (rowIterator.hasNext() && csvContentIterator.hasNext()) {

				Row currentRow = rowIterator.next();
				Iterator<Cell> cellIterator = currentRow.cellIterator();

				skipFirst(cellIterator, properties.getColumnOffset());

				List<String> csvRow = csvContentIterator.next();
				Iterator<String> csvValueIterator = csvRow.iterator();

				while (cellIterator.hasNext() && csvValueIterator.hasNext()) {
					Cell currentCell = cellIterator.next();
					String csvValue = csvValueIterator.next();
					currentCell.setCellValue(csvValue);
				}
			}

			byte[] reportBytes = new byte[0];
			try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
				workbook.write(outputStream);
				reportBytes = outputStream.toByteArray();
			} catch (IOException e) {
				LOGGER.warn("Cannot generate report.", e);
			}
			return reportBytes;
		}
	}

	private void skipFirst(Iterator<?> iterator, int elementsToSkip) {
		int remainingToSkip = elementsToSkip;
		while (iterator.hasNext() && remainingToSkip-- > 0) {
			iterator.next();
		}
	}
}
