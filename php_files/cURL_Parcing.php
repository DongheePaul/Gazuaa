<?php
 header('Content-Type: application/json');
//정보를 요청할 코인들 리스트
//
$coinlist = array("BTC","ETH","QTUM", "ADA", "XRP", "NEO", "XLM", "BCC",  "ETC", "OMG");
//각 코인의 정보를 요청하는 포문
for ($i=0; $i <10 ; $i++) {
  $url = "https://crix-api-endpoint.upbit.com/v1/crix/candles/days?code=CRIX.UPBIT.KRW-".$coinlist[$i];
  //cURL 시작
  $ch = curl_init();
  //url 설정
  curl_setopt($ch, CURLOPT_URL, $url);
  //string 형태로 변환
  curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
  //string 형태로 변환된 리턴값을 $output 변수에 저장한다
  $output = curl_exec($ch);
  //코인별의 리턴값을 $output1에 저장.
  $output1 = $output[$i];
  //cURL 종료
  curl_close($ch);
  //jsonArray 형태의 리턴값을 php 배열 형태로 변환한다.
   $output1 = json_decode($output, true);
   //키값을 통해 밸류 값을 찾는다.
   foreach ($output1 as $value) {
     //코인 이름
     $code = strstr($value['code'], '-');
     //코인 가격
     $tradePrice = $value['tradePrice'];
     //코인 가격 변화액
     $signedChangePrice = $value['signedChangePrice'];
     //코인 가격 변화율
     $signedChangeRate = $value['signedChangeRate'];
     //jsonObject 형태로 바꾼다.
     $json_object[$i] = array('code'=> $code, 'tradePrice'=> $tradePrice, 'signedChangePrice' =>$signedChangePrice, 'signedChangeRate'=>$signedChangeRate);


   }

}
  echo json_encode($json_object);

?>
