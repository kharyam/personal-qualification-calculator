# Personal Qualification Calculator

A simple JEE application that computes whether a person is "qualified" based on income and dependents. The WAR is configured to run on Red Hat EAP 7

* Exposes the service as a REST endpoint - http://localhost:8080/person-qualification-calculator/rest/calculations

        curl -X POST -H "content-type:application/json"  --data '{"name":"Bob Smith","income":10000,"dependents":0}' \
         http://localhost:8080/person-qualification-calculator/rest/calculations

* Exposes the service as a SOAP endpoint - http://localhost:8080/person-qualification-calculator/Calculator?wsdl
* Includes a simple cucumber based junit test for the business logic
