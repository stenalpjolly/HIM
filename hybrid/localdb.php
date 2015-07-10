<?php
error_reporting(0);
 define('DB_HOST', 'localhost');
 define('DB_NAME', 'hybrid');
 define('DB_USER','root');
 define('DB_PASSWORD','');
 
   // Connect to the database
  $con = mysql_connect(DB_HOST,DB_USER,DB_PASSWORD);
  // Make sure we connected succesfully
  if(! $con)
  {
	  die('Connection Failed'.mysql_error());
  }
  
  // Select the database to use
  mysql_select_db(DB_NAME,$con);

?>