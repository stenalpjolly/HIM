<?php
include("localdb.php");

// return json response 
$json = array();
//$user = array();

$username  = $_GET["name"];
$phone = $_GET["phone"];
$emailId = $_GET["email"];
$regID  = $_GET["regId"]; // Registration ID for Gcm or Provided by Layer.com

// Send this message to device
$message = "Registration Success";

/**
 * Registering a user device in database
 * Store reg id in users table
 */
//for($x=1;$x<10;$x++){
//	$user = array("name" => $nameUser,"id"=>"123");
//	$json[] = $user;
//}
 //$json["name"] = array($nameUser,$nameUser);
 //$json["regid"] = array($nameUser,$nameUser);
if (isset($username) && isset($phone) && isset($emailId) && isset($regID) && $username!="" && $phone!="" && $emailId!="" && $regID!="") {
//   print( json_encode(array('users'=>$json)) );
   //$sql = "select * from contact";
   $sql = "INSERT INTO `contact` VALUES ('', '$username','$phone','$emailId','$regID')";
   $result = mysql_query($sql) or die(mysql_error());
   
   print $message;
} else {
    // user details not found
	print "Wrong values.";
}

?>