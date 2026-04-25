---
title: Database
description: Password database
---

## Setup

```mysql
USE example;
```

```mysql
CREATE TABLE IF NOT EXISTS users
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(100) UNIQUE,
    role       ENUM ('user', 'admin') DEFAULT 'user',
    created_at TIMESTAMP              DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```
