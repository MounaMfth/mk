-- SQL script to update user 'ahmed' role to ADMIN
-- Run this in your PostgreSQL client (pgAdmin, IntelliJ, etc.)

DO $$
DECLARE
    v_user_id BIGINT;
BEGIN
    -- Find user ID for 'ahmed'
    SELECT id INTO v_user_id FROM utilisateur WHERE username = 'ahmed';
    
    IF v_user_id IS NOT NULL THEN
        -- Update profile to 'ADMIN' (maps to ROLE_ADMIN in Java)
        UPDATE utilisateur SET profil = 'ADMIN' WHERE id = v_user_id;
        
        -- Ensure ADMIN role is the only role or at least present
        DELETE FROM user_roles WHERE user_id = v_user_id;
        INSERT INTO user_roles (user_id, role) VALUES (v_user_id, 'ADMIN');
        
        RAISE NOTICE 'Role successfully updated to ADMIN for user: ahmed (ID: %)', v_user_id;
    ELSE
        RAISE EXCEPTION 'User "ahmed" not found in the database';
    END IF;
END $$;
