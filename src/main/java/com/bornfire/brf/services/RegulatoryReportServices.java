package com.bornfire.brf.services;

import java.math.BigDecimal;
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
				// TODO Auto-generated catch block
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
		// System.out.println("Starting report generation for: " + filename);

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
