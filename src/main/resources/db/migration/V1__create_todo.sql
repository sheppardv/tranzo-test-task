CREATE TABLE entity (
  id SERIAL PRIMARY KEY,
  name TEXT
);

CREATE TABLE transition_history (
  id SERIAL PRIMARY KEY,
  entity_id SERIAL,
  from_state TEXT,
  to_state TEXT
);

CREATE TABLE transition_matrix (
  id SERIAL PRIMARY KEY,
  from_state TEXT,
  possible_next_states TEXT
);


-- test data
INSERT INTO entity (name) VALUES ('entity1');
INSERT INTO entity (name) VALUES ('entity2');
INSERT INTO entity (name) VALUES ('entity3');

INSERT INTO transition_matrix (from_state, possible_next_states)
VALUES ('init', 'pending,finished');

INSERT INTO transition_matrix (from_state, possible_next_states)
VALUES ('pending', 'closed,finished');