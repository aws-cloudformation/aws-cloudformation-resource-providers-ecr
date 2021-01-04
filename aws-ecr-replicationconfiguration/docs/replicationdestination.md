# AWS::ECR::ReplicationConfiguration ReplicationDestination

An array of objects representing the details of a replication destination.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#region" title="Region">Region</a>" : <i>String</i>,
    "<a href="#registryid" title="RegistryId">RegistryId</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#region" title="Region">Region</a>: <i>String</i>
<a href="#registryid" title="RegistryId">RegistryId</a>: <i>String</i>
</pre>

## Properties

#### Region

A Region to replicate to.

_Required_: Yes

_Type_: String

_Pattern_: <code>[0-z9a-z-]{2,25}</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RegistryId

The account ID of the destination registry to replicate to.

_Required_: Yes

_Type_: String

_Pattern_: <code>^[0-9]{12}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
