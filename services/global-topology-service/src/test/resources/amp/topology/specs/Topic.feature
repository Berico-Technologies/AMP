Feature: Management of a Topic by the GTS

  Scenario:  Creation of a Topic

    Given there is no Topic called int.test.gts.topic.Topic1

    When I create a new Topic called int.test.gts.topic.Topic1 with a description: "Test topic"

    And I retrieve the Topic int.test.gts.topic.Topic1

    Then I should have 1 ProducingGroup for protocol AMQP

    And I should have 1 ConsumingGroup for protocol AMQP

    And I should have 1 Connector binding the only ProducingGroup to the only ConsumingGroup

    And the Topic should have a description of "Test topic"


  Scenario:  Deletion of a Topic

    Given a Topic called int.test.gts.topic.Topic2

    When I remove the Topic called int.test.gts.topic.Topic2

    Then there is no Topic called int.test.gts.topic.Topic2


  Scenario:  An error should occur if I attempt to retrieve a Topic that doesn't exist

    Given there is no Topic called int.test.gts.topic.Topic4

    When I retrieve the Topic int.test.gts.topic.Topic4

    Then I should receive a "Topic does not exist" error from the Topic manager.


  Scenario:  An error should occur if I attempt to remove a Topic that doesn't exist

    Given there is no Topic called int.test.gts.topic.Topic5

    When I remove the Topic called int.test.gts.topic.Topic5

    Then I should receive a "Topic does not exist" error from the Topic manager.