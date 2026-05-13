#!/bin/bash

# Test push FCM via Vercel en recuperant les tokens depuis Supabase
# Usage:
#   ./test-vercel-push-from-supabase.sh [VERCEL_URL]
# Ex:
#   ./test-vercel-push-from-supabase.sh https://flood-alert-lambdav1.vercel.app
#
# Variables optionnelles:
#   SUPABASE_URL, SUPABASE_KEY
#   PUSH_TITLE, PUSH_BODY

set -euo pipefail

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

if ! command -v jq >/dev/null 2>&1; then
  echo -e "${RED}jq est requis mais non installe.${NC}"
  exit 1
fi

VERCEL_URL="${1:-${VERCEL_URL:-https://flood-alert-lambdav1.vercel.app}}"
SUPABASE_URL="${SUPABASE_URL:-https://ruohxnngiorrfptgkfll.supabase.co}"
SUPABASE_KEY="${SUPABASE_KEY:-}"
PUSH_TITLE="${PUSH_TITLE:-Alerte Inondation - Bamako}"
PUSH_BODY="${PUSH_BODY:-Risque eleve d inondation detecte. Eloignez-vous des zones inondables et suivez les consignes de securite.}"

if [ -z "$SUPABASE_KEY" ]; then
  echo -e "${RED}SUPABASE_KEY manquante.${NC}"
  echo -e "${YELLOW}Execute: export SUPABASE_KEY=votre_cle_supabase${NC}"
  exit 1
fi

echo -e "${BLUE}TEST PUSH DEPUIS SUPABASE${NC}"
echo -e "${BLUE}=========================${NC}"
echo -e "${YELLOW}Supabase:${NC} $SUPABASE_URL"
echo -e "${YELLOW}Vercel:${NC} $VERCEL_URL"
echo

echo -e "${BLUE}1) Recuperation des tokens FCM actifs...${NC}"
TOKENS_RESPONSE=$(curl -s -X GET \
  "$SUPABASE_URL/rest/v1/fcm_tokens?select=fcm_token&is_active=eq.true" \
  -H "apikey: $SUPABASE_KEY" \
  -H "Authorization: Bearer $SUPABASE_KEY")

TOKEN_COUNT=$(echo "$TOKENS_RESPONSE" | jq "length" 2>/dev/null || echo "0")
if [ "$TOKEN_COUNT" -eq 0 ]; then
  echo -e "${RED}Aucun token actif trouve.${NC}"
  echo -e "${YELLOW}Reponse:${NC} $TOKENS_RESPONSE"
  exit 1
fi

TOKENS_JSON=$(echo "$TOKENS_RESPONSE" | jq -c "map(.fcm_token)")
echo -e "${GREEN}$TOKEN_COUNT token(s) actif(s) trouve(s).${NC}"
echo

echo -e "${BLUE}2) Envoi de la notification via Vercel...${NC}"
REQUEST_BODY=$(jq -n \
  --argjson tokens "$TOKENS_JSON" \
  --arg title "$PUSH_TITLE" \
  --arg body "$PUSH_BODY" \
  "{
    tokens: \$tokens,
    title: \$title,
    body: \$body,
    data: {
      type: \"flood_alert\",
      source: \"supabase_script\",
      severity: \"high\",
      localite: \"Bamako\"
    }
  }")

PUSH_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST \
  "$VERCEL_URL/api/send-notification" \
  -H "Content-Type: application/json" \
  -d "$REQUEST_BODY")

HTTP_CODE=$(echo "$PUSH_RESPONSE" | tail -n1)
BODY=$(echo "$PUSH_RESPONSE" | sed "\$d")

echo -e "${YELLOW}HTTP:${NC} $HTTP_CODE"
echo "$BODY" | jq "." 2>/dev/null || echo "$BODY"
echo

if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 207 ]; then
  SUCCESS_COUNT=$(echo "$BODY" | jq -r ".successCount // 0" 2>/dev/null || echo "0")
  ERROR_COUNT=$(echo "$BODY" | jq -r ".errorCount // 0" 2>/dev/null || echo "0")
  echo -e "${GREEN}Test termine: ${SUCCESS_COUNT} succes, ${ERROR_COUNT} erreurs.${NC}"
else
  echo -e "${RED}Echec de l envoi push (HTTP $HTTP_CODE).${NC}"
  exit 1
fi

