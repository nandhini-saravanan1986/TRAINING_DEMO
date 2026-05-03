package com.bornfire.brf.services;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import com.bornfire.brf.services.Exceltopdfservice;

import com.bornfire.brf.entities.BRF_052_ARCHIVE_INDEX_Entity;
import com.bornfire.brf.entities.BRF_052_ARCHIVE_INDEX_Repo;

@Service
public class BRF_Archive_Service {

    private static final Logger logger = LoggerFactory.getLogger(BRF_Archive_Service.class);

    @Autowired
    BRF_052_ARCHIVE_INDEX_Repo archiveIndexRepo;

    @Autowired
    BRF_052_ReportService brf052ReportService;
    
    @Autowired
    Exceltopdfservice exceltopdfservice;

    SimpleDateFormat dateformat    = new SimpleDateFormat("dd-MMM-yyyy");
    SimpleDateFormat dateformatAlt = new SimpleDateFormat("dd-MM-yyyy");

    // ─────────────────────────────────────────────────────────────
    // ARCHIVE LIST — powers Archive_Index.html
    // Returns all archived report dates newest first.
    // Add an else-if block here when a new report is added to archive.
    // ─────────────────────────────────────────────────────────────
    public List<BRF_052_ARCHIVE_INDEX_Entity> getArchiveList(String reportCode) {
        if ("BRF_052".equals(reportCode)) {
            return archiveIndexRepo.findAllByOrderByReportDateDesc();
        }
        // future: else if ("BRF_008".equals(reportCode)) { ... }
        return java.util.Collections.emptyList();
    }

    // ─────────────────────────────────────────────────────────────
    // ARCHIVE SUMMARY VIEW — called by controller for
    //   GET /Archive/{reportCode}/{date}
    // Parses the date string, delegates to the correct report service.
    // ─────────────────────────────────────────────────────────────
    public ModelAndView getArchiveSummaryView(String reportCode, String dateStr) {
        logger.info("getArchiveSummaryView → reportCode={} date={}", reportCode, dateStr);
        try {
            Date reportDate = parseDate(dateStr);

            if ("BRF_052".equals(reportCode)) {
                return brf052ReportService.getArchiveSummaryView(reportDate, dateStr);
            }
            // future: else if ("BRF_008".equals(reportCode)) { ... }

            // Unknown report code — return empty view
            ModelAndView mv = new ModelAndView("Archive_Index");
            mv.addObject("error", "Unknown report code: " + reportCode);
            return mv;

        } catch (Exception e) {
            logger.error("getArchiveSummaryView ERROR", e);
            ModelAndView mv = new ModelAndView("Archive_Index");
            mv.addObject("error", "Failed to load archived report: " + e.getMessage());
            return mv;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ARCHIVE DETAIL VIEW — called by controller for
    //   GET /Archive/{reportCode}/{date}/detail
    // ─────────────────────────────────────────────────────────────
 // ── Change method signature — add page, size, filterRowId, filterColId ──
    public ModelAndView getArchiveDetailView(String reportCode, String dateStr,
                                              int page, int size,
                                              String filterRowId, String filterColId) {
        logger.info("getArchiveDetailView → reportCode={} date={}", reportCode, dateStr);
        try {
            Date reportDate = parseDate(dateStr);

            if ("BRF_052".equals(reportCode)) {
                return brf052ReportService.getArchiveDetailView(
                        reportDate, dateStr, page, size, filterRowId, filterColId);
            }

            ModelAndView mv = new ModelAndView("Archive_Index");
            mv.addObject("error", "Unknown report code: " + reportCode);
            return mv;

        } catch (Exception e) {
            logger.error("getArchiveDetailView ERROR", e);
            ModelAndView mv = new ModelAndView("Archive_Index");
            mv.addObject("error", "Failed to load archived detail: " + e.getMessage());
            return mv;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPER — parse date string in multiple formats
    // Accepts: dd-MMM-yyyy  |  dd-MM-yyyy  |  dd/MM/yyyy
    // ─────────────────────────────────────────────────────────────
    private Date parseDate(String dateStr) throws Exception {
        try {
            return dateformat.parse(dateStr);
        } catch (Exception e1) {
            try {
                return dateformatAlt.parse(dateStr);
            } catch (Exception e2) {
                return new SimpleDateFormat("dd/MM/yyyy").parse(dateStr);
            }
        }
    }
 // ─────────────────────────────────────────────────────────────
    // AVAILABLE REPORTS — powers Archive_Report_List.html
    // Add a new map entry here when a new report supports archive.
    // ─────────────────────────────────────────────────────────────
    public List<java.util.Map<String, String>> getAvailableReports() {
        List<java.util.Map<String, String>> list = new java.util.ArrayList<>();

        java.util.Map<String, String> brf052 = new java.util.HashMap<>();
        brf052.put("reportCode", "BRF_052");
        brf052.put("reportName", "Global Liabilities and Capital");
        brf052.put("description", "Banking and Monetary Statistics — Liabilities & Capital");
        list.add(brf052);

        // future: add BRF_008, BRF_060 etc here

        return list;
    }
 // ─────────────────────────────────────────────────────────────
    // ARCHIVE SUMMARY EXCEL — delegates to correct report service
    // ─────────────────────────────────────────────────────────────
    public byte[] getArchiveSummaryExcel(String reportCode, String dateStr) {
        logger.info("getArchiveSummaryExcel → reportCode={} date={}", reportCode, dateStr);
        try {
            if ("BRF_052".equals(reportCode)) {
                return brf052ReportService.BRF_052_ArchiveExcel(dateStr);
            }
            return new byte[0];
        } catch (Exception e) {
            logger.error("getArchiveSummaryExcel ERROR", e);
            return new byte[0];
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ARCHIVE DETAIL EXCEL — delegates to correct report service
    // ─────────────────────────────────────────────────────────────
    public byte[] getArchiveDetailExcel(String reportCode, String dateStr) {
        logger.info("getArchiveDetailExcel → reportCode={} date={}", reportCode, dateStr);
        try {
            if ("BRF_052".equals(reportCode)) {
                return brf052ReportService.BRF_052_ArchiveDetailExcel(dateStr);
            }
            return new byte[0];
        } catch (Exception e) {
            logger.error("getArchiveDetailExcel ERROR", e);
            return new byte[0];
        }
    }
 // ─────────────────────────────────────────────────────────────
    // ARCHIVE SUMMARY PDF
    // Generates Excel from archive tables then converts to PDF
    // via the existing ExcelToPdfService — same chain as live report
    // ─────────────────────────────────────────────────────────────
    public byte[] getArchiveSummaryPdf(String reportCode, String dateStr) {
        logger.info("getArchiveSummaryPdf → reportCode={} date={}", reportCode, dateStr);
        try {
            if ("BRF_052".equals(reportCode)) {
                byte[] excelBytes = brf052ReportService.BRF_052_ArchiveExcel(dateStr);
                if (excelBytes == null || excelBytes.length == 0) return new byte[0];
                return exceltopdfservice.convertExcelBytesToPdf(excelBytes);
            }
            return new byte[0];
        } catch (Exception e) {
            logger.error("getArchiveSummaryPdf ERROR", e);
            return new byte[0];
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ARCHIVE DETAIL PDF
    // Generates Excel from archive detail table then converts to PDF
    // ─────────────────────────────────────────────────────────────
    public byte[] getArchiveDetailPdf(String reportCode, String dateStr) {
        logger.info("getArchiveDetailPdf → reportCode={} date={}", reportCode, dateStr);
        try {
            if ("BRF_052".equals(reportCode)) {
                byte[] excelBytes = brf052ReportService.BRF_052_ArchiveDetailExcel(dateStr);
                if (excelBytes == null || excelBytes.length == 0) return new byte[0];
                return exceltopdfservice.convertExcelBytesToPdf(excelBytes);
            }
            return new byte[0];
        } catch (Exception e) {
            logger.error("getArchiveDetailPdf ERROR", e);
            return new byte[0];
        }
    }
}