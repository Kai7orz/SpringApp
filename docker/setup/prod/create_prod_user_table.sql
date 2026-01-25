CREATE Table prod_database.user (
                                   id INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
                                   role_id INT,
                                   username VARCHAR(50),
                                   email VARCHAR(255),
                                   password VARCHAR(50),
                                   FOREIGN KEY (role_id) REFERENCES role(id)
);