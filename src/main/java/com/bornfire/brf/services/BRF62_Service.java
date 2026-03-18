package com.bornfire.brf.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.bornfire.brf.entities.BRF_62_Summary_Entity;
import com.bornfire.brf.entities.BRF_62_Summary_Repo;

@Service
public class BRF62_Service {

    @Autowired
    BRF_62_Summary_Repo repo;

    public BRF_62_Summary_Entity getReport() {
        return repo.findAll().get(0); 
       
    }

	public Map<String,List<String>> getAllReports() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*public BRF_62_Summary_Entity getReport() {
	    BRF_62_Summary_Entity data = repo.findAll().get(0);
	    System.out.println(data);  
	    return data;
	}*/
}