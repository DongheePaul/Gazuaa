<?php
if($_FILES['file']['name']){
  $title = $_POST['title'];
      //파일명
      $org_file = $_FILES['file']['name'];
      $temp_mem = explode(".",$org_file);
      //확장자
      $file_expt = $temp_mem[1];
      //진행상태변수
      $flow_state = "0";
      //return변수
      $message = "";

      //파일명확인을 위한 변수
      $filename_fix_yn = "0";
      //저장하기 위한 파일명 변수
      $fix_filename = "";
      //파일명 확장을 위한 변수
      $fix_i = 0;
      //3. 파일명 중복검사및처리
      if($flow_state == "0"){
        $fix_filename = $temp_mem[0];
        while($filename_fix_yn == "0")
        {
          $fix_i++;
          if(file_exists( $save_root.$fix_filename . "." . $file_expt)){
            $fix_filename = $temp_mem[0] . "(" . $fix_i . ")";
            $filename_fix_yn = "0";
          }else{
            $filename_fix_yn = "1";
            $fix_filename = $fix_filename . "." . $file_expt;
          }
        }
      }
      move_uploaded_file($_FILES['file']['tmp_name'], $save_root.$fix_filename);
      echo $save_root.$fix_filename."++ title =".$title;
     }

 ?>
