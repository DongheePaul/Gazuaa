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
//로그인한 사용자의 친구 데이터들을 찾는 쿼리문.
//where y.to_email='$user' and friend=0 => 나와 친구인 사람들의 users(유저정보 저장된 테이블) 정보들 중 이메일과 프로필이미지를 가져온다.
$sql = "select x.name, x.email, x.profile_image from users as x join FriendApply as y on x.email=y.from_email where y.to_email='$user' and friend=0";
$result = mysql_query($sql, $connect);
   if(!$result){
   die('Could not query:' . mysql_error());
     echo "fail";
     echo die('Could not query:' . mysql_error());
 }else{
   $friend_list =array();
   while($row = mysql_fetch_assoc($result)){
     $arrayMiddle = array(
       "name" => $row['name'],
       "from" => $row['email'],
       "image" => $row['profile_image']
     );
     array_push($friend_list, $arrayMiddle);
   }

echo json_encode($friend_list);
 }
 mysql_close($connect);
 ?>
