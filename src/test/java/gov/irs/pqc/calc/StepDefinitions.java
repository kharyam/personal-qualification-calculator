package gov.irs.pqc.calc;

import java.util.List;
import java.util.Map;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gov.irs.pqc.model.Person;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class StepDefinitions {
	
	private Calculator calculator = null;
	
	@When("^the calculator has been initialized$")
	public void the_calculator_has_been_initialized() throws Throwable {
	    calculator = new Calculator();
		
	}

	@Then("^validate these results:$")
	public void validate_these_results(List<Map<String,String>> table) throws Throwable {
		for (Map<String,String> row : table) {
			verifyRow(row);
		}
	}

	private void verifyRow(Map<String, String> row) {
		String name = row.get("name");
		Integer income = Integer.parseInt(row.get("income"));
		Integer dependents = Integer.parseInt(row.get("dependents"));		
		Boolean qualified = Boolean.parseBoolean(row.get("qualified"));
		
		System.out.println("Verifying " + name);
		assertThat (calculator.calculate(new Person(name, income, dependents)).getApproved(), equalTo(qualified));
	}
}
