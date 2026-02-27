# NeuroGuard Consultation Service - Guide de Démarrage

## Problèmes Corrigés

### ✅ Problème 1: Type Mismatch dans le Contrôleur
**Erreur:** Les paramètres du contrôleur attendaient `Long` mais le filtre JWT fournissait `String`
**Statut:** RÉSOLU - Tous les paramètres convertis en `String`

### ✅ Problème 2: Identifiants de Base de Données Manquants
**Erreur:** Configuration MySQL incomplète sans username/password
**Statut:** RÉSOLU - Identifiants ajoutés à application.yaml

## Configuration Requise

### 1. MySQL
Assurez-vous que MySQL est en cours d'exécution:
```bash
# Vérifier la connexion
mysql -u root -p root -e "SELECT 1"
```

La base de données `consultation_db` sera créée automatiquement au démarrage.

### 2. Identifiants MySQL (application.yaml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/consultation_db?createDatabaseIfNotExist=true
    username: root        # ✅ Configuré
    password: root        # ✅ Configuré
```

### 3. Clés JWT
La clé JWT doit correspondre entre les services:
```yaml
jwt:
  secret: XDkzF2YNPA/7vXmPYJmaACjY6VBhwHJbr4pzPF5jguE=
```

### 4. Service Utilisateur (Node.js)
Assurez-vous que le service utilisateur est accessible:
```yaml
user-service:
  url: http://localhost:3001
```

## Démarrage du Service

### Option 1: Maven
```bash
cd consultation-service
mvn clean spring-boot:run
```

### Option 2: IDE
1. Ouvrir le projet dans IntelliJ IDEA
2. Cliquer sur le bouton "Run" pour la classe `ConsultationServiceApplication`

### Option 3: Build et Exécution
```bash
mvn clean package
java -jar target/consultation-service-0.0.1-SNAPSHOT.jar
```

## Validation du Service

### Vérifier le démarrage
```bash
curl http://localhost:8084/actuator/health
```

Réponse attendue:
```json
{"status":"UP"}
```

### API Endpoints

Tous les endpoints requièrent un token JWT valide en header:
```
Authorization: Bearer <token_jwt>
```

#### Créer une consultation (Provider)
```bash
POST http://localhost:8084/api/consultations
```

#### Récupérer mes consultations
```bash
GET http://localhost:8084/api/consultations/provider    # Pour Provider
GET http://localhost:8084/api/consultations/patient     # Pour Patient
GET http://localhost:8084/api/consultations/caregiver   # Pour Caregiver
```

#### Obtenir le lien Zoom
```bash
GET http://localhost:8084/api/consultations/{id}/join
```

## Dépannage

### Erreur: "Access denied for user 'root'"
- Vérifiez que MySQL est en cours d'exécution
- Vérifiez les identifiants dans application.yaml
- Test: `mysql -u root -p root`

### Erreur: "No instances available"
- Le service utilisateur (Node.js) doit être en cours d'exécution
- Vérifiez l'URL dans `user-service.url`

### Erreur: "Invalid JWT token"
- Vérifiez que le token est envoyé en header `Authorization: Bearer <token>`
- Vérifiez que la clé JWT secrète correspond entre les services

## Port
Le service s'exécute sur le port **8084**

## Logs
Activez les logs debug pour le dépannage:
```yaml
logging:
  level:
    com.neuroguard.consultationservice: DEBUG
    org.springframework.security: DEBUG
```

## Structure des Entités

### Consultation
- `id` (Long) - Clé primaire
- `providerId` (String) - ID du prestataire
- `patientId` (String) - ID du patient
- `caregiverId` (String) - ID du soignant (optionnel)
- `title`, `description`, `startTime`, `endTime`, `type`, `status`
- `meetingLink`, `meetingId` - Pour les consultations en ligne

## Prochaines Étapes

1. Démarrer MySQL
2. Démarrer le service utilisateur (Node.js sur port 3001)
3. Démarrer le service consultation (port 8084)
4. Obtenir un token JWT depuis le service utilisateur
5. Utiliser le token pour appeler les API du service consultation

## Support
Pour plus d'informations, consultez les fichiers de documentation:
- `TROUBLESHOOTING.md` - Guide de dépannage des erreurs
- `FIXES_APPLIED.md` - Détails des corrections apportées

