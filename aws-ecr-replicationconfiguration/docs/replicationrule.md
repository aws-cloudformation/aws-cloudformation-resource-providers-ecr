# AWS::ECR::ReplicationConfiguration ReplicationRule

An array of objects representing the details of a replication destination.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#repositoryfilters" title="RepositoryFilters">RepositoryFilters</a>" : <i>[ <a href="repositoryfilter.md">RepositoryFilter</a>, ... ]</i>,
    "<a href="#destinations" title="Destinations">Destinations</a>" : <i>[ <a href="replicationdestination.md">ReplicationDestination</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#repositoryfilters" title="RepositoryFilters">RepositoryFilters</a>: <i>
      - <a href="repositoryfilter.md">RepositoryFilter</a></i>
<a href="#destinations" title="Destinations">Destinations</a>: <i>
      - <a href="replicationdestination.md">ReplicationDestination</a></i>
</pre>

## Properties

#### RepositoryFilters

An array of objects representing the details of a repository filter.

_Required_: No

_Type_: List of <a href="repositoryfilter.md">RepositoryFilter</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Destinations

An array of objects representing the details of a replication destination.

_Required_: Yes

_Type_: List of <a href="replicationdestination.md">ReplicationDestination</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

