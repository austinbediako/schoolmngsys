-- Migration V18: Full NaCCA / GES Ghana National Basic Education Curriculum Subject Catalog
-- Seeded for Nursery & KG (Seq 1-4), Lower Primary (Seq 5-7), Upper Primary (Seq 8-10), and JHS (Seq 11-13)

INSERT INTO subjects (name, code, min_level_sequence, max_level_sequence)
SELECT v.name, v.code, v.min_seq, v.max_seq
FROM (VALUES
    ('Language and Literacy', 'LIT', 1, 4),
    ('Numeracy', 'NUM', 1, 4),
    ('Creative Arts', 'CART', 1, 10),
    ('History of Ghana', 'HIST', 5, 10),
    ('Social Studies', 'SOC', 11, 13),
    ('Arabic', 'ARAB', 11, 13)
) AS v(name, code, min_seq, max_seq)
WHERE NOT EXISTS (
    SELECT 1 FROM subjects s WHERE s.code = v.code AND s.archived_at IS NULL
);

-- Update level ranges and names for existing seeded subjects to strictly match NaCCA curriculum
UPDATE subjects SET name = 'English Language', min_level_sequence = 5, max_level_sequence = 13 WHERE code = 'ENG' AND archived_at IS NULL;
UPDATE subjects SET name = 'Mathematics', min_level_sequence = 5, max_level_sequence = 13 WHERE code = 'MATH' AND archived_at IS NULL;
UPDATE subjects SET name = 'Science', min_level_sequence = 5, max_level_sequence = 10 WHERE code = 'SCI' AND archived_at IS NULL;
UPDATE subjects SET name = 'Integrated Science', min_level_sequence = 11, max_level_sequence = 13 WHERE code = 'ISCI' AND archived_at IS NULL;
UPDATE subjects SET name = 'Ghanaian Language and Culture', min_level_sequence = 5, max_level_sequence = 13 WHERE code = 'GHL' AND archived_at IS NULL;
UPDATE subjects SET name = 'Religious and Moral Education', min_level_sequence = 5, max_level_sequence = 13 WHERE code = 'RME' AND archived_at IS NULL;
UPDATE subjects SET name = 'Our World and Our People', min_level_sequence = 1, max_level_sequence = 10 WHERE code = 'OWOP' AND archived_at IS NULL;
UPDATE subjects SET name = 'Computing', min_level_sequence = 8, max_level_sequence = 13 WHERE code = 'ICT' AND archived_at IS NULL;
UPDATE subjects SET name = 'French', min_level_sequence = 8, max_level_sequence = 13 WHERE code = 'FRE' AND archived_at IS NULL;
UPDATE subjects SET name = 'Career Technology', min_level_sequence = 11, max_level_sequence = 13 WHERE code = 'CTECH' AND archived_at IS NULL;
UPDATE subjects SET name = 'Creative Arts and Design', min_level_sequence = 11, max_level_sequence = 13 WHERE code = 'CAD' AND archived_at IS NULL;
UPDATE subjects SET name = 'Physical and Health Education', min_level_sequence = 5, max_level_sequence = 13 WHERE code = 'PE' AND archived_at IS NULL;
