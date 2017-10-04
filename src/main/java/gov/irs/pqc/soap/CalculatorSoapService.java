package gov.irs.pqc.soap;

import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebService;

import gov.irs.pqc.calc.Calculator;
import gov.irs.pqc.model.Person;
import gov.irs.pqc.model.Result;

@WebService(serviceName = "Calculator")
// http://localhost:8080/person-qualification-calculator/Calculator?wsdl
public class CalculatorSoapService {

  @Inject
  private Calculator calculator;

  @WebMethod
  public Result calculate(Person person) {
    return calculator.calculate(person);
  }

}