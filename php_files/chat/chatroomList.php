<?php
//채팅방 목록 불러오는 php 파일.
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

$name = $_POST['name'];

//로그인한 유저가 저장된 채팅방을 모두 불러온다.
$sql = "select * from chatroom where usr1='$name' or usr2='$name'";
$result = mysql_query($sql, $connect);

if(!$result){
  die('Could not query:' . mysql_error());
    echo "fail";
    echo die('Could not query:' . mysql_error());
}

//채팅 상대를 찾는 쿼리문
else if($result){
  $result_list = array();
  while($row = mysql_fetch_assoc($result)){
//usr1이 $name와 같은지 비교.  같지 않다면 usr1이 채팅상대이다.
    $str = strcmp($name, $row['usr1']);
//usr2가 $name와 같은지 비교.  같지 않다면 usr2가 채팅상대이다.
    $str1 = strcmp($name, $row['usr2']);
//"usr1이 $name와 같지 않다" ==>usr1이 채팅상대이다.
      if($str){
        $friendName = $row['usr1'];
        $sql1 = "select message from chat where roomid=".$row['roomID']." order by id desc limit 1";
        $result1 = mysql_query($sql1, $connect);
        if(!$result1){
          die('Could not query for last Message:'. mysql_error());
        }
        else if($result1){
          while($row1 = mysql_fetch_assoc($result1)){
            $sql2 = "select profile_image from users where name='$friendName'";
            $result2 = mysql_query($sql2, $connect);
            if(!$result2){
              die('could not query for frien_profile :'. mysql_error());
            }
            else if($result2){
              while($row2 = mysql_fetch_assoc($result2)){
              $arrayMiddle = array(
                "friend" => $friendName,
                "roomid" => $row['roomID'],
                "lastmsg" => $row1['message'],
                "friend_profile" => $row2['profile_image']
              );
                array_push($result_list, $arrayMiddle);
            }
            //while($row2 = mysql_fetch_assoc($result2)) 끝
            }
            //else if($result2) 끝

          }
          //while($row1 = mysql_fetch_assoc($result1)) 끝


        }
        //else if($result1)  끝
      }
      //  if($str).  "usr1이 $name와 같지 않다" ==>usr1이 채팅상대이다. 끝


//"usr2가 $name와 같지 않다" ==>usr2가 채팅상대이다.
      else if($str1){
        $friendName = $row['usr2'];
        $sql1 = "select message from chat where roomid=".$row['roomID']." order by id desc limit 1";
        $result1 = mysql_query($sql1, $connect);
        if(!$result1){
          die('Could not query for last Message:'. mysql_error());
        }
        else if($result1){
          while($row1 = mysql_fetch_assoc($result1)){
            $sql2 = "select profile_image from users where name='$friendName'";
            $result2 = mysql_query($sql2, $connect);
            if(!$result2){
              die('could not query for frien_profile :'. mysql_error());
            }
            else if($result2){
              while($row2 = mysql_fetch_assoc($result2)){
              $arrayMiddle = array(
                "friend" => $friendName,
                "roomid" => $row['roomID'],
                "lastmsg" => $row1['message'],
                "friend_profile" => $row2['profile_image']
              );
                array_push($result_list, $arrayMiddle);
              }
            }
          }
          //while($row1 = mysql_fetch_assoc($result1)) 끝
        }
        //else if($result1){ 끝
      }
      //else if($str1).   "usr2가 $name와 같지 않다" ==>usr2가 채팅상대이다. 끝.
    }
    //while($row = mysql_fetch_assoc($result)) 끝

    echo json_encode($result_list);
}

mysql_close($connect);
 ?>
