package com.bornfire.brf.entities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BRF_052_Detail_Repo
        extends JpaRepository<BRF_052_Detail_Entity, BRF_052_Detail_Key> {

    // ── Existing full list query — kept for Excel/PDF download ────────────────
    @Query(value = "SELECT * FROM BRF_052_DETAIL " +
                   "WHERE REPORT_DATE = ?1 " +
                   "AND (DEL_FLG IS NULL OR DEL_FLG != 'Y')",
           nativeQuery = true)
    List<BRF_052_Detail_Entity> getDataByDate(Date reportDate);

    // ── Paginated — no filter, sorted by ROW_ID then COLUMN_ID ───────────────
    @Query(value = "SELECT * FROM BRF_052_DETAIL " +
                   "WHERE REPORT_DATE = ?1 " +
                   "AND (DEL_FLG IS NULL OR DEL_FLG != 'Y') " +
                   "ORDER BY ROW_ID ASC, COLUMN_ID ASC",
           countQuery = "SELECT COUNT(*) FROM BRF_052_DETAIL " +
                        "WHERE REPORT_DATE = ?1 " +
                        "AND (DEL_FLG IS NULL OR DEL_FLG != 'Y')",
           nativeQuery = true)
    Page<BRF_052_Detail_Entity> getDataByDatePaged(Date reportDate, Pageable pageable);

    // ── Paginated — with rowId + columnId filter (cell-click from summary) ────
    @Query(value = "SELECT * FROM BRF_052_DETAIL " +
                   "WHERE REPORT_DATE = ?1 " +
                   "AND ROW_ID = ?2 " +
                   "AND COLUMN_ID = ?3 " +
                   "AND (DEL_FLG IS NULL OR DEL_FLG != 'Y') " +
                   "ORDER BY ROW_ID ASC, COLUMN_ID ASC",
           countQuery = "SELECT COUNT(*) FROM BRF_052_DETAIL " +
                        "WHERE REPORT_DATE = ?1 " +
                        "AND ROW_ID = ?2 " +
                        "AND COLUMN_ID = ?3 " +
                        "AND (DEL_FLG IS NULL OR DEL_FLG != 'Y')",
           nativeQuery = true)
    Page<BRF_052_Detail_Entity> getDataByDateAndFilterPaged(Date reportDate,
                                                             String rowId,
                                                             String columnId,
                                                             Pageable pageable);

    // ── Existing update — unchanged ───────────────────────────────────────────
    @Modifying
    @Query(value = "UPDATE BRF_052_DETAIL SET " +
                   "ACCT_NUMBER = ?1, " +
                   "ACCT_NAME = ?2, " +
                   "AMOUNT_AED = ?3, " +
                   "REPORT_REMARKS = ?4 " +
                   "WHERE CUST_ID = ?5 " +
                   "AND ROW_ID = ?6 " +
                   "AND COLUMN_ID = ?7 " +
                   "AND REPORT_DATE = ?8",
           nativeQuery = true)
    int updateDetailRecord(String acctNumber, String acctName,
                           BigDecimal amountAed, String reportRemarks,
                           String custId, String rowId, String columnId,
                           Date reportDate);
}