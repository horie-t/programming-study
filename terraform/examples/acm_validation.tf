# Example Terraform configuration for automating ACM certificate validation
# This file demonstrates how to validate an ACM certificate using Route 53

# Get the hosted zone for your domain
data "aws_route53_zone" "domain" {
  name         = "t-horie.com"  # Replace with your domain
  private_zone = false
}

# Create an ACM certificate
resource "aws_acm_certificate" "cert" {
  domain_name       = "argocd.t-horie.com"  # Replace with your subdomain
  validation_method = "DNS"
  
  lifecycle {
    create_before_destroy = true
  }
  
  tags = {
    Name        = "ArgoCD Certificate"
    Environment = "Production"
  }
}

# Create DNS records for certificate validation
resource "aws_route53_record" "cert_validation" {
  for_each = {
    for dvo in aws_acm_certificate.cert.domain_validation_options : dvo.domain_name => {
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
  zone_id         = data.aws_route53_zone.domain.zone_id
}

# Validate the certificate
resource "aws_acm_certificate_validation" "cert" {
  certificate_arn         = aws_acm_certificate.cert.arn
  validation_record_fqdns = [for record in aws_route53_record.cert_validation : record.fqdn]
}

# Use the validated certificate in an ALB listener
resource "aws_lb_listener" "https" {
  # This is just an example - you would reference your actual ALB ARN
  # load_balancer_arn = aws_lb.example.arn
  load_balancer_arn = "arn:aws:elasticloadbalancing:us-west-2:123456789012:loadbalancer/app/example/abcdef123456"
  port              = "443"
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = aws_acm_certificate_validation.cert.certificate_arn

  default_action {
    type             = "forward"
    target_group_arn = "arn:aws:elasticloadbalancing:us-west-2:123456789012:targetgroup/example/abcdef123456"
  }
}

# Output the certificate ARN and status
output "certificate_arn" {
  value = aws_acm_certificate.cert.arn
}

output "certificate_status" {
  value = aws_acm_certificate.cert.status
}

output "domain_validation_options" {
  value = aws_acm_certificate.cert.domain_validation_options
  sensitive = true
}

# Instructions for applying this configuration
# 1. Replace the domain names and ARNs with your actual values
# 2. Initialize Terraform: terraform init
# 3. Apply the configuration: terraform apply
# 4. The certificate will be automatically validated if you have access to the Route 53 hosted zone