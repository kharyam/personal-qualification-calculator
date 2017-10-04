package gov.irs.pqc.rest;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@RequestScoped
@Path("healthz")
@Produces("text/plain")
@Consumes("text/plain")
public class HealthService {
  
  @GET
  public String health() {
    return "healthy";
  }

}
