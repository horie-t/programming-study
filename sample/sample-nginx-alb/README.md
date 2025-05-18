# Nginx Sample Deployment with AWS ALB

This directory contains Kubernetes manifests to deploy an Nginx web server and expose it to the internet via an AWS Application Load Balancer (ALB).

## Contents

- `nginx-alb.yaml`: Contains the Deployment, Service, and Ingress manifests for Nginx with ALB configuration

## Prerequisites

- EKS cluster with the AWS Load Balancer Controller installed
- IAM roles for service accounts (IRSA) configured for the AWS Load Balancer Controller

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

To remove the deployment, service, and ingress:

```bash
kubectl delete -f nginx-alb.yaml
```

Note that it may take a few minutes for the ALB to be fully deleted from AWS.