# AWS::ECR::Repository ImageScanningConfiguration

The image scanning configuration for the repository. This setting determines whether images are scanned for known vulnerabilities after being pushed to the repository.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#scanonpush" title="ScanOnPush">ScanOnPush</a>" : <i>Boolean</i>
}
</pre>

### YAML

<pre>
<a href="#scanonpush" title="ScanOnPush">ScanOnPush</a>: <i>Boolean</i>
</pre>

## Properties

#### ScanOnPush

The setting that determines whether images are scanned after being pushed to a repository.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

