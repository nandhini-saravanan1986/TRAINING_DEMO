package com.bornfire.brf.controllers;

import java.util.List;


import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.bornfire.brf.entities.BRF_052_ARCHIVE_INDEX_Entity;
import com.bornfire.brf.services.BRF_Archive_Service;

@Controller
@RequestMapping(value = "Archive")
public class BRF_Archive_Controller {

    private static final Logger logger = LoggerFactory.getLogger(BRF_Archive_Controller.class);

    @Autowired
    BRF_Archive_Service archiveService;

    // ─────────────────────────────────────────────────────────────
    // ENDPOINT 1 — Archive Report Selection List
    // URL : GET /BCDRS/Archive
    // View: Archive_Report_List.html
    // Shows all reports that support archiving (currently BRF_052).
    // ─────────────────────────────────────────────────────────────
    @RequestMapping(method = RequestMethod.GET)
    public String archiveHome(Model model, HttpServletRequest req) {

        String userId = (String) req.getSession().getAttribute("USERID");
        logger.info("Archive home accessed by user={}", userId);

        // Hardcoded list — add entries here when new reports support archive
        model.addAttribute("reportList", archiveService.getAvailableReports());
        model.addAttribute("menu", "ARCHIVE");

        return "BRRS/Archive_Report_List";
    }

    // ─────────────────────────────────────────────────────────────
    // ENDPOINT 2 — Archive Index (date list for a specific report)
    // URL : GET /BCDRS/Archive/{reportCode}
    // View: Archive_Index.html
    // e.g. /BCDRS/Archive/BRF_052 → shows all archived dates for BRF_052
    // ─────────────────────────────────────────────────────────────
    @RequestMapping(value = "{reportCode}", method = RequestMethod.GET)
    public String archiveIndex(
            @PathVariable("reportCode") String reportCode,
            Model model,
            HttpServletRequest req) {

        String userId = (String) req.getSession().getAttribute("USERID");
        logger.info("Archive index → reportCode={} user={}", reportCode, userId);

        List<BRF_052_ARCHIVE_INDEX_Entity> archiveList =
                archiveService.getArchiveList(reportCode);

        model.addAttribute("archiveList",  archiveList);
        model.addAttribute("reportCode",   reportCode);
        model.addAttribute("menu",         "ARCHIVE");

        return "BRRS/Archive_Index";
    }

    // ─────────────────────────────────────────────────────────────
    // ENDPOINT 3 — Archived Summary View
    // URL : GET /BCDRS/Archive/{reportCode}/{date}
    // e.g. /BCDRS/Archive/BRF_052/30-06-2025
    // Renders BRF_052.html in archiveMode=true (read-only)
    // ─────────────────────────────────────────────────────────────
    @RequestMapping(value = "{reportCode}/{date}", method = RequestMethod.GET)
    public ModelAndView archiveSummaryView(
            @PathVariable("reportCode") String reportCode,
            @PathVariable("date")       String date,
            HttpServletRequest req) {

        String userId = (String) req.getSession().getAttribute("USERID");
        logger.info("Archive summary view → reportCode={} date={} user={}", reportCode, date, userId);

        return archiveService.getArchiveSummaryView(reportCode, date);
    }

 // ─────────────────────────────────────────────────────────────
 // ENDPOINT 4 — Archived Detail View — paginated
 // URL : GET /BCDRS/Archive/{reportCode}/{date}/detail
 // ─────────────────────────────────────────────────────────────
 @RequestMapping(value = "{reportCode}/{date}/detail", method = RequestMethod.GET)
 public ModelAndView archiveDetailView(
         @PathVariable("reportCode")                            String reportCode,
         @PathVariable("date")                                  String date,
         @RequestParam(value = "page",       defaultValue = "0")  int page,
         @RequestParam(value = "size",       defaultValue = "20") int size,
         @RequestParam(value = "filterRowId", required = false)   String filterRowId,
         @RequestParam(value = "filterColId", required = false)   String filterColId,
         HttpServletRequest req) {

     String userId = (String) req.getSession().getAttribute("USERID");
     logger.info("Archive detail view → reportCode={} date={} page={} user={}",
             reportCode, date, page, userId);

     return archiveService.getArchiveDetailView(reportCode, date, page, size, filterRowId, filterColId);
 }
 // ─────────────────────────────────────────────────────────────
    // ENDPOINT 5 — Archive Summary Excel Download
    // URL : GET /BCDRS/Archive/BRF_052/downloadSummary?todate=
    // ─────────────────────────────────────────────────────────────
    @RequestMapping(value = "BRF_052/downloadSummary", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<ByteArrayResource> downloadArchiveSummary(
            @RequestParam("todate") String todate,
            HttpServletResponse response) {

        response.setContentType("application/octet-stream");
        logger.info("Archive downloadSummary → todate={}", todate);

        try {
            byte[] excelData = archiveService.getArchiveSummaryExcel("BRF_052", todate);

            if (excelData == null || excelData.length == 0) {
                logger.warn("Archive downloadSummary: no data for date {}", todate);
                return ResponseEntity.noContent().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=BRF_052_Archive.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(excelData.length)
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new ByteArrayResource(excelData));

        } catch (Exception e) {
            logger.error("Archive downloadSummary ERROR", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ENDPOINT 6 — Archive Detail Excel Download
    // URL : GET /BCDRS/Archive/BRF_052/downloadDetail?todate=
    // ─────────────────────────────────────────────────────────────
    @RequestMapping(value = "BRF_052/downloadDetail", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<ByteArrayResource> downloadArchiveDetail(
            @RequestParam("todate") String todate,
            HttpServletResponse response) {

        response.setContentType("application/octet-stream");
        logger.info("Archive downloadDetail → todate={}", todate);

        try {
            byte[] excelData = archiveService.getArchiveDetailExcel("BRF_052", todate);

            if (excelData == null || excelData.length == 0) {
                logger.warn("Archive downloadDetail: no data for date {}", todate);
                return ResponseEntity.noContent().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=BRF_052_Archive_Detail.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(excelData.length)
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new ByteArrayResource(excelData));

        } catch (Exception e) {
            logger.error("Archive downloadDetail ERROR", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
 // ─────────────────────────────────────────────────────────────
 // ENDPOINT 7 — Archive Summary PDF Download
 // URL : GET /BCDRS/Archive/BRF_052/downloadSummaryPdf?todate=
 // ─────────────────────────────────────────────────────────────
 @RequestMapping(value = "BRF_052/downloadSummaryPdf", method = RequestMethod.GET)
 public void downloadArchiveSummaryPdf(
         @RequestParam("todate") String todate,
         HttpServletResponse response,
         HttpServletRequest req) {

     logger.info("Archive downloadSummaryPdf → todate={}", todate);
     try {
         byte[] pdfBytes = archiveService.getArchiveSummaryPdf("BRF_052", todate);
         if (pdfBytes == null || pdfBytes.length == 0) {
             response.setStatus(HttpServletResponse.SC_NO_CONTENT);
             return;
         }
         response.setContentType("application/pdf");
         response.setHeader("Content-Disposition", "attachment; filename=\"BRF_052_Archive.pdf\"");
         response.setContentLength(pdfBytes.length);
         try (javax.servlet.ServletOutputStream out = response.getOutputStream()) {
             out.write(pdfBytes);
             out.flush();
         }
     } catch (Exception e) {
         logger.error("Archive downloadSummaryPdf ERROR", e);
         response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
 }

 // ─────────────────────────────────────────────────────────────
 // ENDPOINT 8 — Archive Detail PDF Download
 // URL : GET /BCDRS/Archive/BRF_052/downloadDetailPdf?todate=
 // ─────────────────────────────────────────────────────────────
 @RequestMapping(value = "BRF_052/downloadDetailPdf", method = RequestMethod.GET)
 public void downloadArchiveDetailPdf(
         @RequestParam("todate") String todate,
         HttpServletResponse response,
         HttpServletRequest req) {

     logger.info("Archive downloadDetailPdf → todate={}", todate);
     try {
         byte[] pdfBytes = archiveService.getArchiveDetailPdf("BRF_052", todate);
         if (pdfBytes == null || pdfBytes.length == 0) {
             response.setStatus(HttpServletResponse.SC_NO_CONTENT);
             return;
         }
         response.setContentType("application/pdf");
         response.setHeader("Content-Disposition", "attachment; filename=\"BRF_052_Archive_Detail.pdf\"");
         response.setContentLength(pdfBytes.length);
         try (javax.servlet.ServletOutputStream out = response.getOutputStream()) {
             out.write(pdfBytes);
             out.flush();
         }
     } catch (Exception e) {
         logger.error("Archive downloadDetailPdf ERROR", e);
         response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
 }
}