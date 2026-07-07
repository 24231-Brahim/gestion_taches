#!/usr/bin/env bash
set -e

API="http://localhost:8080/api"
PWD="admin"

# === Configuration : 200+ par table ===
NUM_PROJECTS=50
SPRINTS_PER_PROJECT=4   # => 200
EPICS_PER_PROJECT=4      # => 200
ISSUES_PER_PROJECT=5     # => 250
USERS_IDS=(2 3 4)        # 3 membres par projet => 150

echo "=== Connexion ==="
TOKEN=$(curl -s -X POST "$API/authenticate" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"$PWD\"}" | python3 -c "import sys,json; print(json.load(sys.stdin)['id_token'])" 2>/dev/null || \
  curl -s -X POST "$API/authenticate" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"admin\",\"password\":\"$PWD\"}" | jq -r '.id_token')
echo "Token OK"

# ============================================
# PROJETS
# ============================================
echo ""
echo "=== Projets ($NUM_PROJECTS) ==="
NAMES=(
  Alpha Beta Gamma Delta Epsilon Zeta Eta Theta Iota Kappa
  Lambda Mu Nu Xi Omicron Pi Rho Sigma Tau Upsilon
  Phi Chi Psi Omega Aether Boreas Crios Cronos Eos Helios
  Hyperion Iapetos Mnemosyne Oceanus Phoebe Rheia Selene Styx Themis Triton
  Atlas Prometheus Epimetheus Hestia Demeter Hera Hades Poseidon Zeus Ares
)
for i in $(seq 1 $NUM_PROJECTS); do
  NAME="${NAMES[$((i-1))]}"
  KEY="${NAME:0:3}"
  curl -s -X POST "$API/projects" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"name\":\"Projet $NAME\",\"key\":\"$KEY\",\"createdAt\":\"2026-07-01T08:00:00Z\"}" > /dev/null
  echo "  + Projet $i/$NUM_PROJECTS : $NAME"
done

# ============================================
# SPRINTS
# ============================================
echo ""
echo "=== Sprints ($((NUM_PROJECTS * SPRINTS_PER_PROJECT))) ==="
STATUSES=("ACTIVE" "COMPLETED" "PLANNED" "PLANNED")
for p in $(seq 1 $NUM_PROJECTS); do
  for s in $(seq 0 $((SPRINTS_PER_PROJECT - 1))); do
    START_DAY=$((s * 14 + 1))
    END_DAY=$((START_DAY + 13))
    ST=${STATUSES[$s]}
    curl -s -X POST "$API/sprints" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d "{\"name\":\"Sprint $((s+1)) Projet $p\",\"status\":\"$ST\",\"startDate\":\"2026-07-$(printf '%02d' $START_DAY)\",\"endDate\":\"2026-07-$(printf '%02d' $END_DAY)\",\"projectId\":$p}" > /dev/null
  done
  echo "  + Sprints Projet $p OK"
done

# ============================================
# EPICS
# ============================================
echo ""
echo "=== Epics ($((NUM_PROJECTS * EPICS_PER_PROJECT))) ==="
EPIC_NAMES=(
  Authentification Paiement Recherche Dashboard Notifications
  Rapports Import Export API Mobile
  Performance Securite Onboarding Parametres Profils
  Messagerie Fichiers Workflow Analytics Calendar
)
PRIOS=("HIGH" "MEDIUM" "HIGH" "MEDIUM" "LOW" "LOW" "HIGH" "MEDIUM" "HIGH" "MEDIUM"
       "LOWEST" "HIGH" "MEDIUM" "LOW" "HIGHEST" "MEDIUM" "LOW" "HIGH" "LOWEST" "MEDIUM")
for p in $(seq 1 $NUM_PROJECTS); do
  for e in $(seq 0 $((EPICS_PER_PROJECT - 1))); do
    IDX=$(( (p-1) * EPICS_PER_PROJECT + e ))
    TITLE="${EPIC_NAMES[$((IDX % ${#EPIC_NAMES[@]}))]}"
    PRIO="${PRIOS[$((IDX % ${#PRIOS[@]}))]}"
    STATUS="IN_PROGRESS"
    [[ $((e % 4)) -eq 1 ]] && STATUS="TODO"
    [[ $((e % 4)) -eq 2 ]] && STATUS="DONE"
    curl -s -X POST "$API/epics" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d "{\"title\":\"$TITLE Projet $p\",\"status\":\"$STATUS\",\"priority\":\"$PRIO\",\"projectId\":$p,\"createdAt\":\"2026-07-01T08:00:00Z\"}" > /dev/null
  done
  echo "  + Epics Projet $p OK"
done

# ============================================
# ISSUES
# ============================================
echo ""
echo "=== Issues ($((NUM_PROJECTS * ISSUES_PER_PROJECT))) ==="
TYPES=("STORY" "BUG" "TASK" "SUBTASK" "IMPROVEMENT")
STATUSES_ISSUE=("IN_PROGRESS" "TODO" "DONE" "BACKLOG" "TODO")
PRIOS=("HIGH" "HIGHEST" "MEDIUM" "LOW" "MEDIUM")
TITLES=(
  "Implementer connexion" "Corriger bug validation" "Tests unitaires"
  "Optimiser requetes API" "Ajouter documentation" "Configurer CI/CD"
  "Refactorer service auth" "Corriger affichage mobile" "Ajouter filtrage"
  "Migrer donnees legacy"
)
for p in $(seq 1 $NUM_PROJECTS); do
  for i in $(seq 0 $((ISSUES_PER_PROJECT - 1))); do
    IDX=$(( (p-1) * ISSUES_PER_PROJECT + i ))
    TITLE="${TITLES[$((IDX % ${#TITLES[@]}))]} Projet $p"
    TYPE="${TYPES[$((i % ${#TYPES[@]}))]}"
    ST="${STATUSES_ISSUE[$((i % ${#STATUSES_ISSUE[@]}))]}"
    PRIO="${PRIOS[$((i % ${#PRIOS[@]}))]}"
    SPRINT=$(( (p-1) * SPRINTS_PER_PROJECT + (i % SPRINTS_PER_PROJECT) + 1 ))
    EPIC=$(( (p-1) * EPICS_PER_PROJECT + (i % EPICS_PER_PROJECT) + 1 ))
    curl -s -X POST "$API/issues" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d "{\"title\":\"$TITLE\",\"type\":\"$TYPE\",\"status\":\"$ST\",\"priority\":\"$PRIO\",\"projectId\":$p,\"sprintId\":$SPRINT,\"epicId\":$EPIC,\"createdAt\":\"2026-07-01T08:00:00Z\"}" > /dev/null
  done
  echo "  + Issues Projet $p OK"
done

# ============================================
# MEMBRES
# ============================================
echo ""
echo "=== Membres ($((NUM_PROJECTS * ${#USERS_IDS[@]}))) ==="
for p in $(seq 1 $NUM_PROJECTS); do
  for uid in "${USERS_IDS[@]}"; do
    curl -s -X POST "$API/projects/$p/members/$uid" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" > /dev/null
  done
  echo "  + Membres Projet $p OK"
done

# ============================================
# RÉSULTAT
# ============================================
echo ""
echo "===================================="
echo "  Terminé !"
echo "  Projets    : $NUM_PROJECTS"
echo "  Sprints    : $((NUM_PROJECTS * SPRINTS_PER_PROJECT))"
echo "  Epics      : $((NUM_PROJECTS * EPICS_PER_PROJECT))"
echo "  Issues     : $((NUM_PROJECTS * ISSUES_PER_PROJECT))"
echo "  Membres    : $((NUM_PROJECTS * ${#USERS_IDS[@]}))"
echo "===================================="
