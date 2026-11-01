vault
======
set VAULT_ADDR=http://127.0.0.1:8200

vault server -dev --dev-root-token-id="18456350-7b89-11ed-85e9-a5830358e351"

vault kv put secret/pfmp jwt.secret=jwtSecretKey
