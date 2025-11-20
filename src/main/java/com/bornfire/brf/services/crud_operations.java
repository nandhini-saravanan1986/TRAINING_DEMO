package com.bornfire.brf.services;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bornfire.brf.entities.STD_Demo_Entity;
import com.bornfire.brf.entities.STD_Demo_Repo;

@Service
public class crud_operations {
	@Autowired
	STD_Demo_Repo std_demo_repo;
	public String add(STD_Demo_Entity STD_Demo_Entity) {
		
		std_demo_repo.save(STD_Demo_Entity);
		
		return "addsuccessfully";
		
	}
	public STD_Demo_Entity getData(BigDecimal id) {
		STD_Demo_Entity up=std_demo_repo.findById(id).get();
		return up;
	}
	public void deletedata(BigDecimal id) {
		std_demo_repo.deleteById(id);
		
	}
	

	
	
	

}
