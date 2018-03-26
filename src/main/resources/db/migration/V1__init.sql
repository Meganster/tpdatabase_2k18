CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE "User" (
  id SERIAL PRIMARY KEY,
  email CITEXT COLLATE "ucs_basic" UNIQUE NOT NULL,
  fullname VARCHAR(256) NOT NULL,
  nickname CITEXT COLLATE "ucs_basic" UNIQUE NOT NULL,
  about TEXT
);

CREATE TABLE Forum (
  id SERIAL PRIMARY KEY,
  title VARCHAR(256),
  posts INTEGER DEFAULT 0,
  threads INTEGER DEFAULT 0,
  slug CITEXT COLLATE "ucs_basic" NOT NULL UNIQUE,
  "user" CITEXT COLLATE "ucs_basic" NOT NULL REFERENCES "User" (nickname) ON DELETE CASCADE
);

CREATE TABLE Thread (
  id SERIAL PRIMARY KEY,
  title VARCHAR(256),
  author CITEXT COLLATE "ucs_basic" NOT NULL REFERENCES "User" (nickname) ON DELETE CASCADE,
  forum CITEXT NOT NULL REFERENCES Forum (slug) ON DELETE CASCADE,
  message TEXT,
  votes INTEGER DEFAULT 0,
  slug CITEXT UNIQUE DEFAULT NULL,
  created TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE Post (
  id SERIAL PRIMARY KEY,
  created TIMESTAMPTZ DEFAULT now(),
  forum CITEXT REFERENCES Forum (slug) ON DELETE CASCADE ,
  thread INTEGER REFERENCES Thread (id) ON DELETE CASCADE ,
  author CITEXT COLLATE "ucs_basic" REFERENCES "User" (nickname) ON DELETE CASCADE ,
  parent INTEGER DEFAULT 0,
  message TEXT,
  isEdited BOOLEAN DEFAULT FALSE
);

CREATE TABLE UserVoteForThreads (
  id SERIAL PRIMARY KEY ,
  user_id INTEGER REFERENCES "User" (id) ON DELETE CASCADE ,
  thread_id INTEGER REFERENCES Thread (id) ON DELETE CASCADE,
  vote INTEGER DEFAULT 0,
  UNIQUE (user_id, thread_id)
);