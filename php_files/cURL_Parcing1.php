<?php
 header('Content-Type: application/json');
//, "NEO", "XLM", "BCC",  "ETC", "OMG"
$coinlist = array("BTC","ETH","QTUM", "ADA", "XRP");
for ($i=0; $i <5 ; $i++) {
  $url = "https://crix-api-endpoint.upbit.com/v1/crix/candles/days?code=CRIX.UPBIT.KRW-".$coinlist[$i];
  $ch = curl_init();

  curl_setopt($ch, CURLOPT_URL, $url);
  curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

  $output = curl_exec($ch);
  $output1 = $output[$i];

  $info = curl_getinfo($ch);

  curl_close($ch);

  echo $output;

}

?>
