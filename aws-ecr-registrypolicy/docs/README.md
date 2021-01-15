# AWS::ECR::RegistryPolicy

The AWS::ECR::RegistryPolicy is used to specify permissions for another AWS account and is used when configuring cross-account replication. For more information, see Registry permissions in the Amazon Elastic Container Registry User Guide: https://docs.aws.amazon.com/AmazonECR/latest/userguide/registry-permissions.html

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::ECR::RegistryPolicy",
    "Properties" : {
        "<a href="#policytext" title="PolicyText">PolicyText</a>" : <i>Map</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::ECR::RegistryPolicy
Properties:
    <a href="#policytext" title="PolicyText">PolicyText</a>: <i>Map</i>
</pre>

## Properties

#### PolicyText

The JSON policy text to apply to your registry. The policy text follows the same format as IAM policy text. For more information, see Registry permissions (https://docs.aws.amazon.com/AmazonECR/latest/userguide/registry-permissions.html) in the Amazon Elastic Container Registry User Guide.

_Required_: Yes

_Type_: Map

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the RegistryId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### RegistryId

The registry id.
