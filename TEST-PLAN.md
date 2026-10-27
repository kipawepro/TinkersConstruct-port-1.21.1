# Plan de test — correctifs des issues + synchro upstream

Jar : `build/libs/TinkersConstruct-Unofficial-Port-1.21.1-1.0.0-NeoForge.jar`

Préparation : monde créatif neuf, triche activée. Pour les issues 2, 3 et 5, utiliser aussi un **ancien monde** créé avec la version publiée actuelle (faire une copie avant).

Commandes utiles :
- `/give @s modernfoundry:<item>`
- `/summon <entité>`
- `/kill @e[type=item]` pour nettoyer le sol

---

## A. Les 9 issues

### 1. Melting modifier (Blaze / Squelette)
1. Fabriquer une épée ou un cleaver avec le modificateur **Melting** (ou `/give` un outil qui l'a déjà).
2. Tuer un **Blaze** : le réservoir de l'outil doit contenir du *Blazing Blood* (infobulle avec Shift).
3. Tuer un **Squelette** : le réservoir doit contenir du *Milk*.
4. Déconnexion / reconnexion : le fluide est toujours là.
- ✅ Fermé si le réservoir se remplit et le contenu persiste.

### 2. Part Chest qui perd ses objets
1. Poser un Part Chest, y mettre plusieurs pièces d'outils.
2. Quitter le monde, revenir : les pièces sont toujours là.
3. Casser le coffre : les pièces tombent au sol.
4. Ancien monde : ouvrir un Part Chest existant.
- ✅ Fermé si rien ne disparaît après relog.
- ⚠️ Les objets déjà perdus dans un ancien monde ne reviendront pas.

### 3. Sauvegarde des blocs de smeltery (même cause que l'issue 2)
1. Remplir un Melter, une Casting Table et un Faucet/Channel avec du fluide.
2. Relog : les fluides et les objets sont conservés.
- ✅ Fermé si tout persiste.

### 4. Tree Physics / haches non reconnues
1. Installer Tree Physics (ou tout mod qui teste le tag `minecraft:axes`).
2. Couper un arbre avec Hand Axe, Broad Axe et Mattock : l'arbre doit tomber comme avec une hache vanilla.
3. `/data` ou F3+H : les outils doivent apparaître dans `#minecraft:axes`.
4. Tester aussi un pickaxe sur un mod qui lit `#minecraft:pickaxes`.
- ✅ Fermé si les outils sont reconnus comme vraies haches.

### 5. Kama qui ne tond pas
1. Clic droit avec une Kama sur un **mouton** : laine récupérée, mouton tondu.
2. Tester une **mooshroom** et un **golem de neige**.
3. Kama avec Expanded : plusieurs moutons tondus d'un coup.
- ✅ Fermé si la tonte marche sur les trois.

### 6. Casting Table qui détruit le cast
1. Poser un cast (ex. ingot cast) sur une Casting Table.
2. Casser la table : le cast tombe au sol.
3. Recommencer pendant un coulage en cours (table à moitié remplie).
4. Même test avec un Casting Basin.
- ✅ Fermé si le cast n'est jamais perdu.

### 7. Minerais broyés (Create) non fondables
1. Avec Create installé : mettre `create:crushed_raw_iron` dans un Melter et dans la Smeltery.
2. Doit fondre en fer liquide (mêmes byproducts qu'un raw ore).
3. Vérifier au moins cuivre, or, zinc.
4. Sans Create : aucune erreur dans les logs au démarrage.
- ✅ Fermé si les crushed ores fondent.

### 8. Dupe de la Crafting Station
1. Poser une Crafting Station.
2. Placer une recette qui n'est **pas** dans le coin haut-gauche (ex. bâtons au centre droit).
3. Shift-clic sur le résultat plusieurs fois.
4. Compter les ingrédients consommés : ils doivent correspondre exactement au nombre de crafts.
- ✅ Fermé si aucun objet n'est dupliqué.

### 9. Arcs, arbalètes et Warpick
1. **Longbow / Shortbow** : sans charger, le tir est faible ; chargé à fond, le tir est puissant.
2. **Crossbow** : charger une flèche, l'arbalète reste chargée, tirer fonctionne. Relog avec une arbalète chargée : elle doit rester chargée.
3. **War Pick** : charger et tirer une flèche, comme une arbalète.
4. Utiliser les **flèches du mod** (`modernfoundry:arrow`) comme munitions : elles doivent être acceptées.
- ✅ Fermé si charge, munitions et tir marchent.

---

## B. Synchro upstream (nouveautés)

### Contenu nouveau
- [ ] **Armor stands décoratifs** : `/give @s modernfoundry:bamboo_armor_stand` (et bone, necrotic_bone, clear) ; poser, équiper, casser (l'objet tombe), dispenser.
- [ ] Bamboo = petit, Bone = bras, Necrotic bone = lumineux, Clear = invisible quand équipé.
- [ ] **Matériau corne** (goat horn) sur les ribcages slimesuit.
- [ ] **Phantom cuirass** craftable.
- [ ] **Gold Guard** : +vie selon les pièces en or portées ; **Chrysophilite** : plus de drops d'armure en or.
- [ ] **Outils comestibles** : manger un outil comestible, effets et durabilité.
- [ ] Un outil avec inventaire (Pockets) qui brûle dans la lave : son contenu tombe au sol.
- [ ] Dragon : écailles droppées seulement sur explosions infligeant au moins 2 dégâts.

### Non-régression
- [ ] Monde neuf : le log ne contient **aucune** ligne `ERROR` (vérifié en dev : 3419 recettes, 83 matériaux, 299 modificateurs chargés).
- [ ] Ancien monde : chargement sans crash, outils existants intacts.
- [ ] Construire un outil complet dans la Tinker Station, le réparer, ajouter un modificateur.
- [ ] Smeltery complète : fondre, alloyer, couler.
- [ ] JEI : ouvrir les recettes de casting, melting, part builder sans erreur.
- [ ] Livre (Materials and You) : s'ouvre et les pages s'affichent.
- [ ] Minage de zone (Sledge Hammer) : le bon côté est miné, y compris avec un mod de fake players (ex. Create deployer).

### Pas encore porté
- Bannière invisible avec skyslime.
- Nouvelles catégories JEI d'upstream (garde les anciennes).
