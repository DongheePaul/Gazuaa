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

$user = $_SESSION['email'];


$num = $_POST[board_id];

$sql = "select id, title, content, board_image_dir, time, writer from board where id=".$num;
$result = mysql_query($sql, $connect);
   if(!$result){
   die('Could not query:' . mysql_error());
     echo "fail";
     echo die('Could not query:' . mysql_error());
 }

$row = mysql_fetch_assoc($result);

  $id = $row['id'];
  $title = $row['title'];
  $time = $row['time'];
  $writer = $row['writer'];
  $image = $row['board_image_dir'];
  $content = $row['content'];
  $same = "0";
  // if($user == $writer){
  //
  // }else{
  //   $same = 1;
  // }

  $json_object = array('id'=>$id, 'title'=>$title, 'time'=>$time, 'writer'=>$writer, 'image'=>$image, 'content' => $content, 'same' => $same);
  echo json_encode($json_object);
mysql_close($connect);
 ?>
