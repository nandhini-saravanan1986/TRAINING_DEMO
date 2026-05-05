

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
public class BRRS_BG_DBS10_FINCON_III_1A_ReportServices {

	
	private static final Logger logger = LoggerFactory.getLogger(BRRS_BG_DBS10_FINCON_III_1A_ReportServices.class);
	
	@Autowired
	private Environment env;

	
	 @Autowired
	 private JdbcTemplate jdbcTemplate;

	 
	 SimpleDateFormat dateformat =
	         new SimpleDateFormat("dd-MMM-yyyy");

	 //Summary view
	 public ModelAndView getBG_FINCON_1A_View(

		        String reportId,
		        String fromdate,
		        String todate,
		        String currency,
		        String dtltype,
		        Pageable pageable,
		        String type,
		        BigDecimal version) {

		    ModelAndView mv = new ModelAndView();

		    System.out.println("Fincon 1A CALLED");
		    System.out.println("Type = " + type);
		    System.out.println("Version = " + version);

		    // =====================================================
		    // ARCHIVAL MODE
		    // =====================================================

		    if ("ARCHIVAL".equals(type) && version != null) {

		        List<BRRS_BG_DBIS10_FINCON_III_1A_Archival_ST_Entity> T1Master = new ArrayList<>();

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

		        List<BRRS_BG_DBIS10_FINCON_III_1A_ST_Entity> T1Master = new ArrayList<>();

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

		    mv.setViewName("BRRS/BG_FINCON_1A");
		    mv.addObject("displaymode", "summary");
		   

		    System.out.println("View Loaded: " + mv.getViewName());

		    return mv;
		}

	 
	 
	 //Detail view
	 public ModelAndView getFincon_1A_currentDtl(
		        String reportId,
		        String fromdate,
		        String todate,
		        String currency,
		        String dtltype,
		        Pageable pageable,
		        String filter,
		        String type,
		        String version) {

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

		            List<Fincon_1A_Archival_Detail_Entity> archivalDetailList;

		            if (reportLabel != null && reportAddlCriteria1 != null) {

		                archivalDetailList =
		                      GetArchivalDataByRowIdAndColumnId(
		                                reportLabel,
		                                reportAddlCriteria1,
		                                parsedDate,
		                                version
		                        );

		            } else {

		                archivalDetailList =
		                       getArchivalDetaildatabydateList(
		                                parsedDate,
		                                version
		                        );
		            }

		            mv.addObject("reportdetails", archivalDetailList);
		            mv.addObject("reportmaster12", archivalDetailList);

		            System.out.println("ARCHIVAL DETAIL COUNT: " + archivalDetailList.size());

		        }

		        // =====================================================
		        // CURRENT MODE
		        // =====================================================
		        else {

		            List<FINCON_1A_Detail_Entity> currentDetailList;

		            if (reportLabel != null && reportAddlCriteria1 != null) {

		                currentDetailList =
		                       GetDetailDataByRowIdAndColumnId(
		                                reportLabel,
		                                reportAddlCriteria1,
		                                parsedDate
		                        );

		            } else {

		                currentDetailList =
		                       getDetaildatabydateList(parsedDate);

		            }

		            mv.addObject("reportdetails", currentDetailList);
		            mv.addObject("reportmaster12", currentDetailList);

		            System.out.println("CURRENT DETAIL COUNT: " + currentDetailList.size());
		        }

		    } catch (Exception e) {
		        e.printStackTrace();
		        mv.addObject("errorMessage", e.getMessage());
		    }

		    mv.setViewName("BRRS/BG_FINCON_1A");
		    mv.addObject("displaymode", "Details");
		    mv.addObject("menu", reportId);
		    mv.addObject("currency", currency);
		    mv.addObject("reportId", reportId);

		    return mv;
		}
	 
	 
	 
	//Archival View
	public List<Object[]> getFincon_1A_newArchival() {
				List<Object[]> archivalList = new ArrayList<>();
				

				try {
					
					List<BRRS_BG_DBIS10_FINCON_III_1A_Archival_ST_Entity> repoData =
							getdatabydateListWithVersion();

					if (repoData != null && !repoData.isEmpty()) {
						for (BRRS_BG_DBIS10_FINCON_III_1A_Archival_ST_Entity entity : repoData) {
							Object[] row = new Object[] {
									entity.getReportDate(), 
									entity.getReportVersion(), 
									 entity.getREPORT_RESUBDATE()
							};
							archivalList.add(row);
						}

						System.out.println("Fetched " + archivalList.size() + " archival records");
						BRRS_BG_DBIS10_FINCON_III_1A_Archival_ST_Entity first = repoData.get(0);
						System.out.println("Latest archival version: " + first.getReportVersion());
					} else {
						System.out.println("No archival data found.");
					}

				} catch (Exception e) {
					System.err.println("Error fetching  Fincon_1A  Archival data: " + e.getMessage());
					e.printStackTrace();
				}

				return archivalList;
			}

	 
	 
	 //Detail Download
	public byte[] getFincon_1A_DetailExcel(String filename, String fromdate, String todate, String currency, String dtltype,
			String type, String version) {
		try {
			logger.info("Generating Excel for  Fincon_1A Details...");
			System.out.println("came to Detail download service");

			if (type.equals("ARCHIVAL") & version != null) {
				byte[] ARCHIVALreport = getFincon_1A_DetailExcelARCHIVAL(filename, fromdate, todate, currency, dtltype, type,
						version);
				return ARCHIVALreport;
			}

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("BG_DBIS_FINCON_III_1A");

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
			String[] headers = {  "CUST ID", "ACCT NO", "ACCT NAME", "ACCT BALANCE", "REPORT LABEL", "REPORT ADDL CRITERIA1", "REPORT_DATE" };

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
			List<FINCON_1A_Detail_Entity> reportData = getDetaildatabydateList(parsedToDate);

			if (reportData != null && !reportData.isEmpty()) {
				int rowIndex = 1;
				for (FINCON_1A_Detail_Entity item : reportData) { 
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
				logger.info("No data found for Fincon_1A — only header will be written.");
			}

			// Write to byte[]
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			workbook.write(bos);
			workbook.close();

			logger.info("Excel generation completed with {} row(s).", reportData != null ? reportData.size() : 0);
			return bos.toByteArray();

		} catch (Exception e) {
			logger.error("Error generating FINCON_1A Excel", e);
			return new byte[0];
		}
	}
	
	public byte[] getFincon_1A_DetailExcelARCHIVAL(String filename, String fromdate, String todate, String currency,
			String dtltype, String type, String version) {
		try {
			logger.info("Generating Excel for Fincon_1A ARCHIVAL Details...");
			System.out.println("came to ARCHIVAL Detail download service");
			if (type.equals("ARCHIVAL") & version != null) {

			}
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("BG_DBIS_FINCON_III_1A");

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
			String[] headers = {  "CUST ID", "ACCT NO", "ACCT NAME", "ACCT BALANCE",  "REPORT LABEL", "REPORT ADDL CRITERIA1", "REPORT_DATE" };

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
			List<Fincon_1A_Archival_Detail_Entity> reportData = getArchivalDetaildatabydateList(parsedToDate,
					version);

			if (reportData != null && !reportData.isEmpty()) {
				int rowIndex = 1;
				for (Fincon_1A_Archival_Detail_Entity item : reportData) {
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
				logger.info("No data found for Fincon_1A — only header will be written.");
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

	 
	
	//Summary Download
	public byte[] getFincon_1A_Excel(String filename, String reportId, String fromdate, String todate, String currency,
			String dtltype, String type, BigDecimal version) throws Exception {
		logger.info("Service: Starting Excel generation process in memory.sch17");

		// ARCHIVAL check
		if ("ARCHIVAL".equalsIgnoreCase(type)
		        && version != null
		        && version.compareTo(BigDecimal.ZERO) >= 0) {
				logger.info("Service: Generating ARCHIVAL report for version {}", version);
			return getExcelFincon_1A_ARCHIVALExcel(filename, reportId, fromdate, todate, currency, dtltype, type, version);
		}

		// Fetch data

		List<BRRS_BG_DBIS10_FINCON_III_1A_ST_Entity> dataList = getDataByDate(dateformat.parse(todate));
	
		System.out.println("DATA SIZE IS : "+dataList.size());
		if (dataList.isEmpty()) {
			logger.warn("Service: No data found for  Fincon_1A report. Returning empty result.");
			return new byte[0];
		}

		String templateDir = env.getProperty("output.exportpathtemp");
		String templateFileName = filename;
		System.out.println(filename);
		Path templatePath = Paths.get(templateDir, templateFileName);
		System.out.println(templatePath);

		logger.info("Service: Attempting to load template from path: {}", templatePath.toAbsolutePath());

		if (!Files.exists(templatePath)) {
			// This specific exception will be caught by the controller.
			throw new FileNotFoundException("Template file not found at: " + templatePath.toAbsolutePath());
		}
		if (!Files.isReadable(templatePath)) {
			// A specific exception for permission errors.
			throw new SecurityException(
					"Template file exists but is not readable (check permissions): " + templatePath.toAbsolutePath());
		}

		// This try-with-resources block is perfect. It guarantees all resources are
		// closed automatically.
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
			// numberStyle.setDataFormat(createHelper.createDataFormat().getFormat("0.000"));
			numberStyle.setBorderBottom(BorderStyle.THIN);
			numberStyle.setBorderTop(BorderStyle.THIN);
			numberStyle.setBorderLeft(BorderStyle.THIN);
			numberStyle.setBorderRight(BorderStyle.THIN);
			numberStyle.setFont(font);
			// --- End of Style Definitions ---

			int startRow = 4;

			if (!dataList.isEmpty()) {
				for (int i = 0; i < dataList.size(); i++) {
					BRRS_BG_DBIS10_FINCON_III_1A_ST_Entity record = dataList.get(i);
					System.out.println("rownumber=" + startRow + i);
					Row row = sheet.getRow(startRow + i);
					if (row == null) {
						row = sheet.createRow(startRow + i);
					}


					// Column D
					Cell cellC = row.createCell(3);
					if(record.getR5Amount() != null) {
						cellC.setCellValue(record.getR5Amount().doubleValue());
					    cellC.setCellStyle(numberStyle);
					}
					else {
					    cellC.setCellValue(0);   // IMPORTANT
					    cellC.setCellStyle(numberStyle);
					}

					


					// R6
					row = sheet.getRow(5);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR6Amount() != null ? record.getR6Amount().doubleValue() : 0);

					// R7
					row = sheet.getRow(6);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR7Amount() != null ? record.getR7Amount().doubleValue() : 0);

					// R8
					row = sheet.getRow(7);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR8Amount() != null ? record.getR8Amount().doubleValue() : 0);

					// R9
					row = sheet.getRow(8);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR9Amount() != null ? record.getR9Amount().doubleValue() : 0);

					// R10
					row = sheet.getRow(9);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR10Amount() != null ? record.getR10Amount().doubleValue() : 0);

					// R11
					row = sheet.getRow(10);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR11Amount() != null ? record.getR11Amount().doubleValue() : 0);

					// R12
					row = sheet.getRow(11);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR12Amount() != null ? record.getR12Amount().doubleValue() : 0);

					/*
					 * // R13 row = sheet.getRow(12); cellC = row.getCell(3); if (cellC == null)
					 * cellC = row.createCell(3); cellC.setCellValue(record.getR13Amount() != null ?
					 * record.getR13Amount().doubleValue() : 0);
					 */
					// R14
					
					row = sheet.getRow(13);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR14Amount() != null ? record.getR14Amount().doubleValue() : 0);

					// R15
					row = sheet.getRow(14);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR15Amount() != null ? record.getR15Amount().doubleValue() : 0);

					// R16
					row = sheet.getRow(15);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR16Amount() != null ? record.getR16Amount().doubleValue() : 0);

					// R17
					row = sheet.getRow(16);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR17Amount() != null ? record.getR17Amount().doubleValue() : 0);

					// R18
					row = sheet.getRow(17);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR18Amount() != null ? record.getR18Amount().doubleValue() : 0);

					// R19
					row = sheet.getRow(18);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR19Amount() != null ? record.getR19Amount().doubleValue() : 0);

					// R20
					row = sheet.getRow(19);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR20Amount() != null ? record.getR20Amount().doubleValue() : 0);

					// R21
					row = sheet.getRow(20);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR21Amount() != null ? record.getR21Amount().doubleValue() : 0);

					// R22
					row = sheet.getRow(21);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR22Amount() != null ? record.getR22Amount().doubleValue() : 0);

					// R23
					row = sheet.getRow(22);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR23Amount() != null ? record.getR23Amount().doubleValue() : 0);

					// R24
					row = sheet.getRow(23);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR24Amount() != null ? record.getR24Amount().doubleValue() : 0);

					// R25
					row = sheet.getRow(24);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR25Amount() != null ? record.getR25Amount().doubleValue() : 0);

					// R26
					row = sheet.getRow(25);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR26Amount() != null ? record.getR26Amount().doubleValue() : 0);

					// R27
					row = sheet.getRow(26);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR27Amount() != null ? record.getR27Amount().doubleValue() : 0);

					// R28
					row = sheet.getRow(27);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR28Amount() != null ? record.getR28Amount().doubleValue() : 0);

					// R29 skipped

					// R30 (next line after R28)
					row = sheet.getRow(29);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR30Amount() != null ? record.getR30Amount().doubleValue() : 0);

					// R31
					row = sheet.getRow(30);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR31Amount() != null ? record.getR31Amount().doubleValue() : 0);

					// R32 skipped

					// R33
					row = sheet.getRow(31);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR32Amount() != null ? record.getR32Amount().doubleValue() : 0);

					// R34
					row = sheet.getRow(33);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR34Amount() != null ? record.getR34Amount().doubleValue() : 0);

					// R35
					row = sheet.getRow(34);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR35Amount() != null ? record.getR35Amount().doubleValue() : 0);

					// R36
					row = sheet.getRow(35);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR36Amount() != null ? record.getR36Amount().doubleValue() : 0);

					// R37
					row = sheet.getRow(36);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR37Amount() != null ? record.getR37Amount().doubleValue() : 0);

					// R38
					row = sheet.getRow(37);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR38Amount() != null ? record.getR38Amount().doubleValue() : 0);

					// R39
					row = sheet.getRow(38);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR39Amount() != null ? record.getR39Amount().doubleValue() : 0);

					// R40
					row = sheet.getRow(39);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR40Amount() != null ? record.getR40Amount().doubleValue() : 0);

					// R41
					row = sheet.getRow(40);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR41Amount() != null ? record.getR41Amount().doubleValue() : 0);

					// R42
					row = sheet.getRow(41);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR42Amount() != null ? record.getR42Amount().doubleValue() : 0);
					
				}

				
			} else {

			}

// Write the final workbook content to the in-memory stream.
			workbook.write(out);

			logger.info("Service: Excel data successfully written to memory buffer ({} bytes).", out.size());

			return out.toByteArray();
		}

	}
	
	
	public byte[] getExcelFincon_1A_ARCHIVALExcel(String filename, String reportId, String fromdate, String todate,
			String currency, String dtltype, String type, BigDecimal version) throws Exception {

		logger.info("Service: Starting Excel generation process in memory.");

		

		List<BRRS_BG_DBIS10_FINCON_III_1A_Archival_ST_Entity> dataList = 
				getdatabydateListarchival(dateformat.parse(todate), version);
	
       

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
// This specific exception will be caught by the controller.
			throw new FileNotFoundException("Template file not found at: " + templatePath.toAbsolutePath());
		}
		if (!Files.isReadable(templatePath)) {
// A specific exception for permission errors.
			throw new SecurityException(
					"Template file exists but is not readable (check permissions): " + templatePath.toAbsolutePath());
		}

// This try-with-resources block is perfect. It guarantees all resources are
// closed automatically.
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
// numberStyle.setDataFormat(createHelper.createDataFormat().getFormat("0.000"));
			numberStyle.setBorderBottom(BorderStyle.THIN);
			numberStyle.setBorderTop(BorderStyle.THIN);
			numberStyle.setBorderLeft(BorderStyle.THIN);
			numberStyle.setBorderRight(BorderStyle.THIN);
			numberStyle.setFont(font);
// --- End of Style Definitions ---

			int startRow = 4;

			if (!dataList.isEmpty()) {
				for (int i = 0; i < dataList.size(); i++) {
					BRRS_BG_DBIS10_FINCON_III_1A_Archival_ST_Entity record = dataList.get(i);
					System.out.println("rownumber=" + startRow + i);
					Row row = sheet.getRow(startRow + i);
					if (row == null) {
						row = sheet.createRow(startRow + i);
					}


					// Column D
					Cell cellC = row.createCell(3);
					if(record.getR5Amount() != null) {
						cellC.setCellValue(record.getR5Amount().doubleValue());
					    cellC.setCellStyle(numberStyle);
					}
					else {
					    cellC.setCellValue(0);   // IMPORTANT
					    cellC.setCellStyle(numberStyle);
					}

					


					// R6
					row = sheet.getRow(5);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR6Amount() != null ? record.getR6Amount().doubleValue() : 0);

					// R7
					row = sheet.getRow(6);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR7Amount() != null ? record.getR7Amount().doubleValue() : 0);

					// R8
					row = sheet.getRow(7);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR8Amount() != null ? record.getR8Amount().doubleValue() : 0);

					// R9
					row = sheet.getRow(8);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR9Amount() != null ? record.getR9Amount().doubleValue() : 0);

					// R10
					row = sheet.getRow(9);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR10Amount() != null ? record.getR10Amount().doubleValue() : 0);

					// R11
					row = sheet.getRow(10);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR11Amount() != null ? record.getR11Amount().doubleValue() : 0);

					// R12
					row = sheet.getRow(11);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR12Amount() != null ? record.getR12Amount().doubleValue() : 0);

					/*
					 * // R13 row = sheet.getRow(12); cellC = row.getCell(3); if (cellC == null)
					 * cellC = row.createCell(3); cellC.setCellValue(record.getR13Amount() != null ?
					 * record.getR13Amount().doubleValue() : 0);
					 */
					// R14
					
					row = sheet.getRow(13);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR14Amount() != null ? record.getR14Amount().doubleValue() : 0);

					// R15
					row = sheet.getRow(14);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR15Amount() != null ? record.getR15Amount().doubleValue() : 0);

					// R16
					row = sheet.getRow(15);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR16Amount() != null ? record.getR16Amount().doubleValue() : 0);

					// R17
					row = sheet.getRow(16);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR17Amount() != null ? record.getR17Amount().doubleValue() : 0);

					// R18
					row = sheet.getRow(17);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR18Amount() != null ? record.getR18Amount().doubleValue() : 0);

					// R19
					row = sheet.getRow(18);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR19Amount() != null ? record.getR19Amount().doubleValue() : 0);

					// R20
					row = sheet.getRow(19);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR20Amount() != null ? record.getR20Amount().doubleValue() : 0);

					// R21
					row = sheet.getRow(20);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR21Amount() != null ? record.getR21Amount().doubleValue() : 0);

					// R22
					row = sheet.getRow(21);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR22Amount() != null ? record.getR22Amount().doubleValue() : 0);

					// R23
					row = sheet.getRow(22);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR23Amount() != null ? record.getR23Amount().doubleValue() : 0);

					// R24
					row = sheet.getRow(23);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR24Amount() != null ? record.getR24Amount().doubleValue() : 0);

					// R25
					row = sheet.getRow(24);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR25Amount() != null ? record.getR25Amount().doubleValue() : 0);

					// R26
					row = sheet.getRow(25);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR26Amount() != null ? record.getR26Amount().doubleValue() : 0);

					// R27
					row = sheet.getRow(26);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR27Amount() != null ? record.getR27Amount().doubleValue() : 0);

					// R28
					row = sheet.getRow(27);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR28Amount() != null ? record.getR28Amount().doubleValue() : 0);

					// R29 skipped

					// R30 (next line after R28)
					row = sheet.getRow(29);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR30Amount() != null ? record.getR30Amount().doubleValue() : 0);

					// R31
					row = sheet.getRow(30);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR31Amount() != null ? record.getR31Amount().doubleValue() : 0);

					// R32 skipped

					// R33
					row = sheet.getRow(31);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR32Amount() != null ? record.getR32Amount().doubleValue() : 0);

					// R34
					row = sheet.getRow(33);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR34Amount() != null ? record.getR34Amount().doubleValue() : 0);

					// R35
					row = sheet.getRow(34);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR35Amount() != null ? record.getR35Amount().doubleValue() : 0);

					// R36
					row = sheet.getRow(35);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR36Amount() != null ? record.getR36Amount().doubleValue() : 0);

					// R37
					row = sheet.getRow(36);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR37Amount() != null ? record.getR37Amount().doubleValue() : 0);

					// R38
					row = sheet.getRow(37);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR38Amount() != null ? record.getR38Amount().doubleValue() : 0);

					// R39
					row = sheet.getRow(38);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR39Amount() != null ? record.getR39Amount().doubleValue() : 0);

					// R40
					row = sheet.getRow(39);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR40Amount() != null ? record.getR40Amount().doubleValue() : 0);

					// R41
					row = sheet.getRow(40);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR41Amount() != null ? record.getR41Amount().doubleValue() : 0);

					// R42
					row = sheet.getRow(41);
					cellC = row.getCell(3);
					if (cellC == null) cellC = row.createCell(3);
					cellC.setCellValue(record.getR42Amount() != null ? record.getR42Amount().doubleValue() : 0);
					
				}

				
			} else {

			}

// Write the final workbook content to the in-memory stream.
			workbook.write(out);

			logger.info("Service: Excel data successfully written to memory buffer ({} bytes).", out.size());

			return out.toByteArray();
		}

	}
	

	
	 // Fetch Summary data by report date
	    public List<BRRS_BG_DBIS10_FINCON_III_1A_ST_Entity> getDataByDate(Date reportDate) {

	        String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1A_SUMMARYTABLE WHERE REPORT_DATE = ?";

	        return jdbcTemplate.query(
	                sql,
	                new Object[]{reportDate},
	                new Fincon_1A_RowMapper()
	        );
	    }
	
	 // =========================================================
	    // GET REPORT_DATE + REPORT_VERSION
	    // =========================================================

	    public List<Object[]> getDBIS10_FINCON_1A_archival() {

	        String sql =
	            "SELECT REPORT_DATE, REPORT_VERSION,REPORT_RESUBDATE " +
	            "FROM BRRS_BG_DBIS10_FINCON_III_1A_ARCHIVALTABLE_SUMMARY " +
	            "ORDER BY REPORT_VERSION";

	        return jdbcTemplate.query(
	            sql,
	            (rs, rowNum) -> new Object[] {
	                rs.getDate("REPORT_DATE"),
	                rs.getBigDecimal("REPORT_VERSION"),
	                rs.getDate("REPORT_RESUBDATE")
	            }
	        );
	    }
	    
	   //=========================================================
	   //GET ARCHIVAL FULL DATA BY DATE + VERSION
	   //=========================================================
	   public List<BRRS_BG_DBIS10_FINCON_III_1A_Archival_ST_Entity>
	   getdatabydateListarchival(
	         Date reportDate,
	         BigDecimal reportVersion) {

	     String sql =
	         "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1A_ARCHIVALTABLE_SUMMARY " +
	         "WHERE REPORT_DATE = ? " +
	         "AND REPORT_VERSION = ?";

	     return jdbcTemplate.query(
	             sql,
	             new Object[]{
	                     reportDate,
	                     reportVersion
	             },
	             new Fincon_1A_Archival_RowMapper()
	     );
	   }
	   //=========================================================
	   //GET ALL WITH VERSION
	   //=========================================================

	   public List<BRRS_BG_DBIS10_FINCON_III_1A_Archival_ST_Entity>
	   getdatabydateListWithVersion() {

	    String sql =
	        "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1A_ARCHIVALTABLE_SUMMARY " +
	        "WHERE REPORT_VERSION IS NOT NULL " +
	        "ORDER BY REPORT_VERSION ASC";

	    return jdbcTemplate.query(
	            sql,
	            new Fincon_1A_Archival_RowMapper()
	    );
	   }


	   //=========================================================
	   //GET MAX VERSION BY DATE
	   //=========================================================

	   public BigDecimal findMaxVersion(Date reportDate) {

	    String sql =
	        "SELECT MAX(REPORT_VERSION) " +
	        "FROM BRRS_BG_DBIS10_FINCON_III_1A_ARCHIVALTABLE_SUMMARY " +
	        "WHERE REPORT_DATE = ?";

	    return jdbcTemplate.queryForObject(
	            sql,
	            new Object[]{reportDate},
	            BigDecimal.class
	    );
	   }
	   
	// =========================================================
	// 1. BY DATE + LABEL + CRITERIA
	// =========================================================
	public List<FINCON_1A_Detail_Entity> findByDetailReportDateAndLabelAndCriteria(
	        Date reportDate,
	        String reportLabel,
	        String reportAddlCriteria1) {

	    String sql =
	        "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1A_DETAILTABLE " +
	        "WHERE REPORT_DATE = ? AND REPORT_LABEL = ? AND REPORT_ADDL_CRITERIA_1 = ?";

	    return jdbcTemplate.query(
	            sql,
	            new Object[]{reportDate, reportLabel, reportAddlCriteria1},
	            new Fincon1A_DetailRowMapper()
	    );
	}

	// =========================================================
	// 2. GET ALL (BY DATE - simple)
	// =========================================================
	public List<FINCON_1A_Detail_Entity> getDetaildatabydateList(Date reportdate) {

	    String sql = "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1A_DETAILTABLE WHERE REPORT_DATE = ?";

	    return jdbcTemplate.query(
	            sql,
	            new Object[]{reportdate},
	            new Fincon1A_DetailRowMapper()
	    );
	}

	// =========================================================
	// 3. PAGINATION
	// =========================================================
	public List<FINCON_1A_Detail_Entity> getDetaildatabydateList(
	        Date reportdate, int offset, int limit) {

	    String sql =
	        "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1A_DETAILTABLE " +
	        "WHERE REPORT_DATE = ? OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

	    return jdbcTemplate.query(
	            sql,
	            new Object[]{reportdate, offset, limit},
	            new Fincon1A_DetailRowMapper()
	    );
	}

	// =========================================================
	// 4. COUNT
	// =========================================================
	public int getDetaildatacount(Date reportdate) {

	    String sql =
	        "SELECT COUNT(*) FROM BRRS_BG_DBIS10_FINCON_III_1A_DETAILTABLE WHERE REPORT_DATE = ?";

	    return jdbcTemplate.queryForObject(
	            sql,
	            new Object[]{reportdate},
	            Integer.class
	    );
	}

	// =========================================================
	// 5. BY LABEL + CRITERIA
	// =========================================================
	public List<FINCON_1A_Detail_Entity> GetDetailDataByRowIdAndColumnId(
	        String reportLabel,
	        String reportAddlCriteria1,
	        Date reportdate) {

	    String sql =
	        "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1A_DETAILTABLE " +
	        "WHERE REPORT_LABEL = ? AND REPORT_ADDL_CRITERIA_1 = ? AND REPORT_DATE = ?";

	    return jdbcTemplate.query(
	            sql,
	            new Object[]{reportLabel, reportAddlCriteria1, reportdate},
	            new Fincon1A_DetailRowMapper()
	    );
	}

	// =========================================================
	// 6. BY ACCOUNT NUMBER
	// =========================================================
	public FINCON_1A_Detail_Entity findByAcctnumber(String acct_number) {

	    String sql =
	        "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1A_DETAILTABLE WHERE ACCT_NUMBER = ?";

	    return jdbcTemplate.queryForObject(
	            sql,
	            new Object[]{acct_number},
	            new Fincon1A_DetailRowMapper()
	    );
	}

	// =========================================================
	// 7. BY SNO
	// =========================================================
	public FINCON_1A_Detail_Entity findBySno(String sno) {

	    String sql =
	        "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1A_DETAILTABLE WHERE SNO = ?";

	    return jdbcTemplate.queryForObject(
	            sql,
	            new Object[]{sno},
	            new Fincon1A_DetailRowMapper()
	    );
	}


	// =========================================================
	// 1. GET BY DATE + VERSION
	// =========================================================
	public List<Fincon_1A_Archival_Detail_Entity> getArchivalDetaildatabydateList(
	        Date reportdate,
	        String dataEntryVersion) {

	    String sql =
	        "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1A_ARCHIVALTABLE_DETAIL " +
	        "WHERE REPORT_DATE = ? AND DATA_ENTRY_VERSION = ?";

	    return jdbcTemplate.query(
	            sql,
	            new Object[]{reportdate, dataEntryVersion},
	            new Fincon_1a_ArchivalDetailRowMapper()
	    );
	}

	// =========================================================
	// 2. FILTER BY LABEL + CRITERIA + DATE + VERSION
	// =========================================================
	public List<Fincon_1A_Archival_Detail_Entity> GetArchivalDataByRowIdAndColumnId(
	        String reportLabel,
	        String reportAddlCriteria1,
	        Date reportdate,
	        String dataEntryVersion) {

	    String sql =
	        "SELECT * FROM BRRS_BG_DBIS10_FINCON_III_1A_ARCHIVALTABLE_DETAIL " +
	        "WHERE REPORT_LABEL = ? " +
	        "AND REPORT_ADDL_CRITERIA_1 = ? " +
	        "AND REPORT_DATE = ? " +
	        "AND DATA_ENTRY_VERSION = ?";

	    return jdbcTemplate.query(
	            sql,
	            new Object[]{reportLabel, reportAddlCriteria1, reportdate, dataEntryVersion},
	            new Fincon_1a_ArchivalDetailRowMapper()
	    );
	}

	
	
	
	
	
	//Class for Summary table 
	public static class BRRS_BG_DBIS10_FINCON_III_1A_ST_Entity {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(name = "R5_PARTICULARS")
	    private String r5Particulars;
	    @Column(name = "R5_Y010")
	    private String r5Y010;
	    @Column(name = "R5_AMOUNT_X010")
	    private java.math.BigDecimal r5Amount;

	    @Column(name = "R6_PARTICULARS")
	    private String r6Particulars;
	    @Column(name = "R6_Y020")
	    private String r6Y020;
	    @Column(name = "R6_AMOUNT_X010")
	    private java.math.BigDecimal r6Amount;

	    @Column(name = "R7_PARTICULARS")
	    private String r7Particulars;
	    @Column(name = "R7_Y030")
	    private String r7Y030;
	    @Column(name = "R7_AMOUNT_X010")
	    private java.math.BigDecimal r7Amount;

	    @Column(name = "R8_PARTICULARS")
	    private String r8Particulars;
	    @Column(name = "R8_Y040")
	    private String r8Y040;
	    @Column(name = "R8_AMOUNT_X010")
	    private java.math.BigDecimal r8Amount;

	    @Column(name = "R9_PARTICULARS")
	    private String r9Particulars;
	    @Column(name = "R9_Y050")
	    private String r9Y050;
	    @Column(name = "R9_AMOUNT_X010")
	    private java.math.BigDecimal r9Amount;

	    @Column(name = "R10_PARTICULARS")
	    private String r10Particulars;
	    @Column(name = "R10_Y060")
	    private String r10Y060;
	    @Column(name = "R10_AMOUNT_X010")
	    private java.math.BigDecimal r10Amount;

	    @Column(name = "R11_PARTICULARS")
	    private String r11Particulars;
	    @Column(name = "R11_Y070")
	    private String r11Y070;
	    @Column(name = "R11_AMOUNT_X010")
	    private java.math.BigDecimal r11Amount;

	    @Column(name = "R12_PARTICULARS")
	    private String r12Particulars;
	    @Column(name = "R12_Y080")
	    private String r12Y080;
	    @Column(name = "R12_AMOUNT_X010")
	    private java.math.BigDecimal r12Amount;

	    @Column(name = "R13_PARTICULARS")
	    private String r13Particulars;
	    @Column(name = "R13_Y090")
	    private String r13Y090;
	    @Column(name = "R13_AMOUNT_X010")
	    private java.math.BigDecimal r13Amount;

	    @Column(name = "R14_PARTICULARS")
	    private String r14Particulars;
	    @Column(name = "R14_Y100")
	    private String r14Y100;
	    @Column(name = "R14_AMOUNT_X010")
	    private java.math.BigDecimal r14Amount;

	    @Column(name = "R15_PARTICULARS")
	    private String r15Particulars;
	    @Column(name = "R15_Y110")
	    private String r15Y110;
	    @Column(name = "R15_AMOUNT_X010")
	    private java.math.BigDecimal r15Amount;

	    @Column(name = "R16_PARTICULARS")
	    private String r16Particulars;
	    @Column(name = "R16_Y120")
	    private String r16Y120;
	    @Column(name = "R16_AMOUNT_X010")
	    private java.math.BigDecimal r16Amount;

	    @Column(name = "R17_PARTICULARS")
	    private String r17Particulars;
	    @Column(name = "R17_Y130")
	    private String r17Y130;
	    @Column(name = "R17_AMOUNT_X010")
	    private java.math.BigDecimal r17Amount;

	    @Column(name = "R18_PARTICULARS")
	    private String r18Particulars;
	    @Column(name = "R18_Y140")
	    private String r18Y140;
	    @Column(name = "R18_AMOUNT_X010")
	    private java.math.BigDecimal r18Amount;

	    @Column(name = "R19_PARTICULARS")
	    private String r19Particulars;
	    @Column(name = "R19_Y150")
	    private String r19Y150;
	    @Column(name = "R19_AMOUNT_X010")
	    private java.math.BigDecimal r19Amount;

	    @Column(name = "R20_PARTICULARS")
	    private String r20Particulars;
	    @Column(name = "R20_Y160")
	    private String r20Y160;
	    @Column(name = "R20_AMOUNT_X010")
	    private java.math.BigDecimal r20Amount;

	    @Column(name = "R21_PARTICULARS")
	    private String r21Particulars;
	    @Column(name = "R21_Y170")
	    private String r21Y170;
	    @Column(name = "R21_AMOUNT_X010")
	    private java.math.BigDecimal r21Amount;

	    @Column(name = "R22_PARTICULARS")
	    private String r22Particulars;
	    @Column(name = "R22_Y180")
	    private String r22Y180;
	    @Column(name = "R22_AMOUNT_X010")
	    private java.math.BigDecimal r22Amount;

	    @Column(name = "R23_PARTICULARS")
	    private String r23Particulars;
	    @Column(name = "R23_Y190")
	    private String r23Y190;
	    @Column(name = "R23_AMOUNT_X010")
	    private java.math.BigDecimal r23Amount;

	    @Column(name = "R24_PARTICULARS")
	    private String r24Particulars;
	    @Column(name = "R24_Y200")
	    private String r24Y200;
	    @Column(name = "R24_AMOUNT_X010")
	    private java.math.BigDecimal r24Amount;

	    @Column(name = "R25_PARTICULARS")
	    private String r25Particulars;
	    @Column(name = "R25_Y210")
	    private String r25Y210;
	    @Column(name = "R25_AMOUNT_X010")
	    private java.math.BigDecimal r25Amount;

	    @Column(name = "R26_PARTICULARS")
	    private String r26Particulars;
	    @Column(name = "R26_Y220")
	    private String r26Y220;
	    @Column(name = "R26_AMOUNT_X010")
	    private java.math.BigDecimal r26Amount;

	    @Column(name = "R27_PARTICULARS")
	    private String r27Particulars;
	    @Column(name = "R27_Y230")
	    private String r27Y230;
	    @Column(name = "R27_AMOUNT_X010")
	    private java.math.BigDecimal r27Amount;

	    @Column(name = "R28_PARTICULARS")
	    private String r28Particulars;
	    @Column(name = "R28_Y240")
	    private String r28Y240;
	    @Column(name = "R28_AMOUNT_X010")
	    private java.math.BigDecimal r28Amount;

	    @Column(name = "R29_PARTICULARS")
	    private String r29Particulars;
	    @Column(name = "R29_Y250")
	    private String r29Y250;
	    @Column(name = "R29_AMOUNT_X010")
	    private java.math.BigDecimal r29Amount;

	    @Column(name = "R30_PARTICULARS")
	    private String r30Particulars;
	    @Column(name = "R30_Y260")
	    private String r30Y260;
	    @Column(name = "R30_AMOUNT_X010")
	    private java.math.BigDecimal r30Amount;

	    @Column(name = "R31_PARTICULARS")
	    private String r31Particulars;
	    @Column(name = "R31_Y270")
	    private String r31Y270;
	    @Column(name = "R31_AMOUNT_X010")
	    private java.math.BigDecimal r31Amount;

	    @Column(name = "R32_PARTICULARS")
	    private String r32Particulars;
	    @Column(name = "R32_Y280")
	    private String r32Y280;
	    @Column(name = "R32_AMOUNT_X010")
	    private java.math.BigDecimal r32Amount;

	    @Column(name = "R33_PARTICULARS")
	    private String r33Particulars;
	    @Column(name = "R33_Y290")
	    private String r33Y290;
	    @Column(name = "R33_AMOUNT_X010")
	    private java.math.BigDecimal r33Amount;

	    @Column(name = "R34_PARTICULARS")
	    private String r34Particulars;
	    @Column(name = "R34_Y300")
	    private String r34Y300;
	    @Column(name = "R34_AMOUNT_X010")
	    private java.math.BigDecimal r34Amount;

	    @Column(name = "R35_PARTICULARS")
	    private String r35Particulars;
	    @Column(name = "R35_Y310")
	    private String r35Y310;
	    @Column(name = "R35_AMOUNT_X010")
	    private java.math.BigDecimal r35Amount;

	    @Column(name = "R36_PARTICULARS")
	    private String r36Particulars;
	    @Column(name = "R36_Y320")
	    private String r36Y320;
	    @Column(name = "R36_AMOUNT_X010")
	    private java.math.BigDecimal r36Amount;

	    @Column(name = "R37_PARTICULARS")
	    private String r37Particulars;
	    @Column(name = "R37_Y330")
	    private String r37Y330;
	    @Column(name = "R37_AMOUNT_X010")
	    private java.math.BigDecimal r37Amount;

	    @Column(name = "R38_PARTICULARS")
	    private String r38Particulars;
	    @Column(name = "R38_Y340")
	    private String r38Y340;
	    @Column(name = "R38_AMOUNT_X010")
	    private java.math.BigDecimal r38Amount;

	    @Column(name = "R39_PARTICULARS")
	    private String r39Particulars;
	    @Column(name = "R39_Y350")
	    private String r39Y350;
	    @Column(name = "R39_AMOUNT_X010")
	    private java.math.BigDecimal r39Amount;

	    @Column(name = "R40_PARTICULARS")
	    private String r40Particulars;
	    @Column(name = "R40_Y360")
	    private String r40Y360;
	    @Column(name = "R40_AMOUNT_X010")
	    private java.math.BigDecimal r40Amount;

	    @Column(name = "R41_PARTICULARS")
	    private String r41Particulars;
	    @Column(name = "R41_Y370")
	    private String r41Y370;
	    @Column(name = "R41_AMOUNT_X010")
	    private java.math.BigDecimal r41Amount;

	    @Column(name = "R42_PARTICULARS")
	    private String r42Particulars;
	    @Column(name = "R42_Y380")
	    private String r42Y380;
	    @Column(name = "R42_AMOUNT_X010")
	    private java.math.BigDecimal r42Amount;

	    @Column(name = "REPORT_DATE")
	    @Temporal(TemporalType.DATE)
	    private java.util.Date reportDate;

	    @Column(name = "REPORT_VERSION")
	    private java.math.BigDecimal reportVersion;

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

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getR5Particulars() {
			return r5Particulars;
		}

		public void setR5Particulars(String r5Particulars) {
			this.r5Particulars = r5Particulars;
		}

		public String getR5Y010() {
			return r5Y010;
		}

		public void setR5Y010(String r5y010) {
			r5Y010 = r5y010;
		}

		public java.math.BigDecimal getR5Amount() {
			return r5Amount;
		}

		public void setR5Amount(java.math.BigDecimal r5Amount) {
			this.r5Amount = r5Amount;
		}

		public String getR6Particulars() {
			return r6Particulars;
		}

		public void setR6Particulars(String r6Particulars) {
			this.r6Particulars = r6Particulars;
		}

		public String getR6Y020() {
			return r6Y020;
		}

		public void setR6Y020(String r6y020) {
			r6Y020 = r6y020;
		}

		public java.math.BigDecimal getR6Amount() {
			return r6Amount;
		}

		public void setR6Amount(java.math.BigDecimal r6Amount) {
			this.r6Amount = r6Amount;
		}

		public String getR7Particulars() {
			return r7Particulars;
		}

		public void setR7Particulars(String r7Particulars) {
			this.r7Particulars = r7Particulars;
		}

		public String getR7Y030() {
			return r7Y030;
		}

		public void setR7Y030(String r7y030) {
			r7Y030 = r7y030;
		}

		public java.math.BigDecimal getR7Amount() {
			return r7Amount;
		}

		public void setR7Amount(java.math.BigDecimal r7Amount) {
			this.r7Amount = r7Amount;
		}

		public String getR8Particulars() {
			return r8Particulars;
		}

		public void setR8Particulars(String r8Particulars) {
			this.r8Particulars = r8Particulars;
		}

		public String getR8Y040() {
			return r8Y040;
		}

		public void setR8Y040(String r8y040) {
			r8Y040 = r8y040;
		}

		public java.math.BigDecimal getR8Amount() {
			return r8Amount;
		}

		public void setR8Amount(java.math.BigDecimal r8Amount) {
			this.r8Amount = r8Amount;
		}

		public String getR9Particulars() {
			return r9Particulars;
		}

		public void setR9Particulars(String r9Particulars) {
			this.r9Particulars = r9Particulars;
		}

		public String getR9Y050() {
			return r9Y050;
		}

		public void setR9Y050(String r9y050) {
			r9Y050 = r9y050;
		}

		public java.math.BigDecimal getR9Amount() {
			return r9Amount;
		}

		public void setR9Amount(java.math.BigDecimal r9Amount) {
			this.r9Amount = r9Amount;
		}

		public String getR10Particulars() {
			return r10Particulars;
		}

		public void setR10Particulars(String r10Particulars) {
			this.r10Particulars = r10Particulars;
		}

		public String getR10Y060() {
			return r10Y060;
		}

		public void setR10Y060(String r10y060) {
			r10Y060 = r10y060;
		}

		public java.math.BigDecimal getR10Amount() {
			return r10Amount;
		}

		public void setR10Amount(java.math.BigDecimal r10Amount) {
			this.r10Amount = r10Amount;
		}

		public String getR11Particulars() {
			return r11Particulars;
		}

		public void setR11Particulars(String r11Particulars) {
			this.r11Particulars = r11Particulars;
		}

		public String getR11Y070() {
			return r11Y070;
		}

		public void setR11Y070(String r11y070) {
			r11Y070 = r11y070;
		}

		public java.math.BigDecimal getR11Amount() {
			return r11Amount;
		}

		public void setR11Amount(java.math.BigDecimal r11Amount) {
			this.r11Amount = r11Amount;
		}

		public String getR12Particulars() {
			return r12Particulars;
		}

		public void setR12Particulars(String r12Particulars) {
			this.r12Particulars = r12Particulars;
		}

		public String getR12Y080() {
			return r12Y080;
		}

		public void setR12Y080(String r12y080) {
			r12Y080 = r12y080;
		}

		public java.math.BigDecimal getR12Amount() {
			return r12Amount;
		}

		public void setR12Amount(java.math.BigDecimal r12Amount) {
			this.r12Amount = r12Amount;
		}

		public String getR13Particulars() {
			return r13Particulars;
		}

		public void setR13Particulars(String r13Particulars) {
			this.r13Particulars = r13Particulars;
		}

		public String getR13Y090() {
			return r13Y090;
		}

		public void setR13Y090(String r13y090) {
			r13Y090 = r13y090;
		}

		public java.math.BigDecimal getR13Amount() {
			return r13Amount;
		}

		public void setR13Amount(java.math.BigDecimal r13Amount) {
			this.r13Amount = r13Amount;
		}

		public String getR14Particulars() {
			return r14Particulars;
		}

		public void setR14Particulars(String r14Particulars) {
			this.r14Particulars = r14Particulars;
		}

		public String getR14Y100() {
			return r14Y100;
		}

		public void setR14Y100(String r14y100) {
			r14Y100 = r14y100;
		}

		public java.math.BigDecimal getR14Amount() {
			return r14Amount;
		}

		public void setR14Amount(java.math.BigDecimal r14Amount) {
			this.r14Amount = r14Amount;
		}

		public String getR15Particulars() {
			return r15Particulars;
		}

		public void setR15Particulars(String r15Particulars) {
			this.r15Particulars = r15Particulars;
		}

		public String getR15Y110() {
			return r15Y110;
		}

		public void setR15Y110(String r15y110) {
			r15Y110 = r15y110;
		}

		public java.math.BigDecimal getR15Amount() {
			return r15Amount;
		}

		public void setR15Amount(java.math.BigDecimal r15Amount) {
			this.r15Amount = r15Amount;
		}

		public String getR16Particulars() {
			return r16Particulars;
		}

		public void setR16Particulars(String r16Particulars) {
			this.r16Particulars = r16Particulars;
		}

		public String getR16Y120() {
			return r16Y120;
		}

		public void setR16Y120(String r16y120) {
			r16Y120 = r16y120;
		}

		public java.math.BigDecimal getR16Amount() {
			return r16Amount;
		}

		public void setR16Amount(java.math.BigDecimal r16Amount) {
			this.r16Amount = r16Amount;
		}

		public String getR17Particulars() {
			return r17Particulars;
		}

		public void setR17Particulars(String r17Particulars) {
			this.r17Particulars = r17Particulars;
		}

		public String getR17Y130() {
			return r17Y130;
		}

		public void setR17Y130(String r17y130) {
			r17Y130 = r17y130;
		}

		public java.math.BigDecimal getR17Amount() {
			return r17Amount;
		}

		public void setR17Amount(java.math.BigDecimal r17Amount) {
			this.r17Amount = r17Amount;
		}

		public String getR18Particulars() {
			return r18Particulars;
		}

		public void setR18Particulars(String r18Particulars) {
			this.r18Particulars = r18Particulars;
		}

		public String getR18Y140() {
			return r18Y140;
		}

		public void setR18Y140(String r18y140) {
			r18Y140 = r18y140;
		}

		public java.math.BigDecimal getR18Amount() {
			return r18Amount;
		}

		public void setR18Amount(java.math.BigDecimal r18Amount) {
			this.r18Amount = r18Amount;
		}

		public String getR19Particulars() {
			return r19Particulars;
		}

		public void setR19Particulars(String r19Particulars) {
			this.r19Particulars = r19Particulars;
		}

		public String getR19Y150() {
			return r19Y150;
		}

		public void setR19Y150(String r19y150) {
			r19Y150 = r19y150;
		}

		public java.math.BigDecimal getR19Amount() {
			return r19Amount;
		}

		public void setR19Amount(java.math.BigDecimal r19Amount) {
			this.r19Amount = r19Amount;
		}

		public String getR20Particulars() {
			return r20Particulars;
		}

		public void setR20Particulars(String r20Particulars) {
			this.r20Particulars = r20Particulars;
		}

		public String getR20Y160() {
			return r20Y160;
		}

		public void setR20Y160(String r20y160) {
			r20Y160 = r20y160;
		}

		public java.math.BigDecimal getR20Amount() {
			return r20Amount;
		}

		public void setR20Amount(java.math.BigDecimal r20Amount) {
			this.r20Amount = r20Amount;
		}

		public String getR21Particulars() {
			return r21Particulars;
		}

		public void setR21Particulars(String r21Particulars) {
			this.r21Particulars = r21Particulars;
		}

		public String getR21Y170() {
			return r21Y170;
		}

		public void setR21Y170(String r21y170) {
			r21Y170 = r21y170;
		}

		public java.math.BigDecimal getR21Amount() {
			return r21Amount;
		}

		public void setR21Amount(java.math.BigDecimal r21Amount) {
			this.r21Amount = r21Amount;
		}

		public String getR22Particulars() {
			return r22Particulars;
		}

		public void setR22Particulars(String r22Particulars) {
			this.r22Particulars = r22Particulars;
		}

		public String getR22Y180() {
			return r22Y180;
		}

		public void setR22Y180(String r22y180) {
			r22Y180 = r22y180;
		}

		public java.math.BigDecimal getR22Amount() {
			return r22Amount;
		}

		public void setR22Amount(java.math.BigDecimal r22Amount) {
			this.r22Amount = r22Amount;
		}

		public String getR23Particulars() {
			return r23Particulars;
		}

		public void setR23Particulars(String r23Particulars) {
			this.r23Particulars = r23Particulars;
		}

		public String getR23Y190() {
			return r23Y190;
		}

		public void setR23Y190(String r23y190) {
			r23Y190 = r23y190;
		}

		public java.math.BigDecimal getR23Amount() {
			return r23Amount;
		}

		public void setR23Amount(java.math.BigDecimal r23Amount) {
			this.r23Amount = r23Amount;
		}

		public String getR24Particulars() {
			return r24Particulars;
		}

		public void setR24Particulars(String r24Particulars) {
			this.r24Particulars = r24Particulars;
		}

		public String getR24Y200() {
			return r24Y200;
		}

		public void setR24Y200(String r24y200) {
			r24Y200 = r24y200;
		}

		public java.math.BigDecimal getR24Amount() {
			return r24Amount;
		}

		public void setR24Amount(java.math.BigDecimal r24Amount) {
			this.r24Amount = r24Amount;
		}

		public String getR25Particulars() {
			return r25Particulars;
		}

		public void setR25Particulars(String r25Particulars) {
			this.r25Particulars = r25Particulars;
		}

		public String getR25Y210() {
			return r25Y210;
		}

		public void setR25Y210(String r25y210) {
			r25Y210 = r25y210;
		}

		public java.math.BigDecimal getR25Amount() {
			return r25Amount;
		}

		public void setR25Amount(java.math.BigDecimal r25Amount) {
			this.r25Amount = r25Amount;
		}

		public String getR26Particulars() {
			return r26Particulars;
		}

		public void setR26Particulars(String r26Particulars) {
			this.r26Particulars = r26Particulars;
		}

		public String getR26Y220() {
			return r26Y220;
		}

		public void setR26Y220(String r26y220) {
			r26Y220 = r26y220;
		}

		public java.math.BigDecimal getR26Amount() {
			return r26Amount;
		}

		public void setR26Amount(java.math.BigDecimal r26Amount) {
			this.r26Amount = r26Amount;
		}

		public String getR27Particulars() {
			return r27Particulars;
		}

		public void setR27Particulars(String r27Particulars) {
			this.r27Particulars = r27Particulars;
		}

		public String getR27Y230() {
			return r27Y230;
		}

		public void setR27Y230(String r27y230) {
			r27Y230 = r27y230;
		}

		public java.math.BigDecimal getR27Amount() {
			return r27Amount;
		}

		public void setR27Amount(java.math.BigDecimal r27Amount) {
			this.r27Amount = r27Amount;
		}

		public String getR28Particulars() {
			return r28Particulars;
		}

		public void setR28Particulars(String r28Particulars) {
			this.r28Particulars = r28Particulars;
		}

		public String getR28Y240() {
			return r28Y240;
		}

		public void setR28Y240(String r28y240) {
			r28Y240 = r28y240;
		}

		public java.math.BigDecimal getR28Amount() {
			return r28Amount;
		}

		public void setR28Amount(java.math.BigDecimal r28Amount) {
			this.r28Amount = r28Amount;
		}

		public String getR29Particulars() {
			return r29Particulars;
		}

		public void setR29Particulars(String r29Particulars) {
			this.r29Particulars = r29Particulars;
		}

		public String getR29Y250() {
			return r29Y250;
		}

		public void setR29Y250(String r29y250) {
			r29Y250 = r29y250;
		}

		public java.math.BigDecimal getR29Amount() {
			return r29Amount;
		}

		public void setR29Amount(java.math.BigDecimal r29Amount) {
			this.r29Amount = r29Amount;
		}

		public String getR30Particulars() {
			return r30Particulars;
		}

		public void setR30Particulars(String r30Particulars) {
			this.r30Particulars = r30Particulars;
		}

		public String getR30Y260() {
			return r30Y260;
		}

		public void setR30Y260(String r30y260) {
			r30Y260 = r30y260;
		}

		public java.math.BigDecimal getR30Amount() {
			return r30Amount;
		}

		public void setR30Amount(java.math.BigDecimal r30Amount) {
			this.r30Amount = r30Amount;
		}

		public String getR31Particulars() {
			return r31Particulars;
		}

		public void setR31Particulars(String r31Particulars) {
			this.r31Particulars = r31Particulars;
		}

		public String getR31Y270() {
			return r31Y270;
		}

		public void setR31Y270(String r31y270) {
			r31Y270 = r31y270;
		}

		public java.math.BigDecimal getR31Amount() {
			return r31Amount;
		}

		public void setR31Amount(java.math.BigDecimal r31Amount) {
			this.r31Amount = r31Amount;
		}

		public String getR32Particulars() {
			return r32Particulars;
		}

		public void setR32Particulars(String r32Particulars) {
			this.r32Particulars = r32Particulars;
		}

		public String getR32Y280() {
			return r32Y280;
		}

		public void setR32Y280(String r32y280) {
			r32Y280 = r32y280;
		}

		public java.math.BigDecimal getR32Amount() {
			return r32Amount;
		}

		public void setR32Amount(java.math.BigDecimal r32Amount) {
			this.r32Amount = r32Amount;
		}

		public String getR33Particulars() {
			return r33Particulars;
		}

		public void setR33Particulars(String r33Particulars) {
			this.r33Particulars = r33Particulars;
		}

		public String getR33Y290() {
			return r33Y290;
		}

		public void setR33Y290(String r33y290) {
			r33Y290 = r33y290;
		}

		public java.math.BigDecimal getR33Amount() {
			return r33Amount;
		}

		public void setR33Amount(java.math.BigDecimal r33Amount) {
			this.r33Amount = r33Amount;
		}

		public String getR34Particulars() {
			return r34Particulars;
		}

		public void setR34Particulars(String r34Particulars) {
			this.r34Particulars = r34Particulars;
		}

		public String getR34Y300() {
			return r34Y300;
		}

		public void setR34Y300(String r34y300) {
			r34Y300 = r34y300;
		}

		public java.math.BigDecimal getR34Amount() {
			return r34Amount;
		}

		public void setR34Amount(java.math.BigDecimal r34Amount) {
			this.r34Amount = r34Amount;
		}

		public String getR35Particulars() {
			return r35Particulars;
		}

		public void setR35Particulars(String r35Particulars) {
			this.r35Particulars = r35Particulars;
		}

		public String getR35Y310() {
			return r35Y310;
		}

		public void setR35Y310(String r35y310) {
			r35Y310 = r35y310;
		}

		public java.math.BigDecimal getR35Amount() {
			return r35Amount;
		}

		public void setR35Amount(java.math.BigDecimal r35Amount) {
			this.r35Amount = r35Amount;
		}

		public String getR36Particulars() {
			return r36Particulars;
		}

		public void setR36Particulars(String r36Particulars) {
			this.r36Particulars = r36Particulars;
		}

		public String getR36Y320() {
			return r36Y320;
		}

		public void setR36Y320(String r36y320) {
			r36Y320 = r36y320;
		}

		public java.math.BigDecimal getR36Amount() {
			return r36Amount;
		}

		public void setR36Amount(java.math.BigDecimal r36Amount) {
			this.r36Amount = r36Amount;
		}

		public String getR37Particulars() {
			return r37Particulars;
		}

		public void setR37Particulars(String r37Particulars) {
			this.r37Particulars = r37Particulars;
		}

		public String getR37Y330() {
			return r37Y330;
		}

		public void setR37Y330(String r37y330) {
			r37Y330 = r37y330;
		}

		public java.math.BigDecimal getR37Amount() {
			return r37Amount;
		}

		public void setR37Amount(java.math.BigDecimal r37Amount) {
			this.r37Amount = r37Amount;
		}

		public String getR38Particulars() {
			return r38Particulars;
		}

		public void setR38Particulars(String r38Particulars) {
			this.r38Particulars = r38Particulars;
		}

		public String getR38Y340() {
			return r38Y340;
		}

		public void setR38Y340(String r38y340) {
			r38Y340 = r38y340;
		}

		public java.math.BigDecimal getR38Amount() {
			return r38Amount;
		}

		public void setR38Amount(java.math.BigDecimal r38Amount) {
			this.r38Amount = r38Amount;
		}

		public String getR39Particulars() {
			return r39Particulars;
		}

		public void setR39Particulars(String r39Particulars) {
			this.r39Particulars = r39Particulars;
		}

		public String getR39Y350() {
			return r39Y350;
		}

		public void setR39Y350(String r39y350) {
			r39Y350 = r39y350;
		}

		public java.math.BigDecimal getR39Amount() {
			return r39Amount;
		}

		public void setR39Amount(java.math.BigDecimal r39Amount) {
			this.r39Amount = r39Amount;
		}

		public String getR40Particulars() {
			return r40Particulars;
		}

		public void setR40Particulars(String r40Particulars) {
			this.r40Particulars = r40Particulars;
		}

		public String getR40Y360() {
			return r40Y360;
		}

		public void setR40Y360(String r40y360) {
			r40Y360 = r40y360;
		}

		public java.math.BigDecimal getR40Amount() {
			return r40Amount;
		}

		public void setR40Amount(java.math.BigDecimal r40Amount) {
			this.r40Amount = r40Amount;
		}

		public String getR41Particulars() {
			return r41Particulars;
		}

		public void setR41Particulars(String r41Particulars) {
			this.r41Particulars = r41Particulars;
		}

		public String getR41Y370() {
			return r41Y370;
		}

		public void setR41Y370(String r41y370) {
			r41Y370 = r41y370;
		}

		public java.math.BigDecimal getR41Amount() {
			return r41Amount;
		}

		public void setR41Amount(java.math.BigDecimal r41Amount) {
			this.r41Amount = r41Amount;
		}

		public String getR42Particulars() {
			return r42Particulars;
		}

		public void setR42Particulars(String r42Particulars) {
			this.r42Particulars = r42Particulars;
		}

		public String getR42Y380() {
			return r42Y380;
		}

		public void setR42Y380(String r42y380) {
			r42Y380 = r42y380;
		}

		public java.math.BigDecimal getR42Amount() {
			return r42Amount;
		}

		public void setR42Amount(java.math.BigDecimal r42Amount) {
			this.r42Amount = r42Amount;
		}

		public java.util.Date getReportDate() {
			return reportDate;
		}

		public void setReportDate(java.util.Date reportDate) {
			this.reportDate = reportDate;
		}

		public java.math.BigDecimal getReportVersion() {
			return reportVersion;
		}

		public void setReportVersion(java.math.BigDecimal reportVersion) {
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

	//Row mapper class 
	class Fincon_1A_RowMapper implements RowMapper<BRRS_BG_DBIS10_FINCON_III_1A_ST_Entity> {

	    @Override
	    public BRRS_BG_DBIS10_FINCON_III_1A_ST_Entity mapRow(ResultSet rs, int rowNum) throws SQLException {

	        BRRS_BG_DBIS10_FINCON_III_1A_ST_Entity obj = new BRRS_BG_DBIS10_FINCON_III_1A_ST_Entity();

	        // =========================
	        // R5
	        // =========================
	        obj.setR5Particulars(rs.getString("R5_PARTICULARS"));
	        obj.setR5Y010(rs.getString("R5_Y010"));
	        obj.setR5Amount(rs.getBigDecimal("R5_AMOUNT_X010"));

	        // =========================
	        // R6
	        // =========================
	        obj.setR6Particulars(rs.getString("R6_PARTICULARS"));
	        obj.setR6Y020(rs.getString("R6_Y020"));
	        obj.setR6Amount(rs.getBigDecimal("R6_AMOUNT_X010"));

	        // =========================
	        // R7
	        // =========================
	        obj.setR7Particulars(rs.getString("R7_PARTICULARS"));
	        obj.setR7Y030(rs.getString("R7_Y030"));
	        obj.setR7Amount(rs.getBigDecimal("R7_AMOUNT_X010"));

	        // =========================
	        // R8
	        // =========================
	        obj.setR8Particulars(rs.getString("R8_PARTICULARS"));
	        obj.setR8Y040(rs.getString("R8_Y040"));
	        obj.setR8Amount(rs.getBigDecimal("R8_AMOUNT_X010"));

	        // =========================
	        // R9
	        // =========================
	        obj.setR9Particulars(rs.getString("R9_PARTICULARS"));
	        obj.setR9Y050(rs.getString("R9_Y050"));
	        obj.setR9Amount(rs.getBigDecimal("R9_AMOUNT_X010"));

	        // =========================
	        // R10
	        // =========================
	        obj.setR10Particulars(rs.getString("R10_PARTICULARS"));
	        obj.setR10Y060(rs.getString("R10_Y060"));
	        obj.setR10Amount(rs.getBigDecimal("R10_AMOUNT_X010"));

	        // =========================
	        // R11
	        // =========================
	        obj.setR11Particulars(rs.getString("R11_PARTICULARS"));
	        obj.setR11Y070(rs.getString("R11_Y070"));
	        obj.setR11Amount(rs.getBigDecimal("R11_AMOUNT_X010"));

	        // =========================
	        // R12
	        // =========================
	        obj.setR12Particulars(rs.getString("R12_PARTICULARS"));
	        obj.setR12Y080(rs.getString("R12_Y080"));
	        obj.setR12Amount(rs.getBigDecimal("R12_AMOUNT_X010"));

	        // =========================
	        // R13
	        // =========================
	        obj.setR13Particulars(rs.getString("R13_PARTICULARS"));
	        obj.setR13Y090(rs.getString("R13_Y090"));
	        obj.setR13Amount(rs.getBigDecimal("R13_AMOUNT_X010"));

	        // =========================
	        // R14
	        // =========================
	        obj.setR14Particulars(rs.getString("R14_PARTICULARS"));
	        obj.setR14Y100(rs.getString("R14_Y100"));
	        obj.setR14Amount(rs.getBigDecimal("R14_AMOUNT_X010"));

	        // =========================
	        // R15
	        // =========================
	        obj.setR15Particulars(rs.getString("R15_PARTICULARS"));
	        obj.setR15Y110(rs.getString("R15_Y110"));
	        obj.setR15Amount(rs.getBigDecimal("R15_AMOUNT_X010"));

	        // =========================
	        // R16
	        // =========================
	        obj.setR16Particulars(rs.getString("R16_PARTICULARS"));
	        obj.setR16Y120(rs.getString("R16_Y120"));
	        obj.setR16Amount(rs.getBigDecimal("R16_AMOUNT_X010"));

	        // =========================
	        // R17
	        // =========================
	        obj.setR17Particulars(rs.getString("R17_PARTICULARS"));
	        obj.setR17Y130(rs.getString("R17_Y130"));
	        obj.setR17Amount(rs.getBigDecimal("R17_AMOUNT_X010"));

	        // =========================
	        // R18
	        // =========================
	        obj.setR18Particulars(rs.getString("R18_PARTICULARS"));
	        obj.setR18Y140(rs.getString("R18_Y140"));
	        obj.setR18Amount(rs.getBigDecimal("R18_AMOUNT_X010"));

	        // =========================
	        // R19
	        // =========================
	        obj.setR19Particulars(rs.getString("R19_PARTICULARS"));
	        obj.setR19Y150(rs.getString("R19_Y150"));
	        obj.setR19Amount(rs.getBigDecimal("R19_AMOUNT_X010"));

	        // =========================
	        // R20
	        // =========================
	        obj.setR20Particulars(rs.getString("R20_PARTICULARS"));
	        obj.setR20Y160(rs.getString("R20_Y160"));
	        obj.setR20Amount(rs.getBigDecimal("R20_AMOUNT_X010"));

	        // =========================
	        // R21
	        // =========================
	        obj.setR21Particulars(rs.getString("R21_PARTICULARS"));
	        obj.setR21Y170(rs.getString("R21_Y170"));
	        obj.setR21Amount(rs.getBigDecimal("R21_AMOUNT_X010"));

	        // =========================
	        // R22
	        // =========================
	        obj.setR22Particulars(rs.getString("R22_PARTICULARS"));
	        obj.setR22Y180(rs.getString("R22_Y180"));
	        obj.setR22Amount(rs.getBigDecimal("R22_AMOUNT_X010"));

	        // =========================
	        // R23
	        // =========================
	        obj.setR23Particulars(rs.getString("R23_PARTICULARS"));
	        obj.setR23Y190(rs.getString("R23_Y190"));
	        obj.setR23Amount(rs.getBigDecimal("R23_AMOUNT_X010"));

	        // =========================
	        // R24
	        // =========================
	        obj.setR24Particulars(rs.getString("R24_PARTICULARS"));
	        obj.setR24Y200(rs.getString("R24_Y200"));
	        obj.setR24Amount(rs.getBigDecimal("R24_AMOUNT_X010"));

	        // =========================
	        // R25
	        // =========================
	        obj.setR25Particulars(rs.getString("R25_PARTICULARS"));
	        obj.setR25Y210(rs.getString("R25_Y210"));
	        obj.setR25Amount(rs.getBigDecimal("R25_AMOUNT_X010"));

	        // =========================
	        // R26
	        // =========================
	        obj.setR26Particulars(rs.getString("R26_PARTICULARS"));
	        obj.setR26Y220(rs.getString("R26_Y220"));
	        obj.setR26Amount(rs.getBigDecimal("R26_AMOUNT_X010"));

	        // =========================
	        // R27
	        // =========================
	        obj.setR27Particulars(rs.getString("R27_PARTICULARS"));
	        obj.setR27Y230(rs.getString("R27_Y230"));
	        obj.setR27Amount(rs.getBigDecimal("R27_AMOUNT_X010"));

	        // =========================
	        // R28
	        // =========================
	        obj.setR28Particulars(rs.getString("R28_PARTICULARS"));
	        obj.setR28Y240(rs.getString("R28_Y240"));
	        obj.setR28Amount(rs.getBigDecimal("R28_AMOUNT_X010"));

	        // =========================
	        // R29
	        // =========================
	        obj.setR29Particulars(rs.getString("R29_PARTICULARS"));
	        obj.setR29Y250(rs.getString("R29_Y250"));
	        obj.setR29Amount(rs.getBigDecimal("R29_AMOUNT_X010"));

	        // =========================
	        // R30
	        // =========================
	        obj.setR30Particulars(rs.getString("R30_PARTICULARS"));
	        obj.setR30Y260(rs.getString("R30_Y260"));
	        obj.setR30Amount(rs.getBigDecimal("R30_AMOUNT_X010"));

	        // =========================
	        // R31
	        // =========================
	        obj.setR31Particulars(rs.getString("R31_PARTICULARS"));
	        obj.setR31Y270(rs.getString("R31_Y270"));
	        obj.setR31Amount(rs.getBigDecimal("R31_AMOUNT_X010"));

	        // =========================
	        // R32
	        // =========================
	        obj.setR32Particulars(rs.getString("R32_PARTICULARS"));
	        obj.setR32Y280(rs.getString("R32_Y280"));
	        obj.setR32Amount(rs.getBigDecimal("R32_AMOUNT_X010"));

	        // =========================
	        // R33
	        // =========================
	        obj.setR33Particulars(rs.getString("R33_PARTICULARS"));
	        obj.setR33Y290(rs.getString("R33_Y290"));
	        obj.setR33Amount(rs.getBigDecimal("R33_AMOUNT_X010"));

	        // =========================
	        // R34
	        // =========================
	        obj.setR34Particulars(rs.getString("R34_PARTICULARS"));
	        obj.setR34Y300(rs.getString("R34_Y300"));
	        obj.setR34Amount(rs.getBigDecimal("R34_AMOUNT_X010"));

	        // =========================
	        // R35
	        // =========================
	        obj.setR35Particulars(rs.getString("R35_PARTICULARS"));
	        obj.setR35Y310(rs.getString("R35_Y310"));
	        obj.setR35Amount(rs.getBigDecimal("R35_AMOUNT_X010"));

	        // =========================
	        // R36
	        // =========================
	        obj.setR36Particulars(rs.getString("R36_PARTICULARS"));
	        obj.setR36Y320(rs.getString("R36_Y320"));
	        obj.setR36Amount(rs.getBigDecimal("R36_AMOUNT_X010"));

	        // =========================
	        // R37
	        // =========================
	        obj.setR37Particulars(rs.getString("R37_PARTICULARS"));
	        obj.setR37Y330(rs.getString("R37_Y330"));
	        obj.setR37Amount(rs.getBigDecimal("R37_AMOUNT_X010"));

	        // =========================
	        // R38
	        // =========================
	        obj.setR38Particulars(rs.getString("R38_PARTICULARS"));
	        obj.setR38Y340(rs.getString("R38_Y340"));
	        obj.setR38Amount(rs.getBigDecimal("R38_AMOUNT_X010"));

	        // =========================
	        // R39
	        // =========================
	        obj.setR39Particulars(rs.getString("R39_PARTICULARS"));
	        obj.setR39Y350(rs.getString("R39_Y350"));
	        obj.setR39Amount(rs.getBigDecimal("R39_AMOUNT_X010"));

	        // =========================
	        // R40
	        // =========================
	        obj.setR40Particulars(rs.getString("R40_PARTICULARS"));
	        obj.setR40Y360(rs.getString("R40_Y360"));
	        obj.setR40Amount(rs.getBigDecimal("R40_AMOUNT_X010"));

	        // =========================
	        // R41
	        // =========================
	        obj.setR41Particulars(rs.getString("R41_PARTICULARS"));
	        obj.setR41Y370(rs.getString("R41_Y370"));
	        obj.setR41Amount(rs.getBigDecimal("R41_AMOUNT_X010"));

	        // =========================
	        // R42
	        // =========================
	        obj.setR42Particulars(rs.getString("R42_PARTICULARS"));
	        obj.setR42Y380(rs.getString("R42_Y380"));
	        obj.setR42Amount(rs.getBigDecimal("R42_AMOUNT_X010"));

	        // =========================
	        // COMMON FIELDS
	        // =========================
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

	
	
	//Row mapper class 
	class Fincon_1A_Archival_RowMapper implements RowMapper<BRRS_BG_DBIS10_FINCON_III_1A_Archival_ST_Entity> {

	    @Override
	    public BRRS_BG_DBIS10_FINCON_III_1A_Archival_ST_Entity mapRow(ResultSet rs, int rowNum) throws SQLException {

	    	BRRS_BG_DBIS10_FINCON_III_1A_Archival_ST_Entity obj = new BRRS_BG_DBIS10_FINCON_III_1A_Archival_ST_Entity();

	        // =========================
	        // R5
	        // =========================
	        obj.setR5Particulars(rs.getString("R5_PARTICULARS"));
	        obj.setR5Y010(rs.getString("R5_Y010"));
	        obj.setR5Amount(rs.getBigDecimal("R5_AMOUNT_X010"));

	        // =========================
	        // R6
	        // =========================
	        obj.setR6Particulars(rs.getString("R6_PARTICULARS"));
	        obj.setR6Y020(rs.getString("R6_Y020"));
	        obj.setR6Amount(rs.getBigDecimal("R6_AMOUNT_X010"));

	        // =========================
	        // R7
	        // =========================
	        obj.setR7Particulars(rs.getString("R7_PARTICULARS"));
	        obj.setR7Y030(rs.getString("R7_Y030"));
	        obj.setR7Amount(rs.getBigDecimal("R7_AMOUNT_X010"));

	        // =========================
	        // R8
	        // =========================
	        obj.setR8Particulars(rs.getString("R8_PARTICULARS"));
	        obj.setR8Y040(rs.getString("R8_Y040"));
	        obj.setR8Amount(rs.getBigDecimal("R8_AMOUNT_X010"));

	        // =========================
	        // R9
	        // =========================
	        obj.setR9Particulars(rs.getString("R9_PARTICULARS"));
	        obj.setR9Y050(rs.getString("R9_Y050"));
	        obj.setR9Amount(rs.getBigDecimal("R9_AMOUNT_X010"));

	        // =========================
	        // R10
	        // =========================
	        obj.setR10Particulars(rs.getString("R10_PARTICULARS"));
	        obj.setR10Y060(rs.getString("R10_Y060"));
	        obj.setR10Amount(rs.getBigDecimal("R10_AMOUNT_X010"));

	        // =========================
	        // R11
	        // =========================
	        obj.setR11Particulars(rs.getString("R11_PARTICULARS"));
	        obj.setR11Y070(rs.getString("R11_Y070"));
	        obj.setR11Amount(rs.getBigDecimal("R11_AMOUNT_X010"));

	        // =========================
	        // R12
	        // =========================
	        obj.setR12Particulars(rs.getString("R12_PARTICULARS"));
	        obj.setR12Y080(rs.getString("R12_Y080"));
	        obj.setR12Amount(rs.getBigDecimal("R12_AMOUNT_X010"));

	        // =========================
	        // R13
	        // =========================
	        obj.setR13Particulars(rs.getString("R13_PARTICULARS"));
	        obj.setR13Y090(rs.getString("R13_Y090"));
	        obj.setR13Amount(rs.getBigDecimal("R13_AMOUNT_X010"));

	        // =========================
	        // R14
	        // =========================
	        obj.setR14Particulars(rs.getString("R14_PARTICULARS"));
	        obj.setR14Y100(rs.getString("R14_Y100"));
	        obj.setR14Amount(rs.getBigDecimal("R14_AMOUNT_X010"));

	        // =========================
	        // R15
	        // =========================
	        obj.setR15Particulars(rs.getString("R15_PARTICULARS"));
	        obj.setR15Y110(rs.getString("R15_Y110"));
	        obj.setR15Amount(rs.getBigDecimal("R15_AMOUNT_X010"));

	        // =========================
	        // R16
	        // =========================
	        obj.setR16Particulars(rs.getString("R16_PARTICULARS"));
	        obj.setR16Y120(rs.getString("R16_Y120"));
	        obj.setR16Amount(rs.getBigDecimal("R16_AMOUNT_X010"));

	        // =========================
	        // R17
	        // =========================
	        obj.setR17Particulars(rs.getString("R17_PARTICULARS"));
	        obj.setR17Y130(rs.getString("R17_Y130"));
	        obj.setR17Amount(rs.getBigDecimal("R17_AMOUNT_X010"));

	        // =========================
	        // R18
	        // =========================
	        obj.setR18Particulars(rs.getString("R18_PARTICULARS"));
	        obj.setR18Y140(rs.getString("R18_Y140"));
	        obj.setR18Amount(rs.getBigDecimal("R18_AMOUNT_X010"));

	        // =========================
	        // R19
	        // =========================
	        obj.setR19Particulars(rs.getString("R19_PARTICULARS"));
	        obj.setR19Y150(rs.getString("R19_Y150"));
	        obj.setR19Amount(rs.getBigDecimal("R19_AMOUNT_X010"));

	        // =========================
	        // R20
	        // =========================
	        obj.setR20Particulars(rs.getString("R20_PARTICULARS"));
	        obj.setR20Y160(rs.getString("R20_Y160"));
	        obj.setR20Amount(rs.getBigDecimal("R20_AMOUNT_X010"));

	        // =========================
	        // R21
	        // =========================
	        obj.setR21Particulars(rs.getString("R21_PARTICULARS"));
	        obj.setR21Y170(rs.getString("R21_Y170"));
	        obj.setR21Amount(rs.getBigDecimal("R21_AMOUNT_X010"));

	        // =========================
	        // R22
	        // =========================
	        obj.setR22Particulars(rs.getString("R22_PARTICULARS"));
	        obj.setR22Y180(rs.getString("R22_Y180"));
	        obj.setR22Amount(rs.getBigDecimal("R22_AMOUNT_X010"));

	        // =========================
	        // R23
	        // =========================
	        obj.setR23Particulars(rs.getString("R23_PARTICULARS"));
	        obj.setR23Y190(rs.getString("R23_Y190"));
	        obj.setR23Amount(rs.getBigDecimal("R23_AMOUNT_X010"));

	        // =========================
	        // R24
	        // =========================
	        obj.setR24Particulars(rs.getString("R24_PARTICULARS"));
	        obj.setR24Y200(rs.getString("R24_Y200"));
	        obj.setR24Amount(rs.getBigDecimal("R24_AMOUNT_X010"));

	        // =========================
	        // R25
	        // =========================
	        obj.setR25Particulars(rs.getString("R25_PARTICULARS"));
	        obj.setR25Y210(rs.getString("R25_Y210"));
	        obj.setR25Amount(rs.getBigDecimal("R25_AMOUNT_X010"));

	        // =========================
	        // R26
	        // =========================
	        obj.setR26Particulars(rs.getString("R26_PARTICULARS"));
	        obj.setR26Y220(rs.getString("R26_Y220"));
	        obj.setR26Amount(rs.getBigDecimal("R26_AMOUNT_X010"));

	        // =========================
	        // R27
	        // =========================
	        obj.setR27Particulars(rs.getString("R27_PARTICULARS"));
	        obj.setR27Y230(rs.getString("R27_Y230"));
	        obj.setR27Amount(rs.getBigDecimal("R27_AMOUNT_X010"));

	        // =========================
	        // R28
	        // =========================
	        obj.setR28Particulars(rs.getString("R28_PARTICULARS"));
	        obj.setR28Y240(rs.getString("R28_Y240"));
	        obj.setR28Amount(rs.getBigDecimal("R28_AMOUNT_X010"));

	        // =========================
	        // R29
	        // =========================
	        obj.setR29Particulars(rs.getString("R29_PARTICULARS"));
	        obj.setR29Y250(rs.getString("R29_Y250"));
	        obj.setR29Amount(rs.getBigDecimal("R29_AMOUNT_X010"));

	        // =========================
	        // R30
	        // =========================
	        obj.setR30Particulars(rs.getString("R30_PARTICULARS"));
	        obj.setR30Y260(rs.getString("R30_Y260"));
	        obj.setR30Amount(rs.getBigDecimal("R30_AMOUNT_X010"));

	        // =========================
	        // R31
	        // =========================
	        obj.setR31Particulars(rs.getString("R31_PARTICULARS"));
	        obj.setR31Y270(rs.getString("R31_Y270"));
	        obj.setR31Amount(rs.getBigDecimal("R31_AMOUNT_X010"));

	        // =========================
	        // R32
	        // =========================
	        obj.setR32Particulars(rs.getString("R32_PARTICULARS"));
	        obj.setR32Y280(rs.getString("R32_Y280"));
	        obj.setR32Amount(rs.getBigDecimal("R32_AMOUNT_X010"));

	        // =========================
	        // R33
	        // =========================
	        obj.setR33Particulars(rs.getString("R33_PARTICULARS"));
	        obj.setR33Y290(rs.getString("R33_Y290"));
	        obj.setR33Amount(rs.getBigDecimal("R33_AMOUNT_X010"));

	        // =========================
	        // R34
	        // =========================
	        obj.setR34Particulars(rs.getString("R34_PARTICULARS"));
	        obj.setR34Y300(rs.getString("R34_Y300"));
	        obj.setR34Amount(rs.getBigDecimal("R34_AMOUNT_X010"));

	        // =========================
	        // R35
	        // =========================
	        obj.setR35Particulars(rs.getString("R35_PARTICULARS"));
	        obj.setR35Y310(rs.getString("R35_Y310"));
	        obj.setR35Amount(rs.getBigDecimal("R35_AMOUNT_X010"));

	        // =========================
	        // R36
	        // =========================
	        obj.setR36Particulars(rs.getString("R36_PARTICULARS"));
	        obj.setR36Y320(rs.getString("R36_Y320"));
	        obj.setR36Amount(rs.getBigDecimal("R36_AMOUNT_X010"));

	        // =========================
	        // R37
	        // =========================
	        obj.setR37Particulars(rs.getString("R37_PARTICULARS"));
	        obj.setR37Y330(rs.getString("R37_Y330"));
	        obj.setR37Amount(rs.getBigDecimal("R37_AMOUNT_X010"));

	        // =========================
	        // R38
	        // =========================
	        obj.setR38Particulars(rs.getString("R38_PARTICULARS"));
	        obj.setR38Y340(rs.getString("R38_Y340"));
	        obj.setR38Amount(rs.getBigDecimal("R38_AMOUNT_X010"));

	        // =========================
	        // R39
	        // =========================
	        obj.setR39Particulars(rs.getString("R39_PARTICULARS"));
	        obj.setR39Y350(rs.getString("R39_Y350"));
	        obj.setR39Amount(rs.getBigDecimal("R39_AMOUNT_X010"));

	        // =========================
	        // R40
	        // =========================
	        obj.setR40Particulars(rs.getString("R40_PARTICULARS"));
	        obj.setR40Y360(rs.getString("R40_Y360"));
	        obj.setR40Amount(rs.getBigDecimal("R40_AMOUNT_X010"));

	        // =========================
	        // R41
	        // =========================
	        obj.setR41Particulars(rs.getString("R41_PARTICULARS"));
	        obj.setR41Y370(rs.getString("R41_Y370"));
	        obj.setR41Amount(rs.getBigDecimal("R41_AMOUNT_X010"));

	        // =========================
	        // R42
	        // =========================
	        obj.setR42Particulars(rs.getString("R42_PARTICULARS"));
	        obj.setR42Y380(rs.getString("R42_Y380"));
	        obj.setR42Amount(rs.getBigDecimal("R42_AMOUNT_X010"));

	        // =========================
	        // COMMON FIELDS
	        // =========================
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

	
	//==============================
	// COMPOSITE KEY CLASS INSIDE SERVICE
	// ==============================
	public static class BG_FINCON_1A_PK implements Serializable {

	    private Date report_date;
	    private BigDecimal report_version;

	    public BG_FINCON_1A_PK() {}

	    public BG_FINCON_1A_PK(Date report_date, BigDecimal report_version) {
	        this.report_date = report_date;
	        this.report_version = report_version;
	    }

	    @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (!(o instanceof BG_FINCON_1A_PK)) return false;
	        BG_FINCON_1A_PK that = (BG_FINCON_1A_PK) o;
	        return Objects.equals(report_date, that.report_date) &&
	               Objects.equals(report_version, that.report_version);
	    }

	    @Override
	    public int hashCode() {
	        return Objects.hash(report_date, report_version);
	    }

	    public Date getReport_date() { return report_date; }
	    public void setReport_date(Date report_date) { this.report_date = report_date; }

	    public BigDecimal getReport_version() { return report_version; }
	    public void setReport_version(BigDecimal report_version) { this.report_version = report_version; }
	}
	

	@IdClass(BG_FINCON_1A_PK.class)
	//Class for Archival Summary table 
	public static class BRRS_BG_DBIS10_FINCON_III_1A_Archival_ST_Entity {

		    @Id
		    @GeneratedValue(strategy = GenerationType.IDENTITY)
		    private Long id;

		    @Column(name = "R5_PARTICULARS")
		    private String r5Particulars;
		    @Column(name = "R5_Y010")
		    private String r5Y010;
		    @Column(name = "R5_AMOUNT_X010")
		    private java.math.BigDecimal r5Amount;

		    @Column(name = "R6_PARTICULARS")
		    private String r6Particulars;
		    @Column(name = "R6_Y020")
		    private String r6Y020;
		    @Column(name = "R6_AMOUNT_X010")
		    private java.math.BigDecimal r6Amount;

		    @Column(name = "R7_PARTICULARS")
		    private String r7Particulars;
		    @Column(name = "R7_Y030")
		    private String r7Y030;
		    @Column(name = "R7_AMOUNT_X010")
		    private java.math.BigDecimal r7Amount;

		    @Column(name = "R8_PARTICULARS")
		    private String r8Particulars;
		    @Column(name = "R8_Y040")
		    private String r8Y040;
		    @Column(name = "R8_AMOUNT_X010")
		    private java.math.BigDecimal r8Amount;

		    @Column(name = "R9_PARTICULARS")
		    private String r9Particulars;
		    @Column(name = "R9_Y050")
		    private String r9Y050;
		    @Column(name = "R9_AMOUNT_X010")
		    private java.math.BigDecimal r9Amount;

		    @Column(name = "R10_PARTICULARS")
		    private String r10Particulars;
		    @Column(name = "R10_Y060")
		    private String r10Y060;
		    @Column(name = "R10_AMOUNT_X010")
		    private java.math.BigDecimal r10Amount;

		    @Column(name = "R11_PARTICULARS")
		    private String r11Particulars;
		    @Column(name = "R11_Y070")
		    private String r11Y070;
		    @Column(name = "R11_AMOUNT_X010")
		    private java.math.BigDecimal r11Amount;

		    @Column(name = "R12_PARTICULARS")
		    private String r12Particulars;
		    @Column(name = "R12_Y080")
		    private String r12Y080;
		    @Column(name = "R12_AMOUNT_X010")
		    private java.math.BigDecimal r12Amount;

		    @Column(name = "R13_PARTICULARS")
		    private String r13Particulars;
		    @Column(name = "R13_Y090")
		    private String r13Y090;
		    @Column(name = "R13_AMOUNT_X010")
		    private java.math.BigDecimal r13Amount;

		    @Column(name = "R14_PARTICULARS")
		    private String r14Particulars;
		    @Column(name = "R14_Y100")
		    private String r14Y100;
		    @Column(name = "R14_AMOUNT_X010")
		    private java.math.BigDecimal r14Amount;

		    @Column(name = "R15_PARTICULARS")
		    private String r15Particulars;
		    @Column(name = "R15_Y110")
		    private String r15Y110;
		    @Column(name = "R15_AMOUNT_X010")
		    private java.math.BigDecimal r15Amount;

		    @Column(name = "R16_PARTICULARS")
		    private String r16Particulars;
		    @Column(name = "R16_Y120")
		    private String r16Y120;
		    @Column(name = "R16_AMOUNT_X010")
		    private java.math.BigDecimal r16Amount;

		    @Column(name = "R17_PARTICULARS")
		    private String r17Particulars;
		    @Column(name = "R17_Y130")
		    private String r17Y130;
		    @Column(name = "R17_AMOUNT_X010")
		    private java.math.BigDecimal r17Amount;

		    @Column(name = "R18_PARTICULARS")
		    private String r18Particulars;
		    @Column(name = "R18_Y140")
		    private String r18Y140;
		    @Column(name = "R18_AMOUNT_X010")
		    private java.math.BigDecimal r18Amount;

		    @Column(name = "R19_PARTICULARS")
		    private String r19Particulars;
		    @Column(name = "R19_Y150")
		    private String r19Y150;
		    @Column(name = "R19_AMOUNT_X010")
		    private java.math.BigDecimal r19Amount;

		    @Column(name = "R20_PARTICULARS")
		    private String r20Particulars;
		    @Column(name = "R20_Y160")
		    private String r20Y160;
		    @Column(name = "R20_AMOUNT_X010")
		    private java.math.BigDecimal r20Amount;

		    @Column(name = "R21_PARTICULARS")
		    private String r21Particulars;
		    @Column(name = "R21_Y170")
		    private String r21Y170;
		    @Column(name = "R21_AMOUNT_X010")
		    private java.math.BigDecimal r21Amount;

		    @Column(name = "R22_PARTICULARS")
		    private String r22Particulars;
		    @Column(name = "R22_Y180")
		    private String r22Y180;
		    @Column(name = "R22_AMOUNT_X010")
		    private java.math.BigDecimal r22Amount;

		    @Column(name = "R23_PARTICULARS")
		    private String r23Particulars;
		    @Column(name = "R23_Y190")
		    private String r23Y190;
		    @Column(name = "R23_AMOUNT_X010")
		    private java.math.BigDecimal r23Amount;

		    @Column(name = "R24_PARTICULARS")
		    private String r24Particulars;
		    @Column(name = "R24_Y200")
		    private String r24Y200;
		    @Column(name = "R24_AMOUNT_X010")
		    private java.math.BigDecimal r24Amount;

		    @Column(name = "R25_PARTICULARS")
		    private String r25Particulars;
		    @Column(name = "R25_Y210")
		    private String r25Y210;
		    @Column(name = "R25_AMOUNT_X010")
		    private java.math.BigDecimal r25Amount;

		    @Column(name = "R26_PARTICULARS")
		    private String r26Particulars;
		    @Column(name = "R26_Y220")
		    private String r26Y220;
		    @Column(name = "R26_AMOUNT_X010")
		    private java.math.BigDecimal r26Amount;

		    @Column(name = "R27_PARTICULARS")
		    private String r27Particulars;
		    @Column(name = "R27_Y230")
		    private String r27Y230;
		    @Column(name = "R27_AMOUNT_X010")
		    private java.math.BigDecimal r27Amount;

		    @Column(name = "R28_PARTICULARS")
		    private String r28Particulars;
		    @Column(name = "R28_Y240")
		    private String r28Y240;
		    @Column(name = "R28_AMOUNT_X010")
		    private java.math.BigDecimal r28Amount;

		    @Column(name = "R29_PARTICULARS")
		    private String r29Particulars;
		    @Column(name = "R29_Y250")
		    private String r29Y250;
		    @Column(name = "R29_AMOUNT_X010")
		    private java.math.BigDecimal r29Amount;

		    @Column(name = "R30_PARTICULARS")
		    private String r30Particulars;
		    @Column(name = "R30_Y260")
		    private String r30Y260;
		    @Column(name = "R30_AMOUNT_X010")
		    private java.math.BigDecimal r30Amount;

		    @Column(name = "R31_PARTICULARS")
		    private String r31Particulars;
		    @Column(name = "R31_Y270")
		    private String r31Y270;
		    @Column(name = "R31_AMOUNT_X010")
		    private java.math.BigDecimal r31Amount;

		    @Column(name = "R32_PARTICULARS")
		    private String r32Particulars;
		    @Column(name = "R32_Y280")
		    private String r32Y280;
		    @Column(name = "R32_AMOUNT_X010")
		    private java.math.BigDecimal r32Amount;

		    @Column(name = "R33_PARTICULARS")
		    private String r33Particulars;
		    @Column(name = "R33_Y290")
		    private String r33Y290;
		    @Column(name = "R33_AMOUNT_X010")
		    private java.math.BigDecimal r33Amount;

		    @Column(name = "R34_PARTICULARS")
		    private String r34Particulars;
		    @Column(name = "R34_Y300")
		    private String r34Y300;
		    @Column(name = "R34_AMOUNT_X010")
		    private java.math.BigDecimal r34Amount;

		    @Column(name = "R35_PARTICULARS")
		    private String r35Particulars;
		    @Column(name = "R35_Y310")
		    private String r35Y310;
		    @Column(name = "R35_AMOUNT_X010")
		    private java.math.BigDecimal r35Amount;

		    @Column(name = "R36_PARTICULARS")
		    private String r36Particulars;
		    @Column(name = "R36_Y320")
		    private String r36Y320;
		    @Column(name = "R36_AMOUNT_X010")
		    private java.math.BigDecimal r36Amount;

		    @Column(name = "R37_PARTICULARS")
		    private String r37Particulars;
		    @Column(name = "R37_Y330")
		    private String r37Y330;
		    @Column(name = "R37_AMOUNT_X010")
		    private java.math.BigDecimal r37Amount;

		    @Column(name = "R38_PARTICULARS")
		    private String r38Particulars;
		    @Column(name = "R38_Y340")
		    private String r38Y340;
		    @Column(name = "R38_AMOUNT_X010")
		    private java.math.BigDecimal r38Amount;

		    @Column(name = "R39_PARTICULARS")
		    private String r39Particulars;
		    @Column(name = "R39_Y350")
		    private String r39Y350;
		    @Column(name = "R39_AMOUNT_X010")
		    private java.math.BigDecimal r39Amount;

		    @Column(name = "R40_PARTICULARS")
		    private String r40Particulars;
		    @Column(name = "R40_Y360")
		    private String r40Y360;
		    @Column(name = "R40_AMOUNT_X010")
		    private java.math.BigDecimal r40Amount;

		    @Column(name = "R41_PARTICULARS")
		    private String r41Particulars;
		    @Column(name = "R41_Y370")
		    private String r41Y370;
		    @Column(name = "R41_AMOUNT_X010")
		    private java.math.BigDecimal r41Amount;

		    @Column(name = "R42_PARTICULARS")
		    private String r42Particulars;
		    @Column(name = "R42_Y380")
		    private String r42Y380;
		    @Column(name = "R42_AMOUNT_X010")
		    private java.math.BigDecimal r42Amount;

		    @Column(name = "REPORT_DATE")
		    @Temporal(TemporalType.DATE)
		    private java.util.Date reportDate;

		    @Column(name = "REPORT_VERSION")
		    private java.math.BigDecimal reportVersion;

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
		    private java.util.Date REPORT_RESUBDATE;
		    
		    
			public java.util.Date getREPORT_RESUBDATE() {
				return REPORT_RESUBDATE;
			}

			public void setREPORT_RESUBDATE(java.util.Date rEPORT_RESUBDATE) {
				REPORT_RESUBDATE = rEPORT_RESUBDATE;
			}

			public Long getId() {
				return id;
			}

			public void setId(Long id) {
				this.id = id;
			}

			public String getR5Particulars() {
				return r5Particulars;
			}

			public void setR5Particulars(String r5Particulars) {
				this.r5Particulars = r5Particulars;
			}

			public String getR5Y010() {
				return r5Y010;
			}

			public void setR5Y010(String r5y010) {
				r5Y010 = r5y010;
			}

			public java.math.BigDecimal getR5Amount() {
				return r5Amount;
			}

			public void setR5Amount(java.math.BigDecimal r5Amount) {
				this.r5Amount = r5Amount;
			}

			public String getR6Particulars() {
				return r6Particulars;
			}

			public void setR6Particulars(String r6Particulars) {
				this.r6Particulars = r6Particulars;
			}

			public String getR6Y020() {
				return r6Y020;
			}

			public void setR6Y020(String r6y020) {
				r6Y020 = r6y020;
			}

			public java.math.BigDecimal getR6Amount() {
				return r6Amount;
			}

			public void setR6Amount(java.math.BigDecimal r6Amount) {
				this.r6Amount = r6Amount;
			}

			public String getR7Particulars() {
				return r7Particulars;
			}

			public void setR7Particulars(String r7Particulars) {
				this.r7Particulars = r7Particulars;
			}

			public String getR7Y030() {
				return r7Y030;
			}

			public void setR7Y030(String r7y030) {
				r7Y030 = r7y030;
			}

			public java.math.BigDecimal getR7Amount() {
				return r7Amount;
			}

			public void setR7Amount(java.math.BigDecimal r7Amount) {
				this.r7Amount = r7Amount;
			}

			public String getR8Particulars() {
				return r8Particulars;
			}

			public void setR8Particulars(String r8Particulars) {
				this.r8Particulars = r8Particulars;
			}

			public String getR8Y040() {
				return r8Y040;
			}

			public void setR8Y040(String r8y040) {
				r8Y040 = r8y040;
			}

			public java.math.BigDecimal getR8Amount() {
				return r8Amount;
			}

			public void setR8Amount(java.math.BigDecimal r8Amount) {
				this.r8Amount = r8Amount;
			}

			public String getR9Particulars() {
				return r9Particulars;
			}

			public void setR9Particulars(String r9Particulars) {
				this.r9Particulars = r9Particulars;
			}

			public String getR9Y050() {
				return r9Y050;
			}

			public void setR9Y050(String r9y050) {
				r9Y050 = r9y050;
			}

			public java.math.BigDecimal getR9Amount() {
				return r9Amount;
			}

			public void setR9Amount(java.math.BigDecimal r9Amount) {
				this.r9Amount = r9Amount;
			}

			public String getR10Particulars() {
				return r10Particulars;
			}

			public void setR10Particulars(String r10Particulars) {
				this.r10Particulars = r10Particulars;
			}

			public String getR10Y060() {
				return r10Y060;
			}

			public void setR10Y060(String r10y060) {
				r10Y060 = r10y060;
			}

			public java.math.BigDecimal getR10Amount() {
				return r10Amount;
			}

			public void setR10Amount(java.math.BigDecimal r10Amount) {
				this.r10Amount = r10Amount;
			}

			public String getR11Particulars() {
				return r11Particulars;
			}

			public void setR11Particulars(String r11Particulars) {
				this.r11Particulars = r11Particulars;
			}

			public String getR11Y070() {
				return r11Y070;
			}

			public void setR11Y070(String r11y070) {
				r11Y070 = r11y070;
			}

			public java.math.BigDecimal getR11Amount() {
				return r11Amount;
			}

			public void setR11Amount(java.math.BigDecimal r11Amount) {
				this.r11Amount = r11Amount;
			}

			public String getR12Particulars() {
				return r12Particulars;
			}

			public void setR12Particulars(String r12Particulars) {
				this.r12Particulars = r12Particulars;
			}

			public String getR12Y080() {
				return r12Y080;
			}

			public void setR12Y080(String r12y080) {
				r12Y080 = r12y080;
			}

			public java.math.BigDecimal getR12Amount() {
				return r12Amount;
			}

			public void setR12Amount(java.math.BigDecimal r12Amount) {
				this.r12Amount = r12Amount;
			}

			public String getR13Particulars() {
				return r13Particulars;
			}

			public void setR13Particulars(String r13Particulars) {
				this.r13Particulars = r13Particulars;
			}

			public String getR13Y090() {
				return r13Y090;
			}

			public void setR13Y090(String r13y090) {
				r13Y090 = r13y090;
			}

			public java.math.BigDecimal getR13Amount() {
				return r13Amount;
			}

			public void setR13Amount(java.math.BigDecimal r13Amount) {
				this.r13Amount = r13Amount;
			}

			public String getR14Particulars() {
				return r14Particulars;
			}

			public void setR14Particulars(String r14Particulars) {
				this.r14Particulars = r14Particulars;
			}

			public String getR14Y100() {
				return r14Y100;
			}

			public void setR14Y100(String r14y100) {
				r14Y100 = r14y100;
			}

			public java.math.BigDecimal getR14Amount() {
				return r14Amount;
			}

			public void setR14Amount(java.math.BigDecimal r14Amount) {
				this.r14Amount = r14Amount;
			}

			public String getR15Particulars() {
				return r15Particulars;
			}

			public void setR15Particulars(String r15Particulars) {
				this.r15Particulars = r15Particulars;
			}

			public String getR15Y110() {
				return r15Y110;
			}

			public void setR15Y110(String r15y110) {
				r15Y110 = r15y110;
			}

			public java.math.BigDecimal getR15Amount() {
				return r15Amount;
			}

			public void setR15Amount(java.math.BigDecimal r15Amount) {
				this.r15Amount = r15Amount;
			}

			public String getR16Particulars() {
				return r16Particulars;
			}

			public void setR16Particulars(String r16Particulars) {
				this.r16Particulars = r16Particulars;
			}

			public String getR16Y120() {
				return r16Y120;
			}

			public void setR16Y120(String r16y120) {
				r16Y120 = r16y120;
			}

			public java.math.BigDecimal getR16Amount() {
				return r16Amount;
			}

			public void setR16Amount(java.math.BigDecimal r16Amount) {
				this.r16Amount = r16Amount;
			}

			public String getR17Particulars() {
				return r17Particulars;
			}

			public void setR17Particulars(String r17Particulars) {
				this.r17Particulars = r17Particulars;
			}

			public String getR17Y130() {
				return r17Y130;
			}

			public void setR17Y130(String r17y130) {
				r17Y130 = r17y130;
			}

			public java.math.BigDecimal getR17Amount() {
				return r17Amount;
			}

			public void setR17Amount(java.math.BigDecimal r17Amount) {
				this.r17Amount = r17Amount;
			}

			public String getR18Particulars() {
				return r18Particulars;
			}

			public void setR18Particulars(String r18Particulars) {
				this.r18Particulars = r18Particulars;
			}

			public String getR18Y140() {
				return r18Y140;
			}

			public void setR18Y140(String r18y140) {
				r18Y140 = r18y140;
			}

			public java.math.BigDecimal getR18Amount() {
				return r18Amount;
			}

			public void setR18Amount(java.math.BigDecimal r18Amount) {
				this.r18Amount = r18Amount;
			}

			public String getR19Particulars() {
				return r19Particulars;
			}

			public void setR19Particulars(String r19Particulars) {
				this.r19Particulars = r19Particulars;
			}

			public String getR19Y150() {
				return r19Y150;
			}

			public void setR19Y150(String r19y150) {
				r19Y150 = r19y150;
			}

			public java.math.BigDecimal getR19Amount() {
				return r19Amount;
			}

			public void setR19Amount(java.math.BigDecimal r19Amount) {
				this.r19Amount = r19Amount;
			}

			public String getR20Particulars() {
				return r20Particulars;
			}

			public void setR20Particulars(String r20Particulars) {
				this.r20Particulars = r20Particulars;
			}

			public String getR20Y160() {
				return r20Y160;
			}

			public void setR20Y160(String r20y160) {
				r20Y160 = r20y160;
			}

			public java.math.BigDecimal getR20Amount() {
				return r20Amount;
			}

			public void setR20Amount(java.math.BigDecimal r20Amount) {
				this.r20Amount = r20Amount;
			}

			public String getR21Particulars() {
				return r21Particulars;
			}

			public void setR21Particulars(String r21Particulars) {
				this.r21Particulars = r21Particulars;
			}

			public String getR21Y170() {
				return r21Y170;
			}

			public void setR21Y170(String r21y170) {
				r21Y170 = r21y170;
			}

			public java.math.BigDecimal getR21Amount() {
				return r21Amount;
			}

			public void setR21Amount(java.math.BigDecimal r21Amount) {
				this.r21Amount = r21Amount;
			}

			public String getR22Particulars() {
				return r22Particulars;
			}

			public void setR22Particulars(String r22Particulars) {
				this.r22Particulars = r22Particulars;
			}

			public String getR22Y180() {
				return r22Y180;
			}

			public void setR22Y180(String r22y180) {
				r22Y180 = r22y180;
			}

			public java.math.BigDecimal getR22Amount() {
				return r22Amount;
			}

			public void setR22Amount(java.math.BigDecimal r22Amount) {
				this.r22Amount = r22Amount;
			}

			public String getR23Particulars() {
				return r23Particulars;
			}

			public void setR23Particulars(String r23Particulars) {
				this.r23Particulars = r23Particulars;
			}

			public String getR23Y190() {
				return r23Y190;
			}

			public void setR23Y190(String r23y190) {
				r23Y190 = r23y190;
			}

			public java.math.BigDecimal getR23Amount() {
				return r23Amount;
			}

			public void setR23Amount(java.math.BigDecimal r23Amount) {
				this.r23Amount = r23Amount;
			}

			public String getR24Particulars() {
				return r24Particulars;
			}

			public void setR24Particulars(String r24Particulars) {
				this.r24Particulars = r24Particulars;
			}

			public String getR24Y200() {
				return r24Y200;
			}

			public void setR24Y200(String r24y200) {
				r24Y200 = r24y200;
			}

			public java.math.BigDecimal getR24Amount() {
				return r24Amount;
			}

			public void setR24Amount(java.math.BigDecimal r24Amount) {
				this.r24Amount = r24Amount;
			}

			public String getR25Particulars() {
				return r25Particulars;
			}

			public void setR25Particulars(String r25Particulars) {
				this.r25Particulars = r25Particulars;
			}

			public String getR25Y210() {
				return r25Y210;
			}

			public void setR25Y210(String r25y210) {
				r25Y210 = r25y210;
			}

			public java.math.BigDecimal getR25Amount() {
				return r25Amount;
			}

			public void setR25Amount(java.math.BigDecimal r25Amount) {
				this.r25Amount = r25Amount;
			}

			public String getR26Particulars() {
				return r26Particulars;
			}

			public void setR26Particulars(String r26Particulars) {
				this.r26Particulars = r26Particulars;
			}

			public String getR26Y220() {
				return r26Y220;
			}

			public void setR26Y220(String r26y220) {
				r26Y220 = r26y220;
			}

			public java.math.BigDecimal getR26Amount() {
				return r26Amount;
			}

			public void setR26Amount(java.math.BigDecimal r26Amount) {
				this.r26Amount = r26Amount;
			}

			public String getR27Particulars() {
				return r27Particulars;
			}

			public void setR27Particulars(String r27Particulars) {
				this.r27Particulars = r27Particulars;
			}

			public String getR27Y230() {
				return r27Y230;
			}

			public void setR27Y230(String r27y230) {
				r27Y230 = r27y230;
			}

			public java.math.BigDecimal getR27Amount() {
				return r27Amount;
			}

			public void setR27Amount(java.math.BigDecimal r27Amount) {
				this.r27Amount = r27Amount;
			}

			public String getR28Particulars() {
				return r28Particulars;
			}

			public void setR28Particulars(String r28Particulars) {
				this.r28Particulars = r28Particulars;
			}

			public String getR28Y240() {
				return r28Y240;
			}

			public void setR28Y240(String r28y240) {
				r28Y240 = r28y240;
			}

			public java.math.BigDecimal getR28Amount() {
				return r28Amount;
			}

			public void setR28Amount(java.math.BigDecimal r28Amount) {
				this.r28Amount = r28Amount;
			}

			public String getR29Particulars() {
				return r29Particulars;
			}

			public void setR29Particulars(String r29Particulars) {
				this.r29Particulars = r29Particulars;
			}

			public String getR29Y250() {
				return r29Y250;
			}

			public void setR29Y250(String r29y250) {
				r29Y250 = r29y250;
			}

			public java.math.BigDecimal getR29Amount() {
				return r29Amount;
			}

			public void setR29Amount(java.math.BigDecimal r29Amount) {
				this.r29Amount = r29Amount;
			}

			public String getR30Particulars() {
				return r30Particulars;
			}

			public void setR30Particulars(String r30Particulars) {
				this.r30Particulars = r30Particulars;
			}

			public String getR30Y260() {
				return r30Y260;
			}

			public void setR30Y260(String r30y260) {
				r30Y260 = r30y260;
			}

			public java.math.BigDecimal getR30Amount() {
				return r30Amount;
			}

			public void setR30Amount(java.math.BigDecimal r30Amount) {
				this.r30Amount = r30Amount;
			}

			public String getR31Particulars() {
				return r31Particulars;
			}

			public void setR31Particulars(String r31Particulars) {
				this.r31Particulars = r31Particulars;
			}

			public String getR31Y270() {
				return r31Y270;
			}

			public void setR31Y270(String r31y270) {
				r31Y270 = r31y270;
			}

			public java.math.BigDecimal getR31Amount() {
				return r31Amount;
			}

			public void setR31Amount(java.math.BigDecimal r31Amount) {
				this.r31Amount = r31Amount;
			}

			public String getR32Particulars() {
				return r32Particulars;
			}

			public void setR32Particulars(String r32Particulars) {
				this.r32Particulars = r32Particulars;
			}

			public String getR32Y280() {
				return r32Y280;
			}

			public void setR32Y280(String r32y280) {
				r32Y280 = r32y280;
			}

			public java.math.BigDecimal getR32Amount() {
				return r32Amount;
			}

			public void setR32Amount(java.math.BigDecimal r32Amount) {
				this.r32Amount = r32Amount;
			}

			public String getR33Particulars() {
				return r33Particulars;
			}

			public void setR33Particulars(String r33Particulars) {
				this.r33Particulars = r33Particulars;
			}

			public String getR33Y290() {
				return r33Y290;
			}

			public void setR33Y290(String r33y290) {
				r33Y290 = r33y290;
			}

			public java.math.BigDecimal getR33Amount() {
				return r33Amount;
			}

			public void setR33Amount(java.math.BigDecimal r33Amount) {
				this.r33Amount = r33Amount;
			}

			public String getR34Particulars() {
				return r34Particulars;
			}

			public void setR34Particulars(String r34Particulars) {
				this.r34Particulars = r34Particulars;
			}

			public String getR34Y300() {
				return r34Y300;
			}

			public void setR34Y300(String r34y300) {
				r34Y300 = r34y300;
			}

			public java.math.BigDecimal getR34Amount() {
				return r34Amount;
			}

			public void setR34Amount(java.math.BigDecimal r34Amount) {
				this.r34Amount = r34Amount;
			}

			public String getR35Particulars() {
				return r35Particulars;
			}

			public void setR35Particulars(String r35Particulars) {
				this.r35Particulars = r35Particulars;
			}

			public String getR35Y310() {
				return r35Y310;
			}

			public void setR35Y310(String r35y310) {
				r35Y310 = r35y310;
			}

			public java.math.BigDecimal getR35Amount() {
				return r35Amount;
			}

			public void setR35Amount(java.math.BigDecimal r35Amount) {
				this.r35Amount = r35Amount;
			}

			public String getR36Particulars() {
				return r36Particulars;
			}

			public void setR36Particulars(String r36Particulars) {
				this.r36Particulars = r36Particulars;
			}

			public String getR36Y320() {
				return r36Y320;
			}

			public void setR36Y320(String r36y320) {
				r36Y320 = r36y320;
			}

			public java.math.BigDecimal getR36Amount() {
				return r36Amount;
			}

			public void setR36Amount(java.math.BigDecimal r36Amount) {
				this.r36Amount = r36Amount;
			}

			public String getR37Particulars() {
				return r37Particulars;
			}

			public void setR37Particulars(String r37Particulars) {
				this.r37Particulars = r37Particulars;
			}

			public String getR37Y330() {
				return r37Y330;
			}

			public void setR37Y330(String r37y330) {
				r37Y330 = r37y330;
			}

			public java.math.BigDecimal getR37Amount() {
				return r37Amount;
			}

			public void setR37Amount(java.math.BigDecimal r37Amount) {
				this.r37Amount = r37Amount;
			}

			public String getR38Particulars() {
				return r38Particulars;
			}

			public void setR38Particulars(String r38Particulars) {
				this.r38Particulars = r38Particulars;
			}

			public String getR38Y340() {
				return r38Y340;
			}

			public void setR38Y340(String r38y340) {
				r38Y340 = r38y340;
			}

			public java.math.BigDecimal getR38Amount() {
				return r38Amount;
			}

			public void setR38Amount(java.math.BigDecimal r38Amount) {
				this.r38Amount = r38Amount;
			}

			public String getR39Particulars() {
				return r39Particulars;
			}

			public void setR39Particulars(String r39Particulars) {
				this.r39Particulars = r39Particulars;
			}

			public String getR39Y350() {
				return r39Y350;
			}

			public void setR39Y350(String r39y350) {
				r39Y350 = r39y350;
			}

			public java.math.BigDecimal getR39Amount() {
				return r39Amount;
			}

			public void setR39Amount(java.math.BigDecimal r39Amount) {
				this.r39Amount = r39Amount;
			}

			public String getR40Particulars() {
				return r40Particulars;
			}

			public void setR40Particulars(String r40Particulars) {
				this.r40Particulars = r40Particulars;
			}

			public String getR40Y360() {
				return r40Y360;
			}

			public void setR40Y360(String r40y360) {
				r40Y360 = r40y360;
			}

			public java.math.BigDecimal getR40Amount() {
				return r40Amount;
			}

			public void setR40Amount(java.math.BigDecimal r40Amount) {
				this.r40Amount = r40Amount;
			}

			public String getR41Particulars() {
				return r41Particulars;
			}

			public void setR41Particulars(String r41Particulars) {
				this.r41Particulars = r41Particulars;
			}

			public String getR41Y370() {
				return r41Y370;
			}

			public void setR41Y370(String r41y370) {
				r41Y370 = r41y370;
			}

			public java.math.BigDecimal getR41Amount() {
				return r41Amount;
			}

			public void setR41Amount(java.math.BigDecimal r41Amount) {
				this.r41Amount = r41Amount;
			}

			public String getR42Particulars() {
				return r42Particulars;
			}

			public void setR42Particulars(String r42Particulars) {
				this.r42Particulars = r42Particulars;
			}

			public String getR42Y380() {
				return r42Y380;
			}

			public void setR42Y380(String r42y380) {
				r42Y380 = r42y380;
			}

			public java.math.BigDecimal getR42Amount() {
				return r42Amount;
			}

			public void setR42Amount(java.math.BigDecimal r42Amount) {
				this.r42Amount = r42Amount;
			}

			public java.util.Date getReportDate() {
				return reportDate;
			}

			public void setReportDate(java.util.Date reportDate) {
				this.reportDate = reportDate;
			}

			public java.math.BigDecimal getReportVersion() {
				return reportVersion;
			}

			public void setReportVersion(java.math.BigDecimal reportVersion) {
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

	
	
	//Detail row mapper and Entity
	public class FINCON_1A_Detail_Entity {

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

	    // ================= GETTERS & SETTERS =================

	    public Long getSno() { return sno; }
	    public void setSno(Long sno) { this.sno = sno; }

	    public String getCustId() { return custId; }
	    public void setCustId(String custId) { this.custId = custId; }

	    public String getAcctNumber() { return acctNumber; }
	    public void setAcctNumber(String acctNumber) { this.acctNumber = acctNumber; }

	    public String getAcctName() { return acctName; }
	    public void setAcctName(String acctName) { this.acctName = acctName; }

	    public String getDataType() { return dataType; }
	    public void setDataType(String dataType) { this.dataType = dataType; }

	    public String getReportName() { return reportName; }
	    public void setReportName(String reportName) { this.reportName = reportName; }

	    public String getReportLabel() { return reportLabel; }
	    public void setReportLabel(String reportLabel) { this.reportLabel = reportLabel; }

	    public String getReportAddlCriteria_1() { return reportAddlCriteria_1; }
	    public void setReportAddlCriteria_1(String reportAddlCriteria_1) { this.reportAddlCriteria_1 = reportAddlCriteria_1; }

	    public String getReportRemarks() { return reportRemarks; }
	    public void setReportRemarks(String reportRemarks) { this.reportRemarks = reportRemarks; }

	    public String getModificationRemarks() { return modificationRemarks; }
	    public void setModificationRemarks(String modificationRemarks) { this.modificationRemarks = modificationRemarks; }

	    public String getDataEntryVersion() { return dataEntryVersion; }
	    public void setDataEntryVersion(String dataEntryVersion) { this.dataEntryVersion = dataEntryVersion; }

	    public BigDecimal getAcctBalanceInpula() { return acctBalanceInpula; }
	    public void setAcctBalanceInpula(BigDecimal acctBalanceInpula) { this.acctBalanceInpula = acctBalanceInpula; }

	    public Date getReportDate() { return reportDate; }
	    public void setReportDate(Date reportDate) { this.reportDate = reportDate; }

	    public String getCreateUser() { return createUser; }
	    public void setCreateUser(String createUser) { this.createUser = createUser; }

	    public Date getCreateTime() { return createTime; }
	    public void setCreateTime(Date createTime) { this.createTime = createTime; }

	    public String getModifyUser() { return modifyUser; }
	    public void setModifyUser(String modifyUser) { this.modifyUser = modifyUser; }

	    public Date getModifyTime() { return modifyTime; }
	    public void setModifyTime(Date modifyTime) { this.modifyTime = modifyTime; }

	    public String getVerifyUser() { return verifyUser; }
	    public void setVerifyUser(String verifyUser) { this.verifyUser = verifyUser; }

	    public Date getVerifyTime() { return verifyTime; }
	    public void setVerifyTime(Date verifyTime) { this.verifyTime = verifyTime; }

	    public String getEntityFlg() { return entityFlg; }
	    public void setEntityFlg(String entityFlg) { this.entityFlg = entityFlg; }

	    public String getModifyFlg() { return modifyFlg; }
	    public void setModifyFlg(String modifyFlg) { this.modifyFlg = modifyFlg; }

	    public String getDelFlg() { return delFlg; }
	    public void setDelFlg(String delFlg) { this.delFlg = delFlg; }
	}



	class Fincon1A_DetailRowMapper implements RowMapper<FINCON_1A_Detail_Entity> {

	    @Override
	    public FINCON_1A_Detail_Entity mapRow(ResultSet rs, int rowNum) throws SQLException {

	    	FINCON_1A_Detail_Entity obj = new FINCON_1A_Detail_Entity();

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


	public class Fincon_1A_Archival_Detail_Entity {

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

	    // ================= GETTERS & SETTERS =================

	    public Long getSno() { return sno; }
	    public void setSno(Long sno) { this.sno = sno; }

	    public String getCustId() { return custId; }
	    public void setCustId(String custId) { this.custId = custId; }

	    public String getAcctNumber() { return acctNumber; }
	    public void setAcctNumber(String acctNumber) { this.acctNumber = acctNumber; }

	    public String getAcctName() { return acctName; }
	    public void setAcctName(String acctName) { this.acctName = acctName; }

	    public String getDataType() { return dataType; }
	    public void setDataType(String dataType) { this.dataType = dataType; }

	    public String getReportName() { return reportName; }
	    public void setReportName(String reportName) { this.reportName = reportName; }

	    public String getReportLabel() { return reportLabel; }
	    public void setReportLabel(String reportLabel) { this.reportLabel = reportLabel; }

	    public String getReportAddlCriteria_1() { return reportAddlCriteria_1; }
	    public void setReportAddlCriteria_1(String v) { this.reportAddlCriteria_1 = v; }

	    public String getReportRemarks() { return reportRemarks; }
	    public void setReportRemarks(String v) { this.reportRemarks = v; }

	    public String getModificationRemarks() { return modificationRemarks; }
	    public void setModificationRemarks(String v) { this.modificationRemarks = v; }

	    public String getDataEntryVersion() { return dataEntryVersion; }
	    public void setDataEntryVersion(String v) { this.dataEntryVersion = v; }

	    public BigDecimal getAcctBalanceInpula() { return acctBalanceInpula; }
	    public void setAcctBalanceInpula(BigDecimal v) { this.acctBalanceInpula = v; }

	    public Date getReportDate() { return reportDate; }
	    public void setReportDate(Date v) { this.reportDate = v; }

	    public String getCreateUser() { return createUser; }
	    public void setCreateUser(String v) { this.createUser = v; }

	    public Date getCreateTime() { return createTime; }
	    public void setCreateTime(Date v) { this.createTime = v; }

	    public String getModifyUser() { return modifyUser; }
	    public void setModifyUser(String v) { this.modifyUser = v; }

	    public Date getModifyTime() { return modifyTime; }
	    public void setModifyTime(Date v) { this.modifyTime = v; }

	    public String getVerifyUser() { return verifyUser; }
	    public void setVerifyUser(String v) { this.verifyUser = v; }

	    public Date getVerifyTime() { return verifyTime; }
	    public void setVerifyTime(Date v) { this.verifyTime = v; }

	    public Character getEntityFlg() { return entityFlg; }
	    public void setEntityFlg(Character v) { this.entityFlg = v; }

	    public Character getModifyFlg() { return modifyFlg; }
	    public void setModifyFlg(Character v) { this.modifyFlg = v; }

	    public Character getDelFlg() { return delFlg; }
	    public void setDelFlg(Character v) { this.delFlg = v; }
	}

	class Fincon_1a_ArchivalDetailRowMapper implements RowMapper<Fincon_1A_Archival_Detail_Entity> {

	    @Override
	    public Fincon_1A_Archival_Detail_Entity mapRow(ResultSet rs, int rowNum)
	            throws SQLException {

	    	Fincon_1A_Archival_Detail_Entity obj = new Fincon_1A_Archival_Detail_Entity();

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

	        obj.setEntityFlg(
	            rs.getString("ENTITY_FLG") != null
	                ? rs.getString("ENTITY_FLG").charAt(0)
	                : null);

	        obj.setModifyFlg(
	            rs.getString("MODIFY_FLG") != null
	                ? rs.getString("MODIFY_FLG").charAt(0)
	                : null);

	        obj.setDelFlg(
	            rs.getString("DEL_FLG") != null
	                ? rs.getString("DEL_FLG").charAt(0)
	                : null);

	        return obj;
	    }
	}


	

}
