# AWS::ECR::ReplicationConfiguration

The AWS::ECR::ReplicationConfiguration resource configures the replication destinations for an Amazon Elastic Container Registry (Amazon Private ECR). For more information, see https://docs.aws.amazon.com/AmazonECR/latest/userguide/replication.html

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::ECR::ReplicationConfiguration",
    "Properties" : {
        "<a href="#replicationconfiguration" title="ReplicationConfiguration">ReplicationConfiguration</a>" : <i><a href="replicationconfiguration.md">ReplicationConfiguration</a></i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::ECR::ReplicationConfiguration
Properties:
    <a href="#replicationconfiguration" title="ReplicationConfiguration">ReplicationConfiguration</a>: <i><a href="replicationconfiguration.md">ReplicationConfiguration</a></i>
</pre>

## Properties

#### ReplicationConfiguration

An object representing the replication configuration for a registry.

_Required_: Yes

_Type_: <a href="replicationconfiguration.md">ReplicationConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the RegistryId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### RegistryId

The RegistryId associated with the aws account.

