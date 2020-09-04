# AWS::ECR::Repository

The AWS::ECR::Repository resource specifies an Amazon Elastic Container Registry (Amazon ECR) repository, where users can push and pull Docker images. For more information, see https://docs.aws.amazon.com/AmazonECR/latest/userguide/Repositories.html

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::ECR::Repository",
    "Properties" : {
        "<a href="#lifecyclepolicy" title="LifecyclePolicy">LifecyclePolicy</a>" : <i><a href="lifecyclepolicy.md">LifecyclePolicy</a></i>,
        "<a href="#repositoryname" title="RepositoryName">RepositoryName</a>" : <i>String</i>,
        "<a href="#repositorypolicytext" title="RepositoryPolicyText">RepositoryPolicyText</a>" : <i>Map, String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#imagetagmutability" title="ImageTagMutability">ImageTagMutability</a>" : <i>String</i>,
        "<a href="#imagescanningconfiguration" title="ImageScanningConfiguration">ImageScanningConfiguration</a>" : <i><a href="imagescanningconfiguration.md">ImageScanningConfiguration</a></i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::ECR::Repository
Properties:
    <a href="#lifecyclepolicy" title="LifecyclePolicy">LifecyclePolicy</a>: <i><a href="lifecyclepolicy.md">LifecyclePolicy</a></i>
    <a href="#repositoryname" title="RepositoryName">RepositoryName</a>: <i>String</i>
    <a href="#repositorypolicytext" title="RepositoryPolicyText">RepositoryPolicyText</a>: <i>Map, String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#imagetagmutability" title="ImageTagMutability">ImageTagMutability</a>: <i>String</i>
    <a href="#imagescanningconfiguration" title="ImageScanningConfiguration">ImageScanningConfiguration</a>: <i><a href="imagescanningconfiguration.md">ImageScanningConfiguration</a></i>
</pre>

## Properties

#### LifecyclePolicy

The LifecyclePolicy property type specifies a lifecycle policy. For information about lifecycle policy syntax, see https://docs.aws.amazon.com/AmazonECR/latest/userguide/LifecyclePolicies.html

_Required_: No

_Type_: <a href="lifecyclepolicy.md">LifecyclePolicy</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RepositoryName

The name to use for the repository. The repository name may be specified on its own (such as nginx-web-app) or it can be prepended with a namespace to group the repository into a category (such as project-a/nginx-web-app). If you don't specify a name, AWS CloudFormation generates a unique physical ID and uses that ID for the repository name. For more information, see https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-name.html.

_Required_: No

_Type_: String

_Minimum_: <code>2</code>

_Maximum_: <code>256</code>

_Pattern_: <code>^(?=.{2,256}$)((?:[a-z0-9]+(?:[._-][a-z0-9]+)*/)*[a-z0-9]+(?:[._-][a-z0-9]+)*)$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### RepositoryPolicyText

The JSON repository policy text to apply to the repository. For more information, see https://docs.aws.amazon.com/AmazonECR/latest/userguide/RepositoryPolicyExamples.html in the Amazon Elastic Container Registry User Guide. 

_Required_: No

_Type_: Map, String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

An array of key-value pairs to apply to this resource.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ImageTagMutability

The image tag mutability setting for the repository.

_Required_: No

_Type_: String

_Allowed Values_: <code>MUTABLE</code> | <code>IMMUTABLE</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ImageScanningConfiguration

The image scanning configuration for the repository. This setting determines whether images are scanned for known vulnerabilities after being pushed to the repository.

_Required_: No

_Type_: <a href="imagescanningconfiguration.md">ImageScanningConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the RepositoryName.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Arn

Returns the <code>Arn</code> value.

