package com.bornfire.brf.entities;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BRRS_M_SFINP2_Summary_Repo extends JpaRepository<M_SFINP2_Summary_Entity, Date> {

	@Query(value = "select * from BRRS_M_SFINP2_SUMMARYTABLE  ", nativeQuery = true)
	List<M_SFINP2_Summary_Entity> getdatabydateList(Date rpt_code);
	
}
