UPDATE users 
SET password_hash = '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQiy3aG',
    failed_login_attempts = 0,
    locked_until = NULL
WHERE email = 'admin@example.com';
