-- ATStudio V1 baseline data.
-- Apply exactly once after schema.sql on a newly created database.
-- These six plans are the sole runtime baseline rows; QA users remain opt-in.

INSERT INTO subscriptions (
    name,
    description,
    user_type,
    price_monthly,
    price_yearly,
    download_per_day,
    max_whitelist_channels,
    max_playlists,
    is_active
)
VALUES
    ('STANDARD', 'Individual Standard plan', 'INDIVIDUAL', 9900.00, 99000.00, 5, 1, 3, 1),
    ('DELUXE', 'Individual Deluxe plan', 'INDIVIDUAL', 19900.00, 199000.00, 20, 2, 10, 1),
    ('PREMIUM', 'Individual Premium plan', 'INDIVIDUAL', 29900.00, 299000.00, -1, 2, 10, 1),
    ('STANDARD', 'Business Standard plan', 'BUSINESS', 19900.00, 199000.00, 10, 1, 3, 1),
    ('DELUXE', 'Business Deluxe plan', 'BUSINESS', 49900.00, 499000.00, 50, 2, 10, 1),
    ('PREMIUM', 'Business Premium plan', 'BUSINESS', 99900.00, 999000.00, -1, 2, 10, 1);
