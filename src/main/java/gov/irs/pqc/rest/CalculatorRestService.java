package gov.irs.pqc.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import gov.irs.pqc.calc.Calculator;
import gov.irs.pqc.model.Person;
import gov.irs.pqc.model.Result;

/**
 * Calculates whether a person qualifies or not
 * 
 *
 */
@RequestScoped
@Path("calculations")
@Produces({ "application/json" })
@Consumes({ "application/json" })
public class CalculatorRestService {

  @Inject
  private Calculator calculator;

  @POST
  public Result calculate(Person person) {
    return calculator.calculate(person);
  }

}
