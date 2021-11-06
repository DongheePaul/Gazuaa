<?php
header('content-type: text/html; charset=utf-8');
// 데이터베이스 접속 문자열. (db위치, 유저 이름, 비밀번호)
$connect=mysql_connect("222.239.249.149", "root", ")0p9o8i7u");
// 데이터베이스 선택
mysql_select_db("gazuaa",$connect);
//db연결 실패시
  if (!$connect) {
      echo "연결실패";
      die('Could not connect: ' . mysql_error());

}
  session_start();
  $id = $_POST[u_id];
  $pw = $_POST[u_pw];
  $name = $_POST[u_name];
  $email = $_POST[u_email];


//아이디 중복확인
$sql = "select * from users where email='$email'";
$result = mysql_query($sql, $connect);

if(!$result){
   echo mysql_errno($connect) . ": " . mysql_error($connect). "\n";
}
$count = mysql_num_rows($result);

//아이디 중복확인에서 중복되는 아이디가 없다면
if($count == 0){
  $sql = "INSERT INTO users (name, pw, email) VALUES('$name', '$pw', '$email')";
  $result = mysql_query($sql, $connect) or die(mysql_error($connect));
       echo("0");
       mysql_close($connect);
}

//아이디 중복이라면
  else{
    echo("D");
    mysql_close($connect);
  }

//회원가입이라면
//끝

?>
