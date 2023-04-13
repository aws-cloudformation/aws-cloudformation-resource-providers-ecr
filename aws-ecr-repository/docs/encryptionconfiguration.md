# AWS::ECR::Repository EncryptionConfiguration

The encryption configuration for the repository. This determines how the contents of your repository are encrypted at rest.

By default, when no encryption configuration is set or the AES256 encryption type is used, Amazon ECR uses server-side encryption with Amazon S3-managed encryption keys which encrypts your data at rest using an AES-256 encryption algorithm. This does not require any action on your part.

For more information, see https://docs.aws.amazon.com/AmazonECR/latest/userguide/encryption-at-rest.html

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#encryptiontype" title="EncryptionType">EncryptionType</a>" : <i>String</i>,
    "<a href="#kmskey" title="KmsKey">KmsKey</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#encryptiontype" title="EncryptionType">EncryptionType</a>: <i>String</i>
<a href="#kmskey" title="KmsKey">KmsKey</a>: <i>String</i>
</pre>

## Properties

#### EncryptionType

The encryption type to use.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>AES256</code> | <code>KMS</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### KmsKey

If you use the KMS encryption type, specify the CMK to use for encryption. The alias, key ID, or full ARN of the CMK can be specified. The key must exist in the same Region as the repository. If no key is specified, the default AWS managed CMK for Amazon ECR will be used.

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>2048</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

