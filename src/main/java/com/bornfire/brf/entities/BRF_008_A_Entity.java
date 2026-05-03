package com.bornfire.brf.entities;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "BRF_008_A")
public class BRF_008_A_Entity {

    // R7 - Section header row (VARCHAR fields)
    @Column(name = "R7_ITEM")
    public String R7_ITEM;
    @Column(name = "R7_NOM_AMT")
    public String R7_NOM_AMT;
    @Column(name = "R7_ELA")
    public String R7_ELA;

    // R8 - Item 1.1: Physical cash in hand
    @Column(name = "R8_ITEM")
    public String R8_ITEM;
    @Column(name = "R8_NOM_AMT")
    public BigDecimal R8_NOM_AMT;

    // R9 - Item 1.2: Statutory reserves
    @Column(name = "R9_ITEM")
    public String R9_ITEM;
    @Column(name = "R9_NOM_AMT")
    public BigDecimal R9_NOM_AMT;

    // R10 - Item 1.3: Free and other account balances
    @Column(name = "R10_ITEM")
    public String R10_ITEM;
    @Column(name = "R10_NOM_AMT")
    public BigDecimal R10_NOM_AMT;

    // R11 - Item 1.4: Central Bank CDs
    @Column(name = "R11_ITEM")
    public String R11_ITEM;
    @Column(name = "R11_NOM_AMT")
    public BigDecimal R11_NOM_AMT;

    // R12 - Item 1.5: UAE Federal Government Bonds
    @Column(name = "R12_ITEM")
    public String R12_ITEM;
    @Column(name = "R12_NOM_AMT")
    public BigDecimal R12_NOM_AMT;

    // R13 - Sub Total (1.1 to 1.5)
    @Column(name = "R13_ITEM")
    public String R13_ITEM;
    @Column(name = "R13_NOM_AMT")
    public BigDecimal R13_NOM_AMT;
    @Column(name = "R13_ELA")
    public BigDecimal R13_ELA;

    // R14 - Item 1.6: UAE local govt bonds Rated A+ and above
    @Column(name = "R14_ITEM")
    public String R14_ITEM;
    @Column(name = "R14_NOM_AMT")
    public BigDecimal R14_NOM_AMT;

    // R15 - Item 1.7: UAE local govt bonds Rated A and below
    @Column(name = "R15_ITEM")
    public String R15_ITEM;
    @Column(name = "R15_NOM_AMT")
    public BigDecimal R15_NOM_AMT;

    // R16 - Item 1.8: UAE Public sector Rated A+ and above
    @Column(name = "R16_ITEM")
    public String R16_ITEM;
    @Column(name = "R16_NOM_AMT")
    public BigDecimal R16_NOM_AMT;

    // R17 - Item 1.9: UAE Public sector Rated A and below
    @Column(name = "R17_ITEM")
    public String R17_ITEM;
    @Column(name = "R17_NOM_AMT")
    public BigDecimal R17_NOM_AMT;

    // R18 - Sub total (1.6 to 1.9)
    @Column(name = "R18_ITEM")
    public String R18_ITEM;
    @Column(name = "R18_NOM_AMT")
    public BigDecimal R18_NOM_AMT;
    @Column(name = "R18_ELA")
    public BigDecimal R18_ELA;

    // R19 - Item 1.10: Foreign Sovereign debt
    @Column(name = "R19_ITEM")
    public String R19_ITEM;
    @Column(name = "R19_NOM_AMT")
    public BigDecimal R19_NOM_AMT;
    @Column(name = "R19_ELA")
    public BigDecimal R19_ELA;

    // R20 - Item 1.11: Total
    @Column(name = "R20_ITEM")
    public String R20_ITEM;
    @Column(name = "R20_NOM_AMT")
    public BigDecimal R20_NOM_AMT;
    @Column(name = "R20_ELA")
    public BigDecimal R20_ELA;

    // R21 - Item 2: Total liabilities
    @Column(name = "R21_ITEM")
    public String R21_ITEM;
    @Column(name = "R21_ELA")
    public BigDecimal R21_ELA;

    // R22 - Item 3: ELAR
    @Column(name = "R22_ITEM")
    public String R22_ITEM;
    @Column(name = "R22_ELA")
    public BigDecimal R22_ELA;

    // Metadata
    @Id
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Column(name = "REPORT_DATE")
    private Date reportDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "REPORT_FROM_DATE")
    private Date reportFromDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "REPORT_TO_DATE")
    private Date reportToDate;

    @Column(name = "ENTITY_FLG")
    public String ENTITY_FLG;

    @Column(name = "DEL_FLG")
    public String DEL_FLG;

    @Column(name = "MODIFY_FLG")
    public String MODIFY_FLG;

    @Column(name = "REPORT_CODE")
    public String REPORT_CODE;

    @Temporal(TemporalType.DATE)
    @Column(name = "REPORT_SUBMIT_DATE")
    private Date reportSubmitDate;

    // ---- Getters and Setters ----

    public String getR7_ITEM() { return R7_ITEM; }
    public void setR7_ITEM(String r7_ITEM) { R7_ITEM = r7_ITEM; }
    public String getR7_NOM_AMT() { return R7_NOM_AMT; }
    public void setR7_NOM_AMT(String r7_NOM_AMT) { R7_NOM_AMT = r7_NOM_AMT; }
    public String getR7_ELA() { return R7_ELA; }
    public void setR7_ELA(String r7_ELA) { R7_ELA = r7_ELA; }

    public String getR8_ITEM() { return R8_ITEM; }
    public void setR8_ITEM(String r8_ITEM) { R8_ITEM = r8_ITEM; }
    public BigDecimal getR8_NOM_AMT() { return R8_NOM_AMT; }
    public void setR8_NOM_AMT(BigDecimal r8_NOM_AMT) { R8_NOM_AMT = r8_NOM_AMT; }

    public String getR9_ITEM() { return R9_ITEM; }
    public void setR9_ITEM(String r9_ITEM) { R9_ITEM = r9_ITEM; }
    public BigDecimal getR9_NOM_AMT() { return R9_NOM_AMT; }
    public void setR9_NOM_AMT(BigDecimal r9_NOM_AMT) { R9_NOM_AMT = r9_NOM_AMT; }

    public String getR10_ITEM() { return R10_ITEM; }
    public void setR10_ITEM(String r10_ITEM) { R10_ITEM = r10_ITEM; }
    public BigDecimal getR10_NOM_AMT() { return R10_NOM_AMT; }
    public void setR10_NOM_AMT(BigDecimal r10_NOM_AMT) { R10_NOM_AMT = r10_NOM_AMT; }

    public String getR11_ITEM() { return R11_ITEM; }
    public void setR11_ITEM(String r11_ITEM) { R11_ITEM = r11_ITEM; }
    public BigDecimal getR11_NOM_AMT() { return R11_NOM_AMT; }
    public void setR11_NOM_AMT(BigDecimal r11_NOM_AMT) { R11_NOM_AMT = r11_NOM_AMT; }

    public String getR12_ITEM() { return R12_ITEM; }
    public void setR12_ITEM(String r12_ITEM) { R12_ITEM = r12_ITEM; }
    public BigDecimal getR12_NOM_AMT() { return R12_NOM_AMT; }
    public void setR12_NOM_AMT(BigDecimal r12_NOM_AMT) { R12_NOM_AMT = r12_NOM_AMT; }

    public String getR13_ITEM() { return R13_ITEM; }
    public void setR13_ITEM(String r13_ITEM) { R13_ITEM = r13_ITEM; }
    public BigDecimal getR13_NOM_AMT() { return R13_NOM_AMT; }
    public void setR13_NOM_AMT(BigDecimal r13_NOM_AMT) { R13_NOM_AMT = r13_NOM_AMT; }
    public BigDecimal getR13_ELA() { return R13_ELA; }
    public void setR13_ELA(BigDecimal r13_ELA) { R13_ELA = r13_ELA; }

    public String getR14_ITEM() { return R14_ITEM; }
    public void setR14_ITEM(String r14_ITEM) { R14_ITEM = r14_ITEM; }
    public BigDecimal getR14_NOM_AMT() { return R14_NOM_AMT; }
    public void setR14_NOM_AMT(BigDecimal r14_NOM_AMT) { R14_NOM_AMT = r14_NOM_AMT; }

    public String getR15_ITEM() { return R15_ITEM; }
    public void setR15_ITEM(String r15_ITEM) { R15_ITEM = r15_ITEM; }
    public BigDecimal getR15_NOM_AMT() { return R15_NOM_AMT; }
    public void setR15_NOM_AMT(BigDecimal r15_NOM_AMT) { R15_NOM_AMT = r15_NOM_AMT; }

    public String getR16_ITEM() { return R16_ITEM; }
    public void setR16_ITEM(String r16_ITEM) { R16_ITEM = r16_ITEM; }
    public BigDecimal getR16_NOM_AMT() { return R16_NOM_AMT; }
    public void setR16_NOM_AMT(BigDecimal r16_NOM_AMT) { R16_NOM_AMT = r16_NOM_AMT; }

    public String getR17_ITEM() { return R17_ITEM; }
    public void setR17_ITEM(String r17_ITEM) { R17_ITEM = r17_ITEM; }
    public BigDecimal getR17_NOM_AMT() { return R17_NOM_AMT; }
    public void setR17_NOM_AMT(BigDecimal r17_NOM_AMT) { R17_NOM_AMT = r17_NOM_AMT; }

    public String getR18_ITEM() { return R18_ITEM; }
    public void setR18_ITEM(String r18_ITEM) { R18_ITEM = r18_ITEM; }
    public BigDecimal getR18_NOM_AMT() { return R18_NOM_AMT; }
    public void setR18_NOM_AMT(BigDecimal r18_NOM_AMT) { R18_NOM_AMT = r18_NOM_AMT; }
    public BigDecimal getR18_ELA() { return R18_ELA; }
    public void setR18_ELA(BigDecimal r18_ELA) { R18_ELA = r18_ELA; }

    public String getR19_ITEM() { return R19_ITEM; }
    public void setR19_ITEM(String r19_ITEM) { R19_ITEM = r19_ITEM; }
    public BigDecimal getR19_NOM_AMT() { return R19_NOM_AMT; }
    public void setR19_NOM_AMT(BigDecimal r19_NOM_AMT) { R19_NOM_AMT = r19_NOM_AMT; }
    public BigDecimal getR19_ELA() { return R19_ELA; }
    public void setR19_ELA(BigDecimal r19_ELA) { R19_ELA = r19_ELA; }

    public String getR20_ITEM() { return R20_ITEM; }
    public void setR20_ITEM(String r20_ITEM) { R20_ITEM = r20_ITEM; }
    public BigDecimal getR20_NOM_AMT() { return R20_NOM_AMT; }
    public void setR20_NOM_AMT(BigDecimal r20_NOM_AMT) { R20_NOM_AMT = r20_NOM_AMT; }
    public BigDecimal getR20_ELA() { return R20_ELA; }
    public void setR20_ELA(BigDecimal r20_ELA) { R20_ELA = r20_ELA; }

    public String getR21_ITEM() { return R21_ITEM; }
    public void setR21_ITEM(String r21_ITEM) { R21_ITEM = r21_ITEM; }
    public BigDecimal getR21_ELA() { return R21_ELA; }
    public void setR21_ELA(BigDecimal r21_ELA) { R21_ELA = r21_ELA; }

    public String getR22_ITEM() { return R22_ITEM; }
    public void setR22_ITEM(String r22_ITEM) { R22_ITEM = r22_ITEM; }
    public BigDecimal getR22_ELA() { return R22_ELA; }
    public void setR22_ELA(BigDecimal r22_ELA) { R22_ELA = r22_ELA; }

    public Date getReportDate() { return reportDate; }
    public void setReportDate(Date reportDate) { this.reportDate = reportDate; }
    public Date getReportFromDate() { return reportFromDate; }
    public void setReportFromDate(Date reportFromDate) { this.reportFromDate = reportFromDate; }
    public Date getReportToDate() { return reportToDate; }
    public void setReportToDate(Date reportToDate) { this.reportToDate = reportToDate; }
    public String getENTITY_FLG() { return ENTITY_FLG; }
    public void setENTITY_FLG(String eNTITY_FLG) { ENTITY_FLG = eNTITY_FLG; }
    public String getDEL_FLG() { return DEL_FLG; }
    public void setDEL_FLG(String dEL_FLG) { DEL_FLG = dEL_FLG; }
    public String getMODIFY_FLG() { return MODIFY_FLG; }
    public void setMODIFY_FLG(String mODIFY_FLG) { MODIFY_FLG = mODIFY_FLG; }
    public String getREPORT_CODE() { return REPORT_CODE; }
    public void setREPORT_CODE(String rEPORT_CODE) { REPORT_CODE = rEPORT_CODE; }
    public Date getReportSubmitDate() { return reportSubmitDate; }
    public void setReportSubmitDate(Date reportSubmitDate) { this.reportSubmitDate = reportSubmitDate; }

    public BRF_008_A_Entity() {
        super();
    }
}
