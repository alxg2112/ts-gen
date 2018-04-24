package com.alxg2112.tsgen.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Alexander Gryshchenko
 */
@Log4j2
@Component
public class ReportGenerator {

	private static final Splitter UNIFIED_LINE_SPLITTER = Splitter.on(Pattern.compile("\r|\n|\r\n"));
	private static final Splitter COMMA_SPLITTER = Splitter.on(',');

	private final ReportGeneratorProperties properties;

	@Autowired
	public ReportGenerator(ReportGeneratorProperties properties) {
		this.properties = properties;
	}

	public byte[] generateReport(String rawCsvContent) throws IOException {
		List<String> csvLines = UNIFIED_LINE_SPLITTER.omitEmptyStrings().splitToList(rawCsvContent);

		List<List<String>> csvContent = csvLines.stream()
				.map(COMMA_SPLITTER::split)
				.map(Lists::newArrayList)
				.collect(Collectors.toList());

		FileInputStream excelTemplateFile = new FileInputStream(new File(properties.getTemplateFilename()));
		try (Workbook workbook = new XSSFWorkbook(excelTemplateFile)) {
			Sheet reportSheet = workbook.getSheet(properties.getReportSheetName());

			Iterator<Row> rowIterator = reportSheet.iterator();

			skipNElements(rowIterator, properties.getRowOffset());

			Iterator<List<String>> csvContentIterator = csvContent.iterator();

			while (rowIterator.hasNext() && csvContentIterator.hasNext()) {

				Row currentRow = rowIterator.next();
				Iterator<Cell> cellIterator = currentRow.cellIterator();

				skipNElements(cellIterator, properties.getColumnOffset());

				List<String> csvRow = csvContentIterator.next();
				Iterator<String> csvValueIterator = csvRow.iterator();

				while (cellIterator.hasNext() && csvValueIterator.hasNext()) {
					Cell currentCell = cellIterator.next();
					reportSheet.autoSizeColumn(currentCell.getColumnIndex());
					String csvValue = csvValueIterator.next();
					currentCell.setCellValue(csvValue);
				}
			}

			XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);

			byte[] reportBytes;
			try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
				workbook.write(outputStream);
				reportBytes = outputStream.toByteArray();
			}
			return reportBytes;
		}
	}

	private void skipNElements(Iterator<?> iterator, int elementsToSkip) {
		int remainingToSkip = elementsToSkip;
		while (iterator.hasNext() && remainingToSkip-- > 0) {
			iterator.next();
		}
	}
}
