<?php
session_start();
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
//로그인한 사용자.
$user = $_SESSION['email'];
//로그인한 사용자가 받은 친구신청을 가져오는 쿼리문.
$sql = "select from_email from FriendApply where to_email='$user' and friend is NULL";
$result = mysql_query($sql, $connect);
   if(!$result){
   die('Could not query:' . mysql_error());
     echo "fail";
     echo die('Could not query:' . mysql_error());
 }else{
   //1.새로운 어레이를 생성한다.
   $from_email =array();
   while($row = mysql_fetch_assoc($result)){
     //2.또 다른 어레이에 결과값들을 집어 넣는다.
     $arrayMiddle = array(
       "from" => $row['from_email']
     );
     //2.에서 만든 어레이를 1. 어레이에 집어넣는다.
     array_push($from_email, $arrayMiddle);
   }

echo json_encode($from_email);
 }
 mysql_close($connect);
 ?>
