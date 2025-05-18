# ACM Certificate Validation Guide

## Understanding "Pending Validation" Status

When you create an AWS Certificate Manager (ACM) certificate, it initially enters a "pending validation" status. This means that AWS has created the certificate, but you need to prove that you own the domain before AWS will issue the certificate.

In your Terraform configuration, you have created an ACM certificate for `argocd.t-horie.com` using DNS validation:

```terraform
resource "aws_acm_certificate" "argocd" {
  domain_name       = "argocd.t-horie.com"
  validation_method = "DNS"
  
  tags = var.tags
  
  lifecycle {
    create_before_destroy = true
  }
}
```

## What to Check

When your ACM certificate is in "pending validation" status, you need to check the following:

1. **DNS Validation Records**: AWS creates DNS validation records that you need to add to your domain's DNS configuration.

2. **Domain Ownership**: You need to prove that you own the domain by adding these validation records.

## How to Check and Resolve

### 1. View the Certificate Details in AWS Console

1. Log in to the AWS Management Console
2. Navigate to AWS Certificate Manager (ACM)
3. Find your certificate for `argocd.t-horie.com`
4. Click on the certificate to view its details
5. In the "Domains" section, you'll see the validation status and the required DNS records

### 2. Get Validation Records via AWS CLI

You can also get the validation records using the AWS CLI:

```bash
aws acm describe-certificate --certificate-arn <your-certificate-arn>
```

Look for the `DomainValidationOptions` section in the output, which contains the CNAME records you need to create.

### 3. Add DNS Validation Records

To validate the certificate, you need to add the CNAME records to your domain's DNS configuration:

1. Log in to your DNS provider's management console (e.g., Route 53, GoDaddy, Namecheap)
2. Add the CNAME records provided by ACM
3. The CNAME record will look something like:
   - Name: `_<random-string>.argocd.t-horie.com`
   - Value: `_<random-string>.acm-validations.aws`

### 4. Wait for Validation

After adding the DNS records, it may take some time (usually 30 minutes to a few hours) for AWS to validate the certificate. During this time, the certificate will remain in "pending validation" status.

### 5. Check Validation Status

You can check the validation status using the AWS CLI:

```bash
aws acm describe-certificate --certificate-arn <your-certificate-arn> --query 'Certificate.Status'
```

Or by refreshing the certificate details page in the AWS Console.

## Using Terraform to Automate Validation

If you're using Route 53 for your DNS, you can automate the validation process by adding the following to your Terraform configuration:

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

Then update your ingress resource to depend on the validated certificate:

```terraform
resource "kubernetes_ingress_v1" "argocd" {
  # ... existing configuration ...
  
  depends_on = [
    helm_release.argocd,
    aws_acm_certificate_validation.cert
  ]
}
```

## Troubleshooting

If your certificate remains in "pending validation" status for an extended period, check the following:

1. **DNS Propagation**: DNS changes can take time to propagate. Use tools like `dig` or `nslookup` to verify that your DNS records are correctly set up:
   ```bash
   dig <CNAME-record-name> CNAME
   ```

2. **Correct Domain**: Ensure that the domain in your certificate matches the domain you're validating.

3. **DNS Provider Limitations**: Some DNS providers have specific requirements for CNAME records. Check your DNS provider's documentation.

4. **AWS Region**: Ensure that you're looking at the certificate in the correct AWS region.

## Conclusion

The "pending validation" status is a normal part of the ACM certificate issuance process. By following the steps above, you can validate your certificate and move it to the "issued" status, allowing you to use HTTPS with your ArgoCD installation.