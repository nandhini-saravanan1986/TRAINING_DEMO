package com.bornfire.brf.entities;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "JEF_BRF_67_SUMMARY_TABLE")

public class BRF_67_Summary_Entity {

	public String R0010_CATEGORY;
	public String R0020_CATEGORY;
	public BigDecimal R0020_NO_ACC_UAE_NAT;
	public BigDecimal R0020_BAL_UAE_NAT;
	public BigDecimal R0020_NO_ACC_OTH_NAT;
	public BigDecimal R0020_BAL_OTH_NAT;
	public BigDecimal R0020_NO_ACC_TOT_DEPO;
	public BigDecimal R0020_BAL_TOT_DEPO;
	public String R0030_CATEGORY;
	public BigDecimal R0030_NO_ACC_UAE_NAT;
	public BigDecimal R0030_BAL_UAE_NAT;
	public BigDecimal R0030_NO_ACC_OTH_NAT;
	public BigDecimal R0030_BAL_OTH_NAT;
	public BigDecimal R0030_NO_ACC_TOT_DEPO;
	public BigDecimal R0030_BAL_TOT_DEPO;
	public String R0040_CATEGORY;
	public BigDecimal R0040_NO_ACC_UAE_NAT;
	public BigDecimal R0040_BAL_UAE_NAT;
	public BigDecimal R0040_NO_ACC_OTH_NAT;
	public BigDecimal R0040_BAL_OTH_NAT;
	public BigDecimal R0040_NO_ACC_TOT_DEPO;
	public BigDecimal R0040_BAL_TOT_DEPO;
	public String R0050_CATEGORY;
	public BigDecimal R0050_NO_ACC_UAE_NAT;
	public BigDecimal R0050_BAL_UAE_NAT;
	public BigDecimal R0050_NO_ACC_OTH_NAT;
	public BigDecimal R0050_BAL_OTH_NAT;
	public BigDecimal R0050_NO_ACC_TOT_DEPO;
	public BigDecimal R0050_BAL_TOT_DEPO;
	public String R0060_CATEGORY;
	public BigDecimal R0060_NO_ACC_UAE_NAT;
	public BigDecimal R0060_BAL_UAE_NAT;
	public BigDecimal R0060_NO_ACC_OTH_NAT;
	public BigDecimal R0060_BAL_OTH_NAT;
	public BigDecimal R0060_NO_ACC_TOT_DEPO;
	public BigDecimal R0060_BAL_TOT_DEPO;
	public String R0070_CATEGORY;
	public BigDecimal R0070_NO_ACC_UAE_NAT;
	public BigDecimal R0070_BAL_UAE_NAT;
	public BigDecimal R0070_NO_ACC_OTH_NAT;
	public BigDecimal R0070_BAL_OTH_NAT;
	public BigDecimal R0070_NO_ACC_TOT_DEPO;
	public BigDecimal R0070_BAL_TOT_DEPO;
	public String R0080_CATEGORY;
	public BigDecimal R0080_NO_ACC_UAE_NAT;
	public BigDecimal R0080_BAL_UAE_NAT;
	public BigDecimal R0080_NO_ACC_OTH_NAT;
	public BigDecimal R0080_BAL_OTH_NAT;
	public BigDecimal R0080_NO_ACC_TOT_DEPO;
	public BigDecimal R0080_BAL_TOT_DEPO;
	public String R0090_CATEGORY;
	public String R0100_CATEGORY;
	public BigDecimal R0100_NO_ACC_TOT_DEPO;
	public BigDecimal R0100_BAL_TOT_DEPO;
	public String R0110_CATEGORY;
	public BigDecimal R0110_NO_ACC_TOT_DEPO;
	public BigDecimal R0110_BAL_TOT_DEPO;
	public String R0120_CATEGORY;
	public BigDecimal R0120_NO_ACC_TOT_DEPO;
	public BigDecimal R0120_BAL_TOT_DEPO;
	public String R0130_CATEGORY;
	public BigDecimal R0130_NO_ACC_TOT_DEPO;
	public BigDecimal R0130_BAL_TOT_DEPO;
	public String R0140_CATEGORY;
	public BigDecimal R0140_NO_ACC_TOT_DEPO;
	public BigDecimal R0140_BAL_TOT_DEPO;
	public String R0150_CATEGORY;
	public BigDecimal R0150_NO_ACC_TOT_DEPO;
	public BigDecimal R0150_BAL_TOT_DEPO;
	public String R0160_CATEGORY;
	public BigDecimal R0160_NO_ACC_TOT_DEPO;
	public BigDecimal R0160_BAL_TOT_DEPO;
	
	
    @Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	@Id
	public Date REPORT_DATE;
    public String REPORT_VERSION;
    public String REPORT_FREQUENCY;
    //public String REPORT_CODE;
    public String REPORT_DESC;
    public String ENTITY_FLG;
    public String MODIFY_FLG;
    public String DEL_FLG;
    
    
    
	public String getR0010_CATEGORY() {
		return R0010_CATEGORY;
	}
	public void setR0010_CATEGORY(String r0010_CATEGORY) {
		R0010_CATEGORY = r0010_CATEGORY;
	}
	public String getR0020_CATEGORY() {
		return R0020_CATEGORY;
	}
	public void setR0020_CATEGORY(String r0020_CATEGORY) {
		R0020_CATEGORY = r0020_CATEGORY;
	}
	public BigDecimal getR0020_NO_ACC_UAE_NAT() {
		return R0020_NO_ACC_UAE_NAT;
	}
	public void setR0020_NO_ACC_UAE_NAT(BigDecimal r0020_NO_ACC_UAE_NAT) {
		R0020_NO_ACC_UAE_NAT = r0020_NO_ACC_UAE_NAT;
	}
	public BigDecimal getR0020_BAL_UAE_NAT() {
		return R0020_BAL_UAE_NAT;
	}
	public void setR0020_BAL_UAE_NAT(BigDecimal r0020_BAL_UAE_NAT) {
		R0020_BAL_UAE_NAT = r0020_BAL_UAE_NAT;
	}
	public BigDecimal getR0020_NO_ACC_OTH_NAT() {
		return R0020_NO_ACC_OTH_NAT;
	}
	public void setR0020_NO_ACC_OTH_NAT(BigDecimal r0020_NO_ACC_OTH_NAT) {
		R0020_NO_ACC_OTH_NAT = r0020_NO_ACC_OTH_NAT;
	}
	public BigDecimal getR0020_BAL_OTH_NAT() {
		return R0020_BAL_OTH_NAT;
	}
	public void setR0020_BAL_OTH_NAT(BigDecimal r0020_BAL_OTH_NAT) {
		R0020_BAL_OTH_NAT = r0020_BAL_OTH_NAT;
	}
	public BigDecimal getR0020_NO_ACC_TOT_DEPO() {
		return R0020_NO_ACC_TOT_DEPO;
	}
	public void setR0020_NO_ACC_TOT_DEPO(BigDecimal r0020_NO_ACC_TOT_DEPO) {
		R0020_NO_ACC_TOT_DEPO = r0020_NO_ACC_TOT_DEPO;
	}
	public BigDecimal getR0020_BAL_TOT_DEPO() {
		return R0020_BAL_TOT_DEPO;
	}
	public void setR0020_BAL_TOT_DEPO(BigDecimal r0020_BAL_TOT_DEPO) {
		R0020_BAL_TOT_DEPO = r0020_BAL_TOT_DEPO;
	}
	public String getR0030_CATEGORY() {
		return R0030_CATEGORY;
	}
	public void setR0030_CATEGORY(String r0030_CATEGORY) {
		R0030_CATEGORY = r0030_CATEGORY;
	}
	public BigDecimal getR0030_NO_ACC_UAE_NAT() {
		return R0030_NO_ACC_UAE_NAT;
	}
	public void setR0030_NO_ACC_UAE_NAT(BigDecimal r0030_NO_ACC_UAE_NAT) {
		R0030_NO_ACC_UAE_NAT = r0030_NO_ACC_UAE_NAT;
	}
	public BigDecimal getR0030_BAL_UAE_NAT() {
		return R0030_BAL_UAE_NAT;
	}
	public void setR0030_BAL_UAE_NAT(BigDecimal r0030_BAL_UAE_NAT) {
		R0030_BAL_UAE_NAT = r0030_BAL_UAE_NAT;
	}
	public BigDecimal getR0030_NO_ACC_OTH_NAT() {
		return R0030_NO_ACC_OTH_NAT;
	}
	public void setR0030_NO_ACC_OTH_NAT(BigDecimal r0030_NO_ACC_OTH_NAT) {
		R0030_NO_ACC_OTH_NAT = r0030_NO_ACC_OTH_NAT;
	}
	public BigDecimal getR0030_BAL_OTH_NAT() {
		return R0030_BAL_OTH_NAT;
	}
	public void setR0030_BAL_OTH_NAT(BigDecimal r0030_BAL_OTH_NAT) {
		R0030_BAL_OTH_NAT = r0030_BAL_OTH_NAT;
	}
	public BigDecimal getR0030_NO_ACC_TOT_DEPO() {
		return R0030_NO_ACC_TOT_DEPO;
	}
	public void setR0030_NO_ACC_TOT_DEPO(BigDecimal r0030_NO_ACC_TOT_DEPO) {
		R0030_NO_ACC_TOT_DEPO = r0030_NO_ACC_TOT_DEPO;
	}
	public BigDecimal getR0030_BAL_TOT_DEPO() {
		return R0030_BAL_TOT_DEPO;
	}
	public void setR0030_BAL_TOT_DEPO(BigDecimal r0030_BAL_TOT_DEPO) {
		R0030_BAL_TOT_DEPO = r0030_BAL_TOT_DEPO;
	}
	public String getR0040_CATEGORY() {
		return R0040_CATEGORY;
	}
	public void setR0040_CATEGORY(String r0040_CATEGORY) {
		R0040_CATEGORY = r0040_CATEGORY;
	}
	public BigDecimal getR0040_NO_ACC_UAE_NAT() {
		return R0040_NO_ACC_UAE_NAT;
	}
	public void setR0040_NO_ACC_UAE_NAT(BigDecimal r0040_NO_ACC_UAE_NAT) {
		R0040_NO_ACC_UAE_NAT = r0040_NO_ACC_UAE_NAT;
	}
	public BigDecimal getR0040_BAL_UAE_NAT() {
		return R0040_BAL_UAE_NAT;
	}
	public void setR0040_BAL_UAE_NAT(BigDecimal r0040_BAL_UAE_NAT) {
		R0040_BAL_UAE_NAT = r0040_BAL_UAE_NAT;
	}
	public BigDecimal getR0040_NO_ACC_OTH_NAT() {
		return R0040_NO_ACC_OTH_NAT;
	}
	public void setR0040_NO_ACC_OTH_NAT(BigDecimal r0040_NO_ACC_OTH_NAT) {
		R0040_NO_ACC_OTH_NAT = r0040_NO_ACC_OTH_NAT;
	}
	public BigDecimal getR0040_BAL_OTH_NAT() {
		return R0040_BAL_OTH_NAT;
	}
	public void setR0040_BAL_OTH_NAT(BigDecimal r0040_BAL_OTH_NAT) {
		R0040_BAL_OTH_NAT = r0040_BAL_OTH_NAT;
	}
	public BigDecimal getR0040_NO_ACC_TOT_DEPO() {
		return R0040_NO_ACC_TOT_DEPO;
	}
	public void setR0040_NO_ACC_TOT_DEPO(BigDecimal r0040_NO_ACC_TOT_DEPO) {
		R0040_NO_ACC_TOT_DEPO = r0040_NO_ACC_TOT_DEPO;
	}
	public BigDecimal getR0040_BAL_TOT_DEPO() {
		return R0040_BAL_TOT_DEPO;
	}
	public void setR0040_BAL_TOT_DEPO(BigDecimal r0040_BAL_TOT_DEPO) {
		R0040_BAL_TOT_DEPO = r0040_BAL_TOT_DEPO;
	}
	public String getR0050_CATEGORY() {
		return R0050_CATEGORY;
	}
	public void setR0050_CATEGORY(String r0050_CATEGORY) {
		R0050_CATEGORY = r0050_CATEGORY;
	}
	public BigDecimal getR0050_NO_ACC_UAE_NAT() {
		return R0050_NO_ACC_UAE_NAT;
	}
	public void setR0050_NO_ACC_UAE_NAT(BigDecimal r0050_NO_ACC_UAE_NAT) {
		R0050_NO_ACC_UAE_NAT = r0050_NO_ACC_UAE_NAT;
	}
	public BigDecimal getR0050_BAL_UAE_NAT() {
		return R0050_BAL_UAE_NAT;
	}
	public void setR0050_BAL_UAE_NAT(BigDecimal r0050_BAL_UAE_NAT) {
		R0050_BAL_UAE_NAT = r0050_BAL_UAE_NAT;
	}
	public BigDecimal getR0050_NO_ACC_OTH_NAT() {
		return R0050_NO_ACC_OTH_NAT;
	}
	public void setR0050_NO_ACC_OTH_NAT(BigDecimal r0050_NO_ACC_OTH_NAT) {
		R0050_NO_ACC_OTH_NAT = r0050_NO_ACC_OTH_NAT;
	}
	public BigDecimal getR0050_BAL_OTH_NAT() {
		return R0050_BAL_OTH_NAT;
	}
	public void setR0050_BAL_OTH_NAT(BigDecimal r0050_BAL_OTH_NAT) {
		R0050_BAL_OTH_NAT = r0050_BAL_OTH_NAT;
	}
	public BigDecimal getR0050_NO_ACC_TOT_DEPO() {
		return R0050_NO_ACC_TOT_DEPO;
	}
	public void setR0050_NO_ACC_TOT_DEPO(BigDecimal r0050_NO_ACC_TOT_DEPO) {
		R0050_NO_ACC_TOT_DEPO = r0050_NO_ACC_TOT_DEPO;
	}
	public BigDecimal getR0050_BAL_TOT_DEPO() {
		return R0050_BAL_TOT_DEPO;
	}
	public void setR0050_BAL_TOT_DEPO(BigDecimal r0050_BAL_TOT_DEPO) {
		R0050_BAL_TOT_DEPO = r0050_BAL_TOT_DEPO;
	}
	public String getR0060_CATEGORY() {
		return R0060_CATEGORY;
	}
	public void setR0060_CATEGORY(String r0060_CATEGORY) {
		R0060_CATEGORY = r0060_CATEGORY;
	}
	public BigDecimal getR0060_NO_ACC_UAE_NAT() {
		return R0060_NO_ACC_UAE_NAT;
	}
	public void setR0060_NO_ACC_UAE_NAT(BigDecimal r0060_NO_ACC_UAE_NAT) {
		R0060_NO_ACC_UAE_NAT = r0060_NO_ACC_UAE_NAT;
	}
	public BigDecimal getR0060_BAL_UAE_NAT() {
		return R0060_BAL_UAE_NAT;
	}
	public void setR0060_BAL_UAE_NAT(BigDecimal r0060_BAL_UAE_NAT) {
		R0060_BAL_UAE_NAT = r0060_BAL_UAE_NAT;
	}
	public BigDecimal getR0060_NO_ACC_OTH_NAT() {
		return R0060_NO_ACC_OTH_NAT;
	}
	public void setR0060_NO_ACC_OTH_NAT(BigDecimal r0060_NO_ACC_OTH_NAT) {
		R0060_NO_ACC_OTH_NAT = r0060_NO_ACC_OTH_NAT;
	}
	public BigDecimal getR0060_BAL_OTH_NAT() {
		return R0060_BAL_OTH_NAT;
	}
	public void setR0060_BAL_OTH_NAT(BigDecimal r0060_BAL_OTH_NAT) {
		R0060_BAL_OTH_NAT = r0060_BAL_OTH_NAT;
	}
	public BigDecimal getR0060_NO_ACC_TOT_DEPO() {
		return R0060_NO_ACC_TOT_DEPO;
	}
	public void setR0060_NO_ACC_TOT_DEPO(BigDecimal r0060_NO_ACC_TOT_DEPO) {
		R0060_NO_ACC_TOT_DEPO = r0060_NO_ACC_TOT_DEPO;
	}
	public BigDecimal getR0060_BAL_TOT_DEPO() {
		return R0060_BAL_TOT_DEPO;
	}
	public void setR0060_BAL_TOT_DEPO(BigDecimal r0060_BAL_TOT_DEPO) {
		R0060_BAL_TOT_DEPO = r0060_BAL_TOT_DEPO;
	}
	public String getR0070_CATEGORY() {
		return R0070_CATEGORY;
	}
	public void setR0070_CATEGORY(String r0070_CATEGORY) {
		R0070_CATEGORY = r0070_CATEGORY;
	}
	public BigDecimal getR0070_NO_ACC_UAE_NAT() {
		return R0070_NO_ACC_UAE_NAT;
	}
	public void setR0070_NO_ACC_UAE_NAT(BigDecimal r0070_NO_ACC_UAE_NAT) {
		R0070_NO_ACC_UAE_NAT = r0070_NO_ACC_UAE_NAT;
	}
	public BigDecimal getR0070_BAL_UAE_NAT() {
		return R0070_BAL_UAE_NAT;
	}
	public void setR0070_BAL_UAE_NAT(BigDecimal r0070_BAL_UAE_NAT) {
		R0070_BAL_UAE_NAT = r0070_BAL_UAE_NAT;
	}
	public BigDecimal getR0070_NO_ACC_OTH_NAT() {
		return R0070_NO_ACC_OTH_NAT;
	}
	public void setR0070_NO_ACC_OTH_NAT(BigDecimal r0070_NO_ACC_OTH_NAT) {
		R0070_NO_ACC_OTH_NAT = r0070_NO_ACC_OTH_NAT;
	}
	public BigDecimal getR0070_BAL_OTH_NAT() {
		return R0070_BAL_OTH_NAT;
	}
	public void setR0070_BAL_OTH_NAT(BigDecimal r0070_BAL_OTH_NAT) {
		R0070_BAL_OTH_NAT = r0070_BAL_OTH_NAT;
	}
	public BigDecimal getR0070_NO_ACC_TOT_DEPO() {
		return R0070_NO_ACC_TOT_DEPO;
	}
	public void setR0070_NO_ACC_TOT_DEPO(BigDecimal r0070_NO_ACC_TOT_DEPO) {
		R0070_NO_ACC_TOT_DEPO = r0070_NO_ACC_TOT_DEPO;
	}
	public BigDecimal getR0070_BAL_TOT_DEPO() {
		return R0070_BAL_TOT_DEPO;
	}
	public void setR0070_BAL_TOT_DEPO(BigDecimal r0070_BAL_TOT_DEPO) {
		R0070_BAL_TOT_DEPO = r0070_BAL_TOT_DEPO;
	}
	public String getR0080_CATEGORY() {
		return R0080_CATEGORY;
	}
	public void setR0080_CATEGORY(String r0080_CATEGORY) {
		R0080_CATEGORY = r0080_CATEGORY;
	}
	public BigDecimal getR0080_NO_ACC_UAE_NAT() {
		return R0080_NO_ACC_UAE_NAT;
	}
	public void setR0080_NO_ACC_UAE_NAT(BigDecimal r0080_NO_ACC_UAE_NAT) {
		R0080_NO_ACC_UAE_NAT = r0080_NO_ACC_UAE_NAT;
	}
	public BigDecimal getR0080_BAL_UAE_NAT() {
		return R0080_BAL_UAE_NAT;
	}
	public void setR0080_BAL_UAE_NAT(BigDecimal r0080_BAL_UAE_NAT) {
		R0080_BAL_UAE_NAT = r0080_BAL_UAE_NAT;
	}
	public BigDecimal getR0080_NO_ACC_OTH_NAT() {
		return R0080_NO_ACC_OTH_NAT;
	}
	public void setR0080_NO_ACC_OTH_NAT(BigDecimal r0080_NO_ACC_OTH_NAT) {
		R0080_NO_ACC_OTH_NAT = r0080_NO_ACC_OTH_NAT;
	}
	public BigDecimal getR0080_BAL_OTH_NAT() {
		return R0080_BAL_OTH_NAT;
	}
	public void setR0080_BAL_OTH_NAT(BigDecimal r0080_BAL_OTH_NAT) {
		R0080_BAL_OTH_NAT = r0080_BAL_OTH_NAT;
	}
	public BigDecimal getR0080_NO_ACC_TOT_DEPO() {
		return R0080_NO_ACC_TOT_DEPO;
	}
	public void setR0080_NO_ACC_TOT_DEPO(BigDecimal r0080_NO_ACC_TOT_DEPO) {
		R0080_NO_ACC_TOT_DEPO = r0080_NO_ACC_TOT_DEPO;
	}
	public BigDecimal getR0080_BAL_TOT_DEPO() {
		return R0080_BAL_TOT_DEPO;
	}
	public void setR0080_BAL_TOT_DEPO(BigDecimal r0080_BAL_TOT_DEPO) {
		R0080_BAL_TOT_DEPO = r0080_BAL_TOT_DEPO;
	}
	public String getR0090_CATEGORY() {
		return R0090_CATEGORY;
	}
	public void setR0090_CATEGORY(String r0090_CATEGORY) {
		R0090_CATEGORY = r0090_CATEGORY;
	}
	public String getR0100_CATEGORY() {
		return R0100_CATEGORY;
	}
	public void setR0100_CATEGORY(String r0100_CATEGORY) {
		R0100_CATEGORY = r0100_CATEGORY;
	}
	public BigDecimal getR0100_NO_ACC_TOT_DEPO() {
		return R0100_NO_ACC_TOT_DEPO;
	}
	public void setR0100_NO_ACC_TOT_DEPO(BigDecimal r0100_NO_ACC_TOT_DEPO) {
		R0100_NO_ACC_TOT_DEPO = r0100_NO_ACC_TOT_DEPO;
	}
	public BigDecimal getR0100_BAL_TOT_DEPO() {
		return R0100_BAL_TOT_DEPO;
	}
	public void setR0100_BAL_TOT_DEPO(BigDecimal r0100_BAL_TOT_DEPO) {
		R0100_BAL_TOT_DEPO = r0100_BAL_TOT_DEPO;
	}
	public String getR0110_CATEGORY() {
		return R0110_CATEGORY;
	}
	public void setR0110_CATEGORY(String r0110_CATEGORY) {
		R0110_CATEGORY = r0110_CATEGORY;
	}
	public BigDecimal getR0110_NO_ACC_TOT_DEPO() {
		return R0110_NO_ACC_TOT_DEPO;
	}
	public void setR0110_NO_ACC_TOT_DEPO(BigDecimal r0110_NO_ACC_TOT_DEPO) {
		R0110_NO_ACC_TOT_DEPO = r0110_NO_ACC_TOT_DEPO;
	}
	public BigDecimal getR0110_BAL_TOT_DEPO() {
		return R0110_BAL_TOT_DEPO;
	}
	public void setR0110_BAL_TOT_DEPO(BigDecimal r0110_BAL_TOT_DEPO) {
		R0110_BAL_TOT_DEPO = r0110_BAL_TOT_DEPO;
	}
	public String getR0120_CATEGORY() {
		return R0120_CATEGORY;
	}
	public void setR0120_CATEGORY(String r0120_CATEGORY) {
		R0120_CATEGORY = r0120_CATEGORY;
	}
	public BigDecimal getR0120_NO_ACC_TOT_DEPO() {
		return R0120_NO_ACC_TOT_DEPO;
	}
	public void setR0120_NO_ACC_TOT_DEPO(BigDecimal r0120_NO_ACC_TOT_DEPO) {
		R0120_NO_ACC_TOT_DEPO = r0120_NO_ACC_TOT_DEPO;
	}
	public BigDecimal getR0120_BAL_TOT_DEPO() {
		return R0120_BAL_TOT_DEPO;
	}
	public void setR0120_BAL_TOT_DEPO(BigDecimal r0120_BAL_TOT_DEPO) {
		R0120_BAL_TOT_DEPO = r0120_BAL_TOT_DEPO;
	}
	public String getR0130_CATEGORY() {
		return R0130_CATEGORY;
	}
	public void setR0130_CATEGORY(String r0130_CATEGORY) {
		R0130_CATEGORY = r0130_CATEGORY;
	}
	public BigDecimal getR0130_NO_ACC_TOT_DEPO() {
		return R0130_NO_ACC_TOT_DEPO;
	}
	public void setR0130_NO_ACC_TOT_DEPO(BigDecimal r0130_NO_ACC_TOT_DEPO) {
		R0130_NO_ACC_TOT_DEPO = r0130_NO_ACC_TOT_DEPO;
	}
	public BigDecimal getR0130_BAL_TOT_DEPO() {
		return R0130_BAL_TOT_DEPO;
	}
	public void setR0130_BAL_TOT_DEPO(BigDecimal r0130_BAL_TOT_DEPO) {
		R0130_BAL_TOT_DEPO = r0130_BAL_TOT_DEPO;
	}
	public String getR0140_CATEGORY() {
		return R0140_CATEGORY;
	}
	public void setR0140_CATEGORY(String r0140_CATEGORY) {
		R0140_CATEGORY = r0140_CATEGORY;
	}
	public BigDecimal getR0140_NO_ACC_TOT_DEPO() {
		return R0140_NO_ACC_TOT_DEPO;
	}
	public void setR0140_NO_ACC_TOT_DEPO(BigDecimal r0140_NO_ACC_TOT_DEPO) {
		R0140_NO_ACC_TOT_DEPO = r0140_NO_ACC_TOT_DEPO;
	}
	public BigDecimal getR0140_BAL_TOT_DEPO() {
		return R0140_BAL_TOT_DEPO;
	}
	public void setR0140_BAL_TOT_DEPO(BigDecimal r0140_BAL_TOT_DEPO) {
		R0140_BAL_TOT_DEPO = r0140_BAL_TOT_DEPO;
	}
	public String getR0150_CATEGORY() {
		return R0150_CATEGORY;
	}
	public void setR0150_CATEGORY(String r0150_CATEGORY) {
		R0150_CATEGORY = r0150_CATEGORY;
	}
	public BigDecimal getR0150_NO_ACC_TOT_DEPO() {
		return R0150_NO_ACC_TOT_DEPO;
	}
	public void setR0150_NO_ACC_TOT_DEPO(BigDecimal r0150_NO_ACC_TOT_DEPO) {
		R0150_NO_ACC_TOT_DEPO = r0150_NO_ACC_TOT_DEPO;
	}
	public BigDecimal getR0150_BAL_TOT_DEPO() {
		return R0150_BAL_TOT_DEPO;
	}
	public void setR0150_BAL_TOT_DEPO(BigDecimal r0150_BAL_TOT_DEPO) {
		R0150_BAL_TOT_DEPO = r0150_BAL_TOT_DEPO;
	}
	public String getR0160_CATEGORY() {
		return R0160_CATEGORY;
	}
	public void setR0160_CATEGORY(String r0160_CATEGORY) {
		R0160_CATEGORY = r0160_CATEGORY;
	}
	public BigDecimal getR0160_NO_ACC_TOT_DEPO() {
		return R0160_NO_ACC_TOT_DEPO;
	}
	public void setR0160_NO_ACC_TOT_DEPO(BigDecimal r0160_NO_ACC_TOT_DEPO) {
		R0160_NO_ACC_TOT_DEPO = r0160_NO_ACC_TOT_DEPO;
	}
	public BigDecimal getR0160_BAL_TOT_DEPO() {
		return R0160_BAL_TOT_DEPO;
	}
	public void setR0160_BAL_TOT_DEPO(BigDecimal r0160_BAL_TOT_DEPO) {
		R0160_BAL_TOT_DEPO = r0160_BAL_TOT_DEPO;
	}
	public Date getREPORT_DATE() {
		return REPORT_DATE;
	}
	public void setREPORT_DATE(Date rEPORT_DATE) {
		REPORT_DATE = rEPORT_DATE;
	}
	public String getREPORT_VERSION() {
		return REPORT_VERSION;
	}
	public void setREPORT_VERSION(String rEPORT_VERSION) {
		REPORT_VERSION = rEPORT_VERSION;
	}
	public String getREPORT_FREQUENCY() {
		return REPORT_FREQUENCY;
	}
	public void setREPORT_FREQUENCY(String rEPORT_FREQUENCY) {
		REPORT_FREQUENCY = rEPORT_FREQUENCY;
	}

	/*
	 * public String getREPORT_CODE() { return REPORT_CODE; } public void
	 * setREPORT_CODE(String rEPORT_CODE) { REPORT_CODE = rEPORT_CODE; }
	 */
	public String getREPORT_DESC() {
		return REPORT_DESC;
	}
	public void setREPORT_DESC(String rEPORT_DESC) {
		REPORT_DESC = rEPORT_DESC;
	}
	public String getENTITY_FLG() {
		return ENTITY_FLG;
	}
	public void setENTITY_FLG(String eNTITY_FLG) {
		ENTITY_FLG = eNTITY_FLG;
	}
	public String getMODIFY_FLG() {
		return MODIFY_FLG;
	}
	public void setMODIFY_FLG(String mODIFY_FLG) {
		MODIFY_FLG = mODIFY_FLG;
	}
	public String getDEL_FLG() {
		return DEL_FLG;
	}
	public void setDEL_FLG(String dEL_FLG) {
		DEL_FLG = dEL_FLG;
	}
	public BRF_67_Summary_Entity() {
		super();
		// TODO Auto-generated constructor stub
	}
	
    
    
    

}
