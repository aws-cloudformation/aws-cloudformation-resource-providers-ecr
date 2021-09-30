# AWS::ECR::ReplicationConfiguration RepositoryFilter

An array of objects representing the details of a repository filter.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#filter" title="Filter">Filter</a>" : <i>String</i>,
    "<a href="#filtertype" title="FilterType">FilterType</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#filter" title="Filter">Filter</a>: <i>String</i>
<a href="#filtertype" title="FilterType">FilterType</a>: <i>String</i>
</pre>

## Properties

#### Filter

The repository filter to be applied for replication.

_Required_: Yes

_Type_: String

_Pattern_: <code>^(?:[a-z0-9]+(?:[._-][a-z0-9]*)*/)*[a-z0-9]*(?:[._-][a-z0-9]*)*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### FilterType

Type of repository filter

_Required_: Yes

_Type_: String

_Allowed Values_: <code>PREFIX_MATCH</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

