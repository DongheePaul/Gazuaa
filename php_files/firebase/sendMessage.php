<?php
session_start();
	//데이터베이스에 접속해서 토큰들을 가져와서 FCM에 발신요청
	include_once 'sendMessageFunction.php';
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

	//친구신청 하는 사람
	$from = $_SESSION['email'];
	//친구신청 받는 사람
  $user = $_POST[writer];

  $sql = "select fcm_token from users where email='$user'";
  $result = mysql_query($sql, $connect);
     if(!$result){
     die('Could not query:' . mysql_error());
       echo "fail";
       echo die('Could not query:' . mysql_error());
   }

  $tokens = array();
  $row = mysql_fetch_assoc($result);
  array_push($tokens, $row['fcm_token']);


        $myMessage = $_POST['message']; //폼에서 입력한 메세지를 받음
	if ($myMessage == ""){
		$myMessage = "친구 신청이 도착했습니다.";
	}

	$message = array("message" => $myMessage);
	$message_status = send_notification($tokens, $message);
	echo $message_status."&&";


	$sql1 = "insert into FriendApply(from_email, to_email) values ('$from', '$user')";
	$result1 = mysql_query($sql1, $connect);
	if(!$result1){
		die('Could not query:' . mysql_error());
			echo "fail";
			echo die('Could not query:' . mysql_error());
	}else{
		echo "add in FriendApply list";
	}

	mysql_close($conn);

?>
