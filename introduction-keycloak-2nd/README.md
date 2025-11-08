# 『認証と認可 Keycloak入門 第2版』を読んだメモ

## KeycloakのDockerでの起動

```bash
docker run -p 8080:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=password quay.io/keycloak/keycloak:26.0.0 start-dev
```