<?php
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

$roomid = $_POST['roomid'];
$sql = "select from_name, message from chat where roomid='$roomid'";
$result = mysql_query($sql, $connect);
if(!$result){
die('Could not query:' . mysql_error());
  echo "fail";
  echo die('Could not query:' . mysql_error());
}
else{
$chat_list = array();
  while($row = mysql_fetch_assoc($result)){
    $friend = $row['from_name'];
    $sql1 = "select profile_image from users where name='$friend'";

    $result1 = mysql_query($sql1, $connect);
    if(!$result1){
      echo die('Could not query to find profile_image:' . mysql_error());
    }
    else if($result1){
      while($row1 = mysql_fetch_assoc($result1)){
        $arrayMiddle=array(
          "from" => $row['from_name'],
          "message" => $row['message'],
          "profile" => $row1['profile_image']
        );
        array_push($chat_list, $arrayMiddle);
      }
    }

  }
echo json_encode($chat_list);
}
mysql_close($connect);

?>
