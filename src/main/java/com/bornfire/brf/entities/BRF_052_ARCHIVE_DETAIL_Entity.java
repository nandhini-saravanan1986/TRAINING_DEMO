package com.bornfire.brf.entities;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "BRF_052_ARCHIVE_DETAIL")
@IdClass(BRF_052_ARCHIVE_DETAIL_Key.class)
public class BRF_052_ARCHIVE_DETAIL_Entity {

    @Id
    @Column(name = "CUST_ID")
    private String custId;

    @Column(name = "ACCT_NUMBER")
    private String acctNumber;

    @Column(name = "ACCT_NAME")
    private String acctName;

    @Column(name = "DATA_TYPE")
    private String dataType;

    @Id
    @Column(name = "ROW_ID")
    private String rowId;

    @Id
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

    @Id
    @Column(name = "REPORT_DATE")
    private Date reportDate;

    @Column(name = "REPORT_NAME")
    private String reportName;

    @Column(name = "DEL_FLG")
    private String delFlg;

    // ── Getters & Setters ──────────────────────────────────────────
    public String getCustId() { return custId; }
    public void setCustId(String v) { this.custId = v; }
    public String getAcctNumber() { return acctNumber; }
    public void setAcctNumber(String v) { this.acctNumber = v; }
    public String getAcctName() { return acctName; }
    public void setAcctName(String v) { this.acctName = v; }
    public String getDataType() { return dataType; }
    public void setDataType(String v) { this.dataType = v; }
    public String getRowId() { return rowId; }
    public void setRowId(String v) { this.rowId = v; }
    public String getColumnId() { return columnId; }
    public void setColumnId(String v) { this.columnId = v; }
    public String getReportRemarks() { return reportRemarks; }
    public void setReportRemarks(String v) { this.reportRemarks = v; }
    public String getModificationRemarks() { return modificationRemarks; }
    public void setModificationRemarks(String v) { this.modificationRemarks = v; }
    public String getDataEntryVersion() { return dataEntryVersion; }
    public void setDataEntryVersion(String v) { this.dataEntryVersion = v; }
    public BigDecimal getAmountAed() { return amountAed; }
    public void setAmountAed(BigDecimal v) { this.amountAed = v; }
    public Date getReportDate() { return reportDate; }
    public void setReportDate(Date v) { this.reportDate = v; }
    public String getReportName() { return reportName; }
    public void setReportName(String v) { this.reportName = v; }
    public String getDelFlg() { return delFlg; }
    public void setDelFlg(String v) { this.delFlg = v; }
}