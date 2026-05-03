package roshan.entities;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "DEMO_EMP")
public class Roshanent {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "SALARY")
    private String salary;

    

    public Long   getId()         { return id; }
    public String getName()       { return name; }
    public String getSalary() { return salary; }
    

    public void   setId(Long id)         { this.id = id; }
    public void setName(String name)       { this.name = name; }
    public void setSalary(String salary) { this.salary = salary; }
    public Roshanent() {}
}