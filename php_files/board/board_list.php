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

$load_start = $_POST['load_start_num'];


//게시물 목록을 처음 생성할 때
//최신 5개의 게시물에 대한 정보를 보내준다.
//if($load_start == 1){
$sql = "select id, title, time, writer from board order by id desc limit 10";
$result = mysql_query($sql, $connect);
   if(!$result){
   die('Could not query:' . mysql_error());
     echo "fail";
     echo die('Could not query:' . mysql_error());
 }
 else{
 }

//글번호를 받을 리스트 생성
//다음 페이지를 불러 올 때 글 번호로 불러오는데
//글번호 중 가장 작은 수를 변수로 저장한다.
  $id_list[] = array();
  $title_list[] = array();
  $time_list[] = array();
  $writer_list[] = array();
while($row = mysql_fetch_assoc($result)){

  $id = $row['id'];
  $title = $row['title'];
  $time = $row['time'];
  $writer = $row['writer'];
  //글번호를 id list에 집어넣는다. while문 끝나면 이 리스트에서
  //가장 작은 수를 찾는다. -> 다음 페이지의 첫 게시물 찾기 위해.
array_push($id_list, $id);
array_push($title_list, $title);
array_push($time_list, $time);
array_push($writer_list, $writer);
}

$id_list_smallest = min($id_list);

for($i = 1; $i<11; $i++){
  $id1=$id_list[$i];
  $title1=$title_list[$i];
  $time1=$time_list[$i];
  $writer1=$writer_list[$i];
  $j = $i-1;
  $json_object[$j] = array('id'=>$id1, 'title'=>$title1, 'smallest'=>$id_list_smallest, 'writer'=>$writer1, 'time'=>$time1);
}

echo json_encode($json_object);





//}

//게시물 목록의 첫번째 페이지가 아닌 페이지
// else{
//   $offset_num = $load_start+4;
//   $sql = "select id, title from board order by id desc limit ".$load_start." offset ".$offset_num;
//   $result = mysql_query($sql, $connect);
//      if(!$result){
//      die('Could not query:' . mysql_error());
//        echo "fail //";
//        echo die('Could not query:' . mysql_error());
//    }
//    else{
//      echo "o";
//    }
// }
mysql_close($connect);
 ?>
