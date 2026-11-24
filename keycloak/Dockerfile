# Keycloak with the build-time options baked in, so it starts with --optimized (no rebuild
# on every start), and with the realm definition included for --import-realm.
FROM quay.io/keycloak/keycloak:26.8.1 AS builder

ENV KC_DB=postgres \
    KC_HEALTH_ENABLED=true \
    KC_HTTP_RELATIVE_PATH=/auth
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:26.8.1

COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY realm-framingham.json /opt/keycloak/data/import/

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
# --import-realm creates the realm on first start and leaves an existing one untouched.
CMD ["start", "--optimized", "--import-realm"]
