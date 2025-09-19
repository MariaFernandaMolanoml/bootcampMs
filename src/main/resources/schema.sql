CREATE TABLE IF NOT EXISTS bootcamps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    launch_date DATE NOT NULL,
    duration_in_days INT NOT NULL
);

CREATE TABLE IF NOT EXISTS bootcamp_capabilities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bootcamp_id UUID NOT NULL,
    capability_id UUID NOT NULL,
    CONSTRAINT fk_bootcamp FOREIGN KEY (bootcamp_id) REFERENCES bootcamps(id)
);
