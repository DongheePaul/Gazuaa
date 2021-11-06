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

for($i = 0; $i<15; $i++){
     $sql = "insert into board (id, title, time, writer) values ('0', 'title+$i','time+$i', 'writer+$i')";
     $result = mysql_query($sql, $connect);
        if(!$result){
     		die('Could not query:' . mysql_error());
          echo "fail //";
          echo die('Could not query:' . mysql_error());
     	}
     	else{
     		echo "upload success".$file_path." <- file path//";
     	}
}

mysql_close($connect);

?>
