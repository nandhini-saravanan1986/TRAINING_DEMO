package com.bornfire.brf.entities;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BRRS_M_IS_RESUB_DETAIL_REPO extends JpaRepository<M_IS_RESUB_DETAIL_ENTITY, String> {

	@Query(value = "select * from BRRS_M_IS_RESUB_DETAIL where REPORT_DATE=?1 AND DATA_ENTRY_VERSION=?2", nativeQuery = true)
	List<M_IS_RESUB_DETAIL_ENTITY> getdatabydateList(Date reportdate,String DATA_ENTRY_VERSION);
	
	@Query(value = "select * from BRRS_M_IS_RESUB_DETAIL where REPORT_LABEL =?1 and REPORT_ADDL_CRITERIA_1=?2 AND REPORT_DATE=?3 AND DATA_ENTRY_VERSION=?4", nativeQuery = true)
	List<M_IS_RESUB_DETAIL_ENTITY> GetDataByRowIdAndColumnId(String reportLabel,String reportAddlCriteria_1,Date reportdate,String DATA_ENTRY_VERSION);
}