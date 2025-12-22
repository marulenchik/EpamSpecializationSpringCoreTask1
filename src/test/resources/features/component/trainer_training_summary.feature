Feature: Update trainer training summary

  Scenario: Create a new trainer document when none exists
    Given a trainer training event with username "trainer1", first name "John", last name "Doe", status true, date "2025-05-10", duration 60
    And no existing trainer summary for username "trainer1"
    When the training event is processed
    Then the trainer summary is created with year 2025 month 5 total duration 60

  Scenario: Increment monthly duration for existing trainer
    Given a trainer training event with username "trainer2", first name "Jane", last name "Smith", status true, date "2025-05-15", duration 20
    And an existing trainer summary for username "trainer2" with year 2025 month 5 total duration 30
    When the training event is processed
    Then the trainer summary for year 2025 month 5 has total duration 50

