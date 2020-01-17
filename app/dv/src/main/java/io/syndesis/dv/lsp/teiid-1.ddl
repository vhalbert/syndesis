CREATE VIEW winelist ( 
      wine string(255), price decimal(2, 15), vendor string(255) 
) AS SELECT * FROM PostgresDB.winelist