-- Database Migration Script for UserDetails Properties
-- Run this script to add the missing UserDetails columns to the users table

-- Check if columns exist before adding them
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'enabled')
BEGIN
    ALTER TABLE users ADD enabled bit NOT NULL DEFAULT 1;
    PRINT 'Added enabled column';
END
ELSE
    PRINT 'enabled column already exists';

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'account_non_expired')
BEGIN
    ALTER TABLE users ADD account_non_expired bit NOT NULL DEFAULT 1;
    PRINT 'Added account_non_expired column';
END
ELSE
    PRINT 'account_non_expired column already exists';

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'account_non_locked')
BEGIN
    ALTER TABLE users ADD account_non_locked bit NOT NULL DEFAULT 1;
    PRINT 'Added account_non_locked column';
END
ELSE
    PRINT 'account_non_locked column already exists';

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'credentials_non_expired')
BEGIN
    ALTER TABLE users ADD credentials_non_expired bit NOT NULL DEFAULT 1;
    PRINT 'Added credentials_non_expired column';
END
ELSE
    PRINT 'credentials_non_expired column already exists';

-- Verify the columns were added successfully
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'users' 
    AND COLUMN_NAME IN ('enabled', 'account_non_expired', 'account_non_locked', 'credentials_non_expired')
ORDER BY COLUMN_NAME;

PRINT 'Database migration completed successfully!';
