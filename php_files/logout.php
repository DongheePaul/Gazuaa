<?php
session_start();
$user_id = $_SESSION['email'];
$user_pw=$_SESSION['pw'];
$res=session_destroy();
if($res){
  echo"1";
}
?>
