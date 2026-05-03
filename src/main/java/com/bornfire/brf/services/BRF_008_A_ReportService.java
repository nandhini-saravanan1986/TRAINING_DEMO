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

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import com.bornfire.brf.entities.BRF_008_A_Detail_Entity;
import com.bornfire.brf.entities.BRF_008_A_Detail_Repo;
import com.bornfire.brf.entities.BRF_008_A_Entity;
import com.bornfire.brf.entities.BRF_008_A_Repo;

@Component
@Service
public class BRF_008_A_ReportService {

    private static final Logger logger = LoggerFactory.getLogger(BRF_008_A_ReportService.class);

    @Autowired
    private Environment env;

    @Autowired
    BRF_008_A_Repo brf008aRepo;

    @Autowired
    BRF_008_A_Detail_Repo brf008aDetailRepo;

    SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy");

    // ─────────────────────────────────────────────────────────────────────────
    // SUMMARY VIEW
    // ─────────────────────────────────────────────────────────────────────────
    public ModelAndView getBRF07View(String reportId, String fromdate, String todate,
            String currency, String dtltype, Pageable pageable, String type) {
        ModelAndView mv = new ModelAndView();
        System.out.println("BRF_008_A getBRF07View called → todate=" + todate + " fromdate=" + fromdate);
        try {
            // Accept both dd-MMM-yyyy (controller-converted) and dd/MM/yyyy (raw) formats
            Date d1;
            try {
                d1 = dateformat.parse(todate); // dd-MMM-yyyy
            } catch (Exception ex) {
                d1 = new SimpleDateFormat("dd/MM/yyyy").parse(todate); // fallback
            }
            List<BRF_008_A_Entity> dataList = brf008aRepo.getDataByDate(d1);
            System.out.println("BRF_008_A data size: " + dataList.size());
            mv.addObject("reportsummary", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("BRF_008_A getBRF07View ERROR: " + e.getMessage());
            mv.addObject("reportsummary", new java.util.ArrayList<>());
            mv.addObject("error", "Error fetching BRF_008_A data: " + e.getMessage());
        }
        // Always set these — template needs them for ALL button URLs
        mv.addObject("displaymode", "summary");
        mv.addObject("fromdate",  fromdate  != null ? fromdate  : todate);
        mv.addObject("todate",    todate);
        mv.addObject("asondate",  todate);
        mv.addObject("currency",  currency  != null ? currency  : "AED");
        mv.addObject("dtltype",   dtltype   != null ? dtltype   : "report");
        mv.addObject("type",      type      != null ? type      : "RBS");
        mv.setViewName("BRRS/BRF_07");
        return mv;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DETAIL VIEW (with optional search by ROW_ID)
    // ─────────────────────────────────────────────────────────────────────────
    public ModelAndView getBRF008ADetailView(String reportId, String fromdate, String todate,
            String currency, String dtltype, String type) {
        ModelAndView mv = new ModelAndView();
        System.out.println("BRF_008_A getBRF008ADetailView called → todate=" + todate + " fromdate=" + fromdate);
        try {
            // Accept both dd-MMM-yyyy (controller-converted) and dd/MM/yyyy (raw) formats
            Date d1;
            try {
                d1 = dateformat.parse(todate); // dd-MMM-yyyy
            } catch (Exception ex) {
                d1 = new SimpleDateFormat("dd/MM/yyyy").parse(todate); // fallback
            }
            List<BRF_008_A_Detail_Entity> detailList = brf008aDetailRepo.getDataByDate(d1);
            System.out.println("BRF_008_A Detail size: " + detailList.size());
            mv.addObject("reportdetails", detailList);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("BRF_008_A getBRF008ADetailView ERROR: " + e.getMessage());
            mv.addObject("reportdetails", new java.util.ArrayList<>());
            mv.addObject("error", "Error fetching BRF_008_A detail data: " + e.getMessage());
        }
        // Always set these — template needs them for ALL button URLs
        mv.addObject("displaymode", "detail");
        mv.addObject("fromdate",  fromdate  != null ? fromdate  : todate);
        mv.addObject("todate",    todate);
        mv.addObject("asondate",  todate);
        mv.addObject("currency",  currency  != null ? currency  : "AED");
        mv.addObject("dtltype",   dtltype   != null ? dtltype   : "report");
        mv.addObject("type",      type      != null ? type      : "RBS");
        mv.setViewName("BRRS/BRF_07");
        return mv;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SUMMARY EXCEL DOWNLOAD — Source Temp (Template) Approach
    // ─────────────────────────────────────────────────────────────────────────
    public byte[] BRF_008_A_Excel(String filename, String reportId, String fromdate,
            String todate, String currency, String dtltype) {

        logger.info("BRF_008_A_Excel (TEMPLATE): building workbook for todate={}", todate);
        try {
            Date d1 = dateformat.parse(todate);
            List<BRF_008_A_Entity> dataList = brf008aRepo.getDataByDate(d1);

            if (dataList == null || dataList.isEmpty()) {
                logger.warn("BRF_008_A_Excel: no data found for date {}", todate);
                return new byte[0];
            }

            BRF_008_A_Entity rec = dataList.get(0);

            // ── Load template from SOURCETEMP ─────────────────────────
            String templateDir = env.getProperty("output.exportpathtemp");
            String templateFileName = (filename != null && !filename.isEmpty()) ? filename : "BRF_008_A.xlsx";
            Path templatePath = Paths.get(templateDir, templateFileName);
            System.out.println("Template path: " + templatePath);

            logger.info("Service: Attempting to load template from path: {}", templatePath.toAbsolutePath());

            if (!Files.exists(templatePath)) {
                throw new FileNotFoundException("Template file not found at: " + templatePath.toAbsolutePath());
            }
            if (!Files.isReadable(templatePath)) {
                throw new SecurityException(
                        "Template file exists but is not readable: " + templatePath.toAbsolutePath());
            }

            // ── Open template, fill data, write to memory ─────────────
            try (InputStream templateInputStream = Files.newInputStream(templatePath);
                    Workbook workbook = WorkbookFactory.create(templateInputStream);
                    ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                Sheet sheet = workbook.getSheetAt(0);

                // ── PDF-safe page layout — explicitly re-declare so the
                //    converter never falls back to its own defaults. ──────
                //
                //    Root causes addressed:
                //    1. Landscape orientation — BRF-8 is 5 columns wide;
                //       portrait causes columns to overflow the right edge.
                //    2. Fit-to-width=1 — forces all columns onto one page
                //       horizontally; height pages are unrestricted.
                //    3. A4 paper size — matches the regulatory form size.
                //    4. Explicit margins — converters that ignore the
                //       template's <pageMargins> XML default to 1-inch
                //       margins, wasting ~2 cm on each side.
                //    5. setFitToPage(true) + setAutobreaks(true) — POI
                //       round-trip can silently clear these flags; resetting
                //       them here guarantees they survive workbook.write().
                //    6. setPrintArea — some converters (e.g. LibreOffice
                //       headless) only render the declared print area; without
                //       it they sometimes clip to the first screen-full.
                PrintSetup ps = sheet.getPrintSetup();
                ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
                ps.setLandscape(true);
                ps.setFitWidth((short) 1);   // fit all columns on one page width
                ps.setFitHeight((short) 0);  // unlimited pages vertically
                ps.setLeftToRight(false);
                sheet.setFitToPage(true);
                sheet.setAutobreaks(true);

                // Margins in inches: left/right=0.5, top/bottom=0.75, header/footer=0.3
                sheet.setMargin(Sheet.LeftMargin,   0.5);
                sheet.setMargin(Sheet.RightMargin,  0.5);
                sheet.setMargin(Sheet.TopMargin,    0.75);
                sheet.setMargin(Sheet.BottomMargin, 0.75);
                sheet.setMargin(Sheet.HeaderMargin, 0.3);
                sheet.setMargin(Sheet.FooterMargin, 0.3);

                // Print area: sheet 0, cols A–E (0–4), rows 1–22 (0–21)
                // Adjust the last row index if your template has more rows.
                workbook.setPrintArea(0, 0, 4, 0, 21);

                // --- Style Definitions ---
                CreationHelper createHelper = workbook.getCreationHelper();

                Font font = workbook.createFont();
                font.setFontHeightInPoints((short) 9);
                font.setFontName("Arial");

                CellStyle numberStyle = workbook.createCellStyle();
                numberStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.000"));
                numberStyle.setBorderBottom(BorderStyle.THIN);
                numberStyle.setBorderTop(BorderStyle.THIN);
                numberStyle.setBorderLeft(BorderStyle.THIN);
                numberStyle.setBorderRight(BorderStyle.THIN);
                numberStyle.setFont(font);

                CellStyle textStyle = workbook.createCellStyle();
                textStyle.setBorderBottom(BorderStyle.THIN);
                textStyle.setBorderTop(BorderStyle.THIN);
                textStyle.setBorderLeft(BorderStyle.THIN);
                textStyle.setBorderRight(BorderStyle.THIN);
                textStyle.setFont(font);

                // ── Template layout (POI 0-based row / col indices) ──────────────────
                // POI row 1  (Excel B2): Title
                // POI row 2  (Excel B3): ID: 2-8-2-0
                // POI row 3  (Excel B4): Bank label + value
                // POI row 4  (Excel B5): "As on:" label → date value goes in col 2 (C5)
                // POI row 5  (Excel B6): blank
                // POI row 6  (Excel B7): Column headers
                // POI row 7  (Excel D8):  R8  1.1 Physical cash        → col 3 (D) NOM_AMT
                // POI row 8  (Excel D9):  R9  1.2 Statutory reserves   → col 3 (D) NOM_AMT
                // POI row 9  (Excel D10): R10 1.3 Free balances        → col 3 (D) NOM_AMT
                // POI row 10 (Excel D11): R11 1.4 Central Bank CDs     → col 3 (D) NOM_AMT
                // POI row 11 (Excel D12): R12 1.5 UAE Federal Bonds    → col 3 (D) NOM_AMT
                // POI row 12 (Excel D13): Sub Total 1.1–1.5            → FORMULA — do not write
                // POI row 13 (Excel D14): R14 1.6                      → col 3 (D) NOM_AMT
                // POI row 14 (Excel D15): R15 1.7                      → col 3 (D) NOM_AMT
                // POI row 15 (Excel D16): R16 1.8                      → col 3 (D) NOM_AMT
                // POI row 16 (Excel D17): R17 1.9                      → col 3 (D) NOM_AMT
                // POI row 17 (Excel D18): Sub total 1.6–1.9            → FORMULA — do not write
                // POI row 18 (Excel D19): R19 1.10 Foreign Sovereign   → col 3 (D) NOM_AMT (E19 is formula)
                // POI row 19 (Excel D20): 1.11 Total                   → FORMULA — do not write
                // POI row 20 (Excel E21): R21 2 Total liabilities      → col 4 (E) ELA input
                // POI row 21 (Excel E22): ELAR                         → FORMULA — do not write

                // ── Fill "As on" date → C5 (POI row=4, col=2) ────────
                Row row4 = sheet.getRow(4);
                if (row4 == null) row4 = sheet.createRow(4);
                Cell dateCell = row4.getCell(2);
                if (dateCell == null) dateCell = row4.createCell(2);
                String asOn = rec.getReportDate() != null
                        ? new SimpleDateFormat("dd/MM/yyyy").format(rec.getReportDate()) : "";
                dateCell.setCellValue(asOn);

                // ── Fill data values — Nominal Amount col 3 (D), ELA col 4 (E) ──
                // R8  1.1 Physical cash in hand  → D8  (POI row=7, col=3)
                fillCell(sheet, 7, 3, rec.getR8_NOM_AMT(), numberStyle);
                // R9  1.2 Statutory reserves     → D9  (POI row=8, col=3)
                fillCell(sheet, 8, 3, rec.getR9_NOM_AMT(), numberStyle);
                // R10 1.3 Free and other balances → D10 (POI row=9, col=3)
                fillCell(sheet, 9, 3, rec.getR10_NOM_AMT(), numberStyle);
                // R11 1.4 Central Bank CDs        → D11 (POI row=10, col=3)
                fillCell(sheet, 10, 3, rec.getR11_NOM_AMT(), numberStyle);
                // R12 1.5 UAE Federal Govt Bonds  → D12 (POI row=11, col=3)
                fillCell(sheet, 11, 3, rec.getR12_NOM_AMT(), numberStyle);

                // D13/E13 Sub Total (1.1–1.5) are =SUM formulas — let Excel calculate

                // R14 1.6  → D14 (POI row=13, col=3)
                fillCell(sheet, 13, 3, rec.getR14_NOM_AMT(), numberStyle);
                // R15 1.7  → D15 (POI row=14, col=3)
                fillCell(sheet, 14, 3, rec.getR15_NOM_AMT(), numberStyle);
                // R16 1.8  → D16 (POI row=15, col=3)
                fillCell(sheet, 15, 3, rec.getR16_NOM_AMT(), numberStyle);
                // R17 1.9  → D17 (POI row=16, col=3)
                fillCell(sheet, 16, 3, rec.getR17_NOM_AMT(), numberStyle);

                // D18/E18 Sub total (1.6–1.9) are IF formulas — let Excel calculate

                // R19 1.10 Foreign Sovereign debt → D19 (POI row=18, col=3) NOM_AMT only
                // E19 is an IF formula — do not write ELA here
                fillCell(sheet, 18, 3, rec.getR19_NOM_AMT(), numberStyle);

                // D20/E20 (1.11 Total) are formulas — let Excel calculate

                // R21 2 Total liabilities → E21 (POI row=20, col=4) — only ELA input cell
                fillCell(sheet, 20, 4, rec.getR21_ELA(), numberStyle);

                // E22 ELAR is =IF(E21=0,0,E20/E21) formula — let Excel calculate

                workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();

                workbook.write(out);
                logger.info("BRF_008_A_Excel (TEMPLATE): {} bytes written", out.size());
                return out.toByteArray();
            }

        } catch (Exception e) {
            logger.error("BRF_008_A_Excel error", e);
            return new byte[0];
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DETAIL EXCEL DOWNLOAD — Pure Apache POI (no template file required)
    // Columns: CUST ID | ACCT NUMBER | ACCT NAME | DATA TYPE | ROW ID |
    //          COLUMN ID | AMOUNT (AED) | REPORT REMARKS |
    //          MODIFICATION REMARKS | DATA ENTRY VERSION | REPORT DATE
    // ─────────────────────────────────────────────────────────────────────────
    public byte[] BRF_008_A_DetailExcel(String filename, String fromdate, String todate) {

        logger.info("BRF_008_A_DetailExcel (POI): building workbook for todate={}", todate);
        try {
            Date d1 = dateformat.parse(todate);
            List<BRF_008_A_Detail_Entity> detailList = brf008aDetailRepo.getDataByDate(d1);

            if (detailList == null || detailList.isEmpty()) {
                logger.warn("BRF_008_A_DetailExcel: no detail data found for date {}", todate);
                return new byte[0];
            }

            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                Sheet sheet = workbook.createSheet("BRF_008_A Detail");
                CreationHelper createHelper = workbook.getCreationHelper();

                // ── Column widths ─────────────────────────────────────
                sheet.setColumnWidth(0,  18 * 256);  // CUST ID
                sheet.setColumnWidth(1,  20 * 256);  // ACCT NUMBER
                sheet.setColumnWidth(2,  30 * 256);  // ACCT NAME
                sheet.setColumnWidth(3,  15 * 256);  // DATA TYPE
                sheet.setColumnWidth(4,  12 * 256);  // ROW ID
                sheet.setColumnWidth(5,  12 * 256);  // COLUMN ID
                sheet.setColumnWidth(6,  20 * 256);  // AMOUNT (AED)
                sheet.setColumnWidth(7,  25 * 256);  // REPORT REMARKS
                sheet.setColumnWidth(8,  28 * 256);  // MODIFICATION REMARKS
                sheet.setColumnWidth(9,  22 * 256);  // DATA ENTRY VERSION
                sheet.setColumnWidth(10, 15 * 256);  // REPORT DATE

                // ── Fonts ─────────────────────────────────────────────
                Font normalFont = workbook.createFont();
                normalFont.setFontName("Arial");
                normalFont.setFontHeightInPoints((short) 9);

                Font boldFont = workbook.createFont();
                boldFont.setFontName("Arial");
                boldFont.setFontHeightInPoints((short) 9);
                boldFont.setBold(true);

                Font titleFont = workbook.createFont();
                titleFont.setFontName("Arial");
                titleFont.setFontHeightInPoints((short) 11);
                titleFont.setBold(true);
                titleFont.setColor(IndexedColors.WHITE.getIndex());

                Font headerFont = workbook.createFont();
                headerFont.setFontName("Arial");
                headerFont.setFontHeightInPoints((short) 9);
                headerFont.setBold(true);
                headerFont.setColor(IndexedColors.WHITE.getIndex());

                // ── Shared colour: rgb(107,122,143) — matches HTML header ──
                XSSFColor headerBg = new XSSFColor(new byte[]{(byte) 107, (byte) 122, (byte) 143}, null);
                // Light grey for total row: rgb(211,211,211)
                XSSFColor totalBg  = new XSSFColor(new byte[]{(byte) 211, (byte) 211, (byte) 211}, null);

                // ── Title bar style (merged, full-width) ─────────────
                XSSFCellStyle titleStyle = workbook.createCellStyle();
                titleStyle.setFont(titleFont);
                titleStyle.setFillForegroundColor(headerBg);
                titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                titleStyle.setAlignment(HorizontalAlignment.LEFT);
                titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                // ── Meta label (bold, no fill) ────────────────────────
                CellStyle metaLabelStyle = workbook.createCellStyle();
                metaLabelStyle.setFont(boldFont);

                // ── Meta value (normal, no fill) ──────────────────────
                CellStyle metaValueStyle = workbook.createCellStyle();
                metaValueStyle.setFont(normalFont);

                // ── Column header style ───────────────────────────────
                XSSFCellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(headerBg);
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);
                headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                headerStyle.setWrapText(true);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setBorderTop(BorderStyle.THIN);
                headerStyle.setBorderLeft(BorderStyle.THIN);
                headerStyle.setBorderRight(BorderStyle.THIN);

                // ── Data text cell ────────────────────────────────────
                CellStyle textStyle = workbook.createCellStyle();
                textStyle.setFont(normalFont);
                textStyle.setBorderBottom(BorderStyle.THIN);
                textStyle.setBorderTop(BorderStyle.THIN);
                textStyle.setBorderLeft(BorderStyle.THIN);
                textStyle.setBorderRight(BorderStyle.THIN);
                textStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                // ── Data numeric cell ─────────────────────────────────
                CellStyle numStyle = workbook.createCellStyle();
                numStyle.setFont(normalFont);
                numStyle.setAlignment(HorizontalAlignment.RIGHT);
                numStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.000"));
                numStyle.setBorderBottom(BorderStyle.THIN);
                numStyle.setBorderTop(BorderStyle.THIN);
                numStyle.setBorderLeft(BorderStyle.THIN);
                numStyle.setBorderRight(BorderStyle.THIN);
                numStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                // ── Data date cell (centred) ──────────────────────────
                CellStyle dateStyle = workbook.createCellStyle();
                dateStyle.setFont(normalFont);
                dateStyle.setAlignment(HorizontalAlignment.CENTER);
                dateStyle.setBorderBottom(BorderStyle.THIN);
                dateStyle.setBorderTop(BorderStyle.THIN);
                dateStyle.setBorderLeft(BorderStyle.THIN);
                dateStyle.setBorderRight(BorderStyle.THIN);
                dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                // ── Total row label ───────────────────────────────────
                XSSFCellStyle totalLabelStyle = workbook.createCellStyle();
                totalLabelStyle.setFont(boldFont);
                totalLabelStyle.setFillForegroundColor(totalBg);
                totalLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                totalLabelStyle.setBorderBottom(BorderStyle.MEDIUM);
                totalLabelStyle.setBorderTop(BorderStyle.MEDIUM);
                totalLabelStyle.setBorderLeft(BorderStyle.THIN);
                totalLabelStyle.setBorderRight(BorderStyle.THIN);

                // ── Total row amount ──────────────────────────────────
                XSSFCellStyle totalNumStyle = workbook.createCellStyle();
                totalNumStyle.setFont(boldFont);
                totalNumStyle.setFillForegroundColor(totalBg);
                totalNumStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                totalNumStyle.setAlignment(HorizontalAlignment.RIGHT);
                totalNumStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.000"));
                totalNumStyle.setBorderBottom(BorderStyle.MEDIUM);
                totalNumStyle.setBorderTop(BorderStyle.MEDIUM);
                totalNumStyle.setBorderLeft(BorderStyle.THIN);
                totalNumStyle.setBorderRight(BorderStyle.THIN);

                SimpleDateFormat outFmt = new SimpleDateFormat("dd/MM/yyyy");
                int rowIdx = 0;

                // ── Row 0: Title bar (merged across all 11 columns) ───
                Row titleRow = sheet.createRow(rowIdx++);
                titleRow.setHeightInPoints(22);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue(
                    "BRF 08 - BANKING AND MONETARY STATISTICS: LIQUID ASSETS RATIO (LAR) — DETAIL TABLE");
                titleCell.setCellStyle(titleStyle);

                // ── Row 1: Report Date meta ───────────────────────────
                Row metaRow = sheet.createRow(rowIdx++);
                metaRow.setHeightInPoints(16);
                Cell metaLabel = metaRow.createCell(0);
                metaLabel.setCellValue("Report Date:");
                metaLabel.setCellStyle(metaLabelStyle);
                Cell metaValue = metaRow.createCell(1);
                metaValue.setCellValue(outFmt.format(d1));
                metaValue.setCellStyle(metaValueStyle);

                // ── Row 2: blank spacer ───────────────────────────────
                sheet.createRow(rowIdx++).setHeightInPoints(6);

                // ── Row 3: Column headers ─────────────────────────────
                String[] headers = {
                    "CUST ID", "ACCT NUMBER", "ACCT NAME", "DATA TYPE",
                    "ROW ID", "COLUMN ID", "AMOUNT (AED)",
                    "REPORT REMARKS", "MODIFICATION REMARKS",
                    "DATA ENTRY VERSION", "REPORT DATE"
                };
                Row headerRow = sheet.createRow(rowIdx++);
                headerRow.setHeightInPoints(30);
                for (int c = 0; c < headers.length; c++) {
                    Cell hCell = headerRow.createCell(c);
                    hCell.setCellValue(headers[c]);
                    hCell.setCellStyle(headerStyle);
                }

                // ── Rows 4+: Data rows ────────────────────────────────
                int dataStartRowIdx = rowIdx; // 0-based, used for SUM formula
                for (BRF_008_A_Detail_Entity det : detailList) {
                    Row row = sheet.createRow(rowIdx++);
                    row.setHeightInPoints(15);

                    createTextCell(row, 0, det.getCustId(),              textStyle);
                    createTextCell(row, 1, det.getAcctNumber(),           textStyle);
                    createTextCell(row, 2, det.getAcctName(),             textStyle);
                    createTextCell(row, 3, det.getDataType(),             textStyle);
                    createTextCell(row, 4, det.getRowId(),                textStyle);
                    createTextCell(row, 5, det.getColumnId(),             textStyle);

                    Cell amtCell = row.createCell(6);
                    amtCell.setCellValue(det.getAmountAed() != null ? det.getAmountAed().doubleValue() : 0.0);
                    amtCell.setCellStyle(numStyle);

                    createTextCell(row, 7,  det.getReportRemarks(),       textStyle);
                    createTextCell(row, 8,  det.getModificationRemarks(), textStyle);
                    createTextCell(row, 9,  det.getDataEntryVersion(),    textStyle);
                    createTextCell(row, 10,
                        det.getReportDate() != null ? outFmt.format(det.getReportDate()) : "",
                        dateStyle);
                }

                // ── Total row ─────────────────────────────────────────
                Row totalRow = sheet.createRow(rowIdx);
                totalRow.setHeightInPoints(16);

                Cell totalLabel = totalRow.createCell(0);
                totalLabel.setCellValue("TOTAL");
                totalLabel.setCellStyle(totalLabelStyle);
                for (int c = 1; c <= 5; c++) {
                    totalRow.createCell(c).setCellStyle(totalLabelStyle);
                }

                // SUM formula for AMOUNT (col G). Excel rows are 1-based.
                // dataStartRowIdx is 0-based → Excel row = dataStartRowIdx + 1
                // rowIdx is the total row (0-based) → last data Excel row = rowIdx (same value)
                Cell totalAmtCell = totalRow.createCell(6);
                totalAmtCell.setCellFormula(
                    String.format("SUM(G%d:G%d)", dataStartRowIdx + 1, rowIdx));
                totalAmtCell.setCellStyle(totalNumStyle);

                for (int c = 7; c <= 10; c++) {
                    totalRow.createCell(c).setCellStyle(totalLabelStyle);
                }

                workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();

                workbook.write(out);
                logger.info("BRF_008_A_DetailExcel (POI): {} bytes written, {} data rows",
                    out.size(), detailList.size());
                return out.toByteArray();
            }

        } catch (Exception e) {
            logger.error("BRF_008_A_DetailExcel error", e);
            return new byte[0];
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DETAIL RECORD UPDATE — called by the edit modal save button
    // ─────────────────────────────────────────────────────────────────────────
    public String updateDetailRecord(
            String custId,
            String acctNumber,
            String acctName,
            String dataType,
            String rowId,
            String columnId,
            BigDecimal amountAed,
            String reportRemarks,
            String modificationRemarks,
            String dataEntryVersion,
            String reportDateStr,
            String modifyUser) {

        logger.info("updateDetailRecord: custId={}", custId);

        BRF_008_A_Detail_Entity entity = brf008aDetailRepo.findById(custId).orElse(null);
        if (entity == null) {
            logger.warn("updateDetailRecord: no record found for custId={}", custId);
            return "Record not found for CUST ID: " + custId;
        }

        entity.setAcctNumber(acctNumber);
        entity.setAcctName(acctName);
        entity.setDataType(dataType);
        entity.setRowId(rowId);
        entity.setColumnId(columnId);
        entity.setAmountAed(amountAed);
        entity.setReportRemarks(reportRemarks);
        entity.setModificationRemarks(modificationRemarks);
        entity.setDataEntryVersion(dataEntryVersion);
        entity.setModifyUser(modifyUser);
        entity.setModifyTime(new Date());
        entity.setModifyFlg("M");

        brf008aDetailRepo.save(entity);
        logger.info("updateDetailRecord: saved custId={}", custId);
        return "Record updated successfully.";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper: create a text cell with null-safe value
    // ─────────────────────────────────────────────────────────────────────────
    private void createTextCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper: fill a cell with BigDecimal value
    // ─────────────────────────────────────────────────────────────────────────
    private void fillCell(Sheet sheet, int rowIdx, int colIdx, BigDecimal val, CellStyle style) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) row = sheet.createRow(rowIdx);
        Cell cell = row.getCell(colIdx);
        if (cell == null) cell = row.createCell(colIdx);
        cell.setCellValue(val != null ? val.doubleValue() : 0.0);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }
}
