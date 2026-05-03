package com.bornfire.brf.entities;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BRF_052_ARCHIVE_A_Repo
        extends JpaRepository<BRF_052_ARCHIVE_A_Entity, Date> {

    // Fetch archived summary row for a specific report date
    @Query("SELECT e FROM BRF_052_ARCHIVE_A_Entity e WHERE e.reportDate = :reportDate")
    List<BRF_052_ARCHIVE_A_Entity> getArchiveByDate(@Param("reportDate") Date reportDate);

    // Used by Archive Index list page — returns all archived dates newest first
    @Query("SELECT DISTINCT e.reportDate FROM BRF_052_ARCHIVE_A_Entity e ORDER BY e.reportDate DESC")
    List<Date> findDistinctReportDates();
}