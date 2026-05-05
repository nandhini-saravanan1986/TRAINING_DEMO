package com.bornfire.brf.services;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.transaction.Transactional;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

@Service
@Transactional
public class BRRS_BG_DBS10_FINCON_III_1B_ReportServices {

	private static final Logger logger = LoggerFactory.getLogger(BRRS_BG_DBS10_FINCON_III_1B_ReportServices.class);

	@Autowired
	private Environment env;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy");

	// Summary view
	public ModelAndView getBG_FINCON_1B_View(String reportId, String fromdate, String todate, String currency,
			String dtltype, Pageable pageable, String type, BigDecimal version) {

		ModelAndView mv = new ModelAndView();

		System.out.println("Fincon 1B CALLED");
		System.out.println("Type = " + type);
		System.out.println("Version = " + version);

		// =====================================================
		// ARCHIVAL MODE
		// =====================================================

		if ("ARCHIVAL".equals(type) && version != null) {

			List<BRRS_BG_DBIS10_FINCON_III_1B_Archival_ST_Entity> T1Master = new ArrayList<>();

			try {
				Date dt = dateformat.parse(todate);

				// ============================
				// SUMMARY ARCHIVAL
				// ============================
				T1Master = getdatabydateListarchival(dt, version);

				System.out.println("Archival Summary size = " + T1Master.size());

				mv.addObject("report_date", dateformat.format(dt));

			} catch (Exception e) {
				e.printStackTrace();
			}

			mv.addObject("reportsummary", T1Master);

		}
		// =====================================================
		// NORMAL MODE
		// =====================================================

		else {

			List<BRRS_BG_DBIS10_FINCON_III_1B_ST_Entity> T1Master = new ArrayList<>();

			try {
				Date dt = dateformat.parse(todate);

				// SUMMARY NORMAL
				T1Master = getDataByDate(dt);

				System.out.println("Summary size = " + T1Master.size());

				mv.addObject("report_date", dateformat.format(dt));

			} catch (Exception e) {
				e.printStackTrace();
			}

			mv.addObject("reportsummary", T1Master);
		}

		// =====================================================
		// VIEW SETTINGS
		// =====================================================

		mv.setViewName("BRRS/BG_FINCON_1B");
		mv.addObject("displaymode", "summary");

		System.out.println("View Loaded: " + mv.getViewName());

		return mv;
	}

	// Detail view
	public ModelAndView getFincon_1B_currentDtl(String reportId, String fromdate, String todate, String currency,
			String dtltype, Pageable pageable, String filter, String type, String version) {

		ModelAndView mv = new ModelAndView();

		try {

			Date parsedDate = null;

			if (todate != null && !todate.isEmpty()) {
				parsedDate = dateformat.parse(todate);
			}

			String reportLabel = null;
			String reportAddlCriteria1 = null;

			if (filter != null && filter.contains(",")) {
				String[] parts = filter.split(",");
				if (parts.length >= 2) {
					reportLabel = parts[0];
					reportAddlCriteria1 = parts[1];
				}
			}

			// =====================================================
			// ARCHIVAL MODE
			// =====================================================
			if ("ARCHIVAL".equals(type) && version != null) {

				System.out.println("ARCHIVAL DETAIL MODE");

				List<Fincon_1B_Archival_Detail_Entity> archivalDetailList;

				if (reportLabel != null && reportAddlCriteria1 != null) {

					archivalDetailList = GetArchivalDataByRowIdAndColumnId(reportLabel, reportAddlCriteria1, parsedDate,
							version);

				} else {

					archivalDetailList = getArchivalDetaildatabydateList(parsedDate, version);
				}

				mv.addObject("reportdetails", archivalDetailList);
				mv.addObject("reportmaster12", archivalDetailList);

				System.out.println("ARCHIVAL DETAIL COUNT: " + archivalDetailList.size());

			}

			// =====================================================
			// CURRENT MODE
			// =====================================================
			else {

				List<FINCON_1B_Detail_Entity> currentDetailList;

				if (reportLabel != null && reportAddlCriteria1 != null) {

					currentDetailList = GetDetailDataByRowIdAndColumnId(reportLabel, reportAddlCriteria1, parsedDate);

				} else {

					currentDetailList = getDetaildatabydateList(parsedDate);

				}

				mv.addObject("reportdetails", currentDetailList);
				mv.addObject("reportmaster12", currentDetailList);

				System.out.println("CURRENT DETAIL COUNT: " + currentDetailList.size());
			}

		} catch (Exception e) {
			e.printStackTrace();
			mv.addObject("errorMessage", e.getMessage());
		}

		mv.setViewName("BRRS/BG_FINCON_1B");
		mv.addObject("displaymode", "Details");
		mv.addObject("menu", reportId);
		mv.addObject("currency", currency);
		mv.addObject("reportId", reportId);

		return mv;
	}

	// Archival View
	public List<Object[]> getFincon_1B_newArchival() {
		List<Object[]> archivalList = new ArrayList<>();

		try {

			List<BRRS_BG_DBIS10_FINCON_III_1B_Archival_ST_Entity> repoData = getdatabydateListWithVersion();

			if (repoData != null && !repoData.isEmpty()) {
				for (BRRS_BG_DBIS10_FINCON_III_1B_Archival_ST_Entity entity : repoData) {
					Object[] row = new Object[] { entity.getReportDate(), entity.getReportVersion(),
							entity.getREPORT_RESUBDATE() };
					archivalList.add(row);
				}

				System.out.println("Fetched " + archivalList.size() + " archival records");
				BRRS_BG_DBIS10_FINCON_III_1B_Archival_ST_Entity first = repoData.get(0);
				System.out.println("Latest archival version: " + first.getReportVersion());
			} else {
				System.out.println("No archival data found.");
			}

		} catch (Exception e) {
			System.err.println("Error fetching Fincon_1B Archival data: " + e.getMessage());
			e.printStackTrace();
		}

		return archivalList;
	}

	// Detail Download
	public byte[] getFincon_1B_DetailExcel(String filename, String fromdate, String todate, String currency,
			String dtltype, String type, String version) {
		try {
			logger.info("Generating Excel for Fincon_1B Details...");
			System.out.println("came to Detail download service");

			if (type.equals("ARCHIVAL") & version != null) {
				byte[] ARCHIVALreport = getFincon_1B_DetailExcelARCHIVAL(filename, fromdate, todate, currency, dtltype,
						type, version);
				return ARCHIVALreport;
			}

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("BG_DBIS_FINCON_III_1B");

			// Common border style
			BorderStyle border = BorderStyle.THIN;

			// Header style (left aligned)
			CellStyle headerStyle = workbook.createCellStyle();

			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 10);
			headerStyle.setFont(headerFont);
			headerStyle.setAlignment(HorizontalAlignment.LEFT);
			headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setBorderTop(border);
			headerStyle.setBorderBottom(border);
			headerStyle.setBorderLeft(border);
			headerStyle.setBorderRight(border);

			// Right-aligned header style for ACCT BALANCE
			CellStyle rightAlignedHeaderStyle = workbook.createCellStyle();
			rightAlignedHeaderStyle.cloneStyleFrom(headerStyle);
			rightAlignedHeaderStyle.setAlignment(HorizontalAlignment.RIGHT);

			// Default data style (left aligned)
			CellStyle dataStyle = workbook.createCellStyle();
			dataStyle.setAlignment(HorizontalAlignment.LEFT);
			dataStyle.setBorderTop(border);
			dataStyle.setBorderBottom(border);
			dataStyle.setBorderLeft(border);
			dataStyle.setBorderRight(border);

			// ACCT BALANCE style (right aligned with 3 decimals)
			CellStyle balanceStyle = workbook.createCellStyle();
			balanceStyle.setAlignment(HorizontalAlignment.RIGHT);
			balanceStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
			balanceStyle.setBorderTop(border);
			balanceStyle.setBorderBottom(border);
			balanceStyle.setBorderLeft(border);
			balanceStyle.setBorderRight(border);

			// Header row
			String[] headers = { "CUST ID", "ACCT NO", "ACCT NAME", "ACCT BALANCE", "REPORT LABEL",
					"REPORT ADDL CRITERIA1", "REPORT_DATE" };

			XSSFRow headerRow = sheet.createRow(0);
			for (int i = 0; i < headers.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(headers[i]);

				if (i == 3) { // ACCT BALANCE
					cell.setCellStyle(rightAlignedHeaderStyle);
				} else {
					cell.setCellStyle(headerStyle);
				}

				sheet.setColumnWidth(i, 5000);
			}

			// Get data
			Date parsedToDate = new SimpleDateFormat("dd/MM/yyyy").parse(todate);
			List<FINCON_1B_Detail_Entity> reportData = getDetaildatabydateList(parsedToDate);

			if (reportData != null && !reportData.isEmpty()) {
				int rowIndex = 1;
				for (FINCON_1B_Detail_Entity item : reportData) {
					XSSFRow row = sheet.createRow(rowIndex++);

					row.createCell(0).setCellValue(item.getCustId());
					row.createCell(1).setCellValue(item.getAcctNumber());
					row.createCell(2).setCellValue(item.getAcctName());
					// ACCT BALANCE (right aligned, 3 decimal places)
					Cell balanceCell = row.createCell(3);
					if (item.getAcctBalanceInpula() != null) {
						balanceCell.setCellValue(item.getAcctBalanceInpula().doubleValue());
					} else {
						balanceCell.setCellValue(0);
					}
					balanceCell.setCellStyle(balanceStyle);

					row.createCell(4).setCellValue(item.getReportLabel());
					row.createCell(5).setCellValue(item.getReportAddlCriteria_1());
					row.createCell(6)
							.setCellValue(item.getReportDate() != null
									? new SimpleDateFormat("dd-MM-yyyy").format(item.getReportDate())
									: "");

					// Apply data style for all other cells
					for (int j = 0; j < 7; j++) {
						if (j != 3) {
							row.getCell(j).setCellStyle(dataStyle);
						}
					}
				}
			} else {
				logger.info("No data found for Fincon_1B — only header will be written.");
			}

			// Write to byte[]
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			workbook.write(bos);
			workbook.close();

			logger.info("Excel generation completed with {} row(s).", reportData != null ? reportData.size() : 0);
			return bos.toByteArray();

		} catch (Exception e) {
			logger.error("Error generating FINCON_1B Excel", e);
			return new byte[0];
		}
	}

	public byte[] getFincon_1B_DetailExcelARCHIVAL(String filename, String fromdate, String todate, String currency,
			String dtltype, String type, String version) {
		try {
			logger.info("Generating Excel for Fincon_1B ARCHIVAL Details...");
			System.out.println("came to ARCHIVAL Detail download service");
			if (type.equals("ARCHIVAL") & version != null) {

			}
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("BG_DBIS_FINCON_III_1B");

			// Common border style
			BorderStyle border = BorderStyle.THIN;

			// Header style (left aligned)
			CellStyle headerStyle = workbook.createCellStyle();
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 10);
			headerStyle.setFont(headerFont);
			headerStyle.setAlignment(HorizontalAlignment.LEFT);
			headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setBorderTop(border);
			headerStyle.setBorderBottom(border);
			headerStyle.setBorderLeft(border);
			headerStyle.setBorderRight(border);

			// Right-aligned header style for ACCT BALANCE
			CellStyle rightAlignedHeaderStyle = workbook.createCellStyle();
			rightAlignedHeaderStyle.cloneStyleFrom(headerStyle);
			rightAlignedHeaderStyle.setAlignment(HorizontalAlignment.RIGHT);

			// Default data style (left aligned)
			CellStyle dataStyle = workbook.createCellStyle();
			dataStyle.setAlignment(HorizontalAlignment.LEFT);
			dataStyle.setBorderTop(border);
			dataStyle.setBorderBottom(border);
			dataStyle.setBorderLeft(border);
			dataStyle.setBorderRight(border);

			// ACCT BALANCE style (right aligned with 3 decimals)
			CellStyle balanceStyle = workbook.createCellStyle();
			balanceStyle.setAlignment(HorizontalAlignment.RIGHT);
			balanceStyle.setDataFormat(workbook.createDataFormat().getFormat("0"));
			balanceStyle.setBorderTop(border);
			balanceStyle.setBorderBottom(border);
			balanceStyle.setBorderLeft(border);
			balanceStyle.setBorderRight(border);

			// Header row
			String[] headers = { "CUST ID", "ACCT NO", "ACCT NAME", "ACCT BALANCE", "REPORT LABEL",
					"REPORT ADDL CRITERIA1", "REPORT_DATE" };

			XSSFRow headerRow = sheet.createRow(0);
			for (int i = 0; i < headers.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(headers[i]);

				if (i == 3) { // ACCT BALANCE
					cell.setCellStyle(rightAlignedHeaderStyle);
				} else {
					cell.setCellStyle(headerStyle);
				}

				sheet.setColumnWidth(i, 5000);
			}

			// Get data
			Date parsedToDate = new SimpleDateFormat("dd/MM/yyyy").parse(todate);
			List<Fincon_1B_Archival_Detail_Entity> reportData = getArchivalDetaildatabydateList(parsedToDate, version);

			if (reportData != null && !reportData.isEmpty()) {
				int rowIndex = 1;
				for (Fincon_1B_Archival_Detail_Entity item : reportData) {
					XSSFRow row = sheet.createRow(rowIndex++);

					row.createCell(0).setCellValue(item.getCustId());
					row.createCell(1).setCellValue(item.getAcctNumber());
					row.createCell(2).setCellValue(item.getAcctName());

					// ACCT BALANCE (right aligned, 3 decimal places)
					Cell balanceCell = row.createCell(3);
					if (item.getAcctBalanceInpula() != null) {
						balanceCell.setCellValue(item.getAcctBalanceInpula().doubleValue());
					} else {
						balanceCell.setCellValue(0);
					}
					balanceCell.setCellStyle(balanceStyle);

					row.createCell(4).setCellValue(item.getReportLabel());
					row.createCell(5).setCellValue(item.getReportAddlCriteria_1());
					row.createCell(6)
							.setCellValue(item.getReportDate() != null
									? new SimpleDateFormat("dd-MM-yyyy").format(item.getReportDate())
									: "");

					// Apply data style for all other cells
					for (int j = 0; j < 7; j++) {
						if (j != 3) {
							row.getCell(j).setCellStyle(dataStyle);
						}
					}
				}
			} else {
				logger.info("No data found for Fincon_1B — only header will be written.");
			}

			// Write to byte[]
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			workbook.write(bos);
			workbook.close();

			logger.info("Excel generation completed with {} row(s).", reportData != null ? reportData.size() : 0);
			return bos.toByteArray();

		} catch (Exception e) {
			logger.error("Error generating SCH_17 NEW Excel", e);
			return new byte[0];
		}
	}

	// Summary Download
	public byte[] getFincon_1B_Excel(String filename, String reportId, String fromdate, String todate, String currency,
			String dtltype, String type, BigDecimal version) throws Exception {
		logger.info("Service: Starting Excel generation process in memory.sch17");

		// ARCHIVAL check
		if ("ARCHIVAL".equalsIgnoreCase(type) && version != null && version.compareTo(BigDecimal.ZERO) >= 0) {
			logger.info("Service: Generating ARCHIVAL report for version {}", version);
			return getExcelFincon_1B_ARCHIVALExcel(filename, reportId, fromdate, todate, currency, dtltype, type,
					version);
		}

		// Fetch data
		List<BRRS_BG_DBIS10_FINCON_III_1B_ST_Entity> dataList = getDataByDate(dateformat.parse(todate));

		System.out.println("DATA SIZE IS : " + dataList.size());
		if (dataList.isEmpty()) {
			logger.warn("Service: No data found for Fincon_1B report. Returning empty result.");
			return new byte[0];
		}

		String templateDir = env.getProperty("output.exportpathtemp");
		String templateFileName = filename;
		System.out.println(filename);
		Path templatePath = Paths.get(templateDir, templateFileName);
		System.out.println(templatePath);

		logger.info("Service: Attempting to load template from path: {}", templatePath.toAbsolutePath());

		if (!Files.exists(templatePath)) {
			throw new FileNotFoundException("Template file not found at: " + templatePath.toAbsolutePath());
		}
		if (!Files.isReadable(templatePath)) {
			throw new SecurityException(
					"Template file exists but is not readable (check permissions): " + templatePath.toAbsolutePath());
		}

		try (InputStream templateInputStream = Files.newInputStream(templatePath);
				Workbook workbook = WorkbookFactory.create(templateInputStream);
				ByteArrayOutputStream out = new ByteArrayOutputStream()) {

			Sheet sheet = workbook.getSheetAt(0);

			// --- Style Definitions ---
			CreationHelper createHelper = workbook.getCreationHelper();

			CellStyle dateStyle = workbook.createCellStyle();
			dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
			dateStyle.setBorderBottom(BorderStyle.THIN);
			dateStyle.setBorderTop(BorderStyle.THIN);
			dateStyle.setBorderLeft(BorderStyle.THIN);
			dateStyle.setBorderRight(BorderStyle.THIN);

			CellStyle textStyle = workbook.createCellStyle();
			textStyle.setBorderBottom(BorderStyle.THIN);
			textStyle.setBorderTop(BorderStyle.THIN);
			textStyle.setBorderLeft(BorderStyle.THIN);
			textStyle.setBorderRight(BorderStyle.THIN);

			// Create the font
			Font font = workbook.createFont();
			font.setFontHeightInPoints((short) 8); // size 8
			font.setFontName("Arial");

			CellStyle numberStyle = workbook.createCellStyle();
			numberStyle.setBorderBottom(BorderStyle.THIN);
			numberStyle.setBorderTop(BorderStyle.THIN);
			numberStyle.setBorderLeft(BorderStyle.THIN);
			numberStyle.setBorderRight(BorderStyle.THIN);
			numberStyle.setFont(font);
			// --- End of Style Definitions ---

			int startRow = 6;

			if (!dataList.isEmpty()) {
				for (int i = 0; i < dataList.size(); i++) {
					BRRS_BG_DBIS10_FINCON_III_1B_ST_Entity record = dataList.get(i);
					System.out.println("rownumber=" + startRow + i);
					Row row = sheet.getRow(startRow + i);
					if (row == null) {
						row = sheet.createRow(startRow + i);
					}

					// R7 fields
					Cell cell = row.createCell(4);
					if (record.getR7OiopsX010() != null) {
						cell.setCellValue(record.getR7OiopsX010());
						cell.setCellStyle(textStyle);
					} else {
						cell.setCellValue(0);
						cell.setCellStyle(textStyle);
					}


					row = sheet.getRow(6);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					if (record.getR7DotBdX020() != null) {
						cell.setCellValue(record.getR7DotBdX020());
						cell.setCellStyle(dateStyle);
					} else {
						cell.setCellValue("");
					}

					row = sheet.getRow(6);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					if (record.getR7DotEdX030() != null) {
						cell.setCellValue(record.getR7DotEdX030());
						cell.setCellStyle(dateStyle);
					} else {
						cell.setCellValue("");
					}

					row = sheet.getRow(6);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(record.getR7TottX040() != null ? record.getR7TottX040().doubleValue() : 0);

					row = sheet.getRow(6);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(record.getR7AmountX050() != null ? record.getR7AmountX050().doubleValue() : 0);

					row = sheet.getRow(6);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(record.getR7RyotiX060() != null ? record.getR7RyotiX060().doubleValue() : 0);

					row = sheet.getRow(7);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(record.getR8OiopsX010() != null ?  record.getR8OiopsX010(): "");

					row = sheet.getRow(7);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					if (record.getR8DotBdX020() != null) {
						cell.setCellValue(record.getR8DotBdX020());
						cell.setCellStyle(dateStyle);
					} else {
						cell.setCellValue("");
					}

					row = sheet.getRow(7);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					if (record.getR8DotEdX030() != null) {
						cell.setCellValue(record.getR8DotEdX030());
						cell.setCellStyle(dateStyle);
					} else {
						cell.setCellValue("");
					}

					row = sheet.getRow(7);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(record.getR8TottX040() != null ? record.getR8TottX040().doubleValue() : 0);

					row = sheet.getRow(7);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(record.getR8AmountX050() != null ? record.getR8AmountX050().doubleValue() : 0);

					row = sheet.getRow(7);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(record.getR8RyotiX060() != null ? record.getR8RyotiX060().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(record.getR9OiopsX010() != null ? record.getR9OiopsX010(): "");

					row = sheet.getRow(8);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					if (record.getR9DotBdX020() != null) {
						cell.setCellValue(record.getR9DotBdX020());
						cell.setCellStyle(dateStyle);
					} else {
						cell.setCellValue("");
					}

					row = sheet.getRow(8);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					if (record.getR9DotEdX030() != null) {
						cell.setCellValue(record.getR9DotEdX030());
						cell.setCellStyle(dateStyle);
					} else {
						cell.setCellValue("");
					}

					row = sheet.getRow(8);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(record.getR9TottX040() != null ? record.getR9TottX040().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(record.getR9AmountX050() != null ? record.getR9AmountX050().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(record.getR9RyotiX060() != null ? record.getR9RyotiX060().doubleValue() : 0);

					row = sheet.getRow(9);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(record.getR10OiopsX010() != null ? record.getR10OiopsX010(): "");

					row = sheet.getRow(9);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					if (record.getR10DotBdX020() != null) {
						cell.setCellValue(record.getR10DotBdX020());
						cell.setCellStyle(dateStyle);
					} else {
						cell.setCellValue("");
					}

					row = sheet.getRow(9);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					if (record.getR10DotEdX030() != null) {
						cell.setCellValue(record.getR10DotEdX030());
						cell.setCellStyle(dateStyle);
					} else {
						cell.setCellValue("");
					}

					row = sheet.getRow(9);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(record.getR10TottX040() != null ? record.getR10TottX040().doubleValue() : 0);

					row = sheet.getRow(9);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(record.getR10AmountX050() != null ? record.getR10AmountX050().doubleValue() : 0);

					row = sheet.getRow(9);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(record.getR10RyotiX060() != null ? record.getR10RyotiX060().doubleValue() : 0);

				}
			}

			workbook.write(out);
			logger.info("Service: Excel data successfully written to memory buffer ({} bytes).", out.size());
			return out.toByteArray();
		}
	}

	public byte[] getExcelFincon_1B_ARCHIVALExcel(String filename, String reportId, String fromdate, String todate,
			String currency, String dtltype, String type, BigDecimal version) throws Exception {

		logger.info("Service: Starting Excel generation process in memory.");

		List<BRRS_BG_DBIS10_FINCON_III_1B_Archival_ST_Entity> dataList = getdatabydateListarchival(
				dateformat.parse(todate), version);

		if (dataList.isEmpty()) {
			logger.warn("Service: No data found for SCH_17 new report. Returning empty result.");
			return new byte[0];
		}

		String templateDir = env.getProperty("output.exportpathtemp");
		String templateFileName = filename;
		System.out.println(filename);
		Path templatePath = Paths.get(templateDir, templateFileName);
		System.out.println(templatePath);

		logger.info("Service: Attempting to load template from path: {}", templatePath.toAbsolutePath());

		if (!Files.exists(templatePath)) {
			throw new FileNotFoundException("Template file not found at: " + templatePath.toAbsolutePath());
		}
		if (!Files.isReadable(templatePath)) {
			throw new SecurityException(
					"Template file exists but is not readable (check permissions): " + templatePath.toAbsolutePath());
		}

		try (InputStream templateInputStream = Files.newInputStream(templatePath);
				Workbook workbook = WorkbookFactory.create(templateInputStream);
				ByteArrayOutputStream out = new ByteArrayOutputStream()) {

			Sheet sheet = workbook.getSheetAt(0);

			// --- Style Definitions ---
			CreationHelper createHelper = workbook.getCreationHelper();

			CellStyle dateStyle = workbook.createCellStyle();
			dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
			dateStyle.setBorderBottom(BorderStyle.THIN);
			dateStyle.setBorderTop(BorderStyle.THIN);
			dateStyle.setBorderLeft(BorderStyle.THIN);
			dateStyle.setBorderRight(BorderStyle.THIN);

			CellStyle textStyle = workbook.createCellStyle();
			textStyle.setBorderBottom(BorderStyle.THIN);
			textStyle.setBorderTop(BorderStyle.THIN);
			textStyle.setBorderLeft(BorderStyle.THIN);
			textStyle.setBorderRight(BorderStyle.THIN);

			// Create the font
			Font font = workbook.createFont();
			font.setFontHeightInPoints((short) 8); // size 8
			font.setFontName("Arial");

			CellStyle numberStyle = workbook.createCellStyle();
			numberStyle.setBorderBottom(BorderStyle.THIN);
			numberStyle.setBorderTop(BorderStyle.THIN);
			numberStyle.setBorderLeft(BorderStyle.THIN);
			numberStyle.setBorderRight(BorderStyle.THIN);
			numberStyle.setFont(font);
			// --- End of Style Definitions ---

			int startRow = 6;

			if (!dataList.isEmpty()) {
				for (int i = 0; i < dataList.size(); i++) {
					BRRS_BG_DBIS10_FINCON_III_1B_Archival_ST_Entity record = dataList.get(i);
					System.out.println("rownumber=" + startRow + i);
					Row row = sheet.getRow(startRow + i);
					if (row == null) {
						row = sheet.createRow(startRow + i);
					}

					// R7 fields
					Cell cell = row.createCell(4);
					if (record.getR7OiopsX010() != null) {
						cell.setCellValue(record.getR7OiopsX010());
						cell.setCellStyle(textStyle);
					} else {
						cell.setCellValue(0);
						cell.setCellStyle(textStyle);
					}


					row = sheet.getRow(6);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					if (record.getR7DotBdX020() != null) {
						cell.setCellValue(record.getR7DotBdX020());
						cell.setCellStyle(dateStyle);
					} else {
						cell.setCellValue("");
					}

					row = sheet.getRow(6);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					if (record.getR7DotEdX030() != null) {
						cell.setCellValue(record.getR7DotEdX030());
						cell.setCellStyle(dateStyle);
					} else {
						cell.setCellValue("");
					}

					row = sheet.getRow(6);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(record.getR7TottX040() != null ? record.getR7TottX040().doubleValue() : 0);

					row = sheet.getRow(6);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(record.getR7AmountX050() != null ? record.getR7AmountX050().doubleValue() : 0);

					row = sheet.getRow(6);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(record.getR7RyotiX060() != null ? record.getR7RyotiX060().doubleValue() : 0);

					row = sheet.getRow(7);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(record.getR8OiopsX010() != null ?  record.getR8OiopsX010(): "");

					row = sheet.getRow(7);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					if (record.getR8DotBdX020() != null) {
						cell.setCellValue(record.getR8DotBdX020());
						cell.setCellStyle(dateStyle);
					} else {
						cell.setCellValue("");
					}

					row = sheet.getRow(7);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					if (record.getR8DotEdX030() != null) {
						cell.setCellValue(record.getR8DotEdX030());
						cell.setCellStyle(dateStyle);
					} else {
						cell.setCellValue("");
					}

					row = sheet.getRow(7);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(record.getR8TottX040() != null ? record.getR8TottX040().doubleValue() : 0);

					row = sheet.getRow(7);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(record.getR8AmountX050() != null ? record.getR8AmountX050().doubleValue() : 0);

					row = sheet.getRow(7);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(record.getR8RyotiX060() != null ? record.getR8RyotiX060().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(record.getR9OiopsX010() != null ? record.getR9OiopsX010(): "");

					row = sheet.getRow(8);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					if (record.getR9DotBdX020() != null) {
						cell.setCellValue(record.getR9DotBdX020());
						cell.setCellStyle(dateStyle);
					} else {
						cell.setCellValue("");
					}

					row = sheet.getRow(8);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					if (record.getR9DotEdX030() != null) {
						cell.setCellValue(record.getR9DotEdX030());
						cell.setCellStyle(dateStyle);
					} else {
						cell.setCellValue("");
					}

					row = sheet.getRow(8);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(record.getR9TottX040() != null ? record.getR9TottX040().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(record.getR9AmountX050() != null ? record.getR9AmountX050().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(record.getR9RyotiX060() != null ? record.getR9RyotiX060().doubleValue() : 0);

					row = sheet.getRow(9);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(record.getR10OiopsX010() != null ? record.getR10OiopsX010(): "");

					row = sheet.getRow(9);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					if (record.getR10DotBdX020() != null) {
						cell.setCellValue(record.getR10DotBdX020());
						cell.setCellStyle(dateStyle);
					} else {
						cell.setCellValue("");
					}

					row = sheet.getRow(9);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					if (record.getR10DotEdX030() != null) {
						cell.setCellValue(record.getR10DotEdX030());
						cell.setCellStyle(dateStyle);
					} else {
						cell.setCellValue("");
					}

					row = sheet.getRow(9);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(record.getR10TottX040() != null ? record.getR10TottX040().doubleValue() : 0);

					row = sheet.getRow(9);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(record.getR10AmountX050() != null ? record.getR10AmountX050().doubleValue() : 0);

					row = sheet.getRow(9);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(record.getR10RyotiX060() != null ? record.getR10RyotiX060().doubleValue() : 0);
					}
				
			}

			workbook.write(out);
			logger.info("Service: Excel data successfully written to memory buffer ({} bytes).", out.size());
			return out.toByteArray();
		}
	}

	// Fetch Summary data by report date
	public List<BRRS_BG_DBIS10_FINCON_III_1B_ST_Entity> getDataByDate(Date reportDate) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1B_SUMMARYTABLE WHERE REPORT_DATE = ?";
		return jdbcTemplate.query(sql, new Object[] { reportDate }, new Fincon_1B_RowMapper());
	}

	// =========================================================
	// GET REPORT_DATE + REPORT_VERSION
	// =========================================================
	public List<Object[]> getDBIS10_FINCON_1B_archival() {
		String sql = "SELECT REPORT_DATE, REPORT_VERSION, REPORT_RESUBDATE "
				+ "FROM BRRS_BG_DBIS10_FINCON_III_1B_ARCHIVALTABLE_SUMMARY " + "ORDER BY REPORT_VERSION";
		return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[] { rs.getDate("REPORT_DATE"),
				rs.getBigDecimal("REPORT_VERSION"), rs.getDate("REPORT_RESUBDATE") });
	}

	// =========================================================
	// GET ARCHIVAL FULL DATA BY DATE + VERSION
	// =========================================================
	public List<BRRS_BG_DBIS10_FINCON_III_1B_Archival_ST_Entity> getdatabydateListarchival(Date reportDate,
			BigDecimal reportVersion) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1B_ARCHIVALTABLE_SUMMARY "
				+ "WHERE REPORT_DATE = ? AND REPORT_VERSION = ?";
		return jdbcTemplate.query(sql, new Object[] { reportDate, reportVersion }, new Fincon_1B_Archival_RowMapper());
	}

	// =========================================================
	// GET ALL WITH VERSION
	// =========================================================
	public List<BRRS_BG_DBIS10_FINCON_III_1B_Archival_ST_Entity> getdatabydateListWithVersion() {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1B_ARCHIVALTABLE_SUMMARY "
				+ "WHERE REPORT_VERSION IS NOT NULL ORDER BY REPORT_VERSION ASC";
		return jdbcTemplate.query(sql, new Fincon_1B_Archival_RowMapper());
	}

	// =========================================================
	// GET MAX VERSION BY DATE
	// =========================================================
	public BigDecimal findMaxVersion(Date reportDate) {
		String sql = "SELECT MAX(REPORT_VERSION) " + "FROM BRRS_BG_DBIS10_FINCON_III_1B_ARCHIVALTABLE_SUMMARY "
				+ "WHERE REPORT_DATE = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { reportDate }, BigDecimal.class);
	}

	// =========================================================
	// DETAIL QUERIES
	// =========================================================
	public List<FINCON_1B_Detail_Entity> findByDetailReportDateAndLabelAndCriteria(Date reportDate, String reportLabel,
			String reportAddlCriteria1) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1B_DETAILTABLE "
				+ "WHERE REPORT_DATE = ? AND REPORT_LABEL = ? AND REPORT_ADDL_CRITERIA_1 = ?";
		return jdbcTemplate.query(sql, new Object[] { reportDate, reportLabel, reportAddlCriteria1 },
				new Fincon1B_DetailRowMapper());
	}

	public List<FINCON_1B_Detail_Entity> getDetaildatabydateList(Date reportdate) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1B_DETAILTABLE WHERE REPORT_DATE = ?";
		return jdbcTemplate.query(sql, new Object[] { reportdate }, new Fincon1B_DetailRowMapper());
	}

	public List<FINCON_1B_Detail_Entity> getDetaildatabydateList(Date reportdate, int offset, int limit) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1B_DETAILTABLE "
				+ "WHERE REPORT_DATE = ? OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
		return jdbcTemplate.query(sql, new Object[] { reportdate, offset, limit }, new Fincon1B_DetailRowMapper());
	}

	public int getDetaildatacount(Date reportdate) {
		String sql = "SELECT COUNT(*) FROM BRRS_BG_DBIS10_FINCON_III_1B_DETAILTABLE WHERE REPORT_DATE = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { reportdate }, Integer.class);
	}

	public List<FINCON_1B_Detail_Entity> GetDetailDataByRowIdAndColumnId(String reportLabel, String reportAddlCriteria1,
			Date reportdate) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1B_DETAILTABLE "
				+ "WHERE REPORT_LABEL = ? AND REPORT_ADDL_CRITERIA_1 = ? AND REPORT_DATE = ?";
		return jdbcTemplate.query(sql, new Object[] { reportLabel, reportAddlCriteria1, reportdate },
				new Fincon1B_DetailRowMapper());
	}

	public FINCON_1B_Detail_Entity findByAcctnumber(String acct_number) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1B_DETAILTABLE WHERE ACCT_NUMBER = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { acct_number }, new Fincon1B_DetailRowMapper());
	}

	public FINCON_1B_Detail_Entity findBySno(String sno) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1B_DETAILTABLE WHERE SNO = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { sno }, new Fincon1B_DetailRowMapper());
	}

	// ARCHIVAL DETAIL QUERIES
	public List<Fincon_1B_Archival_Detail_Entity> getArchivalDetaildatabydateList(Date reportdate,
			String dataEntryVersion) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1B_ARCHIVALTABLE_DETAIL "
				+ "WHERE REPORT_DATE = ? AND DATA_ENTRY_VERSION = ?";
		return jdbcTemplate.query(sql, new Object[] { reportdate, dataEntryVersion },
				new Fincon_1b_ArchivalDetailRowMapper());
	}

	public List<Fincon_1B_Archival_Detail_Entity> GetArchivalDataByRowIdAndColumnId(String reportLabel,
			String reportAddlCriteria1, Date reportdate, String dataEntryVersion) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1B_ARCHIVALTABLE_DETAIL "
				+ "WHERE REPORT_LABEL = ? AND REPORT_ADDL_CRITERIA_1 = ? "
				+ "AND REPORT_DATE = ? AND DATA_ENTRY_VERSION = ?";
		return jdbcTemplate.query(sql, new Object[] { reportLabel, reportAddlCriteria1, reportdate, dataEntryVersion },
				new Fincon_1b_ArchivalDetailRowMapper());
	}

	// =========================================================
	// ENTITY CLASSES
	// =========================================================

	// PK Class
	public static class BG_FINCON_1B_PK implements Serializable {
		private Date report_date;
		private BigDecimal report_version;

		public BG_FINCON_1B_PK() {
		}

		public BG_FINCON_1B_PK(Date report_date, BigDecimal report_version) {
			this.report_date = report_date;
			this.report_version = report_version;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof BG_FINCON_1B_PK))
				return false;
			BG_FINCON_1B_PK that = (BG_FINCON_1B_PK) o;
			return Objects.equals(report_date, that.report_date) && Objects.equals(report_version, that.report_version);
		}

		@Override
		public int hashCode() {
			return Objects.hash(report_date, report_version);
		}

		public Date getReport_date() {
			return report_date;
		}

		public void setReport_date(Date report_date) {
			this.report_date = report_date;
		}

		public BigDecimal getReport_version() {
			return report_version;
		}

		public void setReport_version(BigDecimal report_version) {
			this.report_version = report_version;
		}
	}

	// Summary Entity
	public static class BRRS_BG_DBIS10_FINCON_III_1B_ST_Entity {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;

		// R7 fields
		@Column(name = "R7_TRANSACTION_SL_NO")
		private BigDecimal r7TransactionSlNo;

		@Column(name = "R7_NAME_OF_SFI")
		private String r7NameOfSfi;

		@Column(name = "R7_NAME_OF_COUNTER_PARTY")
		private String r7NameOfCounterParty;

		@Column(name = "R7_NATURE_OF_TRANSACTIONS")
		private String r7NatureOfTransactions;

		@Column(name = "R7_OIOTPS_X010")
		private String r7OiopsX010;

		@Column(name = "R7_DOT_BD_X020")
		@Temporal(TemporalType.DATE)
		private Date r7DotBdX020;

		@Column(name = "R7_DOT_ED_X030")
		@Temporal(TemporalType.DATE)
		private Date r7DotEdX030;

		@Column(name = "R7_TOTT_X040")
		private BigDecimal r7TottX040;

		@Column(name = "R7_AMOUNT_X050")
		private BigDecimal r7AmountX050;

		@Column(name = "R7_RYOTI_X060")
		private BigDecimal r7RyotiX060;

		// R8 fields
		@Column(name = "R8_TRANSACTION_SL_NO")
		private BigDecimal r8TransactionSlNo;

		@Column(name = "R8_NAME_OF_SFI")
		private String r8NameOfSfi;

		@Column(name = "R8_NAME_OF_COUNTER_PARTY")
		private String r8NameOfCounterParty;

		@Column(name = "R8_NATURE_OF_TRANSACTIONS")
		private String r8NatureOfTransactions;

		@Column(name = "R8_OIOTPS_X010")
		private String r8OiopsX010;

		@Column(name = "R8_DOT_BD_X020")
		@Temporal(TemporalType.DATE)
		private Date r8DotBdX020;

		@Column(name = "R8_DOT_ED_X030")
		@Temporal(TemporalType.DATE)
		private Date r8DotEdX030;

		@Column(name = "R8_TOTT_X040")
		private BigDecimal r8TottX040;

		@Column(name = "R8_AMOUNT_X050")
		private BigDecimal r8AmountX050;

		@Column(name = "R8_RYOTI_X060")
		private BigDecimal r8RyotiX060;

		// R9 fields
		@Column(name = "R9_TRANSACTION_SL_NO")
		private BigDecimal r9TransactionSlNo;

		@Column(name = "R9_NAME_OF_SFI")
		private String r9NameOfSfi;

		@Column(name = "R9_NAME_OF_COUNTER_PARTY")
		private String r9NameOfCounterParty;

		@Column(name = "R9_NATURE_OF_TRANSACTIONS")
		private String r9NatureOfTransactions;

		@Column(name = "R9_OIOTPS_X010")
		private String r9OiopsX010;

		@Column(name = "R9_DOT_BD_X020")
		@Temporal(TemporalType.DATE)
		private Date r9DotBdX020;

		@Column(name = "R9_DOT_ED_X030")
		@Temporal(TemporalType.DATE)
		private Date r9DotEdX030;

		@Column(name = "R9_TOTT_X040")
		private BigDecimal r9TottX040;

		@Column(name = "R9_AMOUNT_X050")
		private BigDecimal r9AmountX050;

		@Column(name = "R9_RYOTI_X060")
		private BigDecimal r9RyotiX060;

		// R10 fields
		@Column(name = "R10_TRANSACTION_SL_NO")
		private BigDecimal r10TransactionSlNo;

		@Column(name = "R10_NAME_OF_SFI")
		private String r10NameOfSfi;

		@Column(name = "R10_NAME_OF_COUNTER_PARTY")
		private String r10NameOfCounterParty;

		@Column(name = "R10_NATURE_OF_TRANSACTIONS")
		private String r10NatureOfTransactions;

		@Column(name = "R10_OIOTPS_X010")
		private String r10OiopsX010;

		@Column(name = "R10_DOT_BD_X020")
		@Temporal(TemporalType.DATE)
		private Date r10DotBdX020;

		@Column(name = "R10_DOT_ED_X030")
		@Temporal(TemporalType.DATE)
		private Date r10DotEdX030;

		@Column(name = "R10_TOTT_X040")
		private BigDecimal r10TottX040;

		@Column(name = "R10_AMOUNT_X050")
		private BigDecimal r10AmountX050;

		@Column(name = "R10_RYOTI_X060")
		private BigDecimal r10RyotiX060;

		// Common fields
		@Column(name = "REPORT_DATE")
		@Temporal(TemporalType.DATE)
		private Date reportDate;

		@Column(name = "REPORT_VERSION")
		private BigDecimal reportVersion;

		@Column(name = "REPORT_FREQUENCY")
		private String reportFrequency;

		@Column(name = "REPORT_CODE")
		private String reportCode;

		@Column(name = "REPORT_DESC")
		private String reportDesc;

		@Column(name = "ENTITY_FLG")
		private String entityFlg;

		@Column(name = "MODIFY_FLG")
		private String modifyFlg;

		@Column(name = "DEL_FLG")
		private String delFlg;

		// Getters and Setters for R7
		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public BigDecimal getR7TransactionSlNo() {
			return r7TransactionSlNo;
		}

		public void setR7TransactionSlNo(BigDecimal r7TransactionSlNo) {
			this.r7TransactionSlNo = r7TransactionSlNo;
		}

		public String getR7NameOfSfi() {
			return r7NameOfSfi;
		}

		public void setR7NameOfSfi(String r7NameOfSfi) {
			this.r7NameOfSfi = r7NameOfSfi;
		}

		public String getR7NameOfCounterParty() {
			return r7NameOfCounterParty;
		}

		public void setR7NameOfCounterParty(String r7NameOfCounterParty) {
			this.r7NameOfCounterParty = r7NameOfCounterParty;
		}

		public String getR7NatureOfTransactions() {
			return r7NatureOfTransactions;
		}

		public void setR7NatureOfTransactions(String r7NatureOfTransactions) {
			this.r7NatureOfTransactions = r7NatureOfTransactions;
		}

		public String getR7OiopsX010() {
			return r7OiopsX010;
		}

		public void setR7OiopsX010(String r7OiopsX010) {
			this.r7OiopsX010 = r7OiopsX010;
		}

		public Date getR7DotBdX020() {
			return r7DotBdX020;
		}

		public void setR7DotBdX020(Date r7DotBdX020) {
			this.r7DotBdX020 = r7DotBdX020;
		}

		public Date getR7DotEdX030() {
			return r7DotEdX030;
		}

		public void setR7DotEdX030(Date r7DotEdX030) {
			this.r7DotEdX030 = r7DotEdX030;
		}

		public BigDecimal getR7TottX040() {
			return r7TottX040;
		}

		public void setR7TottX040(BigDecimal r7TottX040) {
			this.r7TottX040 = r7TottX040;
		}

		public BigDecimal getR7AmountX050() {
			return r7AmountX050;
		}

		public void setR7AmountX050(BigDecimal r7AmountX050) {
			this.r7AmountX050 = r7AmountX050;
		}

		public BigDecimal getR7RyotiX060() {
			return r7RyotiX060;
		}

		public void setR7RyotiX060(BigDecimal r7RyotiX060) {
			this.r7RyotiX060 = r7RyotiX060;
		}

		// Getters and Setters for R8
		public BigDecimal getR8TransactionSlNo() {
			return r8TransactionSlNo;
		}

		public void setR8TransactionSlNo(BigDecimal r8TransactionSlNo) {
			this.r8TransactionSlNo = r8TransactionSlNo;
		}

		public String getR8NameOfSfi() {
			return r8NameOfSfi;
		}

		public void setR8NameOfSfi(String r8NameOfSfi) {
			this.r8NameOfSfi = r8NameOfSfi;
		}

		public String getR8NameOfCounterParty() {
			return r8NameOfCounterParty;
		}

		public void setR8NameOfCounterParty(String r8NameOfCounterParty) {
			this.r8NameOfCounterParty = r8NameOfCounterParty;
		}

		public String getR8NatureOfTransactions() {
			return r8NatureOfTransactions;
		}

		public void setR8NatureOfTransactions(String r8NatureOfTransactions) {
			this.r8NatureOfTransactions = r8NatureOfTransactions;
		}

		public String getR8OiopsX010() {
			return r8OiopsX010;
		}

		public void setR8OiopsX010(String r8OiopsX010) {
			this.r8OiopsX010 = r8OiopsX010;
		}

		public Date getR8DotBdX020() {
			return r8DotBdX020;
		}

		public void setR8DotBdX020(Date r8DotBdX020) {
			this.r8DotBdX020 = r8DotBdX020;
		}

		public Date getR8DotEdX030() {
			return r8DotEdX030;
		}

		public void setR8DotEdX030(Date r8DotEdX030) {
			this.r8DotEdX030 = r8DotEdX030;
		}

		public BigDecimal getR8TottX040() {
			return r8TottX040;
		}

		public void setR8TottX040(BigDecimal r8TottX040) {
			this.r8TottX040 = r8TottX040;
		}

		public BigDecimal getR8AmountX050() {
			return r8AmountX050;
		}

		public void setR8AmountX050(BigDecimal r8AmountX050) {
			this.r8AmountX050 = r8AmountX050;
		}

		public BigDecimal getR8RyotiX060() {
			return r8RyotiX060;
		}

		public void setR8RyotiX060(BigDecimal r8RyotiX060) {
			this.r8RyotiX060 = r8RyotiX060;
		}

		// Getters and Setters for R9
		public BigDecimal getR9TransactionSlNo() {
			return r9TransactionSlNo;
		}

		public void setR9TransactionSlNo(BigDecimal r9TransactionSlNo) {
			this.r9TransactionSlNo = r9TransactionSlNo;
		}

		public String getR9NameOfSfi() {
			return r9NameOfSfi;
		}

		public void setR9NameOfSfi(String r9NameOfSfi) {
			this.r9NameOfSfi = r9NameOfSfi;
		}

		public String getR9NameOfCounterParty() {
			return r9NameOfCounterParty;
		}

		public void setR9NameOfCounterParty(String r9NameOfCounterParty) {
			this.r9NameOfCounterParty = r9NameOfCounterParty;
		}

		public String getR9NatureOfTransactions() {
			return r9NatureOfTransactions;
		}

		public void setR9NatureOfTransactions(String r9NatureOfTransactions) {
			this.r9NatureOfTransactions = r9NatureOfTransactions;
		}

		public String getR9OiopsX010() {
			return r9OiopsX010;
		}

		public void setR9OiopsX010(String r9OiopsX010) {
			this.r9OiopsX010 = r9OiopsX010;
		}

		public Date getR9DotBdX020() {
			return r9DotBdX020;
		}

		public void setR9DotBdX020(Date r9DotBdX020) {
			this.r9DotBdX020 = r9DotBdX020;
		}

		public Date getR9DotEdX030() {
			return r9DotEdX030;
		}

		public void setR9DotEdX030(Date r9DotEdX030) {
			this.r9DotEdX030 = r9DotEdX030;
		}

		public BigDecimal getR9TottX040() {
			return r9TottX040;
		}

		public void setR9TottX040(BigDecimal r9TottX040) {
			this.r9TottX040 = r9TottX040;
		}

		public BigDecimal getR9AmountX050() {
			return r9AmountX050;
		}

		public void setR9AmountX050(BigDecimal r9AmountX050) {
			this.r9AmountX050 = r9AmountX050;
		}

		public BigDecimal getR9RyotiX060() {
			return r9RyotiX060;
		}

		public void setR9RyotiX060(BigDecimal r9RyotiX060) {
			this.r9RyotiX060 = r9RyotiX060;
		}

		// Getters and Setters for R10
		public BigDecimal getR10TransactionSlNo() {
			return r10TransactionSlNo;
		}

		public void setR10TransactionSlNo(BigDecimal r10TransactionSlNo) {
			this.r10TransactionSlNo = r10TransactionSlNo;
		}

		public String getR10NameOfSfi() {
			return r10NameOfSfi;
		}

		public void setR10NameOfSfi(String r10NameOfSfi) {
			this.r10NameOfSfi = r10NameOfSfi;
		}

		public String getR10NameOfCounterParty() {
			return r10NameOfCounterParty;
		}

		public void setR10NameOfCounterParty(String r10NameOfCounterParty) {
			this.r10NameOfCounterParty = r10NameOfCounterParty;
		}

		public String getR10NatureOfTransactions() {
			return r10NatureOfTransactions;
		}

		public void setR10NatureOfTransactions(String r10NatureOfTransactions) {
			this.r10NatureOfTransactions = r10NatureOfTransactions;
		}

		public String getR10OiopsX010() {
			return r10OiopsX010;
		}

		public void setR10OiopsX010(String r10OiopsX010) {
			this.r10OiopsX010 = r10OiopsX010;
		}

		public Date getR10DotBdX020() {
			return r10DotBdX020;
		}

		public void setR10DotBdX020(Date r10DotBdX020) {
			this.r10DotBdX020 = r10DotBdX020;
		}

		public Date getR10DotEdX030() {
			return r10DotEdX030;
		}

		public void setR10DotEdX030(Date r10DotEdX030) {
			this.r10DotEdX030 = r10DotEdX030;
		}

		public BigDecimal getR10TottX040() {
			return r10TottX040;
		}

		public void setR10TottX040(BigDecimal r10TottX040) {
			this.r10TottX040 = r10TottX040;
		}

		public BigDecimal getR10AmountX050() {
			return r10AmountX050;
		}

		public void setR10AmountX050(BigDecimal r10AmountX050) {
			this.r10AmountX050 = r10AmountX050;
		}

		public BigDecimal getR10RyotiX060() {
			return r10RyotiX060;
		}

		public void setR10RyotiX060(BigDecimal r10RyotiX060) {
			this.r10RyotiX060 = r10RyotiX060;
		}

		// Common fields getters/setters
		public Date getReportDate() {
			return reportDate;
		}

		public void setReportDate(Date reportDate) {
			this.reportDate = reportDate;
		}

		public BigDecimal getReportVersion() {
			return reportVersion;
		}

		public void setReportVersion(BigDecimal reportVersion) {
			this.reportVersion = reportVersion;
		}

		public String getReportFrequency() {
			return reportFrequency;
		}

		public void setReportFrequency(String reportFrequency) {
			this.reportFrequency = reportFrequency;
		}

		public String getReportCode() {
			return reportCode;
		}

		public void setReportCode(String reportCode) {
			this.reportCode = reportCode;
		}

		public String getReportDesc() {
			return reportDesc;
		}

		public void setReportDesc(String reportDesc) {
			this.reportDesc = reportDesc;
		}

		public String getEntityFlg() {
			return entityFlg;
		}

		public void setEntityFlg(String entityFlg) {
			this.entityFlg = entityFlg;
		}

		public String getModifyFlg() {
			return modifyFlg;
		}

		public void setModifyFlg(String modifyFlg) {
			this.modifyFlg = modifyFlg;
		}

		public String getDelFlg() {
			return delFlg;
		}

		public void setDelFlg(String delFlg) {
			this.delFlg = delFlg;
		}
	}

	// Archival Summary Entity
	@IdClass(BG_FINCON_1B_PK.class)
	public static class BRRS_BG_DBIS10_FINCON_III_1B_Archival_ST_Entity {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;

		// R7 fields
		@Column(name = "R7_TRANSACTION_SL_NO")
		private BigDecimal r7TransactionSlNo;

		@Column(name = "R7_NAME_OF_SFI")
		private String r7NameOfSfi;

		@Column(name = "R7_NAME_OF_COUNTER_PARTY")
		private String r7NameOfCounterParty;

		@Column(name = "R7_NATURE_OF_TRANSACTIONS")
		private String r7NatureOfTransactions;

		@Column(name = "R7_OIOTPS_X010")
		private String r7OiopsX010;

		@Column(name = "R7_DOT_BD_X020")
		@Temporal(TemporalType.DATE)
		private Date r7DotBdX020;

		@Column(name = "R7_DOT_ED_X030")
		@Temporal(TemporalType.DATE)
		private Date r7DotEdX030;

		@Column(name = "R7_TOTT_X040")
		private BigDecimal r7TottX040;

		@Column(name = "R7_AMOUNT_X050")
		private BigDecimal r7AmountX050;

		@Column(name = "R7_RYOTI_X060")
		private BigDecimal r7RyotiX060;

		// R8 fields
		@Column(name = "R8_TRANSACTION_SL_NO")
		private BigDecimal r8TransactionSlNo;

		@Column(name = "R8_NAME_OF_SFI")
		private String r8NameOfSfi;

		@Column(name = "R8_NAME_OF_COUNTER_PARTY")
		private String r8NameOfCounterParty;

		@Column(name = "R8_NATURE_OF_TRANSACTIONS")
		private String r8NatureOfTransactions;

		@Column(name = "R8_OIOTPS_X010")
		private String r8OiopsX010;

		@Column(name = "R8_DOT_BD_X020")
		@Temporal(TemporalType.DATE)
		private Date r8DotBdX020;

		@Column(name = "R8_DOT_ED_X030")
		@Temporal(TemporalType.DATE)
		private Date r8DotEdX030;

		@Column(name = "R8_TOTT_X040")
		private BigDecimal r8TottX040;

		@Column(name = "R8_AMOUNT_X050")
		private BigDecimal r8AmountX050;

		@Column(name = "R8_RYOTI_X060")
		private BigDecimal r8RyotiX060;

		// R9 fields
		@Column(name = "R9_TRANSACTION_SL_NO")
		private BigDecimal r9TransactionSlNo;

		@Column(name = "R9_NAME_OF_SFI")
		private String r9NameOfSfi;

		@Column(name = "R9_NAME_OF_COUNTER_PARTY")
		private String r9NameOfCounterParty;

		@Column(name = "R9_NATURE_OF_TRANSACTIONS")
		private String r9NatureOfTransactions;

		@Column(name = "R9_OIOTPS_X010")
		private String r9OiopsX010;

		@Column(name = "R9_DOT_BD_X020")
		@Temporal(TemporalType.DATE)
		private Date r9DotBdX020;

		@Column(name = "R9_DOT_ED_X030")
		@Temporal(TemporalType.DATE)
		private Date r9DotEdX030;

		@Column(name = "R9_TOTT_X040")
		private BigDecimal r9TottX040;

		@Column(name = "R9_AMOUNT_X050")
		private BigDecimal r9AmountX050;

		@Column(name = "R9_RYOTI_X060")
		private BigDecimal r9RyotiX060;

		// R10 fields
		@Column(name = "R10_TRANSACTION_SL_NO")
		private BigDecimal r10TransactionSlNo;

		@Column(name = "R10_NAME_OF_SFI")
		private String r10NameOfSfi;

		@Column(name = "R10_NAME_OF_COUNTER_PARTY")
		private String r10NameOfCounterParty;

		@Column(name = "R10_NATURE_OF_TRANSACTIONS")
		private String r10NatureOfTransactions;

		@Column(name = "R10_OIOTPS_X010")
		private String r10OiopsX010;

		@Column(name = "R10_DOT_BD_X020")
		@Temporal(TemporalType.DATE)
		private Date r10DotBdX020;

		@Column(name = "R10_DOT_ED_X030")
		@Temporal(TemporalType.DATE)
		private Date r10DotEdX030;

		@Column(name = "R10_TOTT_X040")
		private BigDecimal r10TottX040;

		@Column(name = "R10_AMOUNT_X050")
		private BigDecimal r10AmountX050;

		@Column(name = "R10_RYOTI_X060")
		private BigDecimal r10RyotiX060;

		// Common fields
		@Column(name = "REPORT_DATE")
		@Temporal(TemporalType.DATE)
		private Date reportDate;

		@Column(name = "REPORT_VERSION")
		private BigDecimal reportVersion;

		@Column(name = "REPORT_FREQUENCY")
		private String reportFrequency;

		@Column(name = "REPORT_CODE")
		private String reportCode;

		@Column(name = "REPORT_DESC")
		private String reportDesc;

		@Column(name = "ENTITY_FLG")
		private String entityFlg;

		@Column(name = "MODIFY_FLG")
		private String modifyFlg;

		@Column(name = "DEL_FLG")
		private String delFlg;

		@Column(name = "REPORT_RESUBDATE")
		private Date REPORT_RESUBDATE;

		// Getters and Setters
		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public BigDecimal getR7TransactionSlNo() {
			return r7TransactionSlNo;
		}

		public void setR7TransactionSlNo(BigDecimal r7TransactionSlNo) {
			this.r7TransactionSlNo = r7TransactionSlNo;
		}

		public String getR7NameOfSfi() {
			return r7NameOfSfi;
		}

		public void setR7NameOfSfi(String r7NameOfSfi) {
			this.r7NameOfSfi = r7NameOfSfi;
		}

		public String getR7NameOfCounterParty() {
			return r7NameOfCounterParty;
		}

		public void setR7NameOfCounterParty(String r7NameOfCounterParty) {
			this.r7NameOfCounterParty = r7NameOfCounterParty;
		}

		public String getR7NatureOfTransactions() {
			return r7NatureOfTransactions;
		}

		public void setR7NatureOfTransactions(String r7NatureOfTransactions) {
			this.r7NatureOfTransactions = r7NatureOfTransactions;
		}

		public String getR7OiopsX010() {
			return r7OiopsX010;
		}

		public void setR7OiopsX010(String r7OiopsX010) {
			this.r7OiopsX010 = r7OiopsX010;
		}

		public Date getR7DotBdX020() {
			return r7DotBdX020;
		}

		public void setR7DotBdX020(Date r7DotBdX020) {
			this.r7DotBdX020 = r7DotBdX020;
		}

		public Date getR7DotEdX030() {
			return r7DotEdX030;
		}

		public void setR7DotEdX030(Date r7DotEdX030) {
			this.r7DotEdX030 = r7DotEdX030;
		}

		public BigDecimal getR7TottX040() {
			return r7TottX040;
		}

		public void setR7TottX040(BigDecimal r7TottX040) {
			this.r7TottX040 = r7TottX040;
		}

		public BigDecimal getR7AmountX050() {
			return r7AmountX050;
		}

		public void setR7AmountX050(BigDecimal r7AmountX050) {
			this.r7AmountX050 = r7AmountX050;
		}

		public BigDecimal getR7RyotiX060() {
			return r7RyotiX060;
		}

		public void setR7RyotiX060(BigDecimal r7RyotiX060) {
			this.r7RyotiX060 = r7RyotiX060;
		}

		public BigDecimal getR8TransactionSlNo() {
			return r8TransactionSlNo;
		}

		public void setR8TransactionSlNo(BigDecimal r8TransactionSlNo) {
			this.r8TransactionSlNo = r8TransactionSlNo;
		}

		public String getR8NameOfSfi() {
			return r8NameOfSfi;
		}

		public void setR8NameOfSfi(String r8NameOfSfi) {
			this.r8NameOfSfi = r8NameOfSfi;
		}

		public String getR8NameOfCounterParty() {
			return r8NameOfCounterParty;
		}

		public void setR8NameOfCounterParty(String r8NameOfCounterParty) {
			this.r8NameOfCounterParty = r8NameOfCounterParty;
		}

		public String getR8NatureOfTransactions() {
			return r8NatureOfTransactions;
		}

		public void setR8NatureOfTransactions(String r8NatureOfTransactions) {
			this.r8NatureOfTransactions = r8NatureOfTransactions;
		}

		public String getR8OiopsX010() {
			return r8OiopsX010;
		}

		public void setR8OiopsX010(String r8OiopsX010) {
			this.r8OiopsX010 = r8OiopsX010;
		}

		public Date getR8DotBdX020() {
			return r8DotBdX020;
		}

		public void setR8DotBdX020(Date r8DotBdX020) {
			this.r8DotBdX020 = r8DotBdX020;
		}

		public Date getR8DotEdX030() {
			return r8DotEdX030;
		}

		public void setR8DotEdX030(Date r8DotEdX030) {
			this.r8DotEdX030 = r8DotEdX030;
		}

		public BigDecimal getR8TottX040() {
			return r8TottX040;
		}

		public void setR8TottX040(BigDecimal r8TottX040) {
			this.r8TottX040 = r8TottX040;
		}

		public BigDecimal getR8AmountX050() {
			return r8AmountX050;
		}

		public void setR8AmountX050(BigDecimal r8AmountX050) {
			this.r8AmountX050 = r8AmountX050;
		}

		public BigDecimal getR8RyotiX060() {
			return r8RyotiX060;
		}

		public void setR8RyotiX060(BigDecimal r8RyotiX060) {
			this.r8RyotiX060 = r8RyotiX060;
		}

		public BigDecimal getR9TransactionSlNo() {
			return r9TransactionSlNo;
		}

		public void setR9TransactionSlNo(BigDecimal r9TransactionSlNo) {
			this.r9TransactionSlNo = r9TransactionSlNo;
		}

		public String getR9NameOfSfi() {
			return r9NameOfSfi;
		}

		public void setR9NameOfSfi(String r9NameOfSfi) {
			this.r9NameOfSfi = r9NameOfSfi;
		}

		public String getR9NameOfCounterParty() {
			return r9NameOfCounterParty;
		}

		public void setR9NameOfCounterParty(String r9NameOfCounterParty) {
			this.r9NameOfCounterParty = r9NameOfCounterParty;
		}

		public String getR9NatureOfTransactions() {
			return r9NatureOfTransactions;
		}

		public void setR9NatureOfTransactions(String r9NatureOfTransactions) {
			this.r9NatureOfTransactions = r9NatureOfTransactions;
		}

		public String getR9OiopsX010() {
			return r9OiopsX010;
		}

		public void setR9OiopsX010(String r9OiopsX010) {
			this.r9OiopsX010 = r9OiopsX010;
		}

		public Date getR9DotBdX020() {
			return r9DotBdX020;
		}

		public void setR9DotBdX020(Date r9DotBdX020) {
			this.r9DotBdX020 = r9DotBdX020;
		}

		public Date getR9DotEdX030() {
			return r9DotEdX030;
		}

		public void setR9DotEdX030(Date r9DotEdX030) {
			this.r9DotEdX030 = r9DotEdX030;
		}

		public BigDecimal getR9TottX040() {
			return r9TottX040;
		}

		public void setR9TottX040(BigDecimal r9TottX040) {
			this.r9TottX040 = r9TottX040;
		}

		public BigDecimal getR9AmountX050() {
			return r9AmountX050;
		}

		public void setR9AmountX050(BigDecimal r9AmountX050) {
			this.r9AmountX050 = r9AmountX050;
		}

		public BigDecimal getR9RyotiX060() {
			return r9RyotiX060;
		}

		public void setR9RyotiX060(BigDecimal r9RyotiX060) {
			this.r9RyotiX060 = r9RyotiX060;
		}

		public BigDecimal getR10TransactionSlNo() {
			return r10TransactionSlNo;
		}

		public void setR10TransactionSlNo(BigDecimal r10TransactionSlNo) {
			this.r10TransactionSlNo = r10TransactionSlNo;
		}

		public String getR10NameOfSfi() {
			return r10NameOfSfi;
		}

		public void setR10NameOfSfi(String r10NameOfSfi) {
			this.r10NameOfSfi = r10NameOfSfi;
		}

		public String getR10NameOfCounterParty() {
			return r10NameOfCounterParty;
		}

		public void setR10NameOfCounterParty(String r10NameOfCounterParty) {
			this.r10NameOfCounterParty = r10NameOfCounterParty;
		}

		public String getR10NatureOfTransactions() {
			return r10NatureOfTransactions;
		}

		public void setR10NatureOfTransactions(String r10NatureOfTransactions) {
			this.r10NatureOfTransactions = r10NatureOfTransactions;
		}

		public String getR10OiopsX010() {
			return r10OiopsX010;
		}

		public void setR10OiopsX010(String r10OiopsX010) {
			this.r10OiopsX010 = r10OiopsX010;
		}

		public Date getR10DotBdX020() {
			return r10DotBdX020;
		}

		public void setR10DotBdX020(Date r10DotBdX020) {
			this.r10DotBdX020 = r10DotBdX020;
		}

		public Date getR10DotEdX030() {
			return r10DotEdX030;
		}

		public void setR10DotEdX030(Date r10DotEdX030) {
			this.r10DotEdX030 = r10DotEdX030;
		}

		public BigDecimal getR10TottX040() {
			return r10TottX040;
		}

		public void setR10TottX040(BigDecimal r10TottX040) {
			this.r10TottX040 = r10TottX040;
		}

		public BigDecimal getR10AmountX050() {
			return r10AmountX050;
		}

		public void setR10AmountX050(BigDecimal r10AmountX050) {
			this.r10AmountX050 = r10AmountX050;
		}

		public BigDecimal getR10RyotiX060() {
			return r10RyotiX060;
		}

		public void setR10RyotiX060(BigDecimal r10RyotiX060) {
			this.r10RyotiX060 = r10RyotiX060;
		}

		public Date getReportDate() {
			return reportDate;
		}

		public void setReportDate(Date reportDate) {
			this.reportDate = reportDate;
		}

		public BigDecimal getReportVersion() {
			return reportVersion;
		}

		public void setReportVersion(BigDecimal reportVersion) {
			this.reportVersion = reportVersion;
		}

		public String getReportFrequency() {
			return reportFrequency;
		}

		public void setReportFrequency(String reportFrequency) {
			this.reportFrequency = reportFrequency;
		}

		public String getReportCode() {
			return reportCode;
		}

		public void setReportCode(String reportCode) {
			this.reportCode = reportCode;
		}

		public String getReportDesc() {
			return reportDesc;
		}

		public void setReportDesc(String reportDesc) {
			this.reportDesc = reportDesc;
		}

		public String getEntityFlg() {
			return entityFlg;
		}

		public void setEntityFlg(String entityFlg) {
			this.entityFlg = entityFlg;
		}

		public String getModifyFlg() {
			return modifyFlg;
		}

		public void setModifyFlg(String modifyFlg) {
			this.modifyFlg = modifyFlg;
		}

		public String getDelFlg() {
			return delFlg;
		}

		public void setDelFlg(String delFlg) {
			this.delFlg = delFlg;
		}

		public Date getREPORT_RESUBDATE() {
			return REPORT_RESUBDATE;
		}

		public void setREPORT_RESUBDATE(Date REPORT_RESUBDATE) {
			this.REPORT_RESUBDATE = REPORT_RESUBDATE;
		}
	}





	// RowMapper for Summary
	class Fincon_1B_RowMapper implements RowMapper<BRRS_BG_DBIS10_FINCON_III_1B_ST_Entity> {
		@Override
		public BRRS_BG_DBIS10_FINCON_III_1B_ST_Entity mapRow(ResultSet rs, int rowNum) throws SQLException {
			BRRS_BG_DBIS10_FINCON_III_1B_ST_Entity obj = new BRRS_BG_DBIS10_FINCON_III_1B_ST_Entity();

			// R7 fields
			obj.setR7TransactionSlNo(rs.getBigDecimal("R7_TRANSACTION_SL_NO"));
			obj.setR7NameOfSfi(rs.getString("R7_NAME_OF_SFI"));
			obj.setR7NameOfCounterParty(rs.getString("R7_NAME_OF_COUNTER_PARTY"));
			obj.setR7NatureOfTransactions(rs.getString("R7_NATURE_OF_TRANSACTIONS"));
			obj.setR7OiopsX010(rs.getString("R7_OIOTPS_X010"));
			obj.setR7DotBdX020(rs.getDate("R7_DOT_BD_X020"));
			obj.setR7DotEdX030(rs.getDate("R7_DOT_ED_X030"));
			obj.setR7TottX040(rs.getBigDecimal("R7_TOTT_X040"));
			obj.setR7AmountX050(rs.getBigDecimal("R7_AMOUNT_X050"));
			obj.setR7RyotiX060(rs.getBigDecimal("R7_RYOTI_X060"));

			// R8 fields
			obj.setR8TransactionSlNo(rs.getBigDecimal("R8_TRANSACTION_SL_NO"));
			obj.setR8NameOfSfi(rs.getString("R8_NAME_OF_SFI"));
			obj.setR8NameOfCounterParty(rs.getString("R8_NAME_OF_COUNTER_PARTY"));
			obj.setR8NatureOfTransactions(rs.getString("R8_NATURE_OF_TRANSACTIONS"));
			obj.setR8OiopsX010(rs.getString("R8_OIOTPS_X010"));
			obj.setR8DotBdX020(rs.getDate("R8_DOT_BD_X020"));
			obj.setR8DotEdX030(rs.getDate("R8_DOT_ED_X030"));
			obj.setR8TottX040(rs.getBigDecimal("R8_TOTT_X040"));
			obj.setR8AmountX050(rs.getBigDecimal("R8_AMOUNT_X050"));
			obj.setR8RyotiX060(rs.getBigDecimal("R8_RYOTI_X060"));

			// R9 fields
			obj.setR9TransactionSlNo(rs.getBigDecimal("R9_TRANSACTION_SL_NO"));
			obj.setR9NameOfSfi(rs.getString("R9_NAME_OF_SFI"));
			obj.setR9NameOfCounterParty(rs.getString("R9_NAME_OF_COUNTER_PARTY"));
			obj.setR9NatureOfTransactions(rs.getString("R9_NATURE_OF_TRANSACTIONS"));
			obj.setR9OiopsX010(rs.getString("R9_OIOTPS_X010"));
			obj.setR9DotBdX020(rs.getDate("R9_DOT_BD_X020"));
			obj.setR9DotEdX030(rs.getDate("R9_DOT_ED_X030"));
			obj.setR9TottX040(rs.getBigDecimal("R9_TOTT_X040"));
			obj.setR9AmountX050(rs.getBigDecimal("R9_AMOUNT_X050"));
			obj.setR9RyotiX060(rs.getBigDecimal("R9_RYOTI_X060"));

			// R10 fields
			obj.setR10TransactionSlNo(rs.getBigDecimal("R10_TRANSACTION_SL_NO"));
			obj.setR10NameOfSfi(rs.getString("R10_NAME_OF_SFI"));
			obj.setR10NameOfCounterParty(rs.getString("R10_NAME_OF_COUNTER_PARTY"));
			obj.setR10NatureOfTransactions(rs.getString("R10_NATURE_OF_TRANSACTIONS"));
			obj.setR10OiopsX010(rs.getString("R10_OIOTPS_X010"));
			obj.setR10DotBdX020(rs.getDate("R10_DOT_BD_X020"));
			obj.setR10DotEdX030(rs.getDate("R10_DOT_ED_X030"));
			obj.setR10TottX040(rs.getBigDecimal("R10_TOTT_X040"));
			obj.setR10AmountX050(rs.getBigDecimal("R10_AMOUNT_X050"));
			obj.setR10RyotiX060(rs.getBigDecimal("R10_RYOTI_X060"));

			// Common fields
			obj.setReportDate(rs.getDate("REPORT_DATE"));
			obj.setReportVersion(rs.getBigDecimal("REPORT_VERSION"));
			obj.setReportFrequency(rs.getString("REPORT_FREQUENCY"));
			obj.setReportCode(rs.getString("REPORT_CODE"));
			obj.setReportDesc(rs.getString("REPORT_DESC"));
			obj.setEntityFlg(rs.getString("ENTITY_FLG"));
			obj.setModifyFlg(rs.getString("MODIFY_FLG"));
			obj.setDelFlg(rs.getString("DEL_FLG"));

			return obj;
		}
	}

	// RowMapper for Archival Summary
	class Fincon_1B_Archival_RowMapper implements RowMapper<BRRS_BG_DBIS10_FINCON_III_1B_Archival_ST_Entity> {
		@Override
		public BRRS_BG_DBIS10_FINCON_III_1B_Archival_ST_Entity mapRow(ResultSet rs, int rowNum) throws SQLException {
			BRRS_BG_DBIS10_FINCON_III_1B_Archival_ST_Entity obj = new BRRS_BG_DBIS10_FINCON_III_1B_Archival_ST_Entity();

			// R7 fields
			obj.setR7TransactionSlNo(rs.getBigDecimal("R7_TRANSACTION_SL_NO"));
			obj.setR7NameOfSfi(rs.getString("R7_NAME_OF_SFI"));
			obj.setR7NameOfCounterParty(rs.getString("R7_NAME_OF_COUNTER_PARTY"));
			obj.setR7NatureOfTransactions(rs.getString("R7_NATURE_OF_TRANSACTIONS"));
			obj.setR7OiopsX010(rs.getString("R7_OIOTPS_X010"));
			obj.setR7DotBdX020(rs.getDate("R7_DOT_BD_X020"));
			obj.setR7DotEdX030(rs.getDate("R7_DOT_ED_X030"));
			obj.setR7TottX040(rs.getBigDecimal("R7_TOTT_X040"));
			obj.setR7AmountX050(rs.getBigDecimal("R7_AMOUNT_X050"));
			obj.setR7RyotiX060(rs.getBigDecimal("R7_RYOTI_X060"));

			// R8 fields
			obj.setR8TransactionSlNo(rs.getBigDecimal("R8_TRANSACTION_SL_NO"));
			obj.setR8NameOfSfi(rs.getString("R8_NAME_OF_SFI"));
			obj.setR8NameOfCounterParty(rs.getString("R8_NAME_OF_COUNTER_PARTY"));
			obj.setR8NatureOfTransactions(rs.getString("R8_NATURE_OF_TRANSACTIONS"));
			obj.setR8OiopsX010(rs.getString("R8_OIOTPS_X010"));
			obj.setR8DotBdX020(rs.getDate("R8_DOT_BD_X020"));
			obj.setR8DotEdX030(rs.getDate("R8_DOT_ED_X030"));
			obj.setR8TottX040(rs.getBigDecimal("R8_TOTT_X040"));
			obj.setR8AmountX050(rs.getBigDecimal("R8_AMOUNT_X050"));
			obj.setR8RyotiX060(rs.getBigDecimal("R8_RYOTI_X060"));

			// R9 fields
			obj.setR9TransactionSlNo(rs.getBigDecimal("R9_TRANSACTION_SL_NO"));
			obj.setR9NameOfSfi(rs.getString("R9_NAME_OF_SFI"));
			obj.setR9NameOfCounterParty(rs.getString("R9_NAME_OF_COUNTER_PARTY"));
			obj.setR9NatureOfTransactions(rs.getString("R9_NATURE_OF_TRANSACTIONS"));
			obj.setR9OiopsX010(rs.getString("R9_OIOTPS_X010"));
			obj.setR9DotBdX020(rs.getDate("R9_DOT_BD_X020"));
			obj.setR9DotEdX030(rs.getDate("R9_DOT_ED_X030"));
			obj.setR9TottX040(rs.getBigDecimal("R9_TOTT_X040"));
			obj.setR9AmountX050(rs.getBigDecimal("R9_AMOUNT_X050"));
			obj.setR9RyotiX060(rs.getBigDecimal("R9_RYOTI_X060"));

			// R10 fields
			obj.setR10TransactionSlNo(rs.getBigDecimal("R10_TRANSACTION_SL_NO"));
			obj.setR10NameOfSfi(rs.getString("R10_NAME_OF_SFI"));
			obj.setR10NameOfCounterParty(rs.getString("R10_NAME_OF_COUNTER_PARTY"));
			obj.setR10NatureOfTransactions(rs.getString("R10_NATURE_OF_TRANSACTIONS"));
			obj.setR10OiopsX010(rs.getString("R10_OIOTPS_X010"));
			obj.setR10DotBdX020(rs.getDate("R10_DOT_BD_X020"));
			obj.setR10DotEdX030(rs.getDate("R10_DOT_ED_X030"));
			obj.setR10TottX040(rs.getBigDecimal("R10_TOTT_X040"));
			obj.setR10AmountX050(rs.getBigDecimal("R10_AMOUNT_X050"));
			obj.setR10RyotiX060(rs.getBigDecimal("R10_RYOTI_X060"));

			// Common fields
			obj.setReportDate(rs.getDate("REPORT_DATE"));
			obj.setREPORT_RESUBDATE(rs.getDate("REPORT_RESUBDATE"));
			obj.setReportVersion(rs.getBigDecimal("REPORT_VERSION"));
			obj.setReportFrequency(rs.getString("REPORT_FREQUENCY"));
			obj.setReportCode(rs.getString("REPORT_CODE"));
			obj.setReportDesc(rs.getString("REPORT_DESC"));
			obj.setEntityFlg(rs.getString("ENTITY_FLG"));
			obj.setModifyFlg(rs.getString("MODIFY_FLG"));
			obj.setDelFlg(rs.getString("DEL_FLG"));

			return obj;
		}
	}
	
	// Detail Entity
	public class FINCON_1B_Detail_Entity {
		private Long sno;
		private String custId;
		private String acctNumber;
		private String acctName;
		private String dataType;
		private String reportName;
		private String reportLabel;
		private String reportAddlCriteria_1;
		private String reportRemarks;
		private String modificationRemarks;
		private String dataEntryVersion;
		private BigDecimal acctBalanceInpula;
		private Date reportDate;
		private String createUser;
		private Date createTime;
		private String modifyUser;
		private Date modifyTime;
		private String verifyUser;
		private Date verifyTime;
		private String entityFlg;
		private String modifyFlg;
		private String delFlg;

		// Getters and Setters
		public Long getSno() {
			return sno;
		}

		public void setSno(Long sno) {
			this.sno = sno;
		}

		public String getCustId() {
			return custId;
		}

		public void setCustId(String custId) {
			this.custId = custId;
		}

		public String getAcctNumber() {
			return acctNumber;
		}

		public void setAcctNumber(String acctNumber) {
			this.acctNumber = acctNumber;
		}

		public String getAcctName() {
			return acctName;
		}

		public void setAcctName(String acctName) {
			this.acctName = acctName;
		}

		public String getDataType() {
			return dataType;
		}

		public void setDataType(String dataType) {
			this.dataType = dataType;
		}

		public String getReportName() {
			return reportName;
		}

		public void setReportName(String reportName) {
			this.reportName = reportName;
		}

		public String getReportLabel() {
			return reportLabel;
		}

		public void setReportLabel(String reportLabel) {
			this.reportLabel = reportLabel;
		}

		public String getReportAddlCriteria_1() {
			return reportAddlCriteria_1;
		}

		public void setReportAddlCriteria_1(String reportAddlCriteria_1) {
			this.reportAddlCriteria_1 = reportAddlCriteria_1;
		}

		public String getReportRemarks() {
			return reportRemarks;
		}

		public void setReportRemarks(String reportRemarks) {
			this.reportRemarks = reportRemarks;
		}

		public String getModificationRemarks() {
			return modificationRemarks;
		}

		public void setModificationRemarks(String modificationRemarks) {
			this.modificationRemarks = modificationRemarks;
		}

		public String getDataEntryVersion() {
			return dataEntryVersion;
		}

		public void setDataEntryVersion(String dataEntryVersion) {
			this.dataEntryVersion = dataEntryVersion;
		}

		public BigDecimal getAcctBalanceInpula() {
			return acctBalanceInpula;
		}

		public void setAcctBalanceInpula(BigDecimal acctBalanceInpula) {
			this.acctBalanceInpula = acctBalanceInpula;
		}

		public Date getReportDate() {
			return reportDate;
		}

		public void setReportDate(Date reportDate) {
			this.reportDate = reportDate;
		}

		public String getCreateUser() {
			return createUser;
		}

		public void setCreateUser(String createUser) {
			this.createUser = createUser;
		}

		public Date getCreateTime() {
			return createTime;
		}

		public void setCreateTime(Date createTime) {
			this.createTime = createTime;
		}

		public String getModifyUser() {
			return modifyUser;
		}

		public void setModifyUser(String modifyUser) {
			this.modifyUser = modifyUser;
		}

		public Date getModifyTime() {
			return modifyTime;
		}

		public void setModifyTime(Date modifyTime) {
			this.modifyTime = modifyTime;
		}

		public String getVerifyUser() {
			return verifyUser;
		}

		public void setVerifyUser(String verifyUser) {
			this.verifyUser = verifyUser;
		}

		public Date getVerifyTime() {
			return verifyTime;
		}

		public void setVerifyTime(Date verifyTime) {
			this.verifyTime = verifyTime;
		}

		public String getEntityFlg() {
			return entityFlg;
		}

		public void setEntityFlg(String entityFlg) {
			this.entityFlg = entityFlg;
		}

		public String getModifyFlg() {
			return modifyFlg;
		}

		public void setModifyFlg(String modifyFlg) {
			this.modifyFlg = modifyFlg;
		}

		public String getDelFlg() {
			return delFlg;
		}

		public void setDelFlg(String delFlg) {
			this.delFlg = delFlg;
		}
	}

	// RowMapper for Detail
	class Fincon1B_DetailRowMapper implements RowMapper<FINCON_1B_Detail_Entity> {
		@Override
		public FINCON_1B_Detail_Entity mapRow(ResultSet rs, int rowNum) throws SQLException {
			FINCON_1B_Detail_Entity obj = new FINCON_1B_Detail_Entity();

			obj.setSno(rs.getLong("SNO"));
			obj.setCustId(rs.getString("CUST_ID"));
			obj.setAcctNumber(rs.getString("ACCT_NUMBER"));
			obj.setAcctName(rs.getString("ACCT_NAME"));
			obj.setDataType(rs.getString("DATA_TYPE"));
			obj.setReportName(rs.getString("REPORT_NAME"));
			obj.setReportLabel(rs.getString("REPORT_LABEL"));
			obj.setReportAddlCriteria_1(rs.getString("REPORT_ADDL_CRITERIA_1"));
			obj.setReportRemarks(rs.getString("REPORT_REMARKS"));
			obj.setModificationRemarks(rs.getString("MODIFICATION_REMARKS"));
			obj.setDataEntryVersion(rs.getString("DATA_ENTRY_VERSION"));
			obj.setAcctBalanceInpula(rs.getBigDecimal("ACCT_BALANCE_IN_PULA"));
			obj.setReportDate(rs.getDate("REPORT_DATE"));
			obj.setCreateUser(rs.getString("CREATE_USER"));
			obj.setCreateTime(rs.getDate("CREATE_TIME"));
			obj.setModifyUser(rs.getString("MODIFY_USER"));
			obj.setModifyTime(rs.getDate("MODIFY_TIME"));
			obj.setVerifyUser(rs.getString("VERIFY_USER"));
			obj.setVerifyTime(rs.getDate("VERIFY_TIME"));
			obj.setEntityFlg(rs.getString("ENTITY_FLG"));
			obj.setModifyFlg(rs.getString("MODIFY_FLG"));
			obj.setDelFlg(rs.getString("DEL_FLG"));

			return obj;
		}
	}
	
	// Archival Detail Entity
	public class Fincon_1B_Archival_Detail_Entity {
		private Long sno;
		private String custId;
		private String acctNumber;
		private String acctName;
		private String dataType;
		private String reportName;
		private String reportLabel;
		private String reportAddlCriteria_1;
		private String reportRemarks;
		private String modificationRemarks;
		private String dataEntryVersion;
		private BigDecimal acctBalanceInpula;
		private Date reportDate;
		private String createUser;
		private Date createTime;
		private String modifyUser;
		private Date modifyTime;
		private String verifyUser;
		private Date verifyTime;
		private Character entityFlg;
		private Character modifyFlg;
		private Character delFlg;

		// Getters and Setters (same as detail entity but with Character types for
		// flags)
		public Long getSno() {
			return sno;
		}

		public void setSno(Long sno) {
			this.sno = sno;
		}

		public String getCustId() {
			return custId;
		}

		public void setCustId(String custId) {
			this.custId = custId;
		}

		public String getAcctNumber() {
			return acctNumber;
		}

		public void setAcctNumber(String acctNumber) {
			this.acctNumber = acctNumber;
		}

		public String getAcctName() {
			return acctName;
		}

		public void setAcctName(String acctName) {
			this.acctName = acctName;
		}

		public String getDataType() {
			return dataType;
		}

		public void setDataType(String dataType) {
			this.dataType = dataType;
		}

		public String getReportName() {
			return reportName;
		}

		public void setReportName(String reportName) {
			this.reportName = reportName;
		}

		public String getReportLabel() {
			return reportLabel;
		}

		public void setReportLabel(String reportLabel) {
			this.reportLabel = reportLabel;
		}

		public String getReportAddlCriteria_1() {
			return reportAddlCriteria_1;
		}

		public void setReportAddlCriteria_1(String reportAddlCriteria_1) {
			this.reportAddlCriteria_1 = reportAddlCriteria_1;
		}

		public String getReportRemarks() {
			return reportRemarks;
		}

		public void setReportRemarks(String reportRemarks) {
			this.reportRemarks = reportRemarks;
		}

		public String getModificationRemarks() {
			return modificationRemarks;
		}

		public void setModificationRemarks(String modificationRemarks) {
			this.modificationRemarks = modificationRemarks;
		}

		public String getDataEntryVersion() {
			return dataEntryVersion;
		}

		public void setDataEntryVersion(String dataEntryVersion) {
			this.dataEntryVersion = dataEntryVersion;
		}

		public BigDecimal getAcctBalanceInpula() {
			return acctBalanceInpula;
		}

		public void setAcctBalanceInpula(BigDecimal acctBalanceInpula) {
			this.acctBalanceInpula = acctBalanceInpula;
		}

		public Date getReportDate() {
			return reportDate;
		}

		public void setReportDate(Date reportDate) {
			this.reportDate = reportDate;
		}

		public String getCreateUser() {
			return createUser;
		}

		public void setCreateUser(String createUser) {
			this.createUser = createUser;
		}

		public Date getCreateTime() {
			return createTime;
		}

		public void setCreateTime(Date createTime) {
			this.createTime = createTime;
		}

		public String getModifyUser() {
			return modifyUser;
		}

		public void setModifyUser(String modifyUser) {
			this.modifyUser = modifyUser;
		}

		public Date getModifyTime() {
			return modifyTime;
		}

		public void setModifyTime(Date modifyTime) {
			this.modifyTime = modifyTime;
		}

		public String getVerifyUser() {
			return verifyUser;
		}

		public void setVerifyUser(String verifyUser) {
			this.verifyUser = verifyUser;
		}

		public Date getVerifyTime() {
			return verifyTime;
		}

		public void setVerifyTime(Date verifyTime) {
			this.verifyTime = verifyTime;
		}

		public Character getEntityFlg() {
			return entityFlg;
		}

		public void setEntityFlg(Character entityFlg) {
			this.entityFlg = entityFlg;
		}

		public Character getModifyFlg() {
			return modifyFlg;
		}

		public void setModifyFlg(Character modifyFlg) {
			this.modifyFlg = modifyFlg;
		}

		public Character getDelFlg() {
			return delFlg;
		}

		public void setDelFlg(Character delFlg) {
			this.delFlg = delFlg;
		}
	}

	// RowMapper for Archival Detail
	class Fincon_1b_ArchivalDetailRowMapper implements RowMapper<Fincon_1B_Archival_Detail_Entity> {
		@Override
		public Fincon_1B_Archival_Detail_Entity mapRow(ResultSet rs, int rowNum) throws SQLException {
			Fincon_1B_Archival_Detail_Entity obj = new Fincon_1B_Archival_Detail_Entity();

			obj.setSno(rs.getLong("SNO"));
			obj.setCustId(rs.getString("CUST_ID"));
			obj.setAcctNumber(rs.getString("ACCT_NUMBER"));
			obj.setAcctName(rs.getString("ACCT_NAME"));
			obj.setDataType(rs.getString("DATA_TYPE"));
			obj.setReportName(rs.getString("REPORT_NAME"));
			obj.setReportLabel(rs.getString("REPORT_LABEL"));
			obj.setReportAddlCriteria_1(rs.getString("REPORT_ADDL_CRITERIA_1"));
			obj.setReportRemarks(rs.getString("REPORT_REMARKS"));
			obj.setModificationRemarks(rs.getString("MODIFICATION_REMARKS"));
			obj.setDataEntryVersion(rs.getString("DATA_ENTRY_VERSION"));
			obj.setAcctBalanceInpula(rs.getBigDecimal("ACCT_BALANCE_IN_PULA"));
			obj.setReportDate(rs.getDate("REPORT_DATE"));
			obj.setCreateUser(rs.getString("CREATE_USER"));
			obj.setCreateTime(rs.getDate("CREATE_TIME"));
			obj.setModifyUser(rs.getString("MODIFY_USER"));
			obj.setModifyTime(rs.getDate("MODIFY_TIME"));
			obj.setVerifyUser(rs.getString("VERIFY_USER"));
			obj.setVerifyTime(rs.getDate("VERIFY_TIME"));

			String entityFlg = rs.getString("ENTITY_FLG");
			obj.setEntityFlg(entityFlg != null && !entityFlg.isEmpty() ? entityFlg.charAt(0) : null);

			String modifyFlg = rs.getString("MODIFY_FLG");
			obj.setModifyFlg(modifyFlg != null && !modifyFlg.isEmpty() ? modifyFlg.charAt(0) : null);

			String delFlg = rs.getString("DEL_FLG");
			obj.setDelFlg(delFlg != null && !delFlg.isEmpty() ? delFlg.charAt(0) : null);

			return obj;
		}
	}
}