<?php
session_start();
//친구요청을 받은 사람(=로그인한 유저)이 요청을 수락하면 실행되는 php 파일. 친구요청 받은사람과 친구요청한 사람이 친구가 딘다.
//FriendApply 테이블의 friend 칼럼에 yes를 넣어준다.
$connect=mysql_connect("222.239.249.149", "root", ")0p9o8i7u");
// 데이터베이스 선택
mysql_select_db("gazuaa",$connect);
if($connect){
  //echo "db 연결 했슈";
}
if (!$connect) {
   echo "연결실패";
   die('Could not connect: ' . mysql_error());
}

//친구신청 수락하는 사람
$to = $_SESSION['email'];
//친구신청을 요청한 사람.
$from = $_POST[from];
$answer = $_POST[answer];

if($answer=="yes"){
$sql = "update FriendApply set friend=0 where from_email='$from' and to_email='$to'";
$result = mysql_query($sql, $connect);
   if(!$result){
   die('Could not query:' . mysql_error());
     echo "fail //";
     echo die('Could not query:' . mysql_error());
 }
 else{
   echo "yes";
 }
}
else if($answer=="no"){
  $sql = "delete from FriendApply where from_email='$from' and friend is NULL";
  $result = mysql_query($sql, $connect);
     if(!$result){
     die('Could not query:' . mysql_error());
       echo "fail //";
       echo die('Could not query:' . mysql_error());
   }
   else{
     echo "no";
   }
  }

mysql_close($connect);
?>
