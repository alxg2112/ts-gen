package com.alxg2112.tsgen.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

			int currentRowNumber = properties.getRowOffset();
			for (List<String> csvRow : csvContent) {

				Row currentRow = reportSheet.getRow(currentRowNumber);
				if (currentRow == null) {
					currentRow = reportSheet.createRow(currentRowNumber);
				}

				int currentCellNumber = properties.getColumnOffset();
				for (String csvValue : csvRow) {

					Cell currentCell = currentRow.getCell(currentCellNumber);
					if (currentCell == null) {
						currentCell = currentRow.createCell(currentCellNumber);
					}

					reportSheet.autoSizeColumn(currentCell.getColumnIndex());
					currentCell.setCellValue(csvValue);

					currentCellNumber++;
				}

				currentRowNumber++;
			}

			int lastRowNum = reportSheet.getLastRowNum();
			for (int rowPtr = currentRowNumber; rowPtr <= lastRowNum; rowPtr++) {
				Row redundantRow = reportSheet.getRow(rowPtr);
				if (redundantRow != null) {
					reportSheet.removeRow(redundantRow);
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
}
