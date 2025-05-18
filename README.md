# リーンなSaaSサービス開発を支える技術 (Technology Supporting Lean SaaS Service Development)

This repository contains infrastructure as code (IaC) and sample applications for deploying a Kubernetes-based platform on AWS EKS, designed to support lean SaaS service development.

## Repository Structure

- **terraform/**: Terraform configuration for deploying an EKS cluster with essential components
  - AWS EKS Cluster
  - VPC and networking
  - AWS Load Balancer Controller
  - ArgoCD (GitOps continuous delivery tool)

- **sample/**: Sample applications and configurations
  - **sample-nginx/**: Basic Nginx deployment with LoadBalancer service
  - **sample-nginx-alb/**: Nginx deployment with AWS ALB ingress
  - **sample-argocd/**: Sample ArgoCD application manifest

## Getting Started

1. Deploy the infrastructure using Terraform:
   ```bash
   cd terraform
   terraform init
   terraform apply
   ```

2. Configure kubectl to connect to your EKS cluster:
   ```bash
   aws eks update-kubeconfig --region us-west-2 --name lean-saas-eks
   ```

3. Access ArgoCD:
   - Get the URL and credentials from Terraform outputs
   - Use ArgoCD to deploy applications using GitOps

## Documentation

Each directory contains its own README.md with detailed documentation:

- [Terraform Configuration](terraform/README.md)
- [Nginx Sample with LoadBalancer](sample/sample-nginx/README.md)
- [Nginx Sample with ALB](sample/sample-nginx-alb/README.md)
- [ArgoCD Sample Application](sample/sample-argocd/README.md)
