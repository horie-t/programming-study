# AWS Load Balancer Controller Installation

# IAM Policy for AWS Load Balancer Controller
data "http" "aws_load_balancer_controller_policy_json" {
  url = "https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.11.0/docs/install/iam_policy.json"
}

resource "aws_iam_policy" "aws_load_balancer_controller" {
  name        = "AWSLoadBalancerControllerIAMPolicy"
  description = "IAM policy for AWS Load Balancer Controller"
  policy      = data.http.aws_load_balancer_controller_policy_json.body
}

# IAM Role for AWS Load Balancer Controller
module "lb_controller_role" {
  source = "terraform-aws-modules/iam/aws//modules/iam-assumable-role-with-oidc"
  version = "~> 5.55.0"

  create_role = true
  role_name   = "aws-load-balancer-controller"

  provider_url = replace(module.eks.cluster_oidc_issuer_url, "https://", "")

  role_policy_arns = [
    aws_iam_policy.aws_load_balancer_controller.arn
  ]

  oidc_fully_qualified_subjects = [
    "system:serviceaccount:kube-system:aws-load-balancer-controller"
  ]
}

# Helm Release for AWS Load Balancer Controller
resource "helm_release" "aws_load_balancer_controller" {
  name       = "aws-load-balancer-controller"
  repository = "https://aws.github.io/eks-charts"
  chart      = "aws-load-balancer-controller"
  namespace  = "kube-system"
  version    = "1.13.2"  # Specify the chart version compatible with controller v2.11.0

  depends_on = [
    aws_iam_policy.aws_load_balancer_controller,
    module.lb_controller_role
  ]

  set {
    name  = "clusterName"
    value = var.cluster_name
  }

  set {
    name  = "serviceAccount.create"
    value = "true"
  }

  set {
    name  = "serviceAccount.name"
    value = "aws-load-balancer-controller"
  }

  set {
    name  = "serviceAccount.annotations.eks\\.amazonaws\\.com/role-arn"
    value = module.lb_controller_role.iam_role_arn
  }

  # Specify the controller version to match the IAM policy version
  set {
    name  = "image.tag"
    value = "v2.11.0"
  }

  # Enable webhook
  set {
    name  = "enableCertManager"
    value = "false"
  }

  set {
    name  = "webhookCert.create"
    value = "true"
  }

  # Region configuration
  set {
    name  = "region"
    value = "us-west-2"
  }

  set {
    name  = "vpcId"
    value = module.vpc.vpc_id
  }
}
