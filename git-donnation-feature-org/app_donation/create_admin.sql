-- Script to create a new Global Admin user
-- Password will be "admin123" (BCrypt hash: $2a$10$8.V9TrkeS9dwUpWvSdH98uH9.yGZ8fS.g4uS8fS.g4uS8fS.g4uS8)
-- Note: Replace the hash if you want a different password.

-- 1. Insert the user into the utilisateur table
INSERT INTO utilisateur (username, email, password, profil, adresse, telephone, date_creation, date_modification)
VALUES ('admin_global', 'admin@khayriati.mr', '$2a$10$N9qo8uLOickgx2ZrVzY6qe3MYbuO6T24.chEzcoKp890VEuu4J.aW', 'ADMIN', 'Nouakchott', '12345678', CURRENT_DATE, CURRENT_DATE);

-- 2. Get the ID of the newly created user and assign the ADMIN role
-- (Assuming the ID is generated automatically)
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM utilisateur WHERE username = 'admin_global';

-- 3. Verify
SELECT u.id, u.username, u.profil, ur.role 
FROM utilisateur u 
JOIN user_roles ur ON u.id = ur.user_id 
WHERE u.username = 'admin_global';
