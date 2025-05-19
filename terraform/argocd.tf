# ArgoCD Installation

# Create a namespace for ArgoCD
resource "kubernetes_namespace" "argocd" {
  metadata {
    name = "argocd"
  }
}

# Create a Kubernetes secret for GitHub repository credentials
resource "kubernetes_secret" "argocd_github_repo" {
  metadata {
    name      = "argocd-github-repo"
    namespace = kubernetes_namespace.argocd.metadata[0].name
    labels = {
      "argocd.argoproj.io/secret-type" = "repository"
    }
  }

  data = {
    type          = "git"
    url           = "https://github.com/horie-t/argocd-repo"
    username      = "horie-t"
    password      = var.github_token
  }

  depends_on = [
    kubernetes_namespace.argocd
  ]
}

# Helm Release for ArgoCD
resource "helm_release" "argocd" {
  name       = "argocd"
  repository = "https://argoproj.github.io/argo-helm"
  chart      = "argo-cd"
  namespace  = kubernetes_namespace.argocd.metadata[0].name
  version    = "8.0.3"  # Specify a stable version of the chart

  depends_on = [
    helm_release.aws_load_balancer_controller,
    kubernetes_secret.argocd_github_repo
  ]

  # Set ArgoCD server to be accessible via LoadBalancer
  set {
    name  = "server.service.type"
    value = "ClusterIP"
  }

  # Disable TLS on the server as we'll terminate TLS at the ALB
  set {
    name  = "server.extraArgs[0]"
    value = "--insecure"
  }

  # Configure resources for ArgoCD server
  set {
    name  = "server.resources.limits.cpu"
    value = "300m"
  }

  set {
    name  = "server.resources.limits.memory"
    value = "512Mi"
  }

  set {
    name  = "server.resources.requests.cpu"
    value = "100m"
  }

  set {
    name  = "server.resources.requests.memory"
    value = "256Mi"
  }

  # Enable metrics for monitoring
  set {
    name  = "server.metrics.enabled"
    value = "true"
  }
}

# Create an ACM certificate for HTTPS
resource "aws_acm_certificate" "argocd" {
  domain_name       = "argocd.t-horie.com"  # Replace with your actual domain
  validation_method = "DNS"

  tags = var.tags

  lifecycle {
    create_before_destroy = true
  }
}

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
  zone_id         = "Z0604985M5PXV7QMVBX4"
}

resource "aws_acm_certificate_validation" "cert" {
  certificate_arn         = aws_acm_certificate.argocd.arn
  validation_record_fqdns = [for record in aws_route53_record.cert_validation : record.fqdn]
}

# Create a Route53 record for the ArgoCD domain
resource "aws_route53_record" "argocd" {
  zone_id = "Z0604985M5PXV7QMVBX4"  # Same zone_id as used for certificate validation
  name    = aws_acm_certificate.argocd.domain_name
  type    = "CNAME"
  ttl     = 300

  # This is a placeholder. In a real-world scenario, you would need to get the ALB hostname dynamically.
  # For now, we're using a pattern that matches how AWS ALB Controller typically names the ALB.
  # You should replace this with the actual ALB hostname after the ALB is created.
  records = ["k8s-argocdalbgroup-dfd66c57f7-418058172.us-west-2.elb.amazonaws.com"]

  depends_on = [
    kubernetes_ingress_v1.argocd,
    aws_acm_certificate_validation.cert
  ]
}

# Create an Ingress resource for ArgoCD
resource "kubernetes_ingress_v1" "argocd" {
  metadata {
    name      = "argocd-ingress"
    namespace = kubernetes_namespace.argocd.metadata[0].name
    annotations = {
      "kubernetes.io/ingress.class"                    = "alb"
      "alb.ingress.kubernetes.io/scheme"               = "internet-facing"
      "alb.ingress.kubernetes.io/target-type"          = "ip"
      "alb.ingress.kubernetes.io/healthcheck-protocol" = "HTTP"
      "alb.ingress.kubernetes.io/healthcheck-port"     = "traffic-port"
      "alb.ingress.kubernetes.io/healthcheck-path"     = "/"
      "alb.ingress.kubernetes.io/listen-ports"         = "[{\"HTTP\": 80}, {\"HTTPS\": 443}]"
      "alb.ingress.kubernetes.io/ssl-redirect"         = "443"
      "alb.ingress.kubernetes.io/certificate-arn"      = aws_acm_certificate.argocd.arn
      "alb.ingress.kubernetes.io/group.name"           = "argocd-alb-group"
    }
  }

  spec {
    rule {
      http {
        path {
          path      = "/"
          path_type = "Prefix"
          backend {
            service {
              name = "argocd-server"
              port {
                number = 80
              }
            }
          }
        }
      }
    }
  }

  depends_on = [
    helm_release.argocd
  ]
}
