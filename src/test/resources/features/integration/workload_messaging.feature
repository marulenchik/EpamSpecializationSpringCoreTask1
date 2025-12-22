Feature: Workload messaging over ActiveMQ

  Scenario: Send workload update and receive it on the queue
    Given a workload update request for trainer "trainer-jms" first name "Alice" last name "Wong" active true date "2025-05-20" duration 45 action "ADD"
    And transaction id "txn-cuke-123"
    When the workload message is sent
    Then the queue receives a workload message for trainer "trainer-jms" with duration 45 and transaction id "txn-cuke-123"

