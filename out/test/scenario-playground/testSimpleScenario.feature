Feature: Case management

  Scenario: Example 1
    Given a Case:
      | Case Id |
      |      10 |
    And a Product:
      | Subscription Cost    | Product Name          |
      |                150   | Product 1             |
    And some Case Detail:
      | Title                                                                                   | Assignee          | Customer                          |
      | Title 1                                                                                 | John              | Customer 1                        |
      | Title 2                                                                                 | John              | Customer 2                        |
      | Title 3                                                                                 | John              | Customer 3                        |
      | Title 4                                                                                 |                   | Customer 4                        |
      | Title 5                                                                                 | John              | Customer 5                        |
      | Title 6                                                                                 | John              | Customer 6                        |
    And some Detail Provided:
      | Description                                                                             | Reproducer        | Expected Result                   |
      | Description 1                                                                           | Link 1            | Result 1                          |
      | Description 2                                                                           | Link 2            | Result 2                          |
      | Description 3                                                                           | Link 3            | Result 3                          |
      | Description 4                                                                           |                   | Result 4                          |
      | Description 5                                                                           | Link 4            | Result 5                          |
      | Description 6                                                                           | Link 5            | Result 6                          |
    When the case as been created
    Then I expect Next Detail:
      | Answer                | Type                   |
      | Update 1              | Resolution             |

