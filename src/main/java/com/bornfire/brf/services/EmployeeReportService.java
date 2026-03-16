package com.bornfire.brf.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bornfire.brf.entities.EmployeeReport;
import com.bornfire.brf.entities.EmployeeReportRepository;

@Service
public class EmployeeReportService {

    @Autowired
    EmployeeReportRepository employeeRepo;

    // LIST
    public List<EmployeeReport> getAllEmployees() {
        return employeeRepo.findAll();
    }

    // SAVE (ADD / UPDATE)
    public void saveEmployee(EmployeeReport emp) {
        employeeRepo.save(emp);
    }

    // GET BY ID (EDIT PAGE)
    public EmployeeReport getEmployeeById(Integer id) {
        return employeeRepo.findById(id).orElse(null);
    }

    // DELETE
    public void deleteEmployee(Integer id) {
        employeeRepo.deleteById(id);
    }

}