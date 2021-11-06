<?php
header('content-type: text/html; charset=utf-8');
// 데이터베이스 접속 문자열. (db위치, 유저 이름, 비밀번호)
$connect=mysql_connect("222.239.249.149", "root", ")0p9o8i7u");
// 데이터베이스 선택
mysql_select_db("gazuaa",$connect);
if($connect){
  //echo "db 연결 했슈";
}
if (!$connect) {
   echo "연결실패";
   die('Could not connect: ' . mysql_error());

   echo "연결실패";
}
  // 세션 시작
  session_start();


  $email = $_POST[u_email];
  $pw = $_POST[u_pw];
  $name = $_POST[u_name];
  $id = $_POST[u_id];


//페이스북 로그인.
//이미 회원db에 존재하는 이메일인지 중복확인.
  if(isset($name)){
    $sql = "select * from users where email='$email'";
    $result = mysql_query($sql,$connect);
    if(!$result){
      echo mysql_errno($connect) . ": " . mysql_error($connect). "\n";
    }
    $count = mysql_num_rows($result);
//중복확인 끝

    //회원정보 존재하지 않는다면 회원정보 삽입.
    if($count == 0){
      $sql = "INSERT INTO users (name, email, pw) VALUES('$name', '$email', '$id')";
      $result = mysql_query($sql, $connect) or die(mysql_error($connect));

      $output = array();
      $output["result"] = "1";
      $output["email"] = $email;
      $output["name"] = $name;
      $output["id"] = $id;
      $output =  json_encode($output);

         echo  $output;
           $_SESSION['email'] = $email;
           mysql_close($connect);
    }//회원정보 존재하지 않는다면 회원정보 삽입
    //끝

    //회원정보 존재한다면 바로 로그인
    else{

      $output = array();
      $output["result"] = "1";
      $output["email"] = $email;
      $output["name"] = $name;
      $output["id"] = $id;
         $output =  json_encode($output);

         echo  $output;
      $_SESSION['email'] = $email;
      mysql_close($connect);
    }
  }  //페이스북 로그인 끝


  //회원정보로 로그인하는 경우
  else{
  $sql = "SELECT IF(strcmp(pw,'$pw'),0,1) pw_chk FROM users  WHERE email = '$email'";
  $result = mysql_query($sql, $connect);


  // 쿼리 결과
  if($result)
  {
    $row = mysql_fetch_array($result);
    //아이디가 존재하지 않는다면
    if(is_null($row[pw_chk]))
    {

      $output = array();
      $output["result"] = "B";
      $output["email"] = $email;
      $output["name"] = $name;
         $output =  json_encode($output);

         echo  $output;

    }
    //비밀번호 일치
    else if($row[pw_chk] == 1){
      $sql = "select name, email from users where email='$email'";
      $result = mysql_query($sql,$connect);
      $row = mysql_fetch_assoc($result);

      $output = array();
      $output["result"] = "1";
      $output["email"] = $email;
      $output["name"] = $row['name'];
      $output = json_encode($output);

        $_SESSION['email'] = $row['email'];
         echo  $output;
         mysql_close();
    }
    //비밀번호 불일치
    else if($row[pw_chk] == 0){

      $output = array();
      $output["result"] = "0";
      $output["email"] = $email;
      $output["name"] = $name;
         $output =  json_encode($output);

         echo  $output;
      //세션변수 생성.


    }
  }
  //에러 발생시
  else
  {
   echo "mysql_errno($connect)";
  }
}

?>
