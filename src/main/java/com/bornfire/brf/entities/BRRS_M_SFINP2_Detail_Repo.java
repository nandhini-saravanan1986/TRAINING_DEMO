package com.bornfire.brf.entities;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BRRS_M_SFINP2_Detail_Repo extends JpaRepository<M_SFINP2_Detail_Entity, String> {

	@Query(value = "select * from BRRS_M_SFINP2_DETAILTABLE where REPORT_DATE =?1  ", nativeQuery = true)
	List<M_SFINP2_Detail_Entity> getdatabydateList(Date reportdate);
	
	@Query(value = "select * from BRRS_M_SFINP2_DETAILTABLE WHERE REPORT_DATE =?1 and COLUMN_ID =?2 and ROW_ID=?3 ", nativeQuery = true)
	List<M_SFINP2_Detail_Entity> getdatabydateListrow(Date reportdate,String COLUMN_ID, String ROW_ID);
	
	@Query(value = "select * from BRRS_M_SFINP2_DETAILTABLE where REPORT_DATE=?1 offset ?2 rows fetch next ?3 rows only", nativeQuery = true)
	List<M_SFINP2_Detail_Entity> getdatabydateList(Date reportdate,int startpage,int endpage);
	
	@Query(value = "select count(*) from BRRS_M_SFINP2_DETAILTABLE where REPORT_DATE=?1", nativeQuery = true)
	int getdatacount(Date reportdate);
}
