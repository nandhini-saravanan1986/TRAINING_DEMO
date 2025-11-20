package com.bornfire.brf.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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

import com.bornfire.brf.entities.Brf65_sumtb_Entity;
import com.bornfire.brf.entities.Brf65_sumtb_Repo;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.math.BigDecimal;

@Component
@Service

public class BRF65_ReportService {
	private static final Logger logger = LoggerFactory.getLogger(BRF65_ReportService.class);

	@Autowired
	private Environment env;

	@Autowired
	SessionFactory sessionFactory;

	/*
	 * @Autowired Brf65_dettb_Repo brf65_dettb_Repo;
	 */
	@Autowired
	Brf65_sumtb_Repo brf65_sumtb_Repo;

	SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy");

	public ModelAndView getBRF65View(String reportId, String fromdate, String todate, String currency, String dtltype,
			Pageable pageable) {

		ModelAndView mv = new ModelAndView();
		Session hs = sessionFactory.getCurrentSession();
		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;

		List<Brf65_sumtb_Entity> T1Master = new ArrayList<Brf65_sumtb_Entity>();
		try {
			Date d1 = dateformat.parse(todate);
			// T1rep = t1CurProdServiceRepo.getT1CurProdServices(d1);

			// T1Master = hs.createQuery("from BRF1_REPORT_ENTITY a where a.report_date = ?1
			// ", BRF1_REPORT_ENTITY.class)
			// .setParameter(1, df.parse(todate)).getResultList();
			T1Master = brf65_sumtb_Repo.getdatabydateList(dateformat.parse(todate));

		} catch (ParseException e) {
			e.printStackTrace();
		}
		for (Brf65_sumtb_Entity i : T1Master) {
			System.out.println(i);
		}

		// T1rep = t1CurProdServiceRepo.getT1CurProdServices(d1);

		mv.setViewName("BRRS/BRF65");

		mv.addObject("reportsummary", T1Master);
		// mv.addObject("reportmaster", T1Master);
		mv.addObject("displaymode", "summary");
		// mv.addObject("reportsflag", "reportsflag");
		// mv.addObject("menu", reportId);
		System.out.println("scv" + mv.getViewName());

		return mv;

	}

	/*
	 * public ModelAndView getBRF65currentDtl(String reportId, String fromdate,
	 * String todate, String currency, String dtltype, Pageable pageable, String
	 * filter) {
	 * 
	 * int pageSize = pageable.getPageSize(); int currentPage =
	 * pageable.getPageNumber(); int totalPages = 0;
	 * 
	 * ModelAndView mv = new ModelAndView();
	 * 
	 * Session hs = sessionFactory.getCurrentSession(); List<Brf65_dettb_Entity>
	 * T1Dt1 = new ArrayList<Brf65_dettb_Entity>();
	 * 
	 * try { Date d1 = dateformat.parse(todate); String rowId = null; String
	 * columnId = null; if (filter != null && filter.contains(",")) { String[] parts
	 * = filter.split(","); if (parts.length >= 2) { rowId = parts[0]; columnId =
	 * parts[1]; } }
	 * 
	 * if (rowId != null && columnId != null) { System.out.println("Testing"); T1Dt1
	 * = brf65_dettb_Repo.getdatabydateListrow(dateformat.parse(todate), columnId,
	 * rowId);
	 * 
	 * } else {
	 * 
	 * T1Dt1 = brf65_dettb_Repo.getdatabydateList(d1, currentPage, pageSize);
	 * totalPages = brf65_dettb_Repo.getdatacount(dateformat.parse(todate));
	 * mv.addObject("pagination", "YES");
	 * 
	 * }
	 * 
	 * } catch (ParseException e) { e.printStackTrace(); }
	 * 
	 * // Page<Object> T1Dt1Page = new PageImpl<Object>(pagedlist, //
	 * PageRequest.of(currentPage, pageSize), T1Dt1.size());
	 * 
	 * mv.setViewName("BRRS/BRF65"); mv.addObject("displaymode", "Details");
	 * mv.addObject("currentPage", currentPage); mv.addObject("totalPages", (int)
	 * Math.ceil((double) totalPages / 100)); // mv.addObject("reportdetails",
	 * T1Dt1Page.getContent()); mv.addObject("reportdetails", T1Dt1);
	 * mv.addObject("reportmaster12", T1Dt1); // mv.addObject("reportmaster1", qr);
	 * // mv.addObject("singledetail", new T1CurProdDetail());
	 * mv.addObject("reportsflag", "reportsflag"); mv.addObject("menu", reportId);
	 * return mv; }
	 */
	public byte[] BRF65Excel(String filename, String reportId, String fromdate, String todate, String currency,
			String dtltype) throws Exception {
		logger.info("Service: Starting Excel generation process in memory.");

		List<Brf65_sumtb_Entity> dataList = brf65_sumtb_Repo.getdatabydateList(dateformat.parse(todate));

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

			int startRow = 9;

			if (!dataList.isEmpty()) {
				for (int i = 0; i < dataList.size(); i++) {
					Brf65_sumtb_Entity record = dataList.get(i);
					System.out.println("rownumber=" + startRow + i);
					Row row = sheet.getRow(startRow + i);
					if (row == null) {
						row = sheet.createRow(startRow + i);
					}

					// row10 // Column D
					Cell cell3 = row.createCell(3);
					if (record.getR030_RES_ONCE() != null) {
						cell3.setCellValue(record.getR030_RES_ONCE().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					// row10 // column E
					Cell cell4 = row.createCell(4);
					if (record.getR030_RES_TWI() != null) {
						cell4.setCellValue(record.getR030_RES_TWI().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}

					// row10 //column F
					Cell cell5 = row.createCell(5);
					if (record.getR030_RES_MORE_TWI() != null) {
						cell5.setCellValue(record.getR030_RES_MORE_TWI().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}

					// row10 //column H
					Cell cell7 = row.createCell(7);
					if (record.getR030_PRNC_REG_AMMO_OTP() != null) {
						cell7.setCellValue(record.getR030_PRNC_REG_AMMO_OTP().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}

					// row10 //column I
					Cell cell8 = row.createCell(8);
					if (record.getR030_REPY_AFTR_GRP_EXD_1YR() != null) {
						cell8.setCellValue(record.getR030_REPY_AFTR_GRP_EXD_1YR().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// row10 //column J
					Cell cell9 = row.createCell(9);
					if (record.getR030_BLLT_REPY() != null) {
						cell9.setCellValue(record.getR030_BLLT_REPY().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}

					// row10 //column L
					Cell cell11 = row.createCell(11);
					if (record.getR030_IP_OD_CRDT_TO_PL() != null) {
						cell11.setCellValue(record.getR030_IP_OD_CRDT_TO_PL().doubleValue());
						cell11.setCellStyle(numberStyle);
					} else {
						cell11.setCellValue("");
						cell11.setCellStyle(textStyle);
					}

					// row10 //column M
					Cell cell12 = row.createCell(12);
					if (record.getR030_IP_OD_CRDT_TO_INTR_SUSP() != null) {
						cell12.setCellValue(record.getR030_IP_OD_CRDT_TO_INTR_SUSP().doubleValue());
						cell12.setCellStyle(numberStyle);
					} else {
						cell12.setCellValue("");
						cell12.setCellStyle(textStyle);
					}

					// row10 //column N
					Cell cell13 = row.createCell(13);
					if (record.getR030_SPEC_PROV() != null) {
						cell13.setCellValue(record.getR030_SPEC_PROV().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}

					// row11 //column D
					row = sheet.getRow(10);
					cell3 = row.createCell(3);
					if (record.getR040_RES_ONCE() != null) {
						cell3.setCellValue(record.getR040_RES_ONCE().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					// row11 // column E
					cell4 = row.createCell(4);
					if (record.getR040_RES_TWI() != null) {
						cell4.setCellValue(record.getR040_RES_TWI().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}

					// row11 //column F
					cell5 = row.createCell(5);
					if (record.getR040_RES_MORE_TWI() != null) {
						cell5.setCellValue(record.getR040_RES_MORE_TWI().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}

					// row11 //column H
					cell7 = row.createCell(7);
					if (record.getR040_PRNC_REG_AMMO_OTP() != null) {
						cell7.setCellValue(record.getR040_PRNC_REG_AMMO_OTP().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}

					// row11 //column I
					cell8 = row.createCell(8);
					if (record.getR040_REPY_AFTR_GRP_EXD_1YR() != null) {
						cell8.setCellValue(record.getR040_REPY_AFTR_GRP_EXD_1YR().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// row11 //column J
					cell9 = row.createCell(9);
					if (record.getR040_BLLT_REPY() != null) {
						cell9.setCellValue(record.getR040_BLLT_REPY().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}

					// row11 //column L
					cell11 = row.createCell(11);
					if (record.getR040_IP_OD_CRDT_TO_PL() != null) {
						cell11.setCellValue(record.getR040_IP_OD_CRDT_TO_PL().doubleValue());
						cell11.setCellStyle(numberStyle);
					} else {
						cell11.setCellValue("");
						cell11.setCellStyle(textStyle);
					}

					// row11 //column M
					cell12 = row.createCell(12);
					if (record.getR040_IP_OD_CRDT_TO_INTR_SUSP() != null) {
						cell12.setCellValue(record.getR040_IP_OD_CRDT_TO_INTR_SUSP().doubleValue());
						cell12.setCellStyle(numberStyle);
					} else {
						cell12.setCellValue("");
						cell12.setCellStyle(textStyle);
					}

					// row11 //column N
					cell13 = row.createCell(13);
					if (record.getR040_SPEC_PROV() != null) {
						cell13.setCellValue(record.getR040_SPEC_PROV().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}

					// row12 // Column D
					row = sheet.getRow(11);
					cell3 = row.createCell(3);
					if (record.getR050_RES_ONCE() != null) {
						cell3.setCellValue(record.getR050_RES_ONCE().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					// row12 // column E
					cell4 = row.createCell(4);
					if (record.getR050_RES_TWI() != null) {
						cell4.setCellValue(record.getR050_RES_TWI().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}

					// row12 //column F
					cell5 = row.createCell(5);
					if (record.getR050_RES_MORE_TWI() != null) {
						cell5.setCellValue(record.getR050_RES_MORE_TWI().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}

					// row12 //column H
					cell7 = row.createCell(7);
					if (record.getR050_PRNC_REG_AMMO_OTP() != null) {
						cell7.setCellValue(record.getR050_PRNC_REG_AMMO_OTP().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}

					// row12 //column I
					cell8 = row.createCell(8);
					if (record.getR050_REPY_AFTR_GRP_EXD_1YR() != null) {
						cell8.setCellValue(record.getR050_REPY_AFTR_GRP_EXD_1YR().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// row12 //column J
					cell9 = row.createCell(9);
					if (record.getR050_BLLT_REPY() != null) {
						cell9.setCellValue(record.getR050_BLLT_REPY().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}

					// row12 //column L
					cell11 = row.createCell(11);
					if (record.getR050_IP_OD_CRDT_TO_PL() != null) {
						cell11.setCellValue(record.getR050_IP_OD_CRDT_TO_PL().doubleValue());
						cell11.setCellStyle(numberStyle);
					} else {
						cell11.setCellValue("");
						cell11.setCellStyle(textStyle);
					}

					// row12 //column M
					cell12 = row.createCell(12);
					if (record.getR050_IP_OD_CRDT_TO_INTR_SUSP() != null) {
						cell12.setCellValue(record.getR050_IP_OD_CRDT_TO_INTR_SUSP().doubleValue());
						cell12.setCellStyle(numberStyle);
					} else {
						cell12.setCellValue("");
						cell12.setCellStyle(textStyle);
					}

					// row12 //column N
					cell13 = row.createCell(13);
					if (record.getR050_SPEC_PROV() != null) {
						cell13.setCellValue(record.getR050_SPEC_PROV().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}

					// row14 // Column D
					row = sheet.getRow(13);
					cell3 = row.createCell(3);
					if (record.getR070_RES_ONCE() != null) {
						cell3.setCellValue(record.getR070_RES_ONCE().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					// row14 // column E
					cell4 = row.createCell(4);
					if (record.getR070_RES_TWI() != null) {
						cell4.setCellValue(record.getR070_RES_TWI().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}

					// row14 //column F
					cell5 = row.createCell(5);
					if (record.getR070_RES_MORE_TWI() != null) {
						cell5.setCellValue(record.getR070_RES_MORE_TWI().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}

					// row14 //column H
					cell7 = row.createCell(7);
					if (record.getR070_PRNC_REG_AMMO_OTP() != null) {
						cell7.setCellValue(record.getR070_PRNC_REG_AMMO_OTP().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}

					// row14 //column I
					cell8 = row.createCell(8);
					if (record.getR070_REPY_AFTR_GRP_EXD_1YR() != null) {
						cell8.setCellValue(record.getR070_REPY_AFTR_GRP_EXD_1YR().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// row14 //column J
					cell9 = row.createCell(9);
					if (record.getR070_BLLT_REPY() != null) {
						cell9.setCellValue(record.getR070_BLLT_REPY().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}

					// row14 //column L
					cell11 = row.createCell(11);
					if (record.getR070_IP_OD_CRDT_TO_PL() != null) {
						cell11.setCellValue(record.getR070_IP_OD_CRDT_TO_PL().doubleValue());
						cell11.setCellStyle(numberStyle);
					} else {
						cell11.setCellValue("");
						cell11.setCellStyle(textStyle);
					}

					// row14 //column M
					cell12 = row.createCell(12);
					if (record.getR070_IP_OD_CRDT_TO_INTR_SUSP() != null) {
						cell12.setCellValue(record.getR070_IP_OD_CRDT_TO_INTR_SUSP().doubleValue());
						cell12.setCellStyle(numberStyle);
					} else {
						cell12.setCellValue("");
						cell12.setCellStyle(textStyle);
					}

					// row14 //column N
					cell13 = row.createCell(13);
					if (record.getR070_SPEC_PROV() != null) {
						cell13.setCellValue(record.getR070_SPEC_PROV().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}

					// row15 // Column D
					row = sheet.getRow(14);
					cell3 = row.createCell(3);
					if (record.getR080_RES_ONCE() != null) {
						cell3.setCellValue(record.getR080_RES_ONCE().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					// row15 // column E
					cell4 = row.createCell(4);
					if (record.getR080_RES_TWI() != null) {
						cell4.setCellValue(record.getR080_RES_TWI().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}

					// row15 //column F
					cell5 = row.createCell(5);
					if (record.getR080_RES_MORE_TWI() != null) {
						cell5.setCellValue(record.getR080_RES_MORE_TWI().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}

					// row15 //column H
					cell7 = row.createCell(7);
					if (record.getR080_PRNC_REG_AMMO_OTP() != null) {
						cell7.setCellValue(record.getR080_PRNC_REG_AMMO_OTP().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}

					// row15 //column I
					cell8 = row.createCell(8);
					if (record.getR080_REPY_AFTR_GRP_EXD_1YR() != null) {
						cell8.setCellValue(record.getR080_REPY_AFTR_GRP_EXD_1YR().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// row15 //column J
					cell9 = row.createCell(9);
					if (record.getR080_BLLT_REPY() != null) {
						cell9.setCellValue(record.getR080_BLLT_REPY().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}

					// row15 //column L
					cell11 = row.createCell(11);
					if (record.getR080_IP_OD_CRDT_TO_PL() != null) {
						cell11.setCellValue(record.getR080_IP_OD_CRDT_TO_PL().doubleValue());
						cell11.setCellStyle(numberStyle);
					} else {
						cell11.setCellValue("");
						cell11.setCellStyle(textStyle);
					}

					// row15 //column M
					cell12 = row.createCell(12);
					if (record.getR080_IP_OD_CRDT_TO_INTR_SUSP() != null) {
						cell12.setCellValue(record.getR080_IP_OD_CRDT_TO_INTR_SUSP().doubleValue());
						cell12.setCellStyle(numberStyle);
					} else {
						cell12.setCellValue("");
						cell12.setCellStyle(textStyle);
					}

					// row15 //column N
					cell13 = row.createCell(13);
					if (record.getR080_SPEC_PROV() != null) {
						cell13.setCellValue(record.getR080_SPEC_PROV().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}

					// row16 // Column D
					row = sheet.getRow(15);
					cell3 = row.createCell(3);
					if (record.getR090_RES_ONCE() != null) {
						cell3.setCellValue(record.getR090_RES_ONCE().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					// row16 // column E
					cell4 = row.createCell(4);
					if (record.getR090_RES_TWI() != null) {
						cell4.setCellValue(record.getR090_RES_TWI().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}

					// row16 //column F
					cell5 = row.createCell(5);
					if (record.getR090_RES_MORE_TWI() != null) {
						cell5.setCellValue(record.getR090_RES_MORE_TWI().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}

					// row16 //column H
					cell7 = row.createCell(7);
					if (record.getR090_PRNC_REG_AMMO_OTP() != null) {
						cell7.setCellValue(record.getR090_PRNC_REG_AMMO_OTP().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}

					// row16 //column I
					cell8 = row.createCell(8);
					if (record.getR090_REPY_AFTR_GRP_EXD_1YR() != null) {
						cell8.setCellValue(record.getR090_REPY_AFTR_GRP_EXD_1YR().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// row16 //column J
					cell9 = row.createCell(9);
					if (record.getR090_BLLT_REPY() != null) {
						cell9.setCellValue(record.getR090_BLLT_REPY().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}

					// row16 //column L
					cell11 = row.createCell(11);
					if (record.getR090_IP_OD_CRDT_TO_PL() != null) {
						cell11.setCellValue(record.getR090_IP_OD_CRDT_TO_PL().doubleValue());
						cell11.setCellStyle(numberStyle);
					} else {
						cell11.setCellValue("");
						cell11.setCellStyle(textStyle);
					}

					// row16 //column M
					cell12 = row.createCell(12);
					if (record.getR090_IP_OD_CRDT_TO_INTR_SUSP() != null) {
						cell12.setCellValue(record.getR090_IP_OD_CRDT_TO_INTR_SUSP().doubleValue());
						cell12.setCellStyle(numberStyle);
					} else {
						cell12.setCellValue("");
						cell12.setCellStyle(textStyle);
					}

					// row16 //column N
					cell13 = row.createCell(13);
					if (record.getR090_SPEC_PROV() != null) {
						cell13.setCellValue(record.getR090_SPEC_PROV().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}

					// row17 // Column D
					row = sheet.getRow(16);
					cell3 = row.createCell(3);
					if (record.getR100_RES_ONCE() != null) {
						cell3.setCellValue(record.getR100_RES_ONCE().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					// row17 // column E
					cell4 = row.createCell(4);
					if (record.getR100_RES_TWI() != null) {
						cell4.setCellValue(record.getR100_RES_TWI().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}

					// row17 //column F
					cell5 = row.createCell(5);
					if (record.getR100_RES_MORE_TWI() != null) {
						cell5.setCellValue(record.getR100_RES_MORE_TWI().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}

					// row17 //column H
					cell7 = row.createCell(7);
					if (record.getR100_PRNC_REG_AMMO_OTP() != null) {
						cell7.setCellValue(record.getR100_PRNC_REG_AMMO_OTP().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}

					// row17 //column I
					cell8 = row.createCell(8);
					if (record.getR100_REPY_AFTR_GRP_EXD_1YR() != null) {
						cell8.setCellValue(record.getR100_REPY_AFTR_GRP_EXD_1YR().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// row17 //column J
					cell9 = row.createCell(9);
					if (record.getR100_BLLT_REPY() != null) {
						cell9.setCellValue(record.getR100_BLLT_REPY().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}

					// row17 //column L
					cell11 = row.createCell(11);
					if (record.getR100_IP_OD_CRDT_TO_PL() != null) {
						cell11.setCellValue(record.getR100_IP_OD_CRDT_TO_PL().doubleValue());
						cell11.setCellStyle(numberStyle);
					} else {
						cell11.setCellValue("");
						cell11.setCellStyle(textStyle);
					}

					// row17 //column M
					cell12 = row.createCell(12);
					if (record.getR100_IP_OD_CRDT_TO_INTR_SUSP() != null) {
						cell12.setCellValue(record.getR100_IP_OD_CRDT_TO_INTR_SUSP().doubleValue());
						cell12.setCellStyle(numberStyle);
					} else {
						cell12.setCellValue("");
						cell12.setCellStyle(textStyle);
					}

					// row17 //column N
					cell13 = row.createCell(13);
					if (record.getR100_SPEC_PROV() != null) {
						cell13.setCellValue(record.getR100_SPEC_PROV().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}

					// row19 // Column D
					row = sheet.getRow(18);
					cell3 = row.createCell(3);
					if (record.getR120_RES_ONCE() != null) {
						cell3.setCellValue(record.getR120_RES_ONCE().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					// row19 // column E
					cell4 = row.createCell(4);
					if (record.getR120_RES_TWI() != null) {
						cell4.setCellValue(record.getR120_RES_TWI().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}

					// row19 //column F
					cell5 = row.createCell(5);
					if (record.getR120_RES_MORE_TWI() != null) {
						cell5.setCellValue(record.getR120_RES_MORE_TWI().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}

					// row19 //column H
					cell7 = row.createCell(7);
					if (record.getR120_PRNC_REG_AMMO_OTP() != null) {
						cell7.setCellValue(record.getR120_PRNC_REG_AMMO_OTP().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}

					// row19 //column I
					cell8 = row.createCell(8);
					if (record.getR120_REPY_AFTR_GRP_EXD_1YR() != null) {
						cell8.setCellValue(record.getR120_REPY_AFTR_GRP_EXD_1YR().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// row19 //column J
					cell9 = row.createCell(9);
					if (record.getR120_BLLT_REPY() != null) {
						cell9.setCellValue(record.getR120_BLLT_REPY().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}

					// row19 //column L
					cell11 = row.createCell(11);
					if (record.getR120_IP_OD_CRDT_TO_PL() != null) {
						cell11.setCellValue(record.getR120_IP_OD_CRDT_TO_PL().doubleValue());
						cell11.setCellStyle(numberStyle);
					} else {
						cell11.setCellValue("");
						cell11.setCellStyle(textStyle);
					}

					// row19 //column M
					cell12 = row.createCell(12);
					if (record.getR120_IP_OD_CRDT_TO_INTR_SUSP() != null) {
						cell12.setCellValue(record.getR120_IP_OD_CRDT_TO_INTR_SUSP().doubleValue());
						cell12.setCellStyle(numberStyle);
					} else {
						cell12.setCellValue("");
						cell12.setCellStyle(textStyle);
					}

					// row19 //column N
					cell13 = row.createCell(13);
					if (record.getR120_SPEC_PROV() != null) {
						cell13.setCellValue(record.getR120_SPEC_PROV().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}

					// row20 // Column D
					row = sheet.getRow(19);
					cell3 = row.createCell(3);
					if (record.getR130_RES_ONCE() != null) {
						cell3.setCellValue(record.getR130_RES_ONCE().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					// row20 // column E
					cell4 = row.createCell(4);
					if (record.getR130_RES_TWI() != null) {
						cell4.setCellValue(record.getR130_RES_TWI().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}

					// row20 //column F
					cell5 = row.createCell(5);
					if (record.getR130_RES_MORE_TWI() != null) {
						cell5.setCellValue(record.getR130_RES_MORE_TWI().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}

					// row20 //column H
					cell7 = row.createCell(7);
					if (record.getR130_PRNC_REG_AMMO_OTP() != null) {
						cell7.setCellValue(record.getR130_PRNC_REG_AMMO_OTP().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}

					// row20 //column I
					cell8 = row.createCell(8);
					if (record.getR130_REPY_AFTR_GRP_EXD_1YR() != null) {
						cell8.setCellValue(record.getR130_REPY_AFTR_GRP_EXD_1YR().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// row20 //column J
					cell9 = row.createCell(9);
					if (record.getR130_BLLT_REPY() != null) {
						cell9.setCellValue(record.getR130_BLLT_REPY().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}

					// row20 //column L
					cell11 = row.createCell(11);
					if (record.getR130_IP_OD_CRDT_TO_PL() != null) {
						cell11.setCellValue(record.getR130_IP_OD_CRDT_TO_PL().doubleValue());
						cell11.setCellStyle(numberStyle);
					} else {
						cell11.setCellValue("");
						cell11.setCellStyle(textStyle);
					}

					// row20 //column M
					cell12 = row.createCell(12);
					if (record.getR130_IP_OD_CRDT_TO_INTR_SUSP() != null) {
						cell12.setCellValue(record.getR130_IP_OD_CRDT_TO_INTR_SUSP().doubleValue());
						cell12.setCellStyle(numberStyle);
					} else {
						cell12.setCellValue("");
						cell12.setCellStyle(textStyle);
					}

					// row20 //column N
					cell13 = row.createCell(13);
					if (record.getR130_SPEC_PROV() != null) {
						cell13.setCellValue(record.getR130_SPEC_PROV().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}

					// row21 // Column D
					row = sheet.getRow(20);
					cell3 = row.createCell(3);
					if (record.getR140_RES_ONCE() != null) {
						cell3.setCellValue(record.getR140_RES_ONCE().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					// row21 // column E
					cell4 = row.createCell(4);
					if (record.getR140_RES_TWI() != null) {
						cell4.setCellValue(record.getR140_RES_TWI().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}

					// row21 //column F
					cell5 = row.createCell(5);
					if (record.getR140_RES_MORE_TWI() != null) {
						cell5.setCellValue(record.getR140_RES_MORE_TWI().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}

					// row21 //column H
					cell7 = row.createCell(7);
					if (record.getR140_PRNC_REG_AMMO_OTP() != null) {
						cell7.setCellValue(record.getR140_PRNC_REG_AMMO_OTP().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}

					// row21 //column I
					cell8 = row.createCell(8);
					if (record.getR140_REPY_AFTR_GRP_EXD_1YR() != null) {
						cell8.setCellValue(record.getR140_REPY_AFTR_GRP_EXD_1YR().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// row21 //column J
					cell9 = row.createCell(9);
					if (record.getR140_BLLT_REPY() != null) {
						cell9.setCellValue(record.getR140_BLLT_REPY().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}

					// row21 //column L
					cell11 = row.createCell(11);
					if (record.getR140_IP_OD_CRDT_TO_PL() != null) {
						cell11.setCellValue(record.getR140_IP_OD_CRDT_TO_PL().doubleValue());
						cell11.setCellStyle(numberStyle);
					} else {
						cell11.setCellValue("");
						cell11.setCellStyle(textStyle);
					}

					// row21 //column M
					cell12 = row.createCell(12);
					if (record.getR140_IP_OD_CRDT_TO_INTR_SUSP() != null) {
						cell12.setCellValue(record.getR140_IP_OD_CRDT_TO_INTR_SUSP().doubleValue());
						cell12.setCellStyle(numberStyle);
					} else {
						cell12.setCellValue("");
						cell12.setCellStyle(textStyle);
					}

					// row21 //column N
					cell13 = row.createCell(13);
					if (record.getR140_SPEC_PROV() != null) {
						cell13.setCellValue(record.getR140_SPEC_PROV().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}

					// row22 // Column D
					row = sheet.getRow(21);
					cell3 = row.createCell(3);
					if (record.getR150_RES_ONCE() != null) {
						cell3.setCellValue(record.getR150_RES_ONCE().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					// row22 // column E
					cell4 = row.createCell(4);
					if (record.getR150_RES_TWI() != null) {
						cell4.setCellValue(record.getR150_RES_TWI().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}

					// row22 //column F
					cell5 = row.createCell(5);
					if (record.getR150_RES_MORE_TWI() != null) {
						cell5.setCellValue(record.getR150_RES_MORE_TWI().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}

					// row22 //column H
					cell7 = row.createCell(7);
					if (record.getR150_PRNC_REG_AMMO_OTP() != null) {
						cell7.setCellValue(record.getR150_PRNC_REG_AMMO_OTP().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}

					// row22 //column I
					cell8 = row.createCell(8);
					if (record.getR150_REPY_AFTR_GRP_EXD_1YR() != null) {
						cell8.setCellValue(record.getR150_REPY_AFTR_GRP_EXD_1YR().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// row22 //column J
					cell9 = row.createCell(9);
					if (record.getR150_BLLT_REPY() != null) {
						cell9.setCellValue(record.getR150_BLLT_REPY().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}

					// row22 //column L
					cell11 = row.createCell(11);
					if (record.getR150_IP_OD_CRDT_TO_PL() != null) {
						cell11.setCellValue(record.getR150_IP_OD_CRDT_TO_PL().doubleValue());
						cell11.setCellStyle(numberStyle);
					} else {
						cell11.setCellValue("");
						cell11.setCellStyle(textStyle);
					}

					// row22 //column M
					cell12 = row.createCell(12);
					if (record.getR150_IP_OD_CRDT_TO_INTR_SUSP() != null) {
						cell12.setCellValue(record.getR150_IP_OD_CRDT_TO_INTR_SUSP().doubleValue());
						cell12.setCellStyle(numberStyle);
					} else {
						cell12.setCellValue("");
						cell12.setCellStyle(textStyle);
					}

					// row22 //column N
					cell13 = row.createCell(13);
					if (record.getR150_SPEC_PROV() != null) {
						cell13.setCellValue(record.getR150_SPEC_PROV().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}

					// row23 // Column D
					row = sheet.getRow(22);
					cell3 = row.createCell(3);
					if (record.getR160_RES_ONCE() != null) {
						cell3.setCellValue(record.getR160_RES_ONCE().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					// row23 // column E
					cell4 = row.createCell(4);
					if (record.getR160_RES_TWI() != null) {
						cell4.setCellValue(record.getR160_RES_TWI().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}

					// row23 //column F
					cell5 = row.createCell(5);
					if (record.getR160_RES_MORE_TWI() != null) {
						cell5.setCellValue(record.getR160_RES_MORE_TWI().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}

					// row23 //column H
					cell7 = row.createCell(7);
					if (record.getR160_PRNC_REG_AMMO_OTP() != null) {
						cell7.setCellValue(record.getR160_PRNC_REG_AMMO_OTP().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}

					// row23 //column I
					cell8 = row.createCell(8);
					if (record.getR160_REPY_AFTR_GRP_EXD_1YR() != null) {
						cell8.setCellValue(record.getR160_REPY_AFTR_GRP_EXD_1YR().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// row23 //column J
					cell9 = row.createCell(9);
					if (record.getR160_BLLT_REPY() != null) {
						cell9.setCellValue(record.getR160_BLLT_REPY().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}

					// row23 //column L
					cell11 = row.createCell(11);
					if (record.getR160_IP_OD_CRDT_TO_PL() != null) {
						cell11.setCellValue(record.getR160_IP_OD_CRDT_TO_PL().doubleValue());
						cell11.setCellStyle(numberStyle);
					} else {
						cell11.setCellValue("");
						cell11.setCellStyle(textStyle);
					}

					// row23 //column M
					cell12 = row.createCell(12);
					if (record.getR160_IP_OD_CRDT_TO_INTR_SUSP() != null) {
						cell12.setCellValue(record.getR160_IP_OD_CRDT_TO_INTR_SUSP().doubleValue());
						cell12.setCellStyle(numberStyle);
					} else {
						cell12.setCellValue("");
						cell12.setCellStyle(textStyle);
					}

					// row23 //column N
					cell13 = row.createCell(13);
					if (record.getR160_SPEC_PROV() != null) {
						cell13.setCellValue(record.getR160_SPEC_PROV().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}

					// row24 // Column D
					row = sheet.getRow(23);
					cell3 = row.createCell(3);
					if (record.getR170_RES_ONCE() != null) {
						cell3.setCellValue(record.getR170_RES_ONCE().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					// row24 // column E
					cell4 = row.createCell(4);
					if (record.getR170_RES_TWI() != null) {
						cell4.setCellValue(record.getR170_RES_TWI().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}

					// row24 //column F
					cell5 = row.createCell(5);
					if (record.getR170_RES_MORE_TWI() != null) {
						cell5.setCellValue(record.getR170_RES_MORE_TWI().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}

					// row24 //column H
					cell7 = row.createCell(7);
					if (record.getR170_PRNC_REG_AMMO_OTP() != null) {
						cell7.setCellValue(record.getR170_PRNC_REG_AMMO_OTP().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}

					// row24 //column I
					cell8 = row.createCell(8);
					if (record.getR170_REPY_AFTR_GRP_EXD_1YR() != null) {
						cell8.setCellValue(record.getR170_REPY_AFTR_GRP_EXD_1YR().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// row24 //column J
					cell9 = row.createCell(9);
					if (record.getR170_BLLT_REPY() != null) {
						cell9.setCellValue(record.getR170_BLLT_REPY().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}

					// row24 //column L
					cell11 = row.createCell(11);
					if (record.getR170_IP_OD_CRDT_TO_PL() != null) {
						cell11.setCellValue(record.getR170_IP_OD_CRDT_TO_PL().doubleValue());
						cell11.setCellStyle(numberStyle);
					} else {
						cell11.setCellValue("");
						cell11.setCellStyle(textStyle);
					}

					// row24 //column M
					cell12 = row.createCell(12);
					if (record.getR170_IP_OD_CRDT_TO_INTR_SUSP() != null) {
						cell12.setCellValue(record.getR170_IP_OD_CRDT_TO_INTR_SUSP().doubleValue());
						cell12.setCellStyle(numberStyle);
					} else {
						cell12.setCellValue("");
						cell12.setCellStyle(textStyle);
					}

					// row24 //column N
					cell13 = row.createCell(13);
					if (record.getR170_SPEC_PROV() != null) {
						cell13.setCellValue(record.getR170_SPEC_PROV().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}

					// row25 // Column D
					row = sheet.getRow(24);
					cell3 = row.createCell(3);
					if (record.getR180_RES_ONCE() != null) {
						cell3.setCellValue(record.getR180_RES_ONCE().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					// row25 // column E
					cell4 = row.createCell(4);
					if (record.getR180_RES_TWI() != null) {
						cell4.setCellValue(record.getR180_RES_TWI().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}

					// row25 //column F
					cell5 = row.createCell(5);
					if (record.getR180_RES_MORE_TWI() != null) {
						cell5.setCellValue(record.getR180_RES_MORE_TWI().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}

					// row25 //column H
					cell7 = row.createCell(7);
					if (record.getR180_PRNC_REG_AMMO_OTP() != null) {
						cell7.setCellValue(record.getR180_PRNC_REG_AMMO_OTP().doubleValue());
						cell7.setCellStyle(numberStyle);
					} else {
						cell7.setCellValue("");
						cell7.setCellStyle(textStyle);
					}

					// row25 //column I
					cell8 = row.createCell(8);
					if (record.getR180_REPY_AFTR_GRP_EXD_1YR() != null) {
						cell8.setCellValue(record.getR180_REPY_AFTR_GRP_EXD_1YR().doubleValue());
						cell8.setCellStyle(numberStyle);
					} else {
						cell8.setCellValue("");
						cell8.setCellStyle(textStyle);
					}

					// row25 //column J
					cell9 = row.createCell(9);
					if (record.getR180_BLLT_REPY() != null) {
						cell9.setCellValue(record.getR180_BLLT_REPY().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}

					// row25 //column L
					cell11 = row.createCell(11);
					if (record.getR180_IP_OD_CRDT_TO_PL() != null) {
						cell11.setCellValue(record.getR180_IP_OD_CRDT_TO_PL().doubleValue());
						cell11.setCellStyle(numberStyle);
					} else {
						cell11.setCellValue("");
						cell11.setCellStyle(textStyle);
					}

					// row25 //column M
					cell12 = row.createCell(12);
					if (record.getR180_IP_OD_CRDT_TO_INTR_SUSP() != null) {
						cell12.setCellValue(record.getR180_IP_OD_CRDT_TO_INTR_SUSP().doubleValue());
						cell12.setCellStyle(numberStyle);
					} else {
						cell12.setCellValue("");
						cell12.setCellStyle(textStyle);
					}

					// row25 //column N
					cell13 = row.createCell(13);
					if (record.getR180_SPEC_PROV() != null) {
						cell13.setCellValue(record.getR180_SPEC_PROV().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
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

}