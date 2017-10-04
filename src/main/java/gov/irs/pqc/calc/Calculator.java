package gov.irs.pqc.calc;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.validation.Validator;

import gov.irs.pqc.model.Person;
import gov.irs.pqc.model.Result;

@Stateless
public class Calculator {

  @Resource
  private Validator validator;

  public Result calculate(Person person) {

    Result result = new Result();

    if (validPerson(person)) {
      result.setApproved(person.getDependents() == 0 && person.getIncome() <= 20000
          || person.getDependents() != 0 && person.getIncome() / (person.getDependents() + 1) <= 10000);

      result.setNotes(person.getName() + (result.getApproved() ? " was approved." : " was not approved."));
    } else {
      result.setNotes("The inputs to the calculator were not valid.");
    }

    return result;
  }

  private boolean validPerson(Person person) {
    // TODO Remove the validator==null check once the unit tests are updated to
    // mock the validator
    return validator == null || validator.validate(person).isEmpty();
  }

}
