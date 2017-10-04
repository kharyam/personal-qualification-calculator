package gov.irs.pqc.model;

import java.io.Serializable;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Represents a person who will be checked for qualification
 * 
 *
 */
public class Person implements Serializable {

  private static final long serialVersionUID = 1L;

  @NotNull
  private String name;

  @NotNull
  @Min(0)
  private Integer income;

  @NotNull
  @Min(0)
  private Integer dependents;

  public Person() {
    initAttributes();
  }

  public Person(String name, Integer income, Integer dependents) {
    initAttributes();
    if (name != null) {
    	this.name = name;    
    }
    
    if (income != null) {
    	this.income = income;
    }
    
    if (dependents != null) {
      this.dependents = dependents;
    }
    
  }

  public String getName() {
    return name;
  }


  public Integer getIncome() {
    return income;
  }

  public void setIncome(Integer income) {
    this.income = income;
  }

  public Integer getDependents() {
    return dependents;
  }

  public void setDependents(Integer dependents) {
    this.dependents = dependents;
  }

  private void initAttributes() {
	    this.name = "";
	    this.income = 0;
	    this.dependents = 0;	  
  }
}
