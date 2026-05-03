package com.bornfire.brf.entities;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BRF_008_A_Repo extends JpaRepository<BRF_008_A_Entity, Date> {

    @Query(value = "SELECT * FROM BRF_008_A WHERE REPORT_DATE = ?1 AND (DEL_FLG IS NULL OR DEL_FLG != 'Y')", nativeQuery = true)
    List<BRF_008_A_Entity> getDataByDate(Date reportDate);

    @Query(value = "SELECT * FROM BRF_008_A WHERE REPORT_DATE = ?1 AND REPORT_CODE = ?2 AND (DEL_FLG IS NULL OR DEL_FLG != 'Y')", nativeQuery = true)
    List<BRF_008_A_Entity> getDataByDateAndCode(Date reportDate, String reportCode);
}
