<meta charset="utf-8">
<?php
$email = $_GET[id];
$name = $_GET[name];


$output = array();
$output["result"] = "0";
$output["email"] = $email;
$output["name"] = $name;
   $output =  json_encode($output);

   echo  $output;
?>
