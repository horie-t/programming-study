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
