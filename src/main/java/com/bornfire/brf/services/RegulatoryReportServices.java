package com.bornfire.brf.services;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import com.bornfire.brf.services.CBUAE_BRF1_1_ReportService;

import net.sf.jasperreports.engine.JRException;

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

	private static final Logger logger = LoggerFactory.getLogger(RegulatoryReportServices.class);

	public ModelAndView getReportView(String reportId, String reportDate, String fromdate, String todate,
			String currency, String dtltype, String subreportid, String secid, String reportingTime, Pageable pageable,
			BigDecimal srl_no, String req) {

		ModelAndView repsummary = new ModelAndView();

		logger.info("Getting View for the Report :" + reportId);
		switch (reportId) {

		case "BRF001":
			repsummary = cbuae_brf1_1_reportservice.getBRF1_1View(reportId, fromdate, todate, currency, dtltype,
					pageable);
			break;

		case "BRF1_3":
			repsummary = cbuae_brf1_3_reportservice.getBRF1_3View(reportId, fromdate, todate, currency, dtltype,
					pageable);
			break;

		case "BRF2_1":
			repsummary = cbuae_brf2_1_reportservice.getBRF2_1View(reportId, fromdate, todate, currency, dtltype,
					pageable);
			break;

		case "BRF2_2":
			repsummary = cbuae_brf2_2_reportservice.getBRF2_2View(reportId, fromdate, todate, currency, dtltype,
					pageable);
			break;

		case "BRF2_4":
			repsummary = cbuae_brf2_4_reportservice.getBRF2_4View(reportId, fromdate, todate, currency, dtltype,
					pageable);
			break;

		case "BRF2_6":
			repsummary = cbuae_brf2_6_reportservice.getBRF2_6View(reportId, fromdate, todate, currency, dtltype,
					pageable);
			break;

		case "BRF2_7":
			repsummary = cbuae_brf2_7_reportservice.getBRF2_7View(reportId, fromdate, todate, currency, dtltype,
					pageable);
			break;

		case "M_SFINP2":
			repsummary = BRRS_M_SFINP2_reportservice.getM_SFINP2View(reportId, fromdate, todate, currency, dtltype,
					pageable);
			break;

		case "BRF_67":
			repsummary = BRF_67_reportservice.getBRF_67View(reportId, fromdate, todate, currency, dtltype, pageable);
			break;

		
		case "BRF_70":
			repsummary = BRF_70_reportservice.getBRF_70View(reportId, fromdate, todate, currency, dtltype, pageable);
			break;
			
		case "BRF65":
			repsummary = BRF65_ReportService.getBRF65View(reportId, fromdate, todate, currency, dtltype, pageable);
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

		case "BRF001":
			repdetail = cbuae_brf1_1_reportservice.getBRF1_1currentDtl(reportId, fromdate, todate, currency, dtltype,
					pageable, Filter);
			break;

		case "BRF1_3":
			repdetail = cbuae_brf1_3_reportservice.getBRF1_3currentDtl(reportId, fromdate, todate, currency, dtltype,
					pageable, Filter);
			break;

		case "BRF2_1":
			repdetail = cbuae_brf2_1_reportservice.getBRF2_1currentDtl(reportId, fromdate, todate, currency, dtltype,
					pageable, Filter);
			break;

		case "BRF2_2":
			repdetail = cbuae_brf2_2_reportservice.getBRF2_2currentDtl(reportId, fromdate, todate, currency, dtltype,
					pageable, Filter);
			break;

		case "BRF2_4":
			repdetail = cbuae_brf2_4_reportservice.getBRF2_4currentDtl(reportId, fromdate, todate, currency, dtltype,
					pageable, Filter);
			break;

		case "BRF2_6":
			repdetail = cbuae_brf2_6_reportservice.getBRF2_6currentDtl(reportId, fromdate, todate, currency, dtltype,
					pageable, Filter);
			break;

		case "BRF2_7":
			repdetail = cbuae_brf2_7_reportservice.getBRF2_7currentDtl(reportId, fromdate, todate, currency, dtltype,
					pageable, Filter);
			break;

		case "M_SFINP2":
			repdetail = BRRS_M_SFINP2_reportservice.getM_SFINP2currentDtl(reportId, fromdate, todate, currency, dtltype,
					pageable, Filter);
			break;

		case "BRF_67":
			repdetail = BRF_67_reportservice.getBRF_67currentDtl(reportId, fromdate, todate, currency, dtltype,
					pageable, Filter);
			break;
		/*
		 * case "BRF_70": repdetail = BRF_70_reportservice.getBRF_70currentDtl(reportId,
		 * fromdate, todate, currency, dtltype, pageable, Filter); break;
		 */

		}

		return repdetail;
	}

	public byte[] getDownloadFile(String reportId, String filename, String asondate, String fromdate, String todate,
			String currency, String subreportid, String secid, String dtltype, String reportingTime,
			String instancecode, String filter) {

		byte[] repfile = null;

		switch (reportId) {

		case "BRF1_3":
			try {
				repfile = cbuae_brf1_3_reportservice.getBRF2_1Excel(filename, reportId, fromdate, todate, currency,
						dtltype);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case "BRF2_1":
			try {
				repfile = cbuae_brf2_1_reportservice.getBRF2_1Excel(filename, reportId, fromdate, todate, currency,
						dtltype);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case "BRF2_2":
			try {
				repfile = cbuae_brf2_2_reportservice.getBRF2_2Excel(filename, reportId, fromdate, todate, currency,
						dtltype);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case "BRF2_4":
			try {
				repfile = cbuae_brf2_4_reportservice.getBRF2_4Excel(filename, reportId, fromdate, todate, currency,
						dtltype);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case "BRF2_7":
			try {
				repfile = cbuae_brf2_7_reportservice.getBRF2_7Excel(filename, reportId, fromdate, todate, currency,
						dtltype);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case "M_SFINP2":
			try {
				repfile = BRRS_M_SFINP2_reportservice.BRRS_M_SFINP2Excel(filename, reportId, fromdate, todate, currency,
						dtltype);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case "BRF_67":
			try {
				repfile = BRF_67_reportservice.BRF_67_Excel(filename, reportId, fromdate, todate, currency, dtltype);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case "BRF_70":
			try {
				repfile = BRF_70_reportservice.BRF_70Excel(filename, reportId, fromdate, todate, currency, dtltype);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		case "BRF65":
			try {
				repfile = BRF65_ReportService.BRF65Excel(filename, reportId, fromdate, todate, currency, dtltype);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return repfile;
	}

//	public byte[] getDownloadDetailFile(String filename, String fromdate, String todate) {
//	    System.out.println("Came to common service");
//	    
//	    if ("BRF2_4Detail".equals(filename)) {
//	        return cbuae_brf2_4_reportservice.getBRF2_4DetailExcel(filename, fromdate, todate);
//	    } else {
//	        System.out.println("Default");
//	        
//	    }
//	    
//	    return new byte[0];
//	}
	public byte[] getDownloadDetailFile(String filename, String fromdate, String todate) {
		System.out.println("Came to common service1");

		if ("MSFinP2Detail".equals(filename)) {
			return BRRS_M_SFINP2_reportservice.BRRS_M_SFINP2DetailExcel(filename, fromdate, todate);
		}

		if ("BRF_67_Detail".equals(filename)) {
			return BRF_67_reportservice.BRF_67_DetailExcel(filename, fromdate, todate);
		}

		/*
		 * else if("M_PIDetail".equals(filename)) { return
		 * BRRS_M_PI_reportservice.getBRRS_M_PIDetailExcel(filename, fromdate, todate);
		 * 
		 * }
		 */

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
		// System.out.println("Starting report generation for: " + filename);

		byte[] fileData = null;

		if ("MSFinP2Detail".equals(filename)) {
			fileData = BRRS_M_SFINP2_reportservice.BRRS_M_SFINP2DetailExcel(filename, fromdate, todate);
		}

		if ("BRF_67_Detail".equals(filename)) {
			fileData = BRF_67_reportservice.BRF_67_DetailExcel(filename, fromdate, todate);
		}

		/*
		 * else if("M_ISDetail".equals(filename)) { fileData =
		 * BRRS_M_IS_reportservice.BRRS_M_ISDetailExcel(filename, fromdate, todate);
		 * 
		 * }
		 */

		if (fileData == null) {
			logger.warn("Excel generation failed or no data for jobId: {}", jobId);
			jobStorage.put(jobId, "Nodata".getBytes());
		} else {
			jobStorage.put(jobId, fileData);
		}

		System.out.println("Report generation completed for: " + filename);
	}

	public byte[] getReport(String jobId) {
		// System.out.println("Report generation completed for: " + jobId);
		return jobStorage.get(jobId);
	}

	public byte[] getPdfDownloadFile(String reportId, String filename, String asondate, String fromdate, String todate,
			String currency, String subreportid, String secid, String dtltype, String reportingTime,
			String instancecode, String filter) {

		
		byte[] excelBytes =null;

		switch (reportId) {

		case "BRF_67":
			try {

				 excelBytes = BRF_67_reportservice.BRF_67_Excel(filename, reportId, fromdate, todate, currency,dtltype);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case "BRF_70":
			try {
					excelBytes = BRF_70_reportservice.BRF_70Excel(filename, reportId, fromdate, todate, currency,dtltype);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		case "BRF65":
			try {
				excelBytes = BRF65_ReportService.BRF65Excel(filename, reportId, fromdate, todate, currency, dtltype);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case "M_SFINP2":
			try {
				excelBytes = BRRS_M_SFINP2_reportservice.BRRS_M_SFINP2Excel(filename, reportId, fromdate, todate, currency,
						dtltype);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		
		byte[] pdffile = null;
		try {
			pdffile = exceltopdfservice.convertExcelBytesToPdf(excelBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return pdffile;
	}
	
	@Async
	public void generateReportAsyncpdf(String jobId, String filename, String fromdate, String todate, String dtltype,
			String type, String currency, String version) {
		// System.out.println("Starting report generation for: " + filename);

		byte[] fileData = null;

		if ("BRF_67_Detail".equals(filename)) {
			try {
			fileData = BRF_67_reportservice.BRF_67_DetailExcel(filename, fromdate, todate);
			fileData=BRF_67_reportservice.convertExcelBytesToPdf(fileData);
			} catch (Exception e) {
				// TODO Auto-generated catch block
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
