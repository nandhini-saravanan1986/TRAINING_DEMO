package com.bornfire.brf.entities;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmployeeReportRepository extends JpaRepository<EmployeeReport, Integer> {

}
