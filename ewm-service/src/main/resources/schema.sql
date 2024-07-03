CREATE TABLE IF NOT EXISTS USERS (
  ID    BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  NAME  VARCHAR(255) NOT NULL,
  EMAIL VARCHAR(512) NOT NULL,
  CONSTRAINT PK_USER PRIMARY KEY (ID),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (EMAIL)
);

CREATE TABLE IF NOT EXISTS CATEGORIES (
  ID    BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  NAME  VARCHAR(50) NOT NULL UNIQUE,
  CONSTRAINT PK_CATEGORIES PRIMARY KEY (ID),
  CONSTRAINT UQ_CATEGORIES_NAME UNIQUE (NAME)
);


CREATE TABLE IF NOT EXISTS LOCATIONS
(
    ID      BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    LAT     REAL NOT NULL,
    LON     REAL NOT NULL,
    CONSTRAINT PK_LOCATIONS PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS EVENTS (
  ID                 BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  ANNOTATION         VARCHAR(2000) NOT NULL,
  CATEGORY_ID        BIGINT NOT NULL REFERENCES CATEGORIES (ID) ON DELETE CASCADE,
  DESCRIPTION        VARCHAR(7000),
  CREATED            TIMESTAMP WITHOUT TIME ZONE,
  EVENT_DATE         TIMESTAMP WITHOUT TIME ZONE,
  PUBLISHED_DATE     TIMESTAMP WITHOUT TIME ZONE,
  INITIATOR_ID       BIGINT NOT NULL REFERENCES USERS (ID) ON DELETE CASCADE,
  LOCATION_ID        BIGINT NOT NULL REFERENCES LOCATIONS (ID) ON DELETE CASCADE,
  PAID               BOOLEAN,
  CONFIRMED_REQUEST  INTEGER,
  PARTICIPANT_LIMIT  INTEGER NOT NULL,
  REQUEST_MODERATION BOOLEAN,
  TITLE              VARCHAR(120) NOT NULL,
  STATE              VARCHAR(20),
  CONSTRAINT PK_EVENTS PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS REQUESTS
(
    ID           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    EVENT_ID     BIGINT NOT NULL REFERENCES EVENTS (ID),
    REQUESTER_ID BIGINT NOT NULL REFERENCES USERS (ID),
    CREATED      TIMESTAMP WITHOUT TIME ZONE,
    STATUS       VARCHAR(20),
    CONSTRAINT PK_REQUESTS PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS COMPILATIONS
(
    ID     BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    PINNED BOOLEAN NOT NULL,
    TITLE  VARCHAR(50) NOT NULL,
    CONSTRAINT PK_COMPILATIONS PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS COMPILATION_EVENT
(
    ID             BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    EVENT_ID       BIGINT NOT NULL REFERENCES EVENTS (ID) ON DELETE CASCADE,
    COMPILATION_ID BIGINT NOT NULL REFERENCES COMPILATIONS (id) ON DELETE CASCADE,
    CONSTRAINT PK_COMPILATION_EVENT PRIMARY KEY (ID)
);