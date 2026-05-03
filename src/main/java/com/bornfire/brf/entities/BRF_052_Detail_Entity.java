package com.bornfire.brf.entities;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "BRF_052_DETAIL")
@IdClass(BRF_052_Detail_Key.class)          // ← added
public class BRF_052_Detail_Entity {

    @Id
    @Column(name = "CUST_ID")
    private String custId;

    @Column(name = "ACCT_NUMBER")
    private String acctNumber;

    @Column(name = "ACCT_NAME")
    private String acctName;

    @Column(name = "DATA_TYPE")
    private String dataType;

    @Id                                      // ← added
    @Column(name = "ROW_ID")
    private String rowId;

    @Id                                      // ← added
    @Column(name = "COLUMN_ID")
    private String columnId;

    @Column(name = "REPORT_REMARKS")
    private String reportRemarks;

    @Column(name = "MODIFICATION_REMARKS")
    private String modificationRemarks;

    @Column(name = "DATA_ENTRY_VERSION")
    private String dataEntryVersion;

    @Column(name = "AMOUNT_AED")
    private BigDecimal amountAed;

    @Id                                      // ← added
    @Column(name = "REPORT_DATE")
    private Date reportDate;

    @Column(name = "REPORT_NAME")
    private String reportName;

    @Column(name = "DEL_FLG")
    private String delFlg;

    // ── Getters & Setters ──────────────────────────────────────────
    public String getCustId() { return custId; }
    public void setCustId(String custId) { this.custId = custId; }
    public String getAcctNumber() { return acctNumber; }
    public void setAcctNumber(String acctNumber) { this.acctNumber = acctNumber; }
    public String getAcctName() { return acctName; }
    public void setAcctName(String acctName) { this.acctName = acctName; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    public String getRowId() { return rowId; }
    public void setRowId(String rowId) { this.rowId = rowId; }
    public String getColumnId() { return columnId; }
    public void setColumnId(String columnId) { this.columnId = columnId; }
    public String getReportRemarks() { return reportRemarks; }
    public void setReportRemarks(String reportRemarks) { this.reportRemarks = reportRemarks; }
    public String getModificationRemarks() { return modificationRemarks; }
    public void setModificationRemarks(String modificationRemarks) { this.modificationRemarks = modificationRemarks; }
    public String getDataEntryVersion() { return dataEntryVersion; }
    public void setDataEntryVersion(String dataEntryVersion) { this.dataEntryVersion = dataEntryVersion; }
    public BigDecimal getAmountAed() { return amountAed; }
    public void setAmountAed(BigDecimal amountAed) { this.amountAed = amountAed; }
    public Date getReportDate() { return reportDate; }
    public void setReportDate(Date reportDate) { this.reportDate = reportDate; }
    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }
    public String getDelFlg() { return delFlg; }
    public void setDelFlg(String delFlg) { this.delFlg = delFlg; }
}