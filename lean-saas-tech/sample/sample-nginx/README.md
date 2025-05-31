# Nginx Sample Deployment

This directory contains Kubernetes manifests to deploy an Nginx web server and expose it to the internet via a LoadBalancer.

## Contents

- `nginx.yaml`: Contains both the Deployment and Service manifests for Nginx

## Deployment Details

- Deploys 2 replicas of Nginx
- Uses the nginx:1.21 image
- Configures resource requests and limits
- Sets up liveness and readiness probes for better reliability
- Exposes Nginx via an AWS Network Load Balancer

## How to Deploy

After your EKS cluster is up and running, you can deploy this sample using kubectl:

```bash
# Configure kubectl to connect to your EKS cluster
aws eks update-kubeconfig --region us-west-2 --name lean-saas-eks

# Apply the manifest
kubectl apply -f nginx.yaml

# Check the deployment status
kubectl get deployment nginx

# Check the service status and get the external IP/hostname
kubectl get service nginx
```

## Accessing the Application

Once the LoadBalancer is provisioned (which might take a few minutes), you can access Nginx using the external IP or hostname provided by the LoadBalancer:

```bash
# Get the external IP/hostname
export SERVICE_IP=$(kubectl get service nginx -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

# Access Nginx
curl http://$SERVICE_IP
```

## Cleanup

To remove the deployment and service:

```bash
kubectl delete -f nginx.yaml
```