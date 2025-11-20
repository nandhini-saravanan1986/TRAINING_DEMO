package com.bornfire.brf.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="J_BRF_MAPPING_TABLE")
public class Request_code_mapping_Entity {
	@Id
	private String Customer_Id;
	private String Account_No;
	private String Account_Name;
	private String Scheme_Code;
	private String GLSH;
	private String Report_Code;
	private String Report_Label;
	private String Criteria1;
	private String Criteria2;
	private String Criteria3;
	public String getCustomer_Id() {
		return Customer_Id;
	}
	public void setCustomer_Id(String customer_Id) {
		Customer_Id = customer_Id;
	}
	public String getAccount_No() {
		return Account_No;
	}
	public void setAccount_No(String account_No) {
		Account_No = account_No;
	}
	public String getAccount_Name() {
		return Account_Name;
	}
	public void setAccount_Name(String account_Name) {
		Account_Name = account_Name;
	}
	public String getScheme_Code() {
		return Scheme_Code;
	}
	public void setScheme_Code(String scheme_Code) {
		Scheme_Code = scheme_Code;
	}
	public String getGLSH() {
		return GLSH;
	}
	public void setGLSH(String gLSH) {
		GLSH = gLSH;
	}
	public String getReport_Code() {
		return Report_Code;
	}
	public void setReport_Code(String report_Code) {
		Report_Code = report_Code;
	}
	public String getReport_Label() {
		return Report_Label;
	}
	public void setReport_Label(String report_Label) {
		Report_Label = report_Label;
	}
	public String getCriteria1() {
		return Criteria1;
	}
	public void setCriteria1(String criteria1) {
		Criteria1 = criteria1;
	}
	public String getCriteria2() {
		return Criteria2;
	}
	public void setCriteria2(String criteria2) {
		Criteria2 = criteria2;
	}
	public String getCriteria3() {
		return Criteria3;
	}
	public void setCriteria3(String criteria3) {
		Criteria3 = criteria3;
	}
	public Request_code_mapping_Entity(String customer_Id, String account_No, String account_Name, String scheme_Code,
			String gLSH, String report_Code, String report_Label, String criteria1, String criteria2,
			String criteria3) {
		super();
		Customer_Id = customer_Id;
		Account_No = account_No;
		Account_Name = account_Name;
		Scheme_Code = scheme_Code;
		GLSH = gLSH;
		Report_Code = report_Code;
		Report_Label = report_Label;
		Criteria1 = criteria1;
		Criteria2 = criteria2;
		Criteria3 = criteria3;
	}
	public Request_code_mapping_Entity() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

}
