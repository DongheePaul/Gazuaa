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

//글쓴이
$writer = $_SESSION['email'];
//게시글 제목
 $title = $_POST[title];
  //게시글 내용
 $content = $_POST[content];

//게시판에 올릴 사진을 저장할 디렉토리명 : board_image
$file_path = "board_image/";

$date = date("Y-m-d H:i:s");
//$file_path : 경로/파일명
//업로드할 사진파일이 있다면 => 사진파일 업로드 & 파일명 db에 저장.
if($_FILES['file']['name']){
  $file_name = basename($_FILES['file']['name']);
  echo $file_name."= file basename //";
  $file_path = $file_path.$file_name;
  echo $file_path."= file path //";

//서버에 이미지 파일 업로드 성공시
   if(move_uploaded_file($_FILES['file']['tmp_name'], $file_path)){
     $sql = "insert into board (id, title, content, board_image_dir, time, writer) values ('0', '$title','$content', '$file_path', '$date', '$writer')";
     $result = mysql_query($sql, $connect);
        if(!$result){
     		die('Could not query:' . mysql_error());
          echo "fail //";
          echo die('Could not query:' . mysql_error());
     	}
     	else{
     		echo "upload success".$file_path." <- file path//";
     	}

   } else{
 		if($_FILES['file']['error'] > 0){
 	echo '{result: -1, ';
 	//오류 타입에 따라 echo 'msg: "오류종류"}';
 	switch ($_FILES['uploaded_file']['error']){
 	case 1: echo 'msg: "upload_max_filesize 초과"}';break;
 	case 2: echo 'msg: "max_file_size 초과"}';break;
 	case 3: echo 'msg: "파일이 부분만 업로드됐습니다."}';break;
 	case 4: echo 'msg: "파일을 선택해 주세요."}';break;
 	case 6: echo 'msg: "임시 폴더가 존재하지 않습니다."}';break;
 	case 7: echo 'msg: "임시 폴더에 파일을 쓸 수 없습니다. 퍼미션을 살펴 보세요."}';break;
 	case 8: echo 'msg: "확장에 의해 파일 업로드가 중지되었습니다."}';break;
 		}
 	}
 		echo "file upload fail".$_FILES['file']['error'];
 	}
}
//업로드할 사진파일이 있다면 => 사진파일 업로드 & 파일명, 게시글 제목, 게시글 내용 db에 저장.
//업로드할 사진파일이 있다면 => 사진파일 업로드 & 파일명 db에 저장.
//끝

//업로드할 사진파일이 없다면 => 게시물 내용, 제목만 db에 저장.
// else{
// $sql = "insert into board (id, title, content) values ('0', '$title', '$content')";
// $result=mysql_query($sql, $connect) or die(mysql_error());
//
// echo $result;
// echo $title;
// echo $content;
// }
mysql_close($connect);
?>
