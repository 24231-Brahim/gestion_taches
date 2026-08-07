-- ============================================================================
-- SCRIPT SQL DE SEED : gestion_taches (PostgreSQL)
-- ============================================================================
-- Relation: User -> Project -> Sprint/Epic -> Task -> Comment
--
-- Ordre d'insertion (respecte les clés étrangères):
--   1. jhi_authority        (rôles disponibles)
--   2. jhi_user             (10 utilisateurs)
--   3. jhi_user_authority   (association user <-> rôle)
--   4. project              (20 projets)
--   5. sprint               (3 sprints par projet = 60)
--   6. epic                 (3 epics par projet = 60)
--   7. task                 (60 tâches par projet = 1200)
--   8. comment              (commentaires sur les tâches)
--   9. project_member       (membres de chaque projet)
--  10. Mise à jour des séquences
--
-- Pour exécuter:
--   psql -U postgres -d gestion_taches -f seed-data.sql
-- ============================================================================
-- ATTENTION: Ce script supprime toutes les données existantes avant d'insérer
-- les nouvelles données. À n'utiliser qu'en développement/test.
-- ============================================================================

-- ============================================================================
-- NETTOYAGE: Suppression des données existantes (ordre inverse des FK)
-- ============================================================================
TRUNCATE task_history, comment, task,
         sprint, epic, project_member, project,
         notification, attachment, group_message,
         jhi_user_authority, jhi_user, jhi_authority
CASCADE;

-- ============================================================================
-- 1. ROLES (jhi_authority)
-- ============================================================================
INSERT INTO jhi_authority (name) VALUES
  ('ROLE_ADMIN'),
  ('ROLE_USER'),
  ('ROLE_PROJET_MANAGER'),
  ('ROLE_DEVELOPER');

-- ============================================================================
-- 2. UTILISATEURS (jhi_user)
-- ============================================================================
-- Mots de passe: BCrypt de "admin" pour admin, BCrypt de "user" pour les autres
INSERT INTO jhi_user (id, login, password_hash, first_name, last_name, email, activated, lang_key, created_by, created_date) VALUES
  (1,  'admin',    '$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC', 'Ahmed',   'Benali',     'ahmed.benali@example.com',    true, 'fr', 'system', NOW()),
  (2,  'user1',    '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'Karim',   'Meziane',    'karim.meziane@example.com',   true, 'fr', 'system', NOW()),
  (3,  'user2',    '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'Sara',    'Hadj',       'sara.hadj@example.com',        true, 'fr', 'system', NOW()),
  (4,  'manager1', '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'Yacine',  'Khelifi',    'yacine.khelifi@example.com',  true, 'fr', 'system', NOW()),
  (5,  'manager2', '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'Amira',   'Bouzid',     'amira.bouzid@example.com',    true, 'fr', 'system', NOW()),
  (6,  'dev1',     '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'Omar',    'Taleb',      'omar.taleb@example.com',      true, 'fr', 'system', NOW()),
  (7,  'dev2',     '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'Nadia',   'Cherif',     'nadia.cherif@example.com',    true, 'fr', 'system', NOW()),
  (8,  'dev3',     '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'Rachid',  'Hamidi',     'rachid.hamidi@example.com',   true, 'fr', 'system', NOW()),
  (9,  'dev4',     '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'Leila',   'Ait Ahmed',  'leila.aitahmed@example.com',  true, 'fr', 'system', NOW()),
  (10, 'viewer1',  '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'Youcef',  'Brahimi',    'youcef.brahimi@example.com',  true, 'fr', 'system', NOW());

-- ============================================================================
-- 3. ROLES UTILISATEURS (jhi_user_authority)
-- ============================================================================
INSERT INTO jhi_user_authority (user_id, authority_name) VALUES
  (1,  'ROLE_ADMIN'), (1,  'ROLE_USER'),
  (2,  'ROLE_USER'),
  (3,  'ROLE_USER'),
  (4,  'ROLE_PROJET_MANAGER'), (4,  'ROLE_USER'),
  (5,  'ROLE_PROJET_MANAGER'), (5,  'ROLE_USER'),
  (6,  'ROLE_DEVELOPER'), (6,  'ROLE_USER'),
  (7,  'ROLE_DEVELOPER'), (7,  'ROLE_USER'),
  (8,  'ROLE_DEVELOPER'), (8,  'ROLE_USER'),
  (9,  'ROLE_DEVELOPER'), (9,  'ROLE_USER'),
  (10, 'ROLE_USER');

-- ============================================================================
-- 4. PROJETS (20 projets, IDs 11-30)
-- ============================================================================
INSERT INTO project (id, name, description, project_key, created_at, owner_id) VALUES
  (11, 'Gestion Taches',      'Application web de gestion de taches et projets',            'GEST',  '2026-01-15 09:00:00', 1),
  (12, 'E-Commerce Platform', 'Plateforme de vente en ligne avec panier et paiement',        'ECOM',  '2026-01-20 10:30:00', 4),
  (13, 'Blog API',            'API REST pour un systeme de blog avec commentaires',          'BLOG',  '2026-02-01 08:00:00', 5),
  (14, 'Inventory System',    'Systeme de gestion de stock et d''approvisionnement',         'INV',   '2026-02-10 11:00:00', 1),
  (15, 'Chat Application',    'Application de messagerie en temps reel',                     'CHAT',  '2026-02-15 09:30:00', 4),
  (16, 'HR Management',       'Gestion des ressources humaines et paie',                    'HRMS',  '2026-03-01 08:00:00', 5),
  (17, 'Student Portal',      'Portail etudiant pour notes et emplois du temps',             'ETUD',  '2026-03-05 10:00:00', 1),
  (18, 'Healthcare App',      'Application de prise de rendez-vous medical',                 'HLTH',  '2026-03-10 09:00:00', 4),
  (19, 'Real Estate',         'Plateforme d''annonce immobiliere',                           'IMMO',  '2026-03-15 11:00:00', 5),
  (20, 'Food Delivery',       'Service de livraison de repas a domicile',                    'FOOD',  '2026-03-20 08:30:00', 1),
  (21, 'Library System',      'Systeme de gestion de bibliotheque et pret de livres',        'BIBL',  '2026-04-01 09:00:00', 4),
  (22, 'Parking Management',  'Gestion de places de parking et reservation',                 'PARK',  '2026-04-05 10:00:00', 5),
  (23, 'Event Management',    'Organisation et suivi d''evenements',                         'EVT',   '2026-04-10 08:00:00', 1),
  (24, 'Survey Platform',     'Creation et analyse de questionnaires en ligne',              'SURV',  '2026-04-15 11:30:00', 4),
  (25, 'Travel Agency',       'Gestion de voyages et reservations',                          'VOYG',  '2026-05-01 09:00:00', 5),
  (26, 'Fitness Tracker',     'Suivi d''entrainements et de calories',                       'FITN',  '2026-05-05 10:00:00', 1),
  (27, 'Invoice Generator',   'Generation et envoi de factures automatiques',                'FACT',  '2026-05-10 08:30:00', 4),
  (28, 'Task Scheduler',      'Planificateur de taches recurring et notifications',          'PLNV',  '2026-05-15 09:00:00', 5),
  (29, 'Wiki Platform',       'Encyclopedie collaborative interne',                          'WIKI',  '2026-06-01 10:00:00', 1),
  (30, 'Asset Tracking',      'Suivi des actifs materiels de l''entreprise',                 'ACTF',  '2026-06-05 08:00:00', 4);

-- ============================================================================
-- 5. SPRINTS (3 par projet = 60, IDs 31-90)
-- ============================================================================
-- Par projet: Sprint 1 = COMPLETED, Sprint 2 = ACTIVE, Sprint 3 = PLANNED
-- Dates décalées de 14 jours entre sprints
INSERT INTO sprint (id, name, goal, start_date, end_date, status, project_id) VALUES
  (31, 'Sprint 1', 'Mise en place architecture et authentification',     '2026-01-20', '2026-02-03', 'COMPLETED', 11),
  (32, 'Sprint 2', 'Development des fonctionnalites principales',        '2026-02-03', '2026-02-17', 'ACTIVE',    11),
  (33, 'Sprint 3', 'Tests, optimisation et deploiement',                 '2026-02-17', '2026-03-03', 'PLANNED',   11),
  (34, 'Sprint 1', 'Creation du catalogue produits',                     '2026-01-25', '2026-02-08', 'COMPLETED', 12),
  (35, 'Sprint 2', 'Systeme de panier et de commande',                   '2026-02-08', '2026-02-22', 'ACTIVE',    12),
  (36, 'Sprint 3', 'Integration du paiement et livraison',               '2026-02-22', '2026-03-08', 'PLANNED',   12),
  (37, 'Sprint 1', 'CRUD articles et utilisateurs',                      '2026-02-05', '2026-02-19', 'COMPLETED', 13),
  (38, 'Sprint 2', 'Systeme de commentaires et moderation',              '2026-02-19', '2026-03-05', 'ACTIVE',    13),
  (39, 'Sprint 3', 'Moteur de recherche et tags',                        '2026-03-05', '2026-03-19', 'PLANNED',   13),
  (40, 'Sprint 1', 'Gestion des produits et categories',                 '2026-02-15', '2026-03-01', 'COMPLETED', 14),
  (41, 'Sprint 2', 'Suivi des mouvements de stock',                      '2026-03-01', '2026-03-15', 'ACTIVE',    14),
  (42, 'Sprint 3', 'Systeme d''alertes et rapports',                     '2026-03-15', '2026-03-29', 'PLANNED',   14),
  (43, 'Sprint 1', 'Messages privees en temps reel',                     '2026-02-20', '2026-03-05', 'COMPLETED', 15),
  (44, 'Sprint 2', 'Discussions de groupe et notifications',             '2026-03-05', '2026-03-19', 'ACTIVE',    15),
  (45, 'Sprint 3', 'Envoi de fichiers et images',                        '2026-03-19', '2026-04-02', 'PLANNED',   15),
  (46, 'Sprint 1', 'CRUD employes et contrats',                          '2026-03-05', '2026-03-19', 'COMPLETED', 16),
  (47, 'Sprint 2', 'Gestion des conges et absences',                     '2026-03-19', '2026-04-02', 'ACTIVE',    16),
  (48, 'Sprint 3', 'Bulletin de paie et declarations',                   '2026-04-02', '2026-04-16', 'PLANNED',   16),
  (49, 'Sprint 1', 'Authentification et profils etudiants',              '2026-03-10', '2026-03-24', 'COMPLETED', 17),
  (50, 'Sprint 2', 'Affichage et calcul des notes',                      '2026-03-24', '2026-04-07', 'ACTIVE',    17),
  (51, 'Sprint 3', 'Emplois du temps et calendrier',                     '2026-04-07', '2026-04-21', 'PLANNED',   17),
  (52, 'Sprint 1', 'Gestion des dossiers patients',                      '2026-03-15', '2026-03-29', 'COMPLETED', 18),
  (53, 'Sprint 2', 'Prise et gestion de rendez-vous',                    '2026-03-29', '2026-04-12', 'ACTIVE',    18),
  (54, 'Sprint 3', 'Generation d''ordonnances et historique',             '2026-04-12', '2026-04-26', 'PLANNED',   18),
  (55, 'Sprint 1', 'Publication et recherche d''annonces',               '2026-03-20', '2026-04-03', 'COMPLETED', 19),
  (56, 'Sprint 2', 'Planification des visites',                          '2026-04-03', '2026-04-17', 'ACTIVE',    19),
  (57, 'Sprint 3', 'Gestion des contrats et signatures',                 '2026-04-17', '2026-05-01', 'PLANNED',   19),
  (58, 'Sprint 1', 'Liste des restaurants et menus',                     '2026-03-25', '2026-04-08', 'COMPLETED', 20),
  (59, 'Sprint 2', 'Systeme de commande et suivi',                       '2026-04-08', '2026-04-22', 'ACTIVE',    20),
  (60, 'Sprint 3', 'Gestion des livreurs et itineraire',                 '2026-04-22', '2026-05-06', 'PLANNED',   20),
  (61, 'Sprint 1', 'Catalogue de livres et recherche',                   '2026-04-05', '2026-04-19', 'COMPLETED', 21),
  (62, 'Sprint 2', 'Gestion des prets et retours',                       '2026-04-19', '2026-05-03', 'ACTIVE',    21),
  (63, 'Sprint 3', 'Gestion des membres et penalites',                   '2026-05-03', '2026-05-17', 'PLANNED',   21),
  (64, 'Sprint 1', 'Affichage des places disponibles',                   '2026-04-10', '2026-04-24', 'COMPLETED', 22),
  (65, 'Sprint 2', 'Systeme de reservation en ligne',                    '2026-04-24', '2026-05-08', 'ACTIVE',    22),
  (66, 'Sprint 3', 'Paiement automatique et tarification',               '2026-05-08', '2026-05-22', 'PLANNED',   22),
  (67, 'Sprint 1', 'Creation et publication d''evenements',              '2026-04-15', '2026-04-29', 'COMPLETED', 23),
  (68, 'Sprint 2', 'Gestion des inscriptions et paiements',              '2026-04-29', '2026-05-13', 'ACTIVE',    23),
  (69, 'Sprint 3', 'Generation de certificats et badges',                '2026-05-13', '2026-05-27', 'PLANNED',   23),
  (70, 'Sprint 1', 'Creation de questionnaires',                         '2026-04-20', '2026-05-04', 'COMPLETED', 24),
  (71, 'Sprint 2', 'Collecte et stockage des reponses',                  '2026-05-04', '2026-05-18', 'ACTIVE',    24),
  (72, 'Sprint 3', 'Tableaux de bord et analyse de donnees',             '2026-05-18', '2026-06-01', 'PLANNED',   24),
  (73, 'Sprint 1', 'Catalogue de destinations et voyages',               '2026-05-05', '2026-05-19', 'COMPLETED', 25),
  (74, 'Sprint 2', 'Systeme de reservation et paiement',                 '2026-05-19', '2026-06-02', 'ACTIVE',    25),
  (75, 'Sprint 3', 'Systeme d''avis et evaluations',                     '2026-06-02', '2026-06-16', 'PLANNED',   25),
  (76, 'Sprint 1', 'Creation de profil et objectifs',                    '2026-05-10', '2026-05-24', 'COMPLETED', 26),
  (77, 'Sprint 2', 'Suivi des seances d''entrainement',                  '2026-05-24', '2026-06-07', 'ACTIVE',    26),
  (78, 'Sprint 3', 'Suivi alimentaire et calories',                      '2026-06-07', '2026-06-21', 'PLANNED',   26),
  (79, 'Sprint 1', 'Modeles de factures personnalisables',               '2026-05-15', '2026-05-29', 'COMPLETED', 27),
  (80, 'Sprint 2', 'Generation automatique de factures',                 '2026-05-29', '2026-06-12', 'ACTIVE',    27),
  (81, 'Sprint 3', 'Envoi par email et relances',                        '2026-06-12', '2026-06-26', 'PLANNED',   27),
  (82, 'Sprint 1', 'Interface de planification de taches',               '2026-05-20', '2026-06-03', 'COMPLETED', 28),
  (83, 'Sprint 2', 'Taches recurring et pattern',                        '2026-06-03', '2026-06-17', 'ACTIVE',    28),
  (84, 'Sprint 3', 'Systeme de notifications et rappels',                '2026-06-17', '2026-07-01', 'PLANNED',   28),
  (85, 'Sprint 1', 'Creation et edition de pages wiki',                  '2026-06-05', '2026-06-19', 'COMPLETED', 29),
  (86, 'Sprint 2', 'Edition collaborative et historique',                '2026-06-19', '2026-07-03', 'ACTIVE',    29),
  (87, 'Sprint 3', 'Arborescence et categorisation',                     '2026-07-03', '2026-07-17', 'PLANNED',   29),
  (88, 'Sprint 1', 'Inventaire des actifs materiels',                    '2026-06-10', '2026-06-24', 'COMPLETED', 30),
  (89, 'Sprint 2', 'Scan et identification par QR code',                 '2026-06-24', '2026-07-08', 'ACTIVE',    30),
  (90, 'Sprint 3', 'Planification de la maintenance preventive',         '2026-07-08', '2026-07-22', 'PLANNED',   30);

-- ============================================================================
-- 6. EPICS (3 par projet = 60, IDs 91-150)
-- ============================================================================
-- Par projet: Epic 1 = DONE, Epic 2 = IN_PROGRESS, Epic 3 = TODO
INSERT INTO epic (id, title, description, status, priority, created_at, updated_at, start_date, end_date, project_id) VALUES
  (91,  'Authentification',      'Systeme de connexion et gestion des sessions',           'DONE',         'HIGH',     '2026-01-15 09:00:00', '2026-02-03 16:00:00', '2026-01-20', '2026-02-03', 11),
  (92,  'Gestion des taches',    'CRUD complet des taches avec statuts et priorites',     'IN_PROGRESS',  'HIGHEST',  '2026-01-15 09:00:00', '2026-02-10 14:00:00', '2026-02-03', '2026-02-17', 11),
  (93,  'Rapports et stats',     'Tableaux de bord et statistiques de productivite',      'TODO',         'MEDIUM',   '2026-01-15 09:00:00', NULL,                   '2026-02-17', '2026-03-03', 11),
  (94,  'Catalogue produits',    'Affichage et recherche de produits',                    'DONE',         'HIGH',     '2026-01-20 10:30:00', '2026-02-08 17:00:00', '2026-01-25', '2026-02-08', 12),
  (95,  'Panier et commande',    'Ajout au panier, modification, validation',             'IN_PROGRESS',  'HIGHEST',  '2026-01-20 10:30:00', '2026-02-15 11:00:00', '2026-02-08', '2026-02-22', 12),
  (96,  'Paiement en ligne',     'Integration Stripe et PayPal',                          'TODO',         'HIGH',     '2026-01-20 10:30:00', NULL,                   '2026-02-22', '2026-03-08', 12),
  (97,  'Articles',              'CRUD articles avec Markdown',                            'DONE',         'HIGH',     '2026-02-01 08:00:00', '2026-02-19 16:00:00', '2026-02-05', '2026-02-19', 13),
  (98,  'Commentaires',          'Systeme de commentaires hierarchiques',                  'IN_PROGRESS',  'MEDIUM',   '2026-02-01 08:00:00', '2026-02-25 10:00:00', '2026-02-19', '2026-03-05', 13),
  (99,  'Recherche et tags',     'Moteur de recherche full-text et systeme de tags',      'TODO',         'MEDIUM',   '2026-02-01 08:00:00', NULL,                   '2026-03-05', '2026-03-19', 13),
  (100, 'Gestion produits',      'CRUD produits avec categories et fournisseurs',          'DONE',         'HIGH',     '2026-02-10 11:00:00', '2026-03-01 15:00:00', '2026-02-15', '2026-03-01', 14),
  (101, 'Mouvements stock',      'Entrees, sorties et transferts de stock',               'IN_PROGRESS',  'HIGHEST',  '2026-02-10 11:00:00', '2026-03-08 09:00:00', '2026-03-01', '2026-03-15', 14),
  (102, 'Alertes stock',         'Notifications de stock minimum et ruptures',            'TODO',         'HIGH',     '2026-02-10 11:00:00', NULL,                   '2026-03-15', '2026-03-29', 14),
  (103, 'Messages privees',      'Messagerie 1 a 1 en temps reel via WebSocket',          'DONE',         'HIGHEST',  '2026-02-15 09:30:00', '2026-03-05 17:00:00', '2026-02-20', '2026-03-05', 15),
  (104, 'Groupes',               'Creation de groupes et messages de groupe',              'IN_PROGRESS',  'HIGH',     '2026-02-15 09:30:00', '2026-03-12 11:00:00', '2026-03-05', '2026-03-19', 15),
  (105, 'Partage de fichiers',   'Envoi d''images, documents et audio',                   'TODO',         'MEDIUM',   '2026-02-15 09:30:00', NULL,                   '2026-03-19', '2026-04-02', 15),
  (106, 'Gestion employes',      'CRUD employes, contrats et documents',                  'DONE',         'HIGH',     '2026-03-01 08:00:00', '2026-03-19 16:00:00', '2026-03-05', '2026-03-19', 16),
  (107, 'Conges et absences',    'Demandes de conges et validation manager',              'IN_PROGRESS',  'HIGHEST',  '2026-03-01 08:00:00', '2026-03-26 10:00:00', '2026-03-19', '2026-04-02', 16),
  (108, 'Bulletin de paie',      'Calcul automatique et generation de bulletins',         'TODO',         'HIGH',     '2026-03-01 08:00:00', NULL,                   '2026-04-02', '2026-04-16', 16),
  (109, 'Auth etudiants',        'Inscription et authentification etudiants',             'DONE',         'HIGH',     '2026-03-05 10:00:00', '2026-03-24 15:00:00', '2026-03-10', '2026-03-24', 17),
  (110, 'Gestion notes',         'Affichage des notes et calcul de moyenne',              'IN_PROGRESS',  'HIGHEST',  '2026-03-05 10:00:00', '2026-04-01 09:00:00', '2026-03-24', '2026-04-07', 17),
  (111, 'Emploi du temps',       'Calendrier et emplois du temps par filiere',            'TODO',         'MEDIUM',   '2026-03-05 10:00:00', NULL,                   '2026-04-07', '2026-04-21', 17),
  (112, 'Dossiers patients',     'Creation et consultation des dossiers medicaux',        'DONE',         'HIGHEST',  '2026-03-10 09:00:00', '2026-03-29 16:00:00', '2026-03-15', '2026-03-29', 18),
  (113, 'Rendez-vous',           'Prise de RDV en ligne et planning medecin',             'IN_PROGRESS',  'HIGH',     '2026-03-10 09:00:00', '2026-04-05 11:00:00', '2026-03-29', '2026-04-12', 18),
  (114, 'Ordonnances',           'Generation et historique des ordonnances',              'TODO',         'HIGH',     '2026-03-10 09:00:00', NULL,                   '2026-04-12', '2026-04-26', 18),
  (115, 'Annonces immo',         'Publication et recherche d''annonces',                  'DONE',         'HIGH',     '2026-03-15 11:00:00', '2026-04-03 15:00:00', '2026-03-20', '2026-04-03', 19),
  (116, 'Planification visites', 'Prise de RDV pour visites de biens',                    'IN_PROGRESS',  'MEDIUM',   '2026-03-15 11:00:00', '2026-04-10 10:00:00', '2026-04-03', '2026-04-17', 19),
  (117, 'Contrats location',     'Gestion des contrats et signatures numeriques',         'TODO',         'HIGH',     '2026-03-15 11:00:00', NULL,                   '2026-04-17', '2026-05-01', 19),
  (118, 'Restaurants et menus',  'Liste des restaurants et menus',                        'DONE',         'HIGH',     '2026-03-20 08:30:00', '2026-04-08 16:00:00', '2026-03-25', '2026-04-08', 20),
  (119, 'Systeme de commandes',  'Passer commande et suivi en temps reel',                'IN_PROGRESS',  'HIGHEST',  '2026-03-20 08:30:00', '2026-04-15 11:00:00', '2026-04-08', '2026-04-22', 20),
  (120, 'Livraison',             'Gestion des livreurs et suivi GPS',                     'TODO',         'HIGH',     '2026-03-20 08:30:00', NULL,                   '2026-04-22', '2026-05-06', 20),
  (121, 'Catalogue livres',      'Base de donnees de livres et recherche',                'DONE',         'HIGH',     '2026-04-01 09:00:00', '2026-04-19 15:00:00', '2026-04-05', '2026-04-19', 21),
  (122, 'Systeme de prets',      'Emprunt, retour et prolongation',                       'IN_PROGRESS',  'HIGHEST',  '2026-04-01 09:00:00', '2026-04-26 10:00:00', '2026-04-19', '2026-05-03', 21),
  (123, 'Gestion membres',       'Inscriptions et penalites de retard',                   'TODO',         'MEDIUM',   '2026-04-01 09:00:00', NULL,                   '2026-05-03', '2026-05-17', 21),
  (124, 'Places disponibles',    'Affichage temps reel des places',                       'DONE',         'HIGH',     '2026-04-05 10:00:00', '2026-04-24 16:00:00', '2026-04-10', '2026-04-24', 22),
  (125, 'Reservation en ligne',  'Reserver et annuler une place',                          'IN_PROGRESS',  'HIGHEST',  '2026-04-05 10:00:00', '2026-05-01 11:00:00', '2026-04-24', '2026-05-08', 22),
  (126, 'Paiement stationnement','Paiement automatique et tarification',                  'TODO',         'HIGH',     '2026-04-05 10:00:00', NULL,                   '2026-05-08', '2026-05-22', 22),
  (127, 'Creation evenements',   'Creer et publier des evenements',                       'DONE',         'HIGH',     '2026-04-10 08:00:00', '2026-04-29 15:00:00', '2026-04-15', '2026-04-29', 23),
  (128, 'Inscriptions',          'Inscription en ligne et gestion des places',            'IN_PROGRESS',  'HIGHEST',  '2026-04-10 08:00:00', '2026-05-06 10:00:00', '2026-04-29', '2026-05-13', 23),
  (129, 'Badges et certificats', 'Generation de badges et certificats de participation',   'TODO',         'MEDIUM',   '2026-04-10 08:00:00', NULL,                   '2026-05-13', '2026-05-27', 23),
  (130, 'Questionnaires',        'Creation de formulaires et questionnaires',              'DONE',         'HIGH',     '2026-04-15 11:30:00', '2026-05-04 16:00:00', '2026-04-20', '2026-05-04', 24),
  (131, 'Collecte reponses',     'Soumission et stockage des reponses',                   'IN_PROGRESS',  'HIGHEST',  '2026-04-15 11:30:00', '2026-05-11 09:00:00', '2026-05-04', '2026-05-18', 24),
  (132, 'Analyse de donnees',    'Graphiques et statistiques de reponses',                'TODO',         'MEDIUM',   '2026-04-15 11:30:00', NULL,                   '2026-05-18', '2026-06-01', 24),
  (133, 'Destinations',          'Catalogue de voyages et destinations',                  'DONE',         'HIGH',     '2026-05-01 09:00:00', '2026-05-19 15:00:00', '2026-05-05', '2026-05-19', 25),
  (134, 'Reservations voyage',   'Booking et paiement de voyages',                        'IN_PROGRESS',  'HIGHEST',  '2026-05-01 09:00:00', '2026-05-26 11:00:00', '2026-05-19', '2026-06-02', 25),
  (135, 'Avis voyageurs',        'Systeme d''avis et evaluations',                        'TODO',         'LOW',      '2026-05-01 09:00:00', NULL,                   '2026-06-02', '2026-06-16', 25),
  (136, 'Profil sportif',        'Creation de profil et objectifs personnels',            'DONE',         'HIGH',     '2026-05-05 10:00:00', '2026-05-24 16:00:00', '2026-05-10', '2026-05-24', 26),
  (137, 'Seances entrainement',  'Journal d''entrainements et progression',               'IN_PROGRESS',  'HIGHEST',  '2026-05-05 10:00:00', '2026-06-01 10:00:00', '2026-05-24', '2026-06-07', 26),
  (138, 'Suivi nutrition',       'Calcul calorique et plan alimentaire',                  'TODO',         'MEDIUM',   '2026-05-05 10:00:00', NULL,                   '2026-06-07', '2026-06-21', 26),
  (139, 'Modeles factures',      'Templates personnalisables pour factures',              'DONE',         'HIGH',     '2026-05-10 08:30:00', '2026-05-29 15:00:00', '2026-05-15', '2026-05-29', 27),
  (140, 'Generation auto',       'Generation automatique de factures PDF',                'IN_PROGRESS',  'HIGHEST',  '2026-05-10 08:30:00', '2026-06-05 11:00:00', '2026-05-29', '2026-06-12', 27),
  (141, 'Envoi et relances',     'Email automatique et relances impayees',                'TODO',         'HIGH',     '2026-05-10 08:30:00', NULL,                   '2026-06-12', '2026-06-26', 27),
  (142, 'Planning taches',       'Interface drag-and-drop pour planifier',                'DONE',         'HIGH',     '2026-05-15 09:00:00', '2026-06-03 16:00:00', '2026-05-20', '2026-06-03', 28),
  (143, 'Taches recurring',      'Pattern journalier, hebdo, mensuel',                    'IN_PROGRESS',  'HIGHEST',  '2026-05-15 09:00:00', '2026-06-10 10:00:00', '2026-06-03', '2026-06-17', 28),
  (144, 'Notifications',         'Email et push notifications de rappels',                'TODO',         'MEDIUM',   '2026-05-15 09:00:00', NULL,                   '2026-06-17', '2026-07-01', 28),
  (145, 'Pages wiki',            'Creation et edition de pages',                         'DONE',         'HIGH',     '2026-06-01 10:00:00', '2026-06-19 15:00:00', '2026-06-05', '2026-06-19', 29),
  (146, 'Edition collaborative', 'Travail en simultane et historique des versions',       'IN_PROGRESS',  'HIGHEST',  '2026-06-01 10:00:00', '2026-06-26 11:00:00', '2026-06-19', '2026-07-03', 29),
  (147, 'Arborescence',          'Categories, sous-categories et navigation',             'TODO',         'LOW',      '2026-06-01 10:00:00', NULL,                   '2026-07-03', '2026-07-17', 29),
  (148, 'Inventaire actifs',     'Liste et description des actifs',                       'DONE',         'HIGH',     '2026-06-05 08:00:00', '2026-06-24 16:00:00', '2026-06-10', '2026-06-24', 30),
  (149, 'Identification QR',     'Scan de codes QR pour inventaire rapide',               'IN_PROGRESS',  'HIGHEST',  '2026-06-05 08:00:00', '2026-07-01 10:00:00', '2026-06-24', '2026-07-08', 30),
  (150, 'Maintenance',           'Planification de maintenance preventive',               'TODO',         'MEDIUM',   '2026-06-05 08:00:00', NULL,                   '2026-07-08', '2026-07-22', 30);
-- ====================================================================
-- 7. TACHES (60 par projet = 1200, IDs 151-1350)
-- ====================================================================
INSERT INTO task (id, title, description, status, priority, created_at, updated_at, sprint_id, epic_id, project_id, assignee_id, created_by_id) VALUES
  
  (151,'Page de connexion avec formulaire','Page de connexion avec formulaire','DONE','HIGH','2026-01-20 09:00:00','2026-02-25 16:00:00',31,91,11,6,5)
,
  (152,'Inscription utilisateur avec validation','Inscription utilisateur avec validation','DONE','MEDIUM','2026-01-21 09:00:00','2026-02-28 16:00:00',31,91,11,7,1)
,
  (153,'Gestion des sessions JWT','Gestion des sessions JWT','DONE','HIGHEST','2026-01-22 09:00:00','2026-02-28 16:00:00',31,91,11,6,5)
,
  (154,'Reset mot de passe par email','Reset mot de passe par email','DONE','MEDIUM','2026-01-23 09:00:00','2026-02-27 16:00:00',31,91,11,6,1)
,
  (155,'Middleware authentification API','Middleware authentification API','DONE','LOW','2026-01-24 09:00:00','2026-02-28 16:00:00',31,91,11,6,5)
,
  (156,'Protection des routes privees','Protection des routes privees','IN_REVIEW','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',31,91,11,9,1)
,
  (157,'Logout et destruction session','Logout et destruction session','IN_REVIEW','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',31,91,11,8,1)
,
  (158,'Refresh token automatique','Refresh token automatique','DONE','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',31,91,11,8,4)
,
  (159,'Double authentification 2FA','Double authentification 2FA','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',31,91,11,8,1)
,
  (160,'Connexion avec Google OAuth','Connexion avec Google OAuth','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',31,91,11,6,4)
,
  (161,'Historique des connexions','Historique des connexions','IN_REVIEW','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',31,91,11,8,1)
,
  (162,'Verrouillage compte apres echecs','Verrouillage compte apres echecs','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',31,91,11,6,4)
,
  (163,'Gestion des roles utilisateur','Gestion des roles utilisateur','IN_REVIEW','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',31,91,11,8,5)
,
  (164,'Permissions par role','Permissions par role','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',31,91,11,7,5)
,
  (165,'Token activation par email','Token activation par email','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',31,91,11,7,4)
,
  (166,'Profils utilisateur modifiables','Profils utilisateur modifiables','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',31,91,11,6,4)
,
  (167,'Avatar et photo de profil','Avatar et photo de profil','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',31,91,11,8,1)
,
  (168,'Changer mot de passe depuis profil','Changer mot de passe depuis profil','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',31,91,11,7,5)
,
  (169,'Notification appareil inconnu','Notification appareil inconnu','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',31,91,11,7,5)
,
  (170,'Export donnees RGPD','Export donnees RGPD','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',31,91,11,7,4)
,
  (171,'Creer une nouvelle tache','Creer une nouvelle tache','IN_PROGRESS','HIGH','2026-02-03 09:00:00','2026-02-07 14:00:00',32,92,11,7,5)
,
  (172,'Afficher le tableau Kanban','Afficher le tableau Kanban','IN_PROGRESS','MEDIUM','2026-02-04 09:00:00','2026-02-08 14:00:00',32,92,11,7,1)
,
  (173,'Modifier le titre d une tache','Modifier le titre d une tache','TODO','HIGHEST','2026-02-05 09:00:00','2026-02-08 14:00:00',32,92,11,8,1)
,
  (174,'Supprimer tache avec confirmation','Supprimer tache avec confirmation','TODO','MEDIUM','2026-02-06 09:00:00','2026-02-11 14:00:00',32,92,11,8,1)
,
  (175,'Filtrer les taches par statut','Filtrer les taches par statut','TODO','LOW','2026-02-07 09:00:00','2026-02-11 14:00:00',32,92,11,9,1)
,
  (176,'Drag and drop entre colonnes Kanban','Drag and drop entre colonnes Kanban','IN_PROGRESS','MEDIUM','2026-02-08 09:00:00','2026-02-14 14:00:00',32,92,11,7,5)
,
  (177,'Assigner utilisateur a une tache','Assigner utilisateur a une tache','TODO','HIGH','2026-02-09 09:00:00','2026-02-15 14:00:00',32,92,11,8,5)
,
  (178,'Historique changements de statut','Historique changements de statut','TODO','HIGHEST','2026-02-10 09:00:00','2026-02-13 14:00:00',32,92,11,9,4)
,
  (179,'Notifications push aux assignes','Notifications push aux assignes','IN_PROGRESS','LOW','2026-02-11 09:00:00','2026-02-13 14:00:00',32,92,11,9,1)
,
  (180,'Recherche par titre et description','Recherche par titre et description','IN_PROGRESS','MEDIUM','2026-02-12 09:00:00','2026-02-15 14:00:00',32,92,11,7,5)
,
  (181,'Ordre de priorite automatique','Ordre de priorite automatique','TODO','HIGH','2026-02-13 09:00:00','2026-02-18 14:00:00',32,92,11,6,4)
,
  (182,'Echelle de temps par tache','Echelle de temps par tache','TODO','MEDIUM','2026-02-14 09:00:00','2026-02-18 14:00:00',32,92,11,9,5)
,
  (183,'Mode vue liste alternatif','Mode vue liste alternatif','TODO','HIGHEST','2026-02-15 09:00:00','2026-02-17 14:00:00',32,92,11,6,5)
,
  (184,'Copier une tache existante','Copier une tache existante','TODO','LOW','2026-02-16 09:00:00','2026-02-20 14:00:00',32,92,11,8,5)
,
  (185,'Archiver une tache terminee','Archiver une tache terminee','IN_PROGRESS','MEDIUM','2026-02-17 09:00:00','2026-02-20 14:00:00',32,92,11,8,4)
,
  (186,'Etiquettes et labels sur taches','Etiquettes et labels sur taches','TODO','HIGH','2026-02-18 09:00:00','2026-02-22 14:00:00',32,92,11,6,5)
,
  (187,'Sous-taches et checklist','Sous-taches et checklist','TODO','MEDIUM','2026-02-19 09:00:00','2026-02-21 14:00:00',32,92,11,7,5)
,
  (188,'Temps estime vs reel','Temps estime vs reel','IN_PROGRESS','LOW','2026-02-20 09:00:00','2026-02-24 14:00:00',32,92,11,7,1)
,
  (189,'Mode plein ecran pour tache','Mode plein ecran pour tache','IN_PROGRESS','HIGHEST','2026-02-21 09:00:00','2026-02-25 14:00:00',32,92,11,6,5)
,
  (190,'Dupliquer tache autre sprint','Dupliquer tache autre sprint','TODO','MEDIUM','2026-02-22 09:00:00','2026-02-26 14:00:00',32,92,11,6,1)
,
  (191,'Dashboard general avec compteurs','Dashboard general avec compteurs','TODO','HIGH','2026-02-17 09:00:00',NULL,33,93,11,7,1)
,
  (192,'Graphique de burndown du sprint','Graphique de burndown du sprint','TODO','MEDIUM','2026-02-18 09:00:00',NULL,33,93,11,6,1)
,
  (193,'Rapport par assigne','Rapport par assigne','NEW','HIGHEST','2026-02-19 09:00:00',NULL,33,93,11,6,5)
,
  (194,'Exporter les donnees en CSV','Exporter les donnees en CSV','TODO','MEDIUM','2026-02-20 09:00:00',NULL,33,93,11,7,5)
,
  (195,'Metriques de productivite equipe','Metriques de productivite equipe','NEW','LOW','2026-02-21 09:00:00',NULL,33,93,11,7,4)
,
  (196,'Rapport hebdomadaire automatique','Rapport hebdomadaire automatique','NEW','MEDIUM','2026-02-22 09:00:00',NULL,33,93,11,9,1)
,
  (197,'Diagramme de Gantt des sprints','Diagramme de Gantt des sprints','NEW','HIGH','2026-02-23 09:00:00',NULL,33,93,11,7,5)
,
  (198,'Objectifs et cles du sprint','Objectifs et cles du sprint','TODO','HIGHEST','2026-02-24 09:00:00',NULL,33,93,11,9,5)
,
  (199,'Velocity chart multi-sprints','Velocity chart multi-sprints','NEW','LOW','2026-02-25 09:00:00',NULL,33,93,11,9,5)
,
  (200,'Generer le rapport PDF','Generer le rapport PDF','NEW','MEDIUM','2026-02-26 09:00:00',NULL,33,93,11,6,1)
,
  (201,'Ajouter des filtres avances','Ajouter des filtres avances','TODO','HIGH','2026-02-27 09:00:00',NULL,33,93,11,6,4)
,
  (202,'Mode sombre interface','Mode sombre interface','TODO','MEDIUM','2026-02-28 09:00:00',NULL,33,93,11,7,5)
,
  (203,'Responsive design mobile','Responsive design mobile','TODO','HIGHEST','2026-02-28 09:00:00',NULL,33,93,11,6,1)
,
  (204,'Accessibilite WCAG','Accessibilite WCAG','TODO','LOW','2026-02-28 09:00:00',NULL,33,93,11,7,1)
,
  (205,'Internationalisation FR/EN','Internationalisation FR/EN','TODO','MEDIUM','2026-02-28 09:00:00',NULL,33,93,11,8,1)
,
  (206,'Graphique camembert repartition','Graphique camembert repartition','NEW','HIGH','2026-02-28 09:00:00',NULL,33,93,11,7,4)
,
  (207,'Export donnees JSON API','Export donnees JSON API','NEW','MEDIUM','2026-02-28 09:00:00',NULL,33,93,11,7,5)
,
  (208,'Comparaison sprints passes','Comparaison sprints passes','TODO','LOW','2026-02-28 09:00:00',NULL,33,93,11,9,1)
,
  (209,'Taux de completion projet','Taux de completion projet','NEW','HIGHEST','2026-02-28 09:00:00',NULL,33,93,11,9,1)
,
  (210,'Indicateur de risques','Indicateur de risques','TODO','MEDIUM','2026-02-28 09:00:00',NULL,33,93,11,6,5)
,
  (211,'Affichage grille des produits','Affichage grille des produits','DONE','HIGH','2026-01-20 09:00:00','2026-02-26 16:00:00',34,94,12,8,4)
,
  (212,'Page fiche produit detaillee','Page fiche produit detaillee','IN_REVIEW','MEDIUM','2026-01-21 09:00:00','2026-02-28 16:00:00',34,94,12,6,5)
,
  (213,'Categories et sous-categories','Categories et sous-categories','DONE','HIGHEST','2026-01-22 09:00:00','2026-02-28 16:00:00',34,94,12,6,4)
,
  (214,'Recherche avec filtres avances','Recherche avec filtres avances','DONE','MEDIUM','2026-01-23 09:00:00','2026-02-27 16:00:00',34,94,12,6,1)
,
  (215,'Affichage disponibilite stock','Affichage disponibilite stock','DONE','LOW','2026-01-24 09:00:00','2026-02-28 16:00:00',34,94,12,9,1)
,
  (216,'Bouton ajouter au panier','Bouton ajouter au panier','DONE','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',34,94,12,8,4)
,
  (217,'Modifier quantite dans panier','Modifier quantite dans panier','DONE','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',34,94,12,9,5)
,
  (218,'Resume panier sous-total','Resume panier sous-total','DONE','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',34,94,12,6,1)
,
  (219,'Validation de la commande','Validation de la commande','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',34,94,12,9,4)
,
  (220,'Historique des commandes','Historique des commandes','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',34,94,12,9,1)
,
  (221,'Appliquer code de reduction','Appliquer code de reduction','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',34,94,12,6,4)
,
  (222,'Panier persistant entre sessions','Panier persistant entre sessions','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',34,94,12,8,4)
,
  (223,'Alerte stock bas articles panier','Alerte stock bas articles panier','IN_REVIEW','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',34,94,12,9,1)
,
  (224,'Suggestions produits similaires','Suggestions produits similaires','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',34,94,12,7,1)
,
  (225,'Resume commande par email','Resume commande par email','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',34,94,12,6,5)
,
  (226,'Passerelle Stripe cartes bancaires','Passerelle Stripe cartes bancaires','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',34,94,12,6,5)
,
  (227,'Integration PayPal','Integration PayPal','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',34,94,12,7,1)
,
  (228,'Historique des transactions','Historique des transactions','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',34,94,12,7,1)
,
  (229,'Processus de remboursement','Processus de remboursement','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',34,94,12,7,4)
,
  (230,'Validation CVV 3D Secure','Validation CVV 3D Secure','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',34,94,12,7,5)
,
  (231,'Gestion des retours produits','Gestion des retours produits','IN_PROGRESS','HIGH','2026-02-03 09:00:00','2026-02-09 14:00:00',35,95,12,6,4)
,
  (232,'Suivi livraison temps reel','Suivi livraison temps reel','TODO','MEDIUM','2026-02-04 09:00:00','2026-02-07 14:00:00',35,95,12,8,4)
,
  (233,'Point fidelite client','Point fidelite client','IN_PROGRESS','HIGHEST','2026-02-05 09:00:00','2026-02-10 14:00:00',35,95,12,7,4)
,
  (234,'Programme parrainage','Programme parrainage','IN_PROGRESS','MEDIUM','2026-02-06 09:00:00','2026-02-10 14:00:00',35,95,12,8,4)
,
  (235,'Gestion catalogue promotionnel','Gestion catalogue promotionnel','IN_PROGRESS','LOW','2026-02-07 09:00:00','2026-02-13 14:00:00',35,95,12,6,4)
,
  (236,'Gestion fournisseurs produits','Gestion fournisseurs produits','TODO','MEDIUM','2026-02-08 09:00:00','2026-02-14 14:00:00',35,95,12,6,1)
,
  (237,'Stock minimum automatique','Stock minimum automatique','IN_PROGRESS','HIGH','2026-02-09 09:00:00','2026-02-13 14:00:00',35,95,12,8,1)
,
  (238,'Alerte rupture de stock','Alerte rupture de stock','IN_PROGRESS','HIGHEST','2026-02-10 09:00:00','2026-02-14 14:00:00',35,95,12,7,4)
,
  (239,'Import produits depuis CSV','Import produits depuis CSV','IN_PROGRESS','LOW','2026-02-11 09:00:00','2026-02-15 14:00:00',35,95,12,9,5)
,
  (240,'Export catalogue PDF','Export catalogue PDF','TODO','MEDIUM','2026-02-12 09:00:00','2026-02-18 14:00:00',35,95,12,6,5)
,
  (241,'Mode catalogue photos','Mode catalogue photos','IN_PROGRESS','HIGH','2026-02-13 09:00:00','2026-02-17 14:00:00',35,95,12,6,1)
,
  (242,'Comparateur de produits','Comparateur de produits','IN_PROGRESS','MEDIUM','2026-02-14 09:00:00','2026-02-20 14:00:00',35,95,12,6,5)
,
  (243,'Liste de souhaits client','Liste de souhaits client','IN_PROGRESS','HIGHEST','2026-02-15 09:00:00','2026-02-21 14:00:00',35,95,12,8,4)
,
  (244,'Historique prix produits','Historique prix produits','IN_PROGRESS','LOW','2026-02-16 09:00:00','2026-02-20 14:00:00',35,95,12,8,1)
,
  (245,'Rapport ventes par categorie','Rapport ventes par categorie','TODO','MEDIUM','2026-02-17 09:00:00','2026-02-19 14:00:00',35,95,12,9,4)
,
  (246,'Gestion des promotions','Gestion des promotions','IN_PROGRESS','HIGH','2026-02-18 09:00:00','2026-02-20 14:00:00',35,95,12,9,4)
,
  (247,'Pack et bundles produits','Pack et bundles produits','IN_PROGRESS','MEDIUM','2026-02-19 09:00:00','2026-02-23 14:00:00',35,95,12,8,1)
,
  (248,'Gestion garanties produits','Gestion garanties produits','IN_PROGRESS','LOW','2026-02-20 09:00:00','2026-02-25 14:00:00',35,95,12,9,5)
,
  (249,'Suivi satisfaction client','Suivi satisfaction client','TODO','HIGHEST','2026-02-21 09:00:00','2026-02-23 14:00:00',35,95,12,6,1)
,
  (250,'Avis et notes produits','Avis et notes produits','IN_PROGRESS','MEDIUM','2026-02-22 09:00:00','2026-02-28 14:00:00',35,95,12,6,4)
,
  (251,'Facture PDF automatique','Facture PDF automatique','NEW','HIGH','2026-02-17 09:00:00',NULL,36,96,12,7,4)
,
  (252,'Support multi-devises','Support multi-devises','TODO','MEDIUM','2026-02-18 09:00:00',NULL,36,96,12,6,4)
,
  (253,'Portefeuille credit interne','Portefeuille credit interne','NEW','HIGHEST','2026-02-19 09:00:00',NULL,36,96,12,6,4)
,
  (254,'Notification echec paiement','Notification echec paiement','TODO','MEDIUM','2026-02-20 09:00:00',NULL,36,96,12,7,5)
,
  (255,'Paiement recurring abonnement','Paiement recurring abonnement','TODO','LOW','2026-02-21 09:00:00',NULL,36,96,12,8,5)
,
  (256,'Gestion des tailles et couleurs','Gestion des tailles et couleurs','NEW','MEDIUM','2026-02-22 09:00:00',NULL,36,96,12,7,1)
,
  (257,'Programme fidelite points','Programme fidelite points','TODO','HIGH','2026-02-23 09:00:00',NULL,36,96,12,7,4)
,
  (258,'Notifications produits arrives','Notifications produits arrives','TODO','HIGHEST','2026-02-24 09:00:00',NULL,36,96,12,7,5)
,
  (259,'Catalogue saisonnier','Catalogue saisonnier','NEW','LOW','2026-02-25 09:00:00',NULL,36,96,12,9,5)
,
  (260,'Offres flash limitees','Offres flash limitees','TODO','MEDIUM','2026-02-26 09:00:00',NULL,36,96,12,8,1)
,
  (261,'Gestion stock multi-entrepots','Gestion stock multi-entrepots','TODO','HIGH','2026-02-27 09:00:00',NULL,36,96,12,9,1)
,
  (262,'Suivi commande groupee','Suivi commande groupee','NEW','MEDIUM','2026-02-28 09:00:00',NULL,36,96,12,7,1)
,
  (263,'Mode pre-commande','Mode pre-commande','NEW','HIGHEST','2026-02-28 09:00:00',NULL,36,96,12,8,4)
,
  (264,'Gestion catalogues temporaires','Gestion catalogues temporaires','TODO','LOW','2026-02-28 09:00:00',NULL,36,96,12,7,1)
,
  (265,'Alerte promotion personnalisee','Alerte promotion personnalisee','TODO','MEDIUM','2026-02-28 09:00:00',NULL,36,96,12,9,4)
,
  (266,'Rapport conversion panier','Rapport conversion panier','TODO','HIGH','2026-02-28 09:00:00',NULL,36,96,12,6,4)
,
  (267,'Gestion liste d attente produit','Gestion liste d attente produit','NEW','MEDIUM','2026-02-28 09:00:00',NULL,36,96,12,9,5)
,
  (268,'Mode vente privee','Mode vente privee','NEW','LOW','2026-02-28 09:00:00',NULL,36,96,12,8,1)
,
  (269,'Statistiques panier moyen','Statistiques panier moyen','TODO','HIGHEST','2026-02-28 09:00:00',NULL,36,96,12,8,1)
,
  (270,'Rapport fidelisation clients','Rapport fidelisation clients','NEW','MEDIUM','2026-02-28 09:00:00',NULL,36,96,12,8,1)
,
  (271,'API CRUD articles REST','API CRUD articles REST','DONE','HIGH','2026-01-20 09:00:00','2026-02-28 16:00:00',37,97,13,9,4)
,
  (272,'Authentification JWT auteurs','Authentification JWT auteurs','DONE','MEDIUM','2026-01-21 09:00:00','2026-02-28 16:00:00',37,97,13,9,5)
,
  (273,'Validation champs obligatoires','Validation champs obligatoires','DONE','HIGHEST','2026-01-22 09:00:00','2026-02-26 16:00:00',37,97,13,9,5)
,
  (274,'Pagination limit offset','Pagination limit offset','DONE','MEDIUM','2026-01-23 09:00:00','2026-02-28 16:00:00',37,97,13,6,5)
,
  (275,'Support Markdown dans articles','Support Markdown dans articles','DONE','LOW','2026-01-24 09:00:00','2026-02-28 16:00:00',37,97,13,7,4)
,
  (276,'Ajouter un commentaire','Ajouter un commentaire','DONE','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',37,97,13,8,5)
,
  (277,'Lister commentaires pagines','Lister commentaires pagines','DONE','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',37,97,13,8,5)
,
  (278,'Modifier son commentaire','Modifier son commentaire','DONE','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',37,97,13,8,4)
,
  (279,'Supprimer un commentaire','Supprimer un commentaire','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',37,97,13,7,1)
,
  (280,'Commentaires imbriques reponse','Commentaires imbriques reponse','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',37,97,13,7,5)
,
  (281,'Vote like dislike commentaires','Vote like dislike commentaires','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',37,97,13,9,5)
,
  (282,'Signalement commentaire inapproprie','Signalement commentaire inapproprie','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',37,97,13,8,1)
,
  (283,'Notifier auteur d un commentaire','Notifier auteur d un commentaire','IN_REVIEW','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',37,97,13,8,4)
,
  (284,'Detection anti-spam','Detection anti-spam','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',37,97,13,7,5)
,
  (285,'Exporter commentaires JSON','Exporter commentaires JSON','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',37,97,13,6,4)
,
  (286,'Recherche full-text articles','Recherche full-text articles','IN_REVIEW','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',37,97,13,8,1)
,
  (287,'Systeme de tags et categories','Systeme de tags et categories','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',37,97,13,7,1)
,
  (288,'Filtres date auteur categorie','Filtres date auteur categorie','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',37,97,13,6,1)
,
  (289,'Articles populaires par vues','Articles populaires par vues','IN_REVIEW','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',37,97,13,6,4)
,
  (290,'Articles lies en bas de page','Articles lies en bas de page','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',37,97,13,7,5)
,
  (291,'Gestion des brouillons','Gestion des brouillons','TODO','HIGH','2026-02-03 09:00:00','2026-02-06 14:00:00',38,98,13,9,4)
,
  (292,'Publication planifiee','Publication planifiee','IN_PROGRESS','MEDIUM','2026-02-04 09:00:00','2026-02-09 14:00:00',38,98,13,6,1)
,
  (293,'Versioning des articles','Versioning des articles','IN_PROGRESS','HIGHEST','2026-02-05 09:00:00','2026-02-11 14:00:00',38,98,13,7,5)
,
  (294,'Revision des articles','Revision des articles','TODO','MEDIUM','2026-02-06 09:00:00','2026-02-09 14:00:00',38,98,13,6,5)
,
  (295,'Mode lecture sans distractions','Mode lecture sans distractions','IN_PROGRESS','LOW','2026-02-07 09:00:00','2026-02-12 14:00:00',38,98,13,9,1)
,
  (296,'Statistiques vues par article','Statistiques vues par article','TODO','MEDIUM','2026-02-08 09:00:00','2026-02-14 14:00:00',38,98,13,8,4)
,
  (297,'Partage reseaux sociaux','Partage reseaux sociaux','TODO','HIGH','2026-02-09 09:00:00','2026-02-14 14:00:00',38,98,13,9,5)
,
  (298,'Articles favoris utilisateur','Articles favoris utilisateur','IN_PROGRESS','HIGHEST','2026-02-10 09:00:00','2026-02-14 14:00:00',38,98,13,9,4)
,
  (299,'Notification nouvel article','Notification nouvel article','IN_PROGRESS','LOW','2026-02-11 09:00:00','2026-02-16 14:00:00',38,98,13,8,5)
,
  (300,'Resume automatique article','Resume automatique article','IN_PROGRESS','MEDIUM','2026-02-12 09:00:00','2026-02-14 14:00:00',38,98,13,8,4)
,
  (301,'Mode sombre lecture','Mode sombre lecture','IN_PROGRESS','HIGH','2026-02-13 09:00:00','2026-02-17 14:00:00',38,98,13,7,4)
,
  (302,'Table des matieres auto','Table des matieres auto','IN_PROGRESS','MEDIUM','2026-02-14 09:00:00','2026-02-17 14:00:00',38,98,13,6,1)
,
  (303,'Serie d articles multi-parties','Serie d articles multi-parties','IN_PROGRESS','HIGHEST','2026-02-15 09:00:00','2026-02-18 14:00:00',38,98,13,9,5)
,
  (304,'Gestion invitations auteurs','Gestion invitations auteurs','IN_PROGRESS','LOW','2026-02-16 09:00:00','2026-02-21 14:00:00',38,98,13,6,4)
,
  (305,'Mode collaboration en direct','Mode collaboration en direct','IN_PROGRESS','MEDIUM','2026-02-17 09:00:00','2026-02-19 14:00:00',38,98,13,9,4)
,
  (306,'Quiz integre dans articles','Quiz integre dans articles','IN_PROGRESS','HIGH','2026-02-18 09:00:00','2026-02-24 14:00:00',38,98,13,9,4)
,
  (307,'Citations et references','Citations et references','IN_PROGRESS','MEDIUM','2026-02-19 09:00:00','2026-02-21 14:00:00',38,98,13,9,4)
,
  (308,'Export epub article','Export epub article','IN_PROGRESS','LOW','2026-02-20 09:00:00','2026-02-25 14:00:00',38,98,13,8,4)
,
  (309,'Mode hors ligne lecture','Mode hors ligne lecture','TODO','HIGHEST','2026-02-21 09:00:00','2026-02-24 14:00:00',38,98,13,7,4)
,
  (310,'Suggestion de tags auto','Suggestion de tags auto','IN_PROGRESS','MEDIUM','2026-02-22 09:00:00','2026-02-24 14:00:00',38,98,13,9,4)
,
  (311,'Flux RSS des articles','Flux RSS des articles','NEW','HIGH','2026-02-17 09:00:00',NULL,39,99,13,8,5)
,
  (312,'Autocomplete temps reel','Autocomplete temps reel','NEW','MEDIUM','2026-02-18 09:00:00',NULL,39,99,13,7,4)
,
  (313,'Historique de recherche','Historique de recherche','TODO','HIGHEST','2026-02-19 09:00:00',NULL,39,99,13,6,4)
,
  (314,'Migration vers ElasticSearch','Migration vers ElasticSearch','NEW','MEDIUM','2026-02-20 09:00:00',NULL,39,99,13,6,1)
,
  (315,'Cache des recherches frequentes','Cache des recherches frequentes','NEW','LOW','2026-02-21 09:00:00',NULL,39,99,13,7,4)
,
  (316,'Analyse lisibilite texte','Analyse lisibilite texte','TODO','MEDIUM','2026-02-22 09:00:00',NULL,39,99,13,6,4)
,
  (317,'Correction orthographe integree','Correction orthographe integree','NEW','HIGH','2026-02-23 09:00:00',NULL,39,99,13,8,1)
,
  (318,'Mode presentation slides','Mode presentation slides','NEW','HIGHEST','2026-02-24 09:00:00',NULL,39,99,13,8,4)
,
  (319,'Commentaires annotes marges','Commentaires annotes marges','NEW','LOW','2026-02-25 09:00:00',NULL,39,99,13,8,4)
,
  (320,'Index des termes du blog','Index des termes du blog','TODO','MEDIUM','2026-02-26 09:00:00',NULL,39,99,13,6,4)
,
  (321,'Statistiques mots cles','Statistiques mots cles','TODO','HIGH','2026-02-27 09:00:00',NULL,39,99,13,6,4)
,
  (322,'Rapport engagement lecteurs','Rapport engagement lecteurs','TODO','MEDIUM','2026-02-28 09:00:00',NULL,39,99,13,6,5)
,
  (323,'Mode newsletter automatique','Mode newsletter automatique','TODO','HIGHEST','2026-02-28 09:00:00',NULL,39,99,13,6,1)
,
  (324,'Gestion des abonnes RSS','Gestion des abonnes RSS','TODO','LOW','2026-02-28 09:00:00',NULL,39,99,13,6,5)
,
  (325,'Rapport SEO articles','Rapport SEO articles','TODO','MEDIUM','2026-02-28 09:00:00',NULL,39,99,13,7,1)
,
  (326,'Mode article sponsorise','Mode article sponsorise','NEW','HIGH','2026-02-28 09:00:00',NULL,39,99,13,6,5)
,
  (327,'Gestion des publicites','Gestion des publicites','TODO','MEDIUM','2026-02-28 09:00:00',NULL,39,99,13,9,5)
,
  (328,'Mode lecture audio TTS','Mode lecture audio TTS','TODO','LOW','2026-02-28 09:00:00',NULL,39,99,13,8,1)
,
  (329,'Statistiques temps de lecture','Statistiques temps de lecture','NEW','HIGHEST','2026-02-28 09:00:00',NULL,39,99,13,6,1)
,
  (330,'Mode articles series','Mode articles series','TODO','MEDIUM','2026-02-28 09:00:00',NULL,39,99,13,6,5)
,
  (331,'CRUD des produits inventaire','CRUD des produits inventaire','DONE','HIGH','2026-01-20 09:00:00','2026-02-28 16:00:00',40,100,14,8,5)
,
  (332,'Categories et sous-categories','Categories et sous-categories','DONE','MEDIUM','2026-01-21 09:00:00','2026-02-25 16:00:00',40,100,14,9,5)
,
  (333,'Gestion des fournisseurs','Gestion des fournisseurs','DONE','HIGHEST','2026-01-22 09:00:00','2026-02-28 16:00:00',40,100,14,7,1)
,
  (334,'Import CSV de produits','Import CSV de produits','DONE','MEDIUM','2026-01-23 09:00:00','2026-02-26 16:00:00',40,100,14,6,5)
,
  (335,'Generation codes-barres','Generation codes-barres','DONE','LOW','2026-01-24 09:00:00','2026-02-28 16:00:00',40,100,14,9,5)
,
  (336,'Enregistrer entree de stock','Enregistrer entree de stock','DONE','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',40,100,14,8,1)
,
  (337,'Enregistrer sortie de stock','Enregistrer sortie de stock','IN_REVIEW','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',40,100,14,6,4)
,
  (338,'Transfert entre depots','Transfert entre depots','IN_REVIEW','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',40,100,14,7,4)
,
  (339,'Inventaire physique ajustement','Inventaire physique ajustement','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',40,100,14,8,5)
,
  (340,'Journal mouvements de stock','Journal mouvements de stock','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',40,100,14,9,4)
,
  (341,'Configurer stock minimum','Configurer stock minimum','IN_REVIEW','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',40,100,14,8,4)
,
  (342,'Notification rupture de stock','Notification rupture de stock','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',40,100,14,8,4)
,
  (343,'Etat stock temps reel','Etat stock temps reel','IN_REVIEW','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',40,100,14,9,4)
,
  (344,'Calcul valeur totale stock','Calcul valeur totale stock','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',40,100,14,8,1)
,
  (345,'Indicateur rotation de stock','Indicateur rotation de stock','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',40,100,14,8,4)
,
  (346,'Dashboard alertes stock','Dashboard alertes stock','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',40,100,14,8,5)
,
  (347,'Email alertes quotidiennes','Email alertes quotidiennes','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',40,100,14,7,1)
,
  (348,'Rapport mensuel PDF','Rapport mensuel PDF','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',40,100,14,9,5)
,
  (349,'Prevision reapprovisionnement','Prevision reapprovisionnement','IN_REVIEW','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',40,100,14,9,4)
,
  (350,'Export rapports Excel PDF','Export rapports Excel PDF','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',40,100,14,8,1)
,
  (351,'Gestion des depots physiques','Gestion des depots physiques','IN_PROGRESS','HIGH','2026-02-03 09:00:00','2026-02-09 14:00:00',41,101,14,8,5)
,
  (352,'Code-barres personnalise','Code-barres personnalise','IN_PROGRESS','MEDIUM','2026-02-04 09:00:00','2026-02-10 14:00:00',41,101,14,9,5)
,
  (353,'Historique prix d achat','Historique prix d achat','IN_PROGRESS','HIGHEST','2026-02-05 09:00:00','2026-02-11 14:00:00',41,101,14,9,5)
,
  (354,'Stock multi-depots','Stock multi-depots','IN_PROGRESS','MEDIUM','2026-02-06 09:00:00','2026-02-11 14:00:00',41,101,14,8,5)
,
  (355,'Alerte peremption produits','Alerte peremption produits','IN_PROGRESS','LOW','2026-02-07 09:00:00','2026-02-10 14:00:00',41,101,14,8,4)
,
  (356,'Gestion des lots','Gestion des lots','IN_PROGRESS','MEDIUM','2026-02-08 09:00:00','2026-02-10 14:00:00',41,101,14,7,4)
,
  (357,'Traceabilite complete','Traceabilite complete','TODO','HIGH','2026-02-09 09:00:00','2026-02-12 14:00:00',41,101,14,7,1)
,
  (358,'Audit de stock','Audit de stock','TODO','HIGHEST','2026-02-10 09:00:00','2026-02-16 14:00:00',41,101,14,8,5)
,
  (359,'Rapport ecarts inventaire','Rapport ecarts inventaire','TODO','LOW','2026-02-11 09:00:00','2026-02-14 14:00:00',41,101,14,8,1)
,
  (360,'Optimisation seuil commandes','Optimisation seuil commandes','IN_PROGRESS','MEDIUM','2026-02-12 09:00:00','2026-02-15 14:00:00',41,101,14,7,4)
,
  (361,'Alerte stock minimum email','Alerte stock minimum email','IN_PROGRESS','HIGH','2026-02-13 09:00:00','2026-02-19 14:00:00',41,101,14,6,5)
,
  (362,'Graphique evolution stock','Graphique evolution stock','IN_PROGRESS','MEDIUM','2026-02-14 09:00:00','2026-02-16 14:00:00',41,101,14,8,1)
,
  (363,'Rapport rotativite stock','Rapport rotativite stock','TODO','HIGHEST','2026-02-15 09:00:00','2026-02-18 14:00:00',41,101,14,8,5)
,
  (364,'Gestion approvisionnement auto','Gestion approvisionnement auto','TODO','LOW','2026-02-16 09:00:00','2026-02-22 14:00:00',41,101,14,6,1)
,
  (365,'Fournisseurs et delais livraison','Fournisseurs et delais livraison','IN_PROGRESS','MEDIUM','2026-02-17 09:00:00','2026-02-22 14:00:00',41,101,14,9,4)
,
  (366,'Suivi commandes fournisseurs','Suivi commandes fournisseurs','IN_PROGRESS','HIGH','2026-02-18 09:00:00','2026-02-22 14:00:00',41,101,14,7,1)
,
  (367,'Facturation fournisseurs','Facturation fournisseurs','TODO','MEDIUM','2026-02-19 09:00:00','2026-02-24 14:00:00',41,101,14,6,1)
,
  (368,'Comparaison prix fournisseurs','Comparaison prix fournisseurs','TODO','LOW','2026-02-20 09:00:00','2026-02-22 14:00:00',41,101,14,6,5)
,
  (369,'Qualite et controles entree','Qualite et controles entree','IN_PROGRESS','HIGHEST','2026-02-21 09:00:00','2026-02-25 14:00:00',41,101,14,7,5)
,
  (370,'Rappel inventaire periodique','Rappel inventaire periodique','IN_PROGRESS','MEDIUM','2026-02-22 09:00:00','2026-02-28 14:00:00',41,101,14,7,1)
,
  (371,'Mode scanning rapide inventaire','Mode scanning rapide inventaire','NEW','HIGH','2026-02-17 09:00:00',NULL,42,102,14,7,5)
,
  (372,'Photos ecrangements stocks','Photos ecrangements stocks','NEW','MEDIUM','2026-02-18 09:00:00',NULL,42,102,14,9,4)
,
  (373,'Stock minimum saisonnier','Stock minimum saisonnier','TODO','HIGHEST','2026-02-19 09:00:00',NULL,42,102,14,9,4)
,
  (374,'Gestion retours fournisseurs','Gestion retours fournisseurs','NEW','MEDIUM','2026-02-20 09:00:00',NULL,42,102,14,6,5)
,
  (375,'Rapport marge beneficiaire','Rapport marge beneficiaire','TODO','LOW','2026-02-21 09:00:00',NULL,42,102,14,7,5)
,
  (376,'Suivi couts de stockage','Suivi couts de stockage','TODO','MEDIUM','2026-02-22 09:00:00',NULL,42,102,14,8,5)
,
  (377,'Prevision demande saisonniere','Prevision demande saisonniere','TODO','HIGH','2026-02-23 09:00:00',NULL,42,102,14,7,1)
,
  (378,'Optimisation espace depot','Optimisation espace depot','TODO','HIGHEST','2026-02-24 09:00:00',NULL,42,102,14,6,1)
,
  (379,'Controle qualite sortant','Controle qualite sortant','TODO','LOW','2026-02-25 09:00:00',NULL,42,102,14,9,4)
,
  (380,'Rapport performance depot','Rapport performance depot','NEW','MEDIUM','2026-02-26 09:00:00',NULL,42,102,14,9,4)
,
  (381,'Gestion des emballages','Gestion des emballages','TODO','HIGH','2026-02-27 09:00:00',NULL,42,102,14,7,4)
,
  (382,'Suivi poids et volume','Suivi poids et volume','TODO','MEDIUM','2026-02-28 09:00:00',NULL,42,102,14,9,1)
,
  (383,'Rapport pertes et avaries','Rapport pertes et avaries','TODO','HIGHEST','2026-02-28 09:00:00',NULL,42,102,14,8,5)
,
  (384,'Mode comptage automatique','Mode comptage automatique','NEW','LOW','2026-02-28 09:00:00',NULL,42,102,14,7,4)
,
  (385,'Gestion des retours clients','Gestion des retours clients','TODO','MEDIUM','2026-02-28 09:00:00',NULL,42,102,14,7,5)
,
  (386,'Statistiques entrees sorties','Statistiques entrees sorties','TODO','HIGH','2026-02-28 09:00:00',NULL,42,102,14,8,1)
,
  (387,'Rapport annuel inventaire','Rapport annuel inventaire','TODO','MEDIUM','2026-02-28 09:00:00',NULL,42,102,14,6,1)
,
  (388,'Mode inventaire tournant','Mode inventaire tournant','TODO','LOW','2026-02-28 09:00:00',NULL,42,102,14,8,4)
,
  (389,'Gestion des promotions stock','Gestion des promotions stock','TODO','HIGHEST','2026-02-28 09:00:00',NULL,42,102,14,9,5)
,
  (390,'Mode surstockage intelligent','Mode surstockage intelligent','TODO','MEDIUM','2026-02-28 09:00:00',NULL,42,102,14,9,4)
,
  (391,'Configuration WebSocket serveur','Configuration WebSocket serveur','IN_REVIEW','HIGH','2026-01-20 09:00:00','2026-02-23 16:00:00',43,103,15,9,4)
,
  (392,'Messages privees 1 a 1','Messages privees 1 a 1','IN_REVIEW','MEDIUM','2026-01-21 09:00:00','2026-02-28 16:00:00',43,103,15,6,4)
,
  (393,'Liste contacts connectes','Liste contacts connectes','DONE','HIGHEST','2026-01-22 09:00:00','2026-02-25 16:00:00',43,103,15,8,1)
,
  (394,'Indicateur en ligne hors ligne','Indicateur en ligne hors ligne','DONE','MEDIUM','2026-01-23 09:00:00','2026-02-28 16:00:00',43,103,15,6,5)
,
  (395,'Persister messages en base','Persister messages en base','IN_REVIEW','LOW','2026-01-24 09:00:00','2026-02-28 16:00:00',43,103,15,6,1)
,
  (396,'Creer groupe de discussion','Creer groupe de discussion','IN_REVIEW','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',43,103,15,9,4)
,
  (397,'Ajouter membres au groupe','Ajouter membres au groupe','IN_REVIEW','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',43,103,15,9,5)
,
  (398,'Envoyer messages de groupe','Envoyer messages de groupe','DONE','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',43,103,15,9,4)
,
  (399,'Notifications sonores','Notifications sonores','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',43,103,15,8,5)
,
  (400,'Notifications push navigateur','Notifications push navigateur','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',43,103,15,8,4)
,
  (401,'Indicateur en train d ecrire','Indicateur en train d ecrire','IN_REVIEW','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',43,103,15,8,5)
,
  (402,'Marquer messages lus non lus','Marquer messages lus non lus','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',43,103,15,6,4)
,
  (403,'Quitter un groupe','Quitter un groupe','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',43,103,15,8,4)
,
  (404,'Gestion admin du groupe','Gestion admin du groupe','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',43,103,15,6,5)
,
  (405,'Archiver un groupe','Archiver un groupe','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',43,103,15,9,1)
,
  (406,'Upload image dans chat','Upload image dans chat','IN_REVIEW','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',43,103,15,8,5)
,
  (407,'Envoyer document PDF Word','Envoyer document PDF Word','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',43,103,15,6,1)
,
  (408,'Miniature images envoyees','Miniature images envoyees','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',43,103,15,7,4)
,
  (409,'Bouton telechargement fichiers','Bouton telechargement fichiers','IN_REVIEW','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',43,103,15,6,1)
,
  (410,'Limitation taille fichiers 10Mo','Limitation taille fichiers 10Mo','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',43,103,15,7,5)
,
  (411,'Modifier un message envoye','Modifier un message envoye','IN_PROGRESS','HIGH','2026-02-03 09:00:00','2026-02-08 14:00:00',44,104,15,6,5)
,
  (412,'Reactions rapides emoji','Reactions rapides emoji','IN_PROGRESS','MEDIUM','2026-02-04 09:00:00','2026-02-09 14:00:00',44,104,15,7,1)
,
  (413,'Statistiques activite chat','Statistiques activite chat','IN_PROGRESS','HIGHEST','2026-02-05 09:00:00','2026-02-09 14:00:00',44,104,15,7,4)
,
  (414,'Mode videoconference integree','Mode videoconference integree','TODO','MEDIUM','2026-02-06 09:00:00','2026-02-11 14:00:00',44,104,15,8,4)
,
  (415,'Partage d ecran en direct','Partage d ecran en direct','TODO','LOW','2026-02-07 09:00:00','2026-02-13 14:00:00',44,104,15,7,4)
,
  (416,'Effacer message pour tous','Effacer message pour tous','IN_PROGRESS','MEDIUM','2026-02-08 09:00:00','2026-02-14 14:00:00',44,104,15,9,1)
,
  (417,'Messages auto-destruction','Messages auto-destruction','TODO','HIGH','2026-02-09 09:00:00','2026-02-13 14:00:00',44,104,15,7,1)
,
  (418,'Traduction temps reel messages','Traduction temps reel messages','TODO','HIGHEST','2026-02-10 09:00:00','2026-02-14 14:00:00',44,104,15,8,5)
,
  (419,'Notifications sons personnalises','Notifications sons personnalises','IN_PROGRESS','LOW','2026-02-11 09:00:00','2026-02-15 14:00:00',44,104,15,8,5)
,
  (420,'Gestion des contacts bloques','Gestion des contacts bloques','TODO','MEDIUM','2026-02-12 09:00:00','2026-02-17 14:00:00',44,104,15,9,1)
,
  (421,'Groupes favoris en haut','Groupes favoris en haut','TODO','HIGH','2026-02-13 09:00:00','2026-02-17 14:00:00',44,104,15,9,4)
,
  (422,'Statut disponibilite personnalise','Statut disponibilite personnalise','TODO','MEDIUM','2026-02-14 09:00:00','2026-02-18 14:00:00',44,104,15,9,4)
,
  (423,'Mode ne pas deranger','Mode ne pas deranger','IN_PROGRESS','HIGHEST','2026-02-15 09:00:00','2026-02-20 14:00:00',44,104,15,7,5)
,
  (424,'Transfereer message contact','Transfereer message contact','IN_PROGRESS','LOW','2026-02-16 09:00:00','2026-02-20 14:00:00',44,104,15,9,1)
,
  (425,'Citer un message precedent','Citer un message precedent','TODO','MEDIUM','2026-02-17 09:00:00','2026-02-20 14:00:00',44,104,15,9,4)
,
  (426,'Reponse rapide par swipe','Reponse rapide par swipe','TODO','HIGH','2026-02-18 09:00:00','2026-02-24 14:00:00',44,104,15,6,1)
,
  (427,'Mode conversation eclatee','Mode conversation eclatee','TODO','MEDIUM','2026-02-19 09:00:00','2026-02-24 14:00:00',44,104,15,8,1)
,
  (428,'Statistiques temps reponse','Statistiques temps reponse','IN_PROGRESS','LOW','2026-02-20 09:00:00','2026-02-23 14:00:00',44,104,15,9,1)
,
  (429,'Export historique conversation','Export historique conversation','TODO','HIGHEST','2026-02-21 09:00:00','2026-02-26 14:00:00',44,104,15,7,1)
,
  (430,'Mode presentation diaporama','Mode presentation diaporama','IN_PROGRESS','MEDIUM','2026-02-22 09:00:00','2026-02-27 14:00:00',44,104,15,8,5)
,
  (431,'Messages audio enregistres','Messages audio enregistres','TODO','HIGH','2026-02-17 09:00:00',NULL,45,105,15,8,5)
,
  (432,'Support emojis et reactions','Support emojis et reactions','NEW','MEDIUM','2026-02-18 09:00:00',NULL,45,105,15,9,4)
,
  (433,'Recherche historique messages','Recherche historique messages','NEW','HIGHEST','2026-02-19 09:00:00',NULL,45,105,15,6,5)
,
  (434,'Messages epingles importants','Messages epingles importants','TODO','MEDIUM','2026-02-20 09:00:00',NULL,45,105,15,7,5)
,
  (435,'Mode hors ligne envoi differe','Mode hors ligne envoi differe','TODO','LOW','2026-02-21 09:00:00',NULL,45,105,15,7,5)
,
  (436,'Notifications vocales','Notifications vocales','TODO','MEDIUM','2026-02-22 09:00:00',NULL,45,105,15,9,1)
,
  (437,'Salons tematiques publics','Salons tematiques publics','TODO','HIGH','2026-02-23 09:00:00',NULL,45,105,15,9,1)
,
  (438,'Mode silencieux conversations','Mode silencieux conversations','TODO','HIGHEST','2026-02-24 09:00:00',NULL,45,105,15,6,1)
,
  (439,'Messages programmes envoyer','Messages programmes envoyer','NEW','LOW','2026-02-25 09:00:00',NULL,45,105,15,6,4)
,
  (440,'Statistiques engagement groupe','Statistiques engagement groupe','NEW','MEDIUM','2026-02-26 09:00:00',NULL,45,105,15,8,4)
,
  (441,'Mode salon prive temporary','Mode salon prive temporary','TODO','HIGH','2026-02-27 09:00:00',NULL,45,105,15,7,5)
,
  (442,'Gestion des permissions salon','Gestion des permissions salon','NEW','MEDIUM','2026-02-28 09:00:00',NULL,45,105,15,7,1)
,
  (443,'Mode enregistrement vocal','Mode enregistrement vocal','TODO','HIGHEST','2026-02-28 09:00:00',NULL,45,105,15,6,5)
,
  (444,'Chatbot reponses automatiques','Chatbot reponses automatiques','NEW','LOW','2026-02-28 09:00:00',NULL,45,105,15,7,4)
,
  (445,'Mode modelexplication visuel','Mode modelexplication visuel','NEW','MEDIUM','2026-02-28 09:00:00',NULL,45,105,15,7,1)
,
  (446,'Statistiques pics d activite','Statistiques pics d activite','NEW','HIGH','2026-02-28 09:00:00',NULL,45,105,15,8,4)
,
  (447,'Gestion mode absent','Gestion mode absent','TODO','MEDIUM','2026-02-28 09:00:00',NULL,45,105,15,6,4)
,
  (448,'Mode modelexplication audio','Mode modelexplication audio','TODO','LOW','2026-02-28 09:00:00',NULL,45,105,15,7,1)
,
  (449,'Rapport activite mensuelle','Rapport activite mensuelle','NEW','HIGHEST','2026-02-28 09:00:00',NULL,45,105,15,8,5)
,
  (450,'Mode salon tematique permanent','Mode salon tematique permanent','TODO','MEDIUM','2026-02-28 09:00:00',NULL,45,105,15,9,5)
,
  (451,'Formulaire ajout employe','Formulaire ajout employe','DONE','HIGH','2026-01-20 09:00:00','2026-02-24 16:00:00',46,106,16,9,4)
,
  (452,'Vue fiche employe detaillee','Vue fiche employe detaillee','DONE','MEDIUM','2026-01-21 09:00:00','2026-02-25 16:00:00',46,106,16,9,1)
,
  (453,'Gestion contrats CDI CDD stage','Gestion contrats CDI CDD stage','DONE','HIGHEST','2026-01-22 09:00:00','2026-02-27 16:00:00',46,106,16,8,5)
,
  (454,'Upload documents CV diplomes','Upload documents CV diplomes','DONE','MEDIUM','2026-01-23 09:00:00','2026-02-28 16:00:00',46,106,16,6,5)
,
  (455,'Vue arborescente organigramme','Vue arborescente organigramme','DONE','LOW','2026-01-24 09:00:00','2026-02-28 16:00:00',46,106,16,6,5)
,
  (456,'Formulaire demande de conge','Formulaire demande de conge','DONE','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',46,106,16,9,4)
,
  (457,'Interface validation manager','Interface validation manager','IN_REVIEW','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',46,106,16,8,1)
,
  (458,'Calcul solde de conges restant','Calcul solde de conges restant','DONE','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',46,106,16,8,5)
,
  (459,'Calendrier absences equipe','Calendrier absences equipe','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',46,106,16,6,5)
,
  (460,'Historique des conges','Historique des conges','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',46,106,16,8,4)
,
  (461,'Gestion conges maladie','Gestion conges maladie','IN_REVIEW','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',46,106,16,8,5)
,
  (462,'Demande conge sans solde','Demande conge sans solde','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',46,106,16,8,4)
,
  (463,'Email notification validation','Email notification validation','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',46,106,16,7,1)
,
  (464,'Export planning equipes PDF','Export planning equipes PDF','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',46,106,16,8,5)
,
  (465,'Detection jours feries auto','Detection jours feries auto','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',46,106,16,7,4)
,
  (466,'Calcul salaire brut et net','Calcul salaire brut et net','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',46,106,16,8,1)
,
  (467,'Generation bulletin paie PDF','Generation bulletin paie PDF','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',46,106,16,7,1)
,
  (468,'Calcul retenues sociales','Calcul retenues sociales','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',46,106,16,8,1)
,
  (469,'Archives bulletins passes','Archives bulletins passes','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',46,106,16,8,5)
,
  (470,'Preparation declaration fiscale','Preparation declaration fiscale','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',46,106,16,8,5)
,
  (471,'Gestion avantages mutuelle','Gestion avantages mutuelle','IN_PROGRESS','HIGH','2026-02-03 09:00:00','2026-02-07 14:00:00',47,107,16,9,5)
,
  (472,'Calcul et integration primes','Calcul et integration primes','IN_PROGRESS','MEDIUM','2026-02-04 09:00:00','2026-02-06 14:00:00',47,107,16,7,4)
,
  (473,'Calcul heures supplementaires','Calcul heures supplementaires','TODO','HIGHEST','2026-02-05 09:00:00','2026-02-08 14:00:00',47,107,16,8,5)
,
  (474,'Envoi bulletin par email','Envoi bulletin par email','IN_PROGRESS','MEDIUM','2026-02-06 09:00:00','2026-02-10 14:00:00',47,107,16,9,5)
,
  (475,'Simulateur salaire embauche','Simulateur salaire embauche','TODO','LOW','2026-02-07 09:00:00','2026-02-09 14:00:00',47,107,16,6,1)
,
  (476,'Gestion formation employes','Gestion formation employes','IN_PROGRESS','MEDIUM','2026-02-08 09:00:00','2026-02-10 14:00:00',47,107,16,8,4)
,
  (477,'Evaluation annuelle performance','Evaluation annuelle performance','IN_PROGRESS','HIGH','2026-02-09 09:00:00','2026-02-12 14:00:00',47,107,16,7,5)
,
  (478,'Plan de carriere','Plan de carriere','TODO','HIGHEST','2026-02-10 09:00:00','2026-02-16 14:00:00',47,107,16,9,4)
,
  (479,'Recrutement et offres emploi','Recrutement et offres emploi','TODO','LOW','2026-02-11 09:00:00','2026-02-13 14:00:00',47,107,16,7,4)
,
  (480,'Onboarding nouveau employe','Onboarding nouveau employe','TODO','MEDIUM','2026-02-12 09:00:00','2026-02-14 14:00:00',47,107,16,9,4)
,
  (481,'Gestion des entretiens','Gestion des entretiens','IN_PROGRESS','HIGH','2026-02-13 09:00:00','2026-02-18 14:00:00',47,107,16,7,4)
,
  (482,'Suivi periode essai','Suivi periode essai','IN_PROGRESS','MEDIUM','2026-02-14 09:00:00','2026-02-18 14:00:00',47,107,16,8,1)
,
  (483,'Gestion notes de frais','Gestion notes de frais','TODO','HIGHEST','2026-02-15 09:00:00','2026-02-20 14:00:00',47,107,16,8,1)
,
  (484,'Avance sur salaire','Avance sur salaire','IN_PROGRESS','LOW','2026-02-16 09:00:00','2026-02-21 14:00:00',47,107,16,7,1)
,
  (485,'Gestion CSE et comites','Gestion CSE et comites','IN_PROGRESS','MEDIUM','2026-02-17 09:00:00','2026-02-23 14:00:00',47,107,16,7,5)
,
  (486,'Suivi etat psychosocial','Suivi etat psychosocial','IN_PROGRESS','HIGH','2026-02-18 09:00:00','2026-02-21 14:00:00',47,107,16,6,4)
,
  (487,'Gestion teletravail','Gestion teletravail','IN_PROGRESS','MEDIUM','2026-02-19 09:00:00','2026-02-25 14:00:00',47,107,16,7,1)
,
  (488,'Badge et pointage electronique','Badge et pointage electronique','IN_PROGRESS','LOW','2026-02-20 09:00:00','2026-02-24 14:00:00',47,107,16,7,1)
,
  (489,'Reglement interieur numerique','Reglement interieur numerique','IN_PROGRESS','HIGHEST','2026-02-21 09:00:00','2026-02-24 14:00:00',47,107,16,6,4)
,
  (490,'Enquete satisfaction employes','Enquete satisfaction employes','IN_PROGRESS','MEDIUM','2026-02-22 09:00:00','2026-02-24 14:00:00',47,107,16,8,1)
,
  (491,'Gestion succession et remplacement','Gestion succession et remplacement','TODO','HIGH','2026-02-17 09:00:00',NULL,48,108,16,7,1)
,
  (492,'Plan de formation annuel','Plan de formation annuel','NEW','MEDIUM','2026-02-18 09:00:00',NULL,48,108,16,7,5)
,
  (493,'Suivi certifications employes','Suivi certifications employes','NEW','HIGHEST','2026-02-19 09:00:00',NULL,48,108,16,6,1)
,
  (494,'Gestion interims','Gestion interims','TODO','MEDIUM','2026-02-20 09:00:00',NULL,48,108,16,6,1)
,
  (495,'Rapport effectifs et turnover','Rapport effectifs et turnover','NEW','LOW','2026-02-21 09:00:00',NULL,48,108,16,6,5)
,
  (496,'Tableau bord RH dirigeant','Tableau bord RH dirigeant','TODO','MEDIUM','2026-02-22 09:00:00',NULL,48,108,16,9,4)
,
  (497,'Gestion des missions interne','Gestion des missions interne','NEW','HIGH','2026-02-23 09:00:00',NULL,48,108,16,6,4)
,
  (498,'Suivi accidents de travail','Suivi accidents de travail','NEW','HIGHEST','2026-02-24 09:00:00',NULL,48,108,16,7,5)
,
  (499,'Declaration DSN mensuelle','Declaration DSN mensuelle','TODO','LOW','2026-02-25 09:00:00',NULL,48,108,16,8,4)
,
  (500,'Archives documentation RH','Archives documentation RH','TODO','MEDIUM','2026-02-26 09:00:00',NULL,48,108,16,6,4)
,
  (501,'Gestion temps partiel','Gestion temps partiel','NEW','HIGH','2026-02-27 09:00:00',NULL,48,108,16,9,5)
,
  (502,'Suivi convention collective','Suivi convention collective','TODO','MEDIUM','2026-02-28 09:00:00',NULL,48,108,16,9,5)
,
  (503,'Rapport climat social','Rapport climat social','NEW','HIGHEST','2026-02-28 09:00:00',NULL,48,108,16,6,1)
,
  (504,'Gestion des recrutements alumni','Gestion des recrutements alumni','NEW','LOW','2026-02-28 09:00:00',NULL,48,108,16,7,1)
,
  (505,'Mode e-learning integre','Mode e-learning integre','TODO','MEDIUM','2026-02-28 09:00:00',NULL,48,108,16,8,5)
,
  (506,'Gestion des astreintes','Gestion des astreintes','NEW','HIGH','2026-02-28 09:00:00',NULL,48,108,16,8,4)
,
  (507,'Suivi des primes de rendement','Suivi des primes de rendement','NEW','MEDIUM','2026-02-28 09:00:00',NULL,48,108,16,8,4)
,
  (508,'Rapport parite hommes femmes','Rapport parite hommes femmes','NEW','LOW','2026-02-28 09:00:00',NULL,48,108,16,9,1)
,
  (509,'Mode evaluation 360 degres','Mode evaluation 360 degres','TODO','HIGHEST','2026-02-28 09:00:00',NULL,48,108,16,7,4)
,
  (510,'Gestion des departs volontaires','Gestion des departs volontaires','NEW','MEDIUM','2026-02-28 09:00:00',NULL,48,108,16,7,4)
,
  (511,'Formulaire inscription etudiant','Formulaire inscription etudiant','DONE','HIGH','2026-01-20 09:00:00','2026-02-26 16:00:00',49,109,17,9,4)
,
  (512,'Connexion SSO universitaire','Connexion SSO universitaire','DONE','MEDIUM','2026-01-21 09:00:00','2026-02-26 16:00:00',49,109,17,8,4)
,
  (513,'Affichage profil etudiant','Affichage profil etudiant','DONE','HIGHEST','2026-01-22 09:00:00','2026-02-28 16:00:00',49,109,17,8,1)
,
  (514,'Reinitialisation mot de passe','Reinitialisation mot de passe','IN_REVIEW','MEDIUM','2026-01-23 09:00:00','2026-02-26 16:00:00',49,109,17,6,1)
,
  (515,'Tableau de bord etudiant','Tableau de bord etudiant','DONE','LOW','2026-01-24 09:00:00','2026-02-28 16:00:00',49,109,17,9,1)
,
  (516,'Affichage notes par matiere','Affichage notes par matiere','DONE','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',49,109,17,7,5)
,
  (517,'Calcul moyenne generale ponderee','Calcul moyenne generale ponderee','IN_REVIEW','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',49,109,17,8,5)
,
  (518,'Telechargement releve notes PDF','Telechargement releve notes PDF','DONE','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',49,109,17,8,5)
,
  (519,'Vue detaillee par module','Vue detaillee par module','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',49,109,17,8,5)
,
  (520,'Classement promotion par moyenne','Classement promotion par moyenne','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',49,109,17,6,5)
,
  (521,'Statistiques distribution notes','Statistiques distribution notes','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',49,109,17,7,5)
,
  (522,'Alerte notes rattrapage','Alerte notes rattrapage','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',49,109,17,6,4)
,
  (523,'Emploi du temps hebdomadaire','Emploi du temps hebdomadaire','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',49,109,17,6,4)
,
  (524,'Calendrier des examens','Calendrier des examens','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',49,109,17,9,5)
,
  (525,'Planning des cours','Planning des cours','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',49,109,17,6,5)
,
  (526,'Export emplois du temps ICS','Export emplois du temps ICS','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',49,109,17,6,1)
,
  (527,'Notifs changement de salle','Notifs changement de salle','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',49,109,17,8,4)
,
  (528,'Emplois du temps par filiere','Emplois du temps par filiere','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',49,109,17,7,5)
,
  (529,'Filtre par semestre et annee','Filtre par semestre et annee','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',49,109,17,6,1)
,
  (530,'Emploi du temps personnalise','Emploi du temps personnalise','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',49,109,17,6,5)
,
  (531,'Rappel examens automatiske','Rappel examens automatiske','IN_PROGRESS','HIGH','2026-02-03 09:00:00','2026-02-08 14:00:00',50,110,17,9,4)
,
  (532,'Absences et justificatifs en ligne','Absences et justificatifs en ligne','IN_PROGRESS','MEDIUM','2026-02-04 09:00:00','2026-02-08 14:00:00',50,110,17,7,4)
,
  (533,'Cours en ligne et documents','Cours en ligne et documents','IN_PROGRESS','HIGHEST','2026-02-05 09:00:00','2026-02-08 14:00:00',50,110,17,6,1)
,
  (534,'Suivi assiduite et presences','Suivi assiduite et presences','IN_PROGRESS','MEDIUM','2026-02-06 09:00:00','2026-02-08 14:00:00',50,110,17,6,5)
,
  (535,'Resultats partiels et mini-examens','Resultats partiels et mini-examens','IN_PROGRESS','LOW','2026-02-07 09:00:00','2026-02-12 14:00:00',50,110,17,9,5)
,
  (536,'Gestion notes blancs','Gestion notes blancs','TODO','MEDIUM','2026-02-08 09:00:00','2026-02-12 14:00:00',50,110,17,9,4)
,
  (537,'Forum d entraide etudiants','Forum d entraide etudiants','IN_PROGRESS','HIGH','2026-02-09 09:00:00','2026-02-14 14:00:00',50,110,17,6,4)
,
  (538,'Bibliotheque ressources cours','Bibliotheque ressources cours','IN_PROGRESS','HIGHEST','2026-02-10 09:00:00','2026-02-16 14:00:00',50,110,17,8,5)
,
  (539,'Calendrier projets et stages','Calendrier projets et stages','TODO','LOW','2026-02-11 09:00:00','2026-02-14 14:00:00',50,110,17,8,1)
,
  (540,'Bulletin semestriel telechargeable','Bulletin semestriel telechargeable','TODO','MEDIUM','2026-02-12 09:00:00','2026-02-15 14:00:00',50,110,17,6,1)
,
  (541,'Messagerie etudiant-professeur','Messagerie etudiant-professeur','IN_PROGRESS','HIGH','2026-02-13 09:00:00','2026-02-17 14:00:00',50,110,17,7,1)
,
  (542,'Notifications notes publiees','Notifications notes publiees','IN_PROGRESS','MEDIUM','2026-02-14 09:00:00','2026-02-16 14:00:00',50,110,17,8,1)
,
  (543,'Emploi du temps prof disponible','Emploi du temps prof disponible','TODO','HIGHEST','2026-02-15 09:00:00','2026-02-18 14:00:00',50,110,17,9,1)
,
  (544,'Inscription exams en ligne','Inscription exams en ligne','TODO','LOW','2026-02-16 09:00:00','2026-02-22 14:00:00',50,110,17,7,5)
,
  (545,'Gestion des delais de rendus','Gestion des delais de rendus','IN_PROGRESS','MEDIUM','2026-02-17 09:00:00','2026-02-19 14:00:00',50,110,17,6,4)
,
  (546,'Suivi projets tuteur','Suivi projets tuteur','TODO','HIGH','2026-02-18 09:00:00','2026-02-20 14:00:00',50,110,17,6,4)
,
  (547,'Demande de delai supplementaire','Demande de delai supplementaire','IN_PROGRESS','MEDIUM','2026-02-19 09:00:00','2026-02-24 14:00:00',50,110,17,9,5)
,
  (548,'Calendrier soutenances','Calendrier soutenances','TODO','LOW','2026-02-20 09:00:00','2026-02-25 14:00:00',50,110,17,8,1)
,
  (549,'Statistiques reussite par matiere','Statistiques reussite par matiere','IN_PROGRESS','HIGHEST','2026-02-21 09:00:00','2026-02-27 14:00:00',50,110,17,8,1)
,
  (550,'Mode etudes de groupe','Mode etudes de groupe','TODO','MEDIUM','2026-02-22 09:00:00','2026-02-27 14:00:00',50,110,17,8,1)
,
  (551,'Planning revisions fin semestre','Planning revisions fin semestre','TODO','HIGH','2026-02-17 09:00:00',NULL,51,111,17,7,4)
,
  (552,'Flashcards et fiches revision','Flashcards et fiches revision','NEW','MEDIUM','2026-02-18 09:00:00',NULL,51,111,17,9,5)
,
  (553,'Quiz d auto-evaluation','Quiz d auto-evaluation','TODO','HIGHEST','2026-02-19 09:00:00',NULL,51,111,17,9,4)
,
  (554,'Suivi objectifs academiques','Suivi objectifs academiques','NEW','MEDIUM','2026-02-20 09:00:00',NULL,51,111,17,7,4)
,
  (555,'Rapport semestriel parents','Rapport semestriel parents','TODO','LOW','2026-02-21 09:00:00',NULL,51,111,17,6,5)
,
  (556,'Emploi du temps vacances','Emploi du temps vacances','TODO','MEDIUM','2026-02-22 09:00:00',NULL,51,111,17,7,4)
,
  (557,'Gestion bourses et aides','Gestion bourses et aides','NEW','HIGH','2026-02-23 09:00:00',NULL,51,111,17,7,1)
,
  (558,'Recherche stage et alternance','Recherche stage et alternance','TODO','HIGHEST','2026-02-24 09:00:00',NULL,51,111,17,7,4)
,
  (559,'CV et profil professionnel','CV et profil professionnel','TODO','LOW','2026-02-25 09:00:00',NULL,51,111,17,7,5)
,
  (560,'Alumni et reseau anciens','Alumni et reseau anciens','TODO','MEDIUM','2026-02-26 09:00:00',NULL,51,111,17,6,1)
,
  (561,'Mode mentorat par anciens','Mode mentorat par anciens','NEW','HIGH','2026-02-27 09:00:00',NULL,51,111,17,9,5)
,
  (562,'Gestion des competitions academiques','Gestion des competitions academiques','NEW','MEDIUM','2026-02-28 09:00:00',NULL,51,111,17,9,5)
,
  (563,'Rapport orientation post-diplome','Rapport orientation post-diplome','NEW','HIGHEST','2026-02-28 09:00:00',NULL,51,111,17,8,5)
,
  (564,'Mode echanges inter-universites','Mode echanges inter-universites','NEW','LOW','2026-02-28 09:00:00',NULL,51,111,17,7,4)
,
  (565,'Gestion Erasmus','Gestion Erasmus','TODO','MEDIUM','2026-02-28 09:00:00',NULL,51,111,17,9,4)
,
  (566,'Statistiques insertion professionnelle','Statistiques insertion professionnelle','TODO','HIGH','2026-02-28 09:00:00',NULL,51,111,17,8,5)
,
  (567,'Mode projets collaboratifs','Mode projets collaboratifs','TODO','MEDIUM','2026-02-28 09:00:00',NULL,51,111,17,8,5)
,
  (568,'Gestion des publications academiques','Gestion des publications academiques','TODO','LOW','2026-02-28 09:00:00',NULL,51,111,17,8,4)
,
  (569,'Rapport activite associative','Rapport activite associative','NEW','HIGHEST','2026-02-28 09:00:00',NULL,51,111,17,6,1)
,
  (570,'Mode vie etudiante','Mode vie etudiante','NEW','MEDIUM','2026-02-28 09:00:00',NULL,51,111,17,8,1)
,
  (571,'Creer dossier patient','Creer dossier patient','DONE','HIGH','2026-01-20 09:00:00','2026-02-27 16:00:00',52,112,18,9,4)
,
  (572,'Consulter dossier medical','Consulter dossier medical','IN_REVIEW','MEDIUM','2026-01-21 09:00:00','2026-02-28 16:00:00',52,112,18,6,4)
,
  (573,'Historique medical complet','Historique medical complet','DONE','HIGHEST','2026-01-22 09:00:00','2026-02-28 16:00:00',52,112,18,8,5)
,
  (574,'Gestion allergies medicamenteuses','Gestion allergies medicamenteuses','IN_REVIEW','MEDIUM','2026-01-23 09:00:00','2026-02-28 16:00:00',52,112,18,7,1)
,
  (575,'Prise de rendez-vous en ligne','Prise de rendez-vous en ligne','DONE','LOW','2026-01-24 09:00:00','2026-02-27 16:00:00',52,112,18,8,5)
,
  (576,'Planning medecin semaine','Planning medecin semaine','IN_REVIEW','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',52,112,18,7,1)
,
  (577,'Confirmation RDV par email','Confirmation RDV par email','IN_REVIEW','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',52,112,18,9,5)
,
  (578,'Annulation et report RDV','Annulation et report RDV','IN_REVIEW','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',52,112,18,7,4)
,
  (579,'File d attente virtuelle','File d attente virtuelle','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',52,112,18,9,4)
,
  (580,'Rappel RDV automatique','Rappel RDV automatique','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',52,112,18,6,5)
,
  (581,'Generation ordonnance medicale','Generation ordonnance medicale','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',52,112,18,6,4)
,
  (582,'Historique ordonnances patient','Historique ordonnances patient','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',52,112,18,9,4)
,
  (583,'Gestion posologie medicaments','Gestion posologie medicaments','IN_REVIEW','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',52,112,18,7,4)
,
  (584,'Suivi traitements en cours','Suivi traitements en cours','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',52,112,18,7,4)
,
  (585,'Rapport medical imprimable','Rapport medical imprimable','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',52,112,18,7,5)
,
  (586,'Envoyer ordonnance par email','Envoyer ordonnance par email','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',52,112,18,8,5)
,
  (587,'Gestion des mutuelles patient','Gestion des mutuelles patient','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',52,112,18,7,5)
,
  (588,'Statistiques consultations','Statistiques consultations','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',52,112,18,6,5)
,
  (589,'Mot de passe pour proche','Mot de passe pour proche','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',52,112,18,8,1)
,
  (590,'Teleconsultation video','Teleconsultation video','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',52,112,18,7,1)
,
  (591,'Dossier partage entre medecins','Dossier partage entre medecins','TODO','HIGH','2026-02-03 09:00:00','2026-02-06 14:00:00',53,113,18,7,1)
,
  (592,'Alerte interactions medicamenteuses','Alerte interactions medicamenteuses','IN_PROGRESS','MEDIUM','2026-02-04 09:00:00','2026-02-06 14:00:00',53,113,18,6,4)
,
  (593,'Suivi tension et glycemie','Suivi tension et glycemie','TODO','HIGHEST','2026-02-05 09:00:00','2026-02-09 14:00:00',53,113,18,6,5)
,
  (594,'Rapport annuel patient','Rapport annuel patient','TODO','MEDIUM','2026-02-06 09:00:00','2026-02-11 14:00:00',53,113,18,7,5)
,
  (595,'Export dossier medical numerique','Export dossier medical numerique','IN_PROGRESS','LOW','2026-02-07 09:00:00','2026-02-13 14:00:00',53,113,18,7,1)
,
  (596,'Gestion examens complementaires','Gestion examens complementaires','IN_PROGRESS','MEDIUM','2026-02-08 09:00:00','2026-02-13 14:00:00',53,113,18,9,1)
,
  (597,'Rendez-vous suivi chronique','Rendez-vous suivi chronique','IN_PROGRESS','HIGH','2026-02-09 09:00:00','2026-02-15 14:00:00',53,113,18,7,4)
,
  (598,'Gestion certificats medicaux','Gestion certificats medicaux','IN_PROGRESS','HIGHEST','2026-02-10 09:00:00','2026-02-16 14:00:00',53,113,18,9,1)
,
  (599,'Protocoles de soins standardises','Protocoles de soins standardises','IN_PROGRESS','LOW','2026-02-11 09:00:00','2026-02-14 14:00:00',53,113,18,8,4)
,
  (600,'Declarations obligatoires','Declarations obligatoires','IN_PROGRESS','MEDIUM','2026-02-12 09:00:00','2026-02-16 14:00:00',53,113,18,9,4)
,
  (601,'Gestion prescriptions electriques','Gestion prescriptions electriques','TODO','HIGH','2026-02-13 09:00:00','2026-02-16 14:00:00',53,113,18,9,1)
,
  (602,'Suivi patients diabetiques','Suivi patients diabetiques','TODO','MEDIUM','2026-02-14 09:00:00','2026-02-16 14:00:00',53,113,18,7,4)
,
  (603,'Tableau bord medecin generaliste','Tableau bord medecin generaliste','TODO','HIGHEST','2026-02-15 09:00:00','2026-02-17 14:00:00',53,113,18,8,5)
,
  (604,'Alerte rappel vaccins','Alerte rappel vaccins','TODO','LOW','2026-02-16 09:00:00','2026-02-18 14:00:00',53,113,18,6,4)
,
  (605,'Gestion des lits hospitalisations','Gestion des lits hospitalisations','IN_PROGRESS','MEDIUM','2026-02-17 09:00:00','2026-02-23 14:00:00',53,113,18,8,4)
,
  (606,'Suivi prelevements labo','Suivi prelevements labo','IN_PROGRESS','HIGH','2026-02-18 09:00:00','2026-02-21 14:00:00',53,113,18,9,1)
,
  (607,'Resultats examens en ligne','Resultats examens en ligne','TODO','MEDIUM','2026-02-19 09:00:00','2026-02-24 14:00:00',53,113,18,8,1)
,
  (608,'Consentement eclaire numerique','Consentement eclaire numerique','IN_PROGRESS','LOW','2026-02-20 09:00:00','2026-02-24 14:00:00',53,113,18,9,5)
,
  (609,'Gestion des urgences','Gestion des urgences','IN_PROGRESS','HIGHEST','2026-02-21 09:00:00','2026-02-25 14:00:00',53,113,18,9,1)
,
  (610,'Protocole COVID-19','Protocole COVID-19','IN_PROGRESS','MEDIUM','2026-02-22 09:00:00','2026-02-24 14:00:00',53,113,18,6,4)
,
  (611,'Suivi mortalite et morbidite','Suivi mortalite et morbidite','NEW','HIGH','2026-02-17 09:00:00',NULL,54,114,18,7,1)
,
  (612,'Rapport epidemiologique','Rapport epidemiologique','TODO','MEDIUM','2026-02-18 09:00:00',NULL,54,114,18,8,4)
,
  (613,'Gestion des epidemies','Gestion des epidemies','TODO','HIGHEST','2026-02-19 09:00:00',NULL,54,114,18,9,4)
,
  (614,'Suivi qualite de vie patient','Suivi qualite de vie patient','NEW','MEDIUM','2026-02-20 09:00:00',NULL,54,114,18,6,1)
,
  (615,'Enquete satisfaction patient','Enquete satisfaction patient','TODO','LOW','2026-02-21 09:00:00',NULL,54,114,18,8,1)
,
  (616,'Gestion des plaintes','Gestion des plaintes','TODO','MEDIUM','2026-02-22 09:00:00',NULL,54,114,18,9,1)
,
  (617,'Rapport activite mensuelle medecin','Rapport activite mensuelle medecin','TODO','HIGH','2026-02-23 09:00:00',NULL,54,114,18,7,4)
,
  (618,'Gestion des gardes','Gestion des gardes','TODO','HIGHEST','2026-02-24 09:00:00',NULL,54,114,18,6,1)
,
  (619,'Suivi des operations planifiees','Suivi des operations planifiees','NEW','LOW','2026-02-25 09:00:00',NULL,54,114,18,9,1)
,
  (620,'Coordination soins equipe','Coordination soins equipe','TODO','MEDIUM','2026-02-26 09:00:00',NULL,54,114,18,9,5)
,
  (621,'Gestion des transferts patients','Gestion des transferts patients','NEW','HIGH','2026-02-27 09:00:00',NULL,54,114,18,6,5)
,
  (622,'Suivi antecedents familiaux','Suivi antecedents familiaux','NEW','MEDIUM','2026-02-28 09:00:00',NULL,54,114,18,7,5)
,
  (623,'Mode prescription numerique','Mode prescription numerique','TODO','HIGHEST','2026-02-28 09:00:00',NULL,54,114,18,8,4)
,
  (624,'Gestion des cabinets medicalisees','Gestion des cabinets medicalisees','TODO','LOW','2026-02-28 09:00:00',NULL,54,114,18,8,1)
,
  (625,'Rapport decharges medicales','Rapport decharges medicales','NEW','MEDIUM','2026-02-28 09:00:00',NULL,54,114,18,9,1)
,
  (626,'Suivi patients ages dependants','Suivi patients ages dependants','TODO','HIGH','2026-02-28 09:00:00',NULL,54,114,18,8,1)
,
  (627,'Mode telemonitoring chronique','Mode telemonitoring chronique','NEW','MEDIUM','2026-02-28 09:00:00',NULL,54,114,18,6,4)
,
  (628,'Gestion des dispositifs medicaux','Gestion des dispositifs medicaux','NEW','LOW','2026-02-28 09:00:00',NULL,54,114,18,6,5)
,
  (629,'Rapport activite urgences','Rapport activite urgences','NEW','HIGHEST','2026-02-28 09:00:00',NULL,54,114,18,8,4)
,
  (630,'Mode orientation specialists','Mode orientation specialists','TODO','MEDIUM','2026-02-28 09:00:00',NULL,54,114,18,8,1)
,
  (631,'Publier annonce immobiliere','Publier annonce immobiliere','DONE','HIGH','2026-01-20 09:00:00','2026-02-28 16:00:00',55,115,19,7,5)
,
  (632,'Recherche avancee de biens','Recherche avancee de biens','DONE','MEDIUM','2026-01-21 09:00:00','2026-02-24 16:00:00',55,115,19,8,1)
,
  (633,'Filtres type surface prix','Filtres type surface prix','DONE','HIGHEST','2026-01-22 09:00:00','2026-02-26 16:00:00',55,115,19,8,5)
,
  (634,'Galerie photos multi-vues','Galerie photos multi-vues','IN_REVIEW','MEDIUM','2026-01-23 09:00:00','2026-02-27 16:00:00',55,115,19,9,5)
,
  (635,'Planification visite bien','Planification visite bien','DONE','LOW','2026-01-24 09:00:00','2026-02-28 16:00:00',55,115,19,6,5)
,
  (636,'Calendrier disponibilites visites','Calendrier disponibilites visites','DONE','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',55,115,19,8,1)
,
  (637,'Confirmation visite par email','Confirmation visite par email','DONE','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',55,115,19,6,4)
,
  (638,'Annulation de visite','Annulation de visite','IN_REVIEW','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',55,115,19,9,5)
,
  (639,'Evaluation post-visite','Evaluation post-visite','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',55,115,19,6,5)
,
  (640,'Visite virtuelle 360 degres','Visite virtuelle 360 degres','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',55,115,19,8,1)
,
  (641,'Creation contrat de location','Creation contrat de location','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',55,115,19,8,1)
,
  (642,'Signature numerique contrat','Signature numerique contrat','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',55,115,19,9,5)
,
  (643,'Etat des lieux entree sortie','Etat des lieux entree sortie','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',55,115,19,8,1)
,
  (644,'Gestion des quittances','Gestion des quittances','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',55,115,19,8,5)
,
  (645,'Relance loyer impaye','Relance loyer impaye','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',55,115,19,9,1)
,
  (646,'Calcul charges copropriete','Calcul charges copropriete','IN_REVIEW','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',55,115,19,7,5)
,
  (647,'Notifications nouveaux biens','Notifications nouveaux biens','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',55,115,19,6,1)
,
  (648,'Biens sauvegardes favoris','Biens sauvegardes favoris','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',55,115,19,8,4)
,
  (649,'Comparateur de biens','Comparateur de biens','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',55,115,19,7,5)
,
  (650,'Estimation prix immobilier','Estimation prix immobilier','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',55,115,19,9,1)
,
  (651,'Suivi dossiers locataires','Suivi dossiers locataires','IN_PROGRESS','HIGH','2026-02-03 09:00:00','2026-02-05 14:00:00',56,116,19,9,4)
,
  (652,'Gestion des garants','Gestion des garants','IN_PROGRESS','MEDIUM','2026-02-04 09:00:00','2026-02-10 14:00:00',56,116,19,9,4)
,
  (653,'Documents administratifs','Documents administratifs','IN_PROGRESS','HIGHEST','2026-02-05 09:00:00','2026-02-11 14:00:00',56,116,19,6,1)
,
  (654,'Alerte baisse de prix','Alerte baisse de prix','TODO','MEDIUM','2026-02-06 09:00:00','2026-02-08 14:00:00',56,116,19,6,1)
,
  (655,'Statistiques marche immobilier','Statistiques marche immobilier','IN_PROGRESS','LOW','2026-02-07 09:00:00','2026-02-12 14:00:00',56,116,19,8,4)
,
  (656,'Visite video proprietaire','Visite video proprietaire','TODO','MEDIUM','2026-02-08 09:00:00','2026-02-11 14:00:00',56,116,19,6,5)
,
  (657,'Plan interactif quartier','Plan interactif quartier','TODO','HIGH','2026-02-09 09:00:00','2026-02-14 14:00:00',56,116,19,8,4)
,
  (658,'Calcul pret immobilier','Calcul pret immobilier','IN_PROGRESS','HIGHEST','2026-02-10 09:00:00','2026-02-14 14:00:00',56,116,19,7,5)
,
  (659,'Gestion des compromis','Gestion des compromis','TODO','LOW','2026-02-11 09:00:00','2026-02-15 14:00:00',56,116,19,8,5)
,
  (660,'Suivi notaire et acte','Suivi notaire et acte','IN_PROGRESS','MEDIUM','2026-02-12 09:00:00','2026-02-15 14:00:00',56,116,19,6,1)
,
  (661,'Verification identite locataire','Verification identite locataire','TODO','HIGH','2026-02-13 09:00:00','2026-02-15 14:00:00',56,116,19,9,5)
,
  (662,'Mode estimation en ligne','Mode estimation en ligne','IN_PROGRESS','MEDIUM','2026-02-14 09:00:00','2026-02-19 14:00:00',56,116,19,8,5)
,
  (663,'Annonces premium mises en avant','Annonces premium mises en avant','IN_PROGRESS','HIGHEST','2026-02-15 09:00:00','2026-02-20 14:00:00',56,116,19,8,5)
,
  (664,'Photos professionnels','Photos professionnels','IN_PROGRESS','LOW','2026-02-16 09:00:00','2026-02-18 14:00:00',56,116,19,6,1)
,
  (665,'Rapport zone geo immobilier','Rapport zone geo immobilier','TODO','MEDIUM','2026-02-17 09:00:00','2026-02-22 14:00:00',56,116,19,7,5)
,
  (666,'Gestion des visites groupees','Gestion des visites groupees','IN_PROGRESS','HIGH','2026-02-18 09:00:00','2026-02-22 14:00:00',56,116,19,8,5)
,
  (667,'Alerte bien correspondant criteres','Alerte bien correspondant criteres','IN_PROGRESS','MEDIUM','2026-02-19 09:00:00','2026-02-22 14:00:00',56,116,19,8,4)
,
  (668,'Gestion des emmenagements','Gestion des emmenagements','IN_PROGRESS','LOW','2026-02-20 09:00:00','2026-02-23 14:00:00',56,116,19,8,5)
,
  (669,'Suivi consommation energetique','Suivi consommation energetique','IN_PROGRESS','HIGHEST','2026-02-21 09:00:00','2026-02-24 14:00:00',56,116,19,7,1)
,
  (670,'Diagnostics obligatoires','Diagnostics obligatoires','TODO','MEDIUM','2026-02-22 09:00:00','2026-02-28 14:00:00',56,116,19,6,4)
,
  (671,'Gestion copropriete et syndic','Gestion copropriete et syndic','NEW','HIGH','2026-02-17 09:00:00',NULL,57,117,19,8,4)
,
  (672,'Archives transactions passees','Archives transactions passees','NEW','MEDIUM','2026-02-18 09:00:00',NULL,57,117,19,7,5)
,
  (673,'Rapport rotation biens locatifs','Rapport rotation biens locatifs','TODO','HIGHEST','2026-02-19 09:00:00',NULL,57,117,19,6,4)
,
  (674,'Gestion des charges locatives','Gestion des charges locatives','NEW','MEDIUM','2026-02-20 09:00:00',NULL,57,117,19,9,5)
,
  (675,'Suivi delais relocation','Suivi delais relocation','NEW','LOW','2026-02-21 09:00:00',NULL,57,117,19,9,5)
,
  (676,'Mode masque pour acheteur','Mode masque pour acheteur','TODO','MEDIUM','2026-02-22 09:00:00',NULL,57,117,19,7,4)
,
  (677,'Gestion mandats exclusifs','Gestion mandats exclusifs','TODO','HIGH','2026-02-23 09:00:00',NULL,57,117,19,9,4)
,
  (678,'Export annonces portails','Export annonces portails','NEW','HIGHEST','2026-02-24 09:00:00',NULL,57,117,19,8,1)
,
  (679,'Rapport performances agence','Rapport performances agence','NEW','LOW','2026-02-25 09:00:00',NULL,57,117,19,7,1)
,
  (680,'Gestion des contre-propositions','Gestion des contre-propositions','NEW','MEDIUM','2026-02-26 09:00:00',NULL,57,117,19,6,1)
,
  (681,'Mode estimation instantanee','Mode estimation instantanee','NEW','HIGH','2026-02-27 09:00:00',NULL,57,117,19,7,4)
,
  (682,'Gestion des lots de parking','Gestion des lots de parking','TODO','MEDIUM','2026-02-28 09:00:00',NULL,57,117,19,7,4)
,
  (683,'Rapport immobilier locatif','Rapport immobilier locatif','TODO','HIGHEST','2026-02-28 09:00:00',NULL,57,117,19,8,5)
,
  (684,'Mode visites virtuelles groupees','Mode visites virtuelles groupees','TODO','LOW','2026-02-28 09:00:00',NULL,57,117,19,6,4)
,
  (685,'Gestion des acheteurs prequalifies','Gestion des acheteurs prequalifies','TODO','MEDIUM','2026-02-28 09:00:00',NULL,57,117,19,8,1)
,
  (686,'Statistiques delais de vente','Statistiques delais de vente','TODO','HIGH','2026-02-28 09:00:00',NULL,57,117,19,6,5)
,
  (687,'Mode alerte nouveau bien','Mode alerte nouveau bien','TODO','MEDIUM','2026-02-28 09:00:00',NULL,57,117,19,7,1)
,
  (688,'Gestion des promotions immobilières','Gestion des promotions immobilières','NEW','LOW','2026-02-28 09:00:00',NULL,57,117,19,8,5)
,
  (689,'Rapport satisfaction clients agence','Rapport satisfaction clients agence','TODO','HIGHEST','2026-02-28 09:00:00',NULL,57,117,19,6,1)
,
  (690,'Mode estimation loyer','Mode estimation loyer','TODO','MEDIUM','2026-02-28 09:00:00',NULL,57,117,19,8,5)
,
  (691,'Liste restaurants partenaires','Liste restaurants partenaires','DONE','HIGH','2026-01-20 09:00:00','2026-02-28 16:00:00',58,118,20,9,5)
,
  (692,'Affichage menus et plats','Affichage menus et plats','DONE','MEDIUM','2026-01-21 09:00:00','2026-02-28 16:00:00',58,118,20,9,5)
,
  (693,'Filtres cuisine et prix','Filtres cuisine et prix','DONE','HIGHEST','2026-01-22 09:00:00','2026-02-26 16:00:00',58,118,20,8,4)
,
  (694,'Panier de commande repas','Panier de commande repas','DONE','MEDIUM','2026-01-23 09:00:00','2026-02-26 16:00:00',58,118,20,8,4)
,
  (695,'Choix du mode de livraison','Choix du mode de livraison','DONE','LOW','2026-01-24 09:00:00','2026-02-28 16:00:00',58,118,20,7,4)
,
  (696,'Validation et paiement commande','Validation et paiement commande','IN_REVIEW','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',58,118,20,9,1)
,
  (697,'Suivi commande temps reel','Suivi commande temps reel','IN_REVIEW','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',58,118,20,7,4)
,
  (698,'Notification commande acceptee','Notification commande acceptee','DONE','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',58,118,20,8,4)
,
  (699,'Notification livreur en route','Notification livreur en route','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',58,118,20,6,4)
,
  (700,'Confirmation de livraison','Confirmation de livraison','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',58,118,20,6,4)
,
  (701,'Gestion du menu restaurant','Gestion du menu restaurant','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',58,118,20,7,1)
,
  (702,'Ajout et modification plats','Ajout et modification plats','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',58,118,20,9,5)
,
  (703,'Gestion horaires ouverture','Gestion horaires ouverture','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',58,118,20,7,4)
,
  (704,'Zone de livraison config','Zone de livraison config','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',58,118,20,7,1)
,
  (705,'Affectation livreur automatique','Affectation livreur automatique','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',58,118,20,9,4)
,
  (706,'Itineraire livreur GPS','Itineraire livreur GPS','IN_REVIEW','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',58,118,20,6,1)
,
  (707,'Suivi position livreur temps reel','Suivi position livreur temps reel','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',58,118,20,9,1)
,
  (708,'Historique commandes client','Historique commandes client','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',58,118,20,8,1)
,
  (709,'Note et avis apres livraison','Note et avis apres livraison','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',58,118,20,9,5)
,
  (710,'Signaler un probleme','Signaler un probleme','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',58,118,20,8,5)
,
  (711,'Programme fidelite client','Programme fidelite client','IN_PROGRESS','HIGH','2026-02-03 09:00:00','2026-02-07 14:00:00',59,119,20,9,1)
,
  (712,'Code promo restaurant','Code promo restaurant','IN_PROGRESS','MEDIUM','2026-02-04 09:00:00','2026-02-06 14:00:00',59,119,20,9,4)
,
  (713,'Rapport ventes restaurant','Rapport ventes restaurant','TODO','HIGHEST','2026-02-05 09:00:00','2026-02-11 14:00:00',59,119,20,6,5)
,
  (714,'Delai moyen livraison stats','Delai moyen livraison stats','IN_PROGRESS','MEDIUM','2026-02-06 09:00:00','2026-02-10 14:00:00',59,119,20,8,5)
,
  (715,'Gestion des remboursements','Gestion des remboursements','IN_PROGRESS','LOW','2026-02-07 09:00:00','2026-02-12 14:00:00',59,119,20,9,5)
,
  (716,'Suggestion historique commandes','Suggestion historique commandes','TODO','MEDIUM','2026-02-08 09:00:00','2026-02-14 14:00:00',59,119,20,8,5)
,
  (717,'Commande rapide reorder','Commande rapide reorder','IN_PROGRESS','HIGH','2026-02-09 09:00:00','2026-02-14 14:00:00',59,119,20,7,1)
,
  (718,'Restaurant du jour','Restaurant du jour','IN_PROGRESS','HIGHEST','2026-02-10 09:00:00','2026-02-14 14:00:00',59,119,20,8,5)
,
  (719,'Promotions flash temps limit','Promotions flash temps limit','IN_PROGRESS','LOW','2026-02-11 09:00:00','2026-02-17 14:00:00',59,119,20,7,5)
,
  (720,'Livraison gratuite seuil','Livraison gratuite seuil','TODO','MEDIUM','2026-02-12 09:00:00','2026-02-14 14:00:00',59,119,20,6,1)
,
  (721,'Restaurants ouverts maintenent','Restaurants ouverts maintenent','IN_PROGRESS','HIGH','2026-02-13 09:00:00','2026-02-19 14:00:00',59,119,20,8,4)
,
  (722,'Commande groupee entre amis','Commande groupee entre amis','TODO','MEDIUM','2026-02-14 09:00:00','2026-02-20 14:00:00',59,119,20,7,5)
,
  (723,'Gestion des horaires pic','Gestion des horaires pic','IN_PROGRESS','HIGHEST','2026-02-15 09:00:00','2026-02-19 14:00:00',59,119,20,8,5)
,
  (724,'Suivi satisfaction livreur','Suivi satisfaction livreur','IN_PROGRESS','LOW','2026-02-16 09:00:00','2026-02-20 14:00:00',59,119,20,9,5)
,
  (725,'Rapport taux reussite livraison','Rapport taux reussite livraison','TODO','MEDIUM','2026-02-17 09:00:00','2026-02-23 14:00:00',59,119,20,8,5)
,
  (726,'Mode reservation futur','Mode reservation futur','TODO','HIGH','2026-02-18 09:00:00','2026-02-24 14:00:00',59,119,20,9,5)
,
  (727,'Gestion annulation restaurant','Gestion annulation restaurant','IN_PROGRESS','MEDIUM','2026-02-19 09:00:00','2026-02-23 14:00:00',59,119,20,9,1)
,
  (728,'Calendrier menus speciaux','Calendrier menus speciaux','IN_PROGRESS','LOW','2026-02-20 09:00:00','2026-02-26 14:00:00',59,119,20,6,4)
,
  (729,'Gestion ingredients et allergenes','Gestion ingredients et allergenes','IN_PROGRESS','HIGHEST','2026-02-21 09:00:00','2026-02-26 14:00:00',59,119,20,6,5)
,
  (730,'Nutrition info par plat','Nutrition info par plat','IN_PROGRESS','MEDIUM','2026-02-22 09:00:00','2026-02-24 14:00:00',59,119,20,7,5)
,
  (731,'Suivi carbone livraison','Suivi carbone livraison','NEW','HIGH','2026-02-17 09:00:00',NULL,60,120,20,9,1)
,
  (732,'Mode eco-responsable','Mode eco-responsable','NEW','MEDIUM','2026-02-18 09:00:00',NULL,60,120,20,9,1)
,
  (733,'Partenariat avec producteurs','Partenariat avec producteurs','TODO','HIGHEST','2026-02-19 09:00:00',NULL,60,120,20,6,5)
,
  (734,'Commande anticipate','Commande anticipate','TODO','MEDIUM','2026-02-20 09:00:00',NULL,60,120,20,7,1)
,
  (735,'Rapport performance cuisine','Rapport performance cuisine','NEW','LOW','2026-02-21 09:00:00',NULL,60,120,20,8,4)
,
  (736,'Gestion du personnel cuisine','Gestion du personnel cuisine','TODO','MEDIUM','2026-02-22 09:00:00',NULL,60,120,20,9,5)
,
  (737,'Planification rotations livreurs','Planification rotations livreurs','TODO','HIGH','2026-02-23 09:00:00',NULL,60,120,20,9,5)
,
  (738,'Mode restaurant partenaire','Mode restaurant partenaire','NEW','HIGHEST','2026-02-24 09:00:00',NULL,60,120,20,9,5)
,
  (739,'Suivi benefice par plat','Suivi benefice par plat','TODO','LOW','2026-02-25 09:00:00',NULL,60,120,20,7,5)
,
  (740,'Statistiques popularite plats','Statistiques popularite plats','TODO','MEDIUM','2026-02-26 09:00:00',NULL,60,120,20,8,5)
,
  (741,'Mode menu degustation','Mode menu degustation','NEW','HIGH','2026-02-27 09:00:00',NULL,60,120,20,9,4)
,
  (742,'Gestion des stocks cuisine','Gestion des stocks cuisine','TODO','MEDIUM','2026-02-28 09:00:00',NULL,60,120,20,9,5)
,
  (743,'Rapport delai preparation','Rapport delai preparation','NEW','HIGHEST','2026-02-28 09:00:00',NULL,60,120,20,8,5)
,
  (744,'Gestion des precommandes','Gestion des precommandes','TODO','LOW','2026-02-28 09:00:00',NULL,60,120,20,9,1)
,
  (745,'Mode click and collect','Mode click and collect','TODO','MEDIUM','2026-02-28 09:00:00',NULL,60,120,20,8,5)
,
  (746,'Statistiques taux reussite plats','Statistiques taux reussite plats','TODO','HIGH','2026-02-28 09:00:00',NULL,60,120,20,7,4)
,
  (747,'Mode livraison express','Mode livraison express','TODO','MEDIUM','2026-02-28 09:00:00',NULL,60,120,20,7,5)
,
  (748,'Gestion des zones de livraison','Gestion des zones de livraison','NEW','LOW','2026-02-28 09:00:00',NULL,60,120,20,8,4)
,
  (749,'Rapport satisfaction globale','Rapport satisfaction globale','NEW','HIGHEST','2026-02-28 09:00:00',NULL,60,120,20,8,4)
,
  (750,'Mode restaurant pop-up','Mode restaurant pop-up','TODO','MEDIUM','2026-02-28 09:00:00',NULL,60,120,20,9,4)
,
  (751,'Catalogue de livres base','Catalogue de livres base','DONE','HIGH','2026-01-20 09:00:00','2026-02-23 16:00:00',61,121,21,7,4)
,
  (752,'Recherche par titre auteur ISBN','Recherche par titre auteur ISBN','DONE','MEDIUM','2026-01-21 09:00:00','2026-02-27 16:00:00',61,121,21,6,4)
,
  (753,'Details livre et disponibilite','Details livre et disponibilite','DONE','HIGHEST','2026-01-22 09:00:00','2026-02-26 16:00:00',61,121,21,9,1)
,
  (754,'Gestion genres litteraires','Gestion genres litteraires','IN_REVIEW','MEDIUM','2026-01-23 09:00:00','2026-02-28 16:00:00',61,121,21,8,5)
,
  (755,'Emprunter un livre','Emprunter un livre','DONE','LOW','2026-01-24 09:00:00','2026-02-28 16:00:00',61,121,21,7,1)
,
  (756,'Retourner un livre','Retourner un livre','IN_REVIEW','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',61,121,21,7,1)
,
  (757,'Prolonger un emprunt','Prolonger un emprunt','IN_REVIEW','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',61,121,21,7,5)
,
  (758,'Historique des emprunts','Historique des emprunts','DONE','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',61,121,21,6,4)
,
  (759,'Gestion penalites retard','Gestion penalites retard','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',61,121,21,6,5)
,
  (760,'Calcul amende retard','Calcul amende retard','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',61,121,21,7,4)
,
  (761,'Inscription nouveau membre','Inscription nouveau membre','IN_REVIEW','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',61,121,21,9,5)
,
  (762,'Profil membre preferences','Profil membre preferences','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',61,121,21,8,4)
,
  (763,'Notifications retour livre','Notifications retour livre','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',61,121,21,6,5)
,
  (764,'Reservation livre indisponible','Reservation livre indisponible','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',61,121,21,6,1)
,
  (765,'Liste d attente reservation','Liste d attente reservation','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',61,121,21,6,4)
,
  (766,'Rapport livres populaires','Rapport livres populaires','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',61,121,21,8,1)
,
  (767,'Inventaire periodique livres','Inventaire periodique livres','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',61,121,21,6,4)
,
  (768,'Gestion dons de livres','Gestion dons de livres','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',61,121,21,9,1)
,
  (769,'Alerte nouveau livre arrivee','Alerte nouveau livre arrivee','IN_REVIEW','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',61,121,21,8,1)
,
  (770,'Recommandations personnalisees','Recommandations personnalisees','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',61,121,21,7,5)
,
  (771,'Statistiques frequentation','Statistiques frequentation','IN_PROGRESS','HIGH','2026-02-03 09:00:00','2026-02-07 14:00:00',62,122,21,7,1)
,
  (772,'Gestion abonnement annuel','Gestion abonnement annuel','IN_PROGRESS','MEDIUM','2026-02-04 09:00:00','2026-02-10 14:00:00',62,122,21,7,1)
,
  (773,'Carte de membre numerique','Carte de membre numerique','TODO','HIGHEST','2026-02-05 09:00:00','2026-02-09 14:00:00',62,122,21,9,5)
,
  (774,'Export catalogue bibliotheque','Export catalogue bibliotheque','IN_PROGRESS','MEDIUM','2026-02-06 09:00:00','2026-02-08 14:00:00',62,122,21,9,5)
,
  (775,'Quiz litteraire interactif','Quiz litteraire interactif','IN_PROGRESS','LOW','2026-02-07 09:00:00','2026-02-11 14:00:00',62,122,21,7,4)
,
  (776,'Club de lecture en ligne','Club de lecture en ligne','IN_PROGRESS','MEDIUM','2026-02-08 09:00:00','2026-02-13 14:00:00',62,122,21,6,4)
,
  (777,'Livre du mois recommandation','Livre du mois recommandation','IN_PROGRESS','HIGH','2026-02-09 09:00:00','2026-02-13 14:00:00',62,122,21,9,4)
,
  (778,'Suggestion lecture age','Suggestion lecture age','IN_PROGRESS','HIGHEST','2026-02-10 09:00:00','2026-02-14 14:00:00',62,122,21,6,4)
,
  (779,'Gestion res numeriques','Gestion res numeriques','IN_PROGRESS','LOW','2026-02-11 09:00:00','2026-02-13 14:00:00',62,122,21,6,5)
,
  (780,'Abonnement revues et presse','Abonnement revues et presse','TODO','MEDIUM','2026-02-12 09:00:00','2026-02-17 14:00:00',62,122,21,8,1)
,
  (781,'Salle de lecture et etude','Salle de lecture et etude','TODO','HIGH','2026-02-13 09:00:00','2026-02-15 14:00:00',62,122,21,8,5)
,
  (782,'Evenements litteraires','Evenements litteraires','IN_PROGRESS','MEDIUM','2026-02-14 09:00:00','2026-02-17 14:00:00',62,122,21,8,5)
,
  (783,'Gestion des dons et legs','Gestion des dons et legs','IN_PROGRESS','HIGHEST','2026-02-15 09:00:00','2026-02-18 14:00:00',62,122,21,7,4)
,
  (784,'Partenariat ecole et universite','Partenariat ecole et universite','TODO','LOW','2026-02-16 09:00:00','2026-02-22 14:00:00',62,122,21,8,4)
,
  (785,'Rapport emprunts par categorie','Rapport emprunts par categorie','TODO','MEDIUM','2026-02-17 09:00:00','2026-02-23 14:00:00',62,122,21,7,1)
,
  (786,'Mode recherche avancee','Mode recherche avancee','IN_PROGRESS','HIGH','2026-02-18 09:00:00','2026-02-22 14:00:00',62,122,21,9,5)
,
  (787,'Gestion des reserves speciales','Gestion des reserves speciales','IN_PROGRESS','MEDIUM','2026-02-19 09:00:00','2026-02-25 14:00:00',62,122,21,6,1)
,
  (788,'Visite virtuelle bibliotheque','Visite virtuelle bibliotheque','TODO','LOW','2026-02-20 09:00:00','2026-02-26 14:00:00',62,122,21,7,5)
,
  (789,'Signalisations livres endommages','Signalisations livres endommages','IN_PROGRESS','HIGHEST','2026-02-21 09:00:00','2026-02-27 14:00:00',62,122,21,7,4)
,
  (790,'Gestion restauration livres','Gestion restauration livres','TODO','MEDIUM','2026-02-22 09:00:00','2026-02-25 14:00:00',62,122,21,6,5)
,
  (791,'Rapport activite trimestriel','Rapport activite trimestriel','NEW','HIGH','2026-02-17 09:00:00',NULL,63,123,21,9,1)
,
  (792,'Gestion benevoles','Gestion benevoles','NEW','MEDIUM','2026-02-18 09:00:00',NULL,63,123,21,9,1)
,
  (793,'Mode reservation salle lecture','Mode reservation salle lecture','NEW','HIGHEST','2026-02-19 09:00:00',NULL,63,123,21,7,5)
,
  (794,'Catalogue livres anciens','Catalogue livres anciens','NEW','MEDIUM','2026-02-20 09:00:00',NULL,63,123,21,9,5)
,
  (795,'Recherche plein texte contenus','Recherche plein texte contenus','TODO','LOW','2026-02-21 09:00:00',NULL,63,123,21,9,5)
,
  (796,'Notifications disponibilite','Notifications disponibilite','TODO','MEDIUM','2026-02-22 09:00:00',NULL,63,123,21,6,4)
,
  (797,'Gestion exclusions temporelles','Gestion exclusions temporelles','TODO','HIGH','2026-02-23 09:00:00',NULL,63,123,21,6,5)
,
  (798,'Statistiques emprunts en ligne','Statistiques emprunts en ligne','TODO','HIGHEST','2026-02-24 09:00:00',NULL,63,123,21,6,1)
,
  (799,'Rapport satisfaction usagers','Rapport satisfaction usagers','NEW','LOW','2026-02-25 09:00:00',NULL,63,123,21,8,5)
,
  (800,'Mode self-service retour','Mode self-service retour','TODO','MEDIUM','2026-02-26 09:00:00',NULL,63,123,21,6,1)
,
  (801,'Gestion des abonnements e-book','Gestion des abonnements e-book','NEW','HIGH','2026-02-27 09:00:00',NULL,63,123,21,6,4)
,
  (802,'Mode lecture cooperative','Mode lecture cooperative','NEW','MEDIUM','2026-02-28 09:00:00',NULL,63,123,21,7,1)
,
  (803,'Rapport age emprunteurs','Rapport age emprunteurs','NEW','HIGHEST','2026-02-28 09:00:00',NULL,63,123,21,8,4)
,
  (804,'Gestion des evenements culturels','Gestion des evenements culturels','NEW','LOW','2026-02-28 09:00:00',NULL,63,123,21,9,4)
,
  (805,'Mode suggestion automatique','Mode suggestion automatique','TODO','MEDIUM','2026-02-28 09:00:00',NULL,63,123,21,7,5)
,
  (806,'Statistiques genres preferes','Statistiques genres preferes','NEW','HIGH','2026-02-28 09:00:00',NULL,63,123,21,6,1)
,
  (807,'Gestion des partenariats editeurs','Gestion des partenariats editeurs','NEW','MEDIUM','2026-02-28 09:00:00',NULL,63,123,21,9,5)
,
  (808,'Mode reservation inter-bibliotheques','Mode reservation inter-bibliotheques','TODO','LOW','2026-02-28 09:00:00',NULL,63,123,21,7,1)
,
  (809,'Rapport d activite annuel','Rapport d activite annuel','TODO','HIGHEST','2026-02-28 09:00:00',NULL,63,123,21,8,4)
,
  (810,'Mode visites scolaires','Mode visites scolaires','NEW','MEDIUM','2026-02-28 09:00:00',NULL,63,123,21,9,5)
,
  (811,'Affichage places disponibles','Affichage places disponibles','DONE','HIGH','2026-01-20 09:00:00','2026-02-26 16:00:00',64,124,22,7,1)
,
  (812,'Plan du parking interactif','Plan du parking interactif','DONE','MEDIUM','2026-01-21 09:00:00','2026-02-25 16:00:00',64,124,22,7,4)
,
  (813,'Filtrer par zone et etage','Filtrer par zone et etage','DONE','HIGHEST','2026-01-22 09:00:00','2026-02-28 16:00:00',64,124,22,6,5)
,
  (814,'Reserver une place de parking','Reserver une place de parking','IN_REVIEW','MEDIUM','2026-01-23 09:00:00','2026-02-27 16:00:00',64,124,22,6,1)
,
  (815,'Annuler une reservation','Annuler une reservation','DONE','LOW','2026-01-24 09:00:00','2026-02-27 16:00:00',64,124,22,6,5)
,
  (816,'Modifier reservation existante','Modifier reservation existante','IN_REVIEW','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',64,124,22,6,1)
,
  (817,'Historique des reservations','Historique des reservations','DONE','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',64,124,22,9,4)
,
  (818,'Paiement en ligne stationnement','Paiement en ligne stationnement','IN_REVIEW','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',64,124,22,7,1)
,
  (819,'Paiement sur place QR code','Paiement sur place QR code','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',64,124,22,8,1)
,
  (820,'Facture automatique stationnement','Facture automatique stationnement','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',64,124,22,8,5)
,
  (821,'Tarification dinamique horaire','Tarification dinamique horaire','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',64,124,22,7,4)
,
  (822,'Gestion abonnement parking','Gestion abonnement parking','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',64,124,22,7,4)
,
  (823,'Detection immatriculation camera','Detection immatriculation camera','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',64,124,22,6,5)
,
  (824,'Gestion acces vehicule','Gestion acces vehicule','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',64,124,22,9,5)
,
  (825,'Alerte fin de temps stationnement','Alerte fin de temps stationnement','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',64,124,22,8,5)
,
  (826,'Rapport occupation parking','Rapport occupation parking','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',64,124,22,8,5)
,
  (827,'Statistiques heures piques','Statistiques heures piques','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',64,124,22,7,4)
,
  (828,'Gestion des abonnes','Gestion des abonnes','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',64,124,22,9,1)
,
  (829,'Declaration sinistre vehicule','Declaration sinistre vehicule','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',64,124,22,7,4)
,
  (830,'Contact urgence parking','Contact urgence parking','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',64,124,22,9,4)
,
  (831,'Reglage tarifs horaires','Reglage tarifs horaires','TODO','HIGH','2026-02-03 09:00:00','2026-02-06 14:00:00',65,125,22,7,4)
,
  (832,'Gestion parking 2 roues','Gestion parking 2 roues','IN_PROGRESS','MEDIUM','2026-02-04 09:00:00','2026-02-06 14:00:00',65,125,22,7,4)
,
  (833,'Parking PMR accessibilite','Parking PMR accessibilite','TODO','HIGHEST','2026-02-05 09:00:00','2026-02-11 14:00:00',65,125,22,8,1)
,
  (834,'Cameras surveillance securite','Cameras surveillance securite','IN_PROGRESS','MEDIUM','2026-02-06 09:00:00','2026-02-11 14:00:00',65,125,22,6,4)
,
  (835,'Export donnees stationnement','Export donnees stationnement','TODO','LOW','2026-02-07 09:00:00','2026-02-10 14:00:00',65,125,22,6,1)
,
  (836,'Alerte places bientot libres','Alerte places bientot libres','TODO','MEDIUM','2026-02-08 09:00:00','2026-02-11 14:00:00',65,125,22,7,4)
,
  (837,'Mode estimation duree stationnement','Mode estimation duree stationnement','IN_PROGRESS','HIGH','2026-02-09 09:00:00','2026-02-12 14:00:00',65,125,22,8,1)
,
  (838,'Rappel fin parking','Rappel fin parking','IN_PROGRESS','HIGHEST','2026-02-10 09:00:00','2026-02-15 14:00:00',65,125,22,6,5)
,
  (839,'Gestion evenements parking','Gestion evenements parking','IN_PROGRESS','LOW','2026-02-11 09:00:00','2026-02-14 14:00:00',65,125,22,7,4)
,
  (840,'Rapport revenus journalier','Rapport revenus journalier','TODO','MEDIUM','2026-02-12 09:00:00','2026-02-17 14:00:00',65,125,22,8,4)
,
  (841,'Gestion maintenance parkometre','Gestion maintenance parkometre','TODO','HIGH','2026-02-13 09:00:00','2026-02-17 14:00:00',65,125,22,8,4)
,
  (842,'Mode navette parking centre','Mode navette parking centre','IN_PROGRESS','MEDIUM','2026-02-14 09:00:00','2026-02-16 14:00:00',65,125,22,9,4)
,
  (843,'Reservation longue duree','Reservation longue duree','TODO','HIGHEST','2026-02-15 09:00:00','2026-02-19 14:00:00',65,125,22,8,1)
,
  (844,'Gestion prestation valet','Gestion prestation valet','IN_PROGRESS','LOW','2026-02-16 09:00:00','2026-02-22 14:00:00',65,125,22,9,1)
,
  (845,'Statistiques taux occupation','Statistiques taux occupation','IN_PROGRESS','MEDIUM','2026-02-17 09:00:00','2026-02-21 14:00:00',65,125,22,7,1)
,
  (846,'Rapport performance parking','Rapport performance parking','TODO','HIGH','2026-02-18 09:00:00','2026-02-21 14:00:00',65,125,22,9,4)
,
  (847,'Mode tarif preferentiel horaire','Mode tarif preferentiel horaire','IN_PROGRESS','MEDIUM','2026-02-19 09:00:00','2026-02-24 14:00:00',65,125,22,8,1)
,
  (848,'Gestion des zones VIP','Gestion des zones VIP','TODO','LOW','2026-02-20 09:00:00','2026-02-26 14:00:00',65,125,22,7,4)
,
  (849,'Alerte meteo parking','Alerte meteo parking','IN_PROGRESS','HIGHEST','2026-02-21 09:00:00','2026-02-26 14:00:00',65,125,22,8,4)
,
  (850,'Plan evacuation urgence','Plan evacuation urgence','TODO','MEDIUM','2026-02-22 09:00:00','2026-02-26 14:00:00',65,125,22,7,5)
,
  (851,'Suivi consommation energie','Suivi consommation energie','TODO','HIGH','2026-02-17 09:00:00',NULL,66,126,22,7,5)
,
  (852,'Gestion eclairage intelligent','Gestion eclairage intelligent','NEW','MEDIUM','2026-02-18 09:00:00',NULL,66,126,22,9,1)
,
  (853,'Rapport impact environnemental','Rapport impact environnemental','NEW','HIGHEST','2026-02-19 09:00:00',NULL,66,126,22,6,5)
,
  (854,'Gestion stationnement temporaire','Gestion stationnement temporaire','TODO','MEDIUM','2026-02-20 09:00:00',NULL,66,126,22,6,5)
,
  (855,'Mode reservation evenementiel','Mode reservation evenementiel','TODO','LOW','2026-02-21 09:00:00',NULL,66,126,22,6,4)
,
  (856,'Gestion des penalites','Gestion des penalites','TODO','MEDIUM','2026-02-22 09:00:00',NULL,66,126,22,7,1)
,
  (857,'Rapport frequentation mensuel','Rapport frequentation mensuel','TODO','HIGH','2026-02-23 09:00:00',NULL,66,126,22,6,1)
,
  (858,'Mode contactless entree sortie','Mode contactless entree sortie','NEW','HIGHEST','2026-02-24 09:00:00',NULL,66,126,22,9,4)
,
  (859,'Suivi vitesse dans parking','Suivi vitesse dans parking','TODO','LOW','2026-02-25 09:00:00',NULL,66,126,22,9,5)
,
  (860,'Gestion places premium','Gestion places premium','NEW','MEDIUM','2026-02-26 09:00:00',NULL,66,126,22,6,1)
,
  (861,'Mode abonnement entreprise','Mode abonnement entreprise','NEW','HIGH','2026-02-27 09:00:00',NULL,66,126,22,9,1)
,
  (862,'Gestion des visiteurs temporaires','Gestion des visiteurs temporaires','TODO','MEDIUM','2026-02-28 09:00:00',NULL,66,126,22,8,5)
,
  (863,'Rapport delais stationnement','Rapport delais stationnement','TODO','HIGHEST','2026-02-28 09:00:00',NULL,66,126,22,6,5)
,
  (864,'Mode parking collectif','Mode parking collectif','NEW','LOW','2026-02-28 09:00:00',NULL,66,126,22,7,1)
,
  (865,'Gestion des bornes de recharge','Gestion des bornes de recharge','TODO','MEDIUM','2026-02-28 09:00:00',NULL,66,126,22,7,1)
,
  (866,'Statistiques temps moyen stationnement','Statistiques temps moyen stationnement','TODO','HIGH','2026-02-28 09:00:00',NULL,66,126,22,8,4)
,
  (867,'Mode stationnement intelligent','Mode stationnement intelligent','TODO','MEDIUM','2026-02-28 09:00:00',NULL,66,126,22,9,1)
,
  (868,'Gestion des partenariats transport','Gestion des partenariats transport','NEW','LOW','2026-02-28 09:00:00',NULL,66,126,22,9,4)
,
  (869,'Rapport benefices mensuels','Rapport benefices mensuels','NEW','HIGHEST','2026-02-28 09:00:00',NULL,66,126,22,7,5)
,
  (870,'Mode ecoparking','Mode ecoparking','TODO','MEDIUM','2026-02-28 09:00:00',NULL,66,126,22,8,5)
,
  (871,'Creer un evenement','Creer un evenement','DONE','HIGH','2026-01-20 09:00:00','2026-02-26 16:00:00',67,127,23,6,1)
,
  (872,'Configurer lieux et dates','Configurer lieux et dates','DONE','MEDIUM','2026-01-21 09:00:00','2026-02-27 16:00:00',67,127,23,9,5)
,
  (873,'Definir le programme','Definir le programme','DONE','HIGHEST','2026-01-22 09:00:00','2026-02-26 16:00:00',67,127,23,6,5)
,
  (874,'Inscription en ligne','Inscription en ligne','DONE','MEDIUM','2026-01-23 09:00:00','2026-02-28 16:00:00',67,127,23,9,4)
,
  (875,'Gestion des tarifs evenement','Gestion des tarifs evenement','DONE','LOW','2026-01-24 09:00:00','2026-02-27 16:00:00',67,127,23,6,5)
,
  (876,'Paiement des inscriptions','Paiement des inscriptions','DONE','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',67,127,23,7,5)
,
  (877,'Confirmation par email','Confirmation par email','IN_REVIEW','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',67,127,23,8,1)
,
  (878,'Liste des inscrits','Liste des inscrits','DONE','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',67,127,23,6,1)
,
  (879,'Gestion liste d attente','Gestion liste d attente','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',67,127,23,7,5)
,
  (880,'Check-in jour J par QR','Check-in jour J par QR','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',67,127,23,6,4)
,
  (881,'Badge de participant','Badge de participant','IN_REVIEW','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',67,127,23,7,5)
,
  (882,'Certificat de participation','Certificat de participation','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',67,127,23,8,4)
,
  (883,'Photo et video evenement','Photo et video evenement','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',67,127,23,6,5)
,
  (884,'Feedback post-evenement','Feedback post-evenement','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',67,127,23,6,1)
,
  (885,'Rapport de participation','Rapport de participation','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',67,127,23,6,4)
,
  (886,'Gestion des intervenants','Gestion des intervenants','IN_REVIEW','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',67,127,23,9,4)
,
  (887,'Programme et plannings salles','Programme et plannings salles','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',67,127,23,9,4)
,
  (888,'Salle et equipements','Salle et equipements','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',67,127,23,9,4)
,
  (889,'Restauration et traiteur','Restauration et traiteur','IN_REVIEW','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',67,127,23,9,1)
,
  (890,'Transport et hebergement','Transport et hebergement','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',67,127,23,7,5)
,
  (891,'Communication et invitation','Communication et invitation','IN_PROGRESS','HIGH','2026-02-03 09:00:00','2026-02-08 14:00:00',68,128,23,8,1)
,
  (892,'Sponsor et partenaires','Sponsor et partenaires','IN_PROGRESS','MEDIUM','2026-02-04 09:00:00','2026-02-08 14:00:00',68,128,23,9,4)
,
  (893,'Compte-rendu evenement','Compte-rendu evenement','IN_PROGRESS','HIGHEST','2026-02-05 09:00:00','2026-02-09 14:00:00',68,128,23,6,1)
,
  (894,'Archives et bilan','Archives et bilan','TODO','MEDIUM','2026-02-06 09:00:00','2026-02-08 14:00:00',68,128,23,8,4)
,
  (895,'Planification prochain evenement','Planification prochain evenement','IN_PROGRESS','LOW','2026-02-07 09:00:00','2026-02-12 14:00:00',68,128,23,6,1)
,
  (896,'Gestion des invites VIP','Gestion des invites VIP','TODO','MEDIUM','2026-02-08 09:00:00','2026-02-10 14:00:00',68,128,23,9,1)
,
  (897,'Billetterie en ligne securisee','Billetterie en ligne securisee','IN_PROGRESS','HIGH','2026-02-09 09:00:00','2026-02-11 14:00:00',68,128,23,8,5)
,
  (898,'Gestion des benevoles','Gestion des benevoles','TODO','HIGHEST','2026-02-10 09:00:00','2026-02-12 14:00:00',68,128,23,8,4)
,
  (899,'Suivi budget evenement','Suivi budget evenement','IN_PROGRESS','LOW','2026-02-11 09:00:00','2026-02-13 14:00:00',68,128,23,9,4)
,
  (900,'Marketing evenement reseaux','Marketing evenement reseaux','TODO','MEDIUM','2026-02-12 09:00:00','2026-02-18 14:00:00',68,128,23,7,5)
,
  (901,'Location materiel evenement','Location materiel evenement','IN_PROGRESS','HIGH','2026-02-13 09:00:00','2026-02-16 14:00:00',68,128,23,9,1)
,
  (902,'Gestion des lots et prix','Gestion des lots et prix','IN_PROGRESS','MEDIUM','2026-02-14 09:00:00','2026-02-19 14:00:00',68,128,23,8,4)
,
  (903,'Mode streaming en direct','Mode streaming en direct','IN_PROGRESS','HIGHEST','2026-02-15 09:00:00','2026-02-20 14:00:00',68,128,23,6,1)
,
  (904,'Rapport retombes mediatiques','Rapport retombes mediatiques','IN_PROGRESS','LOW','2026-02-16 09:00:00','2026-02-21 14:00:00',68,128,23,9,4)
,
  (905,'Gestion des espaces exterieurs','Gestion des espaces exterieurs','IN_PROGRESS','MEDIUM','2026-02-17 09:00:00','2026-02-20 14:00:00',68,128,23,8,4)
,
  (906,'Securite et plan urgence','Securite et plan urgence','TODO','HIGH','2026-02-18 09:00:00','2026-02-20 14:00:00',68,128,23,7,4)
,
  (907,'Gestion polophonie et son','Gestion polophonie et son','IN_PROGRESS','MEDIUM','2026-02-19 09:00:00','2026-02-24 14:00:00',68,128,23,8,1)
,
  (908,'Animation et conferencier','Animation et conferencier','IN_PROGRESS','LOW','2026-02-20 09:00:00','2026-02-25 14:00:00',68,128,23,9,4)
,
  (909,'Suivi satisfaction participants','Suivi satisfaction participants','IN_PROGRESS','HIGHEST','2026-02-21 09:00:00','2026-02-27 14:00:00',68,128,23,9,5)
,
  (910,'Rapport ROI evenement','Rapport ROI evenement','IN_PROGRESS','MEDIUM','2026-02-22 09:00:00','2026-02-28 14:00:00',68,128,23,8,4)
,
  (911,'Mode early bird tarification','Mode early bird tarification','TODO','HIGH','2026-02-17 09:00:00',NULL,69,129,23,9,4)
,
  (912,'Gestion des co-speakers','Gestion des co-speakers','TODO','MEDIUM','2026-02-18 09:00:00',NULL,69,129,23,9,4)
,
  (913,'Replay video evenement','Replay video evenement','TODO','HIGHEST','2026-02-19 09:00:00',NULL,69,129,23,9,5)
,
  (914,'Reseau alumni evenement','Reseau alumni evenement','TODO','MEDIUM','2026-02-20 09:00:00',NULL,69,129,23,8,4)
,
  (915,'Gestion stands exposition','Gestion stands exposition','TODO','LOW','2026-02-21 09:00:00',NULL,69,129,23,9,4)
,
  (916,'Mode visioconference hybrid','Mode visioconference hybrid','TODO','MEDIUM','2026-02-22 09:00:00',NULL,69,129,23,7,5)
,
  (917,'Gestion Covid protocoles','Gestion Covid protocoles','NEW','HIGH','2026-02-23 09:00:00',NULL,69,129,23,7,5)
,
  (918,'Rapport impact carbone','Rapport impact carbone','NEW','HIGHEST','2026-02-24 09:00:00',NULL,69,129,23,6,5)
,
  (919,'Satisfaction equipe organisatrice','Satisfaction equipe organisatrice','NEW','LOW','2026-02-25 09:00:00',NULL,69,129,23,7,4)
,
  (920,'Mode appreciation publique','Mode appreciation publique','TODO','MEDIUM','2026-02-26 09:00:00',NULL,69,129,23,6,4)
,
  (921,'Gestion des salles virtuelles','Gestion des salles virtuelles','NEW','HIGH','2026-02-27 09:00:00',NULL,69,129,23,7,5)
,
  (922,'Mode ateliers interactifs','Mode ateliers interactifs','TODO','MEDIUM','2026-02-28 09:00:00',NULL,69,129,23,7,4)
,
  (923,'Rapport retombes economiques','Rapport retombes economiques','TODO','HIGHEST','2026-02-28 09:00:00',NULL,69,129,23,9,4)
,
  (924,'Gestion des certifications CFA','Gestion des certifications CFA','TODO','LOW','2026-02-28 09:00:00',NULL,69,129,23,9,5)
,
  (925,'Mode workshops pratiques','Mode workshops pratiques','NEW','MEDIUM','2026-02-28 09:00:00',NULL,69,129,23,9,5)
,
  (926,'Statistiques participants','Statistiques participants','NEW','HIGH','2026-02-28 09:00:00',NULL,69,129,23,9,5)
,
  (927,'Gestion des partenariats media','Gestion des partenariats media','TODO','MEDIUM','2026-02-28 09:00:00',NULL,69,129,23,8,5)
,
  (928,'Mode evenement hybride','Mode evenement hybride','NEW','LOW','2026-02-28 09:00:00',NULL,69,129,23,6,1)
,
  (929,'Rapport d engagement','Rapport d engagement','TODO','HIGHEST','2026-02-28 09:00:00',NULL,69,129,23,8,1)
,
  (930,'Mode afterwork networking','Mode afterwork networking','NEW','MEDIUM','2026-02-28 09:00:00',NULL,69,129,23,6,5)
,
  (931,'Creer un questionnaire','Creer un questionnaire','DONE','HIGH','2026-01-20 09:00:00','2026-02-23 16:00:00',70,130,24,8,4)
,
  (932,'Types de questions variees','Types de questions variees','DONE','MEDIUM','2026-01-21 09:00:00','2026-02-25 16:00:00',70,130,24,6,4)
,
  (933,'Parametrer les reponses','Parametrer les reponses','IN_REVIEW','HIGHEST','2026-01-22 09:00:00','2026-02-27 16:00:00',70,130,24,7,4)
,
  (934,'Partager le sondage par lien','Partager le sondage par lien','DONE','MEDIUM','2026-01-23 09:00:00','2026-02-28 16:00:00',70,130,24,9,5)
,
  (935,'Soumettre reponse anonyme','Soumettre reponse anonyme','IN_REVIEW','LOW','2026-01-24 09:00:00','2026-02-27 16:00:00',70,130,24,7,4)
,
  (936,'Stockage securise reponses','Stockage securise reponses','IN_REVIEW','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',70,130,24,6,4)
,
  (937,'Limiter une reponse par IP','Limiter une reponse par IP','DONE','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',70,130,24,6,5)
,
  (938,'Statistiques temps reel','Statistiques temps reel','DONE','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',70,130,24,8,4)
,
  (939,'Graphiques reponses auto','Graphiques reponses auto','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',70,130,24,9,4)
,
  (940,'Export resultats CSV','Export resultats CSV','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',70,130,24,9,5)
,
  (941,'Comparaison entre sondages','Comparaison entre sondages','IN_REVIEW','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',70,130,24,8,1)
,
  (942,'Questions obligatoires flag','Questions obligatoires flag','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',70,130,24,6,1)
,
  (943,'Logique conditionnelle branchement','Logique conditionnelle branchement','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',70,130,24,7,5)
,
  (944,'Sondage prive ou public','Sondage prive ou public','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',70,130,24,7,5)
,
  (945,'Analyse sentimentale textes','Analyse sentimentale textes','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',70,130,24,9,4)
,
  (946,'Rapport PDF automatique','Rapport PDF automatique','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',70,130,24,7,1)
,
  (947,'Notification nouveau sondage','Notification nouveau sondage','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',70,130,24,9,1)
,
  (948,'Archivage ancien sondage','Archivage ancien sondage','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',70,130,24,7,4)
,
  (949,'Mode preview avant publication','Mode preview avant publication','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',70,130,24,9,1)
,
  (950,'Correction reponses ouvertes','Correction reponses ouvertes','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',70,130,24,7,4)
,
  (951,'Statistiques demographiques','Statistiques demographiques','IN_PROGRESS','HIGH','2026-02-03 09:00:00','2026-02-06 14:00:00',71,131,24,6,5)
,
  (952,'Taux de completion sondage','Taux de completion sondage','IN_PROGRESS','MEDIUM','2026-02-04 09:00:00','2026-02-09 14:00:00',71,131,24,8,1)
,
  (953,'Partage sur reseaux sociaux','Partage sur reseaux sociaux','IN_PROGRESS','HIGHEST','2026-02-05 09:00:00','2026-02-10 14:00:00',71,131,24,8,5)
,
  (954,'API pour integrer sondages','API pour integrer sondages','TODO','MEDIUM','2026-02-06 09:00:00','2026-02-11 14:00:00',71,131,24,8,5)
,
  (955,'Dashboard administrateur','Dashboard administrateur','TODO','LOW','2026-02-07 09:00:00','2026-02-09 14:00:00',71,131,24,9,5)
,
  (956,'Mode inviter par email','Mode inviter par email','IN_PROGRESS','MEDIUM','2026-02-08 09:00:00','2026-02-14 14:00:00',71,131,24,8,4)
,
  (957,'Gestion des doublons reponses','Gestion des doublons reponses','IN_PROGRESS','HIGH','2026-02-09 09:00:00','2026-02-15 14:00:00',71,131,24,8,5)
,
  (958,'Analyse croisee donnees','Analyse croisee donnees','IN_PROGRESS','HIGHEST','2026-02-10 09:00:00','2026-02-12 14:00:00',71,131,24,7,4)
,
  (959,'Mode sondage A B testing','Mode sondage A B testing','IN_PROGRESS','LOW','2026-02-11 09:00:00','2026-02-14 14:00:00',71,131,24,8,1)
,
  (960,'Rapport export PowerBI','Rapport export PowerBI','IN_PROGRESS','MEDIUM','2026-02-12 09:00:00','2026-02-17 14:00:00',71,131,24,7,5)
,
  (961,'Gestion des groupes cibles','Gestion des groupes cibles','TODO','HIGH','2026-02-13 09:00:00','2026-02-18 14:00:00',71,131,24,7,5)
,
  (962,'Mode delai de publication','Mode delai de publication','TODO','MEDIUM','2026-02-14 09:00:00','2026-02-20 14:00:00',71,131,24,7,4)
,
  (963,'Traduction automatique sondage','Traduction automatique sondage','IN_PROGRESS','HIGHEST','2026-02-15 09:00:00','2026-02-19 14:00:00',71,131,24,7,4)
,
  (964,'Mode accessibilite renforcee','Mode accessibilite renforcee','IN_PROGRESS','LOW','2026-02-16 09:00:00','2026-02-21 14:00:00',71,131,24,7,4)
,
  (965,'Rapport delai moyen reponse','Rapport delai moyen reponse','TODO','MEDIUM','2026-02-17 09:00:00','2026-02-19 14:00:00',71,131,24,6,4)
,
  (966,'Mode hors ligne collecte','Mode hors ligne collecte','TODO','HIGH','2026-02-18 09:00:00','2026-02-23 14:00:00',71,131,24,7,1)
,
  (967,'Gestion droits d acces','Gestion droits d acces','TODO','MEDIUM','2026-02-19 09:00:00','2026-02-24 14:00:00',71,131,24,9,4)
,
  (968,'Alerte seuil reponses atteint','Alerte seuil reponses atteint','TODO','LOW','2026-02-20 09:00:00','2026-02-23 14:00:00',71,131,24,9,4)
,
  (969,'Mode sondage ephemere','Mode sondage ephemere','IN_PROGRESS','HIGHEST','2026-02-21 09:00:00','2026-02-25 14:00:00',71,131,24,6,1)
,
  (970,'Rapport fiabilite reponses','Rapport fiabilite reponses','IN_PROGRESS','MEDIUM','2026-02-22 09:00:00','2026-02-28 14:00:00',71,131,24,8,1)
,
  (971,'Integration Google Forms','Integration Google Forms','TODO','HIGH','2026-02-17 09:00:00',NULL,72,132,24,7,4)
,
  (972,'Mode quiz avec score','Mode quiz avec score','NEW','MEDIUM','2026-02-18 09:00:00',NULL,72,132,24,9,5)
,
  (973,'Gestion des rappels envoi','Gestion des rappels envoi','NEW','HIGHEST','2026-02-19 09:00:00',NULL,72,132,24,6,5)
,
  (974,'Mode sondage conditionnel','Mode sondage conditionnel','TODO','MEDIUM','2026-02-20 09:00:00',NULL,72,132,24,8,5)
,
  (975,'Rapport evolution dans le temps','Rapport evolution dans le temps','NEW','LOW','2026-02-21 09:00:00',NULL,72,132,24,6,5)
,
  (976,'Mode collecte terrain','Mode collecte terrain','NEW','MEDIUM','2026-02-22 09:00:00',NULL,72,132,24,7,4)
,
  (977,'Gestion invitations groupees','Gestion invitations groupees','TODO','HIGH','2026-02-23 09:00:00',NULL,72,132,24,7,4)
,
  (978,'Mode reponse partielle sauvegarde','Mode reponse partielle sauvegarde','TODO','HIGHEST','2026-02-24 09:00:00',NULL,72,132,24,9,4)
,
  (979,'Rapport NPS et CSAT','Rapport NPS et CSAT','NEW','LOW','2026-02-25 09:00:00',NULL,72,132,24,6,4)
,
  (980,'Mode panel recurrent','Mode panel recurrent','TODO','MEDIUM','2026-02-26 09:00:00',NULL,72,132,24,7,1)
,
  (981,'Gestion des panels cibles','Gestion des panels cibles','NEW','HIGH','2026-02-27 09:00:00',NULL,72,132,24,8,5)
,
  (982,'Mode sondage longitudinal','Mode sondage longitudinal','NEW','MEDIUM','2026-02-28 09:00:00',NULL,72,132,24,9,1)
,
  (983,'Rapport satisfaction employes','Rapport satisfaction employes','NEW','HIGHEST','2026-02-28 09:00:00',NULL,72,132,24,7,1)
,
  (984,'Mode focus group virtuel','Mode focus group virtuel','TODO','LOW','2026-02-28 09:00:00',NULL,72,132,24,6,5)
,
  (985,'Gestion delais de publication','Gestion delais de publication','NEW','MEDIUM','2026-02-28 09:00:00',NULL,72,132,24,8,5)
,
  (986,'Statistiques taux reponse','Statistiques taux reponse','NEW','HIGH','2026-02-28 09:00:00',NULL,72,132,24,7,4)
,
  (987,'Mode A B testing avance','Mode A B testing avance','TODO','MEDIUM','2026-02-28 09:00:00',NULL,72,132,24,8,5)
,
  (988,'Rapport analyse qualitative','Rapport analyse qualitative','TODO','LOW','2026-02-28 09:00:00',NULL,72,132,24,6,1)
,
  (989,'Gestion multi-sondages','Gestion multi-sondages','NEW','HIGHEST','2026-02-28 09:00:00',NULL,72,132,24,6,4)
,
  (990,'Mode etude de marche','Mode etude de marche','TODO','MEDIUM','2026-02-28 09:00:00',NULL,72,132,24,7,1)
,
  (991,'Catalogue destinations voyage','Catalogue destinations voyage','DONE','HIGH','2026-01-20 09:00:00','2026-02-28 16:00:00',73,133,25,8,1)
,
  (992,'Details du voyage complet','Details du voyage complet','DONE','MEDIUM','2026-01-21 09:00:00','2026-02-28 16:00:00',73,133,25,9,1)
,
  (993,'Galerie photos lieux visites','Galerie photos lieux visites','DONE','HIGHEST','2026-01-22 09:00:00','2026-02-28 16:00:00',73,133,25,9,4)
,
  (994,'Recherche par date et budget','Recherche par date et budget','DONE','MEDIUM','2026-01-23 09:00:00','2026-02-28 16:00:00',73,133,25,6,4)
,
  (995,'Filtres type voyage','Filtres type voyage','DONE','LOW','2026-01-24 09:00:00','2026-02-28 16:00:00',73,133,25,8,4)
,
  (996,'Reserver un voyage complet','Reserver un voyage complet','DONE','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',73,133,25,6,5)
,
  (997,'Choix options supplementaires','Choix options supplementaires','IN_REVIEW','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',73,133,25,8,1)
,
  (998,'Paiement et confirmation','Paiement et confirmation','DONE','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',73,133,25,7,4)
,
  (999,'Itineraire detaille jour par jour','Itineraire detaille jour par jour','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',73,133,25,8,4)
,
  (1000,'Guide de voyage PDF','Guide de voyage PDF','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',73,133,25,7,5)
,
  (1001,'Avis des voyageurs precedents','Avis des voyageurs precedents','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',73,133,25,6,1)
,
  (1002,'Notes et evaluations','Notes et evaluations','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',73,133,25,6,1)
,
  (1003,'Classement meilleures destinations','Classement meilleures destinations','IN_REVIEW','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',73,133,25,9,1)
,
  (1004,'Voyages similaires suggeres','Voyages similaires suggeres','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',73,133,25,7,5)
,
  (1005,'Programme fidelite voyageur','Programme fidelite voyageur','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',73,133,25,8,1)
,
  (1006,'Gestion des bagages','Gestion des bagages','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',73,133,25,8,1)
,
  (1007,'Assurance voyage obligatoire','Assurance voyage obligatoire','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',73,133,25,6,4)
,
  (1008,'Visa et documents voyage','Visa et documents voyage','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',73,133,25,7,4)
,
  (1009,'Alerte promotion voyage','Alerte promotion voyage','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',73,133,25,6,1)
,
  (1010,'Suivi reservation en cours','Suivi reservation en cours','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',73,133,25,6,1)
,
  (1011,'Modification reservation','Modification reservation','IN_PROGRESS','HIGH','2026-02-03 09:00:00','2026-02-05 14:00:00',74,134,25,9,1)
,
  (1012,'Annulation et remboursement','Annulation et remboursement','IN_PROGRESS','MEDIUM','2026-02-04 09:00:00','2026-02-07 14:00:00',74,134,25,6,4)
,
  (1013,'Service client 24/7','Service client 24/7','TODO','HIGHEST','2026-02-05 09:00:00','2026-02-10 14:00:00',74,134,25,6,1)
,
  (1014,'Chatbot voyage assistant','Chatbot voyage assistant','IN_PROGRESS','MEDIUM','2026-02-06 09:00:00','2026-02-10 14:00:00',74,134,25,6,1)
,
  (1015,'Rapport ventes destinations','Rapport ventes destinations','IN_PROGRESS','LOW','2026-02-07 09:00:00','2026-02-12 14:00:00',74,134,25,8,1)
,
  (1016,'Gestion des guides locaux','Gestion des guides locaux','IN_PROGRESS','MEDIUM','2026-02-08 09:00:00','2026-02-14 14:00:00',74,134,25,7,1)
,
  (1017,'Excursions et activites optionnelles','Excursions et activites optionnelles','TODO','HIGH','2026-02-09 09:00:00','2026-02-12 14:00:00',74,134,25,6,5)
,
  (1018,'Transferts aeroport','Transferts aeroport','TODO','HIGHEST','2026-02-10 09:00:00','2026-02-13 14:00:00',74,134,25,8,5)
,
  (1019,'Mode degustation culinaire locale','Mode degustation culinaire locale','IN_PROGRESS','LOW','2026-02-11 09:00:00','2026-02-16 14:00:00',74,134,25,9,5)
,
  (1020,'Rapport avis clients voyage','Rapport avis clients voyage','IN_PROGRESS','MEDIUM','2026-02-12 09:00:00','2026-02-15 14:00:00',74,134,25,6,1)
,
  (1021,'Gestion assurances annulation','Gestion assurances annulation','IN_PROGRESS','HIGH','2026-02-13 09:00:00','2026-02-18 14:00:00',74,134,25,9,4)
,
  (1022,'Suivi etat sante voyage','Suivi etat sante voyage','IN_PROGRESS','MEDIUM','2026-02-14 09:00:00','2026-02-18 14:00:00',74,134,25,7,1)
,
  (1023,'Conseils securite destination','Conseils securite destination','IN_PROGRESS','HIGHEST','2026-02-15 09:00:00','2026-02-21 14:00:00',74,134,25,7,5)
,
  (1024,'Mode voyage responsable','Mode voyage responsable','TODO','LOW','2026-02-16 09:00:00','2026-02-18 14:00:00',74,134,25,8,5)
,
  (1025,'Gestion visas de groupe','Gestion visas de groupe','IN_PROGRESS','MEDIUM','2026-02-17 09:00:00','2026-02-23 14:00:00',74,134,25,7,5)
,
  (1026,'Rapport carbon offset voyage','Rapport carbon offset voyage','TODO','HIGH','2026-02-18 09:00:00','2026-02-23 14:00:00',74,134,25,7,5)
,
  (1027,'Mode voyage sur mesure','Mode voyage sur mesure','IN_PROGRESS','MEDIUM','2026-02-19 09:00:00','2026-02-24 14:00:00',74,134,25,6,1)
,
  (1028,'Concierge personnel voyage','Concierge personnel voyage','TODO','LOW','2026-02-20 09:00:00','2026-02-25 14:00:00',74,134,25,6,1)
,
  (1029,'Rapport popularite saisons','Rapport popularite saisons','TODO','HIGHEST','2026-02-21 09:00:00','2026-02-23 14:00:00',74,134,25,6,5)
,
  (1030,'Gestion des partenariats hotels','Gestion des partenariats hotels','IN_PROGRESS','MEDIUM','2026-02-22 09:00:00','2026-02-27 14:00:00',74,134,25,8,1)
,
  (1031,'Mode flash deal destinations','Mode flash deal destinations','NEW','HIGH','2026-02-17 09:00:00',NULL,75,135,25,9,4)
,
  (1032,'Historique voyages client','Historique voyages client','NEW','MEDIUM','2026-02-18 09:00:00',NULL,75,135,25,7,4)
,
  (1033,'Rapport fidelisation clients','Rapport fidelisation clients','TODO','HIGHEST','2026-02-19 09:00:00',NULL,75,135,25,6,5)
,
  (1034,'Mode waiting list voyages','Mode waiting list voyages','TODO','MEDIUM','2026-02-20 09:00:00',NULL,75,135,25,6,4)
,
  (1035,'Gestion des remboursements','Gestion des remboursements','TODO','LOW','2026-02-21 09:00:00',NULL,75,135,25,6,1)
,
  (1036,'Mode paiement etale','Mode paiement etale','NEW','MEDIUM','2026-02-22 09:00:00',NULL,75,135,25,8,5)
,
  (1037,'Rapport marge par destination','Rapport marge par destination','NEW','HIGH','2026-02-23 09:00:00',NULL,75,135,25,9,4)
,
  (1038,'Gestion guides traducteurs','Gestion guides traducteurs','NEW','HIGHEST','2026-02-24 09:00:00',NULL,75,135,25,6,5)
,
  (1039,'Mode experience immersive','Mode experience immersive','NEW','LOW','2026-02-25 09:00:00',NULL,75,135,25,7,4)
,
  (1040,'Rapport retours clients voyage','Rapport retours clients voyage','TODO','MEDIUM','2026-02-26 09:00:00',NULL,75,135,25,8,1)
,
  (1041,'Gestion des circuits touristiques','Gestion des circuits touristiques','NEW','HIGH','2026-02-27 09:00:00',NULL,75,135,25,7,1)
,
  (1042,'Mode road trip personnalise','Mode road trip personnalise','NEW','MEDIUM','2026-02-28 09:00:00',NULL,75,135,25,8,4)
,
  (1043,'Rapport occupation hebergements','Rapport occupation hebergements','NEW','HIGHEST','2026-02-28 09:00:00',NULL,75,135,25,6,4)
,
  (1044,'Mode voyage wellness','Mode voyage wellness','NEW','LOW','2026-02-28 09:00:00',NULL,75,135,25,7,1)
,
  (1045,'Gestion des sejours linguistiques','Gestion des sejours linguistiques','TODO','MEDIUM','2026-02-28 09:00:00',NULL,75,135,25,6,4)
,
  (1046,'Statistiques saisons fortes','Statistiques saisons fortes','TODO','HIGH','2026-02-28 09:00:00',NULL,75,135,25,9,5)
,
  (1047,'Mode travel companion','Mode travel companion','NEW','MEDIUM','2026-02-28 09:00:00',NULL,75,135,25,6,4)
,
  (1048,'Gestion des partenariats compagnies','Gestion des partenariats compagnies','TODO','LOW','2026-02-28 09:00:00',NULL,75,135,25,6,1)
,
  (1049,'Rapport impact economique','Rapport impact economique','TODO','HIGHEST','2026-02-28 09:00:00',NULL,75,135,25,6,1)
,
  (1050,'Mode voyage solidaire','Mode voyage solidaire','NEW','MEDIUM','2026-02-28 09:00:00',NULL,75,135,25,8,5)
,
  (1051,'Creer profil sportif','Creer profil sportif','IN_REVIEW','HIGH','2026-01-20 09:00:00','2026-02-25 16:00:00',76,136,26,8,1)
,
  (1052,'Definir objectifs fitness','Definir objectifs fitness','DONE','MEDIUM','2026-01-21 09:00:00','2026-02-24 16:00:00',76,136,26,8,5)
,
  (1053,'Suivi poids et mesures corporelles','Suivi poids et mesures corporelles','DONE','HIGHEST','2026-01-22 09:00:00','2026-02-27 16:00:00',76,136,26,8,4)
,
  (1054,'Historique entrainements passes','Historique entrainements passes','IN_REVIEW','MEDIUM','2026-01-23 09:00:00','2026-02-28 16:00:00',76,136,26,6,4)
,
  (1055,'Programme entrainement personnalise','Programme entrainement personnalise','IN_REVIEW','LOW','2026-01-24 09:00:00','2026-02-28 16:00:00',76,136,26,6,4)
,
  (1056,'Exercices avec videos guidees','Exercices avec videos guidees','IN_REVIEW','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',76,136,26,8,1)
,
  (1057,'Chronometre et series reps','Chronometre et series reps','IN_REVIEW','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',76,136,26,7,4)
,
  (1058,'Suivi calories brulees seance','Suivi calories brulees seance','DONE','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',76,136,26,6,5)
,
  (1059,'Journal alimentaire quotidien','Journal alimentaire quotidien','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',76,136,26,7,5)
,
  (1060,'Calcul apports nutritionnels','Calcul apports nutritionnels','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',76,136,26,6,1)
,
  (1061,'Objectifs calories journaliers','Objectifs calories journaliers','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',76,136,26,9,4)
,
  (1062,'Suivi hydratation quotidienne','Suivi hydratation quotidienne','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',76,136,26,7,1)
,
  (1063,'Statistiques progression force','Statistiques progression force','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',76,136,26,7,4)
,
  (1064,'Graphique performance cardio','Graphique performance cardio','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',76,136,26,8,4)
,
  (1065,'Defis et recompenses motivantes','Defis et recompenses motivantes','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',76,136,26,8,1)
,
  (1066,'Partage resultats avec coach','Partage resultats avec coach','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',76,136,26,7,1)
,
  (1067,'Mode hors ligne entrainement','Mode hors ligne entrainement','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',76,136,26,9,1)
,
  (1068,'Integration montre connectee','Integration montre connectee','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',76,136,26,8,4)
,
  (1069,'Rappel entrainement programme','Rappel entrainement programme','IN_REVIEW','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',76,136,26,9,1)
,
  (1070,'Social et comparaison amis','Social et comparaison amis','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',76,136,26,8,5)
,
  (1071,'Programme 30 jours defi','Programme 30 jours defi','TODO','HIGH','2026-02-03 09:00:00','2026-02-07 14:00:00',77,137,26,7,4)
,
  (1072,'Exercices personnalises blessure','Exercices personnalises blessure','TODO','MEDIUM','2026-02-04 09:00:00','2026-02-10 14:00:00',77,137,26,9,1)
,
  (1073,'Suivi blessures et recuperation','Suivi blessures et recuperation','IN_PROGRESS','HIGHEST','2026-02-05 09:00:00','2026-02-10 14:00:00',77,137,26,7,1)
,
  (1074,'Temps de repos recommande','Temps de repos recommande','IN_PROGRESS','MEDIUM','2026-02-06 09:00:00','2026-02-10 14:00:00',77,137,26,7,5)
,
  (1075,'Rapport mensuel fitness','Rapport mensuel fitness','IN_PROGRESS','LOW','2026-02-07 09:00:00','2026-02-12 14:00:00',77,137,26,8,1)
,
  (1076,'Mode coaching IA','Mode coaching IA','IN_PROGRESS','MEDIUM','2026-02-08 09:00:00','2026-02-14 14:00:00',77,137,26,6,1)
,
  (1077,'Calendrier objectifs sportifs','Calendrier objectifs sportifs','IN_PROGRESS','HIGH','2026-02-09 09:00:00','2026-02-11 14:00:00',77,137,26,9,4)
,
  (1078,'Integration Apple Health','Integration Apple Health','IN_PROGRESS','HIGHEST','2026-02-10 09:00:00','2026-02-12 14:00:00',77,137,26,8,1)
,
  (1079,'Mode course a pied GPS','Mode course a pied GPS','IN_PROGRESS','LOW','2026-02-11 09:00:00','2026-02-15 14:00:00',77,137,26,9,5)
,
  (1080,'Suivi velo et cyclisme','Suivi velo et cyclisme','TODO','MEDIUM','2026-02-12 09:00:00','2026-02-14 14:00:00',77,137,26,6,5)
,
  (1081,'Programme yoga et meditation','Programme yoga et meditation','TODO','HIGH','2026-02-13 09:00:00','2026-02-18 14:00:00',77,137,26,7,5)
,
  (1082,'Exercices assis bureau','Exercices assis bureau','IN_PROGRESS','MEDIUM','2026-02-14 09:00:00','2026-02-17 14:00:00',77,137,26,6,4)
,
  (1083,'Mode challenge entre amis','Mode challenge entre amis','IN_PROGRESS','HIGHEST','2026-02-15 09:00:00','2026-02-19 14:00:00',77,137,26,8,4)
,
  (1084,'Rapport composition corporelle','Rapport composition corporelle','TODO','LOW','2026-02-16 09:00:00','2026-02-21 14:00:00',77,137,26,8,4)
,
  (1085,'Statistiques records personnels','Statistiques records personnels','IN_PROGRESS','MEDIUM','2026-02-17 09:00:00','2026-02-20 14:00:00',77,137,26,7,1)
,
  (1086,'Mode preparation marathon','Mode preparation marathon','IN_PROGRESS','HIGH','2026-02-18 09:00:00','2026-02-20 14:00:00',77,137,26,6,1)
,
  (1087,'Suivi charge d entrainement','Suivi charge d entrainement','IN_PROGRESS','MEDIUM','2026-02-19 09:00:00','2026-02-23 14:00:00',77,137,26,9,4)
,
  (1088,'Mode competition et classement','Mode competition et classement','TODO','LOW','2026-02-20 09:00:00','2026-02-24 14:00:00',77,137,26,6,5)
,
  (1089,'Exercices etirements routines','Exercices etirements routines','IN_PROGRESS','HIGHEST','2026-02-21 09:00:00','2026-02-26 14:00:00',77,137,26,9,4)
,
  (1090,'Mode sommeil et recuperation','Mode sommeil et recuperation','IN_PROGRESS','MEDIUM','2026-02-22 09:00:00','2026-02-25 14:00:00',77,137,26,6,1)
,
  (1091,'Rapport coherence cardio','Rapport coherence cardio','TODO','HIGH','2026-02-17 09:00:00',NULL,78,138,26,6,1)
,
  (1092,'Statistiques distances parcourues','Statistiques distances parcourues','NEW','MEDIUM','2026-02-18 09:00:00',NULL,78,138,26,7,1)
,
  (1093,'Mode preparation competition','Mode preparation competition','TODO','HIGHEST','2026-02-19 09:00:00',NULL,78,138,26,8,4)
,
  (1094,'Suivi frequence cardiaque','Suivi frequence cardiaque','NEW','MEDIUM','2026-02-20 09:00:00',NULL,78,138,26,6,5)
,
  (1095,'Rapport intensite seances','Rapport intensite seances','TODO','LOW','2026-02-21 09:00:00',NULL,78,138,26,8,1)
,
  (1096,'Mode sport collectif','Mode sport collectif','TODO','MEDIUM','2026-02-22 09:00:00',NULL,78,138,26,9,4)
,
  (1097,'Calendrier competitions','Calendrier competitions','NEW','HIGH','2026-02-23 09:00:00',NULL,78,138,26,7,1)
,
  (1098,'Coach virtuel personnalise','Coach virtuel personnalise','NEW','HIGHEST','2026-02-24 09:00:00',NULL,78,138,26,6,1)
,
  (1099,'Rapport evolution annuelle','Rapport evolution annuelle','TODO','LOW','2026-02-25 09:00:00',NULL,78,138,26,6,1)
,
  (1100,'Mode wellbeing general','Mode wellbeing general','TODO','MEDIUM','2026-02-26 09:00:00',NULL,78,138,26,7,1)
,
  (1101,'Gestion des blessures chroniques','Gestion des blessures chroniques','NEW','HIGH','2026-02-27 09:00:00',NULL,78,138,26,8,5)
,
  (1102,'Mode rehabilitation','Mode rehabilitation','TODO','MEDIUM','2026-02-28 09:00:00',NULL,78,138,26,8,1)
,
  (1103,'Rapport performance hebdo','Rapport performance hebdo','TODO','HIGHEST','2026-02-28 09:00:00',NULL,78,138,26,6,1)
,
  (1104,'Mode entrainement en groupe','Mode entrainement en groupe','NEW','LOW','2026-02-28 09:00:00',NULL,78,138,26,7,1)
,
  (1105,'Gestion des goals et milestones','Gestion des goals et milestones','NEW','MEDIUM','2026-02-28 09:00:00',NULL,78,138,26,6,1)
,
  (1106,'Statistiques calories depensees','Statistiques calories depensees','NEW','HIGH','2026-02-28 09:00:00',NULL,78,138,26,6,1)
,
  (1107,'Mode preparation triathlon','Mode preparation triathlon','NEW','MEDIUM','2026-02-28 09:00:00',NULL,78,138,26,9,5)
,
  (1108,'Gestion des entra personnalises','Gestion des entra personnalises','NEW','LOW','2026-02-28 09:00:00',NULL,78,138,26,9,1)
,
  (1109,'Rapport taux de realisation','Rapport taux de realisation','NEW','HIGHEST','2026-02-28 09:00:00',NULL,78,138,26,9,1)
,
  (1110,'Mode wellness mental','Mode wellness mental','NEW','MEDIUM','2026-02-28 09:00:00',NULL,78,138,26,7,1)
,
  (1111,'Creer modele facture','Creer modele facture','DONE','HIGH','2026-01-20 09:00:00','2026-02-24 16:00:00',79,139,27,8,4)
,
  (1112,'Personnaliser champs facture','Personnaliser champs facture','DONE','MEDIUM','2026-01-21 09:00:00','2026-02-26 16:00:00',79,139,27,7,5)
,
  (1113,'Logo et couleurs entreprise','Logo et couleurs entreprise','IN_REVIEW','HIGHEST','2026-01-22 09:00:00','2026-02-28 16:00:00',79,139,27,9,5)
,
  (1114,'Generer facture depuis template','Generer facture depuis template','DONE','MEDIUM','2026-01-23 09:00:00','2026-02-28 16:00:00',79,139,27,6,4)
,
  (1115,'Calcul automatique TVA','Calcul automatique TVA','DONE','LOW','2026-01-24 09:00:00','2026-02-28 16:00:00',79,139,27,9,1)
,
  (1116,'Numerotation auto factures','Numerotation auto factures','IN_REVIEW','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',79,139,27,7,5)
,
  (1117,'Gerer articles et produits','Gerer articles et produits','DONE','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',79,139,27,9,5)
,
  (1118,'Appliquer remises et ristournes','Appliquer remises et ristournes','IN_REVIEW','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',79,139,27,9,4)
,
  (1119,'Envoyer facture par email','Envoyer facture par email','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',79,139,27,8,4)
,
  (1120,'Relance facture impayee','Relance facture impayee','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',79,139,27,6,4)
,
  (1121,'Suivi statut facture en cours','Suivi statut facture en cours','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',79,139,27,6,4)
,
  (1122,'Telecharger PDF facture','Telecharger PDF facture','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',79,139,27,8,5)
,
  (1123,'Historique factures emises','Historique factures emises','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',79,139,27,6,1)
,
  (1124,'Rapport chiffre affaires','Rapport chiffre affaires','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',79,139,27,8,5)
,
  (1125,'Facture recurrente automatique','Facture recurrente automatique','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',79,139,27,7,5)
,
  (1126,'Gestion des avoirs','Gestion des avoirs','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',79,139,27,7,1)
,
  (1127,'Support multi-devises facture','Support multi-devises facture','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',79,139,27,8,5)
,
  (1128,'Import depuis ERP','Import depuis ERP','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',79,139,27,6,5)
,
  (1129,'Modele de devis associe','Modele de devis associe','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',79,139,27,7,5)
,
  (1130,'Alerte echeance paiement','Alerte echeance paiement','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',79,139,27,8,1)
,
  (1131,'Statistiques paiements recus','Statistiques paiements recus','IN_PROGRESS','HIGH','2026-02-03 09:00:00','2026-02-06 14:00:00',80,140,27,6,1)
,
  (1132,'Export comptable FEC','Export comptable FEC','TODO','MEDIUM','2026-02-04 09:00:00','2026-02-10 14:00:00',80,140,27,7,1)
,
  (1133,'Modele conforme loi facturation','Modele conforme loi facturation','TODO','HIGHEST','2026-02-05 09:00:00','2026-02-11 14:00:00',80,140,27,6,4)
,
  (1134,'Signature electronique','Signature electronique','IN_PROGRESS','MEDIUM','2026-02-06 09:00:00','2026-02-09 14:00:00',80,140,27,7,4)
,
  (1135,'Facture grouppee mensuelle','Facture grouppee mensuelle','IN_PROGRESS','LOW','2026-02-07 09:00:00','2026-02-10 14:00:00',80,140,27,6,1)
,
  (1136,'Mode proforma et devis','Mode proforma et devis','TODO','MEDIUM','2026-02-08 09:00:00','2026-02-14 14:00:00',80,140,27,7,1)
,
  (1137,'Gestion avances et acomptes','Gestion avances et acomptes','IN_PROGRESS','HIGH','2026-02-09 09:00:00','2026-02-14 14:00:00',80,140,27,6,4)
,
  (1138,'Suivi tresorerie previsionnelle','Suivi tresorerie previsionnelle','TODO','HIGHEST','2026-02-10 09:00:00','2026-02-14 14:00:00',80,140,27,6,4)
,
  (1139,'Mode facturation par projet','Mode facturation par projet','IN_PROGRESS','LOW','2026-02-11 09:00:00','2026-02-16 14:00:00',80,140,27,7,1)
,
  (1140,'Rapport delais paiement clients','Rapport delais paiement clients','TODO','MEDIUM','2026-02-12 09:00:00','2026-02-18 14:00:00',80,140,27,8,1)
,
  (1141,'Gestion des penalites retard','Gestion des penalites retard','TODO','HIGH','2026-02-13 09:00:00','2026-02-19 14:00:00',80,140,27,8,1)
,
  (1142,'Mode relance progressive','Mode relance progressive','IN_PROGRESS','MEDIUM','2026-02-14 09:00:00','2026-02-18 14:00:00',80,140,27,9,4)
,
  (1143,'Rapport age comptes clients','Rapport age comptes clients','IN_PROGRESS','HIGHEST','2026-02-15 09:00:00','2026-02-17 14:00:00',80,140,27,7,5)
,
  (1144,'Mode telepaiement integre','Mode telepaiement integre','TODO','LOW','2026-02-16 09:00:00','2026-02-18 14:00:00',80,140,27,9,5)
,
  (1145,'Gestion des litiges facturation','Gestion des litiges facturation','IN_PROGRESS','MEDIUM','2026-02-17 09:00:00','2026-02-20 14:00:00',80,140,27,8,5)
,
  (1146,'Mode facture credit note','Mode facture credit note','TODO','HIGH','2026-02-18 09:00:00','2026-02-21 14:00:00',80,140,27,6,4)
,
  (1147,'Rapport cash flow mensuel','Rapport cash flow mensuel','IN_PROGRESS','MEDIUM','2026-02-19 09:00:00','2026-02-23 14:00:00',80,140,27,6,1)
,
  (1148,'Mode escompte paiement anticipe','Mode escompte paiement anticipe','IN_PROGRESS','LOW','2026-02-20 09:00:00','2026-02-22 14:00:00',80,140,27,7,5)
,
  (1149,'Gestion tiers payant','Gestion tiers payant','IN_PROGRESS','HIGHEST','2026-02-21 09:00:00','2026-02-25 14:00:00',80,140,27,9,1)
,
  (1150,'Mode facturation collaborative','Mode facturation collaborative','IN_PROGRESS','MEDIUM','2026-02-22 09:00:00','2026-02-24 14:00:00',80,140,27,7,5)
,
  (1151,'Rapport taux encaissement','Rapport taux encaissement','NEW','HIGH','2026-02-17 09:00:00',NULL,81,141,27,8,1)
,
  (1152,'Mode import factures fournisseur','Mode import factures fournisseur','TODO','MEDIUM','2026-02-18 09:00:00',NULL,81,141,27,6,4)
,
  (1153,'Gestion rapprochement bancaire','Gestion rapprochement bancaire','NEW','HIGHEST','2026-02-19 09:00:00',NULL,81,141,27,9,1)
,
  (1154,'Mode emission bons de commande','Mode emission bons de commande','NEW','MEDIUM','2026-02-20 09:00:00',NULL,81,141,27,9,4)
,
  (1155,'Rapport provisions comptables','Rapport provisions comptables','NEW','LOW','2026-02-21 09:00:00',NULL,81,141,27,9,1)
,
  (1156,'Mode facturation internationale','Mode facturation internationale','NEW','MEDIUM','2026-02-22 09:00:00',NULL,81,141,27,7,1)
,
  (1157,'Gestion TVA intracommunautaire','Gestion TVA intracommunautaire','NEW','HIGH','2026-02-23 09:00:00',NULL,81,141,27,6,1)
,
  (1158,'Mode archivage legal numerique','Mode archivage legal numerique','TODO','HIGHEST','2026-02-24 09:00:00',NULL,81,141,27,6,4)
,
  (1159,'Rapport audit facturation','Rapport audit facturation','TODO','LOW','2026-02-25 09:00:00',NULL,81,141,27,6,1)
,
  (1160,'Mode facturation electronique','Mode facturation electronique','NEW','MEDIUM','2026-02-26 09:00:00',NULL,81,141,27,7,4)
,
  (1161,'Gestion des avoirs recurrents','Gestion des avoirs recurrents','TODO','HIGH','2026-02-27 09:00:00',NULL,81,141,27,6,5)
,
  (1162,'Mode split facture','Mode split facture','TODO','MEDIUM','2026-02-28 09:00:00',NULL,81,141,27,8,5)
,
  (1163,'Rapport marges par client','Rapport marges par client','NEW','HIGHEST','2026-02-28 09:00:00',NULL,81,141,27,9,5)
,
  (1164,'Mode facturation projets longs','Mode facturation projets longs','TODO','LOW','2026-02-28 09:00:00',NULL,81,141,27,9,5)
,
  (1165,'Gestion des acomptes progressifs','Gestion des acomptes progressifs','TODO','MEDIUM','2026-02-28 09:00:00',NULL,81,141,27,9,1)
,
  (1166,'Statistiques delais de paiement','Statistiques delais de paiement','TODO','HIGH','2026-02-28 09:00:00',NULL,81,141,27,6,4)
,
  (1167,'Mode prorata temporis','Mode prorata temporis','TODO','MEDIUM','2026-02-28 09:00:00',NULL,81,141,27,7,4)
,
  (1168,'Gestion des credits de facture','Gestion des credits de facture','NEW','LOW','2026-02-28 09:00:00',NULL,81,141,27,8,5)
,
  (1169,'Rapport comptabilite analytique','Rapport comptabilite analytique','TODO','HIGHEST','2026-02-28 09:00:00',NULL,81,141,27,8,1)
,
  (1170,'Mode emission auto avoirs','Mode emission auto avoirs','TODO','MEDIUM','2026-02-28 09:00:00',NULL,81,141,27,6,4)
,
  (1171,'Vue calendrier mensuel','Vue calendrier mensuel','IN_REVIEW','HIGH','2026-01-20 09:00:00','2026-02-23 16:00:00',82,142,28,9,1)
,
  (1172,'Creation tache planifiee','Creation tache planifiee','DONE','MEDIUM','2026-01-21 09:00:00','2026-02-24 16:00:00',82,142,28,6,4)
,
  (1173,'Drag-and-drop replanifier','Drag-and-drop replanifier','DONE','HIGHEST','2026-01-22 09:00:00','2026-02-25 16:00:00',82,142,28,9,1)
,
  (1174,'Tache recurring journaliere','Tache recurring journaliere','DONE','MEDIUM','2026-01-23 09:00:00','2026-02-28 16:00:00',82,142,28,7,1)
,
  (1175,'Tache recurring hebdomadaire','Tache recurring hebdomadaire','DONE','LOW','2026-01-24 09:00:00','2026-02-27 16:00:00',82,142,28,6,4)
,
  (1176,'Tache recurring mensuelle','Tache recurring mensuelle','DONE','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',82,142,28,9,4)
,
  (1177,'Pattern personnalisable recurrence','Pattern personnalisable recurrence','DONE','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',82,142,28,6,5)
,
  (1178,'Notification avant echeance','Notification avant echeance','DONE','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',82,142,28,7,4)
,
  (1179,'Rappel par email automatique','Rappel par email automatique','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',82,142,28,7,4)
,
  (1180,'Rappel push navigateur','Rappel push navigateur','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',82,142,28,9,4)
,
  (1181,'Statut tache en attente','Statut tache en attente','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',82,142,28,8,5)
,
  (1182,'Vue agenda integree','Vue agenda integree','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',82,142,28,6,1)
,
  (1183,'Filtrer par priorite statut','Filtrer par priorite statut','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',82,142,28,9,5)
,
  (1184,'Vue jour semaine mois','Vue jour semaine mois','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',82,142,28,6,4)
,
  (1185,'Export calendrier ICS','Export calendrier ICS','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',82,142,28,9,5)
,
  (1186,'Tache avec sous-taches','Tache avec sous-taches','IN_REVIEW','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',82,142,28,8,1)
,
  (1187,'Dependances entre taches','Dependances entre taches','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',82,142,28,7,1)
,
  (1188,'Mode pomodoro integre','Mode pomodoro integre','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',82,142,28,7,1)
,
  (1189,'Statistiques de realisation','Statistiques de realisation','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',82,142,28,6,4)
,
  (1190,'Rapport productivite hebdo','Rapport productivite hebdo','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',82,142,28,7,5)
,
  (1191,'Pause automatique programmee','Pause automatique programmee','TODO','HIGH','2026-02-03 09:00:00','2026-02-05 14:00:00',83,143,28,8,4)
,
  (1192,'Mode Focus sans distraction','Mode Focus sans distraction','IN_PROGRESS','MEDIUM','2026-02-04 09:00:00','2026-02-09 14:00:00',83,143,28,9,5)
,
  (1193,'Integration Google Calendar','Integration Google Calendar','TODO','HIGHEST','2026-02-05 09:00:00','2026-02-11 14:00:00',83,143,28,8,4)
,
  (1194,'Synchronisation multiple calendriers','Synchronisation multiple calendriers','TODO','MEDIUM','2026-02-06 09:00:00','2026-02-12 14:00:00',83,143,28,6,5)
,
  (1195,'Rapport hebdomadaire auto email','Rapport hebdomadaire auto email','TODO','LOW','2026-02-07 09:00:00','2026-02-12 14:00:00',83,143,28,9,1)
,
  (1196,'Mode Kanban planning','Mode Kanban planning','IN_PROGRESS','MEDIUM','2026-02-08 09:00:00','2026-02-14 14:00:00',83,143,28,7,5)
,
  (1197,'Gestion des delais critiques','Gestion des delais critiques','TODO','HIGH','2026-02-09 09:00:00','2026-02-15 14:00:00',83,143,28,6,1)
,
  (1198,'Alerte chevauchement taches','Alerte chevauchement taches','IN_PROGRESS','HIGHEST','2026-02-10 09:00:00','2026-02-14 14:00:00',83,143,28,8,5)
,
  (1199,'Mode estimation duree tache','Mode estimation duree tache','TODO','LOW','2026-02-11 09:00:00','2026-02-16 14:00:00',83,143,28,8,4)
,
  (1200,'Statistiques temps passe','Statistiques temps passe','TODO','MEDIUM','2026-02-12 09:00:00','2026-02-15 14:00:00',83,143,28,9,1)
,
  (1201,'Rapport analyse Gantt','Rapport analyse Gantt','IN_PROGRESS','HIGH','2026-02-13 09:00:00','2026-02-19 14:00:00',83,143,28,9,1)
,
  (1202,'Mode vue timeline','Mode vue timeline','TODO','MEDIUM','2026-02-14 09:00:00','2026-02-19 14:00:00',83,143,28,9,1)
,
  (1203,'Gestion priorites urgent important','Gestion priorites urgent important','IN_PROGRESS','HIGHEST','2026-02-15 09:00:00','2026-02-20 14:00:00',83,143,28,7,1)
,
  (1204,'Mode delegation tache','Mode delegation tache','IN_PROGRESS','LOW','2026-02-16 09:00:00','2026-02-20 14:00:00',83,143,28,8,1)
,
  (1205,'Calendrier shared equipe','Calendrier shared equipe','IN_PROGRESS','MEDIUM','2026-02-17 09:00:00','2026-02-22 14:00:00',83,143,28,8,4)
,
  (1206,'Mode tache bloquante critique','Mode tache bloquante critique','IN_PROGRESS','HIGH','2026-02-18 09:00:00','2026-02-22 14:00:00',83,143,28,7,1)
,
  (1207,'Rapport retard taches','Rapport retard taches','TODO','MEDIUM','2026-02-19 09:00:00','2026-02-24 14:00:00',83,143,28,8,4)
,
  (1208,'Mode sprint planning','Mode sprint planning','TODO','LOW','2026-02-20 09:00:00','2026-02-25 14:00:00',83,143,28,6,1)
,
  (1209,'Gestion des points de blocage','Gestion des points de blocage','IN_PROGRESS','HIGHEST','2026-02-21 09:00:00','2026-02-26 14:00:00',83,143,28,9,5)
,
  (1210,'Statistiques terminees a temps','Statistiques terminees a temps','TODO','MEDIUM','2026-02-22 09:00:00','2026-02-28 14:00:00',83,143,28,8,1)
,
  (1211,'Mode vue workload equipe','Mode vue workload equipe','TODO','HIGH','2026-02-17 09:00:00',NULL,84,144,28,6,4)
,
  (1212,'Alerte surcharge membre','Alerte surcharge membre','NEW','MEDIUM','2026-02-18 09:00:00',NULL,84,144,28,9,1)
,
  (1213,'Rapport tendance mensuel','Rapport tendance mensuel','NEW','HIGHEST','2026-02-19 09:00:00',NULL,84,144,28,6,1)
,
  (1214,'Mode planning visuel colorise','Mode planning visuel colorise','TODO','MEDIUM','2026-02-20 09:00:00',NULL,84,144,28,7,1)
,
  (1215,'Gestion taches recurring exceptions','Gestion taches recurring exceptions','TODO','LOW','2026-02-21 09:00:00',NULL,84,144,28,6,4)
,
  (1216,'Mode vue projet global','Mode vue projet global','TODO','MEDIUM','2026-02-22 09:00:00',NULL,84,144,28,8,5)
,
  (1217,'Statistiques cycle time','Statistiques cycle time','TODO','HIGH','2026-02-23 09:00:00',NULL,84,144,28,8,4)
,
  (1218,'Mode retrospective sprint auto','Mode retrospective sprint auto','NEW','HIGHEST','2026-02-24 09:00:00',NULL,84,144,28,9,5)
,
  (1219,'Rapport burndown cumule','Rapport burndown cumule','TODO','LOW','2026-02-25 09:00:00',NULL,84,144,28,6,1)
,
  (1220,'Mode analyse causa retards','Mode analyse causa retards','NEW','MEDIUM','2026-02-26 09:00:00',NULL,84,144,28,9,5)
,
  (1221,'Gestion des dependances transverses','Gestion des dependances transverses','NEW','HIGH','2026-02-27 09:00:00',NULL,84,144,28,7,4)
,
  (1222,'Mode timeboxing','Mode timeboxing','NEW','MEDIUM','2026-02-28 09:00:00',NULL,84,144,28,8,4)
,
  (1223,'Rapport allocation ressources','Rapport allocation ressources','TODO','HIGHEST','2026-02-28 09:00:00',NULL,84,144,28,8,5)
,
  (1224,'Mode planning par equipe','Mode planning par equipe','TODO','LOW','2026-02-28 09:00:00',NULL,84,144,28,8,1)
,
  (1225,'Gestion des jalons projet','Gestion des jalons projet','NEW','MEDIUM','2026-02-28 09:00:00',NULL,84,144,28,6,1)
,
  (1226,'Statistiques lead time','Statistiques lead time','NEW','HIGH','2026-02-28 09:00:00',NULL,84,144,28,7,1)
,
  (1227,'Mode critique path','Mode critique path','TODO','MEDIUM','2026-02-28 09:00:00',NULL,84,144,28,7,4)
,
  (1228,'Gestion des risques projet','Gestion des risques projet','NEW','LOW','2026-02-28 09:00:00',NULL,84,144,28,6,5)
,
  (1229,'Rapport avancement global','Rapport avancement global','TODO','HIGHEST','2026-02-28 09:00:00',NULL,84,144,28,9,1)
,
  (1230,'Mode estimation Monte Carlo','Mode estimation Monte Carlo','TODO','MEDIUM','2026-02-28 09:00:00',NULL,84,144,28,6,1)
,
  (1231,'Creer une page wiki','Creer une page wiki','DONE','HIGH','2026-01-20 09:00:00','2026-02-28 16:00:00',85,145,29,8,5)
,
  (1232,'Editeur Markdown avance','Editeur Markdown avance','DONE','MEDIUM','2026-01-21 09:00:00','2026-02-26 16:00:00',85,145,29,9,4)
,
  (1233,'Sauvegarde automatique brouillon','Sauvegarde automatique brouillon','IN_REVIEW','HIGHEST','2026-01-22 09:00:00','2026-02-28 16:00:00',85,145,29,8,1)
,
  (1234,'Historique des versions','Historique des versions','DONE','MEDIUM','2026-01-23 09:00:00','2026-02-26 16:00:00',85,145,29,9,5)
,
  (1235,'Comparer deux versions code','Comparer deux versions code','DONE','LOW','2026-01-24 09:00:00','2026-02-28 16:00:00',85,145,29,8,5)
,
  (1236,'Restaurer version precedente','Restaurer version precedente','IN_REVIEW','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',85,145,29,8,4)
,
  (1237,'Edition en simultane multiple','Edition en simultane multiple','DONE','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',85,145,29,9,1)
,
  (1238,'Commentaires sur page wiki','Commentaires sur page wiki','DONE','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',85,145,29,8,4)
,
  (1239,'Notifications de modification','Notifications de modification','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',85,145,29,7,1)
,
  (1240,'Recherche dans le wiki','Recherche dans le wiki','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',85,145,29,6,5)
,
  (1241,'Arborescence categories pages','Arborescence categories pages','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',85,145,29,7,5)
,
  (1242,'Liens internes entre pages','Liens internes entre pages','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',85,145,29,8,4)
,
  (1243,'Menu de navigation auto-genere','Menu de navigation auto-genere','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',85,145,29,9,5)
,
  (1244,'Mise en forme avancee','Mise en forme avancee','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',85,145,29,7,4)
,
  (1245,'Embed images et videos','Embed images et videos','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',85,145,29,9,5)
,
  (1246,'Mode presentation slides','Mode presentation slides','IN_REVIEW','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',85,145,29,8,1)
,
  (1247,'Droits d acces par role','Droits d acces par role','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',85,145,29,7,4)
,
  (1248,'Pages populaires frequentees','Pages populaires frequentees','IN_REVIEW','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',85,145,29,7,5)
,
  (1249,'Pages recemment modifiees','Pages recemment modifiees','IN_REVIEW','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',85,145,29,7,5)
,
  (1250,'Statistiques de lecture pages','Statistiques de lecture pages','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',85,145,29,7,4)
,
  (1251,'Export PDF du wiki','Export PDF du wiki','IN_PROGRESS','HIGH','2026-02-03 09:00:00','2026-02-07 14:00:00',86,146,29,9,4)
,
  (1252,'Mode hors ligne consultation','Mode hors ligne consultation','IN_PROGRESS','MEDIUM','2026-02-04 09:00:00','2026-02-06 14:00:00',86,146,29,8,4)
,
  (1253,'Templates predefinis pages','Templates predefinis pages','TODO','HIGHEST','2026-02-05 09:00:00','2026-02-11 14:00:00',86,146,29,6,5)
,
  (1254,'Gestion des brouillons','Gestion des brouillons','TODO','MEDIUM','2026-02-06 09:00:00','2026-02-09 14:00:00',86,146,29,6,4)
,
  (1255,'Publication avec validation','Publication avec validation','TODO','LOW','2026-02-07 09:00:00','2026-02-12 14:00:00',86,146,29,6,5)
,
  (1256,'Mode suggestion edition','Mode suggestion edition','TODO','MEDIUM','2026-02-08 09:00:00','2026-02-10 14:00:00',86,146,29,8,4)
,
  (1257,'Alerte conflit d edition','Alerte conflit d edition','IN_PROGRESS','HIGH','2026-02-09 09:00:00','2026-02-13 14:00:00',86,146,29,7,1)
,
  (1258,'Table des matieres auto','Table des matieres auto','TODO','HIGHEST','2026-02-10 09:00:00','2026-02-14 14:00:00',86,146,29,9,1)
,
  (1259,'Mode notation pages utiles','Mode notation pages utiles','IN_PROGRESS','LOW','2026-02-11 09:00:00','2026-02-15 14:00:00',86,146,29,6,4)
,
  (1260,'Commentaires en marge','Commentaires en marge','IN_PROGRESS','MEDIUM','2026-02-12 09:00:00','2026-02-14 14:00:00',86,146,29,8,5)
,
  (1261,'Mode revision par paire','Mode revision par paire','IN_PROGRESS','HIGH','2026-02-13 09:00:00','2026-02-16 14:00:00',86,146,29,9,4)
,
  (1262,'Traduction automatique page','Traduction automatique page','IN_PROGRESS','MEDIUM','2026-02-14 09:00:00','2026-02-16 14:00:00',86,146,29,7,4)
,
  (1263,'Mode page protegee lecture','Mode page protegee lecture','IN_PROGRESS','HIGHEST','2026-02-15 09:00:00','2026-02-20 14:00:00',86,146,29,9,5)
,
  (1264,'Gestion des redirections','Gestion des redirections','TODO','LOW','2026-02-16 09:00:00','2026-02-21 14:00:00',86,146,29,9,4)
,
  (1265,'Mode page archivee','Mode page archivee','IN_PROGRESS','MEDIUM','2026-02-17 09:00:00','2026-02-20 14:00:00',86,146,29,6,5)
,
  (1266,'Statistiques contribution membres','Statistiques contribution membres','IN_PROGRESS','HIGH','2026-02-18 09:00:00','2026-02-24 14:00:00',86,146,29,6,4)
,
  (1267,'Mode discussion page','Mode discussion page','IN_PROGRESS','MEDIUM','2026-02-19 09:00:00','2026-02-21 14:00:00',86,146,29,6,5)
,
  (1268,'Mode glossaire collaboratif','Mode glossaire collaboratif','IN_PROGRESS','LOW','2026-02-20 09:00:00','2026-02-24 14:00:00',86,146,29,7,5)
,
  (1269,'FAQ automatique page','FAQ automatique page','IN_PROGRESS','HIGHEST','2026-02-21 09:00:00','2026-02-26 14:00:00',86,146,29,7,1)
,
  (1270,'Mode tutoriel pas a pas','Mode tutoriel pas a pas','TODO','MEDIUM','2026-02-22 09:00:00','2026-02-24 14:00:00',86,146,29,8,1)
,
  (1271,'Gestion des signets et favoris','Gestion des signets et favoris','TODO','HIGH','2026-02-17 09:00:00',NULL,87,147,29,8,4)
,
  (1272,'Mode publication externe','Mode publication externe','NEW','MEDIUM','2026-02-18 09:00:00',NULL,87,147,29,8,4)
,
  (1273,'Rapport activite wiki mensuel','Rapport activite wiki mensuel','TODO','HIGHEST','2026-02-19 09:00:00',NULL,87,147,29,8,4)
,
  (1274,'Mode page dynamique temps reel','Mode page dynamique temps reel','NEW','MEDIUM','2026-02-20 09:00:00',NULL,87,147,29,7,1)
,
  (1275,'Integration diagrammes Mermaid','Integration diagrammes Mermaid','NEW','LOW','2026-02-21 09:00:00',NULL,87,147,29,6,1)
,
  (1276,'Mode annotation collective','Mode annotation collective','TODO','MEDIUM','2026-02-22 09:00:00',NULL,87,147,29,7,1)
,
  (1277,'Gestion des references bibliographiques','Gestion des references bibliographiques','TODO','HIGH','2026-02-23 09:00:00',NULL,87,147,29,7,1)
,
  (1278,'Mode wiki pedagogique','Mode wiki pedagogique','NEW','HIGHEST','2026-02-24 09:00:00',NULL,87,147,29,6,4)
,
  (1279,'Rapport couverture connaissances','Rapport couverture connaissances','TODO','LOW','2026-02-25 09:00:00',NULL,87,147,29,6,5)
,
  (1280,'Mode modelexplication visuel','Mode modelexplication visuel','TODO','MEDIUM','2026-02-26 09:00:00',NULL,87,147,29,6,5)
,
  (1281,'Gestion des pages orphelines','Gestion des pages orphelines','TODO','HIGH','2026-02-27 09:00:00',NULL,87,147,29,6,5)
,
  (1282,'Mode modelexplication par exemple','Mode modelexplication par exemple','TODO','MEDIUM','2026-02-28 09:00:00',NULL,87,147,29,9,4)
,
  (1283,'Rapport pages non mises a jour','Rapport pages non mises a jour','NEW','HIGHEST','2026-02-28 09:00:00',NULL,87,147,29,6,5)
,
  (1284,'Mode wiki technique','Mode wiki technique','TODO','LOW','2026-02-28 09:00:00',NULL,87,147,29,9,5)
,
  (1285,'Gestion des categories wiki','Gestion des categories wiki','TODO','MEDIUM','2026-02-28 09:00:00',NULL,87,147,29,7,5)
,
  (1286,'Statistiques contributions par membre','Statistiques contributions par membre','TODO','HIGH','2026-02-28 09:00:00',NULL,87,147,29,8,4)
,
  (1287,'Mode pages liees automatiquement','Mode pages liees automatiquement','TODO','MEDIUM','2026-02-28 09:00:00',NULL,87,147,29,8,5)
,
  (1288,'Gestion des versions mineures','Gestion des versions mineures','NEW','LOW','2026-02-28 09:00:00',NULL,87,147,29,7,4)
,
  (1289,'Rapport evolution wiki trimestriel','Rapport evolution wiki trimestriel','TODO','HIGHEST','2026-02-28 09:00:00',NULL,87,147,29,6,4)
,
  (1290,'Mode modelexplication progressive','Mode modelexplication progressive','TODO','MEDIUM','2026-02-28 09:00:00',NULL,87,147,29,6,4)
,
  (1291,'Inventaire des actifs materiels','Inventaire des actifs materiels','DONE','HIGH','2026-01-20 09:00:00','2026-02-27 16:00:00',88,148,30,7,4)
,
  (1292,'Description detaillee actif','Description detaillee actif','DONE','MEDIUM','2026-01-21 09:00:00','2026-02-27 16:00:00',88,148,30,8,5)
,
  (1293,'Photos haute definition actif','Photos haute definition actif','DONE','HIGHEST','2026-01-22 09:00:00','2026-02-28 16:00:00',88,148,30,7,1)
,
  (1294,'Generation code QR unique','Generation code QR unique','IN_REVIEW','MEDIUM','2026-01-23 09:00:00','2026-02-28 16:00:00',88,148,30,7,5)
,
  (1295,'Scan QR identification rapide','Scan QR identification rapide','DONE','LOW','2026-01-24 09:00:00','2026-02-27 16:00:00',88,148,30,7,1)
,
  (1296,'Historique mouvements actif','Historique mouvements actif','DONE','MEDIUM','2026-01-25 09:00:00','2026-02-28 16:00:00',88,148,30,6,5)
,
  (1297,'Localisation actif dans batiment','Localisation actif dans batiment','IN_REVIEW','HIGH','2026-01-26 09:00:00','2026-02-28 16:00:00',88,148,30,7,1)
,
  (1298,'Plan interactif du site','Plan interactif du site','DONE','HIGHEST','2026-01-27 09:00:00','2026-02-28 16:00:00',88,148,30,6,4)
,
  (1299,'Maintenance preventive schedule','Maintenance preventive schedule','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',88,148,30,9,4)
,
  (1300,'Alerte maintenance due bientot','Alerte maintenance due bientot','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',88,148,30,6,1)
,
  (1301,'Historique des interventions','Historique des interventions','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',88,148,30,8,4)
,
  (1302,'Cout maintenance par actif','Cout maintenance par actif','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',88,148,30,9,5)
,
  (1303,'Gestion des pieces detachees','Gestion des pieces detachees','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',88,148,30,6,1)
,
  (1304,'Contrats de maintenance','Contrats de maintenance','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',88,148,30,8,4)
,
  (1305,'Depreciation et amortissement','Depreciation et amortissement','IN_REVIEW','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',88,148,30,8,5)
,
  (1306,'Rapport etat actifs global','Rapport etat actifs global','DONE','HIGH','2026-01-28 09:00:00','2026-02-28 16:00:00',88,148,30,8,1)
,
  (1307,'Actifs repartis par service','Actifs repartis par service','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',88,148,30,9,4)
,
  (1308,'Sortie et entree d actif','Sortie et entree d actif','DONE','LOW','2026-01-28 09:00:00','2026-02-28 16:00:00',88,148,30,9,4)
,
  (1309,'Inventaire annuel par scan QR','Inventaire annuel par scan QR','DONE','HIGHEST','2026-01-28 09:00:00','2026-02-28 16:00:00',88,148,30,6,1)
,
  (1310,'Declaration sinistre actif','Declaration sinistre actif','DONE','MEDIUM','2026-01-28 09:00:00','2026-02-28 16:00:00',88,148,30,7,1)
,
  (1311,'Assurance actifs materiels','Assurance actifs materiels','TODO','HIGH','2026-02-03 09:00:00','2026-02-08 14:00:00',89,149,30,9,5)
,
  (1312,'Export inventaire Excel','Export inventaire Excel','IN_PROGRESS','MEDIUM','2026-02-04 09:00:00','2026-02-10 14:00:00',89,149,30,9,4)
,
  (1313,'QR code personnalise entreprise','QR code personnalise entreprise','IN_PROGRESS','HIGHEST','2026-02-05 09:00:00','2026-02-10 14:00:00',89,149,30,7,4)
,
  (1314,'Maintenance corrective urgente','Maintenance corrective urgente','IN_PROGRESS','MEDIUM','2026-02-06 09:00:00','2026-02-08 14:00:00',89,149,30,9,4)
,
  (1315,'Rapport audit actifs','Rapport audit actifs','IN_PROGRESS','LOW','2026-02-07 09:00:00','2026-02-11 14:00:00',89,149,30,8,5)
,
  (1316,'Gestion des amortissements','Gestion des amortissements','IN_PROGRESS','MEDIUM','2026-02-08 09:00:00','2026-02-12 14:00:00',89,149,30,7,5)
,
  (1317,'Mode estimation valeur residuelle','Mode estimation valeur residuelle','TODO','HIGH','2026-02-09 09:00:00','2026-02-12 14:00:00',89,149,30,6,4)
,
  (1318,'Suivi energie actifs','Suivi energie actifs','TODO','HIGHEST','2026-02-10 09:00:00','2026-02-15 14:00:00',89,149,30,8,5)
,
  (1319,'Rapport conformite securite','Rapport conformite securite','IN_PROGRESS','LOW','2026-02-11 09:00:00','2026-02-14 14:00:00',89,149,30,6,1)
,
  (1320,'Gestion des mises au rebut','Gestion des mises au rebut','TODO','MEDIUM','2026-02-12 09:00:00','2026-02-15 14:00:00',89,149,30,9,4)
,
  (1321,'Mode planification investissements','Mode planification investissements','IN_PROGRESS','HIGH','2026-02-13 09:00:00','2026-02-16 14:00:00',89,149,30,9,4)
,
  (1322,'Statistiques vies actifs','Statistiques vies actifs','IN_PROGRESS','MEDIUM','2026-02-14 09:00:00','2026-02-20 14:00:00',89,149,30,8,1)
,
  (1323,'Rapport maintenance preventive','Rapport maintenance preventive','IN_PROGRESS','HIGHEST','2026-02-15 09:00:00','2026-02-18 14:00:00',89,149,30,9,4)
,
  (1324,'Mode gestion flotte vehicules','Mode gestion flotte vehicules','IN_PROGRESS','LOW','2026-02-16 09:00:00','2026-02-22 14:00:00',89,149,30,9,4)
,
  (1325,'Suivi consommation carburant','Suivi consommation carburant','TODO','MEDIUM','2026-02-17 09:00:00','2026-02-22 14:00:00',89,149,30,6,4)
,
  (1326,'Gestion des permis et visites','Gestion des permis et visites','IN_PROGRESS','HIGH','2026-02-18 09:00:00','2026-02-20 14:00:00',89,149,30,6,4)
,
  (1327,'Mode alerte fin de garantie','Mode alerte fin de garantie','IN_PROGRESS','MEDIUM','2026-02-19 09:00:00','2026-02-21 14:00:00',89,149,30,9,4)
,
  (1328,'Rapport disponibilite actifs','Rapport disponibilite actifs','TODO','LOW','2026-02-20 09:00:00','2026-02-26 14:00:00',89,149,30,6,1)
,
  (1329,'Gestion actifs critiques','Gestion actifs critiques','TODO','HIGHEST','2026-02-21 09:00:00','2026-02-27 14:00:00',89,149,30,8,5)
,
  (1330,'Mode maintenance conditionnelle','Mode maintenance conditionnelle','IN_PROGRESS','MEDIUM','2026-02-22 09:00:00','2026-02-26 14:00:00',89,149,30,9,4)
,
  (1331,'Statistiques MTBF MTTR','Statistiques MTBF MTTR','TODO','HIGH','2026-02-17 09:00:00',NULL,90,150,30,7,4)
,
  (1332,'Rapport Total Cost Ownership','Rapport Total Cost Ownership','NEW','MEDIUM','2026-02-18 09:00:00',NULL,90,150,30,9,5)
,
  (1333,'Mode gestion locataire actifs','Mode gestion locataire actifs','NEW','HIGHEST','2026-02-19 09:00:00',NULL,90,150,30,9,1)
,
  (1334,'Suivi performance actifs','Suivi performance actifs','TODO','MEDIUM','2026-02-20 09:00:00',NULL,90,150,30,7,1)
,
  (1335,'Mode alerte obsolescence','Mode alerte obsolescence','NEW','LOW','2026-02-21 09:00:00',NULL,90,150,30,8,5)
,
  (1336,'Rapport investissement annuel','Rapport investissement annuel','TODO','MEDIUM','2026-02-22 09:00:00',NULL,90,150,30,8,5)
,
  (1337,'Gestion actifs partages','Gestion actifs partages','TODO','HIGH','2026-02-23 09:00:00',NULL,90,150,30,6,1)
,
  (1338,'Mode verification periodique','Mode verification periodique','NEW','HIGHEST','2026-02-24 09:00:00',NULL,90,150,30,7,5)
,
  (1339,'Rapport taux utilisation','Rapport taux utilisation','NEW','LOW','2026-02-25 09:00:00',NULL,90,150,30,6,4)
,
  (1340,'Mode maintenance preditive IA','Mode maintenance preditive IA','NEW','MEDIUM','2026-02-26 09:00:00',NULL,90,150,30,7,1)
,
  (1341,'Gestion des actifs numeriques','Gestion des actifs numeriques','TODO','HIGH','2026-02-27 09:00:00',NULL,90,150,30,9,1)
,
  (1342,'Mode suivi GPS flotte','Mode suivi GPS flotte','NEW','MEDIUM','2026-02-28 09:00:00',NULL,90,150,30,7,5)
,
  (1343,'Rapport consommation energetique','Rapport consommation energetique','NEW','HIGHEST','2026-02-28 09:00:00',NULL,90,150,30,6,4)
,
  (1344,'Mode maintenance basee sur usage','Mode maintenance basee sur usage','TODO','LOW','2026-02-28 09:00:00',NULL,90,150,30,6,4)
,
  (1345,'Gestion des contrats assurence','Gestion des contrats assurence','NEW','MEDIUM','2026-02-28 09:00:00',NULL,90,150,30,7,4)
,
  (1346,'Statistiques cout total possession','Statistiques cout total possession','TODO','HIGH','2026-02-28 09:00:00',NULL,90,150,30,6,5)
,
  (1347,'Mode audit interne actifs','Mode audit interne actifs','NEW','MEDIUM','2026-02-28 09:00:00',NULL,90,150,30,9,5)
,
  (1348,'Gestion des transferts inter-services','Gestion des transferts inter-services','TODO','LOW','2026-02-28 09:00:00',NULL,90,150,30,7,5)
,
  (1349,'Rapport performance maintenance','Rapport performance maintenance','TODO','HIGHEST','2026-02-28 09:00:00',NULL,90,150,30,6,4)
,
  (1350,'Mode capitalisation actifs','Mode capitalisation actifs','NEW','MEDIUM','2026-02-28 09:00:00',NULL,90,150,30,8,5)
;

-- Total: 1200 taches generees
-- ====================================================================
-- 8. COMMENTAIRES (sur ~400 taches, IDs 1351-1850)
-- ====================================================================
INSERT INTO comment (id, content, created_at, task_id, author_id) VALUES
  
  (1351,'La pagination est implementee.','2026-02-19 11:50:00',176,10)
,
  (1352,'Migration de base de donnees requise.','2026-02-28 18:13:00',175,7)
,
  (1353,'Tests unitaires passes avec succes.','2026-02-22 10:54:00',175,8)
,
  (1354,'Accessibilité a verifier avec Lighthouse.','2026-02-03 16:43:00',175,6)
,
  (1355,'Le design system a ete suivi correctement.','2026-02-13 08:17:00',163,6)
,
  (1356,'Les tests de regression sont verts.','2026-02-14 11:23:00',163,1)
,
  (1357,'Le feature flag est en place.','2026-02-15 16:57:00',189,2)
,
  (1358,'Integration avec le frontend en cours.','2026-02-15 08:22:00',189,10)
,
  (1359,'Bloque par la tache precedente.','2026-02-23 10:47:00',189,4)
,
  (1360,'Swagger/OpenAPI a mettre a jour.','2026-02-17 18:11:00',162,3)
,
  (1361,'Besoin de plus de details avant de commencer.','2026-02-01 10:30:00',162,2)
,
  (1362,'Le timeout est trop court, a ajuster.','2026-02-14 18:05:00',165,2)
,
  (1363,'Tests unitaires passes avec succes.','2026-02-20 08:24:00',165,5)
,
  (1364,'Loggin ajoute pour le debug.','2026-02-07 08:40:00',166,7)
,
  (1365,'Corrections mineures a apporter.','2026-02-13 10:22:00',159,9)
,
  (1366,'Le design system a ete suivi correctement.','2026-02-05 17:49:00',199,1)
,
  (1367,'Les erreurs sont bien gerees cote client.','2026-02-20 14:07:00',199,3)
,
  (1368,'Faut integrer avec l API existante.','2026-02-05 12:11:00',199,2)
,
  (1369,'Le timeout est trop court, a ajuster.','2026-02-07 16:20:00',156,3)
,
  (1370,'La page charge en moins de 2 secondes.','2026-02-26 11:54:00',156,8)
,
  (1371,'Corrections mineures a apporter.','2026-02-28 13:11:00',156,2)
,
  (1372,'Le pipeline CI/CD est vert.','2026-02-01 17:53:00',167,5)
,
  (1373,'Code review en attente.','2026-02-07 14:25:00',167,6)
,
  (1374,'WebSocket connecte et fonctionnel.','2026-02-13 14:52:00',197,7)
,
  (1375,'J''ai commence le developpement, avancee lente.','2026-02-02 13:46:00',209,3)
,
  (1376,'La page charge en moins de 2 secondes.','2026-02-08 12:54:00',184,8)
,
  (1377,'La securite a ete renforcee.','2026-02-21 12:06:00',184,4)
,
  (1378,'Le formulaire est validé côté serveur.','2026-02-07 18:45:00',184,6)
,
  (1379,'A detester sur mobile aussi.','2026-02-25 09:56:00',194,8)
,
  (1380,'Le logout détruit bien la session.','2026-02-03 16:18:00',195,5)
,
  (1381,'Le formulaire est validé côté serveur.','2026-02-26 15:34:00',185,6)
,
  (1382,'Bloque par la tache precedente.','2026-02-14 12:26:00',185,5)
,
  (1383,'Le formulaire de validation est complet.','2026-02-06 12:24:00',185,10)
,
  (1384,'Swagger/OpenAPI a mettre a jour.','2026-02-22 15:12:00',201,10)
,
  (1385,'Sujet a discuter en daily standup.','2026-02-07 13:00:00',201,6)
,
  (1386,'L export PDF fonctionne correctement.','2026-02-28 11:57:00',190,5)
,
  (1387,'Le formulaire de validation est complet.','2026-02-26 18:16:00',190,3)
,
  (1388,'Les erreurs sont bien gerees cote client.','2026-02-14 13:42:00',182,4)
,
  (1389,'Dependance avec la lib externe a jour.','2026-02-04 16:34:00',182,1)
,
  (1390,'Swagger/OpenAPI a mettre a jour.','2026-02-07 08:06:00',223,5)
,
  (1391,'WebSocket connecte et fonctionnel.','2026-02-03 17:02:00',223,5)
,
  (1392,'Le timeout est trop court, a ajuster.','2026-02-12 16:40:00',211,5)
,
  (1393,'Le rate limiting est en place.','2026-02-19 12:12:00',211,10)
,
  (1394,'Le tracking des evenements est en place.','2026-02-28 14:25:00',211,2)
,
  (1395,'Corrections mineures a apporter.','2026-02-11 10:56:00',244,8)
,
  (1396,'Bloque par la tache precedente.','2026-02-28 14:26:00',244,8)
,
  (1397,'Le rate limiting est en place.','2026-02-12 12:13:00',230,3)
,
  (1398,'Fonctionnalite conforme aux specs.','2026-02-19 14:11:00',230,8)
,
  (1399,'L historique des modifications est sauvegardé.','2026-02-19 18:56:00',254,8)
,
  (1400,'Le logout détruit bien la session.','2026-02-22 10:04:00',254,8)
,
  (1401,'Merge conflict a resoudre avant le merge.','2026-02-01 09:21:00',258,9)
,
  (1402,'Le lazy loading est implemente.','2026-02-03 13:41:00',258,8)
,
  (1403,'Accessibilité a verifier avec Lighthouse.','2026-02-09 18:00:00',221,9)
,
  (1404,'Le responsive est bon sur tablette.','2026-02-14 12:10:00',221,6)
,
  (1405,'Le logout détruit bien la session.','2026-02-13 08:03:00',243,1)
,
  (1406,'Besoin d une estimation supplementaire.','2026-02-25 12:10:00',261,9)
,
  (1407,'Code review en attente.','2026-02-08 11:07:00',239,7)
,
  (1408,'API rate limiter a configurer.','2026-02-09 12:42:00',239,3)
,
  (1409,'Les tests de regression sont verts.','2026-02-25 16:11:00',224,4)
,
  (1410,'La securite a ete renforcee.','2026-02-03 18:07:00',227,9)
,
  (1411,'La pagination est implementee.','2026-02-27 10:09:00',227,3)
,
  (1412,'Besoin de plus de details avant de commencer.','2026-02-21 14:10:00',265,9)
,
  (1413,'Le client a valide cette feature.','2026-02-20 17:48:00',265,4)
,
  (1414,'Performance a ameliorer sur cette fonctionnalite.','2026-02-25 08:57:00',237,6)
,
  (1415,'Erreur 500 en production a investiguer.','2026-02-07 12:11:00',237,7)
,
  (1416,'Les erreurs sont bien gerees cote client.','2026-02-05 15:41:00',237,2)
,
  (1417,'La pagination est implementee.','2026-02-08 14:58:00',240,1)
,
  (1418,'Env de staging a deployer pour test.','2026-02-26 11:36:00',240,2)
,
  (1419,'Sous-tache creee pour le detail.','2026-02-28 09:56:00',240,10)
,
  (1420,'Le token est bien refresh automatiquement.','2026-02-25 15:23:00',246,3)
,
  (1421,'Corrections mineures a apporter.','2026-02-19 08:37:00',246,6)
,
  (1422,'Optimisation de la requete SQL necessaire.','2026-02-05 08:56:00',268,3)
,
  (1423,'Les tests de regression sont verts.','2026-02-10 09:31:00',251,5)
,
  (1424,'C''est pret pour le test en staging.','2026-02-12 10:38:00',250,1)
,
  (1425,'La notification push est opérationnelle.','2026-02-22 13:51:00',253,6)
,
  (1426,'L animation de transition est fluide.','2026-02-13 12:39:00',253,1)
,
  (1427,'Performance a ameliorer sur cette fonctionnalite.','2026-02-04 14:00:00',285,10)
,
  (1428,'Accessibilité a verifier avec Lighthouse.','2026-02-27 11:08:00',285,1)
,
  (1429,'Documentation a ecrire pour cette partie.','2026-02-02 12:11:00',306,5)
,
  (1430,'Le responsive est bon sur tablette.','2026-02-05 08:59:00',306,7)
,
  (1431,'La securite a ete renforcee.','2026-02-17 14:59:00',306,1)
,
  (1432,'Sous-tache creee pour le detail.','2026-02-06 17:14:00',280,2)
,
  (1433,'Erreur 500 en production a investiguer.','2026-02-05 15:58:00',280,7)
,
  (1434,'La tache a ete decomposee en sous-taches.','2026-02-12 16:10:00',280,2)
,
  (1435,'Accessibilité a verifier avec Lighthouse.','2026-02-07 09:27:00',315,8)
,
  (1436,'Les droits d accès sont vérifiés.','2026-02-05 15:47:00',314,3)
,
  (1437,'Les droits d accès sont vérifiés.','2026-02-06 18:16:00',314,4)
,
  (1438,'Loggin ajoute pour le debug.','2026-02-28 16:04:00',272,9)
,
  (1439,'Loggin ajoute pour le debug.','2026-02-22 14:25:00',319,4)
,
  (1440,'C''est pret pour le test en staging.','2026-02-09 13:49:00',289,4)
,
  (1441,'Accessibilité a verifier avec Lighthouse.','2026-02-16 11:06:00',289,6)
,
  (1442,'Il y a un bug a corriger sur cette partie.','2026-02-14 08:20:00',318,1)
,
  (1443,'Integration avec le frontend en cours.','2026-02-22 12:11:00',318,1)
,
  (1444,'L historique des modifications est sauvegardé.','2026-02-01 17:33:00',296,5)
,
  (1445,'La pagination est implementee.','2026-02-04 10:22:00',297,6)
,
  (1446,'L export PDF fonctionne correctement.','2026-02-17 12:12:00',303,5)
,
  (1447,'Il y a un bug a corriger sur cette partie.','2026-02-08 10:38:00',316,6)
,
  (1448,'Le formulaire est validé côté serveur.','2026-02-01 13:20:00',330,7)
,
  (1449,'PR soumise, en attente de revue.','2026-02-05 09:01:00',282,9)
,
  (1450,'Il y a un bug a corriger sur cette partie.','2026-02-07 11:21:00',282,3)
,
  (1451,'La tache a ete decomposee en sous-taches.','2026-02-25 09:41:00',282,5)
,
  (1452,'Le design est valide par le client.','2026-02-03 13:31:00',304,7)
,
  (1453,'Le token est bien refresh automatiquement.','2026-02-25 16:57:00',320,7)
,
  (1454,'Cache implemente pour ameliorer les perfs.','2026-02-26 09:17:00',274,6)
,
  (1455,'Loggin ajoute pour le debug.','2026-02-03 08:14:00',274,4)
,
  (1456,'La tache a ete decomposee en sous-taches.','2026-02-09 12:39:00',284,2)
,
  (1457,'Le scope a ete modifie par le product owner.','2026-02-22 15:33:00',286,8)
,
  (1458,'Il y a un bug a corriger sur cette partie.','2026-02-24 12:04:00',286,10)
,
  (1459,'C''est pret pour le test en staging.','2026-02-09 15:47:00',380,10)
,
  (1460,'API rate limiter a configurer.','2026-02-20 09:44:00',380,6)
,
  (1461,'Accessibilité a verifier avec Lighthouse.','2026-02-21 10:55:00',355,2)
,
  (1462,'Labels et priorite mis a jour.','2026-02-19 08:01:00',338,10)
,
  (1463,'Cache implemente pour ameliorer les perfs.','2026-02-07 18:40:00',378,4)
,
  (1464,'Tache bien avancee, presque terminee.','2026-02-27 12:11:00',332,7)
,
  (1465,'L animation de transition est fluide.','2026-02-27 12:11:00',332,3)
,
  (1466,'Le rate limiting est en place.','2026-02-04 12:53:00',332,7)
,
  (1467,'Le timeout est trop court, a ajuster.','2026-02-21 09:04:00',349,7)
,
  (1468,'Loggin ajoute pour le debug.','2026-02-09 10:48:00',362,5)
,
  (1469,'Le design est valide par le client.','2026-02-24 17:53:00',379,1)
,
  (1470,'Le design system a ete suivi correctement.','2026-02-05 12:38:00',382,7)
,
  (1471,'La notification push est opérationnelle.','2026-02-10 15:16:00',376,9)
,
  (1472,'Deadline serr mais faisable.','2026-02-15 12:09:00',359,3)
,
  (1473,'Integration avec le frontend en cours.','2026-02-17 10:51:00',359,6)
,
  (1474,'La tache a ete decomposee en sous-taches.','2026-02-20 16:04:00',358,6)
,
  (1475,'Besoin d une estimation supplementaire.','2026-02-14 10:36:00',358,9)
,
  (1476,'La pagination est implementee.','2026-02-19 09:00:00',351,7)
,
  (1477,'Labels et priorite mis a jour.','2026-02-04 09:00:00',363,8)
,
  (1478,'L historique des modifications est sauvegardé.','2026-02-25 11:19:00',346,2)
,
  (1479,'Tests unitaires passes avec succes.','2026-02-23 12:11:00',369,5)
,
  (1480,'Env de staging a deployer pour test.','2026-02-03 17:07:00',365,4)
,
  (1481,'Cache implemente pour ameliorer les perfs.','2026-02-27 11:19:00',365,1)
,
  (1482,'Le pipeline CI/CD est vert.','2026-02-15 18:16:00',337,1)
,
  (1483,'La tache a ete decomposee en sous-taches.','2026-02-07 13:08:00',385,6)
,
  (1484,'Cache implemente pour ameliorer les perfs.','2026-02-22 11:54:00',356,9)
,
  (1485,'Tests unitaires passes avec succes.','2026-02-14 08:04:00',404,3)
,
  (1486,'Le feature flag est en place.','2026-02-22 16:18:00',404,6)
,
  (1487,'Documentation a ecrire pour cette partie.','2026-02-11 09:46:00',442,9)
,
  (1488,'Les tests de regression sont verts.','2026-02-02 18:58:00',442,5)
,
  (1489,'Integration avec le frontend en cours.','2026-02-02 10:40:00',449,3)
,
  (1490,'Dependance avec la lib externe a jour.','2026-02-28 16:34:00',431,7)
,
  (1491,'Le responsive est bon sur tablette.','2026-02-09 09:58:00',424,7)
,
  (1492,'La page charge en moins de 2 secondes.','2026-02-05 09:16:00',429,6)
,
  (1493,'Le logout détruit bien la session.','2026-02-12 14:20:00',418,1)
,
  (1494,'Le responsive est bon sur tablette.','2026-02-19 09:38:00',418,4)
,
  (1495,'A detester sur mobile aussi.','2026-02-14 10:08:00',436,5)
,
  (1496,'Le formulaire de validation est complet.','2026-02-10 18:50:00',436,3)
,
  (1497,'Il y a un bug a corriger sur cette partie.','2026-02-17 16:16:00',421,9)
,
  (1498,'Tests unitaires passes avec succes.','2026-02-14 16:35:00',421,4)
,
  (1499,'Optimisation de la requete SQL necessaire.','2026-02-24 14:05:00',401,7)
,
  (1500,'Le client a valide cette feature.','2026-02-16 11:30:00',445,1)
,
  (1501,'La securite a ete renforcee.','2026-02-16 12:15:00',445,2)
,
  (1502,'Besoin de plus de details avant de commencer.','2026-02-15 09:59:00',413,3)
,
  (1503,'Le client a valide cette feature.','2026-02-05 18:59:00',413,9)
,
  (1504,'La page charge en moins de 2 secondes.','2026-02-02 09:00:00',426,9)
,
  (1505,'L animation de transition est fluide.','2026-02-24 08:28:00',426,4)
,
  (1506,'Integration avec le frontend en cours.','2026-02-25 15:31:00',426,9)
,
  (1507,'Tache bien avancee, presque terminee.','2026-02-24 08:38:00',402,4)
,
  (1508,'Besoin d une estimation supplementaire.','2026-02-21 14:11:00',402,3)
,
  (1509,'Le formulaire est validé côté serveur.','2026-02-15 12:53:00',446,9)
,
  (1510,'Swagger/OpenAPI a mettre a jour.','2026-02-25 08:19:00',446,1)
,
  (1511,'Code review en attente.','2026-02-22 11:36:00',446,1)
,
  (1512,'Migration de base de donnees requise.','2026-02-25 10:18:00',425,8)
,
  (1513,'L export PDF fonctionne correctement.','2026-02-23 08:50:00',410,3)
,
  (1514,'Erreur 500 en production a investiguer.','2026-02-27 13:34:00',410,10)
,
  (1515,'Le formulaire de validation est complet.','2026-02-22 12:11:00',392,7)
,
  (1516,'Le pipeline CI/CD est vert.','2026-02-16 09:24:00',392,5)
,
  (1517,'La pagination est implementee.','2026-02-20 12:30:00',433,5)
,
  (1518,'Tache bien avancee, presque terminee.','2026-02-06 08:44:00',433,9)
,
  (1519,'Documentation a ecrire pour cette partie.','2026-02-06 16:55:00',440,1)
,
  (1520,'Documentation a ecrire pour cette partie.','2026-02-19 10:44:00',465,6)
,
  (1521,'La tache a ete decomposee en sous-taches.','2026-02-17 14:35:00',465,1)
,
  (1522,'Tests unitaires passes avec succes.','2026-02-07 16:07:00',470,8)
,
  (1523,'Le lazy loading est implemente.','2026-02-11 14:34:00',470,7)
,
  (1524,'Le scope a ete modifie par le product owner.','2026-02-03 11:40:00',467,2)
,
  (1525,'Le lazy loading est implemente.','2026-02-06 11:45:00',471,7)
,
  (1526,'Le timeout est trop court, a ajuster.','2026-02-11 09:11:00',471,4)
,
  (1527,'Code review en attente.','2026-02-21 15:24:00',476,2)
,
  (1528,'Corrections mineures a apporter.','2026-02-11 16:00:00',473,8)
,
  (1529,'La securite a ete renforcee.','2026-02-21 14:38:00',506,9)
,
  (1530,'Le formulaire est validé côté serveur.','2026-02-11 12:47:00',506,7)
,
  (1531,'Performance a ameliorer sur cette fonctionnalite.','2026-02-05 15:36:00',506,10)
,
  (1532,'L animation de transition est fluide.','2026-02-16 15:29:00',461,3)
,
  (1533,'Env de staging a deployer pour test.','2026-02-16 17:38:00',508,9)
,
  (1534,'Sous-tache creee pour le detail.','2026-02-15 09:30:00',474,10)
,
  (1535,'Le logout détruit bien la session.','2026-02-07 09:22:00',503,8)
,
  (1536,'Sujet a discuter en daily standup.','2026-02-11 08:46:00',451,7)
,
  (1537,'Le token est bien refresh automatiquement.','2026-02-18 14:34:00',460,2)
,
  (1538,'Le formulaire est validé côté serveur.','2026-02-04 10:37:00',486,1)
,
  (1539,'Les erreurs sont bien gerees cote client.','2026-02-05 11:49:00',486,4)
,
  (1540,'La page charge en moins de 2 secondes.','2026-02-01 13:45:00',486,5)
,
  (1541,'Besoin de plus de details avant de commencer.','2026-02-02 11:14:00',480,4)
,
  (1542,'Swagger/OpenAPI a mettre a jour.','2026-02-07 16:48:00',480,4)
,
  (1543,'Tache bien avancee, presque terminee.','2026-02-15 12:30:00',480,3)
,
  (1544,'Migration de base de donnees requise.','2026-02-11 11:18:00',496,6)
,
  (1545,'Sous-tache creee pour le detail.','2026-02-06 17:02:00',468,2)
,
  (1546,'L historique des modifications est sauvegardé.','2026-02-26 14:58:00',481,4)
,
  (1547,'WebSocket connecte et fonctionnel.','2026-02-25 13:21:00',481,5)
,
  (1548,'Env de staging a deployer pour test.','2026-02-11 18:19:00',491,8)
,
  (1549,'Le design system a ete suivi correctement.','2026-02-01 15:01:00',507,8)
,
  (1550,'Corrections mineures a apporter.','2026-02-20 09:21:00',507,9)
,
  (1551,'Besoin d une estimation supplementaire.','2026-02-27 16:05:00',517,5)
,
  (1552,'J''ai commence le developpement, avancee lente.','2026-02-03 09:43:00',517,8)
,
  (1553,'Le client a valide cette feature.','2026-02-28 13:14:00',512,7)
,
  (1554,'Code review en attente.','2026-02-18 12:51:00',538,3)
,
  (1555,'Les tests de regression sont verts.','2026-02-06 18:00:00',538,7)
,
  (1556,'Le formulaire est validé côté serveur.','2026-02-11 11:24:00',538,6)
,
  (1557,'Les erreurs sont bien gerees cote client.','2026-02-06 17:50:00',556,3)
,
  (1558,'Il y a un bug a corriger sur cette partie.','2026-02-05 08:19:00',534,1)
,
  (1559,'Le client a valide cette feature.','2026-02-05 08:08:00',534,8)
,
  (1560,'A detester sur mobile aussi.','2026-02-19 12:15:00',534,6)
,
  (1561,'PR soumise, en attente de revue.','2026-02-08 12:27:00',555,1)
,
  (1562,'Le dark mode est disponible.','2026-02-03 16:11:00',567,10)
,
  (1563,'Tache bien avancee, presque terminee.','2026-02-22 11:20:00',567,10)
,
  (1564,'API rate limiter a configurer.','2026-02-12 10:39:00',567,7)
,
  (1565,'Le formulaire est validé côté serveur.','2026-02-07 14:52:00',511,4)
,
  (1566,'Le pipeline CI/CD est vert.','2026-02-09 14:55:00',511,8)
,
  (1567,'Sous-tache creee pour le detail.','2026-02-25 12:33:00',522,4)
,
  (1568,'Besoin de plus de details avant de commencer.','2026-02-28 13:29:00',522,4)
,
  (1569,'Le lazy loading est implemente.','2026-02-04 16:45:00',522,4)
,
  (1570,'Documentation a ecrire pour cette partie.','2026-02-07 12:55:00',566,1)
,
  (1571,'La notification push est opérationnelle.','2026-02-01 09:05:00',566,7)
,
  (1572,'PR soumise, en attente de revue.','2026-02-04 09:04:00',533,2)
,
  (1573,'La pagination est implementee.','2026-02-26 13:49:00',561,5)
,
  (1574,'Sous-tache creee pour le detail.','2026-02-20 08:27:00',548,7)
,
  (1575,'Integration avec le frontend en cours.','2026-02-15 13:59:00',532,9)
,
  (1576,'Documentation a ecrire pour cette partie.','2026-02-22 12:38:00',554,5)
,
  (1577,'Le scope a ete modifie par le product owner.','2026-02-02 16:35:00',528,4)
,
  (1578,'La securite a ete renforcee.','2026-02-20 15:07:00',531,4)
,
  (1579,'Les tests de regression sont verts.','2026-02-24 11:07:00',527,7)
,
  (1580,'Le design system a ete suivi correctement.','2026-02-26 10:12:00',535,2)
,
  (1581,'Deadline serr mais faisable.','2026-02-27 09:37:00',521,7)
,
  (1582,'Loggin ajoute pour le debug.','2026-02-05 16:49:00',629,8)
,
  (1583,'Les erreurs sont bien gerees cote client.','2026-02-18 13:59:00',629,8)
,
  (1584,'Le tracking des evenements est en place.','2026-02-25 11:50:00',629,8)
,
  (1585,'Le formulaire est validé côté serveur.','2026-02-05 09:55:00',625,2)
,
  (1586,'Les droits d accès sont vérifiés.','2026-02-06 17:15:00',625,2)
,
  (1587,'Loggin ajoute pour le debug.','2026-02-17 17:26:00',587,3)
,
  (1588,'Le tracking des evenements est en place.','2026-02-26 08:18:00',587,5)
,
  (1589,'Sujet a discuter en daily standup.','2026-02-22 13:56:00',587,3)
,
  (1590,'Code review en attente.','2026-02-20 11:48:00',576,2)
,
  (1591,'Le pipeline CI/CD est vert.','2026-02-12 10:09:00',576,5)
,
  (1592,'Deadline serr mais faisable.','2026-02-12 17:54:00',576,8)
,
  (1593,'Performance a ameliorer sur cette fonctionnalite.','2026-02-12 18:15:00',621,6)
,
  (1594,'Fonctionnalite conforme aux specs.','2026-02-08 17:32:00',586,5)
,
  (1595,'La pagination est implementee.','2026-02-23 17:08:00',584,8)
,
  (1596,'Le design system a ete suivi correctement.','2026-02-21 12:29:00',584,5)
,
  (1597,'Les droits d accès sont vérifiés.','2026-02-07 17:19:00',604,3)
,
  (1598,'Le tracking des evenements est en place.','2026-02-17 15:17:00',604,1)
,
  (1599,'Il y a un bug a corriger sur cette partie.','2026-02-01 14:48:00',604,6)
,
  (1600,'Le feature flag est en place.','2026-02-18 09:08:00',620,5)
,
  (1601,'Le pipeline CI/CD est vert.','2026-02-20 10:34:00',605,10)
,
  (1602,'Tests unitaires passes avec succes.','2026-02-26 16:06:00',572,1)
,
  (1603,'Swagger/OpenAPI a mettre a jour.','2026-02-10 18:32:00',614,7)
,
  (1604,'Dependance avec la lib externe a jour.','2026-02-17 15:00:00',610,10)
,
  (1605,'La tache a ete decomposee en sous-taches.','2026-02-15 17:55:00',610,2)
,
  (1606,'Le responsive est bon sur tablette.','2026-02-12 11:22:00',610,10)
,
  (1607,'L export PDF fonctionne correctement.','2026-02-21 09:50:00',599,1)
,
  (1608,'PR soumise, en attente de revue.','2026-02-19 11:51:00',619,5)
,
  (1609,'Le design system a ete suivi correctement.','2026-02-10 11:24:00',609,1)
,
  (1610,'Merge conflict a resoudre avant le merge.','2026-02-28 08:11:00',600,5)
,
  (1611,'Le scope a ete modifie par le product owner.','2026-02-03 09:24:00',600,10)
,
  (1612,'Labels et priorite mis a jour.','2026-02-05 11:36:00',618,10)
,
  (1613,'Optimisation de la requete SQL necessaire.','2026-02-03 09:53:00',588,2)
,
  (1614,'API rate limiter a configurer.','2026-02-08 16:59:00',607,3)
,
  (1615,'Tests unitaires passes avec succes.','2026-02-15 15:16:00',635,7)
,
  (1616,'Le token est bien refresh automatiquement.','2026-02-23 14:22:00',635,6)
,
  (1617,'Le pipeline CI/CD est vert.','2026-02-07 15:33:00',690,1)
,
  (1618,'Il y a un bug a corriger sur cette partie.','2026-02-22 14:34:00',656,8)
,
  (1619,'Code review en attente.','2026-02-03 15:37:00',656,4)
,
  (1620,'Le timeout est trop court, a ajuster.','2026-02-01 11:14:00',646,8)
,
  (1621,'C''est pret pour le test en staging.','2026-02-18 18:24:00',646,4)
,
  (1622,'Performance a ameliorer sur cette fonctionnalite.','2026-02-21 11:20:00',646,3)
,
  (1623,'L animation de transition est fluide.','2026-02-23 09:37:00',663,6)
,
  (1624,'Deadline serr mais faisable.','2026-02-02 11:26:00',649,8)
,
  (1625,'Les tests de regression sont verts.','2026-02-26 10:00:00',649,7)
,
  (1626,'Le feature flag est en place.','2026-02-07 12:23:00',661,6)
,
  (1627,'Le design est valide par le client.','2026-02-16 12:29:00',661,4)
,
  (1628,'Migration de base de donnees requise.','2026-02-26 14:59:00',661,10)
,
  (1629,'Les erreurs sont bien gerees cote client.','2026-02-01 09:52:00',643,3)
,
  (1630,'Le responsive est bon sur tablette.','2026-02-02 16:58:00',643,9)
,
  (1631,'Cache implemente pour ameliorer les perfs.','2026-02-27 12:29:00',666,9)
,
  (1632,'Besoin de plus de details avant de commencer.','2026-02-16 10:10:00',666,1)
,
  (1633,'Le formulaire est validé côté serveur.','2026-02-21 14:21:00',666,5)
,
  (1634,'Le formulaire de validation est complet.','2026-02-19 15:36:00',681,5)
,
  (1635,'La pagination est implementee.','2026-02-02 11:38:00',677,7)
,
  (1636,'Le formulaire est validé côté serveur.','2026-02-16 16:09:00',677,2)
,
  (1637,'Le rate limiting est en place.','2026-02-11 09:30:00',652,8)
,
  (1638,'Sous-tache creee pour le detail.','2026-02-24 17:09:00',668,10)
,
  (1639,'Code review en attente.','2026-02-05 14:14:00',668,9)
,
  (1640,'Corrections mineures a apporter.','2026-02-21 10:01:00',658,3)
,
  (1641,'Corrections mineures a apporter.','2026-02-12 15:51:00',658,5)
,
  (1642,'Sous-tache creee pour le detail.','2026-02-28 12:06:00',678,10)
,
  (1643,'Les droits d accès sont vérifiés.','2026-02-22 13:14:00',669,1)
,
  (1644,'La securite a ete renforcee.','2026-02-11 08:00:00',669,3)
,
  (1645,'Bloque par la tache precedente.','2026-02-17 08:21:00',651,7)
,
  (1646,'Loggin ajoute pour le debug.','2026-02-26 14:46:00',651,7)
,
  (1647,'Migration de base de donnees requise.','2026-02-25 09:22:00',641,4)
,
  (1648,'Accessibilité a verifier avec Lighthouse.','2026-02-20 10:48:00',641,3)
,
  (1649,'Faut integrer avec l API existante.','2026-02-18 13:25:00',641,1)
,
  (1650,'L export PDF fonctionne correctement.','2026-02-09 10:46:00',659,5)
,
  (1651,'Le formulaire de validation est complet.','2026-02-10 09:34:00',639,1)
,
  (1652,'Labels et priorite mis a jour.','2026-02-07 17:48:00',696,2)
,
  (1653,'API rate limiter a configurer.','2026-02-12 18:45:00',696,3)
,
  (1654,'Le feature flag est en place.','2026-02-09 15:55:00',696,10)
,
  (1655,'Swagger/OpenAPI a mettre a jour.','2026-02-06 17:02:00',730,1)
,
  (1656,'C''est pret pour le test en staging.','2026-02-12 16:28:00',730,4)
,
  (1657,'Le token est bien refresh automatiquement.','2026-02-21 14:45:00',746,10)
,
  (1658,'Sous-tache creee pour le detail.','2026-02-07 15:38:00',746,3)
,
  (1659,'Le dark mode est disponible.','2026-02-28 09:00:00',718,8)
,
  (1660,'Code review en attente.','2026-02-13 12:56:00',724,6)
,
  (1661,'PR soumise, en attente de revue.','2026-02-10 09:57:00',724,5)
,
  (1662,'Bloque par la tache precedente.','2026-02-02 09:20:00',724,10)
,
  (1663,'Accessibilité a verifier avec Lighthouse.','2026-02-19 11:11:00',697,6)
,
  (1664,'Besoin d une estimation supplementaire.','2026-02-06 11:03:00',697,7)
,
  (1665,'La pagination est implementee.','2026-02-11 08:27:00',710,1)
,
  (1666,'Sous-tache creee pour le detail.','2026-02-23 13:12:00',710,5)
,
  (1667,'La page charge en moins de 2 secondes.','2026-02-08 17:17:00',723,7)
,
  (1668,'Les droits d accès sont vérifiés.','2026-02-27 09:21:00',723,4)
,
  (1669,'Tache bien avancee, presque terminee.','2026-02-03 17:43:00',745,2)
,
  (1670,'Swagger/OpenAPI a mettre a jour.','2026-02-27 18:25:00',745,1)
,
  (1671,'Documentation a ecrire pour cette partie.','2026-02-18 17:05:00',745,4)
,
  (1672,'Le rate limiting est en place.','2026-02-12 15:56:00',720,4)
,
  (1673,'Le responsive est bon sur tablette.','2026-02-05 12:25:00',720,7)
,
  (1674,'Optimisation de la requete SQL necessaire.','2026-02-15 12:22:00',705,7)
,
  (1675,'Le lazy loading est implemente.','2026-02-20 16:14:00',700,10)
,
  (1676,'L export PDF fonctionne correctement.','2026-02-05 13:13:00',738,9)
,
  (1677,'Le responsive est bon sur tablette.','2026-02-14 18:16:00',738,9)
,
  (1678,'A detester sur mobile aussi.','2026-02-28 12:22:00',699,5)
,
  (1679,'Documentation a ecrire pour cette partie.','2026-02-04 12:03:00',714,5)
,
  (1680,'Labels et priorite mis a jour.','2026-02-07 15:55:00',734,10)
,
  (1681,'Le design est valide par le client.','2026-02-06 14:06:00',742,7)
,
  (1682,'Merge conflict a resoudre avant le merge.','2026-02-15 11:03:00',742,10)
,
  (1683,'API rate limiter a configurer.','2026-02-18 18:53:00',711,10)
,
  (1684,'Besoin de plus de details avant de commencer.','2026-02-02 13:31:00',743,2)
,
  (1685,'Loggin ajoute pour le debug.','2026-02-19 12:15:00',743,4)
,
  (1686,'L export PDF fonctionne correctement.','2026-02-19 14:07:00',707,6)
,
  (1687,'Faut integrer avec l API existante.','2026-02-15 10:56:00',707,2)
,
  (1688,'C''est pret pour le test en staging.','2026-02-17 13:40:00',807,4)
,
  (1689,'Le logout détruit bien la session.','2026-02-11 12:24:00',764,4)
,
  (1690,'Le feature flag est en place.','2026-02-19 14:41:00',786,2)
,
  (1691,'La tache a ete decomposee en sous-taches.','2026-02-16 16:56:00',763,1)
,
  (1692,'Le client a valide cette feature.','2026-02-06 14:29:00',793,3)
,
  (1693,'Dependance avec la lib externe a jour.','2026-02-16 15:24:00',791,3)
,
  (1694,'La tache a ete decomposee en sous-taches.','2026-02-07 17:30:00',751,7)
,
  (1695,'La notification push est opérationnelle.','2026-02-19 18:09:00',760,2)
,
  (1696,'Le logout détruit bien la session.','2026-02-06 15:34:00',808,5)
,
  (1697,'Tache bien avancee, presque terminee.','2026-02-10 08:00:00',785,10)
,
  (1698,'Le responsive est bon sur tablette.','2026-02-02 14:24:00',783,1)
,
  (1699,'Le dark mode est disponible.','2026-02-20 13:42:00',783,5)
,
  (1700,'Sujet a discuter en daily standup.','2026-02-18 18:09:00',783,2)
,
  (1701,'Le design est valide par le client.','2026-02-15 11:53:00',809,7)
,
  (1702,'Code review en attente.','2026-02-01 12:02:00',796,8)
,
  (1703,'Code review en attente.','2026-02-06 08:25:00',773,7)
,
  (1704,'Accessibilité a verifier avec Lighthouse.','2026-02-11 11:44:00',773,2)
,
  (1705,'Les droits d accès sont vérifiés.','2026-02-21 13:42:00',773,4)
,
  (1706,'Fonctionnalite conforme aux specs.','2026-02-04 16:16:00',803,10)
,
  (1707,'Le rate limiting est en place.','2026-02-02 13:42:00',803,1)
,
  (1708,'Deadline serr mais faisable.','2026-02-23 08:28:00',769,10)
,
  (1709,'Env de staging a deployer pour test.','2026-02-06 15:06:00',762,9)
,
  (1710,'Dependance avec la lib externe a jour.','2026-02-26 10:47:00',752,5)
,
  (1711,'La page charge en moins de 2 secondes.','2026-02-13 11:47:00',774,8)
,
  (1712,'Cache implemente pour ameliorer les perfs.','2026-02-06 12:15:00',772,1)
,
  (1713,'A detester sur mobile aussi.','2026-02-25 13:04:00',847,3)
,
  (1714,'L export PDF fonctionne correctement.','2026-02-23 12:12:00',855,7)
,
  (1715,'Integration avec le frontend en cours.','2026-02-16 16:00:00',835,5)
,
  (1716,'Bloque par la tache precedente.','2026-02-05 14:19:00',835,9)
,
  (1717,'Il y a un bug a corriger sur cette partie.','2026-02-18 18:58:00',861,7)
,
  (1718,'Documentation a ecrire pour cette partie.','2026-02-13 14:24:00',861,9)
,
  (1719,'Bloque par la tache precedente.','2026-02-14 16:26:00',814,5)
,
  (1720,'Env de staging a deployer pour test.','2026-02-08 13:25:00',866,2)
,
  (1721,'L animation de transition est fluide.','2026-02-04 12:14:00',866,1)
,
  (1722,'Env de staging a deployer pour test.','2026-02-20 14:38:00',867,4)
,
  (1723,'La tache a ete decomposee en sous-taches.','2026-02-17 14:01:00',867,8)
,
  (1724,'Loggin ajoute pour le debug.','2026-02-07 08:07:00',849,5)
,
  (1725,'La securite a ete renforcee.','2026-02-02 16:20:00',849,3)
,
  (1726,'Le logout détruit bien la session.','2026-02-09 14:12:00',846,4)
,
  (1727,'Code review en attente.','2026-02-14 09:45:00',869,3)
,
  (1728,'API rate limiter a configurer.','2026-02-08 18:27:00',818,6)
,
  (1729,'Migration de base de donnees requise.','2026-02-14 11:44:00',818,9)
,
  (1730,'Tests unitaires passes avec succes.','2026-02-26 08:44:00',818,3)
,
  (1731,'Env de staging a deployer pour test.','2026-02-04 17:15:00',844,5)
,
  (1732,'L historique des modifications est sauvegardé.','2026-02-18 11:25:00',850,1)
,
  (1733,'PR soumise, en attente de revue.','2026-02-15 11:33:00',850,2)
,
  (1734,'Le design est valide par le client.','2026-02-23 16:52:00',831,4)
,
  (1735,'L historique des modifications est sauvegardé.','2026-02-28 14:13:00',851,2)
,
  (1736,'Le client a valide cette feature.','2026-02-04 09:54:00',863,8)
,
  (1737,'Performance a ameliorer sur cette fonctionnalite.','2026-02-11 08:47:00',863,6)
,
  (1738,'Erreur 500 en production a investiguer.','2026-02-24 18:26:00',848,4)
,
  (1739,'Sous-tache creee pour le detail.','2026-02-13 09:17:00',811,7)
,
  (1740,'L animation de transition est fluide.','2026-02-03 10:04:00',811,9)
,
  (1741,'C''est pret pour le test en staging.','2026-02-05 13:44:00',811,10)
,
  (1742,'Le tracking des evenements est en place.','2026-02-28 08:21:00',813,6)
,
  (1743,'Loggin ajoute pour le debug.','2026-02-08 08:02:00',820,4)
,
  (1744,'Le scope a ete modifie par le product owner.','2026-02-26 15:55:00',888,8)
,
  (1745,'La pagination est implementee.','2026-02-27 17:57:00',888,10)
,
  (1746,'Il y a un bug a corriger sur cette partie.','2026-02-27 14:01:00',901,7)
,
  (1747,'Optimisation de la requete SQL necessaire.','2026-02-23 08:48:00',901,6)
,
  (1748,'Le scope a ete modifie par le product owner.','2026-02-14 13:00:00',909,5)
,
  (1749,'Optimisation de la requete SQL necessaire.','2026-02-20 14:25:00',927,6)
,
  (1750,'Integration avec le frontend en cours.','2026-02-21 15:04:00',913,7)
,
  (1751,'Env de staging a deployer pour test.','2026-02-27 10:34:00',897,8)
,
  (1752,'Migration de base de donnees requise.','2026-02-06 08:09:00',881,8)
,
  (1753,'Besoin de plus de details avant de commencer.','2026-02-11 11:25:00',881,10)
,
  (1754,'Le design est valide par le client.','2026-02-03 13:16:00',881,5)
,
  (1755,'L animation de transition est fluide.','2026-02-05 12:39:00',898,5)
,
  (1756,'Le design est valide par le client.','2026-02-19 17:53:00',912,8)
,
  (1757,'Le scope a ete modifie par le product owner.','2026-02-18 08:55:00',929,7)
,
  (1758,'Bloque par la tache precedente.','2026-02-28 13:31:00',929,1)
,
  (1759,'PR soumise, en attente de revue.','2026-02-16 08:33:00',907,1)
,
  (1760,'Env de staging a deployer pour test.','2026-02-21 16:05:00',883,5)
,
  (1761,'Le token est bien refresh automatiquement.','2026-02-11 16:10:00',890,6)
,
  (1762,'La notification push est opérationnelle.','2026-02-10 10:50:00',915,4)
,
  (1763,'La pagination est implementee.','2026-02-04 12:28:00',915,10)
,
  (1764,'L animation de transition est fluide.','2026-02-24 08:12:00',915,10)
,
  (1765,'Le lazy loading est implemente.','2026-02-26 16:09:00',922,1)
,
  (1766,'J''ai commence le developpement, avancee lente.','2026-02-27 08:42:00',922,6)
,
  (1767,'Le timeout est trop court, a ajuster.','2026-02-18 09:21:00',926,1)
,
  (1768,'Il y a un bug a corriger sur cette partie.','2026-02-03 12:06:00',916,8)
,
  (1769,'Le dark mode est disponible.','2026-02-15 09:52:00',916,1)
,
  (1770,'PR soumise, en attente de revue.','2026-02-27 13:12:00',889,4)
,
  (1771,'Tests unitaires passes avec succes.','2026-02-01 12:33:00',889,9)
,
  (1772,'Documentation a ecrire pour cette partie.','2026-02-28 17:30:00',875,8)
,
  (1773,'Le logout détruit bien la session.','2026-02-21 18:07:00',874,1)
,
  (1774,'Accessibilité a verifier avec Lighthouse.','2026-02-18 11:57:00',874,6)
,
  (1775,'La securite a ete renforcee.','2026-02-20 18:56:00',979,10)
,
  (1776,'Tache bien avancee, presque terminee.','2026-02-08 08:41:00',979,3)
,
  (1777,'Env de staging a deployer pour test.','2026-02-08 16:59:00',990,6)
,
  (1778,'Les tests de regression sont verts.','2026-02-21 11:25:00',943,2)
,
  (1779,'Le formulaire est validé côté serveur.','2026-02-10 16:51:00',943,8)
,
  (1780,'API rate limiter a configurer.','2026-02-14 15:34:00',953,5)
,
  (1781,'La notification push est opérationnelle.','2026-02-19 11:56:00',957,5)
,
  (1782,'Corrections mineures a apporter.','2026-02-24 14:03:00',958,10)
,
  (1783,'API rate limiter a configurer.','2026-02-13 15:00:00',958,5)
,
  (1784,'Le tracking des evenements est en place.','2026-02-16 11:26:00',958,3)
,
  (1785,'Env de staging a deployer pour test.','2026-02-17 12:23:00',976,8)
,
  (1786,'Le token est bien refresh automatiquement.','2026-02-14 12:55:00',976,7)
,
  (1787,'Le client a valide cette feature.','2026-02-07 10:52:00',978,3)
,
  (1788,'WebSocket connecte et fonctionnel.','2026-02-13 16:29:00',978,9)
,
  (1789,'La tache a ete decomposee en sous-taches.','2026-02-23 10:28:00',978,9)
,
  (1790,'WebSocket connecte et fonctionnel.','2026-02-18 15:18:00',988,3)
,
  (1791,'Faut integrer avec l API existante.','2026-02-02 14:38:00',988,10)
,
  (1792,'Le logout détruit bien la session.','2026-02-21 16:45:00',988,7)
,
  (1793,'Accessibilité a verifier avec Lighthouse.','2026-02-04 17:11:00',938,8)
,
  (1794,'Le responsive est bon sur tablette.','2026-02-25 10:20:00',931,9)
,
  (1795,'L historique des modifications est sauvegardé.','2026-02-08 08:41:00',963,6)
,
  (1796,'Le scope a ete modifie par le product owner.','2026-02-14 11:36:00',963,3)
,
  (1797,'Le formulaire est validé côté serveur.','2026-02-11 15:36:00',963,6)
,
  (1798,'Documentation a ecrire pour cette partie.','2026-02-04 09:26:00',954,10)
,
  (1799,'Le lazy loading est implemente.','2026-02-11 12:08:00',954,7)
,
  (1800,'Tache bien avancee, presque terminee.','2026-02-11 11:03:00',954,7)
,
  (1801,'Integration avec le frontend en cours.','2026-02-10 10:33:00',936,1)
,
  (1802,'Migration de base de donnees requise.','2026-02-04 15:51:00',952,3)
,
  (1803,'Documentation a ecrire pour cette partie.','2026-02-13 14:52:00',942,2)
,
  (1804,'Env de staging a deployer pour test.','2026-02-16 09:09:00',964,2)
,
  (1805,'Le tracking des evenements est en place.','2026-02-17 12:45:00',983,5)
,
  (1806,'Le feature flag est en place.','2026-02-14 14:23:00',965,10)
,
  (1807,'Labels et priorite mis a jour.','2026-02-04 10:29:00',965,4)
,
  (1808,'Le tracking des evenements est en place.','2026-02-19 09:14:00',965,9)
,
  (1809,'Besoin d une estimation supplementaire.','2026-02-10 15:41:00',973,6)
,
  (1810,'La pagination est implementee.','2026-02-10 11:56:00',973,3)
,
  (1811,'Le formulaire est validé côté serveur.','2026-02-26 12:58:00',1039,9)
,
  (1812,'Les tests de regression sont verts.','2026-02-16 09:50:00',1040,4)
,
  (1813,'Tests unitaires passes avec succes.','2026-02-04 16:38:00',1040,3)
,
  (1814,'Corrections mineures a apporter.','2026-02-24 17:20:00',1024,5)
,
  (1815,'Les tests de regression sont verts.','2026-02-11 15:09:00',1021,5)
,
  (1816,'La securite a ete renforcee.','2026-02-19 10:55:00',1020,3)
,
  (1817,'Le lazy loading est implemente.','2026-02-19 14:48:00',1020,5)
,
  (1818,'Le token est bien refresh automatiquement.','2026-02-21 16:50:00',1011,8)
,
  (1819,'Faut integrer avec l API existante.','2026-02-08 15:18:00',1011,3)
,
  (1820,'La pagination est implementee.','2026-02-18 12:35:00',1011,9)
,
  (1821,'La tache a ete decomposee en sous-taches.','2026-02-02 15:44:00',993,7)
,
  (1822,'Integration avec le frontend en cours.','2026-02-18 11:03:00',993,1)
,
  (1823,'L animation de transition est fluide.','2026-02-27 08:52:00',993,1)
,
  (1824,'La notification push est opérationnelle.','2026-02-14 17:28:00',1008,4)
,
  (1825,'C''est pret pour le test en staging.','2026-02-03 09:04:00',1006,2)
,
  (1826,'Sous-tache creee pour le detail.','2026-02-27 17:34:00',1006,10)
,
  (1827,'Le token est bien refresh automatiquement.','2026-02-25 12:53:00',998,8)
,
  (1828,'Sujet a discuter en daily standup.','2026-02-09 18:39:00',1016,9)
,
  (1829,'Le timeout est trop court, a ajuster.','2026-02-10 08:03:00',1016,8)
,
  (1830,'Code review en attente.','2026-02-10 18:26:00',1016,9)
,
  (1831,'L historique des modifications est sauvegardé.','2026-02-23 15:40:00',1050,2)
,
  (1832,'Le responsive est bon sur tablette.','2026-02-20 10:13:00',1050,9)
,
  (1833,'Le feature flag est en place.','2026-02-03 11:44:00',997,10)
,
  (1834,'Le timeout est trop court, a ajuster.','2026-02-04 17:36:00',997,9)
,
  (1835,'Le client a valide cette feature.','2026-02-16 09:48:00',1031,6)
,
  (1836,'Le tracking des evenements est en place.','2026-02-05 09:52:00',1034,1)
,
  (1837,'Migration de base de donnees requise.','2026-02-26 17:37:00',1034,2)
,
  (1838,'Migration de base de donnees requise.','2026-02-28 12:22:00',1001,9)
,
  (1839,'Le formulaire de validation est complet.','2026-02-07 15:42:00',1015,10)
,
  (1840,'La page charge en moins de 2 secondes.','2026-02-13 12:11:00',1015,1)
,
  (1841,'C''est pret pour le test en staging.','2026-02-27 18:11:00',1015,3)
,
  (1842,'Il y a un bug a corriger sur cette partie.','2026-02-02 18:34:00',1038,7)
,
  (1843,'J''ai commence le developpement, avancee lente.','2026-02-13 15:44:00',1038,5)
,
  (1844,'Le lazy loading est implemente.','2026-02-22 11:41:00',1038,2)
,
  (1845,'Le dark mode est disponible.','2026-02-23 09:44:00',1004,2)
,
  (1846,'Loggin ajoute pour le debug.','2026-02-20 13:12:00',1004,7)
,
  (1847,'Integration avec le frontend en cours.','2026-02-17 13:43:00',1046,2)
,
  (1848,'WebSocket connecte et fonctionnel.','2026-02-02 13:19:00',1074,2)
,
  (1849,'Besoin d une estimation supplementaire.','2026-02-02 11:07:00',1074,5)
,
  (1850,'La securite a ete renforcee.','2026-02-09 09:27:00',1074,8)
,
  (1851,'Faut integrer avec l API existante.','2026-02-19 17:35:00',1106,3)
,
  (1852,'L historique des modifications est sauvegardé.','2026-02-03 09:23:00',1096,1)
,
  (1853,'Accessibilité a verifier avec Lighthouse.','2026-02-13 18:02:00',1103,1)
,
  (1854,'La tache a ete decomposee en sous-taches.','2026-02-06 12:14:00',1103,7)
,
  (1855,'La page charge en moins de 2 secondes.','2026-02-12 18:40:00',1107,1)
,
  (1856,'C''est pret pour le test en staging.','2026-02-16 14:15:00',1070,8)
,
  (1857,'Cache implemente pour ameliorer les perfs.','2026-02-26 18:57:00',1070,4)
,
  (1858,'C''est pret pour le test en staging.','2026-02-23 18:43:00',1108,8)
,
  (1859,'Besoin de plus de details avant de commencer.','2026-02-27 10:39:00',1062,5)
,
  (1860,'Accessibilité a verifier avec Lighthouse.','2026-02-03 12:33:00',1101,4)
,
  (1861,'Dependance avec la lib externe a jour.','2026-02-28 10:40:00',1101,10)
,
  (1862,'Migration de base de donnees requise.','2026-02-28 11:18:00',1101,9)
,
  (1863,'Code review en attente.','2026-02-15 16:43:00',1078,4)
,
  (1864,'Le lazy loading est implemente.','2026-02-01 09:47:00',1061,1)
,
  (1865,'La page charge en moins de 2 secondes.','2026-02-06 12:42:00',1061,10)
,
  (1866,'Le tracking des evenements est en place.','2026-02-03 12:39:00',1087,10)
,
  (1867,'Il y a un bug a corriger sur cette partie.','2026-02-21 16:37:00',1090,3)
,
  (1868,'La page charge en moins de 2 secondes.','2026-02-05 15:12:00',1059,10)
,
  (1869,'Sous-tache creee pour le detail.','2026-02-10 08:37:00',1059,4)
,
  (1870,'Le dark mode est disponible.','2026-02-28 12:42:00',1104,1)
,
  (1871,'La notification push est opérationnelle.','2026-02-25 16:20:00',1102,7)
,
  (1872,'L export PDF fonctionne correctement.','2026-02-13 09:28:00',1102,3)
,
  (1873,'Accessibilité a verifier avec Lighthouse.','2026-02-03 13:28:00',1086,4)
,
  (1874,'Le design system a ete suivi correctement.','2026-02-02 18:53:00',1093,4)
,
  (1875,'Merge conflict a resoudre avant le merge.','2026-02-08 11:40:00',1093,3)
,
  (1876,'Le responsive est bon sur tablette.','2026-02-08 14:30:00',1054,3)
,
  (1877,'Tache bien avancee, presque terminee.','2026-02-20 11:17:00',1064,1)
,
  (1878,'Le lazy loading est implemente.','2026-02-04 18:04:00',1064,9)
,
  (1879,'Cache implemente pour ameliorer les perfs.','2026-02-22 08:22:00',1121,9)
,
  (1880,'Deadline serr mais faisable.','2026-02-21 15:20:00',1121,9)
,
  (1881,'La notification push est opérationnelle.','2026-02-10 09:54:00',1121,5)
,
  (1882,'Env de staging a deployer pour test.','2026-02-07 10:56:00',1151,9)
,
  (1883,'WebSocket connecte et fonctionnel.','2026-02-25 17:23:00',1135,10)
,
  (1884,'C''est pret pour le test en staging.','2026-02-01 16:46:00',1135,1)
,
  (1885,'Sujet a discuter en daily standup.','2026-02-26 08:34:00',1135,1)
,
  (1886,'Labels et priorite mis a jour.','2026-02-01 18:23:00',1143,3)
,
  (1887,'Optimisation de la requete SQL necessaire.','2026-02-14 08:01:00',1143,8)
,
  (1888,'Optimisation de la requete SQL necessaire.','2026-02-08 09:51:00',1137,4)
,
  (1889,'Les droits d accès sont vérifiés.','2026-02-11 10:07:00',1137,6)
,
  (1890,'Il y a un bug a corriger sur cette partie.','2026-02-23 18:54:00',1137,9)
,
  (1891,'Le feature flag est en place.','2026-02-07 11:51:00',1111,5)
,
  (1892,'Corrections mineures a apporter.','2026-02-14 10:30:00',1111,9)
,
  (1893,'WebSocket connecte et fonctionnel.','2026-02-07 18:36:00',1155,3)
,
  (1894,'Le pipeline CI/CD est vert.','2026-02-11 17:38:00',1155,10)
,
  (1895,'Les droits d accès sont vérifiés.','2026-02-17 08:46:00',1145,6)
,
  (1896,'Integration avec le frontend en cours.','2026-02-13 09:02:00',1117,8)
,
  (1897,'Le feature flag est en place.','2026-02-02 15:45:00',1167,1)
,
  (1898,'Le rate limiting est en place.','2026-02-22 14:19:00',1167,4)
,
  (1899,'Besoin de plus de details avant de commencer.','2026-02-19 09:55:00',1167,9)
,
  (1900,'Le token est bien refresh automatiquement.','2026-02-06 13:13:00',1156,3)
,
  (1901,'Sujet a discuter en daily standup.','2026-02-18 12:40:00',1132,7)
,
  (1902,'La notification push est opérationnelle.','2026-02-12 11:30:00',1132,5)
,
  (1903,'Le lazy loading est implemente.','2026-02-11 12:01:00',1162,9)
,
  (1904,'Sous-tache creee pour le detail.','2026-02-27 14:24:00',1162,8)
,
  (1905,'La securite a ete renforcee.','2026-02-28 09:49:00',1144,3)
,
  (1906,'Les erreurs sont bien gerees cote client.','2026-02-09 17:22:00',1144,7)
,
  (1907,'Sujet a discuter en daily standup.','2026-02-14 13:20:00',1116,10)
,
  (1908,'A detester sur mobile aussi.','2026-02-19 15:17:00',1161,5)
,
  (1909,'Migration de base de donnees requise.','2026-02-01 09:55:00',1130,2)
,
  (1910,'La page charge en moins de 2 secondes.','2026-02-19 14:26:00',1166,1)
,
  (1911,'API rate limiter a configurer.','2026-02-13 15:17:00',1160,7)
,
  (1912,'Migration de base de donnees requise.','2026-02-17 15:30:00',1160,3)
,
  (1913,'PR soumise, en attente de revue.','2026-02-23 08:39:00',1160,3)
,
  (1914,'L animation de transition est fluide.','2026-02-05 10:38:00',1148,7)
,
  (1915,'Le pipeline CI/CD est vert.','2026-02-20 11:41:00',1148,2)
,
  (1916,'L animation de transition est fluide.','2026-02-25 16:36:00',1148,9)
,
  (1917,'Labels et priorite mis a jour.','2026-02-03 16:04:00',1190,6)
,
  (1918,'Le design est valide par le client.','2026-02-28 10:38:00',1213,7)
,
  (1919,'Le tracking des evenements est en place.','2026-02-24 12:17:00',1213,7)
,
  (1920,'Le formulaire est validé côté serveur.','2026-02-23 09:21:00',1230,2)
,
  (1921,'La page charge en moins de 2 secondes.','2026-02-03 10:44:00',1230,9)
,
  (1922,'Fonctionnalite conforme aux specs.','2026-02-26 14:44:00',1230,3)
,
  (1923,'La tache a ete decomposee en sous-taches.','2026-02-19 10:35:00',1198,8)
,
  (1924,'Le timeout est trop court, a ajuster.','2026-02-25 09:12:00',1198,10)
,
  (1925,'Loggin ajoute pour le debug.','2026-02-20 16:31:00',1207,10)
,
  (1926,'PR soumise, en attente de revue.','2026-02-19 18:50:00',1207,5)
,
  (1927,'Merge conflict a resoudre avant le merge.','2026-02-02 11:23:00',1174,8)
,
  (1928,'Besoin de plus de details avant de commencer.','2026-02-18 10:28:00',1171,7)
,
  (1929,'Le responsive est bon sur tablette.','2026-02-18 13:49:00',1171,4)
,
  (1930,'L export PDF fonctionne correctement.','2026-02-17 12:13:00',1224,10)
,
  (1931,'Documentation a ecrire pour cette partie.','2026-02-06 09:06:00',1224,2)
,
  (1932,'Tache bien avancee, presque terminee.','2026-02-20 14:36:00',1229,6)
,
  (1933,'Le formulaire est validé côté serveur.','2026-02-03 17:32:00',1191,2)
,
  (1934,'La page charge en moins de 2 secondes.','2026-02-11 18:09:00',1226,5)
,
  (1935,'A detester sur mobile aussi.','2026-02-09 15:06:00',1217,10)
,
  (1936,'Le dark mode est disponible.','2026-02-01 16:42:00',1209,5)
,
  (1937,'Performance a ameliorer sur cette fonctionnalite.','2026-02-23 12:01:00',1183,8)
,
  (1938,'A detester sur mobile aussi.','2026-02-17 15:59:00',1183,1)
,
  (1939,'Faut integrer avec l API existante.','2026-02-23 12:09:00',1215,5)
,
  (1940,'Sous-tache creee pour le detail.','2026-02-01 16:53:00',1215,6)
,
  (1941,'Il y a un bug a corriger sur cette partie.','2026-02-27 10:49:00',1215,7)
,
  (1942,'L export PDF fonctionne correctement.','2026-02-02 11:53:00',1216,10)
,
  (1943,'La pagination est implementee.','2026-02-07 09:19:00',1199,6)
,
  (1944,'Le formulaire est validé côté serveur.','2026-02-04 12:08:00',1199,6)
,
  (1945,'Le rate limiting est en place.','2026-02-17 13:52:00',1227,9)
,
  (1946,'Performance a ameliorer sur cette fonctionnalite.','2026-02-19 15:27:00',1184,10)
,
  (1947,'Les tests de regression sont verts.','2026-02-07 17:22:00',1218,9)
,
  (1948,'J''ai commence le developpement, avancee lente.','2026-02-02 14:46:00',1268,1)
,
  (1949,'Le responsive est bon sur tablette.','2026-02-11 10:01:00',1287,1)
,
  (1950,'Fonctionnalite conforme aux specs.','2026-02-23 14:52:00',1287,1)
,
  (1951,'L animation de transition est fluide.','2026-02-01 13:32:00',1241,8)
,
  (1952,'Le formulaire est validé côté serveur.','2026-02-14 18:40:00',1241,6)
,
  (1953,'La pagination est implementee.','2026-02-17 13:20:00',1290,8)
,
  (1954,'Le timeout est trop court, a ajuster.','2026-02-21 15:07:00',1290,3)
,
  (1955,'Code review en attente.','2026-02-26 15:28:00',1276,1)
,
  (1956,'Le rate limiting est en place.','2026-02-12 18:03:00',1255,10)
,
  (1957,'Le rate limiting est en place.','2026-02-08 08:36:00',1285,4)
,
  (1958,'WebSocket connecte et fonctionnel.','2026-02-15 13:28:00',1267,6)
,
  (1959,'Les erreurs sont bien gerees cote client.','2026-02-10 17:26:00',1267,4)
,
  (1960,'Les erreurs sont bien gerees cote client.','2026-02-12 15:53:00',1267,5)
,
  (1961,'Besoin d une estimation supplementaire.','2026-02-21 14:14:00',1249,9)
,
  (1962,'Loggin ajoute pour le debug.','2026-02-26 09:10:00',1249,8)
,
  (1963,'Labels et priorite mis a jour.','2026-02-02 12:55:00',1286,6)
,
  (1964,'Le scope a ete modifie par le product owner.','2026-02-24 15:14:00',1286,5)
,
  (1965,'Le feature flag est en place.','2026-02-23 08:13:00',1252,7)
,
  (1966,'Le timeout est trop court, a ajuster.','2026-02-02 09:12:00',1252,1)
,
  (1967,'Labels et priorite mis a jour.','2026-02-17 14:29:00',1256,7)
,
  (1968,'Le dark mode est disponible.','2026-02-24 12:31:00',1256,6)
,
  (1969,'L historique des modifications est sauvegardé.','2026-02-15 15:01:00',1265,1)
,
  (1970,'Loggin ajoute pour le debug.','2026-02-19 08:50:00',1272,6)
,
  (1971,'La page charge en moins de 2 secondes.','2026-02-19 11:03:00',1272,6)
,
  (1972,'Dependance avec la lib externe a jour.','2026-02-07 17:16:00',1244,4)
,
  (1973,'Code review en attente.','2026-02-24 11:06:00',1274,5)
,
  (1974,'Le design est valide par le client.','2026-02-15 13:46:00',1274,3)
,
  (1975,'Le pipeline CI/CD est vert.','2026-02-09 18:05:00',1235,2)
,
  (1976,'Le pipeline CI/CD est vert.','2026-02-04 09:48:00',1277,2)
,
  (1977,'Le formulaire de validation est complet.','2026-02-13 16:12:00',1283,5)
,
  (1978,'Optimisation de la requete SQL necessaire.','2026-02-16 16:29:00',1254,2)
,
  (1979,'Code review en attente.','2026-02-02 15:30:00',1331,6)
,
  (1980,'Le client a valide cette feature.','2026-02-25 17:50:00',1337,5)
,
  (1981,'Corrections mineures a apporter.','2026-02-05 17:42:00',1337,2)
,
  (1982,'Le feature flag est en place.','2026-02-09 13:31:00',1295,7)
,
  (1983,'La page charge en moins de 2 secondes.','2026-02-04 15:51:00',1295,2)
,
  (1984,'Documentation a ecrire pour cette partie.','2026-02-15 18:10:00',1297,3)
,
  (1985,'Env de staging a deployer pour test.','2026-02-23 13:52:00',1297,9)
,
  (1986,'Tests unitaires passes avec succes.','2026-02-08 18:06:00',1327,9)
,
  (1987,'Corrections mineures a apporter.','2026-02-23 14:41:00',1327,7)
,
  (1988,'Le formulaire de validation est complet.','2026-02-12 17:03:00',1294,8)
,
  (1989,'Le pipeline CI/CD est vert.','2026-02-24 10:46:00',1294,8)
,
  (1990,'Le timeout est trop court, a ajuster.','2026-02-16 11:38:00',1308,3)
,
  (1991,'Bloque par la tache precedente.','2026-02-16 08:38:00',1308,3)
,
  (1992,'Les erreurs sont bien gerees cote client.','2026-02-26 11:31:00',1308,5)
,
  (1993,'Les droits d accès sont vérifiés.','2026-02-03 11:25:00',1343,9)
,
  (1994,'WebSocket connecte et fonctionnel.','2026-02-18 09:37:00',1304,10)
,
  (1995,'Les tests de regression sont verts.','2026-02-12 14:09:00',1304,4)
,
  (1996,'Le lazy loading est implemente.','2026-02-06 08:46:00',1304,4)
,
  (1997,'Cache implemente pour ameliorer les perfs.','2026-02-10 10:39:00',1307,5)
,
  (1998,'Loggin ajoute pour le debug.','2026-02-28 14:54:00',1312,5)
,
  (1999,'Swagger/OpenAPI a mettre a jour.','2026-02-13 15:16:00',1328,4)
,
  (2000,'Il y a un bug a corriger sur cette partie.','2026-02-03 14:03:00',1296,4)
,
  (2001,'Cache implemente pour ameliorer les perfs.','2026-02-23 13:57:00',1324,9)
,
  (2002,'Sous-tache creee pour le detail.','2026-02-12 09:13:00',1332,2)
,
  (2003,'Documentation a ecrire pour cette partie.','2026-02-09 17:37:00',1339,5)
,
  (2004,'Accessibilité a verifier avec Lighthouse.','2026-02-08 12:49:00',1326,8)
,
  (2005,'La notification push est opérationnelle.','2026-02-12 17:58:00',1329,6)
,
  (2006,'Le rate limiting est en place.','2026-02-21 13:37:00',1291,4)
,
  (2007,'La securite a ete renforcee.','2026-02-09 09:16:00',1311,5)
;
-- Total: 657 commentaires generes

-- ====================================================================
-- 9. MEMBRES DE PROJET (project_member)
-- ====================================================================
INSERT INTO project_member (id, project_id, user_id, role, joined_at) VALUES
  
  (2008,11,1,'OWNER','2026-01-08 09:00:00')
,
  (2009,11,8,'MANAGER','2026-02-16 10:00:00')
,
  (2010,11,2,'MEMBER','2026-02-19 10:00:00')
,
  (2011,12,4,'OWNER','2026-01-04 09:00:00')
,
  (2012,12,6,'MANAGER','2026-01-16 10:00:00')
,
  (2013,12,10,'MEMBER','2026-01-19 10:00:00')
,
  (2014,12,2,'MEMBER','2026-02-07 10:00:00')
,
  (2015,13,5,'OWNER','2026-01-14 09:00:00')
,
  (2016,13,10,'MANAGER','2026-01-12 10:00:00')
,
  (2017,13,8,'MEMBER','2026-02-17 10:00:00')
,
  (2018,14,1,'OWNER','2026-01-10 09:00:00')
,
  (2019,14,7,'MANAGER','2026-02-09 10:00:00')
,
  (2020,14,3,'MEMBER','2026-01-05 10:00:00')
,
  (2021,15,4,'OWNER','2026-01-11 09:00:00')
,
  (2022,15,3,'MANAGER','2026-02-01 10:00:00')
,
  (2023,15,1,'MEMBER','2026-01-22 10:00:00')
,
  (2024,15,6,'MEMBER','2026-01-04 10:00:00')
,
  (2025,16,5,'OWNER','2026-01-13 09:00:00')
,
  (2026,16,9,'MANAGER','2026-02-10 10:00:00')
,
  (2027,16,10,'MEMBER','2026-01-24 10:00:00')
,
  (2028,16,4,'MEMBER','2026-02-21 10:00:00')
,
  (2029,17,1,'OWNER','2026-01-10 09:00:00')
,
  (2030,17,3,'MANAGER','2026-03-01 10:00:00')
,
  (2031,17,8,'MEMBER','2026-02-04 10:00:00')
,
  (2032,17,7,'MEMBER','2026-02-22 10:00:00')
,
  (2033,18,4,'OWNER','2026-01-12 09:00:00')
,
  (2034,18,9,'MANAGER','2026-02-01 10:00:00')
,
  (2035,18,8,'MEMBER','2026-01-09 10:00:00')
,
  (2036,18,2,'MEMBER','2026-01-07 10:00:00')
,
  (2037,18,5,'MEMBER','2026-02-09 10:00:00')
,
  (2038,19,5,'OWNER','2026-01-04 09:00:00')
,
  (2039,19,8,'MANAGER','2026-03-18 10:00:00')
,
  (2040,19,4,'MEMBER','2026-01-20 10:00:00')
,
  (2041,20,1,'OWNER','2026-01-08 09:00:00')
,
  (2042,20,3,'MANAGER','2026-03-26 10:00:00')
,
  (2043,20,5,'MEMBER','2026-03-21 10:00:00')
,
  (2044,20,10,'MEMBER','2026-03-10 10:00:00')
,
  (2045,21,4,'OWNER','2026-01-13 09:00:00')
,
  (2046,21,3,'MANAGER','2026-01-11 10:00:00')
,
  (2047,21,6,'MEMBER','2026-03-24 10:00:00')
,
  (2048,21,2,'MEMBER','2026-01-22 10:00:00')
,
  (2049,22,5,'OWNER','2026-01-03 09:00:00')
,
  (2050,22,10,'MANAGER','2026-02-24 10:00:00')
,
  (2051,22,7,'MEMBER','2026-02-26 10:00:00')
,
  (2052,22,4,'MEMBER','2026-03-02 10:00:00')
,
  (2053,22,9,'MEMBER','2026-03-03 10:00:00')
,
  (2054,23,1,'OWNER','2026-01-03 09:00:00')
,
  (2055,23,8,'MANAGER','2026-03-07 10:00:00')
,
  (2056,23,3,'MEMBER','2026-03-25 10:00:00')
,
  (2057,23,2,'MEMBER','2026-03-25 10:00:00')
,
  (2058,24,4,'OWNER','2026-01-15 09:00:00')
,
  (2059,24,5,'MANAGER','2026-01-09 10:00:00')
,
  (2060,24,3,'MEMBER','2026-02-17 10:00:00')
,
  (2061,24,2,'MEMBER','2026-02-12 10:00:00')
,
  (2062,24,10,'MEMBER','2026-01-09 10:00:00')
,
  (2063,25,5,'OWNER','2026-01-13 09:00:00')
,
  (2064,25,8,'MANAGER','2026-01-08 10:00:00')
,
  (2065,25,4,'MEMBER','2026-03-02 10:00:00')
,
  (2066,25,3,'MEMBER','2026-03-10 10:00:00')
,
  (2067,26,1,'OWNER','2026-01-13 09:00:00')
,
  (2068,26,2,'MANAGER','2026-03-26 10:00:00')
,
  (2069,26,7,'MEMBER','2026-01-23 10:00:00')
,
  (2070,26,8,'MEMBER','2026-03-16 10:00:00')
,
  (2071,27,4,'OWNER','2026-01-08 09:00:00')
,
  (2072,27,6,'MANAGER','2026-02-20 10:00:00')
,
  (2073,27,10,'MEMBER','2026-03-04 10:00:00')
,
  (2074,27,2,'MEMBER','2026-01-19 10:00:00')
,
  (2075,28,5,'OWNER','2026-01-13 09:00:00')
,
  (2076,28,9,'MANAGER','2026-01-26 10:00:00')
,
  (2077,28,10,'MEMBER','2026-02-22 10:00:00')
,
  (2078,28,4,'MEMBER','2026-03-22 10:00:00')
,
  (2079,29,1,'OWNER','2026-01-12 09:00:00')
,
  (2080,29,10,'MANAGER','2026-02-23 10:00:00')
,
  (2081,29,7,'MEMBER','2026-01-06 10:00:00')
,
  (2082,30,4,'OWNER','2026-01-10 09:00:00')
,
  (2083,30,5,'MANAGER','2026-02-09 10:00:00')
,
  (2084,30,8,'MEMBER','2026-03-06 10:00:00')
;
-- Total: 77 membres de projet generes

-- ====================================================================
-- 10. MISE A JOUR DES SEQUENCES
-- ====================================================================
SELECT setval('sequence_generator', 2084);
