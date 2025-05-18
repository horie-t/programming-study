# Terraform Configuration for EKS Cluster with AWS Load Balancer Controller

This directory contains Terraform configuration files for deploying an Amazon EKS cluster with the AWS Load Balancer Controller.

## Components

- **EKS Cluster**: A managed Kubernetes cluster running on AWS
- **VPC**: A Virtual Private Cloud with public and private subnets
- **Node Group**: A group of EC2 instances running as Kubernetes nodes
- **AWS Load Balancer Controller**: A controller that manages AWS Elastic Load Balancers for Kubernetes services
- **ArgoCD**: A declarative, GitOps continuous delivery tool for Kubernetes

## AWS Load Balancer Controller

The AWS Load Balancer Controller is installed using Helm and configured with the necessary IAM permissions. The controller version is v2.11.0, which matches the IAM policy version.

### IAM Policy

The IAM policy for the AWS Load Balancer Controller is downloaded from the official AWS repository:
https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.11.0/docs/install/iam_policy.json

### Helm Chart

The AWS Load Balancer Controller is installed using the official Helm chart from the EKS charts repository. The chart is configured with the following settings:

- **Version**: 1.7.1 (compatible with controller v2.11.0)
- **Namespace**: kube-system
- **Service Account**: aws-load-balancer-controller
- **IAM Role**: Created with OIDC provider for EKS

## ArgoCD

ArgoCD is installed using Helm and configured to be accessible via HTTPS through an AWS Application Load Balancer (ALB).

### Helm Chart

ArgoCD is installed using the official Helm chart from the Argo Project repository. The chart is configured with the following settings:

- **Version**: 5.51.4
- **Namespace**: argocd
- **Service Type**: ClusterIP (accessed via ALB)
- **TLS**: Disabled on the server (TLS termination happens at the ALB)

### HTTPS Access

ArgoCD is exposed via an AWS Application Load Balancer with the following configuration:

- HTTPS enabled on port 443
- HTTP to HTTPS redirection
- TLS certificate from AWS Certificate Manager
- Internet-facing load balancer

### Route53 Configuration

ArgoCD is configured with a Route53 record that points to the ALB:

- **Domain**: argocd.t-horie.com
- **Record Type**: CNAME
- **Target**: The ALB hostname created by the AWS Load Balancer Controller

This allows you to access ArgoCD using a custom domain name instead of the ALB's default hostname.

### Accessing ArgoCD

After the deployment is complete, you can access ArgoCD using the following steps:

```bash
# Get the ArgoCD URL
echo "ArgoCD URL: $(terraform output -raw argocd_url)"

# Get the ArgoCD Route53 record
echo "ArgoCD Domain: $(terraform output -raw argocd_route53_record)"

# Get the initial admin password
$(terraform output -raw get_argocd_admin_password)

# Check the status of ArgoCD deployment
$(terraform output -raw check_argocd_status)
```

You can access ArgoCD using either the ALB hostname or the custom domain name (if DNS has propagated). The default username is `admin`. You should change the password after the first login.

## Usage

### Deployment

To deploy the EKS cluster with the AWS Load Balancer Controller:

```bash
# Initialize Terraform
terraform init

# Plan the deployment
terraform plan

# Apply the configuration
terraform apply
```

### Accessing the Cluster

After the deployment is complete, you can access the cluster using kubectl:

```bash
# Configure kubectl
$(terraform output -raw configure_kubectl)

# Verify the AWS Load Balancer Controller deployment
$(terraform output -raw check_aws_load_balancer_controller)
```

### Using the AWS Load Balancer Controller

The AWS Load Balancer Controller allows you to create AWS Application Load Balancers (ALBs) and Network Load Balancers (NLBs) for your Kubernetes services.

#### Example Ingress for ALB

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: example-ingress
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internet-facing
spec:
  rules:
    - http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: example-service
                port:
                  number: 80
```

#### Example Service for NLB

```yaml
apiVersion: v1
kind: Service
metadata:
  name: example-service
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-type: nlb
spec:
  type: LoadBalancer
  ports:
    - port: 80
      targetPort: 80
  selector:
    app: example
```

## Cleanup

To destroy the EKS cluster and all associated resources:

```bash
terraform destroy
```

Note that this will delete all resources created by Terraform, including the EKS cluster, VPC, AWS Load Balancer Controller, and ArgoCD.
