# AWS::ECR::Repository LifecyclePolicy

The LifecyclePolicy property type specifies a lifecycle policy. For information about lifecycle policy syntax, see https://docs.aws.amazon.com/AmazonECR/latest/userguide/LifecyclePolicies.html

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#lifecyclepolicytext" title="LifecyclePolicyText">LifecyclePolicyText</a>" : <i>String</i>,
    "<a href="#registryid" title="RegistryId">RegistryId</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#lifecyclepolicytext" title="LifecyclePolicyText">LifecyclePolicyText</a>: <i>String</i>
<a href="#registryid" title="RegistryId">RegistryId</a>: <i>String</i>
</pre>

## Properties

#### LifecyclePolicyText

The JSON repository policy text to apply to the repository.

_Required_: No

_Type_: String

_Minimum_: <code>100</code>

_Maximum_: <code>30720</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RegistryId

The AWS account ID associated with the registry that contains the repository. If you do not specify a registry, the default registry is assumed.

_Required_: No

_Type_: String

_Minimum_: <code>12</code>

_Maximum_: <code>12</code>

_Pattern_: <code>^[0-9]{12}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
