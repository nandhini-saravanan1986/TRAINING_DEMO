package com.bornfire.brf.entities;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface Request_code_mapping_Repo extends JpaRepository<Request_code_mapping_Entity, String> {

	@Query(value = "select * from J_BRF_MAPPING_TABLE", nativeQuery = true)
	List<Request_code_mapping_Entity> getListbydate();

	@Query(value = "SELECT * FROM J_BRF_MAPPING_TABLE WHERE Report_Code IS NOT NULL AND Report_Label IS NOT NULL", nativeQuery = true)
	List<Request_code_mapping_Entity> getListbyMAList();

	@Query(value = "SELECT * FROM J_BRF_MAPPING_TABLE WHERE Report_Code IS NULL OR Report_Label IS NULL", nativeQuery = true)
	List<Request_code_mapping_Entity> getListbyUMAList();

	@Query(value = "SELECT * FROM J_BRF_MAPPING_TABLE "
			+ "WHERE LOWER(REPORT_CODE) LIKE LOWER('%' || :keyword || '%') "
			+ "OR LOWER(REPORT_LABEL) LIKE LOWER('%' || :keyword || '%')"
			+ "OR LOWER(CUSTOMER_ID) LIKE LOWER('%' || :keyword || '%')"
			+ "OR LOWER(ACCOUNT_NO) LIKE LOWER('%' || :keyword || '%')"
			+ "OR LOWER(ACCOUNT_NAME) LIKE LOWER('%' || :keyword || '%')"
			+ "OR LOWER(SCHEME_CODE) LIKE LOWER('%' || :keyword || '%')"
			+ "OR LOWER(GLSH) LIKE LOWER('%' || :keyword || '%')"
			+ "OR LOWER(CRITERIA1) LIKE LOWER('%' || :keyword || '%')"
			+ "OR LOWER(CRITERIA2) LIKE LOWER('%' || :keyword || '%')"
			+ "OR LOWER(CRITERIA3) LIKE LOWER('%' || :keyword || '%')", nativeQuery = true)
	List<Request_code_mapping_Entity> searchByKeyword(@Param("keyword") String keyword);
	
	
	@Query(value = "SELECT * FROM J_BRF_MAPPING_TABLE "
		    + "WHERE ("
		    + "LOWER(REPORT_CODE) LIKE LOWER('%' || :keyword || '%') "
		    + "OR LOWER(REPORT_LABEL) LIKE LOWER('%' || :keyword || '%') "
		    + "OR LOWER(CUSTOMER_ID) LIKE LOWER('%' || :keyword || '%') "
		    + "OR LOWER(ACCOUNT_NO) LIKE LOWER('%' || :keyword || '%') "
		    + "OR LOWER(ACCOUNT_NAME) LIKE LOWER('%' || :keyword || '%') "
		    + "OR LOWER(SCHEME_CODE) LIKE LOWER('%' || :keyword || '%') "
		    + "OR LOWER(GLSH) LIKE LOWER('%' || :keyword || '%') "
		    + "OR LOWER(CRITERIA1) LIKE LOWER('%' || :keyword || '%') "
		    + "OR LOWER(CRITERIA2) LIKE LOWER('%' || :keyword || '%')"
		    + "OR LOWER(CRITERIA3) LIKE LOWER('%' || :keyword || '%')"
		    + ") "
		    + "AND REPORT_CODE IS NOT NULL "
		    + "AND REPORT_LABEL IS NOT NULL",
		    nativeQuery = true)
		List<Request_code_mapping_Entity> searchByKeywordMA(@Param("keyword") String keyword);
		
		@Query(value = "SELECT * FROM J_BRF_MAPPING_TABLE " 
			    + "WHERE ("
			    + "LOWER(REPORT_CODE) LIKE LOWER('%' || :keyword || '%') "
			    + "OR LOWER(REPORT_LABEL) LIKE LOWER('%' || :keyword || '%') "
			    + "OR LOWER(CUSTOMER_ID) LIKE LOWER('%' || :keyword || '%') "
			    + "OR LOWER(ACCOUNT_NO) LIKE LOWER('%' || :keyword || '%') "
			    + "OR LOWER(ACCOUNT_NAME) LIKE LOWER('%' || :keyword || '%') "
			    + "OR LOWER(SCHEME_CODE) LIKE LOWER('%' || :keyword || '%') "
			    + "OR LOWER(GLSH) LIKE LOWER('%' || :keyword || '%') "
			    + "OR LOWER(CRITERIA1) LIKE LOWER('%' || :keyword || '%') "
			    + "OR LOWER(CRITERIA2) LIKE LOWER('%' || :keyword || '%') "
			    + "OR LOWER(CRITERIA3) LIKE LOWER('%' || :keyword || '%') "
			    + ") "
			    + "AND (REPORT_CODE IS NULL OR REPORT_LABEL IS NULL)",
			    nativeQuery = true)
			List<Request_code_mapping_Entity> searchByKeywordUMA(@Param("keyword") String keyword);




}
