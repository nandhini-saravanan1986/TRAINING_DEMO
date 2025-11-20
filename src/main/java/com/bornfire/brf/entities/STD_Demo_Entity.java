package com.bornfire.brf.entities;

import java.math.BigDecimal;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="STUD_DEMO")
public class STD_Demo_Entity {
	private String stud_id;
	private String name;
	private String phone_no;
	private String age;
	private String gender;
	@Id
	private BigDecimal srl_no;
	public String getStud_id() {
		return stud_id;
	}
	public void setStud_id(String stud_id) {
		this.stud_id = stud_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhone_no() {
		return phone_no;
	}
	public void setPhone_no(String phone_no) {
		this.phone_no = phone_no;
	}
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public BigDecimal getSrl_no() {
		return srl_no;
	}
	public void setSrl_no(BigDecimal srl_no) {
		this.srl_no = srl_no;
	}
	public STD_Demo_Entity(String stud_id, String name, String phone_no, String age, String gender, BigDecimal srl_no) {
		super();
		this.stud_id = stud_id;
		this.name = name;
		this.phone_no = phone_no;
		this.age = age;
		this.gender = gender;
		this.srl_no = srl_no;
	}
	public STD_Demo_Entity() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
