# Admin Console K8s Config (GitOps Repository)

Repository này chứa Kubernetes deployment configuration cho **admin-console-backend**, sử dụng:
- **Helm Chart** để quản lý K8s resources
- **ArgoCD** để tự động deploy (GitOps)

## Cấu trúc

```
├── charts/
│   └── admin-console-backend/
│       ├── Chart.yaml
│       ├── values.yaml
│       └── templates/
│           ├── _helpers.tpl
│           ├── deployment.yaml
│           ├── service.yaml
│           ├── configmap.yaml
│           ├── secret.yaml
│           ├── hpa.yaml
│           ├── ingress.yaml
│           └── serviceaccount.yaml
└── argocd/
    └── application.yaml
```

## Quick Start

```bash
# Lint chart
helm lint charts/admin-console-backend/

# Template preview
helm template my-release charts/admin-console-backend/

# Install manually (without ArgoCD)
helm install admin-console-backend charts/admin-console-backend/ -n admin-console --create-namespace

# Apply ArgoCD Application
kubectl apply -f argocd/application.yaml
```
