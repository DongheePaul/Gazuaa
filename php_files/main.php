<?php
session_start();
header('content-type: text/html; charset=utf-8');
// 데이터베이스 접속 문자열. (db위치, 유저 이름, 비밀번호)
$connect=mysql_connect("222.239.249.149", "root", ")0p9o8i7u");
// 데이터베이스 선택
mysql_select_db("gazuaa",$connect);
if (!$connect) {
    echo "연결실패";
    die('Could not connect: ' . mysql_error());

    echo "연결실패";
}

$user_email = $_SESSION['email'];

$sql = "select * from users where email='$user_email'";
$result = mysql_query($sql);
// 쿼리 결과
if($result)
{
  $row = mysql_fetch_array($result);

  echo $row[email];

  // else
  // {
  //   echo "$row[pw_chk]";   // 0이면 비밀번호 불일치, 1이면 일치
  // }
}
else
{
  echo "쿼리 실패";
 echo "mysql_errno($connect)";
}

?>
