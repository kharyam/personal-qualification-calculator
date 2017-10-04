package gov.irs.pqc.calc;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(monochrome = true 
,format = {"pretty", "html:target/cucumber", "json:target/cucumber.json"}
,tags={"@Qualification"} 
)
public class QualificationTest {

}
