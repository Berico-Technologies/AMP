Feature: Management of a ProducingGroup by the GTS

  Scenario: Add a ProducingGroup to an existing Topic

    Given a Topic called int.test.gts.pgroups.Topic1

    And a ProducingGroup identified as "Restricted Group Bravo" for protocol "AMQP"

    And the ProducingGroup has the following AccessControls:
      | allowedActors |        |
      | allowedGroups | admins |
      | deniedActors  | *      |
      | deniedGroups  |        |

    When the ProducingGroup is created on the Topic int.test.gts.pgroups.Topic1

    Then I should have 2 ProducingGroups for protocol AMQP

    And I should have 1 ConsumingGroup for protocol AMQP

    And I should have 1 Connector binding the default ProducingGroup to the only ConsumingGroup


