# Bugs du Dashboard

~~**BUG 1 — La liste des projets affiche "N/A" pour tous les projets**~~ ✅ **CORRIGÉ**

---

~~**BUG 2 — KPI "Tâches en Retard" toujours à 0**~~ ✅ **CORRIGÉ**

---

~~**BUG 4 — Aucun état de chargement (loading)**~~ ✅ **CORRIGÉ**

~~**BUG 5 — Aucune gestion d'erreur**~~ ✅ **CORRIGÉ**

---

~~**BUG 6 — L'objet `params` est partagé entre deux ressources HTTP**~~ ✅ **CORRIGÉ**

---

~~**BUG 7 — `DashboardListsComponent` a des `input.required()` qui ne sont jamais utilisés**~~ ✅ **CORRIGÉ**

---

~~**BUG 8 — `projectProgress()` ne montre que 5 projets de façon arbitraire**~~ ✅ **CORRIGÉ**

---

~~**BUG 9 — Limite fixée à 200, peut ignorer des données**~~ ✅ **CORRIGÉ** (portée à 500)

---

~~**BUG 10 — 4 cartes KPI vides (pas de titre, icône ni valeur)**~~ ✅ **CORRIGÉ**

**Cause :** Les icônes FontAwesome `faRocket`, `faCheckCircle`, `faExclamationCircle` utilisées par les cartes "Projets Actifs", "Terminées" et "Tâches en Retard" n'étaient pas enregistrées dans `font-awesome-icons.ts`. Quand `fa-icon` ne trouve pas l'icône, il lève une erreur silencieuse qui empêche le rendu complet du composant Angular.

**Fix :** Ajout des imports et exports de `faRocket`, `faCheckCircle`, `faExclamationCircle` dans `font-awesome-icons.ts`.

---

~~**BUG 11 — Style `.item-meta` manquant dans la liste "Recent Tasks"**~~ ✅ **CORRIGÉ**

**Cause :** Le template de `lists.component.ts` utilise `<span class="item-meta">` pour afficher le statut des tâches avec une couleur, mais la classe CSS `.item-meta` n'était pas définie dans les styles du composant.

**Fix :** Ajout de la classe `.item-meta` avec les styles appropriés dans `lists.component.ts`.

---

~~**BUG 12 — Quick Actions dans le mauvais ordre**~~ ✅ **CORRIGÉ**

**Cause :** Le composant `<jhi-dashboard-quick-actions />` était placé dans la `.bottom-grid` à côté de la timeline, au lieu d'être entre les KPIs et les graphiques.

**Fix :** Déplacement de `<jhi-dashboard-quick-actions />` entre `.kpi-grid` et `<jhi-dashboard-charts>` dans `dashboard.component.ts`. `.bottom-grid` passe de `1fr 1fr` à `1fr`.

---

~~**BUG 13 — Layout Quick Actions en grille 2×2 au lieu de ligne horizontale**~~ ✅ **CORRIGÉ**

**Cause :** `.qa-grid` utilisait `display: grid; grid-template-columns: 1fr 1fr;` au lieu d'une disposition en ligne.

**Fix :** Passage à `display: flex; flex-direction: row; flex-wrap: wrap;` avec `flex: 1 1 160px;` sur les boutons dans `quick-actions.component.ts`.

---

~~**BUG 14 — Aucune donnée de démonstration en base**~~ ✅ **CORRIGÉ**

**Cause :** Les tables métier (project, sprint, epic, issue, comment, project_member, action_history) étaient vides car aucun seed data n'était défini pour les entités métier.

**Fix :** Création de 7 fichiers CSV de seed (project, sprint, epic, issue, comment, project_member, action_history) avec 2 projets, 4 sprints, 4 epics, 10 issues, 5 commentaires, 6 membres et 7 actions. Nouveau changelog Liquibase `20260630000000_added_seed_data.xml` chargé via `master.xml`.

---
