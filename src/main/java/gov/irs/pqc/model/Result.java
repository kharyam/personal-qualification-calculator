package gov.irs.pqc.model;

import javax.validation.constraints.NotNull;

/**
 * 
 * Represents the result of the qualification check
 *
 */
public class Result {

  private Boolean approved;

  @NotNull
  private String notes;

  public Boolean getApproved() {
    return approved;
  }

  public void setApproved(Boolean approved) {
    this.approved = approved;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

}
