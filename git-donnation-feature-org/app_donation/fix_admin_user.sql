-- Fix admin user password and roles
-- This script will update the global_admin_test user with a properly hashed password
-- and ensure the ADMIN role is set correctly

-- 1. Update password to BCrypt hash of "123456"
UPDATE utilisateur 
SET password = '$2a$10$N9qo8uLOickgx2ZrVzY6qe3MYbuO6T24.chEzcoKp890VEuu4J.aW' 
WHERE id = 6;

-- 2. Ensure ADMIN role exists in user_roles table
-- First, delete any existing roles for this user to avoid duplicates
DELETE FROM user_roles WHERE user_id = 6;

-- Then insert the ADMIN role
INSERT INTO user_roles (user_id, role) VALUES (6, 'ADMIN');

-- 3. Verify the changes
SELECT 
    u.id,
    u.username,
    u.email,
    u.profil,
    ur.role
FROM utilisateur u
LEFT JOIN user_roles ur ON u.id = ur.user_id
WHERE u.id = 6;
