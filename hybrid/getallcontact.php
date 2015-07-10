<?php
header('Content-type: application/json');
include('localdb.php');
$json =  array();
$sql = "select * from contact";
$result = mysql_query($sql) or die(mysql_error());
while($row = mysql_fetch_array($result)){
	$json[] = $row;
}

echo json_encode(array("contacts"=>$json));
?>