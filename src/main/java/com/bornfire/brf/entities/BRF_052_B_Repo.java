package com.bornfire.brf.entities;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BRF_052_B_Repo extends JpaRepository<BRF_052_B_Entity, Date> {

    @Query("SELECT e FROM BRF_052_B_Entity e WHERE e.reportDate = :reportDate AND (e.delFlg IS NULL OR e.delFlg <> '1')")
    List<BRF_052_B_Entity> getDataByDate(@Param("reportDate") Date reportDate);
}
