# Nginx Sample Deployment with AWS ALB

This directory contains Kubernetes manifests to deploy an Nginx web server and expose it to the internet via an AWS Application Load Balancer (ALB).

## Contents

- `nginx-alb.yaml`: Contains the Deployment, Service, and Ingress manifests for Nginx with ALB configuration
- `application.yaml`: ArgoCD Application manifest for deploying this sample using GitOps

## Prerequisites

- EKS cluster with the AWS Load Balancer Controller installed
- IAM roles for service accounts (IRSA) configured for the AWS Load Balancer Controller
- ArgoCD installed (optional, only required for GitOps-based deployment)

If you haven't installed the AWS Load Balancer Controller, you can do so using Helm:

```bash
# Add the EKS chart repo
helm repo add eks https://aws.github.io/eks-charts
helm repo update

# Install the AWS Load Balancer Controller
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=lean-saas-eks \
  --set serviceAccount.create=true \
  --set serviceAccount.name=aws-load-balancer-controller
```

## Deployment Details

- Deploys 2 replicas of Nginx
- Uses the nginx:1.21 image
- Configures resource requests and limits
- Sets up liveness and readiness probes for better reliability
- Exposes Nginx via an AWS Application Load Balancer using an Ingress resource

## How to Deploy

After your EKS cluster is up and running and the AWS Load Balancer Controller is installed, you can deploy this sample using kubectl:

```bash
# Configure kubectl to connect to your EKS cluster
aws eks update-kubeconfig --region us-west-2 --name lean-saas-eks

# Apply the manifest
kubectl apply -f nginx-alb.yaml

# Check the deployment status
kubectl get deployment nginx

# Check the service status
kubectl get service nginx

# Check the ingress status
kubectl get ingress nginx-ingress
```

## Deploying with ArgoCD

This sample can also be deployed using ArgoCD for GitOps-based continuous delivery:

### Option 1: Using Terraform (Recommended)

The Terraform configuration in this repository automatically registers this sample as an ArgoCD application. After applying the Terraform configuration, you can check the status of the application:

```bash
# Check the status of the application in ArgoCD
kubectl get application nginx-alb-sample -n argocd
```

### Option 2: Manual Deployment

You can also manually deploy this sample using the provided ArgoCD Application manifest:

```bash
# Apply the ArgoCD Application manifest
kubectl apply -f application.yaml
```

### Viewing in ArgoCD UI

1. Access the ArgoCD UI using the URL provided in the Terraform outputs
2. Log in with the admin credentials
3. Navigate to the "Applications" section
4. Click on the "nginx-alb-sample" application to view its status and details

## Accessing the Application

Once the ALB is provisioned (which might take a few minutes), you can access Nginx using the ALB's address:

```bash
# Get the ALB address
export ALB_ADDRESS=$(kubectl get ingress nginx-ingress -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

# Access Nginx
curl http://$ALB_ADDRESS
```

You can also open the ALB address in a web browser.

## Cleanup

### Option 1: Cleanup with kubectl

To remove the deployment, service, and ingress directly:

```bash
kubectl delete -f nginx-alb.yaml
```

### Option 2: Cleanup with ArgoCD

If you deployed the application using ArgoCD, you can remove it by deleting the ArgoCD Application:

```bash
# If deployed manually
kubectl delete -f application.yaml

# If deployed with Terraform
kubectl delete application nginx-alb-sample -n argocd
```

Note that it may take a few minutes for the ALB to be fully deleted from AWS.
