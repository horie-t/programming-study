# What to Check When ACM Certificate Status is "Pending Validation"

## Quick Answer

When your AWS Certificate Manager (ACM) certificate status is "pending validation", you need to check:

1. **DNS Validation Records**: You need to add specific DNS records to prove domain ownership.
2. **DNS Configuration**: Ensure your DNS provider is correctly configured to add these records.
3. **Validation Method**: Confirm you're using the appropriate validation method (DNS or Email).
4. **Domain Ownership**: Verify you have access to manage DNS for the domain.

## Detailed Steps to Resolve

### 1. Check the Required Validation Records

For the ArgoCD certificate (`argocd.t-horie.com`), you need to:

```bash
# Get the certificate details
aws acm describe-certificate --certificate-arn $(terraform output -raw argocd_certificate_arn)
```

Look for the `DomainValidationOptions` section, which contains the CNAME records you need to create.

### 2. Add the DNS Records

Add the CNAME records to your DNS provider:

- If using Route 53:
  1. Go to Route 53 in the AWS Console
  2. Select your hosted zone
  3. Create a new record with the name and value from the validation options

- If using another DNS provider:
  1. Log in to your DNS provider's management console
  2. Add a CNAME record with the name and value from the validation options

### 3. Automate with Terraform

If you're using Route 53, you can automate this process by adding the following to your Terraform configuration:

```terraform
resource "aws_route53_record" "cert_validation" {
  for_each = {
    for dvo in aws_acm_certificate.argocd.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  }

  allow_overwrite = true
  name            = each.value.name
  records         = [each.value.record]
  ttl             = 60
  type            = each.value.type
  zone_id         = "<your-route53-zone-id>"
}

resource "aws_acm_certificate_validation" "cert" {
  certificate_arn         = aws_acm_certificate.argocd.arn
  validation_record_fqdns = [for record in aws_route53_record.cert_validation : record.fqdn]
}
```

### 4. Wait for Validation

After adding the DNS records, it may take some time (usually 30 minutes to a few hours) for AWS to validate the certificate.

### 5. Verify the Status

Check the status again:

```bash
aws acm describe-certificate --certificate-arn $(terraform output -raw argocd_certificate_arn) --query 'Certificate.Status'
```

## Additional Resources

For more detailed information, see:

- [ACM Certificate Validation Guide](acm_certificate_validation.md)
- [Terraform Example for ACM Validation](../terraform/examples/acm_validation.tf)
- [AWS Documentation on Certificate Validation](https://docs.aws.amazon.com/acm/latest/userguide/dns-validation.html)

## Conclusion

The "pending validation" status is a normal part of the ACM certificate issuance process. By adding the required DNS validation records, you can prove domain ownership and AWS will issue the certificate, changing its status from "pending validation" to "issued".