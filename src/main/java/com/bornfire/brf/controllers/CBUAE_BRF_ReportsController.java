package com.bornfire.brf.controllers;

import java.io.FileNotFoundException;
import com.bornfire.brf.services.BRF_052_ReportService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.bornfire.brf.entities.M_IS_Summary_Entity1;
import com.bornfire.brf.entities.M_IS_Summary_Entity2;
import com.bornfire.brf.services.BRF_67_ReportService;
import com.bornfire.brf.services.BRRS_M_CALOC_ReportService;
import com.bornfire.brf.services.BRRS_M_IS_ReportService;
import com.bornfire.brf.services.RegulatoryReportServices;
import com.bornfire.brf.services.BRF_008_A_ReportService;


@Controller
@ConfigurationProperties("default")
@RequestMapping(value = "Reports")
public class CBUAE_BRF_ReportsController {
	
	private static final Logger logger = LoggerFactory.getLogger(CBUAE_BRF_ReportsController.class);
		@Autowired
		RegulatoryReportServices regreportServices;
		
		@Autowired
		BRF_67_ReportService BRF_67_reportservice;
	
		@Autowired
		BRRS_M_IS_ReportService M_IS_Service;
		@Autowired
		BRF_008_A_ReportService BRF_008_A_reportservice;
		private String pagesize;
		@Autowired
	    BRF_052_ReportService BRF_052_reportservice;
		
		@Autowired
		BRRS_M_CALOC_ReportService BRRS_M_CALOC_reportService;
	
		public String getPagesize() {
			return pagesize;
		}
	
		public void setPagesize(String pagesize) {
			this.pagesize = pagesize;
		}
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

		// ─────────────────────────────────────────────────────────────────────
		// BRF_008_A DETAIL EDIT — save changes for a single detail record
		// ─────────────────────────────────────────────────────────────────────
		@RequestMapping(value = "BRF_008_A/editDetail", method = RequestMethod.POST)
		@ResponseBody
		public ResponseEntity<String> editBRF008ADetail(
		        @RequestParam("custId")               String custId,
		        @RequestParam("acctNumber")            String acctNumber,
		        @RequestParam("acctName")              String acctName,
		        @RequestParam("dataType")              String dataType,
		        @RequestParam("rowId")                 String rowId,
		        @RequestParam("columnId")              String columnId,
		        @RequestParam("amountAed")             BigDecimal amountAed,
		        @RequestParam("reportRemarks")         String reportRemarks,
		        @RequestParam("modificationRemarks")   String modificationRemarks,
		        @RequestParam("dataEntryVersion")      String dataEntryVersion,
		        @RequestParam("reportDate")            String reportDate,
		        HttpServletRequest req) {

		    String userId = (String) req.getSession().getAttribute("USERID");
		    logger.info("BRF_008_A editDetail called — custId={} by user={}", custId, userId);
		    try {
		        String result = BRF_008_A_reportservice.updateDetailRecord(
		                custId, acctNumber, acctName, dataType, rowId, columnId,
		                amountAed, reportRemarks, modificationRemarks,
		                dataEntryVersion, reportDate, userId);
		        return ResponseEntity.ok(result);
		    } catch (Exception e) {
		        logger.error("BRF_008_A editDetail ERROR for custId={}", custId, e);
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		                .body("Update failed: " + e.getMessage());
		    }
		}

		// ─────────────────────────────────────────────────────────────────────
		// BRF_008_A DETAIL DOWNLOAD — uses source temp (template) approach
		// ─────────────────────────────────────────────────────────────────────
		@RequestMapping(value = "BRF_008_A/downloadDetail", method = RequestMethod.GET)
		@ResponseBody
		public ResponseEntity<ByteArrayResource> downloadBRF008ADetail(
		        HttpServletResponse response,
		        @RequestParam("todate")   String todate,
		        @RequestParam("fromdate") String fromdate) {

		    response.setContentType("application/octet-stream");

		    System.out.println("BRF_008_A downloadDetail called — todate=" + todate + " fromdate=" + fromdate);

		    try {
		        // dates arrive as dd/MM/yyyy from the detail page — convert to dd-MMM-yyyy
		        try {
		            fromdate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(fromdate));
		            todate   = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(todate));
		        } catch (ParseException e) {
		            // already in dd-MMM-yyyy format — leave as-is
		            System.out.println("Date already converted, keeping as-is: " + todate);
		        }

		        // Pass template filename for source temp approach
		        byte[] excelData = BRF_008_A_reportservice.BRF_008_A_DetailExcel("BRF_008_A_Detail.xlsx", fromdate, todate);

		        if (excelData == null || excelData.length == 0) {
		            logger.warn("BRF_008_A downloadDetail: no data for date {}", todate);
		            return ResponseEntity.noContent().build();
		        }

		        ByteArrayResource resource = new ByteArrayResource(excelData);
		        HttpHeaders headers = new HttpHeaders();
		        headers.add(HttpHeaders.CONTENT_DISPOSITION,
		                    "attachment; filename=BRF_008_A_Detail.xlsx");

		        return ResponseEntity.ok()
		                .headers(headers)
		                .contentLength(excelData.length)
		                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
		                .body(resource);

		    } catch (Exception e) {
		        logger.error("BRF_008_A downloadDetail ERROR", e);
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		    }
		}

		// ─────────────────────────────────────────────────────────────────────
		// BRF_008_A SUMMARY DOWNLOAD — uses source temp (template) approach
		// ─────────────────────────────────────────────────────────────────────
		@RequestMapping(value = "BRF_008_A/downloadSummary", method = RequestMethod.GET)
		@ResponseBody
		public ResponseEntity<ByteArrayResource> downloadBRF008ASummary(
		        HttpServletResponse response,
		        @RequestParam("todate")   String todate,
		        @RequestParam("fromdate") String fromdate) {

		    response.setContentType("application/octet-stream");
		    try {
		        try {
		            fromdate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(fromdate));
		            todate   = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(todate));
		        } catch (ParseException e) {
		            System.out.println("Date already converted: " + todate);
		        }

		        // Pass template filename for source temp approach
		        byte[] excelData = BRF_008_A_reportservice.BRF_008_A_Excel(
		                "BRF_008_A.xlsx", "BRF_008_A", fromdate, todate, null, null);

		        if (excelData == null || excelData.length == 0)
		            return ResponseEntity.noContent().build();

		        HttpHeaders headers = new HttpHeaders();
		        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=BRF_008_A.xlsx");
		        return ResponseEntity.ok().headers(headers).contentLength(excelData.length)
		                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
		                .body(new ByteArrayResource(excelData));

		    } catch (Exception e) {
		        logger.error("BRF_008_A downloadSummary ERROR", e);
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		    }
		}
		
		// ─────────────────────────────────────────────────────────────────────────
	    // BRF_052 SUMMARY EXCEL DOWNLOAD — Source Temp (Template) Approach
	    // Called by: downloadSummaryExcel() in BRF_052.html
	    //   URL: /BCDRS/Reports/BRF_052/downloadSummary?fromdate=...&todate=...
	    // ─────────────────────────────────────────────────────────────────────────
		@RequestMapping(value = "BRF_052/downloadSummary", method = RequestMethod.GET)
	    @ResponseBody
	    public ResponseEntity<ByteArrayResource> downloadBRF052Summary(
	            HttpServletResponse response,
	            @RequestParam("todate")   String todate,
	            @RequestParam("fromdate") String fromdate) {

	        response.setContentType("application/octet-stream");
	        logger.info("BRF_052 downloadSummary called — todate={} fromdate={}", todate, fromdate);

	        try {
	            // Dates arrive as dd/MM/yyyy from the page — convert to dd-MMM-yyyy
	            // (the service's dateformat expects dd-MMM-yyyy)
	            try {
	                fromdate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(fromdate));
	                todate   = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(todate));
	            } catch (ParseException e) {
	                // Already in dd-MMM-yyyy — leave as-is
	                logger.info("BRF_052 downloadSummary: dates already converted, keeping as-is: {}", todate);
	            }

	            byte[] excelData = BRF_052_reportservice.BRF_052_Excel(
	                    "BRF_052.xlsx",   // template filename in exportpathtemp directory
	                    "BRF_052",        // reportId
	                    fromdate,
	                    todate,
	                    null,             // currency — not used by this report
	                    null);            // dtltype  — not used by this report

	            if (excelData == null || excelData.length == 0) {
	                logger.warn("BRF_052 downloadSummary: no data for date {}", todate);
	                return ResponseEntity.noContent().build();
	            }

	            HttpHeaders headers = new HttpHeaders();
	            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=BRF_052.xlsx");

	            logger.info("BRF_052 downloadSummary: sending {} bytes", excelData.length);
	            return ResponseEntity.ok()
	                    .headers(headers)
	                    .contentLength(excelData.length)
	                    .contentType(MediaType.parseMediaType(
	                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
	                    .body(new ByteArrayResource(excelData));

	        } catch (Exception e) {
	            logger.error("BRF_052 downloadSummary ERROR", e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	        }
	    }
		// ─────────────────────────────────────────────────────────────────────
		// BRF_052 DETAIL EDIT — save changes for a single detail record
		// ─────────────────────────────────────────────────────────────────────
		@RequestMapping(value = "BRF_052/editDetail", method = RequestMethod.POST)
		@ResponseBody
		public ResponseEntity<String> editBRF052Detail(
		        @RequestParam("custId")          String custId,
		        @RequestParam("acctNumber")      String acctNumber,
		        @RequestParam("acctName")        String acctName,
		        @RequestParam("rowId")           String rowId,
		        @RequestParam("columnId")        String columnId,
		        @RequestParam("amountAed")       BigDecimal amountAed,
		        @RequestParam("reportRemarks")   String reportRemarks,
		        @RequestParam("reportDate")      String reportDate,
		        HttpServletRequest req) {

		    String userId = (String) req.getSession().getAttribute("USERID");
		    logger.info("BRF_052 editDetail called — custId={} by user={}", custId, userId);
		    try {
		        String result = BRF_052_reportservice.updateDetailRecord(
		                custId, acctNumber, acctName, rowId, columnId,
		                amountAed, reportRemarks, reportDate, userId);
		        return ResponseEntity.ok(result);
		    } catch (Exception e) {
		        logger.error("BRF_052 editDetail ERROR for custId={}", custId, e);
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		                .body("Update failed: " + e.getMessage());
		    }
		}
		// ─────────────────────────────────────────────────────────────────────
        // BRF_052 GENERATE REPORT — Runs BRF_052_P stored procedure to refresh
        // summary tables from current detail data.
        // URL: POST /BCDRS/Reports/BRF_052/generateReport?todate=...
        // ─────────────────────────────────────────────────────────────────────
        @RequestMapping(value = "BRF_052/generateReport", method = RequestMethod.POST)
        @ResponseBody
        public ResponseEntity<String> generateBRF052Report(
                @RequestParam("todate") String todate,
                HttpServletRequest req) {

            String userId = (String) req.getSession().getAttribute("USERID");
            logger.info("BRF_052 generateReport called — todate={} by user={}", todate, userId);
            try {
                String result = BRF_052_reportservice.generateSummary(todate);
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                logger.error("BRF_052 generateReport ERROR for todate={}", todate, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to generate report: " + e.getMessage());
            }
        }
		// ─────────────────────────────────────────────────────────────────────────
		// BRF_052 DETAIL EXCEL DOWNLOAD
		// Called by: downloadDetailExcel() in BRF_052.html
		//   URL: /BCDRS/Reports/BRF_052/downloadDetail?fromdate=...&todate=...
		// ─────────────────────────────────────────────────────────────────────────
		@RequestMapping(value = "BRF_052/downloadDetail", method = RequestMethod.GET)
		@ResponseBody
		public ResponseEntity<ByteArrayResource> downloadBRF052Detail(
		        HttpServletResponse response,
		        @RequestParam("todate")   String todate,
		        @RequestParam("fromdate") String fromdate) {

		    response.setContentType("application/octet-stream");
		    logger.info("BRF_052 downloadDetail called — todate={} fromdate={}", todate, fromdate);

		    try {
		        try {
		            fromdate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(fromdate));
		            todate   = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(todate));
		        } catch (ParseException e) {
		            logger.info("BRF_052 downloadDetail: dates already converted: {}", todate);
		        }

		        byte[] excelData = BRF_052_reportservice.BRF_052_DetailExcel("BRF_052_Detail.xlsx", fromdate, todate);

		        if (excelData == null || excelData.length == 0) {
		            logger.warn("BRF_052 downloadDetail: no data for date {}", todate);
		            return ResponseEntity.noContent().build();
		        }

		        HttpHeaders headers = new HttpHeaders();
		        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=BRF_052_Detail.xlsx");

		        return ResponseEntity.ok()
		                .headers(headers)
		                .contentLength(excelData.length)
		                .contentType(MediaType.parseMediaType(
		                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
		                .body(new ByteArrayResource(excelData));

		    } catch (Exception e) {
		        logger.error("BRF_052 downloadDetail ERROR", e);
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		    }
		}
		
		// ─────────────────────────────────────────────────────────────
		// BRF_052 SUMMARY PDF DOWNLOAD — centralized (same as BRF_008)
		// ─────────────────────────────────────────────────────────────
		@RequestMapping(value = "BRF_052/downloadSummaryPdf", method = RequestMethod.GET)
		public void downloadBRF052SummaryPdf(
		        HttpServletResponse response,
		        @RequestParam("todate")   String todate,
		        @RequestParam("fromdate") String fromdate) {

		    logger.info("BRF_052 downloadSummaryPdf called — todate={} fromdate={}", todate, fromdate);

		    try {
		        // Convert dates from dd/MM/yyyy → dd-MMM-yyyy
		        try {
		            fromdate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(fromdate));
		            todate   = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(todate));
		        } catch (ParseException e) {
		            logger.info("BRF_052 downloadSummaryPdf: dates already converted, keeping as-is: {}", todate);
		        }

		        // Step 1: Get PDF bytes via centralized service
		        byte[] pdfBytes = regreportServices.getPdfDownloadFile(
		                "BRF_052",           // reportId (must match switch-case)
		                "BRF_052.xlsx",      // template filename
		                null,                // asondate
		                fromdate,
		                todate,
		                "AED",               // currency
		                null,                // subreportid
		                null,                // secid
		                null,                // dtltype
		                null,                // reportingTime
		                null,                // instancecode
		                null                 // filter
		        );

		        if (pdfBytes == null || pdfBytes.length == 0) {
		            logger.warn("BRF_052 downloadSummaryPdf: no data / PDF generation returned empty");
		            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		            return;
		        }

		        // Step 2: Stream PDF to browser
		        response.setContentType("application/pdf");
		        response.setHeader("Content-Disposition", "attachment; filename=\"BRF_052.pdf\"");
		        response.setContentLength(pdfBytes.length);

		        try (ServletOutputStream out = response.getOutputStream()) {
		            out.write(pdfBytes);
		            out.flush();
		        }

		    } catch (Exception e) {
		        logger.error("BRF_052 downloadSummaryPdf ERROR", e);
		        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		    }
		}
		// ─────────────────────────────────────────────────────────────
		// M_IS SUMMARY PDF DOWNLOAD
		// Generates filled Excel from M_IS.xlsx template, converts to
		// PDF using the table-range-aware overload in Exceltopdfservice
		// (two PdfPTables — one per table on the sheet).
		// ─────────────────────────────────────────────────────────────
		@RequestMapping(value = "M_IS/downloadSummaryPdf", method = RequestMethod.GET)
		public void downloadM_ISSummaryPdf(
		        HttpServletResponse response,
		        @RequestParam("todate")   String todate,
		        @RequestParam("fromdate") String fromdate) {

		    logger.info("M_IS downloadSummaryPdf called — todate={} fromdate={}", todate, fromdate);

		    try {
		        // Convert dates from dd/MM/yyyy → dd-MMM-yyyy if needed
		        try {
		            fromdate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(fromdate));
		            todate   = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(todate));
		        } catch (ParseException e) {
		            logger.info("M_IS downloadSummaryPdf: dates already in correct format, keeping as-is: {}", todate);
		        }

		        // Step 1: Get PDF bytes via centralized service
		        byte[] pdfBytes = regreportServices.getPdfDownloadFile(
		                "M_IS",        // reportId — matches case "M_IS" in getPdfDownloadFile
		                "M_IS.xlsx",   // template filename
		                null,          // asondate
		                fromdate,
		                todate,
		                "AED",         // currency
		                null,          // subreportid
		                null,          // secid
		                null,          // dtltype
		                null,          // reportingTime
		                null,          // instancecode
		                null           // filter
		        );

		        if (pdfBytes == null || pdfBytes.length == 0) {
		            logger.warn("M_IS downloadSummaryPdf: no data / PDF generation returned empty");
		            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		            return;
		        }

		        // Step 2: Stream PDF to browser
		        response.setContentType("application/pdf");
		        response.setHeader("Content-Disposition", "attachment; filename=\"M_IS.pdf\"");
		        response.setContentLength(pdfBytes.length);

		        try (ServletOutputStream out = response.getOutputStream()) {
		            out.write(pdfBytes);
		            out.flush();
		        }

		    } catch (Exception e) {
		        logger.error("M_IS downloadSummaryPdf ERROR", e);
		        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		    }
		}
		// ─────────────────────────────────────────────────────────────
		// BRF_052 DETAIL VIEW — paginated
		// ─────────────────────────────────────────────────────────────
		@RequestMapping(value = "BRF_052/detail", method = RequestMethod.GET)
		public ModelAndView getBRF052Detail(
		        @RequestParam("fromdate")                          String fromdate,
		        @RequestParam("todate")                            String todate,
		        @RequestParam(value = "currency",   required = false) String currency,
		        @RequestParam(value = "dtltype",    required = false) String dtltype,
		        @RequestParam(value = "type",       required = false) String type,
		        @RequestParam(value = "page",       defaultValue = "0")  int page,
		        @RequestParam(value = "size",       defaultValue = "20") int size,
		        @RequestParam(value = "filterRowId", required = false)   String filterRowId,
		        @RequestParam(value = "filterColId", required = false)   String filterColId) {

		    logger.info("BRF_052 detail view called → todate={} fromdate={} page={} filter={}/{}",
		            todate, fromdate, page, filterRowId, filterColId);

		    return BRF_052_reportservice.getBRF052DetailView(
		            "BRF_052", fromdate, todate, currency, dtltype, type,
		            page, size, filterRowId, filterColId
		    );
		}
		    // ─────────────────────────────────────────────────────────────────────
	    // BRF_008_A SUMMARY PDF DOWNLOAD — generate Excel from template, then convert to PDF
	    // ─────────────────────────────────────────────────────────────────────
	    @RequestMapping(value = "BRF_008_A/downloadSummaryPdf", method = RequestMethod.GET)
	    public void downloadBRF008ASummaryPdf(
	            HttpServletResponse response,
	            @RequestParam("todate")   String todate,
	            @RequestParam("fromdate") String fromdate) {

	        logger.info("BRF_008_A downloadSummaryPdf called — todate={} fromdate={}", todate, fromdate);
	        try {
	            // Convert dates from dd/MM/yyyy → dd-MMM-yyyy (service expects dd-MMM-yyyy)
	            try {
	                fromdate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(fromdate));
	                todate   = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(todate));
	            } catch (ParseException e) {
	                logger.info("BRF_008_A downloadSummaryPdf: dates already converted, keeping as-is: {}", todate);
	            }

	            // Step 1: Generate Excel bytes using the source-temp template approach
	            byte[] pdfBytes = regreportServices.getPdfDownloadFile(
	                    "BRF_008_A",           // reportId — matched in switch
	                    "BRF_008_A.xlsx",      // template filename
	                    null,                  // asondate
	                    fromdate,
	                    todate,
	                    "AED",                 // currency
	                    null,                  // subreportid
	                    null,                  // secid
	                    null,                  // dtltype
	                    null,                  // reportingTime
	                    null,                  // instancecode
	                    null                   // filter
	            );

	            if (pdfBytes == null || pdfBytes.length == 0) {
	                logger.warn("BRF_008_A downloadSummaryPdf: no data / PDF generation returned empty");
	                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	                return;
	            }

	            // Step 2: Stream the PDF to the browser
	            response.setContentType("application/pdf");
	            response.setHeader("Content-Disposition", "attachment; filename=\"BRF_008_A.pdf\"");
	            response.setContentLength(pdfBytes.length);
	            try (ServletOutputStream out = response.getOutputStream()) {
	                out.write(pdfBytes);
	                out.flush();
	            }

	        } catch (Exception e) {
	            logger.error("BRF_008_A downloadSummaryPdf ERROR", e);
	            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        }
	    }

	@RequestMapping(value = "{reportid}", method = { RequestMethod.GET, RequestMethod.POST })
		public ModelAndView reportView(@PathVariable("reportid") String reportid,
				@RequestParam(value = "function", required = false) String function,
				@RequestParam("asondate") String asondate, @RequestParam(required = false) String fromdate,
				@RequestParam("todate") String todate, @RequestParam(value = "currency", required = false) String currency,
				@RequestParam(value = "subreportid", required = false) String subreportid,
				@RequestParam(value = "secid", required = false) String secid,
				@RequestParam(value = "dtltype", required = false) String dtltype,
				@RequestParam(value = "type", required = false) String type,
				@RequestParam(value = "version", required = false) BigDecimal version,
				@RequestParam(value = "page", required = false) Optional<Integer> page,
				@RequestParam(value = "size", required = false) Optional<Integer> size,
				@RequestParam(value = "reportingTime", required = false) String reportingTime, Model md,
				HttpServletRequest req, BigDecimal srl_no) throws ParseException {

			String userid = (String) req.getSession().getAttribute("USERID");
			String roleid = (String) req.getSession().getAttribute("ROLEID");
			String accesscode = (String) req.getSession().getAttribute("ACCESSCODE");

			int currentPage = page.orElse(0);
			int pageSize = size.orElse(Integer.parseInt(pagesize));
			System.out.println("date" + fromdate);
			// Assigning required Modal Attributes
			md.addAttribute("UserId", userid);
			md.addAttribute("RoleId", roleid);
			md.addAttribute("UserCol", accesscode);
			md.addAttribute("reportid", reportid);
			md.addAttribute("asondate", asondate);
			md.addAttribute("fromdate", fromdate);
			md.addAttribute("todate", todate);
			md.addAttribute("currency", currency);
			md.addAttribute("dtltype", dtltype);
			md.addAttribute("type", type);
			md.addAttribute("version", version);
			md.addAttribute("reportingTime", reportingTime);
			// md.addAttribute("reportTitle", reportServices.getReportName(reportid));

			try {
			    asondate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(asondate));
			    fromdate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(fromdate));
			    todate   = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(todate));
			} catch (ParseException e) {
			    // dates already in dd-MMM-yyyy — leave as-is
			    logger.info("reportView: dates already converted, keeping as-is");
			}

			ModelAndView mv = new ModelAndView();
			mv = regreportServices.getReportView(reportid, asondate, fromdate, todate, currency, dtltype, subreportid,
					secid, reportingTime, PageRequest.of(currentPage, pageSize), srl_no, userid, type, version);

			return mv;

		}

		@RequestMapping(value = "{reportid}/Details", method = RequestMethod.GET)
		public ModelAndView reportDetail(@PathVariable("reportid") String reportid,
				@RequestParam(value = "instancecode", required = false) String instancecode,
				@RequestParam(value = "filter", required = false) String filter, @RequestParam("asondate") String asondate,
				@RequestParam("fromdate") String fromdate, @RequestParam("todate") String todate,
				@RequestParam("currency") String currency, @RequestParam(value = "type", required = false) String type,
				@RequestParam(value = "version", required = false) String version,
				@RequestParam(value = "subreportid", required = false) String subreportid,
				@RequestParam(value = "secid", required = false) String secid,
				@RequestParam(value = "dtltype", required = false) String dtltype,
				@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size,
				@RequestParam(value = "reportingTime", required = false) String reportingTime, Model md) {

			md.addAttribute("reportid", reportid);
			md.addAttribute("asondate", asondate);
			md.addAttribute("fromdate", fromdate);
			md.addAttribute("todate", todate);
			md.addAttribute("filter", filter);
			md.addAttribute("currency", currency);
			md.addAttribute("dtltype", dtltype);
			md.addAttribute("reportingTime", reportingTime);
			md.addAttribute("type", type);
			md.addAttribute("version", version);
			// md.addAttribute("instancecode", Integer.parseInt(instancecode));
			// md.addAttribute("reportTitle", reportServices.getReportName(reportid));
			md.addAttribute("displaymode", "detail");

			try {
				asondate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(asondate));
				fromdate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(fromdate));
				todate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(todate));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			// logger.info("Getting ModelandView :" + reportid);
			ModelAndView mv = regreportServices.getReportDetails(reportid, instancecode, asondate, fromdate, todate,
					currency, reportingTime, dtltype, subreportid, secid, PageRequest.of(page, size), filter, type,
					version);

			return mv;
		}
		
		@RequestMapping(value = "downloadExcel", method = { RequestMethod.GET, RequestMethod.POST })
		@ResponseBody
		public ResponseEntity<ByteArrayResource> BRFDownload(HttpServletResponse response,
				@RequestParam("reportid") String reportid, @RequestParam("asondate") String asondate,
				@RequestParam("fromdate") String fromdate, @RequestParam("todate") String todate,
				@RequestParam("currency") String currency, @RequestParam(value = "type", required = false) String type,
				@RequestParam(value = "format", required = false) String format,
				@RequestParam(value = "version", required = false) String versionBD,
				@RequestParam(value = "subreportid", required = false) String subreportid,
				@RequestParam(value = "secid", required = false) String secid,
				@RequestParam(value = "dtltype", required = false) String dtltype,
				@RequestParam(value = "reportingTime", required = false) String reportingTime,
				@RequestParam(value = "filename", required = false) String filename,
				@RequestParam(value = "instancecode", required = false) String instancecode,
				@RequestParam(value = "filter", required = false) String filter)
				throws SQLException, FileNotFoundException {

		
			response.setContentType("application/octet-stream");

			BigDecimal version = null;

			if (versionBD != null) {
				versionBD = versionBD.trim();
				if (!versionBD.isEmpty() && !"null".equalsIgnoreCase(versionBD)
						&& !"undefined".equalsIgnoreCase(versionBD)) {
					version = new BigDecimal(versionBD);
				}
			}

			try {
				asondate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(asondate));
				fromdate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(fromdate));
				todate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(todate));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			try {
				System.out.println("came to controller");
				byte[] excelData = regreportServices.getDownloadFile(reportid, filename, asondate, fromdate, todate,
						currency, subreportid, secid, dtltype, reportingTime, instancecode, filter, type, format, version);

				if (excelData == null || excelData.length == 0) {
					logger.warn("Controller: Service returned no data. Responding with 204 No Content.");
					return ResponseEntity.noContent().build();
				}

				ByteArrayResource resource = new ByteArrayResource(excelData);

				HttpHeaders headers = new HttpHeaders();
				filename = filename + ".xlsx";
				headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

				logger.info("Controller: Sending file '{}' to client ({} bytes).", filename, excelData.length);
				return ResponseEntity.ok().headers(headers).contentLength(excelData.length)
						.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(resource);

			} catch (Exception e) {
				logger.error("Controller ERROR: A critical error occurred during file generation.", e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
		}

		@RequestMapping(value = "downloaddetailExcel", method = { RequestMethod.GET, RequestMethod.POST })
		@ResponseBody
		public ResponseEntity<ByteArrayResource> detailDownload(HttpServletResponse response,
				@RequestParam("jobId") String jobId, @RequestParam("filename") String filename,
				@RequestParam(value = "type", required = false) String type,
				@RequestParam(value = "version", required = false) String version,
				@RequestParam(value = "todate", required = false) String todate)
				throws SQLException, FileNotFoundException {

			System.out.println("🔵 [CONTROLLER] DETAIL DOWNLOAD CALLED");
			System.out.println("JobId Passed = " + jobId);
			System.out.println("Filename Passed = " + filename);
			System.out.println("TYPE Passed = " + type);
			System.out.println("VERSION Passed = " + version);
			System.out.println("TODATE Passed = " + todate);

			response.setContentType("application/octet-stream");

			try {
				byte[] excelData = null;

				excelData = regreportServices.getReport(jobId);
				;

				if (excelData == null || excelData.length == 0) {
					logger.warn("Controller: Service returned no data. Responding with 204 No Content.");
					return ResponseEntity.noContent().build();
				}

				ByteArrayResource resource = new ByteArrayResource(excelData);

				HttpHeaders headers = new HttpHeaders();
				filename = filename + ".xls";
				headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

				logger.info("Controller: Sending file '{}' to client ({} bytes).", filename, excelData.length);
				return ResponseEntity.ok().headers(headers).contentLength(excelData.length)
						.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(resource);

			} catch (Exception e) {
				logger.error("Controller ERROR: A critical error occurred during file generation.", e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
		}

		@RequestMapping(value = "/startreport", method = { RequestMethod.GET, RequestMethod.POST })
		@ResponseBody // forces raw text instead of HTML view
		public String startReport(@RequestParam String filename, @RequestParam("fromdate") String fromdate,
				@RequestParam("todate") String todate, @RequestParam String currency,
				@RequestParam("dtltype") String dtltype, @RequestParam("type") String type,
				@RequestParam(value = "version", required = false) String version) {
			String jobId = UUID.randomUUID().toString();
			System.out.println("jobid" + jobId);
			logger.info("Getting Inside startreport");
			regreportServices.generateReportAsync(jobId, filename, fromdate, todate, dtltype, type, currency, version);
			// RT_SLSServices.generateReportAsync(jobId, filename, reportdate,
			// currency,version);
			return jobId;
		}

		@RequestMapping(value = "/checkreport", method = { RequestMethod.GET, RequestMethod.POST })
		@ResponseBody // forces raw text instead of HTML view
		public ResponseEntity<String> checkReport(@RequestParam String jobId) {
			byte[] report = regreportServices.getReport(jobId);
			// System.out.println("Report generation completed for: " + jobId);
			if (report == null) {
				return ResponseEntity.ok("PROCESSING");
			}
			if (report.length == 0) {
				return ResponseEntity.ok("ERROR");
			}

			return ResponseEntity.ok("READY");

		}
		 
			
			  @RequestMapping(value = "downloadpdf", method = { RequestMethod.GET,RequestMethod.POST })			  
			  public void BRFDownloadpdf(HttpServletResponse response,			  
			  @RequestParam("reportid") String reportid,			  
			  @RequestParam("asondate") String asondate,			  
			  @RequestParam("fromdate") String fromdate,			  
			  @RequestParam("todate") String todate,			  
			  @RequestParam("currency") String currency,			  
			  @RequestParam(value = "subreportid", required = false) String subreportid,			  
			  @RequestParam(value = "secid", required = false) String secid,			  
			  @RequestParam(value = "dtltype", required = false) String dtltype,			  
			  @RequestParam(value = "reportingTime", required = false) String reportingTime,			  
			  @RequestParam(value = "filename", required = false) String filename,			  
			  @RequestParam(value = "instancecode", required = false) String instancecode,			  
			  @RequestParam(value = "filter", required = false) String filter) throws
			  SQLException, FileNotFoundException {
				  try {
					  
					  DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

						try {

							fromdate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(fromdate));
							todate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(todate));
						} catch (ParseException e) {
							e.printStackTrace();
						}
				  
				  byte[] pdfBytes = regreportServices.getPdfDownloadFile(reportid, filename, asondate, fromdate, todate, currency,
			                subreportid, secid, dtltype, reportingTime, instancecode, filter);
			  
			  //  Write PDF to response 
				  response.setContentType("application/pdf");
			        response.setHeader("Content-Disposition", "attachment; filename=\"report.pdf\"");
			        response.setContentLength(pdfBytes.length);

			        try (ServletOutputStream out = response.getOutputStream()) {
			            out.write(pdfBytes);
			            out.flush();
			        }

			    } catch (Exception e) {
			        e.printStackTrace();
			    }
			}
			  
			  
			  @RequestMapping(value = "/M_ISupdateAll", method = { RequestMethod.GET, RequestMethod.POST })
				@ResponseBody
				public ResponseEntity<String> updateAllReports(
						@RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date asondate,

						@ModelAttribute M_IS_Summary_Entity1 request1, @ModelAttribute M_IS_Summary_Entity2 request2) {
					try {
						System.out.println("Came to single MIS Update controller");

						// set date into all 3 entities
						request1.setReportDate(asondate);
						request2.setReportDate(asondate);

						// call services
						M_IS_Service.MISUpdate1(request1);
						M_IS_Service.MISUpdate2(request2);

						return ResponseEntity.ok("Modified Successfully.");
					} catch (Exception e) {
						e.printStackTrace();
						return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Update Failed: " + e.getMessage());
					}
				}
 
			  
			  
			  
			/*
			 * @RequestMapping(value = "downloaddetailpdf", method = { RequestMethod.GET,
			 * RequestMethod.POST }) public void detailDownloadpdf(HttpServletResponse
			 * response,
			 * 
			 * @RequestParam("fromdate") String fromdate,
			 * 
			 * @RequestParam("todate") String todate,
			 * 
			 * @RequestParam(value = "filename", required = false) String filename) throws
			 * SQLException, FileNotFoundException { System.out.println("Control");
			 * 
			 * try { byte[] pdfBytes = regreportServices.getDetailPdfDownloadFile(filename,
			 * fromdate, todate);
			 * 
			 * // Write PDF to response response.setContentType("application/pdf");
			 * response.setHeader("Content-Disposition",
			 * "attachment; filename=\"report.pdf\"");
			 * response.setContentLength(pdfBytes.length);
			 * 
			 * try (ServletOutputStream out = response.getOutputStream()) {
			 * out.write(pdfBytes); out.flush(); } } catch (Exception e) { logger.
			 * error("Controller ERROR: A critical error occurred during file generation.",
			 * e); } }
			 */
			  @RequestMapping(value = "/startreportpdf", method = { RequestMethod.GET, RequestMethod.POST })
			   @ResponseBody 
			    public String startReportpdf(@RequestParam String filename,
			    						@RequestParam("fromdate") String fromdate,
			    						@RequestParam("todate") String todate,
			                            @RequestParam String currency,
			                            @RequestParam("dtltype") String dtltype,
			                            @RequestParam("type") String type, 
			                            @RequestParam(value = "version", required = false) String version) 	
			   {
			        String jobId = UUID.randomUUID().toString();
					regreportServices.generateReportAsyncpdf(jobId, filename, fromdate, todate, dtltype, type, currency, version);
			        //RT_SLSServices.generateReportAsync(jobId, filename, reportdate, currency,version);
			        return jobId;
			    }
			// ─────────────────────────────────────────────────────────────────────
			// M_CALOC DETAIL EDIT — load view/edit page for a single detail record
			// URL: GET /Reports/M_CALOC/Detailspage?formmode=...&acctNo=...&asondate=...
			// ─────────────────────────────────────────────────────────────────────
			@RequestMapping(value = "{reportid}/Detailspage", method = RequestMethod.GET)
			public ModelAndView getDetailEditPage(
			        @PathVariable("reportid") String reportid,
			        @RequestParam("acctNo")   String acctNo,
			        @RequestParam("formmode") String formmode) {

			    switch (reportid) {
			        case "M_CALOC":
			            return BRRS_M_CALOC_reportService.getViewOrEditPage(acctNo, formmode);
			        default:
			            logger.warn("Detailspage: no handler for reportid={}", reportid);
			            return new ModelAndView("error");
			    }
			}

			// ─────────────────────────────────────────────────────────────────────
			// M_CALOC DETAIL UPDATE — save edited detail record
			// URL: POST /Reports/M_CALOC/update
			// ─────────────────────────────────────────────────────────────────────
			@RequestMapping(value = "{reportid}/update", method = RequestMethod.POST)
			@ResponseBody
			public ResponseEntity<?> updateDetailRecord(
			        @PathVariable("reportid") String reportid,
			        HttpServletRequest request) {

			    switch (reportid) {
			        case "M_CALOC":
			            return BRRS_M_CALOC_reportService.updateDetailEdit(request);
			        default:
			            logger.warn("update: no handler for reportid={}", reportid);
			            return ResponseEntity.status(HttpStatus.NOT_FOUND)
			                    .body("No handler for reportid: " + reportid);
			    }
			}
			// ─────────────────────────────────────────────────────────────────────
			// M_CALOC SUMMARY PDF DOWNLOAD
			// URL: GET /Reports/M_CALOC/downloadSummaryPdf?fromdate=...&todate=...
			// ─────────────────────────────────────────────────────────────────────
			@RequestMapping(value = "M_CALOC/downloadSummaryPdf", method = RequestMethod.GET)
			public void downloadM_CALOCSummaryPdf(
			        HttpServletResponse response,
			        @RequestParam("todate")   String todate,
			        @RequestParam("fromdate") String fromdate) {

			    logger.info("M_CALOC downloadSummaryPdf called — todate={} fromdate={}", todate, fromdate);
			    try {
			        try {
			            fromdate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(fromdate));
			            todate   = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(todate));
			        } catch (ParseException e) {
			            logger.info("M_CALOC downloadSummaryPdf: dates already converted, keeping as-is: {}", todate);
			        }

			        byte[] pdfBytes = regreportServices.getPdfDownloadFile(
			                "M_CALOC",          // reportId — matches switch-case in RegulatoryReportServices
			                "M_CALOC.xlsx",     // template filename
			                null,               // asondate
			                fromdate,
			                todate,
			                null,               // currency
			                null,               // subreportid
			                null,               // secid
			                null,               // dtltype
			                null,               // reportingTime
			                null,               // instancecode
			                null                // filter
			        );

			        if (pdfBytes == null || pdfBytes.length == 0) {
			            logger.warn("M_CALOC downloadSummaryPdf: PDF generation returned empty");
			            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			            return;
			        }

			        response.setContentType("application/pdf");
			        response.setHeader("Content-Disposition", "attachment; filename=\"M_CALOC.pdf\"");
			        response.setContentLength(pdfBytes.length);

			        try (ServletOutputStream out = response.getOutputStream()) {
			            out.write(pdfBytes);
			            out.flush();
			        }

			    } catch (Exception e) {
			        logger.error("M_CALOC downloadSummaryPdf ERROR", e);
			        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			    }
			}
			  
				
				
				
}
