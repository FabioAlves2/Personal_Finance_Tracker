-- Ensure unique only when code is not null
CREATE UNIQUE INDEX IF NOT EXISTS uk_categories_code
  ON categories (code)
  WHERE code IS NOT NULL;

-- Global income
INSERT INTO categories (code, name, color, icon, kind, user_id)
VALUES ('SAL', 'Salary', '#22C55E', 'badge-dollar', 'INCOME', NULL)
ON CONFLICT (code) DO NOTHING;

INSERT INTO categories (code, name, color, icon, kind, user_id)
VALUES ('BON', 'Bonus', '#16A34A', 'gift', 'INCOME', NULL)
ON CONFLICT (code) DO NOTHING;

-- Global expenses
INSERT INTO categories (code, name, color, icon, kind, user_id)
VALUES ('GROC', 'Groceries', '#F97316', 'shopping-basket', 'EXPENSE', NULL)
ON CONFLICT (code) DO NOTHING;

INSERT INTO categories (code, name, color, icon, kind, user_id)
VALUES ('RENT', 'Rent', '#EF4444', 'home', 'EXPENSE', NULL)
ON CONFLICT (code) DO NOTHING;

INSERT INTO categories (code, name, color, icon, kind, user_id)
VALUES ('TRANS', 'Transport', '#06B6D4', 'bus', 'EXPENSE', NULL)
ON CONFLICT (code) DO NOTHING;

-- Savings
INSERT INTO categories (code, name, color, icon, kind, user_id)
VALUES ('SAV', 'Savings', '#ff6a00', 'savings', 'SAVING', NULL)
ON CONFLICT (code) DO NOTHING;

