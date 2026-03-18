package com.bornfire.brf.controllers;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bornfire.brf.entities.Employee;
import com.bornfire.brf.entities.UserProfile;
import com.bornfire.brf.services.EmployeeService;

@RestController
@ConfigurationProperties("default")
@RequestMapping("/employees")
@CrossOrigin
public class EmployeeController {

    @Autowired
    private EmployeeService service;

    // GET API
    @GetMapping("/getAllEmployeeList")
    public List<Employee> getEmployees(){
        return service.getAllEmployees();
    }

    // POST API
    @PostMapping("/AddEmployee")
    public Employee createEmployee(@RequestBody Employee emp){
        return service.saveEmployee(emp);
    }

    // PUT API
    @PutMapping("/update/{id}")
    public Employee updateEmployee(@PathVariable Long id, @RequestBody Employee emp){
        return service.updateEmployee(id, emp);
    }

    // DELETE API
    @DeleteMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id){
        service.deleteEmployee(id);
        return "Employee Deleted Successfully";
    }
    
    
   
}