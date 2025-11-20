
package com.bornfire.brf.entities;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BRF_70_Summary_Repo extends JpaRepository<BRF_70_Summary_Entity, Date> {

	@Query(value = "select * from BRF_70_SUMMARY_TABLE  ", nativeQuery = true)
	List<BRF_70_Summary_Entity> getdatabydateList(Date rpt_code);
	
}