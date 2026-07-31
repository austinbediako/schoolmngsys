-- WP-1 auth (docs/14 §4, ADR-004, FR-AUTH-01..05, BR-SE-003/005).
-- Account links to exactly one Staff/Guardian/Student record (docs/03 §1) via (person_type, person_id);
-- no FK to those tables yet — people (WP-3) doesn't exist, so this stays an opaque reference for now.

CREATE TABLE accounts (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    person_type            TEXT NOT NULL CHECK (person_type IN ('STAFF', 'GUARDIAN', 'STUDENT')),
    person_id              UUID NOT NULL,
    login_identifier        TEXT NOT NULL,
    phone                  TEXT,
    email                  TEXT,
    password_hash          TEXT NOT NULL,
    status                 TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'DEACTIVATED', 'LOCKED')),
    force_password_change  BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts  INTEGER NOT NULL DEFAULT 0,
    locked_until           TIMESTAMPTZ,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by             UUID,
    updated_by             UUID,
    archived_at            TIMESTAMPTZ,
    version                BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_accounts_login_identifier ON accounts (login_identifier) WHERE archived_at IS NULL;
CREATE UNIQUE INDEX uq_accounts_phone ON accounts (phone) WHERE archived_at IS NULL AND phone IS NOT NULL;
CREATE INDEX idx_accounts_person_type_person_id ON accounts (person_type, person_id);

CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_by  UUID,
    archived_at TIMESTAMPTZ,
    version     BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_roles_name ON roles (name) WHERE archived_at IS NULL;

CREATE TABLE permissions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_by  UUID,
    archived_at TIMESTAMPTZ,
    version     BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_permissions_name ON permissions (name) WHERE archived_at IS NULL;

-- Static catalog relationship, seeded and extended by migration only (docs/03 §4) — no per-row audit columns.
CREATE TABLE role_permissions (
    role_id       UUID NOT NULL REFERENCES roles (id),
    permission_id UUID NOT NULL REFERENCES permissions (id),
    PRIMARY KEY (role_id, permission_id)
);

-- The mutable assignment of a role to an account (ROLE_ASSIGN) — revocation is soft (archived_at),
-- preserving "who had what role when" (memory.md non-negotiable: history is sacred).
CREATE TABLE account_roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id  UUID NOT NULL REFERENCES accounts (id),
    role_id     UUID NOT NULL REFERENCES roles (id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_by  UUID,
    archived_at TIMESTAMPTZ,
    version     BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_account_roles_account_id_role_id
    ON account_roles (account_id, role_id) WHERE archived_at IS NULL;

-- Hashed, rotating refresh tokens (ADR-004) — the one deliberate piece of server-side auth state.
CREATE TABLE refresh_tokens (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id            UUID NOT NULL REFERENCES accounts (id),
    token_hash            TEXT NOT NULL,
    issued_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at            TIMESTAMPTZ NOT NULL,
    revoked_at            TIMESTAMPTZ,
    replaced_by_token_id  UUID,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by            UUID,
    updated_by            UUID,
    archived_at           TIMESTAMPTZ,
    version               BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_refresh_tokens_token_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_account_id ON refresh_tokens (account_id);

-- Append-only login ledger backing lockout (FR-AUTH-05) — same rationale as audit_log: no update/delete path.
CREATE TABLE login_attempts (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    account_id  UUID,
    identifier  TEXT NOT NULL,
    succeeded   BOOLEAN NOT NULL,
    ip          TEXT
);

CREATE INDEX idx_login_attempts_account_id_occurred_at ON login_attempts (account_id, occurred_at);

-- Short-lived OTP for password reset (FR-AUTH-03); hashed, single-use, expiring.
CREATE TABLE password_reset_otps (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id    UUID NOT NULL REFERENCES accounts (id),
    otp_hash      TEXT NOT NULL,
    requested_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at    TIMESTAMPTZ NOT NULL,
    consumed_at   TIMESTAMPTZ
);

CREATE INDEX idx_password_reset_otps_account_id ON password_reset_otps (account_id);
