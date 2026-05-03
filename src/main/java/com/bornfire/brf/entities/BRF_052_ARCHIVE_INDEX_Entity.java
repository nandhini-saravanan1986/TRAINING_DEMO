package com.bornfire.brf.entities;

import java.sql.Timestamp;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "BRF_052_ARCHIVE_INDEX")
public class BRF_052_ARCHIVE_INDEX_Entity {

    @Id
    @Column(name = "REPORT_DATE")
    private Date reportDate;

    @Column(name = "GENERATED_ON")
    private Timestamp generatedOn;

    // Getters & Setters
    public Date getReportDate()          { return reportDate; }
    public void setReportDate(Date v)    { this.reportDate = v; }
    public Timestamp getGeneratedOn()    { return generatedOn; }
    public void setGeneratedOn(Timestamp v) { this.generatedOn = v; }
}