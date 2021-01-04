# AWS::ECR::ReplicationConfiguration ReplicationConfiguration

An object representing the replication configuration for a registry.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#rules" title="Rules">Rules</a>" : <i>[ <a href="replicationrule.md">ReplicationRule</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#rules" title="Rules">Rules</a>: <i>
      - <a href="replicationrule.md">ReplicationRule</a></i>
</pre>

## Properties

#### Rules

An array of objects representing the replication rules for a replication configuration. A replication configuration may contain only one replication rule but the rule may contain one or more replication destinations.

_Required_: No

_Type_: List of <a href="replicationrule.md">ReplicationRule</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
