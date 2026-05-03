package com.bornfire.brf.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class BRF_052_Detail_Key implements Serializable {
	private static final long serialVersionUID = 1L;
    private String custId;
    private String rowId;
    private String columnId;
    private Date reportDate;

    public BRF_052_Detail_Key() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BRF_052_Detail_Key)) return false;
        BRF_052_Detail_Key that = (BRF_052_Detail_Key) o;
        return Objects.equals(custId, that.custId) &&
               Objects.equals(rowId, that.rowId) &&
               Objects.equals(columnId, that.columnId) &&
               Objects.equals(reportDate, that.reportDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(custId, rowId, columnId, reportDate);
    }
}