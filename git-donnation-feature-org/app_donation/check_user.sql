-- Check user 9 (testdonat) details
SELECT 
    u.id,
    u.username,
    u.profil,
    u.organisation_id,
    o.nom as organisation_nom,
    r.role
FROM utilisateur u
LEFT JOIN organisation o ON u.organisation_id = o.id
LEFT JOIN user_roles r ON u.id = r.user_id
WHERE u.id = 9;
