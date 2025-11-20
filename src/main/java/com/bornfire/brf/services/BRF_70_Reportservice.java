package com.bornfire.brf.services;

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

/*import com.bornfire.brf.entities.BRF_70_Detail_Entity;
import com.bornfire.brf.entities.BRF_70_Detail_Repo;*/
import com.bornfire.brf.entities.BRF_70_Summary_Entity;
import com.bornfire.brf.entities.BRF_70_Summary_Repo;
import com.bornfire.brf.entities.M_SFINP2_Summary_Entity;

import java.math.BigDecimal;

@Component
@Service

public class BRF_70_Reportservice {
	private static final Logger logger = LoggerFactory.getLogger(BRF_70_Reportservice.class);

	@Autowired
	private Environment env;

	@Autowired
	SessionFactory sessionFactory;

	/*
	 * @Autowired BRF_70_Detail_Repo BRF_70_DETAIL_Repo;
	 */
	@Autowired
	BRF_70_Summary_Repo BRF_70_Summary_Repo;

	SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy");

	public ModelAndView getBRF_70View(String reportId, String fromdate, String todate, String currency, String dtltype,
			Pageable pageable) {

		ModelAndView mv = new ModelAndView();
		Session hs = sessionFactory.getCurrentSession();
		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;

		List<BRF_70_Summary_Entity> T1Master = new ArrayList<BRF_70_Summary_Entity>();
		try {
			Date d1 = dateformat.parse(todate);
			// T1rep = t1CurProdServiceRepo.getT1CurProdServices(d1);

			// T1Master = hs.createQuery("from BRF1_REPORT_ENTITY a where a.report_date = ?1
			// ", BRF1_REPORT_ENTITY.class)
			// .setParameter(1, df.parse(todate)).getResultList();
			T1Master = BRF_70_Summary_Repo.getdatabydateList(dateformat.parse(todate));

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// T1rep = t1CurProdServiceRepo.getT1CurProdServices(d1);

		mv.setViewName("BRRS/BRF_70");

		mv.addObject("reportsummary", T1Master);
		// mv.addObject("reportmaster", T1Master);
		mv.addObject("displaymode", "summary");
		// mv.addObject("reportsflag", "reportsflag");
		// mv.addObject("menu", reportId);
		System.out.println("scv" + mv.getViewName());

		return mv;

	}

	/*
	 * public ModelAndView getBRF_70currentDtl(String reportId, String fromdate,
	 * String todate, String currency, String dtltype, Pageable pageable, String
	 * filter) {
	 * 
	 * int pageSize = pageable.getPageSize(); int currentPage =
	 * pageable.getPageNumber(); int totalPages = 0;
	 * 
	 * ModelAndView mv = new ModelAndView();
	 * 
	 * Session hs = sessionFactory.getCurrentSession(); List<BRF_70_Detail_Entity>
	 * T1Dt1 = new ArrayList<BRF_70_Detail_Entity>();
	 * 
	 * try { Date d1 = dateformat.parse(todate); String rowId = null; String
	 * columnId = null; if (filter != null && filter.contains(",")) { String[] parts
	 * = filter.split(","); if (parts.length >= 2) { rowId = parts[0]; columnId =
	 * parts[1]; } }
	 * 
	 * if (rowId != null && columnId != null) { System.out.println("Testing"); T1Dt1
	 * = BRF_70_DETAIL_Repo.getdatabydateListrow(dateformat.parse(todate), columnId,
	 * rowId);
	 * 
	 * } else {
	 * 
	 * T1Dt1 = BRF_70_DETAIL_Repo.getdatabydateList(d1, currentPage, pageSize);
	 * totalPages = BRF_70_DETAIL_Repo.getdatacount(dateformat.parse(todate));
	 * mv.addObject("pagination", "YES");
	 * 
	 * }
	 * 
	 * } catch (ParseException e) { e.printStackTrace(); }
	 * 
	 * // Page<Object> T1Dt1Page = new PageImpl<Object>(pagedlist, //
	 * PageRequest.of(currentPage, pageSize), T1Dt1.size());
	 * 
	 * mv.setViewName("BRRS/BRF_70"); System.out.println("11111");
	 * mv.addObject("displaymode", "Details"); mv.addObject("currentPage",
	 * currentPage); mv.addObject("totalPages", (int) Math.ceil((double) totalPages
	 * / 100)); // mv.addObject("reportdetails", T1Dt1Page.getContent());
	 * mv.addObject("reportdetails", T1Dt1); mv.addObject("reportmaster12", T1Dt1);
	 * // mv.addObject("reportmaster1", qr); // mv.addObject("singledetail", new
	 * T1CurProdDetail()); mv.addObject("reportsflag", "reportsflag");
	 * mv.addObject("menu", reportId); return mv; }
	 */

	public byte[] BRF_70Excel(String filename, String reportId, String fromdate, String todate, String currency,
			String dtltype) throws Exception {
		logger.info("Service: Starting Excel generation process in memory.");

		List<BRF_70_Summary_Entity> dataList = BRF_70_Summary_Repo.getdatabydateList(dateformat.parse(todate));

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

			int startRow = 8;

			if (!dataList.isEmpty()) {
				for (int i = 0; i < dataList.size(); i++) {
					BRF_70_Summary_Entity record = dataList.get(i);
					System.out.println("rownumber=" + startRow + i);
					Row row = sheet.getRow(startRow + i);
					if (row == null) {
						row = sheet.createRow(startRow + i);
					}
					Cell cell3 = row.createCell(3);
					if (record.getR0010_MALE_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0010_MALE_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					Cell cell4 = row.createCell(4);
					if (record.getR0010_FEMALE_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0010_FEMALE_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					Cell cell5 = row.createCell(5);
					if (record.getR0010_MALE_OTHER_NAT() != null) {
						cell5.setCellValue(record.getR0010_MALE_OTHER_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					Cell cell6 = row.createCell(6);
					if (record.getR0010_FEMALE_OTHER_NAT() != null) {
						cell6.setCellValue(record.getR0010_FEMALE_OTHER_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
					}
					Cell cell9 = row.createCell(9);
					if (record.getR0010_MALE_POS_LAST_DEC() != null) {
						cell9.setCellValue(record.getR0010_MALE_POS_LAST_DEC().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}
					Cell cell10 = row.createCell(10);
					if (record.getR0010_FEMALE_POS_LAST_DEC() != null) {
						cell10.setCellValue(record.getR0010_FEMALE_POS_LAST_DEC().doubleValue());
						cell10.setCellStyle(numberStyle);
					} else {
						cell10.setCellValue("");
						cell10.setCellStyle(textStyle);
					}
					Cell cell13 = row.createCell(13);
					if (record.getR0010_MALE_UAE_NAT_RESIGNED() != null) {
						cell13.setCellValue(record.getR0010_MALE_UAE_NAT_RESIGNED().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}
					Cell cell14 = row.createCell(14);
					if (record.getR0010_FEMALE_UAE_NAT_RESIGNED() != null) {
						cell14.setCellValue(record.getR0010_FEMALE_UAE_NAT_RESIGNED().doubleValue());
						cell14.setCellStyle(numberStyle);
					} else {
						cell14.setCellValue("");
						cell14.setCellStyle(textStyle);
					}
					Cell cell15 = row.createCell(15);
					if (record.getR0010_MALE_UAE_NAT_JOINED() != null) {
						cell15.setCellValue(record.getR0010_MALE_UAE_NAT_JOINED().doubleValue());
						cell15.setCellStyle(numberStyle);
					} else {
						cell15.setCellValue("");
						cell15.setCellStyle(textStyle);
					}
					Cell cell16 = row.createCell(16);
					if (record.getR0010_FEMALE_UAE_NAT_JOINED() != null) {
						cell16.setCellValue(record.getR0010_FEMALE_OTHER_NAT().doubleValue());
						cell16.setCellStyle(numberStyle);
					} else {
						cell16.setCellValue("");
						cell16.setCellStyle(textStyle);
					}

					row = sheet.getRow(9);

					cell3 = row.createCell(3);
					if (record.getR0020_MALE_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0020_MALE_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					cell4 = row.createCell(4);
					if (record.getR0020_FEMALE_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0020_FEMALE_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					cell5 = row.createCell(5);
					if (record.getR0020_MALE_OTHER_NAT() != null) {
						cell5.setCellValue(record.getR0020_MALE_OTHER_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					cell6 = row.createCell(6);
					if (record.getR0020_FEMALE_OTHER_NAT() != null) {
						cell6.setCellValue(record.getR0020_FEMALE_OTHER_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
					}
					cell9 = row.createCell(9);
					if (record.getR0020_MALE_POS_LAST_DEC() != null) {
						cell9.setCellValue(record.getR0020_MALE_POS_LAST_DEC().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}
					cell10 = row.createCell(10);
					if (record.getR0020_FEMALE_POS_LAST_DEC() != null) {
						cell10.setCellValue(record.getR0020_FEMALE_POS_LAST_DEC().doubleValue());
						cell10.setCellStyle(numberStyle);
					} else {
						cell10.setCellValue("");
						cell10.setCellStyle(textStyle);
					}
					cell13 = row.createCell(13);
					if (record.getR0020_MALE_UAE_NAT_RESIGNED() != null) {
						cell13.setCellValue(record.getR0020_MALE_UAE_NAT_RESIGNED().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}
					cell14 = row.createCell(14);
					if (record.getR0020_FEMALE_UAE_NAT_RESIGNED() != null) {
						cell14.setCellValue(record.getR0020_FEMALE_UAE_NAT_RESIGNED().doubleValue());
						cell14.setCellStyle(numberStyle);
					} else {
						cell14.setCellValue("");
						cell14.setCellStyle(textStyle);
					}
					cell15 = row.createCell(15);
					if (record.getR0020_MALE_UAE_NAT_JOINED() != null) {
						cell15.setCellValue(record.getR0020_MALE_UAE_NAT_JOINED().doubleValue());
						cell15.setCellStyle(numberStyle);
					} else {
						cell15.setCellValue("");
						cell15.setCellStyle(textStyle);
					}
					cell16 = row.createCell(16);
					if (record.getR0020_FEMALE_UAE_NAT_JOINED() != null) {
						cell16.setCellValue(record.getR0020_FEMALE_OTHER_NAT().doubleValue());
						cell16.setCellStyle(numberStyle);
					} else {
						cell16.setCellValue("");
						cell16.setCellStyle(textStyle);
					}

					row = sheet.getRow(10);

					cell3 = row.createCell(3);
					if (record.getR0030_MALE_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0030_MALE_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					cell4 = row.createCell(4);
					if (record.getR0030_FEMALE_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0030_FEMALE_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					cell5 = row.createCell(5);
					if (record.getR0030_MALE_OTHER_NAT() != null) {
						cell5.setCellValue(record.getR0030_MALE_OTHER_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					cell6 = row.createCell(6);
					if (record.getR0030_FEMALE_OTHER_NAT() != null) {
						cell6.setCellValue(record.getR0030_FEMALE_OTHER_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
					}
					cell9 = row.createCell(9);
					if (record.getR0030_MALE_POS_LAST_DEC() != null) {
						cell9.setCellValue(record.getR0030_MALE_POS_LAST_DEC().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}
					cell10 = row.createCell(10);
					if (record.getR0030_FEMALE_POS_LAST_DEC() != null) {
						cell10.setCellValue(record.getR0030_FEMALE_POS_LAST_DEC().doubleValue());
						cell10.setCellStyle(numberStyle);
					} else {
						cell10.setCellValue("");
						cell10.setCellStyle(textStyle);
					}
					cell13 = row.createCell(13);
					if (record.getR0030_MALE_UAE_NAT_RESIGNED() != null) {
						cell13.setCellValue(record.getR0030_MALE_UAE_NAT_RESIGNED().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}
					cell14 = row.createCell(14);
					if (record.getR0030_FEMALE_UAE_NAT_RESIGNED() != null) {
						cell14.setCellValue(record.getR0030_FEMALE_UAE_NAT_RESIGNED().doubleValue());
						cell14.setCellStyle(numberStyle);
					} else {
						cell14.setCellValue("");
						cell14.setCellStyle(textStyle);
					}
					cell15 = row.createCell(15);
					if (record.getR0030_MALE_UAE_NAT_JOINED() != null) {
						cell15.setCellValue(record.getR0030_MALE_UAE_NAT_JOINED().doubleValue());
						cell15.setCellStyle(numberStyle);
					} else {
						cell15.setCellValue("");
						cell15.setCellStyle(textStyle);
					}
					cell16 = row.createCell(16);
					if (record.getR0030_FEMALE_UAE_NAT_JOINED() != null) {
						cell16.setCellValue(record.getR0030_FEMALE_OTHER_NAT().doubleValue());
						cell16.setCellStyle(numberStyle);
					} else {
						cell16.setCellValue("");
						cell16.setCellStyle(textStyle);
					}

					row = sheet.getRow(11);

					cell3 = row.createCell(3);
					if (record.getR0040_MALE_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0040_MALE_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					cell4 = row.createCell(4);
					if (record.getR0040_FEMALE_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0040_FEMALE_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					cell5 = row.createCell(5);
					if (record.getR0040_MALE_OTHER_NAT() != null) {
						cell5.setCellValue(record.getR0040_MALE_OTHER_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					cell6 = row.createCell(6);
					if (record.getR0040_FEMALE_OTHER_NAT() != null) {
						cell6.setCellValue(record.getR0040_FEMALE_OTHER_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
					}
					cell9 = row.createCell(9);
					if (record.getR0040_MALE_POS_LAST_DEC() != null) {
						cell9.setCellValue(record.getR0040_MALE_POS_LAST_DEC().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}
					cell10 = row.createCell(10);
					if (record.getR0040_FEMALE_POS_LAST_DEC() != null) {
						cell10.setCellValue(record.getR0040_FEMALE_POS_LAST_DEC().doubleValue());
						cell10.setCellStyle(numberStyle);
					} else {
						cell10.setCellValue("");
						cell10.setCellStyle(textStyle);
					}
					cell13 = row.createCell(13);
					if (record.getR0040_MALE_UAE_NAT_RESIGNED() != null) {
						cell13.setCellValue(record.getR0040_MALE_UAE_NAT_RESIGNED().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}
					cell14 = row.createCell(14);
					if (record.getR0040_FEMALE_UAE_NAT_RESIGNED() != null) {
						cell14.setCellValue(record.getR0040_FEMALE_UAE_NAT_RESIGNED().doubleValue());
						cell14.setCellStyle(numberStyle);
					} else {
						cell14.setCellValue("");
						cell14.setCellStyle(textStyle);
					}
					cell15 = row.createCell(15);
					if (record.getR0040_MALE_UAE_NAT_JOINED() != null) {
						cell15.setCellValue(record.getR0040_MALE_UAE_NAT_JOINED().doubleValue());
						cell15.setCellStyle(numberStyle);
					} else {
						cell15.setCellValue("");
						cell15.setCellStyle(textStyle);
					}
					cell16 = row.createCell(16);
					if (record.getR0040_FEMALE_UAE_NAT_JOINED() != null) {
						cell16.setCellValue(record.getR0040_FEMALE_OTHER_NAT().doubleValue());
						cell16.setCellStyle(numberStyle);
					} else {
						cell16.setCellValue("");
						cell16.setCellStyle(textStyle);
					}

					row = sheet.getRow(12);

					cell3 = row.createCell(3);
					if (record.getR0050_MALE_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0050_MALE_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					cell4 = row.createCell(4);
					if (record.getR0050_FEMALE_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0050_FEMALE_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					cell5 = row.createCell(5);
					if (record.getR0050_MALE_OTHER_NAT() != null) {
						cell5.setCellValue(record.getR0050_MALE_OTHER_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					cell6 = row.createCell(6);
					if (record.getR0050_FEMALE_OTHER_NAT() != null) {
						cell6.setCellValue(record.getR0050_FEMALE_OTHER_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
					}
					cell9 = row.createCell(9);
					if (record.getR0050_MALE_POS_LAST_DEC() != null) {
						cell9.setCellValue(record.getR0050_MALE_POS_LAST_DEC().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}
					cell10 = row.createCell(10);
					if (record.getR0050_FEMALE_POS_LAST_DEC() != null) {
						cell10.setCellValue(record.getR0050_FEMALE_POS_LAST_DEC().doubleValue());
						cell10.setCellStyle(numberStyle);
					} else {
						cell10.setCellValue("");
						cell10.setCellStyle(textStyle);
					}
					cell13 = row.createCell(13);
					if (record.getR0050_MALE_UAE_NAT_RESIGNED() != null) {
						cell13.setCellValue(record.getR0050_MALE_UAE_NAT_RESIGNED().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}
					cell14 = row.createCell(14);
					if (record.getR0050_FEMALE_UAE_NAT_RESIGNED() != null) {
						cell14.setCellValue(record.getR0050_FEMALE_UAE_NAT_RESIGNED().doubleValue());
						cell14.setCellStyle(numberStyle);
					} else {
						cell14.setCellValue("");
						cell14.setCellStyle(textStyle);
					}
					cell15 = row.createCell(15);
					if (record.getR0050_MALE_UAE_NAT_JOINED() != null) {
						cell15.setCellValue(record.getR0050_MALE_UAE_NAT_JOINED().doubleValue());
						cell15.setCellStyle(numberStyle);
					} else {
						cell15.setCellValue("");
						cell15.setCellStyle(textStyle);
					}
					cell16 = row.createCell(16);
					if (record.getR0050_FEMALE_UAE_NAT_JOINED() != null) {
						cell16.setCellValue(record.getR0050_FEMALE_OTHER_NAT().doubleValue());
						cell16.setCellStyle(numberStyle);
					} else {
						cell16.setCellValue("");
						cell16.setCellStyle(textStyle);
					}

					row = sheet.getRow(13);

					cell3 = row.createCell(3);
					if (record.getR0060_MALE_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0060_MALE_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					cell4 = row.createCell(4);
					if (record.getR0060_FEMALE_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0060_FEMALE_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					cell5 = row.createCell(5);
					if (record.getR0060_MALE_OTHER_NAT() != null) {
						cell5.setCellValue(record.getR0060_MALE_OTHER_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					cell6 = row.createCell(6);
					if (record.getR0060_FEMALE_OTHER_NAT() != null) {
						cell6.setCellValue(record.getR0060_FEMALE_OTHER_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
					}
					cell9 = row.createCell(9);
					if (record.getR0060_MALE_POS_LAST_DEC() != null) {
						cell9.setCellValue(record.getR0060_MALE_POS_LAST_DEC().doubleValue());
						cell9.setCellStyle(numberStyle);
					} else {
						cell9.setCellValue("");
						cell9.setCellStyle(textStyle);
					}
					cell10 = row.createCell(10);
					if (record.getR0060_FEMALE_POS_LAST_DEC() != null) {
						cell10.setCellValue(record.getR0060_FEMALE_POS_LAST_DEC().doubleValue());
						cell10.setCellStyle(numberStyle);
					} else {
						cell10.setCellValue("");
						cell10.setCellStyle(textStyle);
					}
					cell13 = row.createCell(13);
					if (record.getR0060_MALE_UAE_NAT_RESIGNED() != null) {
						cell13.setCellValue(record.getR0060_MALE_UAE_NAT_RESIGNED().doubleValue());
						cell13.setCellStyle(numberStyle);
					} else {
						cell13.setCellValue("");
						cell13.setCellStyle(textStyle);
					}
					cell14 = row.createCell(14);
					if (record.getR0060_FEMALE_UAE_NAT_RESIGNED() != null) {
						cell14.setCellValue(record.getR0060_FEMALE_UAE_NAT_RESIGNED().doubleValue());
						cell14.setCellStyle(numberStyle);
					} else {
						cell14.setCellValue("");
						cell14.setCellStyle(textStyle);
					}
					cell15 = row.createCell(15);
					if (record.getR0060_MALE_UAE_NAT_JOINED() != null) {
						cell15.setCellValue(record.getR0060_MALE_UAE_NAT_JOINED().doubleValue());
						cell15.setCellStyle(numberStyle);
					} else {
						cell15.setCellValue("");
						cell15.setCellStyle(textStyle);
					}
					cell16 = row.createCell(16);
					if (record.getR0060_FEMALE_UAE_NAT_JOINED() != null) {
						cell16.setCellValue(record.getR0060_FEMALE_OTHER_NAT().doubleValue());
						cell16.setCellStyle(numberStyle);
					} else {
						cell16.setCellValue("");
						cell16.setCellStyle(textStyle);
					}

					row = sheet.getRow(15);

					cell3 = row.createCell(3);
					if (record.getR0080_MALE_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0080_MALE_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					cell4 = row.createCell(4);
					if (record.getR0080_FEMALE_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0080_FEMALE_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					cell5 = row.createCell(5);
					if (record.getR0080_MALE_OTHER_NAT() != null) {
						cell5.setCellValue(record.getR0080_MALE_OTHER_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					cell6 = row.createCell(6);
					if (record.getR0080_FEMALE_OTHER_NAT() != null) {
						cell6.setCellValue(record.getR0080_FEMALE_OTHER_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
					}

					row = sheet.getRow(16);

					cell3 = row.createCell(3);
					if (record.getR0090_MALE_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0090_MALE_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					cell4 = row.createCell(4);
					if (record.getR0090_FEMALE_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0090_FEMALE_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					cell5 = row.createCell(5);
					if (record.getR0090_MALE_OTHER_NAT() != null) {
						cell5.setCellValue(record.getR0090_MALE_OTHER_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					cell6 = row.createCell(6);
					if (record.getR0090_FEMALE_OTHER_NAT() != null) {
						cell6.setCellValue(record.getR0090_FEMALE_OTHER_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
					}

					row = sheet.getRow(17);

					cell3 = row.createCell(3);
					if (record.getR0100_MALE_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0100_MALE_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					cell4 = row.createCell(4);
					if (record.getR0100_FEMALE_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0100_FEMALE_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					cell5 = row.createCell(5);
					if (record.getR0100_MALE_OTHER_NAT() != null) {
						cell5.setCellValue(record.getR0100_MALE_OTHER_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					cell6 = row.createCell(6);
					if (record.getR0100_FEMALE_OTHER_NAT() != null) {
						cell6.setCellValue(record.getR0100_FEMALE_OTHER_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
					}

					row = sheet.getRow(18);

					cell3 = row.createCell(3);
					if (record.getR0110_MALE_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0110_MALE_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					cell4 = row.createCell(4);
					if (record.getR0110_FEMALE_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0110_FEMALE_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					cell5 = row.createCell(5);
					if (record.getR0110_MALE_OTHER_NAT() != null) {
						cell5.setCellValue(record.getR0110_MALE_OTHER_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					cell6 = row.createCell(6);
					if (record.getR0110_FEMALE_OTHER_NAT() != null) {
						cell6.setCellValue(record.getR0110_FEMALE_OTHER_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
					}

					row = sheet.getRow(19);

					cell3 = row.createCell(3);
					if (record.getR0120_MALE_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0120_MALE_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					cell4 = row.createCell(4);
					if (record.getR0120_FEMALE_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0120_FEMALE_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					cell5 = row.createCell(5);
					if (record.getR0120_MALE_OTHER_NAT() != null) {
						cell5.setCellValue(record.getR0120_MALE_OTHER_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					cell6 = row.createCell(6);
					if (record.getR0120_FEMALE_OTHER_NAT() != null) {
						cell6.setCellValue(record.getR0120_FEMALE_OTHER_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
					}
					row = sheet.getRow(20);

					cell3 = row.createCell(3);
					if (record.getR0130_MALE_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0130_MALE_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					cell4 = row.createCell(4);
					if (record.getR0130_FEMALE_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0130_FEMALE_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					cell5 = row.createCell(5);
					if (record.getR0130_MALE_OTHER_NAT() != null) {
						cell5.setCellValue(record.getR0130_MALE_OTHER_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					cell6 = row.createCell(6);
					if (record.getR0130_FEMALE_OTHER_NAT() != null) {
						cell6.setCellValue(record.getR0130_FEMALE_OTHER_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
					}

					row = sheet.getRow(21);

					cell3 = row.createCell(3);
					if (record.getR0140_MALE_UAE_NAT() != null) {
						cell3.setCellValue(record.getR0140_MALE_UAE_NAT().doubleValue());
						cell3.setCellStyle(numberStyle);
					} else {
						cell3.setCellValue("");
						cell3.setCellStyle(textStyle);
					}

					cell4 = row.createCell(4);
					if (record.getR0140_FEMALE_UAE_NAT() != null) {
						cell4.setCellValue(record.getR0140_FEMALE_UAE_NAT().doubleValue());
						cell4.setCellStyle(numberStyle);
					} else {
						cell4.setCellValue("");
						cell4.setCellStyle(textStyle);
					}
					cell5 = row.createCell(5);
					if (record.getR0140_MALE_OTHER_NAT() != null) {
						cell5.setCellValue(record.getR0140_MALE_OTHER_NAT().doubleValue());
						cell5.setCellStyle(numberStyle);
					} else {
						cell5.setCellValue("");
						cell5.setCellStyle(textStyle);
					}
					cell6 = row.createCell(6);
					if (record.getR0140_FEMALE_OTHER_NAT() != null) {
						cell6.setCellValue(record.getR0140_FEMALE_OTHER_NAT().doubleValue());
						cell6.setCellStyle(numberStyle);
					} else {
						cell6.setCellValue("");
						cell6.setCellStyle(textStyle);
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

	/*
	 * public byte[] BRF_70DetailExcel(String filename, String fromdate, String
	 * todate) { try { logger.info("Generating Excel for BRF_70 Details...");
	 * System.out.println("came to Detail download service");
	 * 
	 * XSSFWorkbook workbook = new XSSFWorkbook(); XSSFSheet sheet =
	 * workbook.createSheet("BRF_70Details");
	 * 
	 * // Common border style BorderStyle border = BorderStyle.THIN;
	 * 
	 * // Header style (left aligned) CellStyle headerStyle =
	 * workbook.createCellStyle(); Font headerFont = workbook.createFont();
	 * headerFont.setBold(true); headerFont.setFontHeightInPoints((short) 10);
	 * headerStyle.setFont(headerFont);
	 * headerStyle.setAlignment(HorizontalAlignment.LEFT);
	 * headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	 * headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	 * headerStyle.setBorderTop(border); headerStyle.setBorderBottom(border);
	 * headerStyle.setBorderLeft(border); headerStyle.setBorderRight(border);
	 * 
	 * // Right-aligned header style for ACCT BALANCE CellStyle
	 * rightAlignedHeaderStyle = workbook.createCellStyle();
	 * rightAlignedHeaderStyle.cloneStyleFrom(headerStyle);
	 * rightAlignedHeaderStyle.setAlignment(HorizontalAlignment.RIGHT);
	 * 
	 * // Default data style (left aligned) CellStyle dataStyle =
	 * workbook.createCellStyle(); dataStyle.setAlignment(HorizontalAlignment.LEFT);
	 * dataStyle.setBorderTop(border); dataStyle.setBorderBottom(border);
	 * dataStyle.setBorderLeft(border); dataStyle.setBorderRight(border);
	 * 
	 * // ACCT BALANCE style (right aligned with 3 decimals) CellStyle balanceStyle
	 * = workbook.createCellStyle();
	 * balanceStyle.setAlignment(HorizontalAlignment.RIGHT);
	 * balanceStyle.setDataFormat(workbook.createDataFormat().getFormat("0.000"));
	 * balanceStyle.setBorderTop(border); balanceStyle.setBorderBottom(border);
	 * balanceStyle.setBorderLeft(border); balanceStyle.setBorderRight(border);
	 * 
	 * // Header row String[] headers = { "CUST ID", "ACCT NO", "ACCT NAME",
	 * "ACCT BALANCE", "ROWID", "COLUMNID", "REPORT_DATE" };
	 * 
	 * XSSFRow headerRow = sheet.createRow(0); for (int i = 0; i < headers.length;
	 * i++) { Cell cell = headerRow.createCell(i); cell.setCellValue(headers[i]);
	 * 
	 * if (i == 3) { // ACCT BALANCE cell.setCellStyle(rightAlignedHeaderStyle); }
	 * else { cell.setCellStyle(headerStyle); }
	 * 
	 * sheet.setColumnWidth(i, 5000); }
	 * 
	 * // Get data Date parsedToDate = new
	 * SimpleDateFormat("dd/MM/yyyy").parse(todate); List<BRF_70_Detail_Entity>
	 * reportData = BRF_70_DETAIL_Repo.getdatabydateList(parsedToDate);
	 * 
	 * if (reportData != null && !reportData.isEmpty()) { int rowIndex = 1; for
	 * (BRF_70_Detail_Entity item : reportData) { XSSFRow row =
	 * sheet.createRow(rowIndex++);
	 * 
	 * row.createCell(0).setCellValue(item.getCustId());
	 * row.createCell(1).setCellValue(item.getAcctNumber());
	 * row.createCell(2).setCellValue(item.getAcctName());
	 * 
	 * // ACCT BALANCE (right aligned, 3 decimal places) Cell balanceCell =
	 * row.createCell(3); if (item.getAcctBalanceInaed() != null) {
	 * balanceCell.setCellValue(item.getAcctBalanceInaed().doubleValue()); } else {
	 * balanceCell.setCellValue(0.000); } balanceCell.setCellStyle(balanceStyle);
	 * 
	 * row.createCell(4).setCellValue(item.getRowId());
	 * row.createCell(5).setCellValue(item.getColumnId()); row.createCell(6)
	 * .setCellValue(item.getReportDate() != null ? new
	 * SimpleDateFormat("dd-MM-yyyy").format(item.getReportDate()) : "");
	 * 
	 * // Apply data style for all other cells for (int j = 0; j < 7; j++) { if (j
	 * != 3) { row.getCell(j).setCellStyle(dataStyle); } } } } else {
	 * logger.info("No data found for BRF_70 — only header will be written."); }
	 * 
	 * // Write to byte[] ByteArrayOutputStream bos = new ByteArrayOutputStream();
	 * workbook.write(bos); workbook.close();
	 * 
	 * logger.info("Excel generation completed with {} row(s).", reportData != null
	 * ? reportData.size() : 0); return bos.toByteArray();
	 * 
	 * } catch (Exception e) { logger.error("Error generating BRF_70 Excel", e);
	 * return new byte[0]; } }
	 */
	
	

}
