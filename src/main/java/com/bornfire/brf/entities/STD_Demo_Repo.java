package com.bornfire.brf.entities;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface STD_Demo_Repo  extends JpaRepository<STD_Demo_Entity,BigDecimal>{
	@Query(value="select * from STUD_DEMO",nativeQuery=true)
	List<STD_Demo_Entity> getListbydate(); 
	

}
