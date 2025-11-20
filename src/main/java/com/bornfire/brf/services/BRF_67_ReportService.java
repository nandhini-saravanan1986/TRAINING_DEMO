package com.bornfire.brf.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import com.bornfire.brf.entities.BRF_67_Detail_Entity;
import com.bornfire.brf.entities.BRF_67_Detail_Repo;
import com.bornfire.brf.entities.BRF_67_Summary_Entity;
import com.bornfire.brf.entities.BRF_67_Summary_Repo;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
//import com.microsoft.schemas.office.visio.x2012.main.CellType;
import org.apache.poi.ss.usermodel.CellType;
import java.math.BigDecimal;
//import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import com.itextpdf.text.BaseColor;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;


@Component
@Service

public class BRF_67_ReportService {
	private static final Logger logger = LoggerFactory.getLogger(BRF_67_ReportService.class);

	@Autowired
	private Environment env;

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	BRF_67_Detail_Repo BRF_67_DETAIL_Repo;

	@Autowired
	BRF_67_Summary_Repo BRF_67_Summary_Repo;

	SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy");

	public ModelAndView getBRF_67View(String reportId, String fromdate, String todate, String currency, String dtltype,
			Pageable pageable) {

		ModelAndView mv = new ModelAndView();
		Session hs = sessionFactory.getCurrentSession();
		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;

		List<BRF_67_Summary_Entity> T1Master = new ArrayList<BRF_67_Summary_Entity>();
		try {
			Date d1 = dateformat.parse(todate);
			// T1rep = t1CurProdServiceRepo.getT1CurProdServices(d1);

			// T1Master = hs.createQuery("from BRF1_REPORT_ENTITY a where a.report_date = ?1
			// ", BRF1_REPORT_ENTITY.class)
			// .setParameter(1, df.parse(todate)).getResultList();
			T1Master = BRF_67_Summary_Repo.getdatabydateList(dateformat.parse(todate));

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// T1rep = t1CurProdServiceRepo.getT1CurProdServices(d1);

		mv.setViewName("BRRS/BRF_67");

		mv.addObject("reportsummary", T1Master);
		// mv.addObject("reportmaster", T1Master);
		mv.addObject("displaymode", "summary");
		// mv.addObject("reportsflag", "reportsflag");
		// mv.addObject("menu", reportId);
		System.out.println("scv" + mv.getViewName());

		return mv;

	}

	public ModelAndView getBRF_67currentDtl(String reportId, String fromdate, String todate, String currency,
			String dtltype, Pageable pageable, String filter) {

		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int totalPages = 0;

		ModelAndView mv = new ModelAndView();

		Session hs = sessionFactory.getCurrentSession();
		List<BRF_67_Detail_Entity> T1Dt1 = new ArrayList<BRF_67_Detail_Entity>();

		try {
			Date d1 = dateformat.parse(todate);
			String rowId = null;
			String columnId = null;
			if (filter != null && filter.contains(",")) {
				String[] parts = filter.split(",");
				if (parts.length >= 2) {
					rowId = parts[0];
					columnId = parts[1];
				}
			}

			if (rowId != null && columnId != null) {
				System.out.println("Testing");
				T1Dt1 = BRF_67_DETAIL_Repo.getdatabydateListrow(dateformat.parse(todate), columnId, rowId);

			} else {

				T1Dt1 = BRF_67_DETAIL_Repo.getdatabydateList(d1, currentPage, pageSize);
				totalPages = BRF_67_DETAIL_Repo.getdatacount(dateformat.parse(todate));
				mv.addObject("pagination", "YES");

			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Page<Object> T1Dt1Page = new PageImpl<Object>(pagedlist,
		// PageRequest.of(currentPage, pageSize), T1Dt1.size());

		mv.setViewName("BRRS/BRF_67");
		mv.addObject("displaymode", "Details");
		mv.addObject("currentPage", currentPage);
		mv.addObject("totalPages", (int) Math.ceil((double) totalPages / 100));
		// mv.addObject("reportdetails", T1Dt1Page.getContent());
		mv.addObject("reportdetails", T1Dt1);
		mv.addObject("reportmaster12", T1Dt1);
		// mv.addObject("reportmaster1", qr);
		// mv.addObject("singledetail", new T1CurProdDetail());
		mv.addObject("reportsflag", "reportsflag");
		mv.addObject("menu", reportId);
		return mv;
	}

	public byte[] BRF_67_Excel(String filename, String reportId, String fromdate, String todate, String currency,
			String dtltype) throws Exception {
		logger.info("Service: Starting Excel generation process in memory.");
		

		List<BRF_67_Summary_Entity> dataList = BRF_67_Summary_Repo.getdatabydateList(dateformat.parse(todate));

		if (dataList.isEmpty()) {
			logger.warn("Service: No data found for BRRS report. Returning empty result.");
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

			int startRow = 10;

			if (!dataList.isEmpty()) {
				for (int i = 0; i < dataList.size(); i++) {
					BRF_67_Summary_Entity record = dataList.get(i);
					System.out.println("rownumber=" + startRow + i);
					Row row = sheet.getRow(startRow + i);
					if (row == null) {
						row = sheet.createRow(startRow + i);
					}

					// row11
					// Column D
					Cell cell3 = row.createCell(3);
					if (record.getR0020_NO_ACC_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0020_NO_ACC_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}
					// Column E
					Cell cell4 = row.createCell(4);
					if (record.getR0020_BAL_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0020_BAL_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					// Column F
					Cell cell5 = row.createCell(5);
					if (record.getR0020_NO_ACC_OTH_NAT() != null) {
						cell5.setCellValue(record.getR0020_NO_ACC_OTH_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					// Column G
					Cell cell6 = row.createCell(6);
					if (record.getR0020_BAL_OTH_NAT() != null) {
						cell6.setCellValue(record.getR0020_BAL_OTH_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
					}

					// row12
					row = sheet.getRow(11);
					// Column D
					cell3 = row.createCell(3);
					if (record.getR0030_NO_ACC_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0030_NO_ACC_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}
					// Column E
					cell4 = row.createCell(4);
					if (record.getR0030_BAL_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0030_BAL_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					// Column F
					cell5 = row.createCell(5);
					if (record.getR0030_NO_ACC_OTH_NAT() != null) {
						cell5.setCellValue(record.getR0030_NO_ACC_OTH_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					// Column G
					cell6 = row.createCell(6);
					if (record.getR0030_BAL_OTH_NAT() != null) {
						cell6.setCellValue(record.getR0030_BAL_OTH_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
					}

					// row13
					row = sheet.getRow(12);
					// Column D
					cell3 = row.createCell(3);
					if (record.getR0040_NO_ACC_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0040_NO_ACC_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}
					// Column E
					cell4 = row.createCell(4);
					if (record.getR0040_BAL_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0040_BAL_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					// Column F
					cell5 = row.createCell(5);
					if (record.getR0040_NO_ACC_OTH_NAT() != null) {
						cell5.setCellValue(record.getR0040_NO_ACC_OTH_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					// Column G
					cell6 = row.createCell(6);
					if (record.getR0040_BAL_OTH_NAT() != null) {
						cell6.setCellValue(record.getR0040_BAL_OTH_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
					}

					// row14
					row = sheet.getRow(13);
					// Column D
					cell3 = row.createCell(3);
					if (record.getR0050_NO_ACC_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0050_NO_ACC_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}
					// Column E
					cell4 = row.createCell(4);
					if (record.getR0050_BAL_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0050_BAL_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					// Column F
					cell5 = row.createCell(5);
					if (record.getR0050_NO_ACC_OTH_NAT() != null) {
						cell5.setCellValue(record.getR0050_NO_ACC_OTH_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					// Column G
					cell6 = row.createCell(6);
					if (record.getR0050_BAL_OTH_NAT() != null) {
						cell6.setCellValue(record.getR0050_BAL_OTH_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
					}

					// row15
					row = sheet.getRow(14);
					// Column D
					cell3 = row.createCell(3);
					if (record.getR0060_NO_ACC_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0060_NO_ACC_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}
					// Column E
					cell4 = row.createCell(4);
					if (record.getR0060_BAL_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0060_BAL_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					// Column F
					cell5 = row.createCell(5);
					if (record.getR0060_NO_ACC_OTH_NAT() != null) {
						cell5.setCellValue(record.getR0060_NO_ACC_OTH_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					// Column G
					cell6 = row.createCell(6);
					if (record.getR0060_BAL_OTH_NAT() != null) {
						cell6.setCellValue(record.getR0060_BAL_OTH_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
					}

					// row16
					row = sheet.getRow(15);
					// Column D
					cell3 = row.createCell(3);
					if (record.getR0070_NO_ACC_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0070_NO_ACC_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}
					// Column E
					cell4 = row.createCell(4);
					if (record.getR0070_BAL_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0070_BAL_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					// Column F
					cell5 = row.createCell(5);
					if (record.getR0070_NO_ACC_OTH_NAT() != null) {
						cell5.setCellValue(record.getR0070_NO_ACC_OTH_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					// Column G
					cell6 = row.createCell(6);
					if (record.getR0070_BAL_OTH_NAT() != null) {
						cell6.setCellValue(record.getR0070_BAL_OTH_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);

					}

					// Row 19
					row = sheet.getRow(18);
					// Column H
					Cell cell7 = row.createCell(7);
					if (record.getR0100_NO_ACC_TOT_DEPO() != null) {
						cell7.setCellValue(record.getR0100_NO_ACC_TOT_DEPO().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}
					// Column I
					Cell cell8 = row.createCell(8);
					if (record.getR0100_BAL_TOT_DEPO() != null) {
						cell8.setCellValue(record.getR0100_BAL_TOT_DEPO().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// Row 20
					row = sheet.getRow(19);
					// Column H
					cell7 = row.createCell(7);
					if (record.getR0110_NO_ACC_TOT_DEPO() != null) {
						cell7.setCellValue(record.getR0110_NO_ACC_TOT_DEPO().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}
					// Column I
					cell8 = row.createCell(8);
					if (record.getR0110_BAL_TOT_DEPO() != null) {
						cell8.setCellValue(record.getR0110_BAL_TOT_DEPO().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// Row 21
					row = sheet.getRow(20);
					// Column H
					cell7 = row.createCell(7);
					if (record.getR0120_NO_ACC_TOT_DEPO() != null) {
						cell7.setCellValue(record.getR0120_NO_ACC_TOT_DEPO().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}
					// Column I
					cell8 = row.createCell(8);
					if (record.getR0120_BAL_TOT_DEPO() != null) {
						cell8.setCellValue(record.getR0120_BAL_TOT_DEPO().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// Row 22
					row = sheet.getRow(21);
					// Column H
					cell7 = row.createCell(7);
					if (record.getR0130_NO_ACC_TOT_DEPO() != null) {
						cell7.setCellValue(record.getR0130_NO_ACC_TOT_DEPO().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}
					// Column I
					cell8 = row.createCell(8);
					if (record.getR0130_BAL_TOT_DEPO() != null) {
						cell8.setCellValue(record.getR0130_BAL_TOT_DEPO().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// Row 23
					row = sheet.getRow(22);
					// Column H
					cell7 = row.createCell(7);
					if (record.getR0140_NO_ACC_TOT_DEPO() != null) {
						cell7.setCellValue(record.getR0140_NO_ACC_TOT_DEPO().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}
					// Column I
					cell8 = row.createCell(8);
					if (record.getR0140_BAL_TOT_DEPO() != null) {
						cell8.setCellValue(record.getR0140_BAL_TOT_DEPO().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// Row 24
					row = sheet.getRow(23);
					// Column H
					cell7 = row.createCell(7);
					if (record.getR0150_NO_ACC_TOT_DEPO() != null) {
						cell7.setCellValue(record.getR0150_NO_ACC_TOT_DEPO().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}
					// Column I
					cell8 = row.createCell(8);
					if (record.getR0150_BAL_TOT_DEPO() != null) {
						cell8.setCellValue(record.getR0150_BAL_TOT_DEPO().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

				}
				workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
			} else {

			}

			// Write the final workbook content to the in-memory stream.
			workbook.write(out);

			logger.info("Service: Excel data successfully written to memory buffer ({} bytes).", out.size());

			return out.toByteArray();
		}
	}

	public byte[] BRF_67_DetailExcel(String filename, String fromdate, String todate) {
		System.out.println("hi"+todate);
		try {
			logger.info("Generating Excel for BRF_67 Details...");
			System.out.println("came to Detail download service");

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("BRF_67Details");

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
			balanceStyle.setDataFormat(workbook.createDataFormat().getFormat("0.000"));
			balanceStyle.setBorderTop(border);
			balanceStyle.setBorderBottom(border);
			balanceStyle.setBorderLeft(border);
			balanceStyle.setBorderRight(border);

			// Header row
			String[] headers = { "CUST ID", "ACCT NO", "ACCT NAME", "ACCT BALANCE", "ROWID", "COLUMNID",
					"REPORT_DATE" };

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
			List<BRF_67_Detail_Entity> reportData = BRF_67_DETAIL_Repo.getdatabydateList(parsedToDate);

			if (reportData != null && !reportData.isEmpty()) {
				int rowIndex = 1;
				for (BRF_67_Detail_Entity item : reportData) {
					XSSFRow row = sheet.createRow(rowIndex++);

					row.createCell(0).setCellValue(item.getCustId());
					row.createCell(1).setCellValue(item.getAcctNumber());
					row.createCell(2).setCellValue(item.getAcctName());

					// ACCT BALANCE (right aligned, 3 decimal places)
					Cell balanceCell = row.createCell(3);
					if (item.getAcctBalanceInaed() != null) {
						balanceCell.setCellValue(item.getAcctBalanceInaed().doubleValue());
					} else {
						balanceCell.setCellValue(0.000);
					}
					balanceCell.setCellStyle(balanceStyle);

					row.createCell(4).setCellValue(item.getRowId());
					row.createCell(5).setCellValue(item.getColumnId());
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
				logger.info("No data found for BRF_67 — only header will be written.");
			}

			// Write to byte[]
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			workbook.write(bos);
			workbook.close();

			logger.info("Excel generation completed with {} row(s).", reportData != null ? reportData.size() : 0);
			return bos.toByteArray();

		} catch (Exception e) {
			logger.error("Error generating BRF_67 Excel", e);
			return new byte[0];
		}
	}


	public byte[] convertExcelBytesToPdf(byte[] excelBytes) throws Exception {
	    try (
	        InputStream inputStream = new ByteArrayInputStream(excelBytes);
	        Workbook workbook = WorkbookFactory.create(inputStream);
	        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream()
	    ) {
	        Sheet sheet = workbook.getSheetAt(0);
	        Document document = new Document(PageSize.A3.rotate(), 20, 20, 20, 20);
	        PdfWriter.getInstance(document, pdfOut);
	        document.open();
	        Row headerRow = null;
	        for (Row r : sheet) {
	            if (r != null && r.getLastCellNum() > 0) {
	                headerRow = r;
	                break;
	            }
	        }
	        if (headerRow == null) throw new IllegalArgumentException("Excel sheet is empty.");

	        int firstCol = headerRow.getFirstCellNum();
	        int lastCol = headerRow.getLastCellNum();

	        PdfPTable table = new PdfPTable(lastCol - firstCol);
	        table.setWidthPercentage(100);
	        table.setSpacingBefore(10f);

	        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
	        DataFormatter formatter = new DataFormatter();
	        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
	        Set<String> mergedCells = new HashSet<>();

	        for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
	            Row row = sheet.getRow(rowIndex);
	            if (row == null) continue;

	            for (int colIndex = firstCol; colIndex < lastCol; colIndex++) {
	                String cellKey = rowIndex + "-" + colIndex;
	                if (mergedCells.contains(cellKey)) continue;

	                Cell cell = row.getCell(colIndex);
	                String cellValue = "";
	                if (cell != null) {
	                    if (cell.getCellTypeEnum() == CellType.FORMULA) {
	                        CellValue eval = evaluator.evaluate(cell);
	                        if (eval != null) {
	                            switch (eval.getCellTypeEnum()) {
	                                case STRING:  cellValue = eval.getStringValue(); break;
	                                case NUMERIC: cellValue = String.valueOf(eval.getNumberValue()); break;
	                                case BOOLEAN: cellValue = String.valueOf(eval.getBooleanValue()); break;
	                                default: cellValue = "";
	                            }
	                        }
	                    } else {
	                        cellValue = formatter.formatCellValue(cell);
	                    }
	                }

	                PdfPCell pdfCell = new PdfPCell(new Phrase(cellValue));

	                // Handle merged regions
	                for (CellRangeAddress range : mergedRegions) {
	                    if (range.isInRange(rowIndex, colIndex)) {
	                        int rowspan = range.getLastRow() - range.getFirstRow() + 1;
	                        int colspan = range.getLastColumn() - range.getFirstColumn() + 1;
	                        if (rowIndex == range.getFirstRow() && colIndex == range.getFirstColumn()) {
	                            if (rowspan > 1) pdfCell.setRowspan(rowspan);
	                            if (colspan > 1) pdfCell.setColspan(colspan);
	                        } else {
	                            mergedCells.add(cellKey);
	                            pdfCell = null;
	                        }
	                        break;
	                    }
	                }

	                if (pdfCell == null) continue;

	                // Background color (supports .xlsx and .xls)
	                BaseColor bgColor = null;
	                if (cell != null && cell.getCellStyle() != null) {
	                    org.apache.poi.ss.usermodel.Color poiColor = cell.getCellStyle().getFillForegroundColorColor();

	                    if (poiColor instanceof XSSFColor) { // For .xlsx
	                        XSSFColor xssfColor = (XSSFColor) poiColor;
	                        if (xssfColor != null && xssfColor.getRGB() != null) {
	                            byte[] rgb = xssfColor.getRGB();
	                            if (!(rgb[0] == 0 && rgb[1] == 0 && rgb[2] == 0))
	                                bgColor = new BaseColor(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF);
	                        }
	                    } else if (workbook instanceof HSSFWorkbook) { // For .xls
	                        HSSFWorkbook hwb = (HSSFWorkbook) workbook;
	                        short idx = cell.getCellStyle().getFillForegroundColor();
	                        if (idx > 0) {
	                            HSSFPalette palette = hwb.getCustomPalette();
	                            HSSFColor color = palette.getColor(idx);
	                            if (color != null && color.getTriplet() != null) {
	                                short[] rgb = color.getTriplet();
	                                if (!(rgb[0] == 0 && rgb[1] == 0 && rgb[2] == 0))
	                                    bgColor = new BaseColor(rgb[0], rgb[1], rgb[2]);
	                            }
	                        }
	                    }
	                }

	                if (bgColor != null) pdfCell.setBackgroundColor(bgColor);
	                pdfCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	                pdfCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	                pdfCell.setPadding(4f);
	                table.addCell(pdfCell);
	            }
	        }

	        document.add(table);
	        document.close();
	        return pdfOut.toByteArray();
	    }
	}




}
