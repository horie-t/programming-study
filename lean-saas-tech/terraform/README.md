# Terraform Configuration for EKS Cluster with AWS Load Balancer Controller

This directory contains Terraform configuration files for deploying an Amazon EKS cluster with the AWS Load Balancer Controller.

## Components

- **EKS Cluster**: A managed Kubernetes cluster running on AWS
- **VPC**: A Virtual Private Cloud with public and private subnets
- **Node Group**: A group of EC2 instances running as Kubernetes nodes
- **AWS Load Balancer Controller**: A controller that manages AWS Elastic Load Balancers for Kubernetes services
- **ArgoCD**: A declarative, GitOps continuous delivery tool for Kubernetes
- **ECR Repository**: A private Docker container registry for storing and managing container images

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

### Private Repository Access

ArgoCD is configured to access a private GitHub repository using a personal access token. The repository credentials are stored in a Kubernetes secret and used by ArgoCD to authenticate with GitHub.

- **Repository URL**: https://github.com/horie-t/argocd-repo
- **Authentication**: GitHub personal access token
- **Secret Name**: argocd-github-repo

### Pre-configured Applications

ArgoCD is configured with the following applications:

- **Nginx ALB Sample**: A sample application that deploys Nginx with an AWS Application Load Balancer
  - **Repository**: https://github.com/horie-t/argocd-repo (private repository)
  - **Path**: sample/sample-nginx-alb
  - **Namespace**: default
  - **Sync Policy**: Automated with pruning and self-healing

You can check the status of the pre-configured applications using the following commands:

```bash
# Check the status of the Nginx ALB application
$(terraform output -raw check_argocd_nginx_alb_application)
```

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

## ECR Repository

An Amazon Elastic Container Registry (ECR) repository is created to store Docker container images for your applications. The repository is configured with the following settings:

- **Name**: Based on the EKS cluster name (e.g., `lean-saas-eks-app-repository`)
- **Image Scanning**: Enabled on push
- **Image Tag Mutability**: Mutable (allows overwriting of image tags)
- **Lifecycle Policy**: Keeps the last 10 images and expires older ones

### IAM Permissions

The EKS node group is configured with the necessary IAM permissions to access the ECR repository. This allows pods running on the EKS cluster to pull images from the repository without additional configuration.

The IAM policy grants the following permissions:
- Pull images from the repository
- Push images to the repository
- Manage repository settings and images

### Using the ECR Repository

After the deployment is complete, you can use the ECR repository with the following steps:

```bash
# Get the ECR repository URL
echo "ECR Repository URL: $(terraform output -raw ecr_repository_url)"

# Authenticate Docker with ECR
$(terraform output -raw docker_login_command)

# Build and tag your Docker image
docker build -t your-app:latest .

# Push your Docker image to ECR
docker tag your-app:latest $(terraform output -raw ecr_repository_url):latest
docker push $(terraform output -raw ecr_repository_url):latest
```

### Using ECR Images in Kubernetes

To use images from your ECR repository in Kubernetes deployments, specify the full repository URL in your container image:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: your-app
spec:
  replicas: 2
  selector:
    matchLabels:
      app: your-app
  template:
    metadata:
      labels:
        app: your-app
    spec:
      containers:
      - name: your-app
        image: 123456789012.dkr.ecr.us-west-2.amazonaws.com/lean-saas-eks-app-repository:latest  # Replace with your actual ECR repository URL
        ports:
        - containerPort: 80
```

## Usage

### Deployment

To deploy the EKS cluster with the AWS Load Balancer Controller:

1. Create a GitHub personal access token with `repo` scope to access the private repository.

2. Set the GitHub token as an environment variable or in a `.tfvars` file:

   Using environment variable:
   ```bash
   export TF_VAR_github_token="your-github-token"
   ```

   Or create a `terraform.tfvars` file:
   ```
   github_token = "your-github-token"
   ```

3. Deploy the infrastructure:

   ```bash
   # Initialize Terraform
   terraform init

   # Plan the deployment
   terraform plan

   # Apply the configuration
   terraform apply
   ```

   If you're using the environment variable method:
   ```bash
   terraform apply -var="github_token=your-github-token"
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
