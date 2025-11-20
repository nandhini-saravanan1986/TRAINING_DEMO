package com.bornfire.brf.entities;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface Brf65_sumtb_Repo extends JpaRepository<Brf65_sumtb_Entity, Date> {

	@Query(value = "select * from VASI_BRF_65_SUMMARY_TABLE  ", nativeQuery = true)
	List<Brf65_sumtb_Entity> getdatabydateList(Date rpt_code);
	
}