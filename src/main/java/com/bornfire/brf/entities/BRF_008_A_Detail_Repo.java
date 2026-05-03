package com.bornfire.brf.entities;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BRF_008_A_Detail_Repo extends JpaRepository<BRF_008_A_Detail_Entity, String> {

    @Query(value = "SELECT * FROM BRF_008_A_DETAIL WHERE REPORT_DATE = ?1 AND (DEL_FLG IS NULL OR DEL_FLG != 'Y')", nativeQuery = true)
    List<BRF_008_A_Detail_Entity> getDataByDate(Date reportDate);
}