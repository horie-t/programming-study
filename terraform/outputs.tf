# EKS Cluster Outputs
output "cluster_id" {
  description = "EKS cluster ID"
  value       = module.eks.cluster_id
}

output "cluster_endpoint" {
  description = "Endpoint for EKS control plane"
  value       = module.eks.cluster_endpoint
}

output "cluster_security_group_id" {
  description = "Security group ID attached to the EKS cluster"
  value       = module.eks.cluster_security_group_id
}

output "cluster_name" {
  description = "Kubernetes Cluster Name"
  value       = module.eks.cluster_name
}

output "cluster_certificate_authority_data" {
  description = "Base64 encoded certificate data required to communicate with the cluster"
  value       = module.eks.cluster_certificate_authority_data
  sensitive   = true
}

# Node Group Outputs
output "node_security_group_id" {
  description = "Security group ID attached to the EKS nodes"
  value       = aws_security_group.node_group_sg.id
}

# VPC Outputs
output "vpc_id" {
  description = "The ID of the VPC"
  value       = module.vpc.vpc_id
}

output "private_subnets" {
  description = "List of IDs of private subnets"
  value       = module.vpc.private_subnets
}

output "public_subnets" {
  description = "List of IDs of public subnets"
  value       = module.vpc.public_subnets
}

# Command to configure kubectl
output "configure_kubectl" {
  description = "Command to configure kubectl to connect to the EKS cluster"
  value       = "aws eks update-kubeconfig --region us-west-2 --name ${module.eks.cluster_name}"
}

# AWS Load Balancer Controller Outputs
output "aws_load_balancer_controller_role_arn" {
  description = "ARN of the IAM role for AWS Load Balancer Controller"
  value       = module.lb_controller_role.iam_role_arn
}

output "aws_load_balancer_controller_policy_arn" {
  description = "ARN of the IAM policy for AWS Load Balancer Controller"
  value       = aws_iam_policy.aws_load_balancer_controller.arn
}

output "check_aws_load_balancer_controller" {
  description = "Command to check the status of the AWS Load Balancer Controller"
  value       = "kubectl get deployment aws-load-balancer-controller -n kube-system"
}

# ArgoCD Outputs
output "argocd_namespace" {
  description = "Namespace where ArgoCD is installed"
  value       = kubernetes_namespace.argocd.metadata[0].name
}

output "argocd_server_service" {
  description = "Name of the ArgoCD server service"
  value       = "argocd-server"
}

output "argocd_ingress_name" {
  description = "Name of the ArgoCD ingress resource"
  value       = kubernetes_ingress_v1.argocd.metadata[0].name
}

output "argocd_url" {
  description = "URL to access ArgoCD"
  value       = "https://${aws_acm_certificate.argocd.domain_name}"
}

output "get_argocd_admin_password" {
  description = "Command to get the initial ArgoCD admin password"
  value       = "kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath='{.data.password}' | base64 -d"
}

output "check_argocd_status" {
  description = "Command to check the status of ArgoCD deployment"
  value       = "kubectl get pods -n argocd"
}

# Route53 Record for ArgoCD
output "argocd_route53_record" {
  description = "Route53 record for ArgoCD domain"
  value       = aws_route53_record.argocd.name
}

output "argocd_route53_record_type" {
  description = "Type of Route53 record for ArgoCD domain"
  value       = aws_route53_record.argocd.type
}

output "argocd_route53_record_target" {
  description = "Target of Route53 record for ArgoCD domain"
  value       = tolist(aws_route53_record.argocd.records)[0]
}
