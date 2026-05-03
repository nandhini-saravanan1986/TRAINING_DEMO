package com.bornfire.brf.services;

import java.io.ByteArrayOutputStream; 


 
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.sql.CallableStatement;
import java.sql.Connection;
import javax.sql.DataSource;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.bornfire.brf.entities.BRF_052_ARCHIVE_A_Entity;
import com.bornfire.brf.entities.BRF_052_ARCHIVE_A_Repo;
import com.bornfire.brf.entities.BRF_052_ARCHIVE_B_Entity;
import com.bornfire.brf.entities.BRF_052_ARCHIVE_B_Repo;
import com.bornfire.brf.entities.BRF_052_ARCHIVE_C_Entity;
import com.bornfire.brf.entities.BRF_052_ARCHIVE_C_Repo;
import com.bornfire.brf.entities.BRF_052_ARCHIVE_DETAIL_Entity;
import com.bornfire.brf.entities.BRF_052_ARCHIVE_DETAIL_Repo;

import com.bornfire.brf.entities.BRF_052_A_Entity;
import com.bornfire.brf.entities.BRF_052_A_Repo;
import com.bornfire.brf.entities.BRF_052_B_Entity;
import com.bornfire.brf.entities.BRF_052_B_Repo;
import com.bornfire.brf.entities.BRF_052_C_Entity;
import com.bornfire.brf.entities.BRF_052_C_Repo;
import com.bornfire.brf.entities.BRF_052_Detail_Entity;
import com.bornfire.brf.entities.BRF_052_Detail_Repo;

@Component
@Service
public class BRF_052_ReportService {

    private static final Logger logger = LoggerFactory.getLogger(BRF_052_ReportService.class);

    @Autowired
    private Environment env;

    @Autowired
    BRF_052_A_Repo brf052aRepo;

    @Autowired
    BRF_052_B_Repo brf052bRepo;

    @Autowired
    BRF_052_C_Repo brf052cRepo;
    
    @Autowired
    BRF_052_Detail_Repo brf052DetailRepo;
    
    @Autowired
    BRF_052_ARCHIVE_A_Repo brf052ArchiveARepo;

    @Autowired
    BRF_052_ARCHIVE_B_Repo brf052ArchiveBRepo;

    @Autowired
    BRF_052_ARCHIVE_C_Repo brf052ArchiveCRepo;
    
    @Autowired
    BRF_052_ARCHIVE_DETAIL_Repo brf052ArchiveDetailRepo;
    
    @Autowired
    private DataSource dataSource;

    SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy");
 // ─────────────────────────────────────────────────────────────
    // GENERATE SUMMARY — Executes BRF_052_P to rebuild summary
    // tables A/B/C from current detail data for the given date.
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public String generateSummary(String reportDate) {
        logger.info("BRF_052 generateSummary → reportDate={}", reportDate);
        try {
            Date d1;
            try {
                d1 = new SimpleDateFormat("dd-MMM-yyyy").parse(reportDate);
            } catch (Exception ex) {
                try {
                    d1 = new SimpleDateFormat("dd/MM/yyyy").parse(reportDate);
                } catch (Exception ex2) {
                    d1 = new SimpleDateFormat("dd-MM-yyyy").parse(reportDate);
                }
            }

            // Reformat to DD-MM-YYYY — what BRF_052_P VARCHAR2 param expects
            String procDate = new SimpleDateFormat("dd-MM-yyyy").format(d1);
            logger.info("BRF_052 generateSummary → calling BRF_052_P('{}')", procDate);

            Connection conn = null;
            try {
                conn = dataSource.getConnection();
                conn.setAutoCommit(false);  // let the procedure handle its own COMMIT
                try (CallableStatement cs = conn.prepareCall("{CALL BRF_052_P(?)}")) {
                    cs.setString(1, procDate);
                    cs.execute();
                }
            } finally {
                if (conn != null) {
                    try { conn.close(); } catch (Exception ignored) {}
                }
            }

            logger.info("BRF_052 generateSummary → completed for {}", procDate);
            return "Summary generated successfully for " + procDate;

        } catch (Exception e) {
            logger.error("BRF_052 generateSummary ERROR for date={}", reportDate, e);
            throw new RuntimeException("Failed to generate summary: " + e.getMessage(), e);
        }
    }
 // ─────────────────────────────────────────────────────────────
 // DETAIL VIEW
 // ─────────────────────────────────────────────────────────────
 

 // ─────────────────────────────────────────────────────────────
 // DETAIL VIEW — paginated, 20 rows per page
 // Accepts optional filterRowId + filterColId for cell-click
 // ─────────────────────────────────────────────────────────────
 public ModelAndView getBRF052DetailView(String reportId, String fromdate, String todate,
         String currency, String dtltype, String type,
         int page, int size, String filterRowId, String filterColId) {

     ModelAndView mv = new ModelAndView();

     try {
         Date d1;
         try {
             d1 = dateformat.parse(todate);
         } catch (Exception ex) {
             d1 = new SimpleDateFormat("dd/MM/yyyy").parse(todate);
         }

         Pageable pageable = PageRequest.of(page, size);
         Page<BRF_052_Detail_Entity> resultPage;

         boolean hasFilter = filterRowId != null && !filterRowId.isEmpty()
                          && filterColId != null && !filterColId.isEmpty();

         if (hasFilter) {
             resultPage = brf052DetailRepo.getDataByDateAndFilterPaged(d1, filterRowId, filterColId, pageable);
         } else {
             resultPage = brf052DetailRepo.getDataByDatePaged(d1, pageable);
         }

         logger.info("BRF_052 detail page={} size={} total={} filter={}",
                 page, size, resultPage.getTotalElements(), hasFilter);

         mv.addObject("reportdetails",  resultPage.getContent());
         mv.addObject("currentPage",    resultPage.getNumber());
         mv.addObject("totalPages",     resultPage.getTotalPages());
         mv.addObject("totalElements",  resultPage.getTotalElements());
         mv.addObject("pageSize",       size);
         mv.addObject("filterRowId",    filterRowId != null ? filterRowId : "");
         mv.addObject("filterColId",    filterColId != null ? filterColId : "");

     } catch (Exception e) {
         e.printStackTrace();
         mv.addObject("reportdetails",  new java.util.ArrayList<>());
         mv.addObject("currentPage",    0);
         mv.addObject("totalPages",     0);
         mv.addObject("totalElements",  0L);
         mv.addObject("pageSize",       size);
         mv.addObject("filterRowId",    "");
         mv.addObject("filterColId",    "");
         mv.addObject("error", "Error fetching BRF_052 detail data: " + e.getMessage());
     }

     mv.addObject("displaymode", "detail");
     mv.addObject("archiveMode", false);
     mv.addObject("fromdate",  fromdate != null ? fromdate : todate);
     mv.addObject("todate",    todate);
     mv.addObject("asondate",  todate);
     mv.addObject("currency",  currency != null ? currency : "AED");
     mv.addObject("dtltype",   dtltype  != null ? dtltype  : "report");
     mv.addObject("type",      type     != null ? type     : "RBS");
     mv.setViewName("BRRS/BRF_052");
     return mv;
 }
//─────────────────────────────────────────────────────────────────────────
//DETAIL EXCEL DOWNLOAD
//Builds a plain workbook from BRF_052_Detail_Entity rows for the given date.
//─────────────────────────────────────────────────────────────────────────
public byte[] BRF_052_DetailExcel(String filename, String fromdate, String todate) {
  logger.info("BRF_052_DetailExcel: building detail workbook for todate={}", todate);

  try {
      Date d1;
      try {
          d1 = dateformat.parse(todate);
      } catch (Exception ex) {
          d1 = new SimpleDateFormat("dd/MM/yyyy").parse(todate);
      }

      List<BRF_052_Detail_Entity> detailList = brf052DetailRepo.getDataByDate(d1);

      if (detailList.isEmpty()) {
          logger.warn("BRF_052_DetailExcel: no detail data found for date {}", todate);
          return new byte[0];
      }

   // AFTER — SXSSFWorkbook streams rows, fixed widths replace autoSizeColumn
      try (SXSSFWorkbook workbook = new SXSSFWorkbook(500);       // flush every 500 rows
           ByteArrayOutputStream out = new ByteArrayOutputStream(1024 * 1024)) {

          workbook.setCompressTempFiles(true);                     // compress temp files on disk
          Sheet sheet = workbook.createSheet("BRF_052 Detail");

          // Fixed column widths in chars*256 — replaces autoSizeColumn entirely
          int[] colWidths = {15, 20, 35, 10, 12, 18, 30};
          for (int i = 0; i < colWidths.length; i++)
              sheet.setColumnWidth(i, colWidths[i] * 256);

          // Data rows
          int rowIdx = 1;
          for (BRF_052_Detail_Entity rec : detailList) {
              Row row = sheet.createRow(rowIdx++);
              row.createCell(0).setCellValue(rec.getCustId()        != null ? rec.getCustId()        : "");
              row.createCell(1).setCellValue(rec.getAcctNumber()    != null ? rec.getAcctNumber()    : "");
              row.createCell(2).setCellValue(rec.getAcctName()      != null ? rec.getAcctName()      : "");
              row.createCell(3).setCellValue(rec.getRowId()         != null ? rec.getRowId()         : "");
              row.createCell(4).setCellValue(rec.getColumnId()      != null ? rec.getColumnId()      : "");
              if (rec.getAmountAed() != null)
                  row.createCell(5).setCellValue(rec.getAmountAed().doubleValue());
              row.createCell(6).setCellValue(rec.getReportRemarks() != null ? rec.getReportRemarks() : "");
          }


          workbook.write(out);
          workbook.dispose(); 
          logger.info("BRF_052_DetailExcel: {} bytes, {} rows written", out.size(), detailList.size());
          return out.toByteArray();
      }

  } catch (Exception e) {
      logger.error("BRF_052_DetailExcel error", e);
      return new byte[0];
  }
}
//─────────────────────────────────────────────────────────────────────
//DETAIL EDIT — update a single BRF_052_Detail_Entity record
//─────────────────────────────────────────────────────────────────────
@Transactional
public String updateDetailRecord(String custId, String acctNumber, String acctName,
     String rowId, String columnId, BigDecimal amountAed,
     String reportRemarks, String reportDate, String userId) {

 logger.info("BRF_052 updateDetailRecord → custId={} reportDate={}", custId, reportDate);

 try {
     Date d1;
     try {
         d1 = new SimpleDateFormat("dd-MM-yyyy").parse(reportDate);
     } catch (Exception ex) {
         try {
             d1 = new SimpleDateFormat("dd/MM/yyyy").parse(reportDate);
         } catch (Exception ex2) {
             d1 = dateformat.parse(reportDate);
         }
     }

     /// ✅ Native UPDATE — bypasses JPA cache entirely, safe even with duplicate data
     int updated = brf052DetailRepo.updateDetailRecord(
    	        acctNumber, acctName, amountAed, reportRemarks,
    	        custId, rowId, columnId, d1);

    	if (updated == 0) {
    	    throw new RuntimeException(
    	        "No record found for custId=" + custId
    	        + " rowId=" + rowId + " columnId=" + columnId
    	        + " date=" + reportDate);
    	}

    	logger.info("BRF_052 updateDetailRecord: updated {} row(s) for custId={}", updated, custId);
    	return "Record updated successfully.";

 } catch (Exception e) {
     logger.error("BRF_052 updateDetailRecord ERROR for custId={}", custId, e);
     throw new RuntimeException("Update failed: " + e.getMessage(), e);
 }
}

    // ─────────────────────────────────────────────────────────────────────────
    // SUMMARY VIEW
    // ─────────────────────────────────────────────────────────────────────────
    public ModelAndView getBRF052View(String reportId, String fromdate, String todate,
            String currency, String dtltype, String type) {

        ModelAndView mv = new ModelAndView();
        logger.info("BRF_052 getBRF052View → todate={} fromdate={}", todate, fromdate);

        try {
            Date d1;
            try {
                d1 = dateformat.parse(todate);
            } catch (Exception ex) {
                d1 = new SimpleDateFormat("dd/MM/yyyy").parse(todate);
            }

            List<BRF_052_A_Entity> listA = brf052aRepo.getDataByDate(d1);
            List<BRF_052_B_Entity> listB = brf052bRepo.getDataByDate(d1);
            List<BRF_052_C_Entity> listC = brf052cRepo.getDataByDate(d1);

            logger.info("BRF_052 rows → A:{} B:{} C:{}", listA.size(), listB.size(), listC.size());

            BRF_052_A_Entity recA = listA.isEmpty() ? new BRF_052_A_Entity() : listA.get(0);
            BRF_052_B_Entity recB = listB.isEmpty() ? new BRF_052_B_Entity() : listB.get(0);
            BRF_052_C_Entity recC = listC.isEmpty() ? new BRF_052_C_Entity() : listC.get(0);

            mv.addObject("summaryA", recA);
            mv.addObject("summaryB", recB);
            mv.addObject("summaryC", recC);
            mv.addObject("hasData",  !(listA.isEmpty() && listB.isEmpty() && listC.isEmpty()));

        } catch (Exception e) {
            logger.error("BRF_052 getBRF052View ERROR", e);
            mv.addObject("summaryA", new BRF_052_A_Entity());
            mv.addObject("summaryB", new BRF_052_B_Entity());
            mv.addObject("summaryC", new BRF_052_C_Entity());
            mv.addObject("hasData",  false);
            mv.addObject("error",    "Error fetching BRF_052 data: " + e.getMessage());
        }

        mv.addObject("displaymode", "summary");
        mv.addObject("fromdate",  fromdate  != null ? fromdate  : todate);
        mv.addObject("todate",    todate);
        mv.addObject("asondate",  todate);
        mv.addObject("currency",  currency  != null ? currency  : "AED");
        mv.addObject("dtltype",   dtltype   != null ? dtltype   : "report");
        mv.addObject("type",      type      != null ? type      : "RBS");
        mv.setViewName("BRRS/BRF_052");
        return mv;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SUMMARY EXCEL DOWNLOAD — Source Temp (Template) Approach
    //
    // Template: BRF_052.xlsx  (place in output.exportpathtemp directory)
    //   - Sheet 0 "BRF 52GLOBAL LIAB": contains ALL 86 data rows in one sheet
    //   - Sheet 1 "Country_Code": reference data only — not touched
    //
    // Template layout (Sheet 0):
    //   Excel row 5  (POI 4)  : "As on:" label  → date written to col C (index 2)
    //   Excel row 10 (POI 9)  : R10  GOVERNMENT SECTOR  → data cols D–Z (index 3–25)
    //   Excel row 11 (POI 10) : R11  Demand
    //   ...
    //   Excel row 95 (POI 94) : R95  TOTAL LIABILITIES, CAPITAL & RESERVES
    //
    // POI row formula for any row number N:  poiRow = N - 1
    // Col mapping: index 3=Global, 4=UAE, 5=FC1, 6=FC2, ..., 25=FC21
    // ─────────────────────────────────────────────────────────────────────────
    public byte[] BRF_052_Excel(String filename, String reportId, String fromdate,
            String todate, String currency, String dtltype) {

        logger.info("BRF_052_Excel (TEMPLATE): building workbook for todate={}", todate);

        try {
            Date d1;
            try {
                d1 = dateformat.parse(todate);
            } catch (Exception ex) {
                d1 = new SimpleDateFormat("dd/MM/yyyy").parse(todate);
            }

            List<BRF_052_A_Entity> listA = brf052aRepo.getDataByDate(d1);
            List<BRF_052_B_Entity> listB = brf052bRepo.getDataByDate(d1);
            List<BRF_052_C_Entity> listC = brf052cRepo.getDataByDate(d1);

            if (listA.isEmpty() && listB.isEmpty() && listC.isEmpty()) {
                logger.warn("BRF_052_Excel: no data found for date {}", todate);
                return new byte[0];
            }

            BRF_052_A_Entity recA = listA.isEmpty() ? new BRF_052_A_Entity() : listA.get(0);
            BRF_052_B_Entity recB = listB.isEmpty() ? new BRF_052_B_Entity() : listB.get(0);
            BRF_052_C_Entity recC = listC.isEmpty() ? new BRF_052_C_Entity() : listC.get(0);

            // ── Load template ─────────────────────────────────────────────
            String templateDir      = env.getProperty("output.exportpathtemp");
            String templateFileName = (filename != null && !filename.isEmpty()) ? filename : "BRF_052.xlsx";
            Path   templatePath     = Paths.get(templateDir, templateFileName);

            logger.info("BRF_052_Excel: loading template from {}", templatePath.toAbsolutePath());

            if (!Files.exists(templatePath))
                throw new FileNotFoundException("Template not found: " + templatePath.toAbsolutePath());
            if (!Files.isReadable(templatePath))
                throw new SecurityException("Template not readable: " + templatePath.toAbsolutePath());

            try (InputStream in = Files.newInputStream(templatePath);
                 Workbook workbook = WorkbookFactory.create(in);
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                // ── Sheet 0 is the single report sheet — all rows go here ──
                Sheet sheet = workbook.getSheetAt(0);
                
             // ── Set column widths to fit numbers ─────────────────────────────────
                sheet.setColumnWidth(0,  6  * 256);   // No.
                sheet.setColumnWidth(1,  5  * 256);   // Item no (14, 14.1 etc)
                sheet.setColumnWidth(2,  40 * 256);   // Description label
                sheet.setColumnWidth(3,  15 * 256);   // Global Amount
                sheet.setColumnWidth(4,  15 * 256);   // UAE Amount
                for (int fc = 5; fc <= 25; fc++) {    // FC1 – FC21
                    sheet.setColumnWidth(fc, 13 * 256);
                }

                // ── Page setup ────────────────────────────────────────────
                PrintSetup ps = sheet.getPrintSetup();
                ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
                ps.setLandscape(true);
                ps.setFitWidth((short) 1);
                ps.setFitHeight((short) 0);
                sheet.setFitToPage(true);
                sheet.setAutobreaks(true);
                sheet.setMargin(Sheet.LeftMargin,   0.5);
                sheet.setMargin(Sheet.RightMargin,  0.5);
                sheet.setMargin(Sheet.TopMargin,    0.75);
                sheet.setMargin(Sheet.BottomMargin, 0.75);
                sheet.setMargin(Sheet.HeaderMargin, 0.3);
                sheet.setMargin(Sheet.FooterMargin, 0.3);

                // ── Number style ──────────────────────────────────────────
                CellStyle numStyle = workbook.createCellStyle();
                numStyle.setDataFormat(workbook.getCreationHelper()
                        .createDataFormat().getFormat("#,##0.000"));
                numStyle.setBorderBottom(BorderStyle.THIN);
                numStyle.setBorderTop(BorderStyle.THIN);
                numStyle.setBorderLeft(BorderStyle.THIN);
                numStyle.setBorderRight(BorderStyle.THIN);

                // ── Grey style: cloned from template row 10 col D
                //    Preserves theme fill (theme=0, tint=-0.25 ≈ #BFBFBF)
                //    Used for all parent / header / grand-total rows ────────
                CellStyle greyNumStyle = workbook.createCellStyle();
                greyNumStyle.cloneStyleFrom(sheet.getRow(9).getCell(3).getCellStyle());
                greyNumStyle.setDataFormat(workbook.getCreationHelper()
                        .createDataFormat().getFormat("#,##0.000"));
                greyNumStyle.setBorderBottom(BorderStyle.THIN);
                greyNumStyle.setBorderTop(BorderStyle.THIN);
                greyNumStyle.setBorderLeft(BorderStyle.THIN);
                greyNumStyle.setBorderRight(BorderStyle.THIN);
                // Match font to numStyle so grey and white data cells are the same size
                // (cloneStyleFrom inherits template's 8pt; numStyle uses workbook default 11pt)
                greyNumStyle.setFont(workbook.getFontAt(numStyle.getFontIndex()));

                // ── As on date: Excel row 5 → POI row 4, col C → index 2 ─
                Date reportDate = recA.getReportDate() != null ? recA.getReportDate()
                        : (recB.getReportDate() != null ? recB.getReportDate() : recC.getReportDate());
                if (reportDate != null) {
                    writeText(sheet, 4, 2, new SimpleDateFormat("dd/MM/yyyy").format(reportDate));
                }

                // ── Fill all data rows — POI row = R-number - 1 ──────────
                // TABLE A: R10–R50
                wr(sheet, 9, greyNumStyle,
                    recA.getR10GlobalAmt(),
                    recA.getR10UaeAmt(),
                    recA.getR10Fc1Amt(),
                    recA.getR10Fc2Amt(),
                    recA.getR10Fc3Amt(),
                    recA.getR10Fc4Amt(),
                    recA.getR10Fc5Amt(),
                    recA.getR10Fc6Amt(),
                    recA.getR10Fc7Amt(),
                    recA.getR10Fc8Amt(),
                    recA.getR10Fc9Amt(),
                    recA.getR10Fc10Amt(),
                    recA.getR10Fc11Amt(),
                    recA.getR10Fc12Amt(),
                    recA.getR10Fc13Amt(),
                    recA.getR10Fc14Amt(),
                    recA.getR10Fc15Amt(),
                    recA.getR10Fc16Amt(),
                    recA.getR10Fc17Amt(),
                    recA.getR10Fc18Amt(),
                    recA.getR10Fc19Amt(),
                    recA.getR10Fc20Amt(),
                    recA.getR10Fc21Amt());
                wr(sheet, 10, numStyle,
                    recA.getR11GlobalAmt(),
                    recA.getR11UaeAmt(),
                    recA.getR11Fc1Amt(),
                    recA.getR11Fc2Amt(),
                    recA.getR11Fc3Amt(),
                    recA.getR11Fc4Amt(),
                    recA.getR11Fc5Amt(),
                    recA.getR11Fc6Amt(),
                    recA.getR11Fc7Amt(),
                    recA.getR11Fc8Amt(),
                    recA.getR11Fc9Amt(),
                    recA.getR11Fc10Amt(),
                    recA.getR11Fc11Amt(),
                    recA.getR11Fc12Amt(),
                    recA.getR11Fc13Amt(),
                    recA.getR11Fc14Amt(),
                    recA.getR11Fc15Amt(),
                    recA.getR11Fc16Amt(),
                    recA.getR11Fc17Amt(),
                    recA.getR11Fc18Amt(),
                    recA.getR11Fc19Amt(),
                    recA.getR11Fc20Amt(),
                    recA.getR11Fc21Amt());
                wr(sheet, 11, numStyle,
                    recA.getR12GlobalAmt(),
                    recA.getR12UaeAmt(),
                    recA.getR12Fc1Amt(),
                    recA.getR12Fc2Amt(),
                    recA.getR12Fc3Amt(),
                    recA.getR12Fc4Amt(),
                    recA.getR12Fc5Amt(),
                    recA.getR12Fc6Amt(),
                    recA.getR12Fc7Amt(),
                    recA.getR12Fc8Amt(),
                    recA.getR12Fc9Amt(),
                    recA.getR12Fc10Amt(),
                    recA.getR12Fc11Amt(),
                    recA.getR12Fc12Amt(),
                    recA.getR12Fc13Amt(),
                    recA.getR12Fc14Amt(),
                    recA.getR12Fc15Amt(),
                    recA.getR12Fc16Amt(),
                    recA.getR12Fc17Amt(),
                    recA.getR12Fc18Amt(),
                    recA.getR12Fc19Amt(),
                    recA.getR12Fc20Amt(),
                    recA.getR12Fc21Amt());
                wr(sheet, 12, numStyle,
                    recA.getR13GlobalAmt(),
                    recA.getR13UaeAmt(),
                    recA.getR13Fc1Amt(),
                    recA.getR13Fc2Amt(),
                    recA.getR13Fc3Amt(),
                    recA.getR13Fc4Amt(),
                    recA.getR13Fc5Amt(),
                    recA.getR13Fc6Amt(),
                    recA.getR13Fc7Amt(),
                    recA.getR13Fc8Amt(),
                    recA.getR13Fc9Amt(),
                    recA.getR13Fc10Amt(),
                    recA.getR13Fc11Amt(),
                    recA.getR13Fc12Amt(),
                    recA.getR13Fc13Amt(),
                    recA.getR13Fc14Amt(),
                    recA.getR13Fc15Amt(),
                    recA.getR13Fc16Amt(),
                    recA.getR13Fc17Amt(),
                    recA.getR13Fc18Amt(),
                    recA.getR13Fc19Amt(),
                    recA.getR13Fc20Amt(),
                    recA.getR13Fc21Amt());
                wr(sheet, 13, numStyle,
                    recA.getR14GlobalAmt(),
                    recA.getR14UaeAmt(),
                    recA.getR14Fc1Amt(),
                    recA.getR14Fc2Amt(),
                    recA.getR14Fc3Amt(),
                    recA.getR14Fc4Amt(),
                    recA.getR14Fc5Amt(),
                    recA.getR14Fc6Amt(),
                    recA.getR14Fc7Amt(),
                    recA.getR14Fc8Amt(),
                    recA.getR14Fc9Amt(),
                    recA.getR14Fc10Amt(),
                    recA.getR14Fc11Amt(),
                    recA.getR14Fc12Amt(),
                    recA.getR14Fc13Amt(),
                    recA.getR14Fc14Amt(),
                    recA.getR14Fc15Amt(),
                    recA.getR14Fc16Amt(),
                    recA.getR14Fc17Amt(),
                    recA.getR14Fc18Amt(),
                    recA.getR14Fc19Amt(),
                    recA.getR14Fc20Amt(),
                    recA.getR14Fc21Amt());
                wr(sheet, 14, greyNumStyle,
                    recA.getR15GlobalAmt(),
                    recA.getR15UaeAmt(),
                    recA.getR15Fc1Amt(),
                    recA.getR15Fc2Amt(),
                    recA.getR15Fc3Amt(),
                    recA.getR15Fc4Amt(),
                    recA.getR15Fc5Amt(),
                    recA.getR15Fc6Amt(),
                    recA.getR15Fc7Amt(),
                    recA.getR15Fc8Amt(),
                    recA.getR15Fc9Amt(),
                    recA.getR15Fc10Amt(),
                    recA.getR15Fc11Amt(),
                    recA.getR15Fc12Amt(),
                    recA.getR15Fc13Amt(),
                    recA.getR15Fc14Amt(),
                    recA.getR15Fc15Amt(),
                    recA.getR15Fc16Amt(),
                    recA.getR15Fc17Amt(),
                    recA.getR15Fc18Amt(),
                    recA.getR15Fc19Amt(),
                    recA.getR15Fc20Amt(),
                    recA.getR15Fc21Amt());
                wr(sheet, 15, numStyle,
                    recA.getR16GlobalAmt(),
                    recA.getR16UaeAmt(),
                    recA.getR16Fc1Amt(),
                    recA.getR16Fc2Amt(),
                    recA.getR16Fc3Amt(),
                    recA.getR16Fc4Amt(),
                    recA.getR16Fc5Amt(),
                    recA.getR16Fc6Amt(),
                    recA.getR16Fc7Amt(),
                    recA.getR16Fc8Amt(),
                    recA.getR16Fc9Amt(),
                    recA.getR16Fc10Amt(),
                    recA.getR16Fc11Amt(),
                    recA.getR16Fc12Amt(),
                    recA.getR16Fc13Amt(),
                    recA.getR16Fc14Amt(),
                    recA.getR16Fc15Amt(),
                    recA.getR16Fc16Amt(),
                    recA.getR16Fc17Amt(),
                    recA.getR16Fc18Amt(),
                    recA.getR16Fc19Amt(),
                    recA.getR16Fc20Amt(),
                    recA.getR16Fc21Amt());
                wr(sheet, 16, numStyle,
                    recA.getR17GlobalAmt(),
                    recA.getR17UaeAmt(),
                    recA.getR17Fc1Amt(),
                    recA.getR17Fc2Amt(),
                    recA.getR17Fc3Amt(),
                    recA.getR17Fc4Amt(),
                    recA.getR17Fc5Amt(),
                    recA.getR17Fc6Amt(),
                    recA.getR17Fc7Amt(),
                    recA.getR17Fc8Amt(),
                    recA.getR17Fc9Amt(),
                    recA.getR17Fc10Amt(),
                    recA.getR17Fc11Amt(),
                    recA.getR17Fc12Amt(),
                    recA.getR17Fc13Amt(),
                    recA.getR17Fc14Amt(),
                    recA.getR17Fc15Amt(),
                    recA.getR17Fc16Amt(),
                    recA.getR17Fc17Amt(),
                    recA.getR17Fc18Amt(),
                    recA.getR17Fc19Amt(),
                    recA.getR17Fc20Amt(),
                    recA.getR17Fc21Amt());
                wr(sheet, 17, numStyle,
                    recA.getR18GlobalAmt(),
                    recA.getR18UaeAmt(),
                    recA.getR18Fc1Amt(),
                    recA.getR18Fc2Amt(),
                    recA.getR18Fc3Amt(),
                    recA.getR18Fc4Amt(),
                    recA.getR18Fc5Amt(),
                    recA.getR18Fc6Amt(),
                    recA.getR18Fc7Amt(),
                    recA.getR18Fc8Amt(),
                    recA.getR18Fc9Amt(),
                    recA.getR18Fc10Amt(),
                    recA.getR18Fc11Amt(),
                    recA.getR18Fc12Amt(),
                    recA.getR18Fc13Amt(),
                    recA.getR18Fc14Amt(),
                    recA.getR18Fc15Amt(),
                    recA.getR18Fc16Amt(),
                    recA.getR18Fc17Amt(),
                    recA.getR18Fc18Amt(),
                    recA.getR18Fc19Amt(),
                    recA.getR18Fc20Amt(),
                    recA.getR18Fc21Amt());
                wr(sheet, 18, numStyle,
                    recA.getR19GlobalAmt(),
                    recA.getR19UaeAmt(),
                    recA.getR19Fc1Amt(),
                    recA.getR19Fc2Amt(),
                    recA.getR19Fc3Amt(),
                    recA.getR19Fc4Amt(),
                    recA.getR19Fc5Amt(),
                    recA.getR19Fc6Amt(),
                    recA.getR19Fc7Amt(),
                    recA.getR19Fc8Amt(),
                    recA.getR19Fc9Amt(),
                    recA.getR19Fc10Amt(),
                    recA.getR19Fc11Amt(),
                    recA.getR19Fc12Amt(),
                    recA.getR19Fc13Amt(),
                    recA.getR19Fc14Amt(),
                    recA.getR19Fc15Amt(),
                    recA.getR19Fc16Amt(),
                    recA.getR19Fc17Amt(),
                    recA.getR19Fc18Amt(),
                    recA.getR19Fc19Amt(),
                    recA.getR19Fc20Amt(),
                    recA.getR19Fc21Amt());
                wr(sheet, 19, greyNumStyle,
                    recA.getR20GlobalAmt(),
                    recA.getR20UaeAmt(),
                    recA.getR20Fc1Amt(),
                    recA.getR20Fc2Amt(),
                    recA.getR20Fc3Amt(),
                    recA.getR20Fc4Amt(),
                    recA.getR20Fc5Amt(),
                    recA.getR20Fc6Amt(),
                    recA.getR20Fc7Amt(),
                    recA.getR20Fc8Amt(),
                    recA.getR20Fc9Amt(),
                    recA.getR20Fc10Amt(),
                    recA.getR20Fc11Amt(),
                    recA.getR20Fc12Amt(),
                    recA.getR20Fc13Amt(),
                    recA.getR20Fc14Amt(),
                    recA.getR20Fc15Amt(),
                    recA.getR20Fc16Amt(),
                    recA.getR20Fc17Amt(),
                    recA.getR20Fc18Amt(),
                    recA.getR20Fc19Amt(),
                    recA.getR20Fc20Amt(),
                    recA.getR20Fc21Amt());
                wr(sheet, 20, greyNumStyle,
                    recA.getR21GlobalAmt(),
                    recA.getR21UaeAmt(),
                    recA.getR21Fc1Amt(),
                    recA.getR21Fc2Amt(),
                    recA.getR21Fc3Amt(),
                    recA.getR21Fc4Amt(),
                    recA.getR21Fc5Amt(),
                    recA.getR21Fc6Amt(),
                    recA.getR21Fc7Amt(),
                    recA.getR21Fc8Amt(),
                    recA.getR21Fc9Amt(),
                    recA.getR21Fc10Amt(),
                    recA.getR21Fc11Amt(),
                    recA.getR21Fc12Amt(),
                    recA.getR21Fc13Amt(),
                    recA.getR21Fc14Amt(),
                    recA.getR21Fc15Amt(),
                    recA.getR21Fc16Amt(),
                    recA.getR21Fc17Amt(),
                    recA.getR21Fc18Amt(),
                    recA.getR21Fc19Amt(),
                    recA.getR21Fc20Amt(),
                    recA.getR21Fc21Amt());
                wr(sheet, 21, numStyle,
                    recA.getR22GlobalAmt(),
                    recA.getR22UaeAmt(),
                    recA.getR22Fc1Amt(),
                    recA.getR22Fc2Amt(),
                    recA.getR22Fc3Amt(),
                    recA.getR22Fc4Amt(),
                    recA.getR22Fc5Amt(),
                    recA.getR22Fc6Amt(),
                    recA.getR22Fc7Amt(),
                    recA.getR22Fc8Amt(),
                    recA.getR22Fc9Amt(),
                    recA.getR22Fc10Amt(),
                    recA.getR22Fc11Amt(),
                    recA.getR22Fc12Amt(),
                    recA.getR22Fc13Amt(),
                    recA.getR22Fc14Amt(),
                    recA.getR22Fc15Amt(),
                    recA.getR22Fc16Amt(),
                    recA.getR22Fc17Amt(),
                    recA.getR22Fc18Amt(),
                    recA.getR22Fc19Amt(),
                    recA.getR22Fc20Amt(),
                    recA.getR22Fc21Amt());
                wr(sheet, 22, numStyle,
                    recA.getR23GlobalAmt(),
                    recA.getR23UaeAmt(),
                    recA.getR23Fc1Amt(),
                    recA.getR23Fc2Amt(),
                    recA.getR23Fc3Amt(),
                    recA.getR23Fc4Amt(),
                    recA.getR23Fc5Amt(),
                    recA.getR23Fc6Amt(),
                    recA.getR23Fc7Amt(),
                    recA.getR23Fc8Amt(),
                    recA.getR23Fc9Amt(),
                    recA.getR23Fc10Amt(),
                    recA.getR23Fc11Amt(),
                    recA.getR23Fc12Amt(),
                    recA.getR23Fc13Amt(),
                    recA.getR23Fc14Amt(),
                    recA.getR23Fc15Amt(),
                    recA.getR23Fc16Amt(),
                    recA.getR23Fc17Amt(),
                    recA.getR23Fc18Amt(),
                    recA.getR23Fc19Amt(),
                    recA.getR23Fc20Amt(),
                    recA.getR23Fc21Amt());
                wr(sheet, 23, numStyle,
                    recA.getR24GlobalAmt(),
                    recA.getR24UaeAmt(),
                    recA.getR24Fc1Amt(),
                    recA.getR24Fc2Amt(),
                    recA.getR24Fc3Amt(),
                    recA.getR24Fc4Amt(),
                    recA.getR24Fc5Amt(),
                    recA.getR24Fc6Amt(),
                    recA.getR24Fc7Amt(),
                    recA.getR24Fc8Amt(),
                    recA.getR24Fc9Amt(),
                    recA.getR24Fc10Amt(),
                    recA.getR24Fc11Amt(),
                    recA.getR24Fc12Amt(),
                    recA.getR24Fc13Amt(),
                    recA.getR24Fc14Amt(),
                    recA.getR24Fc15Amt(),
                    recA.getR24Fc16Amt(),
                    recA.getR24Fc17Amt(),
                    recA.getR24Fc18Amt(),
                    recA.getR24Fc19Amt(),
                    recA.getR24Fc20Amt(),
                    recA.getR24Fc21Amt());
                wr(sheet, 24, numStyle,
                    recA.getR25GlobalAmt(),
                    recA.getR25UaeAmt(),
                    recA.getR25Fc1Amt(),
                    recA.getR25Fc2Amt(),
                    recA.getR25Fc3Amt(),
                    recA.getR25Fc4Amt(),
                    recA.getR25Fc5Amt(),
                    recA.getR25Fc6Amt(),
                    recA.getR25Fc7Amt(),
                    recA.getR25Fc8Amt(),
                    recA.getR25Fc9Amt(),
                    recA.getR25Fc10Amt(),
                    recA.getR25Fc11Amt(),
                    recA.getR25Fc12Amt(),
                    recA.getR25Fc13Amt(),
                    recA.getR25Fc14Amt(),
                    recA.getR25Fc15Amt(),
                    recA.getR25Fc16Amt(),
                    recA.getR25Fc17Amt(),
                    recA.getR25Fc18Amt(),
                    recA.getR25Fc19Amt(),
                    recA.getR25Fc20Amt(),
                    recA.getR25Fc21Amt());
                wr(sheet, 25, greyNumStyle,
                    recA.getR26GlobalAmt(),
                    recA.getR26UaeAmt(),
                    recA.getR26Fc1Amt(),
                    recA.getR26Fc2Amt(),
                    recA.getR26Fc3Amt(),
                    recA.getR26Fc4Amt(),
                    recA.getR26Fc5Amt(),
                    recA.getR26Fc6Amt(),
                    recA.getR26Fc7Amt(),
                    recA.getR26Fc8Amt(),
                    recA.getR26Fc9Amt(),
                    recA.getR26Fc10Amt(),
                    recA.getR26Fc11Amt(),
                    recA.getR26Fc12Amt(),
                    recA.getR26Fc13Amt(),
                    recA.getR26Fc14Amt(),
                    recA.getR26Fc15Amt(),
                    recA.getR26Fc16Amt(),
                    recA.getR26Fc17Amt(),
                    recA.getR26Fc18Amt(),
                    recA.getR26Fc19Amt(),
                    recA.getR26Fc20Amt(),
                    recA.getR26Fc21Amt());
                wr(sheet, 26, numStyle,
                    recA.getR27GlobalAmt(),
                    recA.getR27UaeAmt(),
                    recA.getR27Fc1Amt(),
                    recA.getR27Fc2Amt(),
                    recA.getR27Fc3Amt(),
                    recA.getR27Fc4Amt(),
                    recA.getR27Fc5Amt(),
                    recA.getR27Fc6Amt(),
                    recA.getR27Fc7Amt(),
                    recA.getR27Fc8Amt(),
                    recA.getR27Fc9Amt(),
                    recA.getR27Fc10Amt(),
                    recA.getR27Fc11Amt(),
                    recA.getR27Fc12Amt(),
                    recA.getR27Fc13Amt(),
                    recA.getR27Fc14Amt(),
                    recA.getR27Fc15Amt(),
                    recA.getR27Fc16Amt(),
                    recA.getR27Fc17Amt(),
                    recA.getR27Fc18Amt(),
                    recA.getR27Fc19Amt(),
                    recA.getR27Fc20Amt(),
                    recA.getR27Fc21Amt());
                wr(sheet, 27, numStyle,
                    recA.getR28GlobalAmt(),
                    recA.getR28UaeAmt(),
                    recA.getR28Fc1Amt(),
                    recA.getR28Fc2Amt(),
                    recA.getR28Fc3Amt(),
                    recA.getR28Fc4Amt(),
                    recA.getR28Fc5Amt(),
                    recA.getR28Fc6Amt(),
                    recA.getR28Fc7Amt(),
                    recA.getR28Fc8Amt(),
                    recA.getR28Fc9Amt(),
                    recA.getR28Fc10Amt(),
                    recA.getR28Fc11Amt(),
                    recA.getR28Fc12Amt(),
                    recA.getR28Fc13Amt(),
                    recA.getR28Fc14Amt(),
                    recA.getR28Fc15Amt(),
                    recA.getR28Fc16Amt(),
                    recA.getR28Fc17Amt(),
                    recA.getR28Fc18Amt(),
                    recA.getR28Fc19Amt(),
                    recA.getR28Fc20Amt(),
                    recA.getR28Fc21Amt());
                wr(sheet, 28, numStyle,
                    recA.getR29GlobalAmt(),
                    recA.getR29UaeAmt(),
                    recA.getR29Fc1Amt(),
                    recA.getR29Fc2Amt(),
                    recA.getR29Fc3Amt(),
                    recA.getR29Fc4Amt(),
                    recA.getR29Fc5Amt(),
                    recA.getR29Fc6Amt(),
                    recA.getR29Fc7Amt(),
                    recA.getR29Fc8Amt(),
                    recA.getR29Fc9Amt(),
                    recA.getR29Fc10Amt(),
                    recA.getR29Fc11Amt(),
                    recA.getR29Fc12Amt(),
                    recA.getR29Fc13Amt(),
                    recA.getR29Fc14Amt(),
                    recA.getR29Fc15Amt(),
                    recA.getR29Fc16Amt(),
                    recA.getR29Fc17Amt(),
                    recA.getR29Fc18Amt(),
                    recA.getR29Fc19Amt(),
                    recA.getR29Fc20Amt(),
                    recA.getR29Fc21Amt());
                wr(sheet, 29, numStyle,
                    recA.getR30GlobalAmt(),
                    recA.getR30UaeAmt(),
                    recA.getR30Fc1Amt(),
                    recA.getR30Fc2Amt(),
                    recA.getR30Fc3Amt(),
                    recA.getR30Fc4Amt(),
                    recA.getR30Fc5Amt(),
                    recA.getR30Fc6Amt(),
                    recA.getR30Fc7Amt(),
                    recA.getR30Fc8Amt(),
                    recA.getR30Fc9Amt(),
                    recA.getR30Fc10Amt(),
                    recA.getR30Fc11Amt(),
                    recA.getR30Fc12Amt(),
                    recA.getR30Fc13Amt(),
                    recA.getR30Fc14Amt(),
                    recA.getR30Fc15Amt(),
                    recA.getR30Fc16Amt(),
                    recA.getR30Fc17Amt(),
                    recA.getR30Fc18Amt(),
                    recA.getR30Fc19Amt(),
                    recA.getR30Fc20Amt(),
                    recA.getR30Fc21Amt());
                wr(sheet, 30, greyNumStyle,
                    recA.getR31GlobalAmt(),
                    recA.getR31UaeAmt(),
                    recA.getR31Fc1Amt(),
                    recA.getR31Fc2Amt(),
                    recA.getR31Fc3Amt(),
                    recA.getR31Fc4Amt(),
                    recA.getR31Fc5Amt(),
                    recA.getR31Fc6Amt(),
                    recA.getR31Fc7Amt(),
                    recA.getR31Fc8Amt(),
                    recA.getR31Fc9Amt(),
                    recA.getR31Fc10Amt(),
                    recA.getR31Fc11Amt(),
                    recA.getR31Fc12Amt(),
                    recA.getR31Fc13Amt(),
                    recA.getR31Fc14Amt(),
                    recA.getR31Fc15Amt(),
                    recA.getR31Fc16Amt(),
                    recA.getR31Fc17Amt(),
                    recA.getR31Fc18Amt(),
                    recA.getR31Fc19Amt(),
                    recA.getR31Fc20Amt(),
                    recA.getR31Fc21Amt());
                wr(sheet, 31, numStyle,
                    recA.getR32GlobalAmt(),
                    recA.getR32UaeAmt(),
                    recA.getR32Fc1Amt(),
                    recA.getR32Fc2Amt(),
                    recA.getR32Fc3Amt(),
                    recA.getR32Fc4Amt(),
                    recA.getR32Fc5Amt(),
                    recA.getR32Fc6Amt(),
                    recA.getR32Fc7Amt(),
                    recA.getR32Fc8Amt(),
                    recA.getR32Fc9Amt(),
                    recA.getR32Fc10Amt(),
                    recA.getR32Fc11Amt(),
                    recA.getR32Fc12Amt(),
                    recA.getR32Fc13Amt(),
                    recA.getR32Fc14Amt(),
                    recA.getR32Fc15Amt(),
                    recA.getR32Fc16Amt(),
                    recA.getR32Fc17Amt(),
                    recA.getR32Fc18Amt(),
                    recA.getR32Fc19Amt(),
                    recA.getR32Fc20Amt(),
                    recA.getR32Fc21Amt());
                wr(sheet, 32, numStyle,
                    recA.getR33GlobalAmt(),
                    recA.getR33UaeAmt(),
                    recA.getR33Fc1Amt(),
                    recA.getR33Fc2Amt(),
                    recA.getR33Fc3Amt(),
                    recA.getR33Fc4Amt(),
                    recA.getR33Fc5Amt(),
                    recA.getR33Fc6Amt(),
                    recA.getR33Fc7Amt(),
                    recA.getR33Fc8Amt(),
                    recA.getR33Fc9Amt(),
                    recA.getR33Fc10Amt(),
                    recA.getR33Fc11Amt(),
                    recA.getR33Fc12Amt(),
                    recA.getR33Fc13Amt(),
                    recA.getR33Fc14Amt(),
                    recA.getR33Fc15Amt(),
                    recA.getR33Fc16Amt(),
                    recA.getR33Fc17Amt(),
                    recA.getR33Fc18Amt(),
                    recA.getR33Fc19Amt(),
                    recA.getR33Fc20Amt(),
                    recA.getR33Fc21Amt());
                wr(sheet, 33, numStyle,
                    recA.getR34GlobalAmt(),
                    recA.getR34UaeAmt(),
                    recA.getR34Fc1Amt(),
                    recA.getR34Fc2Amt(),
                    recA.getR34Fc3Amt(),
                    recA.getR34Fc4Amt(),
                    recA.getR34Fc5Amt(),
                    recA.getR34Fc6Amt(),
                    recA.getR34Fc7Amt(),
                    recA.getR34Fc8Amt(),
                    recA.getR34Fc9Amt(),
                    recA.getR34Fc10Amt(),
                    recA.getR34Fc11Amt(),
                    recA.getR34Fc12Amt(),
                    recA.getR34Fc13Amt(),
                    recA.getR34Fc14Amt(),
                    recA.getR34Fc15Amt(),
                    recA.getR34Fc16Amt(),
                    recA.getR34Fc17Amt(),
                    recA.getR34Fc18Amt(),
                    recA.getR34Fc19Amt(),
                    recA.getR34Fc20Amt(),
                    recA.getR34Fc21Amt());
                wr(sheet, 34, numStyle,
                    recA.getR35GlobalAmt(),
                    recA.getR35UaeAmt(),
                    recA.getR35Fc1Amt(),
                    recA.getR35Fc2Amt(),
                    recA.getR35Fc3Amt(),
                    recA.getR35Fc4Amt(),
                    recA.getR35Fc5Amt(),
                    recA.getR35Fc6Amt(),
                    recA.getR35Fc7Amt(),
                    recA.getR35Fc8Amt(),
                    recA.getR35Fc9Amt(),
                    recA.getR35Fc10Amt(),
                    recA.getR35Fc11Amt(),
                    recA.getR35Fc12Amt(),
                    recA.getR35Fc13Amt(),
                    recA.getR35Fc14Amt(),
                    recA.getR35Fc15Amt(),
                    recA.getR35Fc16Amt(),
                    recA.getR35Fc17Amt(),
                    recA.getR35Fc18Amt(),
                    recA.getR35Fc19Amt(),
                    recA.getR35Fc20Amt(),
                    recA.getR35Fc21Amt());
                wr(sheet, 35, greyNumStyle,
                    recA.getR36GlobalAmt(),
                    recA.getR36UaeAmt(),
                    recA.getR36Fc1Amt(),
                    recA.getR36Fc2Amt(),
                    recA.getR36Fc3Amt(),
                    recA.getR36Fc4Amt(),
                    recA.getR36Fc5Amt(),
                    recA.getR36Fc6Amt(),
                    recA.getR36Fc7Amt(),
                    recA.getR36Fc8Amt(),
                    recA.getR36Fc9Amt(),
                    recA.getR36Fc10Amt(),
                    recA.getR36Fc11Amt(),
                    recA.getR36Fc12Amt(),
                    recA.getR36Fc13Amt(),
                    recA.getR36Fc14Amt(),
                    recA.getR36Fc15Amt(),
                    recA.getR36Fc16Amt(),
                    recA.getR36Fc17Amt(),
                    recA.getR36Fc18Amt(),
                    recA.getR36Fc19Amt(),
                    recA.getR36Fc20Amt(),
                    recA.getR36Fc21Amt());
                wr(sheet, 36, numStyle,
                    recA.getR37GlobalAmt(),
                    recA.getR37UaeAmt(),
                    recA.getR37Fc1Amt(),
                    recA.getR37Fc2Amt(),
                    recA.getR37Fc3Amt(),
                    recA.getR37Fc4Amt(),
                    recA.getR37Fc5Amt(),
                    recA.getR37Fc6Amt(),
                    recA.getR37Fc7Amt(),
                    recA.getR37Fc8Amt(),
                    recA.getR37Fc9Amt(),
                    recA.getR37Fc10Amt(),
                    recA.getR37Fc11Amt(),
                    recA.getR37Fc12Amt(),
                    recA.getR37Fc13Amt(),
                    recA.getR37Fc14Amt(),
                    recA.getR37Fc15Amt(),
                    recA.getR37Fc16Amt(),
                    recA.getR37Fc17Amt(),
                    recA.getR37Fc18Amt(),
                    recA.getR37Fc19Amt(),
                    recA.getR37Fc20Amt(),
                    recA.getR37Fc21Amt());
                wr(sheet, 37, numStyle,
                    recA.getR38GlobalAmt(),
                    recA.getR38UaeAmt(),
                    recA.getR38Fc1Amt(),
                    recA.getR38Fc2Amt(),
                    recA.getR38Fc3Amt(),
                    recA.getR38Fc4Amt(),
                    recA.getR38Fc5Amt(),
                    recA.getR38Fc6Amt(),
                    recA.getR38Fc7Amt(),
                    recA.getR38Fc8Amt(),
                    recA.getR38Fc9Amt(),
                    recA.getR38Fc10Amt(),
                    recA.getR38Fc11Amt(),
                    recA.getR38Fc12Amt(),
                    recA.getR38Fc13Amt(),
                    recA.getR38Fc14Amt(),
                    recA.getR38Fc15Amt(),
                    recA.getR38Fc16Amt(),
                    recA.getR38Fc17Amt(),
                    recA.getR38Fc18Amt(),
                    recA.getR38Fc19Amt(),
                    recA.getR38Fc20Amt(),
                    recA.getR38Fc21Amt());
                wr(sheet, 38, greyNumStyle,
                    recA.getR39GlobalAmt(),
                    recA.getR39UaeAmt(),
                    recA.getR39Fc1Amt(),
                    recA.getR39Fc2Amt(),
                    recA.getR39Fc3Amt(),
                    recA.getR39Fc4Amt(),
                    recA.getR39Fc5Amt(),
                    recA.getR39Fc6Amt(),
                    recA.getR39Fc7Amt(),
                    recA.getR39Fc8Amt(),
                    recA.getR39Fc9Amt(),
                    recA.getR39Fc10Amt(),
                    recA.getR39Fc11Amt(),
                    recA.getR39Fc12Amt(),
                    recA.getR39Fc13Amt(),
                    recA.getR39Fc14Amt(),
                    recA.getR39Fc15Amt(),
                    recA.getR39Fc16Amt(),
                    recA.getR39Fc17Amt(),
                    recA.getR39Fc18Amt(),
                    recA.getR39Fc19Amt(),
                    recA.getR39Fc20Amt(),
                    recA.getR39Fc21Amt());
                wr(sheet, 39, numStyle,
                    recA.getR40GlobalAmt(),
                    recA.getR40UaeAmt(),
                    recA.getR40Fc1Amt(),
                    recA.getR40Fc2Amt(),
                    recA.getR40Fc3Amt(),
                    recA.getR40Fc4Amt(),
                    recA.getR40Fc5Amt(),
                    recA.getR40Fc6Amt(),
                    recA.getR40Fc7Amt(),
                    recA.getR40Fc8Amt(),
                    recA.getR40Fc9Amt(),
                    recA.getR40Fc10Amt(),
                    recA.getR40Fc11Amt(),
                    recA.getR40Fc12Amt(),
                    recA.getR40Fc13Amt(),
                    recA.getR40Fc14Amt(),
                    recA.getR40Fc15Amt(),
                    recA.getR40Fc16Amt(),
                    recA.getR40Fc17Amt(),
                    recA.getR40Fc18Amt(),
                    recA.getR40Fc19Amt(),
                    recA.getR40Fc20Amt(),
                    recA.getR40Fc21Amt());
                wr(sheet, 40, numStyle,
                    recA.getR41GlobalAmt(),
                    recA.getR41UaeAmt(),
                    recA.getR41Fc1Amt(),
                    recA.getR41Fc2Amt(),
                    recA.getR41Fc3Amt(),
                    recA.getR41Fc4Amt(),
                    recA.getR41Fc5Amt(),
                    recA.getR41Fc6Amt(),
                    recA.getR41Fc7Amt(),
                    recA.getR41Fc8Amt(),
                    recA.getR41Fc9Amt(),
                    recA.getR41Fc10Amt(),
                    recA.getR41Fc11Amt(),
                    recA.getR41Fc12Amt(),
                    recA.getR41Fc13Amt(),
                    recA.getR41Fc14Amt(),
                    recA.getR41Fc15Amt(),
                    recA.getR41Fc16Amt(),
                    recA.getR41Fc17Amt(),
                    recA.getR41Fc18Amt(),
                    recA.getR41Fc19Amt(),
                    recA.getR41Fc20Amt(),
                    recA.getR41Fc21Amt());
                wr(sheet, 41, numStyle,
                    recA.getR42GlobalAmt(),
                    recA.getR42UaeAmt(),
                    recA.getR42Fc1Amt(),
                    recA.getR42Fc2Amt(),
                    recA.getR42Fc3Amt(),
                    recA.getR42Fc4Amt(),
                    recA.getR42Fc5Amt(),
                    recA.getR42Fc6Amt(),
                    recA.getR42Fc7Amt(),
                    recA.getR42Fc8Amt(),
                    recA.getR42Fc9Amt(),
                    recA.getR42Fc10Amt(),
                    recA.getR42Fc11Amt(),
                    recA.getR42Fc12Amt(),
                    recA.getR42Fc13Amt(),
                    recA.getR42Fc14Amt(),
                    recA.getR42Fc15Amt(),
                    recA.getR42Fc16Amt(),
                    recA.getR42Fc17Amt(),
                    recA.getR42Fc18Amt(),
                    recA.getR42Fc19Amt(),
                    recA.getR42Fc20Amt(),
                    recA.getR42Fc21Amt());
                wr(sheet, 42, greyNumStyle,
                    recA.getR43GlobalAmt(),
                    recA.getR43UaeAmt(),
                    recA.getR43Fc1Amt(),
                    recA.getR43Fc2Amt(),
                    recA.getR43Fc3Amt(),
                    recA.getR43Fc4Amt(),
                    recA.getR43Fc5Amt(),
                    recA.getR43Fc6Amt(),
                    recA.getR43Fc7Amt(),
                    recA.getR43Fc8Amt(),
                    recA.getR43Fc9Amt(),
                    recA.getR43Fc10Amt(),
                    recA.getR43Fc11Amt(),
                    recA.getR43Fc12Amt(),
                    recA.getR43Fc13Amt(),
                    recA.getR43Fc14Amt(),
                    recA.getR43Fc15Amt(),
                    recA.getR43Fc16Amt(),
                    recA.getR43Fc17Amt(),
                    recA.getR43Fc18Amt(),
                    recA.getR43Fc19Amt(),
                    recA.getR43Fc20Amt(),
                    recA.getR43Fc21Amt());
                wr(sheet, 43, numStyle,
                    recA.getR44GlobalAmt(),
                    recA.getR44UaeAmt(),
                    recA.getR44Fc1Amt(),
                    recA.getR44Fc2Amt(),
                    recA.getR44Fc3Amt(),
                    recA.getR44Fc4Amt(),
                    recA.getR44Fc5Amt(),
                    recA.getR44Fc6Amt(),
                    recA.getR44Fc7Amt(),
                    recA.getR44Fc8Amt(),
                    recA.getR44Fc9Amt(),
                    recA.getR44Fc10Amt(),
                    recA.getR44Fc11Amt(),
                    recA.getR44Fc12Amt(),
                    recA.getR44Fc13Amt(),
                    recA.getR44Fc14Amt(),
                    recA.getR44Fc15Amt(),
                    recA.getR44Fc16Amt(),
                    recA.getR44Fc17Amt(),
                    recA.getR44Fc18Amt(),
                    recA.getR44Fc19Amt(),
                    recA.getR44Fc20Amt(),
                    recA.getR44Fc21Amt());
                wr(sheet, 44, numStyle,
                    recA.getR45GlobalAmt(),
                    recA.getR45UaeAmt(),
                    recA.getR45Fc1Amt(),
                    recA.getR45Fc2Amt(),
                    recA.getR45Fc3Amt(),
                    recA.getR45Fc4Amt(),
                    recA.getR45Fc5Amt(),
                    recA.getR45Fc6Amt(),
                    recA.getR45Fc7Amt(),
                    recA.getR45Fc8Amt(),
                    recA.getR45Fc9Amt(),
                    recA.getR45Fc10Amt(),
                    recA.getR45Fc11Amt(),
                    recA.getR45Fc12Amt(),
                    recA.getR45Fc13Amt(),
                    recA.getR45Fc14Amt(),
                    recA.getR45Fc15Amt(),
                    recA.getR45Fc16Amt(),
                    recA.getR45Fc17Amt(),
                    recA.getR45Fc18Amt(),
                    recA.getR45Fc19Amt(),
                    recA.getR45Fc20Amt(),
                    recA.getR45Fc21Amt());
                wr(sheet, 45, greyNumStyle,
                    recA.getR46GlobalAmt(),
                    recA.getR46UaeAmt(),
                    recA.getR46Fc1Amt(),
                    recA.getR46Fc2Amt(),
                    recA.getR46Fc3Amt(),
                    recA.getR46Fc4Amt(),
                    recA.getR46Fc5Amt(),
                    recA.getR46Fc6Amt(),
                    recA.getR46Fc7Amt(),
                    recA.getR46Fc8Amt(),
                    recA.getR46Fc9Amt(),
                    recA.getR46Fc10Amt(),
                    recA.getR46Fc11Amt(),
                    recA.getR46Fc12Amt(),
                    recA.getR46Fc13Amt(),
                    recA.getR46Fc14Amt(),
                    recA.getR46Fc15Amt(),
                    recA.getR46Fc16Amt(),
                    recA.getR46Fc17Amt(),
                    recA.getR46Fc18Amt(),
                    recA.getR46Fc19Amt(),
                    recA.getR46Fc20Amt(),
                    recA.getR46Fc21Amt());
                wr(sheet, 46, numStyle,
                    recA.getR47GlobalAmt(),
                    recA.getR47UaeAmt(),
                    recA.getR47Fc1Amt(),
                    recA.getR47Fc2Amt(),
                    recA.getR47Fc3Amt(),
                    recA.getR47Fc4Amt(),
                    recA.getR47Fc5Amt(),
                    recA.getR47Fc6Amt(),
                    recA.getR47Fc7Amt(),
                    recA.getR47Fc8Amt(),
                    recA.getR47Fc9Amt(),
                    recA.getR47Fc10Amt(),
                    recA.getR47Fc11Amt(),
                    recA.getR47Fc12Amt(),
                    recA.getR47Fc13Amt(),
                    recA.getR47Fc14Amt(),
                    recA.getR47Fc15Amt(),
                    recA.getR47Fc16Amt(),
                    recA.getR47Fc17Amt(),
                    recA.getR47Fc18Amt(),
                    recA.getR47Fc19Amt(),
                    recA.getR47Fc20Amt(),
                    recA.getR47Fc21Amt());
                wr(sheet, 47, numStyle,
                    recA.getR48GlobalAmt(),
                    recA.getR48UaeAmt(),
                    recA.getR48Fc1Amt(),
                    recA.getR48Fc2Amt(),
                    recA.getR48Fc3Amt(),
                    recA.getR48Fc4Amt(),
                    recA.getR48Fc5Amt(),
                    recA.getR48Fc6Amt(),
                    recA.getR48Fc7Amt(),
                    recA.getR48Fc8Amt(),
                    recA.getR48Fc9Amt(),
                    recA.getR48Fc10Amt(),
                    recA.getR48Fc11Amt(),
                    recA.getR48Fc12Amt(),
                    recA.getR48Fc13Amt(),
                    recA.getR48Fc14Amt(),
                    recA.getR48Fc15Amt(),
                    recA.getR48Fc16Amt(),
                    recA.getR48Fc17Amt(),
                    recA.getR48Fc18Amt(),
                    recA.getR48Fc19Amt(),
                    recA.getR48Fc20Amt(),
                    recA.getR48Fc21Amt());
                wr(sheet, 48, numStyle,
                    recA.getR49GlobalAmt(),
                    recA.getR49UaeAmt(),
                    recA.getR49Fc1Amt(),
                    recA.getR49Fc2Amt(),
                    recA.getR49Fc3Amt(),
                    recA.getR49Fc4Amt(),
                    recA.getR49Fc5Amt(),
                    recA.getR49Fc6Amt(),
                    recA.getR49Fc7Amt(),
                    recA.getR49Fc8Amt(),
                    recA.getR49Fc9Amt(),
                    recA.getR49Fc10Amt(),
                    recA.getR49Fc11Amt(),
                    recA.getR49Fc12Amt(),
                    recA.getR49Fc13Amt(),
                    recA.getR49Fc14Amt(),
                    recA.getR49Fc15Amt(),
                    recA.getR49Fc16Amt(),
                    recA.getR49Fc17Amt(),
                    recA.getR49Fc18Amt(),
                    recA.getR49Fc19Amt(),
                    recA.getR49Fc20Amt(),
                    recA.getR49Fc21Amt());
                wr(sheet, 49, numStyle,
                    recA.getR50GlobalAmt(),
                    recA.getR50UaeAmt(),
                    recA.getR50Fc1Amt(),
                    recA.getR50Fc2Amt(),
                    recA.getR50Fc3Amt(),
                    recA.getR50Fc4Amt(),
                    recA.getR50Fc5Amt(),
                    recA.getR50Fc6Amt(),
                    recA.getR50Fc7Amt(),
                    recA.getR50Fc8Amt(),
                    recA.getR50Fc9Amt(),
                    recA.getR50Fc10Amt(),
                    recA.getR50Fc11Amt(),
                    recA.getR50Fc12Amt(),
                    recA.getR50Fc13Amt(),
                    recA.getR50Fc14Amt(),
                    recA.getR50Fc15Amt(),
                    recA.getR50Fc16Amt(),
                    recA.getR50Fc17Amt(),
                    recA.getR50Fc18Amt(),
                    recA.getR50Fc19Amt(),
                    recA.getR50Fc20Amt(),
                    recA.getR50Fc21Amt());

                // TABLE B: R51–R91
                wr(sheet, 50, greyNumStyle,
                    recB.getR51GlobalAmt(),
                    recB.getR51UaeAmt(),
                    recB.getR51Fc1Amt(),
                    recB.getR51Fc2Amt(),
                    recB.getR51Fc3Amt(),
                    recB.getR51Fc4Amt(),
                    recB.getR51Fc5Amt(),
                    recB.getR51Fc6Amt(),
                    recB.getR51Fc7Amt(),
                    recB.getR51Fc8Amt(),
                    recB.getR51Fc9Amt(),
                    recB.getR51Fc10Amt(),
                    recB.getR51Fc11Amt(),
                    recB.getR51Fc12Amt(),
                    recB.getR51Fc13Amt(),
                    recB.getR51Fc14Amt(),
                    recB.getR51Fc15Amt(),
                    recB.getR51Fc16Amt(),
                    recB.getR51Fc17Amt(),
                    recB.getR51Fc18Amt(),
                    recB.getR51Fc19Amt(),
                    recB.getR51Fc20Amt(),
                    recB.getR51Fc21Amt());
                wr(sheet, 51, numStyle,
                    recB.getR52GlobalAmt(),
                    recB.getR52UaeAmt(),
                    recB.getR52Fc1Amt(),
                    recB.getR52Fc2Amt(),
                    recB.getR52Fc3Amt(),
                    recB.getR52Fc4Amt(),
                    recB.getR52Fc5Amt(),
                    recB.getR52Fc6Amt(),
                    recB.getR52Fc7Amt(),
                    recB.getR52Fc8Amt(),
                    recB.getR52Fc9Amt(),
                    recB.getR52Fc10Amt(),
                    recB.getR52Fc11Amt(),
                    recB.getR52Fc12Amt(),
                    recB.getR52Fc13Amt(),
                    recB.getR52Fc14Amt(),
                    recB.getR52Fc15Amt(),
                    recB.getR52Fc16Amt(),
                    recB.getR52Fc17Amt(),
                    recB.getR52Fc18Amt(),
                    recB.getR52Fc19Amt(),
                    recB.getR52Fc20Amt(),
                    recB.getR52Fc21Amt());
                wr(sheet, 52, numStyle,
                    recB.getR53GlobalAmt(),
                    recB.getR53UaeAmt(),
                    recB.getR53Fc1Amt(),
                    recB.getR53Fc2Amt(),
                    recB.getR53Fc3Amt(),
                    recB.getR53Fc4Amt(),
                    recB.getR53Fc5Amt(),
                    recB.getR53Fc6Amt(),
                    recB.getR53Fc7Amt(),
                    recB.getR53Fc8Amt(),
                    recB.getR53Fc9Amt(),
                    recB.getR53Fc10Amt(),
                    recB.getR53Fc11Amt(),
                    recB.getR53Fc12Amt(),
                    recB.getR53Fc13Amt(),
                    recB.getR53Fc14Amt(),
                    recB.getR53Fc15Amt(),
                    recB.getR53Fc16Amt(),
                    recB.getR53Fc17Amt(),
                    recB.getR53Fc18Amt(),
                    recB.getR53Fc19Amt(),
                    recB.getR53Fc20Amt(),
                    recB.getR53Fc21Amt());
                wr(sheet, 53, numStyle,
                    recB.getR54GlobalAmt(),
                    recB.getR54UaeAmt(),
                    recB.getR54Fc1Amt(),
                    recB.getR54Fc2Amt(),
                    recB.getR54Fc3Amt(),
                    recB.getR54Fc4Amt(),
                    recB.getR54Fc5Amt(),
                    recB.getR54Fc6Amt(),
                    recB.getR54Fc7Amt(),
                    recB.getR54Fc8Amt(),
                    recB.getR54Fc9Amt(),
                    recB.getR54Fc10Amt(),
                    recB.getR54Fc11Amt(),
                    recB.getR54Fc12Amt(),
                    recB.getR54Fc13Amt(),
                    recB.getR54Fc14Amt(),
                    recB.getR54Fc15Amt(),
                    recB.getR54Fc16Amt(),
                    recB.getR54Fc17Amt(),
                    recB.getR54Fc18Amt(),
                    recB.getR54Fc19Amt(),
                    recB.getR54Fc20Amt(),
                    recB.getR54Fc21Amt());
                wr(sheet, 54, greyNumStyle,
                    recB.getR55GlobalAmt(),
                    recB.getR55UaeAmt(),
                    recB.getR55Fc1Amt(),
                    recB.getR55Fc2Amt(),
                    recB.getR55Fc3Amt(),
                    recB.getR55Fc4Amt(),
                    recB.getR55Fc5Amt(),
                    recB.getR55Fc6Amt(),
                    recB.getR55Fc7Amt(),
                    recB.getR55Fc8Amt(),
                    recB.getR55Fc9Amt(),
                    recB.getR55Fc10Amt(),
                    recB.getR55Fc11Amt(),
                    recB.getR55Fc12Amt(),
                    recB.getR55Fc13Amt(),
                    recB.getR55Fc14Amt(),
                    recB.getR55Fc15Amt(),
                    recB.getR55Fc16Amt(),
                    recB.getR55Fc17Amt(),
                    recB.getR55Fc18Amt(),
                    recB.getR55Fc19Amt(),
                    recB.getR55Fc20Amt(),
                    recB.getR55Fc21Amt());
                wr(sheet, 55, numStyle,
                    recB.getR56GlobalAmt(),
                    recB.getR56UaeAmt(),
                    recB.getR56Fc1Amt(),
                    recB.getR56Fc2Amt(),
                    recB.getR56Fc3Amt(),
                    recB.getR56Fc4Amt(),
                    recB.getR56Fc5Amt(),
                    recB.getR56Fc6Amt(),
                    recB.getR56Fc7Amt(),
                    recB.getR56Fc8Amt(),
                    recB.getR56Fc9Amt(),
                    recB.getR56Fc10Amt(),
                    recB.getR56Fc11Amt(),
                    recB.getR56Fc12Amt(),
                    recB.getR56Fc13Amt(),
                    recB.getR56Fc14Amt(),
                    recB.getR56Fc15Amt(),
                    recB.getR56Fc16Amt(),
                    recB.getR56Fc17Amt(),
                    recB.getR56Fc18Amt(),
                    recB.getR56Fc19Amt(),
                    recB.getR56Fc20Amt(),
                    recB.getR56Fc21Amt());
                wr(sheet, 56, greyNumStyle,
                    recB.getR57GlobalAmt(),
                    recB.getR57UaeAmt(),
                    recB.getR57Fc1Amt(),
                    recB.getR57Fc2Amt(),
                    recB.getR57Fc3Amt(),
                    recB.getR57Fc4Amt(),
                    recB.getR57Fc5Amt(),
                    recB.getR57Fc6Amt(),
                    recB.getR57Fc7Amt(),
                    recB.getR57Fc8Amt(),
                    recB.getR57Fc9Amt(),
                    recB.getR57Fc10Amt(),
                    recB.getR57Fc11Amt(),
                    recB.getR57Fc12Amt(),
                    recB.getR57Fc13Amt(),
                    recB.getR57Fc14Amt(),
                    recB.getR57Fc15Amt(),
                    recB.getR57Fc16Amt(),
                    recB.getR57Fc17Amt(),
                    recB.getR57Fc18Amt(),
                    recB.getR57Fc19Amt(),
                    recB.getR57Fc20Amt(),
                    recB.getR57Fc21Amt());
                wr(sheet, 57, numStyle,
                    recB.getR58GlobalAmt(),
                    recB.getR58UaeAmt(),
                    recB.getR58Fc1Amt(),
                    recB.getR58Fc2Amt(),
                    recB.getR58Fc3Amt(),
                    recB.getR58Fc4Amt(),
                    recB.getR58Fc5Amt(),
                    recB.getR58Fc6Amt(),
                    recB.getR58Fc7Amt(),
                    recB.getR58Fc8Amt(),
                    recB.getR58Fc9Amt(),
                    recB.getR58Fc10Amt(),
                    recB.getR58Fc11Amt(),
                    recB.getR58Fc12Amt(),
                    recB.getR58Fc13Amt(),
                    recB.getR58Fc14Amt(),
                    recB.getR58Fc15Amt(),
                    recB.getR58Fc16Amt(),
                    recB.getR58Fc17Amt(),
                    recB.getR58Fc18Amt(),
                    recB.getR58Fc19Amt(),
                    recB.getR58Fc20Amt(),
                    recB.getR58Fc21Amt());
                wr(sheet, 58, numStyle,
                    recB.getR59GlobalAmt(),
                    recB.getR59UaeAmt(),
                    recB.getR59Fc1Amt(),
                    recB.getR59Fc2Amt(),
                    recB.getR59Fc3Amt(),
                    recB.getR59Fc4Amt(),
                    recB.getR59Fc5Amt(),
                    recB.getR59Fc6Amt(),
                    recB.getR59Fc7Amt(),
                    recB.getR59Fc8Amt(),
                    recB.getR59Fc9Amt(),
                    recB.getR59Fc10Amt(),
                    recB.getR59Fc11Amt(),
                    recB.getR59Fc12Amt(),
                    recB.getR59Fc13Amt(),
                    recB.getR59Fc14Amt(),
                    recB.getR59Fc15Amt(),
                    recB.getR59Fc16Amt(),
                    recB.getR59Fc17Amt(),
                    recB.getR59Fc18Amt(),
                    recB.getR59Fc19Amt(),
                    recB.getR59Fc20Amt(),
                    recB.getR59Fc21Amt());
                wr(sheet, 59, numStyle,
                    recB.getR60GlobalAmt(),
                    recB.getR60UaeAmt(),
                    recB.getR60Fc1Amt(),
                    recB.getR60Fc2Amt(),
                    recB.getR60Fc3Amt(),
                    recB.getR60Fc4Amt(),
                    recB.getR60Fc5Amt(),
                    recB.getR60Fc6Amt(),
                    recB.getR60Fc7Amt(),
                    recB.getR60Fc8Amt(),
                    recB.getR60Fc9Amt(),
                    recB.getR60Fc10Amt(),
                    recB.getR60Fc11Amt(),
                    recB.getR60Fc12Amt(),
                    recB.getR60Fc13Amt(),
                    recB.getR60Fc14Amt(),
                    recB.getR60Fc15Amt(),
                    recB.getR60Fc16Amt(),
                    recB.getR60Fc17Amt(),
                    recB.getR60Fc18Amt(),
                    recB.getR60Fc19Amt(),
                    recB.getR60Fc20Amt(),
                    recB.getR60Fc21Amt());
                wr(sheet, 60, numStyle,
                    recB.getR61GlobalAmt(),
                    recB.getR61UaeAmt(),
                    recB.getR61Fc1Amt(),
                    recB.getR61Fc2Amt(),
                    recB.getR61Fc3Amt(),
                    recB.getR61Fc4Amt(),
                    recB.getR61Fc5Amt(),
                    recB.getR61Fc6Amt(),
                    recB.getR61Fc7Amt(),
                    recB.getR61Fc8Amt(),
                    recB.getR61Fc9Amt(),
                    recB.getR61Fc10Amt(),
                    recB.getR61Fc11Amt(),
                    recB.getR61Fc12Amt(),
                    recB.getR61Fc13Amt(),
                    recB.getR61Fc14Amt(),
                    recB.getR61Fc15Amt(),
                    recB.getR61Fc16Amt(),
                    recB.getR61Fc17Amt(),
                    recB.getR61Fc18Amt(),
                    recB.getR61Fc19Amt(),
                    recB.getR61Fc20Amt(),
                    recB.getR61Fc21Amt());
                wr(sheet, 61, numStyle,
                    recB.getR62GlobalAmt(),
                    recB.getR62UaeAmt(),
                    recB.getR62Fc1Amt(),
                    recB.getR62Fc2Amt(),
                    recB.getR62Fc3Amt(),
                    recB.getR62Fc4Amt(),
                    recB.getR62Fc5Amt(),
                    recB.getR62Fc6Amt(),
                    recB.getR62Fc7Amt(),
                    recB.getR62Fc8Amt(),
                    recB.getR62Fc9Amt(),
                    recB.getR62Fc10Amt(),
                    recB.getR62Fc11Amt(),
                    recB.getR62Fc12Amt(),
                    recB.getR62Fc13Amt(),
                    recB.getR62Fc14Amt(),
                    recB.getR62Fc15Amt(),
                    recB.getR62Fc16Amt(),
                    recB.getR62Fc17Amt(),
                    recB.getR62Fc18Amt(),
                    recB.getR62Fc19Amt(),
                    recB.getR62Fc20Amt(),
                    recB.getR62Fc21Amt());
                wr(sheet, 62, numStyle,
                    recB.getR63GlobalAmt(),
                    recB.getR63UaeAmt(),
                    recB.getR63Fc1Amt(),
                    recB.getR63Fc2Amt(),
                    recB.getR63Fc3Amt(),
                    recB.getR63Fc4Amt(),
                    recB.getR63Fc5Amt(),
                    recB.getR63Fc6Amt(),
                    recB.getR63Fc7Amt(),
                    recB.getR63Fc8Amt(),
                    recB.getR63Fc9Amt(),
                    recB.getR63Fc10Amt(),
                    recB.getR63Fc11Amt(),
                    recB.getR63Fc12Amt(),
                    recB.getR63Fc13Amt(),
                    recB.getR63Fc14Amt(),
                    recB.getR63Fc15Amt(),
                    recB.getR63Fc16Amt(),
                    recB.getR63Fc17Amt(),
                    recB.getR63Fc18Amt(),
                    recB.getR63Fc19Amt(),
                    recB.getR63Fc20Amt(),
                    recB.getR63Fc21Amt());
                wr(sheet, 63, numStyle,
                    recB.getR64GlobalAmt(),
                    recB.getR64UaeAmt(),
                    recB.getR64Fc1Amt(),
                    recB.getR64Fc2Amt(),
                    recB.getR64Fc3Amt(),
                    recB.getR64Fc4Amt(),
                    recB.getR64Fc5Amt(),
                    recB.getR64Fc6Amt(),
                    recB.getR64Fc7Amt(),
                    recB.getR64Fc8Amt(),
                    recB.getR64Fc9Amt(),
                    recB.getR64Fc10Amt(),
                    recB.getR64Fc11Amt(),
                    recB.getR64Fc12Amt(),
                    recB.getR64Fc13Amt(),
                    recB.getR64Fc14Amt(),
                    recB.getR64Fc15Amt(),
                    recB.getR64Fc16Amt(),
                    recB.getR64Fc17Amt(),
                    recB.getR64Fc18Amt(),
                    recB.getR64Fc19Amt(),
                    recB.getR64Fc20Amt(),
                    recB.getR64Fc21Amt());
                wr(sheet, 64, greyNumStyle,
                    recB.getR65GlobalAmt(),
                    recB.getR65UaeAmt(),
                    recB.getR65Fc1Amt(),
                    recB.getR65Fc2Amt(),
                    recB.getR65Fc3Amt(),
                    recB.getR65Fc4Amt(),
                    recB.getR65Fc5Amt(),
                    recB.getR65Fc6Amt(),
                    recB.getR65Fc7Amt(),
                    recB.getR65Fc8Amt(),
                    recB.getR65Fc9Amt(),
                    recB.getR65Fc10Amt(),
                    recB.getR65Fc11Amt(),
                    recB.getR65Fc12Amt(),
                    recB.getR65Fc13Amt(),
                    recB.getR65Fc14Amt(),
                    recB.getR65Fc15Amt(),
                    recB.getR65Fc16Amt(),
                    recB.getR65Fc17Amt(),
                    recB.getR65Fc18Amt(),
                    recB.getR65Fc19Amt(),
                    recB.getR65Fc20Amt(),
                    recB.getR65Fc21Amt());
                wr(sheet, 65, numStyle,
                    recB.getR66GlobalAmt(),
                    recB.getR66UaeAmt(),
                    recB.getR66Fc1Amt(),
                    recB.getR66Fc2Amt(),
                    recB.getR66Fc3Amt(),
                    recB.getR66Fc4Amt(),
                    recB.getR66Fc5Amt(),
                    recB.getR66Fc6Amt(),
                    recB.getR66Fc7Amt(),
                    recB.getR66Fc8Amt(),
                    recB.getR66Fc9Amt(),
                    recB.getR66Fc10Amt(),
                    recB.getR66Fc11Amt(),
                    recB.getR66Fc12Amt(),
                    recB.getR66Fc13Amt(),
                    recB.getR66Fc14Amt(),
                    recB.getR66Fc15Amt(),
                    recB.getR66Fc16Amt(),
                    recB.getR66Fc17Amt(),
                    recB.getR66Fc18Amt(),
                    recB.getR66Fc19Amt(),
                    recB.getR66Fc20Amt(),
                    recB.getR66Fc21Amt());
                wr(sheet, 66, numStyle,
                    recB.getR67GlobalAmt(),
                    recB.getR67UaeAmt(),
                    recB.getR67Fc1Amt(),
                    recB.getR67Fc2Amt(),
                    recB.getR67Fc3Amt(),
                    recB.getR67Fc4Amt(),
                    recB.getR67Fc5Amt(),
                    recB.getR67Fc6Amt(),
                    recB.getR67Fc7Amt(),
                    recB.getR67Fc8Amt(),
                    recB.getR67Fc9Amt(),
                    recB.getR67Fc10Amt(),
                    recB.getR67Fc11Amt(),
                    recB.getR67Fc12Amt(),
                    recB.getR67Fc13Amt(),
                    recB.getR67Fc14Amt(),
                    recB.getR67Fc15Amt(),
                    recB.getR67Fc16Amt(),
                    recB.getR67Fc17Amt(),
                    recB.getR67Fc18Amt(),
                    recB.getR67Fc19Amt(),
                    recB.getR67Fc20Amt(),
                    recB.getR67Fc21Amt());
                wr(sheet, 67, numStyle,
                    recB.getR68GlobalAmt(),
                    recB.getR68UaeAmt(),
                    recB.getR68Fc1Amt(),
                    recB.getR68Fc2Amt(),
                    recB.getR68Fc3Amt(),
                    recB.getR68Fc4Amt(),
                    recB.getR68Fc5Amt(),
                    recB.getR68Fc6Amt(),
                    recB.getR68Fc7Amt(),
                    recB.getR68Fc8Amt(),
                    recB.getR68Fc9Amt(),
                    recB.getR68Fc10Amt(),
                    recB.getR68Fc11Amt(),
                    recB.getR68Fc12Amt(),
                    recB.getR68Fc13Amt(),
                    recB.getR68Fc14Amt(),
                    recB.getR68Fc15Amt(),
                    recB.getR68Fc16Amt(),
                    recB.getR68Fc17Amt(),
                    recB.getR68Fc18Amt(),
                    recB.getR68Fc19Amt(),
                    recB.getR68Fc20Amt(),
                    recB.getR68Fc21Amt());
                wr(sheet, 68, numStyle,
                    recB.getR69GlobalAmt(),
                    recB.getR69UaeAmt(),
                    recB.getR69Fc1Amt(),
                    recB.getR69Fc2Amt(),
                    recB.getR69Fc3Amt(),
                    recB.getR69Fc4Amt(),
                    recB.getR69Fc5Amt(),
                    recB.getR69Fc6Amt(),
                    recB.getR69Fc7Amt(),
                    recB.getR69Fc8Amt(),
                    recB.getR69Fc9Amt(),
                    recB.getR69Fc10Amt(),
                    recB.getR69Fc11Amt(),
                    recB.getR69Fc12Amt(),
                    recB.getR69Fc13Amt(),
                    recB.getR69Fc14Amt(),
                    recB.getR69Fc15Amt(),
                    recB.getR69Fc16Amt(),
                    recB.getR69Fc17Amt(),
                    recB.getR69Fc18Amt(),
                    recB.getR69Fc19Amt(),
                    recB.getR69Fc20Amt(),
                    recB.getR69Fc21Amt());
                wr(sheet, 69, numStyle,
                    recB.getR70GlobalAmt(),
                    recB.getR70UaeAmt(),
                    recB.getR70Fc1Amt(),
                    recB.getR70Fc2Amt(),
                    recB.getR70Fc3Amt(),
                    recB.getR70Fc4Amt(),
                    recB.getR70Fc5Amt(),
                    recB.getR70Fc6Amt(),
                    recB.getR70Fc7Amt(),
                    recB.getR70Fc8Amt(),
                    recB.getR70Fc9Amt(),
                    recB.getR70Fc10Amt(),
                    recB.getR70Fc11Amt(),
                    recB.getR70Fc12Amt(),
                    recB.getR70Fc13Amt(),
                    recB.getR70Fc14Amt(),
                    recB.getR70Fc15Amt(),
                    recB.getR70Fc16Amt(),
                    recB.getR70Fc17Amt(),
                    recB.getR70Fc18Amt(),
                    recB.getR70Fc19Amt(),
                    recB.getR70Fc20Amt(),
                    recB.getR70Fc21Amt());
                wr(sheet, 70, numStyle,
                    recB.getR71GlobalAmt(),
                    recB.getR71UaeAmt(),
                    recB.getR71Fc1Amt(),
                    recB.getR71Fc2Amt(),
                    recB.getR71Fc3Amt(),
                    recB.getR71Fc4Amt(),
                    recB.getR71Fc5Amt(),
                    recB.getR71Fc6Amt(),
                    recB.getR71Fc7Amt(),
                    recB.getR71Fc8Amt(),
                    recB.getR71Fc9Amt(),
                    recB.getR71Fc10Amt(),
                    recB.getR71Fc11Amt(),
                    recB.getR71Fc12Amt(),
                    recB.getR71Fc13Amt(),
                    recB.getR71Fc14Amt(),
                    recB.getR71Fc15Amt(),
                    recB.getR71Fc16Amt(),
                    recB.getR71Fc17Amt(),
                    recB.getR71Fc18Amt(),
                    recB.getR71Fc19Amt(),
                    recB.getR71Fc20Amt(),
                    recB.getR71Fc21Amt());
                wr(sheet, 71, numStyle,
                    recB.getR72GlobalAmt(),
                    recB.getR72UaeAmt(),
                    recB.getR72Fc1Amt(),
                    recB.getR72Fc2Amt(),
                    recB.getR72Fc3Amt(),
                    recB.getR72Fc4Amt(),
                    recB.getR72Fc5Amt(),
                    recB.getR72Fc6Amt(),
                    recB.getR72Fc7Amt(),
                    recB.getR72Fc8Amt(),
                    recB.getR72Fc9Amt(),
                    recB.getR72Fc10Amt(),
                    recB.getR72Fc11Amt(),
                    recB.getR72Fc12Amt(),
                    recB.getR72Fc13Amt(),
                    recB.getR72Fc14Amt(),
                    recB.getR72Fc15Amt(),
                    recB.getR72Fc16Amt(),
                    recB.getR72Fc17Amt(),
                    recB.getR72Fc18Amt(),
                    recB.getR72Fc19Amt(),
                    recB.getR72Fc20Amt(),
                    recB.getR72Fc21Amt());
                wr(sheet, 72, numStyle,
                    recB.getR73GlobalAmt(),
                    recB.getR73UaeAmt(),
                    recB.getR73Fc1Amt(),
                    recB.getR73Fc2Amt(),
                    recB.getR73Fc3Amt(),
                    recB.getR73Fc4Amt(),
                    recB.getR73Fc5Amt(),
                    recB.getR73Fc6Amt(),
                    recB.getR73Fc7Amt(),
                    recB.getR73Fc8Amt(),
                    recB.getR73Fc9Amt(),
                    recB.getR73Fc10Amt(),
                    recB.getR73Fc11Amt(),
                    recB.getR73Fc12Amt(),
                    recB.getR73Fc13Amt(),
                    recB.getR73Fc14Amt(),
                    recB.getR73Fc15Amt(),
                    recB.getR73Fc16Amt(),
                    recB.getR73Fc17Amt(),
                    recB.getR73Fc18Amt(),
                    recB.getR73Fc19Amt(),
                    recB.getR73Fc20Amt(),
                    recB.getR73Fc21Amt());
                wr(sheet, 73, numStyle,
                    recB.getR74GlobalAmt(),
                    recB.getR74UaeAmt(),
                    recB.getR74Fc1Amt(),
                    recB.getR74Fc2Amt(),
                    recB.getR74Fc3Amt(),
                    recB.getR74Fc4Amt(),
                    recB.getR74Fc5Amt(),
                    recB.getR74Fc6Amt(),
                    recB.getR74Fc7Amt(),
                    recB.getR74Fc8Amt(),
                    recB.getR74Fc9Amt(),
                    recB.getR74Fc10Amt(),
                    recB.getR74Fc11Amt(),
                    recB.getR74Fc12Amt(),
                    recB.getR74Fc13Amt(),
                    recB.getR74Fc14Amt(),
                    recB.getR74Fc15Amt(),
                    recB.getR74Fc16Amt(),
                    recB.getR74Fc17Amt(),
                    recB.getR74Fc18Amt(),
                    recB.getR74Fc19Amt(),
                    recB.getR74Fc20Amt(),
                    recB.getR74Fc21Amt());
                wr(sheet, 74, greyNumStyle,
                    recB.getR75GlobalAmt(),
                    recB.getR75UaeAmt(),
                    recB.getR75Fc1Amt(),
                    recB.getR75Fc2Amt(),
                    recB.getR75Fc3Amt(),
                    recB.getR75Fc4Amt(),
                    recB.getR75Fc5Amt(),
                    recB.getR75Fc6Amt(),
                    recB.getR75Fc7Amt(),
                    recB.getR75Fc8Amt(),
                    recB.getR75Fc9Amt(),
                    recB.getR75Fc10Amt(),
                    recB.getR75Fc11Amt(),
                    recB.getR75Fc12Amt(),
                    recB.getR75Fc13Amt(),
                    recB.getR75Fc14Amt(),
                    recB.getR75Fc15Amt(),
                    recB.getR75Fc16Amt(),
                    recB.getR75Fc17Amt(),
                    recB.getR75Fc18Amt(),
                    recB.getR75Fc19Amt(),
                    recB.getR75Fc20Amt(),
                    recB.getR75Fc21Amt());
                wr(sheet, 75, numStyle,
                    recB.getR76GlobalAmt(),
                    recB.getR76UaeAmt(),
                    recB.getR76Fc1Amt(),
                    recB.getR76Fc2Amt(),
                    recB.getR76Fc3Amt(),
                    recB.getR76Fc4Amt(),
                    recB.getR76Fc5Amt(),
                    recB.getR76Fc6Amt(),
                    recB.getR76Fc7Amt(),
                    recB.getR76Fc8Amt(),
                    recB.getR76Fc9Amt(),
                    recB.getR76Fc10Amt(),
                    recB.getR76Fc11Amt(),
                    recB.getR76Fc12Amt(),
                    recB.getR76Fc13Amt(),
                    recB.getR76Fc14Amt(),
                    recB.getR76Fc15Amt(),
                    recB.getR76Fc16Amt(),
                    recB.getR76Fc17Amt(),
                    recB.getR76Fc18Amt(),
                    recB.getR76Fc19Amt(),
                    recB.getR76Fc20Amt(),
                    recB.getR76Fc21Amt());
                wr(sheet, 76, numStyle,
                    recB.getR77GlobalAmt(),
                    recB.getR77UaeAmt(),
                    recB.getR77Fc1Amt(),
                    recB.getR77Fc2Amt(),
                    recB.getR77Fc3Amt(),
                    recB.getR77Fc4Amt(),
                    recB.getR77Fc5Amt(),
                    recB.getR77Fc6Amt(),
                    recB.getR77Fc7Amt(),
                    recB.getR77Fc8Amt(),
                    recB.getR77Fc9Amt(),
                    recB.getR77Fc10Amt(),
                    recB.getR77Fc11Amt(),
                    recB.getR77Fc12Amt(),
                    recB.getR77Fc13Amt(),
                    recB.getR77Fc14Amt(),
                    recB.getR77Fc15Amt(),
                    recB.getR77Fc16Amt(),
                    recB.getR77Fc17Amt(),
                    recB.getR77Fc18Amt(),
                    recB.getR77Fc19Amt(),
                    recB.getR77Fc20Amt(),
                    recB.getR77Fc21Amt());
                wr(sheet, 77, numStyle,
                    recB.getR78GlobalAmt(),
                    recB.getR78UaeAmt(),
                    recB.getR78Fc1Amt(),
                    recB.getR78Fc2Amt(),
                    recB.getR78Fc3Amt(),
                    recB.getR78Fc4Amt(),
                    recB.getR78Fc5Amt(),
                    recB.getR78Fc6Amt(),
                    recB.getR78Fc7Amt(),
                    recB.getR78Fc8Amt(),
                    recB.getR78Fc9Amt(),
                    recB.getR78Fc10Amt(),
                    recB.getR78Fc11Amt(),
                    recB.getR78Fc12Amt(),
                    recB.getR78Fc13Amt(),
                    recB.getR78Fc14Amt(),
                    recB.getR78Fc15Amt(),
                    recB.getR78Fc16Amt(),
                    recB.getR78Fc17Amt(),
                    recB.getR78Fc18Amt(),
                    recB.getR78Fc19Amt(),
                    recB.getR78Fc20Amt(),
                    recB.getR78Fc21Amt());
                wr(sheet, 78, numStyle,
                    recB.getR79GlobalAmt(),
                    recB.getR79UaeAmt(),
                    recB.getR79Fc1Amt(),
                    recB.getR79Fc2Amt(),
                    recB.getR79Fc3Amt(),
                    recB.getR79Fc4Amt(),
                    recB.getR79Fc5Amt(),
                    recB.getR79Fc6Amt(),
                    recB.getR79Fc7Amt(),
                    recB.getR79Fc8Amt(),
                    recB.getR79Fc9Amt(),
                    recB.getR79Fc10Amt(),
                    recB.getR79Fc11Amt(),
                    recB.getR79Fc12Amt(),
                    recB.getR79Fc13Amt(),
                    recB.getR79Fc14Amt(),
                    recB.getR79Fc15Amt(),
                    recB.getR79Fc16Amt(),
                    recB.getR79Fc17Amt(),
                    recB.getR79Fc18Amt(),
                    recB.getR79Fc19Amt(),
                    recB.getR79Fc20Amt(),
                    recB.getR79Fc21Amt());
                wr(sheet, 79, numStyle,
                    recB.getR80GlobalAmt(),
                    recB.getR80UaeAmt(),
                    recB.getR80Fc1Amt(),
                    recB.getR80Fc2Amt(),
                    recB.getR80Fc3Amt(),
                    recB.getR80Fc4Amt(),
                    recB.getR80Fc5Amt(),
                    recB.getR80Fc6Amt(),
                    recB.getR80Fc7Amt(),
                    recB.getR80Fc8Amt(),
                    recB.getR80Fc9Amt(),
                    recB.getR80Fc10Amt(),
                    recB.getR80Fc11Amt(),
                    recB.getR80Fc12Amt(),
                    recB.getR80Fc13Amt(),
                    recB.getR80Fc14Amt(),
                    recB.getR80Fc15Amt(),
                    recB.getR80Fc16Amt(),
                    recB.getR80Fc17Amt(),
                    recB.getR80Fc18Amt(),
                    recB.getR80Fc19Amt(),
                    recB.getR80Fc20Amt(),
                    recB.getR80Fc21Amt());
                wr(sheet, 80, numStyle,
                    recB.getR81GlobalAmt(),
                    recB.getR81UaeAmt(),
                    recB.getR81Fc1Amt(),
                    recB.getR81Fc2Amt(),
                    recB.getR81Fc3Amt(),
                    recB.getR81Fc4Amt(),
                    recB.getR81Fc5Amt(),
                    recB.getR81Fc6Amt(),
                    recB.getR81Fc7Amt(),
                    recB.getR81Fc8Amt(),
                    recB.getR81Fc9Amt(),
                    recB.getR81Fc10Amt(),
                    recB.getR81Fc11Amt(),
                    recB.getR81Fc12Amt(),
                    recB.getR81Fc13Amt(),
                    recB.getR81Fc14Amt(),
                    recB.getR81Fc15Amt(),
                    recB.getR81Fc16Amt(),
                    recB.getR81Fc17Amt(),
                    recB.getR81Fc18Amt(),
                    recB.getR81Fc19Amt(),
                    recB.getR81Fc20Amt(),
                    recB.getR81Fc21Amt());
                wr(sheet, 81, numStyle,
                    recB.getR82GlobalAmt(),
                    recB.getR82UaeAmt(),
                    recB.getR82Fc1Amt(),
                    recB.getR82Fc2Amt(),
                    recB.getR82Fc3Amt(),
                    recB.getR82Fc4Amt(),
                    recB.getR82Fc5Amt(),
                    recB.getR82Fc6Amt(),
                    recB.getR82Fc7Amt(),
                    recB.getR82Fc8Amt(),
                    recB.getR82Fc9Amt(),
                    recB.getR82Fc10Amt(),
                    recB.getR82Fc11Amt(),
                    recB.getR82Fc12Amt(),
                    recB.getR82Fc13Amt(),
                    recB.getR82Fc14Amt(),
                    recB.getR82Fc15Amt(),
                    recB.getR82Fc16Amt(),
                    recB.getR82Fc17Amt(),
                    recB.getR82Fc18Amt(),
                    recB.getR82Fc19Amt(),
                    recB.getR82Fc20Amt(),
                    recB.getR82Fc21Amt());
                wr(sheet, 82, greyNumStyle,
                    recB.getR83GlobalAmt(),
                    recB.getR83UaeAmt(),
                    recB.getR83Fc1Amt(),
                    recB.getR83Fc2Amt(),
                    recB.getR83Fc3Amt(),
                    recB.getR83Fc4Amt(),
                    recB.getR83Fc5Amt(),
                    recB.getR83Fc6Amt(),
                    recB.getR83Fc7Amt(),
                    recB.getR83Fc8Amt(),
                    recB.getR83Fc9Amt(),
                    recB.getR83Fc10Amt(),
                    recB.getR83Fc11Amt(),
                    recB.getR83Fc12Amt(),
                    recB.getR83Fc13Amt(),
                    recB.getR83Fc14Amt(),
                    recB.getR83Fc15Amt(),
                    recB.getR83Fc16Amt(),
                    recB.getR83Fc17Amt(),
                    recB.getR83Fc18Amt(),
                    recB.getR83Fc19Amt(),
                    recB.getR83Fc20Amt(),
                    recB.getR83Fc21Amt());
                wr(sheet, 83, greyNumStyle,
                    recB.getR84GlobalAmt(),
                    recB.getR84UaeAmt(),
                    recB.getR84Fc1Amt(),
                    recB.getR84Fc2Amt(),
                    recB.getR84Fc3Amt(),
                    recB.getR84Fc4Amt(),
                    recB.getR84Fc5Amt(),
                    recB.getR84Fc6Amt(),
                    recB.getR84Fc7Amt(),
                    recB.getR84Fc8Amt(),
                    recB.getR84Fc9Amt(),
                    recB.getR84Fc10Amt(),
                    recB.getR84Fc11Amt(),
                    recB.getR84Fc12Amt(),
                    recB.getR84Fc13Amt(),
                    recB.getR84Fc14Amt(),
                    recB.getR84Fc15Amt(),
                    recB.getR84Fc16Amt(),
                    recB.getR84Fc17Amt(),
                    recB.getR84Fc18Amt(),
                    recB.getR84Fc19Amt(),
                    recB.getR84Fc20Amt(),
                    recB.getR84Fc21Amt());
                wr(sheet, 84, numStyle,
                    recB.getR85GlobalAmt(),
                    recB.getR85UaeAmt(),
                    recB.getR85Fc1Amt(),
                    recB.getR85Fc2Amt(),
                    recB.getR85Fc3Amt(),
                    recB.getR85Fc4Amt(),
                    recB.getR85Fc5Amt(),
                    recB.getR85Fc6Amt(),
                    recB.getR85Fc7Amt(),
                    recB.getR85Fc8Amt(),
                    recB.getR85Fc9Amt(),
                    recB.getR85Fc10Amt(),
                    recB.getR85Fc11Amt(),
                    recB.getR85Fc12Amt(),
                    recB.getR85Fc13Amt(),
                    recB.getR85Fc14Amt(),
                    recB.getR85Fc15Amt(),
                    recB.getR85Fc16Amt(),
                    recB.getR85Fc17Amt(),
                    recB.getR85Fc18Amt(),
                    recB.getR85Fc19Amt(),
                    recB.getR85Fc20Amt(),
                    recB.getR85Fc21Amt());
                wr(sheet, 85, numStyle,
                    recB.getR86GlobalAmt(),
                    recB.getR86UaeAmt(),
                    recB.getR86Fc1Amt(),
                    recB.getR86Fc2Amt(),
                    recB.getR86Fc3Amt(),
                    recB.getR86Fc4Amt(),
                    recB.getR86Fc5Amt(),
                    recB.getR86Fc6Amt(),
                    recB.getR86Fc7Amt(),
                    recB.getR86Fc8Amt(),
                    recB.getR86Fc9Amt(),
                    recB.getR86Fc10Amt(),
                    recB.getR86Fc11Amt(),
                    recB.getR86Fc12Amt(),
                    recB.getR86Fc13Amt(),
                    recB.getR86Fc14Amt(),
                    recB.getR86Fc15Amt(),
                    recB.getR86Fc16Amt(),
                    recB.getR86Fc17Amt(),
                    recB.getR86Fc18Amt(),
                    recB.getR86Fc19Amt(),
                    recB.getR86Fc20Amt(),
                    recB.getR86Fc21Amt());
                wr(sheet, 86, greyNumStyle,
                    recB.getR87GlobalAmt(),
                    recB.getR87UaeAmt(),
                    recB.getR87Fc1Amt(),
                    recB.getR87Fc2Amt(),
                    recB.getR87Fc3Amt(),
                    recB.getR87Fc4Amt(),
                    recB.getR87Fc5Amt(),
                    recB.getR87Fc6Amt(),
                    recB.getR87Fc7Amt(),
                    recB.getR87Fc8Amt(),
                    recB.getR87Fc9Amt(),
                    recB.getR87Fc10Amt(),
                    recB.getR87Fc11Amt(),
                    recB.getR87Fc12Amt(),
                    recB.getR87Fc13Amt(),
                    recB.getR87Fc14Amt(),
                    recB.getR87Fc15Amt(),
                    recB.getR87Fc16Amt(),
                    recB.getR87Fc17Amt(),
                    recB.getR87Fc18Amt(),
                    recB.getR87Fc19Amt(),
                    recB.getR87Fc20Amt(),
                    recB.getR87Fc21Amt());
                wr(sheet, 87, numStyle,
                    recB.getR88GlobalAmt(),
                    recB.getR88UaeAmt(),
                    recB.getR88Fc1Amt(),
                    recB.getR88Fc2Amt(),
                    recB.getR88Fc3Amt(),
                    recB.getR88Fc4Amt(),
                    recB.getR88Fc5Amt(),
                    recB.getR88Fc6Amt(),
                    recB.getR88Fc7Amt(),
                    recB.getR88Fc8Amt(),
                    recB.getR88Fc9Amt(),
                    recB.getR88Fc10Amt(),
                    recB.getR88Fc11Amt(),
                    recB.getR88Fc12Amt(),
                    recB.getR88Fc13Amt(),
                    recB.getR88Fc14Amt(),
                    recB.getR88Fc15Amt(),
                    recB.getR88Fc16Amt(),
                    recB.getR88Fc17Amt(),
                    recB.getR88Fc18Amt(),
                    recB.getR88Fc19Amt(),
                    recB.getR88Fc20Amt(),
                    recB.getR88Fc21Amt());
                wr(sheet, 88, numStyle,
                    recB.getR89GlobalAmt(),
                    recB.getR89UaeAmt(),
                    recB.getR89Fc1Amt(),
                    recB.getR89Fc2Amt(),
                    recB.getR89Fc3Amt(),
                    recB.getR89Fc4Amt(),
                    recB.getR89Fc5Amt(),
                    recB.getR89Fc6Amt(),
                    recB.getR89Fc7Amt(),
                    recB.getR89Fc8Amt(),
                    recB.getR89Fc9Amt(),
                    recB.getR89Fc10Amt(),
                    recB.getR89Fc11Amt(),
                    recB.getR89Fc12Amt(),
                    recB.getR89Fc13Amt(),
                    recB.getR89Fc14Amt(),
                    recB.getR89Fc15Amt(),
                    recB.getR89Fc16Amt(),
                    recB.getR89Fc17Amt(),
                    recB.getR89Fc18Amt(),
                    recB.getR89Fc19Amt(),
                    recB.getR89Fc20Amt(),
                    recB.getR89Fc21Amt());
                wr(sheet, 89, numStyle,
                    recB.getR90GlobalAmt(),
                    recB.getR90UaeAmt(),
                    recB.getR90Fc1Amt(),
                    recB.getR90Fc2Amt(),
                    recB.getR90Fc3Amt(),
                    recB.getR90Fc4Amt(),
                    recB.getR90Fc5Amt(),
                    recB.getR90Fc6Amt(),
                    recB.getR90Fc7Amt(),
                    recB.getR90Fc8Amt(),
                    recB.getR90Fc9Amt(),
                    recB.getR90Fc10Amt(),
                    recB.getR90Fc11Amt(),
                    recB.getR90Fc12Amt(),
                    recB.getR90Fc13Amt(),
                    recB.getR90Fc14Amt(),
                    recB.getR90Fc15Amt(),
                    recB.getR90Fc16Amt(),
                    recB.getR90Fc17Amt(),
                    recB.getR90Fc18Amt(),
                    recB.getR90Fc19Amt(),
                    recB.getR90Fc20Amt(),
                    recB.getR90Fc21Amt());
                wr(sheet, 90, numStyle,
                    recB.getR91GlobalAmt(),
                    recB.getR91UaeAmt(),
                    recB.getR91Fc1Amt(),
                    recB.getR91Fc2Amt(),
                    recB.getR91Fc3Amt(),
                    recB.getR91Fc4Amt(),
                    recB.getR91Fc5Amt(),
                    recB.getR91Fc6Amt(),
                    recB.getR91Fc7Amt(),
                    recB.getR91Fc8Amt(),
                    recB.getR91Fc9Amt(),
                    recB.getR91Fc10Amt(),
                    recB.getR91Fc11Amt(),
                    recB.getR91Fc12Amt(),
                    recB.getR91Fc13Amt(),
                    recB.getR91Fc14Amt(),
                    recB.getR91Fc15Amt(),
                    recB.getR91Fc16Amt(),
                    recB.getR91Fc17Amt(),
                    recB.getR91Fc18Amt(),
                    recB.getR91Fc19Amt(),
                    recB.getR91Fc20Amt(),
                    recB.getR91Fc21Amt());

                // TABLE C: R92–R95
                wr(sheet, 91, numStyle,
                    recC.getR92GlobalAmt(),
                    recC.getR92UaeAmt(),
                    recC.getR92Fc1Amt(),
                    recC.getR92Fc2Amt(),
                    recC.getR92Fc3Amt(),
                    recC.getR92Fc4Amt(),
                    recC.getR92Fc5Amt(),
                    recC.getR92Fc6Amt(),
                    recC.getR92Fc7Amt(),
                    recC.getR92Fc8Amt(),
                    recC.getR92Fc9Amt(),
                    recC.getR92Fc10Amt(),
                    recC.getR92Fc11Amt(),
                    recC.getR92Fc12Amt(),
                    recC.getR92Fc13Amt(),
                    recC.getR92Fc14Amt(),
                    recC.getR92Fc15Amt(),
                    recC.getR92Fc16Amt(),
                    recC.getR92Fc17Amt(),
                    recC.getR92Fc18Amt(),
                    recC.getR92Fc19Amt(),
                    recC.getR92Fc20Amt(),
                    recC.getR92Fc21Amt());
                wr(sheet, 92, numStyle,
                    recC.getR93GlobalAmt(),
                    recC.getR93UaeAmt(),
                    recC.getR93Fc1Amt(),
                    recC.getR93Fc2Amt(),
                    recC.getR93Fc3Amt(),
                    recC.getR93Fc4Amt(),
                    recC.getR93Fc5Amt(),
                    recC.getR93Fc6Amt(),
                    recC.getR93Fc7Amt(),
                    recC.getR93Fc8Amt(),
                    recC.getR93Fc9Amt(),
                    recC.getR93Fc10Amt(),
                    recC.getR93Fc11Amt(),
                    recC.getR93Fc12Amt(),
                    recC.getR93Fc13Amt(),
                    recC.getR93Fc14Amt(),
                    recC.getR93Fc15Amt(),
                    recC.getR93Fc16Amt(),
                    recC.getR93Fc17Amt(),
                    recC.getR93Fc18Amt(),
                    recC.getR93Fc19Amt(),
                    recC.getR93Fc20Amt(),
                    recC.getR93Fc21Amt());
                wr(sheet, 93, numStyle,
                    recC.getR94GlobalAmt(),
                    recC.getR94UaeAmt(),
                    recC.getR94Fc1Amt(),
                    recC.getR94Fc2Amt(),
                    recC.getR94Fc3Amt(),
                    recC.getR94Fc4Amt(),
                    recC.getR94Fc5Amt(),
                    recC.getR94Fc6Amt(),
                    recC.getR94Fc7Amt(),
                    recC.getR94Fc8Amt(),
                    recC.getR94Fc9Amt(),
                    recC.getR94Fc10Amt(),
                    recC.getR94Fc11Amt(),
                    recC.getR94Fc12Amt(),
                    recC.getR94Fc13Amt(),
                    recC.getR94Fc14Amt(),
                    recC.getR94Fc15Amt(),
                    recC.getR94Fc16Amt(),
                    recC.getR94Fc17Amt(),
                    recC.getR94Fc18Amt(),
                    recC.getR94Fc19Amt(),
                    recC.getR94Fc20Amt(),
                    recC.getR94Fc21Amt());
                wr(sheet, 94, greyNumStyle,
                    recC.getR95GlobalAmt(),
                    recC.getR95UaeAmt(),
                    recC.getR95Fc1Amt(),
                    recC.getR95Fc2Amt(),
                    recC.getR95Fc3Amt(),
                    recC.getR95Fc4Amt(),
                    recC.getR95Fc5Amt(),
                    recC.getR95Fc6Amt(),
                    recC.getR95Fc7Amt(),
                    recC.getR95Fc8Amt(),
                    recC.getR95Fc9Amt(),
                    recC.getR95Fc10Amt(),
                    recC.getR95Fc11Amt(),
                    recC.getR95Fc12Amt(),
                    recC.getR95Fc13Amt(),
                    recC.getR95Fc14Amt(),
                    recC.getR95Fc15Amt(),
                    recC.getR95Fc16Amt(),
                    recC.getR95Fc17Amt(),
                    recC.getR95Fc18Amt(),
                    recC.getR95Fc19Amt(),
                    recC.getR95Fc20Amt(),
                    recC.getR95Fc21Amt());

                workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
                workbook.write(out);
                logger.info("BRF_052_Excel: {} bytes written", out.size());
                return out.toByteArray();
            }

        } catch (Exception e) {
            logger.error("BRF_052_Excel error", e);
            return new byte[0];
        }
    }
 // ─────────────────────────────────────────────────────────────────────────
    // ARCHIVE SUMMARY EXCEL DOWNLOAD
    // Identical to BRF_052_Excel — only repos and entity types changed to
    // read from archive tables instead of live tables.
    // ─────────────────────────────────────────────────────────────────────────
    public byte[] BRF_052_ArchiveExcel(String todate) {

        logger.info("BRF_052_ArchiveExcel: building workbook for todate={}", todate);

        try {
        	Date d1;
        	try {
        	    d1 = dateformat.parse(todate);                          // dd-MMM-yyyy
        	} catch (Exception ex) {
        	    try {
        	        d1 = new SimpleDateFormat("dd-MM-yyyy").parse(todate);  // dd-MM-yyyy ← THIS was missing
        	    } catch (Exception ex2) {
        	        d1 = new SimpleDateFormat("dd/MM/yyyy").parse(todate);  // dd/MM/yyyy
        	    }
        	}

            List<BRF_052_ARCHIVE_A_Entity> listA = brf052ArchiveARepo.getArchiveByDate(d1);
            List<BRF_052_ARCHIVE_B_Entity> listB = brf052ArchiveBRepo.getArchiveByDate(d1);
            List<BRF_052_ARCHIVE_C_Entity> listC = brf052ArchiveCRepo.getArchiveByDate(d1);

            if (listA.isEmpty() && listB.isEmpty() && listC.isEmpty()) {
                logger.warn("BRF_052_ArchiveExcel: no archive data found for date {}", todate);
                return new byte[0];
            }

            BRF_052_ARCHIVE_A_Entity recA = listA.isEmpty() ? new BRF_052_ARCHIVE_A_Entity() : listA.get(0);
            BRF_052_ARCHIVE_B_Entity recB = listB.isEmpty() ? new BRF_052_ARCHIVE_B_Entity() : listB.get(0);
            BRF_052_ARCHIVE_C_Entity recC = listC.isEmpty() ? new BRF_052_ARCHIVE_C_Entity() : listC.get(0);

            // ── Load template ─────────────────────────────────────────────
            String templateDir      = env.getProperty("output.exportpathtemp");
            String templateFileName = "BRF_052.xlsx";
            Path   templatePath     = Paths.get(templateDir, templateFileName);

            logger.info("BRF_052_ArchiveExcel: loading template from {}", templatePath.toAbsolutePath());

            if (!Files.exists(templatePath))
                throw new FileNotFoundException("Template not found: " + templatePath.toAbsolutePath());
            if (!Files.isReadable(templatePath))
                throw new SecurityException("Template not readable: " + templatePath.toAbsolutePath());

            try (InputStream in = Files.newInputStream(templatePath);
                 Workbook workbook = WorkbookFactory.create(in);
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                Sheet sheet = workbook.getSheetAt(0);

                sheet.setColumnWidth(0,  6  * 256);
                sheet.setColumnWidth(1,  5  * 256);
                sheet.setColumnWidth(2,  40 * 256);
                sheet.setColumnWidth(3,  15 * 256);
                sheet.setColumnWidth(4,  15 * 256);
                for (int fc = 5; fc <= 25; fc++) {
                    sheet.setColumnWidth(fc, 13 * 256);
                }

                PrintSetup ps = sheet.getPrintSetup();
                ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
                ps.setLandscape(true);
                ps.setFitWidth((short) 1);
                ps.setFitHeight((short) 0);
                sheet.setFitToPage(true);
                sheet.setAutobreaks(true);
                sheet.setMargin(Sheet.LeftMargin,   0.5);
                sheet.setMargin(Sheet.RightMargin,  0.5);
                sheet.setMargin(Sheet.TopMargin,    0.75);
                sheet.setMargin(Sheet.BottomMargin, 0.75);
                sheet.setMargin(Sheet.HeaderMargin, 0.3);
                sheet.setMargin(Sheet.FooterMargin, 0.3);

                CellStyle numStyle = workbook.createCellStyle();
                numStyle.setDataFormat(workbook.getCreationHelper()
                        .createDataFormat().getFormat("#,##0.000"));
                numStyle.setBorderBottom(BorderStyle.THIN);
                numStyle.setBorderTop(BorderStyle.THIN);
                numStyle.setBorderLeft(BorderStyle.THIN);
                numStyle.setBorderRight(BorderStyle.THIN);

                CellStyle greyNumStyle = workbook.createCellStyle();
                greyNumStyle.cloneStyleFrom(sheet.getRow(9).getCell(3).getCellStyle());
                greyNumStyle.setDataFormat(workbook.getCreationHelper()
                        .createDataFormat().getFormat("#,##0.000"));
                greyNumStyle.setBorderBottom(BorderStyle.THIN);
                greyNumStyle.setBorderTop(BorderStyle.THIN);
                greyNumStyle.setBorderLeft(BorderStyle.THIN);
                greyNumStyle.setBorderRight(BorderStyle.THIN);
                greyNumStyle.setFont(workbook.getFontAt(numStyle.getFontIndex()));

                Date reportDate = recA.getReportDate() != null ? recA.getReportDate()
                        : (recB.getReportDate() != null ? recB.getReportDate() : recC.getReportDate());
                if (reportDate != null) {
                    writeText(sheet, 4, 2, new SimpleDateFormat("dd/MM/yyyy").format(reportDate));
                }

                // TABLE A: R10–R50
                wr(sheet, 9,  greyNumStyle, recA.getR10GlobalAmt(), recA.getR10UaeAmt(), recA.getR10Fc1Amt(), recA.getR10Fc2Amt(), recA.getR10Fc3Amt(), recA.getR10Fc4Amt(), recA.getR10Fc5Amt(), recA.getR10Fc6Amt(), recA.getR10Fc7Amt(), recA.getR10Fc8Amt(), recA.getR10Fc9Amt(), recA.getR10Fc10Amt(), recA.getR10Fc11Amt(), recA.getR10Fc12Amt(), recA.getR10Fc13Amt(), recA.getR10Fc14Amt(), recA.getR10Fc15Amt(), recA.getR10Fc16Amt(), recA.getR10Fc17Amt(), recA.getR10Fc18Amt(), recA.getR10Fc19Amt(), recA.getR10Fc20Amt(), recA.getR10Fc21Amt());
                wr(sheet, 10, numStyle,     recA.getR11GlobalAmt(), recA.getR11UaeAmt(), recA.getR11Fc1Amt(), recA.getR11Fc2Amt(), recA.getR11Fc3Amt(), recA.getR11Fc4Amt(), recA.getR11Fc5Amt(), recA.getR11Fc6Amt(), recA.getR11Fc7Amt(), recA.getR11Fc8Amt(), recA.getR11Fc9Amt(), recA.getR11Fc10Amt(), recA.getR11Fc11Amt(), recA.getR11Fc12Amt(), recA.getR11Fc13Amt(), recA.getR11Fc14Amt(), recA.getR11Fc15Amt(), recA.getR11Fc16Amt(), recA.getR11Fc17Amt(), recA.getR11Fc18Amt(), recA.getR11Fc19Amt(), recA.getR11Fc20Amt(), recA.getR11Fc21Amt());
                wr(sheet, 11, numStyle,     recA.getR12GlobalAmt(), recA.getR12UaeAmt(), recA.getR12Fc1Amt(), recA.getR12Fc2Amt(), recA.getR12Fc3Amt(), recA.getR12Fc4Amt(), recA.getR12Fc5Amt(), recA.getR12Fc6Amt(), recA.getR12Fc7Amt(), recA.getR12Fc8Amt(), recA.getR12Fc9Amt(), recA.getR12Fc10Amt(), recA.getR12Fc11Amt(), recA.getR12Fc12Amt(), recA.getR12Fc13Amt(), recA.getR12Fc14Amt(), recA.getR12Fc15Amt(), recA.getR12Fc16Amt(), recA.getR12Fc17Amt(), recA.getR12Fc18Amt(), recA.getR12Fc19Amt(), recA.getR12Fc20Amt(), recA.getR12Fc21Amt());
                wr(sheet, 12, numStyle,     recA.getR13GlobalAmt(), recA.getR13UaeAmt(), recA.getR13Fc1Amt(), recA.getR13Fc2Amt(), recA.getR13Fc3Amt(), recA.getR13Fc4Amt(), recA.getR13Fc5Amt(), recA.getR13Fc6Amt(), recA.getR13Fc7Amt(), recA.getR13Fc8Amt(), recA.getR13Fc9Amt(), recA.getR13Fc10Amt(), recA.getR13Fc11Amt(), recA.getR13Fc12Amt(), recA.getR13Fc13Amt(), recA.getR13Fc14Amt(), recA.getR13Fc15Amt(), recA.getR13Fc16Amt(), recA.getR13Fc17Amt(), recA.getR13Fc18Amt(), recA.getR13Fc19Amt(), recA.getR13Fc20Amt(), recA.getR13Fc21Amt());
                wr(sheet, 13, numStyle,     recA.getR14GlobalAmt(), recA.getR14UaeAmt(), recA.getR14Fc1Amt(), recA.getR14Fc2Amt(), recA.getR14Fc3Amt(), recA.getR14Fc4Amt(), recA.getR14Fc5Amt(), recA.getR14Fc6Amt(), recA.getR14Fc7Amt(), recA.getR14Fc8Amt(), recA.getR14Fc9Amt(), recA.getR14Fc10Amt(), recA.getR14Fc11Amt(), recA.getR14Fc12Amt(), recA.getR14Fc13Amt(), recA.getR14Fc14Amt(), recA.getR14Fc15Amt(), recA.getR14Fc16Amt(), recA.getR14Fc17Amt(), recA.getR14Fc18Amt(), recA.getR14Fc19Amt(), recA.getR14Fc20Amt(), recA.getR14Fc21Amt());
                wr(sheet, 14, greyNumStyle, recA.getR15GlobalAmt(), recA.getR15UaeAmt(), recA.getR15Fc1Amt(), recA.getR15Fc2Amt(), recA.getR15Fc3Amt(), recA.getR15Fc4Amt(), recA.getR15Fc5Amt(), recA.getR15Fc6Amt(), recA.getR15Fc7Amt(), recA.getR15Fc8Amt(), recA.getR15Fc9Amt(), recA.getR15Fc10Amt(), recA.getR15Fc11Amt(), recA.getR15Fc12Amt(), recA.getR15Fc13Amt(), recA.getR15Fc14Amt(), recA.getR15Fc15Amt(), recA.getR15Fc16Amt(), recA.getR15Fc17Amt(), recA.getR15Fc18Amt(), recA.getR15Fc19Amt(), recA.getR15Fc20Amt(), recA.getR15Fc21Amt());
                wr(sheet, 15, numStyle,     recA.getR16GlobalAmt(), recA.getR16UaeAmt(), recA.getR16Fc1Amt(), recA.getR16Fc2Amt(), recA.getR16Fc3Amt(), recA.getR16Fc4Amt(), recA.getR16Fc5Amt(), recA.getR16Fc6Amt(), recA.getR16Fc7Amt(), recA.getR16Fc8Amt(), recA.getR16Fc9Amt(), recA.getR16Fc10Amt(), recA.getR16Fc11Amt(), recA.getR16Fc12Amt(), recA.getR16Fc13Amt(), recA.getR16Fc14Amt(), recA.getR16Fc15Amt(), recA.getR16Fc16Amt(), recA.getR16Fc17Amt(), recA.getR16Fc18Amt(), recA.getR16Fc19Amt(), recA.getR16Fc20Amt(), recA.getR16Fc21Amt());
                wr(sheet, 16, numStyle,     recA.getR17GlobalAmt(), recA.getR17UaeAmt(), recA.getR17Fc1Amt(), recA.getR17Fc2Amt(), recA.getR17Fc3Amt(), recA.getR17Fc4Amt(), recA.getR17Fc5Amt(), recA.getR17Fc6Amt(), recA.getR17Fc7Amt(), recA.getR17Fc8Amt(), recA.getR17Fc9Amt(), recA.getR17Fc10Amt(), recA.getR17Fc11Amt(), recA.getR17Fc12Amt(), recA.getR17Fc13Amt(), recA.getR17Fc14Amt(), recA.getR17Fc15Amt(), recA.getR17Fc16Amt(), recA.getR17Fc17Amt(), recA.getR17Fc18Amt(), recA.getR17Fc19Amt(), recA.getR17Fc20Amt(), recA.getR17Fc21Amt());
                wr(sheet, 17, numStyle,     recA.getR18GlobalAmt(), recA.getR18UaeAmt(), recA.getR18Fc1Amt(), recA.getR18Fc2Amt(), recA.getR18Fc3Amt(), recA.getR18Fc4Amt(), recA.getR18Fc5Amt(), recA.getR18Fc6Amt(), recA.getR18Fc7Amt(), recA.getR18Fc8Amt(), recA.getR18Fc9Amt(), recA.getR18Fc10Amt(), recA.getR18Fc11Amt(), recA.getR18Fc12Amt(), recA.getR18Fc13Amt(), recA.getR18Fc14Amt(), recA.getR18Fc15Amt(), recA.getR18Fc16Amt(), recA.getR18Fc17Amt(), recA.getR18Fc18Amt(), recA.getR18Fc19Amt(), recA.getR18Fc20Amt(), recA.getR18Fc21Amt());
                wr(sheet, 18, numStyle,     recA.getR19GlobalAmt(), recA.getR19UaeAmt(), recA.getR19Fc1Amt(), recA.getR19Fc2Amt(), recA.getR19Fc3Amt(), recA.getR19Fc4Amt(), recA.getR19Fc5Amt(), recA.getR19Fc6Amt(), recA.getR19Fc7Amt(), recA.getR19Fc8Amt(), recA.getR19Fc9Amt(), recA.getR19Fc10Amt(), recA.getR19Fc11Amt(), recA.getR19Fc12Amt(), recA.getR19Fc13Amt(), recA.getR19Fc14Amt(), recA.getR19Fc15Amt(), recA.getR19Fc16Amt(), recA.getR19Fc17Amt(), recA.getR19Fc18Amt(), recA.getR19Fc19Amt(), recA.getR19Fc20Amt(), recA.getR19Fc21Amt());
                wr(sheet, 19, greyNumStyle, recA.getR20GlobalAmt(), recA.getR20UaeAmt(), recA.getR20Fc1Amt(), recA.getR20Fc2Amt(), recA.getR20Fc3Amt(), recA.getR20Fc4Amt(), recA.getR20Fc5Amt(), recA.getR20Fc6Amt(), recA.getR20Fc7Amt(), recA.getR20Fc8Amt(), recA.getR20Fc9Amt(), recA.getR20Fc10Amt(), recA.getR20Fc11Amt(), recA.getR20Fc12Amt(), recA.getR20Fc13Amt(), recA.getR20Fc14Amt(), recA.getR20Fc15Amt(), recA.getR20Fc16Amt(), recA.getR20Fc17Amt(), recA.getR20Fc18Amt(), recA.getR20Fc19Amt(), recA.getR20Fc20Amt(), recA.getR20Fc21Amt());
                wr(sheet, 20, greyNumStyle, recA.getR21GlobalAmt(), recA.getR21UaeAmt(), recA.getR21Fc1Amt(), recA.getR21Fc2Amt(), recA.getR21Fc3Amt(), recA.getR21Fc4Amt(), recA.getR21Fc5Amt(), recA.getR21Fc6Amt(), recA.getR21Fc7Amt(), recA.getR21Fc8Amt(), recA.getR21Fc9Amt(), recA.getR21Fc10Amt(), recA.getR21Fc11Amt(), recA.getR21Fc12Amt(), recA.getR21Fc13Amt(), recA.getR21Fc14Amt(), recA.getR21Fc15Amt(), recA.getR21Fc16Amt(), recA.getR21Fc17Amt(), recA.getR21Fc18Amt(), recA.getR21Fc19Amt(), recA.getR21Fc20Amt(), recA.getR21Fc21Amt());
                wr(sheet, 21, numStyle,     recA.getR22GlobalAmt(), recA.getR22UaeAmt(), recA.getR22Fc1Amt(), recA.getR22Fc2Amt(), recA.getR22Fc3Amt(), recA.getR22Fc4Amt(), recA.getR22Fc5Amt(), recA.getR22Fc6Amt(), recA.getR22Fc7Amt(), recA.getR22Fc8Amt(), recA.getR22Fc9Amt(), recA.getR22Fc10Amt(), recA.getR22Fc11Amt(), recA.getR22Fc12Amt(), recA.getR22Fc13Amt(), recA.getR22Fc14Amt(), recA.getR22Fc15Amt(), recA.getR22Fc16Amt(), recA.getR22Fc17Amt(), recA.getR22Fc18Amt(), recA.getR22Fc19Amt(), recA.getR22Fc20Amt(), recA.getR22Fc21Amt());
                wr(sheet, 22, numStyle,     recA.getR23GlobalAmt(), recA.getR23UaeAmt(), recA.getR23Fc1Amt(), recA.getR23Fc2Amt(), recA.getR23Fc3Amt(), recA.getR23Fc4Amt(), recA.getR23Fc5Amt(), recA.getR23Fc6Amt(), recA.getR23Fc7Amt(), recA.getR23Fc8Amt(), recA.getR23Fc9Amt(), recA.getR23Fc10Amt(), recA.getR23Fc11Amt(), recA.getR23Fc12Amt(), recA.getR23Fc13Amt(), recA.getR23Fc14Amt(), recA.getR23Fc15Amt(), recA.getR23Fc16Amt(), recA.getR23Fc17Amt(), recA.getR23Fc18Amt(), recA.getR23Fc19Amt(), recA.getR23Fc20Amt(), recA.getR23Fc21Amt());
                wr(sheet, 23, numStyle,     recA.getR24GlobalAmt(), recA.getR24UaeAmt(), recA.getR24Fc1Amt(), recA.getR24Fc2Amt(), recA.getR24Fc3Amt(), recA.getR24Fc4Amt(), recA.getR24Fc5Amt(), recA.getR24Fc6Amt(), recA.getR24Fc7Amt(), recA.getR24Fc8Amt(), recA.getR24Fc9Amt(), recA.getR24Fc10Amt(), recA.getR24Fc11Amt(), recA.getR24Fc12Amt(), recA.getR24Fc13Amt(), recA.getR24Fc14Amt(), recA.getR24Fc15Amt(), recA.getR24Fc16Amt(), recA.getR24Fc17Amt(), recA.getR24Fc18Amt(), recA.getR24Fc19Amt(), recA.getR24Fc20Amt(), recA.getR24Fc21Amt());
                wr(sheet, 24, numStyle,     recA.getR25GlobalAmt(), recA.getR25UaeAmt(), recA.getR25Fc1Amt(), recA.getR25Fc2Amt(), recA.getR25Fc3Amt(), recA.getR25Fc4Amt(), recA.getR25Fc5Amt(), recA.getR25Fc6Amt(), recA.getR25Fc7Amt(), recA.getR25Fc8Amt(), recA.getR25Fc9Amt(), recA.getR25Fc10Amt(), recA.getR25Fc11Amt(), recA.getR25Fc12Amt(), recA.getR25Fc13Amt(), recA.getR25Fc14Amt(), recA.getR25Fc15Amt(), recA.getR25Fc16Amt(), recA.getR25Fc17Amt(), recA.getR25Fc18Amt(), recA.getR25Fc19Amt(), recA.getR25Fc20Amt(), recA.getR25Fc21Amt());
                wr(sheet, 25, greyNumStyle, recA.getR26GlobalAmt(), recA.getR26UaeAmt(), recA.getR26Fc1Amt(), recA.getR26Fc2Amt(), recA.getR26Fc3Amt(), recA.getR26Fc4Amt(), recA.getR26Fc5Amt(), recA.getR26Fc6Amt(), recA.getR26Fc7Amt(), recA.getR26Fc8Amt(), recA.getR26Fc9Amt(), recA.getR26Fc10Amt(), recA.getR26Fc11Amt(), recA.getR26Fc12Amt(), recA.getR26Fc13Amt(), recA.getR26Fc14Amt(), recA.getR26Fc15Amt(), recA.getR26Fc16Amt(), recA.getR26Fc17Amt(), recA.getR26Fc18Amt(), recA.getR26Fc19Amt(), recA.getR26Fc20Amt(), recA.getR26Fc21Amt());
                wr(sheet, 26, numStyle,     recA.getR27GlobalAmt(), recA.getR27UaeAmt(), recA.getR27Fc1Amt(), recA.getR27Fc2Amt(), recA.getR27Fc3Amt(), recA.getR27Fc4Amt(), recA.getR27Fc5Amt(), recA.getR27Fc6Amt(), recA.getR27Fc7Amt(), recA.getR27Fc8Amt(), recA.getR27Fc9Amt(), recA.getR27Fc10Amt(), recA.getR27Fc11Amt(), recA.getR27Fc12Amt(), recA.getR27Fc13Amt(), recA.getR27Fc14Amt(), recA.getR27Fc15Amt(), recA.getR27Fc16Amt(), recA.getR27Fc17Amt(), recA.getR27Fc18Amt(), recA.getR27Fc19Amt(), recA.getR27Fc20Amt(), recA.getR27Fc21Amt());
                wr(sheet, 27, numStyle,     recA.getR28GlobalAmt(), recA.getR28UaeAmt(), recA.getR28Fc1Amt(), recA.getR28Fc2Amt(), recA.getR28Fc3Amt(), recA.getR28Fc4Amt(), recA.getR28Fc5Amt(), recA.getR28Fc6Amt(), recA.getR28Fc7Amt(), recA.getR28Fc8Amt(), recA.getR28Fc9Amt(), recA.getR28Fc10Amt(), recA.getR28Fc11Amt(), recA.getR28Fc12Amt(), recA.getR28Fc13Amt(), recA.getR28Fc14Amt(), recA.getR28Fc15Amt(), recA.getR28Fc16Amt(), recA.getR28Fc17Amt(), recA.getR28Fc18Amt(), recA.getR28Fc19Amt(), recA.getR28Fc20Amt(), recA.getR28Fc21Amt());
                wr(sheet, 28, numStyle,     recA.getR29GlobalAmt(), recA.getR29UaeAmt(), recA.getR29Fc1Amt(), recA.getR29Fc2Amt(), recA.getR29Fc3Amt(), recA.getR29Fc4Amt(), recA.getR29Fc5Amt(), recA.getR29Fc6Amt(), recA.getR29Fc7Amt(), recA.getR29Fc8Amt(), recA.getR29Fc9Amt(), recA.getR29Fc10Amt(), recA.getR29Fc11Amt(), recA.getR29Fc12Amt(), recA.getR29Fc13Amt(), recA.getR29Fc14Amt(), recA.getR29Fc15Amt(), recA.getR29Fc16Amt(), recA.getR29Fc17Amt(), recA.getR29Fc18Amt(), recA.getR29Fc19Amt(), recA.getR29Fc20Amt(), recA.getR29Fc21Amt());
                wr(sheet, 29, numStyle,     recA.getR30GlobalAmt(), recA.getR30UaeAmt(), recA.getR30Fc1Amt(), recA.getR30Fc2Amt(), recA.getR30Fc3Amt(), recA.getR30Fc4Amt(), recA.getR30Fc5Amt(), recA.getR30Fc6Amt(), recA.getR30Fc7Amt(), recA.getR30Fc8Amt(), recA.getR30Fc9Amt(), recA.getR30Fc10Amt(), recA.getR30Fc11Amt(), recA.getR30Fc12Amt(), recA.getR30Fc13Amt(), recA.getR30Fc14Amt(), recA.getR30Fc15Amt(), recA.getR30Fc16Amt(), recA.getR30Fc17Amt(), recA.getR30Fc18Amt(), recA.getR30Fc19Amt(), recA.getR30Fc20Amt(), recA.getR30Fc21Amt());
                wr(sheet, 30, greyNumStyle, recA.getR31GlobalAmt(), recA.getR31UaeAmt(), recA.getR31Fc1Amt(), recA.getR31Fc2Amt(), recA.getR31Fc3Amt(), recA.getR31Fc4Amt(), recA.getR31Fc5Amt(), recA.getR31Fc6Amt(), recA.getR31Fc7Amt(), recA.getR31Fc8Amt(), recA.getR31Fc9Amt(), recA.getR31Fc10Amt(), recA.getR31Fc11Amt(), recA.getR31Fc12Amt(), recA.getR31Fc13Amt(), recA.getR31Fc14Amt(), recA.getR31Fc15Amt(), recA.getR31Fc16Amt(), recA.getR31Fc17Amt(), recA.getR31Fc18Amt(), recA.getR31Fc19Amt(), recA.getR31Fc20Amt(), recA.getR31Fc21Amt());
                wr(sheet, 31, numStyle,     recA.getR32GlobalAmt(), recA.getR32UaeAmt(), recA.getR32Fc1Amt(), recA.getR32Fc2Amt(), recA.getR32Fc3Amt(), recA.getR32Fc4Amt(), recA.getR32Fc5Amt(), recA.getR32Fc6Amt(), recA.getR32Fc7Amt(), recA.getR32Fc8Amt(), recA.getR32Fc9Amt(), recA.getR32Fc10Amt(), recA.getR32Fc11Amt(), recA.getR32Fc12Amt(), recA.getR32Fc13Amt(), recA.getR32Fc14Amt(), recA.getR32Fc15Amt(), recA.getR32Fc16Amt(), recA.getR32Fc17Amt(), recA.getR32Fc18Amt(), recA.getR32Fc19Amt(), recA.getR32Fc20Amt(), recA.getR32Fc21Amt());
                wr(sheet, 32, numStyle,     recA.getR33GlobalAmt(), recA.getR33UaeAmt(), recA.getR33Fc1Amt(), recA.getR33Fc2Amt(), recA.getR33Fc3Amt(), recA.getR33Fc4Amt(), recA.getR33Fc5Amt(), recA.getR33Fc6Amt(), recA.getR33Fc7Amt(), recA.getR33Fc8Amt(), recA.getR33Fc9Amt(), recA.getR33Fc10Amt(), recA.getR33Fc11Amt(), recA.getR33Fc12Amt(), recA.getR33Fc13Amt(), recA.getR33Fc14Amt(), recA.getR33Fc15Amt(), recA.getR33Fc16Amt(), recA.getR33Fc17Amt(), recA.getR33Fc18Amt(), recA.getR33Fc19Amt(), recA.getR33Fc20Amt(), recA.getR33Fc21Amt());
                wr(sheet, 33, numStyle,     recA.getR34GlobalAmt(), recA.getR34UaeAmt(), recA.getR34Fc1Amt(), recA.getR34Fc2Amt(), recA.getR34Fc3Amt(), recA.getR34Fc4Amt(), recA.getR34Fc5Amt(), recA.getR34Fc6Amt(), recA.getR34Fc7Amt(), recA.getR34Fc8Amt(), recA.getR34Fc9Amt(), recA.getR34Fc10Amt(), recA.getR34Fc11Amt(), recA.getR34Fc12Amt(), recA.getR34Fc13Amt(), recA.getR34Fc14Amt(), recA.getR34Fc15Amt(), recA.getR34Fc16Amt(), recA.getR34Fc17Amt(), recA.getR34Fc18Amt(), recA.getR34Fc19Amt(), recA.getR34Fc20Amt(), recA.getR34Fc21Amt());
                wr(sheet, 34, numStyle,     recA.getR35GlobalAmt(), recA.getR35UaeAmt(), recA.getR35Fc1Amt(), recA.getR35Fc2Amt(), recA.getR35Fc3Amt(), recA.getR35Fc4Amt(), recA.getR35Fc5Amt(), recA.getR35Fc6Amt(), recA.getR35Fc7Amt(), recA.getR35Fc8Amt(), recA.getR35Fc9Amt(), recA.getR35Fc10Amt(), recA.getR35Fc11Amt(), recA.getR35Fc12Amt(), recA.getR35Fc13Amt(), recA.getR35Fc14Amt(), recA.getR35Fc15Amt(), recA.getR35Fc16Amt(), recA.getR35Fc17Amt(), recA.getR35Fc18Amt(), recA.getR35Fc19Amt(), recA.getR35Fc20Amt(), recA.getR35Fc21Amt());
                wr(sheet, 35, greyNumStyle, recA.getR36GlobalAmt(), recA.getR36UaeAmt(), recA.getR36Fc1Amt(), recA.getR36Fc2Amt(), recA.getR36Fc3Amt(), recA.getR36Fc4Amt(), recA.getR36Fc5Amt(), recA.getR36Fc6Amt(), recA.getR36Fc7Amt(), recA.getR36Fc8Amt(), recA.getR36Fc9Amt(), recA.getR36Fc10Amt(), recA.getR36Fc11Amt(), recA.getR36Fc12Amt(), recA.getR36Fc13Amt(), recA.getR36Fc14Amt(), recA.getR36Fc15Amt(), recA.getR36Fc16Amt(), recA.getR36Fc17Amt(), recA.getR36Fc18Amt(), recA.getR36Fc19Amt(), recA.getR36Fc20Amt(), recA.getR36Fc21Amt());
                wr(sheet, 36, numStyle,     recA.getR37GlobalAmt(), recA.getR37UaeAmt(), recA.getR37Fc1Amt(), recA.getR37Fc2Amt(), recA.getR37Fc3Amt(), recA.getR37Fc4Amt(), recA.getR37Fc5Amt(), recA.getR37Fc6Amt(), recA.getR37Fc7Amt(), recA.getR37Fc8Amt(), recA.getR37Fc9Amt(), recA.getR37Fc10Amt(), recA.getR37Fc11Amt(), recA.getR37Fc12Amt(), recA.getR37Fc13Amt(), recA.getR37Fc14Amt(), recA.getR37Fc15Amt(), recA.getR37Fc16Amt(), recA.getR37Fc17Amt(), recA.getR37Fc18Amt(), recA.getR37Fc19Amt(), recA.getR37Fc20Amt(), recA.getR37Fc21Amt());
                wr(sheet, 37, numStyle,     recA.getR38GlobalAmt(), recA.getR38UaeAmt(), recA.getR38Fc1Amt(), recA.getR38Fc2Amt(), recA.getR38Fc3Amt(), recA.getR38Fc4Amt(), recA.getR38Fc5Amt(), recA.getR38Fc6Amt(), recA.getR38Fc7Amt(), recA.getR38Fc8Amt(), recA.getR38Fc9Amt(), recA.getR38Fc10Amt(), recA.getR38Fc11Amt(), recA.getR38Fc12Amt(), recA.getR38Fc13Amt(), recA.getR38Fc14Amt(), recA.getR38Fc15Amt(), recA.getR38Fc16Amt(), recA.getR38Fc17Amt(), recA.getR38Fc18Amt(), recA.getR38Fc19Amt(), recA.getR38Fc20Amt(), recA.getR38Fc21Amt());
                wr(sheet, 38, greyNumStyle, recA.getR39GlobalAmt(), recA.getR39UaeAmt(), recA.getR39Fc1Amt(), recA.getR39Fc2Amt(), recA.getR39Fc3Amt(), recA.getR39Fc4Amt(), recA.getR39Fc5Amt(), recA.getR39Fc6Amt(), recA.getR39Fc7Amt(), recA.getR39Fc8Amt(), recA.getR39Fc9Amt(), recA.getR39Fc10Amt(), recA.getR39Fc11Amt(), recA.getR39Fc12Amt(), recA.getR39Fc13Amt(), recA.getR39Fc14Amt(), recA.getR39Fc15Amt(), recA.getR39Fc16Amt(), recA.getR39Fc17Amt(), recA.getR39Fc18Amt(), recA.getR39Fc19Amt(), recA.getR39Fc20Amt(), recA.getR39Fc21Amt());
                wr(sheet, 39, numStyle,     recA.getR40GlobalAmt(), recA.getR40UaeAmt(), recA.getR40Fc1Amt(), recA.getR40Fc2Amt(), recA.getR40Fc3Amt(), recA.getR40Fc4Amt(), recA.getR40Fc5Amt(), recA.getR40Fc6Amt(), recA.getR40Fc7Amt(), recA.getR40Fc8Amt(), recA.getR40Fc9Amt(), recA.getR40Fc10Amt(), recA.getR40Fc11Amt(), recA.getR40Fc12Amt(), recA.getR40Fc13Amt(), recA.getR40Fc14Amt(), recA.getR40Fc15Amt(), recA.getR40Fc16Amt(), recA.getR40Fc17Amt(), recA.getR40Fc18Amt(), recA.getR40Fc19Amt(), recA.getR40Fc20Amt(), recA.getR40Fc21Amt());
                wr(sheet, 40, numStyle,     recA.getR41GlobalAmt(), recA.getR41UaeAmt(), recA.getR41Fc1Amt(), recA.getR41Fc2Amt(), recA.getR41Fc3Amt(), recA.getR41Fc4Amt(), recA.getR41Fc5Amt(), recA.getR41Fc6Amt(), recA.getR41Fc7Amt(), recA.getR41Fc8Amt(), recA.getR41Fc9Amt(), recA.getR41Fc10Amt(), recA.getR41Fc11Amt(), recA.getR41Fc12Amt(), recA.getR41Fc13Amt(), recA.getR41Fc14Amt(), recA.getR41Fc15Amt(), recA.getR41Fc16Amt(), recA.getR41Fc17Amt(), recA.getR41Fc18Amt(), recA.getR41Fc19Amt(), recA.getR41Fc20Amt(), recA.getR41Fc21Amt());
                wr(sheet, 41, numStyle,     recA.getR42GlobalAmt(), recA.getR42UaeAmt(), recA.getR42Fc1Amt(), recA.getR42Fc2Amt(), recA.getR42Fc3Amt(), recA.getR42Fc4Amt(), recA.getR42Fc5Amt(), recA.getR42Fc6Amt(), recA.getR42Fc7Amt(), recA.getR42Fc8Amt(), recA.getR42Fc9Amt(), recA.getR42Fc10Amt(), recA.getR42Fc11Amt(), recA.getR42Fc12Amt(), recA.getR42Fc13Amt(), recA.getR42Fc14Amt(), recA.getR42Fc15Amt(), recA.getR42Fc16Amt(), recA.getR42Fc17Amt(), recA.getR42Fc18Amt(), recA.getR42Fc19Amt(), recA.getR42Fc20Amt(), recA.getR42Fc21Amt());
                wr(sheet, 42, greyNumStyle, recA.getR43GlobalAmt(), recA.getR43UaeAmt(), recA.getR43Fc1Amt(), recA.getR43Fc2Amt(), recA.getR43Fc3Amt(), recA.getR43Fc4Amt(), recA.getR43Fc5Amt(), recA.getR43Fc6Amt(), recA.getR43Fc7Amt(), recA.getR43Fc8Amt(), recA.getR43Fc9Amt(), recA.getR43Fc10Amt(), recA.getR43Fc11Amt(), recA.getR43Fc12Amt(), recA.getR43Fc13Amt(), recA.getR43Fc14Amt(), recA.getR43Fc15Amt(), recA.getR43Fc16Amt(), recA.getR43Fc17Amt(), recA.getR43Fc18Amt(), recA.getR43Fc19Amt(), recA.getR43Fc20Amt(), recA.getR43Fc21Amt());
                wr(sheet, 43, numStyle,     recA.getR44GlobalAmt(), recA.getR44UaeAmt(), recA.getR44Fc1Amt(), recA.getR44Fc2Amt(), recA.getR44Fc3Amt(), recA.getR44Fc4Amt(), recA.getR44Fc5Amt(), recA.getR44Fc6Amt(), recA.getR44Fc7Amt(), recA.getR44Fc8Amt(), recA.getR44Fc9Amt(), recA.getR44Fc10Amt(), recA.getR44Fc11Amt(), recA.getR44Fc12Amt(), recA.getR44Fc13Amt(), recA.getR44Fc14Amt(), recA.getR44Fc15Amt(), recA.getR44Fc16Amt(), recA.getR44Fc17Amt(), recA.getR44Fc18Amt(), recA.getR44Fc19Amt(), recA.getR44Fc20Amt(), recA.getR44Fc21Amt());
                wr(sheet, 44, numStyle,     recA.getR45GlobalAmt(), recA.getR45UaeAmt(), recA.getR45Fc1Amt(), recA.getR45Fc2Amt(), recA.getR45Fc3Amt(), recA.getR45Fc4Amt(), recA.getR45Fc5Amt(), recA.getR45Fc6Amt(), recA.getR45Fc7Amt(), recA.getR45Fc8Amt(), recA.getR45Fc9Amt(), recA.getR45Fc10Amt(), recA.getR45Fc11Amt(), recA.getR45Fc12Amt(), recA.getR45Fc13Amt(), recA.getR45Fc14Amt(), recA.getR45Fc15Amt(), recA.getR45Fc16Amt(), recA.getR45Fc17Amt(), recA.getR45Fc18Amt(), recA.getR45Fc19Amt(), recA.getR45Fc20Amt(), recA.getR45Fc21Amt());
                wr(sheet, 45, greyNumStyle, recA.getR46GlobalAmt(), recA.getR46UaeAmt(), recA.getR46Fc1Amt(), recA.getR46Fc2Amt(), recA.getR46Fc3Amt(), recA.getR46Fc4Amt(), recA.getR46Fc5Amt(), recA.getR46Fc6Amt(), recA.getR46Fc7Amt(), recA.getR46Fc8Amt(), recA.getR46Fc9Amt(), recA.getR46Fc10Amt(), recA.getR46Fc11Amt(), recA.getR46Fc12Amt(), recA.getR46Fc13Amt(), recA.getR46Fc14Amt(), recA.getR46Fc15Amt(), recA.getR46Fc16Amt(), recA.getR46Fc17Amt(), recA.getR46Fc18Amt(), recA.getR46Fc19Amt(), recA.getR46Fc20Amt(), recA.getR46Fc21Amt());
                wr(sheet, 46, numStyle,     recA.getR47GlobalAmt(), recA.getR47UaeAmt(), recA.getR47Fc1Amt(), recA.getR47Fc2Amt(), recA.getR47Fc3Amt(), recA.getR47Fc4Amt(), recA.getR47Fc5Amt(), recA.getR47Fc6Amt(), recA.getR47Fc7Amt(), recA.getR47Fc8Amt(), recA.getR47Fc9Amt(), recA.getR47Fc10Amt(), recA.getR47Fc11Amt(), recA.getR47Fc12Amt(), recA.getR47Fc13Amt(), recA.getR47Fc14Amt(), recA.getR47Fc15Amt(), recA.getR47Fc16Amt(), recA.getR47Fc17Amt(), recA.getR47Fc18Amt(), recA.getR47Fc19Amt(), recA.getR47Fc20Amt(), recA.getR47Fc21Amt());
                wr(sheet, 47, numStyle,     recA.getR48GlobalAmt(), recA.getR48UaeAmt(), recA.getR48Fc1Amt(), recA.getR48Fc2Amt(), recA.getR48Fc3Amt(), recA.getR48Fc4Amt(), recA.getR48Fc5Amt(), recA.getR48Fc6Amt(), recA.getR48Fc7Amt(), recA.getR48Fc8Amt(), recA.getR48Fc9Amt(), recA.getR48Fc10Amt(), recA.getR48Fc11Amt(), recA.getR48Fc12Amt(), recA.getR48Fc13Amt(), recA.getR48Fc14Amt(), recA.getR48Fc15Amt(), recA.getR48Fc16Amt(), recA.getR48Fc17Amt(), recA.getR48Fc18Amt(), recA.getR48Fc19Amt(), recA.getR48Fc20Amt(), recA.getR48Fc21Amt());
                wr(sheet, 48, numStyle,     recA.getR49GlobalAmt(), recA.getR49UaeAmt(), recA.getR49Fc1Amt(), recA.getR49Fc2Amt(), recA.getR49Fc3Amt(), recA.getR49Fc4Amt(), recA.getR49Fc5Amt(), recA.getR49Fc6Amt(), recA.getR49Fc7Amt(), recA.getR49Fc8Amt(), recA.getR49Fc9Amt(), recA.getR49Fc10Amt(), recA.getR49Fc11Amt(), recA.getR49Fc12Amt(), recA.getR49Fc13Amt(), recA.getR49Fc14Amt(), recA.getR49Fc15Amt(), recA.getR49Fc16Amt(), recA.getR49Fc17Amt(), recA.getR49Fc18Amt(), recA.getR49Fc19Amt(), recA.getR49Fc20Amt(), recA.getR49Fc21Amt());
                wr(sheet, 49, greyNumStyle, recA.getR50GlobalAmt(), recA.getR50UaeAmt(), recA.getR50Fc1Amt(), recA.getR50Fc2Amt(), recA.getR50Fc3Amt(), recA.getR50Fc4Amt(), recA.getR50Fc5Amt(), recA.getR50Fc6Amt(), recA.getR50Fc7Amt(), recA.getR50Fc8Amt(), recA.getR50Fc9Amt(), recA.getR50Fc10Amt(), recA.getR50Fc11Amt(), recA.getR50Fc12Amt(), recA.getR50Fc13Amt(), recA.getR50Fc14Amt(), recA.getR50Fc15Amt(), recA.getR50Fc16Amt(), recA.getR50Fc17Amt(), recA.getR50Fc18Amt(), recA.getR50Fc19Amt(), recA.getR50Fc20Amt(), recA.getR50Fc21Amt());

                // TABLE B: R51–R91
                wr(sheet, 50, greyNumStyle, recB.getR51GlobalAmt(), recB.getR51UaeAmt(), recB.getR51Fc1Amt(), recB.getR51Fc2Amt(), recB.getR51Fc3Amt(), recB.getR51Fc4Amt(), recB.getR51Fc5Amt(), recB.getR51Fc6Amt(), recB.getR51Fc7Amt(), recB.getR51Fc8Amt(), recB.getR51Fc9Amt(), recB.getR51Fc10Amt(), recB.getR51Fc11Amt(), recB.getR51Fc12Amt(), recB.getR51Fc13Amt(), recB.getR51Fc14Amt(), recB.getR51Fc15Amt(), recB.getR51Fc16Amt(), recB.getR51Fc17Amt(), recB.getR51Fc18Amt(), recB.getR51Fc19Amt(), recB.getR51Fc20Amt(), recB.getR51Fc21Amt());
                wr(sheet, 51, numStyle,     recB.getR52GlobalAmt(), recB.getR52UaeAmt(), recB.getR52Fc1Amt(), recB.getR52Fc2Amt(), recB.getR52Fc3Amt(), recB.getR52Fc4Amt(), recB.getR52Fc5Amt(), recB.getR52Fc6Amt(), recB.getR52Fc7Amt(), recB.getR52Fc8Amt(), recB.getR52Fc9Amt(), recB.getR52Fc10Amt(), recB.getR52Fc11Amt(), recB.getR52Fc12Amt(), recB.getR52Fc13Amt(), recB.getR52Fc14Amt(), recB.getR52Fc15Amt(), recB.getR52Fc16Amt(), recB.getR52Fc17Amt(), recB.getR52Fc18Amt(), recB.getR52Fc19Amt(), recB.getR52Fc20Amt(), recB.getR52Fc21Amt());
                wr(sheet, 52, numStyle,     recB.getR53GlobalAmt(), recB.getR53UaeAmt(), recB.getR53Fc1Amt(), recB.getR53Fc2Amt(), recB.getR53Fc3Amt(), recB.getR53Fc4Amt(), recB.getR53Fc5Amt(), recB.getR53Fc6Amt(), recB.getR53Fc7Amt(), recB.getR53Fc8Amt(), recB.getR53Fc9Amt(), recB.getR53Fc10Amt(), recB.getR53Fc11Amt(), recB.getR53Fc12Amt(), recB.getR53Fc13Amt(), recB.getR53Fc14Amt(), recB.getR53Fc15Amt(), recB.getR53Fc16Amt(), recB.getR53Fc17Amt(), recB.getR53Fc18Amt(), recB.getR53Fc19Amt(), recB.getR53Fc20Amt(), recB.getR53Fc21Amt());
                wr(sheet, 53, numStyle,     recB.getR54GlobalAmt(), recB.getR54UaeAmt(), recB.getR54Fc1Amt(), recB.getR54Fc2Amt(), recB.getR54Fc3Amt(), recB.getR54Fc4Amt(), recB.getR54Fc5Amt(), recB.getR54Fc6Amt(), recB.getR54Fc7Amt(), recB.getR54Fc8Amt(), recB.getR54Fc9Amt(), recB.getR54Fc10Amt(), recB.getR54Fc11Amt(), recB.getR54Fc12Amt(), recB.getR54Fc13Amt(), recB.getR54Fc14Amt(), recB.getR54Fc15Amt(), recB.getR54Fc16Amt(), recB.getR54Fc17Amt(), recB.getR54Fc18Amt(), recB.getR54Fc19Amt(), recB.getR54Fc20Amt(), recB.getR54Fc21Amt());
                wr(sheet, 54, greyNumStyle, recB.getR55GlobalAmt(), recB.getR55UaeAmt(), recB.getR55Fc1Amt(), recB.getR55Fc2Amt(), recB.getR55Fc3Amt(), recB.getR55Fc4Amt(), recB.getR55Fc5Amt(), recB.getR55Fc6Amt(), recB.getR55Fc7Amt(), recB.getR55Fc8Amt(), recB.getR55Fc9Amt(), recB.getR55Fc10Amt(), recB.getR55Fc11Amt(), recB.getR55Fc12Amt(), recB.getR55Fc13Amt(), recB.getR55Fc14Amt(), recB.getR55Fc15Amt(), recB.getR55Fc16Amt(), recB.getR55Fc17Amt(), recB.getR55Fc18Amt(), recB.getR55Fc19Amt(), recB.getR55Fc20Amt(), recB.getR55Fc21Amt());
                wr(sheet, 55, numStyle,     recB.getR56GlobalAmt(), recB.getR56UaeAmt(), recB.getR56Fc1Amt(), recB.getR56Fc2Amt(), recB.getR56Fc3Amt(), recB.getR56Fc4Amt(), recB.getR56Fc5Amt(), recB.getR56Fc6Amt(), recB.getR56Fc7Amt(), recB.getR56Fc8Amt(), recB.getR56Fc9Amt(), recB.getR56Fc10Amt(), recB.getR56Fc11Amt(), recB.getR56Fc12Amt(), recB.getR56Fc13Amt(), recB.getR56Fc14Amt(), recB.getR56Fc15Amt(), recB.getR56Fc16Amt(), recB.getR56Fc17Amt(), recB.getR56Fc18Amt(), recB.getR56Fc19Amt(), recB.getR56Fc20Amt(), recB.getR56Fc21Amt());
                wr(sheet, 56, greyNumStyle, recB.getR57GlobalAmt(), recB.getR57UaeAmt(), recB.getR57Fc1Amt(), recB.getR57Fc2Amt(), recB.getR57Fc3Amt(), recB.getR57Fc4Amt(), recB.getR57Fc5Amt(), recB.getR57Fc6Amt(), recB.getR57Fc7Amt(), recB.getR57Fc8Amt(), recB.getR57Fc9Amt(), recB.getR57Fc10Amt(), recB.getR57Fc11Amt(), recB.getR57Fc12Amt(), recB.getR57Fc13Amt(), recB.getR57Fc14Amt(), recB.getR57Fc15Amt(), recB.getR57Fc16Amt(), recB.getR57Fc17Amt(), recB.getR57Fc18Amt(), recB.getR57Fc19Amt(), recB.getR57Fc20Amt(), recB.getR57Fc21Amt());
                wr(sheet, 57, numStyle,     recB.getR58GlobalAmt(), recB.getR58UaeAmt(), recB.getR58Fc1Amt(), recB.getR58Fc2Amt(), recB.getR58Fc3Amt(), recB.getR58Fc4Amt(), recB.getR58Fc5Amt(), recB.getR58Fc6Amt(), recB.getR58Fc7Amt(), recB.getR58Fc8Amt(), recB.getR58Fc9Amt(), recB.getR58Fc10Amt(), recB.getR58Fc11Amt(), recB.getR58Fc12Amt(), recB.getR58Fc13Amt(), recB.getR58Fc14Amt(), recB.getR58Fc15Amt(), recB.getR58Fc16Amt(), recB.getR58Fc17Amt(), recB.getR58Fc18Amt(), recB.getR58Fc19Amt(), recB.getR58Fc20Amt(), recB.getR58Fc21Amt());
                wr(sheet, 58, numStyle,     recB.getR59GlobalAmt(), recB.getR59UaeAmt(), recB.getR59Fc1Amt(), recB.getR59Fc2Amt(), recB.getR59Fc3Amt(), recB.getR59Fc4Amt(), recB.getR59Fc5Amt(), recB.getR59Fc6Amt(), recB.getR59Fc7Amt(), recB.getR59Fc8Amt(), recB.getR59Fc9Amt(), recB.getR59Fc10Amt(), recB.getR59Fc11Amt(), recB.getR59Fc12Amt(), recB.getR59Fc13Amt(), recB.getR59Fc14Amt(), recB.getR59Fc15Amt(), recB.getR59Fc16Amt(), recB.getR59Fc17Amt(), recB.getR59Fc18Amt(), recB.getR59Fc19Amt(), recB.getR59Fc20Amt(), recB.getR59Fc21Amt());
                wr(sheet, 59, numStyle,     recB.getR60GlobalAmt(), recB.getR60UaeAmt(), recB.getR60Fc1Amt(), recB.getR60Fc2Amt(), recB.getR60Fc3Amt(), recB.getR60Fc4Amt(), recB.getR60Fc5Amt(), recB.getR60Fc6Amt(), recB.getR60Fc7Amt(), recB.getR60Fc8Amt(), recB.getR60Fc9Amt(), recB.getR60Fc10Amt(), recB.getR60Fc11Amt(), recB.getR60Fc12Amt(), recB.getR60Fc13Amt(), recB.getR60Fc14Amt(), recB.getR60Fc15Amt(), recB.getR60Fc16Amt(), recB.getR60Fc17Amt(), recB.getR60Fc18Amt(), recB.getR60Fc19Amt(), recB.getR60Fc20Amt(), recB.getR60Fc21Amt());
                wr(sheet, 60, numStyle,     recB.getR61GlobalAmt(), recB.getR61UaeAmt(), recB.getR61Fc1Amt(), recB.getR61Fc2Amt(), recB.getR61Fc3Amt(), recB.getR61Fc4Amt(), recB.getR61Fc5Amt(), recB.getR61Fc6Amt(), recB.getR61Fc7Amt(), recB.getR61Fc8Amt(), recB.getR61Fc9Amt(), recB.getR61Fc10Amt(), recB.getR61Fc11Amt(), recB.getR61Fc12Amt(), recB.getR61Fc13Amt(), recB.getR61Fc14Amt(), recB.getR61Fc15Amt(), recB.getR61Fc16Amt(), recB.getR61Fc17Amt(), recB.getR61Fc18Amt(), recB.getR61Fc19Amt(), recB.getR61Fc20Amt(), recB.getR61Fc21Amt());
                wr(sheet, 61, numStyle,     recB.getR62GlobalAmt(), recB.getR62UaeAmt(), recB.getR62Fc1Amt(), recB.getR62Fc2Amt(), recB.getR62Fc3Amt(), recB.getR62Fc4Amt(), recB.getR62Fc5Amt(), recB.getR62Fc6Amt(), recB.getR62Fc7Amt(), recB.getR62Fc8Amt(), recB.getR62Fc9Amt(), recB.getR62Fc10Amt(), recB.getR62Fc11Amt(), recB.getR62Fc12Amt(), recB.getR62Fc13Amt(), recB.getR62Fc14Amt(), recB.getR62Fc15Amt(), recB.getR62Fc16Amt(), recB.getR62Fc17Amt(), recB.getR62Fc18Amt(), recB.getR62Fc19Amt(), recB.getR62Fc20Amt(), recB.getR62Fc21Amt());
                wr(sheet, 62, numStyle,     recB.getR63GlobalAmt(), recB.getR63UaeAmt(), recB.getR63Fc1Amt(), recB.getR63Fc2Amt(), recB.getR63Fc3Amt(), recB.getR63Fc4Amt(), recB.getR63Fc5Amt(), recB.getR63Fc6Amt(), recB.getR63Fc7Amt(), recB.getR63Fc8Amt(), recB.getR63Fc9Amt(), recB.getR63Fc10Amt(), recB.getR63Fc11Amt(), recB.getR63Fc12Amt(), recB.getR63Fc13Amt(), recB.getR63Fc14Amt(), recB.getR63Fc15Amt(), recB.getR63Fc16Amt(), recB.getR63Fc17Amt(), recB.getR63Fc18Amt(), recB.getR63Fc19Amt(), recB.getR63Fc20Amt(), recB.getR63Fc21Amt());
                wr(sheet, 63, numStyle,     recB.getR64GlobalAmt(), recB.getR64UaeAmt(), recB.getR64Fc1Amt(), recB.getR64Fc2Amt(), recB.getR64Fc3Amt(), recB.getR64Fc4Amt(), recB.getR64Fc5Amt(), recB.getR64Fc6Amt(), recB.getR64Fc7Amt(), recB.getR64Fc8Amt(), recB.getR64Fc9Amt(), recB.getR64Fc10Amt(), recB.getR64Fc11Amt(), recB.getR64Fc12Amt(), recB.getR64Fc13Amt(), recB.getR64Fc14Amt(), recB.getR64Fc15Amt(), recB.getR64Fc16Amt(), recB.getR64Fc17Amt(), recB.getR64Fc18Amt(), recB.getR64Fc19Amt(), recB.getR64Fc20Amt(), recB.getR64Fc21Amt());
                wr(sheet, 64, greyNumStyle, recB.getR65GlobalAmt(), recB.getR65UaeAmt(), recB.getR65Fc1Amt(), recB.getR65Fc2Amt(), recB.getR65Fc3Amt(), recB.getR65Fc4Amt(), recB.getR65Fc5Amt(), recB.getR65Fc6Amt(), recB.getR65Fc7Amt(), recB.getR65Fc8Amt(), recB.getR65Fc9Amt(), recB.getR65Fc10Amt(), recB.getR65Fc11Amt(), recB.getR65Fc12Amt(), recB.getR65Fc13Amt(), recB.getR65Fc14Amt(), recB.getR65Fc15Amt(), recB.getR65Fc16Amt(), recB.getR65Fc17Amt(), recB.getR65Fc18Amt(), recB.getR65Fc19Amt(), recB.getR65Fc20Amt(), recB.getR65Fc21Amt());
                wr(sheet, 65, numStyle,     recB.getR66GlobalAmt(), recB.getR66UaeAmt(), recB.getR66Fc1Amt(), recB.getR66Fc2Amt(), recB.getR66Fc3Amt(), recB.getR66Fc4Amt(), recB.getR66Fc5Amt(), recB.getR66Fc6Amt(), recB.getR66Fc7Amt(), recB.getR66Fc8Amt(), recB.getR66Fc9Amt(), recB.getR66Fc10Amt(), recB.getR66Fc11Amt(), recB.getR66Fc12Amt(), recB.getR66Fc13Amt(), recB.getR66Fc14Amt(), recB.getR66Fc15Amt(), recB.getR66Fc16Amt(), recB.getR66Fc17Amt(), recB.getR66Fc18Amt(), recB.getR66Fc19Amt(), recB.getR66Fc20Amt(), recB.getR66Fc21Amt());
                wr(sheet, 66, numStyle,     recB.getR67GlobalAmt(), recB.getR67UaeAmt(), recB.getR67Fc1Amt(), recB.getR67Fc2Amt(), recB.getR67Fc3Amt(), recB.getR67Fc4Amt(), recB.getR67Fc5Amt(), recB.getR67Fc6Amt(), recB.getR67Fc7Amt(), recB.getR67Fc8Amt(), recB.getR67Fc9Amt(), recB.getR67Fc10Amt(), recB.getR67Fc11Amt(), recB.getR67Fc12Amt(), recB.getR67Fc13Amt(), recB.getR67Fc14Amt(), recB.getR67Fc15Amt(), recB.getR67Fc16Amt(), recB.getR67Fc17Amt(), recB.getR67Fc18Amt(), recB.getR67Fc19Amt(), recB.getR67Fc20Amt(), recB.getR67Fc21Amt());
                wr(sheet, 67, numStyle,     recB.getR68GlobalAmt(), recB.getR68UaeAmt(), recB.getR68Fc1Amt(), recB.getR68Fc2Amt(), recB.getR68Fc3Amt(), recB.getR68Fc4Amt(), recB.getR68Fc5Amt(), recB.getR68Fc6Amt(), recB.getR68Fc7Amt(), recB.getR68Fc8Amt(), recB.getR68Fc9Amt(), recB.getR68Fc10Amt(), recB.getR68Fc11Amt(), recB.getR68Fc12Amt(), recB.getR68Fc13Amt(), recB.getR68Fc14Amt(), recB.getR68Fc15Amt(), recB.getR68Fc16Amt(), recB.getR68Fc17Amt(), recB.getR68Fc18Amt(), recB.getR68Fc19Amt(), recB.getR68Fc20Amt(), recB.getR68Fc21Amt());
                wr(sheet, 68, numStyle,     recB.getR69GlobalAmt(), recB.getR69UaeAmt(), recB.getR69Fc1Amt(), recB.getR69Fc2Amt(), recB.getR69Fc3Amt(), recB.getR69Fc4Amt(), recB.getR69Fc5Amt(), recB.getR69Fc6Amt(), recB.getR69Fc7Amt(), recB.getR69Fc8Amt(), recB.getR69Fc9Amt(), recB.getR69Fc10Amt(), recB.getR69Fc11Amt(), recB.getR69Fc12Amt(), recB.getR69Fc13Amt(), recB.getR69Fc14Amt(), recB.getR69Fc15Amt(), recB.getR69Fc16Amt(), recB.getR69Fc17Amt(), recB.getR69Fc18Amt(), recB.getR69Fc19Amt(), recB.getR69Fc20Amt(), recB.getR69Fc21Amt());
                wr(sheet, 69, numStyle,     recB.getR70GlobalAmt(), recB.getR70UaeAmt(), recB.getR70Fc1Amt(), recB.getR70Fc2Amt(), recB.getR70Fc3Amt(), recB.getR70Fc4Amt(), recB.getR70Fc5Amt(), recB.getR70Fc6Amt(), recB.getR70Fc7Amt(), recB.getR70Fc8Amt(), recB.getR70Fc9Amt(), recB.getR70Fc10Amt(), recB.getR70Fc11Amt(), recB.getR70Fc12Amt(), recB.getR70Fc13Amt(), recB.getR70Fc14Amt(), recB.getR70Fc15Amt(), recB.getR70Fc16Amt(), recB.getR70Fc17Amt(), recB.getR70Fc18Amt(), recB.getR70Fc19Amt(), recB.getR70Fc20Amt(), recB.getR70Fc21Amt());
                wr(sheet, 70, numStyle,     recB.getR71GlobalAmt(), recB.getR71UaeAmt(), recB.getR71Fc1Amt(), recB.getR71Fc2Amt(), recB.getR71Fc3Amt(), recB.getR71Fc4Amt(), recB.getR71Fc5Amt(), recB.getR71Fc6Amt(), recB.getR71Fc7Amt(), recB.getR71Fc8Amt(), recB.getR71Fc9Amt(), recB.getR71Fc10Amt(), recB.getR71Fc11Amt(), recB.getR71Fc12Amt(), recB.getR71Fc13Amt(), recB.getR71Fc14Amt(), recB.getR71Fc15Amt(), recB.getR71Fc16Amt(), recB.getR71Fc17Amt(), recB.getR71Fc18Amt(), recB.getR71Fc19Amt(), recB.getR71Fc20Amt(), recB.getR71Fc21Amt());
                wr(sheet, 71, numStyle,     recB.getR72GlobalAmt(), recB.getR72UaeAmt(), recB.getR72Fc1Amt(), recB.getR72Fc2Amt(), recB.getR72Fc3Amt(), recB.getR72Fc4Amt(), recB.getR72Fc5Amt(), recB.getR72Fc6Amt(), recB.getR72Fc7Amt(), recB.getR72Fc8Amt(), recB.getR72Fc9Amt(), recB.getR72Fc10Amt(), recB.getR72Fc11Amt(), recB.getR72Fc12Amt(), recB.getR72Fc13Amt(), recB.getR72Fc14Amt(), recB.getR72Fc15Amt(), recB.getR72Fc16Amt(), recB.getR72Fc17Amt(), recB.getR72Fc18Amt(), recB.getR72Fc19Amt(), recB.getR72Fc20Amt(), recB.getR72Fc21Amt());
                wr(sheet, 72, numStyle,     recB.getR73GlobalAmt(), recB.getR73UaeAmt(), recB.getR73Fc1Amt(), recB.getR73Fc2Amt(), recB.getR73Fc3Amt(), recB.getR73Fc4Amt(), recB.getR73Fc5Amt(), recB.getR73Fc6Amt(), recB.getR73Fc7Amt(), recB.getR73Fc8Amt(), recB.getR73Fc9Amt(), recB.getR73Fc10Amt(), recB.getR73Fc11Amt(), recB.getR73Fc12Amt(), recB.getR73Fc13Amt(), recB.getR73Fc14Amt(), recB.getR73Fc15Amt(), recB.getR73Fc16Amt(), recB.getR73Fc17Amt(), recB.getR73Fc18Amt(), recB.getR73Fc19Amt(), recB.getR73Fc20Amt(), recB.getR73Fc21Amt());
                wr(sheet, 73, numStyle,     recB.getR74GlobalAmt(), recB.getR74UaeAmt(), recB.getR74Fc1Amt(), recB.getR74Fc2Amt(), recB.getR74Fc3Amt(), recB.getR74Fc4Amt(), recB.getR74Fc5Amt(), recB.getR74Fc6Amt(), recB.getR74Fc7Amt(), recB.getR74Fc8Amt(), recB.getR74Fc9Amt(), recB.getR74Fc10Amt(), recB.getR74Fc11Amt(), recB.getR74Fc12Amt(), recB.getR74Fc13Amt(), recB.getR74Fc14Amt(), recB.getR74Fc15Amt(), recB.getR74Fc16Amt(), recB.getR74Fc17Amt(), recB.getR74Fc18Amt(), recB.getR74Fc19Amt(), recB.getR74Fc20Amt(), recB.getR74Fc21Amt());
                wr(sheet, 74, greyNumStyle, recB.getR75GlobalAmt(), recB.getR75UaeAmt(), recB.getR75Fc1Amt(), recB.getR75Fc2Amt(), recB.getR75Fc3Amt(), recB.getR75Fc4Amt(), recB.getR75Fc5Amt(), recB.getR75Fc6Amt(), recB.getR75Fc7Amt(), recB.getR75Fc8Amt(), recB.getR75Fc9Amt(), recB.getR75Fc10Amt(), recB.getR75Fc11Amt(), recB.getR75Fc12Amt(), recB.getR75Fc13Amt(), recB.getR75Fc14Amt(), recB.getR75Fc15Amt(), recB.getR75Fc16Amt(), recB.getR75Fc17Amt(), recB.getR75Fc18Amt(), recB.getR75Fc19Amt(), recB.getR75Fc20Amt(), recB.getR75Fc21Amt());
                wr(sheet, 75, numStyle,     recB.getR76GlobalAmt(), recB.getR76UaeAmt(), recB.getR76Fc1Amt(), recB.getR76Fc2Amt(), recB.getR76Fc3Amt(), recB.getR76Fc4Amt(), recB.getR76Fc5Amt(), recB.getR76Fc6Amt(), recB.getR76Fc7Amt(), recB.getR76Fc8Amt(), recB.getR76Fc9Amt(), recB.getR76Fc10Amt(), recB.getR76Fc11Amt(), recB.getR76Fc12Amt(), recB.getR76Fc13Amt(), recB.getR76Fc14Amt(), recB.getR76Fc15Amt(), recB.getR76Fc16Amt(), recB.getR76Fc17Amt(), recB.getR76Fc18Amt(), recB.getR76Fc19Amt(), recB.getR76Fc20Amt(), recB.getR76Fc21Amt());
                wr(sheet, 76, numStyle,     recB.getR77GlobalAmt(), recB.getR77UaeAmt(), recB.getR77Fc1Amt(), recB.getR77Fc2Amt(), recB.getR77Fc3Amt(), recB.getR77Fc4Amt(), recB.getR77Fc5Amt(), recB.getR77Fc6Amt(), recB.getR77Fc7Amt(), recB.getR77Fc8Amt(), recB.getR77Fc9Amt(), recB.getR77Fc10Amt(), recB.getR77Fc11Amt(), recB.getR77Fc12Amt(), recB.getR77Fc13Amt(), recB.getR77Fc14Amt(), recB.getR77Fc15Amt(), recB.getR77Fc16Amt(), recB.getR77Fc17Amt(), recB.getR77Fc18Amt(), recB.getR77Fc19Amt(), recB.getR77Fc20Amt(), recB.getR77Fc21Amt());
                wr(sheet, 77, numStyle,     recB.getR78GlobalAmt(), recB.getR78UaeAmt(), recB.getR78Fc1Amt(), recB.getR78Fc2Amt(), recB.getR78Fc3Amt(), recB.getR78Fc4Amt(), recB.getR78Fc5Amt(), recB.getR78Fc6Amt(), recB.getR78Fc7Amt(), recB.getR78Fc8Amt(), recB.getR78Fc9Amt(), recB.getR78Fc10Amt(), recB.getR78Fc11Amt(), recB.getR78Fc12Amt(), recB.getR78Fc13Amt(), recB.getR78Fc14Amt(), recB.getR78Fc15Amt(), recB.getR78Fc16Amt(), recB.getR78Fc17Amt(), recB.getR78Fc18Amt(), recB.getR78Fc19Amt(), recB.getR78Fc20Amt(), recB.getR78Fc21Amt());
                wr(sheet, 78, numStyle,     recB.getR79GlobalAmt(), recB.getR79UaeAmt(), recB.getR79Fc1Amt(), recB.getR79Fc2Amt(), recB.getR79Fc3Amt(), recB.getR79Fc4Amt(), recB.getR79Fc5Amt(), recB.getR79Fc6Amt(), recB.getR79Fc7Amt(), recB.getR79Fc8Amt(), recB.getR79Fc9Amt(), recB.getR79Fc10Amt(), recB.getR79Fc11Amt(), recB.getR79Fc12Amt(), recB.getR79Fc13Amt(), recB.getR79Fc14Amt(), recB.getR79Fc15Amt(), recB.getR79Fc16Amt(), recB.getR79Fc17Amt(), recB.getR79Fc18Amt(), recB.getR79Fc19Amt(), recB.getR79Fc20Amt(), recB.getR79Fc21Amt());
                wr(sheet, 79, numStyle,     recB.getR80GlobalAmt(), recB.getR80UaeAmt(), recB.getR80Fc1Amt(), recB.getR80Fc2Amt(), recB.getR80Fc3Amt(), recB.getR80Fc4Amt(), recB.getR80Fc5Amt(), recB.getR80Fc6Amt(), recB.getR80Fc7Amt(), recB.getR80Fc8Amt(), recB.getR80Fc9Amt(), recB.getR80Fc10Amt(), recB.getR80Fc11Amt(), recB.getR80Fc12Amt(), recB.getR80Fc13Amt(), recB.getR80Fc14Amt(), recB.getR80Fc15Amt(), recB.getR80Fc16Amt(), recB.getR80Fc17Amt(), recB.getR80Fc18Amt(), recB.getR80Fc19Amt(), recB.getR80Fc20Amt(), recB.getR80Fc21Amt());
                wr(sheet, 80, numStyle,     recB.getR81GlobalAmt(), recB.getR81UaeAmt(), recB.getR81Fc1Amt(), recB.getR81Fc2Amt(), recB.getR81Fc3Amt(), recB.getR81Fc4Amt(), recB.getR81Fc5Amt(), recB.getR81Fc6Amt(), recB.getR81Fc7Amt(), recB.getR81Fc8Amt(), recB.getR81Fc9Amt(), recB.getR81Fc10Amt(), recB.getR81Fc11Amt(), recB.getR81Fc12Amt(), recB.getR81Fc13Amt(), recB.getR81Fc14Amt(), recB.getR81Fc15Amt(), recB.getR81Fc16Amt(), recB.getR81Fc17Amt(), recB.getR81Fc18Amt(), recB.getR81Fc19Amt(), recB.getR81Fc20Amt(), recB.getR81Fc21Amt());
                wr(sheet, 81, numStyle,     recB.getR82GlobalAmt(), recB.getR82UaeAmt(), recB.getR82Fc1Amt(), recB.getR82Fc2Amt(), recB.getR82Fc3Amt(), recB.getR82Fc4Amt(), recB.getR82Fc5Amt(), recB.getR82Fc6Amt(), recB.getR82Fc7Amt(), recB.getR82Fc8Amt(), recB.getR82Fc9Amt(), recB.getR82Fc10Amt(), recB.getR82Fc11Amt(), recB.getR82Fc12Amt(), recB.getR82Fc13Amt(), recB.getR82Fc14Amt(), recB.getR82Fc15Amt(), recB.getR82Fc16Amt(), recB.getR82Fc17Amt(), recB.getR82Fc18Amt(), recB.getR82Fc19Amt(), recB.getR82Fc20Amt(), recB.getR82Fc21Amt());
                wr(sheet, 82, greyNumStyle, recB.getR83GlobalAmt(), recB.getR83UaeAmt(), recB.getR83Fc1Amt(), recB.getR83Fc2Amt(), recB.getR83Fc3Amt(), recB.getR83Fc4Amt(), recB.getR83Fc5Amt(), recB.getR83Fc6Amt(), recB.getR83Fc7Amt(), recB.getR83Fc8Amt(), recB.getR83Fc9Amt(), recB.getR83Fc10Amt(), recB.getR83Fc11Amt(), recB.getR83Fc12Amt(), recB.getR83Fc13Amt(), recB.getR83Fc14Amt(), recB.getR83Fc15Amt(), recB.getR83Fc16Amt(), recB.getR83Fc17Amt(), recB.getR83Fc18Amt(), recB.getR83Fc19Amt(), recB.getR83Fc20Amt(), recB.getR83Fc21Amt());
                wr(sheet, 83, greyNumStyle, recB.getR84GlobalAmt(), recB.getR84UaeAmt(), recB.getR84Fc1Amt(), recB.getR84Fc2Amt(), recB.getR84Fc3Amt(), recB.getR84Fc4Amt(), recB.getR84Fc5Amt(), recB.getR84Fc6Amt(), recB.getR84Fc7Amt(), recB.getR84Fc8Amt(), recB.getR84Fc9Amt(), recB.getR84Fc10Amt(), recB.getR84Fc11Amt(), recB.getR84Fc12Amt(), recB.getR84Fc13Amt(), recB.getR84Fc14Amt(), recB.getR84Fc15Amt(), recB.getR84Fc16Amt(), recB.getR84Fc17Amt(), recB.getR84Fc18Amt(), recB.getR84Fc19Amt(), recB.getR84Fc20Amt(), recB.getR84Fc21Amt());
                wr(sheet, 84, numStyle,     recB.getR85GlobalAmt(), recB.getR85UaeAmt(), recB.getR85Fc1Amt(), recB.getR85Fc2Amt(), recB.getR85Fc3Amt(), recB.getR85Fc4Amt(), recB.getR85Fc5Amt(), recB.getR85Fc6Amt(), recB.getR85Fc7Amt(), recB.getR85Fc8Amt(), recB.getR85Fc9Amt(), recB.getR85Fc10Amt(), recB.getR85Fc11Amt(), recB.getR85Fc12Amt(), recB.getR85Fc13Amt(), recB.getR85Fc14Amt(), recB.getR85Fc15Amt(), recB.getR85Fc16Amt(), recB.getR85Fc17Amt(), recB.getR85Fc18Amt(), recB.getR85Fc19Amt(), recB.getR85Fc20Amt(), recB.getR85Fc21Amt());
                wr(sheet, 85, numStyle,     recB.getR86GlobalAmt(), recB.getR86UaeAmt(), recB.getR86Fc1Amt(), recB.getR86Fc2Amt(), recB.getR86Fc3Amt(), recB.getR86Fc4Amt(), recB.getR86Fc5Amt(), recB.getR86Fc6Amt(), recB.getR86Fc7Amt(), recB.getR86Fc8Amt(), recB.getR86Fc9Amt(), recB.getR86Fc10Amt(), recB.getR86Fc11Amt(), recB.getR86Fc12Amt(), recB.getR86Fc13Amt(), recB.getR86Fc14Amt(), recB.getR86Fc15Amt(), recB.getR86Fc16Amt(), recB.getR86Fc17Amt(), recB.getR86Fc18Amt(), recB.getR86Fc19Amt(), recB.getR86Fc20Amt(), recB.getR86Fc21Amt());
                wr(sheet, 86, greyNumStyle, recB.getR87GlobalAmt(), recB.getR87UaeAmt(), recB.getR87Fc1Amt(), recB.getR87Fc2Amt(), recB.getR87Fc3Amt(), recB.getR87Fc4Amt(), recB.getR87Fc5Amt(), recB.getR87Fc6Amt(), recB.getR87Fc7Amt(), recB.getR87Fc8Amt(), recB.getR87Fc9Amt(), recB.getR87Fc10Amt(), recB.getR87Fc11Amt(), recB.getR87Fc12Amt(), recB.getR87Fc13Amt(), recB.getR87Fc14Amt(), recB.getR87Fc15Amt(), recB.getR87Fc16Amt(), recB.getR87Fc17Amt(), recB.getR87Fc18Amt(), recB.getR87Fc19Amt(), recB.getR87Fc20Amt(), recB.getR87Fc21Amt());
                wr(sheet, 87, numStyle,     recB.getR88GlobalAmt(), recB.getR88UaeAmt(), recB.getR88Fc1Amt(), recB.getR88Fc2Amt(), recB.getR88Fc3Amt(), recB.getR88Fc4Amt(), recB.getR88Fc5Amt(), recB.getR88Fc6Amt(), recB.getR88Fc7Amt(), recB.getR88Fc8Amt(), recB.getR88Fc9Amt(), recB.getR88Fc10Amt(), recB.getR88Fc11Amt(), recB.getR88Fc12Amt(), recB.getR88Fc13Amt(), recB.getR88Fc14Amt(), recB.getR88Fc15Amt(), recB.getR88Fc16Amt(), recB.getR88Fc17Amt(), recB.getR88Fc18Amt(), recB.getR88Fc19Amt(), recB.getR88Fc20Amt(), recB.getR88Fc21Amt());
                wr(sheet, 88, numStyle,     recB.getR89GlobalAmt(), recB.getR89UaeAmt(), recB.getR89Fc1Amt(), recB.getR89Fc2Amt(), recB.getR89Fc3Amt(), recB.getR89Fc4Amt(), recB.getR89Fc5Amt(), recB.getR89Fc6Amt(), recB.getR89Fc7Amt(), recB.getR89Fc8Amt(), recB.getR89Fc9Amt(), recB.getR89Fc10Amt(), recB.getR89Fc11Amt(), recB.getR89Fc12Amt(), recB.getR89Fc13Amt(), recB.getR89Fc14Amt(), recB.getR89Fc15Amt(), recB.getR89Fc16Amt(), recB.getR89Fc17Amt(), recB.getR89Fc18Amt(), recB.getR89Fc19Amt(), recB.getR89Fc20Amt(), recB.getR89Fc21Amt());
                wr(sheet, 89, numStyle,     recB.getR90GlobalAmt(), recB.getR90UaeAmt(), recB.getR90Fc1Amt(), recB.getR90Fc2Amt(), recB.getR90Fc3Amt(), recB.getR90Fc4Amt(), recB.getR90Fc5Amt(), recB.getR90Fc6Amt(), recB.getR90Fc7Amt(), recB.getR90Fc8Amt(), recB.getR90Fc9Amt(), recB.getR90Fc10Amt(), recB.getR90Fc11Amt(), recB.getR90Fc12Amt(), recB.getR90Fc13Amt(), recB.getR90Fc14Amt(), recB.getR90Fc15Amt(), recB.getR90Fc16Amt(), recB.getR90Fc17Amt(), recB.getR90Fc18Amt(), recB.getR90Fc19Amt(), recB.getR90Fc20Amt(), recB.getR90Fc21Amt());
                wr(sheet, 90, numStyle,     recB.getR91GlobalAmt(), recB.getR91UaeAmt(), recB.getR91Fc1Amt(), recB.getR91Fc2Amt(), recB.getR91Fc3Amt(), recB.getR91Fc4Amt(), recB.getR91Fc5Amt(), recB.getR91Fc6Amt(), recB.getR91Fc7Amt(), recB.getR91Fc8Amt(), recB.getR91Fc9Amt(), recB.getR91Fc10Amt(), recB.getR91Fc11Amt(), recB.getR91Fc12Amt(), recB.getR91Fc13Amt(), recB.getR91Fc14Amt(), recB.getR91Fc15Amt(), recB.getR91Fc16Amt(), recB.getR91Fc17Amt(), recB.getR91Fc18Amt(), recB.getR91Fc19Amt(), recB.getR91Fc20Amt(), recB.getR91Fc21Amt());

                // TABLE C: R92–R95
                wr(sheet, 91, numStyle,     recC.getR92GlobalAmt(), recC.getR92UaeAmt(), recC.getR92Fc1Amt(), recC.getR92Fc2Amt(), recC.getR92Fc3Amt(), recC.getR92Fc4Amt(), recC.getR92Fc5Amt(), recC.getR92Fc6Amt(), recC.getR92Fc7Amt(), recC.getR92Fc8Amt(), recC.getR92Fc9Amt(), recC.getR92Fc10Amt(), recC.getR92Fc11Amt(), recC.getR92Fc12Amt(), recC.getR92Fc13Amt(), recC.getR92Fc14Amt(), recC.getR92Fc15Amt(), recC.getR92Fc16Amt(), recC.getR92Fc17Amt(), recC.getR92Fc18Amt(), recC.getR92Fc19Amt(), recC.getR92Fc20Amt(), recC.getR92Fc21Amt());
                wr(sheet, 92, numStyle,     recC.getR93GlobalAmt(), recC.getR93UaeAmt(), recC.getR93Fc1Amt(), recC.getR93Fc2Amt(), recC.getR93Fc3Amt(), recC.getR93Fc4Amt(), recC.getR93Fc5Amt(), recC.getR93Fc6Amt(), recC.getR93Fc7Amt(), recC.getR93Fc8Amt(), recC.getR93Fc9Amt(), recC.getR93Fc10Amt(), recC.getR93Fc11Amt(), recC.getR93Fc12Amt(), recC.getR93Fc13Amt(), recC.getR93Fc14Amt(), recC.getR93Fc15Amt(), recC.getR93Fc16Amt(), recC.getR93Fc17Amt(), recC.getR93Fc18Amt(), recC.getR93Fc19Amt(), recC.getR93Fc20Amt(), recC.getR93Fc21Amt());
                wr(sheet, 93, numStyle,     recC.getR94GlobalAmt(), recC.getR94UaeAmt(), recC.getR94Fc1Amt(), recC.getR94Fc2Amt(), recC.getR94Fc3Amt(), recC.getR94Fc4Amt(), recC.getR94Fc5Amt(), recC.getR94Fc6Amt(), recC.getR94Fc7Amt(), recC.getR94Fc8Amt(), recC.getR94Fc9Amt(), recC.getR94Fc10Amt(), recC.getR94Fc11Amt(), recC.getR94Fc12Amt(), recC.getR94Fc13Amt(), recC.getR94Fc14Amt(), recC.getR94Fc15Amt(), recC.getR94Fc16Amt(), recC.getR94Fc17Amt(), recC.getR94Fc18Amt(), recC.getR94Fc19Amt(), recC.getR94Fc20Amt(), recC.getR94Fc21Amt());
                wr(sheet, 94, greyNumStyle, recC.getR95GlobalAmt(), recC.getR95UaeAmt(), recC.getR95Fc1Amt(), recC.getR95Fc2Amt(), recC.getR95Fc3Amt(), recC.getR95Fc4Amt(), recC.getR95Fc5Amt(), recC.getR95Fc6Amt(), recC.getR95Fc7Amt(), recC.getR95Fc8Amt(), recC.getR95Fc9Amt(), recC.getR95Fc10Amt(), recC.getR95Fc11Amt(), recC.getR95Fc12Amt(), recC.getR95Fc13Amt(), recC.getR95Fc14Amt(), recC.getR95Fc15Amt(), recC.getR95Fc16Amt(), recC.getR95Fc17Amt(), recC.getR95Fc18Amt(), recC.getR95Fc19Amt(), recC.getR95Fc20Amt(), recC.getR95Fc21Amt());

                workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
                workbook.write(out);
                logger.info("BRF_052_ArchiveExcel: {} bytes written", out.size());
                return out.toByteArray();
            }

        } catch (Exception e) {
            logger.error("BRF_052_ArchiveExcel error", e);
            return new byte[0];
        }
    }
 // ─────────────────────────────────────────────────────────────
    // ARCHIVE DETAIL EXCEL DOWNLOAD
    // Mirrors BRF_052_DetailExcel — reads from archive detail table.
    // ─────────────────────────────────────────────────────────────
    public byte[] BRF_052_ArchiveDetailExcel(String todate) {
        logger.info("BRF_052_ArchiveDetailExcel: building for todate={}", todate);
        try {
        	Date d1;
        	try {
        	    d1 = dateformat.parse(todate);
        	} catch (Exception ex) {
        	    try {
        	        d1 = new SimpleDateFormat("dd-MM-yyyy").parse(todate);  // ← add this
        	    } catch (Exception ex2) {
        	        d1 = new SimpleDateFormat("dd/MM/yyyy").parse(todate);
        	    }
        	}

            List<BRF_052_ARCHIVE_DETAIL_Entity> detailList =
                    brf052ArchiveDetailRepo.getArchiveByDate(d1);

            if (detailList.isEmpty()) {
                logger.warn("BRF_052_ArchiveDetailExcel: no data for date {}", todate);
                return new byte[0];
            }

         // ─── AFTER (replacement) ───────────────────────────────────────────────────
            try (SXSSFWorkbook workbook = new SXSSFWorkbook(500);          // ✅ line 1 changed
                 ByteArrayOutputStream out = new ByteArrayOutputStream(1024 * 1024)) {  // ✅ line 2 changed

                workbook.setCompressTempFiles(true);                        // ✅ new line added

                Sheet sheet = workbook.createSheet("BRF_052 Archive Detail");

                String[] headers = {"Cust ID", "Account No", "Account Name",
                                     "Row ID", "Column ID", "Amount (AED)", "Remarks"};

                // ✅ Fixed column widths — replaces autoSizeColumn
                int[] colWidths = {15, 20, 35, 10, 12, 18, 30};
                for (int i = 0; i < colWidths.length; i++)
                    sheet.setColumnWidth(i, colWidths[i] * 256);

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                int rowIdx = 1;
                for (BRF_052_ARCHIVE_DETAIL_Entity rec : detailList) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(rec.getCustId()        != null ? rec.getCustId()        : "");
                    row.createCell(1).setCellValue(rec.getAcctNumber()    != null ? rec.getAcctNumber()    : "");
                    row.createCell(2).setCellValue(rec.getAcctName()      != null ? rec.getAcctName()      : "");
                    row.createCell(3).setCellValue(rec.getRowId()         != null ? rec.getRowId()         : "");
                    row.createCell(4).setCellValue(rec.getColumnId()      != null ? rec.getColumnId()      : "");
                    if (rec.getAmountAed() != null)
                        row.createCell(5).setCellValue(rec.getAmountAed().doubleValue());
                    row.createCell(6).setCellValue(rec.getReportRemarks() != null ? rec.getReportRemarks() : "");
                }

                // ❌ autoSizeColumn loop REMOVED entirely

                workbook.write(out);
                workbook.dispose();                                         // ✅ new line — cleans up temp files
                logger.info("BRF_052_ArchiveDetailExcel: {} bytes, {} rows", out.size(), detailList.size());
                return out.toByteArray();
            }

        } catch (Exception e) {
            logger.error("BRF_052_ArchiveDetailExcel error", e);
            return new byte[0];
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // wr: writes 23 values (Global, UAE, FC1..FC21) into one row on the sheet.
    // POI col mapping: 3=Global, 4=UAE, 5=FC1, 6=FC2, ..., 25=FC21
    // ─────────────────────────────────────────────────────────────────────────
    private void wr(Sheet sheet, int poiRow, CellStyle style,
            BigDecimal global, BigDecimal uae,
            BigDecimal fc1,  BigDecimal fc2,  BigDecimal fc3,  BigDecimal fc4,  BigDecimal fc5,
            BigDecimal fc6,  BigDecimal fc7,  BigDecimal fc8,  BigDecimal fc9,  BigDecimal fc10,
            BigDecimal fc11, BigDecimal fc12, BigDecimal fc13, BigDecimal fc14, BigDecimal fc15,
            BigDecimal fc16, BigDecimal fc17, BigDecimal fc18, BigDecimal fc19, BigDecimal fc20,
            BigDecimal fc21) {

        BigDecimal[] vals = { global, uae,
            fc1,  fc2,  fc3,  fc4,  fc5,  fc6,  fc7,  fc8,  fc9,  fc10,
            fc11, fc12, fc13, fc14, fc15, fc16, fc17, fc18, fc19, fc20, fc21 };

        Row row = sheet.getRow(poiRow);
        if (row == null) row = sheet.createRow(poiRow);

        for (int i = 0; i < vals.length; i++) {
            Cell cell = row.getCell(3 + i);
            if (cell == null) cell = row.createCell(3 + i);
            cell.setCellValue(vals[i] != null ? vals[i].doubleValue() : 0.0);
            if (style != null) cell.setCellStyle(style);
        }
    }

    private void writeText(Sheet sheet, int poiRow, int poiCol, String value) {
        Row row = sheet.getRow(poiRow);
        if (row == null) row = sheet.createRow(poiRow);
        Cell cell = row.getCell(poiCol);
        if (cell == null) cell = row.createCell(poiCol);
        cell.setCellValue(value != null ? value : "");
    }
 // ─────────────────────────────────────────────────────────────
    // ARCHIVE SUMMARY VIEW
    // Mirrors getBRF052View exactly — queries ARCHIVE_A/B/C instead
    // of live tables. Called by BRF_Archive_Service.
    // ─────────────────────────────────────────────────────────────
    public ModelAndView getArchiveSummaryView(Date reportDate, String dateStr) {

        ModelAndView mv = new ModelAndView();

        try {
            List<BRF_052_ARCHIVE_A_Entity> listA = brf052ArchiveARepo.getArchiveByDate(reportDate);
            List<BRF_052_ARCHIVE_B_Entity> listB = brf052ArchiveBRepo.getArchiveByDate(reportDate);
            List<BRF_052_ARCHIVE_C_Entity> listC = brf052ArchiveCRepo.getArchiveByDate(reportDate);

            logger.info("BRF_052 archive rows → A:{} B:{} C:{}", listA.size(), listB.size(), listC.size());

            BRF_052_ARCHIVE_A_Entity recA = listA.isEmpty() ? new BRF_052_ARCHIVE_A_Entity() : listA.get(0);
            BRF_052_ARCHIVE_B_Entity recB = listB.isEmpty() ? new BRF_052_ARCHIVE_B_Entity() : listB.get(0);
            BRF_052_ARCHIVE_C_Entity recC = listC.isEmpty() ? new BRF_052_ARCHIVE_C_Entity() : listC.get(0);

            mv.addObject("summaryA", recA);
            mv.addObject("summaryB", recB);
            mv.addObject("summaryC", recC);
            mv.addObject("hasData",  !(listA.isEmpty() && listB.isEmpty() && listC.isEmpty()));

        } catch (Exception e) {
            logger.error("BRF_052 getArchiveSummaryView ERROR for date={}", dateStr, e);
            mv.addObject("summaryA", new BRF_052_ARCHIVE_A_Entity());
            mv.addObject("summaryB", new BRF_052_ARCHIVE_B_Entity());
            mv.addObject("summaryC", new BRF_052_ARCHIVE_C_Entity());
            mv.addObject("hasData",  false);
            mv.addObject("error",    "Error fetching archived data: " + e.getMessage());
        }

        // ── Archive mode flags — BRF_052.html uses these to hide edit/generate ──
        mv.addObject("archiveMode", true);
        mv.addObject("archiveDate", dateStr);
        mv.addObject("displaymode", "summary");
        mv.addObject("fromdate",    dateStr);
        mv.addObject("todate",      dateStr);
        mv.addObject("asondate",    dateStr);
        mv.addObject("currency",    "AED");
        mv.addObject("dtltype",     "report");
        mv.addObject("type",        "RBS");

        // Reuses the same BRF_052.html — no new template needed
        mv.setViewName("BRRS/BRF_052");
        return mv;
    }
    
    // ─────────────────────────────────────────────────────────────
    // ARCHIVE DETAIL VIEW
    // Queries live BRF_052_DETAIL filtered by reportDate.
    // Detail rows are never deleted by BRF_052_P so they always exist.
    // ─────────────────────────────────────────────────────────────
 // ─────────────────────────────────────────────────────────────
    // ARCHIVE DETAIL VIEW
    // Queries BRF_052_ARCHIVE_DETAIL — frozen snapshot for this date.
    // ─────────────────────────────────────────────────────────────
 // ─────────────────────────────────────────────────────────────
 // ARCHIVE DETAIL VIEW — paginated, 20 rows per page
 // Accepts optional filterRowId + filterColId for cell-click
 // ─────────────────────────────────────────────────────────────
 public ModelAndView getArchiveDetailView(Date reportDate, String dateStr,
         int page, int size, String filterRowId, String filterColId) {

     ModelAndView mv = new ModelAndView();

     try {
         Pageable pageable = PageRequest.of(page, size);
         Page<BRF_052_ARCHIVE_DETAIL_Entity> resultPage;

         boolean hasFilter = filterRowId != null && !filterRowId.isEmpty()
                          && filterColId != null && !filterColId.isEmpty();

         if (hasFilter) {
             resultPage = brf052ArchiveDetailRepo.getArchiveByDateAndFilterPaged(
                     reportDate, filterRowId, filterColId, pageable);
         } else {
             resultPage = brf052ArchiveDetailRepo.getArchiveByDatePaged(reportDate, pageable);
         }

         logger.info("BRF_052 archive detail page={} size={} total={}",
                 page, size, resultPage.getTotalElements());

         mv.addObject("reportdetails",  resultPage.getContent());
         mv.addObject("currentPage",    resultPage.getNumber());
         mv.addObject("totalPages",     resultPage.getTotalPages());
         mv.addObject("totalElements",  resultPage.getTotalElements());
         mv.addObject("pageSize",       size);
         mv.addObject("filterRowId",    filterRowId != null ? filterRowId : "");
         mv.addObject("filterColId",    filterColId != null ? filterColId : "");

     } catch (Exception e) {
         logger.error("BRF_052 getArchiveDetailView ERROR for date={}", dateStr, e);
         mv.addObject("reportdetails",  new java.util.ArrayList<>());
         mv.addObject("currentPage",    0);
         mv.addObject("totalPages",     0);
         mv.addObject("totalElements",  0L);
         mv.addObject("pageSize",       size);
         mv.addObject("filterRowId",    "");
         mv.addObject("filterColId",    "");
         mv.addObject("error", "Error fetching archived detail: " + e.getMessage());
     }

     mv.addObject("archiveMode", true);
     mv.addObject("archiveDate", dateStr);
     mv.addObject("displaymode", "detail");
     mv.addObject("fromdate",    dateStr);
     mv.addObject("todate",      dateStr);
     mv.addObject("asondate",    dateStr);
     mv.addObject("currency",    "AED");
     mv.addObject("dtltype",     "report");
     mv.addObject("type",        "RBS");
     mv.setViewName("BRRS/BRF_052");
     return mv;
 }
}