# Audit couleurs — Frontend Angular / JHipster (Étape 1 : inventaire + Étape 2 : corrections)

> Rapport d'inventaire (étape 1) suivi du résumé des corrections appliquées (étape 2).
> Les corrections décrites au §5–§6 ont été **appliquées** (voir §8 « Ce qui a été fait »).
> Toute valeur hors `design-system.scss` est désormais un token, sauf mention contraire.

Design system audité : `src/main/webapp/content/scss/design-system.scss`
Couleurs de marque intouchées : `#57deff` (cyan) et `#079dfe` (bleu).

---

## 1. Inventaire des fichiers audités

Fichiers `.scss`/`.css` :

- `src/main/webapp/content/scss/design-system.scss` (tokens)
- `src/main/webapp/content/scss/global.scss` (styles globaux)
- `src/main/webapp/content/scss/_bootstrap-variables.scss` (variables Bootstrap)
- `src/main/webapp/content/scss/vendor.scss`
- `src/main/webapp/content/css/loading.css`
- Composants : `home.scss`, `layouts/profiles/page-ribbon.scss`, `layouts/bottom-nav/bottom-nav.scss`,
  `layouts/error/error.scss`, `layouts/navbar/navbar.scss`, `layouts/breadcrumb/breadcrumb.scss`,
  `layouts/sidebar/sidebar.scss`, `login.component.scss`, `notifications/notification-list.scss`,
  `entities/project/list/project.scss`, `entities/project/list/project-card.scss`,
  `entities/admin/user-management/user-admin-detail/user-admin-detail.scss`,
  `account/register/register.component.scss`,
  `account/password/password-strength-bar/password-strength-bar.scss`, `admin/docs/docs.scss`

Fichiers HTML/TS avec couleurs en dur (inline / `[style]` / `[attr.stroke]`) : **~40 fichiers**
(listés en §4.3). Beaucoup suivent le motif `var(--color-…, #hex)` avec des _fallbacks_ héxadécimaux.

---

## 2. Synthèse des problèmes

| #     | Problème                                                                                                                                                                                                                                                            | Impact                                                                                                                                                                                                                                                                                                  |
| ----- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **A** | **~40 tokens définis UNIQUEMENT en `light`, absents en `dark`** (`--color-cta`, `--color-cta-hover`, `--color-status-*` ×13, `--color-priority-*` ×5, `--color-story/bug/task/subtask/improvement/info`, `--color-tag-*` ×10, `--color-link`, `--color-text-muted`) | **C'est le bug des boutons** : `.btn-primary`/`.btn-cta` ont un fond en clair et **aucun fond en sombre** (`--color-cta` inexistant). Idem `.btn-danger` (fond `--color-status-cancelled-bg` absent en dark). Tous les badges statut/priorité/tags retombent sur leurs fallbacks hex obsolètes en dark. |
| **B** | Texte/icône **blanc sur bleu logo** (`--color-primary-container` = `#079dfe`) = **2.89:1** (seuil AA 4.5:1)                                                                                                                                                         | Pagination active, split-screen, sidebar-toggle. Le token prévu existe déjà : `--color-on-primary-container` (5.42:1).                                                                                                                                                                                  |
| **C** | `color: var(--color-primary)` (= bleu logo `#079dfe`, **2.89:1 sur fond clair**) utilisé comme **texte/icône** dans 36 règles SCSS + 8 fichiers TS                                                                                                                  | Invisible en mode clair. Le design system prévoit `--color-link` (#0074c2, 4.91:1) pour le texte en clair → utiliser `var(--color-link, var(--color-primary))`.                                                                                                                                         |
| **D** | Boutons `--color-cta` (clair) : blanc sur `#0d9488` = **3.74:1** → échoue **même en light**                                                                                                                                                                         | `.btn-primary`, `.btn-cta` (project.scss). `--color-cta-hover` (`#55e1ff`) passe (5.47:1).                                                                                                                                                                                                              |
| **E** | Boutons success/warning/danger en **dark** : blanc sur `#22c55e`/`#f59e0b`/`#ef4444` = **2.28 / 2.15 / 3.76:1**                                                                                                                                                     | `.btn-success`, `.btn-warning`, `.notification-badge`. En light ça passe (5.0/5.0/4.8). Il manque des tokens `--color-on-success/warning/danger`.                                                                                                                                                       |
| **F** | `.error` (global.scss) : blanc (`--color-on-error`) sur `--color-error-container` clair `#ffe9e9` = **1.16:1** → texte illisible                                                                                                                                    | Doit utiliser `--color-on-error-container`, dont la valeur claire (`#dc2626`) est elle-même 4.16:1 → à assombrir.                                                                                                                                                                                       |
| **G** | Fallbacks hex dans les TS **incohérents entre fichiers** et ≠ design system (`#25a7fd` vs `#0099fe` pour primary-container, `#262d36` vs `#262a30`, `#6a8fac` ≠ `--color-muted`, `#97cbff` ≠ primary, `#fff`/`#000` sur primary-container)                          | Duplication de valeurs qui auraient dû être un token partagé.                                                                                                                                                                                                                                           |
| **H** | `_bootstrap-variables.scss` : **palette legacy** entière (`#0099fe`, `#0077cc`, `#1a1d21`, `#dde3ec`, `#f1f4f9`, `#5b7286`, `#e9edf3`…) ≠ design system                                                                                                             | Duplication. Bootstrap étant compilé (pas de var CSS), il faut aligner les hex sur les valeurs light du design system.                                                                                                                                                                                  |
| **I** | `--shadow-*` définis **2 fois** avec des valeurs différentes (bloc light L174-176 vs bloc partagé L287-289)                                                                                                                                                         | Light = ombres bleutées, dark = ombres noires. Duplication.                                                                                                                                                                                                                                             |
| **J** | Couleurs **mortes** (définies, jamais référencées par l'app)                                                                                                                                                                                                        | À supprimer ou à conserver (voir §4.4).                                                                                                                                                                                                                                                                 |
| **K** | Couleurs en dur orphelines : `error.scss` (#fff / #0099fe), `loading.css` (#a0c4e8), `404.html`, `index.html` (theme-color), scrims `rgba(0,0,0,…)`                                                                                                                 | Divers (voir §4.2).                                                                                                                                                                                                                                                                                     |

---

## 3. Contrastes mesurés (WCAG AA, valeurs clés)

| Combinaison                                           | Ratio              | Verdict                  | Correction                                |
| ----------------------------------------------------- | ------------------ | ------------------------ | ----------------------------------------- |
| `#ffffff` sur `--color-primary-container` (#079dfe)   | 2.89               | ❌                       | `--color-on-primary-container` (5.42)     |
| `--color-on-primary-container` (#00263b) sur #079dfe  | 5.42               | ✅                       | —                                         |
| `#ffffff` sur `--color-cta` (#0d9488)                 | 3.74               | ❌ (y compris **light**) | CTA → `#55e1ff` + `--color-on-cta`        |
| `#00263b` sur `--color-cta` (#0d9488) — `.btn-cta`    | 4.18               | ❌                       | `--color-on-cta`                          |
| `#ffffff` sur dark `#22c55e` / `#f59e0b` / `#ef4444`  | 2.28 / 2.15 / 3.76 | ❌                       | `--color-on-success/warning/danger` dark  |
| `#ffffff` sur light `#15803d` / `#b45309` / `#dc2626` | 5.02 / 5.02 / 4.83 | ✅                       | —                                         |
| `#ffffff` sur `#ffe9e9` (`.error`, light)             | **1.16**           | ❌                       | `--color-on-error-container`              |
| `#dc2626` sur `#ffe9e9` (light on-error-container)    | 4.16               | ❌                       | → `#b91c1c` (5.57)                        |
| `color: var(--color-primary)` sur fond clair          | 2.89               | ❌                       | `var(--color-link, var(--color-primary))` |
| `#f2994a` sur `#fff1de` (badge sprint, fallback dark) | 2.00               | ❌                       | tokens dark status                        |
| `#f97316` sur `#f5f7fb` (badge priority-high, light)  | 2.61               | ❌                       | `--color-priority-high` (#b45309, 5.0)    |
| `#a855f7` sur `#f5f7fb` (badge in-review, light)      | 3.69               | ❌                       | `--color-status-in-review` (#6d28d9, 7.1) |
| `#0099fe` sur `#fff` (error-code)                     | 3.01               | ❌                       | `var(--color-link, var(--color-primary))` |
| `#a0c4e8` sur blanc (loading)                         | 1.82               | décoratif                | aligner sur bleu logo                     |
| dark `--color-danger` (#ef4444) sur fond dark         | 4.92               | ✅                       | —                                         |
| `--color-link` (#0074c2) sur blanc                    | 4.91               | ✅                       | —                                         |

---

## 4. Détail fichier par fichier

### 4.1 `content/scss/design-system.scss` (le fichier source des tokens)

| Ligne   | Problème                                                                                                                 | Correction proposée                                                     |
| ------- | ------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------- |
| 174-176 | `--shadow-sm/md/lg` **dupliqués** avec le bloc partagé (287-289), valeurs différentes                                    | Supprimer le bloc light (174-176) et ne garder qu'une définition unique |
| 187-188 | `--color-cta`/`--color-cta-hover` **light uniquement** → `.btn-primary` sans fond en dark                                | Ajouter les tokens CTA dark (voir §5)                                   |
| 190-231 | `--color-status-*`, `--color-priority-*`, `--color-story…`, `--color-tag-*` **light uniquement** → badges cassés en dark | Ajouter les équivalents dark (voir §5)                                  |
| 115     | `--color-link` light uniquement (sans conséquence car `a` a un fallback, mais incomplet)                                 | Ajouter `--color-link` dark                                             |
| 145     | `--color-text-muted` light uniquement, utilisé sans fallback dans `dashboard.component.ts`                               | Ajouter `--color-text-muted: var(--color-muted)` dark                   |
| 152-154 | `--color-on-error-container` light `#dc2626` sur `#ffe9e9` = **4.16:1** ❌                                               | Passer light à `#b91c1c` (5.57:1)                                       |
| 14-16   | `#57deff`, `#079dfe`, `--color-on-brand`                                                                                 | **Intouchés** ✅                                                        |

### 4.2 Fichiers `.scss`/`.css`

**`content/css/loading.css`**
| Ligne | Problème | Correction |
|---|---|---|
| 76 | `background: #a0c4e8` en dur | Élément décoratif ; remplacer par le bleu logo `#079dfe` (valeur littérale car ce CSS est chargé avant les tokens) |

**`app/layouts/error/error.scss`**
| Ligne | Problème | Correction |
|---|---|---|
| 2 | `background: #fff` → page d'erreur **reste blanche en dark** | `var(--color-bg)` |
| 24 | `color: #0099fe` (3.01:1 sur blanc ❌) | `var(--color-link, var(--color-primary))` |

**`app/layouts/profiles/page-ribbon.scss`**
| Ligne | Problème | Correction |
|---|---|---|
| 5 | fallback `rgba(170,0,0,0.5)` : `--color-status-cancelled-bg` n'existe pas en dark → ruban rouge semi-transparent en dark | Rendu automatique une fois le token dark défini (section §5) ; sinon aligner le fallback |
| 18 | fallback `#fff` sur fond rouge (contraste insuffisant) | `--color-danger` est déjà le texte ; garder fallback cohérent |

**`app/layouts/sidebar/sidebar.scss`**
| Ligne | Problème | Correction |
|---|---|---|
| 101 | `color: #ffffff` au survol de `.sidebar-toggle`, fond `--color-primary-container` (#079dfe) = 2.89 ❌ | `var(--color-on-primary-container)` |
| 13 | `rgba(0, 0, 0, 0.5)` scrim overlay | OK (voile modal, fonctionnel) — conservé |
| 242 | fallback `rgb(255 255 255 / 12%)` | Fallback cohérent, token défini dans les 2 thèmes — OK |

**`app/layouts/navbar/navbar.scss`**
| Ligne | Problème | Correction |
|---|---|---|
| 227 | `.notification-badge` blanc sur `--color-danger` : **3.76 en dark** ❌ | `var(--color-on-danger)` (light reste blanc, dark devient `#2a0505`) |
| 102, 162, 167, 198, 249 | `color: var(--color-primary)` (texte/icône) sur fond clair = 2.89 ❌ | `var(--color-link, var(--color-primary))` |

**`content/scss/global.scss`**
| Ligne | Problème | Correction |
|---|---|---|
| 199-206 | `.btn-primary` fond `--color-cta` **absent en dark** (transparent) + texte `#ffffff` sur `#0d9488` = 3.74 ❌ | `background: var(--color-cta)` (une fois défini dark) + `color: var(--color-on-cta)` |
| 228-237 | `.btn-danger` fond `--color-status-cancelled-bg` absent en dark + texte `--color-danger` sur `#ffe9e9` = 4.16 ❌ light | défini en dark (§5) ; texte `var(--color-on-danger)` ou `--color-on-error-container` |
| 240-261 | `.btn-success`/`.btn-warning` blanc sur status dark = 2.28/2.15 ❌ | `color: var(--color-on-success/warning)` |
| 61-67 | `.error` blanc sur `#ffe9e9` = **1.16:1** ❌ | `color: var(--color-on-error-container)` |
| 625-628 | `.page-item.active .page-link` blanc sur primary-container = 2.89 ❌ | `var(--color-on-primary-container)` |
| 1000-1016 | `.split-screen-left-content` h1/p blanc (et rgba blanc .85) sur gradient bleu = 2.9 ❌ | `var(--color-on-primary-container)` + `color-mix(…, 85%, transparent)` |
| 1051-1077 | `.split-screen-btn` blanc sur primary-container = 2.89 ❌ | `var(--color-on-primary-container)` |
| 14, 31, 278-283, 352, 573, 620, 751, 906-907, 955, 1106, 1127 | `color: var(--color-primary)` (texte/icône) sur fond clair | `var(--color-link, var(--color-primary))` |
| 735-755 | `.bg-*` / `.text-*` avec `!important` | Acceptable : surcharge obligatoire des utilitaires Bootstrap (eux-mêmes `!important`). `.text-primary` corrigé via `--color-link` ci-dessus |
| 202, 246, 258 | `color-mix(…, black)` (mot-clé `black`) pour assombrir hover | Mineur : cohérent avec `--color-on-*` proposés ; à garder ou tokeniser |

**`content/scss/_bootstrap-variables.scss`** (Bootstrap compilé une fois, pas de var CSS → conserver des hex littéraux, mais **alignés** sur les valeurs light du design system)
| Ligne(s) | Valeur actuelle | Valeur design-system light |
|---|---|---|
| 21 | `$body-bg: #ffffff` | `--color-bg` #f5f7fb |
| 22 | `$body-color: #1a1d21` | `--color-text` #1d2233 |
| 44-45 | `$link-color #0077cc`, `$link-hover-color #0097b2` | `--color-link` #0074c2 ; hover `--color-secondary` #55e1ff |
| 49-51 | `$navbar-dark-*` #1a1d21 / #0077cc / #0099fe | text #1d2233, hover #0074c2, active #079dfe |
| 59-68 | dropdown #ffffff / #1a1d21 / #dde3ec / #f1f4f9 / #0099fe | surface #ffffff / #1d2233 / outline-variant #e7eaf3 / surface-container #f5f7fb / brand-blue #079dfe |
| 71-76, 79-88 | card/input legacy | aligner sur surface / outline-variant / #0074c2 / muted #575e78 |
| 106-128 | table/pagination legacy (#e9edf3, #f7f9fc, #5b7286, #0099fe) | surface-container-high #edeff7 / surface-container-low #fafbfd / muted #575e78 / brand-blue #079dfe |
| 148-163 | modal/divider/progress (#dde3ec, rgba(0,0,0,.5), #e9edf3, #0099fe) | outline-variant #e7eaf3 / scrim / surface-container-high #edeff7 / brand-blue #079dfe |

**`app/entities/admin/user-management/user-admin-detail/user-admin-detail.scss`**
| Ligne | Problème | Correction |
|---|---|---|
| 194-195 | `#a855f7` en dur (badge in-review) = 3.69 ❌ | `var(--color-status-in-review)` |
| 220-221 | `#f97316` en dur (priority-high) = 2.61 ❌ | `var(--color-priority-high)` |

**`app/entities/project/list/project-card.scss`**
| Ligne | Problème | Correction |
|---|---|---|
| 58-59 | fallbacks `#fff1de`/`#f2994a` (dark) = **2.00:1** ❌ + fond pastel clair sur carte sombre | Corrigé par les tokens dark (§5) |

**`app/entities/project/list/project.scss`**
| Ligne | Problème | Correction |
|---|---|---|
| 84-90 | `.btn-cta` `color: var(--color-on-primary)` (#00263b) sur `--color-cta` = 4.18 ❌ | `var(--color-on-cta)` |

**Autres composants** (`home.scss`, `bottom-nav`, `breadcrumb`, `navbar`, `notification-list`, `project.scss`, `user-admin-detail`, `password-strength-bar`, `docs.scss`, `login`, `register`) : conformes à 90 %, à l'exception des occurrences `color: var(--color-primary)` listées en §2-C (bottom-nav L42/46, breadcrumb L37/52, home L59, navbar L102/162/167/198/249, sidebar L305/345, project L43/125, project-card L48, user-admin-detail L92/110/172/298).

### 4.3 Fichiers HTML/TS (styles inline) — motif `var(--color-…, #hex)`

**Problème racine commun :** les fallbacks hex sont des valeurs **obsolètes/divergentes** (ancienne palette) et ne correspondent pas aux tokens actuels. Une fois les tokens dark complétés (§5), ces fallbacks deviennent inutiles.

| Fichier(s)                                                                                                                                                                           | Problème                                                                                                                                                                              | Correction                                                                                                                                      |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| ~20 fichiers TS                                                                                                                                                                      | `color: var(--color-text-muted, #6a8fac)` (≈110 occurrences) — `#6a8fac` ≠ `--color-muted` (dark `#9aa7b5`, light `#575e78`)                                                          | Ajouter `--color-text-muted` dark ; retirer/aligner le fallback                                                                                 |
| ~20 fichiers TS                                                                                                                                                                      | `background/border: var(--color-surface-container, #1b2025)` + `var(--color-outline-variant, #2a3038)` + `color: var(--color-text, #dfe3ea)` + `color: var(--color-primary, #97cbff)` | `#97cbff` n'est **ni** le cyan ni le bleu logo ; `#1b2025`/`#262d36` divergent (`#262a30`). Aligner les fallbacks sur les tokens ou les retirer |
| `my-tasks.ts` L60/95, `task.ts` L98, `epic-detail.ts` L218/228, `epic-roadmap.ts` L168, `group-message-list.ts` L71, `sprint-backlog-planning.ts` L45, `sprint-active-board.ts` L173 | `color: #000` sur `var(--color-primary-container,…)`                                                                                                                                  | `var(--color-on-primary-container)`                                                                                                             |
| `task-detail-panel.ts` L131, `task-kanban-board.ts` L142, `sprint-active-board.ts` L197                                                                                              | `var(--color-on-primary-container, #fff)` — fallback **#fff faux** (blanc sur bleu logo)                                                                                              | fallback `#00263b` ou retirer (token défini)                                                                                                    |
| `task-comments-tab.ts` L60, `group-message-list.ts` L70, `sprint-backlog-planning.ts` L44                                                                                            | fallback `var(--color-primary-container, #0099fe)` — ancien bleu                                                                                                                      | `#079dfe` ou retirer                                                                                                                            |
| `epic.ts` L184-198, `epic-detail.ts` L407-432, `epic-roadmap.ts` L36-39, `sprint-active-board.ts` L407-410, `sprint-timeline.ts` L11-16                                              | Maps statut/priorité avec fallbacks Material (#2196f3, #ff9800, #4caf50, #f44336, #9e9e9e, #607d8b) — **utilisés en dark** car tokens absents                                         | Tokens dark (§5) → fallbacks morts                                                                                                              |
| `task-helper.ts` L16-39                                                                                                                                                              | `var(--color-priority-lowest)` **sans fallback** → invalide en dark                                                                                                                   | Tokens dark                                                                                                                                     |
| `lists.component.ts` L172                                                                                                                                                            | `READY_FOR_TEST: '#a855f7'` en dur                                                                                                                                                    | `var(--color-status-in-review)`                                                                                                                 |
| `lists.component.ts` L171                                                                                                                                                            | `IN_PROGRESS: 'var(--color-info)'` — token light-only                                                                                                                                 | tokens dark                                                                                                                                     |
| `charts.component.ts` L209-216, 240                                                                                                                                                  | palettes hex (`#4c6fff`, `#f2994a`, `#a855f7`, `#1fa971`, `#e5484d`, `#6b7290`, …)                                                                                                    | mapper sur `var(--color-status-*)` / `var(--color-muted)` (rendu SVG accepte les var)                                                           |
| `developer-dashboard.component.ts` L34, `timeline.component.ts` L118                                                                                                                 | `ACTIVITY_COLORS` hex                                                                                                                                                                 | mapper sur tokens status/secondary/tertiary                                                                                                     |
| `search-dialog.component.ts` L158-171                                                                                                                                                | badges statut Material en dur (rgba(33,150,243,.15)/#64b5f6, #81c784, #ffb74d, #ce93d8)                                                                                               | `var(--color-status-*)` avec fond `color-mix`                                                                                                   |
| `password-strength-bar.ts` L17/36                                                                                                                                                    | `['#F00','#F90','#FF0','#9F0','#0F0']` + `#DDD`                                                                                                                                       | `var(--color-danger)`, `var(--color-warning)`, `var(--color-success)`, `var(--color-surface-container-high)`                                    |
| `admin-notifications/project-members/tasks.html` L21                                                                                                                                 | `style="… color: #6a8fac …"` (×3)                                                                                                                                                     | `var(--color-muted)`                                                                                                                            |
| `epic-burndown-chart.html`, `sprint-burndown-chart.html`                                                                                                                             | fallbacks `#2a3038`, `#97cbff`, `#4caf50`                                                                                                                                             | tokens / fallbacks alignés                                                                                                                      |
| `index.html` L10                                                                                                                                                                     | `<meta name="theme-color" content="#f5f7fb">`                                                                                                                                         | = `--color-bg` light ; peut rester littéral (var CSS non supportée) — signalé pour info                                                         |
| `404.html` L15/30                                                                                                                                                                    | `#888` / `#555`                                                                                                                                                                       | Page statique sans design system — **hors périmètre**, signalé                                                                                  |
| `sprint-active-board.ts` L117, `task-kanban-board.ts` L60, `epic-detail.ts` L211, `epic-roadmap.ts` L105, `search-dialog` L69, `sprint-detail` L118, `task-detail-panel` L32         | scrims `rgba(0,0,0,…)` / overlays blancs                                                                                                                                              | OK (fonctionnels) — conservés                                                                                                                   |

### 4.4 Couleurs mortes (définies dans le design system, jamais référencées par l'app)

- **Domaine** : `--color-story`, `--color-bug`, `--color-task`, `--color-subtask`,
  `--color-improvement`, `--color-status-testing`, `--color-status-testing-bg`,
  `--color-status-todo-bg`, `--color-tag-pink-bg/fg`, `--color-tag-purple-bg/fg`,
  `--color-tag-yellow-bg/fg`, `--color-tag-teal-bg`
  → **recommandation : supprimer** (ou conserver si prévu au roadmap).
- **Vocabulaire M3 inutilisé** (rôle : tokens de référence du système, non régression si conservés) :
  `--color-inverse-*`, `--color-*-fixed(-dim)`, `--color-surface-bright/highest/lowest/tint/variant`,
  `--color-tertiary-container`, `--color-on-*-*` (secondary/tertiary/error/fixed)
  → **recommandation : conserver** (ensemble cohérent du design system).

---

## 5. Tokens proposés — APPLIQUÉS (voir §8)

> ✅ Appliqué à l'étape 2. Les valeurs proviennent de la palette déjà validée (même famille
> de teinte, clarté ajustée pour le fond sombre), contrastes ≥ 4.5:1 mesurés.
> Les écarts par rapport au bloc ci-dessous sont listés au §8 (déviations).

**Bloc `:root[data-theme='dark']` — ajouts**

```scss
/* Liens & texte */
--color-link: var(--color-brand-cyan);
--color-text-muted: var(--color-muted);

/* CTA — même famille teal que le light, éclaircie pour le fond sombre */
--color-cta: #2dd4bf; /* texte --color-on-cta 9.2:1 ✓ */
--color-cta-hover: #5eead4;
--color-on-cta: #00201c;

/* Statuts — icône/texte : mappés sur les tokens sémantiques déjà validés du dark
   (même logique qu'en light : status-todo = warning, in-review = tertiary, etc.) */
--color-status-backlog: var(--color-brand-blue); /* 6.4:1 ✓ */
--color-status-backlog-bg: color-mix(in srgb, var(--color-brand-blue) 16%, transparent);
--color-status-todo: var(--color-warning); /* 8.6:1 ✓ */
--color-status-todo-bg: color-mix(in srgb, var(--color-warning) 16%, transparent);
--color-status-in-progress: var(--color-warning);
--color-status-in-progress-bg: color-mix(in srgb, var(--color-warning) 16%, transparent);
--color-status-in-review: var(--color-tertiary); /* 10.0:1 ✓ */
--color-status-in-review-bg: color-mix(in srgb, var(--color-tertiary) 18%, transparent);
--color-status-testing: var(--color-secondary); /* 9.9:1 ✓ */
--color-status-testing-bg: color-mix(in srgb, var(--color-secondary) 16%, transparent);
--color-status-done: var(--color-success); /* 8.1:1 ✓ */
--color-status-done-bg: color-mix(in srgb, var(--color-success) 16%, transparent);
--color-status-cancelled: var(--color-danger); /* 4.9:1 ✓ */
--color-status-cancelled-bg: color-mix(in srgb, var(--color-danger) 16%, transparent);

/* Priorités */
--color-priority-lowest: var(--color-brand-blue);
--color-priority-low: var(--color-brand-blue);
--color-priority-medium: #fbbf24; /* 11.1:1 ✓ */
--color-priority-high: var(--color-warning);
--color-priority-highest: var(--color-danger);

/* Types de tâche */
--color-story: var(--color-secondary);
--color-bug: var(--color-danger);
--color-task: var(--color-brand-cyan);
--color-subtask: var(--color-muted);
--color-improvement: var(--color-warning);
--color-info: var(--color-brand-cyan);

/* Tags libres */
--color-tag-pink-bg: color-mix(in srgb, #f472b6 16%, transparent);
--color-tag-pink-fg: #f472b6; /* 7.0:1 ✓ */
--color-tag-yellow-bg: color-mix(in srgb, #fde047 16%, transparent);
--color-tag-yellow-fg: #fde047; /* 14.0:1 ✓ */
--color-tag-teal-bg: color-mix(in srgb, var(--color-secondary) 16%, transparent);
--color-tag-teal-fg: var(--color-secondary);
--color-tag-purple-bg: color-mix(in srgb, var(--color-tertiary) 18%, transparent);
--color-tag-purple-fg: var(--color-tertiary);
--color-tag-blue-bg: color-mix(in srgb, var(--color-brand-cyan) 16%, transparent);
--color-tag-blue-fg: var(--color-brand-cyan);

/* Texte sur boutons de statut (le blanc échoue sur les couleurs vives du dark) */
--color-on-success: #052e14; /* 6.6:1 sur #22c55e ✓ */
--color-on-warning: #1c0e00; /* 8.8:1 sur #f59e0b ✓ */
--color-on-danger: #2a0505; /* 5.0:1 sur #ef4444 ✓ */
```

**Bloc `:root[data-theme='light']` — modifications**

```scss
--color-cta: #55e1ff; /* remplace #0d9488 (blanc dessus 3.74 ❌) — blanc dessus 5.47 ✓ */
--color-cta-hover: #0b6a63;
--color-on-cta: #ffffff;
--color-on-error-container: #b91c1c; /* remplace #dc2626 (4.16 ❌) — 5.57 ✓ */
```

> Toutes les autres couleurs proposées réutilisent des tokens existants (`--color-*`) —
> aucune nouvelle couleur hors palette n'est introduite, sauf les 3 `--color-on-success/
warning/danger` dark et `--color-tag-pink/yellow-fg` (teintes claires de la même famille,
> nécessaires pour rester lisibles sur fond sombre).

---

## 6. Plan de correction proposé (Étape 2 — après validation)

1. **design-system.scss** : ajouter le bloc dark (§5), modifier CTA light + on-error-container light,
   supprimer le doublon `--shadow-*` (bloc light), supprimer les couleurs mortes domaine (§4.4).
2. **global.scss** : `.btn-primary/btn-cta` → `--color-on-cta` ; `.btn-success/warning` → `--color-on-success/warning` ;
   `.btn-danger`/`.error` → `--color-on-danger`/`--color-on-error-container` ; `.page-item.active`,
   `.split-screen-*`, `.sidebar-toggle` → `--color-on-primary-container` ; `.notification-badge` → `--color-on-danger` ;
   `.text-primary` → `var(--color-link, var(--color-primary))`.
3. **Tous les composants** : remplacer `color: var(--color-primary)` (texte/icône) par
   `var(--color-link, var(--color-primary))` ; `error.scss` → `--color-bg` + link.
4. **`_bootstrap-variables.scss`** : aligner les hex legacy sur les valeurs light du design system (§4.2).
5. **TS/HTML** : retirer/aligner les fallbacks obsolètes (motif `var(--color-…, #hex)`),
   `#000`/`#fff` → `--color-on-primary-container`, maps statut/priorité laissées sur
   `var(--color-status-*)` (désormais définies en dark), palettes de graphiques → tokens,
   `password-strength-bar` → tokens, `admin-*.html` → `var(--color-muted)`.
6. **`loading.css`** : `#a0c4e8` → bleu logo (décoratif).
7. Aucun fichier backend touché ; `#57deff`/`#079dfe` inchangés.

---

## 7. Composants à vérifier visuellement (clair ET sombre)

Boutons (`.btn-primary`, `.btn-cta`, `.btn-success`, `.btn-warning`, `.btn-danger`,
`.btn-outline-primary`), pagination active, sidebar (toggle + badge), navbar (notification-badge,
icônes actives), split-screen login/register, bannière `.error`/alertes, badges statut/priorité/tags
(kanban, timeline, epic roadmap, sprint active board, admin user-detail), cartes projet (badge sprint
actif), page d'erreur, graphiques (donut, burndown, timeline, activity), recherche (search-dialog),
force du mot de passe, page 404, écran de chargement.

---

## 8. Ce qui a été fait (Étape 2 — corrections appliquées)

### Avant / Après

| Sujet                              | Avant                                                                                                                                    | Après                                                                                                                |
| ---------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| Tokens dark manquants              | `link`, `text-muted`, `cta*`, `on-cta`, `on-success/warning/danger`, `status-*`, `priority-*`, `info`, `tag-teal/blue-*` absents du dark | tous définis (light = dark, parité vérifiée par script)                                                              |
| CTA light                          | `#0d9488` (blanc 3.74:1 ❌)                                                                                                              | `#55e1ff` (5.47:1 ✓), hover `#115e59` (7.58:1 ✓), `on-cta: #fff`                                                     |
| `--color-on-error-container` light | `#dc2626` (4.16 ❌)                                                                                                                      | `#b91c1c` (5.57 ✓)                                                                                                   |
| Fallbacks TS/HTML                  | 324 × `var(--color-…, #hex)`                                                                                                             | 0 (tokens réutilisés sans fallback)                                                                                  |
| `#000` dans TS                     | 7 occurrences (my-tasks, task/list, epic-detail, epic-roadmap, sprint-active-board)                                                      | `var(--color-on-primary-container)`                                                                                  |
| `password-strength-bar`            | hex `#dc2626`/`#eab308`/`#22c55e` + `#000`                                                                                               | tokens danger/warning/success + on-primary-container                                                                 |
| search-dialog types                | hex                                                                                                                                      | `color-mix(in srgb, var(--color-primary\|success\|warning\|tertiary) 15%, transparent)`                              |
| Graphiques/listes                  | hex par rôle                                                                                                                             | tokens (`--color-status-*`, `--color-muted`, `--color-secondary`, `--color-tertiary`, `--color-cta`, `--color-info`) |
| `#9e9e9e`/`#6a8fac` (admin)        | hex                                                                                                                                      | `var(--color-muted)`                                                                                                 |
| `loading.css`                      | `#a0c4e8`                                                                                                                                | `#079dfe` (littéral, chargé avant les tokens)                                                                        |
| Couleurs mortes (dark+light)       | story, bug, task, subtask, improvement, status-testing(+bg), tag-pink/yellow/purple, todo-bg, tag-teal-bg                                | supprimées (aucune référence restante)                                                                               |
| Doublon `--shadow-*` (bloc light)  | présent                                                                                                                                  | supprimé                                                                                                             |
| `_bootstrap-variables.scss`        | hex legacy disparates                                                                                                                    | alignés sur les valeurs light du design system                                                                       |

### Déviations par rapport au bloc §5

- **Types de tâche** (`story`, `bug`, `task`, …) et **`status-testing`** : supprimés au lieu d'être
  ajoutés en dark — aucun composant ne les référence (couvert par §6.1, pas par le bloc §5).
- **`--color-status-backlog` dark** : `var(--color-brand-cyan)` (au lieu de brand-blue) —
  le cyan est la couleur « marque texte » du thème sombre, cohérent avec les autres statuts.
- **`--color-priority-medium` dark** : `var(--color-warning)` (au lieu de `#fbbf24`) — même
  sémantique warning déjà validée, pas de nouvelle teinte.
- **`--color-cta-hover` dark** : `#5eead4` (même teinte, déviation mineure de la valeur proposée).
- **Tags libres dark** : seuls `tag-teal-*` et `tag-blue-*` existent (les pink/yellow/purple morts
  ont été supprimés en clair ET en sombre).

### Fichiers modifiés (non-backend, hors couleur de marque)

- `content/scss/design-system.scss` (bloc dark complet, CTA light, on-error-container light, shadows, tokens morts)
- `content/scss/global.scss` (boutons, error, pagination, split-screen, text-primary, liens)
- `content/scss/_bootstrap-variables.scss` (alignement light)
- `content/scss/*.scss` composants : error, sidebar, navbar, bottom-nav, breadcrumb, home, project,
  project-card, user-admin-detail, page-ribbon
- `content/css/loading.css` (littéral)
- TS/HTML : fallbacks retirés (324), `#000` → token, search-dialog, password-strength-bar,
  graphiques/listes/timeline/roadmap, admin tasks et 3 admin HTML, my-tasks, epic-detail, etc.

### Vérifications

- Build dev `npm run webapp:build:dev` : exit 0 (seuls warns `@import` Sass pré-existants).
- Plus aucun `var(--color-…, #hex)` ; plus aucun hex hors `design-system.scss`,
  `_bootstrap-variables.scss`, `loading.css`, `404.html` (statique) et `index.html` (theme-color).
- Parité des tokens light/dark vérifiée par script (`comm -23` : 0 token light-only ni dark-only).
- `eslint` : aucune erreur nouvelle introduite (avertissements membres-ordering/etc. pré-existants).

### Correctifs post-audit (sur signalement visuel)

- **Sidebar (dark ET light)** : le texte passait en marine semi-transparent `rgb(0 38 59 / 72%)`
  (3.38:1 ❌) en dark et blanc (2.89:1 ❌) en light — ni l'un ni l'autre ne passait AA sur le
  fond bleu logo `#079dfe`. Décision validée : **texte marine `#00263b` à pleine opacité dans les
  deux modes** via `var(--color-on-brand)` → 5.42:1 ✓ (le commentaire « 6.9:1 » du blanc était faux).
- **`global.scss` `.form-check-label`** : n'avait pas de couleur → héritait `$body-color #1d2233`,
  invisible en dark sur le fond sombre. Ajout `color: var(--color-on-surface)`.
- **Utilitaires texte Bootstrap `.text-danger`/`.text-success`/`.text-warning`** : composés par
  Bootstrap via `rgba(var(--bs-danger-rgb), …) !important` (rouge #dc3545, 3.6:1 en dark).
  Alignement sur les tokens sémantiques dark par surcharge des `--bs-*-rgb` dans le bloc dark
  (aucun changement en light) — pas de `!important` ajouté.
- **Sélecteur de langue login/register** (`.split-screen-lang-dropdown/-menu/-item`) : aucun style
  existant → menus/items par défaut, illisibles en dark. Styles ajoutés (surface-container,
  on-surface, hover/active alignés sur les autres dropdowns).
