OPTIONS(skip=0) LOAD DATA CHARACTERSET ZHS16GBK
INFILE './file/load.csv' 
INTO TABLE T_TEST_LOAD 
APPEND FIELDS TERMINATED BY "," 
optionally enclosed by "\n" 
trailing nullcols(name, alias)