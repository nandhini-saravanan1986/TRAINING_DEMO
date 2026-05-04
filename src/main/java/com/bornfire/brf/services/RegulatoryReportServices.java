package com.bornfire.brf.services;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

@Component
@Service
@Transactional
@ConfigurationProperties("output")
public class RegulatoryReportServices {

	@Autowired
	CBUAE_BRF1_1_ReportService cbuae_brf1_1_reportservice;

	@Autowired
	CBUAE_BRF1_3_ReportService cbuae_brf1_3_reportservice;

	@Autowired
	CBUAE_BRF2_1_ReportService cbuae_brf2_1_reportservice;

	@Autowired
	CBUAE_BRF2_2_ReportService cbuae_brf2_2_reportservice;

	@Autowired
	CBUAE_BRF2_4_ReportService cbuae_brf2_4_reportservice;

	@Autowired
	CBUAE_BRF2_6_ReportService cbuae_brf2_6_reportservice;

	@Autowired
	CBUAE_BRF2_7_ReportService cbuae_brf2_7_reportservice;

	@Autowired
	BRRS_M_SFINP2_ReportService BRRS_M_SFINP2_reportservice;

	@Autowired
	BRF_67_ReportService BRF_67_reportservice;
	
	@Autowired
	BRF_70_Reportservice BRF_70_reportservice;
	
	@Autowired
	BRF65_ReportService BRF65_ReportService;
	
	@Autowired
	Exceltopdfservice exceltopdfservice;
	
	@Autowired
	BRRS_M_IS_ReportService BRRS_M_IS_reportservice;

	@Autowired
	BRF_008_A_ReportService BRF_008_A_reportservice;
	

    @Autowired
    BRF_052_ReportService BRF_052_reportservice;


	private static final Logger logger = LoggerFactory.getLogger(RegulatoryReportServices.class);

	public ModelAndView getReportView(String reportId, String reportDate, String fromdate, String todate,
			String currency, String dtltype, String subreportid, String secid, String reportingTime, Pageable pageable,
			BigDecimal srl_no, String req, String type, BigDecimal version) {

		ModelAndView repsummary = new ModelAndView();

		logger.info("Getting View for the Report :" + reportId);
		switch (reportId) {

		case "M_IS":

			repsummary = BRRS_M_IS_reportservice.getM_ISView(reportId, fromdate, todate, currency, dtltype, pageable,
					type, version);
			break;

		case "BRF_008_A":
			repsummary = BRF_008_A_reportservice.getBRF07View(reportId, fromdate, todate, currency, dtltype, pageable, type);
			break;
			
		case "BRF_052":
            repsummary = BRF_052_reportservice.getBRF052View(
                    reportId, fromdate, todate, currency, dtltype, type);
            break;

		}
		return repsummary;
	}

	public ModelAndView getReportDetails(String reportId, String instanceCode, String asondate, String fromdate,
			String todate, String currency, String reportingTime, String dtltype, String subreportid, String secid,
			Pageable pageable, String Filter, String type, String version) {

		ModelAndView repdetail = new ModelAndView();
		logger.info("Getting Details for the Report :" + reportId);

		switch (reportId) {

		case "M_IS":
			repdetail = BRRS_M_IS_reportservice.getM_IScurrentDtl(reportId, fromdate, todate, currency, dtltype,
					pageable, Filter, type, version);
			break;
		case "BRF_008_A":
		    repdetail = BRF_008_A_reportservice.getBRF008ADetailView(reportId, fromdate, todate, currency, dtltype, type);
		    break;
		
		}

		return repdetail;
	}

	public byte[] getDownloadFile(String reportId, String filename, String asondate, String fromdate, String todate,
			String currency, String subreportid, String secid, String dtltype, String reportingTime,
			String instancecode, String filter, String type, String format, BigDecimal version) {

		byte[] repfile = null;

		switch (reportId) {

		case "M_IS":
			try {
				repfile = BRRS_M_IS_reportservice.BRRS_M_ISExcel(filename, reportId, fromdate, todate, currency,
						dtltype, type, format, version);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
			
		case "BRF_052":
            try {
                repfile = BRF_052_reportservice.BRF_052_Excel(
                        filename,   // typically "BRF_052" — controller appends .xlsx
                        reportId,
                        fromdate,
                        todate,
                        currency,
                        dtltype);
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;

		case "BRF_008_A":
		    try {
		        if ("BRF_008_A_Detail".equals(filename) || "BRF_008_A_Detail.xlsx".equals(filename)) {
		            repfile = BRF_008_A_reportservice.BRF_008_A_DetailExcel(filename, fromdate, todate);
		        } else {
		            repfile = BRF_008_A_reportservice.BRF_008_A_Excel(filename, reportId, fromdate, todate, currency, dtltype);
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		    break;

		}

		return repfile;
	}
	

	public byte[] getDownloadDetailFile(String filename, String fromdate, String todate, String currency,
			String dtltype, String type, String version) {
		System.out.println("Came to Detail download normal method");

		if ("M_ISDetail".equals(filename)) {
			return BRRS_M_IS_reportservice.BRRS_M_ISDetailExcel(filename, fromdate, todate, currency, dtltype, type,
					version);

		}
		else {
			System.out.println("Default");
			System.out.println("Testting");
		}

		return new byte[0];

	}


	private final ConcurrentHashMap<String, byte[]> jobStorage = new ConcurrentHashMap<>();

	@Async
	public void generateReportAsync(String jobId, String filename, String fromdate, String todate, String dtltype,
			String type, String currency, String version) {

		byte[] fileData = null;

		if ("MSFinP2Detail".equals(filename)) {
			fileData = BRRS_M_SFINP2_reportservice.BRRS_M_SFINP2DetailExcel(filename, fromdate, todate);
		}

		if ("BRF_67_Detail".equals(filename)) {
			fileData = BRF_67_reportservice.BRF_67_DetailExcel(filename, fromdate, todate);
		}
		else if ("M_ISDetail".equals(filename)) {
			fileData = BRRS_M_IS_reportservice.BRRS_M_ISDetailExcel(filename, fromdate, todate, currency, dtltype, type,
					version);

		}
		// ✅ ADD THESE TWO — nothing else changes in this file
		else if ("BRF_052_Detail".equals(filename)) {
		    fileData = BRF_052_reportservice.BRF_052_DetailExcel(filename, fromdate, todate);
		}
		else if ("BRF_052_Archive_Detail".equals(filename)) {
		    fileData = BRF_052_reportservice.BRF_052_ArchiveDetailExcel(todate);
		}

		if (fileData == null) {
			logger.warn("Excel generation failed or no data for jobId: {}", jobId);
			jobStorage.put(jobId, "Nodata".getBytes());
		} else {
			jobStorage.put(jobId, fileData);
		}

		System.out.println("Report generation completed for: " + filename);
	}

	public byte[] getReport(String jobId) {
		return jobStorage.get(jobId);
	}

	public byte[] getPdfDownloadFile(String reportId, String filename, String asondate, String fromdate, String todate,
			String currency, String subreportid, String secid, String dtltype, String reportingTime,
			String instancecode, String filter) {

		byte[] excelBytes = null;
		byte[] pdfBytes = null;

        logger.info("PDF request → reportId={} fromdate={} todate={}", reportId, fromdate, todate);

        switch (reportId) {

            case "BRF_008_A":
                try {
                    // Step 1: Generate Excel
                    excelBytes = BRF_008_A_reportservice.BRF_008_A_Excel(
                            filename, reportId, fromdate, todate, currency, dtltype);

                    if (excelBytes == null || excelBytes.length == 0) {
                        logger.warn("No Excel data found for PDF generation → todate={}", todate);
                        return new byte[0];
                    }

                    logger.info("Excel generated successfully → size={} bytes", excelBytes.length);

                    // Step 2: Convert to PDF (CRITICAL FIX)
                    pdfBytes = exceltopdfservice.convertExcelBytesToPdf(excelBytes);

                    if (pdfBytes == null || pdfBytes.length == 0) {
                        logger.error("PDF conversion returned empty bytes");
                        return new byte[0];
                    }

                    logger.info("PDF conversion successful → size={} bytes", pdfBytes.length);

                } catch (Exception e) {
                    logger.error("PDF generation failed for BRF_008_A", e);
                    return new byte[0];
                }
                break;
            default:
                logger.warn("Unknown reportId for PDF generation: {}", reportId);
                return new byte[0];
                
            case "BRF_052":
                try {
                    // Step 1: Generate Excel (template-based)
                    excelBytes = BRF_052_reportservice.BRF_052_Excel(
                            filename,
                            reportId,
                            fromdate,
                            todate,
                            currency,
                            dtltype
                    );

                    if (excelBytes == null || excelBytes.length == 0) {
                        logger.warn("No Excel data found for BRF_052 PDF generation → todate={}", todate);
                        return new byte[0];
                    }

                    logger.info("BRF_052 Excel generated successfully → size={} bytes", excelBytes.length);

                    // Step 2: Convert Excel → PDF
                    pdfBytes = exceltopdfservice.convertExcelBytesToPdf(excelBytes);

                    if (pdfBytes == null || pdfBytes.length == 0) {
                        logger.error("BRF_052 PDF conversion returned empty bytes");
                        return new byte[0];
                    }

                    logger.info("BRF_052 PDF conversion successful → size={} bytes", pdfBytes.length);

                } catch (Exception e) {
                    logger.error("PDF generation failed for BRF_052", e);
                    return new byte[0];
                }
                break;
                
            case "M_IS":
                try {
                    // Step 1: Generate filled Excel from M_IS.xlsx template
                    excelBytes = BRRS_M_IS_reportservice.BRRS_M_ISExcel(
                            "M_IS.xlsx",
                            reportId,
                            fromdate,
                            todate,
                            currency,
                            dtltype,
                            null,      // type = null  → normal live-data path inside BRRS_M_ISExcel
                            "excel",   // format
                            null       // version = null → non-archival
                    );

                    if (excelBytes == null || excelBytes.length == 0) {
                        logger.warn("M_IS: No Excel data found for PDF generation → todate={}", todate);
                        return new byte[0];
                    }

                    logger.info("M_IS: Excel generated → {} bytes", excelBytes.length);

                    // Step 2: Convert using table-range-aware overload.
                    // Two tables share one sheet — each range gets its own maxCol
                    // so Table 1 (cols A–G) is not padded with Table 2's extra columns (H–I).
                    //
                    // POI rows 0–15  → bank/report header rows + Table 1 (5A Income on Investments)
                    // POI rows 16–37 → Table 2 (5B Investment Securities) + footnote row
                    List<int[]> tableRanges = Arrays.asList(
                            new int[]{0,  15},
                            new int[]{16, 37}
                    );
                    pdfBytes = exceltopdfservice.convertExcelBytesToPdf(excelBytes, tableRanges, false);

                    if (pdfBytes == null || pdfBytes.length == 0) {
                        logger.error("M_IS: PDF conversion returned empty bytes");
                        return new byte[0];
                    }

                    logger.info("M_IS: PDF conversion successful → {} bytes", pdfBytes.length);
                    return pdfBytes;

                } catch (Exception e) {
                    logger.error("M_IS: PDF generation failed", e);
                    return new byte[0];
                }

		case "BRF_67":
			try {
				excelBytes = BRF_67_reportservice.BRF_67_Excel(filename, reportId, fromdate, todate, currency, dtltype);
			} catch (Exception e) {
				logger.error("getPdfDownloadFile: BRF_67 Excel generation failed", e);
			}
			break;

		case "BRF_70":
			try {
				excelBytes = BRF_70_reportservice.BRF_70Excel(filename, reportId, fromdate, todate, currency, dtltype);
			} catch (Exception e) {
				logger.error("getPdfDownloadFile: BRF_70 Excel generation failed", e);
			}
			break;

		case "BRF65":
			try {
				excelBytes = BRF65_ReportService.BRF65Excel(filename, reportId, fromdate, todate, currency, dtltype);
			} catch (Exception e) {
				logger.error("getPdfDownloadFile: BRF65 Excel generation failed", e);
			}
			break;

		case "M_SFINP2":
			try {
				excelBytes = BRRS_M_SFINP2_reportservice.BRRS_M_SFINP2Excel(filename, reportId, fromdate, todate,
						currency, dtltype);
			} catch (Exception e) {
				logger.error("getPdfDownloadFile: M_SFINP2 Excel generation failed", e);
			}
			break;
		}

		// Guard: if Excel generation produced nothing, skip conversion immediately.
		// The controller will receive null, log its own warning, and send HTTP 204.
		if (excelBytes == null || excelBytes.length == 0) {
			logger.warn("getPdfDownloadFile: Excel bytes null/empty for reportId={} — skipping PDF conversion", reportId);
			return null;
		}

		logger.info("getPdfDownloadFile: {} Excel bytes ready, starting PDF conversion for reportId={}",
				excelBytes.length, reportId);

		byte[] pdffile = null;
		try {
			pdffile = exceltopdfservice.convertExcelBytesToPdf(excelBytes);
			if (pdffile != null && pdffile.length > 0) {
				logger.info("getPdfDownloadFile: PDF conversion succeeded — {} bytes for reportId={}", pdffile.length, reportId);
			} else {
				logger.warn("getPdfDownloadFile: PDF conversion returned null/empty for reportId={}", reportId);
			}
		} catch (Exception e) {
			// FIX: was e.printStackTrace() — this was the EXACT reason the error was
			// invisible in your log. The exception from convertExcelBytesToPdf was being
			// swallowed silently to stderr while the SLF4J log showed nothing.
			// After this fix, the next run will show the real exception class and message
			// here, which will tell us exactly what to fix in Exceltopdfservice.
			logger.error("getPdfDownloadFile: convertExcelBytesToPdf threw for reportId={} — this is the root cause of the 'No data available' popup",
					reportId, e);
		}

		return pdfBytes;
	}
	
	@Async
	public void generateReportAsyncpdf(String jobId, String filename, String fromdate, String todate, String dtltype,
			String type, String currency, String version) {

		byte[] fileData = null;

		if ("BRF_67_Detail".equals(filename)) {
			try {
			fileData = BRF_67_reportservice.BRF_67_DetailExcel(filename, fromdate, todate);
			fileData = BRF_67_reportservice.convertExcelBytesToPdf(fileData);
			} catch (Exception e) {
				e.printStackTrace();
			}
						
		}		
		if (fileData == null) {
			logger.warn("Pdf generation failed or no data for jobId: {}", jobId);
			jobStorage.put(jobId, "Nodata".getBytes());
		} else {
			jobStorage.put(jobId, fileData);
		}

		System.out.println("Report generation completed for: " + filename);
	}

}
