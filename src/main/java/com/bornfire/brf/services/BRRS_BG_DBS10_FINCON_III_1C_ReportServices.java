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

public class BRRS_BG_DBS10_FINCON_III_1C_ReportServices {

	private static final Logger logger = LoggerFactory.getLogger(BRRS_BG_DBS10_FINCON_III_1C_ReportServices.class);

	@Autowired
	private Environment env;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy");

	// Summary view
	public ModelAndView getBG_FINCON_1C_View(String reportId, String fromdate, String todate, String currency,
			String dtltype, Pageable pageable, String type, BigDecimal version) {

		ModelAndView mv = new ModelAndView();

		System.out.println("Fincon 1C CALLED");
		System.out.println("Type = " + type);
		System.out.println("Version = " + version);

		// =====================================================
		// ARCHIVAL MODE
		// =====================================================

		if ("ARCHIVAL".equals(type) && version != null) {

			List<BRRS_BG_DBIS10_FINCON_III_1C_Archival_ST_Entity> T1Master = new ArrayList<>();

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

			List<BRRS_BG_DBIS10_FINCON_III_1C_ST_Entity> T1Master = new ArrayList<>();

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

		mv.setViewName("BRRS/BG_FINCON_1C");
		mv.addObject("displaymode", "summary");

		System.out.println("View Loaded: " + mv.getViewName());

		return mv;
	}

	// Detail view
	public ModelAndView getFincon_1C_currentDtl(String reportId, String fromdate, String todate, String currency,
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

				List<Fincon_1C_Archival_Detail_Entity> archivalDetailList;

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

				List<FINCON_1C_Detail_Entity> currentDetailList;

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

		mv.setViewName("BRRS/BG_FINCON_1C");
		mv.addObject("displaymode", "Details");
		mv.addObject("menu", reportId);
		mv.addObject("currency", currency);
		mv.addObject("reportId", reportId);

		return mv;
	}

	// Archival View
	public List<Object[]> getFincon_1C_newArchival() {
		List<Object[]> archivalList = new ArrayList<>();

		try {

			List<BRRS_BG_DBIS10_FINCON_III_1C_Archival_ST_Entity> repoData = getdatabydateListWithVersion();

			if (repoData != null && !repoData.isEmpty()) {
				for (BRRS_BG_DBIS10_FINCON_III_1C_Archival_ST_Entity entity : repoData) {
					Object[] row = new Object[] { entity.getReportDate(), entity.getReportVersion(),
							entity.getREPORT_RESUBDATE() };
					archivalList.add(row);
				}

				System.out.println("Fetched " + archivalList.size() + " archival records");
				BRRS_BG_DBIS10_FINCON_III_1C_Archival_ST_Entity first = repoData.get(0);
				System.out.println("Latest archival version: " + first.getReportVersion());
			} else {
				System.out.println("No archival data found.");
			}

		} catch (Exception e) {
			System.err.println("Error fetching Fincon_1C Archival data: " + e.getMessage());
			e.printStackTrace();
		}

		return archivalList;
	}

	// Detail Download
	public byte[] getFincon_1C_DetailExcel(String filename, String fromdate, String todate, String currency,
			String dtltype, String type, String version) {
		try {
			logger.info("Generating Excel for Fincon_1C Details...");
			System.out.println("came to Detail download service");

			if (type.equals("ARCHIVAL") & version != null) {
				byte[] ARCHIVALreport = getFincon_1C_DetailExcelARCHIVAL(filename, fromdate, todate, currency, dtltype,
						type, version);
				return ARCHIVALreport;
			}

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("BG_DBIS_FINCON_III_1C");

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

				if (i == 3) {
					cell.setCellStyle(rightAlignedHeaderStyle);
				} else {
					cell.setCellStyle(headerStyle);
				}

				sheet.setColumnWidth(i, 5000);
			}

			// Get data
			Date parsedToDate = new SimpleDateFormat("dd/MM/yyyy").parse(todate);
			List<FINCON_1C_Detail_Entity> reportData = getDetaildatabydateList(parsedToDate);

			if (reportData != null && !reportData.isEmpty()) {
				int rowIndex = 1;
				for (FINCON_1C_Detail_Entity item : reportData) {
					XSSFRow row = sheet.createRow(rowIndex++);

					row.createCell(0).setCellValue(item.getCustId());
					row.createCell(1).setCellValue(item.getAcctNumber());
					row.createCell(2).setCellValue(item.getAcctName());
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

					for (int j = 0; j < 7; j++) {
						if (j != 3) {
							row.getCell(j).setCellStyle(dataStyle);
						}
					}
				}
			} else {
				logger.info("No data found for Fincon_1C — only header will be written.");
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			workbook.write(bos);
			workbook.close();

			logger.info("Excel generation completed with {} row(s).", reportData != null ? reportData.size() : 0);
			return bos.toByteArray();

		} catch (Exception e) {
			logger.error("Error generating FINCON_1C Excel", e);
			return new byte[0];
		}
	}

	public byte[] getFincon_1C_DetailExcelARCHIVAL(String filename, String fromdate, String todate, String currency,
			String dtltype, String type, String version) {
		try {
			logger.info("Generating Excel for Fincon_1C ARCHIVAL Details...");
			System.out.println("came to ARCHIVAL Detail download service");

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("BG_DBIS_FINCON_III_1C");

			BorderStyle border = BorderStyle.THIN;

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

			CellStyle rightAlignedHeaderStyle = workbook.createCellStyle();
			rightAlignedHeaderStyle.cloneStyleFrom(headerStyle);
			rightAlignedHeaderStyle.setAlignment(HorizontalAlignment.RIGHT);

			CellStyle dataStyle = workbook.createCellStyle();
			dataStyle.setAlignment(HorizontalAlignment.LEFT);
			dataStyle.setBorderTop(border);
			dataStyle.setBorderBottom(border);
			dataStyle.setBorderLeft(border);
			dataStyle.setBorderRight(border);

			CellStyle balanceStyle = workbook.createCellStyle();
			balanceStyle.setAlignment(HorizontalAlignment.RIGHT);
			balanceStyle.setDataFormat(workbook.createDataFormat().getFormat("0"));
			balanceStyle.setBorderTop(border);
			balanceStyle.setBorderBottom(border);
			balanceStyle.setBorderLeft(border);
			balanceStyle.setBorderRight(border);

			String[] headers = { "CUST ID", "ACCT NO", "ACCT NAME", "ACCT BALANCE", "REPORT LABEL",
					"REPORT ADDL CRITERIA1", "REPORT_DATE" };

			XSSFRow headerRow = sheet.createRow(0);
			for (int i = 0; i < headers.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(headers[i]);

				if (i == 3) {
					cell.setCellStyle(rightAlignedHeaderStyle);
				} else {
					cell.setCellStyle(headerStyle);
				}

				sheet.setColumnWidth(i, 5000);
			}

			Date parsedToDate = new SimpleDateFormat("dd/MM/yyyy").parse(todate);
			List<Fincon_1C_Archival_Detail_Entity> reportData = getArchivalDetaildatabydateList(parsedToDate, version);

			if (reportData != null && !reportData.isEmpty()) {
				int rowIndex = 1;
				for (Fincon_1C_Archival_Detail_Entity item : reportData) {
					XSSFRow row = sheet.createRow(rowIndex++);

					row.createCell(0).setCellValue(item.getCustId());
					row.createCell(1).setCellValue(item.getAcctNumber());
					row.createCell(2).setCellValue(item.getAcctName());

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

					for (int j = 0; j < 7; j++) {
						if (j != 3) {
							row.getCell(j).setCellStyle(dataStyle);
						}
					}
				}
			} else {
				logger.info("No data found for Fincon_1C — only header will be written.");
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			workbook.write(bos);
			workbook.close();

			logger.info("Excel generation completed with {} row(s).", reportData != null ? reportData.size() : 0);
			return bos.toByteArray();

		} catch (Exception e) {
			logger.error("Error generating FINCON_1C Excel", e);
			return new byte[0];
		}
	}

	// Summary Download
	public byte[] getFincon_1C_Excel(String filename, String reportId, String fromdate, String todate, String currency,
			String dtltype, String type, BigDecimal version) throws Exception {
		logger.info("Service: Starting Excel generation process in memory.");

		if ("ARCHIVAL".equalsIgnoreCase(type) && version != null && version.compareTo(BigDecimal.ZERO) >= 0) {
			logger.info("Service: Generating ARCHIVAL report for version {}", version);
			return getExcelFincon_1C_ARCHIVALExcel(filename, reportId, fromdate, todate, currency, dtltype, type,
					version);
		}

		List<BRRS_BG_DBIS10_FINCON_III_1C_ST_Entity> dataList = getDataByDate(dateformat.parse(todate));

		System.out.println("DATA SIZE IS : " + dataList.size());
		if (dataList.isEmpty()) {
			logger.warn("Service: No data found for Fincon_1C report. Returning empty result.");
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

			Font font = workbook.createFont();
			font.setFontHeightInPoints((short) 8);
			font.setFontName("Arial");

			CellStyle numberStyle = workbook.createCellStyle();
			numberStyle.setBorderBottom(BorderStyle.THIN);
			numberStyle.setBorderTop(BorderStyle.THIN);
			numberStyle.setBorderLeft(BorderStyle.THIN);
			numberStyle.setBorderRight(BorderStyle.THIN);
			numberStyle.setFont(font);

			int startRow = 4;

			if (!dataList.isEmpty()) {
				for (int i = 0; i < dataList.size(); i++) {
					BRRS_BG_DBIS10_FINCON_III_1C_ST_Entity record = dataList.get(i);
					System.out.println("rownumber=" + startRow + i);
					Row row = sheet.getRow(startRow + i);
					if (row == null) {
						row = sheet.createRow(startRow + i);
					}

					// R8 fields
					Cell cell = row.createCell(3);
					if (record.getR8LeAmountsX010() != null) {
						cell.setCellValue(record.getR8LeAmountsX010().doubleValue());
						cell.setCellStyle(numberStyle);
					} else {
						cell.setCellValue(0); // IMPORTANT
						cell.setCellStyle(numberStyle);
					}

					row = sheet.getRow(7);
					cell = row.getCell(3);
					if (cell == null)
						cell = row.createCell(3);
					cell.setCellValue(
							record.getR8DpAmountsX020() != null ? record.getR8DpAmountsX020().doubleValue() : 0);

					row = sheet.getRow(7);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(
							record.getR8StlAmountsX030() != null ? record.getR8StlAmountsX030().doubleValue() : 0);

					row = sheet.getRow(7);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					cell.setCellValue(
							record.getR8EquityAmountsX040() != null ? record.getR8EquityAmountsX040().doubleValue()
									: 0);

					row = sheet.getRow(7);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					cell.setCellValue(
							record.getR8NeiBdAmountsX050() != null ? record.getR8NeiBdAmountsX050().doubleValue() : 0);

					row = sheet.getRow(7);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(
							record.getR8NeiCpAmountsX060() != null ? record.getR8NeiCpAmountsX060().doubleValue() : 0);

					row = sheet.getRow(7);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(
							record.getR8NeiCdAmountsX070() != null ? record.getR8NeiCdAmountsX070().doubleValue() : 0);

					row = sheet.getRow(7);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(
							record.getR8NeiT2bAmountsX080() != null ? record.getR8NeiT2bAmountsX080().doubleValue()
									: 0);

					row = sheet.getRow(7);
					cell = row.getCell(10);
					if (cell == null)
						cell = row.createCell(10);
					cell.setCellValue(
							record.getR8NeiUomfAmountsX090() != null ? record.getR8NeiUomfAmountsX090().doubleValue()
									: 0);

					row = sheet.getRow(7);
					cell = row.getCell(11);
					if (cell == null)
						cell = row.createCell(11);
					cell.setCellValue(
							record.getR8NeiUovfAmountsX100() != null ? record.getR8NeiUovfAmountsX100().doubleValue()
									: 0);
					cell.setCellStyle(numberStyle);

					row = sheet.getRow(7);
					cell = row.getCell(12);
					if (cell == null)
						cell = row.createCell(12);
					cell.setCellValue(
							record.getR8NeiPoieAmountsX110() != null ? record.getR8NeiPoieAmountsX110().doubleValue()
									: 0);

					row = sheet.getRow(7);
					cell = row.getCell(13);
					if (cell == null)
						cell = row.createCell(13);
					cell.setCellValue(
							record.getR8NeiPoaAmountsX120() != null ? record.getR8NeiPoaAmountsX120().doubleValue()
									: 0);

					row = sheet.getRow(7);
					cell = row.getCell(14);
					if (cell == null)
						cell = row.createCell(14);
					cell.setCellValue(record.getR8NeiOthersAmountsX130() != null
							? record.getR8NeiOthersAmountsX130().doubleValue()
							: 0);

					row = sheet.getRow(7);
					cell = row.getCell(15);
					if (cell == null)
						cell = row.createCell(15);
					cell.setCellValue(
							record.getR8TotalAmountsX140() != null ? record.getR8TotalAmountsX140().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(2);
					if (cell == null)
						cell = row.createCell(2);
					cell.setCellValue(
							record.getR9LeAmountsX010() != null ? record.getR9LeAmountsX010().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(3);
					if (cell == null)
						cell = row.createCell(3);
					cell.setCellValue(
							record.getR9DpAmountsX020() != null ? record.getR9DpAmountsX020().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(
							record.getR9StlAmountsX030() != null ? record.getR9StlAmountsX030().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					cell.setCellValue(
							record.getR9EquityAmountsX040() != null ? record.getR9EquityAmountsX040().doubleValue()
									: 0);

					row = sheet.getRow(8);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					cell.setCellValue(
							record.getR9NeiBdAmountsX050() != null ? record.getR9NeiBdAmountsX050().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(
							record.getR9NeiCpAmountsX060() != null ? record.getR9NeiCpAmountsX060().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(
							record.getR9NeiCdAmountsX070() != null ? record.getR9NeiCdAmountsX070().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(
							record.getR9NeiT2bAmountsX080() != null ? record.getR9NeiT2bAmountsX080().doubleValue()
									: 0);

					row = sheet.getRow(8);
					cell = row.getCell(10);
					if (cell == null)
						cell = row.createCell(10);
					cell.setCellValue(
							record.getR9NeiUomfAmountsX090() != null ? record.getR9NeiUomfAmountsX090().doubleValue()
									: 0);

					row = sheet.getRow(8);
					cell = row.getCell(11);
					if (cell == null)
						cell = row.createCell(11);
					cell.setCellValue(
							record.getR9NeiUovfAmountsX100() != null ? record.getR9NeiUovfAmountsX100().doubleValue()
									: 0);
					cell.setCellStyle(numberStyle);

					row = sheet.getRow(8);
					cell = row.getCell(12);
					if (cell == null)
						cell = row.createCell(12);
					cell.setCellValue(
							record.getR9NeiPoieAmountsX110() != null ? record.getR9NeiPoieAmountsX110().doubleValue()
									: 0);

					row = sheet.getRow(8);
					cell = row.getCell(13);
					if (cell == null)
						cell = row.createCell(13);
					cell.setCellValue(
							record.getR9NeiPoaAmountsX120() != null ? record.getR9NeiPoaAmountsX120().doubleValue()
									: 0);

					row = sheet.getRow(8);
					cell = row.getCell(14);
					if (cell == null)
						cell = row.createCell(14);
					cell.setCellValue(record.getR9NeiOthersAmountsX130() != null
							? record.getR9NeiOthersAmountsX130().doubleValue()
							: 0);

					row = sheet.getRow(8);
					cell = row.getCell(15);
					if (cell == null)
						cell = row.createCell(15);
					cell.setCellValue(
							record.getR9TotalAmountsX140() != null ? record.getR9TotalAmountsX140().doubleValue() : 0);

					row = sheet.getRow(9);
					cell = row.getCell(2);
					if (cell == null)
						cell = row.createCell(2);
					cell.setCellValue(
							record.getR10LeAmountsX010() != null ? record.getR10LeAmountsX010().doubleValue() : 0);

					row = sheet.getRow(9);
					cell = row.getCell(3);
					if (cell == null)
						cell = row.createCell(3);
					cell.setCellValue(
							record.getR10DpAmountsX020() != null ? record.getR10DpAmountsX020().doubleValue() : 0);

					row = sheet.getRow(9);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(
							record.getR10StlAmountsX030() != null ? record.getR10StlAmountsX030().doubleValue() : 0);

					row = sheet.getRow(9);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					cell.setCellValue(
							record.getR10EquityAmountsX040() != null ? record.getR10EquityAmountsX040().doubleValue()
									: 0);

					row = sheet.getRow(9);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					cell.setCellValue(
							record.getR10NeiBdAmountsX050() != null ? record.getR10NeiBdAmountsX050().doubleValue()
									: 0);

					row = sheet.getRow(9);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(
							record.getR10NeiCpAmountsX060() != null ? record.getR10NeiCpAmountsX060().doubleValue()
									: 0);

					row = sheet.getRow(9);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(
							record.getR10NeiCdAmountsX070() != null ? record.getR10NeiCdAmountsX070().doubleValue()
									: 0);

					row = sheet.getRow(9);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(
							record.getR10NeiT2bAmountsX080() != null ? record.getR10NeiT2bAmountsX080().doubleValue()
									: 0);

					row = sheet.getRow(9);
					cell = row.getCell(10);
					if (cell == null)
						cell = row.createCell(10);
					cell.setCellValue(
							record.getR10NeiUomfAmountsX090() != null ? record.getR10NeiUomfAmountsX090().doubleValue()
									: 0);

					row = sheet.getRow(9);
					cell = row.getCell(11);
					if (cell == null)
						cell = row.createCell(11);
					cell.setCellValue(
							record.getR10NeiUovfAmountsX100() != null ? record.getR10NeiUovfAmountsX100().doubleValue()
									: 0);
					cell.setCellStyle(numberStyle);

					row = sheet.getRow(9);
					cell = row.getCell(12);
					if (cell == null)
						cell = row.createCell(12);
					cell.setCellValue(
							record.getR10NeiPoieAmountsX110() != null ? record.getR10NeiPoieAmountsX110().doubleValue()
									: 0);

					row = sheet.getRow(9);
					cell = row.getCell(13);
					if (cell == null)
						cell = row.createCell(13);
					cell.setCellValue(
							record.getR10NeiPoaAmountsX120() != null ? record.getR10NeiPoaAmountsX120().doubleValue()
									: 0);

					row = sheet.getRow(9);
					cell = row.getCell(14);
					if (cell == null)
						cell = row.createCell(14);
					cell.setCellValue(record.getR10NeiOthersAmountsX130() != null
							? record.getR10NeiOthersAmountsX130().doubleValue()
							: 0);

					row = sheet.getRow(9);
					cell = row.getCell(15);
					if (cell == null)
						cell = row.createCell(15);
					cell.setCellValue(
							record.getR10TotalAmountsX140() != null ? record.getR10TotalAmountsX140().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(2);
					if (cell == null)
						cell = row.createCell(2);
					cell.setCellValue(
							record.getR11LeAmountsX010() != null ? record.getR11LeAmountsX010().doubleValue() : 0);

					row = sheet.getRow(10);
					cell = row.getCell(3);
					if (cell == null)
						cell = row.createCell(3);
					cell.setCellValue(
							record.getR11DpAmountsX020() != null ? record.getR11DpAmountsX020().doubleValue() : 0);

					row = sheet.getRow(10);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(
							record.getR11StlAmountsX030() != null ? record.getR11StlAmountsX030().doubleValue() : 0);

					row = sheet.getRow(10);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					cell.setCellValue(
							record.getR11EquityAmountsX040() != null ? record.getR11EquityAmountsX040().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					cell.setCellValue(
							record.getR11NeiBdAmountsX050() != null ? record.getR11NeiBdAmountsX050().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(
							record.getR11NeiCpAmountsX060() != null ? record.getR11NeiCpAmountsX060().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(
							record.getR11NeiCdAmountsX070() != null ? record.getR11NeiCdAmountsX070().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(
							record.getR11NeiT2bAmountsX080() != null ? record.getR11NeiT2bAmountsX080().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(10);
					if (cell == null)
						cell = row.createCell(10);
					cell.setCellValue(
							record.getR11NeiUomfAmountsX090() != null ? record.getR11NeiUomfAmountsX090().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(11);
					if (cell == null)
						cell = row.createCell(11);
					cell.setCellValue(
							record.getR11NeiUovfAmountsX100() != null ? record.getR11NeiUovfAmountsX100().doubleValue()
									: 0);
					cell.setCellStyle(numberStyle);

					row = sheet.getRow(10);
					cell = row.getCell(12);
					if (cell == null)
						cell = row.createCell(12);
					cell.setCellValue(
							record.getR11NeiPoieAmountsX110() != null ? record.getR11NeiPoieAmountsX110().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(13);
					if (cell == null)
						cell = row.createCell(13);
					cell.setCellValue(
							record.getR11NeiPoaAmountsX120() != null ? record.getR11NeiPoaAmountsX120().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(14);
					if (cell == null)
						cell = row.createCell(14);
					cell.setCellValue(record.getR11NeiOthersAmountsX130() != null
							? record.getR11NeiOthersAmountsX130().doubleValue()
							: 0);

					row = sheet.getRow(10);
					cell = row.getCell(15);
					if (cell == null)
						cell = row.createCell(15);
					cell.setCellValue(
							record.getR11TotalAmountsX140() != null ? record.getR11TotalAmountsX140().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(2);
					if (cell == null)
						cell = row.createCell(2);
					cell.setCellValue(
							record.getR12LeAmountsX010() != null ? record.getR12LeAmountsX010().doubleValue() : 0);

					row = sheet.getRow(11);
					cell = row.getCell(3);
					if (cell == null)
						cell = row.createCell(3);
					cell.setCellValue(
							record.getR12DpAmountsX020() != null ? record.getR12DpAmountsX020().doubleValue() : 0);

					row = sheet.getRow(11);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(
							record.getR12StlAmountsX030() != null ? record.getR12StlAmountsX030().doubleValue() : 0);

					row = sheet.getRow(11);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					cell.setCellValue(
							record.getR12EquityAmountsX040() != null ? record.getR12EquityAmountsX040().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					cell.setCellValue(
							record.getR12NeiBdAmountsX050() != null ? record.getR12NeiBdAmountsX050().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(
							record.getR12NeiCpAmountsX060() != null ? record.getR12NeiCpAmountsX060().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(
							record.getR12NeiCdAmountsX070() != null ? record.getR12NeiCdAmountsX070().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(
							record.getR12NeiT2bAmountsX080() != null ? record.getR12NeiT2bAmountsX080().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(10);
					if (cell == null)
						cell = row.createCell(10);
					cell.setCellValue(
							record.getR12NeiUomfAmountsX090() != null ? record.getR12NeiUomfAmountsX090().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(11);
					if (cell == null)
						cell = row.createCell(11);
					cell.setCellValue(
							record.getR12NeiUovfAmountsX100() != null ? record.getR12NeiUovfAmountsX100().doubleValue()
									: 0);
					cell.setCellStyle(numberStyle);

					row = sheet.getRow(11);
					cell = row.getCell(12);
					if (cell == null)
						cell = row.createCell(12);
					cell.setCellValue(
							record.getR12NeiPoieAmountsX110() != null ? record.getR12NeiPoieAmountsX110().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(13);
					if (cell == null)
						cell = row.createCell(13);
					cell.setCellValue(
							record.getR12NeiPoaAmountsX120() != null ? record.getR12NeiPoaAmountsX120().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(14);
					if (cell == null)
						cell = row.createCell(14);
					cell.setCellValue(record.getR12NeiOthersAmountsX130() != null
							? record.getR12NeiOthersAmountsX130().doubleValue()
							: 0);

					row = sheet.getRow(11);
					cell = row.getCell(15);
					if (cell == null)
						cell = row.createCell(15);
					cell.setCellValue(
							record.getR12TotalAmountsX140() != null ? record.getR12TotalAmountsX140().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(2);
					if (cell == null)
						cell = row.createCell(2);
					cell.setCellValue(
							record.getR13LeAmountsX010() != null ? record.getR13LeAmountsX010().doubleValue() : 0);

					row = sheet.getRow(12);
					cell = row.getCell(3);
					if (cell == null)
						cell = row.createCell(3);
					cell.setCellValue(
							record.getR13DpAmountsX020() != null ? record.getR13DpAmountsX020().doubleValue() : 0);

					row = sheet.getRow(12);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(
							record.getR13StlAmountsX030() != null ? record.getR13StlAmountsX030().doubleValue() : 0);

					row = sheet.getRow(12);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					cell.setCellValue(
							record.getR13EquityAmountsX040() != null ? record.getR13EquityAmountsX040().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					cell.setCellValue(
							record.getR13NeiBdAmountsX050() != null ? record.getR13NeiBdAmountsX050().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(
							record.getR13NeiCpAmountsX060() != null ? record.getR13NeiCpAmountsX060().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(
							record.getR13NeiCdAmountsX070() != null ? record.getR13NeiCdAmountsX070().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(
							record.getR13NeiT2bAmountsX080() != null ? record.getR13NeiT2bAmountsX080().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(10);
					if (cell == null)
						cell = row.createCell(10);
					cell.setCellValue(
							record.getR13NeiUomfAmountsX090() != null ? record.getR13NeiUomfAmountsX090().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(11);
					if (cell == null)
						cell = row.createCell(11);
					cell.setCellValue(
							record.getR13NeiUovfAmountsX100() != null ? record.getR13NeiUovfAmountsX100().doubleValue()
									: 0);
					cell.setCellStyle(numberStyle);

					row = sheet.getRow(12);
					cell = row.getCell(12);
					if (cell == null)
						cell = row.createCell(12);
					cell.setCellValue(
							record.getR13NeiPoieAmountsX110() != null ? record.getR13NeiPoieAmountsX110().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(13);
					if (cell == null)
						cell = row.createCell(13);
					cell.setCellValue(
							record.getR13NeiPoaAmountsX120() != null ? record.getR13NeiPoaAmountsX120().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(14);
					if (cell == null)
						cell = row.createCell(14);
					cell.setCellValue(record.getR13NeiOthersAmountsX130() != null
							? record.getR13NeiOthersAmountsX130().doubleValue()
							: 0);

					row = sheet.getRow(12);
					cell = row.getCell(15);
					if (cell == null)
						cell = row.createCell(15);
					cell.setCellValue(
							record.getR13TotalAmountsX140() != null ? record.getR13TotalAmountsX140().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(2);
					if (cell == null)
						cell = row.createCell(2);
					cell.setCellValue(
							record.getR14LeAmountsX010() != null ? record.getR14LeAmountsX010().doubleValue() : 0);

					row = sheet.getRow(13);
					cell = row.getCell(3);
					if (cell == null)
						cell = row.createCell(3);
					cell.setCellValue(
							record.getR14DpAmountsX020() != null ? record.getR14DpAmountsX020().doubleValue() : 0);

					row = sheet.getRow(13);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(
							record.getR14StlAmountsX030() != null ? record.getR14StlAmountsX030().doubleValue() : 0);

					row = sheet.getRow(13);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					cell.setCellValue(
							record.getR14EquityAmountsX040() != null ? record.getR14EquityAmountsX040().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					cell.setCellValue(
							record.getR14NeiBdAmountsX050() != null ? record.getR14NeiBdAmountsX050().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(
							record.getR14NeiCpAmountsX060() != null ? record.getR14NeiCpAmountsX060().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(
							record.getR14NeiCdAmountsX070() != null ? record.getR14NeiCdAmountsX070().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(
							record.getR14NeiT2bAmountsX080() != null ? record.getR14NeiT2bAmountsX080().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(10);
					if (cell == null)
						cell = row.createCell(10);
					cell.setCellValue(
							record.getR14NeiUomfAmountsX090() != null ? record.getR14NeiUomfAmountsX090().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(11);
					if (cell == null)
						cell = row.createCell(11);
					cell.setCellValue(
							record.getR14NeiUovfAmountsX100() != null ? record.getR14NeiUovfAmountsX100().doubleValue()
									: 0);
					cell.setCellStyle(numberStyle);

					row = sheet.getRow(13);
					cell = row.getCell(12);
					if (cell == null)
						cell = row.createCell(12);
					cell.setCellValue(
							record.getR14NeiPoieAmountsX110() != null ? record.getR14NeiPoieAmountsX110().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(13);
					if (cell == null)
						cell = row.createCell(13);
					cell.setCellValue(
							record.getR14NeiPoaAmountsX120() != null ? record.getR14NeiPoaAmountsX120().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(14);
					if (cell == null)
						cell = row.createCell(14);
					cell.setCellValue(record.getR14NeiOthersAmountsX130() != null
							? record.getR14NeiOthersAmountsX130().doubleValue()
							: 0);

					row = sheet.getRow(13);
					cell = row.getCell(15);
					if (cell == null)
						cell = row.createCell(15);
					cell.setCellValue(
							record.getR14TotalAmountsX140() != null ? record.getR14TotalAmountsX140().doubleValue()
									: 0);
				}
			}

			workbook.write(out);
			logger.info("Service: Excel data successfully written to memory buffer ({} bytes).", out.size());
			return out.toByteArray();
		}
	}

	public byte[] getExcelFincon_1C_ARCHIVALExcel(String filename, String reportId, String fromdate, String todate,
			String currency, String dtltype, String type, BigDecimal version) throws Exception {

		logger.info("Service: Starting Excel generation process in memory.");

		List<BRRS_BG_DBIS10_FINCON_III_1C_Archival_ST_Entity> dataList = getdatabydateListarchival(
				dateformat.parse(todate), version);

		if (dataList.isEmpty()) {
			logger.warn("Service: No data found for Fincon_1C report. Returning empty result.");
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

			Font font = workbook.createFont();
			font.setFontHeightInPoints((short) 8);
			font.setFontName("Arial");

			CellStyle numberStyle = workbook.createCellStyle();
			numberStyle.setBorderBottom(BorderStyle.THIN);
			numberStyle.setBorderTop(BorderStyle.THIN);
			numberStyle.setBorderLeft(BorderStyle.THIN);
			numberStyle.setBorderRight(BorderStyle.THIN);
			numberStyle.setFont(font);

			int startRow = 4;

			if (!dataList.isEmpty()) {
				for (int i = 0; i < dataList.size(); i++) {
					BRRS_BG_DBIS10_FINCON_III_1C_Archival_ST_Entity record = dataList.get(i);
					System.out.println("rownumber=" + startRow + i);
					Row row = sheet.getRow(startRow + i);
					if (row == null) {
						row = sheet.createRow(startRow + i);
					}

					// R8 fields
					Cell cell = row.createCell(3);
					if (record.getR8LeAmountsX010() != null) {
						cell.setCellValue(record.getR8LeAmountsX010().doubleValue());
						cell.setCellStyle(numberStyle);
					} else {
						cell.setCellValue(0); // IMPORTANT
						cell.setCellStyle(numberStyle);
					}

					row = sheet.getRow(7);
					cell = row.getCell(3);
					if (cell == null)
						cell = row.createCell(3);
					cell.setCellValue(
							record.getR8DpAmountsX020() != null ? record.getR8DpAmountsX020().doubleValue() : 0);

					row = sheet.getRow(7);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(
							record.getR8StlAmountsX030() != null ? record.getR8StlAmountsX030().doubleValue() : 0);

					row = sheet.getRow(7);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					cell.setCellValue(
							record.getR8EquityAmountsX040() != null ? record.getR8EquityAmountsX040().doubleValue()
									: 0);

					row = sheet.getRow(7);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					cell.setCellValue(
							record.getR8NeiBdAmountsX050() != null ? record.getR8NeiBdAmountsX050().doubleValue() : 0);

					row = sheet.getRow(7);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(
							record.getR8NeiCpAmountsX060() != null ? record.getR8NeiCpAmountsX060().doubleValue() : 0);

					row = sheet.getRow(7);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(
							record.getR8NeiCdAmountsX070() != null ? record.getR8NeiCdAmountsX070().doubleValue() : 0);

					row = sheet.getRow(7);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(
							record.getR8NeiT2bAmountsX080() != null ? record.getR8NeiT2bAmountsX080().doubleValue()
									: 0);

					row = sheet.getRow(7);
					cell = row.getCell(10);
					if (cell == null)
						cell = row.createCell(10);
					cell.setCellValue(
							record.getR8NeiUomfAmountsX090() != null ? record.getR8NeiUomfAmountsX090().doubleValue()
									: 0);

					row = sheet.getRow(7);
					cell = row.getCell(11);
					if (cell == null)
						cell = row.createCell(11);
					cell.setCellValue(
							record.getR8NeiUovfAmountsX100() != null ? record.getR8NeiUovfAmountsX100().doubleValue()
									: 0);
					cell.setCellStyle(numberStyle);

					row = sheet.getRow(7);
					cell = row.getCell(12);
					if (cell == null)
						cell = row.createCell(12);
					cell.setCellValue(
							record.getR8NeiPoieAmountsX110() != null ? record.getR8NeiPoieAmountsX110().doubleValue()
									: 0);

					row = sheet.getRow(7);
					cell = row.getCell(13);
					if (cell == null)
						cell = row.createCell(13);
					cell.setCellValue(
							record.getR8NeiPoaAmountsX120() != null ? record.getR8NeiPoaAmountsX120().doubleValue()
									: 0);

					row = sheet.getRow(7);
					cell = row.getCell(14);
					if (cell == null)
						cell = row.createCell(14);
					cell.setCellValue(record.getR8NeiOthersAmountsX130() != null
							? record.getR8NeiOthersAmountsX130().doubleValue()
							: 0);

					row = sheet.getRow(7);
					cell = row.getCell(15);
					if (cell == null)
						cell = row.createCell(15);
					cell.setCellValue(
							record.getR8TotalAmountsX140() != null ? record.getR8TotalAmountsX140().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(2);
					if (cell == null)
						cell = row.createCell(2);
					cell.setCellValue(
							record.getR9LeAmountsX010() != null ? record.getR9LeAmountsX010().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(3);
					if (cell == null)
						cell = row.createCell(3);
					cell.setCellValue(
							record.getR9DpAmountsX020() != null ? record.getR9DpAmountsX020().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(
							record.getR9StlAmountsX030() != null ? record.getR9StlAmountsX030().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					cell.setCellValue(
							record.getR9EquityAmountsX040() != null ? record.getR9EquityAmountsX040().doubleValue()
									: 0);

					row = sheet.getRow(8);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					cell.setCellValue(
							record.getR9NeiBdAmountsX050() != null ? record.getR9NeiBdAmountsX050().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(
							record.getR9NeiCpAmountsX060() != null ? record.getR9NeiCpAmountsX060().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(
							record.getR9NeiCdAmountsX070() != null ? record.getR9NeiCdAmountsX070().doubleValue() : 0);

					row = sheet.getRow(8);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(
							record.getR9NeiT2bAmountsX080() != null ? record.getR9NeiT2bAmountsX080().doubleValue()
									: 0);

					row = sheet.getRow(8);
					cell = row.getCell(10);
					if (cell == null)
						cell = row.createCell(10);
					cell.setCellValue(
							record.getR9NeiUomfAmountsX090() != null ? record.getR9NeiUomfAmountsX090().doubleValue()
									: 0);

					row = sheet.getRow(8);
					cell = row.getCell(11);
					if (cell == null)
						cell = row.createCell(11);
					cell.setCellValue(
							record.getR9NeiUovfAmountsX100() != null ? record.getR9NeiUovfAmountsX100().doubleValue()
									: 0);
					cell.setCellStyle(numberStyle);

					row = sheet.getRow(8);
					cell = row.getCell(12);
					if (cell == null)
						cell = row.createCell(12);
					cell.setCellValue(
							record.getR9NeiPoieAmountsX110() != null ? record.getR9NeiPoieAmountsX110().doubleValue()
									: 0);

					row = sheet.getRow(8);
					cell = row.getCell(13);
					if (cell == null)
						cell = row.createCell(13);
					cell.setCellValue(
							record.getR9NeiPoaAmountsX120() != null ? record.getR9NeiPoaAmountsX120().doubleValue()
									: 0);

					row = sheet.getRow(8);
					cell = row.getCell(14);
					if (cell == null)
						cell = row.createCell(14);
					cell.setCellValue(record.getR9NeiOthersAmountsX130() != null
							? record.getR9NeiOthersAmountsX130().doubleValue()
							: 0);

					row = sheet.getRow(8);
					cell = row.getCell(15);
					if (cell == null)
						cell = row.createCell(15);
					cell.setCellValue(
							record.getR9TotalAmountsX140() != null ? record.getR9TotalAmountsX140().doubleValue() : 0);

					row = sheet.getRow(9);
					cell = row.getCell(2);
					if (cell == null)
						cell = row.createCell(2);
					cell.setCellValue(
							record.getR10LeAmountsX010() != null ? record.getR10LeAmountsX010().doubleValue() : 0);

					row = sheet.getRow(9);
					cell = row.getCell(3);
					if (cell == null)
						cell = row.createCell(3);
					cell.setCellValue(
							record.getR10DpAmountsX020() != null ? record.getR10DpAmountsX020().doubleValue() : 0);

					row = sheet.getRow(9);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(
							record.getR10StlAmountsX030() != null ? record.getR10StlAmountsX030().doubleValue() : 0);

					row = sheet.getRow(9);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					cell.setCellValue(
							record.getR10EquityAmountsX040() != null ? record.getR10EquityAmountsX040().doubleValue()
									: 0);

					row = sheet.getRow(9);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					cell.setCellValue(
							record.getR10NeiBdAmountsX050() != null ? record.getR10NeiBdAmountsX050().doubleValue()
									: 0);

					row = sheet.getRow(9);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(
							record.getR10NeiCpAmountsX060() != null ? record.getR10NeiCpAmountsX060().doubleValue()
									: 0);

					row = sheet.getRow(9);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(
							record.getR10NeiCdAmountsX070() != null ? record.getR10NeiCdAmountsX070().doubleValue()
									: 0);

					row = sheet.getRow(9);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(
							record.getR10NeiT2bAmountsX080() != null ? record.getR10NeiT2bAmountsX080().doubleValue()
									: 0);

					row = sheet.getRow(9);
					cell = row.getCell(10);
					if (cell == null)
						cell = row.createCell(10);
					cell.setCellValue(
							record.getR10NeiUomfAmountsX090() != null ? record.getR10NeiUomfAmountsX090().doubleValue()
									: 0);

					row = sheet.getRow(9);
					cell = row.getCell(11);
					if (cell == null)
						cell = row.createCell(11);
					cell.setCellValue(
							record.getR10NeiUovfAmountsX100() != null ? record.getR10NeiUovfAmountsX100().doubleValue()
									: 0);
					cell.setCellStyle(numberStyle);

					row = sheet.getRow(9);
					cell = row.getCell(12);
					if (cell == null)
						cell = row.createCell(12);
					cell.setCellValue(
							record.getR10NeiPoieAmountsX110() != null ? record.getR10NeiPoieAmountsX110().doubleValue()
									: 0);

					row = sheet.getRow(9);
					cell = row.getCell(13);
					if (cell == null)
						cell = row.createCell(13);
					cell.setCellValue(
							record.getR10NeiPoaAmountsX120() != null ? record.getR10NeiPoaAmountsX120().doubleValue()
									: 0);

					row = sheet.getRow(9);
					cell = row.getCell(14);
					if (cell == null)
						cell = row.createCell(14);
					cell.setCellValue(record.getR10NeiOthersAmountsX130() != null
							? record.getR10NeiOthersAmountsX130().doubleValue()
							: 0);

					row = sheet.getRow(9);
					cell = row.getCell(15);
					if (cell == null)
						cell = row.createCell(15);
					cell.setCellValue(
							record.getR10TotalAmountsX140() != null ? record.getR10TotalAmountsX140().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(2);
					if (cell == null)
						cell = row.createCell(2);
					cell.setCellValue(
							record.getR11LeAmountsX010() != null ? record.getR11LeAmountsX010().doubleValue() : 0);

					row = sheet.getRow(10);
					cell = row.getCell(3);
					if (cell == null)
						cell = row.createCell(3);
					cell.setCellValue(
							record.getR11DpAmountsX020() != null ? record.getR11DpAmountsX020().doubleValue() : 0);

					row = sheet.getRow(10);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(
							record.getR11StlAmountsX030() != null ? record.getR11StlAmountsX030().doubleValue() : 0);

					row = sheet.getRow(10);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					cell.setCellValue(
							record.getR11EquityAmountsX040() != null ? record.getR11EquityAmountsX040().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					cell.setCellValue(
							record.getR11NeiBdAmountsX050() != null ? record.getR11NeiBdAmountsX050().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(
							record.getR11NeiCpAmountsX060() != null ? record.getR11NeiCpAmountsX060().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(
							record.getR11NeiCdAmountsX070() != null ? record.getR11NeiCdAmountsX070().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(
							record.getR11NeiT2bAmountsX080() != null ? record.getR11NeiT2bAmountsX080().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(10);
					if (cell == null)
						cell = row.createCell(10);
					cell.setCellValue(
							record.getR11NeiUomfAmountsX090() != null ? record.getR11NeiUomfAmountsX090().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(11);
					if (cell == null)
						cell = row.createCell(11);
					cell.setCellValue(
							record.getR11NeiUovfAmountsX100() != null ? record.getR11NeiUovfAmountsX100().doubleValue()
									: 0);
					cell.setCellStyle(numberStyle);

					row = sheet.getRow(10);
					cell = row.getCell(12);
					if (cell == null)
						cell = row.createCell(12);
					cell.setCellValue(
							record.getR11NeiPoieAmountsX110() != null ? record.getR11NeiPoieAmountsX110().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(13);
					if (cell == null)
						cell = row.createCell(13);
					cell.setCellValue(
							record.getR11NeiPoaAmountsX120() != null ? record.getR11NeiPoaAmountsX120().doubleValue()
									: 0);

					row = sheet.getRow(10);
					cell = row.getCell(14);
					if (cell == null)
						cell = row.createCell(14);
					cell.setCellValue(record.getR11NeiOthersAmountsX130() != null
							? record.getR11NeiOthersAmountsX130().doubleValue()
							: 0);

					row = sheet.getRow(10);
					cell = row.getCell(15);
					if (cell == null)
						cell = row.createCell(15);
					cell.setCellValue(
							record.getR11TotalAmountsX140() != null ? record.getR11TotalAmountsX140().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(2);
					if (cell == null)
						cell = row.createCell(2);
					cell.setCellValue(
							record.getR12LeAmountsX010() != null ? record.getR12LeAmountsX010().doubleValue() : 0);

					row = sheet.getRow(11);
					cell = row.getCell(3);
					if (cell == null)
						cell = row.createCell(3);
					cell.setCellValue(
							record.getR12DpAmountsX020() != null ? record.getR12DpAmountsX020().doubleValue() : 0);

					row = sheet.getRow(11);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(
							record.getR12StlAmountsX030() != null ? record.getR12StlAmountsX030().doubleValue() : 0);

					row = sheet.getRow(11);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					cell.setCellValue(
							record.getR12EquityAmountsX040() != null ? record.getR12EquityAmountsX040().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					cell.setCellValue(
							record.getR12NeiBdAmountsX050() != null ? record.getR12NeiBdAmountsX050().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(
							record.getR12NeiCpAmountsX060() != null ? record.getR12NeiCpAmountsX060().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(
							record.getR12NeiCdAmountsX070() != null ? record.getR12NeiCdAmountsX070().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(
							record.getR12NeiT2bAmountsX080() != null ? record.getR12NeiT2bAmountsX080().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(10);
					if (cell == null)
						cell = row.createCell(10);
					cell.setCellValue(
							record.getR12NeiUomfAmountsX090() != null ? record.getR12NeiUomfAmountsX090().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(11);
					if (cell == null)
						cell = row.createCell(11);
					cell.setCellValue(
							record.getR12NeiUovfAmountsX100() != null ? record.getR12NeiUovfAmountsX100().doubleValue()
									: 0);
					cell.setCellStyle(numberStyle);

					row = sheet.getRow(11);
					cell = row.getCell(12);
					if (cell == null)
						cell = row.createCell(12);
					cell.setCellValue(
							record.getR12NeiPoieAmountsX110() != null ? record.getR12NeiPoieAmountsX110().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(13);
					if (cell == null)
						cell = row.createCell(13);
					cell.setCellValue(
							record.getR12NeiPoaAmountsX120() != null ? record.getR12NeiPoaAmountsX120().doubleValue()
									: 0);

					row = sheet.getRow(11);
					cell = row.getCell(14);
					if (cell == null)
						cell = row.createCell(14);
					cell.setCellValue(record.getR12NeiOthersAmountsX130() != null
							? record.getR12NeiOthersAmountsX130().doubleValue()
							: 0);

					row = sheet.getRow(11);
					cell = row.getCell(15);
					if (cell == null)
						cell = row.createCell(15);
					cell.setCellValue(
							record.getR12TotalAmountsX140() != null ? record.getR12TotalAmountsX140().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(2);
					if (cell == null)
						cell = row.createCell(2);
					cell.setCellValue(
							record.getR13LeAmountsX010() != null ? record.getR13LeAmountsX010().doubleValue() : 0);

					row = sheet.getRow(12);
					cell = row.getCell(3);
					if (cell == null)
						cell = row.createCell(3);
					cell.setCellValue(
							record.getR13DpAmountsX020() != null ? record.getR13DpAmountsX020().doubleValue() : 0);

					row = sheet.getRow(12);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(
							record.getR13StlAmountsX030() != null ? record.getR13StlAmountsX030().doubleValue() : 0);

					row = sheet.getRow(12);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					cell.setCellValue(
							record.getR13EquityAmountsX040() != null ? record.getR13EquityAmountsX040().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					cell.setCellValue(
							record.getR13NeiBdAmountsX050() != null ? record.getR13NeiBdAmountsX050().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(
							record.getR13NeiCpAmountsX060() != null ? record.getR13NeiCpAmountsX060().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(
							record.getR13NeiCdAmountsX070() != null ? record.getR13NeiCdAmountsX070().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(
							record.getR13NeiT2bAmountsX080() != null ? record.getR13NeiT2bAmountsX080().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(10);
					if (cell == null)
						cell = row.createCell(10);
					cell.setCellValue(
							record.getR13NeiUomfAmountsX090() != null ? record.getR13NeiUomfAmountsX090().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(11);
					if (cell == null)
						cell = row.createCell(11);
					cell.setCellValue(
							record.getR13NeiUovfAmountsX100() != null ? record.getR13NeiUovfAmountsX100().doubleValue()
									: 0);
					cell.setCellStyle(numberStyle);

					row = sheet.getRow(12);
					cell = row.getCell(12);
					if (cell == null)
						cell = row.createCell(12);
					cell.setCellValue(
							record.getR13NeiPoieAmountsX110() != null ? record.getR13NeiPoieAmountsX110().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(13);
					if (cell == null)
						cell = row.createCell(13);
					cell.setCellValue(
							record.getR13NeiPoaAmountsX120() != null ? record.getR13NeiPoaAmountsX120().doubleValue()
									: 0);

					row = sheet.getRow(12);
					cell = row.getCell(14);
					if (cell == null)
						cell = row.createCell(14);
					cell.setCellValue(record.getR13NeiOthersAmountsX130() != null
							? record.getR13NeiOthersAmountsX130().doubleValue()
							: 0);

					row = sheet.getRow(12);
					cell = row.getCell(15);
					if (cell == null)
						cell = row.createCell(15);
					cell.setCellValue(
							record.getR13TotalAmountsX140() != null ? record.getR13TotalAmountsX140().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(2);
					if (cell == null)
						cell = row.createCell(2);
					cell.setCellValue(
							record.getR14LeAmountsX010() != null ? record.getR14LeAmountsX010().doubleValue() : 0);

					row = sheet.getRow(13);
					cell = row.getCell(3);
					if (cell == null)
						cell = row.createCell(3);
					cell.setCellValue(
							record.getR14DpAmountsX020() != null ? record.getR14DpAmountsX020().doubleValue() : 0);

					row = sheet.getRow(13);
					cell = row.getCell(4);
					if (cell == null)
						cell = row.createCell(4);
					cell.setCellValue(
							record.getR14StlAmountsX030() != null ? record.getR14StlAmountsX030().doubleValue() : 0);

					row = sheet.getRow(13);
					cell = row.getCell(5);
					if (cell == null)
						cell = row.createCell(5);
					cell.setCellValue(
							record.getR14EquityAmountsX040() != null ? record.getR14EquityAmountsX040().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(6);
					if (cell == null)
						cell = row.createCell(6);
					cell.setCellValue(
							record.getR14NeiBdAmountsX050() != null ? record.getR14NeiBdAmountsX050().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(7);
					if (cell == null)
						cell = row.createCell(7);
					cell.setCellValue(
							record.getR14NeiCpAmountsX060() != null ? record.getR14NeiCpAmountsX060().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(8);
					if (cell == null)
						cell = row.createCell(8);
					cell.setCellValue(
							record.getR14NeiCdAmountsX070() != null ? record.getR14NeiCdAmountsX070().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(9);
					if (cell == null)
						cell = row.createCell(9);
					cell.setCellValue(
							record.getR14NeiT2bAmountsX080() != null ? record.getR14NeiT2bAmountsX080().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(10);
					if (cell == null)
						cell = row.createCell(10);
					cell.setCellValue(
							record.getR14NeiUomfAmountsX090() != null ? record.getR14NeiUomfAmountsX090().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(11);
					if (cell == null)
						cell = row.createCell(11);
					cell.setCellValue(
							record.getR14NeiUovfAmountsX100() != null ? record.getR14NeiUovfAmountsX100().doubleValue()
									: 0);
					cell.setCellStyle(numberStyle);

					row = sheet.getRow(13);
					cell = row.getCell(12);
					if (cell == null)
						cell = row.createCell(12);
					cell.setCellValue(
							record.getR14NeiPoieAmountsX110() != null ? record.getR14NeiPoieAmountsX110().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(13);
					if (cell == null)
						cell = row.createCell(13);
					cell.setCellValue(
							record.getR14NeiPoaAmountsX120() != null ? record.getR14NeiPoaAmountsX120().doubleValue()
									: 0);

					row = sheet.getRow(13);
					cell = row.getCell(14);
					if (cell == null)
						cell = row.createCell(14);
					cell.setCellValue(record.getR14NeiOthersAmountsX130() != null
							? record.getR14NeiOthersAmountsX130().doubleValue()
							: 0);

					row = sheet.getRow(13);
					cell = row.getCell(15);
					if (cell == null)
						cell = row.createCell(15);
					cell.setCellValue(
							record.getR14TotalAmountsX140() != null ? record.getR14TotalAmountsX140().doubleValue()
									: 0);
				}
			}

			workbook.write(out);
			logger.info("Service: Excel data successfully written to memory buffer ({} bytes).", out.size());
			return out.toByteArray();
		}
	}

	// Fetch Summary data by report date
	public List<BRRS_BG_DBIS10_FINCON_III_1C_ST_Entity> getDataByDate(Date reportDate) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1C_SUMMARYTABLE WHERE REPORT_DATE = ?";
		return jdbcTemplate.query(sql, new Object[] { reportDate }, new Fincon_1C_RowMapper());
	}

	// GET REPORT_DATE + REPORT_VERSION
	public List<Object[]> getDBIS10_FINCON_1C_archival() {
		String sql = "SELECT REPORT_DATE, REPORT_VERSION, REPORT_RESUBDATE "
				+ "FROM BRRS_BG_DBIS10_FINCON_III_1C_ARCHIVALTABLE_SUMMARY " + "ORDER BY REPORT_VERSION";
		return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[] { rs.getDate("REPORT_DATE"),
				rs.getBigDecimal("REPORT_VERSION"), rs.getDate("REPORT_RESUBDATE") });
	}

	// GET ARCHIVAL FULL DATA BY DATE + VERSION
	public List<BRRS_BG_DBIS10_FINCON_III_1C_Archival_ST_Entity> getdatabydateListarchival(Date reportDate,
			BigDecimal reportVersion) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1C_ARCHIVALTABLE_SUMMARY "
				+ "WHERE REPORT_DATE = ? AND REPORT_VERSION = ?";
		return jdbcTemplate.query(sql, new Object[] { reportDate, reportVersion }, new Fincon_1C_Archival_RowMapper());
	}

	// GET ALL WITH VERSION
	public List<BRRS_BG_DBIS10_FINCON_III_1C_Archival_ST_Entity> getdatabydateListWithVersion() {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1C_ARCHIVALTABLE_SUMMARY "
				+ "WHERE REPORT_VERSION IS NOT NULL ORDER BY REPORT_VERSION ASC";
		return jdbcTemplate.query(sql, new Fincon_1C_Archival_RowMapper());
	}

	// GET MAX VERSION BY DATE
	public BigDecimal findMaxVersion(Date reportDate) {
		String sql = "SELECT MAX(REPORT_VERSION) " + "FROM BRRS_BG_DBIS10_FINCON_III_1C_ARCHIVALTABLE_SUMMARY "
				+ "WHERE REPORT_DATE = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { reportDate }, BigDecimal.class);
	}

	// DETAIL QUERIES
	public List<FINCON_1C_Detail_Entity> findByDetailReportDateAndLabelAndCriteria(Date reportDate, String reportLabel,
			String reportAddlCriteria1) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1C_DETAILTABLE "
				+ "WHERE REPORT_DATE = ? AND REPORT_LABEL = ? AND REPORT_ADDL_CRITERIA_1 = ?";
		return jdbcTemplate.query(sql, new Object[] { reportDate, reportLabel, reportAddlCriteria1 },
				new Fincon1C_DetailRowMapper());
	}

	public List<FINCON_1C_Detail_Entity> getDetaildatabydateList(Date reportdate) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1C_DETAILTABLE WHERE REPORT_DATE = ?";
		return jdbcTemplate.query(sql, new Object[] { reportdate }, new Fincon1C_DetailRowMapper());
	}

	public List<FINCON_1C_Detail_Entity> getDetaildatabydateList(Date reportdate, int offset, int limit) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1C_DETAILTABLE "
				+ "WHERE REPORT_DATE = ? OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
		return jdbcTemplate.query(sql, new Object[] { reportdate, offset, limit }, new Fincon1C_DetailRowMapper());
	}

	public int getDetaildatacount(Date reportdate) {
		String sql = "SELECT COUNT(*) FROM BRRS_BG_DBIS10_FINCON_III_1C_DETAILTABLE WHERE REPORT_DATE = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { reportdate }, Integer.class);
	}

	public List<FINCON_1C_Detail_Entity> GetDetailDataByRowIdAndColumnId(String reportLabel, String reportAddlCriteria1,
			Date reportdate) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1C_DETAILTABLE "
				+ "WHERE REPORT_LABEL = ? AND REPORT_ADDL_CRITERIA_1 = ? AND REPORT_DATE = ?";
		return jdbcTemplate.query(sql, new Object[] { reportLabel, reportAddlCriteria1, reportdate },
				new Fincon1C_DetailRowMapper());
	}

	public FINCON_1C_Detail_Entity findByAcctnumber(String acct_number) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1C_DETAILTABLE WHERE ACCT_NUMBER = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { acct_number }, new Fincon1C_DetailRowMapper());
	}

	public FINCON_1C_Detail_Entity findBySno(String sno) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1C_DETAILTABLE WHERE SNO = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { sno }, new Fincon1C_DetailRowMapper());
	}

	// ARCHIVAL DETAIL QUERIES
	public List<Fincon_1C_Archival_Detail_Entity> getArchivalDetaildatabydateList(Date reportdate,
			String dataEntryVersion) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1C_ARCHIVALTABLE_DETAIL "
				+ "WHERE REPORT_DATE = ? AND DATA_ENTRY_VERSION = ?";
		return jdbcTemplate.query(sql, new Object[] { reportdate, dataEntryVersion },
				new Fincon_1c_ArchivalDetailRowMapper());
	}

	public List<Fincon_1C_Archival_Detail_Entity> GetArchivalDataByRowIdAndColumnId(String reportLabel,
			String reportAddlCriteria1, Date reportdate, String dataEntryVersion) {
		String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1C_ARCHIVALTABLE_DETAIL "
				+ "WHERE REPORT_LABEL = ? AND REPORT_ADDL_CRITERIA_1 = ? "
				+ "AND REPORT_DATE = ? AND DATA_ENTRY_VERSION = ?";
		return jdbcTemplate.query(sql, new Object[] { reportLabel, reportAddlCriteria1, reportdate, dataEntryVersion },
				new Fincon_1c_ArchivalDetailRowMapper());
	}

	// Summary Entity
	public static class BRRS_BG_DBIS10_FINCON_III_1C_ST_Entity {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;

		// R8 fields
		@Column(name = "R8_NAME_OF_SFI")
		private String r8NameOfSfi;

		@Column(name = "R8_NAME_OF_COUNTERPARTY")
		private String r8NameOfCounterparty;

		@Column(name = "R8_LE_AMOUNTS_X010")
		private BigDecimal r8LeAmountsX010;

		@Column(name = "R8_DP_AMOUNTS_X020")
		private BigDecimal r8DpAmountsX020;

		@Column(name = "R8_STL_AMOUNTS_X030")
		private BigDecimal r8StlAmountsX030;

		@Column(name = "R8_EQUITY_AMOUNTS_X040")
		private BigDecimal r8EquityAmountsX040;

		@Column(name = "R8_NEI_BD_AMOUNTS_X050")
		private BigDecimal r8NeiBdAmountsX050;

		@Column(name = "R8_NEI_CP_AMOUNTS_X060")
		private BigDecimal r8NeiCpAmountsX060;

		@Column(name = "R8_NEI_CD_AMOUNTS_X070")
		private BigDecimal r8NeiCdAmountsX070;

		@Column(name = "R8_NEI_T2B_AMOUNTS_X080")
		private BigDecimal r8NeiT2bAmountsX080;

		@Column(name = "R8_NEI_UOMF_AMOUNTS_X090")
		private BigDecimal r8NeiUomfAmountsX090;

		@Column(name = "R8_NEI_UOVF_AMOUNTS_X100")
		private BigDecimal r8NeiUovfAmountsX100;

		@Column(name = "R8_NEI_POIE_AMOUNTS_X110")
		private BigDecimal r8NeiPoieAmountsX110;

		@Column(name = "R8_NEI_POA_AMOUNTS_X120")
		private BigDecimal r8NeiPoaAmountsX120;

		@Column(name = "R8_NEI_OTHERS_AMOUNTS_X130")
		private BigDecimal r8NeiOthersAmountsX130;

		@Column(name = "R8_TOTAL_AMOUNTS_X140")
		private BigDecimal r8TotalAmountsX140;

		// R9 fields
		@Column(name = "R9_NAME_OF_SFI")
		private String r9NameOfSfi;

		@Column(name = "R9_NAME_OF_COUNTERPARTY")
		private String r9NameOfCounterparty;

		@Column(name = "R9_LE_AMOUNTS_X010")
		private BigDecimal r9LeAmountsX010;

		@Column(name = "R9_DP_AMOUNTS_X020")
		private BigDecimal r9DpAmountsX020;

		@Column(name = "R9_STL_AMOUNTS_X030")
		private BigDecimal r9StlAmountsX030;

		@Column(name = "R9_EQUITY_AMOUNTS_X040")
		private BigDecimal r9EquityAmountsX040;

		@Column(name = "R9_NEI_BD_AMOUNTS_X050")
		private BigDecimal r9NeiBdAmountsX050;

		@Column(name = "R9_NEI_CP_AMOUNTS_X060")
		private BigDecimal r9NeiCpAmountsX060;

		@Column(name = "R9_NEI_CD_AMOUNTS_X070")
		private BigDecimal r9NeiCdAmountsX070;

		@Column(name = "R9_NEI_T2B_AMOUNTS_X080")
		private BigDecimal r9NeiT2bAmountsX080;

		@Column(name = "R9_NEI_UOMF_AMOUNTS_X090")
		private BigDecimal r9NeiUomfAmountsX090;

		@Column(name = "R9_NEI_UOVF_AMOUNTS_X100")
		private BigDecimal r9NeiUovfAmountsX100;

		@Column(name = "R9_NEI_POIE_AMOUNTS_X110")
		private BigDecimal r9NeiPoieAmountsX110;

		@Column(name = "R9_NEI_POA_AMOUNTS_X120")
		private BigDecimal r9NeiPoaAmountsX120;

		@Column(name = "R9_NEI_OTHERS_AMOUNTS_X130")
		private BigDecimal r9NeiOthersAmountsX130;

		@Column(name = "R9_TOTAL_AMOUNTS_X140")
		private BigDecimal r9TotalAmountsX140;

		// R10 fields
		@Column(name = "R10_NAME_OF_SFI")
		private String r10NameOfSfi;

		@Column(name = "R10_NAME_OF_COUNTERPARTY")
		private String r10NameOfCounterparty;

		@Column(name = "R10_LE_AMOUNTS_X010")
		private BigDecimal r10LeAmountsX010;

		@Column(name = "R10_DP_AMOUNTS_X020")
		private BigDecimal r10DpAmountsX020;

		@Column(name = "R10_STL_AMOUNTS_X030")
		private BigDecimal r10StlAmountsX030;

		@Column(name = "R10_EQUITY_AMOUNTS_X040")
		private BigDecimal r10EquityAmountsX040;

		@Column(name = "R10_NEI_BD_AMOUNTS_X050")
		private BigDecimal r10NeiBdAmountsX050;

		@Column(name = "R10_NEI_CP_AMOUNTS_X060")
		private BigDecimal r10NeiCpAmountsX060;

		@Column(name = "R10_NEI_CD_AMOUNTS_X070")
		private BigDecimal r10NeiCdAmountsX070;

		@Column(name = "R10_NEI_T2B_AMOUNTS_X080")
		private BigDecimal r10NeiT2bAmountsX080;

		@Column(name = "R10_NEI_UOMF_AMOUNTS_X090")
		private BigDecimal r10NeiUomfAmountsX090;

		@Column(name = "R10_NEI_UOVF_AMOUNTS_X100")
		private BigDecimal r10NeiUovfAmountsX100;

		@Column(name = "R10_NEI_POIE_AMOUNTS_X110")
		private BigDecimal r10NeiPoieAmountsX110;

		@Column(name = "R10_NEI_POA_AMOUNTS_X120")
		private BigDecimal r10NeiPoaAmountsX120;

		@Column(name = "R10_NEI_OTHERS_AMOUNTS_X130")
		private BigDecimal r10NeiOthersAmountsX130;

		@Column(name = "R10_TOTAL_AMOUNTS_X140")
		private BigDecimal r10TotalAmountsX140;

		// R11 fields
		@Column(name = "R11_NAME_OF_SFI")
		private String r11NameOfSfi;

		@Column(name = "R11_NAME_OF_COUNTERPARTY")
		private String r11NameOfCounterparty;

		@Column(name = "R11_LE_AMOUNTS_X010")
		private BigDecimal r11LeAmountsX010;

		@Column(name = "R11_DP_AMOUNTS_X020")
		private BigDecimal r11DpAmountsX020;

		@Column(name = "R11_STL_AMOUNTS_X030")
		private BigDecimal r11StlAmountsX030;

		@Column(name = "R11_EQUITY_AMOUNTS_X040")
		private BigDecimal r11EquityAmountsX040;

		@Column(name = "R11_NEI_BD_AMOUNTS_X050")
		private BigDecimal r11NeiBdAmountsX050;

		@Column(name = "R11_NEI_CP_AMOUNTS_X060")
		private BigDecimal r11NeiCpAmountsX060;

		@Column(name = "R11_NEI_CD_AMOUNTS_X070")
		private BigDecimal r11NeiCdAmountsX070;

		@Column(name = "R11_NEI_T2B_AMOUNTS_X080")
		private BigDecimal r11NeiT2bAmountsX080;

		@Column(name = "R11_NEI_UOMF_AMOUNTS_X090")
		private BigDecimal r11NeiUomfAmountsX090;

		@Column(name = "R11_NEI_UOVF_AMOUNTS_X100")
		private BigDecimal r11NeiUovfAmountsX100;

		@Column(name = "R11_NEI_POIE_AMOUNTS_X110")
		private BigDecimal r11NeiPoieAmountsX110;

		@Column(name = "R11_NEI_POA_AMOUNTS_X120")
		private BigDecimal r11NeiPoaAmountsX120;

		@Column(name = "R11_NEI_OTHERS_AMOUNTS_X130")
		private BigDecimal r11NeiOthersAmountsX130;

		@Column(name = "R11_TOTAL_AMOUNTS_X140")
		private BigDecimal r11TotalAmountsX140;

		// R12 fields
		@Column(name = "R12_NAME_OF_SFI")
		private String r12NameOfSfi;

		@Column(name = "R12_NAME_OF_COUNTERPARTY")
		private String r12NameOfCounterparty;

		@Column(name = "R12_LE_AMOUNTS_X010")
		private BigDecimal r12LeAmountsX010;

		@Column(name = "R12_DP_AMOUNTS_X020")
		private BigDecimal r12DpAmountsX020;

		@Column(name = "R12_STL_AMOUNTS_X030")
		private BigDecimal r12StlAmountsX030;

		@Column(name = "R12_EQUITY_AMOUNTS_X040")
		private BigDecimal r12EquityAmountsX040;

		@Column(name = "R12_NEI_BD_AMOUNTS_X050")
		private BigDecimal r12NeiBdAmountsX050;

		@Column(name = "R12_NEI_CP_AMOUNTS_X060")
		private BigDecimal r12NeiCpAmountsX060;

		@Column(name = "R12_NEI_CD_AMOUNTS_X070")
		private BigDecimal r12NeiCdAmountsX070;

		@Column(name = "R12_NEI_T2B_AMOUNTS_X080")
		private BigDecimal r12NeiT2bAmountsX080;

		@Column(name = "R12_NEI_UOMF_AMOUNTS_X090")
		private BigDecimal r12NeiUomfAmountsX090;

		@Column(name = "R12_NEI_UOVF_AMOUNTS_X100")
		private BigDecimal r12NeiUovfAmountsX100;

		@Column(name = "R12_NEI_POIE_AMOUNTS_X110")
		private BigDecimal r12NeiPoieAmountsX110;

		@Column(name = "R12_NEI_POA_AMOUNTS_X120")
		private BigDecimal r12NeiPoaAmountsX120;

		@Column(name = "R12_NEI_OTHERS_AMOUNTS_X130")
		private BigDecimal r12NeiOthersAmountsX130;

		@Column(name = "R12_TOTAL_AMOUNTS_X140")
		private BigDecimal r12TotalAmountsX140;

		// R13 fields
		@Column(name = "R13_NAME_OF_SFI")
		private String r13NameOfSfi;

		@Column(name = "R13_NAME_OF_COUNTERPARTY")
		private String r13NameOfCounterparty;

		@Column(name = "R13_LE_AMOUNTS_X010")
		private BigDecimal r13LeAmountsX010;

		@Column(name = "R13_DP_AMOUNTS_X020")
		private BigDecimal r13DpAmountsX020;

		@Column(name = "R13_STL_AMOUNTS_X030")
		private BigDecimal r13StlAmountsX030;

		@Column(name = "R13_EQUITY_AMOUNTS_X040")
		private BigDecimal r13EquityAmountsX040;

		@Column(name = "R13_NEI_BD_AMOUNTS_X050")
		private BigDecimal r13NeiBdAmountsX050;

		@Column(name = "R13_NEI_CP_AMOUNTS_X060")
		private BigDecimal r13NeiCpAmountsX060;

		@Column(name = "R13_NEI_CD_AMOUNTS_X070")
		private BigDecimal r13NeiCdAmountsX070;

		@Column(name = "R13_NEI_T2B_AMOUNTS_X080")
		private BigDecimal r13NeiT2bAmountsX080;

		@Column(name = "R13_NEI_UOMF_AMOUNTS_X090")
		private BigDecimal r13NeiUomfAmountsX090;

		@Column(name = "R13_NEI_UOVF_AMOUNTS_X100")
		private BigDecimal r13NeiUovfAmountsX100;

		@Column(name = "R13_NEI_POIE_AMOUNTS_X110")
		private BigDecimal r13NeiPoieAmountsX110;

		@Column(name = "R13_NEI_POA_AMOUNTS_X120")
		private BigDecimal r13NeiPoaAmountsX120;

		@Column(name = "R13_NEI_OTHERS_AMOUNTS_X130")
		private BigDecimal r13NeiOthersAmountsX130;

		@Column(name = "R13_TOTAL_AMOUNTS_X140")
		private BigDecimal r13TotalAmountsX140;

		// R14 fields
		@Column(name = "R14_NAME_OF_SFI")
		private String r14NameOfSfi;

		@Column(name = "R14_NAME_OF_COUNTERPARTY")
		private String r14NameOfCounterparty;

		@Column(name = "R14_LE_AMOUNTS_X010")
		private BigDecimal r14LeAmountsX010;

		@Column(name = "R14_DP_AMOUNTS_X020")
		private BigDecimal r14DpAmountsX020;

		@Column(name = "R14_STL_AMOUNTS_X030")
		private BigDecimal r14StlAmountsX030;

		@Column(name = "R14_EQUITY_AMOUNTS_X040")
		private BigDecimal r14EquityAmountsX040;

		@Column(name = "R14_NEI_BD_AMOUNTS_X050")
		private BigDecimal r14NeiBdAmountsX050;

		@Column(name = "R14_NEI_CP_AMOUNTS_X060")
		private BigDecimal r14NeiCpAmountsX060;

		@Column(name = "R14_NEI_CD_AMOUNTS_X070")
		private BigDecimal r14NeiCdAmountsX070;

		@Column(name = "R14_NEI_T2B_AMOUNTS_X080")
		private BigDecimal r14NeiT2bAmountsX080;

		@Column(name = "R14_NEI_UOMF_AMOUNTS_X090")
		private BigDecimal r14NeiUomfAmountsX090;

		@Column(name = "R14_NEI_UOVF_AMOUNTS_X100")
		private BigDecimal r14NeiUovfAmountsX100;

		@Column(name = "R14_NEI_POIE_AMOUNTS_X110")
		private BigDecimal r14NeiPoieAmountsX110;

		@Column(name = "R14_NEI_POA_AMOUNTS_X120")
		private BigDecimal r14NeiPoaAmountsX120;

		@Column(name = "R14_NEI_OTHERS_AMOUNTS_X130")
		private BigDecimal r14NeiOthersAmountsX130;

		@Column(name = "R14_TOTAL_AMOUNTS_X140")
		private BigDecimal r14TotalAmountsX140;

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

		// Getters and Setters for R8
		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getR8NameOfSfi() {
			return r8NameOfSfi;
		}

		public void setR8NameOfSfi(String r8NameOfSfi) {
			this.r8NameOfSfi = r8NameOfSfi;
		}

		public String getR8NameOfCounterparty() {
			return r8NameOfCounterparty;
		}

		public void setR8NameOfCounterparty(String r8NameOfCounterparty) {
			this.r8NameOfCounterparty = r8NameOfCounterparty;
		}

		public BigDecimal getR8LeAmountsX010() {
			return r8LeAmountsX010;
		}

		public void setR8LeAmountsX010(BigDecimal r8LeAmountsX010) {
			this.r8LeAmountsX010 = r8LeAmountsX010;
		}

		public BigDecimal getR8DpAmountsX020() {
			return r8DpAmountsX020;
		}

		public void setR8DpAmountsX020(BigDecimal r8DpAmountsX020) {
			this.r8DpAmountsX020 = r8DpAmountsX020;
		}

		public BigDecimal getR8StlAmountsX030() {
			return r8StlAmountsX030;
		}

		public void setR8StlAmountsX030(BigDecimal r8StlAmountsX030) {
			this.r8StlAmountsX030 = r8StlAmountsX030;
		}

		public BigDecimal getR8EquityAmountsX040() {
			return r8EquityAmountsX040;
		}

		public void setR8EquityAmountsX040(BigDecimal r8EquityAmountsX040) {
			this.r8EquityAmountsX040 = r8EquityAmountsX040;
		}

		public BigDecimal getR8NeiBdAmountsX050() {
			return r8NeiBdAmountsX050;
		}

		public void setR8NeiBdAmountsX050(BigDecimal r8NeiBdAmountsX050) {
			this.r8NeiBdAmountsX050 = r8NeiBdAmountsX050;
		}

		public BigDecimal getR8NeiCpAmountsX060() {
			return r8NeiCpAmountsX060;
		}

		public void setR8NeiCpAmountsX060(BigDecimal r8NeiCpAmountsX060) {
			this.r8NeiCpAmountsX060 = r8NeiCpAmountsX060;
		}

		public BigDecimal getR8NeiCdAmountsX070() {
			return r8NeiCdAmountsX070;
		}

		public void setR8NeiCdAmountsX070(BigDecimal r8NeiCdAmountsX070) {
			this.r8NeiCdAmountsX070 = r8NeiCdAmountsX070;
		}

		public BigDecimal getR8NeiT2bAmountsX080() {
			return r8NeiT2bAmountsX080;
		}

		public void setR8NeiT2bAmountsX080(BigDecimal r8NeiT2bAmountsX080) {
			this.r8NeiT2bAmountsX080 = r8NeiT2bAmountsX080;
		}

		public BigDecimal getR8NeiUomfAmountsX090() {
			return r8NeiUomfAmountsX090;
		}

		public void setR8NeiUomfAmountsX090(BigDecimal r8NeiUomfAmountsX090) {
			this.r8NeiUomfAmountsX090 = r8NeiUomfAmountsX090;
		}

		public BigDecimal getR8NeiUovfAmountsX100() {
			return r8NeiUovfAmountsX100;
		}

		public void setR8NeiUovfAmountsX100(BigDecimal r8NeiUovfAmountsX100) {
			this.r8NeiUovfAmountsX100 = r8NeiUovfAmountsX100;
		}

		public BigDecimal getR8NeiPoieAmountsX110() {
			return r8NeiPoieAmountsX110;
		}

		public void setR8NeiPoieAmountsX110(BigDecimal r8NeiPoieAmountsX110) {
			this.r8NeiPoieAmountsX110 = r8NeiPoieAmountsX110;
		}

		public BigDecimal getR8NeiPoaAmountsX120() {
			return r8NeiPoaAmountsX120;
		}

		public void setR8NeiPoaAmountsX120(BigDecimal r8NeiPoaAmountsX120) {
			this.r8NeiPoaAmountsX120 = r8NeiPoaAmountsX120;
		}

		public BigDecimal getR8NeiOthersAmountsX130() {
			return r8NeiOthersAmountsX130;
		}

		public void setR8NeiOthersAmountsX130(BigDecimal r8NeiOthersAmountsX130) {
			this.r8NeiOthersAmountsX130 = r8NeiOthersAmountsX130;
		}

		public BigDecimal getR8TotalAmountsX140() {
			return r8TotalAmountsX140;
		}

		public void setR8TotalAmountsX140(BigDecimal r8TotalAmountsX140) {
			this.r8TotalAmountsX140 = r8TotalAmountsX140;
		}

		// Getters and Setters for R9
		public String getR9NameOfSfi() {
			return r9NameOfSfi;
		}

		public void setR9NameOfSfi(String r9NameOfSfi) {
			this.r9NameOfSfi = r9NameOfSfi;
		}

		public String getR9NameOfCounterparty() {
			return r9NameOfCounterparty;
		}

		public void setR9NameOfCounterparty(String r9NameOfCounterparty) {
			this.r9NameOfCounterparty = r9NameOfCounterparty;
		}

		public BigDecimal getR9LeAmountsX010() {
			return r9LeAmountsX010;
		}

		public void setR9LeAmountsX010(BigDecimal r9LeAmountsX010) {
			this.r9LeAmountsX010 = r9LeAmountsX010;
		}

		public BigDecimal getR9DpAmountsX020() {
			return r9DpAmountsX020;
		}

		public void setR9DpAmountsX020(BigDecimal r9DpAmountsX020) {
			this.r9DpAmountsX020 = r9DpAmountsX020;
		}

		public BigDecimal getR9StlAmountsX030() {
			return r9StlAmountsX030;
		}

		public void setR9StlAmountsX030(BigDecimal r9StlAmountsX030) {
			this.r9StlAmountsX030 = r9StlAmountsX030;
		}

		public BigDecimal getR9EquityAmountsX040() {
			return r9EquityAmountsX040;
		}

		public void setR9EquityAmountsX040(BigDecimal r9EquityAmountsX040) {
			this.r9EquityAmountsX040 = r9EquityAmountsX040;
		}

		public BigDecimal getR9NeiBdAmountsX050() {
			return r9NeiBdAmountsX050;
		}

		public void setR9NeiBdAmountsX050(BigDecimal r9NeiBdAmountsX050) {
			this.r9NeiBdAmountsX050 = r9NeiBdAmountsX050;
		}

		public BigDecimal getR9NeiCpAmountsX060() {
			return r9NeiCpAmountsX060;
		}

		public void setR9NeiCpAmountsX060(BigDecimal r9NeiCpAmountsX060) {
			this.r9NeiCpAmountsX060 = r9NeiCpAmountsX060;
		}

		public BigDecimal getR9NeiCdAmountsX070() {
			return r9NeiCdAmountsX070;
		}

		public void setR9NeiCdAmountsX070(BigDecimal r9NeiCdAmountsX070) {
			this.r9NeiCdAmountsX070 = r9NeiCdAmountsX070;
		}

		public BigDecimal getR9NeiT2bAmountsX080() {
			return r9NeiT2bAmountsX080;
		}

		public void setR9NeiT2bAmountsX080(BigDecimal r9NeiT2bAmountsX080) {
			this.r9NeiT2bAmountsX080 = r9NeiT2bAmountsX080;
		}

		public BigDecimal getR9NeiUomfAmountsX090() {
			return r9NeiUomfAmountsX090;
		}

		public void setR9NeiUomfAmountsX090(BigDecimal r9NeiUomfAmountsX090) {
			this.r9NeiUomfAmountsX090 = r9NeiUomfAmountsX090;
		}

		public BigDecimal getR9NeiUovfAmountsX100() {
			return r9NeiUovfAmountsX100;
		}

		public void setR9NeiUovfAmountsX100(BigDecimal r9NeiUovfAmountsX100) {
			this.r9NeiUovfAmountsX100 = r9NeiUovfAmountsX100;
		}

		public BigDecimal getR9NeiPoieAmountsX110() {
			return r9NeiPoieAmountsX110;
		}

		public void setR9NeiPoieAmountsX110(BigDecimal r9NeiPoieAmountsX110) {
			this.r9NeiPoieAmountsX110 = r9NeiPoieAmountsX110;
		}

		public BigDecimal getR9NeiPoaAmountsX120() {
			return r9NeiPoaAmountsX120;
		}

		public void setR9NeiPoaAmountsX120(BigDecimal r9NeiPoaAmountsX120) {
			this.r9NeiPoaAmountsX120 = r9NeiPoaAmountsX120;
		}

		public BigDecimal getR9NeiOthersAmountsX130() {
			return r9NeiOthersAmountsX130;
		}

		public void setR9NeiOthersAmountsX130(BigDecimal r9NeiOthersAmountsX130) {
			this.r9NeiOthersAmountsX130 = r9NeiOthersAmountsX130;
		}

		public BigDecimal getR9TotalAmountsX140() {
			return r9TotalAmountsX140;
		}

		public void setR9TotalAmountsX140(BigDecimal r9TotalAmountsX140) {
			this.r9TotalAmountsX140 = r9TotalAmountsX140;
		}

		// Getters and Setters for R10
		public String getR10NameOfSfi() {
			return r10NameOfSfi;
		}

		public void setR10NameOfSfi(String r10NameOfSfi) {
			this.r10NameOfSfi = r10NameOfSfi;
		}

		public String getR10NameOfCounterparty() {
			return r10NameOfCounterparty;
		}

		public void setR10NameOfCounterparty(String r10NameOfCounterparty) {
			this.r10NameOfCounterparty = r10NameOfCounterparty;
		}

		public BigDecimal getR10LeAmountsX010() {
			return r10LeAmountsX010;
		}

		public void setR10LeAmountsX010(BigDecimal r10LeAmountsX010) {
			this.r10LeAmountsX010 = r10LeAmountsX010;
		}

		public BigDecimal getR10DpAmountsX020() {
			return r10DpAmountsX020;
		}

		public void setR10DpAmountsX020(BigDecimal r10DpAmountsX020) {
			this.r10DpAmountsX020 = r10DpAmountsX020;
		}

		public BigDecimal getR10StlAmountsX030() {
			return r10StlAmountsX030;
		}

		public void setR10StlAmountsX030(BigDecimal r10StlAmountsX030) {
			this.r10StlAmountsX030 = r10StlAmountsX030;
		}

		public BigDecimal getR10EquityAmountsX040() {
			return r10EquityAmountsX040;
		}

		public void setR10EquityAmountsX040(BigDecimal r10EquityAmountsX040) {
			this.r10EquityAmountsX040 = r10EquityAmountsX040;
		}

		public BigDecimal getR10NeiBdAmountsX050() {
			return r10NeiBdAmountsX050;
		}

		public void setR10NeiBdAmountsX050(BigDecimal r10NeiBdAmountsX050) {
			this.r10NeiBdAmountsX050 = r10NeiBdAmountsX050;
		}

		public BigDecimal getR10NeiCpAmountsX060() {
			return r10NeiCpAmountsX060;
		}

		public void setR10NeiCpAmountsX060(BigDecimal r10NeiCpAmountsX060) {
			this.r10NeiCpAmountsX060 = r10NeiCpAmountsX060;
		}

		public BigDecimal getR10NeiCdAmountsX070() {
			return r10NeiCdAmountsX070;
		}

		public void setR10NeiCdAmountsX070(BigDecimal r10NeiCdAmountsX070) {
			this.r10NeiCdAmountsX070 = r10NeiCdAmountsX070;
		}

		public BigDecimal getR10NeiT2bAmountsX080() {
			return r10NeiT2bAmountsX080;
		}

		public void setR10NeiT2bAmountsX080(BigDecimal r10NeiT2bAmountsX080) {
			this.r10NeiT2bAmountsX080 = r10NeiT2bAmountsX080;
		}

		public BigDecimal getR10NeiUomfAmountsX090() {
			return r10NeiUomfAmountsX090;
		}

		public void setR10NeiUomfAmountsX090(BigDecimal r10NeiUomfAmountsX090) {
			this.r10NeiUomfAmountsX090 = r10NeiUomfAmountsX090;
		}

		public BigDecimal getR10NeiUovfAmountsX100() {
			return r10NeiUovfAmountsX100;
		}

		public void setR10NeiUovfAmountsX100(BigDecimal r10NeiUovfAmountsX100) {
			this.r10NeiUovfAmountsX100 = r10NeiUovfAmountsX100;
		}

		public BigDecimal getR10NeiPoieAmountsX110() {
			return r10NeiPoieAmountsX110;
		}

		public void setR10NeiPoieAmountsX110(BigDecimal r10NeiPoieAmountsX110) {
			this.r10NeiPoieAmountsX110 = r10NeiPoieAmountsX110;
		}

		public BigDecimal getR10NeiPoaAmountsX120() {
			return r10NeiPoaAmountsX120;
		}

		public void setR10NeiPoaAmountsX120(BigDecimal r10NeiPoaAmountsX120) {
			this.r10NeiPoaAmountsX120 = r10NeiPoaAmountsX120;
		}

		public BigDecimal getR10NeiOthersAmountsX130() {
			return r10NeiOthersAmountsX130;
		}

		public void setR10NeiOthersAmountsX130(BigDecimal r10NeiOthersAmountsX130) {
			this.r10NeiOthersAmountsX130 = r10NeiOthersAmountsX130;
		}

		public BigDecimal getR10TotalAmountsX140() {
			return r10TotalAmountsX140;
		}

		public void setR10TotalAmountsX140(BigDecimal r10TotalAmountsX140) {
			this.r10TotalAmountsX140 = r10TotalAmountsX140;
		}

		// Getters and Setters for R11
		public String getR11NameOfSfi() {
			return r11NameOfSfi;
		}

		public void setR11NameOfSfi(String r11NameOfSfi) {
			this.r11NameOfSfi = r11NameOfSfi;
		}

		public String getR11NameOfCounterparty() {
			return r11NameOfCounterparty;
		}

		public void setR11NameOfCounterparty(String r11NameOfCounterparty) {
			this.r11NameOfCounterparty = r11NameOfCounterparty;
		}

		public BigDecimal getR11LeAmountsX010() {
			return r11LeAmountsX010;
		}

		public void setR11LeAmountsX010(BigDecimal r11LeAmountsX010) {
			this.r11LeAmountsX010 = r11LeAmountsX010;
		}

		public BigDecimal getR11DpAmountsX020() {
			return r11DpAmountsX020;
		}

		public void setR11DpAmountsX020(BigDecimal r11DpAmountsX020) {
			this.r11DpAmountsX020 = r11DpAmountsX020;
		}

		public BigDecimal getR11StlAmountsX030() {
			return r11StlAmountsX030;
		}

		public void setR11StlAmountsX030(BigDecimal r11StlAmountsX030) {
			this.r11StlAmountsX030 = r11StlAmountsX030;
		}

		public BigDecimal getR11EquityAmountsX040() {
			return r11EquityAmountsX040;
		}

		public void setR11EquityAmountsX040(BigDecimal r11EquityAmountsX040) {
			this.r11EquityAmountsX040 = r11EquityAmountsX040;
		}

		public BigDecimal getR11NeiBdAmountsX050() {
			return r11NeiBdAmountsX050;
		}

		public void setR11NeiBdAmountsX050(BigDecimal r11NeiBdAmountsX050) {
			this.r11NeiBdAmountsX050 = r11NeiBdAmountsX050;
		}

		public BigDecimal getR11NeiCpAmountsX060() {
			return r11NeiCpAmountsX060;
		}

		public void setR11NeiCpAmountsX060(BigDecimal r11NeiCpAmountsX060) {
			this.r11NeiCpAmountsX060 = r11NeiCpAmountsX060;
		}

		public BigDecimal getR11NeiCdAmountsX070() {
			return r11NeiCdAmountsX070;
		}

		public void setR11NeiCdAmountsX070(BigDecimal r11NeiCdAmountsX070) {
			this.r11NeiCdAmountsX070 = r11NeiCdAmountsX070;
		}

		public BigDecimal getR11NeiT2bAmountsX080() {
			return r11NeiT2bAmountsX080;
		}

		public void setR11NeiT2bAmountsX080(BigDecimal r11NeiT2bAmountsX080) {
			this.r11NeiT2bAmountsX080 = r11NeiT2bAmountsX080;
		}

		public BigDecimal getR11NeiUomfAmountsX090() {
			return r11NeiUomfAmountsX090;
		}

		public void setR11NeiUomfAmountsX090(BigDecimal r11NeiUomfAmountsX090) {
			this.r11NeiUomfAmountsX090 = r11NeiUomfAmountsX090;
		}

		public BigDecimal getR11NeiUovfAmountsX100() {
			return r11NeiUovfAmountsX100;
		}

		public void setR11NeiUovfAmountsX100(BigDecimal r11NeiUovfAmountsX100) {
			this.r11NeiUovfAmountsX100 = r11NeiUovfAmountsX100;
		}

		public BigDecimal getR11NeiPoieAmountsX110() {
			return r11NeiPoieAmountsX110;
		}

		public void setR11NeiPoieAmountsX110(BigDecimal r11NeiPoieAmountsX110) {
			this.r11NeiPoieAmountsX110 = r11NeiPoieAmountsX110;
		}

		public BigDecimal getR11NeiPoaAmountsX120() {
			return r11NeiPoaAmountsX120;
		}

		public void setR11NeiPoaAmountsX120(BigDecimal r11NeiPoaAmountsX120) {
			this.r11NeiPoaAmountsX120 = r11NeiPoaAmountsX120;
		}

		public BigDecimal getR11NeiOthersAmountsX130() {
			return r11NeiOthersAmountsX130;
		}

		public void setR11NeiOthersAmountsX130(BigDecimal r11NeiOthersAmountsX130) {
			this.r11NeiOthersAmountsX130 = r11NeiOthersAmountsX130;
		}

		public BigDecimal getR11TotalAmountsX140() {
			return r11TotalAmountsX140;
		}

		public void setR11TotalAmountsX140(BigDecimal r11TotalAmountsX140) {
			this.r11TotalAmountsX140 = r11TotalAmountsX140;
		}

		// Getters and Setters for R12
		public String getR12NameOfSfi() {
			return r12NameOfSfi;
		}

		public void setR12NameOfSfi(String r12NameOfSfi) {
			this.r12NameOfSfi = r12NameOfSfi;
		}

		public String getR12NameOfCounterparty() {
			return r12NameOfCounterparty;
		}

		public void setR12NameOfCounterparty(String r12NameOfCounterparty) {
			this.r12NameOfCounterparty = r12NameOfCounterparty;
		}

		public BigDecimal getR12LeAmountsX010() {
			return r12LeAmountsX010;
		}

		public void setR12LeAmountsX010(BigDecimal r12LeAmountsX010) {
			this.r12LeAmountsX010 = r12LeAmountsX010;
		}

		public BigDecimal getR12DpAmountsX020() {
			return r12DpAmountsX020;
		}

		public void setR12DpAmountsX020(BigDecimal r12DpAmountsX020) {
			this.r12DpAmountsX020 = r12DpAmountsX020;
		}

		public BigDecimal getR12StlAmountsX030() {
			return r12StlAmountsX030;
		}

		public void setR12StlAmountsX030(BigDecimal r12StlAmountsX030) {
			this.r12StlAmountsX030 = r12StlAmountsX030;
		}

		public BigDecimal getR12EquityAmountsX040() {
			return r12EquityAmountsX040;
		}

		public void setR12EquityAmountsX040(BigDecimal r12EquityAmountsX040) {
			this.r12EquityAmountsX040 = r12EquityAmountsX040;
		}

		public BigDecimal getR12NeiBdAmountsX050() {
			return r12NeiBdAmountsX050;
		}

		public void setR12NeiBdAmountsX050(BigDecimal r12NeiBdAmountsX050) {
			this.r12NeiBdAmountsX050 = r12NeiBdAmountsX050;
		}

		public BigDecimal getR12NeiCpAmountsX060() {
			return r12NeiCpAmountsX060;
		}

		public void setR12NeiCpAmountsX060(BigDecimal r12NeiCpAmountsX060) {
			this.r12NeiCpAmountsX060 = r12NeiCpAmountsX060;
		}

		public BigDecimal getR12NeiCdAmountsX070() {
			return r12NeiCdAmountsX070;
		}

		public void setR12NeiCdAmountsX070(BigDecimal r12NeiCdAmountsX070) {
			this.r12NeiCdAmountsX070 = r12NeiCdAmountsX070;
		}

		public BigDecimal getR12NeiT2bAmountsX080() {
			return r12NeiT2bAmountsX080;
		}

		public void setR12NeiT2bAmountsX080(BigDecimal r12NeiT2bAmountsX080) {
			this.r12NeiT2bAmountsX080 = r12NeiT2bAmountsX080;
		}

		public BigDecimal getR12NeiUomfAmountsX090() {
			return r12NeiUomfAmountsX090;
		}

		public void setR12NeiUomfAmountsX090(BigDecimal r12NeiUomfAmountsX090) {
			this.r12NeiUomfAmountsX090 = r12NeiUomfAmountsX090;
		}

		public BigDecimal getR12NeiUovfAmountsX100() {
			return r12NeiUovfAmountsX100;
		}

		public void setR12NeiUovfAmountsX100(BigDecimal r12NeiUovfAmountsX100) {
			this.r12NeiUovfAmountsX100 = r12NeiUovfAmountsX100;
		}

		public BigDecimal getR12NeiPoieAmountsX110() {
			return r12NeiPoieAmountsX110;
		}

		public void setR12NeiPoieAmountsX110(BigDecimal r12NeiPoieAmountsX110) {
			this.r12NeiPoieAmountsX110 = r12NeiPoieAmountsX110;
		}

		public BigDecimal getR12NeiPoaAmountsX120() {
			return r12NeiPoaAmountsX120;
		}

		public void setR12NeiPoaAmountsX120(BigDecimal r12NeiPoaAmountsX120) {
			this.r12NeiPoaAmountsX120 = r12NeiPoaAmountsX120;
		}

		public BigDecimal getR12NeiOthersAmountsX130() {
			return r12NeiOthersAmountsX130;
		}

		public void setR12NeiOthersAmountsX130(BigDecimal r12NeiOthersAmountsX130) {
			this.r12NeiOthersAmountsX130 = r12NeiOthersAmountsX130;
		}

		public BigDecimal getR12TotalAmountsX140() {
			return r12TotalAmountsX140;
		}

		public void setR12TotalAmountsX140(BigDecimal r12TotalAmountsX140) {
			this.r12TotalAmountsX140 = r12TotalAmountsX140;
		}

		// Getters and Setters for R13
		public String getR13NameOfSfi() {
			return r13NameOfSfi;
		}

		public void setR13NameOfSfi(String r13NameOfSfi) {
			this.r13NameOfSfi = r13NameOfSfi;
		}

		public String getR13NameOfCounterparty() {
			return r13NameOfCounterparty;
		}

		public void setR13NameOfCounterparty(String r13NameOfCounterparty) {
			this.r13NameOfCounterparty = r13NameOfCounterparty;
		}

		public BigDecimal getR13LeAmountsX010() {
			return r13LeAmountsX010;
		}

		public void setR13LeAmountsX010(BigDecimal r13LeAmountsX010) {
			this.r13LeAmountsX010 = r13LeAmountsX010;
		}

		public BigDecimal getR13DpAmountsX020() {
			return r13DpAmountsX020;
		}

		public void setR13DpAmountsX020(BigDecimal r13DpAmountsX020) {
			this.r13DpAmountsX020 = r13DpAmountsX020;
		}

		public BigDecimal getR13StlAmountsX030() {
			return r13StlAmountsX030;
		}

		public void setR13StlAmountsX030(BigDecimal r13StlAmountsX030) {
			this.r13StlAmountsX030 = r13StlAmountsX030;
		}

		public BigDecimal getR13EquityAmountsX040() {
			return r13EquityAmountsX040;
		}

		public void setR13EquityAmountsX040(BigDecimal r13EquityAmountsX040) {
			this.r13EquityAmountsX040 = r13EquityAmountsX040;
		}

		public BigDecimal getR13NeiBdAmountsX050() {
			return r13NeiBdAmountsX050;
		}

		public void setR13NeiBdAmountsX050(BigDecimal r13NeiBdAmountsX050) {
			this.r13NeiBdAmountsX050 = r13NeiBdAmountsX050;
		}

		public BigDecimal getR13NeiCpAmountsX060() {
			return r13NeiCpAmountsX060;
		}

		public void setR13NeiCpAmountsX060(BigDecimal r13NeiCpAmountsX060) {
			this.r13NeiCpAmountsX060 = r13NeiCpAmountsX060;
		}

		public BigDecimal getR13NeiCdAmountsX070() {
			return r13NeiCdAmountsX070;
		}

		public void setR13NeiCdAmountsX070(BigDecimal r13NeiCdAmountsX070) {
			this.r13NeiCdAmountsX070 = r13NeiCdAmountsX070;
		}

		public BigDecimal getR13NeiT2bAmountsX080() {
			return r13NeiT2bAmountsX080;
		}

		public void setR13NeiT2bAmountsX080(BigDecimal r13NeiT2bAmountsX080) {
			this.r13NeiT2bAmountsX080 = r13NeiT2bAmountsX080;
		}

		public BigDecimal getR13NeiUomfAmountsX090() {
			return r13NeiUomfAmountsX090;
		}

		public void setR13NeiUomfAmountsX090(BigDecimal r13NeiUomfAmountsX090) {
			this.r13NeiUomfAmountsX090 = r13NeiUomfAmountsX090;
		}

		public BigDecimal getR13NeiUovfAmountsX100() {
			return r13NeiUovfAmountsX100;
		}

		public void setR13NeiUovfAmountsX100(BigDecimal r13NeiUovfAmountsX100) {
			this.r13NeiUovfAmountsX100 = r13NeiUovfAmountsX100;
		}

		public BigDecimal getR13NeiPoieAmountsX110() {
			return r13NeiPoieAmountsX110;
		}

		public void setR13NeiPoieAmountsX110(BigDecimal r13NeiPoieAmountsX110) {
			this.r13NeiPoieAmountsX110 = r13NeiPoieAmountsX110;
		}

		public BigDecimal getR13NeiPoaAmountsX120() {
			return r13NeiPoaAmountsX120;
		}

		public void setR13NeiPoaAmountsX120(BigDecimal r13NeiPoaAmountsX120) {
			this.r13NeiPoaAmountsX120 = r13NeiPoaAmountsX120;
		}

		public BigDecimal getR13NeiOthersAmountsX130() {
			return r13NeiOthersAmountsX130;
		}

		public void setR13NeiOthersAmountsX130(BigDecimal r13NeiOthersAmountsX130) {
			this.r13NeiOthersAmountsX130 = r13NeiOthersAmountsX130;
		}

		public BigDecimal getR13TotalAmountsX140() {
			return r13TotalAmountsX140;
		}

		public void setR13TotalAmountsX140(BigDecimal r13TotalAmountsX140) {
			this.r13TotalAmountsX140 = r13TotalAmountsX140;
		}

		// Getters and Setters for R14
		public String getR14NameOfSfi() {
			return r14NameOfSfi;
		}

		public void setR14NameOfSfi(String r14NameOfSfi) {
			this.r14NameOfSfi = r14NameOfSfi;
		}

		public String getR14NameOfCounterparty() {
			return r14NameOfCounterparty;
		}

		public void setR14NameOfCounterparty(String r14NameOfCounterparty) {
			this.r14NameOfCounterparty = r14NameOfCounterparty;
		}

		public BigDecimal getR14LeAmountsX010() {
			return r14LeAmountsX010;
		}

		public void setR14LeAmountsX010(BigDecimal r14LeAmountsX010) {
			this.r14LeAmountsX010 = r14LeAmountsX010;
		}

		public BigDecimal getR14DpAmountsX020() {
			return r14DpAmountsX020;
		}

		public void setR14DpAmountsX020(BigDecimal r14DpAmountsX020) {
			this.r14DpAmountsX020 = r14DpAmountsX020;
		}

		public BigDecimal getR14StlAmountsX030() {
			return r14StlAmountsX030;
		}

		public void setR14StlAmountsX030(BigDecimal r14StlAmountsX030) {
			this.r14StlAmountsX030 = r14StlAmountsX030;
		}

		public BigDecimal getR14EquityAmountsX040() {
			return r14EquityAmountsX040;
		}

		public void setR14EquityAmountsX040(BigDecimal r14EquityAmountsX040) {
			this.r14EquityAmountsX040 = r14EquityAmountsX040;
		}

		public BigDecimal getR14NeiBdAmountsX050() {
			return r14NeiBdAmountsX050;
		}

		public void setR14NeiBdAmountsX050(BigDecimal r14NeiBdAmountsX050) {
			this.r14NeiBdAmountsX050 = r14NeiBdAmountsX050;
		}

		public BigDecimal getR14NeiCpAmountsX060() {
			return r14NeiCpAmountsX060;
		}

		public void setR14NeiCpAmountsX060(BigDecimal r14NeiCpAmountsX060) {
			this.r14NeiCpAmountsX060 = r14NeiCpAmountsX060;
		}

		public BigDecimal getR14NeiCdAmountsX070() {
			return r14NeiCdAmountsX070;
		}

		public void setR14NeiCdAmountsX070(BigDecimal r14NeiCdAmountsX070) {
			this.r14NeiCdAmountsX070 = r14NeiCdAmountsX070;
		}

		public BigDecimal getR14NeiT2bAmountsX080() {
			return r14NeiT2bAmountsX080;
		}

		public void setR14NeiT2bAmountsX080(BigDecimal r14NeiT2bAmountsX080) {
			this.r14NeiT2bAmountsX080 = r14NeiT2bAmountsX080;
		}

		public BigDecimal getR14NeiUomfAmountsX090() {
			return r14NeiUomfAmountsX090;
		}

		public void setR14NeiUomfAmountsX090(BigDecimal r14NeiUomfAmountsX090) {
			this.r14NeiUomfAmountsX090 = r14NeiUomfAmountsX090;
		}

		public BigDecimal getR14NeiUovfAmountsX100() {
			return r14NeiUovfAmountsX100;
		}

		public void setR14NeiUovfAmountsX100(BigDecimal r14NeiUovfAmountsX100) {
			this.r14NeiUovfAmountsX100 = r14NeiUovfAmountsX100;
		}

		public BigDecimal getR14NeiPoieAmountsX110() {
			return r14NeiPoieAmountsX110;
		}

		public void setR14NeiPoieAmountsX110(BigDecimal r14NeiPoieAmountsX110) {
			this.r14NeiPoieAmountsX110 = r14NeiPoieAmountsX110;
		}

		public BigDecimal getR14NeiPoaAmountsX120() {
			return r14NeiPoaAmountsX120;
		}

		public void setR14NeiPoaAmountsX120(BigDecimal r14NeiPoaAmountsX120) {
			this.r14NeiPoaAmountsX120 = r14NeiPoaAmountsX120;
		}

		public BigDecimal getR14NeiOthersAmountsX130() {
			return r14NeiOthersAmountsX130;
		}

		public void setR14NeiOthersAmountsX130(BigDecimal r14NeiOthersAmountsX130) {
			this.r14NeiOthersAmountsX130 = r14NeiOthersAmountsX130;
		}

		public BigDecimal getR14TotalAmountsX140() {
			return r14TotalAmountsX140;
		}

		public void setR14TotalAmountsX140(BigDecimal r14TotalAmountsX140) {
			this.r14TotalAmountsX140 = r14TotalAmountsX140;
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

	// RowMapper for Summary
	class Fincon_1C_RowMapper implements RowMapper<BRRS_BG_DBIS10_FINCON_III_1C_ST_Entity> {
		@Override
		public BRRS_BG_DBIS10_FINCON_III_1C_ST_Entity mapRow(ResultSet rs, int rowNum) throws SQLException {
			BRRS_BG_DBIS10_FINCON_III_1C_ST_Entity obj = new BRRS_BG_DBIS10_FINCON_III_1C_ST_Entity();

			// R8 fields
			obj.setR8NameOfSfi(rs.getString("R8_NAME_OF_SFI"));
			obj.setR8NameOfCounterparty(rs.getString("R8_NAME_OF_COUNTERPARTY"));
			obj.setR8LeAmountsX010(rs.getBigDecimal("R8_LE_AMOUNTS_X010"));
			obj.setR8DpAmountsX020(rs.getBigDecimal("R8_DP_AMOUNTS_X020"));
			obj.setR8StlAmountsX030(rs.getBigDecimal("R8_STL_AMOUNTS_X030"));
			obj.setR8EquityAmountsX040(rs.getBigDecimal("R8_EQUITY_AMOUNTS_X040"));
			obj.setR8NeiBdAmountsX050(rs.getBigDecimal("R8_NEI_BD_AMOUNTS_X050"));
			obj.setR8NeiCpAmountsX060(rs.getBigDecimal("R8_NEI_CP_AMOUNTS_X060"));
			obj.setR8NeiCdAmountsX070(rs.getBigDecimal("R8_NEI_CD_AMOUNTS_X070"));
			obj.setR8NeiT2bAmountsX080(rs.getBigDecimal("R8_NEI_T2B_AMOUNTS_X080"));
			obj.setR8NeiUomfAmountsX090(rs.getBigDecimal("R8_NEI_UOMF_AMOUNTS_X090"));
			obj.setR8NeiUovfAmountsX100(rs.getBigDecimal("R8_NEI_UOVF_AMOUNTS_X100"));
			obj.setR8NeiPoieAmountsX110(rs.getBigDecimal("R8_NEI_POIE_AMOUNTS_X110"));
			obj.setR8NeiPoaAmountsX120(rs.getBigDecimal("R8_NEI_POA_AMOUNTS_X120"));
			obj.setR8NeiOthersAmountsX130(rs.getBigDecimal("R8_NEI_OTHERS_AMOUNTS_X130"));
			obj.setR8TotalAmountsX140(rs.getBigDecimal("R8_TOTAL_AMOUNTS_X140"));

			// R9 fields
			obj.setR9NameOfSfi(rs.getString("R9_NAME_OF_SFI"));
			obj.setR9NameOfCounterparty(rs.getString("R9_NAME_OF_COUNTERPARTY"));
			obj.setR9LeAmountsX010(rs.getBigDecimal("R9_LE_AMOUNTS_X010"));
			obj.setR9DpAmountsX020(rs.getBigDecimal("R9_DP_AMOUNTS_X020"));
			obj.setR9StlAmountsX030(rs.getBigDecimal("R9_STL_AMOUNTS_X030"));
			obj.setR9EquityAmountsX040(rs.getBigDecimal("R9_EQUITY_AMOUNTS_X040"));
			obj.setR9NeiBdAmountsX050(rs.getBigDecimal("R9_NEI_BD_AMOUNTS_X050"));
			obj.setR9NeiCpAmountsX060(rs.getBigDecimal("R9_NEI_CP_AMOUNTS_X060"));
			obj.setR9NeiCdAmountsX070(rs.getBigDecimal("R9_NEI_CD_AMOUNTS_X070"));
			obj.setR9NeiT2bAmountsX080(rs.getBigDecimal("R9_NEI_T2B_AMOUNTS_X080"));
			obj.setR9NeiUomfAmountsX090(rs.getBigDecimal("R9_NEI_UOMF_AMOUNTS_X090"));
			obj.setR9NeiUovfAmountsX100(rs.getBigDecimal("R9_NEI_UOVF_AMOUNTS_X100"));
			obj.setR9NeiPoieAmountsX110(rs.getBigDecimal("R9_NEI_POIE_AMOUNTS_X110"));
			obj.setR9NeiPoaAmountsX120(rs.getBigDecimal("R9_NEI_POA_AMOUNTS_X120"));
			obj.setR9NeiOthersAmountsX130(rs.getBigDecimal("R9_NEI_OTHERS_AMOUNTS_X130"));
			obj.setR9TotalAmountsX140(rs.getBigDecimal("R9_TOTAL_AMOUNTS_X140"));

			// R10 fields
			obj.setR10NameOfSfi(rs.getString("R10_NAME_OF_SFI"));
			obj.setR10NameOfCounterparty(rs.getString("R10_NAME_OF_COUNTERPARTY"));
			obj.setR10LeAmountsX010(rs.getBigDecimal("R10_LE_AMOUNTS_X010"));
			obj.setR10DpAmountsX020(rs.getBigDecimal("R10_DP_AMOUNTS_X020"));
			obj.setR10StlAmountsX030(rs.getBigDecimal("R10_STL_AMOUNTS_X030"));
			obj.setR10EquityAmountsX040(rs.getBigDecimal("R10_EQUITY_AMOUNTS_X040"));
			obj.setR10NeiBdAmountsX050(rs.getBigDecimal("R10_NEI_BD_AMOUNTS_X050"));
			obj.setR10NeiCpAmountsX060(rs.getBigDecimal("R10_NEI_CP_AMOUNTS_X060"));
			obj.setR10NeiCdAmountsX070(rs.getBigDecimal("R10_NEI_CD_AMOUNTS_X070"));
			obj.setR10NeiT2bAmountsX080(rs.getBigDecimal("R10_NEI_T2B_AMOUNTS_X080"));
			obj.setR10NeiUomfAmountsX090(rs.getBigDecimal("R10_NEI_UOMF_AMOUNTS_X090"));
			obj.setR10NeiUovfAmountsX100(rs.getBigDecimal("R10_NEI_UOVF_AMOUNTS_X100"));
			obj.setR10NeiPoieAmountsX110(rs.getBigDecimal("R10_NEI_POIE_AMOUNTS_X110"));
			obj.setR10NeiPoaAmountsX120(rs.getBigDecimal("R10_NEI_POA_AMOUNTS_X120"));
			obj.setR10NeiOthersAmountsX130(rs.getBigDecimal("R10_NEI_OTHERS_AMOUNTS_X130"));
			obj.setR10TotalAmountsX140(rs.getBigDecimal("R10_TOTAL_AMOUNTS_X140"));

			// R11 fields
			obj.setR11NameOfSfi(rs.getString("R11_NAME_OF_SFI"));
			obj.setR11NameOfCounterparty(rs.getString("R11_NAME_OF_COUNTERPARTY"));
			obj.setR11LeAmountsX010(rs.getBigDecimal("R11_LE_AMOUNTS_X010"));
			obj.setR11DpAmountsX020(rs.getBigDecimal("R11_DP_AMOUNTS_X020"));
			obj.setR11StlAmountsX030(rs.getBigDecimal("R11_STL_AMOUNTS_X030"));
			obj.setR11EquityAmountsX040(rs.getBigDecimal("R11_EQUITY_AMOUNTS_X040"));
			obj.setR11NeiBdAmountsX050(rs.getBigDecimal("R11_NEI_BD_AMOUNTS_X050"));
			obj.setR11NeiCpAmountsX060(rs.getBigDecimal("R11_NEI_CP_AMOUNTS_X060"));
			obj.setR11NeiCdAmountsX070(rs.getBigDecimal("R11_NEI_CD_AMOUNTS_X070"));
			obj.setR11NeiT2bAmountsX080(rs.getBigDecimal("R11_NEI_T2B_AMOUNTS_X080"));
			obj.setR11NeiUomfAmountsX090(rs.getBigDecimal("R11_NEI_UOMF_AMOUNTS_X090"));
			obj.setR11NeiUovfAmountsX100(rs.getBigDecimal("R11_NEI_UOVF_AMOUNTS_X100"));
			obj.setR11NeiPoieAmountsX110(rs.getBigDecimal("R11_NEI_POIE_AMOUNTS_X110"));
			obj.setR11NeiPoaAmountsX120(rs.getBigDecimal("R11_NEI_POA_AMOUNTS_X120"));
			obj.setR11NeiOthersAmountsX130(rs.getBigDecimal("R11_NEI_OTHERS_AMOUNTS_X130"));
			obj.setR11TotalAmountsX140(rs.getBigDecimal("R11_TOTAL_AMOUNTS_X140"));

			// R12 fields
			obj.setR12NameOfSfi(rs.getString("R12_NAME_OF_SFI"));
			obj.setR12NameOfCounterparty(rs.getString("R12_NAME_OF_COUNTERPARTY"));
			obj.setR12LeAmountsX010(rs.getBigDecimal("R12_LE_AMOUNTS_X010"));
			obj.setR12DpAmountsX020(rs.getBigDecimal("R12_DP_AMOUNTS_X020"));
			obj.setR12StlAmountsX030(rs.getBigDecimal("R12_STL_AMOUNTS_X030"));
			obj.setR12EquityAmountsX040(rs.getBigDecimal("R12_EQUITY_AMOUNTS_X040"));
			obj.setR12NeiBdAmountsX050(rs.getBigDecimal("R12_NEI_BD_AMOUNTS_X050"));
			obj.setR12NeiCpAmountsX060(rs.getBigDecimal("R12_NEI_CP_AMOUNTS_X060"));
			obj.setR12NeiCdAmountsX070(rs.getBigDecimal("R12_NEI_CD_AMOUNTS_X070"));
			obj.setR12NeiT2bAmountsX080(rs.getBigDecimal("R12_NEI_T2B_AMOUNTS_X080"));
			obj.setR12NeiUomfAmountsX090(rs.getBigDecimal("R12_NEI_UOMF_AMOUNTS_X090"));
			obj.setR12NeiUovfAmountsX100(rs.getBigDecimal("R12_NEI_UOVF_AMOUNTS_X100"));
			obj.setR12NeiPoieAmountsX110(rs.getBigDecimal("R12_NEI_POIE_AMOUNTS_X110"));
			obj.setR12NeiPoaAmountsX120(rs.getBigDecimal("R12_NEI_POA_AMOUNTS_X120"));
			obj.setR12NeiOthersAmountsX130(rs.getBigDecimal("R12_NEI_OTHERS_AMOUNTS_X130"));
			obj.setR12TotalAmountsX140(rs.getBigDecimal("R12_TOTAL_AMOUNTS_X140"));

			// R13 fields
			obj.setR13NameOfSfi(rs.getString("R13_NAME_OF_SFI"));
			obj.setR13NameOfCounterparty(rs.getString("R13_NAME_OF_COUNTERPARTY"));
			obj.setR13LeAmountsX010(rs.getBigDecimal("R13_LE_AMOUNTS_X010"));
			obj.setR13DpAmountsX020(rs.getBigDecimal("R13_DP_AMOUNTS_X020"));
			obj.setR13StlAmountsX030(rs.getBigDecimal("R13_STL_AMOUNTS_X030"));
			obj.setR13EquityAmountsX040(rs.getBigDecimal("R13_EQUITY_AMOUNTS_X040"));
			obj.setR13NeiBdAmountsX050(rs.getBigDecimal("R13_NEI_BD_AMOUNTS_X050"));
			obj.setR13NeiCpAmountsX060(rs.getBigDecimal("R13_NEI_CP_AMOUNTS_X060"));
			obj.setR13NeiCdAmountsX070(rs.getBigDecimal("R13_NEI_CD_AMOUNTS_X070"));
			obj.setR13NeiT2bAmountsX080(rs.getBigDecimal("R13_NEI_T2B_AMOUNTS_X080"));
			obj.setR13NeiUomfAmountsX090(rs.getBigDecimal("R13_NEI_UOMF_AMOUNTS_X090"));
			obj.setR13NeiUovfAmountsX100(rs.getBigDecimal("R13_NEI_UOVF_AMOUNTS_X100"));
			obj.setR13NeiPoieAmountsX110(rs.getBigDecimal("R13_NEI_POIE_AMOUNTS_X110"));
			obj.setR13NeiPoaAmountsX120(rs.getBigDecimal("R13_NEI_POA_AMOUNTS_X120"));
			obj.setR13NeiOthersAmountsX130(rs.getBigDecimal("R13_NEI_OTHERS_AMOUNTS_X130"));
			obj.setR13TotalAmountsX140(rs.getBigDecimal("R13_TOTAL_AMOUNTS_X140"));

			// R14 fields
			obj.setR14NameOfSfi(rs.getString("R14_NAME_OF_SFI"));
			obj.setR14NameOfCounterparty(rs.getString("R14_NAME_OF_COUNTERPARTY"));
			obj.setR14LeAmountsX010(rs.getBigDecimal("R14_LE_AMOUNTS_X010"));
			obj.setR14DpAmountsX020(rs.getBigDecimal("R14_DP_AMOUNTS_X020"));
			obj.setR14StlAmountsX030(rs.getBigDecimal("R14_STL_AMOUNTS_X030"));
			obj.setR14EquityAmountsX040(rs.getBigDecimal("R14_EQUITY_AMOUNTS_X040"));
			obj.setR14NeiBdAmountsX050(rs.getBigDecimal("R14_NEI_BD_AMOUNTS_X050"));
			obj.setR14NeiCpAmountsX060(rs.getBigDecimal("R14_NEI_CP_AMOUNTS_X060"));
			obj.setR14NeiCdAmountsX070(rs.getBigDecimal("R14_NEI_CD_AMOUNTS_X070"));
			obj.setR14NeiT2bAmountsX080(rs.getBigDecimal("R14_NEI_T2B_AMOUNTS_X080"));
			obj.setR14NeiUomfAmountsX090(rs.getBigDecimal("R14_NEI_UOMF_AMOUNTS_X090"));
			obj.setR14NeiUovfAmountsX100(rs.getBigDecimal("R14_NEI_UOVF_AMOUNTS_X100"));
			obj.setR14NeiPoieAmountsX110(rs.getBigDecimal("R14_NEI_POIE_AMOUNTS_X110"));
			obj.setR14NeiPoaAmountsX120(rs.getBigDecimal("R14_NEI_POA_AMOUNTS_X120"));
			obj.setR14NeiOthersAmountsX130(rs.getBigDecimal("R14_NEI_OTHERS_AMOUNTS_X130"));
			obj.setR14TotalAmountsX140(rs.getBigDecimal("R14_TOTAL_AMOUNTS_X140"));

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
	class Fincon_1C_Archival_RowMapper implements RowMapper<BRRS_BG_DBIS10_FINCON_III_1C_Archival_ST_Entity> {
		@Override
		public BRRS_BG_DBIS10_FINCON_III_1C_Archival_ST_Entity mapRow(ResultSet rs, int rowNum) throws SQLException {
			BRRS_BG_DBIS10_FINCON_III_1C_Archival_ST_Entity obj = new BRRS_BG_DBIS10_FINCON_III_1C_Archival_ST_Entity();

			// R8 fields
			obj.setR8NameOfSfi(rs.getString("R8_NAME_OF_SFI"));
			obj.setR8NameOfCounterparty(rs.getString("R8_NAME_OF_COUNTERPARTY"));
			obj.setR8LeAmountsX010(rs.getBigDecimal("R8_LE_AMOUNTS_X010"));
			obj.setR8DpAmountsX020(rs.getBigDecimal("R8_DP_AMOUNTS_X020"));
			obj.setR8StlAmountsX030(rs.getBigDecimal("R8_STL_AMOUNTS_X030"));
			obj.setR8EquityAmountsX040(rs.getBigDecimal("R8_EQUITY_AMOUNTS_X040"));
			obj.setR8NeiBdAmountsX050(rs.getBigDecimal("R8_NEI_BD_AMOUNTS_X050"));
			obj.setR8NeiCpAmountsX060(rs.getBigDecimal("R8_NEI_CP_AMOUNTS_X060"));
			obj.setR8NeiCdAmountsX070(rs.getBigDecimal("R8_NEI_CD_AMOUNTS_X070"));
			obj.setR8NeiT2bAmountsX080(rs.getBigDecimal("R8_NEI_T2B_AMOUNTS_X080"));
			obj.setR8NeiUomfAmountsX090(rs.getBigDecimal("R8_NEI_UOMF_AMOUNTS_X090"));
			obj.setR8NeiUovfAmountsX100(rs.getBigDecimal("R8_NEI_UOVF_AMOUNTS_X100"));
			obj.setR8NeiPoieAmountsX110(rs.getBigDecimal("R8_NEI_POIE_AMOUNTS_X110"));
			obj.setR8NeiPoaAmountsX120(rs.getBigDecimal("R8_NEI_POA_AMOUNTS_X120"));
			obj.setR8NeiOthersAmountsX130(rs.getBigDecimal("R8_NEI_OTHERS_AMOUNTS_X130"));
			obj.setR8TotalAmountsX140(rs.getBigDecimal("R8_TOTAL_AMOUNTS_X140"));

			// R9 fields
			obj.setR9NameOfSfi(rs.getString("R9_NAME_OF_SFI"));
			obj.setR9NameOfCounterparty(rs.getString("R9_NAME_OF_COUNTERPARTY"));
			obj.setR9LeAmountsX010(rs.getBigDecimal("R9_LE_AMOUNTS_X010"));
			obj.setR9DpAmountsX020(rs.getBigDecimal("R9_DP_AMOUNTS_X020"));
			obj.setR9StlAmountsX030(rs.getBigDecimal("R9_STL_AMOUNTS_X030"));
			obj.setR9EquityAmountsX040(rs.getBigDecimal("R9_EQUITY_AMOUNTS_X040"));
			obj.setR9NeiBdAmountsX050(rs.getBigDecimal("R9_NEI_BD_AMOUNTS_X050"));
			obj.setR9NeiCpAmountsX060(rs.getBigDecimal("R9_NEI_CP_AMOUNTS_X060"));
			obj.setR9NeiCdAmountsX070(rs.getBigDecimal("R9_NEI_CD_AMOUNTS_X070"));
			obj.setR9NeiT2bAmountsX080(rs.getBigDecimal("R9_NEI_T2B_AMOUNTS_X080"));
			obj.setR9NeiUomfAmountsX090(rs.getBigDecimal("R9_NEI_UOMF_AMOUNTS_X090"));
			obj.setR9NeiUovfAmountsX100(rs.getBigDecimal("R9_NEI_UOVF_AMOUNTS_X100"));
			obj.setR9NeiPoieAmountsX110(rs.getBigDecimal("R9_NEI_POIE_AMOUNTS_X110"));
			obj.setR9NeiPoaAmountsX120(rs.getBigDecimal("R9_NEI_POA_AMOUNTS_X120"));
			obj.setR9NeiOthersAmountsX130(rs.getBigDecimal("R9_NEI_OTHERS_AMOUNTS_X130"));
			obj.setR9TotalAmountsX140(rs.getBigDecimal("R9_TOTAL_AMOUNTS_X140"));

			// R10 fields
			obj.setR10NameOfSfi(rs.getString("R10_NAME_OF_SFI"));
			obj.setR10NameOfCounterparty(rs.getString("R10_NAME_OF_COUNTERPARTY"));
			obj.setR10LeAmountsX010(rs.getBigDecimal("R10_LE_AMOUNTS_X010"));
			obj.setR10DpAmountsX020(rs.getBigDecimal("R10_DP_AMOUNTS_X020"));
			obj.setR10StlAmountsX030(rs.getBigDecimal("R10_STL_AMOUNTS_X030"));
			obj.setR10EquityAmountsX040(rs.getBigDecimal("R10_EQUITY_AMOUNTS_X040"));
			obj.setR10NeiBdAmountsX050(rs.getBigDecimal("R10_NEI_BD_AMOUNTS_X050"));
			obj.setR10NeiCpAmountsX060(rs.getBigDecimal("R10_NEI_CP_AMOUNTS_X060"));
			obj.setR10NeiCdAmountsX070(rs.getBigDecimal("R10_NEI_CD_AMOUNTS_X070"));
			obj.setR10NeiT2bAmountsX080(rs.getBigDecimal("R10_NEI_T2B_AMOUNTS_X080"));
			obj.setR10NeiUomfAmountsX090(rs.getBigDecimal("R10_NEI_UOMF_AMOUNTS_X090"));
			obj.setR10NeiUovfAmountsX100(rs.getBigDecimal("R10_NEI_UOVF_AMOUNTS_X100"));
			obj.setR10NeiPoieAmountsX110(rs.getBigDecimal("R10_NEI_POIE_AMOUNTS_X110"));
			obj.setR10NeiPoaAmountsX120(rs.getBigDecimal("R10_NEI_POA_AMOUNTS_X120"));
			obj.setR10NeiOthersAmountsX130(rs.getBigDecimal("R10_NEI_OTHERS_AMOUNTS_X130"));
			obj.setR10TotalAmountsX140(rs.getBigDecimal("R10_TOTAL_AMOUNTS_X140"));

			// R11 fields
			obj.setR11NameOfSfi(rs.getString("R11_NAME_OF_SFI"));
			obj.setR11NameOfCounterparty(rs.getString("R11_NAME_OF_COUNTERPARTY"));
			obj.setR11LeAmountsX010(rs.getBigDecimal("R11_LE_AMOUNTS_X010"));
			obj.setR11DpAmountsX020(rs.getBigDecimal("R11_DP_AMOUNTS_X020"));
			obj.setR11StlAmountsX030(rs.getBigDecimal("R11_STL_AMOUNTS_X030"));
			obj.setR11EquityAmountsX040(rs.getBigDecimal("R11_EQUITY_AMOUNTS_X040"));
			obj.setR11NeiBdAmountsX050(rs.getBigDecimal("R11_NEI_BD_AMOUNTS_X050"));
			obj.setR11NeiCpAmountsX060(rs.getBigDecimal("R11_NEI_CP_AMOUNTS_X060"));
			obj.setR11NeiCdAmountsX070(rs.getBigDecimal("R11_NEI_CD_AMOUNTS_X070"));
			obj.setR11NeiT2bAmountsX080(rs.getBigDecimal("R11_NEI_T2B_AMOUNTS_X080"));
			obj.setR11NeiUomfAmountsX090(rs.getBigDecimal("R11_NEI_UOMF_AMOUNTS_X090"));
			obj.setR11NeiUovfAmountsX100(rs.getBigDecimal("R11_NEI_UOVF_AMOUNTS_X100"));
			obj.setR11NeiPoieAmountsX110(rs.getBigDecimal("R11_NEI_POIE_AMOUNTS_X110"));
			obj.setR11NeiPoaAmountsX120(rs.getBigDecimal("R11_NEI_POA_AMOUNTS_X120"));
			obj.setR11NeiOthersAmountsX130(rs.getBigDecimal("R11_NEI_OTHERS_AMOUNTS_X130"));
			obj.setR11TotalAmountsX140(rs.getBigDecimal("R11_TOTAL_AMOUNTS_X140"));

			// R12 fields
			obj.setR12NameOfSfi(rs.getString("R12_NAME_OF_SFI"));
			obj.setR12NameOfCounterparty(rs.getString("R12_NAME_OF_COUNTERPARTY"));
			obj.setR12LeAmountsX010(rs.getBigDecimal("R12_LE_AMOUNTS_X010"));
			obj.setR12DpAmountsX020(rs.getBigDecimal("R12_DP_AMOUNTS_X020"));
			obj.setR12StlAmountsX030(rs.getBigDecimal("R12_STL_AMOUNTS_X030"));
			obj.setR12EquityAmountsX040(rs.getBigDecimal("R12_EQUITY_AMOUNTS_X040"));
			obj.setR12NeiBdAmountsX050(rs.getBigDecimal("R12_NEI_BD_AMOUNTS_X050"));
			obj.setR12NeiCpAmountsX060(rs.getBigDecimal("R12_NEI_CP_AMOUNTS_X060"));
			obj.setR12NeiCdAmountsX070(rs.getBigDecimal("R12_NEI_CD_AMOUNTS_X070"));
			obj.setR12NeiT2bAmountsX080(rs.getBigDecimal("R12_NEI_T2B_AMOUNTS_X080"));
			obj.setR12NeiUomfAmountsX090(rs.getBigDecimal("R12_NEI_UOMF_AMOUNTS_X090"));
			obj.setR12NeiUovfAmountsX100(rs.getBigDecimal("R12_NEI_UOVF_AMOUNTS_X100"));
			obj.setR12NeiPoieAmountsX110(rs.getBigDecimal("R12_NEI_POIE_AMOUNTS_X110"));
			obj.setR12NeiPoaAmountsX120(rs.getBigDecimal("R12_NEI_POA_AMOUNTS_X120"));
			obj.setR12NeiOthersAmountsX130(rs.getBigDecimal("R12_NEI_OTHERS_AMOUNTS_X130"));
			obj.setR12TotalAmountsX140(rs.getBigDecimal("R12_TOTAL_AMOUNTS_X140"));

			// R13 fields
			obj.setR13NameOfSfi(rs.getString("R13_NAME_OF_SFI"));
			obj.setR13NameOfCounterparty(rs.getString("R13_NAME_OF_COUNTERPARTY"));
			obj.setR13LeAmountsX010(rs.getBigDecimal("R13_LE_AMOUNTS_X010"));
			obj.setR13DpAmountsX020(rs.getBigDecimal("R13_DP_AMOUNTS_X020"));
			obj.setR13StlAmountsX030(rs.getBigDecimal("R13_STL_AMOUNTS_X030"));
			obj.setR13EquityAmountsX040(rs.getBigDecimal("R13_EQUITY_AMOUNTS_X040"));
			obj.setR13NeiBdAmountsX050(rs.getBigDecimal("R13_NEI_BD_AMOUNTS_X050"));
			obj.setR13NeiCpAmountsX060(rs.getBigDecimal("R13_NEI_CP_AMOUNTS_X060"));
			obj.setR13NeiCdAmountsX070(rs.getBigDecimal("R13_NEI_CD_AMOUNTS_X070"));
			obj.setR13NeiT2bAmountsX080(rs.getBigDecimal("R13_NEI_T2B_AMOUNTS_X080"));
			obj.setR13NeiUomfAmountsX090(rs.getBigDecimal("R13_NEI_UOMF_AMOUNTS_X090"));
			obj.setR13NeiUovfAmountsX100(rs.getBigDecimal("R13_NEI_UOVF_AMOUNTS_X100"));
			obj.setR13NeiPoieAmountsX110(rs.getBigDecimal("R13_NEI_POIE_AMOUNTS_X110"));
			obj.setR13NeiPoaAmountsX120(rs.getBigDecimal("R13_NEI_POA_AMOUNTS_X120"));
			obj.setR13NeiOthersAmountsX130(rs.getBigDecimal("R13_NEI_OTHERS_AMOUNTS_X130"));
			obj.setR13TotalAmountsX140(rs.getBigDecimal("R13_TOTAL_AMOUNTS_X140"));

			// R14 fields
			obj.setR14NameOfSfi(rs.getString("R14_NAME_OF_SFI"));
			obj.setR14NameOfCounterparty(rs.getString("R14_NAME_OF_COUNTERPARTY"));
			obj.setR14LeAmountsX010(rs.getBigDecimal("R14_LE_AMOUNTS_X010"));
			obj.setR14DpAmountsX020(rs.getBigDecimal("R14_DP_AMOUNTS_X020"));
			obj.setR14StlAmountsX030(rs.getBigDecimal("R14_STL_AMOUNTS_X030"));
			obj.setR14EquityAmountsX040(rs.getBigDecimal("R14_EQUITY_AMOUNTS_X040"));
			obj.setR14NeiBdAmountsX050(rs.getBigDecimal("R14_NEI_BD_AMOUNTS_X050"));
			obj.setR14NeiCpAmountsX060(rs.getBigDecimal("R14_NEI_CP_AMOUNTS_X060"));
			obj.setR14NeiCdAmountsX070(rs.getBigDecimal("R14_NEI_CD_AMOUNTS_X070"));
			obj.setR14NeiT2bAmountsX080(rs.getBigDecimal("R14_NEI_T2B_AMOUNTS_X080"));
			obj.setR14NeiUomfAmountsX090(rs.getBigDecimal("R14_NEI_UOMF_AMOUNTS_X090"));
			obj.setR14NeiUovfAmountsX100(rs.getBigDecimal("R14_NEI_UOVF_AMOUNTS_X100"));
			obj.setR14NeiPoieAmountsX110(rs.getBigDecimal("R14_NEI_POIE_AMOUNTS_X110"));
			obj.setR14NeiPoaAmountsX120(rs.getBigDecimal("R14_NEI_POA_AMOUNTS_X120"));
			obj.setR14NeiOthersAmountsX130(rs.getBigDecimal("R14_NEI_OTHERS_AMOUNTS_X130"));
			obj.setR14TotalAmountsX140(rs.getBigDecimal("R14_TOTAL_AMOUNTS_X140"));

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

	// =========================================================
	// ENTITY CLASSES
	// =========================================================

	// PK Class
	public static class BG_FINCON_1C_PK implements Serializable {
		private Date report_date;
		private BigDecimal report_version;

		public BG_FINCON_1C_PK() {
		}

		public BG_FINCON_1C_PK(Date report_date, BigDecimal report_version) {
			this.report_date = report_date;
			this.report_version = report_version;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof BG_FINCON_1C_PK))
				return false;
			BG_FINCON_1C_PK that = (BG_FINCON_1C_PK) o;
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

	// Archival Summary Entity
	@IdClass(BG_FINCON_1C_PK.class)
	public static class BRRS_BG_DBIS10_FINCON_III_1C_Archival_ST_Entity {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;

		// R8 fields
		@Column(name = "R8_NAME_OF_SFI")
		private String r8NameOfSfi;

		@Column(name = "R8_NAME_OF_COUNTERPARTY")
		private String r8NameOfCounterparty;

		@Column(name = "R8_LE_AMOUNTS_X010")
		private BigDecimal r8LeAmountsX010;

		@Column(name = "R8_DP_AMOUNTS_X020")
		private BigDecimal r8DpAmountsX020;

		@Column(name = "R8_STL_AMOUNTS_X030")
		private BigDecimal r8StlAmountsX030;

		@Column(name = "R8_EQUITY_AMOUNTS_X040")
		private BigDecimal r8EquityAmountsX040;

		@Column(name = "R8_NEI_BD_AMOUNTS_X050")
		private BigDecimal r8NeiBdAmountsX050;

		@Column(name = "R8_NEI_CP_AMOUNTS_X060")
		private BigDecimal r8NeiCpAmountsX060;

		@Column(name = "R8_NEI_CD_AMOUNTS_X070")
		private BigDecimal r8NeiCdAmountsX070;

		@Column(name = "R8_NEI_T2B_AMOUNTS_X080")
		private BigDecimal r8NeiT2bAmountsX080;

		@Column(name = "R8_NEI_UOMF_AMOUNTS_X090")
		private BigDecimal r8NeiUomfAmountsX090;

		@Column(name = "R8_NEI_UOVF_AMOUNTS_X100")
		private BigDecimal r8NeiUovfAmountsX100;

		@Column(name = "R8_NEI_POIE_AMOUNTS_X110")
		private BigDecimal r8NeiPoieAmountsX110;

		@Column(name = "R8_NEI_POA_AMOUNTS_X120")
		private BigDecimal r8NeiPoaAmountsX120;

		@Column(name = "R8_NEI_OTHERS_AMOUNTS_X130")
		private BigDecimal r8NeiOthersAmountsX130;

		@Column(name = "R8_TOTAL_AMOUNTS_X140")
		private BigDecimal r8TotalAmountsX140;

		// R9 fields
		@Column(name = "R9_NAME_OF_SFI")
		private String r9NameOfSfi;

		@Column(name = "R9_NAME_OF_COUNTERPARTY")
		private String r9NameOfCounterparty;

		@Column(name = "R9_LE_AMOUNTS_X010")
		private BigDecimal r9LeAmountsX010;

		@Column(name = "R9_DP_AMOUNTS_X020")
		private BigDecimal r9DpAmountsX020;

		@Column(name = "R9_STL_AMOUNTS_X030")
		private BigDecimal r9StlAmountsX030;

		@Column(name = "R9_EQUITY_AMOUNTS_X040")
		private BigDecimal r9EquityAmountsX040;

		@Column(name = "R9_NEI_BD_AMOUNTS_X050")
		private BigDecimal r9NeiBdAmountsX050;

		@Column(name = "R9_NEI_CP_AMOUNTS_X060")
		private BigDecimal r9NeiCpAmountsX060;

		@Column(name = "R9_NEI_CD_AMOUNTS_X070")
		private BigDecimal r9NeiCdAmountsX070;

		@Column(name = "R9_NEI_T2B_AMOUNTS_X080")
		private BigDecimal r9NeiT2bAmountsX080;

		@Column(name = "R9_NEI_UOMF_AMOUNTS_X090")
		private BigDecimal r9NeiUomfAmountsX090;

		@Column(name = "R9_NEI_UOVF_AMOUNTS_X100")
		private BigDecimal r9NeiUovfAmountsX100;

		@Column(name = "R9_NEI_POIE_AMOUNTS_X110")
		private BigDecimal r9NeiPoieAmountsX110;

		@Column(name = "R9_NEI_POA_AMOUNTS_X120")
		private BigDecimal r9NeiPoaAmountsX120;

		@Column(name = "R9_NEI_OTHERS_AMOUNTS_X130")
		private BigDecimal r9NeiOthersAmountsX130;

		@Column(name = "R9_TOTAL_AMOUNTS_X140")
		private BigDecimal r9TotalAmountsX140;

		// R10 fields
		@Column(name = "R10_NAME_OF_SFI")
		private String r10NameOfSfi;

		@Column(name = "R10_NAME_OF_COUNTERPARTY")
		private String r10NameOfCounterparty;

		@Column(name = "R10_LE_AMOUNTS_X010")
		private BigDecimal r10LeAmountsX010;

		@Column(name = "R10_DP_AMOUNTS_X020")
		private BigDecimal r10DpAmountsX020;

		@Column(name = "R10_STL_AMOUNTS_X030")
		private BigDecimal r10StlAmountsX030;

		@Column(name = "R10_EQUITY_AMOUNTS_X040")
		private BigDecimal r10EquityAmountsX040;

		@Column(name = "R10_NEI_BD_AMOUNTS_X050")
		private BigDecimal r10NeiBdAmountsX050;

		@Column(name = "R10_NEI_CP_AMOUNTS_X060")
		private BigDecimal r10NeiCpAmountsX060;

		@Column(name = "R10_NEI_CD_AMOUNTS_X070")
		private BigDecimal r10NeiCdAmountsX070;

		@Column(name = "R10_NEI_T2B_AMOUNTS_X080")
		private BigDecimal r10NeiT2bAmountsX080;

		@Column(name = "R10_NEI_UOMF_AMOUNTS_X090")
		private BigDecimal r10NeiUomfAmountsX090;

		@Column(name = "R10_NEI_UOVF_AMOUNTS_X100")
		private BigDecimal r10NeiUovfAmountsX100;

		@Column(name = "R10_NEI_POIE_AMOUNTS_X110")
		private BigDecimal r10NeiPoieAmountsX110;

		@Column(name = "R10_NEI_POA_AMOUNTS_X120")
		private BigDecimal r10NeiPoaAmountsX120;

		@Column(name = "R10_NEI_OTHERS_AMOUNTS_X130")
		private BigDecimal r10NeiOthersAmountsX130;

		@Column(name = "R10_TOTAL_AMOUNTS_X140")
		private BigDecimal r10TotalAmountsX140;

		// R11 fields
		@Column(name = "R11_NAME_OF_SFI")
		private String r11NameOfSfi;

		@Column(name = "R11_NAME_OF_COUNTERPARTY")
		private String r11NameOfCounterparty;

		@Column(name = "R11_LE_AMOUNTS_X010")
		private BigDecimal r11LeAmountsX010;

		@Column(name = "R11_DP_AMOUNTS_X020")
		private BigDecimal r11DpAmountsX020;

		@Column(name = "R11_STL_AMOUNTS_X030")
		private BigDecimal r11StlAmountsX030;

		@Column(name = "R11_EQUITY_AMOUNTS_X040")
		private BigDecimal r11EquityAmountsX040;

		@Column(name = "R11_NEI_BD_AMOUNTS_X050")
		private BigDecimal r11NeiBdAmountsX050;

		@Column(name = "R11_NEI_CP_AMOUNTS_X060")
		private BigDecimal r11NeiCpAmountsX060;

		@Column(name = "R11_NEI_CD_AMOUNTS_X070")
		private BigDecimal r11NeiCdAmountsX070;

		@Column(name = "R11_NEI_T2B_AMOUNTS_X080")
		private BigDecimal r11NeiT2bAmountsX080;

		@Column(name = "R11_NEI_UOMF_AMOUNTS_X090")
		private BigDecimal r11NeiUomfAmountsX090;

		@Column(name = "R11_NEI_UOVF_AMOUNTS_X100")
		private BigDecimal r11NeiUovfAmountsX100;

		@Column(name = "R11_NEI_POIE_AMOUNTS_X110")
		private BigDecimal r11NeiPoieAmountsX110;

		@Column(name = "R11_NEI_POA_AMOUNTS_X120")
		private BigDecimal r11NeiPoaAmountsX120;

		@Column(name = "R11_NEI_OTHERS_AMOUNTS_X130")
		private BigDecimal r11NeiOthersAmountsX130;

		@Column(name = "R11_TOTAL_AMOUNTS_X140")
		private BigDecimal r11TotalAmountsX140;

		// R12 fields
		@Column(name = "R12_NAME_OF_SFI")
		private String r12NameOfSfi;

		@Column(name = "R12_NAME_OF_COUNTERPARTY")
		private String r12NameOfCounterparty;

		@Column(name = "R12_LE_AMOUNTS_X010")
		private BigDecimal r12LeAmountsX010;

		@Column(name = "R12_DP_AMOUNTS_X020")
		private BigDecimal r12DpAmountsX020;

		@Column(name = "R12_STL_AMOUNTS_X030")
		private BigDecimal r12StlAmountsX030;

		@Column(name = "R12_EQUITY_AMOUNTS_X040")
		private BigDecimal r12EquityAmountsX040;

		@Column(name = "R12_NEI_BD_AMOUNTS_X050")
		private BigDecimal r12NeiBdAmountsX050;

		@Column(name = "R12_NEI_CP_AMOUNTS_X060")
		private BigDecimal r12NeiCpAmountsX060;

		@Column(name = "R12_NEI_CD_AMOUNTS_X070")
		private BigDecimal r12NeiCdAmountsX070;

		@Column(name = "R12_NEI_T2B_AMOUNTS_X080")
		private BigDecimal r12NeiT2bAmountsX080;

		@Column(name = "R12_NEI_UOMF_AMOUNTS_X090")
		private BigDecimal r12NeiUomfAmountsX090;

		@Column(name = "R12_NEI_UOVF_AMOUNTS_X100")
		private BigDecimal r12NeiUovfAmountsX100;

		@Column(name = "R12_NEI_POIE_AMOUNTS_X110")
		private BigDecimal r12NeiPoieAmountsX110;

		@Column(name = "R12_NEI_POA_AMOUNTS_X120")
		private BigDecimal r12NeiPoaAmountsX120;

		@Column(name = "R12_NEI_OTHERS_AMOUNTS_X130")
		private BigDecimal r12NeiOthersAmountsX130;

		@Column(name = "R12_TOTAL_AMOUNTS_X140")
		private BigDecimal r12TotalAmountsX140;

		// R13 fields
		@Column(name = "R13_NAME_OF_SFI")
		private String r13NameOfSfi;

		@Column(name = "R13_NAME_OF_COUNTERPARTY")
		private String r13NameOfCounterparty;

		@Column(name = "R13_LE_AMOUNTS_X010")
		private BigDecimal r13LeAmountsX010;

		@Column(name = "R13_DP_AMOUNTS_X020")
		private BigDecimal r13DpAmountsX020;

		@Column(name = "R13_STL_AMOUNTS_X030")
		private BigDecimal r13StlAmountsX030;

		@Column(name = "R13_EQUITY_AMOUNTS_X040")
		private BigDecimal r13EquityAmountsX040;

		@Column(name = "R13_NEI_BD_AMOUNTS_X050")
		private BigDecimal r13NeiBdAmountsX050;

		@Column(name = "R13_NEI_CP_AMOUNTS_X060")
		private BigDecimal r13NeiCpAmountsX060;

		@Column(name = "R13_NEI_CD_AMOUNTS_X070")
		private BigDecimal r13NeiCdAmountsX070;

		@Column(name = "R13_NEI_T2B_AMOUNTS_X080")
		private BigDecimal r13NeiT2bAmountsX080;

		@Column(name = "R13_NEI_UOMF_AMOUNTS_X090")
		private BigDecimal r13NeiUomfAmountsX090;

		@Column(name = "R13_NEI_UOVF_AMOUNTS_X100")
		private BigDecimal r13NeiUovfAmountsX100;

		@Column(name = "R13_NEI_POIE_AMOUNTS_X110")
		private BigDecimal r13NeiPoieAmountsX110;

		@Column(name = "R13_NEI_POA_AMOUNTS_X120")
		private BigDecimal r13NeiPoaAmountsX120;

		@Column(name = "R13_NEI_OTHERS_AMOUNTS_X130")
		private BigDecimal r13NeiOthersAmountsX130;

		@Column(name = "R13_TOTAL_AMOUNTS_X140")
		private BigDecimal r13TotalAmountsX140;

		// R14 fields
		@Column(name = "R14_NAME_OF_SFI")
		private String r14NameOfSfi;

		@Column(name = "R14_NAME_OF_COUNTERPARTY")
		private String r14NameOfCounterparty;

		@Column(name = "R14_LE_AMOUNTS_X010")
		private BigDecimal r14LeAmountsX010;

		@Column(name = "R14_DP_AMOUNTS_X020")
		private BigDecimal r14DpAmountsX020;

		@Column(name = "R14_STL_AMOUNTS_X030")
		private BigDecimal r14StlAmountsX030;

		@Column(name = "R14_EQUITY_AMOUNTS_X040")
		private BigDecimal r14EquityAmountsX040;

		@Column(name = "R14_NEI_BD_AMOUNTS_X050")
		private BigDecimal r14NeiBdAmountsX050;

		@Column(name = "R14_NEI_CP_AMOUNTS_X060")
		private BigDecimal r14NeiCpAmountsX060;

		@Column(name = "R14_NEI_CD_AMOUNTS_X070")
		private BigDecimal r14NeiCdAmountsX070;

		@Column(name = "R14_NEI_T2B_AMOUNTS_X080")
		private BigDecimal r14NeiT2bAmountsX080;

		@Column(name = "R14_NEI_UOMF_AMOUNTS_X090")
		private BigDecimal r14NeiUomfAmountsX090;

		@Column(name = "R14_NEI_UOVF_AMOUNTS_X100")
		private BigDecimal r14NeiUovfAmountsX100;

		@Column(name = "R14_NEI_POIE_AMOUNTS_X110")
		private BigDecimal r14NeiPoieAmountsX110;

		@Column(name = "R14_NEI_POA_AMOUNTS_X120")
		private BigDecimal r14NeiPoaAmountsX120;

		@Column(name = "R14_NEI_OTHERS_AMOUNTS_X130")
		private BigDecimal r14NeiOthersAmountsX130;

		@Column(name = "R14_TOTAL_AMOUNTS_X140")
		private BigDecimal r14TotalAmountsX140;

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

		// Getters and Setters (same pattern as summary entity)
		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getR8NameOfSfi() {
			return r8NameOfSfi;
		}

		public void setR8NameOfSfi(String r8NameOfSfi) {
			this.r8NameOfSfi = r8NameOfSfi;
		}

		public String getR8NameOfCounterparty() {
			return r8NameOfCounterparty;
		}

		public void setR8NameOfCounterparty(String r8NameOfCounterparty) {
			this.r8NameOfCounterparty = r8NameOfCounterparty;
		}

		public BigDecimal getR8LeAmountsX010() {
			return r8LeAmountsX010;
		}

		public void setR8LeAmountsX010(BigDecimal r8LeAmountsX010) {
			this.r8LeAmountsX010 = r8LeAmountsX010;
		}

		public BigDecimal getR8DpAmountsX020() {
			return r8DpAmountsX020;
		}

		public void setR8DpAmountsX020(BigDecimal r8DpAmountsX020) {
			this.r8DpAmountsX020 = r8DpAmountsX020;
		}

		public BigDecimal getR8StlAmountsX030() {
			return r8StlAmountsX030;
		}

		public void setR8StlAmountsX030(BigDecimal r8StlAmountsX030) {
			this.r8StlAmountsX030 = r8StlAmountsX030;
		}

		public BigDecimal getR8EquityAmountsX040() {
			return r8EquityAmountsX040;
		}

		public void setR8EquityAmountsX040(BigDecimal r8EquityAmountsX040) {
			this.r8EquityAmountsX040 = r8EquityAmountsX040;
		}

		public BigDecimal getR8NeiBdAmountsX050() {
			return r8NeiBdAmountsX050;
		}

		public void setR8NeiBdAmountsX050(BigDecimal r8NeiBdAmountsX050) {
			this.r8NeiBdAmountsX050 = r8NeiBdAmountsX050;
		}

		public BigDecimal getR8NeiCpAmountsX060() {
			return r8NeiCpAmountsX060;
		}

		public void setR8NeiCpAmountsX060(BigDecimal r8NeiCpAmountsX060) {
			this.r8NeiCpAmountsX060 = r8NeiCpAmountsX060;
		}

		public BigDecimal getR8NeiCdAmountsX070() {
			return r8NeiCdAmountsX070;
		}

		public void setR8NeiCdAmountsX070(BigDecimal r8NeiCdAmountsX070) {
			this.r8NeiCdAmountsX070 = r8NeiCdAmountsX070;
		}

		public BigDecimal getR8NeiT2bAmountsX080() {
			return r8NeiT2bAmountsX080;
		}

		public void setR8NeiT2bAmountsX080(BigDecimal r8NeiT2bAmountsX080) {
			this.r8NeiT2bAmountsX080 = r8NeiT2bAmountsX080;
		}

		public BigDecimal getR8NeiUomfAmountsX090() {
			return r8NeiUomfAmountsX090;
		}

		public void setR8NeiUomfAmountsX090(BigDecimal r8NeiUomfAmountsX090) {
			this.r8NeiUomfAmountsX090 = r8NeiUomfAmountsX090;
		}

		public BigDecimal getR8NeiUovfAmountsX100() {
			return r8NeiUovfAmountsX100;
		}

		public void setR8NeiUovfAmountsX100(BigDecimal r8NeiUovfAmountsX100) {
			this.r8NeiUovfAmountsX100 = r8NeiUovfAmountsX100;
		}

		public BigDecimal getR8NeiPoieAmountsX110() {
			return r8NeiPoieAmountsX110;
		}

		public void setR8NeiPoieAmountsX110(BigDecimal r8NeiPoieAmountsX110) {
			this.r8NeiPoieAmountsX110 = r8NeiPoieAmountsX110;
		}

		public BigDecimal getR8NeiPoaAmountsX120() {
			return r8NeiPoaAmountsX120;
		}

		public void setR8NeiPoaAmountsX120(BigDecimal r8NeiPoaAmountsX120) {
			this.r8NeiPoaAmountsX120 = r8NeiPoaAmountsX120;
		}

		public BigDecimal getR8NeiOthersAmountsX130() {
			return r8NeiOthersAmountsX130;
		}

		public void setR8NeiOthersAmountsX130(BigDecimal r8NeiOthersAmountsX130) {
			this.r8NeiOthersAmountsX130 = r8NeiOthersAmountsX130;
		}

		public BigDecimal getR8TotalAmountsX140() {
			return r8TotalAmountsX140;
		}

		public void setR8TotalAmountsX140(BigDecimal r8TotalAmountsX140) {
			this.r8TotalAmountsX140 = r8TotalAmountsX140;
		}

		public String getR9NameOfSfi() {
			return r9NameOfSfi;
		}

		public void setR9NameOfSfi(String r9NameOfSfi) {
			this.r9NameOfSfi = r9NameOfSfi;
		}

		public String getR9NameOfCounterparty() {
			return r9NameOfCounterparty;
		}

		public void setR9NameOfCounterparty(String r9NameOfCounterparty) {
			this.r9NameOfCounterparty = r9NameOfCounterparty;
		}

		public BigDecimal getR9LeAmountsX010() {
			return r9LeAmountsX010;
		}

		public void setR9LeAmountsX010(BigDecimal r9LeAmountsX010) {
			this.r9LeAmountsX010 = r9LeAmountsX010;
		}

		public BigDecimal getR9DpAmountsX020() {
			return r9DpAmountsX020;
		}

		public void setR9DpAmountsX020(BigDecimal r9DpAmountsX020) {
			this.r9DpAmountsX020 = r9DpAmountsX020;
		}

		public BigDecimal getR9StlAmountsX030() {
			return r9StlAmountsX030;
		}

		public void setR9StlAmountsX030(BigDecimal r9StlAmountsX030) {
			this.r9StlAmountsX030 = r9StlAmountsX030;
		}

		public BigDecimal getR9EquityAmountsX040() {
			return r9EquityAmountsX040;
		}

		public void setR9EquityAmountsX040(BigDecimal r9EquityAmountsX040) {
			this.r9EquityAmountsX040 = r9EquityAmountsX040;
		}

		public BigDecimal getR9NeiBdAmountsX050() {
			return r9NeiBdAmountsX050;
		}

		public void setR9NeiBdAmountsX050(BigDecimal r9NeiBdAmountsX050) {
			this.r9NeiBdAmountsX050 = r9NeiBdAmountsX050;
		}

		public BigDecimal getR9NeiCpAmountsX060() {
			return r9NeiCpAmountsX060;
		}

		public void setR9NeiCpAmountsX060(BigDecimal r9NeiCpAmountsX060) {
			this.r9NeiCpAmountsX060 = r9NeiCpAmountsX060;
		}

		public BigDecimal getR9NeiCdAmountsX070() {
			return r9NeiCdAmountsX070;
		}

		public void setR9NeiCdAmountsX070(BigDecimal r9NeiCdAmountsX070) {
			this.r9NeiCdAmountsX070 = r9NeiCdAmountsX070;
		}

		public BigDecimal getR9NeiT2bAmountsX080() {
			return r9NeiT2bAmountsX080;
		}

		public void setR9NeiT2bAmountsX080(BigDecimal r9NeiT2bAmountsX080) {
			this.r9NeiT2bAmountsX080 = r9NeiT2bAmountsX080;
		}

		public BigDecimal getR9NeiUomfAmountsX090() {
			return r9NeiUomfAmountsX090;
		}

		public void setR9NeiUomfAmountsX090(BigDecimal r9NeiUomfAmountsX090) {
			this.r9NeiUomfAmountsX090 = r9NeiUomfAmountsX090;
		}

		public BigDecimal getR9NeiUovfAmountsX100() {
			return r9NeiUovfAmountsX100;
		}

		public void setR9NeiUovfAmountsX100(BigDecimal r9NeiUovfAmountsX100) {
			this.r9NeiUovfAmountsX100 = r9NeiUovfAmountsX100;
		}

		public BigDecimal getR9NeiPoieAmountsX110() {
			return r9NeiPoieAmountsX110;
		}

		public void setR9NeiPoieAmountsX110(BigDecimal r9NeiPoieAmountsX110) {
			this.r9NeiPoieAmountsX110 = r9NeiPoieAmountsX110;
		}

		public BigDecimal getR9NeiPoaAmountsX120() {
			return r9NeiPoaAmountsX120;
		}

		public void setR9NeiPoaAmountsX120(BigDecimal r9NeiPoaAmountsX120) {
			this.r9NeiPoaAmountsX120 = r9NeiPoaAmountsX120;
		}

		public BigDecimal getR9NeiOthersAmountsX130() {
			return r9NeiOthersAmountsX130;
		}

		public void setR9NeiOthersAmountsX130(BigDecimal r9NeiOthersAmountsX130) {
			this.r9NeiOthersAmountsX130 = r9NeiOthersAmountsX130;
		}

		public BigDecimal getR9TotalAmountsX140() {
			return r9TotalAmountsX140;
		}

		public void setR9TotalAmountsX140(BigDecimal r9TotalAmountsX140) {
			this.r9TotalAmountsX140 = r9TotalAmountsX140;
		}

		public String getR10NameOfSfi() {
			return r10NameOfSfi;
		}

		public void setR10NameOfSfi(String r10NameOfSfi) {
			this.r10NameOfSfi = r10NameOfSfi;
		}

		public String getR10NameOfCounterparty() {
			return r10NameOfCounterparty;
		}

		public void setR10NameOfCounterparty(String r10NameOfCounterparty) {
			this.r10NameOfCounterparty = r10NameOfCounterparty;
		}

		public BigDecimal getR10LeAmountsX010() {
			return r10LeAmountsX010;
		}

		public void setR10LeAmountsX010(BigDecimal r10LeAmountsX010) {
			this.r10LeAmountsX010 = r10LeAmountsX010;
		}

		public BigDecimal getR10DpAmountsX020() {
			return r10DpAmountsX020;
		}

		public void setR10DpAmountsX020(BigDecimal r10DpAmountsX020) {
			this.r10DpAmountsX020 = r10DpAmountsX020;
		}

		public BigDecimal getR10StlAmountsX030() {
			return r10StlAmountsX030;
		}

		public void setR10StlAmountsX030(BigDecimal r10StlAmountsX030) {
			this.r10StlAmountsX030 = r10StlAmountsX030;
		}

		public BigDecimal getR10EquityAmountsX040() {
			return r10EquityAmountsX040;
		}

		public void setR10EquityAmountsX040(BigDecimal r10EquityAmountsX040) {
			this.r10EquityAmountsX040 = r10EquityAmountsX040;
		}

		public BigDecimal getR10NeiBdAmountsX050() {
			return r10NeiBdAmountsX050;
		}

		public void setR10NeiBdAmountsX050(BigDecimal r10NeiBdAmountsX050) {
			this.r10NeiBdAmountsX050 = r10NeiBdAmountsX050;
		}

		public BigDecimal getR10NeiCpAmountsX060() {
			return r10NeiCpAmountsX060;
		}

		public void setR10NeiCpAmountsX060(BigDecimal r10NeiCpAmountsX060) {
			this.r10NeiCpAmountsX060 = r10NeiCpAmountsX060;
		}

		public BigDecimal getR10NeiCdAmountsX070() {
			return r10NeiCdAmountsX070;
		}

		public void setR10NeiCdAmountsX070(BigDecimal r10NeiCdAmountsX070) {
			this.r10NeiCdAmountsX070 = r10NeiCdAmountsX070;
		}

		public BigDecimal getR10NeiT2bAmountsX080() {
			return r10NeiT2bAmountsX080;
		}

		public void setR10NeiT2bAmountsX080(BigDecimal r10NeiT2bAmountsX080) {
			this.r10NeiT2bAmountsX080 = r10NeiT2bAmountsX080;
		}

		public BigDecimal getR10NeiUomfAmountsX090() {
			return r10NeiUomfAmountsX090;
		}

		public void setR10NeiUomfAmountsX090(BigDecimal r10NeiUomfAmountsX090) {
			this.r10NeiUomfAmountsX090 = r10NeiUomfAmountsX090;
		}

		public BigDecimal getR10NeiUovfAmountsX100() {
			return r10NeiUovfAmountsX100;
		}

		public void setR10NeiUovfAmountsX100(BigDecimal r10NeiUovfAmountsX100) {
			this.r10NeiUovfAmountsX100 = r10NeiUovfAmountsX100;
		}

		public BigDecimal getR10NeiPoieAmountsX110() {
			return r10NeiPoieAmountsX110;
		}

		public void setR10NeiPoieAmountsX110(BigDecimal r10NeiPoieAmountsX110) {
			this.r10NeiPoieAmountsX110 = r10NeiPoieAmountsX110;
		}

		public BigDecimal getR10NeiPoaAmountsX120() {
			return r10NeiPoaAmountsX120;
		}

		public void setR10NeiPoaAmountsX120(BigDecimal r10NeiPoaAmountsX120) {
			this.r10NeiPoaAmountsX120 = r10NeiPoaAmountsX120;
		}

		public BigDecimal getR10NeiOthersAmountsX130() {
			return r10NeiOthersAmountsX130;
		}

		public void setR10NeiOthersAmountsX130(BigDecimal r10NeiOthersAmountsX130) {
			this.r10NeiOthersAmountsX130 = r10NeiOthersAmountsX130;
		}

		public BigDecimal getR10TotalAmountsX140() {
			return r10TotalAmountsX140;
		}

		public void setR10TotalAmountsX140(BigDecimal r10TotalAmountsX140) {
			this.r10TotalAmountsX140 = r10TotalAmountsX140;
		}

		public String getR11NameOfSfi() {
			return r11NameOfSfi;
		}

		public void setR11NameOfSfi(String r11NameOfSfi) {
			this.r11NameOfSfi = r11NameOfSfi;
		}

		public String getR11NameOfCounterparty() {
			return r11NameOfCounterparty;
		}

		public void setR11NameOfCounterparty(String r11NameOfCounterparty) {
			this.r11NameOfCounterparty = r11NameOfCounterparty;
		}

		public BigDecimal getR11LeAmountsX010() {
			return r11LeAmountsX010;
		}

		public void setR11LeAmountsX010(BigDecimal r11LeAmountsX010) {
			this.r11LeAmountsX010 = r11LeAmountsX010;
		}

		public BigDecimal getR11DpAmountsX020() {
			return r11DpAmountsX020;
		}

		public void setR11DpAmountsX020(BigDecimal r11DpAmountsX020) {
			this.r11DpAmountsX020 = r11DpAmountsX020;
		}

		public BigDecimal getR11StlAmountsX030() {
			return r11StlAmountsX030;
		}

		public void setR11StlAmountsX030(BigDecimal r11StlAmountsX030) {
			this.r11StlAmountsX030 = r11StlAmountsX030;
		}

		public BigDecimal getR11EquityAmountsX040() {
			return r11EquityAmountsX040;
		}

		public void setR11EquityAmountsX040(BigDecimal r11EquityAmountsX040) {
			this.r11EquityAmountsX040 = r11EquityAmountsX040;
		}

		public BigDecimal getR11NeiBdAmountsX050() {
			return r11NeiBdAmountsX050;
		}

		public void setR11NeiBdAmountsX050(BigDecimal r11NeiBdAmountsX050) {
			this.r11NeiBdAmountsX050 = r11NeiBdAmountsX050;
		}

		public BigDecimal getR11NeiCpAmountsX060() {
			return r11NeiCpAmountsX060;
		}

		public void setR11NeiCpAmountsX060(BigDecimal r11NeiCpAmountsX060) {
			this.r11NeiCpAmountsX060 = r11NeiCpAmountsX060;
		}

		public BigDecimal getR11NeiCdAmountsX070() {
			return r11NeiCdAmountsX070;
		}

		public void setR11NeiCdAmountsX070(BigDecimal r11NeiCdAmountsX070) {
			this.r11NeiCdAmountsX070 = r11NeiCdAmountsX070;
		}

		public BigDecimal getR11NeiT2bAmountsX080() {
			return r11NeiT2bAmountsX080;
		}

		public void setR11NeiT2bAmountsX080(BigDecimal r11NeiT2bAmountsX080) {
			this.r11NeiT2bAmountsX080 = r11NeiT2bAmountsX080;
		}

		public BigDecimal getR11NeiUomfAmountsX090() {
			return r11NeiUomfAmountsX090;
		}

		public void setR11NeiUomfAmountsX090(BigDecimal r11NeiUomfAmountsX090) {
			this.r11NeiUomfAmountsX090 = r11NeiUomfAmountsX090;
		}

		public BigDecimal getR11NeiUovfAmountsX100() {
			return r11NeiUovfAmountsX100;
		}

		public void setR11NeiUovfAmountsX100(BigDecimal r11NeiUovfAmountsX100) {
			this.r11NeiUovfAmountsX100 = r11NeiUovfAmountsX100;
		}

		public BigDecimal getR11NeiPoieAmountsX110() {
			return r11NeiPoieAmountsX110;
		}

		public void setR11NeiPoieAmountsX110(BigDecimal r11NeiPoieAmountsX110) {
			this.r11NeiPoieAmountsX110 = r11NeiPoieAmountsX110;
		}

		public BigDecimal getR11NeiPoaAmountsX120() {
			return r11NeiPoaAmountsX120;
		}

		public void setR11NeiPoaAmountsX120(BigDecimal r11NeiPoaAmountsX120) {
			this.r11NeiPoaAmountsX120 = r11NeiPoaAmountsX120;
		}

		public BigDecimal getR11NeiOthersAmountsX130() {
			return r11NeiOthersAmountsX130;
		}

		public void setR11NeiOthersAmountsX130(BigDecimal r11NeiOthersAmountsX130) {
			this.r11NeiOthersAmountsX130 = r11NeiOthersAmountsX130;
		}

		public BigDecimal getR11TotalAmountsX140() {
			return r11TotalAmountsX140;
		}

		public void setR11TotalAmountsX140(BigDecimal r11TotalAmountsX140) {
			this.r11TotalAmountsX140 = r11TotalAmountsX140;
		}

		public String getR12NameOfSfi() {
			return r12NameOfSfi;
		}

		public void setR12NameOfSfi(String r12NameOfSfi) {
			this.r12NameOfSfi = r12NameOfSfi;
		}

		public String getR12NameOfCounterparty() {
			return r12NameOfCounterparty;
		}

		public void setR12NameOfCounterparty(String r12NameOfCounterparty) {
			this.r12NameOfCounterparty = r12NameOfCounterparty;
		}

		public BigDecimal getR12LeAmountsX010() {
			return r12LeAmountsX010;
		}

		public void setR12LeAmountsX010(BigDecimal r12LeAmountsX010) {
			this.r12LeAmountsX010 = r12LeAmountsX010;
		}

		public BigDecimal getR12DpAmountsX020() {
			return r12DpAmountsX020;
		}

		public void setR12DpAmountsX020(BigDecimal r12DpAmountsX020) {
			this.r12DpAmountsX020 = r12DpAmountsX020;
		}

		public BigDecimal getR12StlAmountsX030() {
			return r12StlAmountsX030;
		}

		public void setR12StlAmountsX030(BigDecimal r12StlAmountsX030) {
			this.r12StlAmountsX030 = r12StlAmountsX030;
		}

		public BigDecimal getR12EquityAmountsX040() {
			return r12EquityAmountsX040;
		}

		public void setR12EquityAmountsX040(BigDecimal r12EquityAmountsX040) {
			this.r12EquityAmountsX040 = r12EquityAmountsX040;
		}

		public BigDecimal getR12NeiBdAmountsX050() {
			return r12NeiBdAmountsX050;
		}

		public void setR12NeiBdAmountsX050(BigDecimal r12NeiBdAmountsX050) {
			this.r12NeiBdAmountsX050 = r12NeiBdAmountsX050;
		}

		public BigDecimal getR12NeiCpAmountsX060() {
			return r12NeiCpAmountsX060;
		}

		public void setR12NeiCpAmountsX060(BigDecimal r12NeiCpAmountsX060) {
			this.r12NeiCpAmountsX060 = r12NeiCpAmountsX060;
		}

		public BigDecimal getR12NeiCdAmountsX070() {
			return r12NeiCdAmountsX070;
		}

		public void setR12NeiCdAmountsX070(BigDecimal r12NeiCdAmountsX070) {
			this.r12NeiCdAmountsX070 = r12NeiCdAmountsX070;
		}

		public BigDecimal getR12NeiT2bAmountsX080() {
			return r12NeiT2bAmountsX080;
		}

		public void setR12NeiT2bAmountsX080(BigDecimal r12NeiT2bAmountsX080) {
			this.r12NeiT2bAmountsX080 = r12NeiT2bAmountsX080;
		}

		public BigDecimal getR12NeiUomfAmountsX090() {
			return r12NeiUomfAmountsX090;
		}

		public void setR12NeiUomfAmountsX090(BigDecimal r12NeiUomfAmountsX090) {
			this.r12NeiUomfAmountsX090 = r12NeiUomfAmountsX090;
		}

		public BigDecimal getR12NeiUovfAmountsX100() {
			return r12NeiUovfAmountsX100;
		}

		public void setR12NeiUovfAmountsX100(BigDecimal r12NeiUovfAmountsX100) {
			this.r12NeiUovfAmountsX100 = r12NeiUovfAmountsX100;
		}

		public BigDecimal getR12NeiPoieAmountsX110() {
			return r12NeiPoieAmountsX110;
		}

		public void setR12NeiPoieAmountsX110(BigDecimal r12NeiPoieAmountsX110) {
			this.r12NeiPoieAmountsX110 = r12NeiPoieAmountsX110;
		}

		public BigDecimal getR12NeiPoaAmountsX120() {
			return r12NeiPoaAmountsX120;
		}

		public void setR12NeiPoaAmountsX120(BigDecimal r12NeiPoaAmountsX120) {
			this.r12NeiPoaAmountsX120 = r12NeiPoaAmountsX120;
		}

		public BigDecimal getR12NeiOthersAmountsX130() {
			return r12NeiOthersAmountsX130;
		}

		public void setR12NeiOthersAmountsX130(BigDecimal r12NeiOthersAmountsX130) {
			this.r12NeiOthersAmountsX130 = r12NeiOthersAmountsX130;
		}

		public BigDecimal getR12TotalAmountsX140() {
			return r12TotalAmountsX140;
		}

		public void setR12TotalAmountsX140(BigDecimal r12TotalAmountsX140) {
			this.r12TotalAmountsX140 = r12TotalAmountsX140;
		}

		public String getR13NameOfSfi() {
			return r13NameOfSfi;
		}

		public void setR13NameOfSfi(String r13NameOfSfi) {
			this.r13NameOfSfi = r13NameOfSfi;
		}

		public String getR13NameOfCounterparty() {
			return r13NameOfCounterparty;
		}

		public void setR13NameOfCounterparty(String r13NameOfCounterparty) {
			this.r13NameOfCounterparty = r13NameOfCounterparty;
		}

		public BigDecimal getR13LeAmountsX010() {
			return r13LeAmountsX010;
		}

		public void setR13LeAmountsX010(BigDecimal r13LeAmountsX010) {
			this.r13LeAmountsX010 = r13LeAmountsX010;
		}

		public BigDecimal getR13DpAmountsX020() {
			return r13DpAmountsX020;
		}

		public void setR13DpAmountsX020(BigDecimal r13DpAmountsX020) {
			this.r13DpAmountsX020 = r13DpAmountsX020;
		}

		public BigDecimal getR13StlAmountsX030() {
			return r13StlAmountsX030;
		}

		public void setR13StlAmountsX030(BigDecimal r13StlAmountsX030) {
			this.r13StlAmountsX030 = r13StlAmountsX030;
		}

		public BigDecimal getR13EquityAmountsX040() {
			return r13EquityAmountsX040;
		}

		public void setR13EquityAmountsX040(BigDecimal r13EquityAmountsX040) {
			this.r13EquityAmountsX040 = r13EquityAmountsX040;
		}

		public BigDecimal getR13NeiBdAmountsX050() {
			return r13NeiBdAmountsX050;
		}

		public void setR13NeiBdAmountsX050(BigDecimal r13NeiBdAmountsX050) {
			this.r13NeiBdAmountsX050 = r13NeiBdAmountsX050;
		}

		public BigDecimal getR13NeiCpAmountsX060() {
			return r13NeiCpAmountsX060;
		}

		public void setR13NeiCpAmountsX060(BigDecimal r13NeiCpAmountsX060) {
			this.r13NeiCpAmountsX060 = r13NeiCpAmountsX060;
		}

		public BigDecimal getR13NeiCdAmountsX070() {
			return r13NeiCdAmountsX070;
		}

		public void setR13NeiCdAmountsX070(BigDecimal r13NeiCdAmountsX070) {
			this.r13NeiCdAmountsX070 = r13NeiCdAmountsX070;
		}

		public BigDecimal getR13NeiT2bAmountsX080() {
			return r13NeiT2bAmountsX080;
		}

		public void setR13NeiT2bAmountsX080(BigDecimal r13NeiT2bAmountsX080) {
			this.r13NeiT2bAmountsX080 = r13NeiT2bAmountsX080;
		}

		public BigDecimal getR13NeiUomfAmountsX090() {
			return r13NeiUomfAmountsX090;
		}

		public void setR13NeiUomfAmountsX090(BigDecimal r13NeiUomfAmountsX090) {
			this.r13NeiUomfAmountsX090 = r13NeiUomfAmountsX090;
		}

		public BigDecimal getR13NeiUovfAmountsX100() {
			return r13NeiUovfAmountsX100;
		}

		public void setR13NeiUovfAmountsX100(BigDecimal r13NeiUovfAmountsX100) {
			this.r13NeiUovfAmountsX100 = r13NeiUovfAmountsX100;
		}

		public BigDecimal getR13NeiPoieAmountsX110() {
			return r13NeiPoieAmountsX110;
		}

		public void setR13NeiPoieAmountsX110(BigDecimal r13NeiPoieAmountsX110) {
			this.r13NeiPoieAmountsX110 = r13NeiPoieAmountsX110;
		}

		public BigDecimal getR13NeiPoaAmountsX120() {
			return r13NeiPoaAmountsX120;
		}

		public void setR13NeiPoaAmountsX120(BigDecimal r13NeiPoaAmountsX120) {
			this.r13NeiPoaAmountsX120 = r13NeiPoaAmountsX120;
		}

		public BigDecimal getR13NeiOthersAmountsX130() {
			return r13NeiOthersAmountsX130;
		}

		public void setR13NeiOthersAmountsX130(BigDecimal r13NeiOthersAmountsX130) {
			this.r13NeiOthersAmountsX130 = r13NeiOthersAmountsX130;
		}

		public BigDecimal getR13TotalAmountsX140() {
			return r13TotalAmountsX140;
		}

		public void setR13TotalAmountsX140(BigDecimal r13TotalAmountsX140) {
			this.r13TotalAmountsX140 = r13TotalAmountsX140;
		}

		public String getR14NameOfSfi() {
			return r14NameOfSfi;
		}

		public void setR14NameOfSfi(String r14NameOfSfi) {
			this.r14NameOfSfi = r14NameOfSfi;
		}

		public String getR14NameOfCounterparty() {
			return r14NameOfCounterparty;
		}

		public void setR14NameOfCounterparty(String r14NameOfCounterparty) {
			this.r14NameOfCounterparty = r14NameOfCounterparty;
		}

		public BigDecimal getR14LeAmountsX010() {
			return r14LeAmountsX010;
		}

		public void setR14LeAmountsX010(BigDecimal r14LeAmountsX010) {
			this.r14LeAmountsX010 = r14LeAmountsX010;
		}

		public BigDecimal getR14DpAmountsX020() {
			return r14DpAmountsX020;
		}

		public void setR14DpAmountsX020(BigDecimal r14DpAmountsX020) {
			this.r14DpAmountsX020 = r14DpAmountsX020;
		}

		public BigDecimal getR14StlAmountsX030() {
			return r14StlAmountsX030;
		}

		public void setR14StlAmountsX030(BigDecimal r14StlAmountsX030) {
			this.r14StlAmountsX030 = r14StlAmountsX030;
		}

		public BigDecimal getR14EquityAmountsX040() {
			return r14EquityAmountsX040;
		}

		public void setR14EquityAmountsX040(BigDecimal r14EquityAmountsX040) {
			this.r14EquityAmountsX040 = r14EquityAmountsX040;
		}

		public BigDecimal getR14NeiBdAmountsX050() {
			return r14NeiBdAmountsX050;
		}

		public void setR14NeiBdAmountsX050(BigDecimal r14NeiBdAmountsX050) {
			this.r14NeiBdAmountsX050 = r14NeiBdAmountsX050;
		}

		public BigDecimal getR14NeiCpAmountsX060() {
			return r14NeiCpAmountsX060;
		}

		public void setR14NeiCpAmountsX060(BigDecimal r14NeiCpAmountsX060) {
			this.r14NeiCpAmountsX060 = r14NeiCpAmountsX060;
		}

		public BigDecimal getR14NeiCdAmountsX070() {
			return r14NeiCdAmountsX070;
		}

		public void setR14NeiCdAmountsX070(BigDecimal r14NeiCdAmountsX070) {
			this.r14NeiCdAmountsX070 = r14NeiCdAmountsX070;
		}

		public BigDecimal getR14NeiT2bAmountsX080() {
			return r14NeiT2bAmountsX080;
		}

		public void setR14NeiT2bAmountsX080(BigDecimal r14NeiT2bAmountsX080) {
			this.r14NeiT2bAmountsX080 = r14NeiT2bAmountsX080;
		}

		public BigDecimal getR14NeiUomfAmountsX090() {
			return r14NeiUomfAmountsX090;
		}

		public void setR14NeiUomfAmountsX090(BigDecimal r14NeiUomfAmountsX090) {
			this.r14NeiUomfAmountsX090 = r14NeiUomfAmountsX090;
		}

		public BigDecimal getR14NeiUovfAmountsX100() {
			return r14NeiUovfAmountsX100;
		}

		public void setR14NeiUovfAmountsX100(BigDecimal r14NeiUovfAmountsX100) {
			this.r14NeiUovfAmountsX100 = r14NeiUovfAmountsX100;
		}

		public BigDecimal getR14NeiPoieAmountsX110() {
			return r14NeiPoieAmountsX110;
		}

		public void setR14NeiPoieAmountsX110(BigDecimal r14NeiPoieAmountsX110) {
			this.r14NeiPoieAmountsX110 = r14NeiPoieAmountsX110;
		}

		public BigDecimal getR14NeiPoaAmountsX120() {
			return r14NeiPoaAmountsX120;
		}

		public void setR14NeiPoaAmountsX120(BigDecimal r14NeiPoaAmountsX120) {
			this.r14NeiPoaAmountsX120 = r14NeiPoaAmountsX120;
		}

		public BigDecimal getR14NeiOthersAmountsX130() {
			return r14NeiOthersAmountsX130;
		}

		public void setR14NeiOthersAmountsX130(BigDecimal r14NeiOthersAmountsX130) {
			this.r14NeiOthersAmountsX130 = r14NeiOthersAmountsX130;
		}

		public BigDecimal getR14TotalAmountsX140() {
			return r14TotalAmountsX140;
		}

		public void setR14TotalAmountsX140(BigDecimal r14TotalAmountsX140) {
			this.r14TotalAmountsX140 = r14TotalAmountsX140;
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

		public Date getREPORT_RESUBDATE() {
			return REPORT_RESUBDATE;
		}

		public void setREPORT_RESUBDATE(Date REPORT_RESUBDATE) {
			this.REPORT_RESUBDATE = REPORT_RESUBDATE;
		}
	}

	// Detail Entity
	public class FINCON_1C_Detail_Entity {
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
	class Fincon1C_DetailRowMapper implements RowMapper<FINCON_1C_Detail_Entity> {
		@Override
		public FINCON_1C_Detail_Entity mapRow(ResultSet rs, int rowNum) throws SQLException {
			FINCON_1C_Detail_Entity obj = new FINCON_1C_Detail_Entity();

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
	public class Fincon_1C_Archival_Detail_Entity {
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
	class Fincon_1c_ArchivalDetailRowMapper implements RowMapper<Fincon_1C_Archival_Detail_Entity> {
		@Override
		public Fincon_1C_Archival_Detail_Entity mapRow(ResultSet rs, int rowNum) throws SQLException {
			Fincon_1C_Archival_Detail_Entity obj = new Fincon_1C_Archival_Detail_Entity();

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