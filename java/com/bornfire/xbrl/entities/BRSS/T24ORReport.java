package com.bornfire.xbrl.entities.BRSS;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;
@Entity
@Table(name="T24_OR_BUSINESS_CONTINUITY_RECOVERY_TABLE")
public class T24ORReport {

	private String	A1_PRODUCT;
	private String	A1_VALUE;
	private String	A2_PRODUCT;
	private String	A2_VALUE;
	private String	A3_PRODUCT;
	private String	A3_VALUE;
	private String	A4_PRODUCT;
	private String	A5_PRODUCT;
	private String	A5_VALUE;
	private String	A6_PRODUCT;
	private String	A6_VALUE;
	private String	A7_PRODUCT;
	private String	A7_VALUE;
	private String	A8_PRODUCT;
	private String	A8_VALUE;
	private String	A9_PRODUCT;
	private String	A9_VALUE;
	private String	A10_PRODUCT;
	private String	A10_VALUE;
	private String	A11_PRODUCT;
	private String	A12_PRODUCT;
	private String	A12_VALUE;
	private String	A13_PRODUCT;
	private String	A13_VALUE;
	private String	A14_PRODUCT;
	private String	A14_VALUE;
	private String	A15_PRODUCT;
	private String	A15_VALUE;
	private String	A16_PRODUCT;
	private String	A16_VALUE;
	private String	A17_PRODUCT;
	private String	A17_VALUE;
	private String	A18_PRODUCT;
	private String	A18_VALUE;
	private String	A19_PRODUCT;
	private String	A19_VALUE;
	private String	REPORT_CODE;
	private String	REPORT_NAME;
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	@Id
	private Date	REPORT_DATE;
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private Date	REPORT_DUE_DATE;
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private Date	REP_SUBMIT_DATE;
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private Date	REP_PERIOD_FROM;
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private Date	REP_PERIOD_TO;
	private String	REP_FREQ;
	private String	NIL_REPORT_FLG;
	private String	ARCH_FLG;
	private String	ENTITY_FLG;
	private String	DEL_FLG;
	private String	MODIFY_FLG;
	private String	ENTRY_USER;
	private String	MODIFY_USER;
	private String	VERIFY_USER;
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private Date	ENTRY_TIME;
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private Date	MODIFY_TIME;
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private Date	VERIFY_TIME;
	private String	QTR_FLG;
	public String getA1_PRODUCT() {
		return A1_PRODUCT;
	}
	public void setA1_PRODUCT(String a1_PRODUCT) {
		A1_PRODUCT = a1_PRODUCT;
	}
	public String getA1_VALUE() {
		return A1_VALUE;
	}
	public void setA1_VALUE(String a1_VALUE) {
		A1_VALUE = a1_VALUE;
	}
	public String getA2_PRODUCT() {
		return A2_PRODUCT;
	}
	public void setA2_PRODUCT(String a2_PRODUCT) {
		A2_PRODUCT = a2_PRODUCT;
	}
	public String getA2_VALUE() {
		return A2_VALUE;
	}
	public void setA2_VALUE(String a2_VALUE) {
		A2_VALUE = a2_VALUE;
	}
	public String getA3_PRODUCT() {
		return A3_PRODUCT;
	}
	public void setA3_PRODUCT(String a3_PRODUCT) {
		A3_PRODUCT = a3_PRODUCT;
	}
	public String getA3_VALUE() {
		return A3_VALUE;
	}
	public void setA3_VALUE(String a3_VALUE) {
		A3_VALUE = a3_VALUE;
	}
	public String getA4_PRODUCT() {
		return A4_PRODUCT;
	}
	public void setA4_PRODUCT(String a4_PRODUCT) {
		A4_PRODUCT = a4_PRODUCT;
	}
	public String getA5_PRODUCT() {
		return A5_PRODUCT;
	}
	public void setA5_PRODUCT(String a5_PRODUCT) {
		A5_PRODUCT = a5_PRODUCT;
	}
	public String getA5_VALUE() {
		return A5_VALUE;
	}
	public void setA5_VALUE(String a5_VALUE) {
		A5_VALUE = a5_VALUE;
	}
	public String getA6_PRODUCT() {
		return A6_PRODUCT;
	}
	public void setA6_PRODUCT(String a6_PRODUCT) {
		A6_PRODUCT = a6_PRODUCT;
	}
	public String getA6_VALUE() {
		return A6_VALUE;
	}
	public void setA6_VALUE(String a6_VALUE) {
		A6_VALUE = a6_VALUE;
	}
	public String getA7_PRODUCT() {
		return A7_PRODUCT;
	}
	public void setA7_PRODUCT(String a7_PRODUCT) {
		A7_PRODUCT = a7_PRODUCT;
	}
	public String getA7_VALUE() {
		return A7_VALUE;
	}
	public void setA7_VALUE(String a7_VALUE) {
		A7_VALUE = a7_VALUE;
	}
	public String getA8_PRODUCT() {
		return A8_PRODUCT;
	}
	public void setA8_PRODUCT(String a8_PRODUCT) {
		A8_PRODUCT = a8_PRODUCT;
	}
	public String getA8_VALUE() {
		return A8_VALUE;
	}
	public void setA8_VALUE(String a8_VALUE) {
		A8_VALUE = a8_VALUE;
	}
	public String getA9_PRODUCT() {
		return A9_PRODUCT;
	}
	public void setA9_PRODUCT(String a9_PRODUCT) {
		A9_PRODUCT = a9_PRODUCT;
	}
	public String getA9_VALUE() {
		return A9_VALUE;
	}
	public void setA9_VALUE(String a9_VALUE) {
		A9_VALUE = a9_VALUE;
	}
	public String getA10_PRODUCT() {
		return A10_PRODUCT;
	}
	public void setA10_PRODUCT(String a10_PRODUCT) {
		A10_PRODUCT = a10_PRODUCT;
	}
	public String getA10_VALUE() {
		return A10_VALUE;
	}
	public void setA10_VALUE(String a10_VALUE) {
		A10_VALUE = a10_VALUE;
	}
	public String getA11_PRODUCT() {
		return A11_PRODUCT;
	}
	public void setA11_PRODUCT(String a11_PRODUCT) {
		A11_PRODUCT = a11_PRODUCT;
	}
	public String getA12_PRODUCT() {
		return A12_PRODUCT;
	}
	public void setA12_PRODUCT(String a12_PRODUCT) {
		A12_PRODUCT = a12_PRODUCT;
	}
	public String getA12_VALUE() {
		return A12_VALUE;
	}
	public void setA12_VALUE(String a12_VALUE) {
		A12_VALUE = a12_VALUE;
	}
	public String getA13_PRODUCT() {
		return A13_PRODUCT;
	}
	public void setA13_PRODUCT(String a13_PRODUCT) {
		A13_PRODUCT = a13_PRODUCT;
	}
	public String getA13_VALUE() {
		return A13_VALUE;
	}
	public void setA13_VALUE(String a13_VALUE) {
		A13_VALUE = a13_VALUE;
	}
	public String getA14_PRODUCT() {
		return A14_PRODUCT;
	}
	public void setA14_PRODUCT(String a14_PRODUCT) {
		A14_PRODUCT = a14_PRODUCT;
	}
	public String getA14_VALUE() {
		return A14_VALUE;
	}
	public void setA14_VALUE(String a14_VALUE) {
		A14_VALUE = a14_VALUE;
	}
	public String getA15_PRODUCT() {
		return A15_PRODUCT;
	}
	public void setA15_PRODUCT(String a15_PRODUCT) {
		A15_PRODUCT = a15_PRODUCT;
	}
	public String getA15_VALUE() {
		return A15_VALUE;
	}
	public void setA15_VALUE(String a15_VALUE) {
		A15_VALUE = a15_VALUE;
	}
	public String getA16_PRODUCT() {
		return A16_PRODUCT;
	}
	public void setA16_PRODUCT(String a16_PRODUCT) {
		A16_PRODUCT = a16_PRODUCT;
	}
	public String getA16_VALUE() {
		return A16_VALUE;
	}
	public void setA16_VALUE(String a16_VALUE) {
		A16_VALUE = a16_VALUE;
	}
	public String getA17_PRODUCT() {
		return A17_PRODUCT;
	}
	public void setA17_PRODUCT(String a17_PRODUCT) {
		A17_PRODUCT = a17_PRODUCT;
	}
	public String getA17_VALUE() {
		return A17_VALUE;
	}
	public void setA17_VALUE(String a17_VALUE) {
		A17_VALUE = a17_VALUE;
	}
	public String getA18_PRODUCT() {
		return A18_PRODUCT;
	}
	public void setA18_PRODUCT(String a18_PRODUCT) {
		A18_PRODUCT = a18_PRODUCT;
	}
	public String getA18_VALUE() {
		return A18_VALUE;
	}
	public void setA18_VALUE(String a18_VALUE) {
		A18_VALUE = a18_VALUE;
	}
	public String getA19_PRODUCT() {
		return A19_PRODUCT;
	}
	public void setA19_PRODUCT(String a19_PRODUCT) {
		A19_PRODUCT = a19_PRODUCT;
	}
	public String getA19_VALUE() {
		return A19_VALUE;
	}
	public void setA19_VALUE(String a19_VALUE) {
		A19_VALUE = a19_VALUE;
	}
	public String getREPORT_CODE() {
		return REPORT_CODE;
	}
	public void setREPORT_CODE(String rEPORT_CODE) {
		REPORT_CODE = rEPORT_CODE;
	}
	public String getREPORT_NAME() {
		return REPORT_NAME;
	}
	public void setREPORT_NAME(String rEPORT_NAME) {
		REPORT_NAME = rEPORT_NAME;
	}
	public Date getREPORT_DATE() {
		return REPORT_DATE;
	}
	public void setREPORT_DATE(Date rEPORT_DATE) {
		REPORT_DATE = rEPORT_DATE;
	}
	public Date getREPORT_DUE_DATE() {
		return REPORT_DUE_DATE;
	}
	public void setREPORT_DUE_DATE(Date rEPORT_DUE_DATE) {
		REPORT_DUE_DATE = rEPORT_DUE_DATE;
	}
	public Date getREP_SUBMIT_DATE() {
		return REP_SUBMIT_DATE;
	}
	public void setREP_SUBMIT_DATE(Date rEP_SUBMIT_DATE) {
		REP_SUBMIT_DATE = rEP_SUBMIT_DATE;
	}
	public Date getREP_PERIOD_FROM() {
		return REP_PERIOD_FROM;
	}
	public void setREP_PERIOD_FROM(Date rEP_PERIOD_FROM) {
		REP_PERIOD_FROM = rEP_PERIOD_FROM;
	}
	public Date getREP_PERIOD_TO() {
		return REP_PERIOD_TO;
	}
	public void setREP_PERIOD_TO(Date rEP_PERIOD_TO) {
		REP_PERIOD_TO = rEP_PERIOD_TO;
	}
	public String getREP_FREQ() {
		return REP_FREQ;
	}
	public void setREP_FREQ(String rEP_FREQ) {
		REP_FREQ = rEP_FREQ;
	}
	public String getNIL_REPORT_FLG() {
		return NIL_REPORT_FLG;
	}
	public void setNIL_REPORT_FLG(String nIL_REPORT_FLG) {
		NIL_REPORT_FLG = nIL_REPORT_FLG;
	}
	public String getARCH_FLG() {
		return ARCH_FLG;
	}
	public void setARCH_FLG(String aRCH_FLG) {
		ARCH_FLG = aRCH_FLG;
	}
	public String getENTITY_FLG() {
		return ENTITY_FLG;
	}
	public void setENTITY_FLG(String eNTITY_FLG) {
		ENTITY_FLG = eNTITY_FLG;
	}
	public String getDEL_FLG() {
		return DEL_FLG;
	}
	public void setDEL_FLG(String dEL_FLG) {
		DEL_FLG = dEL_FLG;
	}
	public String getMODIFY_FLG() {
		return MODIFY_FLG;
	}
	public void setMODIFY_FLG(String mODIFY_FLG) {
		MODIFY_FLG = mODIFY_FLG;
	}
	public String getENTRY_USER() {
		return ENTRY_USER;
	}
	public void setENTRY_USER(String eNTRY_USER) {
		ENTRY_USER = eNTRY_USER;
	}
	public String getMODIFY_USER() {
		return MODIFY_USER;
	}
	public void setMODIFY_USER(String mODIFY_USER) {
		MODIFY_USER = mODIFY_USER;
	}
	public String getVERIFY_USER() {
		return VERIFY_USER;
	}
	public void setVERIFY_USER(String vERIFY_USER) {
		VERIFY_USER = vERIFY_USER;
	}
	public Date getENTRY_TIME() {
		return ENTRY_TIME;
	}
	public void setENTRY_TIME(Date eNTRY_TIME) {
		ENTRY_TIME = eNTRY_TIME;
	}
	public Date getMODIFY_TIME() {
		return MODIFY_TIME;
	}
	public void setMODIFY_TIME(Date mODIFY_TIME) {
		MODIFY_TIME = mODIFY_TIME;
	}
	public Date getVERIFY_TIME() {
		return VERIFY_TIME;
	}
	public void setVERIFY_TIME(Date vERIFY_TIME) {
		VERIFY_TIME = vERIFY_TIME;
	}
	public String getQTR_FLG() {
		return QTR_FLG;
	}
	public void setQTR_FLG(String qTR_FLG) {
		QTR_FLG = qTR_FLG;
	}
	public T24ORReport(String a1_PRODUCT, String a1_VALUE, String a2_PRODUCT, String a2_VALUE, String a3_PRODUCT,
			String a3_VALUE, String a4_PRODUCT, String a5_PRODUCT, String a5_VALUE, String a6_PRODUCT, String a6_VALUE,
			String a7_PRODUCT, String a7_VALUE, String a8_PRODUCT, String a8_VALUE, String a9_PRODUCT, String a9_VALUE,
			String a10_PRODUCT, String a10_VALUE, String a11_PRODUCT, String a12_PRODUCT, String a12_VALUE,
			String a13_PRODUCT, String a13_VALUE, String a14_PRODUCT, String a14_VALUE, String a15_PRODUCT,
			String a15_VALUE, String a16_PRODUCT, String a16_VALUE, String a17_PRODUCT, String a17_VALUE,
			String a18_PRODUCT, String a18_VALUE, String a19_PRODUCT, String a19_VALUE, String rEPORT_CODE,
			String rEPORT_NAME, Date rEPORT_DATE, Date rEPORT_DUE_DATE, Date rEP_SUBMIT_DATE, Date rEP_PERIOD_FROM,
			Date rEP_PERIOD_TO, String rEP_FREQ, String nIL_REPORT_FLG, String aRCH_FLG, String eNTITY_FLG,
			String dEL_FLG, String mODIFY_FLG, String eNTRY_USER, String mODIFY_USER, String vERIFY_USER,
			Date eNTRY_TIME, Date mODIFY_TIME, Date vERIFY_TIME, String qTR_FLG) {
		super();
		A1_PRODUCT = a1_PRODUCT;
		A1_VALUE = a1_VALUE;
		A2_PRODUCT = a2_PRODUCT;
		A2_VALUE = a2_VALUE;
		A3_PRODUCT = a3_PRODUCT;
		A3_VALUE = a3_VALUE;
		A4_PRODUCT = a4_PRODUCT;
		A5_PRODUCT = a5_PRODUCT;
		A5_VALUE = a5_VALUE;
		A6_PRODUCT = a6_PRODUCT;
		A6_VALUE = a6_VALUE;
		A7_PRODUCT = a7_PRODUCT;
		A7_VALUE = a7_VALUE;
		A8_PRODUCT = a8_PRODUCT;
		A8_VALUE = a8_VALUE;
		A9_PRODUCT = a9_PRODUCT;
		A9_VALUE = a9_VALUE;
		A10_PRODUCT = a10_PRODUCT;
		A10_VALUE = a10_VALUE;
		A11_PRODUCT = a11_PRODUCT;
		A12_PRODUCT = a12_PRODUCT;
		A12_VALUE = a12_VALUE;
		A13_PRODUCT = a13_PRODUCT;
		A13_VALUE = a13_VALUE;
		A14_PRODUCT = a14_PRODUCT;
		A14_VALUE = a14_VALUE;
		A15_PRODUCT = a15_PRODUCT;
		A15_VALUE = a15_VALUE;
		A16_PRODUCT = a16_PRODUCT;
		A16_VALUE = a16_VALUE;
		A17_PRODUCT = a17_PRODUCT;
		A17_VALUE = a17_VALUE;
		A18_PRODUCT = a18_PRODUCT;
		A18_VALUE = a18_VALUE;
		A19_PRODUCT = a19_PRODUCT;
		A19_VALUE = a19_VALUE;
		REPORT_CODE = rEPORT_CODE;
		REPORT_NAME = rEPORT_NAME;
		REPORT_DATE = rEPORT_DATE;
		REPORT_DUE_DATE = rEPORT_DUE_DATE;
		REP_SUBMIT_DATE = rEP_SUBMIT_DATE;
		REP_PERIOD_FROM = rEP_PERIOD_FROM;
		REP_PERIOD_TO = rEP_PERIOD_TO;
		REP_FREQ = rEP_FREQ;
		NIL_REPORT_FLG = nIL_REPORT_FLG;
		ARCH_FLG = aRCH_FLG;
		ENTITY_FLG = eNTITY_FLG;
		DEL_FLG = dEL_FLG;
		MODIFY_FLG = mODIFY_FLG;
		ENTRY_USER = eNTRY_USER;
		MODIFY_USER = mODIFY_USER;
		VERIFY_USER = vERIFY_USER;
		ENTRY_TIME = eNTRY_TIME;
		MODIFY_TIME = mODIFY_TIME;
		VERIFY_TIME = vERIFY_TIME;
		QTR_FLG = qTR_FLG;
	}
	public T24ORReport() {
		super();
		// TODO Auto-generated constructor stub
	}

}
