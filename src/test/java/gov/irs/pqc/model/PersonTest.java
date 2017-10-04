package gov.irs.pqc.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class PersonTest {

	@Test
	public void nonNullNameDefaultConstructor() {
		Person p = new Person();
		assertNotNull(p.getName());
	}

	@Test
	public void nonNullDependentsDefaultConstructor() {
		Person p = new Person();
		assertNotNull(p.getDependents());
	}

	@Test
	public void nonNullIncomeDefaultConstructor() {
		Person p = new Person();
		assertNotNull(p.getIncome());
	}

	
	@Test
	public void nonNullNameParmConstructor() {
		Person p = new Person(null, null, null);
		assertNotNull(p.getName());
	}

	@Test
	public void nonNullDependentsParmConstructor() {
		Person p = new Person(null, null, null);
		assertNotNull(p.getDependents());
	}

	@Test
	public void nonNullIncomeParmConstructor() {
		Person p = new Person(null, null, null);
		assertNotNull(p.getIncome());
	}

	
}
