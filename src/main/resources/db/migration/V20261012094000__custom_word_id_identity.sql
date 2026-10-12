-- Create a dedicated sequence for custom_word.id and attach it as the default value
CREATE SEQUENCE IF NOT EXISTS custom_word_sequence START WITH 1 INCREMENT BY 1;

ALTER TABLE custom_word
    ALTER COLUMN id SET DEFAULT nextval('custom_word_sequence');

ALTER TABLE custom_word
    ALTER COLUMN id SET NOT NULL;
