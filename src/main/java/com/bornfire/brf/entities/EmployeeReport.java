package com.bornfire.brf.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name="EMPLOYEE_REPORT")
public class EmployeeReport {

	@Id	
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "emp_se")
	@SequenceGenerator(name = "emp_se", sequenceName = "EMP_SE", allocationSize = 1)
	@Column(name = "ID")
	private Integer id;
	
	@Column(name="NAME")
    private String name;

    @Column(name="AGE")
    private Integer age;

    @Column(name="DEPT")
    private String dept;

    @Column(name="PLACE")
    private String place;

    @Column(name="JOINED_YEAR")
    private Integer joinedYear;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public Integer getJoinedYear() {
		return joinedYear;
	}

	public void setJoinedYear(Integer joinedYear) {
		this.joinedYear = joinedYear;
	}

    // getters and setters
    
}
