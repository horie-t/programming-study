# Terraform Examples

This directory contains example Terraform configurations that demonstrate how to implement specific features or solve common problems in the infrastructure.

## Available Examples

### ACM Certificate Validation

The `acm_validation.tf` file demonstrates how to automate the validation of AWS Certificate Manager (ACM) certificates using Route 53 DNS records.

#### Key Features:
- Creates an ACM certificate for a specified domain
- Automatically creates the required DNS validation records in Route 53
- Validates the certificate
- Shows how to use the validated certificate with an ALB listener

#### Usage:
1. Modify the domain names and ARNs to match your environment
2. Apply the configuration using Terraform
3. The certificate will be automatically validated if you have access to the Route 53 hosted zone

## How to Use These Examples

These examples are meant to be educational and to provide templates that you can adapt for your specific needs. They are not intended to be used directly in production without modification.

To use an example:

1. Review the example code and comments
2. Copy the relevant parts to your own Terraform configuration
3. Modify the code to match your specific requirements
4. Test thoroughly before deploying to production

## Related Documentation

For more information about ACM certificate validation, see the [ACM Certificate Validation Guide](../../docs/acm_certificate_validation.md) in the docs directory.