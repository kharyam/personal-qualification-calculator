@Qualification
Feature: Verify the qualification rules

Scenario: 1) Check rules for individuals
  When the calculator has been initialized
  Then validate these results:
    | name          | income | dependents | qualified |
    | Alex          | 10000  | 0          | True      |
    | Bob           | 20000  | 0          | True      |
    | Chris         | 20001  | 0          | False     |
    | Don           | 100000 | 0          | False     |

Scenario: 2) Check rules for families
  When the calculator has been initialized
  Then validate these results:
    | name          | income | dependents | qualified |
    | Eric          | 25000  | 1          | False     |
    | Frank         | 20000  | 2          | True      |
    | Greg          | 40000  | 3          | True      |
    | Henry         | 41000  | 3          | False     |