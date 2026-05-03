package com.bornfire.brf.entities;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BRF_052_ARCHIVE_DETAIL_Repo
        extends JpaRepository<BRF_052_ARCHIVE_DETAIL_Entity, BRF_052_ARCHIVE_DETAIL_Key> {

    // ── Existing full list query — kept for Excel/PDF download ────────────────
    @Query(value = "SELECT * FROM BRF_052_ARCHIVE_DETAIL " +
                   "WHERE REPORT_DATE = ?1",
           nativeQuery = true)
    List<BRF_052_ARCHIVE_DETAIL_Entity> getArchiveByDate(Date reportDate);

    // ── Paginated — no filter, sorted by ROW_ID then COLUMN_ID ───────────────
    @Query(value = "SELECT * FROM BRF_052_ARCHIVE_DETAIL " +
                   "WHERE REPORT_DATE = ?1 " +
                   "ORDER BY ROW_ID ASC, COLUMN_ID ASC",
           countQuery = "SELECT COUNT(*) FROM BRF_052_ARCHIVE_DETAIL " +
                        "WHERE REPORT_DATE = ?1",
           nativeQuery = true)
    Page<BRF_052_ARCHIVE_DETAIL_Entity> getArchiveByDatePaged(Date reportDate, Pageable pageable);

    // ── Paginated — with rowId + columnId filter ──────────────────────────────
    @Query(value = "SELECT * FROM BRF_052_ARCHIVE_DETAIL " +
                   "WHERE REPORT_DATE = ?1 " +
                   "AND ROW_ID = ?2 " +
                   "AND COLUMN_ID = ?3 " +
                   "ORDER BY ROW_ID ASC, COLUMN_ID ASC",
           countQuery = "SELECT COUNT(*) FROM BRF_052_ARCHIVE_DETAIL " +
                        "WHERE REPORT_DATE = ?1 " +
                        "AND ROW_ID = ?2 " +
                        "AND COLUMN_ID = ?3",
           nativeQuery = true)
    Page<BRF_052_ARCHIVE_DETAIL_Entity> getArchiveByDateAndFilterPaged(Date reportDate,
                                                                        String rowId,
                                                                        String columnId,
                                                                        Pageable pageable);
}