package com.bornfire.brf.entities;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BRF_67_Summary_Repo extends JpaRepository<BRF_67_Summary_Entity, Date>{
	
	@Query(value = "select * from JEF_BRF_67_SUMMARY_TABLE  ", nativeQuery = true)
	List<BRF_67_Summary_Entity> getdatabydateList(Date rpt_code);

}
