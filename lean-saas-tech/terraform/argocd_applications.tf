# ArgoCD Applications

# Register sample/sample-nginx-alb as an ArgoCD application
resource "kubernetes_manifest" "nginx_alb_application" {
  manifest = {
    apiVersion = "argoproj.io/v1alpha1"
    kind       = "Application"
    metadata = {
      name      = "nginx-alb-sample"
      namespace = "argocd"
    }
    spec = {
      project = "default"
      source = {
        repoURL        = "https://github.com/horie-t/argocd-repo"  # Private repository URL
        targetRevision = "HEAD"
        path           = "private-app"
      }
      destination = {
        server    = "https://kubernetes.default.svc"
        namespace = "default"
      }
      syncPolicy = {
        automated = {
          prune    = true
          selfHeal = true
        }
        syncOptions = ["CreateNamespace=true"]
      }
    }
  }

  depends_on = [
    helm_release.argocd
  ]
}

# Output for the ArgoCD application
output "argocd_nginx_alb_application_name" {
  description = "Name of the ArgoCD application for Nginx ALB"
  value       = kubernetes_manifest.nginx_alb_application.manifest.metadata.name
}

output "argocd_nginx_alb_application_path" {
  description = "Path to the Nginx ALB manifests in the repository"
  value       = kubernetes_manifest.nginx_alb_application.manifest.spec.source.path
}

output "check_argocd_nginx_alb_application" {
  description = "Command to check the status of the Nginx ALB application in ArgoCD"
  value       = "kubectl get application nginx-alb-sample -n argocd"
}
